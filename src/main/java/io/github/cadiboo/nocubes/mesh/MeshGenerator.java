package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

/**
 * @author Cadiboo
 */
public interface MeshGenerator {

	interface FaceAction {

		/**
		 * @return If the traversal should continue
		 */
		boolean apply(Face face, MutableBlockPos posInWorldSpace);

	}

	interface VoxelAction {

		/**
		 * @return If the traversal should continue
		 */
		boolean apply(MutableBlockPos posInWorldSpace, int mask);

	}

}

