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

	static VoxelShape getCollisionShape(BlockState stateIn, IWorldReader worldIn, BlockPos posIn) {

		final VoxelShape collisionShape = getStateCollisionShape(stateIn, worldIn, posIn);

		if (collisionShape.isEmpty()) {  // optimization
			return collisionShape;
		}

		float density = 0;
		try (
				ModProfiler ignored = ModProfiler.get().start("Collisions calculate cube density");
				PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()
		) {

			final WorldBorder worldBorder = worldIn.getWorldBorder();

			final int startX = posIn.getX();
			final int startY = posIn.getY();
			final int startZ = posIn.getZ();

			for (int zOffset = 0; zOffset < 2; ++zOffset) {
				for (int yOffset = 0; yOffset < 2; ++yOffset) {
					for (int xOffset = 0; xOffset < 2; ++xOffset) {

						pooledMutableBlockPos.setPos(
								startX - xOffset,
								startY - yOffset,
								startZ - zOffset
						);

						// Return a fully solid cube if its not loaded
						if (!worldIn.isBlockLoaded(pooledMutableBlockPos) || !worldBorder.contains(pooledMutableBlockPos)) {
							density += 1;
							continue;
						}

						final BlockState testState = worldIn.getBlockState(pooledMutableBlockPos);
						density += ModUtil.getIndividualBlockDensity(TERRAIN_SMOOTHABLE.apply(testState), testState);
					}
				}
			}
		}

		if (density > -1) {
			return VoxelShapes.empty();
		} else if (canSlopeAt(stateIn, worldIn, posIn, collisionShape)) {
			try (ModProfiler ignored = ModProfiler.get().start("Collisions getSlopingCollisionShape")) {
				return getSlopingCollisionShape(stateIn, worldIn, posIn);
			}
		} else {
			return collisionShape;
		}
	}

	private static VoxelShape getSlopingCollisionShape(final BlockState state, IWorldReader world, final BlockPos pos) {
		final double height = blockHeight(pos, world, state);
		VoxelShape shape = VoxelShapes.empty();
		for (Direction direction : Direction.OrdinalDirections) {
			shape = VoxelShapes.combine(shape, cornerBox(pos, direction, height, world), IBooleanFunction.OR);
		}
		return shape;
	}

	private static VoxelShape cornerBox(final BlockPos pos, Direction direction, double blockHeight, IWorldReader world) {
		final double stepHeight = blockHeight - 0.5;
		final double height;
		if (stepHigh(pos.add(direction.x, 0, 0), stepHeight, world) &&
				stepHigh(pos.add(0, 0, direction.z), stepHeight, world) &&
				stepHigh(pos.add(direction.x, 0, direction.z), stepHeight, world)) {
			height = blockHeight;
		} else {
			height = stepHeight;
		}

//		return VoxelShapes.create(
//				pos.getX() + max(0.0, direction.x / 2.0), pos.getY(), pos.getZ() + max(0.0, direction.z / 2.0),
//				pos.getX() + max(0.5, direction.x), pos.getY() + height, pos.getZ() + max(0.5, direction.z)
//		);
		return VoxelShapes.create(
				max(0.0, direction.x / 2.0), 0, max(0.0, direction.z / 2.0),
				max(0.5, direction.x), height, max(0.5, direction.z)
		);
	}

	private static boolean stepHigh(final BlockPos offsetPos, final double stepHeight, IWorldReader world) {
		if (!world.isBlockLoaded(offsetPos) || !world.getWorldBorder().contains(offsetPos)) {
			return true;
		}
		final BlockState neighbor = world.getBlockState(offsetPos);
		return /*neighbor.isTopSolid() && */blockHeight(offsetPos, world, neighbor) >= stepHeight;
	}

	private static double blockHeight(final BlockPos pos, IBlockReader world, final BlockState blockState) {
		VoxelShape box = getStateCollisionShape(blockState, world, pos);
		return box.getEnd(Axis.Y);
	}

	private static boolean canSlopeAt(final BlockState state, IBlockReader worldIn, final BlockPos pos, final VoxelShape collisionBoundingBox) {
		boolean flag = collisionBoundingBox != null && collisionBoundingBox.getEnd(Axis.Y) > 0.5;
		final BlockPos posUp = pos.up();
		return TERRAIN_SMOOTHABLE.apply(state) && flag && getStateCollisionShape(worldIn.getBlockState(posUp), worldIn, posUp).isEmpty();
	}

	@Nonnull
	private static VoxelShape getStateCollisionShape(final BlockState state, final IBlockReader world, final BlockPos pos) {
		if (state == StateHolder.SNOW_LAYER_DEFAULT) {
			return VoxelShapes.empty(); // Stop snow having a collisions AABB with no height that still blocks movement
		} else {
			return state.getCollisionShape(world, pos);
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
