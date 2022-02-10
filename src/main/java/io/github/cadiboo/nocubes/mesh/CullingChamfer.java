package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.function.Predicate;

public class CullingChamfer implements Mesher {

	@Override
	public Vec3i getPositiveAreaExtension() {
		// Need data about the area's direct neighbour blocks blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public Vec3i getNegativeAreaExtension() {
		// Need data about the area's direct neighbour blocks blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public void generateOrThrow(Area area, Predicate<BlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction) {
		var size = area.size;
		int depth = size.getZ();
		int height = size.getY();
		int width = size.getX();

		final float min = 0.25F;
		final float max = 1F - min;

		var blocks = area.getAndCacheBlocks();
		var pos = new BlockPos.MutableBlockPos();
		var face = new Face();
		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					if (isOutsideMesh(x, y, z, size))
						continue;

					boolean smoothable = isSmoothable.test(blocks[index]);
					if (!voxelAction.apply(pos.set(x, y, z), smoothable ? 1 : 0))
						return;
					if (!smoothable)
						// We aren't smoothable
						continue;

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
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + maxX, y + 1, z + maxZ,
							x + maxX, y + 1, z + minZ,
							x + minX, y + 1, z + minZ,
							x + minX, y + 1, z + maxZ
						)))
							return;
					}

					// Down (neg y)
					if (negY) {
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + maxX, y + 0, z + maxZ,
							x + minX, y + 0, z + maxZ,
							x + minX, y + 0, z + minZ,
							x + maxX, y + 0, z + minZ
						)))
							return;
					}

					// South (pos z)
					if (posZ) {
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + maxX, y + maxY, z + 1,
							x + minX, y + maxY, z + 1,
							x + minX, y + minY, z + 1,
							x + maxX, y + minY, z + 1
						)))
							return;
					}

					// North (neg z)
					if (negZ) {
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + maxX, y + maxY, z + 0,
							x + maxX, y + minY, z + 0,
							x + minX, y + minY, z + 0,
							x + minX, y + maxY, z + 0
						)))
							return;
					}

					// East (pos x)
					if (posX) {
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + 1, y + maxY, z + maxZ,
							x + 1, y + minY, z + maxZ,
							x + 1, y + minY, z + minZ,
							x + 1, y + maxY, z + minZ
						)))
							return;
					}

					// West (neg x)
					if (negX) {
						if (!faceAction.apply(pos.set(x, y, z), face.set(
							x + 0, y + maxY, z + maxZ,
							x + 0, y + maxY, z + minZ,
							x + 0, y + minY, z + minZ,
							x + 0, y + minY, z + maxZ
						)))
							return;
					}

					// Top chamfers
					{
						if (posY && posZ) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + minX, y + 1 - min, z + 1,
								x + maxX, y + 1 - min, z + 1,
								x + maxX, y + 1, z + 1 - min,
								x + minX, y + 1, z + 1 - min
							)))
								return;
						}
						if (posY && negZ) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + minX, y + 1, z + 0 + min,
								x + maxX, y + 1, z + 0 + min,
								x + maxX, y + 1 - min, z + 0,
								x + minX, y + 1 - min, z + 0
							)))
								return;
						}
						if (posY && posX) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + 1, y + 1 - min, z + maxZ,
								x + 1, y + 1 - min, z + minZ,
								x + 1 - min, y + 1, z + minZ,
								x + 1 - min, y + 1, z + maxZ
							)))
								return;
						}
						if (posY && negX) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + 0 + min, y + 1, z + maxZ,
								x + 0 + min, y + 1, z + minZ,
								x + 0, y + 1 - min, z + minZ,
								x + 0, y + 1 - min, z + maxZ
							)))
								return;
						}
					}

					// Bottom chamfers
					{
						if (negY && posZ) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + minX, y + 0, z + 1 - min,
								x + maxX, y + 0, z + 1 - min,
								x + maxX, y + 0 + min, z + 1,
								x + minX, y + 0 + min, z + 1
							)))
								return;
						}
						if (negY && negZ) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + minX, y + 0 + min, z + 0,
								x + maxX, y + 0 + min, z + 0,
								x + maxX, y + 0, z + 0 + min,
								x + minX, y + 0, z + 0 + min
							)))
								return;
						}
						if (negY && posX) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + 1 - min, y + 0, z + maxZ,
								x + 1 - min, y + 0, z + minZ,
								x + 1, y + 0 + min, z + minZ,
								x + 1, y + 0 + min, z + maxZ
							)))
								return;
						}
						if (negY && negX) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + 0, y + 0 + min, z + maxZ,
								x + 0, y + 0 + min, z + minZ,
								x + 0 + min, y + 0, z + minZ,
								x + 0 + min, y + 0, z + maxZ
							)))
								return;
						}
					}

					// Side chamfers
					{
						if (posX && posZ) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + 1 - min, y + 0 + minY, z + 1,
								x + 1, y + 0 + minY, z + 1 - min,
								x + 1, y + 0 + maxY, z + 1 - min,
								x + 1 - min, y + 0 + maxY, z + 1
							)))
								return;
						}
						if (posX && negZ) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + 1 - min, y + 0 + maxY, z + 0,
								x + 1, y + 0 + maxY, z + 0 + min,
								x + 1, y + 0 + minY, z + 0 + min,
								x + 1 - min, y + 0 + minY, z + 0
							)))
								return;
						}
						if (negX && posZ) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + 0 + min, y + 0 + maxY, z + 1,
								x + 0, y + 0 + maxY, z + 1 - min,
								x + 0, y + 0 + minY, z + 1 - min,
								x + 0 + min, y + 0 + minY, z + 1
							)))
								return;
						}
						if (negX && negZ) {
							if (!faceAction.apply(pos.set(x, y, z), face.set(
								x + 0 + min, y + 0 + minY, z + 0,
								x + 0, y + 0 + minY, z + 0 + min,
								x + 0, y + 0 + maxY, z + 0 + min,
								x + 0 + min, y + 0 + maxY, z + 0
							)))
								return;
						}
					}
				}
			}
		}
	}

}
