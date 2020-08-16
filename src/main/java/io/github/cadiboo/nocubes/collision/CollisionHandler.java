package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.util.ReusableCache;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.ArrayList;
import java.util.List;

public class CollisionHandler {

	private static final ReusableCache<boolean[][][]> COLLISIONS = new ReusableCache.Local<>();

	public static VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		if (!state.getBlock().canCollide)
			return VoxelShapes.empty();
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state))
			return state.getShape(reader, blockPos);
		if (state.cache != null && !NoCubes.smoothableHandler.isSmoothable(state))
			return state.cache.collisionShape;
		final int x = blockPos.getX();
		final int y = blockPos.getY();
		final int z = blockPos.getZ();
		List<VoxelShape> shapes = new ArrayList<>();
		SurfaceNets.generate(
			x, y, z,
			1, 1, 1,
			reader, state1 -> NoCubes.smoothableHandler.isSmoothable(state1) && state1.getBlock().canCollide, COLLISIONS,
			(pos, face, normal, averageNormal, direction) -> {
				Vec v0 = face.v0;
				Vec v1 = face.v1;
				Vec v2 = face.v2;
				Vec v3 = face.v3;
				Vec centre = Vec.of(
					(v0.x + v1.x + v2.x + v3.x) / 4,
					(v0.y + v1.y + v2.y + v3.y) / 4,
					(v0.z + v1.z + v2.z + v3.z) / 4
				);
				averageNormal.multiply(-0.125d);
				shapes.add(makeShape(0, 0, 0, centre, averageNormal, v0));
				shapes.add(makeShape(0, 0, 0, centre, averageNormal, v1));
				shapes.add(makeShape(0, 0, 0, centre, averageNormal, v2));
				shapes.add(makeShape(0, 0, 0, centre, averageNormal, v3));
				centre.close();
				return true;
			}
		);
		return shapes
			.stream()
			.reduce((a, b) -> VoxelShapes.combine(a, b, IBooleanFunction.OR))
			.orElse(VoxelShapes.empty());
	}

	public static VoxelShape makeShape(int currX, int currY, int currZ, Vec centre, Vec nAverage, Vec v) {
		double w = centre.x - v.x;
		if (-0.01 < w && w < 0.01)
			w = 0.0625 * nAverage.x;
		double h = centre.y - v.y;
		if (-0.01 < h && h < 0.01)
			h = 0.0625 * nAverage.y;
		double l = centre.z - v.z;
		if (-0.01 < l && l < 0.01)
			l = 0.0625 * nAverage.z;
		return VoxelShapes.create(
			currX + v.x, currY + v.y, currZ + v.z,
			currX + v.x + w, currY + v.y + h, currZ + v.z + l
		);
	}
}
