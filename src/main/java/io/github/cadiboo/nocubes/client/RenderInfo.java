package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ChunkInfo;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

public class RenderInfo extends ChunkInfo {

	private final BlockRendererDispatcher blockRendererDispatcher;

	private final RenderChunk renderChunk;

	private final CompiledChunk compiledChunk;

	private final ChunkCompileTaskGenerator generator;

	/**
	 * THE pooledMutableBlockPos WILL NOT BE RELEASED FOR YOU, REMEMBER TO RELEASE IT YOURSELF
	 *
	 * @param cache                   the {@link IBlockAccess}
	 * @param chunkPos                the position of the chunk as a block pos
	 * @param pooledMutableBlockPos   a {@link PooledMutableBlockPos}
	 * @param blockRendererDispatcher the {@link BlockRendererDispatcher} to use
	 * @param renderChunk             the {@link RenderChunk}
	 * @param compiledChunk           the {@link CompiledChunk}
	 * @param generator               the {@link ChunkCompileTaskGenerator}
	 */
	public RenderInfo(final IBlockAccess cache, final BlockPos chunkPos, final PooledMutableBlockPos pooledMutableBlockPos, BlockRendererDispatcher blockRendererDispatcher, RenderChunk renderChunk, CompiledChunk compiledChunk, ChunkCompileTaskGenerator generator) {
		super(cache, chunkPos, pooledMutableBlockPos);
		this.blockRendererDispatcher = blockRendererDispatcher;
		this.renderChunk = renderChunk;
		this.compiledChunk = compiledChunk;
		this.generator = generator;
	}

	public BlockRendererDispatcher getBlockRendererDispatcher() {
		return blockRendererDispatcher;
	}

	public RenderChunk getRenderChunk() {
		return renderChunk;
	}

	public CompiledChunk getCompiledChunk() {
		return compiledChunk;
	}

	public ChunkCompileTaskGenerator getGenerator() {
		return generator;
	}

}
