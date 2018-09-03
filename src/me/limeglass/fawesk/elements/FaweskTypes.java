package me.limeglass.fawesk.elements;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;

import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.Serializer;
import ch.njol.util.coll.CollectionUtils;
import ch.njol.yggdrasil.Fields;
import me.limeglass.fawesk.utils.TypeClassInfo;

public class FaweskTypes {

	public static void register() {
		TypeClassInfo.create(Region.class, "worldeditregion").serializer(new Serializer<Region>() {

			@Override
			public Fields serialize(Region region) throws NotSerializableException {
				final Fields fields = new Fields();
				fields.putObject("world", region.getWorld());
				fields.putObject("max", region.getMaximumPoint());
				fields.putObject("min", region.getMinimumPoint());
				return fields;
			}

			@Override
			public void deserialize(Region o, Fields f) throws StreamCorruptedException, NotSerializableException {
				assert false;
			}
			
			@Override
			protected Region deserialize(final Fields fields) throws StreamCorruptedException {
				final World world = fields.getObject("world", World.class);
				final Vector maximum = fields.getObject("max", Vector.class);
				final Vector minimum = fields.getObject("min", Vector.class);
				if (world == null || maximum == null || minimum == null) throw new StreamCorruptedException();
				return new CuboidRegion(world, maximum, minimum);
			}

			@Override
			public boolean mustSyncDeserialization() {
				return false;
			}

			@Override
			protected boolean canBeInstantiated() {
				return false;
			}
		}).register();
		
		TypeClassInfo.create(BaseBlock.class, "baseblock").changer(new Changer<BaseBlock>() {

			@Override
			@Nullable
			public Class<?>[] acceptChange(ChangeMode mode) {
				return (mode == ChangeMode.SET) ? CollectionUtils.array(ItemType.class) : null;
			}
			
			@SuppressWarnings("deprecation")
			@Override
			public void change(BaseBlock[] baseblocks, @Nullable Object[] delta, ChangeMode mode) {
				if (delta == null) return;
				ItemStack item = ((ItemType) delta[0]).getRandom();
				for (BaseBlock baseblock : baseblocks) {
					baseblock.setIdAndData(item.getTypeId(), item.getData().getData());
				}
			}
			
		}).register();
	}
}
