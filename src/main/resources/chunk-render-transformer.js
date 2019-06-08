var transformerName = "NoCubes ChunkRender Transformer";
var targetClass = "net.minecraft.client.renderer.chunk.ChunkRender";
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
	// rebuildChunk
	new TargetMethod("func_178581_b", "(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V",
		new MethodTransformer(injectPreIterationHook, "injectPreIterationHook"),
		new MethodTransformer(injectBlockRenderHook, "injectBlockRenderHook")
	)
];
finish();

finish();


// Finds the first instruction INVOKESTATIC BlockPos.getAllInBoxMutable
// then finds the previous label
// and inserts after the label and before the label's instructions.
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
		if (instruction.getOpcode() == Opcodes.INVOKESTATIC) {
			if (instruction.owner == "net/minecraft/util/math/BlockPos") {
				if (instruction.name == getAllInBoxMutable_name) {
					if (instruction.desc == "(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;") {
						if (instruction.itf == false) {
							first_INVOKESTATIC_getAllInBoxMutable = instruction;
							log("Found injection point " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!first_INVOKESTATIC_getAllInBoxMutable) {
		throw "Error: Couldn't find injection point!";
	}

	var firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable;
	for (i = instructions.indexOf(first_INVOKESTATIC_getAllInBoxMutable); i >=0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == AbstractInsnNode.LABEL) {
			firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable = instruction;
			log("Found label " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_first_INVOKESTATIC_getAllInBoxMutable) {
		throw "Error: Couldn't find label!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	toInject.add(new VarInsnNode(Opcodes.FLOAD, 1)); // x
	toInject.add(new VarInsnNode(Opcodes.FLOAD, 2)); // t
	toInject.add(new VarInsnNode(Opcodes.FLOAD, 3)); // z
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 4)); // generator
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 5)); // compiledchunk
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 7)); // blockpos - startPosition
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 8)); // blockpos1 - endPosition
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 9)); // world
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 10)); // lvt_10_1_ - visGraph
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 11)); // lvt_11_1_ - hashSetTileEntitiesWithGlobalRenderers
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 12)); // lvt_12_1_ - chunkRenderCache
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 13)); // aboolean - usedRenderLayers
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 14)); // random
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 15)); // blockrendererdispatcher
	toInject.add(new MethodInsnNode(
			//int opcode
			Opcodes.INVOKESTATIC,
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
//	if (!io.github.cadiboo.nocubes.config.Config.renderSmoothTerrain || !iblockstate.nocubes_isTerrainSmoothable())
//	if (!io.github.cadiboo.nocubes.config.Config.renderSmoothLeaves || !iblockstate.nocubes_isLeavesSmoothable())
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

//    INVOKEVIRTUAL net/minecraft/client/renderer/BlockRendererDispatcher.renderFluid (Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IWorldReader;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/fluid/IFluidState;)Z
//    IOR
//    BASTORE
//   L55
//    LINENUMBER 179 L55
//   FRAME CHOP 2
//    GETSTATIC io/github/cadiboo/nocubes/config/Config.renderSmoothTerrain : Z
//    IFEQ L62
//    ALOAD 18
//    INVOKEINTERFACE net/minecraft/block/state/IBlockState.nocubes_isTerrainSmoothable ()Z (itf)
//    IFNE L63
//   L62
//    LINENUMBER 180 L62
//   FRAME SAME
//    GETSTATIC io/github/cadiboo/nocubes/config/Config.renderSmoothLeaves : Z
//    IFEQ L64
//    ALOAD 18
//    INVOKEINTERFACE net/minecraft/block/state/IBlockState.nocubes_isLeavesSmoothable ()Z (itf)
//    IFNE L63
//   L64
//    LINENUMBER 181 L64
//   FRAME SAME
//    ALOAD 18
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getRenderType ()Lnet/minecraft/block/BlockRenderType;
//    GETSTATIC net/minecraft/block/BlockRenderType.INVISIBLE : Lnet/minecraft/block/BlockRenderType;
//    IF_ACMPEQ L62
//    ALOAD 18
//    ALOAD 25
//    INVOKEVIRTUAL net/minecraft/block/BlockState.canRenderInLayer (Lnet/minecraft/util/BlockRenderLayer;)Z
//    IFEQ L63

	var blockCannotRenderLabel;

	var getRenderType_name = ASMAPI.mapMethod("func_185901_i"); // getRenderType

	var BlockState_getRenderType;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/block/BlockState") {
				if (instruction.name == getRenderType_name) {
					if (instruction.desc == "()Lnet/minecraft/block/BlockRenderType;") {
						if (instruction.itf == false) {
							BlockState_getRenderType = instruction;
							log("Found injection point " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!BlockState_getRenderType) {
		throw "Error: Couldn't find injection point!";
	}

	var firstLabelBefore_BlockState_getRenderType;
	for (i = instructions.indexOf(BlockState_getRenderType); i >=0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == AbstractInsnNode.LABEL) {
			firstLabelBefore_BlockState_getRenderType = instruction;
			log("Found label " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_BlockState_getRenderType) {
		throw "Error: Couldn't find label!";
	}

	var lookStart = instructions.indexOf(BlockState_getRenderType);
	var lookMax = lookStart + 10;
	for (var i = lookStart; i < lookMax; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == Opcodes.IFEQ || instruction.getOpcode() == Opcodes.IFNE || instruction.getOpcode() == Opcodes.IF_ACMPEQ) {
			blockCannotRenderLabel = instruction.label;
			log("Found blockCannotRenderLabel " + instruction.label);
			break;
		}
	}
	if (!blockCannotRenderLabel) {
		throw "Error: Couldn't find blockCannotRenderLabel!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();
	var renderSmoothLeavesChecksLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothTerrain", "Z"));
	toInject.add(new JumpInsnNode(Opcodes.IFEQ, renderSmoothLeavesChecksLabel));
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 18)); // blockstate
	toInject.add(new MethodInsnNode(
			//int opcode
			Opcodes.INVOKEVIRTUAL,
			//String owner
			"net/minecraft/block/BlockState",
			//String name
			"nocubes_isTerrainSmoothable",
			//String descriptor
			"()Z",
			//boolean isInterface
			false
	));
	toInject.add(new JumpInsnNode(Opcodes.IFNE, blockCannotRenderLabel));

	toInject.add(renderSmoothLeavesChecksLabel);
	toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothLeaves", "Z"));
	toInject.add(new JumpInsnNode(Opcodes.IFEQ, originalInstructionsLabel));
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 18)); // blockstate
	toInject.add(new MethodInsnNode(
			//int opcode
			Opcodes.INVOKEVIRTUAL,
			//String owner
			"net/minecraft/block/BlockState",
			//String name
			"nocubes_isLeavesSmoothable",
			//String descriptor
			"()Z",
			//boolean isInterface
			false
	));
	toInject.add(new JumpInsnNode(Opcodes.IFNE, blockCannotRenderLabel));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insert(firstLabelBefore_BlockState_getRenderType, toInject);

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
