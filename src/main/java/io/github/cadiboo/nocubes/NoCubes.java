package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.Hooks;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;

/**
 * @author Cadiboo
 */
@Mod(NoCubes.MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	public static SmoothableHandler smoothableHandler;

	public NoCubes() {
		// Blocks is safe to use here, it gets inited before mods are constructed
		smoothableHandler = SmoothableHandler.create(Blocks.STONE.getDefaultState());
		NoCubesConfig.register(ModLoadingContext.get());
		NoCubesNetwork.register();
		FMLJavaModLoadingContext.get().getModEventBus().register(this);
//		ModLoadingContext.get().getActiveContainer().
//		MinecraftForge.EVENT_BUS.register(this);
		Hooks.loadClasses(FMLLoader.getDist());
	}

	/**
	 * For other mods.
	 * Add your blocks as being smoothable.
	 */
	public static void addSmoothable(Block... blocks) {
		for (final Block block : blocks)
			addSmoothable(block.getStateContainer().getValidStates().toArray(new BlockState[0]));
	}

	/**
	 * For other mods.
	 * Add your blocks as being smoothable.
	 */
	public static void addSmoothable(BlockState... states) {
//		for (final BlockState state : states)
//			// Can do this because all blockstates are reset when the server config is loaded
//			smoothableHandler.addSmoothable(state);
	}

}
