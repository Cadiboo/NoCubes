package io.github.cadiboo.nocubes.client.optifine.proxy;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.optifine.Config;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.override.ChunkCacheOF;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;

import java.util.List;

/**
 * @author Cadiboo
 */
public class HD_U_F4 implements OptiFine {

	@Override
	public Object preRenderBlock(final BufferBuilder bufferbuilder1, final BlockRenderLayer blockrenderlayer2, final BlockState blockstate, final BlockPos blockpos2, final RegionRenderCacheBuilder builders, final IEnviromentBlockReader blockAccess) {
		bufferbuilder1.setBlockLayer(blockrenderlayer2);
		RenderEnv renderEnv = bufferbuilder1.getRenderEnv(blockstate, blockpos2);
//		renderEnv.setRegionRenderCacheBuilder(generator.getRegionRenderCacheBuilder());
		renderEnv.setRegionRenderCacheBuilder(builders);
		((ChunkCacheOF) blockAccess).setRenderEnv(renderEnv);
		return renderEnv;
	}

	@Override
	public void postRenderBlock(final Object renderEnvObj, final ChunkRender chunkRender, final RegionRenderCacheBuilder builders, final CompiledChunk compiledchunk, final boolean[] aboolean) {
		final RenderEnv renderEnv = (RenderEnv) renderEnvObj;
		if (renderEnv.isOverlaysRendered()) {
//			chunkRender.postRenderOverlays(generator.getRegionRenderCacheBuilder(), compiledchunk, aboolean);
			chunkRender.postRenderOverlays(builders, compiledchunk, aboolean);
			renderEnv.setOverlaysRendered(false);
		}
	}

	@Override
	public boolean isChunkCacheOF(final Object obj) {
		return obj instanceof ChunkCacheOF;
	}

	@Override
	public ChunkRenderCache getChunkRenderCache(final Object chunkCacheOF) {
		return ((ChunkCacheOF) chunkCacheOF).chunkCache;
	}

	@Override
	public void pushShaderEntity(final IFluidState state, final BlockPos pos, final IEnviromentBlockReader reader, final BufferBuilder buffer) {
		pushShaderEntity(state.getBlockState(), pos, reader, buffer);
	}

	@Override
	public void pushShaderEntity(final BlockState state, final BlockPos pos, final IEnviromentBlockReader reader, final BufferBuilder buffer) {
		if (Config.isShaders())
			SVertexBuilder.pushEntity(state, pos, reader, buffer);
	}

	@Override
	public void popShaderEntity(final BufferBuilder buffer) {
		SVertexBuilder.popEntity(buffer);
	}

	@Override
	public Object getRenderEnvironment(final BufferBuilder bufferBuilder, final BlockState blockStateIn, final BlockPos blockPosIn) {
		return bufferBuilder.getRenderEnv(blockStateIn, blockPosIn);
	}

	@Override
	public IBakedModel getRenderModel(final IBakedModel model, final BlockState state, final Object renderEnv) {
		return BlockModelCustomizer.getRenderModel(model, state, (RenderEnv) renderEnv);
	}

	@Override
	public List<BakedQuad> getRenderQuads(final List<BakedQuad> quads, final IEnviromentBlockReader reader, final BlockState state, final BlockPos pos, final Direction direction, final BlockRenderLayer blockRenderLayer, final long posRandLong, final Object renderEnv) {
		return BlockModelCustomizer.getRenderQuads(quads, reader, state, pos, direction, blockRenderLayer, posRandLong, (RenderEnv) renderEnv);
	}

}
