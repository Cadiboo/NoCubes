package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientEventSubscriber;
import io.github.cadiboo.nocubes.client.TempClientConfigHacks;
import io.github.cadiboo.nocubes.client.gui.config.NoCubesConfigGui;
import io.github.cadiboo.nocubes.client.render.SmoothLightingFluidBlockRenderer;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
@Mod(MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public NoCubes() {

		ModUtil.preloadClass("net.minecraft.block.BlockState", "BlockState");
		ModUtil.preloadClass("net.minecraft.world.IWorldReader", "IWorldReader");
		ModUtil.preloadClass("net.minecraft.world.World", "World");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			ModUtil.preloadClass("net.minecraft.client.renderer.chunk.ChunkRender", "ChunkRender");
			ModUtil.preloadClass("net.minecraft.client.renderer.FluidBlockRenderer", "FluidBlockRenderer");
		});

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener((FMLCommonSetupEvent event) ->
				ModList.get().getModContainerById(MOD_ID).ifPresent(ModUtil::launchUpdateDaemon)
		);
		modEventBus.addListener((FMLLoadCompleteEvent event) -> {
			LOGGER.debug("Replacing fluid renderer");
			final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
			final SmoothLightingFluidBlockRenderer smoothLightingBlockFluidRenderer = new SmoothLightingFluidBlockRenderer();
			blockRendererDispatcher.fluidRenderer = ClientEventSubscriber.smoothLightingBlockFluidRenderer = smoothLightingBlockFluidRenderer;
			LOGGER.debug("Replaced fluid renderer");
		});
		modEventBus.addListener((ModConfig.ModConfigEvent event) -> {
			final ModConfig config = event.getConfig();
			if (config.getSpec() == ConfigHolder.CLIENT_SPEC) {
				ConfigHelper.bakeClient(config);
			} else if (config.getSpec() == ConfigHolder.SERVER_SPEC) {
				ConfigHelper.bakeServer(config);
			}
		});
		modEventBus.addListener((FMLClientSetupEvent event) ->
				TempClientConfigHacks.doConfigHacks()
		);

		final ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
		modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);

		modLoadingContext.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> NoCubesConfigGui::new);

	}

}
