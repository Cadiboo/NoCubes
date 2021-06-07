package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.client.render.LightCache;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.function.Predicate;


/**
 * @author Cadiboo
 * @author Click_Me
 */
public final class OldNoCubes implements MeshGenerator {

	// Points order
	public static final int X0Y0Z0 = 0;
	public static final int X1Y0Z0 = 1;
	public static final int X1Y0Z1 = 2;
	public static final int X0Y0Z1 = 3;
	public static final int X0Y1Z0 = 4;
	public static final int X1Y1Z0 = 5;
	public static final int X1Y1Z1 = 6;
	public static final int X0Y1Z1 = 7;

	@Override
	public void generate(Area area, Predicate<BlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction) {
		try {
			generateOrThrow(area, isSmoothable, voxelAction, faceAction);
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			t.getCause();
		}
	}

	private static void resetPoints(Vec[] points) {
		// The 8 points that make the block.
		// 1 point for each corner
		points[0].set(0, 0, 0);
		points[1].set(1, 0, 0);
		points[2].set(1, 0, 1);
		points[3].set(0, 0, 1);
		points[4].set(0, 1, 0);
		points[5].set(1, 1, 0);
		points[6].set(1, 1, 1);
		points[7].set(0, 1, 1);
	}

	@Override
	public BlockPos getPositiveAreaExtension() {
		// Need data about the each block's direct neighbours to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public BlockPos getNegativeAreaExtension() {
		// Need data about the each block's direct neighbours to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	public void generateOrThrow(Area area, Predicate<BlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction) {
		BlockPos size = area.size;
		int depth = size.getZ();
		int height = size.getY();
		int width = size.getX();

		BlockState[] blocks = area.getAndCacheBlocks();
		BlockPos.Mutable pos = new BlockPos.Mutable();

		Face face = new Face();
		// The 8 points that make the block.
		// 1 point for each corner
		Vec[] points = {new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec()};

//		float roughness = NoCubesConfig.Server.oldNoCubesRoughness;
		float roughness = 0.5F;

		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					if (isOutsideMesh(x, y, z, size))
						continue;

					if (!isSmoothable.test(blocks[index]))
						// We aren't smoothable
						continue;

					resetPoints(points);
					// Loop through all the points:
					// Here everything will be 'smoothed'.
					for (int pointIndex = 0; pointIndex < 8; ++pointIndex) {
						Vec point = points[pointIndex];

						// Give the point the block's coordinates.
						point.x += x;
						point.y += y;
						point.z += z;

						if (!doesPointIntersectWithManufactured(area, point, isSmoothable, pos)) {
							if (pointIndex < 4 && doesPointBottomIntersectWithAir(area, point, pos))
								point.y = y + 1.0F - 0.0001F; // - 0.0001F to prevent z-fighting
							else if (pointIndex >= 4 && doesPointTopIntersectWithAir(area, point, pos))
								point.y = y + 0.0F + 0.0001F; // + 0.0001F to prevent z-fighting
							givePointRoughness(roughness, area, point);
						}
					}

					float max = max(points[0].y, points[1].y, points[2].y, points[3].y, points[4].y, points[5].y, points[6].y, points[7].y);
					float min = min(points[0].y, points[1].y, points[2].y, points[3].y, points[4].y, points[5].y, points[6].y, points[7].y);
					float amountInside = (max - min - 0.5F) * 2;
					if (!voxelAction.apply(pos.set(x, y, z), amountInside))
						return;

					Direction[] directions = ModUtil.DIRECTIONS;
					for (int i = 0, l = directions.length; i < l; ++i) {
						Direction direction = directions[i];
						pos.set(x, y, z).move(direction);

						if (isSmoothable.test(blocks[area.index(pos)]))
							continue;

						//0-3
						//1-2
						//0,0-1,0
						//0,1-1,1
						switch (direction) {
							default:
							case DOWN:
								face.v0.set(points[X0Y0Z1]);
								face.v1.set(points[X0Y0Z0]);
								face.v2.set(points[X1Y0Z0]);
								face.v3.set(points[X1Y0Z1]);
								break;
							case UP:
								face.v0.set(points[X0Y1Z0]);
								face.v1.set(points[X0Y1Z1]);
								face.v2.set(points[X1Y1Z1]);
								face.v3.set(points[X1Y1Z0]);
								break;
							case NORTH:
								face.v0.set(points[X1Y1Z0]);
								face.v1.set(points[X1Y0Z0]);
								face.v2.set(points[X0Y0Z0]);
								face.v3.set(points[X0Y1Z0]);
								break;
							case SOUTH:
								face.v0.set(points[X0Y1Z1]);
								face.v1.set(points[X0Y0Z1]);
								face.v2.set(points[X1Y0Z1]);
								face.v3.set(points[X1Y1Z1]);
								break;
							case WEST:
								face.v0.set(points[X0Y1Z0]);
								face.v1.set(points[X0Y0Z0]);
								face.v2.set(points[X0Y0Z1]);
								face.v3.set(points[X0Y1Z1]);
								break;
							case EAST:
								face.v0.set(points[X1Y1Z1]);
								face.v1.set(points[X1Y0Z1]);
								face.v2.set(points[X1Y0Z0]);
								face.v3.set(points[X1Y1Z0]);
								break;
						}

						if (!faceAction.apply(pos, face))
							return;

					}

				}
			}
		}
	}

	private static float max(float a, float b, float c, float d, float e, float f, float g, float h) {
		float max = a;
		if (b > max)
			max = b;
		if (c > max)
			max = c;
		if (d > max)
			max = d;
		if (e > max)
			max = e;
		if (f > max)
			max = f;
		if (g > max)
			max = g;
		if (h > max)
			max = h;
		return max;
	}

	private static float min(float a, float b, float c, float d, float e, float f, float g, float h) {
		float min = a;
		if (b < min)
			min = b;
		if (c < min)
			min = c;
		if (d < min)
			min = d;
		if (e < min)
			min = e;
		if (f < min)
			min = f;
		if (g < min)
			min = g;
		if (h < min)
			min = h;
		return min;
	}

	public static void givePointRoughness(float roughness, Area area, Vec point) {
		double worldX = area.start.getX() + point.x;
		double worldY = area.start.getY() + point.y;
		double worldZ = area.start.getZ() + point.z;
		long i = (long) (worldX * 3129871d) ^ (long) worldY * 116129781L ^ (long) worldZ;

		i = i * i * 42317861L + i * 11L;
		point.x += ((float) (i >> 16 & 0xF) / 15.0F - 0.5F) * roughness;
		point.y += ((float) (i >> 20 & 0xF) / 15.0F - 0.5F) * roughness;
		point.z += ((float) (i >> 24 & 0xF) / 15.0F - 0.5F) * roughness;
	}

	/**
	 * Check if the state is AIR or PLANT or VINE
	 *
	 * @param state the state
	 * @return if the state is AIR or PLANT or VINE
	 */
	public static boolean isBlockAirOrPlant(BlockState state) {
		Material material = state.getMaterial();
		return material == Material.AIR ||
			material == Material.PLANT ||
			material == Material.WATER_PLANT ||
			material == Material.REPLACEABLE_PLANT ||
			material == Material.REPLACEABLE_FIREPROOF_PLANT ||
			material == Material.REPLACEABLE_WATER_PLANT ||
			material == Material.BAMBOO_SAPLING ||
			material == Material.BAMBOO ||
			material == Material.VEGETABLE;
	}

	public static boolean doesPointTopIntersectWithAir(Area area, Vec point, BlockPos.Mutable pos) {
		boolean intersects = false;
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			if (!isBlockAirOrPlant(area.getBlockState(pos.set(x, y, z))))
				return false;
			if (isBlockAirOrPlant(area.getBlockState(pos.set(x, y - 1, z))))
				intersects = true;
		}
		return intersects;
	}

	public static boolean doesPointBottomIntersectWithAir(Area area, Vec point, BlockPos.Mutable pos) {
		boolean intersects = false;
		boolean notOnly = false;
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			if (!isBlockAirOrPlant(area.getBlockState(pos.set(x, y - 1, z))))
				return false;
			if (!isBlockAirOrPlant(area.getBlockState(pos.set(x, y + 1, z))))
				notOnly = true;
			if (isBlockAirOrPlant(area.getBlockState(pos.set(x, y, z))))
				intersects = true;
		}
		return intersects && notOnly;
	}

	public static boolean doesPointIntersectWithManufactured(Area area, Vec point, Predicate<BlockState> isSmoothable, BlockPos.Mutable pos) {
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			BlockState state0 = area.getBlockState(pos.set(x, y, z));
			if (!isBlockAirOrPlant(state0) && !isSmoothable.test(state0))
				return true;
			BlockState state1 = area.getBlockState(pos.set(x, y - 1, z));
			if (!isBlockAirOrPlant(state1) && !isSmoothable.test(state1))
				return true;
		}
		return false;
	}

}
