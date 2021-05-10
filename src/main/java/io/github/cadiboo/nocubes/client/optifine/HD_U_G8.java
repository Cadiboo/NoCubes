package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.optifine.Config;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.override.ChunkCacheOF;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;
import net.optifine.shaders.Shaders;

import javax.annotation.Nullable;

import static net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;

class HD_U_G8 implements OptiFineProxy {
	private boolean shaders;
	private boolean shadersMidBlock;
	private RenderEnv renderEnv;

	public void preRenderChunk(BlockPos blockpos) {
		boolean shaders = Config.isShaders();
		this.shaders = shaders;
		shadersMidBlock = shaders && Shaders.useMidBlockAttrib;
	}

	public long getSeed(long originalSeed) {
		return Config.isAlternateBlocks() ? 0 : originalSeed;
	}

	public void preRenderBlock(ChunkRender chunkRender, RegionRenderCacheBuilder builder, IBlockDisplayReader chunkCacheOF, RenderType renderType, BufferBuilder buffer, BlockState state, BlockPos.Mutable pos) {
		buffer.setBlockLayer(renderType);
		RenderEnv renderEnv = buffer.getRenderEnv(state, pos);
		this.renderEnv = renderEnv;
		renderEnv.setRegionRenderCacheBuilder(builder);
		((ChunkCacheOF) chunkCacheOF).setRenderEnv(renderEnv);

		if (shadersMidBlock)
			buffer.setMidBlock(
				0.5F + (float) chunkRender.regionDX + (float) (pos.getX() & 15),
				0.5F + (float) chunkRender.regionDY + (float) (pos.getY() & 15),
				0.5F + (float) chunkRender.regionDZ + (float) (pos.getZ() & 15)
			);
		if (shaders)
			SVertexBuilder.pushEntity(state, buffer);
	}

	public IBakedModel getModel(IBakedModel originalModel, BlockState state) {
		return BlockModelCustomizer.getRenderModel(originalModel, state, renderEnv);
	}

	public void postRenderBlock(BufferBuilder buffer, ChunkRender chunkRender, RegionRenderCacheBuilder builder, CompiledChunk compiledChunk) {
		if (shaders)
			SVertexBuilder.popEntity(buffer);

		if (renderEnv.isOverlaysRendered()) {
			chunkRender.postRenderOverlays(builder, compiledChunk);
			renderEnv.setOverlaysRendered(false);
		}
	}

	@Nullable
	@Override
	public BakedQuad getQuadEmissive(BakedQuad quad) {
		return quad.getQuadEmissive();
	}

	@Override
	public void preRenderQuad(BakedQuad emissiveQuad, BlockState state, BlockPos pos) {
		renderEnv.reset(state, pos);
	}
}
