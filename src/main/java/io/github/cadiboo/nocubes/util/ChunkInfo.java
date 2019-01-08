package io.github.cadiboo.nocubes.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

public class ChunkInfo {

	private final IBlockAccess cache;

	private final BlockPos chunkPos;

	private final PooledMutableBlockPos pooledMutableBlockPos;

	/**
	 * THE pooledMutableBlockPos WILL NOT BE RELEASED FOR YOU, REMEMBER TO RELEASE IT YOURSELF
	 * @param cache the {@link IBlockAccess}
	 * @param chunkPos the position of the chunk as a block pos
	 * @param pooledMutableBlockPos a {@link PooledMutableBlockPos}
	 */
	public ChunkInfo(final IBlockAccess cache, final BlockPos chunkPos, PooledMutableBlockPos pooledMutableBlockPos) {
		this.cache = cache;
		this.chunkPos = chunkPos;
		this.pooledMutableBlockPos = pooledMutableBlockPos;
	}

	public IBlockAccess getCache() {
		return cache;
	}

	public BlockPos getChunkPos() {
		return chunkPos;
	}

	public PooledMutableBlockPos getPooledMutableBlockPos() {
		return pooledMutableBlockPos;
	}

}
