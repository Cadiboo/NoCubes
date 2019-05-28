package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * @author Cadiboo
 */
public class LazyPackedLightCache extends XYZCache implements AutoCloseable {

	protected static final int[] EMPTY_NEGATIVE_1 = new int[22 * 22 * 22];
	private static final ThreadLocal<LazyPackedLightCache> POOL = ThreadLocal.withInitial(() -> new LazyPackedLightCache(0, 0, 0));
	private static final ThreadLocal<BlockPos.MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
	static {
		Arrays.fill(EMPTY_NEGATIVE_1, -1);
	}

	private int renderChunkPosX;
	private int renderChunkPosY;
	private int renderChunkPosZ;
	@Nonnull
	private IBlockAccess reader;
	@Nonnull
	private StateCache stateCache;
	@Nonnull
	private int[] cache;

	private boolean inUse;

	private LazyPackedLightCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		this.cache = new int[sizeX * sizeY * sizeZ];
		System.arraycopy(EMPTY_NEGATIVE_1, 0, this.cache, 0, sizeX * sizeY * sizeZ);
		this.inUse = false;
	}

	@Nonnull
	public static LazyPackedLightCache retain(
			final int sizeX, final int sizeY, final int sizeZ,
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

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			System.arraycopy(EMPTY_NEGATIVE_1, 0, pooled.cache, 0, sizeX * sizeY * sizeZ);
			return pooled;
		} else {
			pooled.sizeX = sizeX;
			pooled.sizeY = sizeY;
			pooled.sizeZ = sizeZ;

			final int size = sizeX * sizeY * sizeZ;

			if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
				pooled.cache = new int[size];
			}

			System.arraycopy(EMPTY_NEGATIVE_1, 0, pooled.cache, 0, sizeX * sizeY * sizeZ);

			return pooled;
		}
	}

	public int get(final int x, final int y, final int z) {
		int packedLight = cache[getIndex(x, y, z)];
		if (packedLight == -1) {
			packedLight = stateCache.getBlockStates()[stateCache.getIndex(x, y, z)].getPackedLightmapCoords(
					reader,
					MUTABLE_BLOCK_POS.get().setPos(
							// -2 because offset
							renderChunkPosX + x - 2,
							renderChunkPosY + y - 2,
							renderChunkPosZ + z - 2
					)
			);
			this.cache[getIndex(x, y, z)] = packedLight;
			if (packedLight == -1) LogManager.getLogger().error("BARRRF");
		}
		return packedLight;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
