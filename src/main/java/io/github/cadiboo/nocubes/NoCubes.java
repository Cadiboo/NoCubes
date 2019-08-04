package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientEventSubscriber;
import io.github.cadiboo.nocubes.client.TempClientConfigHacks;
import io.github.cadiboo.nocubes.client.gui.config.NoCubesConfigGui;
import io.github.cadiboo.nocubes.client.render.SmoothLightingFluidBlockRenderer;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import io.github.cadiboo.nocubes.network.C2SRequestAddTerrainSmoothable;
import io.github.cadiboo.nocubes.network.C2SRequestChangeExtendFluidsRange;
import io.github.cadiboo.nocubes.network.C2SRequestChangeTerrainMeshGenerator;
import io.github.cadiboo.nocubes.network.C2SRequestDisableTerrainCollisions;
import io.github.cadiboo.nocubes.network.C2SRequestEnableTerrainCollisions;
import io.github.cadiboo.nocubes.network.C2SRequestRemoveTerrainSmoothable;
import io.github.cadiboo.nocubes.network.S2CAddTerrainSmoothable;
import io.github.cadiboo.nocubes.network.S2CChangeExtendFluidsRange;
import io.github.cadiboo.nocubes.network.S2CChangeTerrainMeshGenerator;
import io.github.cadiboo.nocubes.network.S2CDisableTerrainCollisions;
import io.github.cadiboo.nocubes.network.S2CEnableTerrainCollisions;
import io.github.cadiboo.nocubes.network.S2CRemoveTerrainSmoothable;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
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

	private static final String NETWORK_PROTOCOL_VERSION = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(MOD_ID, "main"),
			() -> NETWORK_PROTOCOL_VERSION,
			NETWORK_PROTOCOL_VERSION::equals,
			NETWORK_PROTOCOL_VERSION::equals
	);

	public NoCubes() {

		LOGGER.debug("Preloading patched classes...");
		ModUtil.preloadClass("net.minecraft.block.BlockState", "BlockState");
		ModUtil.preloadClass("net.minecraft.world.IWorldReader", "IWorldReader");
		ModUtil.preloadClass("net.minecraft.world.World", "World");
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			ModUtil.preloadClass("net.minecraft.client.renderer.chunk.ChunkRender", "ChunkRender");
			ModUtil.preloadClass("net.minecraft.client.renderer.FluidBlockRenderer", "FluidBlockRenderer");
		});
		LOGGER.debug("Finished preloading patched classes");

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

		int networkId = 0;
		CHANNEL.registerMessage(networkId++,
				C2SRequestAddTerrainSmoothable.class,
				C2SRequestAddTerrainSmoothable::encode,
				C2SRequestAddTerrainSmoothable::decode,
				C2SRequestAddTerrainSmoothable::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestChangeExtendFluidsRange.class,
				C2SRequestChangeExtendFluidsRange::encode,
				C2SRequestChangeExtendFluidsRange::decode,
				C2SRequestChangeExtendFluidsRange::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestChangeTerrainMeshGenerator.class,
				C2SRequestChangeTerrainMeshGenerator::encode,
				C2SRequestChangeTerrainMeshGenerator::decode,
				C2SRequestChangeTerrainMeshGenerator::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestDisableTerrainCollisions.class,
				C2SRequestDisableTerrainCollisions::encode,
				C2SRequestDisableTerrainCollisions::decode,
				C2SRequestDisableTerrainCollisions::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestEnableTerrainCollisions.class,
				C2SRequestEnableTerrainCollisions::encode,
				C2SRequestEnableTerrainCollisions::decode,
				C2SRequestEnableTerrainCollisions::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestRemoveTerrainSmoothable.class,
				C2SRequestRemoveTerrainSmoothable::encode,
				C2SRequestRemoveTerrainSmoothable::decode,
				C2SRequestRemoveTerrainSmoothable::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CAddTerrainSmoothable.class,
				S2CAddTerrainSmoothable::encode,
				S2CAddTerrainSmoothable::decode,
				S2CAddTerrainSmoothable::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CChangeExtendFluidsRange.class,
				S2CChangeExtendFluidsRange::encode,
				S2CChangeExtendFluidsRange::decode,
				S2CChangeExtendFluidsRange::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CChangeTerrainMeshGenerator.class,
				S2CChangeTerrainMeshGenerator::encode,
				S2CChangeTerrainMeshGenerator::decode,
				S2CChangeTerrainMeshGenerator::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CDisableTerrainCollisions.class,
				S2CDisableTerrainCollisions::encode,
				S2CDisableTerrainCollisions::decode,
				S2CDisableTerrainCollisions::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CEnableTerrainCollisions.class,
				S2CEnableTerrainCollisions::encode,
				S2CEnableTerrainCollisions::decode,
				S2CEnableTerrainCollisions::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CRemoveTerrainSmoothable.class,
				S2CRemoveTerrainSmoothable::encode,
				S2CRemoveTerrainSmoothable::decode,
				S2CRemoveTerrainSmoothable::handle
		);

	}

}
