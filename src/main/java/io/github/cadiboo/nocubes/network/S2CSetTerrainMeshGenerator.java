package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class S2CSetTerrainMeshGenerator {

	private final MeshGeneratorType newGenerator;

	public S2CSetTerrainMeshGenerator(final MeshGeneratorType meshGeneratorType) {
		this.newGenerator = meshGeneratorType;
	}

	public static void encode(final S2CSetTerrainMeshGenerator msg, final PacketBuffer packetBuffer) {
		packetBuffer.writeInt(msg.newGenerator.ordinal());
	}

	public static S2CSetTerrainMeshGenerator decode(final PacketBuffer packetBuffer) {
		return new S2CSetTerrainMeshGenerator(MeshGeneratorType.VALUES[packetBuffer.readInt()]);
	}

	public static void handle(final S2CSetTerrainMeshGenerator msg, final Supplier<NetworkEvent.Context> contextSupplier) {
		final NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			final MeshGeneratorType newGenerator = msg.newGenerator;
			NoCubesConfig.Server.terrainMeshGenerator = newGenerator;
			Minecraft.getInstance().player.sendMessage(new TranslationTextComponent(MOD_ID + ".setTerrainMeshGenerator", newGenerator));
			if (NoCubesConfig.Client.renderSmoothTerrain) {
				ClientUtil.tryReloadRenderers();
			}
		}));
		context.setPacketHandled(true);
	}

}
