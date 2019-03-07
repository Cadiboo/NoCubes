package io.github.cadiboo.nocubes.tempcore;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
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
	private static boolean DUMP_BYTECODE = true;

	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
		if (transformedName.equals("net.minecraft.block.state.BlockStateContainer$StateImplementation")) {
			return transformClass(basicClass, transformedName,
//					Transformer::redirect_shouldSideBeRendered,
					Transformer::hook_isOpaqueCube,
//					Transformer::redirect_doesSideBlockRendering,
//					Transformer::redirect_getCollisionBoundingBox,
					Transformer::hook_getCollisionBoundingBox
			);
		} else if (transformedName.equals("net.minecraft.entity.Entity")) {
//			return transformClass(basicClass, transformedName,
//					Transformer::redirect_isEntityInsideOpaqueBlock
//			);
		} else if (transformedName.equals("io.github.cadiboo.nocubes.hooks.IsOpaqueCubeHook")) { // WATCH OUT - everything fails if this misses
			return transformClass(basicClass, transformedName,
					Transformer::add_runIsOpaqueCubeDefaultOnce
			);
		} else if (transformedName.equals("io.gitub.cadiboo.nocubes.hooks.GetCollisionBoundingBoxHook")) {
			return transformClass(basicClass, transformedName,
					Transformer::add_runGetCollisionBoundingBoxDefaultOnce
			);
		}
		return basicClass;
	}

	@SafeVarargs
	private final byte[] transformClass(final byte[] basicClass, final String transformedName, final Consumer<ClassNode>... classNodeAcceptors) {
		ClassNode classNode = new ClassNode();
		ClassReader cr = new ClassReader(basicClass);
		LOGGER.info("Starting transforming " + transformedName);
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
		LOGGER.info("Finished transforming " + transformedName);
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

	private static AbstractInsnNode BlockStateContainer$StateImplementation_block() {
		return new FieldInsnNode(
				GETFIELD,
				"net/minecraft/block/state/BlockStateContainer$StateImplementation",
				ObfuscationHelper.remapFieldName("net/minecraft/block/state/BlockStateContainer$StateImplementation", "field_177239_a"),
				"Lnet/minecraft/block/Block;"
		);
	}

	private static void jumpIfNotEnabled(final InsnList insnList, final LabelNode jumpTo) {
		insnList.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/NoCubes", "isEnabled", "()Z", false));
		insnList.add(new JumpInsnNode(IFEQ, jumpTo));
		insnList.add(new LabelNode(new Label()));
	}

	private static void loadBlockAndState(InsnList instructions) {
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(BlockStateContainer$StateImplementation_block());
		instructions.add(new VarInsnNode(ALOAD, 0));
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

	private static class Api {

		public static MethodNode getMethodNode() {
			return new MethodNode();
		}

	}

	private static void hook_isOpaqueCube(final ClassNode classNode) {

		LOGGER.info("Starting adding field runIsOpaqueCubeDefaultOnce");
		classNode.fields.add(new FieldNode(ACC_PUBLIC, "runIsOpaqueCubeDefaultOnce", "Z", null, false));
		LOGGER.info("Finished adding field runIsOpaqueCubeDefaultOnce");

		final MethodNode isOpaqueCube;
		isOpaqueCube = getMethod(classNode, "func_185914_p", "()Z");
		final InsnList instructions = isOpaqueCube.instructions;
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
			final LabelNode labelNode = new LabelNode(new Label());

			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runIsOpaqueCubeDefaultOnce", "Z"));
			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));

			jumpIfNotEnabled(injectedInstructions, labelNode);
			loadBlockAndState(injectedInstructions);
			injectedInstructions.add(new MethodInsnNode(
					INVOKESTATIC,
					"io/github/cadiboo/nocubes/hooks/IsOpaqueCubeHook",
					"isOpaqueCube",
					"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;)Z",
					false
			));
			injectedInstructions.add(new InsnNode(IRETURN));
			injectedInstructions.add(labelNode);

			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_0));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runIsOpaqueCubeDefaultOnce", "Z"));

			instructions.insert(FIRST_LABEL, injectedInstructions);

//#			L0
//>			LINENUMBER 234 L0
//>			ALOAD 0
//>			GETFIELD io/github/cadiboo/nocubes/hooks/BlockStateImplTest.runIsOpaqueCubeDefaultOnce : Z
//>			IFEQ L1
//>			L2
//>			LINENUMBER 235 L2
//>			ALOAD 0
//>			ICONST_0
//>			PUTFIELD io/github/cadiboo/nocubes/hooks/BlockStateImplTest.runIsOpaqueCubeDefaultOnce : Z
//>			L3
//>			LINENUMBER 236 L3
//>			INVOKESTATIC io/github/cadiboo/nocubes/NoCubes.isEnabled ()Z
//>			IFEQ L1
//>			L2
//>			ALOAD 0
//>			GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//>			ALOAD 0
//>			INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.isOpaqueCube (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;)Z
//>			IRETURN
//>			L1
//          Normal code

		}
		LOGGER.info("Finished injecting into isOpaqueCube");
	}

	private static void add_runIsOpaqueCubeDefaultOnce(final ClassNode classNode) {
		LOGGER.info("Starting injecting into runIsOpaqueCubeDefaultOnce");
		{
			final MethodNode runIsOpaqueCubeDefaultOnce;
			runIsOpaqueCubeDefaultOnce = getMethod(classNode, "runIsOpaqueCubeDefaultOnce", "(Lnet/minecraft/block/state/IBlockState;)V");
			final InsnList instructions = runIsOpaqueCubeDefaultOnce.instructions;
			final AbstractInsnNode first = instructions.getFirst();
			final InsnList injectedInstructions = Api.getMethodNode().instructions;
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_1));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runIsOpaqueCubeDefaultOnce", "Z"));
			injectedInstructions.add(new InsnNode(RETURN));
			instructions.insert(first, injectedInstructions);
		}
		LOGGER.info("Finished injecting into runIsOpaqueCubeDefaultOnce");
	}

	private static void hook_getCollisionBoundingBox(final ClassNode classNode) {

		LOGGER.info("Starting adding field runGetCollisionBoundingBoxDefaultOnce");
		classNode.fields.add(new FieldNode(ACC_PUBLIC, "runGetCollisionBoundingBoxDefaultOnce", "Z", null, false));
		LOGGER.info("Finished adding field runGetCollisionBoundingBoxDefaultOnce");

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

		LOGGER.info("Starting injecting into getCollisionBoundingBox");
		{
			//Prep for 1.13
			final InsnList injectedInstructions = Api.getMethodNode().instructions;
			final LabelNode labelNode = new LabelNode(new Label());

			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runGetCollisionBoundingBoxDefaultOnce", "Z"));
			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));

			jumpIfNotEnabled(injectedInstructions, labelNode);
			loadBlockAndState(injectedInstructions);
			injectedInstructions.add(new MethodInsnNode(
					INVOKESTATIC,
					"io/github/cadiboo/nocubes/hooks/GetCollisionBoundingBoxHook",
					"getCollisionBoundingBox",
					"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;",
					false
			));
			injectedInstructions.add(new InsnNode(IRETURN));
			injectedInstructions.add(labelNode);

			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_0));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runGetCollisionBoundingBoxDefaultOnce", "Z"));

			instructions.insert(FIRST_LABEL, injectedInstructions);

		}
		LOGGER.info("Finished injecting into getCollisionBoundingBox");
	}

	private static void add_runGetCollisionBoundingBoxDefaultOnce(final ClassNode classNode) {
		LOGGER.info("Starting injecting into runGetCollisionBoundingBoxDefaultOnce");
		{
			final MethodNode runGetCollisionBoundingBoxDefaultOnce;
			runGetCollisionBoundingBoxDefaultOnce = getMethod(classNode, "runGetCollisionBoundingBoxDefaultOnce", "(Lnet/minecraft/block/state/IBlockState;)V");
			final InsnList instructions = runGetCollisionBoundingBoxDefaultOnce.instructions;
			final AbstractInsnNode first = instructions.getFirst();
			final InsnList injectedInstructions = Api.getMethodNode().instructions;
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_1));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runGetCollisionBoundingBoxDefaultOnce", "Z"));
			instructions.insert(first, injectedInstructions);
		}
		LOGGER.info("Finished injecting into runGetCollisionBoundingBoxDefaultOnce");
	}

	private static void hook_addCollisionBoxToList(final ClassNode classNode) {

		LOGGER.info("Starting adding field runAddCollisionBoxToListDefaultOnce");
		classNode.fields.add(new FieldNode(ACC_PUBLIC, "runAddCollisionBoxToListDefaultOnce", "Z", null, false));
		LOGGER.info("Finished adding field runAddCollisionBoxToListDefaultOnce");

		final MethodNode getCollisionBoundingBox;
		getCollisionBoundingBox = getMethod(classNode, "func_185908_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V");
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

		LOGGER.info("Starting injecting into addCollisionBoxToList");
		{
			//Prep for 1.13
			final InsnList injectedInstructions = Api.getMethodNode().instructions;
			final LabelNode labelNode = new LabelNode(new Label());

			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runAddCollisionBoxToListDefaultOnce", "Z"));
			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));

			jumpIfNotEnabled(injectedInstructions, labelNode);
			loadBlockAndState(injectedInstructions);
			injectedInstructions.add(new MethodInsnNode(
					INVOKESTATIC,
					"io/github/cadiboo/nocubes/hooks/AddCollisionBoxToListHook",
					"addCollisionBoxToList",
					"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V",
					false
			));
			injectedInstructions.add(new InsnNode(IRETURN));
			injectedInstructions.add(labelNode);

			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_0));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runAddCollisionBoxToListDefaultOnce", "Z"));

			instructions.insert(FIRST_LABEL, injectedInstructions);

		}
		LOGGER.info("Finished injecting into addCollisionBoxToList");
	}

	private static void add_runAddCollisionBoxToListDefaultOnce(final ClassNode classNode) {
		LOGGER.info("Starting injecting into runAddCollisionBoxToListDefaultOnce");
		{
			final MethodNode runGetCollisionBoundingBoxDefaultOnce;
			runGetCollisionBoundingBoxDefaultOnce = getMethod(classNode, "runAddCollisionBoxToListDefaultOnce", "(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V");
			final InsnList instructions = runGetCollisionBoundingBoxDefaultOnce.instructions;
			final AbstractInsnNode first = instructions.getFirst();
			final InsnList injectedInstructions = Api.getMethodNode().instructions;
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_1));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "runAddCollisionBoxToListDefaultOnce", "Z"));
			instructions.insert(first, injectedInstructions);
		}
		LOGGER.info("Finished injecting into runAddCollisionBoxToListDefaultOnce");
	}

//	public static void redirect_isEntityInsideOpaqueBlock(final ClassNode classNode) {
//
//		LOGGER.info("Starting injecting into isEntityInsideOpaqueBlock");
//
//		final MethodNode isEntityInsideOpaqueBlock;
//		isEntityInsideOpaqueBlock = getMethod(classNode, "func_70094_T", "()Z");
//		final InsnList instructions = isEntityInsideOpaqueBlock.instructions;
//		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
//		VarInsnNode ALOAD_0 = null;
//		while (iterator.hasNext()) {
//			final AbstractInsnNode insn = iterator.next();
//			if (insn.getOpcode() != ALOAD) {
//				continue;
//			}
//			if (insn.getType() != AbstractInsnNode.VAR_INSN) {
//				continue;
//			}
//			if (((VarInsnNode) insn).var != 0) {
//				continue;
//			}
//			ALOAD_0 = (VarInsnNode) insn;
//			break;
//		}
//
//		if (ALOAD_0 == null) {
//			return;
//		}
//
////		L0
////		LINENUMBER 2207 L0
////>		ALOAD 0
////>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.isEntityInsideOpaqueBlock (Lnet/minecraft/entity/Entity;)Z
////>		IRETURN
////#		ALOAD 0
////		GETFIELD net/minecraft/entity/Entity.noClip : Z
////		IFEQ L1
//
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
//				INVOKESTATIC,
//				"io/github/cadiboo/nocubes/hooks/Hooks",
//				"isEntityInsideOpaqueBlock",
//				"(Lnet/minecraft/entity/Entity;)Z",
//				false
//		));
//		instructions.insertBefore(ALOAD_0, new InsnNode(IRETURN));
//		LOGGER.info("Finished injecting into isEntityInsideOpaqueBlock");
//
//	}
//
//	public static void redirect_shouldSideBeRendered(final ClassNode classNode) {
//
//		LOGGER.info("Starting injecting into shouldSideBeRendered");
//
//		final MethodNode shouldSideBeRendered;
//		try {
//			shouldSideBeRendered = getMethod(classNode, "func_185894_c", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z");
//		} catch (UnableToFindMethodException e) {
//			LOGGER.warn("Unable to find method shouldSideBeRendered|func_185894_c. Assuming not on client and ignoring");
//			return;
//		}
//		final InsnList instructions = shouldSideBeRendered.instructions;
//		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
//		VarInsnNode ALOAD_0 = null;
//		while (iterator.hasNext()) {
//			final AbstractInsnNode insn = iterator.next();
//			if (insn.getOpcode() != ALOAD) {
//				continue;
//			}
//			if (insn.getType() != AbstractInsnNode.VAR_INSN) {
//				continue;
//			}
//			if (((VarInsnNode) insn).var != 0) {
//				continue;
//			}
//			ALOAD_0 = (VarInsnNode) insn;
//			break;
//		}
//
//		if (ALOAD_0 == null) {
//			return;
//		}
//
////		L0
////		LINENUMBER 447 L0
////>		ALOAD 0
////>		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////>		ALOAD 0
////>		ALOAD 1
////>		ALOAD 2
////>		ALOAD 3
////>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.shouldSideBeRendered (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z
////>		IRETURN
////#		ALOAD 0
////		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////		ALOAD 0
////		ALOAD 1
////		ALOAD 2
////		ALOAD 3
////		INVOKEVIRTUAL net/minecraft/block/Block.shouldSideBeRendered (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z
////		IRETURN
//
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, BlockStateContainer$StateImplementation_block());
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 3));
//		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
//				INVOKESTATIC,
//				"io/github/cadiboo/nocubes/hooks/Hooks",
//				"shouldSideBeRendered",
//				"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
//				false
//		));
//		instructions.insertBefore(ALOAD_0, new InsnNode(IRETURN));
//		LOGGER.info("Finished injecting into shouldSideBeRendered");
//
//	}
//
//	private static void redirect_doesSideBlockRendering(final ClassNode classNode) {
//
//		LOGGER.info("Starting injecting into doesSideBlockRendering");
//
//		final MethodNode doesSideBlockRendering;
//		doesSideBlockRendering = getMethod(classNode, "doesSideBlockRendering", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z");
//		final InsnList instructions = doesSideBlockRendering.instructions;
//		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
//		VarInsnNode ALOAD_0 = null;
//		while (iterator.hasNext()) {
//			final AbstractInsnNode insn = iterator.next();
//			if (insn.getOpcode() != ALOAD) {
//				continue;
//			}
//			if (insn.getType() != AbstractInsnNode.VAR_INSN) {
//				continue;
//			}
//			if (((VarInsnNode) insn).var != 0) {
//				continue;
//			}
//			ALOAD_0 = (VarInsnNode) insn;
//			break;
//		}
//
//		if (ALOAD_0 == null) {
//			return;
//		}
//
////		L0
////		LINENUMBER 447 L0
////>		ALOAD 0
////>		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////>		ALOAD 0
////>		ALOAD 1
////>		ALOAD 2
////>		ALOAD 3
////>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.doesSideBlockRendering (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z
////>		IRETURN
////#		ALOAD 0
////		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////		ALOAD 0
////		ALOAD 1
////		ALOAD 2
////		ALOAD 3
////		INVOKEVIRTUAL net/minecraft/block/Block.doesSideBlockRendering (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z
////		IRETURN
//
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, BlockStateContainer$StateImplementation_block());
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 3));
//		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
//				INVOKESTATIC,
//				"io/github/cadiboo/nocubes/hooks/Hooks",
//				"doesSideBlockRendering",
//				"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumFacing;)Z",
//				false
//		));
//		instructions.insertBefore(ALOAD_0, new InsnNode(IRETURN));
//		LOGGER.info("Finished injecting into doesSideBlockRendering");
//	}
//
//
//	public static void redirect_addCollisionBoxToList(final ClassNode classNode) {
//
//		LOGGER.info("Starting injecting into addCollisionBoxToList");
//
//		final MethodNode addCollisionBoxToList = getMethod(classNode, "func_185908_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V");
//		final InsnList instructions = addCollisionBoxToList.instructions;
//		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
//		VarInsnNode ALOAD_0 = null;
//		while (iterator.hasNext()) {
//			final AbstractInsnNode insn = iterator.next();
//			if (insn.getOpcode() != ALOAD) {
//				continue;
//			}
//			if (insn.getType() != AbstractInsnNode.VAR_INSN) {
//				continue;
//			}
//			if (((VarInsnNode) insn).var != 0) {
//				continue;
//			}
//			ALOAD_0 = (VarInsnNode) insn;
//			break;
//		}
//
//		if (ALOAD_0 == null) {
//			return;
//		}
//
////		L0
////		LINENUMBER 463 L0
////>		ALOAD 0
////>		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////>		ALOAD 0
////>		ALOAD 1
////>		ALOAD 2
////>		ALOAD 3
////>		ALOAD 4
////>		ALOAD 5
////>		ILOAD 6
////>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.addCollisionBoxToList (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
////#		ALOAD 0
////		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////		ALOAD 0
////		ALOAD 1
////		ALOAD 2
////		ALOAD 3
////		ALOAD 4
////		ALOAD 5
////		ILOAD 6
////		INVOKEVIRTUAL net/minecraft/block/Block.addCollisionBoxToList (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
////				L1
////		LINENUMBER 464 L1
////				RETURN
//
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, BlockStateContainer$StateImplementation_block());
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 3));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 4));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 5));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ILOAD, 6));
//		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
//				INVOKESTATIC,
//				"io/github/cadiboo/nocubes/hooks/Hooks",
//				"addCollisionBoxToList",
//				"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V",
//				false
//		));
//		instructions.insertBefore(ALOAD_0, new InsnNode(RETURN));
//		LOGGER.info("Finished injecting into addCollisionBoxToList");
//
//	}
//
//	public static void redirect_getCollisionBoundingBox(final ClassNode classNode) {
//
//		LOGGER.info("Starting injecting into getCollisionBoundingBox");
//
//		final MethodNode getCollisionBoundingBox = getMethod(classNode, "func_185890_d", "(Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;");
//		final InsnList instructions = getCollisionBoundingBox.instructions;
//		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
//		VarInsnNode ALOAD_0 = null;
//		while (iterator.hasNext()) {
//			final AbstractInsnNode insn = iterator.next();
//			if (insn.getOpcode() != ALOAD) {
//				continue;
//			}
//			if (insn.getType() != AbstractInsnNode.VAR_INSN) {
//				continue;
//			}
//			if (((VarInsnNode) insn).var != 0) {
//				continue;
//			}
//			ALOAD_0 = (VarInsnNode) insn;
//			break;
//		}
//
//		if (ALOAD_0 == null) {
//			return;
//		}
//
////		L0
////		LINENUMBER 458 L0
////>		ALOAD 0
////>		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////>		ALOAD 0
////>		ALOAD 1
////>		ALOAD 2
////>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.getCollisionBoundingBox (Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;
////>		ARETURN
////#		ALOAD 0
////		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
////		ALOAD 0
////		ALOAD 1
////		ALOAD 2
////		INVOKEVIRTUAL net/minecraft/block/Block.getCollisionBoundingBox (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;
////		ARETURN
//
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, BlockStateContainer$StateImplementation_block());
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
//		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
//		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
//				INVOKESTATIC,
//				"io/github/cadiboo/nocubes/hooks/Hooks",
//				"getCollisionBoundingBox",
//				"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;",
//				false
//		));
//		instructions.insertBefore(ALOAD_0, new InsnNode(ARETURN));
//
//	}

}
