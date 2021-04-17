package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

import java.util.function.Predicate;

public class CullingCubicMeshGenerator implements MeshGenerator {

	@Override
	public Vector3i getPositiveAreaExtension() {
		// Need data about the each block's direct neighbours to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public Vector3i getNegativeAreaExtension() {
		// Need data about the each block's direct neighbours to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public void generate(Area area, Predicate<BlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction) {
		BlockPos start = area.start;
		BlockPos end = area.end;

		int depth = end.getZ() - start.getZ();
		int height = end.getY() - start.getY();
		int width = end.getX() - start.getX();

		BlockState[] blocks = area.getAndCacheBlocks();
		BlockPos.Mutable pos = new BlockPos.Mutable();
		Face face = new Face();
		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					boolean smoothable = isSmoothable.test(blocks[index]);
					if (!voxelAction.apply(pos.set(x, y, z), smoothable ? 1 : 0))
						return;
					if (!smoothable)
						// We aren't smoothable
						continue;

					// Up (pos y)
					if (y < height - 1 && !isSmoothable.test(blocks[index + height]))
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + 0.75F, y + 0.75F, z + 0.75F,
							x + 0.75F, y + 0.75F, z + 0.25F,
							x + 0.25F, y + 0.75F, z + 0.25F,
							x + 0.25F, y + 0.75F, z + 0.75F
						)))
							return;

					// Down (neg y)
					if (y > 0 && !isSmoothable.test(blocks[index - height]))
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + 0.75F, y, z + 0.75F,
							x + 0.25F, y, z + 0.75F,
							x + 0.25F, y, z + 0.25F,
							x + 0.75F, y, z + 0.25F
						)))
							return;

					// South (pos z)
					if (z < depth - 1 && !isSmoothable.test(blocks[index + width * height]))
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + 0.75F, y + 0.75F, z + 0.75F,
							x + 0.25F, y + 0.75F, z + 0.75F,
							x + 0.25F, y + 0.25F, z + 0.75F,
							x + 0.75F, y + 0.25F, z + 0.75F
						)))
							return;

					// North (neg z)
					if (z > 0 && !isSmoothable.test(blocks[index - width * height]))
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + 0.75F, y + 0.75F, z + 0.25F,
							x + 0.75F, y + 0.25F, z + 0.25F,
							x + 0.25F, y + 0.25F, z + 0.25F,
							x + 0.25F, y + 0.75F, z + 0.25F
						)))
							return;

					// East (pos x)
					if (x < width - 1 && !isSmoothable.test(blocks[index + 1]))
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + 0.75F, y + 0.75F, z + 0.75F,
							x + 0.75F, y + 0.25F, z + 0.75F,
							x + 0.75F, y + 0.25F, z + 0.25F,
							x + 0.75F, y + 0.75F, z + 0.25F
						)))
							return;

					// West (neg x)
					if (x > 0 && !isSmoothable.test(blocks[index - 1]))
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + 0.25F, y + 0.75F, z + 0.75F,
							x + 0.25F, y + 0.75F, z + 0.25F,
							x + 0.25F, y + 0.25F, z + 0.25F,
							x + 0.25F, y + 0.25F, z + 0.75F
						)))
							return;
				}
			}
		}
	}

}