package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

abstract class SimpleMesher implements Mesher {

	interface Action {
		boolean apply(int x, int y, int z, int index);
	}

	void generate(Area area, Predicate<BlockState> isSmoothable, Action action) {
		var size = area.size;
		int depth = size.getZ();
		int height = size.getY();
		int width = size.getX();

		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					if (isOutsideMesh(x, y, z, size))
						// Some generators need extra neighbour data, we don't want to call the action for it though
						continue;
					if (!isSmoothable.test(area.getAndCacheBlocks()[index]))
						continue;
					if (!action.apply(x, y, z, index))
						return;
				}
			}
		}
	}
}
