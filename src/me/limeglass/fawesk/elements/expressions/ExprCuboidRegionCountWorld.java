package me.limeglass.fawesk.elements.expressions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.jnbt.anvil.MCAFilterCounter;
import com.boydti.fawe.jnbt.anvil.MCAQueue;
import com.boydti.fawe.jnbt.anvil.filters.CountIdFilter;
import com.boydti.fawe.util.SetQueue;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskExpression;
import me.limeglass.fawesk.utils.annotations.Patterns;
import me.limeglass.fawesk.utils.annotations.Single;

@Name("Fawesk - Cuboid region count worlds")
@Description("Returns how many of the defined block(s) are in the worlds(s).")
@Patterns("(size|count|amount) of %itemtypes/numbers/baseblocks% (in|within|from) %worlds%")
@Single
public class ExprCuboidRegionCountWorld extends FaweskExpression<Number> {
	
	@SuppressWarnings("deprecation")
	@Override
	protected Number[] get(Event event) {
		for (World world : expressions.getAll(event, World.class)) {
			Set<Integer> blocks = new HashSet<Integer>();
			for (Object object : expressions.get(0).getArray(event)) {
				if (object instanceof ItemType) {
					ItemStack item = ((ItemType) object).getRandom();
					blocks.add(item.getTypeId());
				} else if (object instanceof Number) blocks.add((int) object);
				else if (object instanceof BaseBlock) blocks.add(((BaseBlock) object).getId());
			}
			EditSession session = FaweAPI.getEditSessionBuilder(BukkitUtil.getLocalWorld(world)).fastmode(true).autoQueue(true).build();
			MCAQueue queue = new MCAQueue(SetQueue.IMP.getNewQueue(session.getWorld(), true, false));
			CountIdFilter filter = new CountIdFilter();
			blocks.forEach(filter::addBlock);
			MCAFilterCounter counter = queue.filterWorld(filter);
			collection.add(counter.getTotal());
			session.cancel();
		}
		return collection.toArray(new Number[collection.size()]);
	}
}