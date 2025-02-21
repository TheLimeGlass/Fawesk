package me.limeglass.worldeditaliases

import com.sk89q.worldedit.command.util.SuggestionHelper
import com.sk89q.worldedit.world.block.BlockType
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.suggestion.Suggestions
import org.incendo.cloud.context.CommandContext

class Commands(
    private val aliases: MutableMap<String, String>,
    private val instance: WorldEditAliases
) {

    private companion object {
        private const val COMMAND_STRING = "worldeditaliases|wea"
        private const val BLOCK_SUGGESTIONS = "block-suggestions"
    }

    @Suggestions(BLOCK_SUGGESTIONS)
    fun blockSuggestions(source: CommandContext<CommandSender>, input: String): List<String> =
        SuggestionHelper.getNamespacedRegistrySuggestions(BlockType.REGISTRY, input).toList()

    @Command("$COMMAND_STRING")
    @CommandDescription("Base command for WorldEditAliases.")
    fun base(context: CommandContext<CommandSender>) {
        context.sender().sendTranslated("command.aliases-help")
    }

    @Command("$COMMAND_STRING add <block> <alias>")
    @CommandDescription("Add an alias for a block.")
    @Permission("worldeditaliases.command.add")
    fun command(
        context: CommandContext<CommandSender>,
        @Argument("block", suggestions = BLOCK_SUGGESTIONS) block: String,
        @Argument("alias") alias: String
    ) {
        if (aliases.containsKey(alias)) {
            context.sender().sendTranslated("command.aliases-exists") {
                "alias" to Component.text(alias)
            }
            return
        }
        aliases[alias] = block
        instance.saveAliasesToConfig()
        context.sender().sendTranslated("command.aliases-added") {
            "alias" to Component.text(alias)
            "block" to Component.text(block)
        }
    }

    @Command("$COMMAND_STRING remove <alias>")
    @CommandDescription("Remove an alias.")
    @Permission("worldeditaliases.command.remove")
    fun remove(context: CommandContext<CommandSender>, @Argument("alias") alias: String) {
        if (!aliases.containsKey(alias)) {
            context.sender().sendTranslated("command.aliases-not-exists") {
                "alias" to Component.text(alias)
            }
            return
        }
        aliases.remove(alias)
        instance.saveAliasesToConfig()
        context.sender().sendTranslated("command.aliases-removed") {
            "alias" to Component.text(alias)
        }
    }

    @Command("$COMMAND_STRING list")
    @CommandDescription("List all aliases.")
    @Permission("worldeditaliases.command.list")
    fun list(context: CommandContext<CommandSender>) {
        aliases.forEach { (alias, block) ->
            context.sender().sendTranslated("command.aliases-list-entry") {
                "alias" to Component.text(alias)
                "block" to Component.text(block)
            }
        }
    }
}
