package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.start;

/**
 * @author Cadiboo
 */
final class NetHandlerPlayClientTransformer implements Opcodes {

	static void transform(final ClassNode classNode) {

		final List<MethodNode> methods = classNode.methods;

		{
			final String targetMethodDesc = "(Lnet/minecraft/network/play/server/SPacketJoinGame;)V";
			final String targetMethodName = "handleJoinGame";

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

				start("Inject handleJoinGame hook");
				injectHandleJoinGameHook(method.instructions);
				finish();
				break;

			}
		}
	}

	private static void injectHandleJoinGameHook(InsnList instructions) {
		AbstractInsnNode firstReturn = null;
		int arrayLength = instructions.size();
		for (int i = 0; i < arrayLength; ++i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.INSN && instruction.getOpcode() == RETURN) {
				firstReturn = instruction;
				log("Found injection point " + instruction);
				break;
			}
		}
		if (firstReturn == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

		InsnList toInject = new InsnList();

		// Make list of instructions to inject
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKESTATIC,
				//String owner
				"io/github/cadiboo/nocubes/hooks/Hooks",
				//String name
				"handleJoinGame",
				//String descriptor
				"(Lnet/minecraft/client/network/NetHandlerPlayClient;)V",
				//boolean isInterface
				false
		));
		toInject.add(new LabelNode());

		// Inject instructions
		instructions.insertBefore(firstReturn, toInject);

	}

}
