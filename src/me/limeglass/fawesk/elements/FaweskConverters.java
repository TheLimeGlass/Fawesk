package me.limeglass.fawesk.elements;

import org.eclipse.jdt.annotation.Nullable;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.registrations.Converters;

public class FaweskConverters {

	public static void register() {
		Converters.registerConverter(BaseBlock.class, ItemType.class, new Converter<BaseBlock, ItemType>() {
			@Override
			@Nullable
			public ItemType convert(final BaseBlock base) {
				return new ItemType(base.getId(), (short) base.getData());
			}
		});
		
		Converters.registerConverter(BaseBlock.class, String.class, new Converter<BaseBlock, String>() {
			@Override
			@Nullable
			public String convert(final BaseBlock base) {
				return new ItemType(base.getId(), (short) base.getData()).toString();
			}
		});
		
		Converters.registerConverter(CuboidRegion.class, Region.class, new Converter<CuboidRegion, Region>() {
			@Override
			@Nullable
			public Region convert(final CuboidRegion cuboid) {
				return cuboid;
			}
		});
	}
}
