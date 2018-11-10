package clickme.nocubes.asm;

import net.minecraft.launchwrapper.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;
import java.util.*;

public class WorldRenderInjector implements IClassTransformer
{
    public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (!"blo".equals(name)) {
            return bytes;
        }
        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 8);
        MethodNode targetMethod = null;
        for (final MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("a") && methodNode.desc.equals("(Lsv;)V")) {
                targetMethod = methodNode;
                break;
            }
        }
        if (targetMethod == null) {
            return bytes;
        }
        System.out.println("Inside the WorldRenderer class: " + name);
        final MethodNode injectedMethod = (MethodNode)new Object();
        final Label label0 = new Label();
        injectedMethod.visitLabel(label0);
        injectedMethod.visitVarInsn(21, 20);
        final Label label2 = new Label();
        injectedMethod.visitJumpInsn(154, label2);
        final Label label3 = new Label();
        injectedMethod.visitLabel(label3);
        injectedMethod.visitInsn(4);
        injectedMethod.visitVarInsn(54, 20);
        final Label label4 = new Label();
        injectedMethod.visitLabel(label4);
        injectedMethod.visitVarInsn(25, 0);
        injectedMethod.visitVarInsn(21, 17);
        injectedMethod.visitMethodInsn(183, "blo", "b", "(I)V");
        injectedMethod.visitLabel(label2);
        injectedMethod.visitFrame(2, 1, null, 0, null);
        injectedMethod.visitVarInsn(21, 19);
        injectedMethod.visitVarInsn(21, 17);
        injectedMethod.visitVarInsn(21, 2);
        injectedMethod.visitVarInsn(21, 3);
        injectedMethod.visitVarInsn(21, 4);
        injectedMethod.visitVarInsn(25, 15);
        injectedMethod.visitVarInsn(25, 16);
        injectedMethod.visitMethodInsn(184, "clickme/nocubes/renderer/SurfaceNets", "renderChunk", "(IIIILahl;Lblm;)Z");
        injectedMethod.visitInsn(128);
        injectedMethod.visitVarInsn(54, 19);
        final ListIterator iterator = targetMethod.instructions.iterator();
        int varCount = 0;
        while (iterator.hasNext()) {
            final AbstractInsnNode instruction = iterator.next();
            if (instruction.getOpcode() == 165) {
                final JumpInsnNode jumpInsnNode = (JumpInsnNode)instruction;
                targetMethod.instructions.insert(instruction, new JumpInsnNode(154, jumpInsnNode.label));
                targetMethod.instructions.insert(instruction, new MethodInsnNode(184, "clickme/nocubes/NoCubes", "isBlockNatural", "(Laji;)Z"));
                targetMethod.instructions.insert(instruction, new VarInsnNode(25, 24));
                System.out.println("Inserted instructions extra check");
            }
            if (instruction.getOpcode() == 21) {
                final VarInsnNode varInsnNode = (VarInsnNode)instruction;
                if (varInsnNode.var != 19 || ++varCount != 2) {
                    continue;
                }
                targetMethod.instructions.insertBefore(instruction, injectedMethod.instructions);
                System.out.println("Inserted instructions render hook");
            }
        }
        final ClassWriter writer = new ClassWriter(3);
        classNode.accept(writer);
        return writer.toByteArray();
    }
}
