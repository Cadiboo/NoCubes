package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.client.render.struct.PoseStack;
import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.function.Predicate;

public interface Mesher {

	default void generateGeometry(Area area, Predicate<IBlockState> isSmoothable, FaceAction action) {
		try {
			generateGeometryInternal(area, isSmoothable, action);
		} catch (Throwable t) {
//			Util.pauseInIde(t);
			throw t;
		}
	}

	default void generateCollisions(Area area, Predicate<IBlockState> isSmoothable, ShapeConsumer action) {
		try {
			generateCollisionsInternal(area, isSmoothable, action);
		} catch (Throwable t) {
//			Util.pauseInIde(t);
			throw t;
		}
	}

	void generateGeometryInternal(Area area, Predicate<IBlockState> isSmoothable, FaceAction action);

	void generateCollisionsInternal(Area area, Predicate<IBlockState> isSmoothable, ShapeConsumer action);

	Vec3i getPositiveAreaExtension();

	Vec3i getNegativeAreaExtension();

	interface FaceAction {

		/**
		 * @param relativePos The position of the face, positioned relatively to the start of the area
		 * @param face        The face, positioned relatively to the start of the area
		 * @return false if no more faces need to be generated
		 */
		boolean apply(MutableBlockPos relativePos, Face face);

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
	static void translateToMeshStart(PoseStack matrix, BlockPos areaStart, BlockPos renderStartPos) {
		matrix.translate(
			getMeshOffset(areaStart.getX(), renderStartPos.getX()),
			getMeshOffset(areaStart.getY(), renderStartPos.getY()),
			getMeshOffset(areaStart.getZ(), renderStartPos.getZ())
		);
	}

	static int getMeshOffset(int areaStart, int desiredStart) {
		return validateMeshOffset(areaStart - desiredStart);
	}

	/* private */
	static int validateMeshOffset(int meshOffset) {
		assert meshOffset <= 0 : "Meshers won't require a smaller area than they are generating a mesh for";
		assert meshOffset > -3 : "Meshers won't require more than 2 extra blocks on each axis";
		return meshOffset;
	}


}
