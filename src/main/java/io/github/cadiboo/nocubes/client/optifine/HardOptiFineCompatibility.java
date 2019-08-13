package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.optifine.Config;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.override.ChunkCacheOF;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Cadiboo
 */
final class HardOptiFineCompatibility {

	static boolean isChunkCacheOF(final Object obj) {
		return obj instanceof ChunkCacheOF;
	}

	@Nonnull
	static ChunkRenderCache getChunkRenderCache(final IEnviromentBlockReader reader) {
		return ((ChunkCacheOF) reader).chunkCache;
	}

	static void pushShaderThing(
			@Nonnull final BlockState blockState,
			@Nonnull final BlockPos pos,
			@Nonnull final IEnviromentBlockReader reader,
			@Nonnull final BufferBuilder bufferBuilder
	) {
		if (Config.isShaders()) {
			SVertexBuilder.pushEntity(blockState, pos, reader, bufferBuilder);
		}
	}

	static void popShaderThing(@Nonnull final BufferBuilder bufferBuilder) {
		if (Config.isShaders()) {
			SVertexBuilder.popEntity(bufferBuilder);
		}
	}

	static final class BufferBuilderOF {

		@Nonnull
		static Object getRenderEnv(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final BlockState blockState, @Nonnull final BlockPos pos) {
			return bufferBuilder.getRenderEnv(blockState, pos);
		}

	}

	static final class BlockModelCustomizerOF {

		@Nonnull
		static IBakedModel getRenderModel(
				@Nonnull final IBakedModel model,
				@Nonnull final BlockState blockState,
				@Nonnull final Object renderEnv
		) {
			return BlockModelCustomizer.getRenderModel(model, blockState, (RenderEnv) renderEnv);
		}

		@Nonnull
		static List<BakedQuad> getRenderQuads(
				@Nonnull final List<BakedQuad> quads,
				@Nonnull final IEnviromentBlockReader reader,
				@Nonnull final BlockState blockState,
				@Nonnull final BlockPos pos,
				@Nonnull final Direction direction,
				@Nonnull final BlockRenderLayer blockRenderLayer,
				final long rand,
				@Nonnull final Object renderEnv
		) {
			return BlockModelCustomizer.getRenderQuads(quads, reader, blockState, pos, direction, blockRenderLayer, rand, (RenderEnv) renderEnv);
		}

	}

}
