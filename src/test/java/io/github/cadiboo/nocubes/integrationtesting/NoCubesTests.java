package io.github.cadiboo.nocubes.integrationtesting;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig.Server.MesherType;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

import static net.minecraft.world.level.block.Blocks.*;

/**
 * Integration tests
 *
 * @author Cadiboo
 */
final class NoCubesTests {

	record Test(String name, Runnable action) {}

	static Test[] createTests() {
		return new Test[]{
//			test("the version in mods.toml should have been replaced by gradle", () -> assertFalse(0 == ModList.get().getModFileById(NoCubes.MOD_ID).getMods().get(0).getVersion().getMajorVersion())),
			test("stone should be smoothable", () -> assertTrue(NoCubes.smoothableHandler.isSmoothable(STONE.defaultBlockState()))),
			test("dirt should be smoothable", () -> assertTrue(NoCubes.smoothableHandler.isSmoothable(DIRT.defaultBlockState()))),
			test("air should not be smoothable", () -> assertFalse(NoCubes.smoothableHandler.isSmoothable(AIR.defaultBlockState()))),
			test("removing smoothable should work", () -> {
				var dirt = DIRT.defaultBlockState();
				var oldValue = NoCubes.smoothableHandler.isSmoothable(dirt);
				NoCubes.smoothableHandler.setSmoothable(false, dirt);
				assertFalse(NoCubes.smoothableHandler.isSmoothable(dirt));
				if (oldValue)
					NoCubes.smoothableHandler.setSmoothable(true, dirt);
			}),
			test("area sanity check", NoCubesTests::areaSanityCheck),
			test("mesher sanity check", NoCubesTests::mesherSanityCheck),
		};
	}

	private static Test test(String name, Runnable action) {
		return new Test(name, action);
	}

	private static void areaSanityCheck() {
		BlockPos start = new BlockPos(100, 50, 25);
		assertTrue(0 == new Area(null, start, new BlockPos(0, 0, 0)).numBlocks());
		assertTrue(1 == new Area(null, start, new BlockPos(1, 1, 1)).numBlocks());
	}

	private static void mesherSanityCheck() {
		Predicate<BlockState> isSmoothable = $ -> $ == STONE.defaultBlockState();

		var start = new BlockPos(100, 50, 25);
		var area = new Area(null, start, new BlockPos(5, 5, 5)) {
			@Override
			public BlockState[] getAndCacheBlocks() {
				BlockState[] states = new BlockState[numBlocks()];
				for (int i = 0; i < states.length; i++)
					states[i] = i % 2 == 0 ? STONE.defaultBlockState() : AIR.defaultBlockState();
				return states;
			}

			@Override
			public void close() {
				// No-op
			}
		};
		for (var mesher : MesherType.values())
			mesher.instance.generateGeometry(area, isSmoothable, NoCubesTests::checkAndMutate);
	}

	private static boolean checkAndMutate(MutableBlockPos pos, Face face) {
		assertFalse(pos.getX() < 0);
		assertFalse(pos.getX() >= 5);
		pos.move(1000, 1000, 1000);

		assertFalse(face.v0.x < -1);
		assertFalse(face.v0.x >= 6);
		face.v0.x += 10000;
		return true;
	}

	private static void assertTrue(boolean value) {
		if (!value)
			throw new AssertionError("Expected the passed in value to be true");
	}

	private static void assertFalse(boolean value) {
		if (value)
			throw new AssertionError("Expected the passed in value to be false");
	}

}
