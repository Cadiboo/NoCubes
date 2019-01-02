package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.debug.client.render.IDebugRenderAlgorithm.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public final class SurfaceNetsDev {

	private static final ThreadLocal<HashMap<BlockPos, HashMap<BlockPos, ArrayList<Face<Vec3>>>>> FACES_BLOCKPOS_MAP = ThreadLocal.withInitial(HashMap::new);

	private static final int chunkSizeX = 16;

	private static final int chunkSizeY = 16;

	private static final int chunkSizeZ = 16;

	private static final int densityCacheSizeX = chunkSizeX + 1;

	private static final int densityCacheSizeY = chunkSizeY + 1;

	private static final int densityCacheSizeZ = chunkSizeZ + 1;

	private static final int densityCacheArraySize = densityCacheSizeX * densityCacheSizeY * densityCacheSizeZ;

	private static final int cacheSizeX = densityCacheSizeX + 1;

	private static final int cacheSizeY = densityCacheSizeY + 1;

	private static final int cacheSizeZ = densityCacheSizeZ + 1;

	private static final int cacheArraySize = cacheSizeX * cacheSizeY * cacheSizeZ;

//	private static float getBlockDensity(final boolean[] smoothableCache, final IBlockState[] stateCache, final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ, final IBlockAccess cache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final int x, final int y, final int z, PooledMutableBlockPos pooledMutableBlockPos) {
//
//		float density = 0.0F;
//
//		for (int xOffset = 0; xOffset < 2; ++xOffset) {
//			for (int yOffset = 0; yOffset < 2; ++yOffset) {
//				for (int zOffset = 0; zOffset < 2; ++zOffset) {
//
//					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
//					final int index = (x + xOffset) + cacheSizeX * ((y + yOffset) + cacheSizeY * (z + zOffset));
//
//					final IBlockState state = stateCache[index];
//					final boolean shouldSmooth = smoothableCache[index];
//
//					if (shouldSmooth) {
//						pooledMutableBlockPos.setPos(
//								renderChunkPosX + x + xOffset,
//								renderChunkPosY + y + yOffset,
//								renderChunkPosZ + z + zOffset
//						);
//						final AxisAlignedBB aabb = state.getBoundingBox(cache, pooledMutableBlockPos);
//						density += aabb.maxY - aabb.minY;
//					} else {
//						density -= 1;
//					}
//
//					if (state.getBlock() == Blocks.BEDROCK) {
//						density += 0.000000000000000000000000000000000000000000001F;
//					}
//
//				}
//			}
//		}
//
//		return density;
//
//	}

	private static float getBlockDensity(final int posX, final int posY, final int posZ, final boolean[] smoothableCache) {
		float density = 0;
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					final int index = (posX + x) + cacheSizeX * ((posY + y) + cacheSizeY * (posZ + z));

//					final IBlockState state = stateCache[index];
					final boolean shouldSmooth = smoothableCache[index];

					if (shouldSmooth) {
						density++;
					} else {
						density--;
					}
				}
			}
		}
		return density;
	}

	public static void renderPre(final RebuildChunkPreEvent event) {

		// because surface nets takes the 8 points of a block into account, we need to get the densities for +1 block on every positive axis of the chunk
		// because of this, we need to cache +2 blocks on every positive axis of the chunk

		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
		final IBlockAccess cache = ClientUtil.getCache(event);

		final int renderChunkPosX = renderChunkPos.getX();
		final int renderChunkPosY = renderChunkPos.getY();
		final int renderChunkPosZ = renderChunkPos.getZ();

		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();

		try {
			// caches need two extra blocks on every positive axis
			final IBlockState[] states = new IBlockState[cacheArraySize];
			fillStateCache(states, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos);
			final boolean[] smoothables = new boolean[cacheArraySize];
			fillSmoothableCache(smoothables, states);

			// densities needs 1 extra block on every positive axis
			final float[] densities = new float[densityCacheArraySize];
			fillDensityCache(densities, renderChunkPosX, renderChunkPosY, renderChunkPosZ, cache, pooledMutableBlockPos, states, smoothables);

		} catch (final Exception e) {
			ModUtil.crashIfNotDev(e);
		} finally {
			pooledMutableBlockPos.release();
		}

	}

	private static void fillStateCache(final IBlockState[] stateCache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final IBlockAccess cache, PooledMutableBlockPos pos) {
		int index = 0;
		for (int x = 0; x < cacheSizeX; x++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int z = 0; z < cacheSizeZ; z++) {
					stateCache[index] = cache.getBlockState(pos.setPos(renderChunkPosX + x, renderChunkPosY + y, renderChunkPosZ + z));
					index++;
				}
			}
		}
	}

	private static void fillSmoothableCache(final boolean[] smoothableCache, final IBlockState[] stateCache) {
		int index = 0;
		for (int x = 0; x < cacheSizeX; x++) {
			for (int y = 0; y < cacheSizeY; y++) {
				for (int z = 0; z < cacheSizeZ; z++) {
					smoothableCache[index] = ModUtil.shouldSmooth(stateCache[index]);
					index++;
				}
			}
		}
	}

	private static void fillDensityCache(final float[] densityCache, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final IBlockAccess cache, PooledMutableBlockPos pos, final IBlockState[] statesCache, final boolean[] smoothableCache) {
		int index = 0;
		for (int x = 0; x < 17; x++) {
			for (int y = 0; y < 17; y++) {
				for (int z = 0; z < 17; z++) {
//					densityCache[index] = getBlockDensity(smoothableCache, statesCache, cacheSizeX, cacheSizeY, cacheSizeZ, cache, renderChunkPosX, renderChunkPosY, renderChunkPosZ, x, y, z, pos);
					densityCache[index] = getBlockDensity(x, y, z, smoothableCache);
					index++;
				}
			}
		}
	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

		FACES_BLOCKPOS_MAP.get().remove(event.getRenderChunkPosition());

	}

}
