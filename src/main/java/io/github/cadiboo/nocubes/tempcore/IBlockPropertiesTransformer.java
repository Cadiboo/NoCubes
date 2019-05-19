package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.ClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.ClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.ClassTransformer.start;

/**
 * @author Cadiboo
 */
final class IBlockPropertiesTransformer implements Opcodes {

	// Local variable indexes
	private static final int ALOCALVARIABLE_this = 0;

	static void transform(final ClassNode classNode) {

		final List<MethodNode> methods = classNode.methods;

		log("Adding methods...");
		{
			start("Adding make_nocubes_isTerrainSmoothable");
			methods.add(make_nocubes_isTerrainSmoothable());
			finish();
			start("Adding make_nocubes_setTerrainSmoothable");
			methods.add(make_nocubes_setTerrainSmoothable());
			finish();

			start("Adding make_nocubes_isLeavesSmoothable");
			methods.add(make_nocubes_isLeavesSmoothable());
			finish();
			start("Adding make_nocubes_setLeavesSmoothable");
			methods.add(make_nocubes_setLeavesSmoothable());
			finish();
		}
		log("Finished adding methods");

	}

//// access flags 0x1
//  public default nocubes_isTerrainSmoothable()Z
//   L0
//    LINENUMBER 373 L0
//    ICONST_0
//    IRETURN
//   L1
//    LOCALVARIABLE this Lnet/minecraft/block/state/IBlockProperties; L0 L1 0
//    MAXSTACK = 1
//    MAXLOCALS = 1
//
//  // access flags 0x1
//  public default nocubes_setTerrainSmoothable(Z)V
//   L0
//    LINENUMBER 377 L0
//    RETURN
//   L1
//    LOCALVARIABLE this Lnet/minecraft/block/state/IBlockProperties; L0 L1 0
//    LOCALVARIABLE isTerrainSmoothable Z L0 L1 1
//    MAXSTACK = 0
//    MAXLOCALS = 2
//
//// access flags 0x1
//  public default nocubes_isLeavesSmoothable()Z
//   L0
//    LINENUMBER 373 L0
//    ICONST_0
//    IRETURN
//   L1
//    LOCALVARIABLE this Lnet/minecraft/block/state/IBlockProperties; L0 L1 0
//    MAXSTACK = 1
//    MAXLOCALS = 1
//
//  // access flags 0x1
//  public default nocubes_setLeavesSmoothable(Z)V
//   L0
//    LINENUMBER 377 L0
//    RETURN
//   L1
//    LOCALVARIABLE this Lnet/minecraft/block/state/IBlockProperties; L0 L1 0
//    LOCALVARIABLE isLeavesSmoothable Z L0 L1 1
//    MAXSTACK = 0
//    MAXLOCALS = 2

	private static MethodNode make_nocubes_isTerrainSmoothable() {

		MethodNode method = new MethodNode(
//		final int access,
				ACC_PUBLIC,
//		final String name,
				"nocubes_isTerrainSmoothable",
//		final String descriptor,
				"()Z",
//		final String signature,
				null,
//		final String[] exceptions
				null
		);
		method.instructions.add(new InsnNode(ICONST_0));
		method.instructions.add(new InsnNode(IRETURN));

		return method;

	}

	private static MethodNode make_nocubes_setTerrainSmoothable() {

		MethodNode method = new MethodNode(
//		final int access,
				ACC_PUBLIC,
//		final String name,
				"nocubes_setTerrainSmoothable",
//		final String descriptor,
				"(Z)V",
//		final String signature,
				null,
//		final String[] exceptions
				null
		);
		method.instructions.add(new InsnNode(RETURN));

		return method;

	}

	private static MethodNode make_nocubes_isLeavesSmoothable() {

		MethodNode method = new MethodNode(
//		final int access,
				ACC_PUBLIC,
//		final String name,
				"nocubes_isLeavesSmoothable",
//		final String descriptor,
				"()Z",
//		final String signature,
				null,
//		final String[] exceptions
				null
		);
		method.instructions.add(new InsnNode(ICONST_0));
		method.instructions.add(new InsnNode(IRETURN));

		return method;

	}

	private static MethodNode make_nocubes_setLeavesSmoothable() {

		MethodNode method = new MethodNode(
//		final int access,
				ACC_PUBLIC,
//		final String name,
				"nocubes_setLeavesSmoothable",
//		final String descriptor,
				"(Z)V",
//		final String signature,
				null,
//		final String[] exceptions
				null
		);
		method.instructions.add(new InsnNode(RETURN));

		return method;

	}

}
