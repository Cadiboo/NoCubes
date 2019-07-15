package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public final class C2SRequestChangeMeshGenerator {

	private final MeshGeneratorType newGenerator;

	public C2SRequestChangeMeshGenerator(final MeshGeneratorType meshGeneratorType) {
		this.newGenerator = meshGeneratorType;
	}

	public static void encode(final C2SRequestChangeMeshGenerator msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.newGenerator.ordinal());
	}

	public static C2SRequestChangeMeshGenerator decode(final PacketBuffer packetBuffer) {
		return new C2SRequestChangeMeshGenerator(MeshGeneratorType.VALUES[packetBuffer.readInt()]);
	}

	public static void handle(final C2SRequestChangeMeshGenerator msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender != null && sender.server.getPlayerList().canSendCommands(sender.getGameProfile())) {
				final MeshGeneratorType newGenerator = msg.newGenerator;
				// Config saving is async so set it now
				Config.terrainMeshGenerator = newGenerator;
				ConfigHelper.setTerrainMeshGenerator(newGenerator);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CChangeMeshGenerator(newGenerator));
			}
		});
	}

}
