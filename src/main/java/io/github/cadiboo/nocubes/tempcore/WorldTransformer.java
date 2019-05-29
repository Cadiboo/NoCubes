package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.ClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.ClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.ClassTransformer.mapField;
import static io.github.cadiboo.nocubes.tempcore.ClassTransformer.mapMethod;
import static io.github.cadiboo.nocubes.tempcore.ClassTransformer.start;

/**
 * @author Cadiboo
 */
final class WorldTransformer implements Opcodes {

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
			final String targetMethodDesc = "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;)Z";
			final String targetMethodName = mapMethod("net/minecraft/world/World", "func_191504_a", targetMethodDesc);

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

				start("Inject getCollisionBoxes hook");
				injectGetCollisionBoxesHook(method.instructions);
				finish();
				break;

			}
		}

	}

	// Finds the first instruction GETSTATIC Blocks.STONE
// then finds the previous label
// and inserts after that label and before the label's instructions.
	private static void injectGetCollisionBoxesHook(InsnList instructions) {

//	boolean flag1 = entityIn != null && this.isInsideWorldBorder(entityIn);
//
//	IBlockState iblockstate = Blocks.STONE.getDefaultState();

//	boolean flag1 = entityIn != null && this.isInsideWorldBorder(entityIn);
//
//	// NoCubes Start
//	return io.github.cadiboo.nocubes.hooks.Hooks.getCollisionBoxes(this, entityIn, aabb, p_191504_3_, outList, i, j, k, l, i1, j1, worldborder, flag, flag1);
//	// NoCubes End
//	IBlockState iblockstate = Blocks.STONE.getDefaultState();

//    INVOKEVIRTUAL net/minecraft/world/World.isInsideWorldBorder (Lnet/minecraft/entity/Entity;)Z
//    IFEQ L19
//    ICONST_1
//    GOTO L20
//   L19
//   FRAME APPEND [I]
//    ICONST_0
//   L20
//   FRAME SAME1 I
//    ISTORE 13
//   L21
//    LINENUMBER 1433 L21
//    GETSTATIC net/minecraft/init/Blocks.STONE : Lnet/minecraft/block/Block;
//    INVOKEVIRTUAL net/minecraft/block/Block.getDefaultState ()Lnet/minecraft/block/state/IBlockState;
//    ASTORE 14
//   L22

//    INVOKEVIRTUAL net/minecraft/world/World.isInsideWorldBorder (Lnet/minecraft/entity/Entity;)Z
//    IFEQ L11
//    ICONST_1
//    GOTO L12
//   L11
//   FRAME APPEND [I]
//    ICONST_0
//   L12
//   FRAME SAME1 I
//    ISTORE 13
//   L13
//    LINENUMBER 1433 L13
//    ALOAD 0
//    ALOAD 1
//    ALOAD 2
//    ILOAD 3
//    ALOAD 4
//    ILOAD 5
//    ILOAD 6
//    ILOAD 7
//    ILOAD 8
//    ILOAD 9
//    ILOAD 10
//    ALOAD 11
//    ILOAD 12
//    ILOAD 13
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.getCollisionBoxes (Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;IIIIIILnet/minecraft/world/border/WorldBorder;ZZ)Z
//    IRETURN

		String STONE_name = mapField("net/minecraft/init/Blocks", "field_150348_b"); // getAllInBoxMutable

		AbstractInsnNode first_GETSTATIC_STONE = null;
		int arrayLength = instructions.size();
		for (int i = 0; i < arrayLength; ++i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == GETSTATIC) {
				FieldInsnNode instruction = (FieldInsnNode) insn;
				if (instruction.owner.equals("net/minecraft/init/Blocks")) {
					if (instruction.name.equals(STONE_name)) {
						first_GETSTATIC_STONE = instruction;
						log("Found injection point " + instruction);
						break;
					}
				}
			}
		}
		if (first_GETSTATIC_STONE == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

		AbstractInsnNode firstLabelBefore_first_GETSTATIC_STONE = null;
		for (int i = instructions.indexOf(first_GETSTATIC_STONE); i >= 0; --i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				firstLabelBefore_first_GETSTATIC_STONE = instruction;
				log("Found label " + instruction);
				break;
			}
		}
		if (firstLabelBefore_first_GETSTATIC_STONE == null) {
			throw new RuntimeException("Error: Couldn't find label!");
		}

		InsnList toInject = new InsnList();

		// Labels n stuff
//		LabelNode originalInstructionsLabel = new LabelNode();

		// Make list of instructions to inject
		toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
		toInject.add(new VarInsnNode(ALOAD, 1));
		toInject.add(new VarInsnNode(ALOAD, 2));
		toInject.add(new VarInsnNode(ILOAD, 3));
		toInject.add(new VarInsnNode(ALOAD, 4));
		toInject.add(new VarInsnNode(ILOAD, 5));
		toInject.add(new VarInsnNode(ILOAD, 6));
		toInject.add(new VarInsnNode(ILOAD, 7));
		toInject.add(new VarInsnNode(ILOAD, 8));
		toInject.add(new VarInsnNode(ILOAD, 9));
		toInject.add(new VarInsnNode(ILOAD, 10));
		toInject.add(new VarInsnNode(ALOAD, 11));
		toInject.add(new VarInsnNode(ILOAD, 12));
		toInject.add(new VarInsnNode(ILOAD, 13));
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKESTATIC,
				//String owner
				"io/github/cadiboo/nocubes/hooks/Hooks",
				//String name
				"getCollisionBoxes",
				//String descriptor
				"(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;ZLjava/util/List;IIIIIILnet/minecraft/world/border/WorldBorder;ZZ)Z",
				//boolean isInterface
				false
		));
		toInject.add(new InsnNode(IRETURN));

		// Inject instructions
		instructions.insert(firstLabelBefore_first_GETSTATIC_STONE, toInject);

	}

}
