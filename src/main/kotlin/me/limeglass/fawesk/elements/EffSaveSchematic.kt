package me.limeglass.fawesk.elements

import ch.njol.skript.Skript
import ch.njol.skript.lang.Effect
import ch.njol.skript.lang.Expression
import ch.njol.skript.lang.SkriptParser
import ch.njol.util.Kleenean
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import org.bukkit.Location
import org.bukkit.event.Event
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths

class EffSaveSchematic : Effect() {

    companion object {
        init {
            Skript.registerEffect(EffSaveSchematic::class.java,
                "(save|create) schematic %string% from [pos1] %location% [to] [pos2] %location% [with origin %-location%]"
            )
        }
    }

    private var origin: Expression<Location>? = null
    private lateinit var pos1: Expression<Location>
    private lateinit var pos2: Expression<Location>
    private lateinit var name: Expression<String>

    @Suppress("UNCHECKED_CAST")
    override fun init(
        expressions: Array<out Expression<*>?>?,
        matchedPattern: Int,
        isDelayed: Kleenean?,
        parseResult: SkriptParser.ParseResult
    ): Boolean {
        name = expressions?.get(0) as Expression<String>
        pos1 = expressions[1] as Expression<Location>
        pos2 = expressions[2] as Expression<Location>
        origin = expressions[3] as? Expression<Location>
        return true
    }

    override fun execute(event: Event?) {
        val file = WorldEdit.getInstance().schematicsFolderPath.resolve((name.getSingle(event) ?: return) + ".sponge").toFile()
        if (file.exists()) Files.delete(Paths.get(file.path))

        val position1 = pos1.getSingle(event) ?: return
        val position2 = pos2.getSingle(event) ?: return
        val pos1 = BlockVector3.at(position1.blockX, position1.blockY, position1.blockZ)
        val pos2 = BlockVector3.at(position2.blockX, position2.blockY, position2.blockZ)
        val origin = origin?.getSingle(event)?.let { BlockVector3.at(it.blockX, it.blockY, it.blockZ) }
        val world = BukkitWorld(position1.world)
        val region = CuboidRegion(world, pos1, pos2)
        val clipboard = BlockArrayClipboard(region)
        origin?.let { clipboard.origin = it }
        val forwardExtentCopy = ForwardExtentCopy(world, region, clipboard, region.minimumPoint)
        Operations.complete(forwardExtentCopy)

        BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC.getWriter(FileOutputStream(file)).use { it.write(clipboard) }
        //clipboard.save(file, BuiltInClipboardFormat.SPONGE_V3_SCHEMATIC)
    }

    override fun toString(event: Event?, debug: Boolean): String? {
        return "save schematic ${name.toString(event, debug)} from pos1 ${pos1.toString(event, debug)} to pos2 ${pos2.toString(event, debug)}" +
                (origin?.let { "with origin ${origin?.toString(event, debug)}"} ?: "")
    }

}
