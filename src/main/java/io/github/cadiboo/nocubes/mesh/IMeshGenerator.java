package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.ChunkInfo;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

public interface IMeshGenerator {

	/**
	 * Generates a chunk WITHOUT OFFSETTING OR TRANSLATING ANY VERTICES
	 *
	 * @param chunkInfo    the chunk info for use by the faceConsumer
	 * @param faceConsumer the consumer to actually do stuff
	 * @param data         the float[] data
	 * @param dims         the dimensions of the mesh
	 */
	void generateChunk(final ChunkInfo chunkInfo, final TriConsumer<int[], ChunkInfo, Face<Vec3>> faceConsumer, final float[] data, final int[] dims);

}
