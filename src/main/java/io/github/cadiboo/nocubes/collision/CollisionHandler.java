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

	private static final ReusableCache<float[]> COLLISIONS = new ReusableCache.Local<>();

	public static VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		if (state.cache != null)
			return state.cache.collisionShape;
		if (!state.getBlock().canCollide)
			return VoxelShapes.empty();
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state))
			return state.getShape(reader, blockPos);
		final int x = blockPos.getX();
		final int y = blockPos.getY();
		final int z = blockPos.getZ();
		List<VoxelShape> shapes = new ArrayList<>();
		SurfaceNets.generate(
			x, y, z,
			1, 1, 1,
			reader, state1 -> NoCubes.smoothableHandler.isSmoothable(state1) && state1.getBlock().canCollide, COLLISIONS,
			(pos, face, normal, direction) -> {
				Vec v0 = face.v0;
				Vec v1 = face.v1;
				Vec v2 = face.v2;
				Vec v3 = face.v3;
				// Normals
				Vec n0 = Vec.normal(v3, v0, v1).multiply(-1);
				Vec n1 = Vec.normal(v0, v1, v2).multiply(-1);
				Vec n2 = Vec.normal(v1, v2, v3).multiply(-1);
				Vec n3 = Vec.normal(v2, v3, v0).multiply(-1);

				Vec centre = Vec.of(
					(v0.x + v1.x + v2.x + v3.x) / 4,
					(v0.y + v1.y + v2.y + v3.y) / 4,
					(v0.z + v1.z + v2.z + v3.z) / 4
				);

				final Vec nAverage = Vec.of(
					(n0.x + n2.x) / 2,
					(n0.y + n2.y) / 2,
					(n0.z + n2.z) / 2
				);
				nAverage.normalise().multiply(-0.125d);

				shapes.add(makeShape(0, 0, 0, centre, nAverage, v0));
				shapes.add(makeShape(0, 0, 0, centre, nAverage, v1));
				shapes.add(makeShape(0, 0, 0, centre, nAverage, v2));
				shapes.add(makeShape(0, 0, 0, centre, nAverage, v3));
//				shapes.add(makeShape(0, 0, 0, centre, nAverage, centre));
				n0.close();
				n1.close();
				n2.close();
				n3.close();
				nAverage.close();
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
//		v.add(nAverage);
		double w = centre.x - v.x;
		if (-0.01 < w && w < 0.01)
			w = 0.0625 * nAverage.x;
		double h = centre.y - v.y;
		if (-0.01 < h && h < 0.01)
			h = 0.0625 * nAverage.y;
		double l = centre.z - v.z;
		if (-0.01 < l && l < 0.01)
			l = 0.0625 * nAverage.z;
		v.add(currX, currY, currZ);
		return VoxelShapes.create(
			v.x, v.y, v.z,
			v.x + w, v.y + h, v.z + l
		);
	}
}
