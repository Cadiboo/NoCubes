package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.mesh.generator.MarchingCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingTetrahedra;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.generator.SurfaceNets;

/**
 * @author Cadiboo
 */
public enum MeshGeneratorType {

	SurfaceNets(new SurfaceNets()),
	MarchingCubes(new MarchingCubes()),
	MarchingTetrahedra(new MarchingTetrahedra()),

	OldNoCubes(new OldNoCubes());

	private final MeshGenerator meshGenerator;

	MeshGeneratorType(MeshGenerator meshGenerator) {
		this.meshGenerator = meshGenerator;
	}

	public MeshGenerator getMeshGenerator() {
		return meshGenerator;
	}

}
