package me.limeglass.fawesk.elements.expressions;

import org.bukkit.event.Event;
import com.sk89q.worldedit.regions.CuboidRegion;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskPropertyExpression;
import me.limeglass.fawesk.utils.annotations.Properties;
import me.limeglass.fawesk.utils.annotations.PropertiesAddition;

@Name("Fawesk - Cuboid region y-coordinate")
@Description("Returns the highest or lowest y-coordinat of the cuboid region(s).")
@Properties({"cuboidregions", "(1¦max[imum] [(point|pos[ition])]|2¦min[imum] [(point|pos[ition])])[s]", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("cuboid[[ ]region[s]]")
public class ExprCuboidRegionYCoordinate extends FaweskPropertyExpression<CuboidRegion, Number> {
	
	@Override
	protected Number[] get(Event event, CuboidRegion[] cuboids) {
		for (CuboidRegion cuboid : cuboids) {
			if (patternMark == 1) collection.add(cuboid.getMaximumY());
			else collection.add(cuboid.getMinimumY());
		}
		return collection.toArray(new Number[collection.size()]);
	}
}