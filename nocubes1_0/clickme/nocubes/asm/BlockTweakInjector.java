package clickme.nocubes.asm;

import java.util.Iterator;
import java.util.ListIterator;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class BlockTweakInjector implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] bytes) {
      if (bytes == null) {
         return null;
      } else if (!"net.minecraft.block.Block".equals(name)) {
         return bytes;
      } else {
         ClassNode classNode = new ClassNode();
         ClassReader classReader = new ClassReader(bytes);
         classReader.accept(classNode, 8);
         MethodNode targetMethod = null;
         Iterator i$ = classNode.methods.iterator();

         while(i$.hasNext()) {
            MethodNode methodNode = (MethodNode)i$.next();
            if (methodNode.name.equals("shouldSideBeRendered") && methodNode.desc.equals("(Lnet/minecraft/world/IBlockAccess;IIII)Z")) {
               targetMethod = methodNode;
               break;
            }
         }

         if (targetMethod == null) {
            return bytes;
         } else {
            System.out.println("Inside the Block class: " + name);
            MethodNode injectedMethod = new MethodNode();
            ListIterator iterator = targetMethod.instructions.iterator();
            int varCount = 0;

            while(iterator.hasNext()) {
               AbstractInsnNode instruction = (AbstractInsnNode)iterator.next();
               if (instruction.getOpcode() == 165) {
                  JumpInsnNode jumpInsnNode = (JumpInsnNode)instruction;
                  targetMethod.instructions.insert(instruction, new JumpInsnNode(154, jumpInsnNode.label));
                  targetMethod.instructions.insert(instruction, new MethodInsnNode(184, "clickme/nocubes/NoCubes", "isBlockNatural", "(Lnet/minecraft/block/Block;)Z"));
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
