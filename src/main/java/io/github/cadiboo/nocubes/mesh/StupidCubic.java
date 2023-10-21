package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class StupidCubic extends SimpleMesher {

	@Override
	public Vec3i getPositiveAreaExtension() {
		return ModUtil.VEC_ZERO;
	}

	@Override
	public Vec3i getNegativeAreaExtension() {
		return ModUtil.VEC_ZERO;
	}

	@Override
	public void generateCollisionsInternal(Area area, Predicate<BlockState> isSmoothable, ShapeConsumer action) {
		iterateSmoothBlocksInsideMesh(area, isSmoothable, (x, y, z, index) -> ShapeConsumer.acceptFullCube(x, y, z, action));
	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		final float min = 0F;
		final float max = 1F - min;

		var pos = POS_INSTANCE.get();
		var face = FACE_INSTANCE.get();
		iterateSmoothBlocksInsideMesh(area, isSmoothable, (x, y, z, index) -> {
			for (var dir : ModUtil.DIRECTIONS) {
				if (!action.apply(pos.set(x, y, z), dirFace(dir, face, x, y, z, min, max))) {
					return false;
				}
			}
			return true;
		});
	}

	static Face dirFace(Direction dir, Face mut, int x, int y, int z, float min, float max) {
		return switch (dir) {
			case DOWN -> downFace(mut, x, y, z, min, max);
			case UP -> upFace(mut, x, y, z, min, max);
			case NORTH -> northFace(mut, x, y, z, min, max);
			case SOUTH -> southFace(mut, x, y, z, min, max);
			case WEST -> westFace(mut, x, y, z, min, max);
			case EAST -> eastFace(mut, x, y, z, min, max);
		};
	}

	static Face downFace(Face mut, int x, int y, int z, float min, float max) {
		return mut.set(
			x + max, y + min, z + max,
			x + min, y + min, z + max,
			x + min, y + min, z + min,
			x + max, y + min, z + min
		);
	}

	static Face upFace(Face mut, int x, int y, int z, float min, float max) {
		return mut.set(
			x + max, y + max, z + max,
			x + max, y + max, z + min,
			x + min, y + max, z + min,
			x + min, y + max, z + max
		);
	}

	static Face northFace(Face mut, int x, int y, int z, float min, float max) {
		return mut.set(
			x + max, y + max, z + min,
			x + max, y + min, z + min,
			x + min, y + min, z + min,
			x + min, y + max, z + min
		);
	}

	static Face southFace(Face mut, int x, int y, int z, float min, float max) {
		return mut.set(
			x + max, y + max, z + max,
			x + min, y + max, z + max,
			x + min, y + min, z + max,
			x + max, y + min, z + max
		);
	}

	static Face westFace(Face mut, int x, int y, int z, float min, float max) {
		return mut.set(
			x + min, y + max, z + max,
			x + min, y + max, z + min,
			x + min, y + min, z + min,
			x + min, y + min, z + max
		);
	}

	static Face eastFace(Face mut, int x, int y, int z, float min, float max) {
		return mut.set(
			x + max, y + max, z + max,
			x + max, y + min, z + max,
			x + max, y + min, z + min,
			x + max, y + max, z + min
		);
	}

}
