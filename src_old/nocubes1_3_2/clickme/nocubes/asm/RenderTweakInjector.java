package clickme.nocubes.asm;

import java.util.Iterator;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class RenderTweakInjector implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] bytes) {
      if (bytes == null) {
         return null;
      } else {
         boolean obfuscated = true;
         if (!"blm".equals(name)) {
            if (!"net.minecraft.client.renderer.RenderBlocks".equals(name)) {
               return bytes;
            }

            obfuscated = false;
         }

         ClassNode classNode = new ClassNode();
         ClassReader classReader = new ClassReader(bytes);
         classReader.accept(classNode, 8);
         MethodNode targetMethod = null;
         Iterator var8 = classNode.methods.iterator();

         while(var8.hasNext()) {
            MethodNode methodNode = (MethodNode)var8.next();
            if (obfuscated) {
               if ("b".equals(methodNode.name) && "(Laji;III)Z".equals(methodNode.desc)) {
                  targetMethod = methodNode;
                  break;
               }
            } else if ("renderBlockByRenderType".equals(methodNode.name)) {
               targetMethod = methodNode;
               break;
            }
         }

         if (targetMethod == null) {
            return bytes;
         } else {
            MethodNode injectedMethod = new MethodNode();
            Label label0 = new Label();
            injectedMethod.visitLabel(label0);
            injectedMethod.visitVarInsn(25, 1);
            injectedMethod.visitMethodInsn(184, "clickme/nocubes/SoftBlockRenderer", "shouldHookRenderer", obfuscated ? "(Laji;)Z" : "(Lnet/minecraft/block/Block;)Z");
            Label label2 = new Label();
            injectedMethod.visitJumpInsn(153, label2);
            Label label3 = new Label();
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
            ClassWriter writer = new ClassWriter(1);
            classNode.accept(writer);
            return writer.toByteArray();
         }
      }
   }
}
