package me.limeglass.fawesk.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.event.Event;

import ch.njol.skript.lang.Expression;
import me.limeglass.fawesk.Fawesk;

public interface DataChecker {

	public default <T> Boolean areNull(Event event, ExpressionData expressions) {
		if (expressions.getExpressions() == null) return true;
		for (Expression<?> expression : expressions.getExpressions()) {
			if (expression == null) return true;
			if (expression.isSingle() && expression.getSingle(event) == null) {
				Fawesk.debugMessage("An expression was null: " + expression.toString(event, true));
				return true;
			} else if (expression.getAll(event) == null || expression.getAll(event).length == 0) {
				ArrayList<String> nulledExpressions = new ArrayList<String>();
				Arrays.stream(expressions.getExpressions()).filter(expr -> expr != null && expr.getAll(event) != null && expr.getAll(event).length == 0).forEach(expr -> nulledExpressions.add(expr.toString(event, true)));
				Fawesk.debugMessage("Expressions were null: " + nulledExpressions.toString());
				return true;
			}
		}
		return false;
	}
	
	public default <T> Boolean isNull(Event event, ExpressionData expressions, @SuppressWarnings("unchecked") Class<T>... types) {
		Map<Expression<?>, T[]> map = expressions.getAllMapOf(event, types);
		if (map == null || map.isEmpty()) return true;
		for (Entry<Expression<?>, T[]> entry : map.entrySet()) {
			if (entry.getKey() != null && entry.getKey().isSingle() && entry.getKey().getSingle(event) == null) {
				Fawesk.debugMessage("An expression was null: " + entry.getKey().toString(event, true));
				return true;
			} else if (entry.getKey() != null && entry.getKey().getAll(event).length == 0 || entry.getKey().getAll(event) == null) {
				ArrayList<String> nulledExpressions = new ArrayList<String>();
				Arrays.stream(expressions.getExpressions()).filter(expr -> expr != null && expr.getAll(event).length == 0 || expr.getAll(event) == null).forEach(expr -> nulledExpressions.add(expr.toString(event, true)));
				Fawesk.debugMessage("Expressions were null: " + nulledExpressions.toString());
				return true;
			}
		}
		return false;
	}
	
	public default Boolean isNull(Event event, ExpressionData expressions, int index) {
		Expression<?> expression = expressions.get(index);
		if (expression == null) return true;
		if (expression != null && expression.isSingle() && expression.getSingle(event) == null) {
			Fawesk.debugMessage("The expression at index " + index + " was null: " + expression.toString(event, true));
			return true;
		} else if (expression != null && expression.getAll(event).length == 0 || expression.getAll(event) == null) {
			Fawesk.debugMessage("The list expression at index " + index + " was null: " + expression.toString(event, true));
			return true;
		}
		return false;
	}
}