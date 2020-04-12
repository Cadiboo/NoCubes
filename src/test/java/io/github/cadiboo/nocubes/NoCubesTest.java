package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.test.Test;
import net.minecraft.block.Blocks;

import java.util.Collections;
import java.util.List;

import static io.github.cadiboo.nocubes.test.TestUtil.assertFalse;
import static io.github.cadiboo.nocubes.test.TestUtil.assertTrue;

/**
 * @author Cadiboo
 */
public class NoCubesTest {

	public static void addTests(final List<Test> tests) {
		Collections.addAll(tests,
			new Test("stone should be smoothable", () -> assertTrue(NoCubes.isStateSmoothable(Blocks.STONE.getDefaultState()))),
			new Test("dirt should be smoothable", () -> assertTrue(NoCubes.isStateSmoothable(Blocks.DIRT.getDefaultState()))),
			new Test("air should not be smoothable", () -> assertFalse(NoCubes.isStateSmoothable(Blocks.AIR.getDefaultState()))),
			new Test("setting smoothable should work", () -> {
				NoCubes.setStateSmoothable(Blocks.DIRT.getDefaultState(), false);
				assertFalse(NoCubes.isStateSmoothable(Blocks.DIRT.getDefaultState()));
			})
		);
	}

}
