package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(NoCubes.MOD_ID)
public class Init {
	public Init() {
		register(FMLJavaModLoadingContext.get().getModEventBus(), MinecraftForge.EVENT_BUS, ModLoadingContext.get());
	}
	public static void register(IEventBus modBus, IEventBus forgeBus, ModLoadingContext context) {
		NoCubesConfig.register(context, modBus);
		if (FMLEnvironment.dist.isClient())
			ClientInit.register(modBus, forgeBus);
		NoCubesNetwork.register();
	}
}
