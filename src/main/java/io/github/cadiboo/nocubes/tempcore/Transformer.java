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
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ListIterator;
import java.util.function.Consumer;

import static io.github.cadiboo.renderchunkrebuildchunkhooks.core.classtransformer.RenderChunkRebuildChunkHooksRenderChunkClassTransformer.DEBUG_DUMP_BYTECODE_DIR;
import static net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

/**
 * @author Cadiboo
 */
public class Transformer implements IClassTransformer, Opcodes {

	private static final Logger LOGGER = LogManager.getLogger();
	private static boolean DUMP_BYTECODE = false;

	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
		if (transformedName.equals("net.minecraft.block.state.BlockStateContainer$StateImplementation")) {
			return transformClass(basicClass, transformedName,
					Transformer::redirect_shouldSideBeRendered,
//					Transformer::redirect_doesSideBlockRendering,
					Transformer::redirect_getCollisionBoundingBox,
					Transformer::redirect_addCollisionBoxToList
			);
		} else if (transformedName.equals("net.minecraft.entity.Entity")) {
			return transformClass(basicClass, transformedName,
					Transformer::redirect_isEntityInsideOpaqueBlock
			);
		}
		return basicClass;
	}

	@SafeVarargs
	private final byte[] transformClass(final byte[] basicClass, final String transformedName, final Consumer<ClassNode>... classNodeAcceptors) {
		ClassNode classNode = new ClassNode();
		ClassReader cr = new ClassReader(basicClass);
		if (DUMP_BYTECODE) try {
			Path pathToFile = Paths.get(DEBUG_DUMP_BYTECODE_DIR + transformedName + "_before_hooks.txt");
			pathToFile.toFile().getParentFile().mkdirs();
			PrintWriter filePrinter = new PrintWriter(pathToFile.toFile());
			ClassReader reader = new ClassReader(basicClass);
			TraceClassVisitor tracingVisitor = new TraceClassVisitor(filePrinter);
			reader.accept(tracingVisitor, 0);
			pathToFile = Paths.get(DEBUG_DUMP_BYTECODE_DIR + transformedName + "_before_hooks.class");
			pathToFile.toFile().getParentFile().mkdirs();
			FileOutputStream fileOutputStream = new FileOutputStream(pathToFile.toFile());
			fileOutputStream.write(basicClass);
			fileOutputStream.close();
		} catch (Exception var16) {
			LogManager.getLogger().error("Failed to dump bytecode of classes before injecting hooks!", var16);
		}
		cr.accept(classNode, 4);
		for (final Consumer<ClassNode> classNodeAcceptor : classNodeAcceptors) {
			classNodeAcceptor.accept(classNode);
		}
		ClassWriter out = new ClassWriter(3);
		classNode.accept(out);
		if (DUMP_BYTECODE) {
			try {
				byte[] bytes = out.toByteArray();
				Path pathToFile = Paths.get(DEBUG_DUMP_BYTECODE_DIR + transformedName + "_after_hooks.txt");
				pathToFile.toFile().getParentFile().mkdirs();
				PrintWriter filePrinter = new PrintWriter(pathToFile.toFile());
				ClassReader reader = new ClassReader(bytes);
				TraceClassVisitor tracingVisitor = new TraceClassVisitor(filePrinter);
				reader.accept(tracingVisitor, 0);
				Path pathToClass = Paths.get(DEBUG_DUMP_BYTECODE_DIR + transformedName + "_after_hooks.class");
				pathToClass.toFile().getParentFile().mkdirs();
				FileOutputStream fileOutputStream = new FileOutputStream(pathToClass.toFile());
				fileOutputStream.write(bytes);
				fileOutputStream.close();
			} catch (Exception var14) {
				LogManager.getLogger().error("Failed to dump bytecode of classes after injecting hooks!", var14);
			}
		}
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

	public static void redirect_isEntityInsideOpaqueBlock(final ClassNode classNode) {

		LOGGER.info("Starting injecting into isEntityInsideOpaqueBlock");

		final MethodNode isEntityInsideOpaqueBlock;
		isEntityInsideOpaqueBlock = getMethod(classNode, "func_70094_T", "()Z");
		final InsnList instructions = isEntityInsideOpaqueBlock.instructions;
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
//		LINENUMBER 2207 L0
//>		ALOAD 0
//>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.isEntityInsideOpaqueBlock (Lnet/minecraft/entity/Entity;)Z
//>		IRETURN
//#		ALOAD 0
//		GETFIELD net/minecraft/entity/Entity.noClip : Z
//		IFEQ L1

		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
				INVOKESTATIC,
				"io/github/cadiboo/nocubes/hooks/Hooks",
				"isEntityInsideOpaqueBlock",
				"(Lnet/minecraft/entity/Entity;)Z",
				false
		));
		instructions.insertBefore(ALOAD_0, new InsnNode(IRETURN));
		LOGGER.info("Finished injecting into isEntityInsideOpaqueBlock");

	}

	public static void redirect_shouldSideBeRendered(final ClassNode classNode) {

		LOGGER.info("Starting injecting into shouldSideBeRendered");

		final MethodNode shouldSideBeRendered;
		try {
			shouldSideBeRendered = getMethod(classNode, "func_185894_c", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z");
		} catch (UnableToFindMethodException e) {
			LOGGER.warn("Unable to find method shouldSideBeRendered|func_185894_c. Assuming not on client and ignoring");
			return;
		}
		final InsnList instructions = shouldSideBeRendered.instructions;
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
//		LINENUMBER 447 L0
//>		ALOAD 0
//>		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//>		ALOAD 0
//>		ALOAD 1
//>		ALOAD 2
//>		ALOAD 3
//>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.shouldSideBeRendered (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z
//>		IRETURN
//#		ALOAD 0
//		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//		ALOAD 0
//		ALOAD 1
//		ALOAD 2
//		ALOAD 3
//		INVOKEVIRTUAL net/minecraft/block/Block.shouldSideBeRendered (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z
//		IRETURN

		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, BlockStateContainer$StateImplementation_block());
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 3));
		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
				INVOKESTATIC,
				"io/github/cadiboo/nocubes/hooks/Hooks",
				"shouldSideBeRendered",
				"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
				false
		));
		instructions.insertBefore(ALOAD_0, new InsnNode(IRETURN));
		LOGGER.info("Finished injecting into shouldSideBeRendered");

	}

	private static void redirect_doesSideBlockRendering(final ClassNode classNode) {

		LOGGER.info("Starting injecting into doesSideBlockRendering");

		final MethodNode doesSideBlockRendering;
		doesSideBlockRendering = getMethod(classNode, "doesSideBlockRendering", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z");
		final InsnList instructions = doesSideBlockRendering.instructions;
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
//		LINENUMBER 447 L0
//>		ALOAD 0
//>		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//>		ALOAD 0
//>		ALOAD 1
//>		ALOAD 2
//>		ALOAD 3
//>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.doesSideBlockRendering (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z
//>		IRETURN
//#		ALOAD 0
//		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//		ALOAD 0
//		ALOAD 1
//		ALOAD 2
//		ALOAD 3
//		INVOKEVIRTUAL net/minecraft/block/Block.doesSideBlockRendering (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z
//		IRETURN

		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, BlockStateContainer$StateImplementation_block());
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 3));
		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
				INVOKESTATIC,
				"io/github/cadiboo/nocubes/hooks/Hooks",
				"doesSideBlockRendering",
				"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
				false
		));
		instructions.insertBefore(ALOAD_0, new InsnNode(IRETURN));
		LOGGER.info("Finished injecting into doesSideBlockRendering");
	}

	private static AbstractInsnNode BlockStateContainer$StateImplementation_block() {
		return new FieldInsnNode(
				GETFIELD,
				"net/minecraft/block/state/BlockStateContainer$StateImplementation",
				ObfuscationHelper.remapFieldName("net/minecraft/block/state/BlockStateContainer$StateImplementation", "field_177239_a"),
				"Lnet/minecraft/block/Block;"
		);
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
		instructions.insertBefore(ALOAD_0, BlockStateContainer$StateImplementation_block());
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

	public static void redirect_getCollisionBoundingBox(final ClassNode classNode) {

		LOGGER.info("Starting injecting into getCollisionBoundingBox");

		final MethodNode getCollisionBoundingBox = getMethod(classNode, "func_185890_d", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;");
		final InsnList instructions = getCollisionBoundingBox.instructions;
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
//		LINENUMBER 458 L0
//>		ALOAD 0
//>		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//>		ALOAD 0
//>		ALOAD 1
//>		ALOAD 2
//>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.getCollisionBoundingBox (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;
//>		ARETURN
//#		ALOAD 0
//		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//		ALOAD 0
//		ALOAD 1
//		ALOAD 2
//		INVOKEVIRTUAL net/minecraft/block/Block.getCollisionBoundingBox (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;
//		ARETURN

		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, BlockStateContainer$StateImplementation_block());
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
				INVOKESTATIC,
				"io/github/cadiboo/nocubes/hooks/Hooks",
				"getCollisionBoundingBox",
				"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;",
				false
		));
		instructions.insertBefore(ALOAD_0, new InsnNode(ARETURN));
		LOGGER.info("Finished injecting into getCollisionBoundingBox");

	}

	// Copied from  ObfuscationReflectionHelper
	private static class ObfuscationHelper {

		private static String remapMethodName(final String internalClassName, final String methodName, final String desc) {
			final String remappedName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(internalClassName, methodName, desc);
			LOGGER.info("remapped name " + methodName + " to " + remappedName);
			return remappedName;
		}

		private static String remapFieldName(final String internalClassName, final String fieldName) {
			final String remappedName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(internalClassName, fieldName, null);
			LOGGER.info("remapped name " + fieldName + " to " + remappedName);
			return remappedName;
		}

	}

}
