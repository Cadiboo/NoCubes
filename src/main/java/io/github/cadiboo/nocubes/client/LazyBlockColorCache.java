package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.biome.BiomeColors.ColorResolver;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class LazyBlockColorCache extends XYZCache implements AutoCloseable {

	private static final int[] EMPTY = new int[22 * 22 * 22];
	private static final ThreadLocal<LazyBlockColorCache> POOL = ThreadLocal.withInitial(() -> new LazyBlockColorCache(0, 0, 0));
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);

	private int renderChunkPosX;
	private int renderChunkPosY;
	private int renderChunkPosZ;
	@Nonnull
	private IWorldReaderBase reader;
	@Nonnull
	private int[] cache;
	@Nonnull
	private ColorResolver colorResolver;

	private boolean inUse;

	private LazyBlockColorCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		cache = new int[sizeX * sizeY * sizeZ];
		this.inUse = false;
	}

	@Nonnull
	public static LazyBlockColorCache retain(
			final int sizeX, final int sizeY, final int sizeZ,
			@Nonnull final IWorldReaderBase reader, @Nonnull final ColorResolver colorResolver,
			final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ
	) {

		final LazyBlockColorCache pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("LazyBlockColorCache is already in use!");
		}
		pooled.inUse = true;

		pooled.reader = reader;
		pooled.colorResolver = colorResolver;

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

	private static int getColor(IWorldReaderBase worldIn, MutableBlockPos pos, ColorResolver resolver) {
		int red = 0;
		int green = 0;
		int blue = 0;
		int radius = Minecraft.getInstance().gameSettings.biomeBlendRadius;
		int area = (radius * 2 + 1) * (radius * 2 + 1);

		final int posX = pos.getX();
		final int posY = pos.getY();
		final int posZ = pos.getZ();

		int max = radius + 1;

		for (int z = -radius; z < max; ++z) {
			for (int x = -radius; x < max; ++x) {
				pos.setPos(posX + x, posY, posZ + z);
				int color = resolver.getColor(worldIn.getBiome(pos), pos);
				red += (color & 0xFF0000) >> 16;
				green += (color & '\uff00') >> 8;
				blue += color & 0xFF;
			}
		}
		return (red / area & 255) << 16 | (green / area & 255) << 8 | blue / area & 255;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

	public int get(final int x, final int y, final int z) {
		int color = this.cache[getIndex(x, y, z)];
		if (color == 0) {
			color = getColor(
					this.reader,
					MUTABLE_BLOCK_POS.get().setPos(
							// -2 because offset
							renderChunkPosX + x - 2,
							renderChunkPosY + y - 2,
							renderChunkPosZ + z - 2
					),
					this.colorResolver
			);
			this.cache[getIndex(x, y, z)] = color;
			if (color == 0) LogManager.getLogger().error("BARRRF");
		}
		return color;
	}

}
