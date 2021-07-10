package io.github.cadiboo.nocubes.collision;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.ReuseableStream;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.stream.Stream;

import static net.minecraft.util.Direction.*;
import static net.minecraft.util.math.BlockPos.*;
import static net.minecraft.util.math.MathHelper.*;

abstract class Scratch extends Entity {
	public Scratch(EntityType<?> p_i48580_1_, World p_i48580_2_) {
		super(p_i48580_1_, p_i48580_2_);
	}

	public static Vector3d collideBoundingBoxHeuristically(@Nullable Entity entity, Vector3d vec, AxisAlignedBB aabb, World world, ISelectionContext ctx, ReuseableStream<VoxelShape> nonBlockShapes) {
		// Shapes: Other entities, the world border.
		boolean xStatic = vec.x == 0.0D;
		boolean yStatic = vec.y == 0.0D;
		boolean zStatic = vec.z == 0.0D;
		boolean xOrYMoving = !xStatic || !yStatic;
		boolean xOrZMoving = !xStatic || !zStatic;
		boolean yOrZMoving = !yStatic || !zStatic;
		if (xOrYMoving && xOrZMoving && yOrZMoving) {
			ReuseableStream<VoxelShape> allShapes = new ReuseableStream<>(Stream.concat(nonBlockShapes.getStream(), world.getBlockCollisions(entity, aabb.expandTowards(vec))));
			return collideBoundingBoxLegacy(vec, aabb, allShapes);
		} else {
			return collideBoundingBox(vec, aabb, world, ctx, nonBlockShapes);
		}
	}

	static AxisAlignedBB move(AxisAlignedBB aabb, double x, double y, double z) {
		if (x == 0 && y == 0 && z == 0)
			return aabb;
		return aabb.move(x, y, z);
	}

	public static Vector3d collideBoundingBox(
		Vector3d motion, AxisAlignedBB aabb, IWorldReader world, ISelectionContext ctx, ReuseableStream<VoxelShape> nonBlockShapes
	) {
		double motionX = motion.x;
		double motionY = motion.y;
		double motionZ = motion.z;
		if (motionY != 0.0D) {
			motionY = VoxelShapes.collide(Axis.Y, aabb, world, motionY, ctx, nonBlockShapes.getStream());
			if (motionY != 0.0D)
				aabb = aabb.move(0.0D, motionY, 0.0D);
		}

		boolean motionMoreTowardsZ = Math.abs(motionX) < Math.abs(motionZ);
		if (motionMoreTowardsZ && motionZ != 0.0D) {
			motionZ = VoxelShapes.collide(Axis.Z, aabb, world, motionZ, ctx, nonBlockShapes.getStream());
			if (motionZ != 0.0D)
				aabb = aabb.move(0.0D, 0.0D, motionZ);
		}

		if (motionX != 0.0D) {
			motionX = VoxelShapes.collide(Axis.X, aabb, world, motionX, ctx, nonBlockShapes.getStream());
			if (!motionMoreTowardsZ && motionX != 0.0D)
				aabb = aabb.move(motionX, 0.0D, 0.0D);
		}

		if (!motionMoreTowardsZ && motionZ != 0.0D)
			motionZ = VoxelShapes.collide(Axis.Z, aabb, world, motionZ, ctx, nonBlockShapes.getStream());
		return new Vector3d(motionX, motionY, motionZ);
	}


	public static double collideLegacy(Axis axis, AxisAlignedBB aabb, Stream<VoxelShape> allShapes, double motion) {
		Iterator<VoxelShape> iterator = allShapes.iterator();
		while (iterator.hasNext()) {
			if (Math.abs(motion) < 0.0000001)
				return 0.0D;
			motion = iterator.next().collide(axis, aabb, motion);
		}
		return motion;
	}

	public static double collide(Axis axis, AxisAlignedBB aabb, IWorldReader world, double motion, ISelectionContext ctx, Stream<VoxelShape> nonBlockShapes) {
		return collide(aabb, world, motion, ctx, AxisRotation.between(axis, Axis.Z), nonBlockShapes);
	}

	private static double collide(AxisAlignedBB aabb, IWorldReader world, double motion, ISelectionContext ctx, AxisRotation _rotation_, Stream<VoxelShape> nonBlockShapes) {
		if (aabb.getXsize() < 0.000001 || aabb.getYsize() < 0.000001 || aabb.getZsize() < 0.000001)
			return motion;
		if (Math.abs(motion) < 0.0000001)
			return 0.0D;

		AxisRotation _inverse_ = _rotation_.inverse();
		Axis cycledX = _inverse_.cycle(Axis.X);
		Axis cycledY = _inverse_.cycle(Axis.Y);
		Axis cycledZ = _inverse_.cycle(Axis.Z);
		Mutable pos = new Mutable();

		int minX = floor(aabb.min(cycledX) - 0.0000001) - 1;
		int maxX = floor(aabb.max(cycledX) + 0.0000001) + 1;
		int minY = floor(aabb.min(cycledY) - 0.0000001) - 1;
		int maxY = floor(aabb.max(cycledY) + 0.0000001) + 1;
		double d0 = aabb.min(cycledZ) - 0.0000001;
		double d1 = aabb.max(cycledZ) + 0.0000001;
		boolean motionInitiallyPositive = motion > 0.0D;
		int minZ = motionInitiallyPositive ? floor(aabb.max(cycledZ) - 0.0000001) - 1 : floor(aabb.min(cycledZ) + 0.0000001) + 1;
		int maxZ = lastC(motion, d0, d1);
		int zIncrement = motionInitiallyPositive ? 1 : -1;
		int z = minZ;

		while (true) {
			if (motionInitiallyPositive) {
				if (z > maxZ)
					break;
			} else if (z < maxZ)
				break;

			for (int x = minX; x <= maxX; ++x) {
				for (int y = minY; y <= maxY; ++y) {
					int boundariesTouched = 0;
					if (x == minX || x == maxX)
						++boundariesTouched;
					if (y == minY || y == maxY)
						++boundariesTouched;
					if (z == minZ || z == maxZ)
						++boundariesTouched;
					if (boundariesTouched >= 3)
						continue;

					pos.set(_inverse_, x, y, z);
					BlockState blockstate = world.getBlockState(pos);
					if (boundariesTouched == 1 && !blockstate.hasLargeCollisionShape())
						continue;
					if (boundariesTouched == 2 && !blockstate.is(Blocks.MOVING_PISTON))
						continue;

					motion = blockstate.getCollisionShape(world, pos, ctx).collide(cycledZ, aabb.move(-pos.getX(), -pos.getY(), -pos.getZ()), motion);
					if (Math.abs(motion) < 0.0000001)
						return 0.0D;
					maxZ = lastC(motion, d0, d1);
				}
			}
			z += zIncrement;
		}

		double[] motionRef = {motion};
		nonBlockShapes.forEach((shape) -> motionRef[0] = shape.collide(cycledZ, aabb, motionRef[0]));
		return motionRef[0];
	}

	private static int lastC(double p_216385_0_, double p_216385_2_, double p_216385_4_) {
		return p_216385_0_ > 0.0D ? floor(p_216385_4_ + p_216385_0_) + 1 : floor(p_216385_2_ + p_216385_0_) - 1;
	}


}
