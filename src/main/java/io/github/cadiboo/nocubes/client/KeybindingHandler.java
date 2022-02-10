package io.github.cadiboo.nocubes.client;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

/**
 * @author Cadiboo
 */
public final class KeybindingHandler {

	private static final Logger LOG = LogManager.getLogger();

	public static void registerKeybindings(IEventBus bus) {
		LOG.debug("Registering keybindings");
		var keybindings = Lists.newArrayList(
			makeKeybinding("toggleVisuals", GLFW.GLFW_KEY_O, KeybindingHandler::toggleVisuals),
			makeKeybinding("toggleSmoothable", GLFW.GLFW_KEY_N, KeybindingHandler::toggleLookedAtSmoothable)
		);
		bus.addListener((TickEvent.ClientTickEvent event) -> {
			if (event.phase != TickEvent.Phase.END)
				return;
			for (var keybinding : keybindings)
				if (keybinding.getKey().consumeClick()) {
					LOG.debug("Keybinding {} pressed", keybinding.getKey().getName());
					keybinding.getValue().run();
				}
		});
	}

	private static Pair<KeyMapping, Runnable> makeKeybinding(String name, int key, Runnable action) {
		LOG.debug("Registering keybinding {}", name);
		var mapping = new KeyMapping(NoCubes.MOD_ID + ".key." + name, key, NoCubes.MOD_ID + ".keycategory");
		ClientRegistry.registerKeyBinding(mapping);
		return Pair.of(mapping, action);
	}

	private static void toggleVisuals() {
		if (NoCubesConfig.Client.render && NoCubesConfig.Server.forceVisuals) {
			ClientUtil.warnPlayer(NoCubes.MOD_ID + ".notification.visualsForcedByServer");
			return;
		}
		NoCubesConfig.Client.updateRender(!NoCubesConfig.Client.render);
		ClientUtil.reloadAllChunks("toggleVisuals was pressed");
	}

	private static void toggleLookedAtSmoothable() {
		var minecraft = Minecraft.getInstance();
		var world = minecraft.level;
		var player = minecraft.player;
		var lookingAt = minecraft.hitResult;
		if (world == null || player == null || lookingAt == null || lookingAt.getType() != HitResult.Type.BLOCK) {
			LOG.debug("toggleLookedAtSmoothable preconditions not met (world={}, player={}, lookingAt={})", world, player, lookingAt);
			return;
		}

		var targeted = ((BlockHitResult) lookingAt);
		var targetedState = world.getBlockState(targeted.getBlockPos());
		var newValue = !NoCubes.smoothableHandler.isSmoothable(targetedState);
		// Add all states if the player is not crouching (to make it easy to toggle on/off all leaves)
		// If the player needs fine-grained control over which specific blockstates are smoothable they can crouch
		// (Yes I know it says shift, it actually checks the crouch key)
		var states = player.isShiftKeyDown() ? new BlockState[]{targetedState} : ModUtil.getStates(targetedState.getBlock()).toArray(BlockState[]::new);

		LOG.debug("toggleLookedAtSmoothable currentServerHasNoCubes={}", NoCubesNetwork.currentServerHasNoCubes);
		if (!NoCubesNetwork.currentServerHasNoCubes) {
			// The server doesn't have NoCubes, directly modify the smoothable state to hackily allow the player to have visuals
			NoCubes.smoothableHandler.setSmoothable(newValue, states);
			ClientUtil.reloadAllChunks("toggleLookedAtSmoothable was pressed while connected to a server that doesn't have NoCubes installed");
		} else {
			// We're on a server (possibly singleplayer) with NoCubes installed
			if (C2SRequestUpdateSmoothable.checkPermissionAndNotifyIfUnauthorised(player, minecraft.getSingleplayerServer()))
				// Only send the packet if we have permission, don't send a packet that will be denied
				NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestUpdateSmoothable(newValue, states));
		}
	}

}
