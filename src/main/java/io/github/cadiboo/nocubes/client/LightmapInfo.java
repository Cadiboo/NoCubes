package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import javax.annotation.Nonnull;

import static net.minecraft.util.math.MathHelper.floor;

/**
 * @author Cadiboo
 */
public final class LightmapInfo implements AutoCloseable {

	private static final ThreadLocal<LightmapInfo> POOL = ThreadLocal.withInitial(() -> new LightmapInfo(0, 0, 0, 0, 0, 0, 0, 0));

	public int skylight0;
	public int skylight1;
	public int skylight2;
	public int skylight3;
	public int blocklight0;
	public int blocklight1;
	public int blocklight2;
	public int blocklight3;

	private boolean inUse;

	private LightmapInfo(final int skylight0, final int skylight1, final int skylight2, final int skylight3, final int blocklight0, final int blocklight1, final int blocklight2, final int blocklight3) {
		this.skylight0 = skylight0;
		this.skylight1 = skylight1;
		this.skylight2 = skylight2;
		this.skylight3 = skylight3;
		this.blocklight0 = blocklight0;
		this.blocklight1 = blocklight1;
		this.blocklight2 = blocklight2;
		this.blocklight3 = blocklight3;
		this.inUse = false;
	}

	public static LightmapInfo generateLightmapInfo(
		@Nonnull final LightCache lazyPackedLightCache,
		@Nonnull final Vec v0,
		@Nonnull final Vec v1,
		@Nonnull final Vec v2,
		final Vec v3,
		@Nonnull final MutableBlockPos pooledMutableBlockPos
	) {
		try (final ModProfiler ignored = ModProfiler.get().start("generateLightmapInfo")) {
			switch (Minecraft.getMinecraft().gameSettings.ambientOcclusion) {
				case 0:
					return generateLightmapInfoFlat(v0, lazyPackedLightCache, pooledMutableBlockPos);
				default:
				case 1:
					return generateLightmapInfoSmooth(v0, v1, v2, v3, lazyPackedLightCache, pooledMutableBlockPos);
				case 2:
					return generateLightmapInfoSmoothAO(v0, v1, v2, v3, lazyPackedLightCache, pooledMutableBlockPos);
			}
		}
	}

	private static LightmapInfo generateLightmapInfoSmoothAO(
		@Nonnull final Vec v0, @Nonnull final Vec v1, @Nonnull final Vec v2, @Nonnull final Vec v3,
		@Nonnull final LightCache packedLightCache,
		@Nonnull final MutableBlockPos pooledMutableBlockPos
	) {
		return generateLightmapInfoSmooth(v0, v1, v2, v3, packedLightCache, pooledMutableBlockPos);
	}

	private static LightmapInfo generateLightmapInfoSmooth(
		@Nonnull final Vec v0, @Nonnull final Vec v1, @Nonnull final Vec v2, @Nonnull final Vec v3,
		@Nonnull final LightCache light,
		@Nonnull final MutableBlockPos pooledMutableBlockPos
	) {
		// TODO pool these arrays? (I think pooling them is more overhead than its worth)
		// 3x3x3 cache
		final int[] packedLight0 = new int[3 * 3 * 3];
		final int[] packedLight1 = new int[3 * 3 * 3];
		final int[] packedLight2 = new int[3 * 3 * 3];
		final int[] packedLight3 = new int[3 * 3 * 3];

		final int v0XOffset = floor(v0.x);
		final int v0YOffset = floor(v0.y);
		final int v0ZOffset = floor(v0.z);

		final int v1XOffset = floor(v1.x);
		final int v1YOffset = floor(v1.y);
		final int v1ZOffset = floor(v1.z);

		final int v2XOffset = floor(v2.x);
		final int v2YOffset = floor(v2.y);
		final int v2ZOffset = floor(v2.z);

		final int v3XOffset = floor(v3.x);
		final int v3YOffset = floor(v3.y);
		final int v3ZOffset = floor(v3.z);

		int index = 0;
		// From (0, 0, 0) to (1, 1, 1), accounting for cache offset
		for (int zOffset = -1; zOffset < 1; ++zOffset) {
			for (int yOffset = -1; yOffset < 1; ++yOffset) {
				for (int xOffset = -1; xOffset < 1; ++xOffset, ++index) {
					final int x0 = v0XOffset + xOffset;
					final int y0 = v0YOffset + yOffset;
					final int z0 = v0ZOffset + zOffset;
					packedLight0[index] = light.get(x0, y0, z0, pooledMutableBlockPos);
					final int x1 = v1XOffset + xOffset;
					final int y1 = v1YOffset + yOffset;
					final int z1 = v1ZOffset + zOffset;
					packedLight1[index] = light.get(x1, y1, z1, pooledMutableBlockPos);
					final int x2 = v2XOffset + xOffset;
					final int y2 = v2YOffset + yOffset;
					final int z2 = v2ZOffset + zOffset;
					packedLight2[index] = light.get(x2, y2, z2, pooledMutableBlockPos);
					final int x3 = v3XOffset + xOffset;
					final int y3 = v3YOffset + yOffset;
					final int z3 = v3ZOffset + zOffset;
					packedLight3[index] = light.get(x3, y3, z3, pooledMutableBlockPos);
				}
			}
		}

		final int skylight0 = getSkylight(packedLight0);
		final int skylight1 = getSkylight(packedLight1);
		final int skylight2 = getSkylight(packedLight2);
		final int skylight3 = getSkylight(packedLight3);

		final int blocklight0 = getBlocklight(packedLight0);
		final int blocklight1 = getBlocklight(packedLight1);
		final int blocklight2 = getBlocklight(packedLight2);
		final int blocklight3 = getBlocklight(packedLight3);

		return retain(
			skylight0, skylight1, skylight2, skylight3,
			blocklight0, blocklight1, blocklight2, blocklight3
		);
	}

	private static LightmapInfo generateLightmapInfoFlat(
		@Nonnull final Vec v0,
		@Nonnull final LightCache lazyPackedLightCache,
		@Nonnull final MutableBlockPos pooledMutableBlockPos
	) {
		final int v0XOffset = floor(v0.x);
		final int v0YOffset = floor(v0.y);
		final int v0ZOffset = floor(v0.z);

		final int[] packedLight0 = new int[3 * 3 * 3];

		int index = 0;
		// From (-1, -1, -1) to (1, 1, 1)
		for (int zOffset = -1; zOffset < 2; ++zOffset) {
			for (int yOffset = -1; yOffset < 2; ++yOffset) {
				for (int xOffset = -1; xOffset < 2; ++xOffset, ++index) {
					packedLight0[index] = lazyPackedLightCache.get(
						v0XOffset + xOffset,
						v0YOffset + yOffset,
						v0ZOffset + zOffset,
						pooledMutableBlockPos
					);
				}
			}
		}

		final int skylight0 = getSkylight(packedLight0);

		final int blocklight0 = getBlocklight(packedLight0);

		return retain(
			skylight0, skylight0, skylight0, skylight0,
			blocklight0, blocklight0, blocklight0, blocklight0
		);
	}

	private static int getSkylight(final int[] packedLight) {
		return max(
			packedLight[0] >> 16 & 0xFFFF,
			packedLight[1] >> 16 & 0xFFFF,
			packedLight[2] >> 16 & 0xFFFF,
			packedLight[3] >> 16 & 0xFFFF,
			packedLight[4] >> 16 & 0xFFFF,
			packedLight[5] >> 16 & 0xFFFF,
			packedLight[6] >> 16 & 0xFFFF,
			packedLight[7] >> 16 & 0xFFFF,
			packedLight[8] >> 16 & 0xFFFF,
			packedLight[9] >> 16 & 0xFFFF,
			packedLight[10] >> 16 & 0xFFFF,
			packedLight[11] >> 16 & 0xFFFF,
			packedLight[12] >> 16 & 0xFFFF,
			packedLight[13] >> 16 & 0xFFFF,
			packedLight[14] >> 16 & 0xFFFF,
			packedLight[15] >> 16 & 0xFFFF,
			packedLight[16] >> 16 & 0xFFFF,
			packedLight[17] >> 16 & 0xFFFF,
			packedLight[18] >> 16 & 0xFFFF,
			packedLight[19] >> 16 & 0xFFFF,
			packedLight[20] >> 16 & 0xFFFF,
			packedLight[21] >> 16 & 0xFFFF,
			packedLight[22] >> 16 & 0xFFFF,
			packedLight[23] >> 16 & 0xFFFF,
			packedLight[24] >> 16 & 0xFFFF,
			packedLight[25] >> 16 & 0xFFFF,
			packedLight[26] >> 16 & 0xFFFF
		);
	}

	private static int getBlocklight(final int[] packedLight) {
		return max(
			packedLight[0] & 0xFFFF,
			packedLight[1] & 0xFFFF,
			packedLight[2] & 0xFFFF,
			packedLight[3] & 0xFFFF,
			packedLight[4] & 0xFFFF,
			packedLight[5] & 0xFFFF,
			packedLight[6] & 0xFFFF,
			packedLight[7] & 0xFFFF,
			packedLight[8] & 0xFFFF,
			packedLight[9] & 0xFFFF,
			packedLight[10] & 0xFFFF,
			packedLight[11] & 0xFFFF,
			packedLight[12] & 0xFFFF,
			packedLight[13] & 0xFFFF,
			packedLight[14] & 0xFFFF,
			packedLight[15] & 0xFFFF,
			packedLight[16] & 0xFFFF,
			packedLight[17] & 0xFFFF,
			packedLight[18] & 0xFFFF,
			packedLight[19] & 0xFFFF,
			packedLight[20] & 0xFFFF,
			packedLight[21] & 0xFFFF,
			packedLight[22] & 0xFFFF,
			packedLight[23] & 0xFFFF,
			packedLight[24] & 0xFFFF,
			packedLight[25] & 0xFFFF,
			packedLight[26] & 0xFFFF
		);
	}

	private static int max(int i0, final int i1, final int i2, final int i3, final int i4, final int i5, final int i6, final int i7, final int i8, final int i9, final int i10, final int i11, final int i12, final int i13, final int i14, final int i15, final int i16, final int i17, final int i18, final int i19, final int i20, final int i21, final int i22, final int i23, final int i24, final int i25, final int i26) {
		if (i1 > i0) i0 = i1;
		if (i2 > i0) i0 = i2;
		if (i3 > i0) i0 = i3;
		if (i4 > i0) i0 = i4;
		if (i5 > i0) i0 = i5;
		if (i6 > i0) i0 = i6;
		if (i7 > i0) i0 = i7;
		if (i8 > i0) i0 = i8;
		if (i9 > i0) i0 = i9;
		if (i10 > i0) i0 = i10;
		if (i11 > i0) i0 = i11;
		if (i12 > i0) i0 = i12;
		if (i13 > i0) i0 = i13;
		if (i14 > i0) i0 = i14;
		if (i15 > i0) i0 = i15;
		if (i16 > i0) i0 = i16;
		if (i17 > i0) i0 = i17;
		if (i18 > i0) i0 = i18;
		if (i19 > i0) i0 = i19;
		if (i20 > i0) i0 = i20;
		if (i21 > i0) i0 = i21;
		if (i22 > i0) i0 = i22;
		if (i23 > i0) i0 = i23;
		if (i24 > i0) i0 = i24;
		if (i25 > i0) i0 = i25;
		return (i0 > i26) ? i0 : i26;
	}

	public static LightmapInfo retain(
		final int skylight0, final int skylight1, final int skylight2, final int skylight3,
		final int blocklight0, final int blocklight1, final int blocklight2, final int blocklight3
	) {

		LightmapInfo pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("LightmapInfo is already in use!");
		}
		pooled.inUse = true;

		pooled.skylight0 = skylight0;
		pooled.skylight1 = skylight1;
		pooled.skylight2 = skylight2;
		pooled.skylight3 = skylight3;
		pooled.blocklight0 = blocklight0;
		pooled.blocklight1 = blocklight1;
		pooled.blocklight2 = blocklight2;
		pooled.blocklight3 = blocklight3;

		return pooled;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
