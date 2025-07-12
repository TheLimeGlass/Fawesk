package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.classes.Changer.ChangeMode
import ch.njol.skript.doc.Examples
import ch.njol.skript.expressions.ExprInput
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.InputSource
import ch.njol.skript.lang.InputSource.InputData
import ch.njol.skript.lang.SkriptParser.ParseResult
import ch.njol.skript.lang.parser.ParserInstance
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.skript.util.BlockStateBlock
import ch.njol.util.Kleenean
import com.google.common.collect.Iterators
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.world.block.BlockTypes
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.event.Event
import java.util.concurrent.CompletableFuture

@Examples(
    "parallel set fawe blocks from {test1} to {test2} where [block input is not air] to air # Use parallel for unordered large updates",
    "set fawe blocks from {test1} to {test2} to wheat[age=5]",
    "loop fawe blocks within {test1} to {test2} where [block input is a diamond block or a grass block] to air"
)
class ExprBlocks : SimpleExpression<Block>(), InputSource {

    companion object {
        init {
            Skript.registerExpression(ExprBlocks::class.java, Block::class.java, ExpressionType.SIMPLE,
                "[:parallel] (worldedit|fawe) (blocks|things) (within|from) %location% (to|and) %location%",
                "[:parallel] (worldedit|fawe) (blocks|things) (within|from) %location% (to|and) %location% (where|that match) \\[<.+>\\]",
                "[the] [:parallel] (worldedit|fawe) (block|thing)[s] [at] %locations% (where|that match) \\[<.+>\\]",
                "[the] [:parallel] (worldedit|fawe) (block|thing)[s] [at] %locations%"
            )
            if (!ParserInstance.isRegistered(InputData::class.java))
                ParserInstance.registerData(InputData::class.java) { InputData(ParserInstance.get()) }
        }
    }

    private val dependentInputs = mutableSetOf<ExprInput<*>>()
    private var location1: Expression<Location>? = null
    private var location2: Expression<Location>? = null
    private var locations: Expression<Location>? = null
    private var filterCondition: Condition? = null
    private var unparsedCondition: String? = null
    private var currentValue: Any? = null
    private var parallel: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: ParseResult
    ): Boolean {
        parallel = parseResult.hasTag("parallel")
        if (matchedPattern < 2) {
            location1 = expressions[0] as Expression<Location>
            location2 = expressions[1] as Expression<Location>
            if (matchedPattern == 0) return true
        } else {
            locations = expressions[0] as Expression<Location>
            if (matchedPattern == 3) return true
        }

        unparsedCondition = parseResult.regexes[0].group()
        val inputData = parser.getData(InputData::class.java).apply { source = this@ExprBlocks }
        filterCondition = Condition.parse(unparsedCondition, "Can't understand this condition: $unparsedCondition")
        inputData.source = this
        return filterCondition != null
    }

    override fun iterator(event: Event?): Iterator<Block>? {
        return CompletableFuture.supplyAsync {
            val vectors = mutableListOf<BlockVector3>()
            val world: World
            if (locations != null) {
                val locations = locations!!.getArray(event)
                if (locations.any { it.world == null }) return@supplyAsync null
                world = locations.all { it.world == locations.first().world }.let {
                    locations.first().world!!
                }
                vectors.addAll(locations.map { BlockVector3.at(it.blockX, it.blockY, it.blockZ) })
            } else {
                val loc1 = location1!!.getSingle(event) ?: return@supplyAsync null
                val loc2 = location2!!.getSingle(event) ?: return@supplyAsync null
                if (loc1.world == null || loc2.world == null) return@supplyAsync null
                if (loc1.world != loc2.world) return@supplyAsync null
                world = loc1.world!!
                val pos1 = BlockVector3.at(loc1.blockX, loc1.blockY, loc1.blockZ)
                val pos2 = BlockVector3.at(loc2.blockX, loc2.blockY, loc2.blockZ)
                val region = CuboidRegion(pos1, pos2)
                for (vector in region) {
                    vectors.add(BlockVector3.at(vector.x(), vector.y(), vector.z()))
                }
            }

            val editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitWorld(world)).build()
            val stream = vectors.stream()
            if (parallel) stream.parallel()
            stream.map { vector ->
                val block = editSession.getBlock(vector)
                val location = BukkitAdapter.adapt(world, vector)
                val blockState = BukkitAdapter.adapt(block).createBlockState().copy(location)
                BlockStateBlock(blockState)
            }.filter {
                if (filterCondition == null) return@filter true
                currentValue = it
                filterCondition!!.check(event)
            }.toList().iterator()
        }.get()
    }

    override fun acceptChange(change: ChangeMode): Array<out Class<*>?>? =
        if (change == ChangeMode.SET) arrayOf(BlockData::class.java) else null

    override fun change(event: Event?, delta: Array<out Any?>?, mode: ChangeMode) {
        var blockData = delta?.get(0) as BlockData
        CompletableFuture.supplyAsync {
            val world: BukkitWorld = if (locations != null) BukkitWorld(locations!!.getArray(event).first().world) // Properly handled in iterator
            else {
                BukkitWorld(location1!!.getSingle(event)?.world ?: return@supplyAsync)
            }
            val editSession = WorldEdit.getInstance().newEditSessionBuilder().world(world).build()
            val blocks = iterator(event)?.asSequence()?.map {
                BlockVector3.at(it.location.blockX, it.location.blockY, it.location.blockZ)
            }?.toSet() ?: return@supplyAsync
            if (blockData.material.isAir) {
                editSession.setBlocks(blocks, BlockTypes.AIR)
            } else {
                editSession.setBlocks(blocks, BukkitAdapter.adapt(blockData))
            }
            editSession.close()
        }
    }

    override fun toString(e: Event?, debug: Boolean): String {
        if (locations != null) return "fawe blocks ${locations!!.toString(e, debug)}"
        return "fawe blocks from ${location1!!.toString(e, debug)} to ${location2!!.toString(e, debug)}" +
            (filterCondition?.let { " where ${it.toString(e, debug)}" } ?: "")
    }

    // bloat
    override fun get(event: Event?): Array<Block>? = iterator(event)?.let { Iterators.toArray(it, Block::class.java) }
    override fun isSingle(): Boolean = locations != null && locations!!.isSingle
    override fun getDependentInputs(): Set<ExprInput<*>> = dependentInputs
    override fun getReturnType(): Class<out Block> = Block::class.java
    override fun getCurrentValue(): Any? = currentValue

}
