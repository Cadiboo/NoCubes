package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.util.math.BlockPos;

public interface MeshGenerator {

	void generate(Area area, FaceAction action);

	interface FaceAction {

		/** Return false if no more faces need to be generated */
		boolean apply(BlockPos pos, Face face);

	}

}
