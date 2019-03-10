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
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.TraceClassVisitor;

import javax.annotation.Nullable;
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
	public byte[] transform(final String name, final String transformedName, @Nullable final byte[] basicClass) {
		if (basicClass == null) {
			return null;
		}
		switch (transformedName) {
			case "net.minecraft.block.state.BlockStateContainer$StateImplementation":
				return transformClass(basicClass, transformedName,
						Transformer::hook_isOpaqueCube,
						Transformer::hook_getCollisionBoundingBox,
						Transformer::hook_addCollisionBoxToList
				);
			case "net.minecraft.entity.Entity":
//		    	return transformClass(basicClass, transformedName,
//		    			Transformer::redirect_isEntityInsideOpaqueBlock
//		    	);
				break;
			case "io.github.cadiboo.nocubes.hooks.IsOpaqueCubeHook": // WATCH OUT - everything fails if this misses
				return transformClass(basicClass, transformedName,
						Transformer::add_runIsOpaqueCubeDefaultOnce
				);
			case "io.github.cadiboo.nocubes.hooks.GetCollisionBoundingBoxHook": // WATCH OUT - everything fails if this misses
				return transformClass(basicClass, transformedName,
						Transformer::add_runGetCollisionBoundingBoxDefaultOnce
				);
			case "io.github.cadiboo.nocubes.hooks.AddCollisionBoxToListHook": // WATCH OUT - everything fails if this misses
				return transformClass(basicClass, transformedName,
						Transformer::add_runAddCollisionBoxToListDefaultOnce
				);
		}
//		else if (transformedName.equals("io.gitub.cadiboo.nocubes.hooks.GetCollisionBoundingBoxHook")) {
//			return transformClass(basicClass, transformedName
////					,
////					Transformer::add_runGetCollisionBoundingBoxDefaultOnce
//			);
//		}
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

	// Copied from  ObfuscationReflectionHelper
	private static class ObfuscationHelper {

		private static String remapMethodName(final String internalClassName, final String methodName, final String desc) {
			final String remappedName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(internalClassName, methodName, desc);
			LOGGER.info("Remapped method name " + methodName + " to " + remappedName);
			return remappedName;
		}

		private static String remapFieldName(final String internalClassName, final String fieldName) {
			final String remappedName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(internalClassName, fieldName, null);
			LOGGER.info("Remapped field name " + fieldName + " to " + remappedName);
			return remappedName;
		}

	}

	private static class Api {

		public static MethodNode getMethodNode() {
			return new MethodNode();
		}

	}

	private static void invokeRunOnceInMethod(final MethodNode methodNode, final String fieldName) {
		final InsnList instructions = methodNode.instructions;
		instructions.clear();
		instructions.add(new LabelNode(new Label()));
		setRunOnceToTRUE(instructions, fieldName);
		instructions.add(new LabelNode(new Label()));
		instructions.add(new InsnNode(RETURN));
	}

	private static void addRunOnceFieldToClass(final ClassNode classNode, final String fieldName) {
		LOGGER.info("Starting adding field " + fieldName);
		{
			classNode.fields.add(new FieldNode(ACC_PUBLIC, fieldName, "Z", null, false));
		}
		LOGGER.info("Finished adding field " + fieldName);
	}

	private static void setRunOnceToTRUE(final InsnList instructions, final String fieldName) {
		instructions.add(new VarInsnNode(ALOAD, 0));
		instructions.add(new InsnNode(ICONST_1));
		instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", fieldName, "Z"));
	}

	public static final String run_isOpaqueCube_fieldName = "nocubes_RunIsOpaqueCubeDefaultOnce";
	public static final String run_getCollisionBoundingBox_fieldName = "nocubes_RunGetCollisionBoundingBoxDefaultOnce";
	public static final String run_addCollisionBoxToList_fieldName = "nocubes_RunAddCollisionBoxToListDefaultOnce";

	private static void hook_isOpaqueCube(final ClassNode classNode) {

		addRunOnceFieldToClass(classNode, run_isOpaqueCube_fieldName);

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

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_isOpaqueCube_fieldName, "Z"));
			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/NoCubes", "areHooksEnabled", "()Z", false));
			injectedInstructions.add(new JumpInsnNode(IFEQ, labelNode));

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(BlockStateContainer$StateImplementation_block());
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new MethodInsnNode(
					INVOKESTATIC,
					"io/github/cadiboo/nocubes/hooks/IsOpaqueCubeHook",
					"isOpaqueCube",
					"(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;)Z",
					false
			));

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new InsnNode(IRETURN));
			injectedInstructions.add(labelNode);

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new InsnNode(ICONST_0));
			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_isOpaqueCube_fieldName, "Z"));

			injectedInstructions.add(new LabelNode(new Label()));
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
			invokeRunOnceInMethod(
					getMethod(classNode, "runIsOpaqueCubeDefaultOnce", "(Lnet/minecraft/block/state/BlockStateContainer$StateImplementation;)V"),
					run_isOpaqueCube_fieldName
			);
		}
		LOGGER.info("Finished injecting into runIsOpaqueCubeDefaultOnce");
	}

	private static void hook_getCollisionBoundingBox(final ClassNode classNode) {

		addRunOnceFieldToClass(classNode, run_getCollisionBoundingBox_fieldName);

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

	private static void add_runGetCollisionBoundingBoxDefaultOnce(final ClassNode classNode) {
		LOGGER.info("Starting injecting into runGetCollisionBoundingBoxDefaultOnce");
		{
			invokeRunOnceInMethod(
					getMethod(classNode, "runGetCollisionBoundingBoxDefaultOnce", "(Lnet/minecraft/block/state/BlockStateContainer$StateImplementation;)V"),
					run_getCollisionBoundingBox_fieldName
			);
		}
		LOGGER.info("Finished injecting into runGetCollisionBoundingBoxDefaultOnce");
	}

	private static void hook_addCollisionBoxToList(final ClassNode classNode) {

		addRunOnceFieldToClass(classNode, run_addCollisionBoxToList_fieldName);

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

			final LabelNode labelNode = new LabelNode(new Label());
			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", run_addCollisionBoxToList_fieldName, "Z"));
			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));
			injectedInstructions.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/NoCubes", "areHooksEnabled", "()Z", false));
			injectedInstructions.add(new JumpInsnNode(IFEQ, labelNode));

			injectedInstructions.add(new LabelNode(new Label()));
			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/state/BlockStateContainer$StateImplementation", "block", "Lnet/minecraft/block/Block;"));
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

	private static void add_runAddCollisionBoxToListDefaultOnce(final ClassNode classNode) {
		LOGGER.info("Starting injecting into runAddCollisionBoxToListDefaultOnce");
		{
			invokeRunOnceInMethod(
					getMethod(classNode, "runAddCollisionBoxToListDefaultOnce", "(Lnet/minecraft/block/Block;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V"),
					run_addCollisionBoxToList_fieldName
			);
		}
		LOGGER.info("Finished injecting into runAddCollisionBoxToListDefaultOnce");
	}

}
