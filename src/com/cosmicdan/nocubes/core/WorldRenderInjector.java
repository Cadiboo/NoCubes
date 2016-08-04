package com.cosmicdan.nocubes.core;

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

import com.cosmicdan.nocubes.Main;

/*
 * Original by Click_Me
 * Reverse-engineered and re-implemented (with permission) by CosmicDan
 * 
 */
public class WorldRenderInjector implements IClassTransformer {
   public byte[] transform(String name, String transformedName, byte[] bytes) {
      if(bytes == null) {
         return null;
      //} else if(!"blo".equals(name)) {
      } else if(!"net.minecraft.client.renderer.WorldRenderer".equals(name)) {
         return bytes;
      } else {
         ClassNode classNode = new ClassNode();
         ClassReader classReader = new ClassReader(bytes);
         classReader.accept(classNode, 8);
         MethodNode targetMethod = null;

         for(MethodNode methodNode : classNode.methods) {
            //if(methodNode.name.equals("a") && methodNode.desc.equals("(Lsv;)V")) {
        	//if(methodNode.name.equals("func_147892_a") && methodNode.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;)V")) {
        	if(methodNode.name.equals("updateRenderer") && methodNode.desc.equals("(Lnet/minecraft/entity/EntityLivingBase;)V")) {
               targetMethod = methodNode;
               break;
            }
         }

         if(targetMethod == null) {
            return bytes;
         } else {
        	//Main.LOGGER.warn("~~~ Inside the WorldRenderer class: " + name);
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
            //injectedMethod.visitMethodInsn(183, "blo", "b", "(I)V");
            injectedMethod.visitMethodInsn(183, "net/minecraft/client/renderer/WorldRenderer", "preRenderBlocks", "(I)V");
            injectedMethod.visitLabel(label1);
            injectedMethod.visitFrame(2, 1, (Object[])null, 0, (Object[])null);
            injectedMethod.visitVarInsn(21, 19);
            injectedMethod.visitVarInsn(21, 17);
            injectedMethod.visitVarInsn(21, 2);
            injectedMethod.visitVarInsn(21, 3);
            injectedMethod.visitVarInsn(21, 4);
            injectedMethod.visitVarInsn(25, 15);
            injectedMethod.visitVarInsn(25, 16);
            injectedMethod.visitMethodInsn(184, "com/cosmicdan/nocubes/renderer/SurfaceNets", "renderChunk", "(IIIILnet/minecraft/world/IBlockAccess;Lnet/minecraft/client/renderer/RenderBlocks;)Z");
            injectedMethod.visitInsn(128);
            injectedMethod.visitVarInsn(54, 19);
            ListIterator iterator = targetMethod.instructions.iterator();
            int varCount = 0;

            while(iterator.hasNext()) {
               AbstractInsnNode instruction = (AbstractInsnNode)iterator.next();
               if(instruction.getOpcode() == 165) {
                  JumpInsnNode jumpInsnNode = (JumpInsnNode)instruction;
                  targetMethod.instructions.insert(instruction, new JumpInsnNode(154, jumpInsnNode.label));
                  targetMethod.instructions.insert(instruction, new MethodInsnNode(184, "com/cosmicdan/nocubes/Main", "shouldSmooth", "(Lnet/minecraft/block/Block;)Z"));
                  targetMethod.instructions.insert(instruction, new VarInsnNode(25, 24));
                  //Main.LOGGER.warn("~~~~ Inserted instructions extra check");
               }

               if(instruction.getOpcode() == 21) {
                  VarInsnNode varInsnNode = (VarInsnNode)instruction;
                  if(varInsnNode.var == 19) {
                     ++varCount;
                     if(varCount == 2) {
                        targetMethod.instructions.insertBefore(instruction, injectedMethod.instructions);
                        //Main.LOGGER.warn("~~~ Inserted instructions render hook");
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
