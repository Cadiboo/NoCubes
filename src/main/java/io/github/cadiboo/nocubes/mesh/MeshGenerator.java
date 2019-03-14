package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingTetrahedra;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.generator.SurfaceNets;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;

import java.util.HashMap;

/**
 * @author Cadiboo
 */
public enum MeshGenerator {

	SurfaceNets(new SurfaceNets()),
	MarchingCubes(new MarchingCubes()),
	MarchingTetrahedra(new MarchingTetrahedra()),

	OldNoCubes(new OldNoCubes());

	private final IMeshGenerator meshGenerator;

	MeshGenerator(IMeshGenerator meshGenerator) {
		this.meshGenerator = meshGenerator;
	}

	public HashMap<Vec3b, FaceList> generateChunk(final float[] data, final byte[] dims) {
		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateChunkMesh" + this.name())) {
			return meshGenerator.generateChunk(data, dims);
		}
	}

	public FaceList generateBlock(final byte[] pos, final float[] grid) {
		return meshGenerator.generateBlock(pos, grid);
	}

}
