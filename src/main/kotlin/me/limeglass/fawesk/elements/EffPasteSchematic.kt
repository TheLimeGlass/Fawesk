package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.math.BlockVector3
import org.bukkit.Location
import org.bukkit.event.Event
import java.io.FileInputStream
import java.util.concurrent.CompletableFuture

class EffPasteSchematic : Effect() {

    companion object {
        init {
            Skript.registerEffect(EffPasteSchematic::class.java,
                "paste schematic %string% at %location% [air:exclud[ing|e] air] [entities:exclud[ing|e] entities]"
            )
        }
    }

    private lateinit var location: Expression<Location>
    private lateinit var name: Expression<String>
    private var entities: Boolean = true
    private var air: Boolean = true

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        name = expressions?.get(0) as Expression<String>
        location = expressions[1] as Expression<Location>
        entities = !parseResult.hasTag("entities")
        air = !parseResult.hasTag("air")
        return true
    }

    override fun execute(event: Event?) {
        val file = WorldEdit.getInstance().schematicsFolderPath.resolve("${name.getSingle(event)}.sponge").toFile()
        val location = location.getSingle(event) ?: return
        val vector = BlockVector3.at(location.blockX, location.blockY, location.blockZ)
        val world = BukkitWorld(location.world ?: return)
        if (!file.exists()) {
            Skript.error("Schematic file not found. ${name.getSingle(event)}")
            return
        }
        val format = BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
        // TODO define the format with an expression
        //val format = ClipboardFormats.findByFile(file)

        format.getReader(FileInputStream(file)).use { reader ->
            val clipboard = reader?.read() ?: return
            CompletableFuture.runAsync { clipboard.paste(world, vector, false, air, entities, null) }
        }
    }

    override fun toString(event: Event?, debug: Boolean): String? {
        return "paste schematic ${name.toString(event, debug)} at ${location.toString(event, debug)}"
    }

}
