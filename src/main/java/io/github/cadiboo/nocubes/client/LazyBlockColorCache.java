package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeColorHelper.ColorResolver;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class LazyBlockColorCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<LazyBlockColorCache> POOL = ThreadLocal.withInitial(LazyBlockColorCache::new);
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);
	@Nonnull
	private IBlockAccess reader;
	@Nonnull
	private int[] cache;
	@Nonnull
	private ColorResolver colorResolver;
	private int renderChunkPosX;
	private int renderChunkPosY;
	private int renderChunkPosZ;
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
			@Nonnull final IBlockAccess reader, @Nonnull final ColorResolver colorResolver,
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

	private static int get(final int xIn, final int yIn, final int zIn, final int[] cache, final int index, final int radius, final int area, final int max, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final MutableBlockPos pos, final IBlockAccess reader, final ColorResolver colorResolver) {
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
				byte[] currentChunkBiomes = ClientUtil.getChunk(currentChunkPosX, currentChunkPosZ, reader).getBiomeArray();

				for (int zOffset = -radius; zOffset < max; ++zOffset) {
					for (int xOffset = -radius; xOffset < max; ++xOffset) {

						int x = posX + xOffset;
						int z = posZ + zOffset;

						if (currentChunkPosX != x >> 4 || currentChunkPosZ != z >> 4) {
							currentChunkPosX = x >> 4;
							currentChunkPosZ = z >> 4;
							currentChunkBiomes = ClientUtil.getChunk(currentChunkPosX, currentChunkPosZ, reader).getBiomeArray();
						}

						pos.setPos(x, posY, z);

						final int resolvedColor = colorResolver.getColorAtPos(Biome.getBiome(currentChunkBiomes[(z & 15) << 4 | x & 15], Biomes.DEFAULT), pos);

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

	@Deprecated
	public int get(final int xIn, final int yIn, final int zIn) {
		final int biomeBlendRadius = 3;// Minecraft.getInstance().gameSettings.biomeBlendRadius;
		return get(xIn, yIn, zIn, this.cache, biomeBlendRadius, (biomeBlendRadius * 2 + 1) * (biomeBlendRadius * 2 + 1), biomeBlendRadius + 1, this.renderChunkPosX, this.renderChunkPosY, this.renderChunkPosZ, MUTABLE_BLOCK_POS.get(), this.reader, this.colorResolver);
	}

	@Deprecated
	private int get(final int xIn, final int yIn, final int zIn, final int[] cache, final int radius, final int area, final int max, final int renderChunkPosX, final int renderChunkPosY, final int renderChunkPosZ, final MutableBlockPos pos, final IBlockAccess reader, final ColorResolver colorResolver) {
		return get(xIn, yIn, zIn, cache, getIndex(xIn, yIn, zIn), radius, area, max, renderChunkPosX, renderChunkPosY, renderChunkPosZ, pos, reader, colorResolver);
	}

}
