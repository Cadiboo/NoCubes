package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.C2SRequestSetCollisions;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = NoCubes.MOD_ID, value = Dist.CLIENT)
public final class KeybindHandler {

	private static final List<Pair<KeyBinding, Runnable>> KEYBINDS = new LinkedList<>();

	@SubscribeEvent
	public static void onClientSetupEvent(FMLClientSetupEvent event) {
		KEYBINDS.clear();
		KEYBINDS.add(makeKeybind("toggleVisuals", GLFW.GLFW_KEY_O, KeybindHandler::toggleVisuals));
		KEYBINDS.add(makeKeybind("toggleSmoothable", GLFW.GLFW_KEY_N, KeybindHandler::toggleLookedAtSmoothable));
		KEYBINDS.add(makeKeybind("toggleCollisions", GLFW.GLFW_KEY_C, KeybindHandler::toggleCollisions));
	}

	private static Pair<KeyBinding, Runnable> makeKeybind(String name, int key, Runnable action) {
		KeyBinding keyBinding = new KeyBinding(NoCubes.MOD_ID + ".key." + name, key, NoCubes.MOD_ID + ".keycategory");
		ClientRegistry.registerKeyBinding(keyBinding);
		return Pair.of(keyBinding, action);
	}

	@SubscribeEvent
	public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		for (Pair<KeyBinding, Runnable> keybind : KEYBINDS)
			if (keybind.getKey().consumeClick())
				keybind.getValue().run();
	}

	private static void toggleVisuals() {
		if (NoCubesConfig.Client.render && NoCubesConfig.Server.forceVisuals) {
			Minecraft.getInstance().player.sendMessage(new TranslationTextComponent(NoCubes.MOD_ID + ".notification.visualsForcedByServer").withStyle(TextFormatting.RED), Util.NIL_UUID);
			return;
		}
		NoCubesConfig.Client.updateRender(!NoCubesConfig.Client.render);
		ClientUtil.reloadAllChunks(Minecraft.getInstance());
	}

	private static void toggleLookedAtSmoothable() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientWorld world = minecraft.level;
		ClientPlayerEntity player = minecraft.player;
		RayTraceResult lookingAt = minecraft.hitResult;
		if (world == null || player == null || lookingAt == null || lookingAt.getType() != RayTraceResult.Type.BLOCK)
			return;
		BlockRayTraceResult lookingAtBlock = ((BlockRayTraceResult) lookingAt);
		BlockState state = world.getBlockState(lookingAtBlock.getBlockPos());
		boolean newValue = !NoCubes.smoothableHandler.isSmoothable(state);
		if (!NoCubesNetwork.currentServerHasNoCubes) {
			// The server doesn't have NoCubes, directly modify the smoothable state to hackily allow the player to have visuals
			NoCubes.smoothableHandler.setSmoothable(newValue, state);
			ClientUtil.reloadAllChunks(Minecraft.getInstance());
		} else {
			// We're on a server (possibly singleplayer) with NoCubes installed
			if (!player.hasPermissions(REQUIRED_PERMISSION_LEVEL))
				// Not enough permission, don't send packet that will be denied
				return;
			// Send an update request packet
			NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestUpdateSmoothable(state, newValue));
		}
	}

	private static void toggleCollisions() {
		if (!NoCubesNetwork.currentServerHasNoCubes) {
			Minecraft.getInstance().player.sendMessage(new TranslationTextComponent(NoCubes.MOD_ID + ".notification.nocubesNotInstalledOnServerCollisionsUnavailable").withStyle(TextFormatting.RED), Util.NIL_UUID);
			return;
		}
		// Send an update request packet
		NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestSetCollisions(!NoCubesConfig.Server.collisionsEnabled));
	}

}
