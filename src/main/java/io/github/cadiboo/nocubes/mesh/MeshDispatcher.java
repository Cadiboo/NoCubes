package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.DensityCache;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.nocubes.util.Vec3b;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public class MeshDispatcher {

	public HashMap<Vec3b, FaceList> generateChunk(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final IIsSmoothable isSmoothable
	) {

		if (ModConfig.getMeshGenerator() == MeshGenerator.OldNoCubes) {
			return generateChunkOldNoCubes(chunkPos, isSmoothable);
		}

		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateChunk")) {
			PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
			try {
				final byte meshSizeX;
				final byte meshSizeY;
				final byte meshSizeZ;
				switch (ModConfig.getMeshGenerator()) {
					// Yay, Surface Nets is special and needs an extra +1. Why? No-one knows :/
					case SurfaceNets:
						meshSizeX = 18;
						meshSizeY = 18;
						meshSizeZ = 18;
						break;
					default:
						meshSizeX = 17;
						meshSizeY = 17;
						meshSizeZ = 17;
						break;
					case OldNoCubes:
						throw new UnsupportedOperationException();
				}

				return generateCachesAndChunkData(chunkPos, blockAccess, isSmoothable, pooledMutableBlockPos, meshSizeX, meshSizeY, meshSizeZ);
			} finally {
				pooledMutableBlockPos.release();
			}
		}
	}

	protected HashMap<Vec3b, FaceList> generateCachesAndChunkData(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final IIsSmoothable isSmoothable,
			final PooledMutableBlockPos pooledMutableBlockPos,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ
	) {
		final int chunkPosX = chunkPos.getX();
		final int chunkPosY = chunkPos.getY();
		final int chunkPosZ = chunkPos.getZ();

		try (final StateCache stateCache = generateMeshStateCache(chunkPosX, chunkPosY, chunkPosZ, meshSizeX, meshSizeY, meshSizeZ, blockAccess, pooledMutableBlockPos)) {
			try (final SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(stateCache, isSmoothable)) {
				try (final DensityCache data = CacheUtil.generateDensityCache(chunkPosX, chunkPosY, chunkPosZ, stateCache, smoothableCache, blockAccess, pooledMutableBlockPos)) {
					return ModConfig.getMeshGenerator().generateChunk(data.getDensityCache(), new byte[]{meshSizeX, meshSizeY, meshSizeZ});
				}
			}
		}
	}

	protected HashMap<Vec3b, FaceList> generateChunkOldNoCubes(final BlockPos chunkPos, final IIsSmoothable isSmoothable) {
		return null;
	}

	protected StateCache generateMeshStateCache(
			final int chunkPosX, final int chunkPosY, final int chunkPosZ,
			final int meshSizeX, final int meshSizeY, final int meshSizeZ,
			final IBlockAccess blockAccess,
			final PooledMutableBlockPos pooledMutableBlockPos
	) {
		// Density takes +1 block on every negative axis into account so we need to start at -1 block
		final int cacheStartPosX = chunkPosX - 1;
		final int cacheStartPosY = chunkPosY - 1;
		final int cacheStartPosZ = chunkPosZ - 1;

		// Density takes +1 block on every negative axis into account so we need to add 1 to the size of the cache (it only takes +1 on NEGATIVE axis)
		final int cacheSizeX = meshSizeX + 1;
		final int cacheSizeY = meshSizeY + 1;
		final int cacheSizeZ = meshSizeZ + 1;

		return CacheUtil.generateStateCache(
				cacheStartPosX, cacheStartPosY, cacheStartPosZ,
				cacheSizeX, cacheSizeY, cacheSizeZ,
				blockAccess,
				pooledMutableBlockPos
		);
	}

	public FaceList generateBlock(final BlockPos pos, final IBlockAccess blockAccess, final IIsSmoothable isSmoothable) {
		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateBlock")) {
			PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
			try {
				final int posX = pos.getX();
				final int posY = pos.getY();
				final int posZ = pos.getZ();

				// Convert block pos to relative block pos
				// For example 68 -> 4, 127 -> 15, 4 -> 4, 312312312 -> 8
				final byte[] posRelativeToChunk = new byte[]{
						(byte) (posX - ((posX >> 4) << 4)),
						(byte) (posY - ((posY >> 4) << 4)),
						(byte) (posZ - ((posZ >> 4) << 4))
				};

				final float[] neighbourDensityGrid = generateNeighbourDensityGrid(posX, posY, posZ, blockAccess, isSmoothable, pooledMutableBlockPos);
				return ModConfig.getMeshGenerator().generateBlock(posRelativeToChunk, neighbourDensityGrid);
			} finally {
				pooledMutableBlockPos.release();
			}
		}
	}

	public float[] generateNeighbourDensityGrid(final int posX, final int posY, final int posZ, final IBlockAccess blockAccess, final IIsSmoothable isSmoothable, final PooledMutableBlockPos pooledMutableBlockPos) {
		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateNeighbourDensityGrid")) {
			final float[] neighbourDensityGrid = new float[8];

			int neighbourDensityGridIndex = 0;
			for (int zOffset = 0; zOffset < 2; ++zOffset) {
				for (int yOffset = 0; yOffset < 2; ++yOffset) {
					for (byte xOffset = 0; xOffset < 2; ++xOffset, ++neighbourDensityGridIndex) {
						pooledMutableBlockPos.setPos(
								posX + xOffset,
								posY + yOffset,
								posZ + yOffset
						);
						final IBlockState state = blockAccess.getBlockState(pooledMutableBlockPos);
						final boolean isStateSmoothable = isSmoothable.isSmoothable(state);
						neighbourDensityGrid[neighbourDensityGridIndex] = ModUtil.getIndividualBlockDensity(isStateSmoothable, state, blockAccess, pooledMutableBlockPos);
					}
				}
			}
			return neighbourDensityGrid;
		}
	}

}
