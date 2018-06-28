package me.limeglass.time.elements.datetime;

import java.time.Month;

import org.bukkit.event.Event;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import me.limeglass.time.lang.TimeExpression;
import me.limeglass.time.utils.TimeZones;
import me.limeglass.time.utils.annotations.Patterns;
import me.limeglass.time.utils.annotations.RegisterType;
import me.limeglass.time.utils.annotations.Single;
@Name("DateTime - New date and time")
@Description("Returns a new DateTime with optional parameters.")
@Examples({"set {_date} to todays date",
	"set {_date} to todays date in timezone us eastern",
	"set {_date} to a new date with year 1996, month july, day 26, 6 hours, and 56 minutes",
	"set {_date} to a new date with year 2005, month may, day 30, 1 hour, 90 minutes, 5 seconds, and 100 milliseconds"})
@Patterns("[(todays|[a] [new])] date[( and time|time)]"
			+ "[with [year] %-number%"
			+ "[(, | with)] [month] %-number/month%"
			+ "[(, | with)] [day] %-number%"
			+ "[(, | with)] %-number% [hour[s]]"
			+ "[(, [and]| with)] %-number% [minute[s]]]"
		+ "[[(, [and]| with)] %-number% [second[s]]"
		+ "[[(, [and]| with)] %-number% [milli[-]second[s]]"
		+ "[[[(, [and] | with)][in]] time[ ]zone %-timezone%]")
@RegisterType("datetime")
@Single
public class ExprDateTime extends TimeExpression<DateTime> {
	
	@Override
	protected DateTime[] get(Event event) {
		DateTime date = new DateTime();
		if (!isNull(event, TimeZones.class)) date = new DateTime(DateTimeZone.forTimeZone(expressions.getSingle(event, TimeZones.class).getTimeZone()));//(expressions.getInt(event, 5));
		if (!isNull(event, 0, 1, 2, 3, 4)) {
			date = date.withYear(expressions.getInt(event, 0));
			Object month = expressions.get(1).getSingle(event);
			if (month instanceof Month) date = date.withMonthOfYear(((Month) month).getValue());
			else date = date.withMonthOfYear((int)month);
			date = date.withDayOfMonth(expressions.getInt(event, 2))
				.withHourOfDay(expressions.getInt(event, 3))
				.withMinuteOfHour(expressions.getInt(event, 4));
		}
		if (!isNull(event, 5)) date = date.withSecondOfMinute(expressions.getInt(event, 5));
		if (!isNull(event, 6)) date = date.withMillisOfSecond(expressions.getInt(event, 6));
		return new DateTime[] {date};
	}
}