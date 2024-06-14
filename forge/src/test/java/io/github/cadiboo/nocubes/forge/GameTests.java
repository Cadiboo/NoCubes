package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.integrationtesting.GameTestsAdapter;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.Collection;

@GameTestHolder(NoCubes.MOD_ID)
public class GameTests {
	@GameTestGenerator
	public static Collection<TestFunction> createTests() {
		return GameTestsAdapter.createTests();
	}
}
