package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.doc.Examples
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser.ParseResult
import ch.njol.skript.util.AsyncEffect
import ch.njol.skript.util.Task
import ch.njol.util.Kleenean
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.Location
import org.bukkit.event.Event
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

@Examples(
    "save schematic \"example.schematic\" from {pos1} to {pos2}",
    "save schematic \"example.sponge\" from {pos1} to {pos2} using sponge v3",
    "save schematic \"example.schem\" from {pos1} to {pos2} with origin player's location",
    "save schematic \"example.schematic\" from {pos1} to {pos2} using fast v3 with origin player's location including entities and including biomes"
)
class EffSaveSchematic : AsyncEffect() {

    companion object {
        init {
            Skript.registerEffect(EffSaveSchematic::class.java,
                "(save|create) schematic %string% from [pos1] %location% [to] [pos2] %location% " +
                        "[using [clipboard] [format] %-clipboardformat/builtinclipboardformat%] " +
                        "[with origin %-location%] [entities:includ[ing|e] entities] [biome:[and] includ[ing|e] biome[s]]"
            )
        }
    }

    private var format: Expression<ClipboardFormat>? = null
    private var origin: Expression<Location>? = null

    private lateinit var pos1: Expression<Location>
    private lateinit var pos2: Expression<Location>
    private lateinit var name: Expression<String>

    private var entities = false
    private var biome = false

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: ParseResult
    ): Boolean {
        name = expressions?.get(0) as Expression<String>
        pos1 = expressions[1] as Expression<Location>
        pos2 = expressions[2] as Expression<Location>
        format = expressions[3] as? Expression<ClipboardFormat>
        origin = expressions[4] as? Expression<Location>
        entities = parseResult.hasTag("entities")
        biome = parseResult.hasTag("biome")
        return true
    }

    override fun execute(event: Event?) {
        val name = name.getSingle(event)
        if (name == null || !name.contains(".")) {
            Skript.error("Schematic name must contain a file extension. ${name ?: return} or the name provided was null.")
            return
        }
        val file = WorldEdit.getInstance().schematicsFolderPath.resolve(name).toFile()
        if (file.exists()) Files.delete(Paths.get(file.path))

        val position1 = pos1.getSingle(event) ?: return
        val position2 = pos2.getSingle(event) ?: return
        val bukkitWorld = position1.world ?: return
        if (bukkitWorld != position2.world) {
            Skript.error("The two positions must be in the same world.")
            return
        }
        val pos1 = BlockVector3.at(position1.blockX, position1.blockY, position1.blockZ)
        val pos2 = BlockVector3.at(position2.blockX, position2.blockY, position2.blockZ)
        val origin = origin?.getSingle(event)?.let { BlockVector3.at(it.blockX, it.blockY, it.blockZ) }
        val world = BukkitWorld(position1.world)
        val region = CuboidRegion(world, pos1, pos2)
        val clipboard = BlockArrayClipboard(region)
        val forwardExtentCopy = ForwardExtentCopy(world, region, clipboard, region.minimumPoint)
        forwardExtentCopy.isCopyingBiomes = biome
        origin?.let { clipboard.origin = it }
        if (entities) {
            forwardExtentCopy.isCopyingEntities = true
            region.chunks.parallelStream().forEach {
                Task.callSync { bukkitWorld.getChunkAt(it.x(), it.z()).load() }
            }
        }

        Operations.complete(forwardExtentCopy)
        val format = format?.getSingle(event) ?: ClipboardFormats.findByFile(file) ?: BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC
        format.getWriter(FileOutputStream(file)).use { it.write(clipboard) }
    }

    override fun toString(event: Event?, debug: Boolean): String? {
        return "save schematic ${name.toString(event, debug)} from pos1 ${pos1.toString(event, debug)} to pos2 ${pos2.toString(event, debug)}" +
                (origin?.let { "with origin ${origin?.toString(event, debug)}"} ?: "")
    }

}
