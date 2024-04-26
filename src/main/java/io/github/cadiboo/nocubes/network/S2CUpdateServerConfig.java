package io.github.cadiboo.nocubes.network;

import com.electronwill.nightconfig.core.file.FileConfig;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Cadiboo
 */
public record S2CUpdateServerConfig(
	byte[] data
) implements CustomPacketPayload {
	public static ResourceLocation ID = new ResourceLocation(NoCubes.MOD_ID, S2CUpdateServerConfig.class.getSimpleName().toLowerCase());

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

	public static void encode(S2CUpdateServerConfig msg, FriendlyByteBuf buffer) {
		buffer.writeByteArray(msg.data);
	}

	public static S2CUpdateServerConfig decode(FriendlyByteBuf buffer) {
		var data = buffer.readByteArray();
		return new S2CUpdateServerConfig(data);
	}

	public static void handle(S2CUpdateServerConfig msg, ConfigurationPayloadContext ctx) {
		handle(msg);
	}

	private static void handle(S2CUpdateServerConfig msg) {
		NoCubesConfig.Hacks.receiveSyncedServerConfig(msg.data);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		encode(this, buffer);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
}
