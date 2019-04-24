//package io.github.cadiboo.nocubes.tempcore;
//
//import org.objectweb.asm.Label;
//import org.objectweb.asm.Opcodes;
//import org.objectweb.asm.tree.AbstractInsnNode;
//import org.objectweb.asm.tree.ClassNode;
//import org.objectweb.asm.tree.FieldInsnNode;
//import org.objectweb.asm.tree.FieldNode;
//import org.objectweb.asm.tree.FrameNode;
//import org.objectweb.asm.tree.InsnList;
//import org.objectweb.asm.tree.InsnNode;
//import org.objectweb.asm.tree.JumpInsnNode;
//import org.objectweb.asm.tree.LabelNode;
//import org.objectweb.asm.tree.MethodInsnNode;
//import org.objectweb.asm.tree.MethodNode;
//import org.objectweb.asm.tree.VarInsnNode;
//
//import java.util.ListIterator;
//
//import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.Api;
//import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.LOGGER;
//import static io.github.cadiboo.nocubes.tempcore.TransformerDispatcher.getMethod;
//
///**
// * @author Cadiboo
// */
//final class IsEntityInsideOpaqueBlockTransformer implements Opcodes {
//
//	private static final String runIsEntityInsideOpaqueBlock_fieldName = "nocubes_runIsEntityInsideOpaqueBlockDefaultOnce";
//
//	static void hook_isEntityInsideOpaqueBlock(final ClassNode classNode) {
//
//		LOGGER.info("Starting adding field " + runIsEntityInsideOpaqueBlock_fieldName);
//		{
//			classNode.fields.add(new FieldNode(ACC_PUBLIC, runIsEntityInsideOpaqueBlock_fieldName, "Z", null, false));
//		}
//		LOGGER.info("Finished adding field " + runIsEntityInsideOpaqueBlock_fieldName);
//
//		final MethodNode isEntityInsideOpaqueBlock = getMethod(classNode, "func_70094_T", "()Z");
//		final InsnList instructions = isEntityInsideOpaqueBlock.instructions;
//		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
//		LabelNode FIRST_LABEL = null;
//		while (iterator.hasNext()) {
//			final AbstractInsnNode insn = iterator.next();
//			if (insn.getType() != AbstractInsnNode.LABEL) {
//				continue;
//			}
//			FIRST_LABEL = (LabelNode) insn;
//			break;
//		}
//
//		LOGGER.info("Starting injecting into isEntityInsideOpaqueBlock");
//		{
//			//Prep for 1.13
//			final InsnList injectedInstructions = Api.getMethodNode().instructions;
//
//			// if(!nocubes_runIsEntityInsideOpaqueBlockDefaultOnce && NoCubes.hooksEnabled()) {
//			//     return IsEntityInsideOpaqueBlockHook.isEntityInsideOpaqueBlock(this);
//			// }
//			// nocubes_runIsEntityInsideOpaqueBlockDefaultOnce = false;
//			// // Normal Code
//
//			final LabelNode labelNode = new LabelNode(new Label());
//			injectedInstructions.add(new LabelNode(new Label()));
//			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
//			injectedInstructions.add(new FieldInsnNode(GETFIELD, "net/minecraft/entity/Entity", runIsEntityInsideOpaqueBlock_fieldName, "Z"));
//			injectedInstructions.add(new JumpInsnNode(IFNE, labelNode));
//			injectedInstructions.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/NoCubes", "areHooksEnabled", "()Z", false));
//			injectedInstructions.add(new JumpInsnNode(IFEQ, labelNode));
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
//			injectedInstructions.add(new MethodInsnNode(INVOKESTATIC, "io/github/cadiboo/nocubes/hooks/IsEntityInsideOpaqueBlockHook", "isEntityInsideOpaqueBlock", "(Lnet/minecraft/entity/Entity;)Z", false));
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			injectedInstructions.add(new InsnNode(IRETURN));
//
//			injectedInstructions.add(labelNode);
//			injectedInstructions.add(new FrameNode(F_SAME, -1, null, -1, null));
//			injectedInstructions.add(new VarInsnNode(ALOAD, 0));
//			injectedInstructions.add(new InsnNode(ICONST_0));
//			injectedInstructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/Entity", runIsEntityInsideOpaqueBlock_fieldName, "Z"));
//
//			injectedInstructions.add(new LabelNode(new Label()));
//			instructions.insert(FIRST_LABEL, injectedInstructions);
//
//		}
//		LOGGER.info("Finished injecting into isEntityInsideOpaqueBlock");
//
//	}
//
//	static void add_runIsEntityInsideOpaqueBlockDefaultOnce(final ClassNode classNode) {
//		LOGGER.info("Starting injecting into runIsEntityInsideOpaqueBlockDefaultOnce");
//		{
//			final InsnList instructions = getMethod(classNode, "runIsEntityInsideOpaqueBlockDefaultOnce", "(Lnet/minecraft/entity/Entity;)Z").instructions;
//
//			instructions.clear();
//
//			instructions.add(new LabelNode(new Label()));
//			instructions.add(new VarInsnNode(ALOAD, 0));
//			instructions.add(new InsnNode(ICONST_1));
//			instructions.add(new FieldInsnNode(PUTFIELD, "net/minecraft/entity/Entity", runIsEntityInsideOpaqueBlock_fieldName, "Z"));
//
//			instructions.add(new LabelNode(new Label()));
//			instructions.add(new InsnNode(RETURN));
//		}
//		LOGGER.info("Finished injecting into runIsEntityInsideOpaqueBlockDefaultOnce");
//	}
//
//}
