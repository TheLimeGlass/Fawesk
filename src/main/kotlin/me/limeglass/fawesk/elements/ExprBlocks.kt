package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.expressions.ExprInput
import ch.njol.skript.lang.Condition
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.ExpressionType
import ch.njol.skript.lang.InputSource
import ch.njol.skript.lang.InputSource.InputData
import ch.njol.skript.lang.SkriptParser
import ch.njol.skript.lang.parser.ParserInstance
import ch.njol.skript.lang.util.SimpleExpression
import ch.njol.skript.util.BlockStateBlock
import ch.njol.util.Kleenean
import com.google.common.collect.Iterators
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Location
import org.bukkit.event.Event
import java.util.concurrent.CompletableFuture
import com.sk89q.worldedit.regions.CuboidRegion
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.bukkit.BukkitWorld
import org.bukkit.block.Block

class ExprBlocks : SimpleExpression<Block>(), InputSource {

    companion object {
        init {
            Skript.registerExpression(ExprBlocks::class.java, Block::class.java, ExpressionType.SIMPLE,
                "(worldedit|fawe) blocks (within|from) %location% to %location% (where|that match) \\[<.+>\\]",
                "(worldedit|fawe) blocks (within|from) %location% to %location%"
            )
            if (!ParserInstance.isRegistered(InputData::class.java))
                ParserInstance.registerData(InputData::class.java) { InputData(ParserInstance.get()) }
        }
    }

    private val dependentInputs = mutableSetOf<ExprInput<*>>()
    private lateinit var location1: Expression<Location>
    private lateinit var location2: Expression<Location>
    private var filterCondition: Condition? = null
    private var unparsedCondition: String? = null
    private var currentValue: Any? = null
    private var air: Boolean = false

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        location1 = expressions[0] as Expression<Location>
        location2 = expressions[1] as Expression<Location>
        if (matchedPattern != 0) return true

        unparsedCondition = parseResult.regexes[0].group()
        val inputData = parser.getData(InputData::class.java).apply { source = this@ExprBlocks }
        filterCondition = Condition.parse(unparsedCondition, "Can't understand this condition: $unparsedCondition")
        inputData.source = this
        return filterCondition != null
    }

    override fun iterator(event: Event?): Iterator<Block>? {
        val location1 = location1.getSingle(event) ?: return null
        val location2 = location2.getSingle(event) ?: return null
        if (location1.world != location2.world) return null
        val world = location1.world
        val pos1 = BlockVector3.at(location1.blockX, location1.blockY, location1.blockZ)
        val pos2 = BlockVector3.at(location2.blockX, location2.blockY, location2.blockZ)
        return CompletableFuture.supplyAsync {
            val region = CuboidRegion(pos1, pos2)
            val editSession = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitWorld(world)).build()
            region.mapNotNull { vector ->
                val block = editSession.getBlock(vector)
                if (air && block.isAir) return@mapNotNull null
                val location = BukkitAdapter.adapt(world, vector)
                val blockState = BukkitAdapter.adapt(block).createBlockState().copy(location)
                BlockStateBlock(blockState)
            }.filter {
                currentValue = it
                filterCondition?.check(event) == true
            }.toMutableList().iterator()
        }.get()
    }

    override fun toString(e: Event?, debug: Boolean): String =
        "fawe blocks from ${location1.toString(e, debug)} to ${location2.toString(e, debug)}" +
                (filterCondition?.let { " where ${it.toString(e, debug)}" } ?: "")

    // bloat
    override fun get(event: Event?): Array<Block>? = iterator(event)?.let { Iterators.toArray(it, Block::class.java) }
    override fun getDependentInputs(): Set<ExprInput<*>> = dependentInputs
    override fun getReturnType(): Class<out Block> = Block::class.java
    override fun getCurrentValue(): Any? = currentValue
    override fun isSingle(): Boolean = false

}
