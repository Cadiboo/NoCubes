//package io.github.cadiboo.nocubes.tempcore;
//
//import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.tree.ClassNode;
//import org.objectweb.asm.tree.InnerClassNode;
//
///**
// * @author Cadiboo
// */
//final class FMLHandshakeMessageTransformer implements Opcodes {
//
//	static void transform(final ClassNode classNode) {
//
////  // access flags 0x9
////  public static INNERCLASS net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage S2CConfigData
//		classNode.innerClasses.add(new InnerClassNode(
//				"net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9",
//				null,
//				null,
//				0x4008
//		));
//
//	}
//
//}
