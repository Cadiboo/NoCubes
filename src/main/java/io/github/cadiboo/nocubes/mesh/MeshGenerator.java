package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.mesh.generator.MarchingCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingTetrahedra;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.generator.SurfaceNets;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.PooledFace;

import java.util.ArrayList;
import java.util.Map;

public enum MeshGenerator {

//	OldNoCubes(new OldNoCubes()),
	SurfaceNets(new SurfaceNets()),
	MarchingCubes(new MarchingCubes()),
	MarchingTetrahedra(new MarchingTetrahedra()),

	;

	private final IMeshGenerator meshGenerator;

	MeshGenerator(IMeshGenerator meshGenerator) {
		this.meshGenerator = meshGenerator;
	}

	public Map<int[], ArrayList<PooledFace>> generateChunk(final float[] data, final int[] dims) {
		final Map<int[], ArrayList<PooledFace>> chunkData = meshGenerator.generateChunk(data, dims);
		if(ModConfig.offsetVertices) {
			offsetChunkVertices(chunkData);
		}
		return chunkData;
	}

	private static void offsetChunkVertices(Map<int[], ArrayList<PooledFace>> chunkData) {
		chunkData.forEach((pos, list) -> {
			list.forEach(face -> {
				ModUtil.offsetVertex(face.getVertex0());
				ModUtil.offsetVertex(face.getVertex1());
				ModUtil.offsetVertex(face.getVertex2());
				ModUtil.offsetVertex(face.getVertex3());
			});
		});
	}

}
