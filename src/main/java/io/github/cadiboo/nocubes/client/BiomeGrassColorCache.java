package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.BiomeColors;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class BiomeGrassColorCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<BiomeGrassColorCache> POOL = ThreadLocal.withInitial(() -> new BiomeGrassColorCache(0, 0, 0));
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);

	private int renderChunkPosX;
	private int renderChunkPosY;
	private int renderChunkPosZ;
	@Nonnull
	private IWorldReader reader;
	@Nonnull
	private int[] cache;

	private BiomeGrassColorCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		cache = new int[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public static BiomeGrassColorCache retain(
			final int sizeX, final int sizeY, final int sizeZ,
			@Nonnull final IWorldReader reader,
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ
	) {

		final BiomeGrassColorCache pooled = POOL.get();

		pooled.reader = reader;

		pooled.renderChunkPosX = renderChunkPosX;
		pooled.renderChunkPosY = renderChunkPosY;
		pooled.renderChunkPosZ = renderChunkPosZ;

		{
			final int[] tempCache = pooled.cache;
			for (int i = 0, len = tempCache.length; i < len; ++i) {
				tempCache[i] = -1;
			}
		}

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			return pooled;
		}

		pooled.sizeX = sizeX;
		pooled.sizeY = sizeY;
		pooled.sizeZ = sizeZ;

		final int size = sizeX * sizeY * sizeZ;

		if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
			pooled.cache = new int[size];
		}

		{
			final int[] tempCache = pooled.cache;
			for (int i = 0, len = tempCache.length; i < len; ++i) {
				tempCache[i] = -1;
			}
		}

		return pooled;
	}

	@Nonnull
	public int[] getBiomeGrassColorCache() {
		return cache;
	}

	@Override
	public void close() {
	}

	public int get(final int x, final int y, final int z) {
		final int[] cache = this.cache;
		final int index = getIndex(x, y, z);
		int color = cache[index];
		if (color == -1) {
			color = BiomeColors.getGrassColor(
					reader,
					MUTABLE_BLOCK_POS.get().setPos(
							// -2 because offset
							renderChunkPosX + x - 2,
							renderChunkPosY + y - 2,
							renderChunkPosZ + z - 2
					)
			);
			cache[index] = color;
		}
		return color;
	}

}
