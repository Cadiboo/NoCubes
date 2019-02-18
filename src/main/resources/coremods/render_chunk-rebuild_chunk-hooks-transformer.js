function initializeCoreMod() {
	return {
		'coremodone': {
			'target': {
				'type': 'CLASS',
				'name': 'net.minecraft.client.renderer.chunk.RenderChunk'
			},
			'transformer': function(classNode) {
				print("Class RenderChunk: ", classNode.name);

				var methods = classNode.methods;

				var arrayLength = methods.length;
				for (var i = 0; i < arrayLength; i++) {
					var method = methods[i];

					if(methodMatches(method)) {
						print("Injecting hooks...");
						injectHooks(method.instructions);
						print("Successfully injected hooks!");
						break;
					}

				}

				return classNode;
			}
		}
	}
}

var DEBUG_METHODS = true;

function methodNameMatches(methodName) {
	var deobfNameEquals = "rebuildChunk".equals(methodName);
	var mcpNameEquals = "func_178581_b".equals(methodName);

	var anyEqual = deobfNameEquals || mcpNameEquals;
	if(anyEqual) {
		print(deobfNameEquals ? "Matched a deobfuscated name - we are in a DEOBFUSCATED/MCP-NAMED DEVELOPER Environment" : "Matched an SRG name - We are in an SRG-NAMED PRODUCTION Environment")
	}

	return anyEqual;
}

var RENDER_CHUNK_REBUILD_CHUNK_DESCRIPTOR = "(FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V";

function methodMatches(method) {
	if (!method.desc.equals(RENDER_CHUNK_REBUILD_CHUNK_DESCRIPTOR)) {
		if (DEBUG_METHODS) {
			print("Method with name \"" + method.name + "\" and description \"" + method.desc + "\" did not match");
		}
		return false;
	}

	if (DEBUG_METHODS) {
		print("Method with name \"" + method.name + "\" and description \"" + method.desc + "\" matched!");
	}

	// make sure not to overwrite resortTransparency (it has the same description but it's name is "a" or "func_178570_a" while rebuildChunk's name is "b" or "func_178581_b")
	if (!methodNameMatches(method.name)) {
		if (DEBUG_METHODS) {
			print("Method with name \"" + method.name + "\" and description \"" + method.desc + "\" was rejected");
		}
		return false;
	}

	if (DEBUG_METHODS) {
		print("Method with name \"" + method.name + "\" and description \"" + method.desc + "\" matched and passed");
	}

	return true;
}

function injectHooks(instructions) {

//	var instructionsArray = instructions.toArray();
//	var arrayLength = instructionsArray.length;
//	for (var i = 0; i < arrayLength; i++) {
//		var instruction = instructionsArray[i];
//		print(insnToString(instruction));
//	}

	print("Redirecting method... This is incompatible with other mods and will soon be made better.")

	redirectMethodTemp(instructions);

	print("Successfully redirected method!");

}

var/*Class/Interface*/ Opcodes = Java.type('org.objectweb.asm.Opcodes');
var/*Class*/ MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var/*Class*/ MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var/*Class*/ InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var/*Class*/ VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var/*Class*/ InsnList = Java.type('org.objectweb.asm.tree.InsnList');

var ALOAD = Opcodes.ALOAD;
var FLOAD = Opcodes.FLOAD;
var RETURN = Opcodes.RETURN;
var INVOKESTATIC = Opcodes.INVOKESTATIC;

var ALOAD_this = 0;
var FLOAD_x = 1;
var FLOAD_y = 2;
var FLOAD_z = 3;
var ALOAD_generator = 4;

// Finds the first instruction (NEW net/minecraft/client/renderer/chunk/CompiledChunk)
// and inserts before it. Ugly, effective, incompatible
function redirectMethodTemp(instructions) {

	var NEW_CompiledChunk;

	var instructionsArray = instructions.toArray();
	var arrayLength = instructionsArray.length;
	for (var i = 0; i < arrayLength; i++) {
		var instruction = instructionsArray[i];

		if(instruction.getOpcode() == Opcodes.NEW) {
			NEW_CompiledChunk = instruction;
			print("Found injection point " + instruction);
			break;
		}

	}

	if (!NEW_CompiledChunk) {
		print("Error: Couldn't find injection point!");
	}

	var tempInstructionList = new InsnList();

	tempInstructionList.add(new VarInsnNode(ALOAD, ALOAD_this)); // this
	tempInstructionList.add(new VarInsnNode(FLOAD, FLOAD_x)); // x
	tempInstructionList.add(new VarInsnNode(FLOAD, FLOAD_y)); // y
	tempInstructionList.add(new VarInsnNode(FLOAD, FLOAD_z)); // z
	tempInstructionList.add(new VarInsnNode(ALOAD, ALOAD_generator)); // generator
	REBUILD_CHUNK_REDIRECT_TEMP.visit(tempInstructionList);
	tempInstructionList.add(new InsnNode(RETURN));

	instructions.insertBefore(NEW_CompiledChunk, tempInstructionList);

	print("Successfully inserted instructions!");

}

var REBUILD_CHUNK_REDIRECT_TEMP =  {
	'visit': function(insnList) {
		// invokestatic io/github/cadiboo/renderchunkrebuildchunkhooks/hooks/OverwriteHookTemp(Lnet/minecraft/client/renderer/chunk/RenderChunk;FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V
		insnList.add(
			new MethodInsnNode(
				//int opcode
				INVOKESTATIC,
				//String owner
				"io/github/cadiboo/renderchunkrebuildchunkhooks/hooks/OverwriteHookTemp",
				//String name
				"rebuildChunk",
				//String descriptor
				"(Lnet/minecraft/client/renderer/chunk/RenderChunk;FFFLnet/minecraft/client/renderer/chunk/ChunkRenderTask;)V",
				//boolean isInterface
				false
			)
		);
	}
}
