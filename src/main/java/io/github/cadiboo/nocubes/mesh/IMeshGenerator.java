package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.Vec3b;

import javax.annotation.Nonnull;
import java.util.Map;

public interface IMeshGenerator {

	/**
	 * Generates a chunk WITHOUT OFFSETTING OR TRANSLATING ANY VERTICES
	 *
	 * @param data the float[] data
	 * @param dims the dimensions of the mesh
	 */
	@Nonnull
	Map<Vec3b, FaceList> generateChunk(final float[] data, final byte[] dims);

}
