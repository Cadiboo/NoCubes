package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class CubicMeshGenerator implements MeshGenerator {

	@Override
	public void generate(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		BlockPos start = area.start;
		BlockPos end = area.end;

		int depth = end.getZ() - start.getZ();
		int height = end.getY() - start.getY();
		int width = end.getX() - start.getX();

		BlockState[] blocks = area.getAndCacheBlocks();
		BlockPos.Mutable pos = start.mutable();
		Face face = new Face();
		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					if (!isSmoothable.test(blocks[index]))
						// We aren't smoothable
						continue;

					// Up (pos y)
					if (y < height - 1 && !isSmoothable.test(blocks[index + height]))
						action.apply(pos, face.set(
							x + 1, y + 1, z + 0,
							x + 0, y + 1, z + 0,
							x + 0, y + 1, z + 1,
							x + 1, y + 1, z + 1
						));

					// Down (neg y)
					if (y > 0 && !isSmoothable.test(blocks[index - height]))
						action.apply(pos, face.set(
							x + 1, y, z + 1,
							x + 0, y, z + 1,
							x + 0, y, z + 0,
							x + 1, y, z + 0
						));

					// South (pos z)
					if (z < depth - 1 && !isSmoothable.test(blocks[index + width * height]))
						action.apply(pos, face.set(
							x + 1, y + 1, z + 1,
							x + 0, y + 1, z + 1,
							x + 0, y + 0, z + 1,
							x + 1, y + 0, z + 1
						));

					// North (neg z)
					if (z > 0 && !isSmoothable.test(blocks[index - width * height]))
						action.apply(pos, face.set(
							x + 1, y + 0, z + 0,
							x + 0, y + 0, z + 0,
							x + 0, y + 1, z + 0,
							x + 1, y + 1, z + 0
						));

					// East (pos x)
					if (x < width - 1 && !isSmoothable.test(blocks[index + 1]))
						action.apply(pos, face.set(
							x + 1, y + 0, z + 1,
							x + 1, y + 0, z + 0,
							x + 1, y + 1, z + 0,
							x + 1, y + 1, z + 1
						));

					// West (neg x)
					if (x > 0 && !isSmoothable.test(blocks[index - 1]))
						action.apply(pos, face.set(
							x + 0, y + 1, z + 1,
							x + 0, y + 1, z + 0,
							x + 0, y + 0, z + 0,
							x + 0, y + 0, z + 1
						));
				}
			}
		}
	}

}
