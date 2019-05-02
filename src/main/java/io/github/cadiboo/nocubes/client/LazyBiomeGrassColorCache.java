package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.BiomeColors;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class LazyBiomeGrassColorCache extends XYZCache implements AutoCloseable {

	protected static final int[] EMPTY = new int[22 * 22 * 22];
	private static final ThreadLocal<LazyBiomeGrassColorCache> POOL = ThreadLocal.withInitial(() -> new LazyBiomeGrassColorCache(0, 0, 0));
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);

	private int renderChunkPosX;
	private int renderChunkPosY;
	private int renderChunkPosZ;
	@Nonnull
	private IWorldReader reader;
	@Nonnull
	private int[] cache;

	private LazyBiomeGrassColorCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		cache = new int[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public static LazyBiomeGrassColorCache retain(
			final int sizeX, final int sizeY, final int sizeZ,
			@Nonnull final IWorldReader reader,
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ
	) {

		final LazyBiomeGrassColorCache pooled = POOL.get();

		pooled.reader = reader;

		pooled.renderChunkPosX = renderChunkPosX;
		pooled.renderChunkPosY = renderChunkPosY;
		pooled.renderChunkPosZ = renderChunkPosZ;

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			System.arraycopy(EMPTY, 0, pooled.cache, 0, sizeX * sizeY * sizeZ);
			return pooled;
		} else {
			pooled.sizeX = sizeX;
			pooled.sizeY = sizeY;
			pooled.sizeZ = sizeZ;

			final int size = sizeX * sizeY * sizeZ;

			if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
				pooled.cache = new int[size];
			}

			return pooled;
		}
	}

	@Override
	public void close() {
	}

	public int get(final int x, final int y, final int z) {
		int color = this.cache[getIndex(x, y, z)];
		if (color == 0) {
			color = BiomeColors.getGrassColor(
					reader,
					MUTABLE_BLOCK_POS.get().setPos(
							// -2 because offset
							renderChunkPosX + x - 2,
							renderChunkPosY + y - 2,
							renderChunkPosZ + z - 2
					)
			);
			this.cache[getIndex(x, y, z)] = color;
			if (color == 0) LogManager.getLogger().error("BARRRF");
		}
		return color;
	}

}
