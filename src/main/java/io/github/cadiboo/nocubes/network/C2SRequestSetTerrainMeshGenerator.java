package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * Message from Client to Server to request setting the TerrainMeshGenerator to a new value.
 *
 * Validates that the sender has the permission to perform this action and if they do
 * sets the TerrainMeshGenerator to the new value and notifies all clients (including the
 * one that sent this packet) of the new value.
 *
 * @author Cadiboo
 */
public final class C2SRequestSetTerrainMeshGenerator {

	private final MeshGeneratorType newGenerator;

	public C2SRequestSetTerrainMeshGenerator(final MeshGeneratorType meshGeneratorType) {
		this.newGenerator = meshGeneratorType;
	}

	public static void encode(final C2SRequestSetTerrainMeshGenerator msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeVarInt(msg.newGenerator.ordinal());
	}

	public static C2SRequestSetTerrainMeshGenerator decode(final PacketBuffer packetBuffer) {
		return new C2SRequestSetTerrainMeshGenerator(MeshGeneratorType.getValues()[packetBuffer.readVarInt()]);
	}

	/**
	 * Called on the Server.
	 */
	public static void handle(final C2SRequestSetTerrainMeshGenerator msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null)
				return;
			if (sender.hasPermissionLevel(COMMAND_PERMISSION_LEVEL)) {
				final MeshGeneratorType newGenerator = msg.newGenerator;
				ConfigHelper.setTerrainMeshGenerator(newGenerator);
				NoCubesNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainMeshGenerator(newGenerator));
			} else
				sender.sendMessage(new TranslationTextComponent(MOD_ID + ".setTerrainMeshGeneratorNoPermission"));
		});
		context.setPacketHandled(true);
	}

}
