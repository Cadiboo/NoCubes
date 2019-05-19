package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Vec3;

import javax.annotation.Nonnull;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

/**
 * @author Cadiboo
 */
public class BlockColorInfo implements AutoCloseable {

	private static final ThreadLocal<BlockColorInfo> POOL = ThreadLocal.withInitial(() -> new BlockColorInfo(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1));

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

	private boolean isReleased = true;

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
	}

	public static BlockColorInfo generateBiomeGrassColorInfo(
			@Nonnull final LazyBlockColorCache biomeGrassColorCache,
			@Nonnull final Vec3 v0,
			@Nonnull final Vec3 v1,
			@Nonnull final Vec3 v2,
			@Nonnull final Vec3 v3,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ
	) {
		try (final ModProfiler ignored = ModProfiler.get().start("generateBiomeGrassColorInfo")) {
			return generateBiomeGrassColorInfo(v0, v1, v2, v3, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, biomeGrassColorCache);
		}
	}

	private static BlockColorInfo generateBiomeGrassColorInfo(
			@Nonnull final Vec3 v0, @Nonnull final Vec3 v1, @Nonnull final Vec3 v2, @Nonnull final Vec3 v3,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ,
			@Nonnull final LazyBlockColorCache biomeGrassColorCache
	) {
		// TODO pool these arrays? (I think pooling them is more overhead than its worth)
		// 3x3x3 cache
		final int[] blockColor0 = new int[27];
		final int[] blockColor1 = new int[27];
		final int[] blockColor2 = new int[27];
		final int[] blockColor3 = new int[27];

		final int v0XOffset = 1 + clamp(floor(v0.x) - renderChunkPositionX, -1, 16);
		final int v0YOffset = 1 + clamp(floor(v0.y) - renderChunkPositionY, -1, 16);
		final int v0ZOffset = 1 + clamp(floor(v0.z) - renderChunkPositionZ, -1, 16);

		final int v1XOffset = 1 + clamp(floor(v1.x) - renderChunkPositionX, -1, 16);
		final int v1YOffset = 1 + clamp(floor(v1.y) - renderChunkPositionY, -1, 16);
		final int v1ZOffset = 1 + clamp(floor(v1.z) - renderChunkPositionZ, -1, 16);

		final int v2XOffset = 1 + clamp(floor(v2.x) - renderChunkPositionX, -1, 16);
		final int v2YOffset = 1 + clamp(floor(v2.y) - renderChunkPositionY, -1, 16);
		final int v2ZOffset = 1 + clamp(floor(v2.z) - renderChunkPositionZ, -1, 16);

		final int v3XOffset = 1 + clamp(floor(v3.x) - renderChunkPositionX, -1, 16);
		final int v3YOffset = 1 + clamp(floor(v3.y) - renderChunkPositionY, -1, 16);
		final int v3ZOffset = 1 + clamp(floor(v3.z) - renderChunkPositionZ, -1, 16);

		int index = 0;
		// From (-1, -1, -1) to (1, 1, 1), accounting for cache offset
		for (int zOffset = 0; zOffset < 3; ++zOffset) {
			for (int yOffset = 0; yOffset < 3; ++yOffset) {
				for (int xOffset = 0; xOffset < 3; ++xOffset, ++index) {
					blockColor0[index] = biomeGrassColorCache.get((v0XOffset + xOffset), (v0YOffset + yOffset), (v0ZOffset + zOffset));
					blockColor1[index] = biomeGrassColorCache.get((v1XOffset + xOffset), (v1YOffset + yOffset), (v1ZOffset + zOffset));
					blockColor2[index] = biomeGrassColorCache.get((v2XOffset + xOffset), (v2YOffset + yOffset), (v2ZOffset + zOffset));
					blockColor3[index] = biomeGrassColorCache.get((v3XOffset + xOffset), (v3YOffset + yOffset), (v3ZOffset + zOffset));
				}
			}
		}

		//TODO FIXME this is very wrong
		final int color0 = getColor(blockColor0);
		final int color1 = getColor(blockColor1);
		final int color2 = getColor(blockColor2);
		final int color3 = getColor(blockColor3);

		final float red0 = ((color0 >> 16) & 255) / 255F;
		final float green0 = ((color0 >> 8) & 255) / 255F;
		final float blue0 = ((color0) & 255) / 255F;
		final float red1 = ((color1 >> 16) & 255) / 255F;
		final float green1 = ((color1 >> 8) & 255) / 255F;
		final float blue1 = ((color1) & 255) / 255F;
		final float red2 = ((color2 >> 16) & 255) / 255F;
		final float green2 = ((color2 >> 8) & 255) / 255F;
		final float blue2 = ((color2) & 255) / 255F;
		final float red3 = ((color3 >> 16) & 255) / 255F;
		final float green3 = ((color3 >> 8) & 255) / 255F;
		final float blue3 = ((color3) & 255) / 255F;

		return retain(
				red0, green0, blue0,
				red1, green1, blue1,
				red2, green2, blue2,
				red3, green3, blue3
		);
	}

	private static int getColor(final int[] colors) {
		return max(
				colors[0],
				colors[1],
				colors[2],
				colors[3],
				colors[4],
				colors[5],
				colors[6],
				colors[7],
				colors[8],
				colors[9],
				colors[10],
				colors[11],
				colors[12],
				colors[13],
				colors[14],
				colors[15],
				colors[16],
				colors[17],
				colors[18],
				colors[19],
				colors[20],
				colors[21],
				colors[22],
				colors[23],
				colors[24],
				colors[25],
				colors[26]
		);
	}

	private static int max(int i0, final int i1, final int i2, final int i3, final int i4, final int i5, final int i6, final int i7, final int i8, final int i9, final int i10, final int i11, final int i12, final int i13, final int i14, final int i15, final int i16, final int i17, final int i18, final int i19, final int i20, final int i21, final int i22, final int i23, final int i24, final int i25, final int i26) {
		i0 = (i0 >= i1) ? i0 : i1;
		i0 = (i0 >= i2) ? i0 : i2;
		i0 = (i0 >= i3) ? i0 : i3;
		i0 = (i0 >= i4) ? i0 : i4;
		i0 = (i0 >= i5) ? i0 : i5;
		i0 = (i0 >= i6) ? i0 : i6;
		i0 = (i0 >= i7) ? i0 : i7;
		i0 = (i0 >= i8) ? i0 : i8;
		i0 = (i0 >= i9) ? i0 : i9;
		i0 = (i0 >= i10) ? i0 : i10;
		i0 = (i0 >= i11) ? i0 : i11;
		i0 = (i0 >= i12) ? i0 : i12;
		i0 = (i0 >= i13) ? i0 : i13;
		i0 = (i0 >= i14) ? i0 : i14;
		i0 = (i0 >= i15) ? i0 : i15;
		i0 = (i0 >= i16) ? i0 : i16;
		i0 = (i0 >= i17) ? i0 : i17;
		i0 = (i0 >= i18) ? i0 : i18;
		i0 = (i0 >= i19) ? i0 : i19;
		i0 = (i0 >= i20) ? i0 : i20;
		i0 = (i0 >= i21) ? i0 : i21;
		i0 = (i0 >= i22) ? i0 : i22;
		i0 = (i0 >= i23) ? i0 : i23;
		i0 = (i0 >= i24) ? i0 : i24;
		i0 = (i0 >= i25) ? i0 : i25;
		return (i0 >= i26) ? i0 : i26;
	}

	public static BlockColorInfo retain(
			final float red0, final float green0, final float blue0,
			final float red1, final float green1, final float blue1,
			final float red2, final float green2, final float blue2,
			final float red3, final float green3, final float blue3
	) {

		BlockColorInfo pooled = POOL.get();

		if (!pooled.isReleased) {
			throw new IllegalStateException();
		}

		pooled.isReleased = false;

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

	@Override
	public void close() {
		this.isReleased = true;
	}

}
