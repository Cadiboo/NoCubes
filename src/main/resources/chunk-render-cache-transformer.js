var transformerName = "NoCubes BlockState Transformer";
var targetClass = "net.minecraft.client.renderer.chunk.ChunkRenderCache";
clinit();
start("Initialisation");

start("fieldsToAdd");
// Params: int access, String name, String descriptor, String signature, Object value
var fieldsToAdd = [

];
finish();

start("methodsToAdd");
var methodsToAdd = [

];
finish();

start("targetMethods");
var targetMethods = [
	// <init>
	new TargetMethod("<init>", "(Lnet/minecraft/world/World;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V",
		new MethodTransformer(injectInitChunkRenderCacheHook, "injectInitChunkRenderCacheHook")
	),
	// generateCache
	new TargetMethod("func_212397_a", "(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;",
		new MethodTransformer(injectGenerateCacheHook, "injectGenerateCacheHook")
	)
];
finish();

finish();


// 1) Find first INVOKEVIRTUAL net/minecraft/util/math/BlockPos.getX ()I
// 1) Find first label before
// 2) inject right after label
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
		if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/util/math/BlockPos") {
				if (instruction.name == getX_name) {
					if (instruction.desc == "()I") {
						if (instruction.itf == false) {
							first_INVOKEVIRTUAL_getX = instruction;
							log("Found injection point " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!first_INVOKEVIRTUAL_getX) {
		throw "Error: Couldn't find injection point!";
	}
	
	var firstLabelBefore_first_INVOKEVIRTUAL_getX;
	for (i = instructions.indexOf(first_INVOKEVIRTUAL_getX); i >=0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == AbstractInsnNode.LABEL) {
			firstLabelBefore_first_INVOKEVIRTUAL_getX = instruction;
			log("Found label " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_first_INVOKEVIRTUAL_getX) {
		throw "Error: Couldn't find label!";
	}

	var toInject = new InsnList();

	// Labels n stuff

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	toInject.add(new VarInsnNode(Opcodes.ILOAD, 2)); // chunkStartX
	toInject.add(new VarInsnNode(Opcodes.ILOAD, 3)); // chunkStartZ
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 4)); // chunks
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 5)); // start
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 6)); // end
	toInject.add(new MethodInsnNode(
			//int opcode
			Opcodes.INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"initChunkRenderCache",
			//String descriptor
			"(Lnet/minecraft/client/renderer/chunk/ChunkRenderCache;II[[Lnet/minecraft/world/chunk/Chunk;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)V",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(Opcodes.RETURN));

	// Inject instructions
	instructions.insert(firstLabelBefore_first_INVOKEVIRTUAL_getX, toInject);

}

// 1) Find last ACONST_NULL then ARETURN
// 2) find previous label
// 3) find previous label
// 4) find previous INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.isEmptyBetween
// 5) find next ISTORE
// 6) inject GOTO to label
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

	var firstACONST_NULL_then_ARETURN;
	var previousInsn; // The previous insn that was checked (technically the next insn in the list)
	var arrayLength = instructions.size();
	for (var i = arrayLength - 1; i >=0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == Opcodes.ACONST_NULL) {
			if (!previousInsn) {
				continue;
			}
			if (previousInsn.getOpcode() == Opcodes.ARETURN) {
				firstACONST_NULL_then_ARETURN = instruction;
				log("Found ACONST_NULL & ARETURN");
				break;
			}
		}
		previousInsn = instruction;
	}
	if (!firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find ACONST_NULL & ARETURN!";
	}
	
	var firstIFEQBefore_firstACONST_NULL_then_ARETURN;
	for (i = instructions.indexOf(firstACONST_NULL_then_ARETURN); i >=0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == Opcodes.IFEQ) {
			firstIFEQBefore_firstACONST_NULL_then_ARETURN = instruction;
			log("Found IFEQ");
			break;
		}
	}
	if (!firstIFEQBefore_firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find IFEQ!";
	}

	var firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN;
	for (i = instructions.indexOf(firstIFEQBefore_firstACONST_NULL_then_ARETURN); i >=0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == AbstractInsnNode.LABEL) {
			firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN = instruction;
			log("Found label " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find label!";
	}

	// 4) find previous INVOKEVIRTUAL net/minecraft/world/chunk/Chunk.isEmptyBetween

	var isEmptyBetween_name = ASMAPI.mapMethod("func_76606_c"); // isEmptyBetween

	var firstINVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN;
	for (i = instructions.indexOf(firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN); i >=0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/world/chunk/Chunk") {
				if (instruction.name == isEmptyBetween_name) {
					if (instruction.desc == "(II)Z") {
						if (instruction.itf == false) {
							firstINVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN = instruction;
							log("Found injection point " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!firstINVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find label!";
	}

	// 5) find next ISTORE

	var firstISTORE_after_First_INVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN;
	for (var i = instructions.indexOf(firstINVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN); i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == Opcodes.ISTORE) {
			firstISTORE_after_First_INVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN = instruction;
			log("Found injection point " + instruction);
			break;
		}
	}
	if (!firstISTORE_after_First_INVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN) {
		throw "Error: Couldn't find injection point!";
	}


	var toInject = new InsnList();

	// Labels n stuff

	// Make list of instructions to inject
	toInject.add(new JumpInsnNode(Opcodes.GOTO, firstLabelBefore_firstIFEQBefore_firstACONST_NULL_then_ARETURN));

	// Inject instructions
	instructions.insert(firstISTORE_after_First_INVOKEVIRTUAL_Chunk_isEmptyBetween_Before_firstIFEQBefore_firstACONST_NULL_then_ARETURN, toInject);

}




















function initializeCoreMod() {
	log("Initialising " + transformerName);
	return {
		transformerName: {
			'target': {
				'type': 'CLASS',
				'name': targetClass
			},
			'transformer': function(classNode) {

				log("Starting");
				var hasFinished = false;
				try {
					(function() { // Workaround for a Nashorn bug on Java < 80u60

						if (fieldsToAdd.length > 0) {
							start("Adding Fields");
							var fields = classNode.fields;
							for (var i in fieldsToAdd) {
								var field = fieldsToAdd[i];
								log("Adding Field \"" + field.name + "\"");
								fields.add(field);
							}
							finish();
						}

						if (methodsToAdd.length > 0) {
							start("Adding Methods");
							var methods = classNode.methods;
							for (var i in methodsToAdd) {
								var method = methodsToAdd[i];
								log("Adding Method \"" + method.name + "\"");
								methods.add(method);
							}
							finish();
						}

						if (targetMethods.length > 0) {
							var targetMethodsToFind = targetMethods.length;
							start("Transforming Methods");
							for (var j in targetMethods) {
								var targetMethod = targetMethods[j];
								log("Target Method \"" + targetMethod.name + "\" - \"" + targetMethod.desc + "\"");
							}
							var methods = classNode.methods;
							for (var i in methods) {
								if (targetMethodsToFind == 0) {
									break;
								}
								var method = methods[i];
								var methodName = method.name;
								var methodDesc = method.desc;
								var methodInstructions = method.instructions;
								log("Examining Method \"" + methodName + "\" - \"" + methodDesc + "\"");
								for (var j in targetMethods) {
									var targetMethod = targetMethods[j];
									var targetMethodName = targetMethod.name;
									var targetMethodDesc = targetMethod.desc;
									if (targetMethodName.equals(methodName) && targetMethodDesc.equals(methodDesc)) {
										log("Target Method \"" + targetMethodName + "\" - \"" + targetMethodDesc + "\" matched");
										--targetMethodsToFind;
										targetMethod.found = true;
										var methodTransformers = targetMethod.transformers;
										for (var k in methodTransformers) {
											var methodTransformer = methodTransformers[k];
											start("Apply " + methodTransformer.name);
											methodTransformer.func(methodInstructions);
											finish();
										}
									}
								}
							}
							if (targetMethodsToFind != 0) {
								for (var j in targetMethods) {
									var targetMethod = targetMethods[j];
									if (!targetMethod.found) {
										log("Failed to find Target Method \"" + targetMethod.name + "\" - \"" + targetMethod.desc + "\"!");
									}
								}
								throw "Failed to find all Target Methods!";
							}
							finish();
						}

						hasFinished = true;
					})(); // Workaround for a Nashorn bug on Java < 80u60
				} finally {
					// Hacks because rethrowing an exception sets the linenumber to where it was re-thrown
					if(!hasFinished) {
						log("Caught exception from " + currentlyRunning.pop());
					}
				}
				log("Finished");

				return classNode;
			}
		}
	}
}

function TargetMethod(name, desc, transformer1, transformer2, transformer3) { // Varargs seems not to work :/
	this.name = ASMAPI.mapMethod(name);
	this.desc = desc;
	this.found = false;
	this.transformers = [];
	if (transformer1 != undefined) this.transformers.push(transformer1);
	if (transformer2 != undefined) this.transformers.push(transformer2);
	if (transformer3 != undefined) this.transformers.push(transformer3);
}

function MethodTransformer(func, name) {
	this.func = func;
	this.name = name;
}

function removeBetweenInclusive(instructions, startInstruction, endInstruction) {
	var start = instructions.indexOf(startInstruction);
	var end = instructions.indexOf(endInstruction);
	for (var i = start; i < end; ++i) {
		instructions.remove(instructions.get(start));
	}
}

function start(name) {
	log("Starting " + name);
	currentlyRunning.push(name);
}

function finish() {
	var name = currentlyRunning.pop();
	log("Finished " + name);
}

function log(msg) {
	var str = "[" + transformerName + "]";
	for (var i in currentlyRunning) {
		str += " [" + currentlyRunning[i] + "]";
	}
	print(str + ": " + msg);
}

function clinit() {
	currentlyRunning = [];

	/*Class/Interface*/ Opcodes = Java.type('org.objectweb.asm.Opcodes');
	/*Class*/ ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

	/*Class*/ InsnList = Java.type('org.objectweb.asm.tree.InsnList');
	/*Class*/ LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');

	/*Class*/ FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');
	/*Class*/ MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');

	/*Class*/ AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
	/*Class*/ InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
	/*Class*/ VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
	/*Class*/ FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
	/*Class*/ MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
	/*Class*/ JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
	/*Class*/ TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
}
