package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;

import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public interface MeshGenerator {
	VoxelAction DEFAULT_VOXEL_ACTION = (pos, amount) -> true;

	default void generate(Area area, Predicate<IBlockState> isSmoothable, FaceAction action) {
		generate(area, isSmoothable, DEFAULT_VOXEL_ACTION, action);
	}

	void generate(Area area, Predicate<IBlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction);

	Vec3i getPositiveAreaExtension();

	Vec3i getNegativeAreaExtension();

	interface FaceAction {

		/**
		 * Return false if no more faces need to be generated
		 *
		 * @param pos  The position of the face, positioned relatively to the start of the area
		 * @param face The face, positioned relatively to the start of the area
		 */
		boolean apply(MutableBlockPos pos, Face face);

	}

	interface VoxelAction {

		/**
		 * Return false if no more voxels need to iterated over
		 *
		 * @param pos                    The position of the voxel, positioned relatively to the start of the area
		 * @param amountInsideIsosurface The amount of the voxel that is inside the isosurface (range 0-1)
		 */
		boolean apply(MutableBlockPos pos, float amountInsideIsosurface);
	}

	static float[] generateCornerDistanceField(Area area, Predicate<IBlockState> isSmoothable) {
		IBlockAccess world = area.world;
		int startX = area.start.getX();
		int startY = area.start.getY();
		int startZ = area.start.getZ();
		int maxX = area.end.getX() - startX + 1;
		int maxY = area.end.getY() - startY + 1;
		int maxZ = area.end.getZ() - startZ + 1;
		MutableBlockPos pos = new MutableBlockPos();

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
								pos.setPos(
									startX + x - xOffset,
									startY + y - yOffset,
									startZ + z - zOffset
								);
								final IBlockState state = world.getBlockState(pos);
								density += ModUtil.getIndividualBlockDensity(isSmoothable.test(state), state);
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
