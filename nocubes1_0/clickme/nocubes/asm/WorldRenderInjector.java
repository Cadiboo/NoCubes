package clickme.nocubes.asm;

import java.util.Iterator;
import java.util.ListIterator;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class WorldRenderInjector implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] bytes) {
      if (bytes == null) {
         return null;
      } else if (!"blo".equals(name)) {
         return bytes;
      } else {
         ClassNode classNode = new ClassNode();
         ClassReader classReader = new ClassReader(bytes);
         classReader.accept(classNode, 8);
         MethodNode targetMethod = null;
         Iterator i$ = classNode.methods.iterator();

         while(i$.hasNext()) {
            MethodNode methodNode = (MethodNode)i$.next();
            if (methodNode.name.equals("a") && methodNode.desc.equals("(Lsv;)V")) {
               targetMethod = methodNode;
               break;
            }
         }

         if (targetMethod == null) {
            return bytes;
         } else {
            System.out.println("Inside the WorldRenderer class: " + name);
            MethodNode injectedMethod = new MethodNode();
            Label label0 = new Label();
            injectedMethod.visitLabel(label0);
            injectedMethod.visitVarInsn(21, 20);
            Label label1 = new Label();
            injectedMethod.visitJumpInsn(154, label1);
            Label label2 = new Label();
            injectedMethod.visitLabel(label2);
            injectedMethod.visitInsn(4);
            injectedMethod.visitVarInsn(54, 20);
            Label label3 = new Label();
            injectedMethod.visitLabel(label3);
            injectedMethod.visitVarInsn(25, 0);
            injectedMethod.visitVarInsn(21, 17);
            injectedMethod.visitMethodInsn(183, "blo", "b", "(I)V");
            injectedMethod.visitLabel(label1);
            injectedMethod.visitFrame(2, 1, (Object[])null, 0, (Object[])null);
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
            ListIterator iterator = targetMethod.instructions.iterator();
            int varCount = 0;

            while(iterator.hasNext()) {
               AbstractInsnNode instruction = (AbstractInsnNode)iterator.next();
               if (instruction.getOpcode() == 165) {
                  JumpInsnNode jumpInsnNode = (JumpInsnNode)instruction;
                  targetMethod.instructions.insert(instruction, new JumpInsnNode(154, jumpInsnNode.label));
                  targetMethod.instructions.insert(instruction, new MethodInsnNode(184, "clickme/nocubes/NoCubes", "isBlockNatural", "(Laji;)Z"));
                  targetMethod.instructions.insert(instruction, new VarInsnNode(25, 24));
                  System.out.println("Inserted instructions extra check");
               }

               if (instruction.getOpcode() == 21) {
                  VarInsnNode varInsnNode = (VarInsnNode)instruction;
                  if (varInsnNode.var == 19) {
                     ++varCount;
                     if (varCount == 2) {
                        targetMethod.instructions.insertBefore(instruction, injectedMethod.instructions);
                        System.out.println("Inserted instructions render hook");
                     }
                  }
               }
            }

            ClassWriter writer = new ClassWriter(3);
            classNode.accept(writer);
            return writer.toByteArray();
         }
      }
   }
}
