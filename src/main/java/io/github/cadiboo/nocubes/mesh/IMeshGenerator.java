package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec3;

import java.util.function.Consumer;

public interface IMeshGenerator {

	/**
	 * Generates a chunk WITHOUT OFFSETTING OR TRANSLATING ANY VERTICES
	 *
	 * @param faceConsumer the consumer to actually do stuff
	 * @param data the float[] data
	 * @param dims the dimensions of the mesh
	 */
	void generateChunk(final Consumer<Face<Vec3>> faceConsumer, final float[] data, final int[] dims);

}
