package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.mesh.generator.MarchingCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingTetrahedra;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.generator.SurfaceNets;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec3;

import java.util.ArrayList;
import java.util.Map;

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

	public Map<int[], ArrayList<Face<Vec3>>> generateChunk(final float[] data, final int[] dims) {
		return meshGenerator.generateChunk(data, dims);
	}

}
