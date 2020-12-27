//package io.github.cadiboo.nocubes.network;
//
//import io.github.cadiboo.nocubes.config.NoCubesConfig;
//import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.network.PacketBuffer;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
//import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
//
//import static io.github.cadiboo.nocubes.network.NoCubesNetwork.executeIfPlayerHasPermission;
//
///**
// * @author Cadiboo
// */
//public final class C2SRequestSetTerrainMeshGenerator implements IMessage, IMessageHandler<C2SRequestSetTerrainMeshGenerator, IMessage> {
//
//	private /*final*/ MeshGeneratorType newGenerator;
//
//	public C2SRequestSetTerrainMeshGenerator(final MeshGeneratorType meshGeneratorType) {
//		this.newGenerator = meshGeneratorType;
//	}
//
//	public C2SRequestSetTerrainMeshGenerator() {
//	}
//
//	@Override
//	public IMessage onMessage(final C2SRequestSetTerrainMeshGenerator msg, final MessageContext context) {
//		executeIfPlayerHasPermission(context, "setTerrainMeshGenerator", () -> {
//			MeshGeneratorType newGenerator = msg.newGenerator;
//			NoCubesConfig.Server.setTerrainMeshGenerator(newGenerator);
//			NoCubesNetwork.CHANNEL.sendToAll(new S2CSetTerrainMeshGenerator(newGenerator));
//		});
//		return null;
//	}
//
//	@Override
//	public void fromBytes(final ByteBuf buf) {
//		this.newGenerator = MeshGeneratorType.VALUES[new PacketBuffer(buf).readInt()];
//	}
//
//	@Override
//	public void toBytes(final ByteBuf buf) {
//		new PacketBuffer(buf).writeInt(this.newGenerator.ordinal());
//	}
//
//}
