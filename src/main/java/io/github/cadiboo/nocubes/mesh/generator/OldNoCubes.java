package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.Vec3b;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public class OldNoCubes implements IMeshGenerator {

	@Nonnull
	@Override
	public HashMap<Vec3b, FaceList> generateChunk(final float[] scalarFieldData, final byte[] dimensions) {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public FaceList generateBlock(final byte[] position, final float[] neighbourDensityGrid) {
		throw new UnsupportedOperationException();
	}

}
