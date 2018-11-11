package clickme.nocubes.asm;

import net.minecraft.launchwrapper.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;
import java.util.*;

public class BlockTweakInjector implements IClassTransformer
{
    public byte[] transform(final String name, final String transformedName, final byte[] bytes)
    {
        if(bytes == null)
        {
            return null;
        }
        boolean obfuscated = true;
        if(!"aji".equals(name))
        {
            if(!"net.minecraft.block.Block".equals(name))
            {
                return bytes;
            }
            obfuscated = false;
        }
        final ClassNode classNode = new ClassNode();
        final ClassReader classReader = new ClassReader(bytes);
        classReader.accept((ClassVisitor)classNode, 8);
        MethodNode targetMethod = null;
        for(final MethodNode methodNode : classNode.methods)
        {
            if(obfuscated)
            {
                if("a".equals(methodNode.name) && "(Lahb;IIILazt;Ljava/util/List;Lsa;)V".equals(methodNode.desc))
                {
                    targetMethod = methodNode;
                    break;
                }
                continue;
            }
            else
            {
                if("addCollisionBoxesToList".equals(methodNode.name))
                {
                    targetMethod = methodNode;
                    break;
                }
                continue;
            }
        }
        if(targetMethod == null)
        {
            return bytes;
        }
        final MethodNode injectedMethod = new MethodNode();
        final Label label0 = new Label();
        injectedMethod.visitLabel(label0);
        injectedMethod.visitVarInsn(25, 0);
        injectedMethod.visitMethodInsn(184, "clickme/nocubes/NoCubes", "isBlockSoftForCollision", obfuscated ? "(Laji;)Z" : "(Lnet/minecraft/block/Block;)Z");
        final Label label2 = new Label();
        injectedMethod.visitJumpInsn(153, label2);
        final Label label3 = new Label();
        injectedMethod.visitLabel(label3);
        injectedMethod.visitVarInsn(25, 0);
        injectedMethod.visitVarInsn(25, 1);
        injectedMethod.visitVarInsn(21, 2);
        injectedMethod.visitVarInsn(21, 3);
        injectedMethod.visitVarInsn(21, 4);
        injectedMethod.visitVarInsn(25, 5);
        injectedMethod.visitVarInsn(25, 6);
        injectedMethod.visitVarInsn(25, 7);
        injectedMethod.visitMethodInsn(184, "clickme/nocubes/SoftBlockRenderer", "inject", obfuscated ? "(Laji;Lahb;IIILazt;Ljava/util/List;Lsa;)V" : "(Lnet/minecraft/block/Block;Lnet/minecraft/world/World;IIILnet/minecraft/util/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;)V");
        final Label label4 = new Label();
        injectedMethod.visitLabel(label4);
        injectedMethod.visitInsn(177);
        injectedMethod.visitLabel(label2);
        targetMethod.instructions.insert(injectedMethod.instructions);
        final ClassWriter writer = new ClassWriter(3);
        classNode.accept((ClassVisitor)writer);
        return writer.toByteArray();
    }
}
