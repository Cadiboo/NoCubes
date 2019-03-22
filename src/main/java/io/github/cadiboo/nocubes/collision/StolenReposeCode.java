package io.github.cadiboo.nocubes.collision;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.hooks.AddCollisionBoxToListHook;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;
import static java.lang.Math.max;

/**
 * This is 95% coppied from Repose
 *
 * @author Cadiboo
 */
public final class StolenReposeCode {

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

	public static void addCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {

		final AxisAlignedBB collisionBoundingBox = state.getCollisionBoundingBox(worldIn, pos);
		if (collisionBoundingBox != null) { // optimization
			if (canUseSlope(entityIn) && canSlopeAt(state, worldIn, pos, collisionBoundingBox)) {
				collidingBoxes.addAll(slopingCollisionBoxes(state, worldIn, pos).stream().filter(entityBox::intersects).collect(Collectors.toList()));
			} else {
				AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			}
		}

	}

	public static List<AxisAlignedBB> slopingCollisionBoxes(final IBlockState state, World world, final BlockPos pos) {
		final double height = blockHeight(pos, world, world.getBlockState(pos));
		final ArrayList<AxisAlignedBB> list = new ArrayList<>();
		for (Direction direction : Direction.OrdinalDirections) {
			list.add(cornerBox(pos, direction, height, world));
		}
		return list;
	}

	private static AxisAlignedBB cornerBox(final BlockPos pos, Direction direction, double blockHeight, World world) {
		double stepHeight = blockHeight - 0.5;
		final double height;
		if (stepHigh(pos.add(direction.x, 0, 0), stepHeight, world) &&
				stepHigh(pos.add(0, 0, direction.z), stepHeight, world) &&
				stepHigh(pos.add(direction.x, 0, direction.z), stepHeight, world)) {
			height = blockHeight;
		} else {
			height = stepHeight;
		}

		return new AxisAlignedBB(
				pos.getX() + max(0.0, direction.x / 2.0), pos.getY(), pos.getZ() + max(0.0, direction.z / 2.0),
				pos.getX() + max(0.5, direction.x), pos.getY() + height, pos.getZ() + max(0.5, direction.z)
		);
	}

	private static boolean stepHigh(final BlockPos offsetPos, final double stepHeight, World world) {
		final IBlockState neighbor = world.getBlockState(offsetPos);
		return neighbor.getBlock().isTopSolid(neighbor) && blockHeight(offsetPos, world, neighbor) >= stepHeight;
	}

	private static double blockHeight(final BlockPos pos, World world, final IBlockState blockState) {
		AxisAlignedBB box = blockState.getCollisionBoundingBox(world, pos);
		return box == null ? 0 : box.maxY;
	}

	public static boolean canSlopeAt(final IBlockState state, World worldIn, final BlockPos pos, final AxisAlignedBB collisionBoundingBox) {
		boolean flag = collisionBoundingBox != null && collisionBoundingBox.maxY > 0.5;
		final BlockPos posUp = pos.up();
		return TERRAIN_SMOOTHABLE.isSmoothable(state) && flag && worldIn.getBlockState(posUp).getCollisionBoundingBox(worldIn, posUp) == null;
	}

	public static boolean canUseSlope(final Entity entity) {
//		return entity instanceof EntityPlayer || entity instanceof EntityCreature;
		return true;
	}

}
