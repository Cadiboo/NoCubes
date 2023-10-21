package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class CullingChamfer extends SimpleMesher {

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
		generate(area, isSmoothable, (x, y, z, index) -> action.accept(
			// TODO: Make this adhere to the chamfer
			x, y, z,
			x + 1, y + 1, z + 1
		));
	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		final float min = 0.25F;
		final float max = 1F - min;

		var size = area.size;
		int height = size.getY();
		int width = size.getX();

		var pos = POS_INSTANCE.get();
		var face = FACE_INSTANCE.get();
		generate(area, isSmoothable, (x, y, z, index) -> {
			var blocks = area.getAndCacheBlocks();
			// Same offsets as in Area#generateDirectionOffsetsLookup
			boolean posY = !isSmoothable.test(blocks[index + height]);
			boolean negY = !isSmoothable.test(blocks[index - height]);
			boolean posX = !isSmoothable.test(blocks[index + 1]);
			boolean negX = !isSmoothable.test(blocks[index - 1]);
			boolean posZ = !isSmoothable.test(blocks[index + width * height]);
			boolean negZ = !isSmoothable.test(blocks[index - width * height]);

			float maxZ = posZ ? max : 1;
			float minZ = negZ ? min : 0;
			float maxY = posY ? max : 1;
			float minY = negY ? min : 0;
			float maxX = posX ? max : 1;
			float minX = negX ? min : 0;

			// Up (pos y)
			if (posY) {
				if (!action.apply(pos.set(x, y, z), face.set(
					x + maxX, y + 1, z + maxZ,
					x + maxX, y + 1, z + minZ,
					x + minX, y + 1, z + minZ,
					x + minX, y + 1, z + maxZ
				)))
					return false;
			}

			// Down (neg y)
			if (negY) {
				if (!action.apply(pos.set(x, y, z), face.set(
					x + maxX, y + 0, z + maxZ,
					x + minX, y + 0, z + maxZ,
					x + minX, y + 0, z + minZ,
					x + maxX, y + 0, z + minZ
				)))
					return false;
			}

			// South (pos z)
			if (posZ) {
				if (!action.apply(pos.set(x, y, z), face.set(
					x + maxX, y + maxY, z + 1,
					x + minX, y + maxY, z + 1,
					x + minX, y + minY, z + 1,
					x + maxX, y + minY, z + 1
				)))
					return false;
			}

			// North (neg z)
			if (negZ) {
				if (!action.apply(pos.set(x, y, z), face.set(
					x + maxX, y + maxY, z + 0,
					x + maxX, y + minY, z + 0,
					x + minX, y + minY, z + 0,
					x + minX, y + maxY, z + 0
				)))
					return false;
			}

			// East (pos x)
			if (posX) {
				if (!action.apply(pos.set(x, y, z), face.set(
					x + 1, y + maxY, z + maxZ,
					x + 1, y + minY, z + maxZ,
					x + 1, y + minY, z + minZ,
					x + 1, y + maxY, z + minZ
				)))
					return false;
			}

			// West (neg x)
			if (negX) {
				if (!action.apply(pos.set(x, y, z), face.set(
					x + 0, y + maxY, z + maxZ,
					x + 0, y + maxY, z + minZ,
					x + 0, y + minY, z + minZ,
					x + 0, y + minY, z + maxZ
				)))
					return false;
			}

			// TODO: The orientation of faces on these chamfers is wrong

			// Top chamfers
			{
				if (posY && posZ) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + minX, y + 1 - min, z + 1,
						x + maxX, y + 1 - min, z + 1,
						x + maxX, y + 1, z + 1 - min,
						x + minX, y + 1, z + 1 - min
					)))
						return false;
				}
				if (posY && negZ) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + minX, y + 1, z + 0 + min,
						x + maxX, y + 1, z + 0 + min,
						x + maxX, y + 1 - min, z + 0,
						x + minX, y + 1 - min, z + 0
					)))
						return false;
				}
				if (posY && posX) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + 1, y + 1 - min, z + maxZ,
						x + 1, y + 1 - min, z + minZ,
						x + 1 - min, y + 1, z + minZ,
						x + 1 - min, y + 1, z + maxZ
					)))
						return false;
				}
				if (posY && negX) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + 0 + min, y + 1, z + maxZ,
						x + 0 + min, y + 1, z + minZ,
						x + 0, y + 1 - min, z + minZ,
						x + 0, y + 1 - min, z + maxZ
					)))
						return false;
				}
			}

			// Bottom chamfers
			{
				if (negY && posZ) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + minX, y + 0, z + 1 - min,
						x + maxX, y + 0, z + 1 - min,
						x + maxX, y + 0 + min, z + 1,
						x + minX, y + 0 + min, z + 1
					)))
						return false;
				}
				if (negY && negZ) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + minX, y + 0 + min, z + 0,
						x + maxX, y + 0 + min, z + 0,
						x + maxX, y + 0, z + 0 + min,
						x + minX, y + 0, z + 0 + min
					)))
						return false;
				}
				if (negY && posX) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + 1 - min, y + 0, z + maxZ,
						x + 1 - min, y + 0, z + minZ,
						x + 1, y + 0 + min, z + minZ,
						x + 1, y + 0 + min, z + maxZ
					)))
						return false;
				}
				if (negY && negX) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + 0, y + 0 + min, z + maxZ,
						x + 0, y + 0 + min, z + minZ,
						x + 0 + min, y + 0, z + minZ,
						x + 0 + min, y + 0, z + maxZ
					)))
						return false;
				}
			}

			// Side chamfers
			{
				if (posX && posZ) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + 1 - min, y + 0 + minY, z + 1,
						x + 1, y + 0 + minY, z + 1 - min,
						x + 1, y + 0 + maxY, z + 1 - min,
						x + 1 - min, y + 0 + maxY, z + 1
					)))
						return false;
				}
				if (posX && negZ) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + 1 - min, y + 0 + maxY, z + 0,
						x + 1, y + 0 + maxY, z + 0 + min,
						x + 1, y + 0 + minY, z + 0 + min,
						x + 1 - min, y + 0 + minY, z + 0
					)))
						return false;
				}
				if (negX && posZ) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + 0 + min, y + 0 + maxY, z + 1,
						x + 0, y + 0 + maxY, z + 1 - min,
						x + 0, y + 0 + minY, z + 1 - min,
						x + 0 + min, y + 0 + minY, z + 1
					)))
						return false;
				}
				if (negX && negZ) {
					if (!action.apply(pos.set(x, y, z), face.set(
						x + 0 + min, y + 0 + minY, z + 0,
						x + 0, y + 0 + minY, z + 0 + min,
						x + 0, y + 0 + maxY, z + 0 + min,
						x + 0 + min, y + 0 + maxY, z + 0
					)))
						return false;
				}
			}
			return true;
		});
	}

}
