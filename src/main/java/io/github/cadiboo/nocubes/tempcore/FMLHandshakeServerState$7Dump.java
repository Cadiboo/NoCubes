package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class FMLHandshakeServerState$7Dump implements Opcodes {

	static byte[] dump() {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;
		AnnotationVisitor av0;

		cw.visit(V1_8, ACC_SUPER + ACC_ENUM, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7", null, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState", null);

		cw.visitSource("FMLHandshakeServerState.java", null);

		cw.visitOuterClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState", null, null);

		cw.visitInnerClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$HandshakeAck", "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", "HandshakeAck", ACC_PUBLIC + ACC_STATIC);

		cw.visitInnerClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData", "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", "S2CConfigData", ACC_PUBLIC + ACC_STATIC);

		cw.visitInnerClass("net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7", null, null, ACC_ENUM);

		{
			mv = cw.visitMethod(0, "<init>", "(Ljava/lang/String;I)V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(136, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ILOAD, 2);
			mv.visitInsn(ACONST_NULL);
			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState", "<init>", "(Ljava/lang/String;ILnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;)V", false);
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7;", null, l0, l1, 0);
			mv.visitMaxs(4, 3);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "accept", "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage;Ljava/util/function/Consumer;)V", "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage;Ljava/util/function/Consumer<-Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;>;)V", null);
			{
				av0 = mv.visitParameterAnnotation(1, "Ljavax/annotation/Nullable;", true);
				av0.visitEnd();
			}
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(139, l0);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitFieldInsn(GETSTATIC, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7", "COMPLETE", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;");
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/function/Consumer", "accept", "(Ljava/lang/Object;)V", true);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(140, l1);
			mv.visitFieldInsn(GETSTATIC, "io/github/cadiboo/nocubes/config/ConfigTracker", "INSTANCE", "Lio/github/cadiboo/nocubes/config/ConfigTracker;");
			mv.visitInsn(ICONST_0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "io/github/cadiboo/nocubes/config/ConfigTracker", "syncConfigs", "(Z)Ljava/util/List;", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
			mv.visitVarInsn(ASTORE, 5);
			Label l2 = new Label();
			mv.visitJumpInsn(GOTO, l2);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitFrame(Opcodes.F_FULL, 6, new Object[]{"net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7", "io/netty/channel/ChannelHandlerContext", "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage", "java/util/function/Consumer", Opcodes.TOP, "java/util/Iterator"}, 0, new Object[]{});
			mv.visitVarInsn(ALOAD, 5);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
			mv.visitTypeInsn(CHECKCAST, "org/apache/commons/lang3/tuple/Pair");
			mv.visitVarInsn(ASTORE, 4);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLineNumber(141, l4);
			mv.visitFieldInsn(GETSTATIC, "io/github/cadiboo/nocubes/NoCubes", "LOGGER", "Lorg/apache/logging/log4j/Logger;");
			mv.visitLdcInsn("Sending Config: {}");
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/commons/lang3/tuple/Pair", "getKey", "()Ljava/lang/Object;", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "org/apache/logging/log4j/Logger", "info", "(Ljava/lang/String;Ljava/lang/Object;)V", true);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLineNumber(142, l5);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitVarInsn(ALOAD, 4);
			mv.visitMethodInsn(INVOKEVIRTUAL, "org/apache/commons/lang3/tuple/Pair", "getValue", "()Ljava/lang/Object;", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/ChannelHandlerContext", "writeAndFlush", "(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;", true);
			mv.visitFieldInsn(GETSTATIC, "io/netty/channel/ChannelFutureListener", "FIRE_EXCEPTION_ON_FAILURE", "Lio/netty/channel/ChannelFutureListener;");
			mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/ChannelFuture", "addListener", "(Lio/netty/util/concurrent/GenericFutureListener;)Lio/netty/channel/ChannelFuture;", true);
			mv.visitInsn(POP);
			mv.visitLabel(l2);
			mv.visitLineNumber(140, l2);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitVarInsn(ALOAD, 5);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
			mv.visitJumpInsn(IFNE, l3);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitLineNumber(144, l6);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitTypeInsn(NEW, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$HandshakeAck");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7", "ordinal", "()I", false);
			mv.visitMethodInsn(INVOKESPECIAL, "net/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$HandshakeAck", "<init>", "(I)V", false);
			mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/ChannelHandlerContext", "writeAndFlush", "(Ljava/lang/Object;)Lio/netty/channel/ChannelFuture;", true);
			mv.visitFieldInsn(GETSTATIC, "io/netty/channel/ChannelFutureListener", "FIRE_EXCEPTION_ON_FAILURE", "Lio/netty/channel/ChannelFutureListener;");
			mv.visitMethodInsn(INVOKEINTERFACE, "io/netty/channel/ChannelFuture", "addListener", "(Lio/netty/util/concurrent/GenericFutureListener;)Lio/netty/channel/ChannelFuture;", true);
			mv.visitInsn(POP);
			Label l7 = new Label();
			mv.visitLabel(l7);
			mv.visitLineNumber(145, l7);
			mv.visitInsn(RETURN);
			Label l8 = new Label();
			mv.visitLabel(l8);
			mv.visitLocalVariable("this", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState$7;", null, l0, l8, 0);
			mv.visitLocalVariable("ctx", "Lio/netty/channel/ChannelHandlerContext;", null, l0, l8, 1);
			mv.visitLocalVariable("msg", "Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage;", null, l0, l8, 2);
			mv.visitLocalVariable("cons", "Ljava/util/function/Consumer;", "Ljava/util/function/Consumer<-Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeServerState;>;", l0, l8, 3);
			mv.visitLocalVariable("pair", "Lorg/apache/commons/lang3/tuple/Pair;", "Lorg/apache/commons/lang3/tuple/Pair<Ljava/lang/String;Lnet/minecraftforge/fml/common/network/handshake/FMLHandshakeMessage$S2CConfigData;>;", l4, l2, 4);
			mv.visitMaxs(4, 6);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}

}
