var transformerName = "NoCubes BlockFluidRenderer Transformer";

var isSRG;

function initializeCoreMod() {
	return {
		transformerName: {
			'target': {
				'type': 'CLASS',
				'name': 'net.minecraft.client.renderer.BlockFluidRenderer'
			},
			'transformer': function(classNode) {

				var methods = classNode.methods;

				for (var i in methods) {
					var method = methods[i];
					var methodName = method.name;

					var deobfNameEquals = "isAdjacentFluidSameAs".equals(methodName);
					var srgNameEquals = "func_209557_a".equals(methodName);

					if (!deobfNameEquals && !srgNameEquals) {
						log("Did not match method " + methodName);
						continue;
					}

					log("Matched method " + methodName);

					log(deobfNameEquals ? "Matched a deobfuscated name - we are in a DEOBFUSCATED/MCP-NAMED DEVELOPER Environment" : "Matched an SRG name - We are in an SRG-NAMED PRODUCTION Environment")

					isSRG = srgNameEquals;

					log("Modifying logic...");
					var hasFinished = false;
					try {
						start("modify_isAdjacentFluidSameAs")
						modify_isAdjacentFluidSameAs(method.instructions);
						finish();
						hasFinished = true;
					} finally {
						// Hacks because rethrowing an exception sets the linenumber to where it was re-thrown
						if(!hasFinished) {
							var name = currentlyRunning;
							currentlyRunning = undefined;
							log("Caught exception from " + name);
						}
					}
					log("Successfully modified logic!");
					break;

				}

				return classNode;
			}
		}
	}
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
//var/*Class*/ InsnList = Java.type('org.objectweb.asm.tree.InsnList');

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

// Local variable indexes
var ALOCALVARIABLE_this = 0;
var ALOCALVARIABLE_blockpos = 4;
var ALOCALVARIABLE_fluidstate = 5;
var ALOCALVARIABLE_blockstate = 6;










function modify_isAdjacentFluidSameAs(instructions) {

	var first_INVOKEVIRTUAL_offset;
	var arrayLength = instructions.size();
	for (var i = 0; i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getOpcode() == INVOKEVIRTUAL) {
			if (instruction.owner == "net/minecraft/util/math/BlockPos") {
				//CPW PLS GIVE ME A WAY TO REMAP SRG TO NAMES FOR DEV
				if (instruction.name == "func_177972_a" || instruction.name == "offset") {
					if (instruction.desc == "(Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;") {
						if (instruction.itf == false) {
							first_INVOKEVIRTUAL_offset = instruction;
							log("Found injection point " + instruction);
							break;
						}
					}
				}
			}
		}
	}
	if (!first_INVOKEVIRTUAL_offset) {
		throw "Error: Couldn't find injection point!";
	}

	var firstLabelAfter_first_INVOKEVIRTUAL_offset;
	for (i = instructions.indexOf(first_INVOKEVIRTUAL_offset); i < arrayLength; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == AbstractInsnNode.LABEL) {
			firstLabelAfter_first_INVOKEVIRTUAL_offset = instruction;
			log("Found label " + instruction);
			break;
		}
	}
	if (!firstLabelAfter_first_INVOKEVIRTUAL_offset) {
		throw "Error: Couldn't find label!";
	}

	//FFS why
	var toInject = ASMAPI.getMethodNode().instructions;

	// Labels n stuff
	var originalInstructionsLabel = new LabelNode();
	var executeOverrideLabel = new LabelNode();

//	BlockPos blockpos = pos.offset(side);
//	IFluidState ifluidstate = worldIn.getFluidState(blockpos);
//
//	BlockPos blockpos = pos.offset(side);
//	if (NoCubes.isEnabled()) {
//		final IBlockState blockState = worldIn.getBlockState(blockpos);
//		if (blockState.nocubes_isTerrainSmoothable() || blockState.nocubes_isLeavesSmoothable()) {
//			return !worldIn.getBlockState(blockpos.up()).isSolid();
//		}
//	}
//	IFluidState ifluidstate = worldIn.getFluidState(blockpos);


//   L0
//    LINENUMBER 47 L0
//    ALOAD 1
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.offset (Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;
//    ASTORE 4
//   L1
//    LINENUMBER 48 L1
//    ALOAD 0
//    ALOAD 4
//    INVOKEINTERFACE net/minecraft/world/IBlockReader.getFluidState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState; (itf)
//    ASTORE 5

//   L0
//    LINENUMBER 46 L0
//    ALOAD 1
//    ALOAD 2
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.offset (Lnet/minecraft/util/EnumFacing;)Lnet/minecraft/util/math/BlockPos;
//    ASTORE 4
//   L1
//    LINENUMBER 48 L1
//    INVOKESTATIC io/github/cadiboo/nocubes/NoCubes.isEnabled ()Z
//    IFEQ L2
//   L3
//    LINENUMBER 49 L3
//    ALOAD 0
//    ALOAD 4
//    INVOKEINTERFACE net/minecraft/world/IBlockReader.getBlockState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState; (itf)
//    ASTORE 5
//   L4
//    LINENUMBER 50 L4
//    ALOAD 5
//    INVOKEINTERFACE net/minecraft/block/state/IBlockState.nocubes_isTerrainSmoothable ()Z (itf)
//    IFNE L5
//    ALOAD 5
//    INVOKEINTERFACE net/minecraft/block/state/IBlockState.nocubes_isLeavesSmoothable ()Z (itf)
//    IFEQ L2
//   L5
//    LINENUMBER 51 L5
//   FRAME APPEND [net/minecraft/util/math/BlockPos net/minecraft/block/state/IBlockState]
//    ALOAD 0
//    ALOAD 4
//    INVOKEVIRTUAL net/minecraft/util/math/BlockPos.up ()Lnet/minecraft/util/math/BlockPos;
//    INVOKEINTERFACE net/minecraft/world/IBlockReader.getBlockState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState; (itf)
//    INVOKEINTERFACE net/minecraft/block/state/IBlockState.isSolid ()Z (itf)
//    IFNE L6
//    ICONST_1
//    GOTO L7
//   L6
//   FRAME SAME1 I
//    IRETURN
//   L2
//    LINENUMBER 55 L2
//   FRAME CHOP 1
//    ALOAD 0
//    ALOAD 4
//    INVOKEINTERFACE net/minecraft/world/IBlockReader.getFluidState (Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/fluid/IFluidState; (itf)
//    ASTORE 5

	// Make list of instructions to inject
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKESTATIC,
			//String owner
			"io/github/cadiboo/nocubes/NoCubes",
			//String name
			"isEnabled",
			//String descriptor
			"()Z",
			//boolean isInterface
			false
	));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));

	toInject.add(new LabelNode());
	toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
	toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_blockpos));
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEINTERFACE,
			//String owner
			"net/minecraft/world/IBlockReader",
			//String name
			isSRG ? "func_180495_p" : "getBlockState",
			//String descriptor
			"(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
			//boolean isInterface
			true
	));
	toInject.add(new VarInsnNode(ASTORE, ALOCALVARIABLE_blockstate));

	toInject.add(new LabelNode());
	toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_blockstate));
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEINTERFACE,
			//String owner
			"net/minecraft/block/state/IBlockState",
			//String name
			"nocubes_isTerrainSmoothable",
			//String descriptor
			"()Z",
			//boolean isInterface
			true
	));
	toInject.add(new JumpInsnNode(IFNE, executeOverrideLabel));

	toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_blockstate));
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEINTERFACE,
			//String owner
			"net/minecraft/block/state/IBlockState",
			//String name
			"nocubes_isLeavesSmoothable",
			//String descriptor
			"()Z",
			//boolean isInterface
			true
	));
	toInject.add(new JumpInsnNode(IFEQ, originalInstructionsLabel));

	toInject.add(executeOverrideLabel);
	toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
	toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_blockpos));
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEVIRTUAL,
			//String owner
			"net/minecraft/util/math/BlockPos",
			//String name
			isSRG ? "func_177984_a" : "up",
			//String descriptor
			"()Lnet/minecraft/util/math/BlockPos;",
			//boolean isInterface
			false
	));
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEINTERFACE,
			//String owner
			"net/minecraft/world/IBlockReader",
			//String name
			isSRG ? "func_180495_p" : "getBlockState",
			//String descriptor
			"(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/state/IBlockState;",
			//boolean isInterface
			true
	));
	toInject.add(new MethodInsnNode(
			//int opcode
			INVOKEINTERFACE,
			//String owner
			"net/minecraft/block/state/IBlockState",
			//String name
			isSRG ? "func_200132_m" : "isSolid",
			//String descriptor
			"()Z",
			//boolean isInterface
			true
	));
	var returnTrueLabel = new LabelNode();
	toInject.add(new JumpInsnNode(IFEQ, returnTrueLabel));

	toInject.add(new LabelNode());
	toInject.add(new InsnNode(ICONST_0));
	toInject.add(new InsnNode(IRETURN));

	toInject.add(returnTrueLabel);
	toInject.add(new InsnNode(ICONST_1));
	toInject.add(new InsnNode(IRETURN));

	toInject.add(originalInstructionsLabel);

	// Inject instructions
	instructions.insertBefore(firstLabelAfter_first_INVOKEVIRTUAL_offset, toInject);

}
