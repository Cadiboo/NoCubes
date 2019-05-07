package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.IsSmoothable;
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
			final int densityCacheAddX, final int densityCacheAddY, final int densityCacheAddZ,
			@Nonnull final IsSmoothable isSmoothable,
			@Nonnull final MeshGeneratorType meshGeneratorType
	) {
		if (meshGeneratorType == MeshGeneratorType.OldNoCubes) {
			try (PooledMutableBlockPos pooledMutableBlockPos1 = PooledMutableBlockPos.retain()) {
				return OldNoCubes.generateChunk(chunkPos, blockAccess, isSmoothable, pooledMutableBlockPos1);
			}
		}

		final MeshGenerator meshGenerator = meshGeneratorType.getMeshGenerator();
		return generateChunkMeshOffset(
				chunkPos, blockAccess, pooledMutableBlockPos,
				stateCache, smoothableCache, densityCache,
				densityCacheAddX, densityCacheAddY, densityCacheAddZ,
				isSmoothable,
				meshGenerator
		);
	}

	@Nonnull
	public static HashMap<Vec3b, FaceList> generateChunkMeshOffset(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nullable final StateCache stateCache,
			@Nullable final SmoothableCache smoothableCache,
			@Nullable final DensityCache densityCache,
			final int densityCacheAddX, final int densityCacheAddY, final int densityCacheAddZ,
			@Nonnull final IsSmoothable isSmoothable,
			@Nonnull final MeshGenerator meshGenerator
	) {
		return offsetChunkMesh(
				chunkPos,
				generateChunkMeshUnOffset(
						chunkPos, blockAccess, pooledMutableBlockPos,
						stateCache, smoothableCache, densityCache,
						densityCacheAddX, densityCacheAddY, densityCacheAddZ,
						isSmoothable,
						meshGenerator
				)
		);
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
			final int densityCacheAddX, final int densityCacheAddY, final int densityCacheAddZ,
			@Nonnull final IsSmoothable isSmoothable,
			@Nonnull MeshGenerator meshGenerator
	) {
		try (final ModProfiler ignored = ModProfiler.get().start("generateChunkMeshUnOffset")) {
			final int chunkPosX = chunkPos.getX();
			final int chunkPosY = chunkPos.getY();
			final int chunkPosZ = chunkPos.getZ();

			// A chunk is 0-15 so 16, we add one because idk and then surface nets needs another +1 because reasons
			final byte meshSizeX = (byte) (17 + meshGenerator.getSizeXExtension());
			final byte meshSizeY = (byte) (17 + meshGenerator.getSizeYExtension());
			final byte meshSizeZ = (byte) (17 + meshGenerator.getSizeZExtension());

			return generateMeshUnOffset(
					blockAccess, pooledMutableBlockPos,
					isSmoothable,
					stateCache,
					chunkPosX, chunkPosY, chunkPosZ,
					smoothableCache,
					densityCache,
					densityCacheAddX, densityCacheAddY, densityCacheAddZ,
					meshSizeX, meshSizeY, meshSizeZ,
					meshGenerator
			);
		}
	}

	// ++laziness
	@Nonnull
	public static FaceList generateBlockMeshOffset(
			@Nonnull final BlockPos pos,
			@Nonnull final IBlockReader blockAccess,
			@Nonnull final IsSmoothable isSmoothable,
			@Nonnull final MeshGeneratorType meshGenerator
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
			@Nonnull final IsSmoothable isSmoothable,
			@Nonnull final MeshGeneratorType meshGenerator
	) {

		if (meshGenerator == MeshGeneratorType.OldNoCubes) {
			return OldNoCubes.generateBlock(pos, blockAccess, isSmoothable, pooledMutableBlockPos);
		}

		final FaceList chunkData = generateBlockMeshUnOffset(
				pos, blockAccess, pooledMutableBlockPos,
				isSmoothable,
				meshGenerator
		);

		final int chunkPosX = (pos.getX() >> 4) << 4;
		final int chunkPosY = (pos.getY() >> 4) << 4;
		final int chunkPosZ = (pos.getZ() >> 4) << 4;

		return offsetFaceList(chunkPosX, chunkPosY, chunkPosZ, chunkData);
	}

	/**
	 * @return the un offset vertices for the block
	 */
	@Nonnull
	public static FaceList generateBlockMeshUnOffset(@Nonnull final BlockPos pos, @Nonnull final IBlockReader blockAccess, final PooledMutableBlockPos pooledMutableBlockPos, @Nonnull final IsSmoothable isSmoothable, @Nonnull final MeshGeneratorType meshGeneratorType) {

		try (ModProfiler ignored = ModProfiler.get().start("generateBlock")) {
//			if(true)
//				return meshGeneratorType.generateBlock(pos, blockAccess, isSmoothable);

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
			if (meshGeneratorType == MeshGeneratorType.MarchingCubes) {
				addX = 1;
				addY = 1;
				addZ = 1;
				subX = 0;
				subY = 0;
				subZ = 0;
			} else if (meshGeneratorType == MeshGeneratorType.MarchingTetrahedra) {
				addX = 1;
				addY = 1;
				addZ = 1;
				subX = 0;
				subY = 0;
				subZ = 0;
			} else if (meshGeneratorType == MeshGeneratorType.SurfaceNets) {
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

			final MeshGenerator meshGenerator = meshGeneratorType.getMeshGenerator();

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
									density += ModUtil.getIndividualBlockDensity(isSmoothable.apply(state), state);
								}
							}
						}
						densityData[index] = density;

					}
				}
			}

			FaceList finalFaces = FaceList.retain();
			final HashMap<Vec3b, FaceList> vec3bFaceListHashMap = meshGenerator.generateChunk(densityData, new byte[]{meshSizeX, meshSizeY, meshSizeZ});
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
		offsetMesh(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), chunkData);
		return chunkData;
	}

	/**
	 * Modifies the chunk data mesh in! Returns the offset mesh for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	public static HashMap<Vec3b, FaceList> offsetMesh(final int offsetX, final int offsetY, final int offsetZ, @Nonnull final HashMap<Vec3b, FaceList> meshData) {
		for (FaceList faces : meshData.values()) {
			offsetFaceList(offsetX, offsetY, offsetZ, faces);
		}
		return meshData;
	}

	/**
	 * Modifies the block mesh passed in! Returns the offset mesh for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	public static FaceList offsetFaceList(final int chunkPosX, final int chunkPosY, final int chunkPosZ, @Nonnull final FaceList faces) {
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

	public static HashMap<Vec3b, FaceList> generateMeshUnOffset(
			@Nonnull final IBlockReader blockAccess, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final IsSmoothable isSmoothable,
			@Nullable final StateCache stateCacheIn,
			final int chunkPosX, final int chunkPosY, final int chunkPosZ,
			@Nullable final SmoothableCache smoothableCacheIn,
			@Nullable final DensityCache densityCacheIn,
			final int densityCacheAddX, final int densityCacheAddY, final int densityCacheAddZ,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ,
			@Nonnull final MeshGenerator meshGenerator
	) {
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
								densityCacheAddX, densityCacheAddY, densityCacheAddZ,
								stateCache,
								smoothableCache
						)
		) {
			return meshGenerator.generateChunk(densityCache.getDensityCache(), new byte[]{meshSizeX, meshSizeY, meshSizeZ});
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

}
