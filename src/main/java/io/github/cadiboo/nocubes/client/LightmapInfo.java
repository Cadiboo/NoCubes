package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.Vec3.PooledVec3;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

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

		final int packedLight0 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(round(v0.x), round(v0.y), round(v0.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight1 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(round(v1.x), round(v1.y), round(v1.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight2 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(round(v2.x), round(v2.y), round(v2.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);
		final int packedLight3 = blockAccess.getBlockState(pooledMutableBlockPos.setPos(round(v3.x), round(v3.y), round(v3.z))).getPackedLightmapCoords(blockAccess, pooledMutableBlockPos);

		return new LightmapInfo(
				packedLight0 >> 16 & 0xFFFF,
				packedLight1 >> 16 & 0xFFFF,
				packedLight2 >> 16 & 0xFFFF,
				packedLight3 >> 16 & 0xFFFF,
				packedLight0 & 0xFFFF,
				packedLight1 & 0xFFFF,
				packedLight2 & 0xFFFF,
				packedLight3 & 0xFFFF
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
