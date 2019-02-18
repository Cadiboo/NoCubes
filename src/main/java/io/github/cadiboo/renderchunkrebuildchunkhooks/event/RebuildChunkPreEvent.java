package io.github.cadiboo.renderchunkrebuildchunkhooks.event;

import io.github.cadiboo.renderchunkrebuildchunkhooks.util.RenderChunkCacheReference;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkCache;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nonnull;
import java.util.HashSet;

/**
 * @author Cadiboo
 */
@Cancelable
public class RebuildChunkPreEvent extends RebuildChunkEvent {

	private final RenderChunkCacheReference renderChunkCacheReference;
	private final VisGraph visGraph;
	private final HashSet tileEntitiesWithGlobalRenderers;

	public RebuildChunkPreEvent(
			@Nonnull final RenderChunk renderChunk,
			final float x,
			final float y,
			final float z,
			@Nonnull final ChunkRenderTask generator,
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

	public RenderChunkCache getRenderChunkCache() {
		return renderChunkCacheReference.getReference();
	}

	public RenderChunkCache setRenderChunkCache(final RenderChunkCache reference) {
		final RenderChunkCache oldReference = renderChunkCacheReference.getReference();
		renderChunkCacheReference.setReference(reference);
		return oldReference;
	}

	public VisGraph getVisGraph() {
		return visGraph;
	}

	public HashSet getTileEntitiesWithGlobalRenderers() {
		return tileEntitiesWithGlobalRenderers;
	}

}
