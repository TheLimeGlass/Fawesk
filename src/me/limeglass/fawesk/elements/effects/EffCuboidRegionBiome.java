package me.limeglass.fawesk.elements.effects;

import org.bukkit.event.Event;
import com.boydti.fawe.FaweAPI;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.FlatRegionFunction;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.FlatRegionVisitor;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskEffect;
import me.limeglass.fawesk.utils.annotations.Patterns;

@Name("Fawesk - Cuboid biome")
@Description("Changes the biomes of the Cuboid region(s)")
@Patterns("change biome of [the] [cuboid[[ ]region][s]] %cuboidregions% to [biome] %string%")
public class EffCuboidRegionBiome extends FaweskEffect {

	@Override
	protected void execute(Event event) {
		if (areNull(event)) return;
		String input = expressions.getSingle(event, String.class);
		for (CuboidRegion cuboid : expressions.getAll(event, CuboidRegion.class)) {
			World world = cuboid.getWorld();
			BiomeRegistry registry = world.getWorldData().getBiomeRegistry();
			EditSession session = FaweAPI.getEditSessionBuilder(world).autoQueue(true).build();
			BaseBiome biome = null;
			for (BaseBiome baseBiome : registry.getBiomes()) {
				if (registry.getData(baseBiome).getName().equalsIgnoreCase(input)) {
					biome = baseBiome;
					break;
				}
			}
			if (biome == null) continue;
			FlatRegionFunction function = new BiomeReplace(session, biome);
			FlatRegionVisitor visitor = new FlatRegionVisitor(Regions.asFlatRegion(cuboid), function);
			try {
				Operations.completeLegacy(visitor);
			} catch (MaxChangedBlocksException e) {
				Skript.error("The maximum amount of changed blocks has been exceeded.");
			}
		}
	}
}
