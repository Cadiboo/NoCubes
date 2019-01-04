package io.github.cadiboo.nocubes.debug.client.render;

import io.github.cadiboo.nocubes.client.render.OldNoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.cadiboo.nocubes.client.render.OldNoCubes.isBlockAirOrPlant;

public class DebugOldNoCubes implements IDebugRenderAlgorithm {

	@Nonnull
	@Override
	public List<Vec3> getVertices(final PooledMutableBlockPos pos, final IBlockAccess world) {

		if (!ModUtil.shouldSmooth(world.getBlockState(pos))) {
			return Collections.emptyList();
		}

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		// The 8 points that make the block.
		// 1 point for each corner
		final List<Vec3> points = ModUtil.getBlankVertexList();

		// Loop through all the points:
		// Here everything will be 'smoothed'.
		for (int pointIndex = 0; pointIndex < 8; pointIndex++) {

			final Vec3 point = points.get(pointIndex);

			// Give the point the block's coordinates.
			point.x += (double) x;
			point.y += (double) y;
			point.z += (double) z;

			// Check if the point is intersecting with a smoothable block.
			if (doesPointIntersectWithSmoothable(world, pos.setPos(point.x, point.y, point.z))) {
				if (pointIndex < 4 && doesPointBottomIntersectWithAir(world, pos.setPos(point.x, point.y, point.z))) {
					point.y = (double) y + 1.0D;
				} else if (pointIndex >= 4 && doesPointTopIntersectWithAir(world, pos.setPos(point.x, point.y, point.z))) {
					point.y = (double) y;
				}

				if (ModConfig.offsetVertices) {
					OldNoCubes.givePointRoughness(point);
				}
			}

			point.x -= (double) x;
			point.y -= (double) y;
			point.z -= (double) z;

		}

		return points;

	}

	@Nonnull
	@Override
	public List<Face<Vec3>> getFaces(final PooledMutableBlockPos pos, final IBlockAccess world) {
		final ArrayList<Face<Vec3>> faces = new ArrayList<>();

		final List<Vec3> vertices = getVertices(pos, world);

		if (vertices.isEmpty()) {
			return Collections.emptyList();
		}

		final Vec3 v0 = vertices.get(0);
		final Vec3 v1 = vertices.get(1);
		final Vec3 v2 = vertices.get(2);
		final Vec3 v3 = vertices.get(3);
		final Vec3 v4 = vertices.get(4);
		final Vec3 v5 = vertices.get(5);
		final Vec3 v6 = vertices.get(6);
		final Vec3 v7 = vertices.get(7);

		//down
		faces.add(new Face<>(
				v3,
				v0,
				v1,
				v2
		));

		//up
		faces.add(new Face<>(
				v4,
				v7,
				v6,
				v5
		));

		//north
		faces.add(new Face<>(
				v5,
				v1,
				v0,
				v4
		));

		//south
		faces.add(new Face<>(
				v7,
				v3,
				v2,
				v6
		));

		//west
		faces.add(new Face<>(
				v4,
				v0,
				v3,
				v7
		));

		//east
		faces.add(new Face<>(
				v6,
				v2,
				v1,
				v5
		));

		return faces;
	}

	/**
	 * Check if the block's top side intersects with air.
	 *
	 * @param world the world
	 * @param pos   the pos
	 * @return if the block's top side intersects with air.
	 */
	public static boolean doesPointTopIntersectWithAir(IBlockAccess world, PooledMutableBlockPos pos) {
		boolean intersects = false;

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (x - (double) (i & 1));
			int z1 = (int) (z - (double) (i >> 1 & 1));
			if (!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) y, z1)))) {
				return false;
			}

			if (isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) y - 1, z1)))) {
				intersects = true;
			}
		}

		return intersects;
	}

	/**
	 * Check if the block's bottom side intersects with air.
	 *
	 * @param world the world
	 * @param pos   the pos
	 * @return if the block's bottom side intersects with air.
	 */
	public static boolean doesPointBottomIntersectWithAir(IBlockAccess world, PooledMutableBlockPos pos) {
		boolean intersects = false;
		boolean notOnly = false;

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (x - (double) (i & 1));
			int z1 = (int) (z - (double) (i >> 1 & 1));
			if (!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) y - 1, z1)))) {
				return false;
			}

			if (!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) y + 1, z1)))) {
				notOnly = true;
			}

			if (isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) y, z1)))) {
				intersects = true;
			}
		}

		return intersects && notOnly;
	}

	/**
	 * Check if the point is intersecting with a smoothable block.
	 *
	 * @param world the world
	 * @param pos   the pos
	 * @return if the point is intersecting with a smoothable block.
	 */
	public static boolean doesPointIntersectWithSmoothable(IBlockAccess world, PooledMutableBlockPos pos) {

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (x - (double) (i & 1));
			int z1 = (int) (y - (double) (i >> 1 & 1));
			IBlockState block = world.getBlockState(new BlockPos(x1, (int) y, z1));
			if (!isBlockAirOrPlant(block) && !ModUtil.shouldSmooth(block)) {
				return false;
			}

			IBlockState block1 = world.getBlockState(new BlockPos(x1, (int) y - 1, z1));
			if (!isBlockAirOrPlant(block1) && !ModUtil.shouldSmooth(block1)) {
				return false;
			}
		}
		return true;
	}

}
