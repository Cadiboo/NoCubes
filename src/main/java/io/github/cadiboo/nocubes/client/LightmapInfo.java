package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import static java.lang.Math.floor;
import static net.minecraft.util.math.MathHelper.clamp;

/**
 * @author Cadiboo
 */
//TODO pooling?
public class LightmapInfo {

	public final int skylight0;

	public LightmapInfo(final int skylight0, final int skylight1, final int skylight2, final int skylight3, final int blocklight0, final int blocklight1, final int blocklight2, final int blocklight3) {
		this.skylight0 = skylight0;
		this.skylight1 = skylight1;
		this.skylight2 = skylight2;
		this.skylight3 = skylight3;
		this.blocklight0 = blocklight0;
		this.blocklight1 = blocklight1;
		this.blocklight2 = blocklight2;
		this.blocklight3 = blocklight3;
	}

	public final int skylight1;
	public final int skylight2;
	public final int skylight3;
	public final int blocklight0;
	public final int blocklight1;
	public final int blocklight2;
	public final int blocklight3;

	public static LightmapInfo generateLightmapInfo(
			final PackedLightCache packedLightCache,
			final Vec3 v0,
			final Vec3 v1,
			final Vec3 v2,
			final Vec3 v3,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ,
			final int[] pos,
			final IBlockAccess blockAccess, final PooledMutableBlockPos pooledMutableBlockPos
	) {

		final int cachesSizeX = packedLightCache.sizeX;
		final int cachesSizeY = packedLightCache.sizeY;
		final int cachesSizeZ = packedLightCache.sizeZ;

		final int[] packedLightCacheArray = packedLightCache.getPackedLightCache();

		// 3*3*3
		final int[] packedLight0 = new int[27];
		final int[] packedLight1 = new int[27];
		final int[] packedLight2 = new int[27];
		final int[] packedLight3 = new int[27];

		final int v0XOffset = clamp((int) floor(v0.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v0YOffset = clamp((int) floor(v0.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v0ZOffset = clamp((int) floor(v0.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		final int v1XOffset = clamp((int) floor(v1.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v1YOffset = clamp((int) floor(v1.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v1ZOffset = clamp((int) floor(v1.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		final int v2XOffset = clamp((int) floor(v2.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v2YOffset = clamp((int) floor(v2.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v2ZOffset = clamp((int) floor(v2.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		final int v3XOffset = clamp((int) floor(v3.x) - renderChunkPositionX, 0, cachesSizeX - 1);
		final int v3YOffset = clamp((int) floor(v3.y) - renderChunkPositionY, 0, cachesSizeY - 1);
		final int v3ZOffset = clamp((int) floor(v3.z) - renderChunkPositionZ, 0, cachesSizeZ - 1);

		int index = 0;
		for (int zOffset = -1; zOffset < 2; zOffset++) {
			for (int yOffset = -1; yOffset < 2; yOffset++) {
				for (int xOffset = -1; xOffset < 2; xOffset++, index++) {
					packedLight0[index] = packedLightCacheArray[packedLightCache.getIndex(v0XOffset + xOffset + 1, v0YOffset + yOffset + 1, v0ZOffset + zOffset + 1)];
					packedLight1[index] = packedLightCacheArray[packedLightCache.getIndex(v1XOffset + xOffset + 1, v1YOffset + yOffset + 1, v1ZOffset + zOffset + 1)];
					packedLight2[index] = packedLightCacheArray[packedLightCache.getIndex(v2XOffset + xOffset + 1, v2YOffset + yOffset + 1, v2ZOffset + zOffset + 1)];
					packedLight3[index] = packedLightCacheArray[packedLightCache.getIndex(v3XOffset + xOffset + 1, v3YOffset + yOffset + 1, v3ZOffset + zOffset + 1)];
				}
			}
		}

		final int skylight0 = ModUtil.max(
				packedLight0[0] >> 16 & 0xFFFF,
				packedLight0[1] >> 16 & 0xFFFF,
				packedLight0[2] >> 16 & 0xFFFF,
				packedLight0[3] >> 16 & 0xFFFF,
				packedLight0[4] >> 16 & 0xFFFF,
				packedLight0[5] >> 16 & 0xFFFF,
				packedLight0[6] >> 16 & 0xFFFF,
				packedLight0[7] >> 16 & 0xFFFF,
				packedLight0[8] >> 16 & 0xFFFF,
				packedLight0[9] >> 16 & 0xFFFF,
				packedLight0[10] >> 16 & 0xFFFF,
				packedLight0[11] >> 16 & 0xFFFF,
				packedLight0[12] >> 16 & 0xFFFF,
				packedLight0[13] >> 16 & 0xFFFF,
				packedLight0[14] >> 16 & 0xFFFF,
				packedLight0[15] >> 16 & 0xFFFF,
				packedLight0[16] >> 16 & 0xFFFF,
				packedLight0[17] >> 16 & 0xFFFF,
				packedLight0[18] >> 16 & 0xFFFF,
				packedLight0[19] >> 16 & 0xFFFF,
				packedLight0[20] >> 16 & 0xFFFF,
				packedLight0[21] >> 16 & 0xFFFF,
				packedLight0[22] >> 16 & 0xFFFF,
				packedLight0[23] >> 16 & 0xFFFF,
				packedLight0[24] >> 16 & 0xFFFF,
				packedLight0[25] >> 16 & 0xFFFF,
				packedLight0[26] >> 16 & 0xFFFF
		);

		final int skylight1 = ModUtil.max(
				packedLight1[0] >> 16 & 0xFFFF,
				packedLight1[1] >> 16 & 0xFFFF,
				packedLight1[2] >> 16 & 0xFFFF,
				packedLight1[3] >> 16 & 0xFFFF,
				packedLight1[4] >> 16 & 0xFFFF,
				packedLight1[5] >> 16 & 0xFFFF,
				packedLight1[6] >> 16 & 0xFFFF,
				packedLight1[7] >> 16 & 0xFFFF,
				packedLight1[8] >> 16 & 0xFFFF,
				packedLight1[9] >> 16 & 0xFFFF,
				packedLight1[10] >> 16 & 0xFFFF,
				packedLight1[11] >> 16 & 0xFFFF,
				packedLight1[12] >> 16 & 0xFFFF,
				packedLight1[13] >> 16 & 0xFFFF,
				packedLight1[14] >> 16 & 0xFFFF,
				packedLight1[15] >> 16 & 0xFFFF,
				packedLight1[16] >> 16 & 0xFFFF,
				packedLight1[17] >> 16 & 0xFFFF,
				packedLight1[18] >> 16 & 0xFFFF,
				packedLight1[19] >> 16 & 0xFFFF,
				packedLight1[20] >> 16 & 0xFFFF,
				packedLight1[21] >> 16 & 0xFFFF,
				packedLight1[22] >> 16 & 0xFFFF,
				packedLight1[23] >> 16 & 0xFFFF,
				packedLight1[24] >> 16 & 0xFFFF,
				packedLight1[25] >> 16 & 0xFFFF,
				packedLight1[26] >> 16 & 0xFFFF
		);

		final int skylight2 = ModUtil.max(
				packedLight2[0] >> 16 & 0xFFFF,
				packedLight2[1] >> 16 & 0xFFFF,
				packedLight2[2] >> 16 & 0xFFFF,
				packedLight2[3] >> 16 & 0xFFFF,
				packedLight2[4] >> 16 & 0xFFFF,
				packedLight2[5] >> 16 & 0xFFFF,
				packedLight2[6] >> 16 & 0xFFFF,
				packedLight2[7] >> 16 & 0xFFFF,
				packedLight2[8] >> 16 & 0xFFFF,
				packedLight2[9] >> 16 & 0xFFFF,
				packedLight2[10] >> 16 & 0xFFFF,
				packedLight2[11] >> 16 & 0xFFFF,
				packedLight2[12] >> 16 & 0xFFFF,
				packedLight2[13] >> 16 & 0xFFFF,
				packedLight2[14] >> 16 & 0xFFFF,
				packedLight2[15] >> 16 & 0xFFFF,
				packedLight2[16] >> 16 & 0xFFFF,
				packedLight2[17] >> 16 & 0xFFFF,
				packedLight2[18] >> 16 & 0xFFFF,
				packedLight2[19] >> 16 & 0xFFFF,
				packedLight2[20] >> 16 & 0xFFFF,
				packedLight2[21] >> 16 & 0xFFFF,
				packedLight2[22] >> 16 & 0xFFFF,
				packedLight2[23] >> 16 & 0xFFFF,
				packedLight2[24] >> 16 & 0xFFFF,
				packedLight2[25] >> 16 & 0xFFFF,
				packedLight2[26] >> 16 & 0xFFFF
		);

		final int skylight3 = ModUtil.max(
				packedLight3[0] >> 16 & 0xFFFF,
				packedLight3[1] >> 16 & 0xFFFF,
				packedLight3[2] >> 16 & 0xFFFF,
				packedLight3[3] >> 16 & 0xFFFF,
				packedLight3[4] >> 16 & 0xFFFF,
				packedLight3[5] >> 16 & 0xFFFF,
				packedLight3[6] >> 16 & 0xFFFF,
				packedLight3[7] >> 16 & 0xFFFF,
				packedLight3[8] >> 16 & 0xFFFF,
				packedLight3[9] >> 16 & 0xFFFF,
				packedLight3[10] >> 16 & 0xFFFF,
				packedLight3[11] >> 16 & 0xFFFF,
				packedLight3[12] >> 16 & 0xFFFF,
				packedLight3[13] >> 16 & 0xFFFF,
				packedLight3[14] >> 16 & 0xFFFF,
				packedLight3[15] >> 16 & 0xFFFF,
				packedLight3[16] >> 16 & 0xFFFF,
				packedLight3[17] >> 16 & 0xFFFF,
				packedLight3[18] >> 16 & 0xFFFF,
				packedLight3[19] >> 16 & 0xFFFF,
				packedLight3[20] >> 16 & 0xFFFF,
				packedLight3[21] >> 16 & 0xFFFF,
				packedLight3[22] >> 16 & 0xFFFF,
				packedLight3[23] >> 16 & 0xFFFF,
				packedLight3[24] >> 16 & 0xFFFF,
				packedLight3[25] >> 16 & 0xFFFF,
				packedLight3[26] >> 16 & 0xFFFF
		);

		final int blocklight0 = ModUtil.max(
				packedLight0[0] & 0xFFFF,
				packedLight0[1] & 0xFFFF,
				packedLight0[2] & 0xFFFF,
				packedLight0[3] & 0xFFFF,
				packedLight0[4] & 0xFFFF,
				packedLight0[5] & 0xFFFF,
				packedLight0[6] & 0xFFFF,
				packedLight0[7] & 0xFFFF,
				packedLight0[8] & 0xFFFF,
				packedLight0[9] & 0xFFFF,
				packedLight0[10] & 0xFFFF,
				packedLight0[11] & 0xFFFF,
				packedLight0[12] & 0xFFFF,
				packedLight0[13] & 0xFFFF,
				packedLight0[14] & 0xFFFF,
				packedLight0[15] & 0xFFFF,
				packedLight0[16] & 0xFFFF,
				packedLight0[17] & 0xFFFF,
				packedLight0[18] & 0xFFFF,
				packedLight0[19] & 0xFFFF,
				packedLight0[20] & 0xFFFF,
				packedLight0[21] & 0xFFFF,
				packedLight0[22] & 0xFFFF,
				packedLight0[23] & 0xFFFF,
				packedLight0[24] & 0xFFFF,
				packedLight0[25] & 0xFFFF,
				packedLight0[26] & 0xFFFF
		);

		final int blocklight1 = ModUtil.max(
				packedLight1[0] & 0xFFFF,
				packedLight1[1] & 0xFFFF,
				packedLight1[2] & 0xFFFF,
				packedLight1[3] & 0xFFFF,
				packedLight1[4] & 0xFFFF,
				packedLight1[5] & 0xFFFF,
				packedLight1[6] & 0xFFFF,
				packedLight1[7] & 0xFFFF,
				packedLight1[8] & 0xFFFF,
				packedLight1[9] & 0xFFFF,
				packedLight1[10] & 0xFFFF,
				packedLight1[11] & 0xFFFF,
				packedLight1[12] & 0xFFFF,
				packedLight1[13] & 0xFFFF,
				packedLight1[14] & 0xFFFF,
				packedLight1[15] & 0xFFFF,
				packedLight1[16] & 0xFFFF,
				packedLight1[17] & 0xFFFF,
				packedLight1[18] & 0xFFFF,
				packedLight1[19] & 0xFFFF,
				packedLight1[20] & 0xFFFF,
				packedLight1[21] & 0xFFFF,
				packedLight1[22] & 0xFFFF,
				packedLight1[23] & 0xFFFF,
				packedLight1[24] & 0xFFFF,
				packedLight1[25] & 0xFFFF,
				packedLight1[26] & 0xFFFF
		);

		final int blocklight2 = ModUtil.max(
				packedLight2[0] & 0xFFFF,
				packedLight2[1] & 0xFFFF,
				packedLight2[2] & 0xFFFF,
				packedLight2[3] & 0xFFFF,
				packedLight2[4] & 0xFFFF,
				packedLight2[5] & 0xFFFF,
				packedLight2[6] & 0xFFFF,
				packedLight2[7] & 0xFFFF,
				packedLight2[8] & 0xFFFF,
				packedLight2[9] & 0xFFFF,
				packedLight2[10] & 0xFFFF,
				packedLight2[11] & 0xFFFF,
				packedLight2[12] & 0xFFFF,
				packedLight2[13] & 0xFFFF,
				packedLight2[14] & 0xFFFF,
				packedLight2[15] & 0xFFFF,
				packedLight2[16] & 0xFFFF,
				packedLight2[17] & 0xFFFF,
				packedLight2[18] & 0xFFFF,
				packedLight2[19] & 0xFFFF,
				packedLight2[20] & 0xFFFF,
				packedLight2[21] & 0xFFFF,
				packedLight2[22] & 0xFFFF,
				packedLight2[23] & 0xFFFF,
				packedLight2[24] & 0xFFFF,
				packedLight2[25] & 0xFFFF,
				packedLight2[26] & 0xFFFF
		);

		final int blocklight3 = ModUtil.max(
				packedLight3[0] & 0xFFFF,
				packedLight3[1] & 0xFFFF,
				packedLight3[2] & 0xFFFF,
				packedLight3[3] & 0xFFFF,
				packedLight3[4] & 0xFFFF,
				packedLight3[5] & 0xFFFF,
				packedLight3[6] & 0xFFFF,
				packedLight3[7] & 0xFFFF,
				packedLight3[8] & 0xFFFF,
				packedLight3[9] & 0xFFFF,
				packedLight3[10] & 0xFFFF,
				packedLight3[11] & 0xFFFF,
				packedLight3[12] & 0xFFFF,
				packedLight3[13] & 0xFFFF,
				packedLight3[14] & 0xFFFF,
				packedLight3[15] & 0xFFFF,
				packedLight3[16] & 0xFFFF,
				packedLight3[17] & 0xFFFF,
				packedLight3[18] & 0xFFFF,
				packedLight3[19] & 0xFFFF,
				packedLight3[20] & 0xFFFF,
				packedLight3[21] & 0xFFFF,
				packedLight3[22] & 0xFFFF,
				packedLight3[23] & 0xFFFF,
				packedLight3[24] & 0xFFFF,
				packedLight3[25] & 0xFFFF,
				packedLight3[26] & 0xFFFF
		);

		return new LightmapInfo(
				skylight0, skylight1, skylight2, skylight3,
				blocklight0, blocklight1, blocklight2, blocklight3
		);

	}

}
