package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.doc.Description
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
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.event.Event
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture

@Description(
	"This expression retrieves blocks from WorldEdit schematics, allowing you to filter and manipulate them.",
	"You can specify a clipboard format and apply conditions to filter the blocks.",
	"If the vectors as locations option is used, the blocks will be returned with their BlockState locations set to the vector within the clipboard."
)
class ExprSchematicBlocks : SimpleExpression<Block>(), InputSource {

	companion object {
		init {
			Skript.registerExpression(
				ExprSchematicBlocks::class.java, Block::class.java, ExpressionType.SIMPLE,
				"[the] (worldedit|fawe) blocks of schematic[s] %strings% [[with] origin %-location%] [using [clipboard] [format] %-clipboardformat/builtinclipboardformat%] (where|that match) \\[<.+>\\]",
				"[the] (worldedit|fawe) blocks of schematic[s] %strings% [[with] origin %-location%] [using [clipboard] [format] %-clipboardformat/builtinclipboardformat%]",
			)
			if (!ParserInstance.isRegistered(InputData::class.java))
				ParserInstance.registerData(InputData::class.java) { InputData(ParserInstance.get()) }
		}
	}

	private val dependentInputs = mutableSetOf<ExprInput<*>>()
	private var format: Expression<ClipboardFormat>? = null
	private lateinit var schematics: Expression<String>
	private var origin: Expression<Location>? = null
	private var filterCondition: Condition? = null
	private var unparsedCondition: String? = null
	private var currentValue: Any? = null

	@Suppress("UNCHECKED_CAST")
	override fun init(
		expressions: Array<out Expression<*>?>,
		matchedPattern: Int,
		isDelayed: Kleenean?,
		parseResult: ParseResult
	): Boolean {
		schematics = expressions[0] as Expression<String>
		origin = expressions[1] as Expression<Location>?
		format = expressions[2] as? Expression<ClipboardFormat>
		if (matchedPattern == 1) return true

		unparsedCondition = parseResult.regexes[0].group()
		val inputData = parser.getData(InputData::class.java).apply { source = this@ExprSchematicBlocks }
		filterCondition = Condition.parse(unparsedCondition, "Can't understand this condition: $unparsedCondition")
		inputData.source = this
		return filterCondition != null
	}

	private fun getClipboards(event: Event?) = schematics.getArray(event).mapNotNull { schematicName ->
		val name = schematicName.trim()
		if (name.isEmpty()) {
			Skript.error("Schematic name cannot be empty.")
			return@mapNotNull null
		}
		val file = WorldEdit.getInstance().schematicsFolderPath.resolve(name).toFile()
		if (!file.exists()) {
			Skript.error("Schematic file $name does not exist.")
			return@mapNotNull null
		}
		val format =
			format?.getSingle(event) ?: ClipboardFormats.findByFile(file) ?: BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
		format.getReader(FileInputStream(file))?.read()
	}.toList()

	override fun iterator(event: Event?): Iterator<Block>? {
		val origin = this@ExprSchematicBlocks.origin?.getSingle(event)
		return CompletableFuture.supplyAsync {
			val clipboards = getClipboards(event)
			val stream = clipboards.flatMap { clipboard ->
				clipboard.region?.map { vector -> clipboard to BlockVector3.at(vector.x(), vector.y(), vector.z()) }
					?: emptyList()
			}.stream().parallel()

			stream.map { (clipboard, vector) ->
				var blockState = BukkitAdapter.adapt(clipboard.getBlock(vector)).createBlockState()
				val clipboardOrigin = clipboard.origin ?: clipboard.minimumPoint
				origin?.let {
					val offset = Location(
						it.world,
						(vector.x() - clipboardOrigin.x()).toDouble(),
						(vector.y() - clipboardOrigin.y()).toDouble(),
						(vector.z() - clipboardOrigin.z()).toDouble()
					)
					val location = it.clone().apply {
						x += offset.x
						y += offset.y
						z += offset.z
					}
					blockState = blockState.copy(location)
				}
				BlockStateBlock(blockState)
			}.filter {
				filterCondition?.let {
					currentValue = it
					it.check(event)
				} != false
			}.toList().iterator()
		}.get()
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
	override fun getCurrentValue(): Any? = currentValue
	override fun isSingle(): Boolean = false

}
