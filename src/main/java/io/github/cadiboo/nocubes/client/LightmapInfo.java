package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.Vec3.PooledVec3;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import static java.lang.Math.floor;
import static java.lang.Math.round;

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
			final PooledVec3 v0,
			final PooledVec3 v1,
			final PooledVec3 v2,
			final PooledVec3 v3,
			final int renderChunkPositionX,
			final int renderChunkPositionY,
			final int renderChunkPositionZ,
			final int[] pos,
			final IBlockAccess blockAccess, final PooledMutableBlockPos pooledMutableBlockPos
	) {

		final int packedLight0m10m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v0.x) - 1, floor(v0.y), floor(v0.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight0m100 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v0.x) - 1, floor(v0.y), floor(v0.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight0m11m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v0.x) - 1, floor(v0.y) + 1, floor(v0.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight0m110 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v0.x) - 1, floor(v0.y) + 1, floor(v0.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight000m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v0.x), floor(v0.y), floor(v0.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight0000 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v0.x), floor(v0.y), floor(v0.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight001m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v0.x), floor(v0.y) + 1, floor(v0.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight0010 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v0.x), floor(v0.y) + 1, floor(v0.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);

		final int packedLight1m10m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v1.x) - 1, floor(v1.y), floor(v1.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight1m100 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v1.x) - 1, floor(v1.y), floor(v1.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight1m11m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v1.x) - 1, floor(v1.y) + 1, floor(v1.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight1m110 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v1.x) - 1, floor(v1.y) + 1, floor(v1.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight100m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v1.x), floor(v1.y), floor(v1.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight1000 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v1.x), floor(v1.y), floor(v1.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight101m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v1.x), floor(v1.y) + 1, floor(v1.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight1010 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v1.x), floor(v1.y) + 1, floor(v1.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);

		final int packedLight2m10m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v2.x) - 1, floor(v2.y), floor(v2.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight2m100 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v2.x) - 1, floor(v2.y), floor(v2.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight2m11m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v2.x) - 1, floor(v2.y) + 1, floor(v2.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight2m110 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v2.x) - 1, floor(v2.y) + 1, floor(v2.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight200m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v2.x), floor(v2.y), floor(v2.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight2000 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v2.x), floor(v2.y), floor(v2.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight201m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v2.x), floor(v2.y) + 1, floor(v2.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight2010 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v2.x), floor(v2.y) + 1, floor(v2.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);

		final int packedLight3m10m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v3.x) - 1, floor(v3.y), floor(v3.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight3m100 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v3.x) - 1, floor(v3.y), floor(v3.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight3m11m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v3.x) - 1, floor(v3.y) + 1, floor(v3.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight3m110 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v3.x) - 1, floor(v3.y) + 1, floor(v3.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight300m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v3.x), floor(v3.y), floor(v3.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight3000 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v3.x), floor(v3.y), floor(v3.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight301m1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v3.x), floor(v3.y) + 1, floor(v3.z) - 1)).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight3010 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(floor(v3.x), floor(v3.y) + 1, floor(v3.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);

		final int skylight0 = max(
				packedLight0m10m1 >> 16 & 0xFFFF,
				packedLight0m100 >> 16 & 0xFFFF,
				packedLight0m11m1 >> 16 & 0xFFFF,
				packedLight0m110 >> 16 & 0xFFFF,
				packedLight000m1 >> 16 & 0xFFFF,
				packedLight0000 >> 16 & 0xFFFF,
				packedLight001m1 >> 16 & 0xFFFF,
				packedLight0010 >> 16 & 0xFFFF
		);

		final int skylight1 = max(
				packedLight1m10m1 >> 16 & 0xFFFF,
				packedLight1m100 >> 16 & 0xFFFF,
				packedLight1m11m1 >> 16 & 0xFFFF,
				packedLight1m110 >> 16 & 0xFFFF,
				packedLight100m1 >> 16 & 0xFFFF,
				packedLight1000 >> 16 & 0xFFFF,
				packedLight101m1 >> 16 & 0xFFFF,
				packedLight1010 >> 16 & 0xFFFF
		);

		final int skylight2 = max(
				packedLight2m10m1 >> 16 & 0xFFFF,
				packedLight2m100 >> 16 & 0xFFFF,
				packedLight2m11m1 >> 16 & 0xFFFF,
				packedLight2m110 >> 16 & 0xFFFF,
				packedLight200m1 >> 16 & 0xFFFF,
				packedLight2000 >> 16 & 0xFFFF,
				packedLight201m1 >> 16 & 0xFFFF,
				packedLight2010 >> 16 & 0xFFFF
		);

		final int skylight3 = max(
				packedLight3m10m1 >> 16 & 0xFFFF,
				packedLight3m100 >> 16 & 0xFFFF,
				packedLight3m11m1 >> 16 & 0xFFFF,
				packedLight3m110 >> 16 & 0xFFFF,
				packedLight300m1 >> 16 & 0xFFFF,
				packedLight3000 >> 16 & 0xFFFF,
				packedLight301m1 >> 16 & 0xFFFF,
				packedLight3010 >> 16 & 0xFFFF
		);

		final int blocklight0 = max(
				packedLight0m10m1 & 0xFFFF,
				packedLight0m100 & 0xFFFF,
				packedLight0m11m1 & 0xFFFF,
				packedLight0m110 & 0xFFFF,
				packedLight000m1 & 0xFFFF,
				packedLight0000 & 0xFFFF,
				packedLight001m1 & 0xFFFF,
				packedLight0010 & 0xFFFF
		);

		final int blocklight1 = max(
				packedLight1m10m1 & 0xFFFF,
				packedLight1m100 & 0xFFFF,
				packedLight1m11m1 & 0xFFFF,
				packedLight1m110 & 0xFFFF,
				packedLight100m1 & 0xFFFF,
				packedLight1000 & 0xFFFF,
				packedLight101m1 & 0xFFFF,
				packedLight1010 & 0xFFFF
		);

		final int blocklight2 = max(
				packedLight2m10m1 & 0xFFFF,
				packedLight2m100 & 0xFFFF,
				packedLight2m11m1 & 0xFFFF,
				packedLight2m110 & 0xFFFF,
				packedLight200m1 & 0xFFFF,
				packedLight2000 & 0xFFFF,
				packedLight201m1 & 0xFFFF,
				packedLight2010 & 0xFFFF
		);

		final int blocklight3 = max(
				packedLight3m10m1 & 0xFFFF,
				packedLight3m100 & 0xFFFF,
				packedLight3m11m1 & 0xFFFF,
				packedLight3m110 & 0xFFFF,
				packedLight300m1 & 0xFFFF,
				packedLight3000 & 0xFFFF,
				packedLight301m1 & 0xFFFF,
				packedLight3010 & 0xFFFF
		);

		return new LightmapInfo(
				skylight0, skylight1, skylight2, skylight3,
				blocklight0, blocklight1, blocklight2, blocklight3
		);

	}

	public static final int max(int... ints) {
		int max = 0;
		for (final int anInt : ints) {
			if (max < anInt) max = anInt;
		}
		return max;
	}

}
