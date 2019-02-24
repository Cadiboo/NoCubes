package io.github.cadiboo.rcrch.event;

import io.github.cadiboo.rcrch.util.RenderChunkCacheReference;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nonnull;
import java.util.HashSet;

/**
 * @author Cadiboo
 */
@Cancelable
public class RebuildChunkPreRenderSetupEvent extends RebuildChunkEvent {

	private final RenderChunkCacheReference renderChunkCacheReference;
	private final VisGraph visGraph;
	private final HashSet tileEntitiesWithGlobalRenderers;

	public RebuildChunkPreRenderSetupEvent(
			@Nonnull final RenderChunk renderChunk,
			final float x,
			final float y,
			final float z,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkStartPosition,
			@Nonnull final BlockPos renderChunkEndPosition,
			@Nonnull final World world,
			@Nonnull final RenderChunkCacheReference renderChunkCacheReference,
			@Nonnull final VisGraph visGraph,
			@Nonnull final HashSet tileEntitiesWithGlobalRenderers
	) {
		super(renderChunk, x, y, z, generator, compiledChunk, renderChunkStartPosition, renderChunkEndPosition, world);
		this.renderChunkCacheReference = renderChunkCacheReference;
		this.visGraph = visGraph;
		this.tileEntitiesWithGlobalRenderers = tileEntitiesWithGlobalRenderers;
	}

	public ChunkCache getChunkCache() {
		return renderChunkCacheReference.get();
	}

	public ChunkCache setChunkCache(final ChunkCache renderChunkCache) {
		final ChunkCache oldChunkCache = renderChunkCacheReference.get();
		renderChunkCacheReference.set(renderChunkCache);
		return oldChunkCache;
	}

	public VisGraph getVisGraph() {
		return visGraph;
	}

	public HashSet getTileEntitiesWithGlobalRenderers() {
		return tileEntitiesWithGlobalRenderers;
	}

}
