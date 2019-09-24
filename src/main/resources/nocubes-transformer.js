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

	return {
		"ChunkRenderCache#<init>": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.chunk.ChunkRenderCache",
				"methodName": "<init>",
				"methodDesc": "(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V"
			},
			"transformer": function(methodNode) {
				// OptiFine makes the BlockState[] and IFluidState[] null.
				// Vanilla doesn't use null anywhere in the method.
				var instructions = methodNode.instructions;
				for (var i = instructions.size() - 1; i >= 0; --i) {
					if (instructions.get(i).getOpcode() == ACONST_NULL) {
						isOptiFinePresent = true;
						print("Found OptiFine - ChunkRenderCache#<init> NULL");
						break;
					}
				}
				if (!isOptiFinePresent)
					injectInitChunkRenderCacheHook(instructions);
				return methodNode;
			}
		},
		"ChunkRenderCache#generateCache": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.chunk.ChunkRenderCache",
				"methodName": "func_212397_a",
				"methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;"
			},
			"transformer": function(methodNode) {
				// OptiFine immediately calls another method.
				// Vanilla returns null when the chunk is empty.
				var instructions = methodNode.instructions;
				for (var i = instructions.size() - 1; i >= 0; --i) {
					if (instructions.get(i).getOpcode() == ACONST_NULL) {
						print("Found lack of OptiFine - ChunkRenderCache#generateCache NULL");
						isOptiFinePresent = false;
						break;
					}
				}
				if (!isOptiFinePresent)
					injectGenerateCacheHook(instructions);
				return methodNode;
			}
		},
		"ChunkRenderCache#generateCache OptiFine": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.chunk.ChunkRenderCache",
				"methodName": "generateCache",
				"methodDesc": "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;IZ)Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;"
			},
			"transformer": function(methodNode) {
				// OptiFine added method
				isOptiFinePresent = true;
				print("Found OptiFine - ChunkRenderCache#generateCache OptiFine");
				injectGenerateCacheHook(methodNode.instructions);
				return methodNode;
			}
		},
		"BlockRendererDispatcher#renderBlockDamage": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.BlockRendererDispatcher",
				"methodName": "func_215329_a",
				"methodDesc": "(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IEnviromentBlockReader;)V"
			},
			"transformer": function(methodNode) {
				injectRenderBlockDamageHook(methodNode.instructions);
				return methodNode;
			}
		},
		"ChunkRender#rebuildChunk": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.chunk.ChunkRender",
				"methodName": "func_178581_b",
				"methodDesc": "(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V"
			},
			"transformer": function(methodNode) {
				var instructions = methodNode.instructions;
				// Check if any instructions reference an OptiFine class.
				for (var i = instructions.size() - 1; i >= 0; --i) {
					var instruction = instructions.get(i);
					if (instruction.getType() == METHOD_INSN && instruction.owner == "net/optifine/override/ChunkCacheOF") {
						isOptiFinePresent = true;
						print("Found OptiFine - ChunkCacheOF class");
						break;
					}
				}
				injectPreIterationHook(instructions);
				injectBlockRenderHook(instructions);
				injectFluidRenderBypass(instructions);
				return methodNode;
			}
		},
		"ClientWorld#markForRerender": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.world.ClientWorld",
				"methodName": "func_225319_b",
				"methodDesc": "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V"
			},
			"transformer": function(methodNode) {
				injectMarkForRerenderHook(methodNode.instructions);
				return methodNode;
			}
		},
		"IWorldReader#getCollisionShapes": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.world.IWorldReader",
				"methodName": "func_223438_b",
				"methodDesc": "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/stream/Stream;"
			},
			"transformer": function(methodNode) {
				injectGetCollisionShapesHook(methodNode.instructions);
				return methodNode;
			}
		},
		"World#getFluidState": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.world.World",
				"methodName": "func_204610_c",
				"methodDesc": "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;"
			},
			"transformer": function(methodNode) {
				injectGetFluidStateHook(methodNode.instructions);
				return methodNode;
			}
		},
		"BlockState#isSolid": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.block.BlockState",
				"methodName": "func_200132_m",
				"methodDesc": "()Z"
			},
			"transformer": function(methodNode) {
				injectIsSolidHook(methodNode.instructions);
				return methodNode;
			}
		},
		"BlockState#causesSuffocation": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.block.BlockState",
				"methodName": "func_215696_m",
				"methodDesc": "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z"
			},
			"transformer": function(methodNode) {
				injectCausesSuffocationHook(methodNode.instructions);
				return methodNode;
			}
		},
		"BlockState": {
			"target": {
				"type": "CLASS",
				"name": "net.minecraft.block.BlockState"
			},
			"transformer": function(classNode) {
				var fields = classNode.fields;
				// Params: int access, String name, String descriptor, String signature, Object value
				fields.add(new FieldNode(ACC_PUBLIC, "nocubes_isTerrainSmoothable", "Z", null, false));
				fields.add(new FieldNode(ACC_PUBLIC, "nocubes_isLeavesSmoothable", "Z", null, false));
				return classNode;
			}
		},
		"VoxelShapes#getAllowedOffset": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.util.math.shapes.VoxelShapes",
				"methodName": "func_216386_a",
				"methodDesc": "(Lnet/minecraft/util/math/AxisAlignedBB;Lnet/minecraft/world/IWorldReader;DLnet/minecraft/util/math/shapes/ISelectionContext;Lnet/minecraft/util/AxisRotation;Ljava/util/stream/Stream;)D"
			},
			"transformer": function(methodNode) {
				injectGetAllowedOffsetHook(methodNode.instructions);
				return methodNode;
			}
		}
	}
}

// 1) Find first INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
// 2) Find first label before
// 3) inject right after label
function injectInitChunkRenderCacheHook(instructions) {

//	this.cacheStartPos = start;
//	this.cacheSizeX = end.getX() - start.getX() + 1;

//	this.cacheStartPos = start;
//	// NoCubes Start
//	io.github.cadiboo.nocubes.hooks.Hooks.initChunkRenderCache(this, chunkStartX, chunkStartZ, chunks, start, end);
//	// NoCubes End


//   L5
//    LINENUMBER 68 L5
//    ALOAD 0
//    ALOAD 5
//    PUTFIELD net/minecraft/client/renderer/chunk/ChunkRenderCache.cacheStartPos : Lnet/minecraft/util/math/BlockPos;
//   L6
//    LINENUMBER 70 L6
//    ALOAD 0
//    ALOAD 6
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
//    ALOAD 5
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
//    ISUB
//    ICONST_1
//    IADD
//    PUTFIELD net/minecraft/client/renderer/chunk/ChunkRenderCache.cacheSizeX : I
//   L7

//   L5
//    LINENUMBER 42 L5
//    ALOAD 0
//    ALOAD 5
//    PUTFIELD net/minecraft/client/renderer/chunk/ChunkRenderCache.cacheStartPos : Lnet/minecraft/util/math/BlockPos;
//   L6
//    LINENUMBER 44 L6
//    ALOAD 0
//    ILOAD 2
//    ILOAD 3
//    ALOAD 4
//    ALOAD 5
//    ALOAD 6
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.initChunkRenderCache (Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V
//    RETURN
//   L7


	var getX_name = ASMAPI.mapMethod("func_177958_n"); // Vec3i.getX

	var first_INVOKEVIRTUAL_getX;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/util/math/BlockPos") {
				if (instruction.name == getX_name) {
					if (instruction.desc == "()I") {
						if (instruction.itf == false) {
							first_INVOKEVIRTUAL_getX = instruction;
							print("Found injection point \"Vec3i.getX\" " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!first_INVOKEVIRTUAL_getX) {
		throw "Error: Couldn't find injection point \"Vec3i.getX\"!";
	}

	var firstLabelBefore_first_INVOKEVIRTUAL_getX;
	for (i = instructions.indexOf(first_INVOKEVIRTUAL_getX); i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			firstLabelBefore_first_INVOKEVIRTUAL_getX = instruction;
			print("Found label \"firstLabelBefore_first_INVOKEVIRTUAL_getX\" " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_first_INVOKEVIRTUAL_getX) {
		throw "Error: Couldn't find label \"firstLabelBefore_first_INVOKEVIRTUAL_getX\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new VarInsnNode(ILOAD, 2)); // chunkStartX
	toInject.add(new VarInsnNode(ILOAD, 3)); // chunkStartZ
	toInject.add(new VarInsnNode(ALOAD, 4)); // chunks
	toInject.add(new VarInsnNode(ALOAD, 5)); // start
	toInject.add(new VarInsnNode(ALOAD, 6)); // end
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"initChunkRenderCache",
			//String descriptor
			"(Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(RETURN));

	// Inject instructions
	instructions.insert(firstLabelBefore_first_INVOKEVIRTUAL_getX, toInject);

}

// 1) Find last ACONST_NULL then ARETURN
// 2) Find previous IFEQ
// 3) Find previous label
// 4) Find previous INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.isEmptyBetween
// 5) Find next ISTORE
// 6) Inject GOTO to label after ISTORE
function injectGenerateCacheHook(instructions) {

//	for (int x = start.getX() >> 4; x <= end.getX() >> 4; ++x) {
//		for (int z = start.getZ() >> 4; z <= end.getZ() >> 4; ++z) {
//			Chunk chunk = chunks[x - chunkStartX][z - chunkStartZ];
//			if (!chunk.isEmptyBetween(start.getY(), end.getY())) {
//				lvt_9_2_ = false;
//			}
//		}
//	}

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


//   L7
//    LINENUMBER 43 L7
//   FRAME FULL [net/minecraft/world/World net/minecraft/util/math/BlockPos net/minecraft/util/math/BlockPos T I I T T [[Lnet/minecraft/world/chunk/Chunk;] []
//    ICONST_1
//    ISTORE 9
//   L13
//    LINENUMBER 44 L13
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
//    ICONST_4
//    ISHR
//    ISTORE 10
//   L14
//   FRAME APPEND [I I]
//    ILOAD 10
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
//    ICONST_4
//    ISHR
//    IF_ICMPGT L15
//   L16
//    LINENUMBER 45 L16
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getZ ()I
//    ICONST_4
//    ISHR
//    ISTORE 11
//   L17
//   FRAME APPEND [I]
//    ILOAD 11
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getZ ()I
//    ICONST_4
//    ISHR
//    IF_ICMPGT L18
//   L19
//    LINENUMBER 46 L19
//    ALOAD 8
//    ILOAD 10
//    ILOAD 4
//    ISUB
//    AALOAD
//    ILOAD 11
//    ILOAD 5
//    ISUB
//    AALOAD
//    ASTORE 12
//   L20
//    LINENUMBER 47 L20
//    ALOAD 12
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getY ()I
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getY ()I
//    INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.isEmptyBetween (II)Z
//    IFNE L21
//   L22
//    LINENUMBER 48 L22
//    ICONST_0
//    ISTORE 9
//   L21
//    LINENUMBER 45 L21
//   FRAME SAME
//    IINC 11 1
//    GOTO L17
//   L18
//    LINENUMBER 44 L18
//   FRAME CHOP 1
//    IINC 10 1
//    GOTO L14
//   L15
//    LINENUMBER 53 L15
//   FRAME CHOP 1
//    ILOAD 9
//    IFEQ L23
//   L24
//    LINENUMBER 54 L24
//    ACONST_NULL
//    ARETURN

//   L7
//    LINENUMBER 62 L7
//   FRAME CHOP 1
//    ICONST_1
//    ISTORE 9
//   L13
//    LINENUMBER 67 L13
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
//    ICONST_4
//    ISHR
//    ISTORE 10
//   L14
//   FRAME APPEND [I I]
//    ILOAD 10
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
//    ICONST_4
//    ISHR
//    IF_ICMPGT L15
//   L16
//    LINENUMBER 68 L16
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getZ ()I
//    ICONST_4
//    ISHR
//    ISTORE 11
//   L17
//   FRAME APPEND [I]
//    ILOAD 11
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getZ ()I
//    ICONST_4
//    ISHR
//    IF_ICMPGT L18
//   L19
//    LINENUMBER 69 L19
//    ALOAD 8
//    ILOAD 10
//    ILOAD 4
//    ISUB
//    AALOAD
//    ILOAD 11
//    ILOAD 5
//    ISUB
//    AALOAD
//    ASTORE 12
//   L20
//    LINENUMBER 70 L20
//    ALOAD 12
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getY ()I
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getY ()I
//    INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.isEmptyBetween (II)Z
//    IFNE L21
//   L22
//    LINENUMBER 71 L22
//    ICONST_0
//    ISTORE 9
//   L23
//    LINENUMBER 73 L23
//    GOTO L15
//   L21
//    LINENUMBER 68 L21
//   FRAME SAME
//    IINC 11 1
//    GOTO L17
//   L18
//    LINENUMBER 67 L18
//   FRAME CHOP 1
//    IINC 10 1
//    GOTO L14
//   L15
//    LINENUMBER 79 L15
//   FRAME CHOP 1
//    ILOAD 9
//    IFEQ L24
//   L25
//    LINENUMBER 80 L25
//    ACONST_NULL
//    ARETURN

	// 1) Find last ACONST_NULL then ARETURN
	var firstACONST_NULL_then_ARETURN;
	var previousInsn; // The previous insn that was checked (technically the next insn in the list)
	var arrayLength = instructions.size();
	for (var i = arrayLength - 1; i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == ACONST_NULL) {
			if (!previousInsn) {
				continue;
			}
			if (previousInsn.getOpcode() == ARETURN) {
				firstACONST_NULL_then_ARETURN = instruction;
				print("Found ACONST_NULL & ARETURN");
				break;
			}
		}
		previousInsn = instruction;
	}
	if (!firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find ACONST_NULL & ARETURN!";
	}

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
	if (!firstIFEQBefore_firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find IFEQ!";
	}

	// 3) Find previous Label
	var firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN;
	for (i = instructions.indexOf(firstIFEQBefore_firstACONST_NULL_then_ARETURN); i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN = instruction;
			print("Found label \"previous Label\" " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find label \"previous Label\"!";
	}

	// 4) Find previous INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.isEmptyBetween
	var isEmptyBetween_name = ASMAPI.mapMethod("func_76606_c"); // isEmptyBetween

	var firstINVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN;
	for (i = instructions.indexOf(firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN); i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/world/chunk/Chunk") {
				if (instruction.name == isEmptyBetween_name) {
					if (instruction.desc == "(II)Z") {
						if (instruction.itf == false) {
							firstINVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN = instruction;
							print("Found injection point \"Previous INVOKEVIRTUAL Chunk.isEmptyBetween\" " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!firstINVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find injection point \"Previous INVOKEVIRTUAL Chunk.isEmptyBetween\"!";
	}

	// 5) Find next ISTORE
	var firstISTORE_after_First_INVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN;
	for (var i = instructions.indexOf(firstINVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN); i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == ISTORE) {
			firstISTORE_after_First_INVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN = instruction;
			print("Found injection point \"next ISTORE\" " + instruction);
			break;
		}
	}
	if (!firstISTORE_after_First_INVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find injection point \"next ISTORE\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff

	// Make list of instructions to inject
	toInject.add(new JumpInsnNode(GOTO, firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN));

	// Inject instructions
	instructions.insert(firstISTORE_after_First_INVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN, toInject);

}

// 1) Find first label
// 2) Insert right after label
function injectRenderBlockDamageHook(instructions) {

//	public void renderBlockDamage(BlockState state, BlockPos pos, TextureAtlasSprite sprite, IEnviromentBlockReader reader) {
//		if (state.getRenderType() == BlockRenderType.MODEL) {

//	public void renderBlockDamage(BlockState state, BlockPos pos, TextureAtlasSprite sprite, IEnviromentBlockReader reader) {
//		// NoCubes Start
//		if(io.github.cadiboo.nocubes.hooks.Hooks.renderBlockDamage(this, state, pos, sprite, reader)){
//			return;
//		}
//		// NoCubes End
//		if (state.getRenderType() == BlockRenderType.MODEL) {


//  public renderBlockDamage(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IEnviromentBlockReader;)V
//   L0
//    LINENUMBER 40 L0
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getRenderType ()Lnet/minecraft/block/BlockRenderType;
//    GETSTATIC net/minecraft/block/BlockRenderType.MODEL : Lnet/minecraft/block/BlockRenderType;
//    IF_ACMPNE L1
//   L2

//  public renderBlockDamage(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IEnviromentBlockReader;)V
//   L0
//    LINENUMBER 40 L0
//    ALOAD 0
//    ALOAD 1
//    ALOAD 2
//    ALOAD 3
//    ALOAD 4
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.renderBlockDamage (Lnet/minecraft/client/renderer/BlockRendererDispatcher;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IEnviromentBlockReader;)Z
//    IFEQ L1
//   L2
//    LINENUMBER 41 L2
//    RETURN
//   L1
//    LINENUMBER 43 L1
//   FRAME SAME
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getRenderType ()Lnet/minecraft/block/BlockRenderType;
//    GETSTATIC net/minecraft/block/BlockRenderType.MODEL : Lnet/minecraft/block/BlockRenderType;
//    IF_ACMPNE L3
//   L4


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
	toInject.add(new VarInsnNode(ALOAD, 1)); // state
	toInject.add(new VarInsnNode(ALOAD, 2)); // pos
	toInject.add(new VarInsnNode(ALOAD, 3)); // sprite
	toInject.add(new VarInsnNode(ALOAD, 4)); // reader
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"renderBlockDamage",
			//String descriptor
			"(Lnet/minecraft/client/renderer/BlockRendererDispatcher;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IEnviromentBlockReader;)Z",
			//boolean isInterface
			false
	));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
	toInject.add(new InsnNode(RETURN));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insert(firstLabel, toInject);

}

// 1) Finds the first instruction INVOKESTATIC BlockPos.getAllInBoxMutable
// 2) Finds the previous label
// 3) Inserts after the label and before the label's instructions.
function injectPreIterationHook(instructions) {

//	BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//
//	for(BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos1)) {

//	BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//
//	// NoCubes Start
//	io.github.cadiboo.nocubes.hooks.Hooks.preIteration(this, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher);
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

	var getAllInBoxMutable_owner;
	var getAllInBoxMutable_name;
	var getAllInBoxMutable_desc;
	if (!isOptiFinePresent) {
		getAllInBoxMutable_owner = "net/minecraft/util/math/BlockPos";
		getAllInBoxMutable_name = ASMAPI.mapMethod("func_218278_a"); // BlockPos#getAllInBoxMutable
		getAllInBoxMutable_desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;";
	} else {
		getAllInBoxMutable_owner = "net/optifine/BlockPosM";
		getAllInBoxMutable_name = "getAllInBoxMutable"; // BlockPosM#getAllInBoxMutable
		getAllInBoxMutable_desc = "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;";
	}

	var first_INVOKESTATIC_getAllInBoxMutable;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKESTATIC) {
			if (instruction.owner == getAllInBoxMutable_owner) {
				if (instruction.name == getAllInBoxMutable_name) {
					if (instruction.desc == getAllInBoxMutable_desc) {
						if (instruction.itf == false) {
							first_INVOKESTATIC_getAllInBoxMutable = instruction;
							print("Found injection point \"first BlockPos.getAllInBoxMutable\" " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!first_INVOKESTATIC_getAllInBoxMutable) {
		throw "Error: Couldn't find injection point \"first BlockPos.getAllInBoxMutable\"!";
	}

	var firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable;
	for (i = instructions.indexOf(first_INVOKESTATIC_getAllInBoxMutable); i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable = instruction;
			print("Found label \"next Label\" " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable) {
		throw "Error: Couldn't find label \"next Label\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new VarInsnNode(FLOAD, 1)); // x
	toInject.add(new VarInsnNode(FLOAD, 2)); // t
	toInject.add(new VarInsnNode(FLOAD, 3)); // z
	toInject.add(new VarInsnNode(ALOAD, 4)); // generator
	toInject.add(new VarInsnNode(ALOAD, 5)); // compiledchunk
	toInject.add(new VarInsnNode(ALOAD, 7)); // blockpos - startPosition
	toInject.add(new VarInsnNode(ALOAD, 8)); // blockpos1 - endPosition
	toInject.add(new VarInsnNode(ALOAD, 9)); // world
	toInject.add(new VarInsnNode(ALOAD, 10)); // lvt_10_1_ - visGraph
	toInject.add(new VarInsnNode(ALOAD, 11)); // lvt_11_1_ - hashSetTileEntitiesWithGlobalRenderers
	if (!isOptiFinePresent) {
		toInject.add(new VarInsnNode(ALOAD, 12)); // lvt_12_1_ - chunkRenderCache
		toInject.add(new VarInsnNode(ALOAD, 13)); // aboolean - usedRenderLayers
		toInject.add(new VarInsnNode(ALOAD, 14)); // random
		toInject.add(new VarInsnNode(ALOAD, 15)); // blockrendererdispatcher
	} else {
		toInject.add(new VarInsnNode(ALOAD, 12)); // lvt_12_1_ - chunkCacheOF
		toInject.add(new VarInsnNode(ALOAD, 15)); // aboolean - usedRenderLayers
		toInject.add(new VarInsnNode(ALOAD, 16)); // random
		toInject.add(new VarInsnNode(ALOAD, 17)); // blockrendererdispatcher
	}
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"preIteration",
			//String descriptor
			"(Lnet/minecraft/client/renderer/chunk/ChunkRender;FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;Lnet/minecraft/client/renderer/chunk/CompiledChunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/client/renderer/chunk/VisGraph;Ljava/util/HashSet;Lnet/minecraft/world/IEnviromentBlockReader;[ZLjava/util/Random;Lnet/minecraft/client/renderer/BlockRendererDispatcher;)V",
			//boolean isInterface
			false
	));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insert(firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable, toInject);

}

// 1) find BlockState.getRenderType
// 2) find label for BlockState.getRenderType
// 3) find label that BlockState.getRenderType would jump to
// 4) insert right after BlockState.getRenderType label
function injectBlockRenderHook(instructions) {


//	if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && iblockstate.canRenderInLayer(blockrenderlayer1)) {

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.hooks.Hooks.canBlockStateRender(blockstate)))
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

	var blockCannotRenderLabel;

	var getRenderType_name = ASMAPI.mapMethod("func_185901_i"); // getRenderType

	var BlockState_getRenderType;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/block/BlockState") {
				if (instruction.name == getRenderType_name) {
					if (instruction.desc == "()Lnet/minecraft/block/BlockRenderType;") {
						if (instruction.itf == false) {
							BlockState_getRenderType = instruction;
							print("Found injection point \"first BlockState.getRenderType\" " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!BlockState_getRenderType) {
		throw "Error: Couldn't find injection point \"first BlockState.getRenderType\"!";
	}

	var firstLabelBefore_BlockState_getRenderType;
	for (i = instructions.indexOf(BlockState_getRenderType); i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			firstLabelBefore_BlockState_getRenderType = instruction;
			print("Found label \"next Label\" " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_BlockState_getRenderType) {
		throw "Error: Couldn't find label \"next Label\"!";
	}

	var lookStart = instructions.indexOf(BlockState_getRenderType);
	var lookMax = lookStart + 10;
	for (var i = lookStart; i < lookMax; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == IFEQ || instruction.getOpcode() == IFNE || instruction.getOpcode() == IF_ACMPEQ) {
			blockCannotRenderLabel = instruction.label;
			print("Found blockCannotRenderLabel " + instruction.label);
			break;
		}
	}
	if (!blockCannotRenderLabel) {
		throw "Error: Couldn't find blockCannotRenderLabel!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();

	// Make list of instructions to inject
	if (!isOptiFinePresent)
		toInject.add(new VarInsnNode(ALOAD, 18)); // blockstate
	else
		toInject.add(new VarInsnNode(ALOAD, 20)); // blockstate
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"canBlockStateRender",
			//String descriptor
			"(Lnet/minecraft/block/BlockState;)Z",
			//boolean isInterface
			false
	));
	toInject.add(new JumpInsnNode(IFEQ, blockCannotRenderLabel));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insert(firstLabelBefore_BlockState_getRenderType, toInject);

}

// 1) Finds the first instruction INVOKEVIRTUAL ChunkRenderCache.getFluidState
// 2) Then injects
// 3) Then removes the two previous instructions and then the instruction
function injectFluidRenderBypass(instructions) {

// Forge/Vanilla/Original
//	}
//
//	IFluidState ifluidstate = lvt_12_1_.getFluidState(blockpos2);
//	net.minecraftforge.client.model.data.IModelData modelData = generator.getModelData(blockpos2);

// Forge/OptiFine/Original
//	IFluidState ifluidstate = blockstate.getFluidState();
//		if (!ifluidstate.isEmpty()) {

// Forge/Vanilla/Patched
//	}
//
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

// Forge/Vanilla/Original
//    ALOAD 5
//    ALOAD 20
//    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/CompiledChunk.addTileEntity (Lnet/minecraft/tileentity/TileEntity;)V
//   L40
//    LINENUMBER 175 L40
//   FRAME CHOP 2
//    ALOAD 12
//    ALOAD 17
//    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/ChunkRenderCache.getFluidState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;
//    ASTORE 20
//   L48
//    LINENUMBER 177 L48
//    ALOAD 4
//    ALOAD 17
//    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/ChunkRenderTask.getModelData (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraftforge/client/model/data/IModelData;
//    ASTORE 21

// Forge/Vanilla/Patched
//    ALOAD 5
//    ALOAD 20
//    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/CompiledChunk.addTileEntity (Lnet/minecraft/tileentity/TileEntity;)V
//   L40
//    LINENUMBER 175 L40
//   FRAME CHOP 2
//    GETSTATIC net/minecraft/fluid/Fluids.EMPTY : Lnet/minecraft/fluid/Fluid;
//    INVOKEVIRTUAL net/minecraft/fluid/Fluid.getDefaultState ()Lnet/minecraft/fluid/IFluidState;
//    ASTORE 20
//   L48
//    LINENUMBER 177 L48
//    ALOAD 4
//    ALOAD 17
//    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/ChunkRenderTask.getModelData (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraftforge/client/model/data/IModelData;
//    ASTORE 21

// Forge/OptiFine/Original
//    ALOAD 5
//    ALOAD 22
//    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/CompiledChunk.addTileEntity (Lnet/minecraft/tileentity/TileEntity;)V
//   L43
//    LINENUMBER 307 L43
//   FRAME CHOP 2
//    ALOAD 20
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getFluidState ()Lnet/minecraft/fluid/IFluidState;
//    ASTORE 22
//   L51
//    LINENUMBER 309 L51
//    ALOAD 22
//    INVOKEINTERFACE net/minecraft/fluid/IFluidState.isEmpty ()Z (itf)
//    IFNE L52

// Forge/OptiFine/Patched
//    ALOAD 5
//    ALOAD 22
//    INVOKEVIRTUAL net/minecraft/client/renderer/chunk/CompiledChunk.addTileEntity (Lnet/minecraft/tileentity/TileEntity;)V
//   L43
//    LINENUMBER 307 L43
//   FRAME CHOP 2
//    GETSTATIC net/minecraft/fluid/Fluids.EMPTY : Lnet/minecraft/fluid/Fluid;
//    INVOKEVIRTUAL net/minecraft/fluid/Fluid.getDefaultState ()Lnet/minecraft/fluid/IFluidState;
//    ASTORE 22
//   L51
//    LINENUMBER 309 L51
//    ALOAD 22
//    INVOKEINTERFACE net/minecraft/fluid/IFluidState.isEmpty ()Z (itf)
//    IFNE L52

	var getFluidState_owner;
	var getFluidState_name;
	var getFluidState_desc;
	if (!isOptiFinePresent) {
		getFluidState_owner = "net/minecraft/client/renderer/chunk/ChunkRenderCache";
		getFluidState_name = ASMAPI.mapMethod("func_204610_c"); // ChunkRenderCache#getFluidState
		getFluidState_desc = "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;";
	} else {
		getFluidState_owner = "net/minecraft/block/BlockState";
		getFluidState_name = ASMAPI.mapMethod("func_204520_s"); // BlockState#getFluidState
		getFluidState_desc = "()Lnet/minecraft/fluid/IFluidState;";
	}

	var first_INVOKEVIRTUAL_getFluidState;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKEVIRTUAL) {
			if (instruction.owner == getFluidState_owner) {
				if (instruction.name == getFluidState_name) {
					if (instruction.desc == getFluidState_desc) {
						if (instruction.itf == false) {
							first_INVOKEVIRTUAL_getFluidState = instruction;
							print("Found injection point \"first_INVOKEVIRTUAL_getFluidState\" " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!first_INVOKEVIRTUAL_getFluidState) {
		throw "Error: Couldn't find injection point \"first_INVOKEVIRTUAL_getFluidState\"!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();

	var Fluids_EMPTY_name = ASMAPI.mapField("field_204541_a"); // Fluids.EMPTY
	var Fluid_getDefaultState_name = ASMAPI.mapMethod("func_207188_f"); // Fluid#getDefaultState

	// Make list of instructions to inject
	toInject.add(new FieldInsnNode(GETSTATIC, "net/minecraft/fluid/Fluids", Fluids_EMPTY_name, "Lnet/minecraft/fluid/Fluid;"));
	toInject.add(new MethodInsnNode(
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
	));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insert(first_INVOKEVIRTUAL_getFluidState, toInject);

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

// 1) Finds the first label
// 2) injects after first label
function injectMarkForRerenderHook(instructions) {

//	public void markForRerender(BlockPos pos, BlockState oldState, BlockState newState) {
//		this.worldRenderer.markForRerender(pos, oldState, newState);
//	}

//   public void markForRerender(BlockPos pos, BlockState oldState, BlockState newState) {
//		// NoCubes Start
//		io.github.cadiboo.nocubes.hooks.Hooks.markForRerender(this.mc, this.worldRenderer, pos, oldState, newState);
//		// NoCubes End
//	}


//  public markForRerender(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V
//   L0
//    LINENUMBER 525 L0
//    ALOAD 0
//    GETFIELD net/minecraft/client/world/ClientWorld.worldRenderer : Lnet/minecraft/client/renderer/WorldRenderer;
//    ALOAD 1
//    ALOAD 2
//    ALOAD 3
//    INVOKEVIRTUAL net/minecraft/client/renderer/WorldRenderer.markForRerender (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V
//   L1
//    LINENUMBER 526 L1
//    RETURN

//  public markForRerender(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V
//   L0
//    LINENUMBER 547 L0
//    ALOAD 0
//    GETFIELD net/minecraft/client/world/ClientWorld.mc : Lnet/minecraft/client/Minecraft;
//    ALOAD 0
//    GETFIELD net/minecraft/client/world/ClientWorld.worldRenderer : Lnet/minecraft/client/renderer/WorldRenderer;
//    ALOAD 1
//    ALOAD 2
//    ALOAD 3
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.markForRerender (Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V
//    RETURN


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
	var mc_name = ASMAPI.mapField("field_73037_M"); // mc
	var worldRenderer_name = ASMAPI.mapField("field_217430_d"); // worldRenderer

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/world/ClientWorld", mc_name, "Lnet/minecraft/client/Minecraft;"));
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/world/ClientWorld", worldRenderer_name, "Lnet/minecraft/client/renderer/WorldRenderer;"));
	toInject.add(new VarInsnNode(ALOAD, 1)); // pos
	toInject.add(new VarInsnNode(ALOAD, 2)); // oldState
	toInject.add(new VarInsnNode(ALOAD, 3)); // newState
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"markForRerender",
			//String descriptor
			"(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/renderer/WorldRenderer;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(RETURN));

	// Inject instructions
	instructions.insert(firstLabel, toInject);

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

// 1) Find first label
// 2) inject right after first label
function injectIsSolidHook(instructions) {

//	return this.getBlock().isSolid(this);

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.config.Config.renderSmoothTerrain && this.nocubes_isTerrainSmoothable) return false;
//	if (io.github.cadiboo.nocubes.config.Config.renderSmoothLeaves && this.nocubes_isLeavesSmoothable) return false;
//	// NoCubes End
//	return this.getBlock().isSolid(this);


//  public default isSolid()Z
//   L0
//    LINENUMBER 212 L0
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getBlock ()Lnet/minecraft/block/Block;
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/Block.isSolid (Lnet/minecraft/block/BlockState;)Z
//    IRETURN

//  public default isSolid()Z
//   L0
//    LINENUMBER 213 L0
//    GETSTATIC io/github/cadiboo/nocubes/config/Config.renderSmoothTerrain : Z
//    IFEQ L1
//    ALOAD 0
//    GETFIELD net/minecraft/block/BlockState.nocubes_isTerrainSmoothable : Z
//    IFEQ L1
//    ICONST_0
//    IRETURN
//   L1
//    LINENUMBER 214 L1
//   FRAME SAME
//    GETSTATIC io/github/cadiboo/nocubes/config/Config.renderSmoothLeaves : Z
//    IFEQ L2
//    ALOAD 0
//    GETFIELD net/minecraft/block/BlockState.nocubes_isLeavesSmoothable : Z
//    IFEQ L2
//    ICONST_0
//    IRETURN
//   L2
//    LINENUMBER 213 L2
//   FRAME SAME
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getBlock ()Lnet/minecraft/block/Block;
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/Block.isSolid (Lnet/minecraft/block/BlockState;)Z
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
	var leavesChecksLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothTerrain", "Z"));
	toInject.add(new JumpInsnNode(IFEQ, leavesChecksLabel));
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/BlockState", "nocubes_isTerrainSmoothable", "Z"));
	toInject.add(new JumpInsnNode(IFEQ, leavesChecksLabel));
	toInject.add(new InsnNode(ICONST_0));
	toInject.add(new InsnNode(IRETURN));
	toInject.add(leavesChecksLabel);
	toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothLeaves", "Z"));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/BlockState", "nocubes_isLeavesSmoothable", "Z"));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
	toInject.add(new InsnNode(ICONST_0));
	toInject.add(new InsnNode(IRETURN));

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



















function removeBetweenInclusive(instructions, startInstruction, endInstruction) {
	var start = instructions.indexOf(startInstruction);
	var end = instructions.indexOf(endInstruction);
	for (var i = start; i < end; ++i) {
		instructions.remove(instructions.get(start));
	}
}

function printInstructions(instructions) {
	return;
	var arrayLength = instructions.size();
	var labelNames = {
		length: 0
	};
	for (var i = 0; i < arrayLength; ++i) {
		var text = getInstructionText(instructions.get(i), labelNames);
		if (text.length > 0) // Some instructions like frames and lines are ignored
			print(text);
	}
}
function getInstructionText(instruction, labelNames) {
	var out = "";
	if (instruction.getOpcode() > 0)
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
			out += getInstructionText(instruction.label, labelNames);
		break;
		case 8: // LABEL
			var label = instruction.getLabel();
			var labelName = labelNames[label.hashCode()];
			if (labelName == undefined) {
				labelName = "L" + labelNames.length;
				labelNames[label.hashCode()] = labelName;
				++labelNames.length;
			}
			out += labelName;
		break;
		case 9: // LDC_INSN
			out += instruction.cst;
		break;
		case 10: // IINC_INSN
			out += instruction.var + " " + instruction.incr;
		break;
	}
	return out;
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
