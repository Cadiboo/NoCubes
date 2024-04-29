package io.github.cadiboo.nocubes.hooks;

import junit.framework.TestCase;
import org.junit.Assert;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class MixinAsmTest extends TestCase {

	public void testFindMethodNode() {
		var classNode = new ClassNode();
		classNode.methods.add(new MethodNode(
			Opcodes.ACC_PRIVATE,
			"foo",
			"()V",
			null,
			null
		));
		var expected = new MethodNode(
			Opcodes.ACC_PRIVATE,
			"foo",
			"()I",
			null,
			null
		);
		classNode.methods.add(expected);
		var actual = MixinAsm.findMethodNode(classNode, "foo", "()I");
		Assert.assertEquals(expected, actual);
	}

	public void testFindFirstLabelBefore() {
		// int x = 0;
		// Object foo = new Object();
		// int y = foo.hashCode();
		// return = x + y;
		var l0 = new LabelNode();
		var l1 = new LabelNode();
		var l2 = new LabelNode();
		var l3 = new LabelNode();
		var instructions = MixinAsm.listOf(
			l0,
			new LineNumberNode(0, l0),
			new InsnNode(Opcodes.ICONST_0),

			l1,
			new LineNumberNode(1, l1),
			new TypeInsnNode(Opcodes.NEW, "java/lang/Object"),
			new InsnNode(Opcodes.DUP),
			new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "init", "()V"),
			new VarInsnNode(Opcodes.ASTORE, 2),

			l2,
			new LineNumberNode(2, l2),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I"),
			new VarInsnNode(Opcodes.ISTORE, 3),

			l3,
			new LineNumberNode(3, l3),
			new VarInsnNode(Opcodes.ILOAD, 1),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new InsnNode(Opcodes.IADD),
			new InsnNode(Opcodes.IRETURN)
		);

		var expected = (LabelNode) instructions.get(3); // l1
		var target = (MethodInsnNode) instructions.get(7); // INVOKESPECIAL Object.init
		var actual = MixinAsm.findFirstLabelBefore(instructions, target);
		Assert.assertEquals(expected, actual);
	}

	public void testTryFindFirstFieldInstruction() {
		var expected = new FieldInsnNode(Opcodes.PUTFIELD, "owner/package/OwnerClass", "foo", "Z");
		var instructions = MixinAsm.listOf(
			new FieldInsnNode(Opcodes.PUTFIELD, "owner/package/OwnerClass", "foo", "I"),
			expected,
			new FieldInsnNode(Opcodes.PUTFIELD, "owner/package/OwnerClass", "bar", "Z")
		);
		var actual = MixinAsm.tryFindFirstFieldInstruction(instructions, Opcodes.PUTFIELD, "owner/package/OwnerClass", "foo", "Z");
		Assert.assertEquals(expected, actual);
	}

	public void testFindFirstMethodCall() {
		var expected = new MethodInsnNode(Opcodes.INVOKESTATIC, "owner/package/OwnerClass", "foo", "()V");
		var instructions = MixinAsm.listOf(
			expected.clone(null),
			new MethodInsnNode(Opcodes.INVOKESPECIAL, "owner/package/OwnerClass", "foo", "()V"),
			new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "owner/package/OwnerClass", "foo", "()I"),
			new MethodInsnNode(Opcodes.INVOKESTATIC, "owner/package/OwnerClass", "foo", "()I"),
			expected
		);
		var actual = MixinAsm.findFirstMethodCall(instructions, Opcodes.INVOKESTATIC, "owner/package/OwnerClass", "foo", "()V", 1);
		Assert.assertEquals(expected, actual);
	}

	public void testRemoveBetweenIndicesInclusive() {
		// int x = 0;
		// Object foo = new Object();
		// int y = foo.hashCode();
		// return = x + y;
		var l0 = new LabelNode();
		var l1 = new LabelNode();
		var l2 = new LabelNode();
		var l3 = new LabelNode();
		var instructions = MixinAsm.listOf(
			l0,
			new LineNumberNode(0, l0),
			new InsnNode(Opcodes.ICONST_0),

			l1,
			new LineNumberNode(1, l1),
			new TypeInsnNode(Opcodes.NEW, "java/lang/Object"),
			new InsnNode(Opcodes.DUP),
			new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "init", "()V"),
			new VarInsnNode(Opcodes.ASTORE, 2),

			l2,
			new LineNumberNode(2, l2),
			new VarInsnNode(Opcodes.ALOAD, 2),
			new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I"),
			new VarInsnNode(Opcodes.ISTORE, 3),

			l3,
			new LineNumberNode(3, l3),
			new VarInsnNode(Opcodes.ILOAD, 1),
			new VarInsnNode(Opcodes.ILOAD, 3),
			new InsnNode(Opcodes.IADD),
			new InsnNode(Opcodes.IRETURN)
		);

		var expected = """
			\s\s\sL0
			\s\s\s\sLINENUMBER 0 L0
			\s\s\s\sICONST_0
			\s\s\s\sIRETURN
			""";

		MixinAsm.removeBetweenIndicesInclusive(instructions, instructions.indexOf(l1), instructions.size() - 2);

		Assert.assertEquals(expected, MixinAsm.stringifyInstructions(instructions));
	}
}
