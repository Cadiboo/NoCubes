package io.github.cadiboo.nocubes.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ExtendedBlockView;

public final class OptifineCompatibility {

//	private static final Class<?> OPTIFINE_CONFIG;
//	private static final MethodHandle CONFIG_IS_SHADERS;
//
//	private static final Class<?> S_VERTEX_BUILDER;
//	private static final MethodHandle PUSH_ENTITY;
//	private static final MethodHandle POP_ENTITY;
//
//	static {
//		Class<?> optifineConfig = null;
//		MethodHandle configIsShaders = null;
//		Class<?> SVertexBuilder = null;
//		MethodHandle pushEntity = null;
//		MethodHandle popEntity = null;
//		try {
//			optifineConfig = ReflectionHelper.getClass(Loader.instance().getModClassLoader(), "Config");
//		} catch (ReflectionHelper.UnableToFindClassException e) {
//			//Optifine doesn't exist;
//		}
//
//		if (optifineConfig != null) {
//			try {
//				configIsShaders = MethodHandles.publicLookup().unreflect(ReflectionHelper.findMethod(optifineConfig, "isShaders", null));
//			} catch (ReflectionHelper.UnableToFindMethodException e) {
//				//Optifine doesn't exist;
//			} catch (IllegalAccessException e) {
//				//Wtf?
//			}
//
//			try {
//				SVertexBuilder = ReflectionHelper.getClass(Loader.instance().getModClassLoader(), "net.optifine.shaders.SVertexBuilder");
//			} catch (ReflectionHelper.UnableToFindClassException e) {
//				//Optifine doesn't exist;
//			}
//
//			{
//				try {
//					pushEntity = MethodHandles.publicLookup().unreflect(ReflectionHelper.findMethod(SVertexBuilder, "pushEntity", null, IBlockState.class, BlockPos.class, IBlockAccess.class, BufferBuilder.class));
//				} catch (ReflectionHelper.UnableToFindMethodException e) {
//					//Optifine doesn't exist;
//				} catch (IllegalAccessException e) {
//					//Wtf?
//				}
//			}
//
//			{
//				try {
//					popEntity = MethodHandles.publicLookup().unreflect(ReflectionHelper.findMethod(SVertexBuilder, "popEntity", null, BufferBuilder.class));
//				} catch (ReflectionHelper.UnableToFindMethodException e) {
//					//Optifine doesn't exist;
//				} catch (IllegalAccessException e) {
//					//Wtf?
//				}
//			}
//		}
//		OPTIFINE_CONFIG = optifineConfig;
//		CONFIG_IS_SHADERS = configIsShaders;
//		S_VERTEX_BUILDER = SVertexBuilder;
//		PUSH_ENTITY = pushEntity;
//		POP_ENTITY = popEntity;
//
//	}
//
//	private static boolean Config_isShaders() {
//
//		if (CONFIG_IS_SHADERS == null) return false;
//
//		try {
//			return (boolean) CONFIG_IS_SHADERS.invokeExact();
//		} catch (final Exception e) {
//			e.printStackTrace();
//		} catch (Throwable throwable) {
//			throw new RuntimeException(throwable);
//		}
//
//		return false;
//
//	}

	public static void pushShaderThing(BlockState blockStateIn, BlockPos blockPosIn, ExtendedBlockView blockAccess, BufferBuilder worldRendererIn) {

//		if (Config.isShaders()) {
//			SVertexBuilder.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
//		}

//		if (Config_isShaders()) {
//			SVertexBuilder_pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
//		}
	}

	public static void popShaderThing(BufferBuilder worldRendererIn) {

//		if (Config.isShaders()) {
//			SVertexBuilder.popEntity(worldRendererIn);
//		}

//		if (Config_isShaders()) {
//			SVertexBuilder_popEntity(worldRendererIn);
//		}

	}

	private static void SVertexBuilder_pushEntity(final BlockState blockStateIn, final BlockPos blockPosIn, final ExtendedBlockView blockAccess, final BufferBuilder worldRendererIn) {

//		if (PUSH_ENTITY == null) return;
//
//		try {
//			PUSH_ENTITY.invokeExact(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
//		} catch (final Exception e) {
//			e.printStackTrace();
//		} catch (Throwable throwable) {
//			throw new RuntimeException(throwable);
//		}

	}

	private static void SVertexBuilder_popEntity(final BufferBuilder worldRendererIn) {

//		if (POP_ENTITY == null) return;
//
//		try {
//			POP_ENTITY.invokeExact(worldRendererIn);
//		} catch (final Exception e) {
//			e.printStackTrace();
//		} catch (Throwable throwable) {
//			throw new RuntimeException(throwable);
//		}

	}

}
