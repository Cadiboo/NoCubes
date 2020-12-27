//package io.github.cadiboo.nocubes.client;
//
//import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.init.Biomes;
//import net.minecraft.util.math.BlockPos.MutableBlockPos;
//import net.minecraft.world.IBlockAccess;
//import net.minecraft.world.biome.Biome;
//import net.minecraft.world.biome.BiomeColorHelper.ColorResolver;
//import net.minecraftforge.fml.common.EnhancedRuntimeException;
//import org.apache.logging.log4j.LogManager;
//
//import javax.annotation.Nonnull;
//import java.util.function.Predicate;
//
///**
// * @author Cadiboo
// */
//public final class LazyBlockColorCache extends XYZCache implements AutoCloseable {
//
//	private static final ThreadLocal<LazyBlockColorCache> POOL = ThreadLocal.withInitial(() -> new LazyBlockColorCache(0, 0, 0, 0, 0, 0));
//	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);
//
//	public int chunkRenderPosX;
//	public int chunkRenderPosY;
//	public int chunkRenderPosZ;
//	@Nonnull
//	public IBlockAccess reader;
//	@Nonnull
//	public int[] cache;
//	@Nonnull
//	public ColorResolver colorResolver;
//	@Nonnull
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
//
//}
