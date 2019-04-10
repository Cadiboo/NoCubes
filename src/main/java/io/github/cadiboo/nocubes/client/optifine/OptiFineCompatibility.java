package io.github.cadiboo.nocubes.client.optifine;

import cpw.mods.modlauncher.Launcher;
import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper.UnableToFindMethodException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Random;

import static io.github.cadiboo.nocubes.util.reflect.ObfuscationReflectionHelperCopy.findMethod;

/**
 * @author Cadiboo
 */
public final class OptiFineCompatibility {

	public static final boolean OPTIFINE_INSTALLED;
	static {
		boolean optiFineInstalled;
		try {
			Class.forName("Config", false, OptiFineCompatibility.class.getClassLoader());
			optiFineInstalled = true;
		} catch (ClassNotFoundException e) {
			// Its ok, This just means that OptiFine isn't installed
			optiFineInstalled = false;
			NoCubes.NO_CUBES_LOG.info("OptiFineCompatibility: OptiFine not detected.");
		}
		OPTIFINE_INSTALLED = optiFineInstalled;
	}

	public static void pushShaderThing(@Nonnull final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final IBlockReader blockAccess, @Nonnull final BufferBuilder worldRendererIn) {
		if (!OPTIFINE_INSTALLED) {
			return;
		}
		SVertexBuilderOF.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
	}

	public static void popShaderThing(@Nonnull final BufferBuilder worldRendererIn) {
		if (!OPTIFINE_INSTALLED) {
			return;
		}
		SVertexBuilderOF.popEntity(worldRendererIn);
	}

	public static final class Config {

		private static final Class<?> clazz;
		private static final MethodHandle CONFIG_IS_SHADERS;
		static {
			if (!OPTIFINE_INSTALLED) {
				clazz = null;
			} else {
				final String className = "Config";
				try {
					clazz = Class.forName(className, false, OptiFineCompatibility.class.getClassLoader());
				} catch (ClassNotFoundException e) {
					final CrashReport crashReport = new CrashReport("Unable to find class \"" + className + "\". Class does not exist!", e);
					crashReport.makeCategory("Finding Class");
					throw new ReportedException(crashReport);
				}
			}
		}

		static {
			if (!OPTIFINE_INSTALLED) {
				CONFIG_IS_SHADERS = null;
			} else {
				final String methodName = "isShaders";
				try {
					CONFIG_IS_SHADERS = MethodHandles.publicLookup().unreflect(findMethod(Config.clazz, methodName, boolean.class));
				} catch (UnableToFindMethodException | IllegalAccessException e) {
					final CrashReport crashReport = new CrashReport("Unable to find method \"" + methodName + "\" for class \"" + Config.clazz + "\".", e);
					crashReport.makeCategory("Finding Method");
					throw new ReportedException(crashReport);
				}
			}
		}

		public static boolean isShaders() {
			if (!OPTIFINE_INSTALLED) {
				return false;
			}

			try {
				return (boolean) CONFIG_IS_SHADERS.invokeExact();
			} catch (final ReportedException e) {
				throw e;
			} catch (final Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		}

	}

	public static final class SVertexBuilderOF {

		public static void pushEntity(@Nonnull final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final IBlockReader blockAccess, @Nonnull final BufferBuilder worldRendererIn) {
			if (!OPTIFINE_INSTALLED) {
				return;
			}
			HardOptiFineCompatibility.SVertexBuilderOF.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
		}

		public static void popEntity(@Nonnull final BufferBuilder worldRendererIn) {
			if (!OPTIFINE_INSTALLED) {
				return;
			}
			HardOptiFineCompatibility.SVertexBuilderOF.popEntity(worldRendererIn);
		}

	}

	public static final class BufferBuilderOF {

		@Nullable
		public static Object getRenderEnv(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final IBlockReader blockAccess, @Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
			if (!OPTIFINE_INSTALLED) {
				return null;
			}

			return HardOptiFineCompatibility.BufferBuilderOF.getRenderEnv(bufferBuilder, blockAccess, state, pos);
		}

	}

	public static final class BlockModelCustomizer {

		@Nullable
		public static IBakedModel getRenderModel(@Nonnull final IBakedModel model, @Nonnull final IBlockState state, @Nonnull final Object renderEnv) {
			if (!OPTIFINE_INSTALLED) {
				return null;
			}

			return HardOptiFineCompatibility.BlockModelCustomizerOF.getRenderModel(model, state, renderEnv);
		}

		@Nullable
		public static List<BakedQuad> getRenderQuads(@Nonnull final List<BakedQuad> quads, @Nonnull final IBlockReader blockAccess, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final EnumFacing facing, @Nonnull final BlockRenderLayer blockRenderLayer, @Nonnull final Random rand, @Nonnull final Object renderEnv) {
			if (!OPTIFINE_INSTALLED) {
				return null;
			}

			return HardOptiFineCompatibility.BlockModelCustomizerOF.getRenderQuads(quads, blockAccess, state, pos, facing, blockRenderLayer, rand, renderEnv);
		}

	}

}
