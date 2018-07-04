package me.limeglass.fawesk.elements.conditions;

import org.bukkit.Location;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.regions.Region;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskCondition;
import me.limeglass.fawesk.utils.annotations.Patterns;

@Name("Fawesk- Worldedit region contains location")
@Description("Check if the worldedit region(s) contains the location(s).")
@Patterns("[(all [[of] the]|the)] [world[ ]edit][ ]region[s] %regions% (1¦[does] (ha(s|ve)|contain[s])|2¦do[es](n't| not) (have|contain)) %locations/vectors%")
public class CondRegionContains extends FaweskCondition {

	public boolean check(Event event) {
		for (Region region : expressions.getAll(event, Region.class)) {
			for (Object object : expressions.get(1).getArray(event)) {
				if (object instanceof Location) {
					Location location = (Location) object;
					if (!region.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) return !isNegated();
				} else if (object instanceof Vector) {
					Vector vector = (Vector) object;
					if (!region.contains(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ())) return !isNegated();
				}
			}
		}
		return isNegated();
	}
}