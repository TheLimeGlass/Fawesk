package me.limeglass.fawesk.elements.conditions;

import org.bukkit.event.Event;
import com.sk89q.worldedit.regions.CuboidRegion;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskCondition;
import me.limeglass.fawesk.utils.annotations.Patterns;

@Name("Fawesk- Cuboid region is global")
@Description("Check if a cuboid region is global.")
@Patterns("[(all [[of] the]|the)] [world[ ]edit][ ]region[s] %regions% (1¦(is|are)|2¦(is|are)(n't| not)) global")
public class CondCuboidRegionGlobal extends FaweskCondition {

	public boolean check(Event event) {
		for (CuboidRegion cuboid : expressions.getAll(event, CuboidRegion.class)) {
			if (!cuboid.isGlobal()) return !isNegated();
		}
		return isNegated();
	}
}