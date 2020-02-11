package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

/**
 * @author Cadiboo
 */
public class CollisionHandler {

	public static VoxelShape getCollisionShape(final BlockState blockState, final IBlockReader worldIn, final BlockPos pos) {
		final int negPosX = -pos.getX();
		final int negPosY = -pos.getY();
		final int negPosZ = -pos.getZ();
		try (BlockPos.PooledMutable pooled = BlockPos.PooledMutable.retain()) {
			try (FaceList faces = NoCubesConfig.Server.terrainMeshGenerator.getMeshGenerator().generateBlock(pos, worldIn, IsSmoothable.TERRAIN_SMOOTHABLE, pooled)) {
				VoxelShape shape = VoxelShapes.empty();
				for (final Face face : faces) {
					try (
							Vec3 v0 = face.getVertex0();
							Vec3 v1 = face.getVertex1();
							Vec3 v2 = face.getVertex2();
							Vec3 v3 = face.getVertex3()
					) {
						v0.addOffset(negPosX, negPosY, negPosZ);
						v1.addOffset(negPosX, negPosY, negPosZ);
						v2.addOffset(negPosX, negPosY, negPosZ);
						v3.addOffset(negPosX, negPosY, negPosZ);
						shape = VoxelShapes.combine(shape, makeShapeForVertex(v0), IBooleanFunction.OR);
						shape = VoxelShapes.combine(shape, makeShapeForVertex(v1), IBooleanFunction.OR);
						shape = VoxelShapes.combine(shape, makeShapeForVertex(v2), IBooleanFunction.OR);
						shape = VoxelShapes.combine(shape, makeShapeForVertex(v3), IBooleanFunction.OR);

						// lerp(t, v0, v1)
						final double v01x = MathHelper.lerp(0.5D, v0.x, v1.x);
						final double v23x = MathHelper.lerp(0.5D, v2.x, v3.x);
						final double v0123x = MathHelper.lerp(0.5D, v01x, v23x);
						final double v01y = MathHelper.lerp(0.5D, v0.y, v1.y);
						final double v23y = MathHelper.lerp(0.5D, v2.y, v3.y);
						final double v0123y = MathHelper.lerp(0.5D, v01y, v23y);
						final double v01z = MathHelper.lerp(0.5D, v0.z, v1.z);
						final double v23z = MathHelper.lerp(0.5D, v2.z, v3.z);
						final double v0123z = MathHelper.lerp(0.5D, v01z, v23z);
						try (Vec3 interp = Vec3.retain(v0123x, v0123y, v0123z)) {
							shape = VoxelShapes.combine(shape, makeShapeForVertex(interp), IBooleanFunction.OR);
						}
					}
				}
				return shape;
			}
		}
	}

	private static VoxelShape makeShapeForVertex(final Vec3 v) {
		final float grow = 2 / 16F;
		return VoxelShapes.create(
				v.x - grow, v.y - grow, v.z - grow,
				v.x + grow, v.y + grow, v.z + grow
		);
	}

}
