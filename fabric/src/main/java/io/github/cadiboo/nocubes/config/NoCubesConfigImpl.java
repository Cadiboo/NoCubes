package io.github.cadiboo.nocubes.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;

// TODO: REPLACE WITH AN ACTUAL CONFIG SYSTEM
/**
 * @see NoCubesConfig
 */
public final class NoCubesConfigImpl {

	public static void updateServerConfigSmoothable(boolean newValue, BlockState[] states) {
		var whitelist = new ArrayList<String>();
		var blacklist = new ArrayList<String>();
		NoCubesConfig.Smoothables.updateUserDefinedSmoothableStringLists(newValue, states, whitelist, blacklist);
		NoCubesConfig.Smoothables.recomputeInMemoryLookup(BuiltInRegistries.BLOCK.stream(), whitelist, blacklist, true);
	}

	public static void loadServerConfig() {
		loadDummyServerConfig();
	}

	public static void loadDefaultServerConfig() {
		loadDummyServerConfig();
	}

	public static void loadDummyServerConfig() {
		NoCubesConfig.Common.debugEnabled = true;
		NoCubesConfig.Client.render = true;
		NoCubesConfig.Client.renderSelectionBox = true;
		NoCubesConfig.Client.selectionBoxColor = new ColorParser.Color(0, 0, 0, 0x66).toRenderableColor();
		NoCubesConfig.Server.mesher = NoCubesConfig.Server.MesherType.SurfaceNets.instance;
		NoCubesConfig.Server.collisionsEnabled = true;
		NoCubesConfig.Server.tempMobCollisionsDisabled = true;
		NoCubesConfig.Server.extendFluidsRange = 3;
		updateServerConfigSmoothable(true, new BlockState[0]);
	}

	public static byte[] readConfigFileBytes() {
		return new byte[0];
	}

	public static void receiveSyncedServerConfig(byte[] configData) {
		// TODO: Actually use the data
		assert configData.length == 0; // Since we are just debugging, and have no config system yet
		loadDummyServerConfig();
	}

	/**
	 * Implementation of {@link NoCubesConfig.Common}
	 */
	public static class Common {
	}

	/**
	 * Implementation of {@link NoCubesConfig.Client}
	 */
	public static class Client {
		public static void updateRender(boolean render) {
			NoCubesConfig.Client.render = render;
		}
	}

	/**
	 * Implementation of {@link NoCubesConfig.Server}
	 */
	public static class Server {
	}

}
