package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public interface MeshGenerator {

	/**
	 * Generates a chunk WITHOUT OFFSETTING OR TRANSLATING ANY VERTICES
	 *
	 * @param scalarFieldData the float[] data
	 * @param dimensions      the dimensions of the mesh
	 * @return the chunk vertices
	 */
	@Nonnull
	HashMap<Vec3b, FaceList> generateChunk(@Nonnull final float[] scalarFieldData, @Nonnull final byte[] dimensions);

	/**
	 * @param position             the byte[] position relative to the chunk pos
	 * @param neighbourDensityGrid the neighbour density grid
	 * @return the block vertices
	 */
	@Nonnull
	FaceList generateBlock(@Nonnull final float[] scalarFieldData, @Nonnull final byte[] dimensions);

	default byte getSizeXExtension() {
		return 0;
	}

	default byte getSizeYExtension() {
		return 0;
	}

	default byte getSizeZExtension() {
		return 0;
	}

	@Nonnull
	FaceList generateBlock(@Nonnull final BlockPos pos, @Nonnull final IBlockReader cache, @Nonnull final IsSmoothable isSmoothable);

	@Nonnull
	default float[] generateScalarFieldData(
			final int startX, final int startY, final int startZ,
			final int endX, final int endY, final int endZ,
			@Nonnull final IBlockReader blockAccess, @Nonnull final IsSmoothable isSmoothable, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos
	) {

		final int maxX = endX - startX;
		final int maxY = endY - startY;
		final int maxZ = endZ - startZ;

		final float[] scalarFieldData = new float[maxX * maxY * maxZ];

		int index = 0;
		float density;
		for (int z = 0; z < maxZ; ++z) {
			for (int y = 0; y < maxY; ++y) {
				for (int x = 0; x < maxX; ++x, ++index) {
					density = 0;
					for (int zOffset = 0; zOffset < 2; ++zOffset) {
						for (int yOffset = 0; yOffset < 2; ++yOffset) {
							for (int xOffset = 0; xOffset < 2; ++xOffset) {

								pooledMutableBlockPos.setPos(
										startX + x - xOffset,
										startY + y - yOffset,
										startZ + z - zOffset
								);

								final IBlockState state = blockAccess.getBlockState(pooledMutableBlockPos);
								density += ModUtil.getIndividualBlockDensity(isSmoothable.apply(state), state);
							}
						}
					}
					scalarFieldData[index] = density;
				}
			}
		}
		return scalarFieldData;
	}

}
