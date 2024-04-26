package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

@Mod(NoCubes.MOD_ID)
public class Init {
	public Init(IEventBus modBus, ModContainer modContainer) {
		register(modBus, NeoForge.EVENT_BUS, ModLoadingContext.get());
	}
	public static void register(IEventBus modBus, IEventBus forgeBus, ModLoadingContext context) {
		NoCubesConfig.register(context, modBus);
		if (FMLEnvironment.dist.isClient())
			ClientInit.register(modBus, forgeBus);
		modBus.addListener((RegisterPayloadHandlerEvent event) -> NoCubesNetwork.register(event));
	}
}
