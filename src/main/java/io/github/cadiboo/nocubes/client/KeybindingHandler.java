package io.github.cadiboo.nocubes.client;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public final class KeybindingHandler {

	public static void registerKeybindings(IEventBus bus) {
		var keybindings = Lists.newArrayList(
			makeKeybinding("toggleVisuals", GLFW.GLFW_KEY_O, KeybindingHandler::toggleVisuals),
			makeKeybinding("toggleSmoothable", GLFW.GLFW_KEY_N, KeybindingHandler::toggleLookedAtSmoothable)
		);
		bus.addListener((TickEvent.ClientTickEvent event) -> {
			if (event.phase != TickEvent.Phase.END)
				return;
			for (var keybinding : keybindings)
				if (keybinding.getKey().consumeClick())
					keybinding.getValue().run();
		});
	}

	private static Pair<KeyMapping, Runnable> makeKeybinding(String name, int key, Runnable action) {
		var mapping = new KeyMapping(NoCubes.MOD_ID + ".key." + name, key, NoCubes.MOD_ID + ".keycategory");
		ClientRegistry.registerKeyBinding(mapping);
		return Pair.of(mapping, action);
	}

	private static void toggleVisuals() {
		if (NoCubesConfig.Client.render && NoCubesConfig.Server.forceVisuals) {
			Minecraft.getInstance().player.sendMessage(new TranslatableComponent(NoCubes.MOD_ID + ".notification.visualsForcedByServer").withStyle(ChatFormatting.RED), Util.NIL_UUID);
			return;
		}
		NoCubesConfig.Client.updateRender(!NoCubesConfig.Client.render);
		ClientUtil.reloadAllChunks();
	}

	private static void toggleLookedAtSmoothable() {
		var minecraft = Minecraft.getInstance();
		var world = minecraft.level;
		var player = minecraft.player;
		var lookingAt = minecraft.hitResult;
		if (world == null || player == null || lookingAt == null || lookingAt.getType() != HitResult.Type.BLOCK)
			return;

		var targeted = ((BlockHitResult) lookingAt);
		var targetedState = world.getBlockState(targeted.getBlockPos());
		var newValue = !NoCubes.smoothableHandler.isSmoothable(targetedState);
		// Add all states if the player is pressing shift (to make it east to toggle on/off all leaves)
		var states = player.isShiftKeyDown() ? ModUtil.getStates(targetedState.getBlock()).toArray(BlockState[]::new): new BlockState[] {targetedState};

		if (!NoCubesNetwork.currentServerHasNoCubes) {
			// The server doesn't have NoCubes, directly modify the smoothable state to hackily allow the player to have visuals
			NoCubes.smoothableHandler.setSmoothable(newValue, states);
			ClientUtil.reloadAllChunks();
		} else {
			// We're on a server (possibly singleplayer) with NoCubes installed
			if (!player.hasPermissions(REQUIRED_PERMISSION_LEVEL))
				// Not enough permission, don't send packet that will be denied
				return;
			// Send an update request packet
			NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestUpdateSmoothable(newValue, states));
		}
	}

}
