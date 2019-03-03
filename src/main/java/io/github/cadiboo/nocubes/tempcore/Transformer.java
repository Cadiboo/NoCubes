package io.github.cadiboo.nocubes.tempcore;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ListIterator;
import java.util.function.Consumer;

import static net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

/**
 * @author Cadiboo
 */
public class Transformer implements IClassTransformer, Opcodes {

	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
		if (transformedName.equals("net.minecraft.block.state.BlockStateContainer$StateImplementation")) {
			return transformClass(basicClass, Transformer::redirect_addCollisionBoxToList);
		}

		return basicClass;
	}

	private byte[] transformClass(final byte[] basicClass, final Consumer<ClassNode> classNodeAcceptor) {
		ClassNode classNode = new ClassNode();
		ClassReader cr = new ClassReader(basicClass);
		cr.accept(classNode, 4);
		classNodeAcceptor.accept(classNode);
		ClassWriter out = new ClassWriter(3);
		classNode.accept(out);
		return out.toByteArray();
	}

	private static MethodNode getMethod(ClassNode classNode, String srgName, String methodDescription) {
		final String methodName = ObfuscationHelper.remapMethodName(classNode.name, srgName, methodDescription);
		for (final MethodNode method : classNode.methods) {
			if (method.name.equals(methodName)) {
				return method;
			}
		}
		StringBuilder names = new StringBuilder();
		for (MethodNode methodNode : classNode.methods) {
			names.append(methodNode.name).append(" | ").append(methodNode.desc).append("\n");
		}
		throw new UnableToFindMethodException(new Exception(srgName + " does not exist!", new Exception(names.toString())));
	}

	public static void redirect_addCollisionBoxToList(final ClassNode classNode) {

		LOGGER.info("Starting injecting into addCollisionBoxToList");

		final MethodNode addCollisionBoxToList = getMethod(classNode, "func_185908_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V");
		final InsnList instructions = addCollisionBoxToList.instructions;
		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
		VarInsnNode ALOAD_0 = null;
		while (iterator.hasNext()) {
			final AbstractInsnNode insn = iterator.next();
			if (insn.getOpcode() != ALOAD) {
				continue;
			}
			if (insn.getType() != AbstractInsnNode.VAR_INSN) {
				continue;
			}
			if (((VarInsnNode) insn).var != 0) {
				continue;
			}
			ALOAD_0 = (VarInsnNode) insn;
			break;
		}

		if (ALOAD_0 == null) {
			return;
		}

//		L0
//		LINENUMBER 463 L0
//>		ALOAD 0
//>		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//>		ALOAD 0
//>		ALOAD 1
//>		ALOAD 2
//>		ALOAD 3
//>		ALOAD 4
//>		ALOAD 5
//>		ILOAD 6
//>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.addCollisionBoxToList (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
//#		ALOAD 0
//		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//		ALOAD 0
//		ALOAD 1
//		ALOAD 2
//		ALOAD 3
//		ALOAD 4
//		ALOAD 5
//		ILOAD 6
//		INVOKEVIRTUAL net/minecraft/block/Block.addCollisionBoxToList (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
//				L1
//		LINENUMBER 464 L1
//				RETURN

		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, new FieldInsnNode(
				GETFIELD,
				"net/minecraft/block/state/BlockStateContainer$StateImplementation",
				"block",
				"Lnet/minecraft/block/Block;"
		));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 3));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 4));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 5));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ILOAD, 6));
		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
				INVOKESTATIC,
				"io/github/cadiboo/nocubes/hooks/Hooks",
				"addCollisionBoxToList",
				"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V",
				false
		));
		instructions.insertBefore(ALOAD_0, new InsnNode(RETURN));
		LOGGER.info("Finished injecting into addCollisionBoxToList");

	}

	//Coppied from 1.13 ObfuscationReflectionHelper

	private static class ObfuscationHelper {

		private static String remapMethodName(String internalClassName, String methodName, String desc) {
			final String remappedName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(internalClassName, methodName, desc);
			LOGGER.info("remapped name " + methodName + " to " + remappedName);
			return remappedName;
		}

	}

}
