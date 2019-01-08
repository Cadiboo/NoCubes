package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.ChunkInfo;
import io.github.cadiboo.nocubes.mesh.generator.MarchingCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingTetrahedra;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.generator.SurfaceNets;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec3;
import org.apache.logging.log4j.util.TriConsumer;

public enum MeshGenerator {

	OldNoCubes(new OldNoCubes()),
	SurfaceNets(new SurfaceNets()),
	MarchingCubes(new MarchingCubes()),
	MarchingTetrahedra(new MarchingTetrahedra()),

	;

	private final IMeshGenerator meshGenerator;

	MeshGenerator(IMeshGenerator meshGenerator) {
		this.meshGenerator = meshGenerator;
	}

	public void generateChunk(final ChunkInfo chunkInfo, final TriConsumer<int[], ChunkInfo, Face<Vec3>> faceConsumer, final float[] data, final int[] dims) {
		meshGenerator.generateChunk(chunkInfo, faceConsumer, data, dims);
	}

}
