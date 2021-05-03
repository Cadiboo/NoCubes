package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IBlockReader;

public final class CollisionHandler {

	public static VoxelShape getCollisionShape(boolean canCollide, BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		try {
			return getCollisionShapeOrThrow(canCollide, state, reader, blockPos, context);
		} catch (Throwable t) {
			if (!ModUtil.IS_DEVELOPER_WORKSPACE.get())
				throw t;
			return canCollide ? VoxelShapes.block() : VoxelShapes.empty();
		}
	}

	// TODO: Why is the 'cache' of every blockstate storing an empty VoxelShape... this is causing issues like
	// grass paths turning to dirt causing a crash because dirt's VoxelShape is empty
	// and not being able to place snow anywhere ('Block.doesSideFillSquare' is returning false for a flat area of stone)
	public static VoxelShape getCollisionShapeOrThrow(boolean canCollide, BlockState state, IBlockReader reader, BlockPos blockPos, ISelectionContext context) {
		if (!canCollide)
			return VoxelShapes.empty();
		if (!NoCubesConfig.Server.collisionsEnabled || !NoCubes.smoothableHandler.isSmoothable(state))
			return state.getShape(reader, blockPos);
		if (context.getEntity() instanceof FallingBlockEntity)
			// Stop sand etc. breaking when it falls
			return state.getShape(reader, blockPos);
		if (context.getEntity() == null)
			// Stop grass path turning to dirt causing a crash from trying to turn an empty VoxelShape into an AABB
			return state.getShape(reader, blockPos);

		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		Vector3i negativeAreaExtension = generator.getNegativeAreaExtension();
		BlockPos start = blockPos.subtract(negativeAreaExtension);
		BlockPos end = blockPos.offset(1, 1, 1).offset(generator.getPositiveAreaExtension());
		VoxelShape[] ref = {VoxelShapes.empty()};
		try (Area area = new Area(reader, start, end)) {
			new OOCollisionHandler(generator).generate(area, ((x0, y0, z0, x1, y1, z1) -> {
				VoxelShape shape = VoxelShapes.box(
					x0 - negativeAreaExtension.getX(), y0 - negativeAreaExtension.getY(), z0 - negativeAreaExtension.getZ(),
					x1 - negativeAreaExtension.getX(), y1 - negativeAreaExtension.getY(), z1 - negativeAreaExtension.getZ()
				);
				ref[0] = VoxelShapes.joinUnoptimized(ref[0], shape, IBooleanFunction.OR);
			}));
			return ref[0];//.optimize();
		}
	}

//	static class CollisionCreationData {
//		final Face normal = new Face(new Vec(), new Vec(), new Vec(), new Vec());
//		final Vec averageOfNormal = new Vec();
//		final Vec centre = new Vec();
//	}

}
