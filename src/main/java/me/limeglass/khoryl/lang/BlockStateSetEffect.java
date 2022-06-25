package me.limeglass.khoryl.lang;

import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.Event;

import ch.njol.skript.Skript;
import ch.njol.skript.log.ErrorQuality;
import me.limeglass.khoryl.Khoryl;

public abstract class BlockStateSetEffect<S extends BlockState> extends SetEffect<Block> implements BlockStateSyntax<S> {

	protected final boolean printErrors;

	public BlockStateSetEffect() {
		printErrors = Khoryl.getInstance().canRuntimeError();
	}

	protected static void register(Class<? extends SetEffect<?>> effect, String property) {
		register(effect, property, "blocks");
	}

	protected abstract void execute(S data, boolean value);

	@SuppressWarnings("unchecked")
	@Override
	protected void execute(Event event) {
		boolean value = getBoolean(event);
		for (Block block : getExpression().getArray(event)) {
			BlockState state = block.getState();
			if (!accepts(state)) {
				if (printErrors)
					Skript.error("A block state was not of type " + getCastingBlockStateClass().getName()
							+ " in property expression '" + getPropertyName() + "'", ErrorQuality.SEMANTIC_ERROR);
				continue;
			}
			S complete = (S) state;
			execute(complete, value);
			complete.update(true);
		}
	}

}
