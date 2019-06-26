var transformerName = "NoCubes IWorldReader Transformer";
var targetClass = "net.minecraft.world.IWorldReader";
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
 	// getCollisionBoxes
	new TargetMethod(
		"func_223438_b", "(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;)Ljava/util/stream/Stream;",
		new MethodTransformer(injectGetCollisionShapesHook, "injectGetCollisionShapesHook")
	)
];
finish();

finish();


// Finds the first instruction NEW CubeCoordinateIterator
// then finds the previous label
// and inserts after that label and before the label's instructions.
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
		if (instruction.getOpcode() == Opcodes.NEW) {
			if (instruction.desc == "net/minecraft/util/math/CubeCoordinateIterator") {
				first_NEW_CubeCoordinateIterator = instruction;
				log("Found injection point " + instruction);
				break;
			}
		}
	}
	if (!first_NEW_CubeCoordinateIterator) {
		throw "Error: Couldn't find injection point!";
	}

	var firstLabelBefore_first_NEW_CubeCoordinateIterator;
	for (i = instructions.indexOf(first_NEW_CubeCoordinateIterator); i > 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == AbstractInsnNode.LABEL) {
			firstLabelBefore_first_NEW_CubeCoordinateIterator = instruction;
			log("Found label " + instruction);
			break;
		}
	}
	if (!firstLabelBefore_first_NEW_CubeCoordinateIterator) {
		throw "Error: Couldn't find label!";
	}

	var toInject = new InsnList();

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 1)); // entity
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 2)); // aabb
	toInject.add(new VarInsnNode(Opcodes.ILOAD, 3)); // i minXm1
	toInject.add(new VarInsnNode(Opcodes.ILOAD, 4)); // j maxXp1
	toInject.add(new VarInsnNode(Opcodes.ILOAD, 5)); // k minYm1
	toInject.add(new VarInsnNode(Opcodes.ILOAD, 6)); // l maxYp1
	toInject.add(new VarInsnNode(Opcodes.ILOAD, 7)); // i1 minZm1
	toInject.add(new VarInsnNode(Opcodes.ILOAD, 8)); // j1 maxZp1
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 9)); // iselectioncontext
	toInject.add(new MethodInsnNode(
			//int opcode
			Opcodes.INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/hooks/Hooks",
			//String name
			"getCollisionShapes",
			//String descriptor
			"(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;IIIIIILnet/minecraft/util/math/shapes/ISelectionContext;)Ljava/util/stream/Stream;",
			//boolean isInterface
			false
	));
	toInject.add(new InsnNode(Opcodes.ARETURN));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insertBefore(firstLabelBefore_first_NEW_CubeCoordinateIterator, toInject);

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
