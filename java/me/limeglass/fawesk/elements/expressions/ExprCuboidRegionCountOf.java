package me.limeglass.fawesk.elements.expressions;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskExpression;
import me.limeglass.fawesk.utils.annotations.Patterns;

@Name("Fawesk - Cuboid region count of")
@Description("Returns how many of the defined block(s) are in the Cuboid region(s).")
@Patterns("(size|count|amount) (of|1¦[of] excluding) %itemtypes/baseblocks% (in|within|from) cuboid[[ ]region[s]] %cuboidregions%")
public class ExprCuboidRegionCountOf extends FaweskExpression<Number> {
	
	@SuppressWarnings("deprecation")
	@Override
	protected Number[] get(Event event) {
		for (CuboidRegion cuboid : expressions.getAll(event, CuboidRegion.class)) {
			Set<BaseBlock> blocks = new HashSet<BaseBlock>();
			for (Object object : expressions.get(0).getArray(event)) {
				if (object instanceof ItemType) {
					ItemStack item = ((ItemType) object).getRandom();
					blocks.add(new BaseBlock(item.getTypeId(), item.getData().getData()));
				} else if (object instanceof BaseBlock) blocks.add((BaseBlock) object);
			}
			EditSession session = FaweAPI.getEditSessionBuilder(cuboid.getWorld()).autoQueue(true).build();
			if (patternMark == 1) {
				collection.add(session.countBlocks(cuboid, session
						.getBlockDistributionWithData(cuboid)
						.parallelStream()
						.filter(block -> !blocks.contains(block.getID()))
						.map(block -> block.getID())
						.collect(Collectors.toSet())));
			} else collection.add(session.countBlocks(cuboid, blocks));
		}
		return collection.toArray(new Number[collection.size()]);
	}
}