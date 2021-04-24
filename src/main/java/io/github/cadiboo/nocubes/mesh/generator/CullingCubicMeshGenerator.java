package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.function.Predicate;

public class CullingCubicMeshGenerator implements MeshGenerator {

	@Override
	public Vec3i getPositiveAreaExtension() {
		// Need data about the each block's direct neighbours to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public Vec3i getNegativeAreaExtension() {
		// Need data about the each block's direct neighbours to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public void generate(Area area, Predicate<IBlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction) {
		BlockPos start = area.start;
		BlockPos end = area.end;

		int depth = end.getZ() - start.getZ();
		int height = end.getY() - start.getY();
		int width = end.getX() - start.getX();

		final float min = 0.75F;
		final float max = 0.25F;

		IBlockState[] blocks = area.getAndCacheBlocks();
		MutableBlockPos pos = new MutableBlockPos();
		Face face = new Face();
		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					boolean smoothable = isSmoothable.test(blocks[index]);
					if (!voxelAction.apply(pos.setPos(x, y, z), smoothable ? 1 : 0))
						return;
					if (!smoothable)
						// We aren't smoothable
						continue;

					// Up (pos y)
					if (y < height - 1 && !isSmoothable.test(blocks[index + height]))
						if (!faceAction.apply(pos.setPos(x, y, z), face.set(
							x + min, y + min, z + min,
							x + min, y + min, z + max,
							x + max, y + min, z + max,
							x + max, y + min, z + min
						)))
							return;

					// Down (neg y)
					if (y > 0 && !isSmoothable.test(blocks[index - height]))
						if (!faceAction.apply(pos.setPos(x, y, z), face.set(
							x + min, y, z + min,
							x + max, y, z + min,
							x + max, y, z + max,
							x + min, y, z + max
						)))
							return;

					// South (pos z)
					if (z < depth - 1 && !isSmoothable.test(blocks[index + width * height]))
						if (!faceAction.apply(pos.setPos(x, y, z), face.set(
							x + min, y + min, z + min,
							x + max, y + min, z + min,
							x + max, y + max, z + min,
							x + min, y + max, z + min
						)))
							return;

					// North (neg z)
					if (z > 0 && !isSmoothable.test(blocks[index - width * height]))
						if (!faceAction.apply(pos.setPos(x, y, z), face.set(
							x + min, y + min, z + max,
							x + min, y + max, z + max,
							x + max, y + max, z + max,
							x + max, y + min, z + max
						)))
							return;

					// East (pos x)
					if (x < width - 1 && !isSmoothable.test(blocks[index + 1]))
						if (!faceAction.apply(pos.setPos(x, y, z), face.set(
							x + min, y + min, z + min,
							x + min, y + max, z + min,
							x + min, y + max, z + max,
							x + min, y + min, z + max
						)))
							return;

					// West (neg x)
					if (x > 0 && !isSmoothable.test(blocks[index - 1]))
						if (!faceAction.apply(pos.setPos(x, y, z), face.set(
							x + max, y + min, z + min,
							x + max, y + min, z + max,
							x + max, y + max, z + max,
							x + max, y + max, z + min
						)))
							return;
				}
			}
		}
	}

}
