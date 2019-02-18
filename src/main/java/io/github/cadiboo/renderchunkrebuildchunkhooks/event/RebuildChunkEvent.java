package io.github.cadiboo.renderchunkrebuildchunkhooks.event;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;

/**
 * Base class for all events this mod provides
 * Called when a {@link RenderChunk#rebuildChunk} is called.
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}
 */
public class RebuildChunkEvent extends Event {

	@Nonnull
	private final RenderChunk renderChunk;
	private final float x;
	private final float y;
	private final float z;
	@Nonnull
	private final ChunkRenderTask generator;
	@Nonnull
	private final CompiledChunk compiledChunk;
	@Nonnull
	private final BlockPos renderChunkStartPosition;
	@Nonnull
	private final BlockPos renderChunkEndPosition;
	@Nonnull
	private final World world;

	public RebuildChunkEvent(
			@Nonnull final RenderChunk renderChunk,
			final float x,
			final float y,
			final float z,
			@Nonnull final ChunkRenderTask generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkStartPosition,
			@Nonnull final BlockPos renderChunkEndPosition,
			@Nonnull final World world
	) {
		this.renderChunk = renderChunk;
		this.x = x;
		this.y = y;
		this.z = z;
		this.generator = generator;
		this.compiledChunk = compiledChunk;
		this.renderChunkStartPosition = renderChunkStartPosition;
		this.renderChunkEndPosition = renderChunkEndPosition;
		this.world = world;
	}

	@Nonnull
	public RenderChunk getRenderChunk() {
		return renderChunk;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	@Nonnull
	public ChunkRenderTask getGenerator() {
		return generator;
	}

	@Nonnull
	public CompiledChunk getCompiledChunk() {
		return compiledChunk;
	}

	@Nonnull
	public BlockPos getRenderChunkStartPosition() {
		return renderChunkStartPosition;
	}

	@Nonnull
	public BlockPos getRenderChunkEndPosition() {
		return renderChunkEndPosition;
	}

	@Nonnull
	public World getWorld() {
		return world;
	}

	@Nonnull
	public BufferBuilder getBufferBuilderByLayer(final BlockRenderLayer blockRenderLayer) {
		return this.getGenerator().getRegionRenderCacheBuilder().getBuilder(blockRenderLayer);
	}

	@Nonnull
	public BufferBuilder getBufferBuilderById(final int blockRenderLayerOrdinal) {
		return this.getGenerator().getRegionRenderCacheBuilder().getBuilder(blockRenderLayerOrdinal);
	}

}
