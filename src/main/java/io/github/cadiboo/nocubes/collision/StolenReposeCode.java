package io.github.cadiboo.nocubes.collision;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static java.lang.Math.max;

/**
 * This is 95% coppied from Repose
 *
 * @author Cadiboo
 */
final class StolenReposeCode {

	static void addCollisionBoxToList(IBlockState stateIn, World worldIn, BlockPos posIn, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {

		final AxisAlignedBB collisionBoundingBox = getStateCollisionBoundingBox(stateIn, worldIn, posIn);
		if (collisionBoundingBox == null) {  // optimization
			return;
		}

		final float density = getDensity(worldIn, posIn);
		if (density > -1) {
			return;
		}
		if (canSlopeAt(stateIn, worldIn, posIn, collisionBoundingBox)) {
			addSlopingCollisionBoxes(stateIn, worldIn, posIn, collidingBoxes, entityBox::intersects);
		} else {
			stateIn.addCollisionBoxToList(worldIn, posIn, entityBox, collidingBoxes, entityIn, isActualState);
		}
	}

	private static float getDensity(final World reader, final BlockPos pos) {
		float density = 0;
		try (ModProfiler ignored = ModProfiler.get().start("Collisions calculate cube density")) {
			PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
			try {
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

							final IBlockState testState = reader.getBlockState(pooledMutableBlockPos);
							density += ModUtil.getIndividualBlockDensity(TERRAIN_SMOOTHABLE.apply(testState), testState);
						}
					}
				}
			} finally {
				pooledMutableBlockPos.release();
			}
		}
		return density;
	}

	private static boolean canSlope(final IBlockState state) {
		return TERRAIN_SMOOTHABLE.apply(state);
	}

	private static boolean canSlopeAt(final IBlockState state, World worldIn, final BlockPos pos, final AxisAlignedBB collisionBoundingBox) {
		final AxisAlignedBB box = getStateCollisionBoundingBox(state, worldIn, pos);
		final BlockPos posUp = pos.up();
		return canSlope(state) && (box == null || (box.maxY > 0.5 && getStateCollisionBoundingBox(worldIn.getBlockState(posUp), worldIn, posUp) == null));
	}

	private static void addSlopingCollisionBoxes(final IBlockState state, World world, final BlockPos pos, final List<AxisAlignedBB> collidingBoxes, final Predicate<AxisAlignedBB> predicate) {
		final double height = blockHeight(pos, world, state);
		final double stepHeight = height - 0.5;
		final boolean slopingShore = true; //Config.slopingShores;
		final boolean submerged = world.getBlockState(pos.up()).getMaterial().isLiquid();
		for (Direction direction : Direction.OrdinalDirections) {
			final AxisAlignedBB box = cornerBox(pos, direction, height, stepHeight, slopingShore, submerged, world);
			if (predicate.test(box)) {
				collidingBoxes.add(box);
			}
		}
	}

	private static AxisAlignedBB cornerBox(final BlockPos pos, Direction direction, double blockHeight, double stepHeight, boolean slopingShore, boolean submerged, World world) {
		final double height;
		if (stepHigh(pos.add(direction.x, 0, 0), stepHeight, slopingShore, submerged, world) &&
				stepHigh(pos.add(0, 0, direction.z), stepHeight, slopingShore, submerged, world) &&
				stepHigh(pos.add(direction.x, 0, direction.z), stepHeight, slopingShore, submerged, world)) {
			height = blockHeight;
		} else {
			height = stepHeight;
		}

		final int posX = pos.getX();
		final int posY = pos.getY();
		final int posZ = pos.getZ();

		return new AxisAlignedBB(
				posX + max(0.0, direction.x / 2.0), posY, posZ + max(0.0, direction.z / 2.0),
				posX + max(0.5, direction.x), posY + height, posZ + max(0.5, direction.z)
		);
	}

	private static boolean stepHigh(final BlockPos offsetPos, final double stepHeight, final boolean slopingShore, final boolean submerged, World world) {
		if (!world.isBlockLoaded(offsetPos) || !world.getWorldBorder().contains(offsetPos)) {
			return true;
		}
		final IBlockState neighbor = world.getBlockState(offsetPos);
		if (!slopingShore && !submerged && neighbor.getMaterial().isLiquid()) {
			return true;
		}
		if (neighbor.getMaterial().blocksMovement()) {
			return blockHeight(offsetPos, world, neighbor) >= stepHeight;
		}
		return false;
	}

	private static double blockHeight(final BlockPos pos, World world, final IBlockState blockState) {
		final AxisAlignedBB box = getStateCollisionBoundingBox(blockState, world, pos);
		return box == null ? 0 : box.maxY;
	}

	@Nullable
	private static AxisAlignedBB getStateCollisionBoundingBox(final IBlockState state, final IBlockAccess world, final BlockPos pos) {
		if (state == StateHolder.SNOW_LAYER_DEFAULT) {
			return null; // Stop snow having a collisions AABB with no height that still blocks movement
		} else {
			return state.getCollisionBoundingBox(world, pos);
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
