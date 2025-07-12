package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.classes.Changer.ChangeMode
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
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.event.Event
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture

class ExprSchematicBlocks : SimpleExpression<Block>(), InputSource {

    companion object {
        init {
            Skript.registerExpression(ExprSchematicBlocks::class.java, Block::class.java, ExpressionType.SIMPLE,
                "[the] [:parallel] (worldedit|fawe) blocks of schematic[s] %strings% [using [clipboard] [format] %-clipboardformat/builtinclipboardformat%]",
                "[the] [:parallel] (worldedit|fawe) blocks of schematic[s] %strings% (where|that match) \\[<.+>\\]",
            )
            if (!ParserInstance.isRegistered(InputData::class.java))
                ParserInstance.registerData(InputData::class.java) { InputData(ParserInstance.get()) }
        }
    }

    private val dependentInputs = mutableSetOf<ExprInput<*>>()
    private var format: Expression<ClipboardFormat>? = null
    private lateinit var schematics: Expression<String>
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
        schematics = expressions[0] as Expression<String>
        if (matchedPattern == 0) {
            format = expressions[1] as? Expression<ClipboardFormat>
            return true
        }

        unparsedCondition = parseResult.regexes[0].group()
        val inputData = parser.getData(InputData::class.java).apply { source = this@ExprSchematicBlocks }
        filterCondition = Condition.parse(unparsedCondition, "Can't understand this condition: $unparsedCondition")
        inputData.source = this
        return filterCondition != null
    }

    private fun getClipboards(event: Event?) = schematics.stream(event)?.map { schematicName: String ->
            val name = schematicName.trim()
            if (name.isEmpty()) {
                Skript.error("Schematic name cannot be empty.")
                return@map null
            }

            val file = WorldEdit.getInstance().schematicsFolderPath.resolve(name).toFile()
            if (!file.exists()) {
                Skript.error("Schematic file $name does not exist.")
                return@map null
            }

            val format = this@ExprSchematicBlocks.format?.getSingle(event)
                ?: ClipboardFormats.findByFile(file)
                ?: BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
            format.getReader(FileInputStream(file))?.read() ?: return@map null
        }?.toList() ?: arrayListOf()

    override fun iterator(event: Event?): Iterator<Block>? {
        return CompletableFuture.supplyAsync {
            val clipboards = getClipboards(event)
            val stream = clipboards.flatMap { clipboard ->
                clipboard?.region?.map { vector -> clipboard to BlockVector3.at(vector.x(), vector.y(), vector.z()) }
                    ?: emptyList()
            }.stream().apply { if (parallel) parallel() }

            stream.map { (clipboard, vector) ->
                val worldEditBlockState = clipboard.getBlock(vector)
                val blockState = BukkitAdapter.adapt(worldEditBlockState).createBlockState()
                BlockStateBlock(blockState)
            }.filter {
                filterCondition?.let {
                    currentValue = it
                    it.check(event)
                } != false
            }.toList().iterator()
        }.get()
    }

    override fun acceptChange(change: ChangeMode): Array<out Class<*>?>? =
        if (change == ChangeMode.SET) arrayOf(BlockData::class.java) else null

    override fun change(event: Event?, delta: Array<out Any?>?, mode: ChangeMode) {
        var blockData = delta?.get(0) as BlockData
        CompletableFuture.supplyAsync {
            val clipboards = getClipboards(event)
            val stream = clipboards.flatMap { clipboard ->
                clipboard?.region?.map { vector -> clipboard to BlockVector3.at(vector.x(), vector.y(), vector.z()) }
                    ?: emptyList()
            }.stream().apply { if (parallel) parallel() }

            stream.forEach { (clipboard, vector) ->
                val worldEditBlockState = clipboard.getBlock(vector)
                val blockState = BukkitAdapter.adapt(worldEditBlockState).createBlockState()
                val skriptBlockState = BlockStateBlock(blockState)
                filterCondition?.let {
                    currentValue = skriptBlockState
                    if (!it.check(event)) return@forEach
                }
                clipboard.setBlock(vector.x(), vector.y(), vector.z(), BukkitAdapter.adapt(blockData))
            }
        }
    }

    override fun toString(e: Event?, debug: Boolean): String {
        return "worldedit blocks of schematics ${schematics.toString(e, debug)}" +
                (if (format != null) " using clipboard format ${format!!.toString(e, debug)}" else "") +
                (if (unparsedCondition != null) " where $unparsedCondition" else "")
    }

    // bloat
    override fun get(event: Event?): Array<Block>? = iterator(event)?.let { Iterators.toArray(it, Block::class.java) }
    override fun getDependentInputs(): Set<ExprInput<*>> = dependentInputs
    override fun getReturnType(): Class<out Block> = Block::class.java
    override fun isSingle(): Boolean = schematics.isSingle
    override fun getCurrentValue(): Any? = currentValue

}
