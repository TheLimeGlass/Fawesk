package me.limeglass.fawesk.lang;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.event.Event;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import me.limeglass.fawesk.Syntax;
import me.limeglass.fawesk.Fawesk;

public abstract class FaweskCondition extends Condition implements DataChecker {

	protected ExpressionData expressions;
	protected int patternMark;
	
	@Override
	public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parser) {
		if (expressions != null && getSyntax() != null) this.expressions = new ExpressionData(expressions, getSyntax()[0]);
		this.patternMark = parser.mark;
		setNegated(parser.mark == 1);
		return true;
	}
	
	public String[] getSyntax() {
		return Syntax.get(getClass().getSimpleName());
	}
	
	@Override
	public String toString(Event event, boolean debug) {
		ArrayList<String> values = new ArrayList<String>();
		String modSyntax = Syntax.isModified(getClass()) ? "Modified syntax: " + Arrays.toString(getSyntax()) : Arrays.toString(getSyntax());
		if (event == null) {
			Fawesk.debugMessage(getClass().getSimpleName() + " - " + modSyntax);
		} else {
			Arrays.asList(expressions.getExpressions()).stream().forEach(expression->values.add(expression.toString(event, debug)));
			Fawesk.debugMessage(getClass().getSimpleName() + " - " + modSyntax + " (" + event.getEventName() + ")" + " Data: " + Arrays.toString(values.toArray()));
		}
		return Fawesk.getInstance().getNameplate() + getClass().getSimpleName() + "- Syntax: " + Arrays.toString(getSyntax());
	}
	
	@Override
	public boolean check(Event event) {
		if (areNull(event)) return false;
		return check(event);
	}

	public <T> Boolean isNull(Event event, @SuppressWarnings("unchecked") Class<T>... types) {
		return isNull(event, expressions, types);
	}
	
	public Boolean isNull(Event event, int index) {
		return isNull(event, expressions, index);
	}

	public Boolean areNull(Event event) {
		return areNull(event, expressions);
	}
}