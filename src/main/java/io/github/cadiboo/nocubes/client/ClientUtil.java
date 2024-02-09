package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.io.File;

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
				clientConfigComponent()
			);
	}

	public static void sendPlayerInfoMessage() {
		if (NoCubesConfig.Client.infoMessage)
			Minecraft.getInstance().player.sendSystemMessage(
				Component.translatable(NoCubes.MOD_ID + ".notification.infoMessage",
				KeyMappings.translate(KeyMappings.TOGGLE_SMOOTHABLE_BLOCK_TYPE),
				NoCubesConfig.Client.INFO_MESSAGE,
				clientConfigComponent()
			).withStyle(ChatFormatting.GREEN));
	}

	private static MutableComponent clientConfigComponent() {
		var configFile = new File(ConfigTracker.INSTANCE.getConfigFileName(NoCubes.MOD_ID, ModConfig.Type.CLIENT));
		return Component.literal(configFile.getName())
				.withStyle(ChatFormatting.UNDERLINE)
				.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, configFile.getAbsolutePath())));
	}
}
