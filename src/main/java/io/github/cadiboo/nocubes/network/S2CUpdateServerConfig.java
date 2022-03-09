package io.github.cadiboo.nocubes.network;

import com.electronwill.nightconfig.core.file.FileConfig;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public class S2CUpdateServerConfig {

	public final byte[] data;

	public S2CUpdateServerConfig(byte[] data) {
		this.data = data;
	}

	public static S2CUpdateServerConfig create(ModConfig serverConfig) {
		assert FMLEnvironment.dist.isDedicatedServer() : "This should not be called on clients because they don't need their logical server config synced (they just reference it directly)";
		try {
			File file = ((FileConfig) serverConfig.getConfigData()).getFile();
			byte[] data = Files.readAllBytes(file.toPath());
			return new S2CUpdateServerConfig(data);
		} catch (IOException e) {
			throw new RuntimeException("Could not read NoCubes server config file!", e);
		}
	}

	public static void encode(S2CUpdateServerConfig msg, PacketBuffer buffer) {
		buffer.writeByteArray(msg.data);
	}

	public static S2CUpdateServerConfig decode(PacketBuffer buffer) {
		byte[] data = buffer.readByteArray();
		return new S2CUpdateServerConfig(data);
	}

	public static void handle(S2CUpdateServerConfig msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		NoCubesConfig.Hacks.receiveSyncedServerConfig(msg);
		ctx.setPacketHandled(true);
	}

	public byte[] getBytes() {
		return data;
	}

}
