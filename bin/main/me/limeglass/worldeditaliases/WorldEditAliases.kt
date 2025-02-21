package me.limeglass.worldeditaliases

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.extension.input.InputParseException
import com.sk89q.worldedit.extension.input.ParserContext
import com.sk89q.worldedit.internal.registry.InputParser
import com.sk89q.worldedit.world.block.BaseBlock
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.kotlin.coroutines.annotations.installCoroutineSupport
import org.incendo.cloud.paper.PaperCommandManager
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper
import org.incendo.cloud.paper.util.sender.Source

class WorldEditAliases : JavaPlugin() {

    private val ALIASES = mutableMapOf<String, String>()

    override fun onEnable() {
        CommandSenderExt.miniMessage = MiniMessage.miniMessage()
        CommandSenderExt.adventure = BukkitAudiences.create(this)
        val commandManager = PaperCommandManager.builder(PaperSimpleSenderMapper.simpleSenderMapper())
            .executionCoordinator(ExecutionCoordinator.asyncCoordinator())
            .buildOnEnable(this)

        val annotationParser = AnnotationParser(commandManager, Source::class.java)
        annotationParser.installCoroutineSupport()
        annotationParser.parse(Commands(ALIASES, this))

        saveDefaultConfig()
        config.getList("aliases")?.forEach {
            val alias = it as YamlConfiguration
            ALIASES[alias.getString("alias")!!] = alias.getString("block")!!
        }

        val worldedit = WorldEdit.getInstance()
        WorldEdit.getInstance().blockFactory.register(object : InputParser<BaseBlock>(worldedit) {
            @Throws(InputParseException::class)
            override fun parseFromInput(input: String, context: ParserContext): BaseBlock? {
                return null
            }
        })
    }

    override fun onDisable() {
        saveAliasesToConfig()
    }

    fun saveAliasesToConfig() {
        config.set("aliases", ALIASES.map { mapOf("alias" to it.key, "block" to it.value) })
        saveConfig()
    }

}
