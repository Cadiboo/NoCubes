package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Proxy;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
@Mod(modid = MOD_ID)
public final class NoCubes {

	public static final String MOD_ID = "nocubes";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	@SidedProxy(serverSide = "io.github.cadiboo.nocubes.server.ServerProxy", clientSide = "io.github.cadiboo.nocubes.client.ClientProxy")
	public static Proxy PROXY = null;

	public NoCubes() {

//		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
//		modEventBus.addListener((ModConfig.ModConfigEvent event) -> {
//			final ModConfig config = event.getConfig();
//			if (config.getSpec() == ConfigHolder.CLIENT_SPEC) {
//				ConfigHelper.bakeClient(config);
//			} else if (config.getSpec() == ConfigHolder.SERVER_SPEC) {
//				ConfigHelper.bakeServer(config);
//			}
//		});
//
//		final ModLoadingContext modLoadingContext = ModLoadingContext.get();
//		modLoadingContext.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
//		modLoadingContext.registerConfig(ModConfig.Type.SERVER, ConfigHolder.SERVER_SPEC);

	}

	@Mod.EventHandler
	public void onPreInit(final FMLPreInitializationEvent event) {
		PROXY.preloadClasses();
		ModUtil.launchUpdateDaemon(Loader.instance().getIndexedModList().get(MOD_ID));
	}

	@Mod.EventHandler
	public void onPostInit(final FMLPostInitializationEvent event) {
		PROXY.replaceFluidRendererCauseImBored();
	}

}
