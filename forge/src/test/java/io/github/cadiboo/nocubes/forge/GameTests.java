package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.integrationtesting.GameTestsAdapter;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

@GameTestHolder(NoCubes.MOD_ID)
public class GameTests {

	// Refers to './run/gameTestServer/gameteststructures/empty.snbt'
	public static final String EMPTY_STRUCTURE = new ResourceLocation(NoCubes.MOD_ID, "empty").toString();

	@GameTestGenerator
	public static Collection<TestFunction> createTests() {
		return GameTestsAdapter.createTests(EMPTY_STRUCTURE, () -> ForgeRegistries.BLOCKS.getValues().stream());
	}
}
