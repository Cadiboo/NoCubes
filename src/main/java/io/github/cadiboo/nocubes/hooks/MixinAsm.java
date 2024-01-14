package io.github.cadiboo.nocubes.hooks;

import net.minecraftforge.coremod.api.ASMAPI;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

/**
 * We use Mixins to do most of our ASM (runtime class modification).
 * However, Mixins can't do everything, this class contains the modifications we can't do with Mixins.
 * Used by {@link io.github.cadiboo.nocubes.mixin.NoCubesMixinPlugin}.
 */
public final class MixinAsm {

	private static boolean transformChunkRendererRanAlready = false;
	private static boolean transformFluidRendererRanAlready = false;

	/**
	 * Hooks multiple parts of the chunk rendering method to allow us to do our own custom rendering
	 * - Injects our {@link io.github.cadiboo.nocubes.hooks.Hooks#preIteration} hook
	 * - Injects our {@link io.github.cadiboo.nocubes.hooks.Hooks#getRenderFluidState} hook
	 */
	public static void transformChunkRenderer(ClassNode targetClass) {
		if (transformChunkRendererRanAlready)
			return;
		transformChunkRendererRanAlready = true;

		var methodNode = findMethodNode(
			targetClass,
			"m_234467_", // "compile"
			"(FFFLnet/minecraft/client/renderer/ChunkBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask$CompileResults;"
		);
		var instructions = methodNode.instructions;

		var isOptiFinePresent = detectOptiFine(instructions);
		// OptiFine G8 added two booleans to the stack (shaders and shadersMidBlock)
		var ofg8 = isOptiFinePresent && null != tryFindFirstFieldInstruction(instructions, Opcodes.GETSTATIC, "net/optifine/shaders/Shaders", "useMidBlockAttrib", "Z");

		// Inject the hook where we do our rendering
		// We inject right above where vanilla loops (iterates) through all the blocks
		{
			var positionsIteratorCall = findFirstMethodCall(
				methodNode,
				ASMAPI.MethodType.STATIC,
				isOptiFinePresent ? "net/optifine/BlockPosM" : "net/minecraft/core/BlockPos",
				isOptiFinePresent ? "getAllInBoxMutable" : ASMAPI.mapMethod("m_121940_"), // BlockPos#betweenClosed
				"(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/BlockPos;)Ljava/lang/Iterable;",
				0 // startIndex
			);
			var firstLabelBeforePositionsIteratorCall = findFirstLabelBefore(instructions, positionsIteratorCall);

			// I'm not sure if this is still necessary, but it works so I'm not touching it (I remember it was painful to get right)
			var outerClassFieldName = isOptiFinePresent ? "this$1" : ASMAPI.mapField("f_112859_");
			instructions.insert(firstLabelBeforePositionsIteratorCall, ASMAPI.listOf(
				// Fields
				new VarInsnNode(Opcodes.ALOAD, 0), // this
				new VarInsnNode(Opcodes.ALOAD, 0), // ChunkRender.this
				new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask", outerClassFieldName, "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;"),
				// Params
				new VarInsnNode(Opcodes.ALOAD, 4), // buffers
				// Local variables
				new VarInsnNode(Opcodes.ALOAD, 7), // chunkPos
				new VarInsnNode(Opcodes.ALOAD, isOptiFinePresent ? 11 : 10), // world (from RebuildTask.region)
				// Scoped local variables
				new VarInsnNode(Opcodes.ALOAD, isOptiFinePresent ? 10 : 11), // matrix
				new VarInsnNode(Opcodes.ALOAD, isOptiFinePresent ? 15 : 12), // usedLayers
				new VarInsnNode(Opcodes.ALOAD, isOptiFinePresent ? (ofg8 ? 16 : 14) : 13), // random
				new VarInsnNode(Opcodes.ALOAD, isOptiFinePresent ? (ofg8 ? 17 : 15) : 14), // dispatcher
				callNoCubesHook("preIteration", "(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk$RebuildTask;Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$RenderChunk;Lnet/minecraft/client/renderer/ChunkBufferBuilderPack;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/BlockAndTintGetter;Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/Set;Lnet/minecraft/util/RandomSource;Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;)V"),
				new LabelNode() // Label for original instructions
			));
			print("Done injecting the preIteration hook");
		}

		// Redirects 'state.getFluidState()' to our own code so we can have extended fluids render properly
		{
			var getFluidStateCall = findFirstMethodCall(
				methodNode,
				ASMAPI.MethodType.VIRTUAL,
				"net/minecraft/world/level/block/state/BlockState",
				ASMAPI.mapMethod("m_60819_"), // getFluidState
				"()Lnet/minecraft/world/level/material/FluidState;",
				0 // startIndex
			);
			var previousLabel = findFirstLabelBefore(instructions, getFluidStateCall);
			removeBetweenIndicesInclusive(instructions, instructions.indexOf(previousLabel) + 1, instructions.indexOf(getFluidStateCall));
			instructions.insert(previousLabel, ASMAPI.listOf(
				new VarInsnNode(Opcodes.ALOAD, isOptiFinePresent ? (ofg8 ? 19 : 17) : 16), // pos
				new VarInsnNode(Opcodes.ALOAD, isOptiFinePresent ? (ofg8 ? 20 : 18) : 18), // state
				callNoCubesHook("getRenderFluidState", "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;")
			));
			// We didn't remove the ASTORE instruction with our 'removeBetweenIndicesInclusive' so the result of our hook call automatically gets stored
			print("Done injecting the fluid state getter redirect");
		}

	}

	static boolean detectOptiFine(InsnList instructions) {
		var length = instructions.size();
		for (var i = 0; i < length; ++i) {
			var instruction = instructions.get(i);
			if (instruction instanceof MethodInsnNode methodInsnNode) {
				var owner = methodInsnNode.owner;
				if (Objects.equals(owner, "net/optifine/override/ChunkCacheOF") || Objects.equals(owner, "net/optifine/BlockPosM")) {
					print("Detected OptiFine");
					return true;
				}
			}
		}
		print("Did not detect OptiFine");
		return false;
	}

	/**
	 * Changes fluid rendering to support extended fluid rendering
	 * - Injects our {@link io.github.cadiboo.nocubes.hooks.Hooks#getRenderFluidState} hook
	 */
	public static void transformFluidRenderer(ClassNode targetClass) {
		if (transformFluidRendererRanAlready)
			return;
		transformFluidRendererRanAlready = true;

		var methodNode = findMethodNode(
			targetClass,
			"m_234369_", // tesselate
			"(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/material/FluidState;)V"
		);
		var instructions = methodNode.instructions;
		// Redirect every 'blockState.getFluidState()' call preceded by a 'world.getBlockState(pos)' to our 'getRenderFluidState' hook
		// This could be converted to a Mixin but
		// - Each offset block pos would need to be recreated (currently using DUP_X1 to avoid this) making it less efficient that this ASM
		// - Targeting each different 'blockState.getFluidState()' call might be hard
		// Warning - clever/complex code:
		// - Uses DUP_X1 to copy the 'pos' parameter from the 'world.getBlockState(pos)' call onto the stack (below the 'world' param to not interfere with the call)
		// - Uses DUP to copy the 'state' returned from the 'world.getBlockState(pos)' call onto the stack
		// - Removes the existing 'blockState.getFluidState()' call
		// - Calls our 'getRenderFluidState' with the 'pos' and 'state', removing them from the stack
		// Repeats this for all 6 invocations at the start of the method
		var lastIndex = 0;
		for (var direction = 0; direction < 6; ++direction) {
			var getBlockStateCall = findFirstMethodCall(
				methodNode,
				ASMAPI.MethodType.INTERFACE,
				"net/minecraft/world/level/BlockAndTintGetter",
				ASMAPI.mapMethod("m_8055_"), // getBlockState
				"(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
				lastIndex + 1
			);
			// DUP the blockPos parameter and put it lower on the stack than world
			instructions.insertBefore(getBlockStateCall, new InsnNode(Opcodes.DUP_X1));
			// DUP the returned blockState
			instructions.insert(getBlockStateCall, new InsnNode(Opcodes.DUP));
			lastIndex = instructions.indexOf(getBlockStateCall);
			var getFluidStateCall = findFirstMethodCall(
				methodNode,
				ASMAPI.MethodType.VIRTUAL,
				"net/minecraft/world/level/block/state/BlockState",
				ASMAPI.mapMethod("m_60819_"), // getFluidState
				"()Lnet/minecraft/world/level/material/FluidState;",
				lastIndex + 1
			);
			var previousLabel = findFirstLabelBefore(instructions, getFluidStateCall);
			removeBetweenIndicesInclusive(instructions, instructions.indexOf(previousLabel) + 1, instructions.indexOf(getFluidStateCall));
			instructions.insert(previousLabel, callNoCubesHook("getRenderFluidState", "(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/world/level/material/FluidState;"));
			// We didn't remove the ASTORE instruction with our 'removeBetweenIndicesInclusive' so the result of our hook call automatically gets stored
		}
	}

	// region Utility functions

	static void print(String msg) {
		LogManager.getLogger("NoCubes ASM").info(msg);
	}

	static MethodNode findMethodNode(ClassNode classNode, String obfuscatedName, String desc) {
		var name = ASMAPI.mapMethod(obfuscatedName);
		for (MethodNode methodNode : classNode.methods) {
			if (name.equals(methodNode.name) && desc.equals(methodNode.desc))
				return methodNode;
		}
		throw new RuntimeException("NoCubes: Could not find method " + name);
	}

	static void assertInstructionFound(AbstractInsnNode instruction, String name, InsnList instructions) {
		if (instruction == null)
			throw new RuntimeException("Error: Couldn't find '" + name + "' in instructions:\n" + stringifyInstructions(instructions));
	}

	static LabelNode findFirstLabelBefore(InsnList instructions, AbstractInsnNode start) {
		return findFirstLabelBeforeIndex(instructions, instructions.indexOf(start));
	}

	static LabelNode findFirstLabelBeforeIndex(InsnList instructions, int startIndex) {
		var length = instructions.size();
		if (startIndex == -1)
			startIndex = length - 1;
		for (var i = startIndex; i >= 0; --i) {
			var instruction = instructions.get(i);
			if (instruction instanceof LabelNode labelNode) {
				print("Found first label before index " + startIndex + ": " + labelNode);
				return labelNode;
			}
		}
		throw new RuntimeException("Error: Couldn't find first label before index " + startIndex + " in " + stringifyInstructions(instructions));
	}

	static FieldInsnNode tryFindFirstFieldInstruction(InsnList instructions, int opcode, String owner, String name, String desc) {
		for (int i = 0, length = instructions.size(); i < length; ++i) {
			var instruction = instructions.get(i);
			if (!(instruction instanceof FieldInsnNode fieldInsnNode) || fieldInsnNode.getOpcode() != opcode || !Objects.equals(fieldInsnNode.owner, owner) || !Objects.equals(fieldInsnNode.name, name) || !Objects.equals(fieldInsnNode.desc, desc))
				continue;
			return fieldInsnNode;
		}
		return null;
	}

	static MethodInsnNode findFirstMethodCall(MethodNode methodNode, ASMAPI.MethodType methodType, String owner, String name, String desc, int startIndex) {
		var instruction = ASMAPI.findFirstMethodCallAfter(methodNode, methodType, owner, name, desc, startIndex);
		assertInstructionFound(instruction, name + " call", methodNode.instructions);
		return instruction;
	}

	/**
	 * Utility function to create an INVOKESTATIC call to one of our hooks
	 *
	 * @param {string} name The name of the hook method
	 * @param {string} desc The hook method's method descriptor
	 * @return {object} The transformersObj with all transformers wrapped
	 */
	static MethodInsnNode callNoCubesHook(String name, String desc) {
		return new MethodInsnNode(
			//int opcode
			Opcodes.INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			name,
			//String descriptor
			desc,
			//boolean isInterface
			false
		);
	}

	/**
	 * Utility function for removing multiple instructions
	 *
	 * @param {InsnList} instructions The list of instructions to modify
	 * @param {number} start The index of the first instruction in the list to be removed
	 * @param {number} end The index of the last instruction in the list to be removed
	 */
	static void removeBetweenIndicesInclusive(InsnList instructions, int start, int end) {
		for (var i = start; i <= end; ++i)
			instructions.remove(instructions.get(start));
	}

	static String stringifyInstructions(InsnList instructions) {
		var printer = new Textifier();
		var visitor = new TraceMethodVisitor(printer);

		instructions.accept(visitor);

		var writer = new StringWriter();
		printer.print(new PrintWriter(writer));
		return writer.toString();
	}

	// endregion

}
