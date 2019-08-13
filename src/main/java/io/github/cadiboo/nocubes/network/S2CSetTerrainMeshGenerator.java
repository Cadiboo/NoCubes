package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.util.DistExecutor;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;

/**
 * @author Cadiboo
 */
public final class S2CSetTerrainMeshGenerator implements IMessage, IMessageHandler<S2CSetTerrainMeshGenerator, IMessage> {

	private /*final*/ MeshGeneratorType newGenerator;

	public S2CSetTerrainMeshGenerator(final MeshGeneratorType meshGeneratorType) {
		this.newGenerator = meshGeneratorType;
	}

	public S2CSetTerrainMeshGenerator() {
	}

	@Override
	public IMessage onMessage(final S2CSetTerrainMeshGenerator msg, final MessageContext context) {
		Minecraft.getMinecraft().addScheduledTask(() -> DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
			final MeshGeneratorType newGenerator = msg.newGenerator;
			Config.terrainMeshGenerator = newGenerator;
			Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation(MOD_ID + ".setTerrainMeshGenerator", newGenerator));
			if (Config.renderSmoothTerrain) {
				ClientUtil.tryReloadRenderers();
			}
		}));
		return null;
	}

	@Override
	public void fromBytes(final ByteBuf buf) {
		this.newGenerator = MeshGeneratorType.VALUES[new PacketBuffer(buf).readInt()];
	}

	@Override
	public void toBytes(final ByteBuf buf) {
		new PacketBuffer(buf).writeInt(this.newGenerator.ordinal());
	}

}
