var transformerName = "NoCubes World Transformer";
var targetClass = "net.minecraft.world.World";
var targetMethods = [
	new TargetMethod(
		"func_204610_c", // getFluidState
		"(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState;",
		new MethodTransformer(injectGetFluidStateHook, "injectGetFluidStateHook")
	)
];


// Local variable indexes
var ALOCALVARIABLE_this = 0;
var ALOCALVARIABLE_movingEntity = 1;
var ALOCALVARIABLE_area = 2;
var ALOCALVARIABLE_entityShape = 3;
var ILOCALVARIABLE_flag1 = 6;


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


	var getChunkAt_name = mapMethod("func_175726_f"); // getChunkAt

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
		if (instruction.getOpcode() == ARETURN) {
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




















function initializeCoreMod() {
	return {
		transformerName: {
			'target': {
				'type': 'CLASS',
				'name': targetClass
			},
			'transformer': function(classNode) {

				var methods = classNode.methods;

				var hasFinished = false;
				try {
					for (var i in targetMethods) {

						var targetMethod = targetMethods[i];
						var targetMethodName = mapMethod(targetMethod.name);
						var targetMethodDesc = targetMethod.desc;
						var methodTransformers = targetMethod.transformers;

						start("Find " + targetMethodName);
						for (var j in methods) {
							var method = methods[j];

							if (!method.name.equals(targetMethodName)) {
								log("Did not match method name " + targetMethodName + " - " + method.name);
								continue;
							} else if (!method.desc.equals(targetMethodDesc)) {
								log("Did not match method desc " + targetMethodDesc + " - " + method.desc);
								continue;
							}
							log("Matched method " + method.name + " " + method.desc);

							finish();

							for (var k in methodTransformers) {
								var methodTransformer = methodTransformers[k];
								start("Apply " + methodTransformer.name);
								methodTransformer.func(method.instructions);
								finish();
							}
							break;
						}

					}
					hasFinished = true;
				} finally {
					// Hacks because rethrowing an exception sets the linenumber to where it was re-thrown
					if(!hasFinished) {
						var name = currentlyRunning;
						currentlyRunning = undefined;
						log("Caught exception from " + name);
					}
				}

				return classNode;
			}
		}
	}
}

function TargetMethod(name, desc, transformer1, transformer2, transformer3) { //Var args seems not to work :/
	this.name = name;
	this.desc = desc;
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

var currentlyRunning;

function start(name) {
	log("Starting " + name);
	currentlyRunning = name;
}

function finish() {
	var name = currentlyRunning;
	currentlyRunning = undefined;
	log("Finished " + name);
}

function log(msg) {
	if (currentlyRunning == undefined) {
		print("["+transformerName+"]: " + msg);
	} else {
		print("["+transformerName+"] [" + currentlyRunning + "]: " + msg);
	}
}

function mapMethod(srgName) {
	return ASMAPI.mapMethod(srgName);
}

function mapField(srgName) {
	return ASMAPI.mapField(srgName);
}

var/*Class/Interface*/ Opcodes = Java.type('org.objectweb.asm.Opcodes');
var/*Class*/ MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var/*Class*/ MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var/*Class*/ InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var/*Class*/ VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var/*Class*/ AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
var/*Class*/ JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var/*Class*/ LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var/*Class*/ TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var/*Class*/ FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var/*Class*/ FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');
var/*Class*/ InsnList = Java.type('org.objectweb.asm.tree.InsnList');

var/*Class*/ ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');

// Opcodes

// Access flags values, defined in
// - https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.1-200-E.1
// - https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.5-200-A.1
// - https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.6-200-A.1
// - https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.25
	var ACC_PUBLIC = Opcodes.ACC_PUBLIC; // class, field, method
	var ACC_PRIVATE = Opcodes.ACC_PRIVATE; // class, field, method
	var ACC_PROTECTED = Opcodes.ACC_PROTECTED; // class, field, method
	var ACC_STATIC = Opcodes.ACC_STATIC; // field, method
	var ACC_FINAL = Opcodes.ACC_FINAL; // class, field, method, parameter
	var ACC_SUPER = Opcodes.ACC_SUPER; // class
	var ACC_SYNCHRONIZED = Opcodes.ACC_SYNCHRONIZED; // method
	var ACC_OPEN = Opcodes.ACC_OPEN; // module
	var ACC_TRANSITIVE = Opcodes.ACC_TRANSITIVE; // module requires
	var ACC_VOLATILE = Opcodes.ACC_VOLATILE; // field
	var ACC_BRIDGE = Opcodes.ACC_BRIDGE; // method
	var ACC_STATIC_PHASE = Opcodes.ACC_STATIC_PHASE; // module requires
	var ACC_VARARGS = Opcodes.ACC_VARARGS; // method
	var ACC_TRANSIENT = Opcodes.ACC_TRANSIENT; // field
	var ACC_NATIVE = Opcodes.ACC_NATIVE; // method
	var ACC_INTERFACE = Opcodes.ACC_INTERFACE; // class
	var ACC_ABSTRACT = Opcodes.ACC_ABSTRACT; // class, method
	var ACC_STRICT = Opcodes.ACC_STRICT; // method
	var ACC_SYNTHETIC = Opcodes.ACC_SYNTHETIC; // class, field, method, parameter, module *
	var ACC_ANNOTATION = Opcodes.ACC_ANNOTATION; // class
	var ACC_ENUM = Opcodes.ACC_ENUM; // class(?) field inner
	var ACC_MANDATED = Opcodes.ACC_MANDATED; // parameter, module, module *
	var ACC_MODULE = Opcodes.ACC_MODULE; // class

// ASM specific access flags.
// WARNING: the 16 least significant bits must NOT be used, to avoid conflicts with standard
// access flags, and also to make sure that these flags are automatically filtered out when
// written in class files (because access flags are stored using 16 bits only).

	var ACC_DEPRECATED = Opcodes.ACC_DEPRECATED; // class, field, method

// Possible values for the type operand of the NEWARRAY instruction.
// See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html#jvms-6.5.newarray.

	var T_BOOLEAN = Opcodes.T_BOOLEAN;
	var T_CHAR = Opcodes.T_CHAR;
	var T_FLOAT = Opcodes.T_FLOAT;
	var T_DOUBLE = Opcodes.T_DOUBLE;
	var T_BYTE = Opcodes.T_BYTE;
	var T_SHORT = Opcodes.T_SHORT;
	var T_INT = Opcodes.T_INT;
	var T_LONG = Opcodes.T_LONG;

// Possible values for the reference_kind field of CONSTANT_MethodHandle_info structures.
// See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.4.8.

	var H_GETFIELD = Opcodes.H_GETFIELD;
	var H_GETSTATIC = Opcodes.H_GETSTATIC;
	var H_PUTFIELD = Opcodes.H_PUTFIELD;
	var H_PUTSTATIC = Opcodes.H_PUTSTATIC;
	var H_INVOKEVIRTUAL = Opcodes.H_INVOKEVIRTUAL;
	var H_INVOKESTATIC = Opcodes.H_INVOKESTATIC;
	var H_INVOKESPECIAL = Opcodes.H_INVOKESPECIAL;
	var H_NEWINVOKESPECIAL = Opcodes.H_NEWINVOKESPECIAL;
	var H_INVOKEINTERFACE = Opcodes.H_INVOKEINTERFACE;

// ASM specific stack map frame types, used in {@link ClassVisitor#visitFrame}.

	/** An expanded frame. See {@link ClassReader#EXPAND_FRAMES}. */
	var F_NEW = Opcodes.F_NEW;

	/** A compressed frame with complete frame data. */
	var F_FULL = Opcodes.F_FULL;

	/**
	* A compressed frame where locals are the same as the locals in the previous frame, except that
	* additional 1-3 locals are defined, and with an empty stack.
	*/
	var F_APPEND = Opcodes.F_APPEND;

	/**
	* A compressed frame where locals are the same as the locals in the previous frame, except that
	* the last 1-3 locals are absent and with an empty stack.
	*/
	var F_CHOP = Opcodes.F_CHOP;

	/**
	* A compressed frame with exactly the same locals as the previous frame and with an empty stack.
	*/
	var F_SAME = Opcodes.F_SAME;

	/**
	* A compressed frame with exactly the same locals as the previous frame and with a single value
	* on the stack.
	*/
	var F_SAME1 = Opcodes.F_SAME1;

// The JVM opcode values (with the MethodVisitor method name used to visit them in comment, and
// where '-' means 'same method name as on the previous line').
// See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-6.html.

	var NOP = Opcodes.NOP; // visitInsn
	var ACONST_NULL = Opcodes.ACONST_NULL; // -
	var ICONST_M1 = Opcodes.ICONST_M1; // -
	var ICONST_0 = Opcodes.ICONST_0; // -
	var ICONST_1 = Opcodes.ICONST_1; // -
	var ICONST_2 = Opcodes.ICONST_2; // -
	var ICONST_3 = Opcodes.ICONST_3; // -
	var ICONST_4 = Opcodes.ICONST_4; // -
	var ICONST_5 = Opcodes.ICONST_5; // -
	var LCONST_0 = Opcodes.LCONST_0; // -
	var LCONST_1 = Opcodes.LCONST_1; // -
	var FCONST_0 = Opcodes.FCONST_0; // -
	var FCONST_1 = Opcodes.FCONST_1; // -
	var FCONST_2 = Opcodes.FCONST_2; // -
	var DCONST_0 = Opcodes.DCONST_0; // -
	var DCONST_1 = Opcodes.DCONST_1; // -
	var BIPUSH = Opcodes.BIPUSH; // visitIntInsn
	var SIPUSH = Opcodes.SIPUSH; // -
	var LDC = Opcodes.LDC; // visitLdcInsn
	var ILOAD = Opcodes.ILOAD; // visitVarInsn
	var LLOAD = Opcodes.LLOAD; // -
	var FLOAD = Opcodes.FLOAD; // -
	var DLOAD = Opcodes.DLOAD; // -
	var ALOAD = Opcodes.ALOAD; // -
	var IALOAD = Opcodes.IALOAD; // visitInsn
	var LALOAD = Opcodes.LALOAD; // -
	var FALOAD = Opcodes.FALOAD; // -
	var DALOAD = Opcodes.DALOAD; // -
	var AALOAD = Opcodes.AALOAD; // -
	var BALOAD = Opcodes.BALOAD; // -
	var CALOAD = Opcodes.CALOAD; // -
	var SALOAD = Opcodes.SALOAD; // -
	var ISTORE = Opcodes.ISTORE; // visitVarInsn
	var LSTORE = Opcodes.LSTORE; // -
	var FSTORE = Opcodes.FSTORE; // -
	var DSTORE = Opcodes.DSTORE; // -
	var ASTORE = Opcodes.ASTORE; // -
	var IASTORE = Opcodes.IASTORE; // visitInsn
	var LASTORE = Opcodes.LASTORE; // -
	var FASTORE = Opcodes.FASTORE; // -
	var DASTORE = Opcodes.DASTORE; // -
	var AASTORE = Opcodes.AASTORE; // -
	var BASTORE = Opcodes.BASTORE; // -
	var CASTORE = Opcodes.CASTORE; // -
	var SASTORE = Opcodes.SASTORE; // -
	var POP = Opcodes.POP; // -
	var POP2 = Opcodes.POP2; // -
	var DUP = Opcodes.DUP; // -
	var DUP_X1 = Opcodes.DUP_X1; // -
	var DUP_X2 = Opcodes.DUP_X2; // -
	var DUP2 = Opcodes.DUP2; // -
	var DUP2_X1 = Opcodes.DUP2_X1; // -
	var DUP2_X2 = Opcodes.DUP2_X2; // -
	var SWAP = Opcodes.SWAP; // -
	var IADD = Opcodes.IADD; // -
	var LADD = Opcodes.LADD; // -
	var FADD = Opcodes.FADD; // -
	var DADD = Opcodes.DADD; // -
	var ISUB = Opcodes.ISUB; // -
	var LSUB = Opcodes.LSUB; // -
	var FSUB = Opcodes.FSUB; // -
	var DSUB = Opcodes.DSUB; // -
	var IMUL = Opcodes.IMUL; // -
	var LMUL = Opcodes.LMUL; // -
	var FMUL = Opcodes.FMUL; // -
	var DMUL = Opcodes.DMUL; // -
	var IDIV = Opcodes.IDIV; // -
	var LDIV = Opcodes.LDIV; // -
	var FDIV = Opcodes.FDIV; // -
	var DDIV = Opcodes.DDIV; // -
	var IREM = Opcodes.IREM; // -
	var LREM = Opcodes.LREM; // -
	var FREM = Opcodes.FREM; // -
	var DREM = Opcodes.DREM; // -
	var INEG = Opcodes.INEG; // -
	var LNEG = Opcodes.LNEG; // -
	var FNEG = Opcodes.FNEG; // -
	var DNEG = Opcodes.DNEG; // -
	var ISHL = Opcodes.ISHL; // -
	var LSHL = Opcodes.LSHL; // -
	var ISHR = Opcodes.ISHR; // -
	var LSHR = Opcodes.LSHR; // -
	var IUSHR = Opcodes.IUSHR; // -
	var LUSHR = Opcodes.LUSHR; // -
	var IAND = Opcodes.IAND; // -
	var LAND = Opcodes.LAND; // -
	var IOR = Opcodes.IOR; // -
	var LOR = Opcodes.LOR; // -
	var IXOR = Opcodes.IXOR; // -
	var LXOR = Opcodes.LXOR; // -
	var IINC = Opcodes.IINC; // visitIincInsn
	var I2L = Opcodes.I2L; // visitInsn
	var I2F = Opcodes.I2F; // -
	var I2D = Opcodes.I2D; // -
	var L2I = Opcodes.L2I; // -
	var L2F = Opcodes.L2F; // -
	var L2D = Opcodes.L2D; // -
	var F2I = Opcodes.F2I; // -
	var F2L = Opcodes.F2L; // -
	var F2D = Opcodes.F2D; // -
	var D2I = Opcodes.D2I; // -
	var D2L = Opcodes.D2L; // -
	var D2F = Opcodes.D2F; // -
	var I2B = Opcodes.I2B; // -
	var I2C = Opcodes.I2C; // -
	var I2S = Opcodes.I2S; // -
	var LCMP = Opcodes.LCMP; // -
	var FCMPL = Opcodes.FCMPL; // -
	var FCMPG = Opcodes.FCMPG; // -
	var DCMPL = Opcodes.DCMPL; // -
	var DCMPG = Opcodes.DCMPG; // -
	var IFEQ = Opcodes.IFEQ; // visitJumpInsn
	var IFNE = Opcodes.IFNE; // -
	var IFLT = Opcodes.IFLT; // -
	var IFGE = Opcodes.IFGE; // -
	var IFGT = Opcodes.IFGT; // -
	var IFLE = Opcodes.IFLE; // -
	var IF_ICMPEQ = Opcodes.IF_ICMPEQ; // -
	var IF_ICMPNE = Opcodes.IF_ICMPNE; // -
	var IF_ICMPLT = Opcodes.IF_ICMPLT; // -
	var IF_ICMPGE = Opcodes.IF_ICMPGE; // -
	var IF_ICMPGT = Opcodes.IF_ICMPGT; // -
	var IF_ICMPLE = Opcodes.IF_ICMPLE; // -
	var IF_ACMPEQ = Opcodes.IF_ACMPEQ; // -
	var IF_ACMPNE = Opcodes.IF_ACMPNE; // -
	var GOTO = Opcodes.GOTO; // -
	var JSR = Opcodes.JSR; // -
	var RET = Opcodes.RET; // visitVarInsn
	var TABLESWITCH = Opcodes.TABLESWITCH; // visiTableSwitchInsn
	var LOOKUPSWITCH = Opcodes.LOOKUPSWITCH; // visitLookupSwitch
	var IRETURN = Opcodes.IRETURN; // visitInsn
	var LRETURN = Opcodes.LRETURN; // -
	var FRETURN = Opcodes.FRETURN; // -
	var DRETURN = Opcodes.DRETURN; // -
	var ARETURN = Opcodes.ARETURN; // -
	var RETURN = Opcodes.RETURN; // -
	var GETSTATIC = Opcodes.GETSTATIC; // visitFieldInsn
	var PUTSTATIC = Opcodes.PUTSTATIC; // -
	var GETFIELD = Opcodes.GETFIELD; // -
	var PUTFIELD = Opcodes.PUTFIELD; // -
	var INVOKEVIRTUAL = Opcodes.INVOKEVIRTUAL; // visitMethodInsn
	var INVOKESPECIAL = Opcodes.INVOKESPECIAL; // -
	var INVOKESTATIC = Opcodes.INVOKESTATIC; // -
	var INVOKEINTERFACE = Opcodes.INVOKEINTERFACE; // -
	var INVOKEDYNAMIC = Opcodes.INVOKEDYNAMIC; // visitInvokeDynamicInsn
	var NEW = Opcodes.NEW; // visitTypeInsn
	var NEWARRAY = Opcodes.NEWARRAY; // visitIntInsn
	var ANEWARRAY = Opcodes.ANEWARRAY; // visitTypeInsn
	var ARRAYLENGTH = Opcodes.ARRAYLENGTH; // visitInsn
	var ATHROW = Opcodes.ATHROW; // -
	var CHECKCAST = Opcodes.CHECKCAST; // visitTypeInsn
	var INSTANCEOF = Opcodes.INSTANCEOF; // -
	var MONITORENTER = Opcodes.MONITORENTER; // visitInsn
	var MONITOREXIT = Opcodes.MONITOREXIT; // -
	var MULTIANEWARRAY = Opcodes.MULTIANEWARRAY; // visitMultiANewArrayInsn
	var IFNULL = Opcodes.IFNULL; // visitJumpInsn
	var IFNONNULL = Opcodes.IFNONNULL; // -
