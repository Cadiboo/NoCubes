package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class C2SRequestChangeTerrainMeshGenerator {

	private final MeshGeneratorType newGenerator;

	public C2SRequestChangeTerrainMeshGenerator(final MeshGeneratorType meshGeneratorType) {
		this.newGenerator = meshGeneratorType;
	}

	public static void encode(final C2SRequestChangeTerrainMeshGenerator msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.newGenerator.ordinal());
	}

	public static C2SRequestChangeTerrainMeshGenerator decode(final PacketBuffer packetBuffer) {
		return new C2SRequestChangeTerrainMeshGenerator(MeshGeneratorType.VALUES[packetBuffer.readInt()]);
	}

	public static void handle(final C2SRequestChangeTerrainMeshGenerator msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null) {
				return;
			}
			if (sender.server.getPlayerList().canSendCommands(sender.getGameProfile())) {
				final MeshGeneratorType newGenerator = msg.newGenerator;
				// Config saving is async so set it now
				Config.terrainMeshGenerator = newGenerator;
				ConfigHelper.setTerrainMeshGenerator(newGenerator);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CChangeTerrainMeshGenerator(newGenerator));
			} else {
				sender.sendMessage(new TranslationTextComponent(MOD_ID + ".changeTerrainMeshGeneratorNoPermission"));
			}
		});
	}

}
