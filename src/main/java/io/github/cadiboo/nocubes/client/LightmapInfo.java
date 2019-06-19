package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

/**
 * @author Cadiboo
 */
public class LightmapInfo implements AutoCloseable {

	private static final Logger LOGGER = LogManager.getLogger();

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
			@Nonnull final LazyPackedLightCache lazyPackedLightCache,
			@Nonnull final Vec3 v0,
			@Nonnull final Vec3 v1,
			@Nonnull final Vec3 v2,
			final Vec3 v3,
			final int renderChunkPosX,
			final int renderChunkPosY,
			final int renderChunkPosZ,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		try (final ModProfiler ignored = ModProfiler.get().start("generateLightmapInfo")) {
			switch (Minecraft.getMinecraft().gameSettings.ambientOcclusion) {
				case 0:
					return generateLightmapInfoFlat(v0, renderChunkPosX, renderChunkPosY, renderChunkPosZ, lazyPackedLightCache, pooledMutableBlockPos);
				default:
				case 1:
					return generateLightmapInfoSmooth(v0, v1, v2, v3, renderChunkPosX, renderChunkPosY, renderChunkPosZ, lazyPackedLightCache, pooledMutableBlockPos);
				case 2:
					return generateLightmapInfoSmoothAO(v0, v1, v2, v3, renderChunkPosX, renderChunkPosY, renderChunkPosZ, lazyPackedLightCache, pooledMutableBlockPos);
			}
		}
	}

	private static LightmapInfo generateLightmapInfoSmoothAO(
			@Nonnull final Vec3 v0, @Nonnull final Vec3 v1, @Nonnull final Vec3 v2, @Nonnull final Vec3 v3,
			final int renderChunkPosX,
			final int renderChunkPosY,
			final int renderChunkPosZ,
			@Nonnull final LazyPackedLightCache packedLightCache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		return generateLightmapInfoSmooth(v0, v1, v2, v3, renderChunkPosX, renderChunkPosY, renderChunkPosZ, packedLightCache, pooledMutableBlockPos);
	}

	private static LightmapInfo generateLightmapInfoSmooth(
			@Nonnull final Vec3 v0, @Nonnull final Vec3 v1, @Nonnull final Vec3 v2, @Nonnull final Vec3 v3,
			final int renderChunkPosX,
			final int renderChunkPosY,
			final int renderChunkPosZ,
			@Nonnull final LazyPackedLightCache lazyPackedLightCache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {
		// TODO pool these arrays? (I think pooling them is more overhead than its worth)
		// 3x3x3 cache
		final int[] packedLight0 = new int[27];
		final int[] packedLight1 = new int[27];
		final int[] packedLight2 = new int[27];
		final int[] packedLight3 = new int[27];

		final int v0XOffset = 1 + clamp(floor(v0.x) - renderChunkPosX, -1, 16);
		final int v0YOffset = 1 + clamp(floor(v0.y) - renderChunkPosY, -1, 16);
		final int v0ZOffset = 1 + clamp(floor(v0.z) - renderChunkPosZ, -1, 16);

		final int v1XOffset = 1 + clamp(floor(v1.x) - renderChunkPosX, -1, 16);
		final int v1YOffset = 1 + clamp(floor(v1.y) - renderChunkPosY, -1, 16);
		final int v1ZOffset = 1 + clamp(floor(v1.z) - renderChunkPosZ, -1, 16);

		final int v2XOffset = 1 + clamp(floor(v2.x) - renderChunkPosX, -1, 16);
		final int v2YOffset = 1 + clamp(floor(v2.y) - renderChunkPosY, -1, 16);
		final int v2ZOffset = 1 + clamp(floor(v2.z) - renderChunkPosZ, -1, 16);

		final int v3XOffset = 1 + clamp(floor(v3.x) - renderChunkPosX, -1, 16);
		final int v3YOffset = 1 + clamp(floor(v3.y) - renderChunkPosY, -1, 16);
		final int v3ZOffset = 1 + clamp(floor(v3.z) - renderChunkPosZ, -1, 16);

		int index = 0;
		// From (-1, -1, -1) to (1, 1, 1), accounting for cache offset
		for (int zOffset = 0; zOffset < 3; ++zOffset) {
			for (int yOffset = 0; yOffset < 3; ++yOffset) {
				for (int xOffset = 0; xOffset < 3; ++xOffset, ++index) {
					packedLight0[index] = lazyPackedLightCache.get((v0XOffset + xOffset), (v0YOffset + yOffset), (v0ZOffset + zOffset));
					packedLight1[index] = lazyPackedLightCache.get((v1XOffset + xOffset), (v1YOffset + yOffset), (v1ZOffset + zOffset));
					packedLight2[index] = lazyPackedLightCache.get((v2XOffset + xOffset), (v2YOffset + yOffset), (v2ZOffset + zOffset));
					packedLight3[index] = lazyPackedLightCache.get((v3XOffset + xOffset), (v3YOffset + yOffset), (v3ZOffset + zOffset));
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
			@Nonnull final Vec3 v0,
			final int renderChunkPosX,
			final int renderChunkPosY,
			final int renderChunkPosZ,
			@Nonnull final LazyPackedLightCache lazyPackedLightCache,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {

		final int v0XOffset = 1 + clamp(floor(v0.x) - renderChunkPosX, -1, 16);
		final int v0YOffset = 1 + clamp(floor(v0.y) - renderChunkPosY, -1, 16);
		final int v0ZOffset = 1 + clamp(floor(v0.z) - renderChunkPosZ, -1, 16);

		final int[] packedLight0 = new int[27];

		final int[] cache = lazyPackedLightCache.cache;
		final StateCache stateCache = lazyPackedLightCache.stateCache;
		final IBlockAccess reader = lazyPackedLightCache.reader;
		final int startPaddingX = lazyPackedLightCache.startPaddingX;
		final int startPaddingY = lazyPackedLightCache.startPaddingY;
		final int startPaddingZ = lazyPackedLightCache.startPaddingZ;
		final int diffX = stateCache.startPaddingX - startPaddingX;
		final int diffY = stateCache.startPaddingY - startPaddingY;
		final int diffZ = stateCache.startPaddingZ - startPaddingZ;

		int index = 0;
		// From (-1, -1, -1) to (1, 1, 1), accounting for cache offset
		for (int zOffset = 0; zOffset < 3; ++zOffset) {
			for (int yOffset = 0; yOffset < 3; ++yOffset) {
				for (int xOffset = 0; xOffset < 3; ++xOffset, ++index) {
					packedLight0[index] = lazyPackedLightCache.get((v0XOffset + xOffset), (v0YOffset + yOffset), (v0ZOffset + zOffset), cache, stateCache, reader, pooledMutableBlockPos, renderChunkPosX, renderChunkPosY, renderChunkPosZ, startPaddingX, startPaddingY, startPaddingZ, diffX, diffY, diffZ);
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
