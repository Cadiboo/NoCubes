package io.github.cadiboo.nocubes.debug.client.render;

import com.google.common.collect.Lists;
import io.github.cadiboo.nocubes.client.render.OldNoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DebugOldNoCubes implements IDebugRenderAlgorithm {

	@Nonnull
	@Override
	public List<Vec3> getVertices(final BlockPos pos, final IBlockAccess world) {

		if (!ModUtil.shouldSmooth(world.getBlockState(pos))) {
			return Collections.emptyList();
		}

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		// The 8 points that make the block.
		// 1 point for each corner
		final List<Vec3> points = Lists.newArrayList(
				new Vec3(0, 0, 0),
				new Vec3(1, 0, 0),
				new Vec3(1, 0, 1),
				new Vec3(0, 0, 1),
				new Vec3(0, 1, 0),
				new Vec3(1, 1, 0),
				new Vec3(1, 1, 1),
				new Vec3(0, 1, 1)
		);

		// Loop through all the points:
		// Here everything will be 'smoothed'.
		for (int pointIndex = 0; pointIndex < 8; pointIndex++) {

			final Vec3 point = points.get(pointIndex);

			// Give the point the block's coordinates.
			point.xCoord += (double) x;
			point.yCoord += (double) y;
			point.zCoord += (double) z;

			// Check if the point is intersecting with a smoothable block.
			if (OldNoCubes.doesPointIntersectWithSmoothable(world, point)) {
				if (pointIndex < 4 && OldNoCubes.doesPointBottomIntersectWithAir(world, point)) {
					point.yCoord = (double) y + 1.0D;
				} else if (pointIndex >= 4 && OldNoCubes.doesPointTopIntersectWithAir(world, point)) {
					point.yCoord = (double) y;
				}

				if (ModConfig.offsetVertices) {
					OldNoCubes.givePointRoughness(point);
				}
			}
		}

		return points;

	}

	@Nonnull
	@Override
	public List<Face<Vec3>> getFaces(final BlockPos pos, final IBlockAccess world) {
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
				v0,
				v1,
				v2,
				v3
		));

		//up
		faces.add(new Face<>(
				v7,
				v6,
				v5,
				v4
		));

		//north
		faces.add(new Face<>(
				v1,
				v0,
				v4,
				v5
		));

		//south
		faces.add(new Face<>(
				v6,
				v7,
				v3,
				v2
		));

		//west
		faces.add(new Face<>(
				v0,
				v3,
				v7,
				v4
		));

		//east
		faces.add(new Face<>(
				v5,
				v6,
				v2,
				v1
		));

		return faces;
	}

}
