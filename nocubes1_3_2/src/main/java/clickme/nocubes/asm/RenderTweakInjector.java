package clickme.nocubes.asm;

import net.minecraft.launchwrapper.*;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.*;
import java.util.*;

public class RenderTweakInjector implements IClassTransformer
{
    public byte[] transform(final String name, final String transformedName, final byte[] bytes)
    {
        if(bytes == null)
        {
            return null;
        }
        boolean obfuscated = true;
        if(!"blm".equals(name))
        {
            if(!"net.minecraft.client.renderer.RenderBlocks".equals(name))
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
                if("b".equals(methodNode.name) && "(Laji;III)Z".equals(methodNode.desc))
                {
                    targetMethod = methodNode;
                    break;
                }
                continue;
            }
            else
            {
                if("renderBlockByRenderType".equals(methodNode.name))
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
        injectedMethod.visitVarInsn(25, 1);
        injectedMethod.visitMethodInsn(184, "clickme/nocubes/SoftBlockRenderer", "shouldHookRenderer", obfuscated ? "(Laji;)Z" : "(Lnet/minecraft/block/Block;)Z");
        final Label label2 = new Label();
        injectedMethod.visitJumpInsn(153, label2);
        final Label label3 = new Label();
        injectedMethod.visitLabel(label3);
        injectedMethod.visitFieldInsn(178, "clickme/nocubes/NoCubes", "softBlockRenderer", "Lclickme/nocubes/SoftBlockRenderer;");
        injectedMethod.visitVarInsn(25, 1);
        injectedMethod.visitVarInsn(21, 2);
        injectedMethod.visitVarInsn(21, 3);
        injectedMethod.visitVarInsn(21, 4);
        injectedMethod.visitVarInsn(25, 0);
        injectedMethod.visitMethodInsn(182, "clickme/nocubes/SoftBlockRenderer", "directRenderHook", obfuscated ? "(Laji;IIILblm;)Z" : "(Lnet/minecraft/block/Block;IIILnet/minecraft/client/renderer/RenderBlocks;)Z");
        injectedMethod.visitInsn(172);
        injectedMethod.visitLabel(label2);
        targetMethod.instructions.insert(injectedMethod.instructions);
        final ClassWriter writer = new ClassWriter(1);
        classNode.accept((ClassVisitor)writer);
        return writer.toByteArray();
    }
}
