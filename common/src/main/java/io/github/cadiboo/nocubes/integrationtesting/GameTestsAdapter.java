package io.github.cadiboo.nocubes.integrationtesting;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final public class GameTestsAdapter {

	public static Collection<TestFunction> createTests(Supplier<Stream<Block>> getAllBlocks) {
		return Arrays.stream(NoCubesTests.createTests(getAllBlocks))
			.map(test -> new TestFunction(
				NoCubes.MOD_ID + "Integration", // batch
				NoCubes.MOD_ID + "_" + test.name().replace(' ', '_'),
				new ResourceLocation(NoCubes.MOD_ID, "empty").toString(), // structure
				Rotation.NONE,
				20, // maxTicks
				20L, // setupTicks
				true, // required
				helper -> {
					try {
						test.action().run();
					} catch (AssertionError e) {
						helper.fail(e.getMessage());
						return;
					}
					helper.succeed();
				}
			))
			.collect(Collectors.toList());
	}
}
