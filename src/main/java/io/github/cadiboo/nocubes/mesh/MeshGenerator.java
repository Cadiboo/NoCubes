package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import java.util.function.Predicate;

public interface MeshGenerator {

	void generate(Area area, Predicate<BlockState> isSmoothable, FaceAction action);

    Vector3i getPositiveAreaExtension();

    Vector3i getNegativeAreaExtension();

    interface FaceAction {

		/**
		 * Return false if no more faces need to be generated
		 *
		 * @param pos  The position of the face, positioned relatively to the start of the area
		 * @param face The face, positioned relatively to the start of the area
		 */
		boolean apply(BlockPos.Mutable pos, Face face);

	}

}
