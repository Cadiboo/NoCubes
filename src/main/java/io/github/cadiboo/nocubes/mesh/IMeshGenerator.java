package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

public interface IMeshGenerator {

	/**
	 * Generates a chunk WITHOUT OFFSETTING OR TRANSLATING ANY VERTICES
	 *
	 * @param data the float[] data
	 * @param dims the dimensions of the mesh
	 */
	@Nonnull
	Map<int[], ArrayList<Face<Vec3>>> generateChunk(final float[] data, final int[] dims);

}
