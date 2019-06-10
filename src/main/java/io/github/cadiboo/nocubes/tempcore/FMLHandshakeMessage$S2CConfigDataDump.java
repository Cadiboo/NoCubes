//package io.github.cadiboo.nocubes.tempcore;
//
//import org.objectweb.asm.AnnotationVisitor;
//import org.objectweb.asm.ClassWriter;
//import org.objectweb.asm.FieldVisitor;
//import org.objectweb.asm.Label;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;
//
//final class FMLHandshakeMessage$S2CConfigDataDump implements Opcodes {
//
//	static byte[] dump() {
//
//		ClassWriter cw = new ClassWriter(0);
//		FieldVisitor fv;
//		MethodVisitor mv;
//		AnnotationVisitor av0;
//
//		cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", null, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", null);
//
//		cw.visitSource("FMLHandshakeMessage.java", null);
//
//		cw.visitInnerClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", "S2CConfigData", ACC_PUBLIC + ACC_STATIC);
//
//		{
//			fv = cw.visitField(ACC_PRIVATE, "fileName", "Ljava/lang/String;", null, null);
//			fv.visitEnd();
//		}
//		{
//			fv = cw.visitField(ACC_PRIVATE, "fileData", "[B", null, null);
//			fv.visitEnd();
//		}
//		{
//			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
//			mv.visitCode();
//			Label l0 = new Label();
//			mv.visitLabel(l0);
//			mv.visitLineNumber(294, l0);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", "<init>", "()V", false);
//			Label l1 = new Label();
//			mv.visitLabel(l1);
//			mv.visitLineNumber(295, l1);
//			mv.visitInsn(RETURN);
//			Label l2 = new Label();
//			mv.visitLabel(l2);
//			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;", null, l0, l2, 0);
//			mv.visitMaxs(1, 1);
//			mv.visitEnd();
//		}
//		{
//			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/String;[B)V", null, null);
//			mv.visitCode();
//			Label l0 = new Label();
//			mv.visitLabel(l0);
//			mv.visitLineNumber(297, l0);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", "<init>", "()V", false);
//			Label l1 = new Label();
//			mv.visitLabel(l1);
//			mv.visitLineNumber(298, l1);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitVarInsn(ALOAD, 1);
//			mv.visitFieldInsn(PUTFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "fileName", "Ljava/lang/String;");
//			Label l2 = new Label();
//			mv.visitLabel(l2);
//			mv.visitLineNumber(299, l2);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitVarInsn(ALOAD, 2);
//			mv.visitFieldInsn(PUTFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "fileData", "[B");
//			Label l3 = new Label();
//			mv.visitLabel(l3);
//			mv.visitLineNumber(300, l3);
//			mv.visitInsn(RETURN);
//			Label l4 = new Label();
//			mv.visitLabel(l4);
//			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;", null, l0, l4, 0);
//			mv.visitLocalVariable("configFileName", "Ljava/lang/String;", null, l0, l4, 1);
//			mv.visitLocalVariable("configFileData", "[B", null, l0, l4, 2);
//			mv.visitMaxs(2, 3);
//			mv.visitEnd();
//		}
//		{
//			mv = cw.visitMethod(ACC_PUBLIC, "fromBytes", "(Lio/netty/buffer/ByteBuf;)V", null, null);
//			mv.visitCode();
//			Label l0 = new Label();
//			mv.visitLabel(l0);
//			mv.visitLineNumber(304, l0);
//			mv.visitTypeInsn(NEW, "net/minecraft/network/PacketBuffer");
//			mv.visitInsn(DUP);
//			mv.visitVarInsn(ALOAD, 1);
//			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/network/PacketBuffer", "<init>", "(Lio/netty/buffer/ByteBuf;)V", false);
//			mv.visitVarInsn(ASTORE, 2);
//			Label l1 = new Label();
//			mv.visitLabel(l1);
//			mv.visitLineNumber(305, l1);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitVarInsn(ALOAD, 2);
//			mv.visitIntInsn(SIPUSH, 128);
//			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "readString", "(I)Ljava/lang/String;", false);
//			mv.visitFieldInsn(PUTFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "fileName", "Ljava/lang/String;");
//			Label l2 = new Label();
//			mv.visitLabel(l2);
//			mv.visitLineNumber(306, l2);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitVarInsn(ALOAD, 2);
//			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "readByteArray", "()[B", false);
//			mv.visitFieldInsn(PUTFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "fileData", "[B");
//			Label l3 = new Label();
//			mv.visitLabel(l3);
//			mv.visitLineNumber(307, l3);
//			mv.visitInsn(RETURN);
//			Label l4 = new Label();
//			mv.visitLabel(l4);
//			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;", null, l0, l4, 0);
//			mv.visitLocalVariable("byteBuf", "Lio/netty/buffer/ByteBuf;", null, l0, l4, 1);
//			mv.visitLocalVariable("buffer", "Lnet/minecraft/network/PacketBuffer;", null, l1, l4, 2);
//			mv.visitMaxs(3, 3);
//			mv.visitEnd();
//		}
//		{
//			mv = cw.visitMethod(ACC_PUBLIC, "toBytes", "(Lio/netty/buffer/ByteBuf;)V", null, null);
//			mv.visitCode();
//			Label l0 = new Label();
//			mv.visitLabel(l0);
//			mv.visitLineNumber(311, l0);
//			mv.visitTypeInsn(NEW, "net/minecraft/network/PacketBuffer");
//			mv.visitInsn(DUP);
//			mv.visitVarInsn(ALOAD, 1);
//			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraft/network/PacketBuffer", "<init>", "(Lio/netty/buffer/ByteBuf;)V", false);
//			mv.visitVarInsn(ASTORE, 2);
//			Label l1 = new Label();
//			mv.visitLabel(l1);
//			mv.visitLineNumber(312, l1);
//			mv.visitVarInsn(ALOAD, 2);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitFieldInsn(GETFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "fileName", "Ljava/lang/String;");
//			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "writeString", "(Ljava/lang/String;)Lnet/minecraft/network/PacketBuffer;", false);
//			mv.visitInsn(POP);
//			Label l2 = new Label();
//			mv.visitLabel(l2);
//			mv.visitLineNumber(313, l2);
//			mv.visitVarInsn(ALOAD, 2);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitFieldInsn(GETFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "fileData", "[B");
//			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/network/PacketBuffer", "writeByteArray", "([B)Lnet/minecraft/network/PacketBuffer;", false);
//			mv.visitInsn(POP);
//			Label l3 = new Label();
//			mv.visitLabel(l3);
//			mv.visitLineNumber(314, l3);
//			mv.visitInsn(RETURN);
//			Label l4 = new Label();
//			mv.visitLabel(l4);
//			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;", null, l0, l4, 0);
//			mv.visitLocalVariable("byteBuf", "Lio/netty/buffer/ByteBuf;", null, l0, l4, 1);
//			mv.visitLocalVariable("buffer", "Lnet/minecraft/network/PacketBuffer;", null, l1, l4, 2);
//			mv.visitMaxs(3, 3);
//			mv.visitEnd();
//		}
//		{
//			mv = cw.visitMethod(ACC_PUBLIC, "getFileName", "()Ljava/lang/String;", null, null);
//			mv.visitCode();
//			Label l0 = new Label();
//			mv.visitLabel(l0);
//			mv.visitLineNumber(317, l0);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitFieldInsn(GETFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "fileName", "Ljava/lang/String;");
//			mv.visitInsn(ARETURN);
//			Label l1 = new Label();
//			mv.visitLabel(l1);
//			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;", null, l0, l1, 0);
//			mv.visitMaxs(1, 1);
//			mv.visitEnd();
//		}
//		{
//			mv = cw.visitMethod(ACC_PUBLIC, "getBytes", "()[B", null, null);
//			mv.visitCode();
//			Label l0 = new Label();
//			mv.visitLabel(l0);
//			mv.visitLineNumber(321, l0);
//			mv.visitVarInsn(ALOAD, 0);
//			mv.visitFieldInsn(GETFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "fileData", "[B");
//			mv.visitInsn(ARETURN);
//			Label l1 = new Label();
//			mv.visitLabel(l1);
//			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;", null, l0, l1, 0);
//			mv.visitMaxs(1, 1);
//			mv.visitEnd();
//		}
//		cw.visitEnd();
//
//		return cw.toByteArray();
//	}
//
//}
