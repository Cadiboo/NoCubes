package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.PooledFace;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class OldNoCubes implements IMeshGenerator {

	@Nonnull
	@Override
	public Map<int[], ArrayList<PooledFace>> generateChunk(final float[] data, final int[] dims) {
		return Collections.emptyMap();
	}

}
