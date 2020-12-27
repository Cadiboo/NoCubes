//package io.github.cadiboo.nocubes.network;
//
//import io.github.cadiboo.nocubes.client.ClientUtil;
//import io.github.cadiboo.nocubes.config.NoCubesConfig;
//import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
//import io.github.cadiboo.nocubes.future.DistExecutor;
//import io.netty.buffer.ByteBuf;
//import net.minecraft.client.Minecraft;
//import net.minecraft.network.PacketBuffer;
//import net.minecraft.util.text.TextComponentTranslation;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
//import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
//import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
//import net.minecraftforge.fml.relauncher.Side;
//
//import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
//
///**
// * @author Cadiboo
// */
//public final class S2CSetTerrainMeshGenerator implements IMessage, IMessageHandler<S2CSetTerrainMeshGenerator, IMessage> {
//
//	private /*final*/ MeshGeneratorType newGenerator;
//
//	public S2CSetTerrainMeshGenerator(MeshGeneratorType meshGeneratorType) {
//		this.newGenerator = meshGeneratorType;
//	}
//
//	public S2CSetTerrainMeshGenerator() {
//	}
//
//	@Override
//	public IMessage onMessage(S2CSetTerrainMeshGenerator msg, MessageContext context) {
//		Minecraft.getMinecraft().addScheduledTask(() -> DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
//			MeshGeneratorType newGenerator = msg.newGenerator;
//			NoCubesConfig.Server.terrainMeshGenerator = newGenerator;
//			Minecraft.getMinecraft().player.sendMessage(new TextComponentTranslation(MOD_ID + ".setTerrainMeshGenerator", newGenerator));
//			if (NoCubesConfig.Client.renderSmoothTerrain)
//				ClientUtil.tryReloadRenderers();
//		}));
//		return null;
//	}
//
//	@Override
//	public void fromBytes(ByteBuf buf) {
//		this.newGenerator = MeshGeneratorType.VALUES[new PacketBuffer(buf).readInt()];
//	}
//
//	@Override
//	public void toBytes(ByteBuf buf) {
//		new PacketBuffer(buf).writeInt(this.newGenerator.ordinal());
//	}
//
//}
