package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec3;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class OldNoCubes implements IMeshGenerator {

	@Nonnull
	@Override
	public Map<int[], ArrayList<Face<Vec3>>> generateChunk(final float[] data, final int[] dims) {
		return Collections.emptyMap();
	}

}
