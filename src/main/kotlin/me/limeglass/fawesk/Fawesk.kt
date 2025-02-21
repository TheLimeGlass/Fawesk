package me.limeglass.fawesk

import ch.njol.skript.Skript
import org.bukkit.plugin.java.JavaPlugin

class Fawesk : JavaPlugin() {

    override fun onEnable() {
        Skript.registerAddon(this)
            .loadClasses("me.limeglass.skriptfawe", "elements")
            //.setLanguageFileDirectory("lang")
    }

}
