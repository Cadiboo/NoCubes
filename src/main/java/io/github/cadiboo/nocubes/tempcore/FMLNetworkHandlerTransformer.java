package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.mapField;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.start;

/**
 * @author Cadiboo
 */
final class FMLNetworkHandlerTransformer implements Opcodes {

	// Local variable indexes
	private static final int ALOCALVARIABLE_this = 0;
	private static final int ALOCALVARIABLE_movingEntity = 1;
	private static final int ALOCALVARIABLE_area = 2;
	private static final int ALOCALVARIABLE_entityShape = 3;
	private static final int ILOCALVARIABLE_flag1 = 6;

	static void transform(final ClassNode classNode) {

		final List<MethodNode> methods = classNode.methods;

		//getCollisionBoxes
		{
			final String targetMethodDesc = "(Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$ModList;Lnet/minecraftforge/fml/relauncher/Side;)Ljava/lang/String;";
			final String targetMethodName = "checkModList";

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

				start("Inject checkModList hook");
				injectCheckModList(method.instructions);
				finish();
				break;

			}
		}
	}

	private static void injectCheckModList(InsnList instructions) {
		AbstractInsnNode firstLabel = null;
		int arrayLength = instructions.size();
		for (int i = 0; i < arrayLength; ++i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				firstLabel = instruction;
				log("Found injection point " + instruction);
				break;
			}
		}
		if (firstLabel == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

		InsnList toInject = new InsnList();

		// Make list of instructions to inject
		toInject.add(new VarInsnNode(ALOAD, 0));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKESTATIC,
				//String owner
				"io/github/cadiboo/nocubes/hooks/Hooks",
				//String name
				"onCheckModList",
				//String descriptor
				"(Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$ModList;Lnet/minecraftforge/fml/relauncher/Side;)V",
				//boolean isInterface
				false
		));
		toInject.add(new LabelNode());

		// Inject instructions
		instructions.insert(firstLabel, toInject);

	}

}
