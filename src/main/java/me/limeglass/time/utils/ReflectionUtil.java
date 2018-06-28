package me.limeglass.time.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ReflectionUtil {
	
	public static String getVersion() {
		return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
	}
	
	public static Class<?> getNMSClass(String classString) throws ClassNotFoundException {
		String name = "net.minecraft.server." + getVersion() + classString;
		Class<?> nmsClass = Class.forName(name);
		return nmsClass;
	}
	
	public static Class<?> getOBCClass(String classString) {
		String name = "org.bukkit.craftbukkit." + getVersion() + classString;
		@SuppressWarnings("rawtypes")
		Class obcClass = null;
		try {
			obcClass = Class.forName(name);
		}
		catch (ClassNotFoundException error) {
			error.printStackTrace();
			return null;
		}
		return obcClass;
	}
	
	public static Object getConnection(Player player) throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		Object nmsPlayer = getHandle(player);
		Field connectionField = nmsPlayer.getClass().getField("playerConnection");
		return connectionField.get(nmsPlayer);
	}
	
	public static <T> boolean setField(Class<T> from, Object obj, String field, Object newValue){
		try {
			Field f = from.getDeclaredField(field);
			f.setAccessible(true);
			f.set(obj, newValue);
			return true;
		} catch (Exception e){}
		return false;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getField(String field, Class<?> from, Object obj){
		try{
			Field f = from.getDeclaredField(field);
			f.setAccessible(true);
			return (T) f.get(obj);
		} catch (Exception e){}
		return null;	
	}
	
	public static Object getHandle(Object obj) {
		if (obj != null) {
			try {
				Method getHandle = obj.getClass().getMethod("getHandle");
				getHandle.setAccessible(true);
				return getHandle.invoke(obj);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void sendPacket(Object object, Player... players) throws NoSuchMethodException {
		try {
			for (Player player: players) {
				Method method = getConnection(player).getClass().getMethod("sendPacket", getNMSClass("Packet"));
				method.invoke(getConnection(player), object);
			}
		} catch (SecurityException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
