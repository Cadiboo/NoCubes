package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.Vec;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.EnhancedRuntimeException;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class LazyPackedLightCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<LazyPackedLightCache> POOL = ThreadLocal.withInitial(() -> new LazyPackedLightCache(0, 0, 0, 0, 0, 0));
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);
	public IBlockAccess reader;
	public StateCache stateCache;
	public int[] cache;
	private int chunkRenderPosX;
	private int chunkRenderPosY;
	private int chunkRenderPosZ;
	private boolean inUse;

	private LazyPackedLightCache(
		final int startPaddingX, final int startPaddingY, final int startPaddingZ,
		final int sizeX, final int sizeY, final int sizeZ
	) {
		super(startPaddingX, startPaddingY, startPaddingZ, sizeX, sizeY, sizeZ);
		final int size = sizeX * sizeY * sizeZ;
		this.cache = new int[size];
		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, this.cache, 0, size);
		this.inUse = false;
	}

	@Nonnull
	public static LazyPackedLightCache retain(
		final int startPaddingX, final int startPaddingY, final int startPaddingZ,
		final int sizeX, final int sizeY, final int sizeZ,
		@Nonnull final IBlockAccess reader,
		@Nonnull final StateCache stateCache,
		final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ
	) {

		final LazyPackedLightCache pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("LazyPackedLightCache is already in use!");
		}
		pooled.inUse = true;

		pooled.reader = reader;
		pooled.stateCache = stateCache;

		pooled.chunkRenderPosX = chunkRenderPosX;
		pooled.chunkRenderPosY = chunkRenderPosY;
		pooled.chunkRenderPosZ = chunkRenderPosZ;

		pooled.startPaddingX = startPaddingX;
		pooled.startPaddingY = startPaddingY;
		pooled.startPaddingZ = startPaddingZ;

		final int size = sizeX * sizeY * sizeZ;

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, size);
			return pooled;
		}

		pooled.sizeX = sizeX;
		pooled.sizeY = sizeY;
		pooled.sizeZ = sizeZ;

		if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
			pooled.cache = new int[size];
		}

		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, size);

		return pooled;
	}

	public static int get(
		final int x, final int y, final int z,
		final int[] cache,
		final int index,
		final StateCache stateCache, final IBlockAccess reader,
		final MutableBlockPos mutableBlockPos,
		final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
		final int startPaddingX, final int startPaddingY, final int startPaddingZ,
		final int diffX, final int diffY, final int diffZ,
		final int stateCacheSizeX, final int stateCacheSizeY
	) {
		int packedLight = cache[index];
		if (packedLight == -1)
			cache[index] = packedLight = stateCache.getBlockStates()[stateCache.getIndex(
				x + diffX,
				y + diffY,
				z + diffZ,
				stateCacheSizeX, stateCacheSizeY
			)].getPackedLightmapCoords(
				reader,
				mutableBlockPos.setPos(
					chunkRenderPosX + x - startPaddingX,
					chunkRenderPosY + y - startPaddingY,
					chunkRenderPosZ + z - startPaddingZ
				)
			);
		assert packedLight != -1;
		return packedLight;
	}

	public int get(Vec v) {

	}

	public int get(int x, int y, int z) {
		int index = index(x, y, z);
		int color = array[index];
		if (color == -1)
			array[index] = color = compute(x, y, z);
		return color;
	}

}
