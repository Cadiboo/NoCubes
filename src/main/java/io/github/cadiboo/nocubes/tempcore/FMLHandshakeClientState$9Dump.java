package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class FMLHandshakeClientState$9Dump implements Opcodes {

	static byte[] dump() {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_8, ACC_SUPER + ACC_ENUM, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9", null, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState", null);

		cw.visitSource("FMLHandshakeClientState.java", null);

		cw.visitOuterClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState", null, null);

		cw.visitInnerClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9", null, null, ACC_ENUM);

		cw.visitInnerClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$HandshakeAck", "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", "HandshakeAck", ACC_PUBLIC + ACC_STATIC);

		cw.visitInnerClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", "S2CConfigData", ACC_PUBLIC + ACC_STATIC);

		{
			fv = cw.visitField(ACC_PRIVATE, "hasRecievedHandshakeAckPacket", "Z", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(0, "<init>", "(Ljava/lang/String;I)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(160, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState", "<init>", "(Ljava/lang/String;ILnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;)V", false);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(162, l1);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9", "hasRecievedHandshakeAckPacket", "Z");
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLineNumber(160, l2);
			mv.visitInsn(RETURN);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9;", null, l0, l3, 0);
			mv.visitMaxs(4, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "accept", "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage;Ljava/util/function/Consumer;)V", "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage;Ljava/util/function/Consumer<-Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;>;)V", null);
			{
				av0 = mv.visitParameterAnnotation(1, "Ljavax/annotation/Nullable;", true);
				av0.visitEnd();
			}
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(167, l0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(INSTANCEOF, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$HandshakeAck");
			Label l1 = new Label();
			mv.visitJumpInsn(IFEQ, l1);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLineNumber(168, l2);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9", "hasRecievedHandshakeAckPacket", "Z");
			Label l3 = new Label();
			mv.visitJumpInsn(IFNE, l3);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLineNumber(169, l4);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_1);
			mv.visitFieldInsn(PUTFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9", "hasRecievedHandshakeAckPacket", "Z");
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLineNumber(170, l5);
			mv.visitInsn(RETURN);
			mv.visitLabel(l3);
			mv.visitLineNumber(172, l3);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTFIELD, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9", "hasRecievedHandshakeAckPacket", "Z");
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitLineNumber(173, l6);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitFieldInsn(GETSTATIC, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9", "PENDINGCOMPLETE", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;");
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", true);
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitLineNumber(174, l7);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(NEW, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$HandshakeAck");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9", "ordinal", "()I", false);
			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$HandshakeAck", "<init>", "(I)V", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/ChannelHandlerContext", "writeAndFlush", "(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;", true);
			mv.visitFieldInsn(GETSTATIC, "io/netty/channel/ChannelFutureListener", "FIRE_EXCEPTION_ON_FAILURE", "Lio/netty/channel/ChannelFutureListener;");
			mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/ChannelFuture", "addListener", "(Lio/netty/util/concurrent/GenericFutureListener;)Lio/netty/channel/ChannelFuture;", true);
			mv.visitInsn(POP);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitLineNumber(175, l8);
			Label l9 = new Label();
			mv.visitJumpInsn(GOTO, l9);
			mv.visitLabel(l1);
			mv.visitLineNumber(176, l1);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitTypeInsn(CHECKCAST, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData");
			mv.visitVarInsn(ASTORE, 4);
			Label l10 = new Label();
			mv.visitLabel(l10);
			mv.visitLineNumber(177, l10);
			mv.visitFieldInsn(GETSTATIC, "io/github/cadiboo/nocubes/NoCubes", "LOGGER", "Lorg/apache/logging/log4j/Logger;");
			mv.visitLdcInsn("Received Config: {}");
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "getFileName", "()Ljava/lang/String;", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "info", "(Ljava/lang/String;Ljava/lang/Object;)V", true);
			Label l11 = new Label();
			mv.visitLabel(l11);
			mv.visitLineNumber(178, l11);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKESTATIC, "io/github/cadiboo/nocubes/config/ConfigHandshakeHandler", "handleConfigSync", "(Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;)V", false);
			mv.visitLabel(l9);
			mv.visitLineNumber(180, l9);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			Label l12 = new Label();
			mv.visitLabel(l12);
			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState$9;", null, l0, l12, 0);
			mv.visitLocalVariable("ctx", "Lio/netty/channel/ChannelHandlerContext;", null, l0, l12, 1);
			mv.visitLocalVariable("msg", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage;", null, l0, l12, 2);
			mv.visitLocalVariable("cons", "Ljava/util/function/Consumer;", "Ljava/util/function/Consumer<-Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeClientState;>;", l0, l12, 3);
			mv.visitLocalVariable("pkt", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;", null, l10, l9, 4);
			mv.visitMaxs(4, 5);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

}
