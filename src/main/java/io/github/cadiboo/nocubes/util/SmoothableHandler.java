package io.github.cadiboo.nocubes.util;

import com.google.common.collect.Sets;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author Cadiboo
 */
public final class SmoothableHandler {

	private static final Set<BlockState> SMOOTHABLES = Sets.newIdentityHashSet();
	static {
		SMOOTHABLES.add(Blocks.STONE.getDefaultState());
		SMOOTHABLES.add(Blocks.DIRT.getDefaultState());
	}

	public static boolean isStateSmoothable(@Nonnull final BlockState state) {
		return SMOOTHABLES.contains(state);
	}

	public static void setStateSmoothable(@Nonnull final BlockState state, final boolean smoothable) {
		if (smoothable)
			SMOOTHABLES.add(state);
		else
			SMOOTHABLES.remove(state);
	}

}
