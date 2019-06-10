//package io.github.cadiboo.nocubes.tempcore;
//
//import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.Type;
//import org.objectweb.asm.tree.AbstractInsnNode;
//import org.objectweb.asm.tree.ClassNode;
//import org.objectweb.asm.tree.InnerClassNode;
//import org.objectweb.asm.tree.InsnList;
//import org.objectweb.asm.tree.InsnNode;
//import org.objectweb.asm.tree.LabelNode;
//import org.objectweb.asm.tree.LdcInsnNode;
//import org.objectweb.asm.tree.MethodInsnNode;
//import org.objectweb.asm.tree.MethodNode;
//import org.objectweb.asm.tree.VarInsnNode;
//
//import java.util.List;
//
//import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.finish;
//import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.log;
//import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.start;
//
///**
// * @author Cadiboo
// */
//final class FMLHandshakeCodecTransformer implements Opcodes {
//
//	static void transform(final ClassNode classNode) {
//
////  // access flags 0x9
////  public static INNERCLASS net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage S2CConfigData
//		classNode.innerClasses.add(new InnerClassNode(
//				"net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData",
//				"net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage",
//				"S2CConfigData",
//				0x9
//		));
//
//		final List<MethodNode> methods = classNode.methods;
//
//		//<init>
//		{
//			final String targetMethodDesc = "()V";
//			final String targetMethodName = "<init>";
//
//			start("Find " + targetMethodName);
//			for (final MethodNode method : methods) {
//
//				if (!method.name.equals(targetMethodName)) {
//					log("Did not match method name " + targetMethodName + " - " + method.name);
//					continue;
//				} else if (!method.desc.equals(targetMethodDesc)) {
//					log("Did not match method desc " + targetMethodDesc + " - " + method.desc);
//					continue;
//				}
//				log("Matched method " + method.name + " " + method.desc);
//
//				finish();
//
//				start("Inject into <clinit>");
//				injectIntoInit(method.instructions);
//				finish();
//				break;
//
//			}
//		}
//
//	}
//
//	// find fist ICONST_3
//// and fix next label
//// inject after next label before insns
//	private static void injectIntoInit(final InsnList instructions) {
//
////   L4
////    LINENUMBER 32 L4
////    ALOAD 0
////    ICONST_3
////    LDC Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$RegistryData;.class
////    INVOKEVIRTUAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeCodec.addDiscriminator (ILjava/lang/Class;)Lnet/minecraftforge/fml/common/network/FMLIndexedMessageToMessageCodec;
////    POP
////   L5
//
////   L4
////    LINENUMBER 32 L4
////    ALOAD 0
////    ICONST_3
////    LDC Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$RegistryData;.class
////    INVOKEVIRTUAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeCodec.addDiscriminator (ILjava/lang/Class;)Lnet/minecraftforge/fml/common/network/FMLIndexedMessageToMessageCodec;
////    POP
////   L5
////    LINENUMBER 34 L5
////    ALOAD 0
////    ICONST_4
////    LDC Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;.class
////    INVOKEVIRTUAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeCodec.addDiscriminator (ILjava/lang/Class;)Lnet/minecraftforge/fml/common/network/FMLIndexedMessageToMessageCodec;
////    POP
////   L6
//
//		AbstractInsnNode first_ICONST_3 = null;
//		final int arrayLength = instructions.size();
//		for (int i = 0; i < arrayLength; ++i) {
//			AbstractInsnNode insn = instructions.get(i);
//			if (insn.getOpcode() == ICONST_3) {
//				first_ICONST_3 = insn;
//				log("Found Injection Point");
//				break;
//			}
//		}
//		if (first_ICONST_3 == null) {
//			throw new RuntimeException("Error: Couldn't find injection point!");
//		}
//
//		AbstractInsnNode firstLabelAfter_first_ICONST_3 = null;
//		for (int i = instructions.indexOf(first_ICONST_3); i < arrayLength; ++i) {
//			AbstractInsnNode instruction = instructions.get(i);
//			if (instruction.getType() == AbstractInsnNode.LABEL) {
//				firstLabelAfter_first_ICONST_3 = instruction;
//				log("Found label " + instruction);
//				break;
//			}
//		}
//		if (firstLabelAfter_first_ICONST_3 == null) {
//			throw new RuntimeException("Error: Couldn't find label!");
//		}
//
//		InsnList toInject = new InsnList();
//
//		// Labels n stuff
//		LabelNode originalInstructionsLabel = new LabelNode();
//
////    LINENUMBER 34 L5
////    ALOAD 0
////    ICONST_4
////    LDC Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;.class
////    INVOKEVIRTUAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeCodec.addDiscriminator (ILjava/lang/Class;)Lnet/minecraftforge/fml/common/network/FMLIndexedMessageToMessageCodec;
////    POP
//
//		// Make list of instructions to inject
//		toInject.add(new VarInsnNode(ALOAD, 0));
//		toInject.add(new InsnNode(ICONST_4));
//		toInject.add(new LdcInsnNode(Type.getType("Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;.class")));
//		toInject.add(new MethodInsnNode(
//				//int opcode
//				INVOKEVIRTUAL,
//				//String owner
//				"net/minecraftforge/fml/common/network/handshake/FMLHandshakeCodec",
//				//String name
//				"addDiscriminator",
//				//String descriptor
//				"(ILjava/lang/Class;)Lnet/minecraftforge/fml/common/network/FMLIndexedMessageToMessageCodec;",
//				//boolean isInterface
//				false
//		));
//		toInject.add(new InsnNode(POP));
//
//		toInject.add(originalInstructionsLabel);
//
//		// Inject instructions
//		instructions.insert(firstLabelAfter_first_ICONST_3, toInject);
//
//	}
//
//}
