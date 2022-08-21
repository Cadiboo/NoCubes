package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.KeyMappings;
import io.github.cadiboo.nocubes.client.render.OverlayRenderers;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

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
		if (FMLEnvironment.dist.isClient())
			modBus.addListener((RegisterKeyMappingsEvent event) -> KeyMappings.register(event, MinecraftForge.EVENT_BUS));
		NoCubesNetwork.register();
		modBus.addListener((FMLClientSetupEvent event) -> OverlayRenderers.register(MinecraftForge.EVENT_BUS));
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
	 * Add your block(s) as being smoothable 'by default' (may be overridden/server in world by user config).
	 */
	public static void addSmoothable(Block... blocks) {
		for (var block : blocks)
			addSmoothable(ModUtil.getStates(block).toArray(new BlockState[0]));
	}

	/**
	 * For other mods.
	 * Add your block(s) as being smoothable 'by default' (may be overridden/server in world by user config).
	 */
	public static void addSmoothable(BlockState... states) {
		NoCubesConfig.Smoothables.addDefault(states);
	}
	// endregion

}
