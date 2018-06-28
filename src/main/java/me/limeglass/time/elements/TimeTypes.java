package me.limeglass.time.elements;

import java.time.DayOfWeek;
import java.time.Month;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.Nullable;
import org.joda.time.DateTime;

import com.google.common.collect.Sets;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Timeperiod;
import ch.njol.skript.util.Timespan;
import ch.njol.util.coll.CollectionUtils;
import me.limeglass.time.utils.EnumClassInfo;
import me.limeglass.time.utils.TimeZones;

public class TimeTypes {

	static {
		EnumClassInfo.create(TimeZones.class, "timezone", null);
		EnumClassInfo.create(DayOfWeek.class, "day", null);
		EnumClassInfo.create(Month.class, "month", null);
		
		new Changer<DateTime>() {
			
			private int milliseconds = 0;
			
			@Override
			@Nullable
			public Class<?>[] acceptChange(ChangeMode mode) {
				if (mode == ChangeMode.REMOVE_ALL || mode == ChangeMode.DELETE)
					return null;
				return CollectionUtils.array(Timespan[].class, Timeperiod[].class, Date[].class);
			}
			
			private long getMilliseconds(Object object) {
				long millis = 0L;
				if (object instanceof Timespan) millis = ((Timespan) object).getMilliSeconds();
				else if (object instanceof Date) {
					millis = ((Date) object).difference(new Date()).getMilliSeconds();
				} else if (object instanceof Timeperiod) {
					millis = ((Timeperiod) object).end - ((Timeperiod) object).start;
				}
				return millis;
			}

			@Override
			public void change(DateTime[] dates, @Nullable Object[] delta, ChangeMode mode) {
				if (dates == null) return;
				milliseconds = 0;
				Sets.newHashSet(delta).parallelStream().map(object -> getMilliseconds(object)).collect(Collectors.toSet());
				for (DateTime date : dates) {
					switch (mode) {
						case SET:
							date.withMillis(milliseconds);
							break;
						case ADD:
							date.plusMillis(milliseconds);
							break;
						case REMOVE:
							date.minusMillis(milliseconds);
							break;
						case RESET:
							date.withMillis(0);
							break;
						case DELETE:
						case REMOVE_ALL:
						default:
							break;
					}
				}
			}
		};
	}
}
