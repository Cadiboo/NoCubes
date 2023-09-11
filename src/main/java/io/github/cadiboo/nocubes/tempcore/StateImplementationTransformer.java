package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.mapMethod;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.start;

/**
 * @author Cadiboo
 */
final class StateImplementationTransformer implements Opcodes {

	// Local variable indexes
	private static final int ALOCALVARIABLE_this = 0;
	private static final int ILOCALVARIABLE_newIsSmoothable = 1;

	static void transform(final ClassNode classNode) {

		log("Adding Field nocubes_isSmoothable");
		classNode.fields.add(new FieldNode(
//					final int access,
				ACC_PUBLIC,
//					final String name,
				"nocubes_isSmoothable",
//					final String descriptor,
				"Z",
//					final String signature,
				null,
//					final Object value
				false
		));
		log("Finished adding Field nocubes_isSmoothable");

		final List<MethodNode> methods = classNode.methods;

		//causesSuffocation
		{
			final String targetMethodDesc = "()Z";
			final String targetMethodName = mapMethod("net/minecraft/block/state/IBlockProperties", "func_191058_s", targetMethodDesc);

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

				start("Apply " + "causesSuffocation hook");
				injectCausesSuffocationHook(method.instructions);
				finish();
				break;

			}
		}

		//isOpaqueCube
		{
			final String targetMethodDesc = "()Z";
			final String targetMethodName = mapMethod("net/minecraft/block/state/IBlockProperties", "func_185914_p", targetMethodDesc);

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

				start("Apply " + "isOpaqueCube hook");
				injectIsOpaqueCubeHook(method.instructions);
				finish();
				break;

			}
		}

		classNode.interfaces.add("io/github/cadiboo/nocubes/util/INoCubesBlockState");
		log("Adding methods...");
		{
			start("Adding nocubes_isSmoothable");
			methods.add(make_nocubes_isSmoothable());
			finish();
			start("Adding nocubes_setSmoothable");
			methods.add(make_nocubes_setSmoothable());
			finish();
		}
		log("Finished adding methods");

	}

//// access flags 0x2
//  private Z nocubes_isSmoothable
//
//  // access flags 0x1
//  public nocubes_isSmoothable()Z
//   L0
//    LINENUMBER 27 L0
//    ALOAD 0
//    GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.nocubes_isSmoothable : Z
//    IRETURN
//   L1
//    LOCALVARIABLE this Lnet/minecraft/block/state/BlockStateContainer$StateImplementation; L0 L1 0
//    MAXSTACK = 1
//    MAXLOCALS = 1
//
//  // access flags 0x1
//  public nocubes_setSmoothable(Z)V
//   L0
//    LINENUMBER 32 L0
//    ALOAD 0
//    ILOAD 1
//    PUTFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.nocubes_isSmoothable : Z
//   L1
//    LINENUMBER 33 L1
//    RETURN
//   L2
//    LOCALVARIABLE this Lnet/minecraft/block/state/BlockStateContainer$StateImplementation; L0 L2 0
//    LOCALVARIABLE isTerrainSmoothable Z L0 L2 1
//    MAXSTACK = 2
//    MAXLOCALS = 2

	private static MethodNode make_nocubes_isSmoothable() {

		MethodNode method = new MethodNode(
//		final int access,
				ACC_PUBLIC,
//		final String name,
				"nocubes_isSmoothable",
//		final String descriptor,
				"()Z",
//		final String signature,
				null,
//		final String[] exceptions
				null
		);
		method.instructions.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
		method.instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "nocubes_isSmoothable", "Z"));
		method.instructions.add(new InsnNode(IRETURN));

		return method;

	}

	private static MethodNode make_nocubes_setSmoothable() {

		MethodNode method = new MethodNode(
//		final int access,
				ACC_PUBLIC,
//		final String name,
				"nocubes_setSmoothable",
//		final String descriptor,
				"(Z)V",
//		final String signature,
				null,
//		final String[] exceptions
				null
		);
		method.instructions.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
		method.instructions.add(new VarInsnNode(ILOAD, ILOCALVARIABLE_newIsSmoothable));
		method.instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "nocubes_isSmoothable", "Z"));
		method.instructions.add(new InsnNode(RETURN));

		return method;

	}

	// 1) Find first label
// 2) inject right after first label
	private static void injectIsOpaqueCubeHook(InsnList instructions) {

//	return this.getBlock().isOpaqueCube(this);

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.config.NoCubesConfig.Client.render && this.nocubes_isSmoothable()) return false;
//	// NoCubes End
//	return this.getBlock().isOpaqueCube(this);

//  public default isOpaqueCube()Z
//   L0
//    LINENUMBER 212 L0
//    ALOAD 0
//    INVOKEINTERFACE net/minecraft/block/state/IBlockProperties.getBlock ()Lnet/minecraft/block/Block; (itf)
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/Block.isOpaqueCube (Lnet/minecraft/block/state/IBlockProperties;)Z
//    IRETURN

//  public default isOpaqueCube()Z
//   L0
//    LINENUMBER 213 L0
//    GETSTATIC io/github/cadiboo/nocubes/config/NoCubesConfig.Client.render : Z
//    IFEQ L1
//    ALOAD 0
//    INVOKEINTERFACE net/minecraft/block/state/IBlockProperties.nocubes_isSmoothable ()Z (itf)
//    IFEQ L1
//    ICONST_0
//    IRETURN
//   L1
//   L2
//    LINENUMBER 213 L2
//   FRAME SAME
//    ALOAD 0
//    INVOKEINTERFACE net/minecraft/block/state/IBlockProperties.getBlock ()Lnet/minecraft/block/Block; (itf)
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/Block.isOpaqueCube (Lnet/minecraft/block/state/IBlockProperties;)Z
//    IRETURN

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

		// Labels n stuff
		LabelNode originalInstructionsLabel = new LabelNode();

		// Make list of instructions to inject
		toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/config/NoCubesConfig$Client", "render", "Z"));
		toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
		toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKEINTERFACE,
				//String owner
				"net/minecraft/block/state/IBlockProperties",
				//String name
				"nocubes_isSmoothable",
				//String descriptor
				"()Z",
				//boolean isInterface
				true
		));
		toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new InsnNode(IRETURN));

		toInject.add(originalInstructionsLabel);

		// Inject instructions
		instructions.insert(firstLabel, toInject);

	}

	// 1) Find first label
// 2) inject right after first label
	private static void injectCausesSuffocationHook(InsnList instructions) {

//	return this.getBlock().causesSuffocation(this);

//	// NoCubes Start
//	if (NoCubesConfig.Server.collisionsEnabled && this.nocubes_isSmoothable()) return false;
//	// NoCubes End
//	return this.getBlock().causesSuffocation(this);

//  public default causesSuffocation()Z
//   L0
//    LINENUMBER 321 L0
//    ALOAD 0
//    INVOKEINTERFACE net/minecraft/block/state/IBlockProperties.getBlock ()Lnet/minecraft/block/Block; (itf)
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/Block.causesSuffocation (Lnet/minecraft/block/state/IBlockProperties;)Z
//    IRETURN

//  public default causesSuffocation()Z
//   L0
//    LINENUMBER 325 L0
//    GETSTATIC io/github/cadiboo/nocubes/config/NoCubesConfig.Server.collisionsEnabled : Z
//    IFEQ L1
//    ALOAD 0
//    INVOKEINTERFACE net/minecraft/block/state/IBlockProperties.nocubes_isSmoothable ()Z (itf)
//    IFEQ L1
//    ICONST_0
//    IRETURN
//   L1
//    LINENUMBER 327 L1
//   FRAME SAME
//    ALOAD 0
//    INVOKEINTERFACE net/minecraft/block/state/IBlockProperties.getBlock ()Lnet/minecraft/block/Block; (itf)
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/Block.causesSuffocation (Lnet/minecraft/block/state/IBlockProperties;)Z
//    IRETURN

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

		// Labels n stuff
		LabelNode originalInstructionsLabel = new LabelNode();

		// Make list of instructions to inject
		toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/config/NoCubesConfig$Server", "collisionsEnabled", "Z"));
		toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
		toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKEINTERFACE,
				//String owner
				"net/minecraft/block/state/IBlockProperties",
				//String name
				"nocubes_isSmoothable",
				//String descriptor
				"()Z",
				//boolean isInterface
				true
		));
		toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
		toInject.add(new InsnNode(ICONST_0));
		toInject.add(new InsnNode(IRETURN));

		toInject.add(originalInstructionsLabel);

		// Inject instructions
		instructions.insert(firstLabel, toInject);

	}

}
