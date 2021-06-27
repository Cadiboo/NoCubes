package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ThreadLocalArrayCache;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

/**
 * @author Cadiboo
 */
public final class LazyBlockColorCache {

	private static final ThreadLocalArrayCache<int[]> CACHE = new ThreadLocalArrayCache<>(int[]::new, array -> array.length, ClientUtil::resetIntArray);

	private final Area area;
	private int[] array;

	public LazyBlockColorCache(Area area) {
		this.area = area;
	}

	//
//	public int chunkRenderPosX;
//	public int chunkRenderPosY;
//	public int chunkRenderPosZ;
//	public IBlockAccess reader;
//	public int[] cache;
//	public ColorResolver colorResolver;
//	public Predicate<IBlockState> shouldApply;
//
//	private boolean inUse;
//
//	private LazyBlockColorCache(
//			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
//			final int sizeX, final int sizeY, final int sizeZ
//	) {
//		super(startPaddingX, startPaddingY, startPaddingZ, sizeX, sizeY, sizeZ);
//		final int size = sizeX * sizeY * sizeZ;
//		this.cache = new int[size];
//		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, this.cache, 0, size);
//		this.inUse = false;
//	}
//
//	@Nonnull
//	public static LazyBlockColorCache retain(
//			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
//			final int sizeX, final int sizeY, final int sizeZ,
//			@Nonnull final IBlockAccess reader, @Nonnull final ColorResolver colorResolver, @Nonnull final Predicate<IBlockState> shouldApply,
//			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ
//	) {
//
//		final LazyBlockColorCache pooled = POOL.get();
//
//		if (pooled.inUse) {
//			throw new IllegalStateException("LazyBlockColorCache is already in use!");
//		}
//		pooled.inUse = true;
//
//		pooled.reader = reader;
//		pooled.colorResolver = colorResolver;
//		pooled.shouldApply = shouldApply;
//
//		pooled.chunkRenderPosX = chunkRenderPosX;
//		pooled.chunkRenderPosY = chunkRenderPosY;
//		pooled.chunkRenderPosZ = chunkRenderPosZ;
//
//		pooled.startPaddingX = startPaddingX;
//		pooled.startPaddingY = startPaddingY;
//		pooled.startPaddingZ = startPaddingZ;
//
//		final int size = sizeX * sizeY * sizeZ;
//
//		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
//			System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, size);
//			return pooled;
//		}
//
//		pooled.sizeX = sizeX;
//		pooled.sizeY = sizeY;
//		pooled.sizeZ = sizeZ;
//
//		if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
//			pooled.cache = new int[size];
//		}
//
//		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, size);
//
//		return pooled;
//	}
//
//	public static int get(
//			final int xIn, final int yIn, final int zIn,
//			final int[] cache,
//			final int index,
//			final int radius, final int area, final int max,
//			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
//			final MutableBlockPos pos,
//			final IBlockAccess reader,
//			final ColorResolver colorResolver,
//			final boolean useCache
//	) {
//		try {
//			if (useCache) {
//				int color = cache[index];
//				if (color == -1) {
//					color = getColor(xIn, yIn, zIn, radius, area, max, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pos, reader, colorResolver);
//					cache[index] = color;
//					if (color == -1) {
//						LogManager.getLogger().error("Color = -1. wtf");
//					}
//				}
//				return color;
//			} else {
//				return getColor(xIn, yIn, zIn, radius, area, max, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pos, reader, colorResolver);
//			}
//		} catch (final ArrayIndexOutOfBoundsException e) {
//			throw new CustomArrayIndexOutOfBoundsException(
//					xIn, yIn, zIn,
//					cache,
//					index,
//					radius, area, max,
//					chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
//					pos,
//					reader,
//					colorResolver,
//					e
//			);
//		}
//	}
//
//	public static int getColor(final int xIn, final int yIn, final int zIn, final int radius, final int area, final int max, final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ, final MutableBlockPos pos, final IBlockAccess reader, final ColorResolver colorResolver) {
//		int red = 0;
//		int green = 0;
//		int blue = 0;
//
//		// TODO FIXME: I don't think that this should be hardcoded -2?
//		// -2 because offset
//		final int posX = chunkRenderPosX + xIn - 2;
//		final int posY = chunkRenderPosY + yIn - 2;
//		final int posZ = chunkRenderPosZ + zIn - 2;
//
//		int currentChunkPosX = posX >> 4;
//		int currentChunkPosZ = posZ >> 4;
//		byte[] currentChunkBiomes = ClientUtil.getChunk(currentChunkPosX, currentChunkPosZ, reader).getBiomeArray();
//
//		for (int zOffset = -radius; zOffset < max; ++zOffset) {
//			for (int xOffset = -radius; xOffset < max; ++xOffset) {
//
//				int x = posX + xOffset;
//				int z = posZ + zOffset;
//
//				if (currentChunkPosX != x >> 4 || currentChunkPosZ != z >> 4) {
//					currentChunkPosX = x >> 4;
//					currentChunkPosZ = z >> 4;
//					currentChunkBiomes = ClientUtil.getChunk(currentChunkPosX, currentChunkPosZ, reader).getBiomeArray();
//				}
//
//				pos.setPos(x, posY, z);
//
//				final int resolvedColor = colorResolver.getColorAtPos(Biome.getBiome(currentChunkBiomes[(z & 15) << 4 | x & 15], Biomes.DEFAULT), pos);
//
//				red += (resolvedColor & 0xFF0000) >> 16;
//				green += (resolvedColor & '\uff00') >> 8;
//				blue += resolvedColor & 0xFF;
//			}
//		}
//		return (red / area & 0xFF) << 16 | (green / area & 0xFF) << 8 | blue / area & 0xFF;
//	}
//
//	@Override
//	public void close() {
//		this.inUse = false;
//	}
//
//	@Deprecated
//	public int get(final int xIn, final int yIn, final int zIn) {
////		final int biomeBlendRadius = Minecraft.getInstance().gameSettings.biomeBlendRadius;
//		final int biomeBlendRadius = 3;
//		final int d = biomeBlendRadius * 2 + 1;
//		return get(xIn, yIn, zIn, this.cache, getIndex(xIn, yIn, zIn, this.sizeX, this.sizeY), biomeBlendRadius, d * d, biomeBlendRadius + 1, this.chunkRenderPosX, this.chunkRenderPosY, this.chunkRenderPosZ, MUTABLE_BLOCK_POS.get(), this.reader, this.colorResolver, true);
//	}
//
//	@Deprecated
//	public int get(final int xIn, final int yIn, final int zIn, final int[] cache, final int radius, final int area, final int max, final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ, final MutableBlockPos pos, final IBlockAccess reader, final ColorResolver colorResolver, final boolean useCache) {
//		return get(xIn, yIn, zIn, cache, getIndex(xIn, yIn, zIn, this.sizeX, this.sizeY), radius, area, max, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pos, reader, colorResolver, useCache);
//	}
//
//	private static class CustomArrayIndexOutOfBoundsException extends EnhancedRuntimeException {
//
//		private final int xIn;
//		private final int yIn;
//		private final int zIn;
//		private final int[] cache;
//		private final int index;
//		private final int radius;
//		private final int area;
//		private final int max;
//		private final int chunkRenderPosX;
//		private final int chunkRenderPosY;
//		private final int chunkRenderPosZ;
//		private final MutableBlockPos pos;
//		private final IBlockAccess reader;
//		private final ColorResolver colorResolver;
//
//		CustomArrayIndexOutOfBoundsException(final int xIn, final int yIn, final int zIn, final int[] cache, final int index, final int radius, final int area, final int max, final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ, final MutableBlockPos pos, final IBlockAccess reader, final ColorResolver colorResolver, final ArrayIndexOutOfBoundsException e) {
//			super(e);
//			this.xIn = xIn;
//			this.yIn = yIn;
//			this.zIn = zIn;
//			this.cache = cache;
//			this.index = index;
//			this.radius = radius;
//			this.area = area;
//			this.max = max;
//			this.chunkRenderPosX = chunkRenderPosX;
//			this.chunkRenderPosY = chunkRenderPosY;
//			this.chunkRenderPosZ = chunkRenderPosZ;
//			this.pos = pos;
//			this.reader = reader;
//			this.colorResolver = colorResolver;
//		}
//
//		@Override
//		protected void printStackTrace(final WrappedPrintStream stream) {
//			stream.println("xIn: " + xIn);
//			stream.println("yIn: " + yIn);
//			stream.println("zIn: " + zIn);
//			stream.println("cache: " + cache);
//			stream.println("index: " + index);
//			stream.println("radius: " + radius);
//			stream.println("area: " + area);
//			stream.println("max: " + max);
//			stream.println("chunkRenderPosX: " + chunkRenderPosX);
//			stream.println("chunkRenderPosY: " + chunkRenderPosY);
//			stream.println("chunkRenderPosZ: " + chunkRenderPosZ);
//			stream.println("pos: " + pos);
//			stream.println("reader: " + reader);
//			stream.println("colorResolver: " + colorResolver);
//		}
//
//	}

//	public int get(Vec v) {
//	}

	/** x, y & z are relative to the start of the area. */
	public int get(int x, int y, int z, MutableBlockPos unsetWorldPos) {
		int index = index(x, y, z);
		int[] array = getArray();
		int color = array[index];
		if (color == -1) {
			BlockPos start = area.start;
			unsetWorldPos.setPos(start.getX() + x, start.getY() + y, start.getZ() + z);
			array[index] = color = compute(index, unsetWorldPos);
		}
		return color;
	}

	private int compute(int index, MutableBlockPos worldPos) {
		Area area = this.area;
		return area.getAndCacheBlocks()[index].getPackedLightmapCoords(area.world, worldPos);
	}

	private int[] getArray() {
		int[] array = this.array;
		if (array == null)
			this.array = array = CACHE.takeArray(area.getLength());
		return array;
	}

	private int index(int x, int y, int z) {
		BlockPos size = area.size;
		return size.getX() * size.getY() * z + size.getX() * y + x;
	}

}
