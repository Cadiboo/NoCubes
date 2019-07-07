package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.mapField;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.mapMethod;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.start;

/**
 * @author Cadiboo
 */
final class RenderChunkTransformer implements Opcodes {

	private static boolean optifine;

	static void transform(final ClassNode classNode) {

		final List<MethodNode> methods = classNode.methods;

		//rebuildChunk
		{
			final String targetMethodDesc = "(FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V";
			final String targetMethodName = mapMethod("net/minecraft/client/renderer/chunk/RenderChunk", "func_178581_b", targetMethodDesc);

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

				start("Inject pre-iteration hook");
				injectPreIterationHook(method.instructions);
				finish();
				start("Apply " + "BlockRender hook");
				injectBlockRenderHook(method.instructions);
				finish();
				break;

			}
		}

	}

	// Finds the first instruction INVOKESTATIC BlockPos.getAllInBoxMutable
// then finds the previous label
// and inserts after the label and before the label's instructions.
	private static void injectPreIterationHook(InsnList instructions) {

//	BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();

//	// NoCubes Start
//	io.github.cadiboo.nocubes.hooks.Hooks.preIteration(this, x, y, z, generator, compiledchunk, blockpos, blockpos1, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher);
//	// NoCubes End
//	for(BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos1)) {

//   L31
//    LINENUMBER 137 L31
//    INVOKESTATIC net/minecraft/client/Minecraft.getInstance ()Lnet/minecraft/client/Minecraft;
//    INVOKEVIRTUAL net/minecraft/client/Minecraft.getBlockRendererDispatcher ()Lnet/minecraft/client/renderer/BlockRendererDispatcher;
//    ASTORE 15
//   L32
//    LINENUMBER 139 L32
//    ALOAD 7
//    ALOAD 8
//    INVOKESTATIC net/minecraft/util/math/BlockPos.getAllInBoxMutable (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;
//    INVOKEINTERFACE java/lang/Iterable.iterator ()Ljava/util/Iterator; (itf)
//    ASTORE 16
//   L33

//   L31
//    LINENUMBER 141 L31
//    INVOKESTATIC net/minecraft/client/Minecraft.getInstance ()Lnet/minecraft/client/Minecraft;
//    INVOKEVIRTUAL net/minecraft/client/Minecraft.getBlockRendererDispatcher ()Lnet/minecraft/client/renderer/BlockRendererDispatcher;
//    ASTORE 15
//   L32
//    LINENUMBER 143 L32
//    ALOAD 0
//    FLOAD 1
//    FLOAD 2
//    FLOAD 3
//    ALOAD 4
//    ALOAD 5
//    ALOAD 7
//    ALOAD 8
//    ALOAD 9
//    ALOAD 10
//    ALOAD 11
//    ALOAD 12
//    ALOAD 13
//    ALOAD 14
//    ALOAD 15
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.preIteration (Lnet/minecraft/client/renderer/chunk/RenderChunk;FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;Lnet/minecraft/client/renderer/chunk/CompiledChunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/client/renderer/chunk/RenderChunkCache;Lnet/minecraft/client/renderer/chunk/VisGraph;Ljava/util/HashSet;[ZLjava/util/Random;Lnet/minecraft/client/renderer/BlockRendererDispatcher;)V
//   L33
//    LINENUMBER 144 L33
//    ALOAD 7
//    ALOAD 8
//    INVOKESTATIC net/minecraft/util/math/BlockPos.getAllInBoxMutable (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;
//    INVOKEINTERFACE java/lang/Iterable.iterator ()Ljava/util/Iterator; (itf)
//    ASTORE 16
//   L34

		String getAllInBoxMutable_name = mapMethod("net/minecraft/util/math/BlockPos", "func_177975_b", "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;"); // getAllInBoxMutable

		optifine = false;

		AbstractInsnNode first_INVOKESTATIC_getAllInBoxMutable = null;
		int arrayLength = instructions.size();
		for (int i = 0; i < arrayLength; ++i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == INVOKESTATIC) {
				MethodInsnNode instruction = (MethodInsnNode) insn;
				if (instruction.owner.equals("net/minecraft/util/math/BlockPos") || instruction.owner.equals("net/optifine/BlockPosM")) {
					if (instruction.name.equals(getAllInBoxMutable_name) || instruction.name.equals("getAllInBoxMutable")) {
						if (instruction.desc.equals("(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;")) {
							if (instruction.itf == false) {
								first_INVOKESTATIC_getAllInBoxMutable = instruction;
								log("Found Injection Point");
								optifine = instruction.owner.equals("net/optifine/BlockPosM");
								log("OptiFine: " + optifine);
								break;
							}
						}
					}
				}
			}
		}
		if (first_INVOKESTATIC_getAllInBoxMutable == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

		AbstractInsnNode firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable = null;
		for (int i = instructions.indexOf(first_INVOKESTATIC_getAllInBoxMutable); i >= 0; --i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable = instruction;
				log("Found label " + instruction);
				break;
			}
		}
		if (firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable == null) {
			throw new RuntimeException("Error: Couldn't find label!");
		}

		InsnList toInject = new InsnList();

		// Labels n stuff
		LabelNode originalInstructionsLabel = new LabelNode();

//		ALOAD 0
//		FLOAD 1
//		FLOAD 2
//		FLOAD 3
//		ALOAD 4
//		ALOAD 5
//		ALOAD 7
//		ALOAD 8
//		ALOAD 0
//		GETFIELD net/minecraft/client/renderer/chunk/RenderChunk.world : Lnet/minecraft/world/World;
//		ALOAD 0
//		GETFIELD net/minecraft/client/renderer/chunk/RenderChunk.worldView : Lnet/minecraft/world/ChunkCache;
//		ALOAD 9
//		ALOAD 10
//		ALOAD 11
//		ALOAD 12
//		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.preIteration

		final String worldFieldName = mapField("net/minecraft/client/renderer/chunk/RenderChunk", "field_178588_d");
		// Make list of instructions to inject
		if (!optifine) {
			final String worldViewFieldName = mapField("net/minecraft/client/renderer/chunk/RenderChunk", "field_189564_r");

			toInject.add(new VarInsnNode(ALOAD, 0)); // this
			toInject.add(new VarInsnNode(FLOAD, 1)); // x
			toInject.add(new VarInsnNode(FLOAD, 2)); // y
			toInject.add(new VarInsnNode(FLOAD, 3)); // z
			toInject.add(new VarInsnNode(ALOAD, 4)); // generator
			toInject.add(new VarInsnNode(ALOAD, 5)); // compiledchunk
			toInject.add(new VarInsnNode(ALOAD, 7)); // blockpos | position
			toInject.add(new VarInsnNode(ALOAD, 8)); // blockpos1 | endPosition
			toInject.add(new VarInsnNode(ALOAD, 0));
			toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/chunk/RenderChunk", worldFieldName, "Lnet/minecraft/world/World;"));
			toInject.add(new VarInsnNode(ALOAD, 0));
			toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/chunk/RenderChunk", worldViewFieldName, "Lnet/minecraft/world/ChunkCache;"));
			toInject.add(new VarInsnNode(ALOAD, 9)); // VisGraph
			toInject.add(new VarInsnNode(ALOAD, 10)); // tileEntitiesWithGlobalRenderers
			toInject.add(new VarInsnNode(ALOAD, 11)); // aboolean | usedRenderLayers
			toInject.add(new VarInsnNode(ALOAD, 12)); // blockrendererdispatcher
		} else {
//			private static final int ALOAD_blockAccess_chunkCacheOF = 11;
//			private static final int ALOAD_aboolean = 12;
//			private static final int ALOAD_blockrendererdispatcher = 13;
			toInject.add(new VarInsnNode(ALOAD, 0)); // this
			toInject.add(new VarInsnNode(FLOAD, 1)); // x
			toInject.add(new VarInsnNode(FLOAD, 2)); // y
			toInject.add(new VarInsnNode(FLOAD, 3)); // z
			toInject.add(new VarInsnNode(ALOAD, 4)); // generator
			toInject.add(new VarInsnNode(ALOAD, 5)); // compiledchunk
			toInject.add(new VarInsnNode(ALOAD, 7)); // blockpos | position
			toInject.add(new VarInsnNode(ALOAD, 8)); // blockpos1 | endPosition
			toInject.add(new VarInsnNode(ALOAD, 0));
			toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/chunk/RenderChunk", worldFieldName, "Lnet/minecraft/world/World;"));
			toInject.add(new VarInsnNode(ALOAD, 11)); // blockAccess | chunkCacheOF
			toInject.add(new VarInsnNode(ALOAD, 9)); // VisGraph
			toInject.add(new VarInsnNode(ALOAD, 10)); // tileEntitiesWithGlobalRenderers
			toInject.add(new VarInsnNode(ALOAD, 12)); // aboolean | usedRenderLayers
			toInject.add(new VarInsnNode(ALOAD, 13)); // blockrendererdispatcher
		}
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKESTATIC,
				//String owner
				"io/github/cadiboo/nocubes/hooks/Hooks",
				//String name
				"preIteration",
				//String descriptor
				"(Lnet/minecraft/client/renderer/chunk/RenderChunk;FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;Lnet/minecraft/client/renderer/chunk/CompiledChunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/chunk/VisGraph;Ljava/util/HashSet;[ZLnet/minecraft/client/renderer/BlockRendererDispatcher;)V",
				//boolean isInterface
				false
		));

		toInject.add(originalInstructionsLabel);

		// Inject instructions
		instructions.insert(firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable, toInject);

	}

	// 1) find IBlockState.getRenderType
// 2) find label for IBlockState.getRenderType
// 3) find label that IBlockState.getRenderType would jump to
// 4) insert right after IBlockState.getRenderType label
	static void injectBlockRenderHook(InsnList instructions) {

//	if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && iblockstate.canRenderInLayer(blockrenderlayer1)) {

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.hooks.Hooks.canBlockStateRender(iblockstate))
//	// NoCubes End
//	if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && iblockstate.canRenderInLayer(blockrenderlayer1)) {

//    INVOKEVIRTUAL net/minecraft/client/renderer/BlockRendererDispatcher.renderFluid (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IWorldReader;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/fluid/IFluidState;)Z
//    IOR
//    BASTORE
//   L54
//    LINENUMBER 174 L54
//   FRAME CHOP 2
//    ALOAD 18
//    INVOKEINTERFACE net/minecraft/block/state/IBlockState.getRenderType ()Lnet/minecraft/util/EnumBlockRenderType; (itf)
//    GETSTATIC net/minecraft/util/EnumBlockRenderType.INVISIBLE : Lnet/minecraft/util/EnumBlockRenderType;
//    IF_ACMPEQ L61
//    ALOAD 18
//    ALOAD 25
//    INVOKEINTERFACE net/minecraft/block/state/IBlockState.canRenderInLayer (Lnet/minecraft/util/BlockRenderLayer;)Z (itf)
//    IFEQ L61

//    INVOKEVIRTUAL net/minecraft/client/renderer/BlockRendererDispatcher.func_215331_a (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IEnviromentBlockReader;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/fluid/IFluidState;)Z
//    IOR
//    BASTORE
//   L54
//    LINENUMBER 192 L54
//   FRAME CHOP 2
//    ALOAD 18
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.canBlockStateRender (Lnet/minecraft/block/BlockState;)Z
//    IFEQ L61
//   L62
//    LINENUMBER 194 L62
//    ALOAD 18
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getRenderType ()Lnet/minecraft/block/BlockRenderType;
//    GETSTATIC net/minecraft/block/BlockRenderType.INVISIBLE : Lnet/minecraft/block/BlockRenderType;
//    IF_ACMPEQ L61
//    ALOAD 18
//    ALOAD 25
//    INVOKEVIRTUAL net/minecraft/block/BlockState.canRenderInLayer (Lnet/minecraft/util/BlockRenderLayer;)Z
//    IFEQ L61

		LabelNode blockCannotRenderLabel = null;

		String getRenderType_name = mapMethod("net/minecraft/block/state/IBlockProperties", "func_185901_i", "()Lnet/minecraft/util/EnumBlockRenderType;"); // getRenderType

		AbstractInsnNode IBlockState_getRenderType = null;
		int arrayLength = instructions.size();
		for (int i = 0; i < arrayLength; ++i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == INVOKEINTERFACE) {
				MethodInsnNode instruction = (MethodInsnNode) insn;
				if (instruction.owner.equals("net/minecraft/block/state/IBlockState")) {
					if (instruction.name.equals(getRenderType_name)) {
						if (instruction.desc.equals("()Lnet/minecraft/util/EnumBlockRenderType;")) {
							if (instruction.itf == true) {
								IBlockState_getRenderType = instruction;
								log("Found injection point " + instruction);
								break;
							}
						}
					}
				}
			}
		}
		if (IBlockState_getRenderType == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

		AbstractInsnNode firstLabelBefore_IBlockState_getRenderType = null;
		for (int i = instructions.indexOf(IBlockState_getRenderType); i >= 0; --i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				firstLabelBefore_IBlockState_getRenderType = instruction;
				log("Found label " + instruction);
				break;
			}
		}
		if (firstLabelBefore_IBlockState_getRenderType == null) {
			throw new RuntimeException("Error: Couldn't find label!");
		}

		int lookStart = instructions.indexOf(IBlockState_getRenderType);
		int lookMax = lookStart + 10;
		for (int i = lookStart; i < lookMax; ++i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getOpcode() == IFEQ || instruction.getOpcode() == IFNE || instruction.getOpcode() == IF_ACMPEQ) {
				JumpInsnNode jumpInsn = (JumpInsnNode) instruction;
				blockCannotRenderLabel = jumpInsn.label;
				log("Found blockCannotRenderLabel " + jumpInsn.label);
				break;
			}
		}
		if (blockCannotRenderLabel == null) {
			throw new RuntimeException("Error: Couldn't find blockCannotRenderLabel!");
		}

		InsnList toInject = new InsnList();

		// Labels n stuff
		LabelNode originalInstructionsLabel = new LabelNode();

		// Make list of instructions to inject
		toInject.add(new VarInsnNode(ALOAD, optifine ? 18 : 15)); // iblockstate
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKESTATIC,
				//String owner
				"io/github/cadiboo/nocubes/hooks/Hooks",
				//String name
				"canBlockStateRender",
				//String descriptor
				"(Lnet/minecraft/block/state/IBlockState;)Z",
				//boolean isInterface
				false
		));
		toInject.add(new JumpInsnNode(IFEQ, blockCannotRenderLabel));

		toInject.add(originalInstructionsLabel);

		// Inject instructions
		instructions.insert(firstLabelBefore_IBlockState_getRenderType, toInject);

	}

}
