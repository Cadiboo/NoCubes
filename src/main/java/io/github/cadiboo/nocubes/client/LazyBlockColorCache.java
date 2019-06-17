package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

import static net.minecraft.world.biome.BiomeColors.IColorResolver;

/**
 * @author Cadiboo
 */
public class LazyBlockColorCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<LazyBlockColorCache> POOL = ThreadLocal.withInitial(LazyBlockColorCache::new);
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);

	private int renderChunkPosX;
	private int renderChunkPosY;
	private int renderChunkPosZ;
	@Nonnull
	private IEnviromentBlockReader reader;
	@Nonnull
	private int[] cache;
	@Nonnull
	private IColorResolver colorResolver;

	private boolean inUse;

	//TODO: make this a non hardcoded size so that I can render small sections of the world
	private LazyBlockColorCache() {
		//From -2 to +2
		super(2, 2, 2, 20, 20, 20);
		this.cache = new int[8000];
		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, this.cache, 0, 8000);
		this.inUse = false;
	}

	@Nonnull
	public static LazyBlockColorCache retain(
			@Nonnull final IEnviromentBlockReader reader, @Nonnull final IColorResolver colorResolver,
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

		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, 8000);

		return pooled;
	}

	private static int get(final int xIn, final int yIn, final int zIn, final int[] cache, final int index, final int radius, final int area, final int max, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final MutableBlockPos pos, final IEnviromentBlockReader reader, final IColorResolver colorResolver) {
		int color = cache[index];
		if (color == -1) {

			{
				int red = 0;
				int green = 0;
				int blue = 0;

				// -2 because offset
				final int posX = renderChunkPosX + xIn - 2;
				final int posY = renderChunkPosY + yIn - 2;
				final int posZ = renderChunkPosZ + zIn - 2;

				int currentChunkPosX = posX >> 4;
				int currentChunkPosZ = posZ >> 4;
				Biome[] currentChunkBiomes = ClientUtil.getChunk(currentChunkPosX, currentChunkPosZ, reader).getBiomes();

				for (int zOffset = -radius; zOffset < max; ++zOffset) {
					for (int xOffset = -radius; xOffset < max; ++xOffset) {

						int x = posX + xOffset;
						int z = posZ + zOffset;

						if (currentChunkPosX != x >> 4 || currentChunkPosZ != z >> 4) {
							currentChunkPosX = x >> 4;
							currentChunkPosZ = z >> 4;
							currentChunkBiomes = ClientUtil.getChunk(currentChunkPosX, currentChunkPosZ, reader).getBiomes();
						}

						pos.setPos(x, posY, z);

						final int resolvedColor = colorResolver.getColor(currentChunkBiomes[(z & 15) << 4 | x & 15], pos);

						red += (resolvedColor & 0xFF0000) >> 16;
						green += (resolvedColor & '\uff00') >> 8;
						blue += resolvedColor & 0xFF;
					}
				}
				color = (red / area & 0xFF) << 16 | (green / area & 0xFF) << 8 | blue / area & 0xFF;
			}

			cache[index] = color;
			if (color == -1) {
				LogManager.getLogger().error("Color = -1. wtf");
			}
		}
		return color;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

	public int get(final int xIn, final int yIn, final int zIn) {
		return get(xIn, yIn, zIn, this.cache, Minecraft.getInstance().gameSettings.biomeBlendRadius, (Minecraft.getInstance().gameSettings.biomeBlendRadius * 2 + 1) * (Minecraft.getInstance().gameSettings.biomeBlendRadius * 2 + 1), Minecraft.getInstance().gameSettings.biomeBlendRadius + 1, this.renderChunkPosX, this.renderChunkPosY, this.renderChunkPosZ, MUTABLE_BLOCK_POS.get(), this.reader, this.colorResolver);
	}

	private int get(final int xIn, final int yIn, final int zIn, final int[] cache, final int radius, final int area, final int max, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final MutableBlockPos pos, final IEnviromentBlockReader reader, final IColorResolver colorResolver) {
		return get(xIn, yIn, zIn, cache, getIndex(xIn, yIn, zIn), radius, area, max, renderChunkPosX, renderChunkPosY, renderChunkPosZ, pos, reader, colorResolver);
	}

}
