package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;

public final class ClientUtil {

	public static void warnPlayer(String translationKey, Object... formatArgs) {
		ModUtil.warnPlayer(Minecraft.getInstance().player, translationKey, formatArgs);
	}

	public static FluidState getExtendedFluidState(BlockPos pos) {
		ClientWorld level = Minecraft.getInstance().level;
		return level == null ? Fluids.EMPTY.defaultFluidState() : ModUtil.getExtendedFluidState(level, pos);
	}
}
