package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.KeybindingHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.Hooks;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * @author Cadiboo
 */
@Mod(NoCubes.MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	// Blocks is safe to use here, it gets inited before mods are constructed
	public static final SmoothableHandler smoothableHandler = SmoothableHandler.create(Blocks.STONE.defaultBlockState());

	public NoCubes() {
		NoCubesConfig.register(ModLoadingContext.get());
		NoCubesNetwork.register();
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		modBus.addListener((FMLClientSetupEvent event) -> KeybindingHandler.registerKeybindings());
		Hooks.loadClasses(FMLLoader.getDist());
	}

	/**
	 * For other mods.
	 * Add your blocks as being smoothable.
	 */
	public static void addSmoothable(Block... blocks) {
		for (var block : blocks)
			addSmoothable(ModUtil.getStates(block).toArray(new BlockState[0]));
	}

	/**
	 * For other mods.
	 * Add your blocks as being smoothable.
	 */
	public static void addSmoothable(BlockState... states) {
		NoCubesConfig.Smoothables.addDefault(states);
	}

}
