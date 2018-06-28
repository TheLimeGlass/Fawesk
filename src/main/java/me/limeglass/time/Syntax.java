package me.limeglass.time;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.util.SimpleEvent;
import me.limeglass.time.utils.Utils;
import me.limeglass.time.utils.annotations.AllChangers;
import me.limeglass.time.utils.annotations.Changers;

public class Syntax {

	private static HashMap<String, String[]> modified = new HashMap<String, String[]>();
	private static HashMap<String, String[]> completeSyntax = new HashMap<String, String[]>();

	public static String[] register(Class<?> syntaxClass, String... syntax) {
		if (syntaxClass.isAnnotationPresent(me.limeglass.time.utils.annotations.Disabled.class)) return null;
		String type = "Expressions";
		if (Condition.class.isAssignableFrom(syntaxClass)) type = "Conditions";
		else if (Effect.class.isAssignableFrom(syntaxClass)) type = "Effects";
		else if (SimpleEvent.class.isAssignableFrom(syntaxClass)) type = "Events";
		else if (PropertyExpression.class.isAssignableFrom(syntaxClass)) type = "PropertyExpressions";
		String node = "Syntax." + type + "." + syntaxClass.getSimpleName() + ".";
		if (!Time.getConfiguration("syntax").isSet(node + "enabled")) {
			Time.getConfiguration("syntax").set(node + "enabled", true);
			Time.save("syntax");
		}
		if (syntaxClass.isAnnotationPresent(Changers.class) || syntaxClass.isAnnotationPresent(AllChangers.class)) {
			if (syntaxClass.isAnnotationPresent(AllChangers.class)) Time.getConfiguration("syntax").set(node + "changers", "All changers");
			else {
				ChangeMode[] changers = syntaxClass.getAnnotation(Changers.class).value();
				Time.getConfiguration("syntax").set(node + "changers", Arrays.toString(changers));
			}
			Time.save("syntax");
		}
		if (syntaxClass.isAnnotationPresent(Description.class)) {
			String[] descriptions = syntaxClass.getAnnotation(Description.class).value();
			Time.getConfiguration("syntax").set(node + "description", descriptions[0]);
			Time.save("syntax");
		}
		if (!Time.getConfiguration("syntax").getBoolean(node + "enabled")) {
			if (Time.getInstance().getConfig().getBoolean("NotRegisteredSyntax", false)) Time.consoleMessage(node.toString() + " didn't register!");
			return null;
		}
		if (!Time.getConfiguration("syntax").isSet(node + "syntax")) {
			Time.getConfiguration("syntax").set(node + "syntax", syntax);
			Time.save("syntax");
			return add(syntaxClass.getSimpleName(), syntax);
		}
		List<String> data = Time.getConfiguration("syntax").getStringList(node + "syntax");
		if (!Utils.compareArrays(data.toArray(new String[data.size()]), syntax)) modified.put(syntaxClass.getSimpleName(), syntax);
		if (Time.getConfiguration("syntax").isList(node + "syntax")) {
			List<String> syntaxes = Time.getConfiguration("syntax").getStringList(node + "syntax");
			return add(syntaxClass.getSimpleName(), syntaxes.toArray(new String[syntaxes.size()]));
		}
		return add(syntaxClass.getSimpleName(), new String[]{Time.getConfiguration("syntax").getString(node + "syntax")});
	}
	
	public static Boolean isModified(@SuppressWarnings("rawtypes") Class syntaxClass) {
		return modified.containsKey(syntaxClass.getSimpleName());
	}
	
	public static String[] get(String syntaxClass) {
		return completeSyntax.get(syntaxClass);
	}
	
	private static String[] add(String syntaxClass, String... syntax) {
		if (!completeSyntax.containsValue(syntax)) {
			completeSyntax.put(syntaxClass, syntax);
		}
		return syntax;
	}
}