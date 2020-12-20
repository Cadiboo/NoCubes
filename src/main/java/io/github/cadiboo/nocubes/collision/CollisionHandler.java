package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.util.Face;
import io.github.cadiboo.nocubes.client.render.util.ReusableCache;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.ArrayList;
import java.util.List;

import static io.github.cadiboo.nocubes.client.render.OverlayRenderer.makeShape;

public class CollisionHandler {

	private static final ReusableCache<boolean[]> COLLISION = new ReusableCache.Local<>();

	public static VoxelShape getCollisionShape(boolean canCollide, BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		try {
			return getCollisionShapeOrThrow(canCollide, state, reader, blockPos, context);
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			return VoxelShapes.empty();
		}
	}

	public static VoxelShape getCollisionShapeOrThrow(boolean canCollide, BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		if (!canCollide)
			return VoxelShapes.empty();
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state))
			return state.getShape(reader, blockPos);
		if (context.getEntity() instanceof FallingBlockEntity)
			// Stop sand etc. breaking when it falls
			return state.getShape(reader, blockPos);
		final int x = blockPos.getX();
		final int y = blockPos.getY();
		final int z = blockPos.getZ();
//		final Face normal = new Face(new Vec(), new Vec(), new Vec(), new Vec());
//		final Vec averageOfNormal = new Vec();
//		final Vec centre = new Vec();
		List<VoxelShape> shapes = new ArrayList<>();
		SurfaceNets.generate(
			x, y, z,
			1, 1, 1,
			reader, NoCubes.smoothableHandler::isSmoothable, COLLISION,
			(pos, face) -> {
				Vec v0 = face.v0;
				Vec v1 = face.v1;
				Vec v2 = face.v2;
				Vec v3 = face.v3;

//				face.assignNormalTo(normal);
//				normal.multiply(-1);
//				face.assignAverageTo(centre);

//				normal.assignAverageTo(averageOfNormal);
//				averageOfNormal.normalise().multiply(-0.125d);

				// Normals
				Vec n0 = Vec.normal(v3, v0, v1, new Vec()).multiply(-1);
				Vec n1 = Vec.normal(v0, v1, v2, new Vec()).multiply(-1);
				Vec n2 = Vec.normal(v1, v2, v3, new Vec()).multiply(-1);
				Vec n3 = Vec.normal(v2, v3, v0, new Vec()).multiply(-1);

				Vec centre = new Vec(
					(v0.x + v1.x + v2.x + v3.x) / 4,
					(v0.y + v1.y + v2.y + v3.y) / 4,
					(v0.z + v1.z + v2.z + v3.z) / 4
				);

				final Vec nAverage = new Vec(
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
				return true;
			}
		);
		return shapes
			.stream()
			.reduce((a, b) -> VoxelShapes.combine(a, b, IBooleanFunction.OR))
			.orElse(VoxelShapes.empty());
	}

}
