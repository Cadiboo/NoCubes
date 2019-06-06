package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigHolder;
import io.github.cadiboo.nocubes.server.ServerProxy;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Proxy;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.gui.GuiConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
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
	public static final Proxy PROXY = DistExecutor.runForDist(() -> () -> new ClientProxy(), () -> () -> new ServerProxy());
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public NoCubes() {

		PROXY.preloadClasses();

		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		modEventBus.addListener((FMLCommonSetupEvent event) ->
				ModUtil.launchUpdateDaemon(ModList.get().getModContainerById(MOD_ID).get())
		);
		modEventBus.addListener((FMLLoadCompleteEvent event) ->
				PROXY.replaceFluidRendererCauseImBored()
		);
		modEventBus.addListener((ModConfig.ModConfigEvent event) -> {
			final ModConfig config = event.getConfig();
			if (config.getSpec() == ConfigHolder.CLIENT_SPEC) {
				ConfigHelper.bakeClient(config);
			} else if (config.getSpec() == ConfigHolder.SERVER_SPEC) {
				ConfigHelper.bakeServer(config);
			}
		});

		final ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
		modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);

		modLoadingContext.registerExtensionPoint(ExtensionPoint.CONFIGGUIFACTORY, () -> GuiConfig::new);

	}

}
