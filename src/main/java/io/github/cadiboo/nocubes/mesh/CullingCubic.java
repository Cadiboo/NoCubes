package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.function.Predicate;

public class CullingCubic extends SimpleMesher {

	@Override
	public Vec3i getPositiveAreaExtension() {
		// Need data about the area's direct neighbour blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public Vec3i getNegativeAreaExtension() {
		// Need data about the area's direct neighbour blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public void generateCollisionsInternal(Area area, Predicate<IBlockState> isSmoothable, ShapeConsumer action) {
		generate(area, isSmoothable, (x, y, z, index) -> ShapeConsumer.acceptFullCube(x, y, z, action));
	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<IBlockState> isSmoothable, FaceAction action) {
		final float min = 0F;
		final float max = 1F - min;

		BlockPos size = area.size;
		int height = size.getY();
		int width = size.getX();

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		Face face = new Face();
		generate(area, isSmoothable, (x, y, z, index) -> {
			IBlockState[] blocks = area.getAndCacheBlocks();
			// Up (pos y)
			if (!isSmoothable.test(blocks[index + height]))
				if (!action.apply(pos.setPos(x, y, z), face.set(
					x + max, y + max, z + max,
					x + max, y + max, z + min,
					x + min, y + max, z + min,
					x + min, y + max, z + max
				)))
					return false;

			// Down (neg y)
			if (!isSmoothable.test(blocks[index - height]))
				if (!action.apply(pos.setPos(x, y, z), face.set(
					x + max, y, z + max,
					x + min, y, z + max,
					x + min, y, z + min,
					x + max, y, z + min
				)))
					return false;

			// South (pos z)
			if (!isSmoothable.test(blocks[index + width * height]))
				if (!action.apply(pos.setPos(x, y, z), face.set(
					x + max, y + max, z + max,
					x + min, y + max, z + max,
					x + min, y + min, z + max,
					x + max, y + min, z + max
				)))
					return false;

			// North (neg z)
			if (!isSmoothable.test(blocks[index - width * height]))
				if (!action.apply(pos.setPos(x, y, z), face.set(
					x + max, y + max, z + min,
					x + max, y + min, z + min,
					x + min, y + min, z + min,
					x + min, y + max, z + min
				)))
					return false;

			// East (pos x)
			if (!isSmoothable.test(blocks[index + 1]))
				if (!action.apply(pos.setPos(x, y, z), face.set(
					x + max, y + max, z + max,
					x + max, y + min, z + max,
					x + max, y + min, z + min,
					x + max, y + max, z + min
				)))
					return false;

			// West (neg x)
			if (!isSmoothable.test(blocks[index - 1]))
				if (!action.apply(pos.setPos(x, y, z), face.set(
					x + min, y + max, z + max,
					x + min, y + max, z + min,
					x + min, y + min, z + min,
					x + min, y + min, z + max
				)))
					return false;
			return true;
		});
	}

}
