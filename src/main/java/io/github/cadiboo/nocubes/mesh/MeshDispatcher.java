package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.DensityCache;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3b;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cadiboo
 */
public class MeshDispatcher {

	@Nonnull
	public HashMap<Vec3b, FaceList> generateOffsetChunk(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final IIsSmoothable isSmoothable
	) {
		final HashMap<Vec3b, FaceList> chunkData = generateUnOffsetChunk(chunkPos, blockAccess, isSmoothable);
		return offsetChunkVertices(chunkPos, chunkData);
	}

	/**
	 * @param chunkPos
	 * @param blockAccess
	 * @param isSmoothable
	 * @return the un offset vertices for the chunk
	 */
	@Nonnull
	public HashMap<Vec3b, FaceList> generateUnOffsetChunk(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final IIsSmoothable isSmoothable
	) {

		if (ModConfig.getMeshGenerator() == MeshGenerator.OldNoCubes) {
			return generateChunkOldNoCubes(chunkPos, blockAccess, isSmoothable);
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

	@Nonnull
	protected HashMap<Vec3b, FaceList> generateCachesAndChunkData(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final IIsSmoothable isSmoothable,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ
	) {
		final int chunkPosX = chunkPos.getX();
		final int chunkPosY = chunkPos.getY();
		final int chunkPosZ = chunkPos.getZ();

		try (final StateCache stateCache = generateMeshStateCache(chunkPosX, chunkPosY, chunkPosZ, meshSizeX, meshSizeY, meshSizeZ, blockAccess, pooledMutableBlockPos)) {
			try (final SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(stateCache, isSmoothable)) {
				try (final DensityCache densityCache = CacheUtil.generateDensityCache(chunkPosX, chunkPosY, chunkPosZ, stateCache, smoothableCache, blockAccess, pooledMutableBlockPos)) {
					return ModConfig.getMeshGenerator().generateChunk(densityCache.getDensityCache(), new byte[]{meshSizeX, meshSizeY, meshSizeZ});
				}
			}
		}
	}

	@Nonnull
	protected HashMap<Vec3b, FaceList> generateChunkOldNoCubes(@Nonnull final BlockPos chunkPos, @Nonnull final IBlockAccess blockAccess, @Nonnull final IIsSmoothable isSmoothable) {
		PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
			return OldNoCubes.generateChunk(chunkPos, blockAccess, isSmoothable, pooledMutableBlockPos);
		} finally {
			pooledMutableBlockPos.release();
		}
	}

	@Nonnull
	protected StateCache generateMeshStateCache(
			final int startPosX, final int startPosY, final int startPosZ,
			final int meshSizeX, final int meshSizeY, final int meshSizeZ,
			@Nonnull final IBlockAccess blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		// Density takes +1 block on every negative axis into account so we need to start at -1 block
		final int cacheStartPosX = startPosX - 1;
		final int cacheStartPosY = startPosY - 1;
		final int cacheStartPosZ = startPosZ - 1;

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

	/**
	 * @param pos
	 * @param blockAccess
	 * @param isSmoothable
	 * @return the offset vertices for the block
	 */
	@Nonnull
	public FaceList generateOffsetBlock(@Nonnull final BlockPos pos, @Nonnull final IBlockAccess blockAccess, @Nonnull final IIsSmoothable isSmoothable) {
		final FaceList chunkData = generateUnOffsetBlock(pos, blockAccess, isSmoothable);

		final int chunkPosX = (pos.getX() >> 4) << 4;
		final int chunkPosY = (pos.getY() >> 4) << 4;
		final int chunkPosZ = (pos.getZ() >> 4) << 4;

		return offsetBlockVertices(chunkPosX, chunkPosY, chunkPosZ, chunkData);
	}

	/**
	 * @param pos
	 * @param blockAccess
	 * @param isSmoothable
	 * @return the un offset vertices for the block
	 */
	@Nonnull
	public FaceList generateUnOffsetBlock(@Nonnull final BlockPos pos, @Nonnull final IBlockAccess blockAccess, @Nonnull final IIsSmoothable isSmoothable) {
		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateBlock")) {
			PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
			try {
				final int posX = pos.getX();
				final int posY = pos.getY();
				final int posZ = pos.getZ();

				// Convert block pos to relative block pos
				// For example 68 -> 4, 127 -> 15, 4 -> 4, 312312312 -> 8
				final int relativePosX = posX & 15;
				final int relativePosY = posY & 15;
				final int relativePosZ = posZ & 15;

				final byte[] posRelativeToChunk = new byte[]{
						(byte) relativePosX,
						(byte) relativePosY,
						(byte) relativePosZ
				};

				//TODO: I don't think I need this to be 2
				final byte meshSizeX = 2;
				final byte meshSizeY = 2;
				final byte meshSizeZ = 2;

				try (final StateCache stateCache = generateMeshStateCache(posX, posY, posZ, meshSizeX, meshSizeY, meshSizeZ, blockAccess, pooledMutableBlockPos)) {
					try (final SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(stateCache, isSmoothable)) {
						try (final DensityCache densityCache = CacheUtil.generateDensityCache(posX, posY, posZ, stateCache, smoothableCache, blockAccess, pooledMutableBlockPos)) {
							final float[] neighbourDensityGrid = generateNeighbourDensityGrid(densityCache);
							return ModConfig.getMeshGenerator().generateBlock(posRelativeToChunk, neighbourDensityGrid);
						}
					}
				}
			} finally {
				pooledMutableBlockPos.release();
			}
		}
	}

	@Nonnull
	public float[] generateNeighbourDensityGrid(@Nonnull final DensityCache densityCache) {
		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateNeighbourDensityGrid")) {
			final float[] neighbourDensityGrid = new float[8];

			final float[] densityCacheArray = densityCache.getDensityCache();

			int neighbourDensityGridIndex = 0;
			for (int zOffset = 0; zOffset < 2; ++zOffset) {
				for (int yOffset = 0; yOffset < 2; ++yOffset) {
					for (byte xOffset = 0; xOffset < 2; ++xOffset, ++neighbourDensityGridIndex) {
						neighbourDensityGrid[neighbourDensityGridIndex] = densityCacheArray[densityCache.getIndex(xOffset, yOffset, zOffset)];
					}
				}
			}
			return neighbourDensityGrid;
		}
	}

	/**
	 * Modifies the chunk data passed in! Returns the offset data for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	public HashMap<Vec3b, FaceList> offsetChunkVertices(@Nonnull final BlockPos chunkPos, @Nonnull final HashMap<Vec3b, FaceList> chunkData) {
		for (FaceList faces : chunkData.values()) {
			offsetBlockVertices(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), faces);
		}
		return chunkData;
	}

	/**
	 * Modifies the block data passed in! Returns the offset data for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	public FaceList offsetBlockVertices(final int chunkPosX, final int chunkPosY, final int chunkPosZ, @Nonnull final FaceList faces) {
		for (Face face : faces) {
			final Vec3 vertex0 = face.getVertex0();
			final Vec3 vertex1 = face.getVertex1();
			final Vec3 vertex2 = face.getVertex2();
			final Vec3 vertex3 = face.getVertex3();

			vertex0.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex1.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex2.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex3.addOffset(chunkPosX, chunkPosY, chunkPosZ);

			if (ModConfig.offsetVertices) {
				ModUtil.offsetVertex(vertex0);
				ModUtil.offsetVertex(vertex1);
				ModUtil.offsetVertex(vertex2);
				ModUtil.offsetVertex(vertex3);
			}
		}
		return faces;
	}

}
