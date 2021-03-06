package me.limeglass.fawesk.elements.expressions;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.util.Direction;
import me.limeglass.fawesk.lang.FaweskExpression;
import me.limeglass.fawesk.utils.annotations.Patterns;
import me.limeglass.fawesk.utils.annotations.RegisterType;
import me.limeglass.fawesk.utils.annotations.Single;

@Name("Fawesk - Cuboid region")
@Description("Returns the CuboidRegion between two locations. This is also defined as a worldeditregion.")
@Patterns({"[fawe[sk]] (world[ ]edit|cuboid)[[ ]region] from %location% [(to [the]|on|towards)] %direction/location%",
		"[fawe[sk]] (world[ ]edit|cuboid)[[ ]region] (within|between|from) %block% (and|to) %block%"})
@RegisterType("cuboidregion")
@Single
public class ExprCuboidRegion extends FaweskExpression<CuboidRegion> {
	
	@Override
	protected CuboidRegion[] get(Event event) {
		if (areNull(event)) return null;
		Object from = expressions.get(0).getSingle(event);
		Object to = expressions.get(1).getSingle(event);
		
		Location fromLoc = from instanceof Block ? ((Block)from).getLocation() : (Location) from;
		Vector fromVector = new Vector(fromLoc.getX(), fromLoc.getY(), fromLoc.getZ());
		
		World world = BukkitUtil.getLocalWorld(fromLoc.getWorld());
		
		Location toLoc = to instanceof Block ? ((Block)to).getLocation() : to instanceof Location ? (Location)to : ((Direction) to).getDirection(fromLoc).toLocation(fromLoc.getWorld());
		Vector toVector = new Vector(toLoc.getX(), toLoc.getY(), toLoc.getZ());
		
		return new CuboidRegion[] {new CuboidRegion(world, fromVector, toVector)};
	}
}