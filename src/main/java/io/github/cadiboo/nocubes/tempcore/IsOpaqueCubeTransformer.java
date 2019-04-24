//package io.github.cadiboo.nocubes.tempcore;
//
//import org.objectweb.asm.Label;
//import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.tree.AbstractInsnNode;
//import org.objectweb.asm.tree.ClassNode;
//import org.objectweb.asm.tree.FieldInsnNode;
//import org.objectweb.asm.tree.FieldNode;
//import org.objectweb.asm.tree.InsnList;
//import org.objectweb.asm.tree.InsnNode;
//import org.objectweb.asm.tree.JumpInsnNode;
//import org.objectweb.asm.tree.LabelNode;
//import org.objectweb.asm.tree.MethodInsnNode;
//import org.objectweb.asm.tree.MethodNode;
//import org.objectweb.asm.tree.VarInsnNode;
//
//import java.util.ListIterator;
//
//import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.Api;
//import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.BlockStateContainer$StateImplementation_block;
//import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.LOGGER;
//import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.getMethod;
//
///**
// * @author Cadiboo
// */
//final class IsOpaqueCubeTransformer implements Opcodes {
//
//	private static final String run_isOpaqueCube_fieldName = "nocubes_runIsOpaqueCubeDefaultOnce";
//
//	static void hook_isOpaqueCube(final ClassNode classNode) {
//
//		LOGGER.info("Starting adding field " + run_isOpaqueCube_fieldName);
//		{
//			classNode.fields.add(new FieldNode(ACC_PUBLIC, run_isOpaqueCube_fieldName, "Z", null, false));
//		}
//		LOGGER.info("Finished adding field " + run_isOpaqueCube_fieldName);
//
//		final MethodNode isOpaqueCube;
//		isOpaqueCube = getMethod(classNode, "func_185914_p", "()Z");
//		final InsnList instructions = isOpaqueCube.instructions;
//		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
//		LabelNode FIRST_LABEL = null;
//		while (iterator.hasNext()) {
//			final AbstractInsnNode insn = iterator.next();
//			if (insn.getType() != AbstractInsnNode.LABEL) {
//				continue;
//			}
//			FIRST_LABEL = (LabelNode) insn;
//			break;
//		}
//
//		LOGGER.info("Starting injecting into isOpaqueCube");
//		{
//			//Prep for 1.13
//			final InsnList injectedInstructions = Api.getMethodNode().instructions;
//			final LabelNode labelNode = new LabelNode(new Label());
//
//			// if(!nocubes_runIsEntityInsideOpaqueBlockDefaultOnce && NoCubes.hooksEnabled()) {
//			//     return IsEntityInsideOpaqueBlockHook.isEntityInsideOpaqueBlock(this);
//			// }
//			// nocubes_runIsEntityInsideOpaqueBlockDefaultOnce = false;
//			// // Normal Code
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
//			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_isOpaqueCube_fieldName, "Z"));
//			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			injectedInstructions.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/NoCubes", "areHooksEnabled", "()Z", false));
//			injectedInstructions.add(new JumpInsnNode(IFEQ, labelNode));
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
//			injectedInstructions.add(BlockStateContainer$StateImplementation_block());
//			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
//			injectedInstructions.add(new MethodInsnNode(
//					INVOKESTATIC,
//					"io/github/cadiboo/nocubes/hooks/IsOpaqueCubeHook",
//					"isOpaqueCube",
//					"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;)Z",
//					false
//			));
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			injectedInstructions.add(new InsnNode(IRETURN));
//			injectedInstructions.add(labelNode);
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
//			injectedInstructions.add(new InsnNode(ICONST_0));
//			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_isOpaqueCube_fieldName, "Z"));
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			instructions.insert(FIRST_LABEL, injectedInstructions);
//
////#			L0
////>			LINENUMBER 234 L0
////>			ALOAD 0
////>			GETFIELD io/github/cadiboo/nocubes/hooks/BlockStateImplTest.runIsOpaqueCubeDefaultOnce : Z
////>			IFEQ L1
////>			L2
////>			LINENUMBER 235 L2
////>			ALOAD 0
////>			ICONST_0
////>			PUTFIELD io/github/cadiboo/nocubes/hooks/BlockStateImplTest.runIsOpaqueCubeDefaultOnce : Z
////>			L3
////>			LINENUMBER 236 L3
////>			INVOKESTATIC io/github/cadiboo/nocubes/NoCubes.isEnabled ()Z
////>			IFEQ L1
////>			L2
////>			ALOAD 0
////>			GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////>			ALOAD 0
////>			INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.isOpaqueCube (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;)Z
////>			IRETURN
////>			L1
////          Normal code
//
//		}
//		LOGGER.info("Finished injecting into isOpaqueCube");
//	}
//
//	static void add_runIsOpaqueCubeDefaultOnce(final ClassNode classNode) {
//		LOGGER.info("Starting injecting into runIsOpaqueCubeDefaultOnce");
//		{
//			final InsnList instructions = getMethod(classNode, "runIsOpaqueCubeDefaultOnce", "(Lnet/minecraft/block/state/BlockStateContainer$StateImplementation;)V").instructions;
//
//			instructions.clear();
//
//			instructions.add(new LabelNode(new Label()));
//			instructions.add(new VarInsnNode(ALOAD, 0));
//			instructions.add(new InsnNode(ICONST_1));
//			instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_isOpaqueCube_fieldName, "Z"));
//
//			instructions.add(new LabelNode(new Label()));
//			instructions.add(new InsnNode(RETURN));
//		}
//		LOGGER.info("Finished injecting into runIsOpaqueCubeDefaultOnce");
//	}
//
//}
