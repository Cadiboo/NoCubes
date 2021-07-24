package io.github.cadiboo.nocubes.network;

import com.electronwill.nightconfig.core.file.FileConfig;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public class S2CUpdateServerConfig {

	private final byte[] data;

	public static S2CUpdateServerConfig create(ModConfig serverConfig) {
		assert FMLEnvironment.dist.isDedicatedServer();
		try {
			File file = ((FileConfig) serverConfig.getConfigData()).getFile();
			byte[] data = Files.readAllBytes(file.toPath());
			return new S2CUpdateServerConfig(data);
		} catch (IOException e) {
			throw new RuntimeException("Could not read NoCubes server config file!", e);
		}
	}

	private S2CUpdateServerConfig(byte[] data) {
		this.data = data;
	}

	public static void encode(S2CUpdateServerConfig msg, FriendlyByteBuf buffer) {
		buffer.writeByteArray(msg.data);
	}

	public static S2CUpdateServerConfig decode(FriendlyByteBuf buffer) {
		byte[] data = buffer.readByteArray();
		return new S2CUpdateServerConfig(data);
	}

	public static void handle(S2CUpdateServerConfig msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		NoCubesConfig.ReloadHacks.receiveSyncedServerConfig(msg);
		ctx.setPacketHandled(true);
	}

	public byte[] getBytes() {
		return data;
	}

}
