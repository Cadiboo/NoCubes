package io.github.cadiboo.nocubes.integrationtesting;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Collections;
import java.util.List;

/**
 * @author Cadiboo
 */
public class NoCubesTest {

	public static void addTests(final List<Test> tests) {
		Collections.addAll(tests,
//			new Test("stone should be smoothable", () -> assertTrue(NoCubes.smoothableHandler.isSmoothable(Blocks.STONE.defaultBlockState()))),
//			new Test("dirt should be smoothable", () -> assertTrue(NoCubes.smoothableHandler.isSmoothable(Blocks.DIRT.defaultBlockState()))),
			new Test("air should not be smoothable", () -> assertFalse(NoCubes.smoothableHandler.isSmoothable(Blocks.AIR.defaultBlockState()))),
			new Test("removing smoothable should work", () -> {
				BlockState dirt = Blocks.DIRT.defaultBlockState();
				boolean oldValue = NoCubes.smoothableHandler.isSmoothable(dirt);
				NoCubes.smoothableHandler.removeSmoothable(dirt);
				assertFalse(NoCubes.smoothableHandler.isSmoothable(dirt));
				if (oldValue)
					NoCubes.smoothableHandler.addSmoothable(dirt);
			})
		);
	}

	private static void assertFalse(boolean value) {
		if (value)
			throw new AssertionError("Expected the passed in value to be false");
	}

}
