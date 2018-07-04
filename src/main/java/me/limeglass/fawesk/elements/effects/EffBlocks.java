package me.limeglass.fawesk.elements.effects;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.util.Direction;
import me.limeglass.fawesk.Fawesk;
import me.limeglass.fawesk.lang.FaweskEffect;
import me.limeglass.fawesk.objects.FaweskCommandSender;
import me.limeglass.fawesk.utils.annotations.Patterns;

@Name("Fawesk - set blocks")
@Description("Sets all the blocks within two points to a block/itemtype.")
@Patterns({"set [(all [[of] the]|the)] [fawe[sk]] ((item|block)[ ]types|[base[ ]]blocks) from %location% [(on|towards)] %direction% to %string/itemtypes%",
		"set [(all [[of] the]|the)] [fawe[sk]] ((item|block)[ ]types|[base[ ]]blocks) (within|between|from) %block% (and|to) %block% to %string/itemtypes%"})
public class EffBlocks extends FaweskEffect {

	@SuppressWarnings("deprecation")
	@Override
	protected void execute(Event event) {
		if (areNull(event)) return;
		Object from = expressions.get(0).getSingle(event);
		CuboidRegion region;
		World world;
		
		if (!(from instanceof CuboidRegion)) {
			Object to = expressions.get(1).getSingle(event);
			
			Location fromLoc = from instanceof Block ? ((Block)from).getLocation() : (Location) from;
			Vector fromVector = new Vector(fromLoc.getX(), fromLoc.getY(), fromLoc.getZ());
			
			world = BukkitUtil.getLocalWorld(fromLoc.getWorld());
		
			Location toLoc = to instanceof Block ? ((Block)to).getLocation() : ((Direction) to).getDirection(fromLoc).toLocation(fromLoc.getWorld());
			Vector toVector = new Vector(toLoc.getX(), toLoc.getY(), toLoc.getZ());
			
			region = new CuboidRegion(world, fromVector, toVector);
		} else {
			region = (CuboidRegion) from;
			world = region.getWorld();
		}

		EditSession session = FaweAPI.getEditSessionBuilder(world).autoQueue(true).build();
		
		session.setBlocks(region, new BaseBlock(0));
		session.flushQueue();
		
		Object[] delta = expressions.get(2).getAll(event);
		
		if (delta != null) {
			if (delta[0] instanceof String) {
				String input = (String) delta[0];
				LocalSession local = new LocalSession();
				local.remember(session);
				ParserContext context = new ParserContext();
				context.setActor(new FaweskCommandSender(Fawesk.getInstance(), Bukkit.getConsoleSender()));
				context.setSession(local);
				context.setWorld(world);
				Pattern pattern = null;
				try {
					pattern = WorldEdit.getInstance().getPatternFactory().parseFromInput(input, context);
				} catch (InputParseException e) {
					Skript.exception(e, "The input " + input + " was not a valid input for WorldEdit/FAWE");
				}
				if (pattern == null) return;
				session.setBlocks(region, pattern);
			} else if (delta[0] instanceof ItemType) {
				ItemStack item = ((ItemType)delta[0]).getItem().getRandom();
				session.setBlocks(region, new BaseBlock(item.getTypeId(), item.getData().getData()));
			} else {
				Bukkit.broadcastMessage("not block " + delta[0].getClass());
			}
		}
		session.flushQueue();
	}
}