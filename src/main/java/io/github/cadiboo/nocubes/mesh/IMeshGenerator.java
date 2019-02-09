package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Face;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

public interface IMeshGenerator {

	/**
	 * Generates a chunk WITHOUT OFFSETTING OR TRANSLATING ANY VERTICES
	 *
	 * @param data the float[] data
	 * @param dims the dimensions of the mesh
	 */
	@Nonnull
	Map<int[], ArrayList<Face>> generateChunk(final float[] data, final int[] dims);

//	final int[][] cube_vertices = CUBE_VERTICES;
//	final int[][] tetra_list = TETRA_LIST;
//
//	final int[] x = {0, 0, 0};
//	int n = 0;
//	final float[] grid = new float[8];
//	final HashMap<int[], ArrayList<Face<Vec3>>> posToFaces = new HashMap<>();
//	final ArrayList<Face<Vec3>> faces = new ArrayList<>();
//
//	final int[] tempPos = {0, 0, 0};
//	System.arraycopy(x, 0, tempPos, 0, 3);
//	posToFaces.put(tempPos, new ArrayList<>(faces));
//	faces.clear();
//
//	return posToFaces;

}
