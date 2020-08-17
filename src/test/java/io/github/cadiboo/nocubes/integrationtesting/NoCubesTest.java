package io.github.cadiboo.nocubes.integrationtesting;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.block.Blocks;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cadiboo
 */
public class NoCubesTest {

	public static void addTests(final List<Test> tests) {
		Collections.addAll(tests,
//			new Test("stone should be smoothable", () -> assertTrue(NoCubes.smoothableHandler.isSmoothable(Blocks.STONE.getDefaultState()))),
//			new Test("dirt should be smoothable", () -> assertTrue(NoCubes.smoothableHandler.isSmoothable(Blocks.DIRT.getDefaultState()))),
			new Test("air should not be smoothable", () -> assertFalse(NoCubes.smoothableHandler.isSmoothable(Blocks.AIR.getDefaultState()))),
			new Test("removing smoothable should work", () -> {
				NoCubes.smoothableHandler.removeSmoothable(Blocks.DIRT.getDefaultState());
				assertFalse(NoCubes.smoothableHandler.isSmoothable(Blocks.DIRT.getDefaultState()));
			})
		);
	}

}
