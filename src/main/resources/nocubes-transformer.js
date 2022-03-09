/**
 * This function is called by Forge before any minecraft classes are loaded to setup the coremod.
 *
 * @return {object} All the transformers of this coremod.
 */
function initializeCoreMod() {

	// Classes
	Opcodes = Java.type('org.objectweb.asm.Opcodes');
	ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
	LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
	AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
	InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
	VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
	FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
	MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');

	// Opcodes
	INVOKESTATIC = Opcodes.INVOKESTATIC;
	ALOAD = Opcodes.ALOAD;
	ARETURN = Opcodes.ARETURN;
	GETSTATIC = Opcodes.GETSTATIC;
	GETFIELD = Opcodes.GETFIELD;

	// Instruction types
	LABEL = AbstractInsnNode.LABEL;
	METHOD_INSN = AbstractInsnNode.METHOD_INSN;

	return wrapWithLogging({
		// region Rendering
		// Hooks multiple parts of the chunk rendering method to allow us to do our own custom rendering
		'ChunkRender#rebuildChunk': {
			'target': {
				'type': 'METHOD',
				'class': 'net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender$RebuildTask',
				'methodName': 'func_228940_a_', // compile
				'methodDesc': '(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;)Ljava/util/Set;'
			},
			'transformer': function(methodNode) {
				var instructions = methodNode.instructions;
				var isOptiFinePresent = detectOptiFine(instructions);
				// OptiFine G8 added two booleans to the stack (shaders and shadersMidBlock)
				var ofg8 = isOptiFinePresent && !!tryFindFirstFieldInstruction(instructions, GETSTATIC, 'net/optifine/shaders/Shaders', 'useMidBlockAttrib', 'Z');

				// Inject the hook where we do our rendering
				// We inject right above where vanilla loops (iterates) through all the the blocks
				{
					var positionsIteratorCall = findFirstMethodCall(
						methodNode,
						ASMAPI.MethodType.STATIC,
						isOptiFinePresent ? 'net/optifine/BlockPosM' : 'net/minecraft/util/math/BlockPos',
						isOptiFinePresent ? 'getAllInBoxMutable' : ASMAPI.mapMethod('func_218278_a'), // BlockPos#betweenClosed
						'(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Ljava/lang/Iterable;'
					);
					var firstLabelBeforePositionsIteratorCall = findFirstLabelBefore(instructions, positionsIteratorCall);

					// I'm not sure if this is still necessary, but it works so I'm not touching it (I remember it was painful to get right)
					var outerClassFieldName = isOptiFinePresent ? 'this$1' : ASMAPI.mapField('field_228939_e_');
					instructions.insert(firstLabelBeforePositionsIteratorCall, ASMAPI.listOf(
						new VarInsnNode(ALOAD, 0), // this
						new VarInsnNode(ALOAD, 0), // ChunkRender.this
						new FieldInsnNode(GETFIELD, 'net/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender$RebuildTask', outerClassFieldName, 'Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender;'),
						new VarInsnNode(ALOAD, 4), // compiledChunkIn
						new VarInsnNode(ALOAD, 5), // builderIn
						new VarInsnNode(ALOAD, 7), // blockpos - startPosition
						new VarInsnNode(ALOAD, isOptiFinePresent ? 12 : 11), // chunkrendercache
						new VarInsnNode(ALOAD, isOptiFinePresent ? 11 : 12), // matrixstack
						new VarInsnNode(ALOAD, isOptiFinePresent ? (ofg8 ? 16 : 14) : 13), // random
						new VarInsnNode(ALOAD, isOptiFinePresent ? (ofg8 ? 17 : 15) : 14), // blockrendererdispatcher
						callNoCubesHook('preIteration', '(Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender$RebuildTask;Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$ChunkRender;Lnet/minecraft/client/renderer/chunk/ChunkRenderDispatcher$CompiledChunk;Lnet/minecraft/client/renderer/RegionRenderCacheBuilder;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/world/IBlockDisplayReader;Lcom/mojang/blaze3d/matrix/MatrixStack;Ljava/util/Random;Lnet/minecraft/client/renderer/BlockRendererDispatcher;)V'),
						new LabelNode() // Label for original instructions
					));
					print('Done injecting the preIteration hook');
				}

				// OptiFine optimises 'chunkRenderCache.getFluidState(pos)' to 'state.getFluidState()'
				// This breaks our extended fluids rendering so we undo it
				if (isOptiFinePresent) {
					var getFluidStateCall = findFirstMethodCall(
						methodNode,
						ASMAPI.MethodType.VIRTUAL,
						'net/minecraft/block/BlockState',
						ASMAPI.mapMethod('func_204520_s'), // getFluidState
						'()Lnet/minecraft/fluid/FluidState;'
					);
					var previousLabel = findFirstLabelBefore(instructions, getFluidStateCall);
					removeBetweenIndicesInclusive(instructions, instructions.indexOf(previousLabel) + 1, instructions.indexOf(getFluidStateCall));
					instructions.insert(previousLabel, ASMAPI.listOf(
						new VarInsnNode(ALOAD, ofg8 ? 19 : 17), // pos
						new VarInsnNode(ALOAD, ofg8 ? 20 : 18), // state
						callNoCubesHook('getRenderFluidStateOptiFine', '(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Lnet/minecraft/fluid/FluidState;')
					));
					// We didn't remove the ASTORE instruction with our 'removeBetweenIndicesInclusive' so the result of our hook call automatically gets stored
					print('Done injecting the fluid render bypass hook');
				}
				return methodNode;
			}
		},
		// endregion Rendering
	});
}










// Utility functions

function assertInstructionFound(instruction, name, instructions) {
	if (!instruction)
		throw "Error: Couldn't find '" + name + "' in instructions:\n" + stringifyInstructions(instructions);
}

function findFirstLabelBefore(instructions, start) {
	return findFirstLabelBeforeIndex(instructions, instructions.indexOf(start));
}

function findFirstLabelBeforeIndex(instructions, startIndex) {
	var length = instructions.size();
	if (!startIndex)
		startIndex = length - 1;
	var i;
	for (i = startIndex; i >= 0; --i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == LABEL) {
			print('Found first label before index ' + startIndex + ': ' + instruction);
			return instruction;
		}
	}
	throw "Error: Couldn't find first label before index " + startIndex + ' in ' + stringifyInstructions(instructions);
}

function tryFindFirstFieldInstruction(instructions, opcode, owner, name, desc) {
	for (var i = 0, length = instructions.size(); i < length; ++i) {
		var instruction = instructions.get(i);
		if (instruction.opcode != opcode || instruction.owner != owner || instruction.name != name || instruction.desc != desc)
			continue;
		return instruction;
	}
	return null;
}

function findFirstMethodCall(methodNode, methodType, owner, name, desc) {
	var instruction = ASMAPI.findFirstMethodCall(methodNode, methodType, owner, name, desc);
	assertInstructionFound(instruction, name + 'Call', methodNode.instructions);
	return instruction;
}

/**
 * Utility function to create an INVOKESTATIC call to one of our hooks
 *
 * @param {string} name The name of the hook method
 * @param {string} desc The hook method's method descriptor
 * @return {object} The transformersObj with all transformers wrapped
 */
function callNoCubesHook(name, desc) {
	return new MethodInsnNode(
		//int opcode
		INVOKESTATIC,
		//String owner
		'io/github/cadiboo/nocubes/hooks/Hooks',
		//String name
		name,
		//String descriptor
		desc,
		//boolean isInterface
		false
	);
}

/**
 * Utility function for removing multiple instructions
 *
 * @param {InsnList} instructions The list of instructions to modify
 * @param {number} start The index of the first instruction in the list to be removed
 * @param {number} end The index of the last instruction in the list to be removed
 */
function removeBetweenIndicesInclusive(instructions, start, end) {
	for (var i = start; i <= end; ++i)
		instructions.remove(instructions.get(start));
}

function detectOptiFine(instructions) {
	var length = instructions.size();
	var i;
	for (i = 0; i < length; ++i) {
		var instruction = instructions.get(i);
		if (instruction.getType() == METHOD_INSN) {
			var owner = instruction.owner;
			if (owner == 'net/optifine/override/ChunkCacheOF' || owner == 'net/optifine/BlockPosM') {
				print('Detected OptiFine')
				return true;
			}
		}
	}
	print('Did not detect OptiFine')
	return false;
}










// Wrappers

/**
 * Utility function to wrap all transformers in transformers that have logging
 *
 * @param {object} transformersObj All the transformers of this coremod
 * @return {object} The transformersObj with all transformers wrapped
 */
function wrapWithLogging(transformersObj) {
	var oldPrint = print;
	// Global variable because makeLoggingTransformerFunction is a separate function (thanks to scoping issues)
	currentPrintTransformer = null;
	print = function(msg) {
		if (currentPrintTransformer)
			msg = '[' + currentPrintTransformer + ']: ' + msg;
		oldPrint('[NoCubes Transformer] ' + msg);
	};

	for (var transformerObjName in transformersObj) {
		var transformerObj = transformersObj[transformerObjName];

		var transformer = transformerObj.transformer;
		if (!transformer)
			continue;

		transformerObj.transformer = makeLoggingTransformerFunction(transformerObjName, transformer);
	}
	return transformersObj;
}

/**
 * Utility function for making the wrapper transformer function with logging
 * Not part of {@link #wrapWithLogging) because of scoping issues (Nashhorn
 * doesn't support 'let' which would fix the issues)
 *
 * @param {string} transformerObjName The name of the transformer
 * @param {transformer} transformer The transformer function
 * @return {function} A transformer that wraps the old transformer
 */
function makeLoggingTransformerFunction(transformerObjName, transformer) {
	return function(obj) {
		currentPrintTransformer = transformerObjName;
		print('Starting Transform');
		obj = transformer(obj);
		print('Finished Transform');
		currentPrintTransformer = null;
		return obj;
	};
}


















// Debugging

/**
 * Util function to print a list of instructions for debugging
 *
 * @param {InsnList} instructions The list of instructions to print
 */
function printInstructions(instructions) {
	var labelNames = {
		length: 0
	};
	for (var i = 0, length = instructions.size(); i < length; ++i) {
		var text = getInstructionText(instructions.get(i), labelNames);
		if (text.length > 0) // Some instructions are ignored
			print(text);
	}
}

/**
 * Util function to stringify a list of instructions for debugging
 *
 * @param {InsnList} instructions The list of instructions to stringify
 * @returns {string} The stringified instructions, joined with newlines
 */
function stringifyInstructions(instructions) {
	var fullText = '';
	var labelNames = {
		length: 0
	};
	var arrayLength = instructions.size();
	var i;
	for (i = 0; i < arrayLength; ++i) {
		var text = getInstructionText(instructions.get(i), labelNames);
		if (text.length > 0) // Some instructions are ignored
			fullText += text + '\n';
	}
	return fullText;
}

/**
 * Util function to get the text for an instruction
 *getInstructionText
 * @param {AbstractInsnNode} instruction The instruction to generate text for
 * @param {Map<int, string>} labelNames The names of the labels in the format Map<LabelHashCode, LabelName>
 */
function getInstructionText(instruction, labelNames) {
	var out = '';
	if (instruction.getType() != 8) // LABEL
		out += ' '; // Nice formatting
	if (instruction.getOpcode() > 0) // Labels, Frames and LineNumbers don't have opcodes
		out += OPCODES[instruction.getOpcode()] + ' ';
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
			out += instruction.owner + '.' + instruction.name + ' ' + instruction.desc;
		break;
		case 5: // METHOD_INSN
			out += instruction.owner + '.' + instruction.name + ' ' + instruction.desc + ' (' + instruction.itf + ')';
		break;
		case 6: // INVOKE_DYNAMIC_INSN
			out += instruction.name + ' ' + instruction.desc;
		break;
		case 7: // JUMP_INSN
			out += getLabelName(instruction.label, labelNames);
		break;
		case 8: // LABEL
			out += getLabelName(instruction.getLabel(), labelNames);
		break;
		case 9: // LDC_INSN
			out += instruction.cst;
		break;
		case 10: // IINC_INSN
			out += instruction.var + ' ' + instruction.incr;
		break;
		case 11: // TABLESWITCH_INSN
			out += instruction.min + ' ' + instruction.max;
			out += '\n';
			for (var i = 0; i < instruction.labels.length; ++i) {
			  out += '   ' + (instruction.min + i) + ': ';
			  out += getLabelName(instruction.labels[i], labelNames);
			  out += '\n';
			}
			out += '   ' + 'default: ' + getLabelName(instruction.dflt, labelNames);
		break;
		case 12: // LOOKUPSWITCH_INSN
			for (var i = 0; i < instruction.labels.length; ++i) {
			  out += '   ' + instruction.keys[i] + ': ';
			  out += getLabelName(instruction.labels[i], labelNames);
			  out += '\n';
			}
			out += '   ' + 'default: ' + getLabelName(instruction.dflt, labelNames);
		break;
		case 13: // MULTIANEWARRAY_INSN
			out += instruction.desc + ' ' + instruction.dims;
		break;
		case 14: // FRAME
			out += 'FRAME';
			// Frames don't work because Nashhorn calls AbstractInsnNode#getType()
			// instead of accessing FrameNode#type for the code 'instruction.type'
			// so there is no way to get the frame type of the FrameNode
		break;
		case 15: // LINENUMBER
			out += 'LINENUMBER ';
			out += instruction.line + ' ' + getLabelName(instruction.start.getLabel(), labelNames);
		break;
	}
	return out;
}

/**
 * Util function to get the name for a LabelNode 'instruction'
 *
 * @param {LabelNode} label The label to generate a name for
 * @param {Map<int, string>} labelNames The names of other labels in the format Map<LabelHashCode, LabelName>
 */
function getLabelName(label, labelNames) {
	var labelHashCode = label.hashCode();
	var labelName = labelNames[labelHashCode];
	if (labelName == undefined) {
		labelName = 'L' + labelNames.length;
		labelNames[labelHashCode] = labelName;
		++labelNames.length;
	}
	return labelName;
}

/** The names of the Java Virtual Machine opcodes. */
OPCODES = [
	'NOP', // 0 (0x0)
	'ACONST_NULL', // 1 (0x1)
	'ICONST_M1', // 2 (0x2)
	'ICONST_0', // 3 (0x3)
	'ICONST_1', // 4 (0x4)
	'ICONST_2', // 5 (0x5)
	'ICONST_3', // 6 (0x6)
	'ICONST_4', // 7 (0x7)
	'ICONST_5', // 8 (0x8)
	'LCONST_0', // 9 (0x9)
	'LCONST_1', // 10 (0xa)
	'FCONST_0', // 11 (0xb)
	'FCONST_1', // 12 (0xc)
	'FCONST_2', // 13 (0xd)
	'DCONST_0', // 14 (0xe)
	'DCONST_1', // 15 (0xf)
	'BIPUSH', // 16 (0x10)
	'SIPUSH', // 17 (0x11)
	'LDC', // 18 (0x12)
	'LDC_W', // 19 (0x13)
	'LDC2_W', // 20 (0x14)
	'ILOAD', // 21 (0x15)
	'LLOAD', // 22 (0x16)
	'FLOAD', // 23 (0x17)
	'DLOAD', // 24 (0x18)
	'ALOAD', // 25 (0x19)
	'ILOAD_0', // 26 (0x1a)
	'ILOAD_1', // 27 (0x1b)
	'ILOAD_2', // 28 (0x1c)
	'ILOAD_3', // 29 (0x1d)
	'LLOAD_0', // 30 (0x1e)
	'LLOAD_1', // 31 (0x1f)
	'LLOAD_2', // 32 (0x20)
	'LLOAD_3', // 33 (0x21)
	'FLOAD_0', // 34 (0x22)
	'FLOAD_1', // 35 (0x23)
	'FLOAD_2', // 36 (0x24)
	'FLOAD_3', // 37 (0x25)
	'DLOAD_0', // 38 (0x26)
	'DLOAD_1', // 39 (0x27)
	'DLOAD_2', // 40 (0x28)
	'DLOAD_3', // 41 (0x29)
	'ALOAD_0', // 42 (0x2a)
	'ALOAD_1', // 43 (0x2b)
	'ALOAD_2', // 44 (0x2c)
	'ALOAD_3', // 45 (0x2d)
	'IALOAD', // 46 (0x2e)
	'LALOAD', // 47 (0x2f)
	'FALOAD', // 48 (0x30)
	'DALOAD', // 49 (0x31)
	'AALOAD', // 50 (0x32)
	'BALOAD', // 51 (0x33)
	'CALOAD', // 52 (0x34)
	'SALOAD', // 53 (0x35)
	'ISTORE', // 54 (0x36)
	'LSTORE', // 55 (0x37)
	'FSTORE', // 56 (0x38)
	'DSTORE', // 57 (0x39)
	'ASTORE', // 58 (0x3a)
	'ISTORE_0', // 59 (0x3b)
	'ISTORE_1', // 60 (0x3c)
	'ISTORE_2', // 61 (0x3d)
	'ISTORE_3', // 62 (0x3e)
	'LSTORE_0', // 63 (0x3f)
	'LSTORE_1', // 64 (0x40)
	'LSTORE_2', // 65 (0x41)
	'LSTORE_3', // 66 (0x42)
	'FSTORE_0', // 67 (0x43)
	'FSTORE_1', // 68 (0x44)
	'FSTORE_2', // 69 (0x45)
	'FSTORE_3', // 70 (0x46)
	'DSTORE_0', // 71 (0x47)
	'DSTORE_1', // 72 (0x48)
	'DSTORE_2', // 73 (0x49)
	'DSTORE_3', // 74 (0x4a)
	'ASTORE_0', // 75 (0x4b)
	'ASTORE_1', // 76 (0x4c)
	'ASTORE_2', // 77 (0x4d)
	'ASTORE_3', // 78 (0x4e)
	'IASTORE', // 79 (0x4f)
	'LASTORE', // 80 (0x50)
	'FASTORE', // 81 (0x51)
	'DASTORE', // 82 (0x52)
	'AASTORE', // 83 (0x53)
	'BASTORE', // 84 (0x54)
	'CASTORE', // 85 (0x55)
	'SASTORE', // 86 (0x56)
	'POP', // 87 (0x57)
	'POP2', // 88 (0x58)
	'DUP', // 89 (0x59)
	'DUP_X1', // 90 (0x5a)
	'DUP_X2', // 91 (0x5b)
	'DUP2', // 92 (0x5c)
	'DUP2_X1', // 93 (0x5d)
	'DUP2_X2', // 94 (0x5e)
	'SWAP', // 95 (0x5f)
	'IADD', // 96 (0x60)
	'LADD', // 97 (0x61)
	'FADD', // 98 (0x62)
	'DADD', // 99 (0x63)
	'ISUB', // 100 (0x64)
	'LSUB', // 101 (0x65)
	'FSUB', // 102 (0x66)
	'DSUB', // 103 (0x67)
	'IMUL', // 104 (0x68)
	'LMUL', // 105 (0x69)
	'FMUL', // 106 (0x6a)
	'DMUL', // 107 (0x6b)
	'IDIV', // 108 (0x6c)
	'LDIV', // 109 (0x6d)
	'FDIV', // 110 (0x6e)
	'DDIV', // 111 (0x6f)
	'IREM', // 112 (0x70)
	'LREM', // 113 (0x71)
	'FREM', // 114 (0x72)
	'DREM', // 115 (0x73)
	'INEG', // 116 (0x74)
	'LNEG', // 117 (0x75)
	'FNEG', // 118 (0x76)
	'DNEG', // 119 (0x77)
	'ISHL', // 120 (0x78)
	'LSHL', // 121 (0x79)
	'ISHR', // 122 (0x7a)
	'LSHR', // 123 (0x7b)
	'IUSHR', // 124 (0x7c)
	'LUSHR', // 125 (0x7d)
	'IAND', // 126 (0x7e)
	'LAND', // 127 (0x7f)
	'IOR', // 128 (0x80)
	'LOR', // 129 (0x81)
	'IXOR', // 130 (0x82)
	'LXOR', // 131 (0x83)
	'IINC', // 132 (0x84)
	'I2L', // 133 (0x85)
	'I2F', // 134 (0x86)
	'I2D', // 135 (0x87)
	'L2I', // 136 (0x88)
	'L2F', // 137 (0x89)
	'L2D', // 138 (0x8a)
	'F2I', // 139 (0x8b)
	'F2L', // 140 (0x8c)
	'F2D', // 141 (0x8d)
	'D2I', // 142 (0x8e)
	'D2L', // 143 (0x8f)
	'D2F', // 144 (0x90)
	'I2B', // 145 (0x91)
	'I2C', // 146 (0x92)
	'I2S', // 147 (0x93)
	'LCMP', // 148 (0x94)
	'FCMPL', // 149 (0x95)
	'FCMPG', // 150 (0x96)
	'DCMPL', // 151 (0x97)
	'DCMPG', // 152 (0x98)
	'IFEQ', // 153 (0x99)
	'IFNE', // 154 (0x9a)
	'IFLT', // 155 (0x9b)
	'IFGE', // 156 (0x9c)
	'IFGT', // 157 (0x9d)
	'IFLE', // 158 (0x9e)
	'IF_ICMPEQ', // 159 (0x9f)
	'IF_ICMPNE', // 160 (0xa0)
	'IF_ICMPLT', // 161 (0xa1)
	'IF_ICMPGE', // 162 (0xa2)
	'IF_ICMPGT', // 163 (0xa3)
	'IF_ICMPLE', // 164 (0xa4)
	'IF_ACMPEQ', // 165 (0xa5)
	'IF_ACMPNE', // 166 (0xa6)
	'GOTO', // 167 (0xa7)
	'JSR', // 168 (0xa8)
	'RET', // 169 (0xa9)
	'TABLESWITCH', // 170 (0xaa)
	'LOOKUPSWITCH', // 171 (0xab)
	'IRETURN', // 172 (0xac)
	'LRETURN', // 173 (0xad)
	'FRETURN', // 174 (0xae)
	'DRETURN', // 175 (0xaf)
	'ARETURN', // 176 (0xb0)
	'RETURN', // 177 (0xb1)
	'GETSTATIC', // 178 (0xb2)
	'PUTSTATIC', // 179 (0xb3)
	'GETFIELD', // 180 (0xb4)
	'PUTFIELD', // 181 (0xb5)
	'INVOKEVIRTUAL', // 182 (0xb6)
	'INVOKESPECIAL', // 183 (0xb7)
	'INVOKESTATIC', // 184 (0xb8)
	'INVOKEINTERFACE', // 185 (0xb9)
	'INVOKEDYNAMIC', // 186 (0xba)
	'NEW', // 187 (0xbb)
	'NEWARRAY', // 188 (0xbc)
	'ANEWARRAY', // 189 (0xbd)
	'ARRAYLENGTH', // 190 (0xbe)
	'ATHROW', // 191 (0xbf)
	'CHECKCAST', // 192 (0xc0)
	'INSTANCEOF', // 193 (0xc1)
	'MONITORENTER', // 194 (0xc2)
	'MONITOREXIT', // 195 (0xc3)
	'WIDE', // 196 (0xc4)
	'MULTIANEWARRAY', // 197 (0xc5)
	'IFNULL', // 198 (0xc6)
	'IFNONNULL' // 199 (0xc7)
];

// endregion Debugging
