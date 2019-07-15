package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientEventSubscriber;
import io.github.cadiboo.nocubes.client.TempClientConfigHacks;
import io.github.cadiboo.nocubes.client.gui.config.NoCubesConfigGui;
import io.github.cadiboo.nocubes.client.render.SmoothLightingFluidBlockRenderer;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import io.github.cadiboo.nocubes.network.C2SRequestAddSmoothable;
import io.github.cadiboo.nocubes.network.C2SRequestChangeExtendFluidsRange;
import io.github.cadiboo.nocubes.network.C2SRequestDisableCollisions;
import io.github.cadiboo.nocubes.network.C2SRequestEnableCollisions;
import io.github.cadiboo.nocubes.network.C2SRequestRemoveSmoothable;
import io.github.cadiboo.nocubes.network.S2CAddSmoothable;
import io.github.cadiboo.nocubes.network.S2CChangeExtendFluidsRange;
import io.github.cadiboo.nocubes.network.S2CDisableCollisions;
import io.github.cadiboo.nocubes.network.S2CEnableCollisions;
import io.github.cadiboo.nocubes.network.S2CRemoveSmoothable;
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

		int networkId = 0;
		CHANNEL.registerMessage(networkId++,
				C2SRequestAddSmoothable.class,
				C2SRequestAddSmoothable::encode,
				C2SRequestAddSmoothable::decode,
				C2SRequestAddSmoothable::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestChangeExtendFluidsRange.class,
				C2SRequestChangeExtendFluidsRange::encode,
				C2SRequestChangeExtendFluidsRange::decode,
				C2SRequestChangeExtendFluidsRange::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestDisableCollisions.class,
				C2SRequestDisableCollisions::encode,
				C2SRequestDisableCollisions::decode,
				C2SRequestDisableCollisions::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestEnableCollisions.class,
				C2SRequestEnableCollisions::encode,
				C2SRequestEnableCollisions::decode,
				C2SRequestEnableCollisions::handle
		);
		CHANNEL.registerMessage(networkId++,
				C2SRequestRemoveSmoothable.class,
				C2SRequestRemoveSmoothable::encode,
				C2SRequestRemoveSmoothable::decode,
				C2SRequestRemoveSmoothable::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CAddSmoothable.class,
				S2CAddSmoothable::encode,
				S2CAddSmoothable::decode,
				S2CAddSmoothable::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CChangeExtendFluidsRange.class,
				S2CChangeExtendFluidsRange::encode,
				S2CChangeExtendFluidsRange::decode,
				S2CChangeExtendFluidsRange::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CDisableCollisions.class,
				S2CDisableCollisions::encode,
				S2CDisableCollisions::decode,
				S2CDisableCollisions::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CEnableCollisions.class,
				S2CEnableCollisions::encode,
				S2CEnableCollisions::decode,
				S2CEnableCollisions::handle
		);
		CHANNEL.registerMessage(networkId++,
				S2CRemoveSmoothable.class,
				S2CRemoveSmoothable::encode,
				S2CRemoveSmoothable::decode,
				S2CRemoveSmoothable::handle
		);

	}

}
