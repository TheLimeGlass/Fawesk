package me.limeglass.fawesk.objects;

import java.io.File;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.eclipse.jdt.annotation.Nullable;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.auth.AuthorizationException;

import ch.njol.skript.Skript;
import me.limeglass.fawesk.Fawesk;

public class FaweskCommandSender implements Actor {

	private static final UUID DEFAULT_ID = UUID.fromString("a133eb4b-4cab-42cd-9fd9-7e7b9a3f74be");

	private CommandSender sender;
	private Fawesk plugin;

	public FaweskCommandSender(Fawesk plugin, CommandSender sender) {
		this.plugin = plugin;
		this.sender = sender;
	}

	@Override
	public UUID getUniqueId() {
		return DEFAULT_ID;
	}

	@Override
	public String getName() {
		return sender.getName();
	}

	@Override
	public void printRaw(String msg) {
		for (String part : msg.split("\n")) {
			sender.sendMessage(part);
		}
	}

	@Override
	public void print(String msg) {
		Fawesk.consoleMessage(msg.split("\n"));
	}

	@Override
	public void printDebug(String msg) {
		for (String part : msg.split("\n")) {
			Fawesk.debugMessage(part);
		}
	}

	@Override
	public void printError(String msg) {
		Skript.error(msg);
	}

	@Override
	public boolean canDestroyBedrock() {
		return true;
	}

	@Override
	public String[] getGroups() {
		return new String[0];
	}

	@Override
	public boolean hasPermission(String perm) {
		return true;
	}

	@Override
	public void checkPermission(String permission) throws AuthorizationException {
	}

	@Override
	public boolean isPlayer() {
		return false;
	}

	@Override
	public File openFileOpenDialog(String[] extensions) {
		return null;
	}

	@Override
	public File openFileSaveDialog(String[] extensions) {
		return null;
	}

	@Override
	public void dispatchCUIEvent(CUIEvent event) {
	}

	@Override
	public SessionKey getSessionKey() {
		return new SessionKey() {
			@Nullable
			@Override
			public String getName() {
				return null;
			}

			@Override
			public boolean isActive() {
				return false;
			}

			@Override
			public boolean isPersistent() {
				return false;
			}

			@Override
			public UUID getUniqueId() {
				return DEFAULT_ID;
			}
		};
	}

	public Fawesk getPlugin() {
		return plugin;
	}

	public void setPlugin(Fawesk plugin) {
		this.plugin = plugin;
	}
}