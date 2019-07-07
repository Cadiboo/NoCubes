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
	
	ISTORE = Opcodes.ISTORE;

	RETURN = Opcodes.RETURN;
	ARETURN = Opcodes.ARETURN;
	IRETURN = Opcodes.IRETURN;

	NEW = Opcodes.NEW;

	ACONST_NULL = Opcodes.ACONST_NULL;
	ICONST_0 = Opcodes.ICONST_0;

	IFEQ = Opcodes.IFEQ;
	IFNE = Opcodes.IFNE;
	IF_ACMPEQ = Opcodes.IF_ACMPEQ;

	GETFIELD = Opcodes.GETFIELD;
	PUTFIELD = Opcodes.PUTFIELD;
	GETSTATIC = Opcodes.GETSTATIC;

	GOTO = Opcodes.GOTO;

	LABEL = AbstractInsnNode.LABEL;

	return {
		"ChunkRenderCache#<init>": {
			"target": {
				"type": "METHOD",
				"class": "net.minecraft.client.renderer.chunk.ChunkRenderCache",
				"methodName": "<init>",
				"methodDesc": "(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V"
			},
			"transformer": function(methodNode) {
				injectInitChunkRenderCacheHook(methodNode.instructions);
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
				"methodName": "func_217396_m",
				"methodDesc": "(Lnet/minecraft/util/math/BlockPos;)V"
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
				var methods = classNode.methods;
				methods.add(make_nocubes_isTerrainSmoothable());
				methods.add(make_nocubes_setTerrainSmoothable());
				methods.add(make_nocubes_isLeavesSmoothable());
				methods.add(make_nocubes_setLeavesSmoothable());
				return classNode;
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
	for (i = instructions.indexOf(first_INVOKEVIRTUAL_getX); i >=0; --i) {
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
// 6) Fnject GOTO to label
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
	for (var i = arrayLength - 1; i >=0; --i) {
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
	for (i = instructions.indexOf(firstACONST_NULL_then_ARETURN); i >=0; --i) {
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
	for (i = instructions.indexOf(firstIFEQBefore_firstACONST_NULL_then_ARETURN); i >=0; --i) {
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
	for (i = instructions.indexOf(firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN); i >=0; --i) {
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
//		if(io.github.cadiboo.nocubes.hooks.Hooks.renderBlockDamage(this, state, pos, sprite, reader)){
//			return;
//		}
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

	var getAllInBoxMutable_name = ASMAPI.mapMethod("func_218278_a"); // getAllInBoxMutable

	var first_INVOKESTATIC_getAllInBoxMutable;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKESTATIC) {
			if (instruction.owner == "net/minecraft/util/math/BlockPos") {
				if (instruction.name == getAllInBoxMutable_name) {
					if (instruction.desc == "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;") {
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
	for (i = instructions.indexOf(first_INVOKESTATIC_getAllInBoxMutable); i >=0; --i) {
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
	toInject.add(new VarInsnNode(ALOAD, 12)); // lvt_12_1_ - chunkRenderCache
	toInject.add(new VarInsnNode(ALOAD, 13)); // aboolean - usedRenderLayers
	toInject.add(new VarInsnNode(ALOAD, 14)); // random
	toInject.add(new VarInsnNode(ALOAD, 15)); // blockrendererdispatcher
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"preIteration",
			//String descriptor
			"(Lnet/minecraft/client/renderer/chunk/ChunkRender;FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;Lnet/minecraft/client/renderer/chunk/CompiledChunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/World;Lnet/minecraft/client/renderer/chunk/VisGraph;Ljava/util/HashSet;Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;[ZLjava/util/Random;Lnet/minecraft/client/renderer/BlockRendererDispatcher;)V",
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
	for (i = instructions.indexOf(BlockState_getRenderType); i >=0; --i) {
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
	toInject.add(new VarInsnNode(ALOAD, 18)); // blockstate
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

//	}
//
//	IFluidState ifluidstate = lvt_12_1_.getFluidState(blockpos2);
//	net.minecraftforge.client.model.data.IModelData modelData = generator.getModelData(blockpos2);

//	}
//
//	// NoCubes Start
////	IFluidState ifluidstate = lvt_12_1_.getFluidState(blockpos2);
//	IFluidState ifluidstate = net.minecraft.fluid.Fluids.EMPTY.getDefaultState();
//	// NoCubes End
//	net.minecraftforge.client.model.data.IModelData modelData = generator.getModelData(blockpos2);

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

	var getFluidState_name = ASMAPI.mapMethod("func_204610_c"); // getFluidState

	var first_INVOKEVIRTUAL_getFluidState;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/client/renderer/chunk/ChunkRenderCache") {
				if (instruction.name == getFluidState_name) {
					if (instruction.desc == "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;") {
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

	// Remove "ALOAD 12", "ALOAD 17", "INVOKEVIRTUAL getFluidState"
	instructions.remove(first_INVOKEVIRTUAL_getFluidState.getPrevious().getPrevious());
	instructions.remove(first_INVOKEVIRTUAL_getFluidState.getPrevious());
	instructions.remove(first_INVOKEVIRTUAL_getFluidState);

}

// 1) Finds the first label
// 2) injects after first label
function injectMarkForRerenderHook(instructions) {

//	public void markForRerender(BlockPos pos) {
//		this.worldRenderer.markBlockRangeForRenderUpdate(pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ());
//	}

//	public void markForRerender(BlockPos pos) {
//		// NoCubes Start
//		io.github.cadiboo.nocubes.hooks.Hooks.markForRerender(pos, this.worldRenderer);
//		// NoCubes End
//	}


//  public markForRerender(Lnet/minecraft/util/math/BlockPos;)V
//   L0
//    LINENUMBER 525 L0
//    ALOAD 0
//    GETFIELD net/minecraft/client/world/ClientWorld.worldRenderer : Lnet/minecraft/client/renderer/WorldRenderer;
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getY ()I
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getZ ()I
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getY ()I
//    ALOAD 1
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getZ ()I
//    INVOKEVIRTUAL net/minecraft/client/renderer/WorldRenderer.markBlockRangeForRenderUpdate (IIIIII)V
//   L1
//    LINENUMBER 526 L1
//    RETURN

//  public markForRerender(Lnet/minecraft/util/math/BlockPos;)V
//   L0
//    LINENUMBER 548 L0
//    ALOAD 1
//    ALOAD 0
//    GETFIELD net/minecraft/client/world/ClientWorld.worldRenderer : Lnet/minecraft/client/renderer/WorldRenderer;
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.markForRerender (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;)V
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
	var worldRenderer_name = ASMAPI.mapField("field_217430_d"); // worldRenderer

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(ALOAD, 1)); // pos
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new FieldInsnNode(GETFIELD, "net/minecraft/client/world/ClientWorld", worldRenderer_name, "Lnet/minecraft/client/renderer/WorldRenderer;"));
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"markForRerender",
			//String descriptor
			"(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;)V",
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

//// access flags 0x2
//  private Z nocubes_isTerrainSmoothable
//  private Z nocubes_isLeavesSmoothable
//
//  // access flags 0x1
//  public nocubes_isTerrainSmoothable()Z
//   L0
//    LINENUMBER 27 L0
//    ALOAD 0
//    GETFIELD net/minecraft/block/BlockState.nocubes_isTerrainSmoothable : Z
//    IRETURN
//   L1
//    LOCALVARIABLE this Lnet/minecraft/block/BlockState; L0 L1 0
//    MAXSTACK = 1
//    MAXLOCALS = 1
//
//  // access flags 0x1
//  public nocubes_setTerrainSmoothable(Z)V
//   L0
//    LINENUMBER 32 L0
//    ALOAD 0
//    ILOAD 1
//    PUTFIELD net/minecraft/block/BlockState.nocubes_isTerrainSmoothable : Z
//   L1
//    LINENUMBER 33 L1
//    RETURN
//   L2
//    LOCALVARIABLE this Lnet/minecraft/block/BlockState; L0 L2 0
//    LOCALVARIABLE isTerrainSmoothable Z L0 L2 1
//    MAXSTACK = 2
//    MAXLOCALS = 2
//
//  // access flags 0x1
//  public nocubes_isLeavesSmoothable()Z
//   L0make_nocubes_setLeavesSmoothable
//    LINENUMBER 27 L0
//    ALOAD 0
//    GETFIELD net/minecraft/block/BlockState.nocubes_isLeavesSmoothable : Z
//    IRETURN
//   L1
//    LOCALVARIABLE this Lnet/minecraft/block/BlockState; L0 L1 0
//    MAXSTACK = 1
//    MAXLOCALS = 1
//
//  // access flags 0x1
//  public nocubes_setLeavesSmoothable(Z)V
//   L0
//    LINENUMBER 32 L0
//    ALOAD 0
//    ILOAD 1
//    PUTFIELD net/minecraft/block/BlockState.nocubes_isLeavesSmoothable : Z
//   L1
//    LINENUMBER 33 L1
//    RETURN
//   L2
//    LOCALVARIABLE this Lnet/minecraft/block/BlockState; L0 L2 0
//    LOCALVARIABLE isTerrainSmoothable Z L0 L2 1
//    MAXSTACK = 2
//    MAXLOCALS = 2


function make_nocubes_isTerrainSmoothable() {
	var method = new MethodNode(
//		final int access,
		ACC_PUBLIC,
//		final String name,
		"nocubes_isTerrainSmoothable",
//		final String descriptor,
		"()Z",
//		final String signature,
		null,
//		final String[] exceptions
		null
	);
	var instructions = method.instructions;
	instructions.add(new VarInsnNode(ALOAD, 0)); // this
	instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/BlockState", "nocubes_isTerrainSmoothable", "Z"));
	instructions.add(new InsnNode(IRETURN));
	return method;
}

function make_nocubes_setTerrainSmoothable() {
	var method = new MethodNode(
//		final int access,
		ACC_PUBLIC,
//		final String name,
		"nocubes_setTerrainSmoothable",
//		final String descriptor,
		"(Z)V",
//		final String signature,
		null,
//		final String[] exceptions
		null
	);
	var instructions = method.instructions;
	instructions.add(new VarInsnNode(ALOAD, 0)); // this
	instructions.add(new VarInsnNode(ILOAD, 1)); // newIsTerrainSmoothable
	instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/BlockState", "nocubes_isTerrainSmoothable", "Z"));
	instructions.add(new InsnNode(RETURN));
	return method;
}

function make_nocubes_isLeavesSmoothable() {
	var method = new MethodNode(
//		final int access,
		ACC_PUBLIC,
//		final String name,
		"nocubes_isLeavesSmoothable",
//		final String descriptor,
		"()Z",
//		final String signature,
		null,
//		final String[] exceptions
		null
	);
	var instructions = method.instructions;
	instructions.add(new VarInsnNode(ALOAD, 0)); // this
	instructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/block/BlockState", "nocubes_isLeavesSmoothable", "Z"));
	instructions.add(new InsnNode(IRETURN));
	return method;
}

function make_nocubes_setLeavesSmoothable() {
	var method = new MethodNode(
//		final int access,
		ACC_PUBLIC,
//		final String name,
		"nocubes_setLeavesSmoothable",
//		final String descriptor,
		"(Z)V",
//		final String signature,
		null,
//		final String[] exceptions
		null
	);
	var instructions = method.instructions;
	instructions.add(new VarInsnNode(ALOAD, 0)); // this
	instructions.add(new VarInsnNode(ILOAD, 1)); // newIsLeavesSmoothable
	instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/block/BlockState", "nocubes_isLeavesSmoothable", "Z"));
	instructions.add(new InsnNode(RETURN));
	return method;
}


// 1) Find first label
// 2) inject right after first label
function injectIsSolidHook(instructions) {

//	return this.getBlock().isSolid(this);

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.config.Config.renderSmoothTerrain && this.nocubes_isTerrainSmoothable()) return false;
//	if (io.github.cadiboo.nocubes.config.Config.renderSmoothLeaves && this.nocubes_isLeavesSmoothable()) return false;
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
//    INVOKEVIRTUAL net/minecraft/block/BlockState.nocubes_isTerrainSmoothable ()Z
//    IFEQ L1
//    ICONST_0
//    IRETURN
//   L1
//    LINENUMBER 214 L1
//   FRAME SAME
//    GETSTATIC io/github/cadiboo/nocubes/config/Config.renderSmoothLeaves : Z
//    IFEQ L2
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/BlockState.nocubes_isLeavesSmoothable ()Z
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
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEVIRTUAL,
			//String owner
			"net/minecraft/block/BlockState",
			//String name
			"nocubes_isTerrainSmoothable",
			//String descriptor
			"()Z",
			//boolean isInterface
			false
	));
	toInject.add(new JumpInsnNode(IFEQ, leavesChecksLabel));
	toInject.add(new InsnNode(ICONST_0));
	toInject.add(new InsnNode(IRETURN));
	toInject.add(leavesChecksLabel);
	toInject.add(new FieldInsnNode(GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothLeaves", "Z"));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));
	toInject.add(new VarInsnNode(ALOAD, 0)); // this
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEVIRTUAL,
			//String owner
			"net/minecraft/block/BlockState",
			//String name
			"nocubes_isLeavesSmoothable",
			//String descriptor
			"()Z",
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

// 1) Find first label
// 2) inject right after first label
function injectCausesSuffocationHook(instructions) {

//	return this.getBlock().causesSuffocation(this, reader, pos);

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.hooks.Hooks.doesNotCauseSuffocation(this, reader, pos)) return false;
//	// NoCubes End
//	return this.getBlock().causesSuffocation(this, reader, pos);


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




















function removeBetweenInclusive(instructions, startInstruction, endInstruction) {
	var start = instructions.indexOf(startInstruction);
	var end = instructions.indexOf(endInstruction);
	for (var i = start; i < end; ++i) {
		instructions.remove(instructions.get(start));
	}
}
