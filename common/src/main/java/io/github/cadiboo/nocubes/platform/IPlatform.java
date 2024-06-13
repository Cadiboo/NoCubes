package io.github.cadiboo.nocubes.platform;

import io.github.cadiboo.nocubes.util.IBlockStateSerializer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public interface IPlatform {

	/**
	 * Gets the name of the current platform
	 *
	 * @return The name of the current platform.
	 */
	String getPlatformName();

	/**
	 * Checks if a mod with the given id is loaded.
	 *
	 * @param modId The mod to check if it is loaded.
	 * @return True if the mod is loaded, false otherwise.
	 */
	boolean isModLoaded(String modId);

	/**
	 * Check if the game is currently in a development environment.
	 *
	 * @return True if in a development environment, false otherwise.
	 */
	boolean isDevelopmentEnvironment();

	/**
	 * Gets the name of the environment type as a string.
	 *
	 * @return The name of the environment type.
	 */
	default String getEnvironmentName() {
		return isDevelopmentEnvironment() ? "development" : "production";
	}

	IBlockStateSerializer blockStateSerializer();

	boolean isPlant(BlockState state);

	void updateClientVisuals(boolean render);

	boolean trySendC2SRequestUpdateSmoothable(Player player, boolean newValue, BlockState[] states);

	Component clientConfigComponent();
}
