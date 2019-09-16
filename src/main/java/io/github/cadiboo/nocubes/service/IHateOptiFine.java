package io.github.cadiboo.nocubes.service;

import net.minecraftforge.coremod.api.ASMAPI;
import optifine.OptiFineTransformer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * @author Cadiboo
 */
public final class IHateOptiFine {

	public static boolean onDebugClass(OptiFineTransformer transformer, ClassNode input) {
		for (final FieldNode field : input.fields) {
			field.name = ASMAPI.mapField(field.name);
		}
		for (final MethodNode method : input.methods) {
			method.name = ASMAPI.mapMethod(method.name);
			method.instructions.iterator().forEachRemaining(insn -> {
				if (insn instanceof FieldInsnNode) {
					final FieldInsnNode fieldInsn = (FieldInsnNode) insn;
					fieldInsn.name = ASMAPI.mapField(fieldInsn.name);
				} else if (insn instanceof MethodInsnNode) {
					final MethodInsnNode methodInsn = (MethodInsnNode) insn;
					methodInsn.name = ASMAPI.mapMethod(methodInsn.name);
				}
			});
		}
		return false;
	}

}
