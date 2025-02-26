package me.limeglass.fawesk

import ch.njol.skript.Skript
import me.limeglass.fawesk.elements.DefaultClasses
import org.bukkit.plugin.java.JavaPlugin

class Fawesk : JavaPlugin() {

    override fun onEnable() {
        Skript.registerAddon(this)
            .loadClasses("me.limeglass.fawesk", "elements")
            .setLanguageFileDirectory("lang")

        DefaultClasses.register()
    }

}
