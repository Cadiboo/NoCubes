package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public final class MeshDispatcher {

	@Nonnull
	public static HashMap<Vec3b, FaceList> generateChunkMeshOffset(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nullable final StateCache stateCache,
			@Nullable final SmoothableCache smoothableCache,
			@Nullable final DensityCache densityCache,
			@Nonnull final IIsSmoothable isSmoothable,
			@Nonnull final MeshGenerator meshGenerator
	) {
		if (meshGenerator == MeshGenerator.OldNoCubes) {
			return generateChunkMeshOffsetOldNoCubes(chunkPos, blockAccess, isSmoothable);
		}

		final HashMap<Vec3b, FaceList> chunkData = generateChunkMeshUnOffset(chunkPos, blockAccess, pooledMutableBlockPos, stateCache, smoothableCache, densityCache, isSmoothable, meshGenerator);
		return offsetChunkMesh(chunkPos, chunkData);
	}

	/**
	 * @return the un offset vertices for the chunk
	 */
	@Nonnull
	public static HashMap<Vec3b, FaceList> generateChunkMeshUnOffset(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nullable final StateCache stateCache,
			@Nullable final SmoothableCache smoothableCache,
			@Nullable final DensityCache densityCache,
			@Nonnull final IIsSmoothable isSmoothable,
			@Nonnull MeshGenerator meshGenerator
	) {
		try (final ModProfiler ignored = ModProfiler.get().start("generateChunkMeshUnOffset")) {
			//TODO: FIX THIS
			final byte meshSizeX;
			final byte meshSizeY;
			final byte meshSizeZ;
			switch (meshGenerator) {
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
			return generateCachesAndChunkData(chunkPos, blockAccess, isSmoothable, pooledMutableBlockPos, stateCache, smoothableCache, densityCache, meshSizeX, meshSizeY, meshSizeZ, meshGenerator);
		}
	}

	// ++laziness
	@Nonnull
	public static FaceList generateBlockMeshOffset(
			@Nonnull final BlockPos pos,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final IIsSmoothable isSmoothable,
			@Nonnull final MeshGenerator meshGenerator
	) {
		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			return generateBlockMeshOffset(pos, blockAccess, pooledMutableBlockPos, isSmoothable, meshGenerator);
		}
	}

	/**
	 * @return the offset vertices for the block
	 */
	@Nonnull
	public static FaceList generateBlockMeshOffset(
			@Nonnull final BlockPos pos,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final IIsSmoothable isSmoothable,
			@Nonnull final MeshGenerator meshGenerator
	) {

		if (meshGenerator == MeshGenerator.OldNoCubes) {
			return generateOffsetBlockOldNoCubes(pos, blockAccess, pooledMutableBlockPos, isSmoothable);
		}

		final FaceList chunkData = generateBlockMeshUnOffset(pos, blockAccess, pooledMutableBlockPos, isSmoothable, meshGenerator);

		final int chunkPosX = (pos.getX() >> 4) << 4;
		final int chunkPosY = (pos.getY() >> 4) << 4;
		final int chunkPosZ = (pos.getZ() >> 4) << 4;

		return offsetBlockMesh(chunkPosX, chunkPosY, chunkPosZ, chunkData);
	}

	/**
	 * @return the un offset vertices for the block
	 */
	@Nonnull
	public static FaceList generateBlockMeshUnOffset(@Nonnull final BlockPos pos, @Nonnull final IBlockReader blockAccess, final PooledMutableBlockPos pooledMutableBlockPos, @Nonnull final IIsSmoothable isSmoothable, @Nonnull final MeshGenerator meshGenerator) {

		try (ModProfiler ignored = ModProfiler.get().start("generateBlock")) {
//			if(true)
//				return meshGenerator.generateBlock(pos, blockAccess, isSmoothable);

			final int posX = pos.getX();
			final int posY = pos.getY();
			final int posZ = pos.getZ();

			// Convert block pos to relative block pos
			// For example 68 -> 4, 127 -> 15, 4 -> 4, 312312312 -> 8
			final int relativePosX = posX & 15;
			final int relativePosY = posY & 15;
			final int relativePosZ = posZ & 15;

			final byte addX;
			final byte addY;
			final byte addZ;
			final byte subX;
			final byte subY;
			final byte subZ;
			//FFS
			if (meshGenerator == MeshGenerator.MarchingCubes) {
				addX = 1;
				addY = 1;
				addZ = 1;
				subX = 0;
				subY = 0;
				subZ = 0;
			} else if (meshGenerator == MeshGenerator.MarchingTetrahedra) {
				addX = 1;
				addY = 1;
				addZ = 1;
				subX = 0;
				subY = 0;
				subZ = 0;
			} else if (meshGenerator == MeshGenerator.SurfaceNets) {
				addX = 0;
				addY = 0;
				addZ = 0;
				subX = 1;
				subY = 1;
				subZ = 1;
			} else {
				addX = 0;
				addY = 0;
				addZ = 0;
				subX = 0;
				subY = 0;
				subZ = 0;
			}

			final byte meshSizeX = (byte) (2 + addX + subX + meshGenerator.getSizeXExtension());
			final byte meshSizeY = (byte) (2 + addY + subY + meshGenerator.getSizeYExtension());
			final byte meshSizeZ = (byte) (2 + addZ + subZ + meshGenerator.getSizeZExtension());

			final float[] densityData = new float[meshSizeX * meshSizeY * meshSizeZ];

			final int startPosX = posX - subX;
			final int startPosY = posY - subY;
			final int startPosZ = posZ - subZ;

			int index = 0;
			for (int z = 0; z < meshSizeX; ++z) {
				for (int y = 0; y < meshSizeY; ++y) {
					for (int x = 0; x < meshSizeZ; ++x, ++index) {

						float density = 0;
						for (int zOffset = 0; zOffset < 2; ++zOffset) {
							for (int yOffset = 0; yOffset < 2; ++yOffset) {
								for (int xOffset = 0; xOffset < 2; ++xOffset) {

									pooledMutableBlockPos.setPos(startPosX + x - xOffset, startPosY + y - yOffset, startPosZ + z - zOffset);
									final IBlockState state = blockAccess.getBlockState(pooledMutableBlockPos);
									density += ModUtil.getIndividualBlockDensity(isSmoothable.isSmoothable(state), state);
								}
							}
						}
						densityData[index] = density;

					}
				}
			}

			FaceList finalFaces = FaceList.retain();
			final HashMap<Vec3b, FaceList> vec3bFaceListHashMap = meshGenerator.generateChunk(densityData, meshSizeX, meshSizeY, meshSizeZ);
			for (final FaceList generatedFaceList : vec3bFaceListHashMap.values()) {
				finalFaces.addAll(generatedFaceList);
				generatedFaceList.close();
			}
			for (final Vec3b vec3b : vec3bFaceListHashMap.keySet()) {
				vec3b.close();
			}

			for (Face face : finalFaces) {
				final Vec3 vertex0 = face.getVertex0();
				final Vec3 vertex1 = face.getVertex1();
				final Vec3 vertex2 = face.getVertex2();
				final Vec3 vertex3 = face.getVertex3();

				vertex0.addOffset(relativePosX, relativePosY, relativePosZ);
				vertex1.addOffset(relativePosX, relativePosY, relativePosZ);
				vertex2.addOffset(relativePosX, relativePosY, relativePosZ);
				vertex3.addOffset(relativePosX, relativePosY, relativePosZ);

				vertex0.addOffset(-subX, -subY, -subZ);
				vertex1.addOffset(-subX, -subY, -subZ);
				vertex2.addOffset(-subX, -subY, -subZ);
				vertex3.addOffset(-subX, -subY, -subZ);

			}

			return finalFaces;
		}
	}

	/**
	 * Modifies the chunk data mesh in! Returns the offset mesh for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	public static HashMap<Vec3b, FaceList> offsetChunkMesh(@Nonnull final BlockPos chunkPos, @Nonnull final HashMap<Vec3b, FaceList> chunkData) {
		for (FaceList faces : chunkData.values()) {
			offsetBlockMesh(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), faces);
		}
		return chunkData;
	}

	/**
	 * Modifies the block mesh passed in! Returns the offset mesh for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	public static FaceList offsetBlockMesh(final int chunkPosX, final int chunkPosY, final int chunkPosZ, @Nonnull final FaceList faces) {
		for (Face face : faces) {
			final Vec3 vertex0 = face.getVertex0();
			final Vec3 vertex1 = face.getVertex1();
			final Vec3 vertex2 = face.getVertex2();
			final Vec3 vertex3 = face.getVertex3();

			vertex0.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex1.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex2.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex3.addOffset(chunkPosX, chunkPosY, chunkPosZ);

//			if (ModConfig.offsetVertices) {
//				ModUtil.offsetVertex(vertex0);
//				ModUtil.offsetVertex(vertex1);
//				ModUtil.offsetVertex(vertex2);
//				ModUtil.offsetVertex(vertex3);
//			}
		}
		return faces;
	}

	@Nonnull
	public static HashMap<Vec3b, FaceList> generateCachesAndChunkData(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final IIsSmoothable isSmoothable,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nullable final StateCache stateCacheIn,
			@Nullable final SmoothableCache smoothableCacheIn,
			@Nullable final DensityCache densityCacheIn,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ,
			@Nonnull final MeshGenerator meshGenerator
	) {
		final int chunkPosX = chunkPos.getX();
		final int chunkPosY = chunkPos.getY();
		final int chunkPosZ = chunkPos.getZ();

		try (
				final StateCache stateCache =
						stateCacheIn != null ?
								stateCacheIn :
								generateMeshStateCache(
										chunkPosX, chunkPosY, chunkPosZ,
										meshSizeX, meshSizeY, meshSizeZ,
										blockAccess, pooledMutableBlockPos
								);
				final SmoothableCache smoothableCache =
						smoothableCacheIn != null ?
								smoothableCacheIn :
								CacheUtil.generateSmoothableCache(stateCache, isSmoothable);
				final DensityCache densityCache =
						densityCacheIn != null ?
								densityCacheIn : CacheUtil.generateDensityCache(
								meshSizeX, meshSizeY, meshSizeZ,
								//TODO fix?
								// add 1 because we assume a that the cache starts 1 block before where we want
								1, 1, 1,
								stateCache,
								smoothableCache
						)
		) {
			return meshGenerator.generateChunk(densityCache.getDensityCache(), meshSizeX, meshSizeY, meshSizeZ);
		}
	}

	@Nonnull
	public static HashMap<Vec3b, FaceList> generateChunkMeshOffsetOldNoCubes(@Nonnull final BlockPos chunkPos, @Nonnull final IBlockReader blockAccess, @Nonnull final IIsSmoothable isSmoothable) {
		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			return OldNoCubes.generateChunk(chunkPos, blockAccess, isSmoothable, pooledMutableBlockPos);
		}
	}

	@Nonnull
	public static StateCache generateMeshStateCache(
			final int startPosX, final int startPosY, final int startPosZ,
			final int meshSizeX, final int meshSizeY, final int meshSizeZ,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		try (final ModProfiler ignored = ModProfiler.get().start("generateMeshStateCache")) {
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
	}

	@Nonnull
	public static FaceList generateOffsetBlockOldNoCubes(
			@Nonnull final BlockPos blockPos,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final IIsSmoothable isSmoothable
	) {
		return OldNoCubes.generateBlock(blockPos, blockAccess, isSmoothable, pooledMutableBlockPos);
	}

}
