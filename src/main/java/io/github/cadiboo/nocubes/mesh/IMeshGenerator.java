package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public interface IMeshGenerator {

	/**
	 * Generates a chunk WITHOUT OFFSETTING OR TRANSLATING ANY VERTICES
	 *
	 * @param scalarFieldData the float[] data
	 * @param dimensions      the dimensions of the mesh
	 * @return the chunk vertices
	 */
	@Nonnull
	HashMap<Vec3b, FaceList> generateChunk(final float[] scalarFieldData, final byte[] dimensions);

	/**
	 * @param position             the byte[] position relative to the chunk pos
	 * @param neighbourDensityGrid the neighbour density grid
	 * @return the block vertices
	 */
	@Nonnull
	FaceList generateBlock(byte[] position, float[] neighbourDensityGrid);

}
