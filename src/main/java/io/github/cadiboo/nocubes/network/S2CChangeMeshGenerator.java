package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * @author Cadiboo
 */
public final class S2CChangeMeshGenerator {

	private final MeshGeneratorType newGenerator;

	public S2CChangeMeshGenerator(final MeshGeneratorType meshGeneratorType) {
		this.newGenerator = meshGeneratorType;
	}

	public static void encode(final S2CChangeMeshGenerator msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.newGenerator.ordinal());
	}

	public static S2CChangeMeshGenerator decode(final PacketBuffer packetBuffer) {
		return new S2CChangeMeshGenerator(MeshGeneratorType.VALUES[packetBuffer.readInt()]);
	}

	public static void handle(final S2CChangeMeshGenerator msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		contextSupplier.get().enqueueWork(() -> Config.terrainMeshGenerator = msg.newGenerator);
	}

}
