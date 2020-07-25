package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.C2SRequestUpdateSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.smoothable.ServerSmoothableChangeHandler;
import io.github.cadiboo.nocubes.util.BlockStateConverter;
import javafx.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Cadiboo
 */
public class KeybindHandler {

	private final List<Pair<KeyBinding, Runnable>> keybinds = new LinkedList<>();

	public KeybindHandler() {
		keybinds.add(makeKeybind("toggleSmoothable", GLFW.GLFW_KEY_N, this::toggleLookedAtSmoothable));
		keybinds.add(makeKeybind("toggleVisuals", GLFW.GLFW_KEY_O, this::toggleVisuals));
	}

	private void toggleVisuals() {
//		NoCubesConfig.
		System.out.println("toggleVisuals");
	}

	private Pair<KeyBinding, Runnable> makeKeybind(String name, int key, Runnable action) {
		KeyBinding keyBinding = new KeyBinding(NoCubes.MOD_ID + ".key." + name, key, NoCubes.MOD_ID + ".keycategory");
		return new Pair<>(keyBinding, action);
	}

	private void toggleLookedAtSmoothable() {
		Minecraft minecraft = Minecraft.getInstance();
		ClientWorld world = minecraft.world;
		RayTraceResult lookingAt = minecraft.objectMouseOver;
		if (world == null || lookingAt == null || lookingAt.getType() != RayTraceResult.Type.BLOCK)
			return;
		BlockRayTraceResult lookingAtBlock = ((BlockRayTraceResult) lookingAt);
		BlockState state = world.getBlockState(lookingAtBlock.getPos());
		boolean newValue = !NoCubes.smoothableHandler.isSmoothable(state);
		System.out.println("toggleLookedAtSmoothable to " + newValue + " for " +BlockStateConverter.toString(state));
		boolean singleplayer = minecraft.isSingleplayer() && !minecraft.getIntegratedServer().getPublic();
		if (singleplayer || !NoCubesNetwork.currentServerHasNoCubes) {
			// Either we're in singleplayer or the server doesn't have NoCubes
			// Allow the player to have visuals
			NoCubesConfig.Client.updateSmoothablePreference(newValue, state);
		}
		if (singleplayer || NoCubesNetwork.currentServerHasNoCubes){
			// We're on a server with NoCubes installed
			if (!minecraft.player.hasPermissionLevel(ServerSmoothableChangeHandler.REQUIRED_PERMISSION_LEVEL))
				// Not enough permission, don't send packet that will be denied
				return;
			// Send an update request packet
			NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestUpdateSmoothable(state, newValue));
		}
	}

	@SubscribeEvent
	public void onClientTickEvent(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		for (Pair<KeyBinding, Runnable> keybind : keybinds)
			if (keybind.getKey().isPressed())
				keybind.getValue().run();
	}

}
