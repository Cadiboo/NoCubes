package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.start;

/**
 * @author Cadiboo
 */
final class FMLHandshakeServerStateTransformer implements Opcodes {

	static void transform(final ClassNode classNode) {

//  // access flags 0x4008
//  static enum INNERCLASS net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7 null null
		classNode.innerClasses.add(new InnerClassNode(
				"net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7",
				null,
				null,
				0x4008
		));

//  // access flags 0x4019
//  public final static enum Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState; CONFIG
		classNode.fields.add(new FieldNode(
				0x4019,
				"CONFIG",
				"Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;",
				null,
				null
		));

		final List<MethodNode> methods = classNode.methods;

		//<clinit>
		{
			final String targetMethodDesc = "()V";
			final String targetMethodName = "<clinit>";

			start("Find " + targetMethodName);
			for (final MethodNode method : methods) {

				if (!method.name.equals(targetMethodName)) {
					log("Did not match method name " + targetMethodName + " - " + method.name);
					continue;
				} else if (!method.desc.equals(targetMethodDesc)) {
					log("Did not match method desc " + targetMethodDesc + " - " + method.desc);
					continue;
				}
				log("Matched method " + method.name + " " + method.desc);

				finish();

				start("Inject into <clinit>");
				injectIntoClinit(method.instructions);
				finish();
				break;

			}
		}

	}

	// Finds the last instruction NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$6
// then finds the next label
// then inserts after the label and before the label's instructions
// then sets the value of the next BIPUSH to 9
// then finds the last instruction PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState.$VALUES : [Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;
// then injects beforehand
	private static void injectIntoClinit(InsnList instructions) {

//   L5
//    LINENUMBER 120 L5
//    NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$6
//    DUP
//    LDC "ERROR"
//    ICONST_5
//    INVOKESPECIAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$6.<init> (Ljava/lang/String;I)V
//    PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState.ERROR : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;
//   L6
//    LINENUMBER 40 L6
//    BIPUSH 6
//    ANEWARRAY net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState
//    DUP
//    ICONST_0

//   L5
//    LINENUMBER 126 L5
//    NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$6
//    DUP
//    LDC "ERROR"
//    ICONST_5
//    INVOKESPECIAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$6.<init> (Ljava/lang/String;I)V
//    PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState.ERROR : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;
//   L6
//    LINENUMBER 134 L6
//    NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7
//    DUP
//    LDC "CONFIG"
//    BIPUSH 6
//    INVOKESPECIAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7.<init> (Ljava/lang/String;I)V
//    PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState.CONFIG : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;
//   L7
//    LINENUMBER 44 L7
//    BIPUSH 7
//    ANEWARRAY net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState
//    DUP
//    ICONST_0

//		NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7

		AbstractInsnNode last_NEW_FMLHandshakeServerState = null;
		final int arrayLength = instructions.size();
		for (int i = arrayLength; i >= 0; --i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == NEW) {
				TypeInsnNode instruction = (TypeInsnNode) insn;
				if (instruction.desc.equals("net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7")) {
					last_NEW_FMLHandshakeServerState = instruction;
					log("Found Injection Point");
					break;
				}
			}
		}
		if (last_NEW_FMLHandshakeServerState == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

		AbstractInsnNode firstLabelAfter_last_NEW_FMLHandshakeServerState = null;
		for (int i = instructions.indexOf(last_NEW_FMLHandshakeServerState); i < arrayLength; ++i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				firstLabelAfter_last_NEW_FMLHandshakeServerState = instruction;
				log("Found label " + instruction);
				break;
			}
		}
		if (firstLabelAfter_last_NEW_FMLHandshakeServerState == null) {
			throw new RuntimeException("Error: Couldn't find label!");
		}

		InsnList toInject = new InsnList();

		// Labels n stuff
		LabelNode originalInstructionsLabel = new LabelNode();

//    LINENUMBER 134 L6
//    NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7
//    DUP
//    LDC "CONFIG"
//    BIPUSH 6
//    INVOKESPECIAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7.<init> (Ljava/lang/String;I)V
//    PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState.CONFIG : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;

		// Make list of instructions to inject
		toInject.add(new TypeInsnNode(NEW, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7"));
		toInject.add(new InsnNode(DUP));
		toInject.add(new LdcInsnNode("CONFIG"));
		toInject.add(new IntInsnNode(BIPUSH, 6));
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKESPECIAL,
				//String owner
				"net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7",
				//String name
				"<init>",
				//String descriptor
				"(Ljava/lang/String;I)V",
				//boolean isInterface
				false
		));
		toInject.add(new FieldInsnNode(PUTSTATIC, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState", "CONFIG", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;"));

		toInject.add(originalInstructionsLabel);

		// Inject instructions
		instructions.insert(firstLabelAfter_last_NEW_FMLHandshakeServerState, toInject);

		IntInsnNode nextBIPUSH = null;
		for (int i = instructions.indexOf(originalInstructionsLabel); i < arrayLength; ++i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.INT_INSN) {
				if (instruction.getOpcode() == BIPUSH) {
					nextBIPUSH = (IntInsnNode) instruction;
					log("Found BIPUSH " + instruction);
					break;
				}
			}
		}
		if (nextBIPUSH == null) {
			throw new RuntimeException("Error: Couldn't find label!");
		}

		nextBIPUSH.operand = 7;

//		PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState.$VALUES : [Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;

		AbstractInsnNode last_PUTSTATIC_FMLHandshakeServerState$VALUES = null;
		for (int i = arrayLength; i >= 0; --i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == PUTSTATIC) {
				FieldInsnNode instruction = (FieldInsnNode) insn;
				if (instruction.owner.equals("net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState")) {
					if (instruction.name.equals("$VALUES")) {
						if (instruction.desc.equals("[Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;")) {
							last_PUTSTATIC_FMLHandshakeServerState$VALUES = instruction;
							log("Found Injection Point");
							break;
						}
					}
				}
			}
		}
		if (last_PUTSTATIC_FMLHandshakeServerState$VALUES == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

//    DUP
//    BIPUSH 6
//    GETSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState.CONFIG : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;
//    AASTORE

		InsnList toInject2 = new InsnList();

		// Make list of instructions to inject
		toInject2.add(new InsnNode(DUP));
		toInject2.add(new IntInsnNode(BIPUSH, 6));
		toInject2.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState", "CONFIG", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;"));
		toInject2.add(new InsnNode(AASTORE));

		// Inject instructions
		instructions.insertBefore(last_PUTSTATIC_FMLHandshakeServerState$VALUES, toInject2);

	}

}
