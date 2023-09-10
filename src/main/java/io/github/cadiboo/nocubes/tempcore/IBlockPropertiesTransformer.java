package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.start;

/**
 * @author Cadiboo
 */
final class IBlockPropertiesTransformer implements Opcodes {


	static void transform(final ClassNode classNode) {

		final List<MethodNode> methods = classNode.methods;

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

//// access flags 0x1
//  public default nocubes_isSmoothable()Z
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
//  public default nocubes_setSmoothable(Z)V
//   L0
//    LINENUMBER 377 L0
//    RETURN
//   L1
//    LOCALVARIABLE this Lnet/minecraft/block/state/IBlockProperties; L0 L1 0
//    LOCALVARIABLE isTerrainSmoothable Z L0 L1 1
//    MAXSTACK = 0
//    MAXLOCALS = 2
//

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
		method.instructions.add(new InsnNode(ICONST_0));
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
		method.instructions.add(new InsnNode(RETURN));

		return method;

	}

}
