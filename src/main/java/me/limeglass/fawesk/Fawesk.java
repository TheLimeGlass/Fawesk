package me.limeglass.fawesk;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import me.limeglass.fawesk.elements.Register;
import me.limeglass.fawesk.utils.Utils;

public class Fawesk extends JavaPlugin {
	
	private static Map<String, FileConfiguration> files = new HashMap<String, FileConfiguration>();
	private String packageName = "me.limeglass.fawesk";
	private static String prefix = "&8[&cfawesk&8] &7";
	private String nameplate = "[fawesk] ";
	private static Fawesk instance;
	private SkriptAddon addon;
	private Metrics metrics;
	
	public void onEnable(){
		addon = Skript.registerAddon(this).setLanguageFileDirectory("lang");
		instance = this;
		saveDefaultConfig();
		File config = new File(getDataFolder(), "config.yml");
		if (!Objects.equals(getDescription().getVersion(), getConfig().getString("version"))) {
			consoleMessage("&aNew update found! Updating files now...");
			if (config.exists()) config.delete();
		}
		for (String name : Arrays.asList("config", "syntax")) {
			File file = new File(getDataFolder(), name + ".yml");
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				saveResource(file.getName(), false);
			}
			FileConfiguration configuration = new YamlConfiguration();
			try {
				configuration.load(file);
			} catch (IOException | InvalidConfigurationException e) {
				e.printStackTrace();
			}
			files.put(name, configuration);
		}
		metrics = new Metrics(this);
		Register.register();
		if (!getConfig().getBoolean("DisableRegisteredInfo", false)) Bukkit.getLogger().info(nameplate + "has been enabled!");
	}
	
	public static Fawesk getInstance() {
		return instance;
	}
	
	public SkriptAddon getAddonInstance() {
		return addon;
	}
	
	public Metrics getMetrics() {
		return metrics;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getNameplate() {
		return nameplate;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	//Grabs a FileConfiguration of a defined name. The name can't contain .yml in it.
	public static FileConfiguration getConfiguration(String file) {
		return (files.containsKey(file)) ? files.get(file) : null;
	}
	
	public static void save(String configuration) {
		try {
			File configurationFile = new File(instance.getDataFolder(), configuration + ".yml");
			getConfiguration(configuration).save(configurationFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void debugMessage(String text) {
		if (instance.getConfig().getBoolean("debug")) consoleMessage("&b" + text);
	}

	public static void consoleMessage(String... messages) {
		for (String text : messages) Bukkit.getConsoleSender().sendMessage(Utils.cc(prefix + text));
	}
}