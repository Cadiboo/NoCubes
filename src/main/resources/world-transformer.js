var transformerName = "NoCubes World Transformer";
var targetClass = "net.minecraft.world.World";
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
 	// getFluidState
	new TargetMethod(
		"func_204610_c", "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;",
		new MethodTransformer(injectGetFluidStateHook, "injectGetFluidStateHook")
	)
];
finish();

finish();


// Finds the first instruction INVOKEVIRTUAL World.getChunk
// Finds the next instruction ARETURN
// Inserts before World.getChunk
// removes everything between World.getChunk and ARETURN
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
		if (instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/world/World") {
				if (instruction.name == getChunkAt_name) {
					if (instruction.desc == "(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/world/chunk/Chunk;") {
						if (instruction.itf == false) {
							first_INVOKEVIRTUAL_World_getChunkAt = instruction;
							log("Found injection point " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!first_INVOKEVIRTUAL_World_getChunkAt) {
		throw "Error: Couldn't find injection point!";
	}

	var next_ARETURN;
//	var arrayLength = instructions.size();
	for (var i = instructions.indexOf(first_INVOKEVIRTUAL_World_getChunkAt); i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == Opcodes.ARETURN) {
			next_ARETURN = instruction;
			log("Found injection point " + instruction);
			break;
		}
	}
	if (!next_ARETURN) {
		throw "Error: Couldn't find injection point!";
	}

	var toInject = new InsnList();

	// Labels n stuff

	// Make list of instructions to inject
	toInject.add(new MethodInsnNode(
			//int opcode
			Opcodes.INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"getFluidState",
			//String descriptor
			"(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(Opcodes.ARETURN));

	// Inject instructions
	instructions.insertBefore(first_INVOKEVIRTUAL_World_getChunkAt, toInject);

	removeBetweenInclusive(instructions, first_INVOKEVIRTUAL_World_getChunkAt, next_ARETURN);

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
