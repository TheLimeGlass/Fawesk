package me.limeglass.fawesk.elements.expressions;

import org.bukkit.event.Event;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskPropertyExpression;
import me.limeglass.fawesk.utils.annotations.Properties;
import me.limeglass.fawesk.utils.annotations.PropertiesAddition;
import me.limeglass.fawesk.utils.annotations.RegisterType;

@Name("Fawesk - Cuboid region subregions")
@Description("Returns either the region(s) wall(s) or region(s) face(s) of the cuboid(s).")
@Properties({"cuboidregions", "(1¦wall[s]|2¦face[s]) [region][s]", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("[(world[ ]edit|cuboid)][[ ]region[s]]")
@RegisterType("worldeditregion")
public class ExprCuboidRegionSubregions extends FaweskPropertyExpression<CuboidRegion, Region> {
	
	@Override
	protected Region[] get(Event event, CuboidRegion[] cuboids) {
		for (CuboidRegion cuboid : cuboids) {
			switch (patternMark) {
				case 1:
					collection.add(cuboid.getWalls());
					break;
				case 2:
					collection.add(cuboid.getFaces());
					break;
			}
		}
		return collection.toArray(new Region[collection.size()]);
	}
}