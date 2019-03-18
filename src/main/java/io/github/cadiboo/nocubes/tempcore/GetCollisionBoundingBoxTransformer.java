package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;

import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.Api;
import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.BlockStateContainer$StateImplementation_block;
import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.LOGGER;
import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.getMethod;

/**
 * @author Cadiboo
 */
final class GetCollisionBoundingBoxTransformer implements Opcodes {

	private static final String run_getCollisionBoundingBox_fieldName = "nocubes_runGetCollisionBoundingBoxDefaultOnce";

	static void hook_getCollisionBoundingBox(final ClassNode classNode) {

		LOGGER.info("Starting adding field " + run_getCollisionBoundingBox_fieldName);
		{
			classNode.fields.add(new FieldNode(ACC_PUBLIC, run_getCollisionBoundingBox_fieldName, "Z", null, false));
		}
		LOGGER.info("Finished adding field " + run_getCollisionBoundingBox_fieldName);

		final MethodNode getCollisionBoundingBox;
		getCollisionBoundingBox = getMethod(classNode, "func_185890_d", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;");
		final InsnList instructions = getCollisionBoundingBox.instructions;
		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
		LabelNode FIRST_LABEL = null;
		while (iterator.hasNext()) {
			final AbstractInsnNode insn = iterator.next();
			if (insn.getType() != AbstractInsnNode.LABEL) {
				continue;
			}
			FIRST_LABEL = (LabelNode) insn;
			break;
		}

		LOGGER.info("Starting injecting into isOpaqueCube");
		{
			//Prep for 1.13
			final InsnList injectedInstructions = Api.getMethodNode().instructions;

			// if(!nocubes_runGetCollisionBoundingBoxDefaultOnce && NoCubes.hooksEnabled()) {
			//     return IsEntityInsideOpaqueBlockHook.isEntityInsideOpaqueBlock(this);
			// }
			// nocubes_runGetCollisionBoundingBoxDefaultOnce = false;
			// // Normal Code

			final LabelNode labelNode = new LabelNode(new Label());
			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_getCollisionBoundingBox_fieldName, "Z"));
			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/NoCubes", "areHooksEnabled", "()Z", false));
			injectedInstructions.add(new JumpInsnNode(IFEQ, labelNode));

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(BlockStateContainer$StateImplementation_block());
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new VarInsnNode(ALOAD, 1));
			injectedInstructions.add(new VarInsnNode(ALOAD, 2));
			injectedInstructions.add(new MethodInsnNode(
					INVOKESTATIC,
					"io/github/cadiboo/nocubes/hooks/GetCollisionBoundingBoxHook",
					"getCollisionBoundingBox",
					"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;",
					false
			));
			injectedInstructions.add(new InsnNode(ARETURN));

			injectedInstructions.add(labelNode);
			injectedInstructions.add(new FrameNode(F_SAME, -1, null, -1, null));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_0));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_getCollisionBoundingBox_fieldName, "Z"));

			injectedInstructions.add(new LabelNode(new Label()));
			instructions.insert(FIRST_LABEL, injectedInstructions);

//			L0
//			LINENUMBER 246 L0
//			ALOAD 0
//			GETFIELD io/github / cadiboo / nocubes / hooks / BlockStateImplTest.runIsOpaqueCubeDefaultOnce :Z
//			IFNE L1
//			L2
//			LINENUMBER 247 L2
//			INVOKESTATIC io/github / cadiboo / nocubes / NoCubes.isEnabled() Z
//			IFEQ L1
//			L3
//			LINENUMBER 248 L3
//			ALOAD 0
//			GETFIELD io/github / cadiboo / nocubes / hooks / BlockStateImplTest.block :Lnet / minecraft / block / Block;
//			ALOAD 0
//			ALOAD 1
//			ALOAD 2
//			INVOKESTATIC io/
//			github / cadiboo / nocubes / hooks / GetCollisionBoundingBoxHook.getCollisionBoundingBox(Lnet / minecraft / block / Block;
//			Lnet / minecraft / block / state / IBlockState;
//			Lnet / minecraft / world / IBlockAccess;
//			Lnet / minecraft / util / math / BlockPos;)Lnet / minecraft / util / math / AxisAlignedBB;
//			ARETURN
//			L1
//			LINENUMBER 250 L1
//			FRAME SAME
//			ALOAD 0
//			ICONST_0
//			PUTFIELD io/github / cadiboo / nocubes / hooks / BlockStateImplTest.runIsOpaqueCubeDefaultOnce :Z
//			L4
//			normal code

		}
		LOGGER.info("Finished injecting into isOpaqueCube");
	}

	static void add_runGetCollisionBoundingBoxDefaultOnce(final ClassNode classNode) {
		LOGGER.info("Starting injecting into runGetCollisionBoundingBoxDefaultOnce");
		{
			final InsnList instructions = getMethod(classNode, "runGetCollisionBoundingBoxDefaultOnce", "(Lnet/minecraft/block/state/BlockStateContainer$StateImplementation;)V").instructions;

			instructions.clear();

			instructions.add(new LabelNode(new Label()));
			instructions.add(new VarInsnNode(ALOAD, 0));
			instructions.add(new InsnNode(ICONST_1));
			instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_getCollisionBoundingBox_fieldName, "Z"));

			instructions.add(new LabelNode(new Label()));
			instructions.add(new InsnNode(RETURN));
		}
		LOGGER.info("Finished injecting into runGetCollisionBoundingBoxDefaultOnce");
	}

}
