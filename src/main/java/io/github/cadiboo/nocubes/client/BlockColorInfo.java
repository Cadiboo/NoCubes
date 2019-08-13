package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.BiomeColors.IColorResolver;

import javax.annotation.Nonnull;
import java.util.Calendar;

import static java.util.Calendar.AUGUST;
import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.DAY_OF_YEAR;
import static java.util.Calendar.FRIDAY;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.OCTOBER;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

/**
 * @author Cadiboo
 */
public final class BlockColorInfo implements AutoCloseable {

	private static final ThreadLocal<BlockColorInfo> POOL = ThreadLocal.withInitial(() -> new BlockColorInfo(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));
	public static boolean rainbow;
	public static boolean black;
	static {
		refresh();
	}
	public float red0;
	public float green0;
	public float blue0;
	public float red1;
	public float green1;
	public float blue1;
	public float red2;
	public float green2;
	public float blue2;
	public float red3;
	public float green3;
	public float blue3;

	private boolean inUse;

	private BlockColorInfo(
			final float red0, final float green0, final float blue0,
			final float red1, final float green1, final float blue1,
			final float red2, final float green2, final float blue2,
			final float red3, final float green3, final float blue3
	) {
		this.red0 = red0;
		this.green0 = green0;
		this.blue0 = blue0;
		this.red1 = red1;
		this.green1 = green1;
		this.blue1 = blue1;
		this.red2 = red2;
		this.green2 = green2;
		this.blue2 = blue2;
		this.red3 = red3;
		this.green3 = green3;
		this.blue3 = blue3;
		this.inUse = false;
	}

	public static BlockColorInfo generateBlockColorInfo(
			@Nonnull final LazyBlockColorCache lazyBlockColorCache,
			@Nonnull final Vec3 v0,
			@Nonnull final Vec3 v1,
			@Nonnull final Vec3 v2,
			@Nonnull final Vec3 v3,
			final int chunkRenderPosX,
			final int chunkRenderPosY,
			final int chunkRenderPosZ,
			final int[] cache,
			final int sizeX, final int sizeY,
			final int biomeBlendRadius, final int area, final int max,
			final IEnviromentBlockReader reader,
			final IColorResolver colorResolver,
			final boolean useCache,
			final BlockPos.PooledMutableBlockPos pooledMutableBlockPos
	) {

		if (black) {
			return retain(
					0, 0, 0,
					0, 0, 0,
					0, 0, 0,
					0, 0, 0
			);
		}

		// TODO pool these arrays? (I think pooling them is more overhead than its worth)
		// 3x3x3 cache
		final int[] blockColor0 = new int[27];
		final int[] blockColor1 = new int[27];
		final int[] blockColor2 = new int[27];
		final int[] blockColor3 = new int[27];

		// TODO offset shouldn't be hardcoded +1 anymore
		final int v0XOffset = 1 + clamp(floor(v0.x) - chunkRenderPosX, -1, 16);
		final int v0YOffset = 1 + clamp(floor(v0.y) - chunkRenderPosY, -1, 16);
		final int v0ZOffset = 1 + clamp(floor(v0.z) - chunkRenderPosZ, -1, 16);

		final int v1XOffset = 1 + clamp(floor(v1.x) - chunkRenderPosX, -1, 16);
		final int v1YOffset = 1 + clamp(floor(v1.y) - chunkRenderPosY, -1, 16);
		final int v1ZOffset = 1 + clamp(floor(v1.z) - chunkRenderPosZ, -1, 16);

		final int v2XOffset = 1 + clamp(floor(v2.x) - chunkRenderPosX, -1, 16);
		final int v2YOffset = 1 + clamp(floor(v2.y) - chunkRenderPosY, -1, 16);
		final int v2ZOffset = 1 + clamp(floor(v2.z) - chunkRenderPosZ, -1, 16);

		final int v3XOffset = 1 + clamp(floor(v3.x) - chunkRenderPosX, -1, 16);
		final int v3YOffset = 1 + clamp(floor(v3.y) - chunkRenderPosY, -1, 16);
		final int v3ZOffset = 1 + clamp(floor(v3.z) - chunkRenderPosZ, -1, 16);

		int index = 0;
		// From (-1, -1, -1) to (1, 1, 1), accounting for cache offset
		for (int zOffset = 0; zOffset < 3; ++zOffset) {
			for (int yOffset = 0; yOffset < 3; ++yOffset) {
				for (int xOffset = 0; xOffset < 3; ++xOffset, ++index) {
					final int x0 = v0XOffset + xOffset;
					final int y0 = v0YOffset + yOffset;
					final int z0 = v0ZOffset + zOffset;
					blockColor0[index] = LazyBlockColorCache.get(x0, y0, z0, cache, lazyBlockColorCache.getIndex(x0, y0, z0, sizeX, sizeY), biomeBlendRadius, area, max, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pooledMutableBlockPos, reader, colorResolver, useCache);
					final int x1 = v1XOffset + xOffset;
					final int y1 = v1YOffset + yOffset;
					final int z1 = v1ZOffset + zOffset;
					blockColor1[index] = LazyBlockColorCache.get(x1, y1, z1, cache, lazyBlockColorCache.getIndex(x1, y1, z1, sizeX, sizeY), biomeBlendRadius, area, max, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pooledMutableBlockPos, reader, colorResolver, useCache);
					final int x2 = v2XOffset + xOffset;
					final int y2 = v2YOffset + yOffset;
					final int z2 = v2ZOffset + zOffset;
					blockColor2[index] = LazyBlockColorCache.get(x2, y2, z2, cache, lazyBlockColorCache.getIndex(x2, y2, z2, sizeX, sizeY), biomeBlendRadius, area, max, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pooledMutableBlockPos, reader, colorResolver, useCache);
					final int x3 = v3XOffset + xOffset;
					final int y3 = v3YOffset + yOffset;
					final int z3 = v3ZOffset + zOffset;
					blockColor3[index] = LazyBlockColorCache.get(x3, y3, z3, cache, lazyBlockColorCache.getIndex(x3, y3, z3, sizeX, sizeY), biomeBlendRadius, area, max, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pooledMutableBlockPos, reader, colorResolver, useCache);
				}
			}
		}

		int red0 = 0;
		int green0 = 0;
		int blue0 = 0;
		int red1 = 0;
		int green1 = 0;
		int blue1 = 0;
		int red2 = 0;
		int green2 = 0;
		int blue2 = 0;
		int red3 = 0;
		int green3 = 0;
		int blue3 = 0;

		// All arrays are 3x3x3 so 27
		for (int colorIndex = 0; colorIndex < 27; colorIndex++) {
			int color0 = blockColor0[colorIndex];
			red0 += (color0 & 0xFF0000) >> 16;
			green0 += (color0 & 0x00FF00) >> 8;
			blue0 += (color0 & 0x0000FF);
			int color1 = blockColor1[colorIndex];
			red1 += (color1 & 0xFF0000) >> 16;
			green1 += (color1 & 0x00FF00) >> 8;
			blue1 += (color1 & 0x0000FF);
			int color2 = blockColor2[colorIndex];
			red2 += (color2 & 0xFF0000) >> 16;
			green2 += (color2 & 0x00FF00) >> 8;
			blue2 += (color2 & 0x0000FF);
			int color3 = blockColor3[colorIndex];
			red3 += (color3 & 0xFF0000) >> 16;
			green3 += (color3 & 0x00FF00) >> 8;
			blue3 += (color3 & 0x0000FF);
		}

		if (rainbow) {
			return retain(
					red0, green0, blue0,
					red1, green1, blue1,
					red2, green2, blue2,
					red3, green3, blue3
			);
		} else {
			// colorPart = colorPart / 27F
			// Dividing by 0xFF here and not dividing later results in gray with purple edges
			// Dividing by 27F here and not dividing later results in weird colors that still follows biomes
			// Not dividing at all results in rainbow terrain that doesn't follow biomes but is still related to them
			red0 /= 27F;
			green0 /= 27F;
			blue0 /= 27F;
			red1 /= 27F;
			green1 /= 27F;
			blue1 /= 27F;
			red2 /= 27F;
			green2 /= 27F;
			blue2 /= 27F;
			red3 /= 27F;
			green3 /= 27F;
			blue3 /= 27F;

			// Dividing by 0xFF before and not dividing here results in gray with purple edges
			// Dividing by 27F before and not dividing here results in weird colors that still follows biomes
			// Not dividing at all results in rainbow terrain that doesn't follow biomes but is still related to them
			return retain(
					red0 / 255F, green0 / 255F, blue0 / 255F,
					red1 / 255F, green1 / 255F, blue1 / 255F,
					red2 / 255F, green2 / 255F, blue2 / 255F,
					red3 / 255F, green3 / 255F, blue3 / 255F
			);
		}

	}

	public static BlockColorInfo retain(
			final float red0, final float green0, final float blue0,
			final float red1, final float green1, final float blue1,
			final float red2, final float green2, final float blue2,
			final float red3, final float green3, final float blue3
	) {

		BlockColorInfo pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("BlockColorInfo is already in use!");
		}
		pooled.inUse = true;

		pooled.red0 = red0;
		pooled.green0 = green0;
		pooled.blue0 = blue0;
		pooled.red1 = red1;
		pooled.green1 = green1;
		pooled.blue1 = blue1;
		pooled.red2 = red2;
		pooled.green2 = green2;
		pooled.blue2 = blue2;
		pooled.red3 = red3;
		pooled.green3 = green3;
		pooled.blue3 = blue3;

		return pooled;
	}

	public static void refresh() {
		rainbow = isWearItPurpleDay();
		black = isHalloween();
	}

	private static boolean isWearItPurpleDay() {
		// "https://praveenlobo.com/blog/get-last-friday-of-the-month-in-java/"
		Calendar lastFridayOfAugust = Calendar.getInstance();
		// Wear it purple day is the last friday of august
		lastFridayOfAugust.set(MONTH, AUGUST);
		lastFridayOfAugust.add(MONTH, 1); // go to next month

		// calculate the number of days to subtract to get the last desired day of the month
		int lobosMagicNumber = (13 - FRIDAY) % 7;
		lastFridayOfAugust.add(DAY_OF_MONTH, -(((lobosMagicNumber + lastFridayOfAugust.get(DAY_OF_WEEK)) % 7) + 1));

		return lastFridayOfAugust.get(DAY_OF_YEAR) == Calendar.getInstance().get(DAY_OF_YEAR);
	}

	private static boolean isHalloween() {
		Calendar calendar = Calendar.getInstance();
		return calendar.get(MONTH) == OCTOBER && calendar.get(DAY_OF_MONTH) == 31;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
