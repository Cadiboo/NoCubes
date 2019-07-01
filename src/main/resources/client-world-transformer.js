var transformerName = "NoCubes ClientWorld Transformer";
var targetClass = "net.minecraft.client.world.ClientWorld";
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
 	// markForRerender
	new TargetMethod(
		"func_217396_m", "(Lnet/minecraft/util/math/BlockPos;)V",
		new MethodTransformer(injectMarkForRerenderHook, "injectMarkForRerenderHook")
	)
];
finish();

finish();


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
		if (instruction.getType() == AbstractInsnNode.LABEL) {
			firstLabel = instruction;
			log("Found injection point " + instruction);
			break;
		}
	}
	if (!firstLabel) {
		throw "Error: Couldn't find injection point!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var worldRenderer_name = ASMAPI.mapField("field_217430_d"); // worldRenderer

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // pos
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	toInject.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/world/ClientWorld", worldRenderer_name, "Lnet/minecraft/client/renderer/WorldRenderer;"));
	toInject.add(new MethodInsnNode(
			//int opcode
			Opcodes.INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"markForRerender",
			//String descriptor
			"(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/WorldRenderer;)V",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(Opcodes.RETURN));

	// Inject instructions
	instructions.insert(firstLabel, toInject);

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
