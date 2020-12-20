package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.util.Face;
import io.github.cadiboo.nocubes.client.render.util.ReusableCache;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import java.util.ArrayList;
import java.util.List;

public class CollisionHandler {

	private static final ReusableCache<boolean[]> COLLISION = new ReusableCache.Local<>();
	private static final ReusableCache<CollisionCreationData> DATA = new ReusableCache.Local<>();

	public static VoxelShape getCollisionShape(boolean canCollide, BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		try {
			return getCollisionShapeOrThrow(canCollide, state, reader, blockPos, context);
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			return canCollide ? VoxelShapes.fullCube() : VoxelShapes.empty();
		}
	}

	// TODO: Why is the 'cache' of every blockstate storing an empty VoxelShape... this is causing issues like
	// grass paths turning to dirt causing a crash because dirt's VoxelShape is empty
	// and not being able to place snow anywhere ('Block.doesSideFillSquare' is returning false for a flat area of stone)
	public static VoxelShape getCollisionShapeOrThrow(boolean canCollide, BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		if (!canCollide)
			return VoxelShapes.empty();
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state))
			return state.getShape(reader, blockPos);
		if (context.getEntity() instanceof FallingBlockEntity)
			// Stop sand etc. breaking when it falls
			return state.getShape(reader, blockPos);
		if (reader.getBlockState(blockPos) != state)
			// Stop grass path turning to dirt causing a crash from trying to turn an empty VoxelShape into an AABB
			return state.getShape(reader, blockPos);
		List<VoxelShape> shapes = new ArrayList<>();
		SurfaceNets.generate(
			blockPos.getX(), blockPos.getY(), blockPos.getZ(),
			1, 1, 1,
			reader, NoCubes.smoothableHandler::isSmoothable, COLLISION,
			(pos, face) -> {
				CollisionCreationData data = DATA.getOrCreate(CollisionCreationData::new);
				Face normal = data.normal;
				Vec centre = data.centre;
				Vec averageOfNormal = data.averageOfNormal;

				face.assignNormalTo(normal);
				face.assignAverageTo(centre);

				normal.assignAverageTo(averageOfNormal);
				averageOfNormal.normalise().multiply(0.125d);

				shapes.add(makeShape(0, 0, 0, centre, averageOfNormal, face.v0));
				shapes.add(makeShape(0, 0, 0, centre, averageOfNormal, face.v1));
				shapes.add(makeShape(0, 0, 0, centre, averageOfNormal, face.v2));
				shapes.add(makeShape(0, 0, 0, centre, averageOfNormal, face.v3));
				return true;
			}
		);
		return shapes
			.stream()
			.reduce((a, b) -> VoxelShapes.combine(a, b, IBooleanFunction.OR))
			.orElse(VoxelShapes.empty());
	}

	public static VoxelShape makeShape(int currX, int currY, int currZ, Vec centre, Vec averageOfNormal, Vec v) {
		double w = centre.x - v.x;
		if (-0.01 < w && w < 0.01)
			w = 0.0625 * averageOfNormal.x;
		double h = centre.y - v.y;
		if (-0.01 < h && h < 0.01)
			h = 0.0625 * averageOfNormal.y;
		double l = centre.z - v.z;
		if (-0.01 < l && l < 0.01)
			l = 0.0625 * averageOfNormal.z;
		v.add(currX, currY, currZ);
		return VoxelShapes.create(
			v.x, v.y, v.z,
			v.x + w, v.y + h, v.z + l
		);
	}

	static class CollisionCreationData {
		final Face normal = new Face(new Vec(), new Vec(), new Vec(), new Vec());
		final Vec averageOfNormal = new Vec();
		final Vec centre = new Vec();
	}

}
