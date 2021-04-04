package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;

public class CubicMeshGenerator implements MeshGenerator {
	@Override
	public void generate(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		BlockPos start = area.start;
		BlockPos end = area.end;

		int maxZ = end.getZ();
		int maxY = end.getY();
		int maxX = end.getX();

		int width = end.getX() - start.getX();

		BlockState[] blocks = area.getAndCacheBlocks();
		BlockPos.Mutable pos = start.mutable();
		Face face = new Face();
		int index = 0;
		for (int z = start.getZ(); z < maxZ; ++z)
			for (int y = start.getY(); y < maxY; ++y)
				for (int x = start.getX(); x < maxX; ++x, ++index) {
					if (y == maxY - 1)
						continue;
					BlockState state = blocks[index];
					if (!isSmoothable.test(state))
						continue;
					BlockState up = blocks[index + width];
					if (!isSmoothable.test(up))
						continue;
					pos.set(x, y, z);
					face.v0.set(x + 1, y, z + 1);
					face.v1.set(x + 0, y, z + 1);
					face.v2.set(x + 0, y, z + 0);
					face.v3.set(x + 1, y, z + 0);
					action.apply(pos, face);
				}

	}
}
