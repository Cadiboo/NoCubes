package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.network.NoCubesNetwork.REQUIRED_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public class C2SRequestSetCollisions {

	private final boolean newValue;

	public C2SRequestSetCollisions(boolean newValue) {
		this.newValue = newValue;
	}

	public static C2SRequestSetCollisions decode(PacketBuffer buffer) {
		boolean newValue = buffer.readBoolean();
		return new C2SRequestSetCollisions(newValue);
	}

	public static void encode(C2SRequestSetCollisions msg, PacketBuffer buffer) {
		buffer.writeBoolean(msg.newValue);
	}

	public static void handle(C2SRequestSetCollisions msg, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context ctx = contextSupplier.get();
		ServerPlayerEntity sender = ctx.getSender();
		boolean hasPermission = sender.hasPermissions(REQUIRED_PERMISSION_LEVEL);
		if (hasPermission) {
			boolean newValue = msg.newValue;
			// Guards against useless config reload and/or someone spamming these packets to the server and the server spamming all clients
			if (NoCubesConfig.Server.collisionsEnabled != newValue) {
				NoCubesConfig.Server.updateCollisions(newValue);
				// Send back update to all clients
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetCollisions(newValue));
			} else
				// Somehow the client is out of sync, just notify them
				NoCubesNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new S2CSetCollisions(newValue));
		} else
			sender.sendMessage(new TranslationTextComponent(NoCubes.MOD_ID + ".command.setCollisionsNoPermission"), Util.NIL_UUID);
		ctx.setPacketHandled(true);
	}

}
