package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.Vec3b;

import javax.annotation.Nonnull;
import java.util.HashMap;

public interface IMeshGenerator {

	/**
	 * Generates a chunk WITHOUT OFFSETTING OR TRANSLATING ANY VERTICES
	 *
	 * @param scalarFieldData the float[] data
	 * @param dimensions      the dimensions of the mesh
	 * @return the chunk vertices
	 */
	@Nonnull
	HashMap<Vec3b, FaceList> generateChunk(final float[] scalarFieldData, final byte[] dimensions);


}
