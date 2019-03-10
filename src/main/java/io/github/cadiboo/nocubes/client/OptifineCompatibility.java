package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ObfuscationReflectionHelperCopy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindClassException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static io.github.cadiboo.nocubes.util.ObfuscationReflectionHelperCopy.findMethod;

/**
 * @author Cadiboo
 */
public final class OptifineCompatibility {

	private static final Class<?> OPTIFINE_CONFIG;
	private static final MethodHandle CONFIG_IS_SHADERS;

	private static final Class<?> S_VERTEX_BUILDER;
	private static final MethodHandle PUSH_ENTITY;
	private static final MethodHandle POP_ENTITY;

	static {
		Class<?> optifineConfig = null;
		MethodHandle configIsShaders = null;
		Class<?> SVertexBuilder = null;
		MethodHandle pushEntity = null;
		MethodHandle popEntity = null;
		try {
			optifineConfig = ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), "Config");
		} catch (UnableToFindClassException e) {
			//Optifine doesn't exist;
		}

		if (optifineConfig != null) {
			try {
				configIsShaders = MethodHandles.publicLookup().unreflect(findMethod(optifineConfig, "isShaders", boolean.class));
			} catch (UnableToFindMethodException e) {
				//Optifine doesn't exist;
			} catch (IllegalAccessException e) {
				//Wtf?
			}

			try {
				SVertexBuilder = ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), "net.optifine.shaders.SVertexBuilder");
			} catch (UnableToFindClassException e) {
				//Optifine doesn't exist;
			}

			{
				try {
					pushEntity = MethodHandles.publicLookup().unreflect(findMethod(SVertexBuilder, "pushEntity", void.class, IBlockState.class, BlockPos.class, IBlockAccess.class, BufferBuilder.class));
				} catch (UnableToFindMethodException e) {
					//Optifine doesn't exist;
				} catch (IllegalAccessException e) {
					//Wtf?
				}
			}

			{
				try {
					popEntity = MethodHandles.publicLookup().unreflect(findMethod(SVertexBuilder, "popEntity", void.class, BufferBuilder.class));
				} catch (UnableToFindMethodException e) {
					//Optifine doesn't exist;
				} catch (IllegalAccessException e) {
					//Wtf?
				}
			}
		}
		OPTIFINE_CONFIG = optifineConfig;
		CONFIG_IS_SHADERS = configIsShaders;
		S_VERTEX_BUILDER = SVertexBuilder;
		PUSH_ENTITY = pushEntity;
		POP_ENTITY = popEntity;

	}

	private static boolean Config_isShaders() {

		if (CONFIG_IS_SHADERS == null) return false;

		try {
			return (boolean) CONFIG_IS_SHADERS.invokeExact();
		} catch (final Exception e) {
			e.printStackTrace();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}

		return false;

	}

	public static void pushShaderThing(IBlockState blockStateIn, BlockPos blockPosIn, IBlockAccess blockAccess, BufferBuilder worldRendererIn) {

//		if (Config.isShaders()) {
//			SVertexBuilder.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
//		}

		if (Config_isShaders()) {
			SVertexBuilder_pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
		}
	}

	public static void popShaderThing(BufferBuilder worldRendererIn) {

//		if (Config.isShaders()) {
//			SVertexBuilder.popEntity(worldRendererIn);
//		}

		if (Config_isShaders()) {
			SVertexBuilder_popEntity(worldRendererIn);
		}

	}

	private static void SVertexBuilder_pushEntity(final IBlockState blockStateIn, final BlockPos blockPosIn, final IBlockAccess blockAccess, final BufferBuilder worldRendererIn) {

		if (PUSH_ENTITY == null) return;

		try {
			PUSH_ENTITY.invokeExact(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
		} catch (final Exception e) {
			e.printStackTrace();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}

	}

	private static void SVertexBuilder_popEntity(final BufferBuilder worldRendererIn) {

		if (POP_ENTITY == null) return;

		try {
			POP_ENTITY.invokeExact(worldRendererIn);
		} catch (final Exception e) {
			e.printStackTrace();
		} catch (Throwable throwable) {
			throw new RuntimeException(throwable);
		}

	}

}
