/**
 * This function is called by Forge before any minecraft classes are loaded to
 * setup the coremod.
 *
 * @return {object} All the transformers of this coremod.
 */
function initializeCoreMod() {

	/*Class/Interface*/ Opcodes = Java.type("org.objectweb.asm.Opcodes");
	/*Class*/ ASMAPI = Java.type("net.minecraftforge.coremod.api.ASMAPI");

	/*Class*/ InsnList = Java.type("org.objectweb.asm.tree.InsnList");
	/*Class*/ LabelNode = Java.type("org.objectweb.asm.tree.LabelNode");

	/*Class*/ FieldNode = Java.type("org.objectweb.asm.tree.FieldNode");
	/*Class*/ MethodNode = Java.type("org.objectweb.asm.tree.MethodNode");

	/*Class*/ AbstractInsnNode = Java.type("org.objectweb.asm.tree.AbstractInsnNode");
	/*Class*/ InsnNode = Java.type("org.objectweb.asm.tree.InsnNode");
	/*Class*/ VarInsnNode = Java.type("org.objectweb.asm.tree.VarInsnNode");
	/*Class*/ FieldInsnNode = Java.type("org.objectweb.asm.tree.FieldInsnNode");
	/*Class*/ MethodInsnNode = Java.type("org.objectweb.asm.tree.MethodInsnNode");
	/*Class*/ JumpInsnNode = Java.type("org.objectweb.asm.tree.JumpInsnNode");
	/*Class*/ TypeInsnNode = Java.type("org.objectweb.asm.tree.TypeInsnNode");

	ACC_PUBLIC = Opcodes.ACC_PUBLIC;

	INVOKESTATIC = Opcodes.INVOKESTATIC;
	INVOKEVIRTUAL = Opcodes.INVOKEVIRTUAL;
	INVOKEINTERFACE = Opcodes.INVOKEINTERFACE;

	ALOAD = Opcodes.ALOAD;
	ILOAD = Opcodes.ILOAD;
	FLOAD = Opcodes.FLOAD;
	DLOAD = Opcodes.DLOAD;

	ISTORE = Opcodes.ISTORE;

	RETURN = Opcodes.RETURN;
	ARETURN = Opcodes.ARETURN;
	IRETURN = Opcodes.IRETURN;
	DRETURN = Opcodes.DRETURN;

	NEW = Opcodes.NEW;
	CHECKCAST = Opcodes.CHECKCAST;

	ACONST_NULL = Opcodes.ACONST_NULL;
	ICONST_0 = Opcodes.ICONST_0;

	IFEQ = Opcodes.IFEQ;
	IFNE = Opcodes.IFNE;
	IF_ACMPEQ = Opcodes.IF_ACMPEQ;

	GETFIELD = Opcodes.GETFIELD;
	GETSTATIC = Opcodes.GETSTATIC;

	GOTO = Opcodes.GOTO;

	LABEL = AbstractInsnNode.LABEL;
	METHOD_INSN = AbstractInsnNode.METHOD_INSN;

	isOptiFinePresent = false;

	return wrapWithLogging(wrapMethodTransformers({
//		"ChunkRenderCache#<init>": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.client.renderer.chunk.ChunkRenderCache",
//				"methodName": "<init>",
//				"methodDesc": "(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V"
//			},
//			"transformer": function(methodNode) {
//				// OptiFine makes the BlockState[] and IFluidState[] null.
//				// Vanilla doesn't use null anywhere in the method.
//				var instructions = methodNode.instructions;
//				for (var i = instructions.size() - 1; i >= 0; --i) {
//					if (instructions.get(i).getOpcode() == ACONST_NULL) {
//						isOptiFinePresent = true;
//						print("Found OptiFine - ChunkRenderCache#<init> NULL");
//						break;
//					}
//				}
//				if (!isOptiFinePresent)
//					injectInitChunkRenderCacheHook(instructions);
//				return methodNode;
//			}
//		},
//		"ChunkRenderCache.generateCache": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.client.renderer.chunk.ChunkRenderCache",
//				"methodName": "func_212397_a",
//				"methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;"
//			},
//			"transformer": function(methodNode) {
//				// OptiFine immediately calls another method.
//				var instructions = methodNode.instructions;
//				for (var i = instructions.size() - 1; i >= 0; --i) {
//					var instruction = instructions.get(i)
//					if (instruction.getOpcode() != INVOKESTATIC)
//						continue;
//					if (!instruction.owner.equals("net/minecraft/client/renderer/chunk/ChunkRenderCache"))
//						continue;
//					// OptiFine's methods aren't obfuscated and this one has an MCP name
//					if (!instruction.name.equals("generateCache"))
//						continue;
//					// This method has an extra boolean parameter at the end
//					if (!instruction.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;IZ)Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;"))
//						continue;
//					print("Found OptiFine - ChunkRenderCache.generateCache (OptiFine Overload)");
//					isOptiFinePresent = true;
//					break;
//				}
//				if (!isOptiFinePresent)
//					injectGenerateCacheHook(instructions);
//				return methodNode;
//			}
//		},
//		"ChunkRenderCache.generateCache OptiFine": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.client.renderer.chunk.ChunkRenderCache",
//				"methodName": "generateCache",
//				"methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;IZ)Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;"
//			},
//			"transformer": function(methodNode) {
//				// OptiFine added method
//				isOptiFinePresent = true;
//				print("Found OptiFine - ChunkRenderCache#generateCache OptiFine");
//				injectGenerateCacheHook(methodNode.instructions);
//				return methodNode;
//			}
//		},
//		"BlockRendererDispatcher#renderBlockDamage": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.client.renderer.BlockRendererDispatcher",
//				// Forge-added overload
//				"methodName": "renderBlockDamage",
//				"methodDesc": "(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockDisplayReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraftforge/client/model/data/IModelData;)V"
//			},
//			"transformer": function(methodNode) {
//				injectRenderBlockDamageHook(methodNode.instructions);
//				return methodNode;
//			}
//		},

		// Hooks multiple parts of the chunk rendering method to allow us to do our own custom rendering
		"ChunkRender#rebuildChunk": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender$RebuildTask",
				"methodName": "func_228940_a_", // "compile"
				"methodDesc": "(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Ljava/util/Set;"
			},
			"transformer": function(methodNode) {
				var instructions = methodNode.instructions;
				var isOptiFinePresent = detectOptiFine(instructions);

				// Inject the hook where we do our rendering
				// We inject right above where vanilla loops (iterates) through all the the blocks
				{
					var positionsIteratorCall = ASMAPI.findFirstMethodCall(
						methodNode,
						ASMAPI.MethodType.STATIC,
						isOptiFinePresent ? "net/optifine/BlockPosM" : "net/minecraft/util/math/BlockPos",
						isOptiFinePresent ? "getAllInBoxMutable" : ASMAPI.mapMethod("func_218278_a"), // BlockPos#betweenClosed
						"(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;"
					);
					if (!positionsIteratorCall)
						throw "Error: Couldn't find 'positionsIteratorCall' in " + stringifyInstructions(instructions);
					var firstLabelBeforePositionsIteratorCall = findFirstLabelBefore(instructions, instructions.indexOf(positionsIteratorCall));

					var outerClassFieldName = ASMAPI.mapField("field_228939_e_");
					instructions.insert(firstLabelBeforePositionsIteratorCall, ASMAPI.listOf(
						new VarInsnNode(ALOAD, 0), // this
						new VarInsnNode(ALOAD, 0), // ChunkRender.this
						new FieldInsnNode(GETFIELD, "net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender$RebuildTask", outerClassFieldName, "Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender;"),
						new VarInsnNode(ALOAD, 4), // compiledChunkIn
						new VarInsnNode(ALOAD, 5), // builderIn
						new VarInsnNode(ALOAD, 7), // blockpos - startPosition
						new VarInsnNode(ALOAD, isOptiFinePresent ? 12 : 11), // chunkrendercache
						new VarInsnNode(ALOAD, isOptiFinePresent ? 11 : 12), // matrixstack
						new VarInsnNode(ALOAD, isOptiFinePresent ? 16 : 13), // random
						new VarInsnNode(ALOAD, isOptiFinePresent ? 17 : 14), // blockrendererdispatcher
						callNoCubesHook("preIteration", "(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender$RebuildTask;Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender;Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockDisplayReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Ljava/util/Random;Lnet/minecraft/client/renderer/BlockRendererDispatcher;)V"),
						new LabelNode() // Label for original instructions
					));
				}
				print("Done injecting the preIteration hook");

				// Inject the hook where we cancel vanilla's block rendering for smoothable blocks
				{
					// The code that we are trying to inject looks like this:
					//	// NoCubes Start
                    //	if (io.github.cadiboo.nocubes.hooks.Hooks.canBlockStateRender(blockstate)))
                    //	// NoCubes End
                    //	if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && iblockstate.canRenderInLayer(blockrenderlayer1)) {

					var getRenderTypeName = ASMAPI.mapMethod("func_185901_i"); // getRenderType
					var getRenderTypeCall = ASMAPI.findFirstMethodCall(
						methodNode,
						ASMAPI.MethodType.VIRTUAL,
						"net/minecraft/block/BlockState",
						getRenderTypeName,
						"()Lnet/minecraft/block/BlockRenderType;"
					);
					if (!getRenderTypeCall)
						throw "Error: Couldn't find 'getRenderTypeCall' in " + stringifyInstructions(instructions);
					var getRenderTypeCallIndex = instructions.indexOf(getRenderTypeCall);
					var firstLabelBeforeGetRenderTypeCall = findFirstLabelBefore(instructions, getRenderTypeCallIndex);
					var branchIfBlockIsInvisibleInstruction = ASMAPI.findFirstInstructionAfter(methodNode, IF_ACMPEQ, getRenderTypeCallIndex);
					if (!branchIfBlockIsInvisibleInstruction)
						throw "Error: Couldn't find 'branchIfBlockIsInvisible' instruction in " + stringifyInstructions(instructions);
					var labelToJumpToIfBlockIsInvisible = branchIfBlockIsInvisibleInstruction.label

					instructions.insert(firstLabelBeforeGetRenderTypeCall, ASMAPI.listOf(
						new VarInsnNode(ALOAD, isOptiFinePresent ? 20 : 17), // blockstate
						callNoCubesHook("canBlockStateRender", "(Lnet/minecraft/block/BlockState;)Z"),
                    	new JumpInsnNode(IFEQ, labelToJumpToIfBlockIsInvisible),
						new LabelNode() // Label for original instructions
					));
				}
				print("Done injecting the canBlockStateRender hook");

//				injectFluidRenderBypass(instructions);
				return methodNode;
			}
		},

		// Hooks the function that gets called when a block is updated and marked for re-render
		// We need to extend the range of blocks that get marked for re-render (from vanilla's 1 to 2)
		// This fixes seams that appear when meshes along chunk borders change
		"ClientWorld#setBlocksDirty": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.world.ClientWorld",
				"methodName": "func_225319_b",
				"methodDesc": "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V"
			},
			"transformer": function(methodNode) {
				// Redirect execution to our hook
				var minecraft_name = ASMAPI.mapField("field_73037_M"); // mc, minecraft
				var levelRenderer_name = ASMAPI.mapField("field_217430_d"); // worldRenderer, levelRenderer
				injectAfterFirstLabel(methodNode.instructions, ASMAPI.listOf(
					new VarInsnNode(ALOAD, 0), // this
					new FieldInsnNode(GETFIELD, "net/minecraft/client/world/ClientWorld", minecraft_name, "Lnet/minecraft/client/Minecraft;"),
					new VarInsnNode(ALOAD, 0), // this
					new FieldInsnNode(GETFIELD, "net/minecraft/client/world/ClientWorld", levelRenderer_name, "Lnet/minecraft/client/renderer/WorldRenderer;"),
					new VarInsnNode(ALOAD, 1), // pos
					new VarInsnNode(ALOAD, 2), // oldState
					new VarInsnNode(ALOAD, 3), // newState
					callNoCubesHook("markForRerender", "(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V"),
					new InsnNode(RETURN),
					new LabelNode() // Label for original instructions
				));
				return methodNode;
			}
		},

//		"IWorldReader#getCollisionShapes": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.world.IWorldReader",
//				"methodName": "func_223438_b",
//				"methodDesc": "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/stream/Stream;"
//			},
//			"transformer": function(methodNode) {
//				injectGetCollisionShapesHook(methodNode.instructions);
//				return methodNode;
//			}
//		},
//		"World#getFluidState": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.world.World",
//				"methodName": "func_204610_c",
//				"methodDesc": "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;"
//			},
//			"transformer": function(methodNode) {
//				injectGetFluidStateHook(methodNode.instructions);
//				return methodNode;
//			}
//		},
//		"BlockState#isSolid": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.block.AbstractBlock$AbstractBlockState",
//				"methodName": "func_200132_m",
//				"methodDesc": "()Z"
//			},
//			"transformer": function(methodNode) {
//				injectIsSolidHook(methodNode.instructions);
//				return methodNode;
//			}
//		},
		"BlockState#getCollisionShape(NoContext)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.block.AbstractBlock$AbstractBlockState",
				"methodName": "func_196952_d",
				"methodDesc": "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/shapes/VoxelShape;"
			},
			"transformer": function(methodNode) {
				// Redirect execution to our hook
				injectAfterFirstLabel(methodNode.instructions, ASMAPI.listOf(
					new VarInsnNode(ALOAD, 0), // this
					new VarInsnNode(ALOAD, 1), // reader
					new VarInsnNode(ALOAD, 2), // pos
					callNoCubesHook("getCollisionShape", "(Lnet/minecraft/block/AbstractBlock$AbstractBlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/shapes/VoxelShape;"),
					new InsnNode(ARETURN),
					new LabelNode() // Label for original instructions
				));
				return methodNode;
			}
		},
		"BlockState#getCollisionShape(WithContext)": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.block.AbstractBlock$AbstractBlockState",
				"methodName": "func_215685_b",
				"methodDesc": "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;"
			},
			"transformer": function(methodNode) {
				// Redirect execution to our hook
				injectAfterFirstLabel(methodNode.instructions, ASMAPI.listOf(
					new VarInsnNode(ALOAD, 0), // this
					new VarInsnNode(ALOAD, 1), // reader
					new VarInsnNode(ALOAD, 2), // pos
					new VarInsnNode(ALOAD, 3), // context
					callNoCubesHook("getCollisionShape", "(Lnet/minecraft/block/AbstractBlock$AbstractBlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/shapes/ISelectionContext;)Lnet/minecraft/util/math/shapes/VoxelShape;"),
					new InsnNode(ARETURN),
					new LabelNode() // Label for original instructions
				));
				return methodNode;
			}
		},
		"BlockState#isCollisionShapeFullBlock": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.block.AbstractBlock$AbstractBlockState",
				"methodName": "func_235785_r_",
				"methodDesc": "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"
			},
			"transformer": function(methodNode) {
				// Redirect execution to our hook
				injectAfterFirstLabel(methodNode.instructions, ASMAPI.listOf(
					new VarInsnNode(ALOAD, 0), // this
					new VarInsnNode(ALOAD, 1), // reader
					new VarInsnNode(ALOAD, 2), // pos
					callNoCubesHook("isCollisionShapeFullBlock", "(Lnet/minecraft/block/AbstractBlock$AbstractBlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"),
					new InsnNode(IRETURN),
					new LabelNode() // Label for original instructions
				));
				return methodNode;
			}
		},
//		"BlockState#isCollisionShapeLargerThanFullBlock": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.block.AbstractBlock$AbstractBlockState",
//				"methodName": "func_215704_f",
//				"methodDesc": "()Z"
//			},
//			"transformer": function(methodNode) {
//				// TODO: Do I even need this?
//				var instructions = methodNode.instructions;
//				var firstIRETURN;
//            	var arrayLength = instructions.size();
//            	for (var i = 0; i < arrayLength; ++i) {
//            		var instruction = instructions.get(i);
//            		if (instruction.getOpcode() == IRETURN) {
//            			firstIRETURN = instruction;
//            			print("Found injection point \"first IRETURN\" " + instruction);
//            			break;
//            		}
//            	}
//            	if (!firstIRETURN) {
//            		throw "Error: Couldn't find injection point \"first IRETURN\"!";
//            	}
//
//            	var toInject = new InsnList();
//
//            	// Make list of instructions to inject
//            	toInject.add(new InsnNode(Opcodes.DUP)); // ret
//            	toInject.add(new VarInsnNode(ALOAD, 0)); // this
//				toInject.add(new MethodInsnNode(
//						//int opcode
//						INVOKESTATIC,
//						//String owner
//						"io/github/cadiboo/nocubes/hooks/Hooks",
//						//String name
//						"isCollisionShapeLargerThanFullBlock",
//						//String descriptor
//						"(ZLnet/minecraft/block/AbstractBlock$AbstractBlockState;)Z",
//						//boolean isInterface
//						false
//				));
//
//            	// Inject instructions
//            	instructions.insertBefore(firstIRETURN, toInject);
//				return methodNode;
//			}
//		},
//		"BlockState#causesSuffocation": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.block.BlockState",
//				"methodName": "func_215696_m",
//				"methodDesc": "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"
//			},
//			"transformer": function(methodNode) {
//				injectCausesSuffocationHook(methodNode.instructions);
//				return methodNode;
//			}
//		},
		"BlockState": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.block.BlockState"
			},
			"transformer": function(classNode) {
				var fields = classNode.fields;
				// Params: int access, String name, String descriptor, String signature, Object value
				fields.add(new FieldNode(ACC_PUBLIC, "nocubes_isTerrainSmoothable", "Z", null, false));
//				fields.add(new FieldNode(ACC_PUBLIC, "nocubes_isLeavesSmoothable", "Z", null, false));
				return classNode;
			}
		}
//		,
//		"VoxelShapes#getAllowedOffset": {
//			"target": {
//				"type": "METHOD",
//				"class": "net.minecraft.util.math.shapes.VoxelShapes",
//				"methodName": "func_216386_a",
//				"methodDesc": "(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/world/IWorldReader;DLnet/minecraft/util/math/shapes/ISelectionContext;Lnet/minecraft/util/AxisRotation;Ljava/util/stream/Stream;)D"
//			},
//			"transformer": function(methodNode) {
//				injectGetAllowedOffsetHook(methodNode.instructions);
//				return methodNode;
//			}
//		}
	}));
}

// 1) Find first INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
// 2) Find first label before
// 3) inject right after label
function injectInitChunkRenderCacheHook(instructions) {
//	this.cacheStartPos = start;
//	// NoCubes Start
//	io.github.cadiboo.nocubes.hooks.Hooks.initChunkRenderCache(this, chunkStartX, chunkStartZ, chunks, start, end);
//	// NoCubes End

	var getXCall = ASMAPI.findFirstMethodCall(
		methodNode,
		ASMAPI.MethodType.VIRTUAL,
		"net/minecraft/util/math/BlockPos",
		ASMAPI.mapMethod("func_177958_n"), // Vec3i.getX
		"()I"
	);
	if (!getXCall)
		throw "Error: Couldn't find 'getXCall' in " + stringifyInstructions(instructions);

	var firstLabelBeforeGetXCall = findFirstLabelBefore(instructions, instructions.indexOf(getXCall));
	instructions.insert(firstLabelBeforeGetXCall, ASMAPI.listOf(
		new VarInsnNode(ALOAD, 0), // this
		new VarInsnNode(ILOAD, 2), // chunkStartX
		new VarInsnNode(ILOAD, 3), // chunkStartZ
		new VarInsnNode(ALOAD, 4), // chunks
		new VarInsnNode(ALOAD, 5), // start
		new VarInsnNode(ALOAD, 6), // end
		callNoCubesHook("initChunkRenderCache", "(Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V"),
		new InsnNode(RETURN),
		new LabelNode() // Label for original instructions
	));
}

// 1) Find last ACONST_NULL then ARETURN
// 2) Find previous IFEQ
// 3) Find previous label
// 4) Find previous INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.isEmptyBetween
// 5) Find next ISTORE
// 6) Inject GOTO to label after ISTORE
function injectGenerateCacheHook(instructions) {
//	// NoCubes Start
//	IS_EMPTY:
//	// NoCubes End
//	for (int x = start.getX() >> 4; x <= end.getX() >> 4; ++x) {
//		for (int z = start.getZ() >> 4; z <= end.getZ() >> 4; ++z) {
//			Chunk chunk = chunks[x - chunkStartX][z - chunkStartZ];
//			if (!chunk.isEmptyBetween(start.getY(), end.getY())) {
//				empty = false;
//				// NoCubes Start
//				break IS_EMPTY;
//				// NoCubes End
//			}
//		}
//	}

	// 1) Find last ACONST_NULL then ARETURN
	var firstACONST_NULL_then_ARETURN;
	var previousInsn; // The previous insn that was checked (technically the next insn in the list)
	var length = instructions.size();
	for (var i = length - 1; i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == ACONST_NULL) {
			if (!previousInsn)
				continue;
			if (previousInsn.getOpcode() == ARETURN) {
				firstACONST_NULL_then_ARETURN = instruction;
				print("Found ACONST_NULL & ARETURN");
				break;
			}
		}
		previousInsn = instruction;
	}
	if (!firstACONST_NULL_then_ARETURN)
		throw "Error: Couldn't find ACONST_NULL & ARETURN in " + stringifyInstructions(instructions);

	// 2) Find previous IFEQ
	var firstIFEQBefore_firstACONST_NULL_then_ARETURN;
	for (i = instructions.indexOf(firstACONST_NULL_then_ARETURN); i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == IFEQ) {
			firstIFEQBefore_firstACONST_NULL_then_ARETURN = instruction;
			print("Found IFEQ");
			break;
		}
	}
	if (!firstIFEQBefore_firstACONST_NULL_then_ARETURN)
		throw "Error: Couldn't find IFEQ in " + stringifyInstructions(instructions);

	// 3) Find previous Label
	var previousLabel = findFirstLabelBefore(instructions, instructions.indexOf(firstIFEQBefore_firstACONST_NULL_then_ARETURN));

	// 4) Find previous INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.isEmptyBetween
	var isEmptyBetweenCall = ASMAPI.findFirstMethodCallBefore(
		methodNode,
		ASMAPI.MethodType.VIRTUAL,
		"net/minecraft/world/chunk/Chunk",
		ASMAPI.mapMethod("func_76606_c"), // isEmptyBetween
		"(II)Z"
	);
	if (!isEmptyBetweenCall)
		throw "Error: Couldn't find 'isEmptyBetween' in " + stringifyInstructions(instructions);

	// 5) Find next ISTORE
	var nextISTORE = ASMAPI.findFirstInstructionAfter(methodNode, ISTORE, instructions.indexOf(isEmptyBetweenCall));
	if (!firstIFEQBefore_firstACONST_NULL_then_ARETURN)
		throw "Error: Couldn't find next 'ISTORE' in " + stringifyInstructions(instructions);

	instructions.insert(nextISTORE, ASMAPI.listOf(
		new JumpInsnNode(GOTO, previousLabel)
	));
}

// 1) Find first label
// 2) Insert right after label
function injectRenderBlockDamageHook(instructions) {
//	public void renderBlockDamage(BlockState state, BlockPos pos, TextureAtlasSprite sprite, IEnviromentBlockReader reader) {
//		// NoCubes Start
//		if(io.github.cadiboo.nocubes.hooks.Hooks.renderBlockDamage(this, state, pos, sprite, reader)){
//			return;
//		}
//		// NoCubes End
//		if (state.getRenderType() == BlockRenderType.MODEL) {

	var originalInstructionsLabel = new LabelNode();
	injectAfterFirstLabel(instructions, ASMAPI.listOf(
		new VarInsnNode(ALOAD, 0), // this
		new VarInsnNode(ALOAD, 1), // state
		new VarInsnNode(ALOAD, 2), // pos
		new VarInsnNode(ALOAD, 3), // lightReaderIn
		new VarInsnNode(ALOAD, 4), // matrixStackIn
		new VarInsnNode(ALOAD, 5), // vertexBuilderIn
		new VarInsnNode(ALOAD, 6), // modelData
		callNoCubesHook("renderBlockDamage", "(Lnet/minecraft/client/renderer/BlockRendererDispatcher;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockDisplayReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Lcom/mojang/blaze3d/vertex/IVertexBuilder;Lnet/minecraftforge/client/model/data/IModelData;)Z"),
		new JumpInsnNode(IFEQ, originalInstructionsLabel),
		new InsnNode(RETURN),
		originalInstructionsLabel
	));
}

// 1) Finds the first instruction INVOKEVIRTUAL ChunkRenderCache.getFluidState
// 2) Then injects
// 3) Then removes the two previous instructions and then the instruction
function injectFluidRenderBypass(instructions) {
// Forge/Vanilla/Patched
//	// NoCubes Start
////	IFluidState ifluidstate = lvt_12_1_.getFluidState(blockpos2);
//	IFluidState ifluidstate = net.minecraft.fluid.Fluids.EMPTY.getDefaultState();
//	// NoCubes End
//	net.minecraftforge.client.model.data.IModelData modelData = generator.getModelData(blockpos2);

// Forge/OptiFine/Patched
//	// NoCubes Start
////	IFluidState ifluidstate = blockstate.getFluidState();
//	IFluidState ifluidstate = net.minecraft.fluid.Fluids.EMPTY.getDefaultState();
//	// NoCubes End
//		if (!ifluidstate.isEmpty()) {

	var isOptiFinePresent = detectOptiFine(instructions);
	var getFluidStateCall = ASMAPI.findFirstMethodCall(
		methodNode,
		ASMAPI.MethodType.VIRTUAL,
		isOptiFinePresent ? "net/minecraft/block/BlockState" : "net/minecraft/client/renderer/chunk/ChunkRenderCache",
		// isOptiFinePresent ? BlockState#getFluidState : ChunkRenderCache#getFluidState
		isOptiFinePresent ? ASMAPI.mapMethod("func_204520_s") : ASMAPI.mapMethod("func_204610_c"),
		isOptiFinePresent ? "()Lnet/minecraft/fluid/IFluidState;" : "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;"
	);
	if (!getFluidStateCall)
		throw "Error: Couldn't find 'getFluidState' call in " + stringifyInstructions(instructions);

	var Fluids_EMPTY_name = ASMAPI.mapField("field_204541_a"); // Fluids.EMPTY
	var Fluid_getDefaultState_name = ASMAPI.mapMethod("func_207188_f"); // Fluid#getDefaultState
	instructions.insert(first_INVOKEVIRTUAL_getFluidState, ASMAPI.listOf(
		new FieldInsnNode(GETSTATIC, "net/minecraft/fluid/Fluids", Fluids_EMPTY_name, "Lnet/minecraft/fluid/Fluid;"),
		new MethodInsnNode(
			//int opcode
			INVOKEVIRTUAL,
			//String owner
			"net/minecraft/fluid/Fluid",
			//String name
			Fluid_getDefaultState_name,
			//String descriptor
			"()Lnet/minecraft/fluid/IFluidState;",
			//boolean isInterface
			false
		)
	));

	// Remove old instructions
	if (!isOptiFinePresent) {
		// ALOAD 12 (ChunkRenderCache)
		instructions.remove(first_INVOKEVIRTUAL_getFluidState.getPrevious().getPrevious());
		// ALOAD 17 (BlockPos)
		instructions.remove(first_INVOKEVIRTUAL_getFluidState.getPrevious());
		// INVOKEVIRTUAL ChunkRenderCache#getFluidState
		instructions.remove(first_INVOKEVIRTUAL_getFluidState);
	} else {
		// ALOAD 20 (BlockState)
		instructions.remove(first_INVOKEVIRTUAL_getFluidState.getPrevious());
		// INVOKEVIRTUAL BlockState#getFluidState
		instructions.remove(first_INVOKEVIRTUAL_getFluidState);
	}
}

// 1) Finds the first instruction NEW CubeCoordinateIterator
// 2) Finds the previous label
// 3) Inserts after that label and before the label's instructions.
function injectGetCollisionShapesHook(instructions) {

//	final ISelectionContext iselectioncontext = entityIn == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(entityIn);
//	final CubeCoordinateIterator cubecoordinateiterator = new CubeCoordinateIterator(i, k, i1, j, l, j1);

//	final ISelectionContext iselectioncontext = entityIn == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(entityIn);
//	// NoCubes Start
//	return io.github.cadiboo.nocubes.hooks.Hooks.getCollisionShapes(this, entityIn, aabb, i, j, k, l, i1, j1, iselectioncontext);
//	// NoCubes End


//    ALOAD 1
//    INVOKESTATIC net/minecraft/util/math/shapes/ISelectionContext.forEntity (Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/shapes/ISelectionContext; (itf)
//   L8
//   FRAME SAME1 net/minecraft/util/math/shapes/ISelectionContext
//    ASTORE 9
//   L9
//    LINENUMBER 146 L9
//    NEW net/minecraft/util/math/CubeCoordinateIterator
//    DUP
//    ILOAD 3
//    ILOAD 5
//    ILOAD 7
//    ILOAD 4
//    ILOAD 6
//    ILOAD 8
//    INVOKESPECIAL net/minecraft/util/math/CubeCoordinateIterator.<init> (IIIIII)V
//    ASTORE 10

//    ALOAD 1
//    INVOKESTATIC net/minecraft/util/math/shapes/ISelectionContext.forEntity (Lnet/minecraft/entity/Entity;)Lnet/minecraft/util/math/shapes/ISelectionContext; (itf)
//   L8
//   FRAME SAME1 net/minecraft/util/math/shapes/ISelectionContext
//    ASTORE 9
//   L9
//    LINENUMBER 147 L9
//    ALOAD 0
//    ALOAD 1
//    ALOAD 2
//    ILOAD 3
//    ILOAD 4
//    ILOAD 5
//    ILOAD 6
//    ILOAD 7
//    ILOAD 8
//    ALOAD 9
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.getCollisionShapes (Lnet/minecraft/world/IWorldReader;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;IIIIIILnet/minecraft/util/math/shapes/ISelectionContext;)Ljava/util/stream/Stream;
//    ARETURN


	var first_NEW_CubeCoordinateIterator;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == NEW) {
			if (instruction.desc == "net/minecraft/util/math/CubeCoordinateIterator") {
				first_NEW_CubeCoordinateIterator = instruction;
				print("Found injection point \"first_NEW_CubeCoordinateIterator\" " + instruction);
				break;
			}
		}
	}
	if (!first_NEW_CubeCoordinateIterator) {
		throw "Error: Couldn't find injection point \"first_NEW_CubeCoordinateIterator\"!";
	}

	var firstLabelBefore_first_NEW_CubeCoordinateIterator;
	for (i = instructions.indexOf(first_NEW_CubeCoordinateIterator); i > 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			firstLabelBefore_first_NEW_CubeCoordinateIterator = instruction;
			print("Found label \"next Label\" " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_first_NEW_CubeCoordinateIterator) {
		throw "Error: Couldn't find label \"next Label\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new VarInsnNode(ALOAD, 1)); // entity
	toInject.add(new VarInsnNode(ALOAD, 2)); // aabb
	toInject.add(new VarInsnNode(ILOAD, 3)); // i minXm1
	toInject.add(new VarInsnNode(ILOAD, 4)); // j maxXp1
	toInject.add(new VarInsnNode(ILOAD, 5)); // k minYm1
	toInject.add(new VarInsnNode(ILOAD, 6)); // l maxYp1
	toInject.add(new VarInsnNode(ILOAD, 7)); // i1 minZm1
	toInject.add(new VarInsnNode(ILOAD, 8)); // j1 maxZp1
	toInject.add(new VarInsnNode(ALOAD, 9)); // iselectioncontext
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"getCollisionShapes",
			//String descriptor
			"(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;IIIIIILnet/minecraft/util/math/shapes/ISelectionContext;)Ljava/util/stream/Stream;",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(ARETURN));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insertBefore(firstLabelBefore_first_NEW_CubeCoordinateIterator, toInject);

}

// 1) Finds the first instruction INVOKEVIRTUAL World.getChunk
// 2) Finds the next instruction ARETURN
// 3) Inserts before World.getChunk
// 4) Removes everything between World.getChunk and ARETURN
function injectGetFluidStateHook(instructions) {

//	if (isOutsideBuildHeight(pos)) {
//		return Fluids.EMPTY.getDefaultState();
//	} else {
//		Chunk chunk = this.getChunk(pos);
//		return chunk.getFluidState(pos);
//	}

//	if (isOutsideBuildHeight(pos)) {
//		return Fluids.EMPTY.getDefaultState();
//	} else {
//		// NoCubes Start
//		return io.github.cadiboo.nocubes.hooks.Hooks.getFluidState(this, pos);
//		//Chunk chunk = this.getChunk(pos);
//		//return chunk.getFluidState(pos);
//		// NoCubes End
//	}


//  public getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;
//   L0
//    LINENUMBER 553 L0
//    ALOAD 1
//    INVOKESTATIC net/minecraft/world/World.isOutsideBuildHeight (Lnet/minecraft/util/math/BlockPos;)Z
//    IFEQ L1
//   L2
//    LINENUMBER 554 L2
//    GETSTATIC net/minecraft/init/Fluids.EMPTY : Lnet/minecraft/fluid/Fluid;
//    INVOKEVIRTUAL net/minecraft/fluid/Fluid.getDefaultState ()Lnet/minecraft/fluid/IFluidState;
//    ARETURN
//   L1
//    LINENUMBER 556 L1
//   FRAME SAME
//    ALOAD 0
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/world/World.getChunk (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/Chunk;
//    ASTORE 2
//   L3
//    LINENUMBER 557 L3
//    ALOAD 2
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.getFluidState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;
//    ARETURN

//  public getFluidState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;
//   L0
//    LINENUMBER 640 L0
//    ALOAD 1
//    INVOKESTATIC net/minecraft/world/World.isOutsideBuildHeight (Lnet/minecraft/util/math/BlockPos;)Z
//    IFEQ L1
//   L2
//    LINENUMBER 641 L2
//    GETSTATIC net/minecraft/init/Fluids.EMPTY : Lnet/minecraft/fluid/Fluid;
//    INVOKEVIRTUAL net/minecraft/fluid/Fluid.getDefaultState ()Lnet/minecraft/fluid/IFluidState;
//    ARETURN
//   L1
//    LINENUMBER 644 L1
//   FRAME SAME
//    ALOAD 0
//    ALOAD 1
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.getFluidState (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;
//    ARETURN


	var getChunkAt_name = ASMAPI.mapMethod("func_175726_f"); // getChunkAt

	var first_INVOKEVIRTUAL_World_getChunkAt;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/world/World") {
				if (instruction.name == getChunkAt_name) {
					if (instruction.desc == "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/Chunk;") {
						if (instruction.itf == false) {
							first_INVOKEVIRTUAL_World_getChunkAt = instruction;
							print("Found injection point \"first getChunkAt\" " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!first_INVOKEVIRTUAL_World_getChunkAt) {
		throw "Error: Couldn't find injection point \"first getChunkAt\"!";
	}

	var next_ARETURN;
//	var arrayLength = instructions.size();
	for (var i = instructions.indexOf(first_INVOKEVIRTUAL_World_getChunkAt); i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == ARETURN) {
			next_ARETURN = instruction;
			print("Found injection point \"next ARETURN\" " + instruction);
			break;
		}
	}
	if (!next_ARETURN) {
		throw "Error: Couldn't find injection point \"next ARETURN\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff

	// Make list of instructions to inject
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"getFluidState",
			//String descriptor
			"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(ARETURN));

	// Inject instructions
	instructions.insertBefore(first_INVOKEVIRTUAL_World_getChunkAt, toInject);

	removeBetweenInclusive(instructions, first_INVOKEVIRTUAL_World_getChunkAt, next_ARETURN);

}

// TODO: Check this, might want to use this for lighting.
// 1) Find first label
// 2) inject right after first label
function injectIsSolidHook(instructions) {

//	return this.isSolid;

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.config.NoCubesConfig.Client.render && io.github.cadiboo.nocubes.NoCubes.smoothableHandler.isSmoothable((BlockState) this))
//		return false;
//	// NoCubes End
//	return this.isSolid;

////	// NoCubes Start
////	if (io.github.cadiboo.nocubes.config.Config.renderSmoothTerrain && this.nocubes_isTerrainSmoothable) return false;
////	if (io.github.cadiboo.nocubes.config.Config.renderSmoothLeaves && this.nocubes_isLeavesSmoothable) return false;
////	// NoCubes End
////	return this.isSolid;


//  public isSolid()Z
////   L0
////    LINENUMBER 212 L0
////    ALOAD 0
////    INVOKEVIRTUAL net/minecraft/block/BlockState.getBlock ()Lnet/minecraft/block/Block;
////    ALOAD 0
////    INVOKEVIRTUAL net/minecraft/block/Block.isSolid (Lnet/minecraft/block/BlockState;)Z
////    IRETURN

//  public isSolid()Z
//   L0
//    LINENUMBER 601 L0
//    GETSTATIC io/github/cadiboo/nocubes/config/NoCubesConfig$Client.render : Z
//    IFEQ L1
//    GETSTATIC io/github/cadiboo/nocubes/NoCubes.smoothableHandler : Lio/github/cadiboo/nocubes/smoothable/SmoothableHandler;
//    ALOAD 0
//    CHECKCAST net/minecraft/block/BlockState
//    INVOKEINTERFACE io/github/cadiboo/nocubes/smoothable/SmoothableHandler.isSmoothable (Lnet/minecraft/block/BlockState;)Z (itf)
//    IFEQ L1
//   L2
//    LINENUMBER 602 L2
//    ICONST_0
//    IRETURN
//   L1
//    LINENUMBER 603 L1
//   FRAME SAME
//    ALOAD 0
//    GETFIELD net/minecraft/block/AbstractBlock$AbstractBlockState.isSolid : Z
//    IRETURN

////  public default isSolid()Z
////   L0
////    LINENUMBER 213 L0
////    GETSTATIC io/github/cadiboo/nocubes/config/Config.renderSmoothTerrain : Z
////    IFEQ L1
////    ALOAD 0
////    GETFIELD net/minecraft/block/BlockState.nocubes_isTerrainSmoothable : Z
////    IFEQ L1
////    ICONST_0
////    IRETURN
////   L1
////    LINENUMBER 214 L1
////   FRAME SAME
////    GETSTATIC io/github/cadiboo/nocubes/config/Config.renderSmoothLeaves : Z
////    IFEQ L2
////    ALOAD 0
////    GETFIELD net/minecraft/block/BlockState.nocubes_isLeavesSmoothable : Z
////    IFEQ L2
////    ICONST_0
////    IRETURN
////   L2
////    LINENUMBER 213 L2
////   FRAME SAME
////    ALOAD 0
////    INVOKEVIRTUAL net/minecraft/block/BlockState.getBlock ()Lnet/minecraft/block/Block;
////    ALOAD 0
////    INVOKEVIRTUAL net/minecraft/block/Block.isSolid (Lnet/minecraft/block/BlockState;)Z
////    IRETURN


	var firstLabel;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			firstLabel = instruction;
			print("Found injection point \"first Label\" " + instruction);
			break;
		}
	}
	if (!firstLabel) {
		throw "Error: Couldn't find injection point \"first Label\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/config/NoCubesConfig$Client", "render", "Z"));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
	toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/NoCubes", "smoothableHandler", "Lio/github/cadiboo/nocubes/smoothable/SmoothableHandler;"));
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new TypeInsnNode(CHECKCAST, "net/minecraft/block/BlockState"));
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEINTERFACE,
			//String owner
			"io/github/cadiboo/nocubes/smoothable/SmoothableHandler",
			//String name
			"isSmoothable",
			//String descriptor
			"(Lnet/minecraft/block/BlockState;)Z",
			//boolean isInterface
			true
	));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
	toInject.add(new InsnNode(ICONST_0));
	toInject.add(new InsnNode(IRETURN));

//	// Labels n stuff
//	var originalInstructionsLabel = new LabelNode();
//	var leavesChecksLabel = new LabelNode();
//
//	// Make list of instructions to inject
//	toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothTerrain", "Z"));
//	toInject.add(new JumpInsnNode(IFEQ, leavesChecksLabel));
//	toInject.add(new VarInsnNode(ALOAD, 0)); // this
//	toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/BlockState", "nocubes_isTerrainSmoothable", "Z"));
//	toInject.add(new JumpInsnNode(IFEQ, leavesChecksLabel));
//	toInject.add(new InsnNode(ICONST_0));
//	toInject.add(new InsnNode(IRETURN));
//	toInject.add(leavesChecksLabel);
//	toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothLeaves", "Z"));
//	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
//	toInject.add(new VarInsnNode(ALOAD, 0)); // this
//	toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/BlockState", "nocubes_isLeavesSmoothable", "Z"));
//	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
//	toInject.add(new InsnNode(ICONST_0));
//	toInject.add(new InsnNode(IRETURN));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insert(firstLabel, toInject);

}

// 1) Find first label
// 2) inject right after first label
function injectCausesSuffocationHook(instructions) {

//	return this.getBlock().causesSuffocation(this, worldIn, pos);

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.hooks.Hooks.doesNotCauseSuffocation(this, worldIn, pos)) return false;
//	// NoCubes End
//	return this.getBlock().causesSuffocation(this, worldIn, pos);


//  public default causesSuffocation(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z
//   L0
//    LINENUMBER 321 L0
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getBlock ()Lnet/minecraft/block/Block;
//    ALOAD 0
//    ALOAD 1
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/block/Block.causesSuffocation (Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z
//    IRETURN

//  public causesSuffocation(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z
//   L0
//    LINENUMBER 325 L0
//    ALOAD 0
//    ALOAD 1
//    ALOAD 2
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.doesNotCauseSuffocation (Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z
//    IFEQ L1
//    ICONST_0
//    IRETURN
//   L1
//    LINENUMBER 327 L1
//   FRAME SAME
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getBlock ()Lnet/minecraft/block/Block;
//    ALOAD 0
//    ALOAD 1
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/block/Block.causesSuffocation (Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z
//    IRETURN


	var firstLabel;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			firstLabel = instruction;
			print("Found injection point \"first Label\" " + instruction);
			break;
		}
	}
	if (!firstLabel) {
		throw "Error: Couldn't find injection point \"first Label\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new VarInsnNode(ALOAD, 1)); // reader
	toInject.add(new VarInsnNode(ALOAD, 2)); // pos
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"doesNotCauseSuffocation",
			//String descriptor
			"(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z",
			//boolean isInterface
			false
	));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
	toInject.add(new InsnNode(ICONST_0));
	toInject.add(new InsnNode(IRETURN));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insert(firstLabel, toInject);

}

// 1) Finds the first instruction NEW World.getChunk
// 2) Finds the next instruction ARETURN
// 3) Inserts before World.getChunk
// 4) Removes everything between World.getChunk and ARETURN
function injectGetAllowedOffsetHook(instructions) {

//	Direction.Axis rotZ = rot.rotate(Direction.Axis.Z);
//	BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos();

//	Direction.Axis rotZ = rot.rotate(Direction.Axis.Z);
//	// NoCubes Start
//	return io.github.cadiboo.nocubes.hooks.Hooks.getAllowedOffset(collisionBox, worldReader, desiredOffset, selectionContext, rotationAxis, possibleHits, rot, rotX, rotY, rotZ);
//	// NoCubes End
////	BlockPos.MutableBlockPos mbp = new BlockPos.MutableBlockPos();


//   L7
//    LINENUMBER 197 L7
//    ALOAD 7
//    GETSTATIC net/minecraft/util/Direction$Axis.Z : Lnet/minecraft/util/Direction$Axis;
//    INVOKEVIRTUAL net/minecraft/util/AxisRotation.rotate (Lnet/minecraft/util/Direction$Axis;)Lnet/minecraft/util/Direction$Axis;
//    ASTORE 10
//   L8
//    LINENUMBER 198 L8
//    NEW net/minecraft/util/math/BlockPos$MutableBlockPos
//    DUP
//    INVOKESPECIAL net/minecraft/util/math/BlockPos$MutableBlockPos.<init> ()V
//    ASTORE 11
//   L9
//    LINENUMBER 199 L9
//    ALOAD 0
//    ALOAD 8
//    INVOKEVIRTUAL net/minecraft/util/math/AxisAlignedBB.getMin (Lnet/minecraft/util/Direction$Axis;)D
//    LDC 1.0E-7
//    DSUB
//    INVOKESTATIC net/minecraft/util/math/MathHelper.floor (D)I
//    ICONST_1
//    ISUB
//    ISTORE 12

//   L7
//    LINENUMBER 197 L7
//    ALOAD 7
//    GETSTATIC net/minecraft/util/Direction$Axis.Z : Lnet/minecraft/util/Direction$Axis;
//    INVOKEVIRTUAL net/minecraft/util/AxisRotation.rotate (Lnet/minecraft/util/Direction$Axis;)Lnet/minecraft/util/Direction$Axis;
//    ASTORE 10
//   L8
//    LINENUMBER 199 L8
//    ALOAD 0
//    ALOAD 1
//    DLOAD 2
//    ALOAD 4
//    ALOAD 5
//    ALOAD 6
//    ALOAD 7
//    ALOAD 8
//    ALOAD 9
//    ALOAD 10
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.getAllowedOffset (Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/world/IWorldReader;DLnet/minecraft/util/math/shapes/ISelectionContext;Lnet/minecraft/util/AxisRotation;Ljava/util/stream/Stream;Lnet/minecraft/util/AxisRotation;Lnet/minecraft/util/Direction$Axis;Lnet/minecraft/util/Direction$Axis;Lnet/minecraft/util/Direction$Axis;)D
//    DRETURN


	var first_NEW_MutableBlockPos;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == NEW) {
			if (instruction.desc == "net/minecraft/util/math/BlockPos$MutableBlockPos") {
				first_NEW_MutableBlockPos = instruction;
				print("Found injection point \"first_NEW_MutableBlockPos\" " + instruction);
				break;
			}
		}
	}
	if (!first_NEW_MutableBlockPos) {
		throw "Error: Couldn't find injection point \"first_NEW_MutableBlockPos\"!";
	}

	var firstLabelBefore_first_NEW_MutableBlockPos;
	for (i = instructions.indexOf(first_NEW_MutableBlockPos); i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			firstLabelBefore_first_NEW_MutableBlockPos = instruction;
			print("Found label \"firstLabelBefore_first_NEW_MutableBlockPos\" " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_first_NEW_MutableBlockPos) {
		throw "Error: Couldn't find label \"firstLabelBefore_first_NEW_MutableBlockPos\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(ALOAD, 0)); // collisionBox
	toInject.add(new VarInsnNode(ALOAD, 1)); // worldReader
	toInject.add(new VarInsnNode(DLOAD, 2)); // desiredOffset
	toInject.add(new VarInsnNode(ALOAD, 4)); // selectionContext
	toInject.add(new VarInsnNode(ALOAD, 5)); // rotationAxis
	toInject.add(new VarInsnNode(ALOAD, 6)); // possibleHits
	toInject.add(new VarInsnNode(ALOAD, 7)); // reversedRotation
	toInject.add(new VarInsnNode(ALOAD, 8)); // rotX
	toInject.add(new VarInsnNode(ALOAD, 9)); // rotY
	toInject.add(new VarInsnNode(ALOAD, 10)); // rotZ
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"getAllowedOffset",
			//String descriptor
			"(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/world/IWorldReader;DLnet/minecraft/util/math/shapes/ISelectionContext;Lnet/minecraft/util/AxisRotation;Ljava/util/stream/Stream;Lnet/minecraft/util/AxisRotation;Lnet/minecraft/util/Direction$Axis;Lnet/minecraft/util/Direction$Axis;Lnet/minecraft/util/Direction$Axis;)D",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(DRETURN));

	// Inject instructions
	instructions.insert(firstLabelBefore_first_NEW_MutableBlockPos, toInject);

}

function findFirstLabel(instructions, startIndex) {
	if (!startIndex)
		startIndex = 0;
	var length = instructions.size();
	var i;
	for (i = startIndex; i < length; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			print("Found first label after index " + startIndex + ": " + instruction);
			return instruction;
		}
	}
	throw "Error: Couldn't find first label after index " + startIndex + " in " + stringifyInstructions(instructions);
}

function findFirstLabelBefore(instructions, startIndex) {
	var length = instructions.size();
	if (!startIndex)
		startIndex = length - 1;
	var i;
	for (i = startIndex; i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			print("Found first label before index " + startIndex + ": " + instruction);
			return instruction;
		}
	}
	throw "Error: Couldn't find first label before index " + startIndex + " in " + stringifyInstructions(instructions);
}

/**
 * Utility function to create an INVOKESTATIC call to one of our hooks
 *
 * @param {string} name The name of the hook method
 * @param {string} desc The hook method's method descriptor
 * @return {object} The transformersObj with all transformers wrapped
 */
function callNoCubesHook(name, desc) {
	return new MethodInsnNode(
		//int opcode
		INVOKESTATIC,
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

function injectAfterFirstLabel(instructions, instructionsToInject) {
	var injectAfter = findFirstLabel(instructions);
	instructions.insert(injectAfter, instructionsToInject);
}

function detectOptiFine(instructions) {
	var length = instructions.size();
	var i;
	for (i = 0; i < length; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == METHOD_INSN) {
			var owner = instruction.owner;
			if (owner == "net/optifine/override/ChunkCacheOF" || owner == "net/optifine/BlockPosM")
				return true;
		}
	}
	return false;
}


















/**
 * Utility function to wrap all transformers in transformers that have logging
 *
 * @param {object} transformersObj All the transformers of this coremod
 * @return {object} The transformersObj with all transformers wrapped
 */
function wrapWithLogging(transformersObj) {
	var oldPrint = print;
	// Global variable because makeLoggingTransformerFunction is a separate function (thanks to scoping issues)
	currentPrintTransformer = null;
	print = function(msg) {
		if (currentPrintTransformer)
			msg = "[" + currentPrintTransformer + "]: " + msg;
		oldPrint("[NoCubes Transformer] " + msg);
	};

	for (var transformerObjName in transformersObj) {
		var transformerObj = transformersObj[transformerObjName];

		var transformer = transformerObj.transformer;
		if (!transformer)
			continue;

		transformerObj.transformer = makeLoggingTransformerFunction(transformerObjName, transformer);
	}
	return transformersObj;
}

/**
 * Utility function for making the wrapper transformer function with logging
 * Not part of {@link #wrapWithLogging) because of scoping issues (Nashhorn
 * doesn't support "let" which would fix the issues)
 *
 * @param {string} transformerObjName The name of the transformer
 * @param {transformer} transformer The transformer function
 * @return {function} A transformer that wraps the old transformer
 */
function makeLoggingTransformerFunction(transformerObjName, transformer) {
	return function(obj) {
		currentPrintTransformer = transformerObjName;
		print("Starting Transform");
		obj = transformer(obj);
		print("Finished Transform");
		currentPrintTransformer = null;
		return obj;
	};
}

/**
 * Utility function to wrap all method transformers in class transformers
 * to make them run after OptiFine's class transformers
 *
 * @param {object} transformersObj All the transformers of this coremod
 * @return {object} The transformersObj with all method transformers wrapped
 */
function wrapMethodTransformers(transformersObj) {
	for (var transformerObjName in transformersObj) {
		var transformerObj = transformersObj[transformerObjName];

		var target = transformerObj.target;
		if (!target)
			continue;

		var type = target.type;
		if (!type || !type.equals("METHOD"))
			continue;

		var clazz = target.class;
		if (!clazz)
			continue;

		var methodName = target.methodName;
		if (!methodName)
			continue;

		var mappedMethodName = ASMAPI.mapMethod(methodName);

		var methodDesc = target.methodDesc;
		if (!methodDesc)
			continue;

		var methodTransformer = transformerObj.transformer;
		if (!methodTransformer)
			continue;

		var newTransformerObjName = "(Method2ClassTransformerWrapper) " + transformerObjName;
		var newTransformerObj = {
			"target": {
				"type": "CLASS",
				"name": clazz,
			},
			"transformer": makeClass2MethodTransformerFunction(mappedMethodName, methodDesc, methodTransformer)
		};

		transformersObj[newTransformerObjName] = newTransformerObj;
		delete transformersObj[transformerObjName];
	}
	return transformersObj;
}

/**
 * Utility function for making the wrapper class transformer function
 * Not part of {@link #wrapMethodTransformers) because of scoping issues (Nashhorn
 * doesn't support "let" which would fix the issues)
 *
 * @param {string} mappedMethodName The (mapped) name of the target method
 * @param {string} methodDesc The description of the target method
 * @param {methodTransformer} transformer The method transformer function
 * @return {function} A class transformer that wraps the methodTransformer
 */
function makeClass2MethodTransformerFunction(mappedMethodName, methodDesc, methodTransformer) {
	return function(classNode) {
		var methods = classNode.methods;
		for (var i in methods) {
			var methodNode = methods[i];
			if (!methodNode.name.equals(mappedMethodName))
				continue;
			if (!methodNode.desc.equals(methodDesc))
				continue;
			methods[i] = methodTransformer(methodNode);
			return classNode;
		}
		var searchedMethods = "[";
		for (var i in methods) {
			var methodNode = methods[i];
			searchedMethods + "\"" + classNode.name + "." + methodNode.name + " " + methodNode.desc + "\"";
		}
		searchedMethods += "]";
		throw new Error("Method transformer did not find a method! Target method was \"" + classNode.name + "." + mappedMethodName + " " + methodDesc + "\". Searched " + searchedMethods + ".")
	};
}



















/**
 * Utility function for removing multiple instructions
 *
 * @param {InsnList} instructions The list of instructions to modify
 * @param {InsnList} startInstruction The first instruction of instructions to be removed
 * @param {InsnList} endInstruction The last instruction of instructions to be removed
 */
function removeBetweenInclusive(instructions, startInstruction, endInstruction) {
	var start = instructions.indexOf(startInstruction);
	var end = instructions.indexOf(endInstruction);
	for (var i = start; i <= end; ++i) {
		instructions.remove(instructions.get(start));
	}
}

/**
 * Util function to print a list of instructions for debugging
 *
 * @param {InsnList} instructions The list of instructions to print
 */
function printInstructions(instructions) {
	var arrayLength = instructions.size();
	var labelNames = {
		length: 0
	};
	for (var i = 0; i < arrayLength; ++i) {
		var text = getInstructionText(instructions.get(i), labelNames);
		if (text.length > 0) // Some instructions are ignored
			print(text);
	}
}

/**
 * Util function to stringify a list of instructions for debugging
 *
 * @param {InsnList} instructions The list of instructions to stringify
 * @returns {string} The stringified instructions, joined with newlines
 */
function stringifyInstructions(instructions) {
	var fullText = "";
	var labelNames = {
		length: 0
	};
	var arrayLength = instructions.size();
	var i;
	for (i = 0; i < arrayLength; ++i) {
		var text = getInstructionText(instructions.get(i), labelNames);
		if (text.length > 0) // Some instructions are ignored
			fullText += text + "\n";
	}
	return fullText;
}

/**
 * Util function to get the text for an instruction
 *getInstructionText
 * @param {AbstractInsnNode} instruction The instruction to generate text for
 * @param {Map<int, string>} labelNames The names of the labels in the format Map<LabelHashCode, LabelName>
 */
function getInstructionText(instruction, labelNames) {
	var out = "";
	if (instruction.getType() != 8) // LABEL
		out += " "; // Nice formatting
	if (instruction.getOpcode() > 0) // Labels, Frames and LineNumbers don't have opcodes
		out += OPCODES[instruction.getOpcode()] + " ";
	switch (instruction.getType()) {
		default:
		case 0: // INSN
		break;
		case 1: // INT_INSN
			out += instruction.operand;
		break;
		case 2: // VAR_INSN
			out += instruction.var;
		break;
		case 3: // TYPE_INSN
			out += instruction.desc;
		break;
		case 4: // FIELD_INSN
			out += instruction.owner + "." + instruction.name + " " + instruction.desc;
		break;
		case 5: // METHOD_INSN
			out += instruction.owner + "." + instruction.name + " " + instruction.desc + " (" + instruction.itf + ")";
		break;
		case 6: // INVOKE_DYNAMIC_INSN
			out += instruction.name + " " + instruction.desc;
		break;
		case 7: // JUMP_INSN
			out += getLabelName(instruction.label, labelNames);
		break;
		case 8: // LABEL
			out += getLabelName(instruction.getLabel(), labelNames);
		break;
		case 9: // LDC_INSN
			out += instruction.cst;
		break;
		case 10: // IINC_INSN
			out += instruction.var + " " + instruction.incr;
		break;
		case 11: // TABLESWITCH_INSN
			out += instruction.min + " " + instruction.max;
			out += "\n";
			for (var i = 0; i < instruction.labels.length; ++i) {
			  out += "   " + (instruction.min + i) + ": ";
			  out += getLabelName(instruction.labels[i], labelNames);
			  out += "\n";
			}
			out += "   " + "default: " + getLabelName(instruction.dflt, labelNames);
		break;
		case 12: // LOOKUPSWITCH_INSN
			for (var i = 0; i < instruction.labels.length; ++i) {
			  out += "   " + instruction.keys[i] + ": ";
			  out += getLabelName(instruction.labels[i], labelNames);
			  out += "\n";
			}
			out += "   " + "default: " + getLabelName(instruction.dflt, labelNames);
		break;
		case 13: // MULTIANEWARRAY_INSN
			out += instruction.desc + " " + instruction.dims;
		break;
		case 14: // FRAME
			out += "FRAME";
			// Frames don't work because Nashhorn calls AbstractInsnNode#getType()
			// instead of accessing FrameNode#type for the code "instruction.type"
			// so there is no way to get the frame type of the FrameNode
		break;
		case 15: // LINENUMBER
			out += "LINENUMBER ";
			out += instruction.line + " " + getLabelName(instruction.start.getLabel(), labelNames);
		break;
	}
	return out;
}

/**
 * Util function to get the name for a LabelNode "instruction"
 *
 * @param {LabelNode} label The label to generate a name for
 * @param {Map<int, string>} labelNames The names of other labels in the format Map<LabelHashCode, LabelName>
 */
function getLabelName(label, labelNames) {
	var labelHashCode = label.hashCode();
	var labelName = labelNames[labelHashCode];
	if (labelName == undefined) {
		labelName = "L" + labelNames.length;
		labelNames[labelHashCode] = labelName;
		++labelNames.length;
	}
	return labelName;
}

/** The names of the Java Virtual Machine opcodes. */
OPCODES = [
	"NOP", // 0 (0x0)
	"ACONST_NULL", // 1 (0x1)
	"ICONST_M1", // 2 (0x2)
	"ICONST_0", // 3 (0x3)
	"ICONST_1", // 4 (0x4)
	"ICONST_2", // 5 (0x5)
	"ICONST_3", // 6 (0x6)
	"ICONST_4", // 7 (0x7)
	"ICONST_5", // 8 (0x8)
	"LCONST_0", // 9 (0x9)
	"LCONST_1", // 10 (0xa)
	"FCONST_0", // 11 (0xb)
	"FCONST_1", // 12 (0xc)
	"FCONST_2", // 13 (0xd)
	"DCONST_0", // 14 (0xe)
	"DCONST_1", // 15 (0xf)
	"BIPUSH", // 16 (0x10)
	"SIPUSH", // 17 (0x11)
	"LDC", // 18 (0x12)
	"LDC_W", // 19 (0x13)
	"LDC2_W", // 20 (0x14)
	"ILOAD", // 21 (0x15)
	"LLOAD", // 22 (0x16)
	"FLOAD", // 23 (0x17)
	"DLOAD", // 24 (0x18)
	"ALOAD", // 25 (0x19)
	"ILOAD_0", // 26 (0x1a)
	"ILOAD_1", // 27 (0x1b)
	"ILOAD_2", // 28 (0x1c)
	"ILOAD_3", // 29 (0x1d)
	"LLOAD_0", // 30 (0x1e)
	"LLOAD_1", // 31 (0x1f)
	"LLOAD_2", // 32 (0x20)
	"LLOAD_3", // 33 (0x21)
	"FLOAD_0", // 34 (0x22)
	"FLOAD_1", // 35 (0x23)
	"FLOAD_2", // 36 (0x24)
	"FLOAD_3", // 37 (0x25)
	"DLOAD_0", // 38 (0x26)
	"DLOAD_1", // 39 (0x27)
	"DLOAD_2", // 40 (0x28)
	"DLOAD_3", // 41 (0x29)
	"ALOAD_0", // 42 (0x2a)
	"ALOAD_1", // 43 (0x2b)
	"ALOAD_2", // 44 (0x2c)
	"ALOAD_3", // 45 (0x2d)
	"IALOAD", // 46 (0x2e)
	"LALOAD", // 47 (0x2f)
	"FALOAD", // 48 (0x30)
	"DALOAD", // 49 (0x31)
	"AALOAD", // 50 (0x32)
	"BALOAD", // 51 (0x33)
	"CALOAD", // 52 (0x34)
	"SALOAD", // 53 (0x35)
	"ISTORE", // 54 (0x36)
	"LSTORE", // 55 (0x37)
	"FSTORE", // 56 (0x38)
	"DSTORE", // 57 (0x39)
	"ASTORE", // 58 (0x3a)
	"ISTORE_0", // 59 (0x3b)
	"ISTORE_1", // 60 (0x3c)
	"ISTORE_2", // 61 (0x3d)
	"ISTORE_3", // 62 (0x3e)
	"LSTORE_0", // 63 (0x3f)
	"LSTORE_1", // 64 (0x40)
	"LSTORE_2", // 65 (0x41)
	"LSTORE_3", // 66 (0x42)
	"FSTORE_0", // 67 (0x43)
	"FSTORE_1", // 68 (0x44)
	"FSTORE_2", // 69 (0x45)
	"FSTORE_3", // 70 (0x46)
	"DSTORE_0", // 71 (0x47)
	"DSTORE_1", // 72 (0x48)
	"DSTORE_2", // 73 (0x49)
	"DSTORE_3", // 74 (0x4a)
	"ASTORE_0", // 75 (0x4b)
	"ASTORE_1", // 76 (0x4c)
	"ASTORE_2", // 77 (0x4d)
	"ASTORE_3", // 78 (0x4e)
	"IASTORE", // 79 (0x4f)
	"LASTORE", // 80 (0x50)
	"FASTORE", // 81 (0x51)
	"DASTORE", // 82 (0x52)
	"AASTORE", // 83 (0x53)
	"BASTORE", // 84 (0x54)
	"CASTORE", // 85 (0x55)
	"SASTORE", // 86 (0x56)
	"POP", // 87 (0x57)
	"POP2", // 88 (0x58)
	"DUP", // 89 (0x59)
	"DUP_X1", // 90 (0x5a)
	"DUP_X2", // 91 (0x5b)
	"DUP2", // 92 (0x5c)
	"DUP2_X1", // 93 (0x5d)
	"DUP2_X2", // 94 (0x5e)
	"SWAP", // 95 (0x5f)
	"IADD", // 96 (0x60)
	"LADD", // 97 (0x61)
	"FADD", // 98 (0x62)
	"DADD", // 99 (0x63)
	"ISUB", // 100 (0x64)
	"LSUB", // 101 (0x65)
	"FSUB", // 102 (0x66)
	"DSUB", // 103 (0x67)
	"IMUL", // 104 (0x68)
	"LMUL", // 105 (0x69)
	"FMUL", // 106 (0x6a)
	"DMUL", // 107 (0x6b)
	"IDIV", // 108 (0x6c)
	"LDIV", // 109 (0x6d)
	"FDIV", // 110 (0x6e)
	"DDIV", // 111 (0x6f)
	"IREM", // 112 (0x70)
	"LREM", // 113 (0x71)
	"FREM", // 114 (0x72)
	"DREM", // 115 (0x73)
	"INEG", // 116 (0x74)
	"LNEG", // 117 (0x75)
	"FNEG", // 118 (0x76)
	"DNEG", // 119 (0x77)
	"ISHL", // 120 (0x78)
	"LSHL", // 121 (0x79)
	"ISHR", // 122 (0x7a)
	"LSHR", // 123 (0x7b)
	"IUSHR", // 124 (0x7c)
	"LUSHR", // 125 (0x7d)
	"IAND", // 126 (0x7e)
	"LAND", // 127 (0x7f)
	"IOR", // 128 (0x80)
	"LOR", // 129 (0x81)
	"IXOR", // 130 (0x82)
	"LXOR", // 131 (0x83)
	"IINC", // 132 (0x84)
	"I2L", // 133 (0x85)
	"I2F", // 134 (0x86)
	"I2D", // 135 (0x87)
	"L2I", // 136 (0x88)
	"L2F", // 137 (0x89)
	"L2D", // 138 (0x8a)
	"F2I", // 139 (0x8b)
	"F2L", // 140 (0x8c)
	"F2D", // 141 (0x8d)
	"D2I", // 142 (0x8e)
	"D2L", // 143 (0x8f)
	"D2F", // 144 (0x90)
	"I2B", // 145 (0x91)
	"I2C", // 146 (0x92)
	"I2S", // 147 (0x93)
	"LCMP", // 148 (0x94)
	"FCMPL", // 149 (0x95)
	"FCMPG", // 150 (0x96)
	"DCMPL", // 151 (0x97)
	"DCMPG", // 152 (0x98)
	"IFEQ", // 153 (0x99)
	"IFNE", // 154 (0x9a)
	"IFLT", // 155 (0x9b)
	"IFGE", // 156 (0x9c)
	"IFGT", // 157 (0x9d)
	"IFLE", // 158 (0x9e)
	"IF_ICMPEQ", // 159 (0x9f)
	"IF_ICMPNE", // 160 (0xa0)
	"IF_ICMPLT", // 161 (0xa1)
	"IF_ICMPGE", // 162 (0xa2)
	"IF_ICMPGT", // 163 (0xa3)
	"IF_ICMPLE", // 164 (0xa4)
	"IF_ACMPEQ", // 165 (0xa5)
	"IF_ACMPNE", // 166 (0xa6)
	"GOTO", // 167 (0xa7)
	"JSR", // 168 (0xa8)
	"RET", // 169 (0xa9)
	"TABLESWITCH", // 170 (0xaa)
	"LOOKUPSWITCH", // 171 (0xab)
	"IRETURN", // 172 (0xac)
	"LRETURN", // 173 (0xad)
	"FRETURN", // 174 (0xae)
	"DRETURN", // 175 (0xaf)
	"ARETURN", // 176 (0xb0)
	"RETURN", // 177 (0xb1)
	"GETSTATIC", // 178 (0xb2)
	"PUTSTATIC", // 179 (0xb3)
	"GETFIELD", // 180 (0xb4)
	"PUTFIELD", // 181 (0xb5)
	"INVOKEVIRTUAL", // 182 (0xb6)
	"INVOKESPECIAL", // 183 (0xb7)
	"INVOKESTATIC", // 184 (0xb8)
	"INVOKEINTERFACE", // 185 (0xb9)
	"INVOKEDYNAMIC", // 186 (0xba)
	"NEW", // 187 (0xbb)
	"NEWARRAY", // 188 (0xbc)
	"ANEWARRAY", // 189 (0xbd)
	"ARRAYLENGTH", // 190 (0xbe)
	"ATHROW", // 191 (0xbf)
	"CHECKCAST", // 192 (0xc0)
	"INSTANCEOF", // 193 (0xc1)
	"MONITORENTER", // 194 (0xc2)
	"MONITOREXIT", // 195 (0xc3)
	"WIDE", // 196 (0xc4)
	"MULTIANEWARRAY", // 197 (0xc5)
	"IFNULL", // 198 (0xc6)
	"IFNONNULL" // 199 (0xc7)
];
