package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public interface MeshGenerator {

	void generate(Area area, Predicate<BlockState> isSmoothable, FaceAction action);

	interface FaceAction {

		/** Return false if no more faces need to be generated */
		boolean apply(BlockPos pos, Face face);

	}

}
