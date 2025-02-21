package me.limeglass.worldeditaliases

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import kotlin.collections.component1
import kotlin.collections.component2

object CommandSenderExt {
    lateinit var adventure: BukkitAudiences
    lateinit var config: YamlConfiguration
    lateinit var miniMessage: MiniMessage
}

fun CommandSender.sendTranslated(node: String, parameters: (MutableMap<String, Component>.() -> Unit)? = null) {
    CommandSenderExt.config.getString("messages.$node")?.let {
        val parametersMap = mutableMapOf<String, Component>().also { parameters?.invoke(it) }
        val translatedParams = parametersMap.map { (key, value) -> Placeholder.component(key.lowercase(), value) }
        val component = CommandSenderExt.miniMessage.deserialize(it, TagResolver.resolver(translatedParams))
        CommandSenderExt.adventure.sender(this).sendMessage(component)
    }
}
