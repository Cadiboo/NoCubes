package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.smoothable.ServerSmoothableChangeHandler;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = NoCubes.MOD_ID, value = Dist.CLIENT)
public final class KeybindHandler {

	private static final List<Pair<KeyBinding, Runnable>> KEYBINDS = new LinkedList<>();

	static {
		KEYBINDS.add(makeKeybind("toggleSmoothable", GLFW.GLFW_KEY_N, KeybindHandler::toggleLookedAtSmoothable));
		KEYBINDS.add(makeKeybind("toggleVisuals", GLFW.GLFW_KEY_O, KeybindHandler::toggleVisuals));
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

	private static void reloadAllChunks(Minecraft minecraft) {
		WorldRenderer worldRenderer = minecraft.levelRenderer;
		if (worldRenderer != null)
			worldRenderer.allChanged();
	}

	private static void toggleVisuals() {
		NoCubesConfig.Client.updateRender(!NoCubesConfig.Client.render);
		reloadAllChunks(Minecraft.getInstance());
	}

	private static void toggleLookedAtSmoothable() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientWorld world = minecraft.level;
		RayTraceResult lookingAt = minecraft.hitResult;
		if (world == null || lookingAt == null || lookingAt.getType() != RayTraceResult.Type.BLOCK)
			return;
		BlockRayTraceResult lookingAtBlock = ((BlockRayTraceResult) lookingAt);
		BlockState state = world.getBlockState(lookingAtBlock.getBlockPos());
		boolean newValue = !NoCubes.smoothableHandler.isSmoothable(state);
		boolean singleplayer = minecraft.hasSingleplayerServer() && !minecraft.getSingleplayerServer().isPublished();
		if (singleplayer || !NoCubesNetwork.currentServerHasNoCubes) {
			// Either we're in singleplayer or the server doesn't have NoCubes
			// Allow the player to have visuals
			NoCubesConfig.Client.updateSmoothablePreference(newValue, state);
			reloadAllChunks(minecraft);
		}
		if (singleplayer || NoCubesNetwork.currentServerHasNoCubes) {
			// We're on a server with NoCubes installed
			if (!minecraft.player.hasPermissions(ServerSmoothableChangeHandler.REQUIRED_PERMISSION_LEVEL))
				// Not enough permission, don't send packet that will be denied
				return;
			// Send an update request packet
			NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestUpdateSmoothable(state, newValue));
		}
	}

}
