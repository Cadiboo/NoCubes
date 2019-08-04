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
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;

/**
 * @author Cadiboo
 */
public final class C2SRequestSetTerrainMeshGenerator {

	private final MeshGeneratorType newGenerator;

	public C2SRequestSetTerrainMeshGenerator(final MeshGeneratorType meshGeneratorType) {
		this.newGenerator = meshGeneratorType;
	}

	public static void encode(final C2SRequestSetTerrainMeshGenerator msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.newGenerator.ordinal());
	}

	public static C2SRequestSetTerrainMeshGenerator decode(final PacketBuffer packetBuffer) {
		return new C2SRequestSetTerrainMeshGenerator(MeshGeneratorType.VALUES[packetBuffer.readInt()]);
	}

	public static void handle(final C2SRequestSetTerrainMeshGenerator msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> {
			final ServerPlayerEntity sender = context.getSender();
			if (sender == null) {
				return;
			}
			if (sender.hasPermissionLevel(COMMAND_PERMISSION_LEVEL)) {
				final MeshGeneratorType newGenerator = msg.newGenerator;
				// Config saving is async so set it now
				Config.terrainMeshGenerator = newGenerator;
				ConfigHelper.setTerrainMeshGenerator(newGenerator);
				NoCubes.CHANNEL.send(PacketDistributor.ALL.noArg(), new S2CSetTerrainMeshGenerator(newGenerator));
			} else {
				sender.sendMessage(new TranslationTextComponent(MOD_ID + ".setTerrainMeshGeneratorNoPermission"));
			}
		});
		context.setPacketHandled(true);
	}

}
