package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = NoCubes.MOD_ID, value = Dist.CLIENT)
public final class KeybindingHandler {

	private static final List<Pair<KeyMapping, Runnable>> KEYBINDS = new LinkedList<>();

	public static void registerKeybindings() {
		KEYBINDS.clear();
		KEYBINDS.add(makeKeybinding("toggleVisuals", GLFW.GLFW_KEY_O, KeybindingHandler::toggleVisuals));
		KEYBINDS.add(makeKeybinding("toggleSmoothable", GLFW.GLFW_KEY_N, KeybindingHandler::toggleLookedAtSmoothable));
	}

	private static Pair<KeyMapping, Runnable> makeKeybinding(String name, int key, Runnable action) {
		var mapping = new KeyMapping(NoCubes.MOD_ID + ".key." + name, key, NoCubes.MOD_ID + ".keycategory");
		ClientRegistry.registerKeyBinding(mapping);
		return Pair.of(mapping, action);
	}

	@SubscribeEvent
	public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		for (Pair<KeyMapping, Runnable> keybind : KEYBINDS)
			if (keybind.getKey().consumeClick())
				keybind.getValue().run();
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
		BlockHitResult lookingAtBlock = ((BlockHitResult) lookingAt);
		BlockState state = world.getBlockState(lookingAtBlock.getBlockPos());
		boolean newValue = !NoCubes.smoothableHandler.isSmoothable(state);
		if (!NoCubesNetwork.currentServerHasNoCubes) {
			// The server doesn't have NoCubes, directly modify the smoothable state to hackily allow the player to have visuals
			NoCubes.smoothableHandler.setSmoothable(newValue, state);
			ClientUtil.reloadAllChunks();
		} else {
			// We're on a server (possibly singleplayer) with NoCubes installed
			if (!player.hasPermissions(REQUIRED_PERMISSION_LEVEL))
				// Not enough permission, don't send packet that will be denied
				return;
			// Send an update request packet
			NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestUpdateSmoothable(state, newValue));
		}
	}

}
