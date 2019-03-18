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
final class AddCollisionBoxToListTransformer implements Opcodes {

	private static final String run_addCollisionBoxToList_fieldName = "nocubes_runAddCollisionBoxToListDefaultOnce";

	static void hook_addCollisionBoxToList(final ClassNode classNode) {

		LOGGER.info("Starting adding field " + run_addCollisionBoxToList_fieldName);
		{
			classNode.fields.add(new FieldNode(ACC_PUBLIC, run_addCollisionBoxToList_fieldName, "Z", null, false));
		}
		LOGGER.info("Finished adding field " + run_addCollisionBoxToList_fieldName);

		final MethodNode addCollisionBoxToList = getMethod(classNode, "func_185908_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V");
		final InsnList instructions = addCollisionBoxToList.instructions;
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

		LOGGER.info("Starting injecting into addCollisionBoxToList");
		{
			//Prep for 1.13
			final InsnList injectedInstructions = Api.getMethodNode().instructions;

			// if(!nocubes_runAddCollisionBoxToListDefaultOnce && NoCubes.hooksEnabled()) {
			//     return AddCollisionBoxToListHook.addCollisionBoxToList(this.block, this, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			// }
			// nocubes_runAddCollisionBoxToListDefaultOnce = false;
			// // Normal Code

			final LabelNode labelNode = new LabelNode(new Label());

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_addCollisionBoxToList_fieldName, "Z"));
			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));
			injectedInstructions.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/NoCubes", "areHooksEnabled", "()Z", false));
			injectedInstructions.add(new JumpInsnNode(IFEQ, labelNode));

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(BlockStateContainer$StateImplementation_block());
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new VarInsnNode(ALOAD, 1));
			injectedInstructions.add(new VarInsnNode(ALOAD, 2));
			injectedInstructions.add(new VarInsnNode(ALOAD, 3));
			injectedInstructions.add(new VarInsnNode(ALOAD, 4));
			injectedInstructions.add(new VarInsnNode(ALOAD, 5));
			injectedInstructions.add(new VarInsnNode(ILOAD, 6)); //ILOAD!
			injectedInstructions.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/hooks/AddCollisionBoxToListHook", "addCollisionBoxToList", "(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V", false));

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new InsnNode(RETURN));

			injectedInstructions.add(labelNode);
			injectedInstructions.add(new FrameNode(F_SAME, -1, null, -1, null));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_0));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_addCollisionBoxToList_fieldName, "Z"));

			injectedInstructions.add(new LabelNode(new Label()));
			instructions.insert(FIRST_LABEL, injectedInstructions);

//			L0
//			LINENUMBER 257 L0
//			ALOAD 0
//			GETFIELD io/github/cadiboo/nocubes/hooks/BlockStateImplTest.runAddCollisionBoxToListDefaultOnce : Z
//			IFNE L1
//			INVOKESTATIC io/github/cadiboo/nocubes/NoCubes.areHooksEnabled ()Z
//			IFEQ L1
//			L2
//			LINENUMBER 258 L2
//			ALOAD 0
//			GETFIELD io/github/cadiboo/nocubes/hooks/BlockStateImplTest.block : Lnet/minecraft/block/Block;
//			ALOAD 0
//			ALOAD 1
//			ALOAD 2
//			ALOAD 3
//			ALOAD 4
//			ALOAD 5
//			ILOAD 6
//			INVOKESTATIC io/github/cadiboo/nocubes/hooks/AddCollisionBoxToListHook.addCollisionBoxToList (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
//				L3
//			LINENUMBER 259 L3
//				RETURN
//			L1
//			LINENUMBER 261 L1
//			FRAME SAME
//			ALOAD 0
//			ICONST_0
//			PUTFIELD io/github/cadiboo/nocubes/hooks/BlockStateImplTest.runAddCollisionBoxToListDefaultOnce : Z
//				L4
//			LINENUMBER 263 L4
//			ALOAD 0
//			GETFIELD io/github/cadiboo/nocubes/hooks/BlockStateImplTest.block : Lnet/minecraft/block/Block;
//			ALOAD 0
//			ALOAD 1
//			ALOAD 2
//			ALOAD 3
//			ALOAD 4
//			ALOAD 5
//			ILOAD 6
//			INVOKEVIRTUAL net/minecraft/block/Block.addCollisionBoxToList (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
//				L5
//			LINENUMBER 264 L5
//				RETURN
//			L6
//			LOCALVARIABLE this Lio/github/cadiboo/nocubes/hooks/BlockStateImplTest; L0 L6 0
//			LOCALVARIABLE worldIn Lnet/minecraft/world/World; L0 L6 1
//			LOCALVARIABLE pos Lnet/minecraft/util/math/BlockPos; L0 L6 2
//			LOCALVARIABLE entityBox Lnet/minecraft/util/math/AxisAlignedBB; L0 L6 3
//			LOCALVARIABLE collidingBoxes Ljava/util/List; L0 L6 4
//			// signature Ljava/util/List<Lnet/minecraft/util/math/AxisAlignedBB;>;
//			// declaration: collidingBoxes extends java.util.List<net.minecraft.util.math.AxisAlignedBB>
//			LOCALVARIABLE entityIn Lnet/minecraft/entity/Entity; L0 L6 5
//			LOCALVARIABLE p_185908_6_ Z L0 L6 6
//			MAXSTACK = 8
//			MAXLOCALS = 7

		}
		LOGGER.info("Finished injecting into addCollisionBoxToList");

	}

	static void add_runAddCollisionBoxToListDefaultOnce(final ClassNode classNode) {
		LOGGER.info("Starting injecting into runAddCollisionBoxToListDefaultOnce");
		{
			final InsnList instructions = getMethod(classNode, "runAddCollisionBoxToListDefaultOnce", "(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V").instructions;

			instructions.clear();

			instructions.add(new LabelNode(new Label()));
			instructions.add(new VarInsnNode(ALOAD, 0));
			instructions.add(new InsnNode(ICONST_1));
			instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_addCollisionBoxToList_fieldName, "Z"));

			instructions.add(new LabelNode(new Label()));
			instructions.add(new InsnNode(RETURN));
		}
		LOGGER.info("Finished injecting into runAddCollisionBoxToListDefaultOnce");
	}

}
