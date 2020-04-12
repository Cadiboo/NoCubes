package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.util.SmoothableHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
@Mod(NoCubes.MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";

	public NoCubes() {
	}

	public static boolean isStateSmoothable(@Nonnull BlockState state) {
		return SmoothableHandler.isStateSmoothable(state);
	}

	public static void setStateSmoothable(@Nonnull final BlockState state, final boolean smoothable) {
		SmoothableHandler.setStateSmoothable(state, smoothable);
	}

}
