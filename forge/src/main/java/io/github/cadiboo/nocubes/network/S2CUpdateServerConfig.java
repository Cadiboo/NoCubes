package io.github.cadiboo.nocubes.network;

import com.electronwill.nightconfig.core.file.FileConfig;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

public record S2CUpdateServerConfig(
	byte[] data
) {

	public static S2CUpdateServerConfig create(ModConfig serverConfig) {
		assert FMLEnvironment.dist.isDedicatedServer() : "This should not be called on clients because they don't need their logical server config synced (they just reference it directly)";
		try {
			var file = ((FileConfig) serverConfig.getConfigData()).getFile();
			var data = Files.readAllBytes(file.toPath());
			return new S2CUpdateServerConfig(data);
		} catch (IOException e) {
			throw new RuntimeException("Could not read NoCubes server config file!", e);
		}
	}

	public static void handle(S2CUpdateServerConfig msg, Supplier<CustomPayloadEvent.Context> contextSupplier) {
		var ctx = contextSupplier.get();
		NoCubesNetworkClient.handleS2CUpdateServerConfig(ctx::enqueueWork, msg.data());
		ctx.setPacketHandled(true);
	}
}
