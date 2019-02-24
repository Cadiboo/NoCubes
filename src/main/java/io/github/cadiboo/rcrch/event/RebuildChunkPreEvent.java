package io.github.cadiboo.rcrch.event;

import io.github.cadiboo.rcrch.util.WorldReference;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
@Cancelable
public class RebuildChunkPreEvent extends RebuildChunkEvent {

	@Nonnull
	private final WorldReference worldReference;

	public RebuildChunkPreEvent(
			@Nonnull final RenderChunk renderChunk,
			final float x,
			final float y,
			final float z,
			@Nonnull final ChunkCompileTaskGenerator generator,
			@Nonnull final CompiledChunk compiledChunk,
			@Nonnull final BlockPos renderChunkStartPosition,
			@Nonnull final BlockPos renderChunkEndPosition,
			@Nonnull final WorldReference worldReference
	) {
		//pass null in as the world, and override the getWorld method;
		super(renderChunk, x, y, z, generator, compiledChunk, renderChunkStartPosition, renderChunkEndPosition, null);
		this.worldReference = worldReference;
	}

	@Nonnull
	@Override
	public World getWorld() {
		return worldReference.get();
	}

	@Nonnull
	public World setWorld(World world) {
		final World oldWorld = worldReference.get();
		worldReference.set(world);
		return oldWorld;
	}

}
