package me.limeglass.fawesk.elements.effects;

import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import com.sk89q.worldedit.regions.CuboidRegion;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import me.limeglass.fawesk.lang.FaweskEffect;
import me.limeglass.fawesk.utils.annotations.Patterns;

@Name("Fawesk - Cuboid region expand")
@Description("Expands the Cuboid region(s)")
@Patterns("expand [(all [[of] the]|the)] [cuboid[[ ]region][s]] %cuboidregions% (to|by) [the] [vector] %vectors%")
public class EffCuboidRegionExpand extends FaweskEffect {

	@Override
	protected void execute(Event event) {
		if (areNull(event)) return;
		for (CuboidRegion cuboid : expressions.getAll(event, CuboidRegion.class)) {
			cuboid.expand(getBukkitVectors(event));
		}
	}
	
	private com.sk89q.worldedit.Vector[] getBukkitVectors(Event event) {
		Vector[] bukkitVectors = expressions.getAll(event, Vector.class);
		com.sk89q.worldedit.Vector[] vectors = new com.sk89q.worldedit.Vector[bukkitVectors.length];
		for (int i = 0; i < bukkitVectors.length; i++) {
			vectors[i] = new com.sk89q.worldedit.Vector(bukkitVectors[i].getX(), bukkitVectors[i].getY(), bukkitVectors[i].getZ());
		}
		return vectors;
	}
}