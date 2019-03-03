package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.mesh.generator.MarchingCubes;
import io.github.cadiboo.nocubes.mesh.generator.MarchingTetrahedra;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.generator.SurfaceNets;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3b;

import java.util.HashMap;
import java.util.Map;

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
		final HashMap<Vec3b, FaceList> chunkData = meshGenerator.generateChunk(data, dims);
		if (ModConfig.offsetVertices) {
			offsetChunkVertices(chunkData);
		}
		return chunkData;
	}

	public FaceList generateBlock(final byte[] pos, final float[] grid) {
		final FaceList chunkData = meshGenerator.generateBlock(pos, grid);
		if (ModConfig.offsetVertices) {
			offsetBlockVertices(chunkData);
		}
		return chunkData;
	}

	private static void offsetChunkVertices(Map<Vec3b, FaceList> chunkData) {
		chunkData.forEach((pos, list) -> {
			list.forEach(face -> {
				ModUtil.offsetVertex(face.getVertex0());
				ModUtil.offsetVertex(face.getVertex1());
				ModUtil.offsetVertex(face.getVertex2());
				ModUtil.offsetVertex(face.getVertex3());
			});
		});
	}

	private static void offsetBlockVertices(final FaceList faces) {
		faces.forEach(face -> {
			ModUtil.offsetVertex(face.getVertex0());
			ModUtil.offsetVertex(face.getVertex1());
			ModUtil.offsetVertex(face.getVertex2());
			ModUtil.offsetVertex(face.getVertex3());
		});
	}

}
