package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.platform.IPlatform;
import io.github.cadiboo.nocubes.platform.PlatformLoader;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ServiceLoader;

/**
 * @author Cadiboo
 */
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	public static final SmoothableHandler smoothableHandler = SmoothableHandler.create();
	public static final IPlatform platform = PlatformLoader.load(IPlatform.class);

	// region API
	/**
	 * For other mods.
	 * Check if a block is smoothable in-world (takes user/server configuration into account).
	 */
	public static boolean isSmoothable(BlockState state) {
		return smoothableHandler.isSmoothable(state);
	}

	/**
	 * For other mods.
	 * Add your block(s) as being smoothable 'by default' (may be overridden in world by user/server config).
	 */
	public static void addSmoothable(Block... blocks) {
		for (var block : blocks)
			addSmoothable(ModUtil.getStates(block).toArray(new BlockState[0]));
	}

	/**
	 * For other mods.
	 * Add your block(s) as being smoothable 'by default' (may be overridden in world by user/server config).
	 */
	public static void addSmoothable(BlockState... states) {
		NoCubesConfig.Smoothables.addDefault(states);
	}
	// endregion

}
