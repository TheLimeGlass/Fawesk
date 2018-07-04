package me.limeglass.fawesk.elements.expressions;

import java.util.Set;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.regions.Region;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskPropertyExpression;
import me.limeglass.fawesk.utils.annotations.Properties;
import me.limeglass.fawesk.utils.annotations.PropertiesAddition;

@Name("Fawesk - Worldedit region vectors")
@Description("Returns either the center/max/min/chunks vector location(s) of the worldedit region(s).")
@Properties({"worldeditregions", "(1¦center|2¦max[imum] [(point|pos[ition])]|3¦min[imum] [(point|pos[ition])]|4¦chunk [cube[s]])[s]", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("(world[ ]edit|cuboid)[[ ]region[s]]")
public class ExprRegionVectors extends FaweskPropertyExpression<Region, Vector> {
	
	@Override
	protected Vector[] get(Event event, Region[] regions) {
		for (Region region : regions) {
			switch (patternMark) {
				case 1:
					add(region.getCenter());
					break;
				case 2:
					add(region.getMaximumPoint());
					break;
				case 3:
					add(region.getMinimumPoint());
					break;
				case 4:
					addAll(region.getChunkCubes());
					break;
			}
		}
		return collection.toArray(new Vector[collection.size()]);
	}
	
	private void add(com.sk89q.worldedit.Vector vector) {
		collection.add(new Vector(vector.getX(), vector.getY(), vector.getZ()));
	}
	
	private void addAll(Set<com.sk89q.worldedit.Vector> vectors) {
		for (com.sk89q.worldedit.Vector vector : vectors) {
			collection.add(new Vector(vector.getX(), vector.getY(), vector.getZ()));
		}
	}
}