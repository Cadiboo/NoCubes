package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import java.util.function.Predicate;

public interface MeshGenerator {

	VoxelAction DEFAULT_VOXEL_ACTION = (pos, amount) -> true;

	default void generate(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		generate(area, isSmoothable, DEFAULT_VOXEL_ACTION, action);
	}

	void generate(Area area, Predicate<BlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction);

	Vector3i getPositiveAreaExtension();

	Vector3i getNegativeAreaExtension();

	interface FaceAction {

		/**
		 * @param relativePos  The position of the face, positioned relatively to the start of the area
		 * @param face The face, positioned relatively to the start of the area
		 * @return false if no more faces need to be generated
		 */
		boolean apply(BlockPos.Mutable relativePos, Face face);

	}

	/* protected */ default boolean isOutsideMesh(int x, int y, int z, BlockPos size) {
		Vector3i negativeExtension = getNegativeAreaExtension();
		Vector3i positiveExtension = getPositiveAreaExtension();
		// Block is outside where we are generating it for, we only query it for its neighbouring faces
		return x >= size.getX() - positiveExtension.getX() || x < negativeExtension.getX() ||
			y >= size.getY() - positiveExtension.getY() || y < negativeExtension.getY() ||
			z >= size.getZ() - positiveExtension.getZ() || z < negativeExtension.getZ();
	}

	interface VoxelAction {

		/**
		 * @param relativePos                    The position of the voxel, positioned relatively to the start of the area
		 * @param amountInsideIsosurface The amount of the voxel that is inside the isosurface (range 0-1)
		 * @return false if no more voxels need to iterated over
		 */
		boolean apply(BlockPos.Mutable relativePos, float amountInsideIsosurface);

	}

}
