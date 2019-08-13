package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class CacheUtil {

//	@SuppressWarnings("MismatchedReadAndWriteOfArray") // It should never be written to and all its values should always be null
//	private static final IFluidState[] NULL_FLUID_STATES = new IFluidState[8000];
//	private static final ThreadLocal<IFluidState[]> EXTEND_FLUID_STATES_THREAD_LOCAL = ThreadLocal.withInitial(() -> new IFluidState[0]);

	/**
	 * Generates a {@link StateCache}
	 */
	public static StateCache generateStateCache(
			// from position
			final int fromX, final int fromY, final int fromZ,
			// to position
			final int toX, final int toY, final int toZ,
			// the difference between the chunkRenderPosition and from position. Always positive
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			@Nonnull final World cache,
			@Nonnull PooledMutableBlockPos pooledMutableBlockPos
	) {
		final int cacheSizeX = Math.abs(toX - fromX);
		final int cacheSizeY = Math.abs(toY - fromY);
		final int cacheSizeZ = Math.abs(toZ - fromZ);
		final StateCache stateCache = StateCache.retain(
				startPaddingX, startPaddingY, startPaddingZ,
				cacheSizeX, cacheSizeY, cacheSizeZ
		);
		try {
			final IBlockState[] blockStates = stateCache.getBlockStates();
//			final IFluidState[] fluidStates = stateCache.getFluidStates();

			try (ModProfiler ignored = ModProfiler.get().start("fillStateCache")) {
				fillStateCache(fromX, fromY, fromZ, cacheSizeX, cacheSizeY, cacheSizeZ, cache, pooledMutableBlockPos, blockStates/*, fluidStates*/);
			}
//			try (ModProfiler ignored = ModProfiler.get().start("calculateStateCacheExtendedFluids")) {
//				calculateStateCacheExtendedFluids(cacheSizeX, cacheSizeY, cacheSizeZ, blockStates, fluidStates, stateCache, stateCache.sizeX, stateCache.sizeY);
//			}

			return stateCache;
		} catch (final Exception e) {
			// getBlockState/getFluidState can throw an error if its trying to get a region for a chunk out of bounds
			// close the state cache to prevent errors about it already being in use
			stateCache.close();
			throw e;
		}
	}

//	/**
//	 * Because we fill state caches in an optimised way, our World#getFluidState override isn't applied,
//	 * so we need to extend fluids.
//	 */
//	private static void calculateStateCacheExtendedFluids(
//			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
//			@Nonnull final BlockState[] blockStates, @Nonnull final IFluidState[] fluidStates, @Nonnull final StateCache stateCache,
//			final int stateCacheSizeX, final int stateCacheSizeY
//	) {
//		final int extend = Config.extendFluidsRange.getRange();
//		if (extend < 1) {
//			return;
//		}
//		final int fluidStatesLength = fluidStates.length;
//		IFluidState[] extendedFluidStates = EXTEND_FLUID_STATES_THREAD_LOCAL.get();
//		if (extendedFluidStates.length < fluidStatesLength) {
//			extendedFluidStates = new IFluidState[fluidStatesLength];
//			EXTEND_FLUID_STATES_THREAD_LOCAL.set(extendedFluidStates);
//		} else {
//			if (fluidStatesLength > 0x4000) {
//				Arrays.fill(extendedFluidStates, null);
//			} else {
//				System.arraycopy(NULL_FLUID_STATES, 0, extendedFluidStates, 0, fluidStatesLength);
//			}
//		}
//
//		int index = 0;
//		for (int z = 0; z < cacheSizeZ; ++z) {
//			for (int y = 0; y < cacheSizeY; ++y) {
//				for (int x = 0; x < cacheSizeX; ++x, ++index) {
//
//					// Do not extend if not terrain smoothable
//					if (!blockStates[index].nocubes_isTerrainSmoothable) {
//						continue;
//					}
//
//					SEARCH_FOR_FLUID:
//					for (int xOffset = -extend; xOffset <= extend; ++xOffset) {
//						for (int zOffset = -extend; zOffset <= extend; ++zOffset) {
//
//							// No point in checking myself
//							if (xOffset == 0 && zOffset == 0) {
//								continue;
//							}
//
//							final int checkX = x + xOffset;
//							final int checkZ = z + zOffset;
//
//							if (checkX < 0 || checkX >= cacheSizeX) continue;
//							if (checkZ < 0 || checkZ >= cacheSizeZ) continue;
//
//							final IFluidState state1 = fluidStates[stateCache.getIndex(
//									checkX,
//									y,
//									checkZ,
//									stateCacheSizeX, stateCacheSizeY
//							)];
//							if (state1.isSource()) {
//								extendedFluidStates[index] = state1;
//								break SEARCH_FOR_FLUID;
//							}
//
//						}
//					}
//
//				}
//			}
//		}
//
//		for (int i = 0; i < fluidStatesLength; ++i) {
//			final IFluidState extendedFluidState = extendedFluidStates[i];
//			if (extendedFluidState != null) {
//				fluidStates[i] = extendedFluidState;
//			}
//		}
//
//	}

	/**
	 * Fills a state cache in an optimised way getting values from chunks rather than from the world
	 * Could be optimised even more to use ChunkSections
	 */
	private static void fillStateCache(
			final int fromX, final int fromY, final int fromZ,
			final int cacheSizeX, final int cacheSizeY, final int cacheSizeZ,
			@Nonnull final World cache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			final IBlockState[] blockStates//, final IFluidState[] fluidStates
	) {

//		int index = 0;
//		for (int z = 0; z < cacheSizeZ; ++z) {
//			for (int y = 0; y < cacheSizeY; ++y) {
//				for (int x = 0; x < cacheSizeX; ++x, ++index) {
//					pooledMutableBlockPos.setPos(fromX + x, fromY + y, fromZ + z);
//					blockStates[index] = cache.getBlockState(pooledMutableBlockPos);
//					fluidStates[index] = cache.getFluidState(pooledMutableBlockPos);
//				}
//			}
//		}

		int cx = fromX >> 4;
		int cz = fromZ >> 4;
		Chunk currentChunk = cache.getChunk(cx, cz);
		int index = 0;
		for (int z = 0; z < cacheSizeZ; ++z) {
			for (int y = 0; y < cacheSizeY; ++y) {
				for (int x = 0; x < cacheSizeX; ++x, ++index) {

					final int checkX = fromX + x;
					final int checkZ = fromZ + z;

					boolean changed = false;

					if (cx != checkX >> 4) {
						cx = checkX >> 4;
						changed = true;
					}
					if (cz != checkZ >> 4) {
						cz = checkZ >> 4;
						changed = true;
					}
					if (changed) {
						currentChunk = cache.getChunk(cx, cz);
					}

					// TODO: Use System.arrayCopy on the chunk sections
					pooledMutableBlockPos.setPos(checkX, fromY + y, checkZ);
//					blockStates[index] = currentChunk.getBlockState(pooledMutableBlockPos);
//					fluidStates[index] = currentChunk.getFluidState(pooledMutableBlockPos);
					final IBlockState blockState = currentChunk.getBlockState(pooledMutableBlockPos);
					blockStates[index] = blockState;
//					fluidStates[index] = blockState.getFluidState();
				}
			}
		}
	}

	/**
	 * Generates a {@link SmoothableCache} from a {@link StateCache}
	 */
	public static SmoothableCache generateSmoothableCache(
			// from position
			// Usually chunkRenderPosition - 1 because DensityCaches need 1 extra block on each negative axis
			final int fromX, final int fromY, final int fromZ,
			// to position
			// Usually chunkRenderPosition + size + 1 because SmoothableCaches need 1 extra block on each axis
			final int toX, final int toY, final int toZ,
			// the difference between the chunkRenderPosition and from position. Always positive
			// Usually 1 because DensityCaches need 1 extra block on each negative axis
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final IsSmoothable isStateSmoothable
	) {
		final int cacheSizeX = Math.abs(toX - fromX);
		final int cacheSizeY = Math.abs(toY - fromY);
		final int cacheSizeZ = Math.abs(toZ - fromZ);
		try (ModProfiler ignored = ModProfiler.get().start("generate smoothableCache")) {
			final SmoothableCache smoothableCache = SmoothableCache.retain(startPaddingX, startPaddingY, startPaddingZ, cacheSizeX, cacheSizeY, cacheSizeZ);
			final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();

			final int stateCacheSizeX = stateCache.sizeX;
			final int stateCacheSizeY = stateCache.sizeY;
			final int diffX = stateCache.startPaddingX - startPaddingX;
			final int diffY = stateCache.startPaddingY - startPaddingY;
			final int diffZ = stateCache.startPaddingZ - startPaddingZ;
			final IBlockState[] blockStateArray = stateCache.getBlockStates();

			int smoothableIndex = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++smoothableIndex) {
						smoothableCacheArray[smoothableIndex] = isStateSmoothable.apply(
								blockStateArray[stateCache.getIndex(
										x + diffX,
										y + diffY,
										z + diffZ,
										stateCacheSizeX, stateCacheSizeY
								)]
						);
					}
				}
			}

			return smoothableCache;
		}
	}

	/**
	 * Generates a {@link DensityCache} from a {@link StateCache} and a {@link SmoothableCache}
	 */
	public static DensityCache generateDensityCache(
			// from position
			// Usually chunkRenderPosition - 1 because DensityCaches need 1 extra block on each negative axis
			final int fromX, final int fromY, final int fromZ,
			// to position
			final int toX, final int toY, final int toZ,
			// the difference between the chunkRenderPosition and from position. Always positive
			// Usually 1 because DensityCaches need 1 extra block on each negative axis
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache
	) {
		final int cacheSizeX = Math.abs(toX - fromX);
		final int cacheSizeY = Math.abs(toY - fromY);
		final int cacheSizeZ = Math.abs(toZ - fromZ);
		try (ModProfiler ignored = ModProfiler.get().start("generate densityCache")) {
			final DensityCache densityCache = DensityCache.retain(startPaddingX, startPaddingY, startPaddingZ, cacheSizeX, cacheSizeY, cacheSizeZ);
			final float[] densityCacheArray = densityCache.getDensityCache();

			int index = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++index) {
						densityCacheArray[index] = getBlockDensity(
								x, y, z,
								startPaddingX, startPaddingY, startPaddingZ,
								stateCache, smoothableCache
						);
					}
				}
			}
			return densityCache;
		}
	}

	/**
	 * Gets the density for a block (between -8 and 8) based on the smoothability of itself and its 7 neighbours in positive directions
	 */
	private static float getBlockDensity(
			final int posX, final int posY, final int posZ,
			// the difference between the chunkRenderPosition and the density cache's from position. Always positive
			final int densityCacheStartPaddingX, final int densityCacheStartPaddingY, final int densityCacheStartPaddingZ,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache
	) {
		final int stateCacheDiffX = stateCache.startPaddingX - densityCacheStartPaddingX;
		final int stateCacheDiffY = stateCache.startPaddingY - densityCacheStartPaddingY;
		final int stateCacheDiffZ = stateCache.startPaddingZ - densityCacheStartPaddingZ;

		final int stateCacheSizeX = stateCache.sizeX;
		final int stateCacheSizeY = stateCache.sizeY;
		final IBlockState[] stateCacheArray = stateCache.getBlockStates();

		final int smoothableCacheSizeX = smoothableCache.sizeX;
		final int smoothableCacheSizeY = smoothableCache.sizeY;
		final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();

		float density = 0;
		for (int zOffset = 0; zOffset < 2; ++zOffset) {
			for (int yOffset = 0; yOffset < 2; ++yOffset) {
				for (int xOffset = 0; xOffset < 2; ++xOffset) {
					density += ModUtil.getIndividualBlockDensity(
							smoothableCacheArray[smoothableCache.getIndex(
									posX + xOffset,
									posY + yOffset,
									posZ + zOffset,
									smoothableCacheSizeX, smoothableCacheSizeY
							)],
							stateCacheArray[stateCache.getIndex(
									posX + xOffset + stateCacheDiffX,
									posY + yOffset + stateCacheDiffY,
									posZ + zOffset + stateCacheDiffZ,
									stateCacheSizeX, stateCacheSizeY
							)]
					);
				}
			}
		}
		return density;
	}

}
