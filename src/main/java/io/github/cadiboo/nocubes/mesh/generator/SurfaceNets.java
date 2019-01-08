package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.util.ChunkInfo;
import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

public class SurfaceNets implements IMeshGenerator {

	@Override
	public void generateChunk(final ChunkInfo chunkInfo, final TriConsumer<int[], ChunkInfo, Face<Vec3>> faceConsumer, final float[] data, final int[] dims) {

	}

}
