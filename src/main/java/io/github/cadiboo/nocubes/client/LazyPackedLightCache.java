package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class LazyPackedLightCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<LazyPackedLightCache> POOL = ThreadLocal.withInitial(LazyPackedLightCache::new);
	private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
	@Nonnull
	public IBlockAccess reader;
	@Nonnull
	public StateCache stateCache;
	@Nonnull
	public int[] cache;
	private int renderChunkPosX;
	private int renderChunkPosY;
	private int renderChunkPosZ;
	private boolean inUse;

	//TODO: make this a non hardcoded size so that I can render small sections of the world
	private LazyPackedLightCache() {
		//From -2 to +2
		super(2, 2, 2, 20, 20, 20);
		this.cache = new int[8000];
		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, this.cache, 0, 8000);
		this.inUse = false;
	}

	@Nonnull
	public static LazyPackedLightCache retain(
			@Nonnull final IBlockAccess reader,
			@Nonnull final StateCache stateCache,
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ
	) {

		final LazyPackedLightCache pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("LazyPackedLightCache is already in use!");
		}
		pooled.inUse = true;

		pooled.reader = reader;
		pooled.stateCache = stateCache;

		pooled.renderChunkPosX = renderChunkPosX;
		pooled.renderChunkPosY = renderChunkPosY;
		pooled.renderChunkPosZ = renderChunkPosZ;

		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, 8000);

		return pooled;
	}

	public static int get(
			final int x, final int y, final int z,
			final int[] cache,
			final int index,
			final StateCache stateCache, final IBlockAccess reader,
			final BlockPos.MutableBlockPos mutableBlockPos,
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ,
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int diffX, final int diffY, final int diffZ
	) {
		int packedLight = cache[index];
		if (packedLight == -1) {
			packedLight = stateCache.getBlockStates()[stateCache.getIndex(x + diffX, y + diffY, z + diffZ)].getPackedLightmapCoords(
					reader,
					mutableBlockPos.setPos(
							renderChunkPosX + x - startPaddingX,
							renderChunkPosY + y - startPaddingY,
							renderChunkPosZ + z - startPaddingZ
					)
			);
			cache[index] = packedLight;
			if (packedLight == -1) LogManager.getLogger().error("BARRRF");
		}
		return packedLight;
	}

	@Deprecated
	public int get(final int x, final int y, final int z) {
		return get(x, y, z, this.cache, this.stateCache, this.reader, MUTABLE_BLOCK_POS.get(), this.renderChunkPosX, this.renderChunkPosY, this.renderChunkPosZ, this.startPaddingX, this.startPaddingY, this.startPaddingZ, this.stateCache.startPaddingX - this.startPaddingX, this.stateCache.startPaddingY - this.startPaddingY, this.stateCache.startPaddingZ - this.startPaddingZ);
	}

	@Deprecated
	public int get(final int x, final int y, final int z, final int[] cache, final StateCache stateCache, final IBlockAccess reader, final BlockPos.MutableBlockPos mutableBlockPos, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final int startPaddingX, final int startPaddingY, final int startPaddingZ, final int diffX, final int diffY, final int diffZ) {
		return get(x, y, z, cache, getIndex(x, y, z), stateCache, reader, mutableBlockPos, renderChunkPosX, renderChunkPosY, renderChunkPosZ, startPaddingX, startPaddingY, startPaddingZ, diffX, diffY, diffZ);
	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
