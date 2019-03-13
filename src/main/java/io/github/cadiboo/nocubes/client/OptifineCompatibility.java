package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.ObfuscationReflectionHelperCopy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindClassException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.List;

import static io.github.cadiboo.nocubes.util.ObfuscationReflectionHelperCopy.findMethod;

/**
 * @author Cadiboo
 */
public final class OptifineCompatibility {

	public static final boolean OPTIFINE_INSTALLED;
	static {
		boolean optifineInstalled;
		try {
			ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), "Config");
			optifineInstalled = true;
		} catch (UnableToFindClassException e) {
			// Its ok, This just means that OptiFine isn't installed
			optifineInstalled = false;
			NoCubes.NO_CUBES_LOG.info("OptifineCompatibility: OptiFine not detected.");
		}
		OPTIFINE_INSTALLED = optifineInstalled;
	}

	public static final class Config {

		private static final Class<?> clazz;
		static {
			if (!OPTIFINE_INSTALLED) {
				clazz = null;
			} else {
				final String className = "Config";
				try {
					clazz = ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), className);
				} catch (UnableToFindClassException e) {
					final CrashReport crashReport = new CrashReport("Unable to find class \"" + className + "\". Class does not exist!", e);
					crashReport.makeCategory("Finding Class");
					throw new ReportedException(crashReport);
				}
			}
		}

		private static final MethodHandle CONFIG_IS_SHADERS;
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

		private static boolean isShaders() {
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

	public static final class SVertexBuilder {

		private static final Class<?> S_VERTEX_BUILDER;
		private static final MethodHandle PUSH_ENTITY;
		private static final MethodHandle POP_ENTITY;
		static {

			if (!OPTIFINE_INSTALLED) {
				S_VERTEX_BUILDER = null;
				PUSH_ENTITY = null;
				POP_ENTITY = null;
			} else {
				String className = "net.optifine.shaders.SVertexBuilder";
				try {
					S_VERTEX_BUILDER = ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), className);
				} catch (UnableToFindClassException e) {
					final CrashReport crashReport = new CrashReport("Unable to find class \"" + className + "\". Class does not exist!", e);
					crashReport.makeCategory("Finding Class");
					throw new ReportedException(crashReport);
				}
				{
					final String methodName = "pushEntity";
					try {
						PUSH_ENTITY = MethodHandles.publicLookup().unreflect(findMethod(S_VERTEX_BUILDER, methodName, void.class, IBlockState.class, BlockPos.class, IBlockAccess.class, BufferBuilder.class));
					} catch (UnableToFindMethodException | IllegalAccessException e) {
						final CrashReport crashReport = new CrashReport("Unable to find method \"" + methodName + "\" for class \"" + S_VERTEX_BUILDER + "\".", e);
						crashReport.makeCategory("Finding Method");
						throw new ReportedException(crashReport);
					}

				}
				{
					final String methodName = "popEntity";
					try {
						POP_ENTITY = MethodHandles.publicLookup().unreflect(findMethod(S_VERTEX_BUILDER, methodName, void.class, BufferBuilder.class));
					} catch (UnableToFindMethodException | IllegalAccessException e) {
						final CrashReport crashReport = new CrashReport("Unable to find method \"" + methodName + "\" for class \"" + S_VERTEX_BUILDER + "\". Method does not exist!", e);
						crashReport.makeCategory("Finding Method");
						throw new ReportedException(crashReport);
					}

				}
			}

		}

		public static void pushEntity(final IBlockState blockStateIn, final BlockPos blockPosIn, final IBlockAccess blockAccess, final BufferBuilder worldRendererIn) {
			if (!OPTIFINE_INSTALLED) {
				return;
			}

			try {
				PUSH_ENTITY.invokeExact(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
			} catch (final ReportedException e) {
				throw e;
			} catch (final Throwable throwable) {
				throw new RuntimeException(throwable);
			}

		}

		public static void popEntity(final BufferBuilder worldRendererIn) {
			if (!OPTIFINE_INSTALLED) {
				return;
			}

			try {
				POP_ENTITY.invokeExact(worldRendererIn);
			} catch (final ReportedException e) {
				throw e;
			} catch (final Throwable throwable) {
				throw new RuntimeException(throwable);
			}

		}

	}

	public static final class BufferBuilderOF {

		private static final MethodHandle GET_RENDER_ENV;
		static {
			if (!OPTIFINE_INSTALLED) {
				GET_RENDER_ENV = null;
			} else {
				final String methodName = "getRenderEnv";
				Class<?> clazz = BufferBuilder.class;
				final Method method;
				try {
					method = clazz.getDeclaredMethod(methodName, IBlockAccess.class, IBlockState.class, BlockPos.class);
				} catch (NoSuchMethodException e) {
					final CrashReport crashReport = new CrashReport("Unable to find method \"" + methodName + "\" for class \"" + clazz + "\". Method does not exist!", e);
					crashReport.makeCategory("Finding Method");
					throw new ReportedException(crashReport);
				}
				method.setAccessible(true);

				try {
					GET_RENDER_ENV = MethodHandles.publicLookup().unreflect(method);
				} catch (IllegalAccessException e) {
					final CrashReport crashReport = new CrashReport("Unable to unreflect method \"" + methodName + "\" for class \"" + clazz + "\".", e);
					crashReport.makeCategory("Unreflecting Method");
					throw new ReportedException(crashReport);
				}
			}
		}

		@Nullable
		public static Object getRenderEnv(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final IBlockAccess blockAccess, @Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
			if (!OPTIFINE_INSTALLED) {
				return null;
			}

			try {
				return GET_RENDER_ENV.invoke(bufferBuilder, blockAccess, state, pos);
			} catch (final ReportedException e) {
				throw e;
			} catch (final Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		}

	}

	public static final class RenderEnv {

		@Nullable
		public static final Class<?> clazz;
		static {
			if (!OPTIFINE_INSTALLED) {
				clazz = null;
			} else {
				final String className = "net.optifine.render.RenderEnv";
				try {
					clazz = ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), className);
				} catch (UnableToFindClassException e) {
					final CrashReport crashReport = new CrashReport("Unable to find class \"" + className + "\". Class does not exist!", e);
					crashReport.makeCategory("Finding Class");
					throw new ReportedException(crashReport);
				}
			}
		}

	}

	public static final class BlockModelCustomizer {

		@Nullable
		public static final Class<?> clazz;
		static {
			if (!OPTIFINE_INSTALLED) {
				clazz = null;
			} else {
				final String className = "net.optifine.model.BlockModelCustomizer";
				try {
					clazz = ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), className);
				} catch (UnableToFindClassException e) {
					final CrashReport crashReport = new CrashReport("Unable to find class \"" + className + "\". Class does not exist!", e);
					crashReport.makeCategory("Finding Class");
					throw new ReportedException(crashReport);
				}
			}
		}

		private static final MethodHandle GET_RENDER_MODEL;
		private static final MethodHandle GET_RENDER_QUADS;
		static {
			if (!OPTIFINE_INSTALLED) {
				GET_RENDER_MODEL = null;
				GET_RENDER_QUADS = null;
			} else {
				{
					final String methodName = "getRenderModel";
					final Method method;
					try {
						method = clazz.getDeclaredMethod(methodName, IBakedModel.class, IBlockState.class, RenderEnv.clazz);
					} catch (NoSuchMethodException e) {
						final CrashReport crashReport = new CrashReport("Unable to find method \"" + methodName + "\" for class \"" + clazz + "\". Method does not exist!", e);
						crashReport.makeCategory("Finding Method");
						throw new ReportedException(crashReport);
					}
					method.setAccessible(true);

					try {
						GET_RENDER_MODEL = MethodHandles.publicLookup().unreflect(method);
					} catch (IllegalAccessException e) {
						final CrashReport crashReport = new CrashReport("Unable to unreflect method \"" + methodName + "\" for class \"" + clazz + "\".", e);
						crashReport.makeCategory("Unreflecting Method");
						throw new ReportedException(crashReport);
					}
				}
				{
					final String methodName = "getRenderQuads";
					final Method method;
					try {
						method = clazz.getDeclaredMethod(methodName, List.class, IBlockAccess.class, IBlockState.class, BlockPos.class, EnumFacing.class, BlockRenderLayer.class, long.class, RenderEnv.clazz);
					} catch (NoSuchMethodException e) {
						final CrashReport crashReport = new CrashReport("Unable to find method \"" + methodName + "\" for class \"" + clazz + "\". Method does not exist!", e);
						crashReport.makeCategory("Finding Method");
						throw new ReportedException(crashReport);
					}
					method.setAccessible(true);

					try {
						GET_RENDER_QUADS = MethodHandles.publicLookup().unreflect(method);
					} catch (IllegalAccessException e) {
						final CrashReport crashReport = new CrashReport("Unable to unreflect method \"" + methodName + "\" for class \"" + clazz + "\".", e);
						crashReport.makeCategory("Unreflecting Method");
						throw new ReportedException(crashReport);
					}
				}
			}
		}

		@Nullable
		public static IBakedModel getRenderModel(final IBakedModel model, final IBlockState state, final Object renderEnv) {
			if (!OPTIFINE_INSTALLED) {
				return null;
			}

			try {
				return (IBakedModel) GET_RENDER_MODEL.invoke(model, state, renderEnv);
			} catch (final ReportedException e) {
				throw e;
			} catch (final Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		}

		@Nullable
		@SuppressWarnings("unchecked")
		public static List<BakedQuad> getRenderQuads(final List<BakedQuad> quads, final IBlockAccess blockAccess, final IBlockState state, final BlockPos pos, final EnumFacing facing, final BlockRenderLayer blockRenderLayer, final long rand, final Object renderEnv) {
			if (!OPTIFINE_INSTALLED) {
				return null;
			}

			try {
				return (List<BakedQuad>) GET_RENDER_QUADS.invoke(quads, blockAccess, state, pos, facing, blockRenderLayer, rand, renderEnv);
			} catch (final ReportedException e) {
				throw e;
			} catch (final Throwable throwable) {
				throw new RuntimeException(throwable);
			}
		}

	}

	public static void pushShaderThing(IBlockState blockStateIn, BlockPos blockPosIn, IBlockAccess blockAccess, BufferBuilder worldRendererIn) {
		if (Config.isShaders()) {
			SVertexBuilder.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
		}
	}

	public static void popShaderThing(BufferBuilder worldRendererIn) {
		if (Config.isShaders()) {
			SVertexBuilder.popEntity(worldRendererIn);
		}
	}

}
