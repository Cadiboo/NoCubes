package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.math.Vec3b;

/**
 * @author Cadiboo
 */
public interface IMeshGenerator {

	HashMap<Vec3b, FaceList> generateChunk(final float[] data, final byte[] dims);

}
