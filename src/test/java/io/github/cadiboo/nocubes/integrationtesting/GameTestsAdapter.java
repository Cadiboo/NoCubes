package io.github.cadiboo.nocubes.integrationtesting;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Rotation;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@GameTestHolder(NoCubes.MOD_ID)
final public class GameTestsAdapter {

	// Refers to './run/gameTestServer/gameteststructures/empty.snbt'
	public static final String EMPTY_STRUCTURE = new ResourceLocation(NoCubes.MOD_ID, "empty").toString();

	@GameTestGenerator
	public static Collection<TestFunction> createTests() {
		return Arrays.stream(NoCubesTests.createTests())
			.map(test -> new TestFunction(
				NoCubes.MOD_ID + " integration", // batch
				test.name().replace(' ', '_'),
				EMPTY_STRUCTURE, // structure
				Rotation.NONE,
				20, // maxTicks
				20L, // setupTicks
				true, // required
				helper -> {
					test.action().run();
					helper.succeed();
				}
			))
			.collect(Collectors.toList());
	}
}
