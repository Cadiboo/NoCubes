package io.github.cadiboo.nocubes;

import net.minecraft.block.BlockState;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
@Mod(NoCubes.MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";

	public NoCubes() {
		var example_var_syntax = "local variable type inference";
	}

	public static boolean isStateSmoothable(@Nonnull BlockState state) {
		return false;
	}

}
