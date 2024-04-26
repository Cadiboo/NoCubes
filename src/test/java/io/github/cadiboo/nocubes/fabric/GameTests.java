package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.integrationtesting.GameTestsAdapter;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;

import java.util.Collection;

public class GameTests {
	@GameTestGenerator
	public static Collection<TestFunction> createTests() {
		return GameTestsAdapter.createTests();
	}
}
