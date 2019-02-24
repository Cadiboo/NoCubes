package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3b;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class OldNoCubes implements IMeshGenerator {

	// TODO FIXME this entire thing is broken and not really compatible with the current rendering model.
	// should I add a special-snowflake renderer for OldNoCubes?

	@Nonnull
	@Override
	public HashMap<Vec3b, FaceList> generateChunk(final float[] data, final byte[] dims) {

		final HashMap<Vec3b, FaceList> posToFaces = new HashMap<>();

		for (byte z = 0; z < dims[0]; ++z) {
			for (byte y = 0; y < dims[1]; ++y) {
				for (byte x = 0; x < dims[2]; ++x) {

					final boolean[] neighbours = new boolean[8];

					byte neighbourIndex = 0;
					//neighbours
					for (byte xOffset = 0; xOffset < 2; ++xOffset) {
						for (byte yOffset = 0; yOffset < 2; ++yOffset) {
							for (byte zOffset = 0; zOffset < 2; ++zOffset, ++neighbourIndex) {
								//TODO: set the index in the loop with whatever black magic mikelasko uses
								final int dataIndex = (x + xOffset) + dims[0] * (y + yOffset + dims[1] * (z + zOffset));

								final float neighbourDensity = data[dataIndex];
								final boolean isNeighbourInsideIsosurface = neighbourDensity < 0;

								neighbours[neighbourIndex] = isNeighbourInsideIsosurface;
							}
						}
					}

					final FaceList faces = FaceList.retain();

					// (0, 1, 0), (0, 0, 0), (0, 0, 1), (0, 1, 1)
					if (neighbours[2] && neighbours[0] && neighbours[1] && neighbours[3]) {
						faces.add(
								Face.retain(
										Vec3.retain(x, y + 1, z),
										Vec3.retain(x, y, z),
										Vec3.retain(x, y, z + 1),
										Vec3.retain(x, y + 1, z + 1)
								)
						);
					}

					posToFaces.put(Vec3b.retain(x, y, z), faces);
				}

			}
		}

		return posToFaces;
	}

	@Nonnull
	@Override
	public FaceList generateBlock(final byte[] position, final float[] neighbourDensityGrid) {
		return FaceList.retain();
	}

}
