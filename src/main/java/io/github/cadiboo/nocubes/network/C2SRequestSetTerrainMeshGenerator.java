package io.github.cadiboo.nocubes.network;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_LEVEL;
import static io.github.cadiboo.nocubes.util.ModUtil.COMMAND_PERMISSION_NAME;

/**
 * @author Cadiboo
 */
public final class C2SRequestSetTerrainMeshGenerator implements IMessage, IMessageHandler<C2SRequestSetTerrainMeshGenerator, IMessage> {

	private /*final*/ MeshGeneratorType newGenerator;

	public C2SRequestSetTerrainMeshGenerator(final MeshGeneratorType meshGeneratorType) {
		this.newGenerator = meshGeneratorType;
	}

	public C2SRequestSetTerrainMeshGenerator() {
	}

	@Override
	public IMessage onMessage(final C2SRequestSetTerrainMeshGenerator msg, final MessageContext context) {
		context.getServerHandler().player.server.addScheduledTask(() -> {
			final EntityPlayerMP sender = context.getServerHandler().player;
			if (sender == null) {
				return;
			}
			if (sender.canUseCommand(COMMAND_PERMISSION_LEVEL, COMMAND_PERMISSION_NAME)) {
				final MeshGeneratorType newGenerator = msg.newGenerator;
				// Config saving is async so set it now
				Config.terrainMeshGenerator = newGenerator;
				ConfigHelper.setTerrainMeshGenerator(newGenerator);
				NoCubes.CHANNEL.sendToAll(new S2CSetTerrainMeshGenerator(newGenerator));
			} else {
				sender.sendMessage(new TextComponentTranslation(MOD_ID + ".setTerrainMeshGeneratorNoPermission"));
			}
		});
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
