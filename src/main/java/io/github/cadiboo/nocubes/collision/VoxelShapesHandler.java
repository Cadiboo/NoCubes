package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;

import javax.annotation.Nullable;
import java.util.stream.Stream;

import static io.github.cadiboo.nocubes.collision.CollisionHandler.shouldApplyMeshCollisions;
import static io.github.cadiboo.nocubes.collision.CollisionHandler.shouldApplyReposeCollisions;
import static net.minecraft.util.math.MathHelper.floor;
import static net.minecraft.util.math.shapes.VoxelShapes.getDifferenceFloored;

/**
 * @author Cadiboo
 */
public final class VoxelShapesHandler {

	// I think that I can use MeshDispatcher.getBlockMesh or whatever
	// And StolenReposeCode.getCollisionShape
	// TODO: Fix later

	/**
	 * FIXME: This is horrible and it doesn't work right but I think its better than nothing
	 */
	public static double getAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, double desiredOffset, final ISelectionContext context, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Axis rotX, final Axis rotY, final Axis rotZ) {
		@Nullable final Entity entity = context.getEntity();
		if (!Config.terrainCollisions) {
			return getVanillaAllowedOffset(collisionBox, worldReader, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
		} else if (shouldApplyMeshCollisions(entity)) {
			return getMeshAllowedOffset(collisionBox, worldReader, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
		} else if (shouldApplyReposeCollisions(entity)) {
			return getReposeAllowedOffset(collisionBox, worldReader, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
		} else {
			return getVanillaAllowedOffset(collisionBox, worldReader, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
		}
	}

	private static double getMeshAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, double desiredOffset, final ISelectionContext selectionContext, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Axis rotX, final Axis rotY, final Axis rotZ) {
		// TODO
		double[] adouble = new double[]{desiredOffset};
		possibleHits.forEach((shape) -> adouble[0] = shape.getAllowedOffset(rotZ, collisionBox, adouble[0]));
		return adouble[0];
	}

	private static double getVanillaAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, double desiredOffset, final ISelectionContext selectionContext, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Axis rotX, final Axis rotY, final Axis rotZ) {
		try (PooledMutableBlockPos pos = PooledMutableBlockPos.retain()) {
			final int minXm1 = floor(collisionBox.getMin(rotX) - 0.0000001) - 1;
			final int maxXp1 = floor(collisionBox.getMax(rotX) + 0.0000001) + 1;
			final int minYm1 = floor(collisionBox.getMin(rotY) - 0.0000001) - 1;
			final int maxYp1 = floor(collisionBox.getMax(rotY) + 0.0000001) + 1;
			final double minZ = collisionBox.getMin(rotZ) - 0.0000001;
			final double maxZ = collisionBox.getMax(rotZ) + 0.0000001;
			final boolean over0 = desiredOffset > 0.0D;
			final int initialZ = over0 ? floor(collisionBox.getMax(rotZ) - 0.0000001) - 1 : floor(collisionBox.getMin(rotZ) + 0.0000001) + 1;
			int diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
			final int p1orm1 = over0 ? 1 : -1;
			int z = initialZ;
			while (true) {
				if (over0) {
					if (z > diffFloored) {
						break;
					}
				} else if (z < diffFloored) {
					break;
				}
				for (int x = minXm1; x <= maxXp1; ++x) {
					for (int y = minYm1; y <= maxYp1; ++y) {
						int numBoundariesTouched = 0;
						if (x == minXm1 || x == maxXp1) {
							++numBoundariesTouched;
						}
						if (y == minYm1 || y == maxYp1) {
							++numBoundariesTouched;
						}
						if (z == initialZ || z == diffFloored) {
							++numBoundariesTouched;
						}
						if (numBoundariesTouched < 3) {
							pos.func_218295_a(reversedRotation, x, y, z);
							BlockState blockstate = worldReader.getBlockState(pos);
							if (numBoundariesTouched != 1 || blockstate.isCollisionShapeLargerThanFullBlock()) {
								if (numBoundariesTouched != 2 || blockstate.getBlock() == Blocks.MOVING_PISTON) {
									desiredOffset = blockstate.getCollisionShape(worldReader, pos, selectionContext).getAllowedOffset(rotZ, collisionBox.offset(-pos.getX(), -pos.getY(), -pos.getZ()), desiredOffset);
									if (Math.abs(desiredOffset) < 0.0000001) {
										return 0.0D;
									}
									diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
								}
							}
						}
					}
				}
				z += p1orm1;
			}
		}
		double[] adouble = new double[]{desiredOffset};
		possibleHits.forEach((shape) -> adouble[0] = shape.getAllowedOffset(rotZ, collisionBox, adouble[0]));
		return adouble[0];
	}

	private static double getReposeAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, double desiredOffset, final ISelectionContext selectionContext, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Axis rotX, final Axis rotY, final Axis rotZ) {
		try (PooledMutableBlockPos pos = PooledMutableBlockPos.retain()) {
			final int minXm1 = floor(collisionBox.getMin(rotX) - 0.0000001) - 1;
			final int maxXp1 = floor(collisionBox.getMax(rotX) + 0.0000001) + 1;
			final int minYm1 = floor(collisionBox.getMin(rotY) - 0.0000001) - 1;
			final int maxYp1 = floor(collisionBox.getMax(rotY) + 0.0000001) + 1;
			final double minZ = collisionBox.getMin(rotZ) - 0.0000001;
			final double maxZ = collisionBox.getMax(rotZ) + 0.0000001;
			final boolean over0 = desiredOffset > 0.0D;
			final int initialZ = over0 ? floor(collisionBox.getMax(rotZ) - 0.0000001) - 1 : floor(collisionBox.getMin(rotZ) + 0.0000001) + 1;
			int diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
			final int p1orm1 = over0 ? 1 : -1;
			int z = initialZ;
			while (true) {
				if (over0) {
					if (z > diffFloored) {
						break;
					}
				} else if (z < diffFloored) {
					break;
				}
				for (int x = minXm1; x <= maxXp1; ++x) {
					for (int y = minYm1; y <= maxYp1; ++y) {
						int numBoundariesTouched = 0;
						if (x == minXm1 || x == maxXp1) {
							++numBoundariesTouched;
						}
						if (y == minYm1 || y == maxYp1) {
							++numBoundariesTouched;
						}
						if (z == initialZ || z == diffFloored) {
							++numBoundariesTouched;
						}
						if (numBoundariesTouched < 3) {
							pos.func_218295_a(reversedRotation, x, y, z);
							BlockState blockstate = worldReader.getBlockState(pos);
							if (numBoundariesTouched != 1 || blockstate.isCollisionShapeLargerThanFullBlock()) {
								if (numBoundariesTouched != 2 || blockstate.getBlock() == Blocks.MOVING_PISTON) {
									final VoxelShape unOffsetCollisionShape;
									if (blockstate.nocubes_isTerrainSmoothable) {
										// TODO: Improve performance (StateCache?)
										unOffsetCollisionShape = StolenReposeCode.getCollisionShape(blockstate, worldReader, pos, selectionContext);
									} else {
										unOffsetCollisionShape = blockstate.getCollisionShape(worldReader, pos, selectionContext);
									}
									desiredOffset = unOffsetCollisionShape.getAllowedOffset(rotZ, collisionBox.offset(-pos.getX(), -pos.getY(), -pos.getZ()), desiredOffset);
									if (Math.abs(desiredOffset) < 0.0000001) {
										return 0.0D;
									}
									diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
								}
							}
						}
					}
				}
				z += p1orm1;
			}
		}
		double[] adouble = new double[]{desiredOffset};
		possibleHits.forEach((shape) -> adouble[0] = shape.getAllowedOffset(rotZ, collisionBox, adouble[0]));
		return adouble[0];
	}

}
