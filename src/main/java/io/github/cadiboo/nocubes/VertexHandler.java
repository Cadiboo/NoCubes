package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.DensityCache;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.SmoothableCache;
import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.nocubes.util.Vec3b;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public final class VertexHandler {

	public static final HashMap<IBlockReader, HashMap<BlockPos, HashMap<Vec3b, FaceList>>> VERTICES = new HashMap<>();

	public static HashMap<Vec3b, FaceList> generateVertices(@Nonnull IBlockReader world, @Nonnull BlockPos chunkPos) {
		HashMap<BlockPos, HashMap<Vec3b, FaceList>> worldData = VERTICES.computeIfAbsent(world, k -> new HashMap<>());
		{
			final HashMap<Vec3b, FaceList> chunkData = worldData.get(chunkPos);
			if (chunkData != null) {
				closeChunk(chunkData);
			}
		}

		final byte meshSizeX;
		final byte meshSizeY;
		final byte meshSizeZ;
		if (ModConfig.getMeshGenerator() == MeshGenerator.SurfaceNets) {
			//yay, surface nets is special and needs an extra +1. why? no-one knows
			meshSizeX = 18;
			meshSizeY = 18;
			meshSizeZ = 18;
		} else {
			meshSizeX = 17;
			meshSizeY = 17;
			meshSizeZ = 17;
		}

		final int chunkPosX = chunkPos.getX();
		final int chunkPosY = chunkPos.getY();
		final int chunkPosZ = chunkPos.getZ();

		try (final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {

			final StateCache stateCache = generateStateCache(
					chunkPosX, chunkPosY, chunkPosZ,
					meshSizeX, meshSizeY, meshSizeZ,
					world,
					pooledMutableBlockPos
			);

			final SmoothableCache terrainSmoothableCache = CacheUtil.generateSmoothableCache(
					stateCache, TERRAIN_SMOOTHABLE
			);

			final DensityCache data = CacheUtil.generateDensityCache(
					chunkPosX, chunkPosY, chunkPosZ,
					stateCache, terrainSmoothableCache,
					world,
					pooledMutableBlockPos
			);

			final HashMap<Vec3b, FaceList> chunkData = ModConfig.getMeshGenerator().generateChunk(
					data.getDensityCache(),
					new byte[]{meshSizeX, meshSizeY, meshSizeZ}
			);

			worldData.put(chunkPos.toImmutable(), chunkData);
			return chunkData;
		}

	}

	private static StateCache generateStateCache(
			final int chunkPosX, final int chunkPosY, final int chunkPosZ,
			final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ,
			final IBlockReader blockAccess,
			final PooledMutableBlockPos pooledMutableBlockPos
	) {
		// Density takes +1 block on every negative axis into account so we need to start at -1 block
		final int cacheStartPosX = chunkPosX - 1;
		final int cacheStartPosY = chunkPosY - 1;
		final int cacheStartPosZ = chunkPosZ - 1;

		// Density takes +1 block on every negative axis into account so we need to add 1 to the size of the cache (it only takes +1 on NEGATIVE axis)
		// All up this is +1 block (1 for Density)
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

	private static void closeChunk(final HashMap<Vec3b, FaceList> chunkData) {
		chunkData.forEach((vec3b, faces) -> {
			faces.forEach(face -> {
				face.getVertex0().close();
				face.getVertex1().close();
				face.getVertex2().close();
				face.getVertex3().close();
				face.close();
			});
			faces.close();
			vec3b.close();
		});
	}

	@Nullable
	public static FaceList getFaces(@Nonnull IBlockReader world, @Nonnull BlockPos pos) {
		try (PooledMutableBlockPos chunkPos = PooledMutableBlockPos.retain(
				(pos.getX() >> 4) << 4,
				pos.getY(),
				(pos.getZ() >> 4) << 4
		)) {
			try (Vec3b vec3b = Vec3b.retain(
					(byte) (pos.getX() - chunkPos.getX()),
					(byte) (pos.getY() - chunkPos.getY()),
					(byte) (pos.getZ() - chunkPos.getZ())
			)) {
				final HashMap<BlockPos, HashMap<Vec3b, FaceList>> worldData = VERTICES.get(world);
				if (worldData == null) {
					return null;
				}
				final HashMap<Vec3b, FaceList> chunkData = worldData.get(chunkPos);
				if (chunkData == null) {
					return null;
				}
				return chunkData.get(vec3b);
			}
		}
	}

	@Nullable
	public static HashMap<Vec3b, FaceList> getChunkData(@Nonnull IBlockReader world, @Nonnull BlockPos chunkPos) {
		final HashMap<BlockPos, HashMap<Vec3b, FaceList>> worldData = VERTICES.get(world);
		if (worldData == null) {
			return null;
		}
		return worldData.get(chunkPos);
	}

}
