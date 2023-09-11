package io.github.cadiboo.nocubes.network;

import com.electronwill.nightconfig.core.file.FileConfig;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.fml.config.ModConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Cadiboo
 */
public class S2CUpdateServerConfig implements IMessage, IMessageHandler<S2CUpdateServerConfig, IMessage> {
	byte[] data;

	public S2CUpdateServerConfig() {
	}

	public S2CUpdateServerConfig(final byte[] data) {
		this.data = data;
	}

	public static S2CUpdateServerConfig create(ModConfig serverConfig) {
		assert FMLCommonHandler.instance().getSide().isServer() : "This should not be called on clients because they don't need their logical server config synced (they just reference it directly)";
		try {
			File file = ((FileConfig) serverConfig.getConfigData()).getFile();
			byte[] data = Files.readAllBytes(file.toPath());
			return new S2CUpdateServerConfig(data);
		} catch (IOException e) {
			throw new RuntimeException("Could not read NoCubes server config file!", e);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		buffer.writeByteArray(this.data);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer buffer = new PacketBuffer(buf);
		this.data = buffer.readByteArray();
	}

	@Override
	public IMessage onMessage(S2CUpdateServerConfig msg, MessageContext context) {
		NoCubesConfig.Hacks.receiveSyncedServerConfig(msg);
		return null;
	}

	public byte[] getBytes() {
		return data;
	}

}
