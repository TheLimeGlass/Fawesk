package me.limeglass.fawesk.elements.expressions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.eclipse.jdt.annotation.Nullable;

import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.util.Direction;
import me.limeglass.fawesk.lang.FaweskExpression;
import me.limeglass.fawesk.utils.annotations.Patterns;

@Name("Fawesk - BaseBlocks between different")
@Description("Returns the different baseblocks between two locations. Returning only one of each item.")
@Patterns({"[(all [[of] the]|the)] [fawe[sk]] base[ ]blocks from %location% [(on|towards)] %direction%",
		"[(all [[of] the]|the)] [fawe[sk]] base[ ]blocks (within|from) [cuboid[[ ]region[s]]] %worldeditregion%",
		"[(all [[of] the]|the)] [fawe[sk]] base[ ]blocks (within|between|from) %block% (and|to) %block%"})
public class ExprBaseBlocks extends FaweskExpression<BaseBlock> {
	
	@Override
	protected BaseBlock[] get(Event event) {
		final Set<BaseBlock> blocks = new HashSet<BaseBlock>();
		final Iterator<? extends BaseBlock> iterator = this.iterator(event);
		if (iterator == null) return null;
		iterator.forEachRemaining(block -> blocks.add(block));
		return blocks.toArray(new BaseBlock[blocks.size()]);
	}
	
	@Override
	@Nullable
	public Iterator<? extends BaseBlock> iterator(final Event event) {
		if (expressions.size() <= 0) return null;
		final Set<BaseBlock> blocks = new HashSet<BaseBlock>();
		Object from = expressions.get(0).getSingle(event);
		Region region = null;
		World world = null;
		
		if (!(from instanceof Region)) {
			Object to = expressions.get(1).getSingle(event);
			Location fromLoc = from instanceof Block ? ((Block)from).getLocation() : (Location) from;
			Vector fromVector = new Vector(fromLoc.getX(), fromLoc.getY(), fromLoc.getZ());
			
			world = BukkitUtil.getLocalWorld(fromLoc.getWorld());
		
			Location toLoc = to instanceof Block ? ((Block)to).getLocation() : ((Direction) to).getDirection(fromLoc).toLocation(fromLoc.getWorld());
			Vector toVector = new Vector(toLoc.getX(), toLoc.getY(), toLoc.getZ());
			
			region = new CuboidRegion(world, fromVector, toVector);
		} else if (from instanceof Region) {
			region = (Region) from;
			world = region.getWorld();
		}
		
		if (world == null) return null;

		EditSession session = FaweAPI.getEditSessionBuilder(world).autoQueue(true).build();
		
		blocks.addAll(session.getBlockDistributionWithData(region)
			.parallelStream()
			.map(block -> block.getID())
			.collect(Collectors.toSet()));
		
		session.flushQueue();
		
		return blocks.iterator();
	}

}
