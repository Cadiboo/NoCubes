package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

import static io.github.cadiboo.nocubes.util.ModUtil.max;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

/**
 * @author Cadiboo
 */
public class LightmapInfo implements AutoCloseable {

	private static int instances = 0;

	public int skylight0;
	public int skylight1;
	public int skylight2;
	public int skylight3;
	public int blocklight0;
	public int blocklight1;
	public int blocklight2;
	public int blocklight3;

	private static final ThreadLocal<LightmapInfo> POOL = ThreadLocal.withInitial(() -> new LightmapInfo(0, 0, 0, 0, 0, 0, 0, 0));

	private LightmapInfo(final int skylight0, final int skylight1, final int skylight2, final int skylight3, final int blocklight0, final int blocklight1, final int blocklight2, final int blocklight3) {
		this.skylight0 = skylight0;
		this.skylight1 = skylight1;
		this.skylight2 = skylight2;
		this.skylight3 = skylight3;
		this.blocklight0 = blocklight0;
		this.blocklight1 = blocklight1;
		this.blocklight2 = blocklight2;
		this.blocklight3 = blocklight3;
		++instances;
	}

	public static LightmapInfo generateLightmapInfo(
			final PackedLightCache packedLightCache,
			final Vec3 v0,
			final Vec3 v1,
			final Vec3 v2,
			final Vec3 v3,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ
	) {
		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateLightmapInfo")) {
			switch (Minecraft.getMinecraft().gameSettings.ambientOcclusion) {
				case 0:
					return generateLightmapInfoFlat(v0, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, packedLightCache.sizeX, packedLightCache.sizeY, packedLightCache.sizeZ, packedLightCache.getPackedLightCache());
				default:
				case 1:
					return generateLightmapInfoSmooth(v0, v1, v2, v3, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, packedLightCache.sizeX, packedLightCache.sizeY, packedLightCache.sizeZ, packedLightCache.getPackedLightCache());
				case 2:
					return generateLightmapInfoSmoothAO(v0, v1, v2, v3, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, packedLightCache.sizeX, packedLightCache.sizeY, packedLightCache.sizeZ, packedLightCache.getPackedLightCache());
			}
		}
	}

	private static LightmapInfo generateLightmapInfoSmoothAO(
			@Nonnull final Vec3 v0, @Nonnull final Vec3 v1, @Nonnull final Vec3 v2, @Nonnull final Vec3 v3,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ,
			final int cachesSizeX,
			final int cachesSizeY,
			final int cachesSizeZ,
			@Nonnull final int[] packedLightCacheArray
	) {
		return generateLightmapInfoSmooth(v0, v1, v2, v3, renderChunkPositionX, renderChunkPositionY, renderChunkPositionZ, cachesSizeX, cachesSizeY, cachesSizeZ, packedLightCacheArray);
	}

	private static LightmapInfo generateLightmapInfoSmooth(
			@Nonnull final Vec3 v0, @Nonnull final Vec3 v1, @Nonnull final Vec3 v2, @Nonnull final Vec3 v3,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ,
			final int cachesSizeX,
			final int cachesSizeY,
			final int cachesSizeZ,
			@Nonnull final int[] packedLightCacheArray
	) {
		// TODO pool these arrays? (I think pooling them is more overhead than its worth)
		// 3x3x3 cache
		final int[] packedLight0 = new int[27];
		final int[] packedLight1 = new int[27];
		final int[] packedLight2 = new int[27];
		final int[] packedLight3 = new int[27];

		final int v0XOffset = clamp(floor(v0.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v0YOffset = clamp(floor(v0.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v0ZOffset = clamp(floor(v0.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		final int v1XOffset = clamp(floor(v1.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v1YOffset = clamp(floor(v1.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v1ZOffset = clamp(floor(v1.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		final int v2XOffset = clamp(floor(v2.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v2YOffset = clamp(floor(v2.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v2ZOffset = clamp(floor(v2.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		final int v3XOffset = clamp(floor(v3.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v3YOffset = clamp(floor(v3.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v3ZOffset = clamp(floor(v3.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		int index = 0;
		// From (-1, -1, -1) to (1, 1, 1), accounting for cache offset
		for (int zOffset = 0; zOffset < 3; ++zOffset) {
			for (int yOffset = 0; yOffset < 3; ++yOffset) {
				for (int xOffset = 0; xOffset < 3; ++xOffset, ++index) {
					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					packedLight0[index] = packedLightCacheArray[(v0XOffset + xOffset) + cachesSizeX * ((v0YOffset + yOffset) + cachesSizeY * (v0ZOffset + zOffset))];
					packedLight1[index] = packedLightCacheArray[(v1XOffset + xOffset) + cachesSizeX * ((v1YOffset + yOffset) + cachesSizeY * (v1ZOffset + zOffset))];
					packedLight2[index] = packedLightCacheArray[(v2XOffset + xOffset) + cachesSizeX * ((v2YOffset + yOffset) + cachesSizeY * (v2ZOffset + zOffset))];
					packedLight3[index] = packedLightCacheArray[(v3XOffset + xOffset) + cachesSizeX * ((v3YOffset + yOffset) + cachesSizeY * (v3ZOffset + zOffset))];
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
			final Vec3 v0,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ,
			final int cachesSizeX,
			final int cachesSizeY,
			final int cachesSizeZ,
			final int[] packedLightCacheArray
	) {

		final int v3XOffset = clamp(floor(v0.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v3YOffset = clamp(floor(v0.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v3ZOffset = clamp(floor(v0.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		final int[] packedLight3 = new int[27];

		int index = 0;
		// From (-1, -1, -1) to (1, 1, 1), accounting for cache offset
		for (int zOffset = 0; zOffset < 3; ++zOffset) {
			for (int yOffset = 0; yOffset < 3; ++yOffset) {
				for (int xOffset = 0; xOffset < 3; ++xOffset, ++index) {
					// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
					packedLight3[index] = packedLightCacheArray[(v3XOffset + xOffset) + cachesSizeX * ((v3YOffset + yOffset) + cachesSizeY * (v3ZOffset + zOffset))];
				}
			}
		}

		final int skylight3 = getSkylight(packedLight3);

		final int blocklight3 = getBlocklight(packedLight3);

		return retain(
				skylight3, skylight3, skylight3, skylight3,
				blocklight3, blocklight3, blocklight3, blocklight3
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

	public static LightmapInfo retain(final int skylight0, final int skylight1, final int skylight2,
	                                  final int skylight3, final int blocklight0, final int blocklight1, final int blocklight2,
	                                  final int blocklight3) {

		LightmapInfo pooled = POOL.get();

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
	}

	public static int getInstances() {
		return instances;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		--instances;
	}

}
