var transformerName = "NoCubes BlockState Transformer";
var targetClass = "net.minecraft.block.BlockState";
clinit();
start("Initialisation");

start("fieldsToAdd");
// Params: int access, String name, String descriptor, String signature, Object value
var fieldsToAdd = [
	new FieldNode(Opcodes.ACC_PUBLIC, "nocubes_isTerrainSmoothable", "Z", null, false),
	new FieldNode(Opcodes.ACC_PUBLIC, "nocubes_isLeavesSmoothable", "Z", null, false),
];
finish();

start("methodsToAdd");
var methodsToAdd = [
	make_nocubes_isTerrainSmoothable(),
	make_nocubes_setTerrainSmoothable(),
	make_nocubes_isLeavesSmoothable(),
	make_nocubes_setLeavesSmoothable(),
];
finish();

start("targetMethods");
var targetMethods = [
	// isSolid
	new TargetMethod("func_200132_m", "()Z",
		new MethodTransformer(injectIsSolidHook, "injectIsSolidHook")
	),
	// causesSuffocation
	new TargetMethod("func_215696_m", "(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;)Z",
		new MethodTransformer(injectCausesSuffocationHook, "injectCausesSuffocationHook")
	)
];
finish();

finish();


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
		Opcodes.ACC_PUBLIC,
//		final String name,
		"nocubes_isTerrainSmoothable",
//		final String descriptor,
		"()Z",
//		final String signature,
		null,
//		final String[] exceptions
		null
	);
	method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/block/BlockState", "nocubes_isTerrainSmoothable", "Z"));
	method.instructions.add(new InsnNode(Opcodes.IRETURN));

	return method;

}

function make_nocubes_setTerrainSmoothable() {

	var method = new MethodNode(
//		final int access,
		Opcodes.ACC_PUBLIC,
//		final String name,
		"nocubes_setTerrainSmoothable",
//		final String descriptor,
		"(Z)V",
//		final String signature,
		null,
//		final String[] exceptions
		null
	);
	instructions = method.instructions;
	instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	instructions.add(new VarInsnNode(Opcodes.ILOAD, 1)); // newIsTerrainSmoothable
	instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/block/BlockState", "nocubes_isTerrainSmoothable", "Z"));
	instructions.add(new InsnNode(Opcodes.RETURN));

	return method;

}

function make_nocubes_isLeavesSmoothable() {

	var method = new MethodNode(
//		final int access,
		Opcodes.ACC_PUBLIC,
//		final String name,
		"nocubes_isLeavesSmoothable",
//		final String descriptor,
		"()Z",
//		final String signature,
		null,
//		final String[] exceptions
		null
	);
	method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	method.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/block/BlockState", "nocubes_isLeavesSmoothable", "Z"));
	method.instructions.add(new InsnNode(Opcodes.IRETURN));

	return method;

}

function make_nocubes_setLeavesSmoothable() {

	var method = new MethodNode(
//		final int access,
		Opcodes.ACC_PUBLIC,
//		final String name,
		"nocubes_setLeavesSmoothable",
//		final String descriptor,
		"(Z)V",
//		final String signature,
		null,
//		final String[] exceptions
		null
	);
	method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
	method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 1)); // newIsLeavesSmoothable
	method.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/block/BlockState", "nocubes_isLeavesSmoothable", "Z"));
	method.instructions.add(new InsnNode(Opcodes.RETURN));

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
	var originalInstructionsLabel = new LabelNode();
	var leavesChecksLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothTerrain", "Z"));
	toInject.add(new JumpInsnNode(Opcodes.IFEQ, leavesChecksLabel));
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
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
	toInject.add(new JumpInsnNode(Opcodes.IFEQ, leavesChecksLabel));
	toInject.add(new InsnNode(Opcodes.ICONST_0));
	toInject.add(new InsnNode(Opcodes.IRETURN));
	toInject.add(leavesChecksLabel);
	toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "renderSmoothLeaves", "Z"));
	toInject.add(new JumpInsnNode(Opcodes.IFEQ, originalInstructionsLabel));
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
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
	toInject.add(new JumpInsnNode(Opcodes.IFEQ, originalInstructionsLabel));
	toInject.add(new InsnNode(Opcodes.ICONST_0));
	toInject.add(new InsnNode(Opcodes.IRETURN));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insert(firstLabel, toInject);

}

// 1) Find first label
// 2) inject right after first label
function injectCausesSuffocationHook(instructions) {

//	return this.getBlock().causesSuffocation(this);

//	// NoCubes Start
//	if (io.github.cadiboo.nocubes.config.Config.terrainCollisions && this.nocubes_isTerrainSmoothable()) return false;
//	// NoCubes End
//	return this.getBlock().causesSuffocation(this);


//  public default causesSuffocation()Z
//   L0
//    LINENUMBER 321 L0
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getBlock ()Lnet/minecraft/block/Block;
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/Block.causesSuffocation (Lnet/minecraft/block/BlockState;)Z
//    IRETURN

//  public default causesSuffocation()Z
//   L0
//    LINENUMBER 325 L0
//    GETSTATIC io/github/cadiboo/nocubes/config/Config.terrainCollisions : Z
//    IFEQ L1
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/BlockState.nocubes_isTerrainSmoothable ()Z
//    IFEQ L1
//    ICONST_0
//    IRETURN
//   L1
//    LINENUMBER 327 L1
//   FRAME SAME
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/BlockState.getBlock ()Lnet/minecraft/block/Block;
//    ALOAD 0
//    INVOKEVIRTUAL net/minecraft/block/Block.causesSuffocation (Lnet/minecraft/block/BlockState;)Z
//    IRETURN


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
	var originalInstructionsLabel = new LabelNode();

	// Make list of instructions to inject
	toInject.add(new FieldInsnNode(Opcodes.GETSTATIC, "io/github/cadiboo/nocubes/config/Config", "terrainCollisions", "Z"));
	toInject.add(new JumpInsnNode(Opcodes.IFEQ, originalInstructionsLabel));
	toInject.add(new VarInsnNode(Opcodes.ALOAD, 0)); // this
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
	toInject.add(new JumpInsnNode(Opcodes.IFEQ, originalInstructionsLabel));
	toInject.add(new InsnNode(Opcodes.ICONST_0));
	toInject.add(new InsnNode(Opcodes.IRETURN));

	toInject.add(originalInstructionsLabel);

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
