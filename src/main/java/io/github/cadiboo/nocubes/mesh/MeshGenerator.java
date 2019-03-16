package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingTetrahedra;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.generator.SurfaceNets;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
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

	public HashMap<Vec3b, FaceList> generateChunk(final float[] data, final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ) {
		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateChunkMesh" + this.name())) {
			return meshGenerator.generateChunk(data, new byte[]{meshSizeX, meshSizeY, meshSizeZ});
		}
	}

	public FaceList generateBlock(final float[] data, final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ) {
		try (final ModProfiler ignored = NoCubes.getProfiler().start("generateBlockMesh" + this.name())) {
			return meshGenerator.generateBlock(data, new byte[]{meshSizeX, meshSizeY, meshSizeZ});
		}
	}

	public byte getSizeXExtension() {
		return meshGenerator.getSizeXExtension();
	}

	public byte getSizeYExtension() {
		return meshGenerator.getSizeYExtension();
	}

	public byte getSizeZExtension() {
		return meshGenerator.getSizeZExtension();
	}

}
