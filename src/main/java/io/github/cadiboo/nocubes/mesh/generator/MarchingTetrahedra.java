package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3b;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public class MarchingTetrahedra implements IMeshGenerator {

	private final byte[][] CUBE_VERTICES = {
			{0, 0, 0},
			{1, 0, 0},
			{1, 1, 0},
			{0, 1, 0},
			{0, 0, 1},
			{1, 0, 1},
			{1, 1, 1},
			{0, 1, 1}
	};

	private final byte[][] TETRA_LIST = {
			{0, 2, 3, 7},
			{0, 6, 2, 7},
			{0, 4, 6, 7},
			{0, 6, 1, 2},
			{0, 1, 6, 4},
			{5, 6, 1, 4}
	};

	@Override
	@Nonnull
	public HashMap<Vec3b, FaceList> generateChunk(final float[] data, final byte[] dims) {

		final byte[][] cube_vertices = CUBE_VERTICES;
		final byte[][] tetra_list = TETRA_LIST;

		final byte[] x = {0, 0, 0};
		short n = 0;
		final float[] grid = new float[8];
		final HashMap<Vec3b, FaceList> posToFaces = new HashMap<>();

		//March over the volume
		for (x[2] = 0; x[2] < dims[2] - 1; ++x[2], n += dims[0])
			for (x[1] = 0; x[1] < dims[1] - 1; ++x[1], ++n)
				for (x[0] = 0; x[0] < dims[0] - 1; ++x[0], ++n) {
					//Read in cube
					for (byte i = 0; i < 8; ++i) {
						grid[i] = data[n + cube_vertices[i][0] + dims[0] * (cube_vertices[i][1] + dims[1] * cube_vertices[i][2])];
					}
					final FaceList faces = FaceList.retain();
					for (byte i = 0; i < tetra_list.length; ++i) {
						byte[] T = tetra_list[i];
						byte triindex = 0;
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
								faces.add(
										Face.retain(
												interp(T[0], T[1], grid, x),
												interp(T[0], T[3], grid, x),
												interp(T[0], T[2], grid, x)
										)
								);
								break;
							case 0x01:
								faces.add(
										Face.retain(
												interp(T[0], T[1], grid, x),
												interp(T[0], T[2], grid, x),
												interp(T[0], T[3], grid, x)
										)
								);
								break;
							case 0x0D:
								faces.add(
										Face.retain(
												interp(T[1], T[0], grid, x),
												interp(T[1], T[2], grid, x),
												interp(T[1], T[3], grid, x)
										)
								);
								break;
							case 0x02:
								faces.add(
										Face.retain(
												interp(T[1], T[0], grid, x),
												interp(T[1], T[3], grid, x),
												interp(T[1], T[2], grid, x)
										)
								);
								break;
							case 0x0C:
								faces.add(
										Face.retain(
												interp(T[1], T[2], grid, x),
												interp(T[1], T[3], grid, x),
												interp(T[0], T[3], grid, x),
												interp(T[0], T[2], grid, x)
										)
								);
								break;
							case 0x03:
								faces.add(
										Face.retain(
												interp(T[1], T[2], grid, x),
												interp(T[0], T[2], grid, x),
												interp(T[0], T[3], grid, x),
												interp(T[1], T[3], grid, x)
										)
								);
								break;
							case 0x04:
								faces.add(
										Face.retain(
												interp(T[2], T[0], grid, x),
												interp(T[2], T[1], grid, x),
												interp(T[2], T[3], grid, x)
										)
								);
								break;
							case 0x0B:
								faces.add(
										Face.retain(
												interp(T[2], T[0], grid, x),
												interp(T[2], T[3], grid, x),
												interp(T[2], T[1], grid, x)
										)
								);
								break;
							case 0x05:
								faces.add(
										Face.retain(
												interp(T[0], T[1], grid, x),
												interp(T[1], T[2], grid, x),
												interp(T[2], T[3], grid, x),
												interp(T[0], T[3], grid, x)
										)
								);
								break;
							case 0x0A:
								faces.add(
										Face.retain(
												interp(T[0], T[1], grid, x),
												interp(T[0], T[3], grid, x),
												interp(T[2], T[3], grid, x),
												interp(T[1], T[2], grid, x)
										)
								);
								break;
							case 0x06:
								faces.add(
										Face.retain(
												interp(T[2], T[3], grid, x),
												interp(T[0], T[2], grid, x),
												interp(T[0], T[1], grid, x),
												interp(T[1], T[3], grid, x)
										)
								);
								break;
							case 0x09:
								faces.add(
										Face.retain(
												interp(T[2], T[3], grid, x),
												interp(T[1], T[3], grid, x),
												interp(T[0], T[1], grid, x),
												interp(T[0], T[2], grid, x)
										)
								);
								break;
							case 0x07:
								faces.add(
										Face.retain(
												interp(T[3], T[0], grid, x),
												interp(T[3], T[1], grid, x),
												interp(T[3], T[2], grid, x)
										)
								);
								break;
							case 0x08:
								faces.add(
										Face.retain(
												interp(T[3], T[0], grid, x),
												interp(T[3], T[2], grid, x),
												interp(T[3], T[1], grid, x)
										)
								);
								break;
						}
					}

					posToFaces.put(Vec3b.retain(x[0], x[1], x[2]), faces);
				}

		return posToFaces;
	}

	@Override
	@Nonnull
	public FaceList generateBlock(final byte[] x, final float[] grid) {
		final FaceList faces = FaceList.retain();
		for (byte[] T : TETRA_LIST) {
			byte triindex = 0;
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
					faces.add(
							Face.retain(
									interp(T[0], T[1], grid, x),
									interp(T[0], T[3], grid, x),
									interp(T[0], T[2], grid, x)
							)
					);
					break;
				case 0x01:
					faces.add(
							Face.retain(
									interp(T[0], T[1], grid, x),
									interp(T[0], T[2], grid, x),
									interp(T[0], T[3], grid, x)
							)
					);
					break;
				case 0x0D:
					faces.add(
							Face.retain(
									interp(T[1], T[0], grid, x),
									interp(T[1], T[2], grid, x),
									interp(T[1], T[3], grid, x)
							)
					);
					break;
				case 0x02:
					faces.add(
							Face.retain(
									interp(T[1], T[0], grid, x),
									interp(T[1], T[3], grid, x),
									interp(T[1], T[2], grid, x)
							)
					);
					break;
				case 0x0C:
					faces.add(
							Face.retain(
									interp(T[1], T[2], grid, x),
									interp(T[1], T[3], grid, x),
									interp(T[0], T[3], grid, x),
									interp(T[0], T[2], grid, x)
							)
					);
					break;
				case 0x03:
					faces.add(
							Face.retain(
									interp(T[1], T[2], grid, x),
									interp(T[0], T[2], grid, x),
									interp(T[0], T[3], grid, x),
									interp(T[1], T[3], grid, x)
							)
					);
					break;
				case 0x04:
					faces.add(
							Face.retain(
									interp(T[2], T[0], grid, x),
									interp(T[2], T[1], grid, x),
									interp(T[2], T[3], grid, x)
							)
					);
					break;
				case 0x0B:
					faces.add(
							Face.retain(
									interp(T[2], T[0], grid, x),
									interp(T[2], T[3], grid, x),
									interp(T[2], T[1], grid, x)
							)
					);
					break;
				case 0x05:
					faces.add(
							Face.retain(
									interp(T[0], T[1], grid, x),
									interp(T[1], T[2], grid, x),
									interp(T[2], T[3], grid, x),
									interp(T[0], T[3], grid, x)
							)
					);
					break;
				case 0x0A:
					faces.add(
							Face.retain(
									interp(T[0], T[1], grid, x),
									interp(T[0], T[3], grid, x),
									interp(T[2], T[3], grid, x),
									interp(T[1], T[2], grid, x)
							)
					);
					break;
				case 0x06:
					faces.add(
							Face.retain(
									interp(T[2], T[3], grid, x),
									interp(T[0], T[2], grid, x),
									interp(T[0], T[1], grid, x),
									interp(T[1], T[3], grid, x)
							)
					);
					break;
				case 0x09:
					faces.add(
							Face.retain(
									interp(T[2], T[3], grid, x),
									interp(T[1], T[3], grid, x),
									interp(T[0], T[1], grid, x),
									interp(T[0], T[2], grid, x)
							)
					);
					break;
				case 0x07:
					faces.add(
							Face.retain(
									interp(T[3], T[0], grid, x),
									interp(T[3], T[1], grid, x),
									interp(T[3], T[2], grid, x)
							)
					);
					break;
				case 0x08:
					faces.add(
							Face.retain(
									interp(T[3], T[0], grid, x),
									interp(T[3], T[2], grid, x),
									interp(T[3], T[1], grid, x)
							)
					);
					break;
			}
		}
		return faces;
	}

	private Vec3 interp(final byte i0, final byte i1, float[] grid, byte[] x) {
		final float g0 = grid[i0];
		final float g1 = grid[i1];
		byte[] p0 = CUBE_VERTICES[i0];
		final byte[] p1 = CUBE_VERTICES[i1];
		final Vec3 v = Vec3.retain(x[0], x[1], x[2]);
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
