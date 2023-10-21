package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

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
	public void generateCollisionsInternal(Area area, Predicate<BlockState> isSmoothable, ShapeConsumer action) {
		generate(area, isSmoothable, (x, y, z, index) -> ShapeConsumer.acceptFullCube(x, y, z, action));
	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		final float min = 0F;
		final float max = 1F - min;

		var size = area.size;
		int height = size.getY();
		int width = size.getX();

		var pos = POS_INSTANCE.get();
		var face = FACE_INSTANCE.get();
		generate(area, isSmoothable, (x, y, z, index) -> {
			var blocks = area.getAndCacheBlocks();
			// Up (pos y)
			if (!isSmoothable.test(blocks[index + height]))
				if (!action.apply(pos.set(x, y, z), StupidCubic.upFace(face, x, y, z, min, max)))
					return false;

			// Down (neg y)
			if (!isSmoothable.test(blocks[index - height]))
				if (!action.apply(pos.set(x, y, z), StupidCubic.downFace(face, x, y, z, min, max)))
					return false;

			// South (pos z)
			if (!isSmoothable.test(blocks[index + width * height]))
				if (!action.apply(pos.set(x, y, z), StupidCubic.southFace(face, x, y, z, min, max)))
					return false;

			// North (neg z)
			if (!isSmoothable.test(blocks[index - width * height]))
				if (!action.apply(pos.set(x, y, z), StupidCubic.northFace(face, x, y, z, min, max)))
					return false;

			// East (pos x)
			if (!isSmoothable.test(blocks[index + 1]))
				if (!action.apply(pos.set(x, y, z), StupidCubic.eastFace(face, x, y, z, min, max)))
					return false;

			// West (neg x)
			if (!isSmoothable.test(blocks[index - 1]))
				if (!action.apply(pos.set(x, y, z), StupidCubic.westFace(face, x, y, z, min, max)))
					return false;
			return true;
		});
	}

}
