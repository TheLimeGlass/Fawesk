package me.limeglass.fawesk.elements.expressions;

import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.regions.CuboidRegion;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskPropertyExpression;
import me.limeglass.fawesk.utils.annotations.Changers;
import me.limeglass.fawesk.utils.annotations.Properties;
import me.limeglass.fawesk.utils.annotations.PropertiesAddition;
import me.limeglass.fawesk.utils.annotations.Settable;

@Name("Fawesk - Cuboid region positions")
@Description("Returns either the first or second position vector(s) of the cuboid region(s).")
@Properties({"cuboidregions", "(point|pos[ition])][( |-)](1¦1|2¦2)", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("cuboid[[ ]region[s]]")
@Changers(ChangeMode.SET)
@Settable({Location.class, Vector.class})
public class ExprCuboidRegionPositions extends FaweskPropertyExpression<CuboidRegion, Vector> {
	
	@Override
	protected Vector[] get(Event event, CuboidRegion[] cuboids) {
		for (CuboidRegion cuboid : cuboids) {
			if (patternMark == 1) add(cuboid.getPos1());
			else add(cuboid.getPos2());
		}
		return collection.toArray(new Vector[collection.size()]);
	}
	
	private void add(com.sk89q.worldedit.Vector vector) {
		collection.add(new Vector(vector.getX(), vector.getY(), vector.getZ()));
	}
	
	@Override
	public void change(Event event, Object[] delta, ChangeMode mode) {
		if (isNull(event) || delta == null) return;
		Object object = delta[0];
		com.sk89q.worldedit.Vector vector = null;
		if (object instanceof Vector) {
			vector = (com.sk89q.worldedit.Vector) object;
		} else if (object instanceof Location) {
			Location location = (Location) object;
			vector = new com.sk89q.worldedit.Vector(location.getX(), location.getY(), location.getZ());
		}
		for (CuboidRegion cuboid : expressions.getAll(event, CuboidRegion.class)) {
			if (patternMark == 1) cuboid.setPos1(vector);
			else cuboid.setPos2(vector);
		}
	}
}