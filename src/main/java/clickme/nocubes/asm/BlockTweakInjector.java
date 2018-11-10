package clickme.nocubes.asm;

import net.minecraft.launchwrapper.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;
import java.util.*;

public class BlockTweakInjector implements IClassTransformer
{
    public byte[] transform(final String name, final String transformedName, final byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        if (!"net.minecraft.block.Block".equals(name)) {
            return bytes;
        }
        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 8);
        MethodNode targetMethod = null;
        for (final MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("shouldSideBeRendered") && methodNode.desc.equals("(Lnet/minecraft/world/IBlockAccess;IIII)Z")) {
                targetMethod = methodNode;
                break;
            }
        }
        if (targetMethod == null) {
            return bytes;
        }
        System.out.println("Inside the Block class: " + name);
        final MethodNode injectedMethod = (MethodNode)new Object();
        final ListIterator iterator = targetMethod.instructions.iterator();
        int varCount = 0;
        while (iterator.hasNext()) {
            final AbstractInsnNode instruction = iterator.next();
            if (instruction.getOpcode() == 165) {
                final JumpInsnNode jumpInsnNode = (JumpInsnNode)instruction;
                targetMethod.instructions.insert(instruction, new JumpInsnNode(154, jumpInsnNode.label));
                targetMethod.instructions.insert(instruction, new MethodInsnNode(184, "clickme/nocubes/NoCubes", "isBlockNatural", "(Lnet/minecraft/block/Block;)Z"));
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
