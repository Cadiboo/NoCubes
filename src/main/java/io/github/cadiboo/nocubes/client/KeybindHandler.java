package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.C2SRequestSetSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;
import static org.lwjgl.input.Keyboard.*;

@Mod.EventBusSubscriber(modid = MOD_ID, value = CLIENT)
public class KeybindHandler {

	private static final List<Pair<KeyBinding, Runnable>> KEYBINDS = new LinkedList<>();

	static {
		KEYBINDS.add(makeKeybind("toggleVisuals", KEY_O, KeybindHandler::toggleVisuals));

		KEYBINDS.add(makeKeybind("toggleTerrainSmoothable", KEY_N, () -> toggleLookedAtSmoothable(IsSmoothable.TERRAIN)));
		KEYBINDS.add(makeKeybind("toggleLeavesSmoothable", KEY_K, () -> toggleLookedAtSmoothable(IsSmoothable.LEAVES)));
		KEYBINDS.add(makeKeybind("toggleTerrainCollisions", KEY_C, () -> { }));
	}

	private static Pair<KeyBinding, Runnable> makeKeybind(String name, int key, Runnable action) {
		KeyBinding keyBinding = new KeyBinding(NoCubes.MOD_ID + ".key." + name, key, NoCubes.MOD_ID + ".keycategory");
		return Pair.of(keyBinding, action);
	}

	@SubscribeEvent
	public static void onClientTickEvent(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		for (Pair<KeyBinding, Runnable> keybind : KEYBINDS)
			if (keybind.getKey().isPressed())
				keybind.getValue().run();
	}


	private static void reloadAllChunks(Minecraft minecraft) {
//		WorldRenderer worldRenderer = minecraft.worldRenderer;
		RenderGlobal worldRenderer = minecraft.renderGlobal;
		if (worldRenderer != null)
			worldRenderer.loadRenderers();
	}

	private static void toggleVisuals() {
		NoCubesConfig.Client.updateRender(!NoCubesConfig.Client.render);
		reloadAllChunks(ClientUtil.getMinecraft());
	}

	private static void toggleLookedAtSmoothable(IsSmoothable isSmoothable) {
		Minecraft minecraft = ClientUtil.getMinecraft();
		IBlockState state = getLookingAtState(minecraft);
		if (state == null)
			return;

		boolean newValue = !isSmoothable.test(state);
		boolean singleplayer = minecraft.isSingleplayer() && !minecraft.getIntegratedServer().getPublic();

		if (singleplayer || !NoCubesNetwork.currentServerHasNoCubes) {
			// Either we're in singleplayer or the server doesn't have NoCubes
			// Allow the player to have visuals
			if (isSmoothable != IsSmoothable.TERRAIN)
				throw new NotImplementedException("lazy");
			NoCubesConfig.Client.updateTerrainSmoothable(newValue, state);
			reloadAllChunks(minecraft);
		}

		if (singleplayer || NoCubesNetwork.currentServerHasNoCubes) {
			// We're on a server with NoCubes installed
			if (!ModUtil.doesPlayerHavePermission(minecraft.player))
				// Not enough permission, don't send packet that will be denied
				return;
			// Send an update request packet
			NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestSetSmoothable(isSmoothable, state, newValue));
		}
	}

	@Nullable
	private static IBlockState getLookingAtState(Minecraft minecraft) {
//		ClientWorld world = minecraft.world;
		WorldClient world = minecraft.world;
		RayTraceResult lookingAt = minecraft.objectMouseOver;
//		if (world == null || lookingAt == null || lookingAt.getType() != RayTraceResult.Type.BLOCK)
		if (world == null || lookingAt == null || lookingAt.typeOfHit != RayTraceResult.Type.BLOCK)
			return null;
//		BlockRayTraceResult lookingAtBlock = ((BlockRayTraceResult) lookingAt);
//		return world.getBlockState(lookingAtBlock.getPos());
		return world.getBlockState(lookingAt.getBlockPos());
	}

}
