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

	static void pushShaderThing(@Nonnull final BlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final IEnviromentBlockReader blockAccess, @Nonnull final BufferBuilder worldRendererIn) {
		if (Config.isShaders()) {
			SVertexBuilder.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
		}
	}

	static void popShaderThing(@Nonnull final BufferBuilder worldRendererIn) {
		if (Config.isShaders()) {
			SVertexBuilder.popEntity(worldRendererIn);
		}
	}

	static final class BufferBuilderOF {

		@Nonnull
		static Object getRenderEnv(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final IEnviromentBlockReader blockAccess, @Nonnull final BlockState state, @Nonnull final BlockPos pos) {
			return bufferBuilder.getRenderEnv(/*blockAccess,*/ state, pos);
		}

	}

	static final class BlockModelCustomizerOF {

		@Nonnull
		static IBakedModel getRenderModel(@Nonnull final IBakedModel model, @Nonnull final BlockState state, @Nonnull final Object renderEnv) {
			return BlockModelCustomizer.getRenderModel(model, state, (RenderEnv) renderEnv);
		}

		@Nonnull
		static List<BakedQuad> getRenderQuads(@Nonnull final List<BakedQuad> quads, @Nonnull final IEnviromentBlockReader blockAccess, @Nonnull final BlockState state, @Nonnull final BlockPos pos, @Nonnull final Direction facing, @Nonnull final BlockRenderLayer blockRenderLayer, final long rand, @Nonnull final Object renderEnv) {
			return BlockModelCustomizer.getRenderQuads(quads, blockAccess, state, pos, facing, blockRenderLayer, rand, (RenderEnv) renderEnv);
		}

	}

}
