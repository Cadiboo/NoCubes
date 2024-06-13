package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class ClientUtil {

	public static void warnPlayer(String translationKey, Object... formatArgs) {
		ModUtil.warnPlayer(Minecraft.getInstance().player, translationKey, formatArgs);
	}

	public static FluidState getExtendedFluidState(BlockPos pos) {
		var level = Minecraft.getInstance().level;
		return level == null ? Fluids.EMPTY.defaultFluidState() : ModUtil.getExtendedFluidState(level, pos);
	}

	public static void warnPlayerIfVisualsDisabled() {
		if (!NoCubesConfig.Client.render)
			warnPlayer(
				NoCubes.MOD_ID + ".notification.visualsDisabled",
				KeyMappings.translate(KeyMappings.TOGGLE_VISUALS),
				NoCubesConfig.Client.RENDER,
				NoCubes.platform.clientConfigComponent()
			);
	}

	public static void sendPlayerInfoMessage() {
		if (NoCubesConfig.Client.infoMessage)
			Minecraft.getInstance().player.sendSystemMessage(
				Component.translatable(NoCubes.MOD_ID + ".notification.infoMessage",
				KeyMappings.translate(KeyMappings.TOGGLE_SMOOTHABLE_BLOCK_TYPE),
				NoCubesConfig.Client.INFO_MESSAGE,
				NoCubes.platform.clientConfigComponent()
			).withStyle(ChatFormatting.GREEN));
	}
}
