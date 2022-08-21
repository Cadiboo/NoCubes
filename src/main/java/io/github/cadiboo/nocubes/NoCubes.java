package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.KeyMappings;
import io.github.cadiboo.nocubes.client.render.OverlayRenderers;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

/**
 * @author Cadiboo
 */
@Mod(NoCubes.MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	public static final SmoothableHandler smoothableHandler = SmoothableHandler.create();

	public NoCubes() {
		var modBus = FMLJavaModLoadingContext.get().getModEventBus();
		NoCubesConfig.register(ModLoadingContext.get(), modBus);
		NoCubesNetwork.register();
		modBus.addListener((FMLClientSetupEvent event) -> {
			KeyMappings.register(MinecraftForge.EVENT_BUS);
			OverlayRenderers.register(MinecraftForge.EVENT_BUS);
		});
	}

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
