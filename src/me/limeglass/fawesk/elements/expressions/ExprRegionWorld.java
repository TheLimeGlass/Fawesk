package me.limeglass.fawesk.elements.expressions;

import org.bukkit.Bukkit;

import org.bukkit.World;
import org.bukkit.event.Event;
import com.sk89q.worldedit.regions.Region;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskPropertyExpression;
import me.limeglass.fawesk.utils.annotations.Properties;
import me.limeglass.fawesk.utils.annotations.PropertiesAddition;

@Name("Fawesk - Worldedit region world")
@Description("Returns the world(s) of the worledit region(s).")
@Properties({"worldeditregions", "world[s]", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("(world[ ]edit|cuboid)[[ ]region[s]]")
public class ExprRegionWorld extends FaweskPropertyExpression<Region, World> {
	
	@Override
	protected World[] get(Event event, Region[] regions) {
		for (Region region : regions) {
			World world = Bukkit.getWorld(region.getWorld().getName());
			collection.add(world);
		}
		return collection.toArray(new World[collection.size()]);
	}
}