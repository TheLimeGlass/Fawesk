package me.limeglass.fawesk.elements.expressions;

import org.bukkit.event.Event;

import com.sk89q.worldedit.regions.CuboidRegion;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.Fawesk;
import me.limeglass.fawesk.lang.FaweskPropertyExpression;
import me.limeglass.fawesk.utils.annotations.Properties;
import me.limeglass.fawesk.utils.annotations.PropertiesAddition;

@Name("Fawesk - Cuboid region count")
@Description("Returns the size/amount of items in the Cuboid region(s).")
@Properties({"cuboidregions", "(size|count|amount)", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("cuboid[[ ]region[s]]")
public class ExprCuboidRegionCount extends FaweskPropertyExpression<CuboidRegion, Number> {
	
	@Override
	protected Number[] get(Event event, CuboidRegion[] cuboids) {
		for (CuboidRegion cuboid : cuboids) {
			collection.add(cuboid.getHeight() * cuboid.getLength() * cuboid.getWidth());
			Fawesk.consoleMessage(cuboid.getHeight() * cuboid.getLength() * cuboid.getWidth() + " wat");
		}
		return collection.toArray(new Number[collection.size()]);
	}
}