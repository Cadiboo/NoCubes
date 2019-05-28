package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunkCache;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.Region;
import net.minecraft.world.biome.BiomeColors.ColorResolver;
import net.minecraft.world.chunk.IChunk;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * @author Cadiboo
 */
public class LazyBlockColorCache extends XYZCache implements AutoCloseable {

	private static final int[] NEGATIVE_1 = new int[22 * 22 * 22];
	private static final ThreadLocal<LazyBlockColorCache> POOL = ThreadLocal.withInitial(() -> new LazyBlockColorCache(0, 0, 0));
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);

	static {
		Arrays.fill(NEGATIVE_1, -1);
	}

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
		this.cache = new int[sizeX * sizeY * sizeZ];
		System.arraycopy(NEGATIVE_1, 0, this.cache, 0, sizeX * sizeY * sizeZ);
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
			System.arraycopy(NEGATIVE_1, 0, pooled.cache, 0, sizeX * sizeY * sizeZ);
			return pooled;
		} else {
			pooled.sizeX = sizeX;
			pooled.sizeY = sizeY;
			pooled.sizeZ = sizeZ;

			final int size = sizeX * sizeY * sizeZ;

			if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
				pooled.cache = new int[size];
			}

			System.arraycopy(NEGATIVE_1, 0, pooled.cache, 0, size);
			return pooled;
		}
	}

	@Override
	public void close() {
		this.inUse = false;
	}

	public int get(final int xIn, final int yIn, final int zIn) {
		int color = this.cache[getIndex(xIn, yIn, zIn)];
		if (color == -1) {

			{
				int red = 0;
				int green = 0;
				int blue = 0;
				final int radius = Minecraft.getInstance().gameSettings.biomeBlendRadius;
				final int area = (radius * 2 + 1) * (radius * 2 + 1);
				final int max = radius + 1;

				// -2 because offset
				final int posX = renderChunkPosX + xIn - 2;
				final int posY = renderChunkPosY + yIn - 2;
				final int posZ = renderChunkPosZ + zIn - 2;

				final MutableBlockPos pos = MUTABLE_BLOCK_POS.get();
				final IWorldReaderBase reader = this.reader;
				final ColorResolver colorResolver = this.colorResolver;
				int currentChunkPosX = posX >> 4;
				int currentChunkPosZ = posZ >> 4;
				IChunk currentChunk = getChunk(currentChunkPosX, currentChunkPosZ, reader);

				for (int zOffset = -radius; zOffset < max; ++zOffset) {
					for (int xOffset = -radius; xOffset < max; ++xOffset) {

						int x = posX + xOffset;
						int z = posZ + zOffset;

						if (currentChunkPosX != x >> 4 || currentChunkPosZ != z >> 4) {
							currentChunkPosX = x >> 4;
							currentChunkPosZ = z >> 4;
							currentChunk = getChunk(currentChunkPosX, currentChunkPosZ, reader);
						}

						pos.setPos(x, posY, z);

						final int resolvedColor = colorResolver.getColor(currentChunk.getBiomes()[(z & 15) << 4 | x & 15], pos);

						red += (resolvedColor & 0xFF0000) >> 16;
						green += (resolvedColor & '\uff00') >> 8;
						blue += resolvedColor & 0xFF;
					}
				}
				color = (red / area & 0xFF) << 16 | (green / area & 0xFF) << 8 | blue / area & 0xFF;
			}

			this.cache[getIndex(xIn, yIn, zIn)] = color;
			if (color == -1) {
				LogManager.getLogger().error("Color = -1. wtf");
			}
		}
		return color;
	}

	private IChunk getChunk(final int currentChunkPosX, final int currentChunkPosZ, final IWorldReaderBase reader) {
//		if (reader instanceof IWorld) { // This should never be the case...
//			return ((IWorld) reader).getChunk(currentChunkPosX, currentChunkPosZ);
//		} else
		if (reader instanceof RenderChunkCache) {
			RenderChunkCache renderChunkCache = (RenderChunkCache) reader;
			final int x = currentChunkPosX - renderChunkCache.chunkStartX;
			final int z = currentChunkPosZ - renderChunkCache.chunkStartZ;
			return renderChunkCache.chunks[x][z];
		} else if (OptiFineCompatibility.isChunkCacheOF(reader)) {
			Region region = OptiFineCompatibility.getRegion(reader);
			final int x = currentChunkPosX - region.chunkX;
			final int z = currentChunkPosZ - region.chunkZ;
			return region.chunkArray[x][z];
		}
		throw new IllegalStateException("Should Not Reach Here!");
	}

}
