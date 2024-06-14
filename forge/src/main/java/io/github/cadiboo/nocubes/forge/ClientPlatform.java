package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfigImpl;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.platform.IClientPlatform;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class ClientPlatform implements IClientPlatform {
	private static final Logger LOG = LogManager.getLogger();

	@Override
	public void updateClientVisuals(boolean render) {
		NoCubesConfigImpl.Client.updateRender(render);
	}

	@Override
	public boolean trySendC2SRequestUpdateSmoothable(LocalPlayer player, boolean newValue, BlockState[] states) {
		LOG.debug("toggleLookedAtSmoothable currentServerHasNoCubes={}", NoCubesNetwork.currentServerHasNoCubes);
		if (!NoCubesNetwork.currentServerHasNoCubes) {
			// The server doesn't have NoCubes, directly modify the smoothable state to hackily allow the player to have visuals
			return false;
		} else {
			// We're on a server (possibly singleplayer) with NoCubes installed
			if (C2SRequestUpdateSmoothable.checkPermissionAndNotifyIfUnauthorised(player, Minecraft.getInstance().getSingleplayerServer()))
				// Only send the packet if we have permission, don't send a packet that will be denied
				NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestUpdateSmoothable(newValue, states));
		}
		return true;
	}

	@Override
	public Component clientConfigComponent() {
		var configFile = new File(ConfigTracker.INSTANCE.getConfigFileName(NoCubes.MOD_ID, ModConfig.Type.CLIENT));
		return Component.literal(configFile.getName())
			.withStyle(ChatFormatting.UNDERLINE)
			.withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, configFile.getAbsolutePath())));
	}
}
