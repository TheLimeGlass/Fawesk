package me.limeglass.fawesk.elements.expressions;

import org.bukkit.event.Event;
import com.sk89q.worldedit.regions.Region;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskPropertyExpression;
import me.limeglass.fawesk.utils.annotations.Properties;
import me.limeglass.fawesk.utils.annotations.PropertiesAddition;

@Name("Fawesk - Worldedit region numbers")
@Description("Returns either the area/width/length/height of the worldedit region(s).")
@Properties({"worldeditregions", "(1¦area [size]|2¦width|3¦length|4¦height)[s]", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("(world[ ]edit|cuboid)[[ ]region[s]]")
public class ExprRegionNumbers extends FaweskPropertyExpression<Region, Number> {
	
	@Override
	protected Number[] get(Event event, Region[] regions) {
		for (Region region : regions) {
			switch (patternMark) {
				case 1:
					collection.add(region.getArea());
					break;
				case 2:
					collection.add(region.getWidth());
					break;
				case 3:
					collection.add(region.getLength());
					break;
				case 4:
					collection.add(region.getHeight());
					break;
			}
		}
		return collection.toArray(new Number[collection.size()]);
	}
}