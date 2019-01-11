package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MarchingTetrahedra implements IMeshGenerator {

	private final int[][] CUBE_VERTICES = {
			{0, 0, 0},
			{1, 0, 0},
			{1, 1, 0},
			{0, 1, 0},
			{0, 0, 1},
			{1, 0, 1},
			{1, 1, 1},
			{0, 1, 1}
	};

	private final int[][] TETRA_LIST = {
			{0, 2, 3, 7},
			{0, 6, 2, 7},
			{0, 4, 6, 7},
			{0, 6, 1, 2},
			{0, 1, 6, 4},
			{5, 6, 1, 4}
	};

	@Override
	@Nonnull
	public Map<int[], ArrayList<Face<Vec3>>> generateChunk(final float[] data, final int[] dims) {

		final int[][] cube_vertices = CUBE_VERTICES;
		final int[][] tetra_list = TETRA_LIST;

		final int[] x = {0, 0, 0};
		int n = 0;
		final float[] grid = new float[8];
		final HashMap<int[], ArrayList<Face<Vec3>>> posToFaces = new HashMap<>();
		final ArrayList<Face<Vec3>> faces = new ArrayList<>();

		//March over the volume
		for (x[2] = 0; x[2] < dims[2] - 1; ++x[2], n += dims[0])
			for (x[1] = 0; x[1] < dims[1] - 1; ++x[1], ++n)
				for (x[0] = 0; x[0] < dims[0] - 1; ++x[0], ++n) {
					//Read in cube
					for (int i = 0; i < 8; ++i) {
						grid[i] = data[n + cube_vertices[i][0] + dims[0] * (cube_vertices[i][1] + dims[1] * cube_vertices[i][2])];
					}
					for (int i = 0; i < tetra_list.length; ++i) {
						int[] T = tetra_list[i];
						int triindex = 0;
						if (grid[T[0]] < 0) triindex |= 1;
						if (grid[T[1]] < 0) triindex |= 2;
						if (grid[T[2]] < 0) triindex |= 4;
						if (grid[T[3]] < 0) triindex |= 8;

						//Handle each case
						switch (triindex) {
							case 0x00:
							case 0x0F:
								break;
							case 0x0E:
								faces.add(new Face<>(
										interp(T[0], T[1], grid, x)
										, interp(T[0], T[3], grid, x)
										, interp(T[0], T[2], grid, x)));
								break;
							case 0x01:
								faces.add(new Face<>(
										interp(T[0], T[1], grid, x)
										, interp(T[0], T[2], grid, x)
										, interp(T[0], T[3], grid, x)));
								break;
							case 0x0D:
								faces.add(new Face<>(
										interp(T[1], T[0], grid, x)
										, interp(T[1], T[2], grid, x)
										, interp(T[1], T[3], grid, x)));
								break;
							case 0x02:
								faces.add(new Face<>(
										interp(T[1], T[0], grid, x)
										, interp(T[1], T[3], grid, x)
										, interp(T[1], T[2], grid, x)));
								break;
							case 0x0C:
								faces.add(new Face<>(
										interp(T[1], T[2], grid, x)
										, interp(T[1], T[3], grid, x)
										, interp(T[0], T[3], grid, x)
										, interp(T[0], T[2], grid, x)));
								break;
							case 0x03:
								faces.add(new Face<>(
										interp(T[1], T[2], grid, x)
										, interp(T[0], T[2], grid, x)
										, interp(T[0], T[3], grid, x)
										, interp(T[1], T[3], grid, x)));
								break;
							case 0x04:
								faces.add(new Face<>(
										interp(T[2], T[0], grid, x)
										, interp(T[2], T[1], grid, x)
										, interp(T[2], T[3], grid, x)));
								break;
							case 0x0B:
								faces.add(new Face<>(
										interp(T[2], T[0], grid, x)
										, interp(T[2], T[3], grid, x)
										, interp(T[2], T[1], grid, x)));
								break;
							case 0x05:
								faces.add(new Face<>(
										interp(T[0], T[1], grid, x)
										, interp(T[1], T[2], grid, x)
										, interp(T[2], T[3], grid, x)
										, interp(T[0], T[3], grid, x)));
								break;
							case 0x0A:
								faces.add(new Face<>(
										interp(T[0], T[1], grid, x)
										, interp(T[0], T[3], grid, x)
										, interp(T[2], T[3], grid, x)
										, interp(T[1], T[2], grid, x)));
								break;
							case 0x06:
								faces.add(new Face<>(
										interp(T[2], T[3], grid, x)
										, interp(T[0], T[2], grid, x)
										, interp(T[0], T[1], grid, x)
										, interp(T[1], T[3], grid, x)));
								break;
							case 0x09:
								faces.add(new Face<>(
										interp(T[2], T[3], grid, x)
										, interp(T[1], T[3], grid, x)
										, interp(T[0], T[1], grid, x)
										, interp(T[0], T[2], grid, x)));
								break;
							case 0x07:
								faces.add(new Face<>(
										interp(T[3], T[0], grid, x)
										, interp(T[3], T[1], grid, x)
										, interp(T[3], T[2], grid, x)));
								break;
							case 0x08:
								faces.add(new Face<>(
										interp(T[3], T[0], grid, x)
										, interp(T[3], T[2], grid, x)
										, interp(T[3], T[1], grid, x)));
								break;
						}
					}

					final int[] tempPos = {0, 0, 0};
					System.arraycopy(x, 0, tempPos, 0, 3);
					posToFaces.put(tempPos, new ArrayList<>(faces));
					faces.clear();
				}

		return posToFaces;
	}

	private Vec3 interp(final int i0, final int i1, float[] grid, int[] x) {
		final float g0 = grid[i0];
		final float g1 = grid[i1];
		int[] p0 = CUBE_VERTICES[i0];
		final int[] p1 = CUBE_VERTICES[i1];
		final Vec3 v = new Vec3(x[0], x[1], x[2]);
		float t = g0 - g1;
		if (Math.abs(t) > 1e-6) {
			t = g0 / t;
		}
		v.x += p0[0] + t * (p1[0] - p0[0]);
		v.y += p0[1] + t * (p1[1] - p0[1]);
		v.z += p0[2] + t * (p1[2] - p0[2]);
		return v;
	}

}
