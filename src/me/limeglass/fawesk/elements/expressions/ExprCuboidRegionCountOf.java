package me.limeglass.fawesk.elements.expressions;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.jnbt.anvil.MCAFilterCounter;
import com.boydti.fawe.jnbt.anvil.MCAQueue;
import com.boydti.fawe.jnbt.anvil.filters.CountIdFilter;
import com.boydti.fawe.object.RegionWrapper;
import com.boydti.fawe.util.SetQueue;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.Fawesk;
import me.limeglass.fawesk.lang.FaweskExpression;
import me.limeglass.fawesk.utils.annotations.Patterns;
import me.limeglass.fawesk.utils.annotations.Single;

@SuppressWarnings("deprecation")
@Name("Fawesk - Cuboid region count of")
@Description("Returns how many of the defined block(s) are in the Cuboid region(s).")
@Patterns("(size|count|amount) (of|1¦[of] excluding) %itemtypes/numbers/baseblocks% (in|within|from) cuboid[[ ]region[s]] %cuboidregions%")
@Single
public class ExprCuboidRegionCountOf extends FaweskExpression<Number> {
	
	@Override
	protected Number[] get(Event event) {
		for (CuboidRegion cuboid : expressions.getAll(event, CuboidRegion.class)) {
			Set<Integer> blocks = new HashSet<Integer>();
			for (Object object : expressions.get(0).getArray(event)) {
				if (object instanceof ItemType) {
					ItemStack item = ((ItemType) object).getRandom();
					blocks.add(item.getTypeId());
				} else if (object instanceof Number) blocks.add((int) object);
				else if (object instanceof BaseBlock) blocks.add(((BaseBlock) object).getId());
			}
			EditSession session = FaweAPI.getEditSessionBuilder(cuboid.getWorld()).fastmode(true).autoQueue(true).build();
			MCAQueue queue = new MCAQueue(SetQueue.IMP.getNewQueue(session.getWorld(), true, true));
			CountIdFilter filter = new CountIdFilter();
			if (patternMark == 1) {
				session.getBlockDistributionWithData(cuboid).parallelStream()
					.filter(block -> !blocks.contains(block.getID().getId()))
					.forEach(block -> filter.addBlock(block.getID().getId()));
			} else blocks.forEach(filter::addBlock);
			for (Integer id : blocks) {
				Fawesk.consoleMessage("block: " + id);
			}
			MCAFilterCounter counter = queue.filterRegion(filter, new RegionWrapper(cuboid.getMinimumPoint(), cuboid.getMaximumPoint()));
			collection.add(counter.getTotal());
			session.cancel();
			/*TaskManager.IMP.async(new Runnable() {
			    @Override
			    public void run() {
			        AsyncWorld world = AsyncWorld.wrap(Bukkit.getWorld(cuboid.getWorld().getName()));
			        Block block = world.getBlockAt(0, 0, 0);
			        block.setType(Material.BEDROCK);
			        // When you are done
			        world.commit();
			    }
			});*/
			//AsyncWorld world = AsyncWorld.create(new WorldCreator(cuboid.getWorld().getName()));
		}
		return collection.toArray(new Number[collection.size()]);
	}
}