package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.util.ModUtil.DIRECTION_VALUES;
import static io.github.cadiboo.nocubes.util.ModUtil.DIRECTION_VALUES_LENGTH;

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

	private void resetPoints(Vec[] points) {
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

	@Override
	public void generate(Area area, Predicate<IBlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction) {
		// The 8 points that make the block.
		// 1 point for each corner
		Vec[] points = { new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec(), new Vec()};
		resetPoints(points);
		Face face = new Face();
		IBlockAccess world = area.world;

		for (MutableBlockPos pos : BlockPos.getAllInBoxMutable(area.start, area.start.add(area.size).add(-1, -1, -1))) {
			IBlockState state = world.getBlockState(pos);

			int y = pos.getY();
			int x = pos.getX();
			int z = pos.getZ();

			boolean smoothable = isSmoothable.test(state);
			if (!voxelAction.apply(pos, smoothable ? 1 : 0))
				return;
			if (!smoothable)
				continue;

			// Loop through all the points:
			// Here everything will be 'smoothed'.
			for (int pointIndex = 0; pointIndex < 8; ++pointIndex) {
				Vec point = points[pointIndex];

				// Give the point the block's coordinates.
				point.x += x;
				point.y += y;
				point.z += z;

				if (!doesPointIntersectWithManufactured(world, point, isSmoothable, pos)) {
					if ((pointIndex < 4) && (doesPointBottomIntersectWithAir(world, point, pos)))
						point.y = y + 1.0F - 0.0001F; // - 0.0001F to prevent z-fighting
					else if ((pointIndex >= 4) && (doesPointTopIntersectWithAir(world, point, pos)))
						point.y = y + 0.0F + 0.0001F; // + 0.0001F to prevent z-fighting
//					if (ModConfig.offsetVertices)
//						givePointRoughness(point);

				}
			}

			for (int i = 0; i < DIRECTION_VALUES_LENGTH; ++i) {
				final EnumFacing direction = DIRECTION_VALUES[i];
				if (isSmoothable.test(world.getBlockState(pos.setPos(x, y, z).move(direction))))
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

	/**
	 * Click_Me's equivalent of our ModUtil.offsetVertex
	 */
	@Nonnull
	public static Vec givePointRoughness(@Nonnull final Vec point) {
		long i = (long) (point.x * 3129871.0D) ^ (long) point.y * 116129781L ^ (long) point.z;

		i = i * i * 42317861L + i * 11L;
		point.x += ((float) (i >> 16 & 0xF) / 15.0F - 0.5F) * 0.5F;
		point.y += ((float) (i >> 20 & 0xF) / 15.0F - 0.5F) * 0.5F;
		point.z += ((float) (i >> 24 & 0xF) / 15.0F - 0.5F) * 0.5F;
		return point;
	}

	/**
	 * Check if the state is AIR or PLANT or VINE
	 *
	 * @param state the state
	 * @return if the state is AIR or PLANT or VINE
	 */
	public static boolean isBlockAirOrPlant(@Nonnull final IBlockState state) {
		Material material = state.getMaterial();
		return (material == Material.AIR) || (material == Material.PLANTS) || (material == Material.VINE);
	}

	/**
	 * Check if the block's top side intersects with air.
	 *
	 * @param world                 the world to use
	 * @param point                 the point to use
	 * @param pooledMutableBlockPos the pooled mutable pos to use
	 * @return if the block's top side intersects with air.
	 */
	public static boolean doesPointTopIntersectWithAir(@Nonnull final IBlockAccess world, @Nonnull final Vec point, @Nonnull final MutableBlockPos pooledMutableBlockPos) {
		boolean intersects = false;
		for (int i = 0; i < 4; i++) {
			int x1 = (int) (point.x - (i & 0x1));
			int z1 = (int) (point.z - (i >> 1 & 0x1));
			if (!isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y, z1)))) {
				return false;
			}
			if (isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y - 1, z1)))) {
				intersects = true;
			}
		}
		return intersects;
	}

	/**
	 * Check if the block's bottom side intersects with air.
	 *
	 * @param world                 the world to use
	 * @param point                 the point to use
	 * @param pooledMutableBlockPos the pooled mutable pos to use
	 * @return if the block's bottom side intersects with air.
	 */
	public static boolean doesPointBottomIntersectWithAir(@Nonnull final IBlockAccess world, @Nonnull final Vec point, @Nonnull final MutableBlockPos pooledMutableBlockPos) {
		boolean intersects = false;
		boolean notOnly = false;
		for (int i = 0; i < 4; i++) {
			int x1 = (int) (point.x - (i & 0x1));
			int z1 = (int) (point.z - (i >> 1 & 0x1));
			if (!isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y - 1, z1)))) {
				return false;
			}
			if (!isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y + 1, z1)))) {
				notOnly = true;
			}
			if (isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y, z1)))) {
				intersects = true;
			}
		}
		return (intersects) && (notOnly);
	}

	public static boolean doesPointIntersectWithManufactured(@Nonnull final IBlockAccess world, @Nonnull final Vec point, @Nonnull final Predicate<IBlockState> isSmoothable, @Nonnull final MutableBlockPos pooledMutableBlockPos) {
		for (int i = 0; i < 4; i++) {
			int x1 = (int) (point.x - (i & 0x1));
			int z1 = (int) (point.z - (i >> 1 & 0x1));
			IBlockState state0 = world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y, z1));
			if ((!isBlockAirOrPlant(state0)) && (!isSmoothable.test(state0))) {
				return true;
			}
			IBlockState state1 = world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y - 1, z1));
			if ((!isBlockAirOrPlant(state1)) && (!isSmoothable.test(state1))) {
				return true;
			}
		}
		return false;
	}

}
