package me.limeglass.fawesk.elements.expressions;

import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.event.Event;

import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskPropertyExpression;
import me.limeglass.fawesk.utils.annotations.Properties;
import me.limeglass.fawesk.utils.annotations.PropertiesAddition;

@Name("Fawesk - Cuboid region count")
@Description("Returns the size/amount of items in the Cuboid region(s).")
@Properties({"cuboidregions", "(size|count|amount)", "{1}[(all [[of] the]|the)]"})
@PropertiesAddition("cuboid[[ ]region[s]]")
public class ExprCuboidRegionCount extends FaweskPropertyExpression<CuboidRegion, Number> {
	
	@Override
	protected Number[] get(Event event, CuboidRegion[] cuboids) {
		for (CuboidRegion cuboid : cuboids) {
			EditSession session = FaweAPI.getEditSessionBuilder(cuboid.getWorld()).autoQueue(true).build();
			Set<BaseBlock> blocks = session.getBlockDistributionWithData(cuboid)
				.parallelStream()
				.map(block -> block.getID())
				.collect(Collectors.toSet());
			collection.add(session.countBlocks(cuboid, blocks));
			session.flushQueue();
		}
		return collection.toArray(new Number[collection.size()]);
	}
}