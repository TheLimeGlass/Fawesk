package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.doc.Examples
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser.ParseResult
import ch.njol.skript.util.AsyncEffect
import ch.njol.util.Kleenean
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Location
import org.bukkit.event.Event
import java.io.FileInputStream

@Examples(
	"paste schematic \"example.schematic\" at location",
	"paste schematic \"example.sponge\" at location using sponge v3",
	"paste schematic \"example.schem\" at location using fast v3 excluding air excluding entities"
)
class EffPasteSchematic : AsyncEffect() {

	companion object {
		init {
			Skript.registerEffect(
				EffPasteSchematic::class.java,
				"paste schematic %string% at %location% " +
					"[using [clipboard] [format] %-clipboardformat/builtinclipboardformat%] " +
					"[air:exclud[ing|e] air] [entities:exclud[ing|e] entities]"
			)
		}
	}

	private var format: Expression<ClipboardFormat>? = null
	private lateinit var location: Expression<Location>
	private lateinit var name: Expression<String>
	private var entities: Boolean = true
	private var air: Boolean = true

	@Suppress("UNCHECKED_CAST")
	override fun init(
		expressions: Array<out Expression<*>?>?,
		matchedPattern: Int,
		isDelayed: Kleenean?,
		parseResult: ParseResult
	): Boolean {
		name = expressions?.get(0) as Expression<String>
		location = expressions[1] as Expression<Location>
		format = expressions[2] as? Expression<ClipboardFormat>
		entities = !parseResult.hasTag("entities")
		air = !parseResult.hasTag("air")
		return true
	}

	override fun execute(event: Event?) {
		val name = name.getSingle(event)
		if (name == null || !name.contains(".")) {
			Skript.error("Schematic name must contain a file extension. ${name ?: return} or the name provided was null.")
			return
		}
		val file = WorldEdit.getInstance().schematicsFolderPath.resolve(name).toFile()
		val location = location.getSingle(event) ?: return
		val vector = BlockVector3.at(location.blockX, location.blockY, location.blockZ)
		val world = BukkitWorld(location.world ?: return)
		if (!file.exists()) {
			Skript.error("Schematic file not found. $name")
			return
		}

		val format = EffPasteSchematic@ format?.getSingle(event) ?: ClipboardFormats.findByFile(file)
		?: BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
		format.getReader(FileInputStream(file)).use { reader ->
			val clipboard = reader?.read() ?: return
			clipboard.paste(world, vector, false, air, entities, null)
		}
	}

	override fun toString(event: Event?, debug: Boolean): String? {
		return "paste schematic ${name.toString(event, debug)} at ${location.toString(event, debug)}"
	}

}
