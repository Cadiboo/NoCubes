package io.github.cadiboo.nocubes.collision;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.BlockState;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.border.WorldBorder;

import javax.annotation.Nonnull;

import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static java.lang.Math.max;

/**
 * This is 95% coppied from Repose
 *
 * @author Cadiboo
 */
final class StolenReposeCode {

	/**
	 * @return the UN OFFSET collision shape
	 */
	public static VoxelShape getCollisionShape(BlockState stateIn, IWorldReader worldIn, BlockPos posIn, final ISelectionContext context) {

		final VoxelShape collisionShape = getStateCollisionShape(stateIn, worldIn, posIn, context);

		if (collisionShape.isEmpty()) { // optimization
			return collisionShape;
		}

		final float density = getDensity(worldIn, posIn);
		// > 0 means outside isosurface
		// > -1 means mostly outside isosurface
		if (density > -1) {
			return VoxelShapes.empty();
		} else if (canSlopeAt(stateIn, worldIn, posIn, collisionShape, context)) {
			try (ModProfiler ignored = ModProfiler.get().start("Collisions getSlopingCollisionShape")) {
				return getSlopingCollisionShape(stateIn, worldIn, posIn, context);
			}
		} else {
			return collisionShape;
		}
	}

	private static float getDensity(final IWorldReader reader, final BlockPos pos) {
		float density = 0;
		try (
				ModProfiler ignored = ModProfiler.get().start("Collisions calculate cube density");
				PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()
		) {
			final WorldBorder worldBorder = reader.getWorldBorder();

			final int startX = pos.getX();
			final int startY = pos.getY();
			final int startZ = pos.getZ();

			for (int zOffset = 0; zOffset < 2; ++zOffset) {
				for (int yOffset = 0; yOffset < 2; ++yOffset) {
					for (int xOffset = 0; xOffset < 2; ++xOffset) {

						pooledMutableBlockPos.setPos(
								startX - xOffset,
								startY - yOffset,
								startZ - zOffset
						);

						// Return a fully solid cube if its not loaded
						if (!reader.isBlockLoaded(pooledMutableBlockPos) || !worldBorder.contains(pooledMutableBlockPos)) {
							density += 1;
							continue;
						}

						final BlockState testState = reader.getBlockState(pooledMutableBlockPos);
						density += ModUtil.getIndividualBlockDensity(TERRAIN_SMOOTHABLE.apply(testState), testState);
					}
				}
			}
		}
		return density;
	}

	private static VoxelShape getSlopingCollisionShape(final BlockState state, IWorldReader world, final BlockPos pos, final ISelectionContext context) {
		final double height = blockHeight(pos, world, state, context);
		VoxelShape shape = VoxelShapes.empty();
		for (Direction direction : Direction.OrdinalDirections) {
			shape = VoxelShapes.combine(shape, cornerBox(pos, direction, height, world, context), IBooleanFunction.OR);
		}
		return shape;
	}

	private static VoxelShape cornerBox(final BlockPos pos, Direction direction, double blockHeight, IWorldReader world, final ISelectionContext context) {
		final double stepHeight = blockHeight - 0.5;
		final double height;
		final int dirX = direction.x;
		final int dirZ = direction.z;
		if (stepHigh(pos.add(dirX, 0, 0), stepHeight, world, context) &&
				stepHigh(pos.add(0, 0, dirZ), stepHeight, world, context) &&
				stepHigh(pos.add(dirX, 0, dirZ), stepHeight, world, context)) {
			height = blockHeight;
		} else {
			height = stepHeight;
		}

//		final int posX = pos.getX();
//		final int posY = pos.getY();
//		final int posZ = pos.getZ();
//		return VoxelShapes.create(
//				posX + max(0.0, dirX / 2.0), posY, posZ + max(0.0, dirZ / 2.0),
//				posX + max(0.5, dirX), posY + height, posZ + max(0.5, dirZ)
//		);
		return VoxelShapes.create(
				max(0.0, dirX / 2.0), 0, max(0.0, dirZ / 2.0),
				max(0.5, dirX), height, max(0.5, dirZ)
		);
	}

	private static boolean stepHigh(final BlockPos offsetPos, final double stepHeight, IWorldReader world, final ISelectionContext context) {
		if (!world.isBlockLoaded(offsetPos) || !world.getWorldBorder().contains(offsetPos)) {
			return true;
		}
		final BlockState neighbor = world.getBlockState(offsetPos);
		return /*neighbor.isTopSolid() && */blockHeight(offsetPos, world, neighbor, context) >= stepHeight;
	}

	private static double blockHeight(final BlockPos pos, IBlockReader world, final BlockState blockState, final ISelectionContext context) {
		return getStateCollisionShape(blockState, world, pos, context).getEnd(Axis.Y);
	}

	private static boolean canSlopeAt(final BlockState state, IBlockReader worldIn, final BlockPos pos, final VoxelShape collisionBoundingBox, final ISelectionContext context) {
		boolean flag = collisionBoundingBox != null && collisionBoundingBox.getEnd(Axis.Y) > 0.5;
		final BlockPos posUp = pos.up();
		return TERRAIN_SMOOTHABLE.apply(state) && flag && getStateCollisionShape(worldIn.getBlockState(posUp), worldIn, posUp, context).isEmpty();
	}

	@Nonnull
	private static VoxelShape getStateCollisionShape(final BlockState state, final IBlockReader world, final BlockPos pos, final ISelectionContext context) {
		if (state == StateHolder.SNOW_LAYER_DEFAULT) {
			return VoxelShapes.empty(); // Stop snow having a collisions AABB with no height that still blocks movement
		} else {
			return state.getCollisionShape(world, pos, context);
		}
	}

	private enum Direction {

		North(0, -1),
		South(0, 1),
		East(1, 0),
		West(-1, 0),

		//val NorthEast = North + East
		NorthEast(North.x + East.x, North.z + East.z),
		NorthWest(North.x + West.x, North.z + West.z),
		SouthEast(South.x + East.x, South.z + East.z),
		SouthWest(South.x + West.x, South.z + West.z);
		public static final ImmutableList<Direction> OrdinalDirections = ImmutableList.of(NorthEast, NorthWest, SouthEast, SouthWest);

		private final int x;
		private final int z;

		Direction(final int x, final int z) {
			this.x = x;
			this.z = z;
		}
	}

}
