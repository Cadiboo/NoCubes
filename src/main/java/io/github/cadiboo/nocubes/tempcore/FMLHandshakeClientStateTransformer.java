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
final class FMLHandshakeClientStateTransformer implements Opcodes {

	static void transform(final ClassNode classNode) {

//  // access flags 0x4008
//  static enum INNERCLASS net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9 null null
		classNode.innerClasses.add(new InnerClassNode(
				"net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9",
				null,
				null,
				0x4008
		));

//  // access flags 0x4019
//  public final static enum Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState; PENDINGCONFIG
		classNode.fields.add(new FieldNode(
				0x4019,
				"PENDINGCONFIG",
				"Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;",
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

	// Finds the last instruction NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$8
// then finds the next label
// then inserts after the label and before the label's instructions
// then sets the value of the next BIPUSH to 9
// then finds the last instruction PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState.$VALUES : [Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;
// then injects beforehand
	private static void injectIntoClinit(InsnList instructions) {

//   L7
//    LINENUMBER 208 L7
//    NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$8
//    DUP
//    LDC "ERROR"
//    BIPUSH 7
//    INVOKESPECIAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$8.<init> (Ljava/lang/String;I)V
//    PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState.ERROR : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;
//   L8
//    LINENUMBER 55 L8
//    BIPUSH 8
//    ANEWARRAY net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState
//    DUP
//    ICONST_0

//   L7
//    LINENUMBER 158 L7
//    NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$8
//    DUP
//    LDC "ERROR"
//    BIPUSH 7
//    INVOKESPECIAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$8.<init> (Ljava/lang/String;I)V
//    PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState.ERROR : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;
//   L8
//    LINENUMBER 163 L8
//    NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9
//    DUP
//    LDC "PENDINGCONFIG"
//    BIPUSH 8
//    INVOKESPECIAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9.<init> (Ljava/lang/String;I)V
//    PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState.PENDINGCONFIG : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;
//   L9
//    LINENUMBER 40 L9
//    BIPUSH 9
//    ANEWARRAY net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState
//    DUP
//    ICONST_0

//		NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$8

		AbstractInsnNode last_NEW_FMLHandshakeClientState = null;
		final int arrayLength = instructions.size();
		for (int i = arrayLength; i >= 0; --i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == NEW) {
				TypeInsnNode instruction = (TypeInsnNode) insn;
				if (instruction.desc.equals("net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$8")) {
					last_NEW_FMLHandshakeClientState = instruction;
					log("Found Injection Point");
					break;
				}
			}
		}
		if (last_NEW_FMLHandshakeClientState == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

		AbstractInsnNode firstLabelAfter_last_NEW_FMLHandshakeClientState = null;
		for (int i = instructions.indexOf(last_NEW_FMLHandshakeClientState); i < arrayLength; ++i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				firstLabelAfter_last_NEW_FMLHandshakeClientState = instruction;
				log("Found label " + instruction);
				break;
			}
		}
		if (firstLabelAfter_last_NEW_FMLHandshakeClientState == null) {
			throw new RuntimeException("Error: Couldn't find label!");
		}

		InsnList toInject = new InsnList();

		// Labels n stuff
		LabelNode originalInstructionsLabel = new LabelNode();

//    LINENUMBER 163 L8
//    NEW net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9
//    DUP
//    LDC "PENDINGCONFIG"
//    BIPUSH 8
//    INVOKESPECIAL net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9.<init> (Ljava/lang/String;I)V
//    PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState.PENDINGCONFIG : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;

		// Make list of instructions to inject
		toInject.add(new TypeInsnNode(NEW, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9"));
		toInject.add(new InsnNode(DUP));
		toInject.add(new LdcInsnNode("PENDINGCONFIG"));
		toInject.add(new IntInsnNode(BIPUSH, 8));
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKESPECIAL,
				//String owner
				"net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9",
				//String name
				"<init>",
				//String descriptor
				"(Ljava/lang/String;I)V",
				//boolean isInterface
				false
		));
		toInject.add(new FieldInsnNode(PUTSTATIC, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState", "PENDINGCONFIG", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;"));

		toInject.add(originalInstructionsLabel);

		// Inject instructions
		instructions.insert(firstLabelAfter_last_NEW_FMLHandshakeClientState, toInject);

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

		nextBIPUSH.operand = 9;

//		PUTSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState.$VALUES : [Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;

		AbstractInsnNode last_PUTSTATIC_FMLHandshakeClientState$VALUES = null;
		for (int i = arrayLength; i >= 0; --i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == PUTSTATIC) {
				FieldInsnNode instruction = (FieldInsnNode) insn;
				if (instruction.owner.equals("net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState")) {
					if (instruction.name.equals("$VALUES")) {
						if (instruction.desc.equals("[Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;")) {
							last_PUTSTATIC_FMLHandshakeClientState$VALUES = instruction;
							log("Found Injection Point");
							break;
						}
					}
				}
			}
		}
		if (last_PUTSTATIC_FMLHandshakeClientState$VALUES == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

//    DUP
//    BIPUSH 8
//    GETSTATIC net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState.PENDINGCONFIG : Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;
//    AASTORE

		InsnList toInject2 = new InsnList();

		// Make list of instructions to inject
		toInject2.add(new InsnNode(DUP));
		toInject2.add(new IntInsnNode(BIPUSH, 8));
		toInject2.add(new FieldInsnNode(GETSTATIC, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState", "PENDINGCONFIG", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;"));
		toInject2.add(new InsnNode(AASTORE));

		// Inject instructions
		instructions.insertBefore(last_PUTSTATIC_FMLHandshakeClientState$VALUES, toInject2);

	}

}
