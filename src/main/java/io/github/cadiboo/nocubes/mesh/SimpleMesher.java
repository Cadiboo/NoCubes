package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

abstract class SimpleMesher implements Mesher {

	interface Action {
		boolean apply(int x, int y, int z, int index);
	}

	void iterateSmoothBlocksInsideMesh(Area area, Predicate<BlockState> isSmoothable, Action action) {
		var size = area.size;
		int depth = size.getZ();
		int height = size.getY();
		int width = size.getX();

		var negativeExtension = getNegativeAreaExtension();
		var positiveExtension = getPositiveAreaExtension();
		int meshStartX = negativeExtension.getX();
		int meshEndX = size.getX() - positiveExtension.getX();
		int meshStartY = negativeExtension.getY();
		int meshEndY = size.getY() - positiveExtension.getY();
		var meshStartZ = negativeExtension.getZ();
		int meshEndZ = size.getZ() - positiveExtension.getZ();

		var blocks = area.getAndCacheBlocks();
		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					if (isOutsideMesh(x, y, z, meshStartX, meshEndX, meshStartY, meshEndY, meshStartZ, meshEndZ)) {
						// Some generators need extra neighbour data, we don't want to call the action for it though
						continue;
					}
					if (!isSmoothable.test(blocks[index]))
						continue;
					if (!action.apply(x, y, z, index))
						return;
				}
			}
		}
	}

	boolean isOutsideMesh(
		int x, int y, int z,
		int meshStartX, int meshEndX,
		int meshStartY, int meshEndY,
		int meshStartZ, int meshEndZ
	) {
		// Block is outside where we are generating it for, we only query it for its neighbouring faces
		if (x < meshStartX || x >= meshEndX)
			return true;
		if (y < meshStartY || y >= meshEndY)
			return true;
		if (z < meshStartZ || z >= meshEndZ)
			return true;
		return false;
	}
}
