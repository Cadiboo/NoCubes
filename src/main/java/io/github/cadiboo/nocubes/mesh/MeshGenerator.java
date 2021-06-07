package io.github.cadiboo.nocubes.mesh;

import com.mojang.blaze3d.matrix.MatrixStack;
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
		 * @param relativePos The position of the face, positioned relatively to the start of the area
		 * @param face        The face, positioned relatively to the start of the area
		 * @return false if no more faces need to be generated
		 */
		boolean apply(BlockPos.Mutable relativePos, Face face);

	}

	interface VoxelAction {

		/**
		 * @param relativePos            The position of the voxel, positioned relatively to the start of the area
		 * @param amountInsideIsosurface The amount of the voxel that is inside the isosurface (range 0-1)
		 * @return false if no more voxels need to iterated over
		 */
		boolean apply(BlockPos.Mutable relativePos, float amountInsideIsosurface);

	}

	/* protected */
	default boolean isOutsideMesh(int x, int y, int z, BlockPos size) {
		Vector3i negativeExtension = getNegativeAreaExtension();
		Vector3i positiveExtension = getPositiveAreaExtension();
		// Block is outside where we are generating it for, we only query it for its neighbouring faces
		return x >= size.getX() - positiveExtension.getX() || x < negativeExtension.getX() ||
			y >= size.getY() - positiveExtension.getY() || y < negativeExtension.getY() ||
			z >= size.getZ() - positiveExtension.getZ() || z < negativeExtension.getZ();
	}

	/**
	 * The vertices in meshes are generated relative to {@link Area#start}.
	 * {@link Area#start} is not necessarily the place where the final mesh should be rendered.
	 * The difference between the start of the area and the position we are generating for
	 * This exists because:
	 * To render a 16x16x16 area you need the data of a 18x18x18 area (+1 voxel on each axis)
	 * So the area is going to start at chunkPos - 1 (and extend 18 blocks)
	 * And the vertices are going to be relative to the start of the area
	 * We need to add an offset to the vertices because we want them to be relative to the start of the chunk, not the area
	 */
	static void translateToMeshStart(MatrixStack matrix, BlockPos areaStart, BlockPos renderStartPos) {
		matrix.translate(
			validateMeshOffset(areaStart.getX() - renderStartPos.getX()),
			validateMeshOffset(areaStart.getY() - renderStartPos.getY()),
			validateMeshOffset(areaStart.getZ() - renderStartPos.getZ())
		);
	}

	/* private */
	static int validateMeshOffset(int meshOffset) {
		assert meshOffset <= 0 : "Mesh generators won't require a smaller area than they are generating a mesh for";
		assert meshOffset > -2 : "Mesh generators won't require more than 2 extra blocks on each axis";
		return meshOffset;
	}


}
