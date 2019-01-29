package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.PooledFace;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static io.github.cadiboo.nocubes.util.Vec3.PooledVec3;

public class OldNoCubes implements IMeshGenerator {

	// TODO FIXME this entire thing is broken and not really compatible with the current rendering model.
	// should I add a special-snowflake renderer for OldNoCubes?

	@Nonnull
	@Override
	public Map<int[], ArrayList<PooledFace>> generateChunk(final float[] data, final int[] dims) {

		final HashMap<int[], ArrayList<PooledFace>> posToFaces = new HashMap<>();
		final ArrayList<PooledFace> faces = new ArrayList<>();

		for (int z = 0; z < dims[0]; ++z) {
			for (int y = 0; y < dims[1]; ++y) {
				for (int x = 0; x < dims[2]; ++x) {

					final boolean[] neighbours = new boolean[8];

					int neighbourIndex = 0;
					//neighbours
					for (int xOffset = 0; xOffset < 2; ++xOffset) {
						for (int yOffset = 0; yOffset < 2; ++yOffset) {
							for (int zOffset = 0; zOffset < 2; ++zOffset, ++neighbourIndex) {
								//TODO: set the index in the loop with whatever black magic mikelasko uses
								final int dataIndex = (x + xOffset) + dims[0] * (y + yOffset + dims[1] * (z + zOffset));

								final float neighbourDensity = data[dataIndex];
								final boolean isNeighbourInsideIsosurface = neighbourDensity < 0;

								neighbours[neighbourIndex] = isNeighbourInsideIsosurface;
							}
						}
					}

					// (0, 1, 0), (0, 0, 0), (0, 0, 1), (0, 1, 1)
					if (neighbours[2] && neighbours[0] && neighbours[1] && neighbours[3]) {
						faces.add(
								PooledFace.retain(
										PooledVec3.retain(x, y + 1, z),
										PooledVec3.retain(x, y, z),
										PooledVec3.retain(x, y, z + 1),
										PooledVec3.retain(x, y + 1, z + 1)
								)
						);
					}

					posToFaces.put(new int[]{x, y, z}, new ArrayList<>(faces));
					faces.clear();
				}

			}
		}

		return posToFaces;
	}

}
