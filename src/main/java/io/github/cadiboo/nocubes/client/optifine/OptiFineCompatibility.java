package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Cadiboo
 */
public final class OptiFineCompatibility {

	public static final boolean OPTIFINE_INSTALLED = OptiFineLocator.isOptiFineInstalledAndCompatible();

	public static void pushShaderThing(
			@Nonnull final BlockState blockStateIn,
			@Nonnull final BlockPos blockPosIn,
			@Nonnull final IEnviromentBlockReader blockAccess,
			@Nonnull final BufferBuilder worldRendererIn
	) {
		if (!OPTIFINE_INSTALLED) {
			return;
		} else {
			HardOptiFineCompatibility.pushShaderThing(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
		}
	}

	public static void pushShaderThing(
			@Nonnull final IFluidState iFluidState,
			@Nonnull final BlockPos blockPosIn,
			@Nonnull final IEnviromentBlockReader blockAccess,
			@Nonnull final BufferBuilder worldRendererIn
	) {
		pushShaderThing(iFluidState.getBlockState(), blockPosIn, blockAccess, worldRendererIn);
	}

	public static void popShaderThing(@Nonnull final BufferBuilder worldRendererIn) {
		if (!OPTIFINE_INSTALLED) {
			return;
		} else {
			HardOptiFineCompatibility.popShaderThing(worldRendererIn);
		}
	}

	public static boolean isChunkCacheOF(@Nonnull final IEnviromentBlockReader reader) {
		if (!OPTIFINE_INSTALLED) {
			return false;
		} else {
			return HardOptiFineCompatibility.isChunkCacheOF(reader);
		}
	}

	@Nonnull
	public static ChunkRenderCache getChunkRenderCache(@Nonnull final IEnviromentBlockReader reader) {
		if (!OPTIFINE_INSTALLED) {
			throw new OptiFineNotPresentException();
		} else {
			return HardOptiFineCompatibility.getChunkRenderCache(reader);
		}
	}

	public static final class BufferBuilderOF {

		@Nonnull
		public static Object getRenderEnv(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final BlockState state, @Nonnull final BlockPos pos) {
			if (!OPTIFINE_INSTALLED) {
				throw new OptiFineNotPresentException();
			}
			return HardOptiFineCompatibility.BufferBuilderOF.getRenderEnv(bufferBuilder, state, pos);
		}

	}

	public static final class BlockModelCustomizer {

		@Nonnull
		public static IBakedModel getRenderModel(
				@Nonnull final IBakedModel model,
				@Nonnull final BlockState blockState,
				@Nonnull final Object renderEnv
		) {
			if (!OPTIFINE_INSTALLED) {
				throw new OptiFineNotPresentException();
			}
			return HardOptiFineCompatibility.BlockModelCustomizerOF.getRenderModel(model, blockState, renderEnv);
		}

		@Nonnull
		public static List<BakedQuad> getRenderQuads(
				@Nonnull final List<BakedQuad> quads,
				@Nonnull final IEnviromentBlockReader reader,
				@Nonnull final BlockState blockState,
				@Nonnull final BlockPos pos,
				@Nonnull final Direction direction,
				@Nonnull final BlockRenderLayer blockRenderLayer,
				final long rand,
				@Nonnull final Object renderEnv
		) {
			if (!OPTIFINE_INSTALLED) {
				throw new OptiFineNotPresentException();
			}
			return HardOptiFineCompatibility.BlockModelCustomizerOF.getRenderQuads(quads, reader, blockState, pos, direction, blockRenderLayer, rand, renderEnv);
		}

	}

}
