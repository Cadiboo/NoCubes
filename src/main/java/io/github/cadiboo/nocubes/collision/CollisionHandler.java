package io.github.cadiboo.nocubes.collision;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.hooks.AddCollisionBoxToListHook;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
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

import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;
import static java.lang.Math.max;

/**
 * This is 95% coppied from Repose
 *
 * @author Cadiboo
 */
@SuppressWarnings({
		"unused", // Hooks get invoked by ASM redirects
		"weakerAccess" // Hooks need to be public to be invoked
})
public final class CollisionHandler {

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

		if (!TERRAIN_SMOOTHABLE.isSmoothable(state)) {
			if (LEAVES_SMOOTHABLE.isSmoothable(state)) {
				if (entityIn != null) {
					// TODO
					entityIn.motionX *= 0.99;
//					entityIn.motionY *= 0.99;
					entityIn.motionZ *= 0.99;
				}
			} else {
				AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			}
			return;
		}

		//NOOOOOO
//		final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlock(pos, worldIn, TERRAIN_SMOOTHABLE);
//		if (faces != null) {
//			if (faces.isEmpty()) {
//				return;
//			}
//			faces.forEach(face -> {
//				face.getVertex0().close();
//				face.getVertex1().close();
//				face.getVertex2().close();
//				face.getVertex3().close();
//				face.close();
//			});
//			faces.close();
//		}

		final AxisAlignedBB collisionBoundingBox = state.getCollisionBoundingBox(worldIn, pos);
		if (collisionBoundingBox != null) { // optimization
			if (canUseSlope(entityIn) && canSlopeAt(state, worldIn, pos, collisionBoundingBox)) {
				collidingBoxes.addAll(slopingCollisionBoxes(state, worldIn, pos).stream().filter(entityBox::intersects).collect(Collectors.toList()));
			} else {
				AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			}
		}

//		addMeshCollisionBoxesToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
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

	private static void addMeshCollisionBoxesToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		final float boxRadius = 0.05F;

		try (final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlockMeshOffset(pos, worldIn, TERRAIN_SMOOTHABLE, ModConfig.terrainMeshGenerator)) {
			for (Face face : faces) {
				// Ew, Yay, Java 8 variable try-with-resources support
				try {
					final Vec3 v0 = face.getVertex0();
					final Vec3 v1 = face.getVertex1();
					final Vec3 v2 = face.getVertex2();
					final Vec3 v3 = face.getVertex3();

					final int posX = pos.getX();
					final int posY = pos.getY();
					final int posZ = pos.getZ();

					final AxisAlignedBB v0box = createAndOffsetAxisAlignedBBForVertex(posX, posY, posZ, v0, boxRadius);
					if (entityBox.intersects(v0box)) {
						collidingBoxes.add(v0box);
					}

					final AxisAlignedBB v1box = createAndOffsetAxisAlignedBBForVertex(posX, posY, posZ, v1, boxRadius);
					if (entityBox.intersects(v1box)) {
						collidingBoxes.add(v1box);
					}

					final AxisAlignedBB v2box = createAndOffsetAxisAlignedBBForVertex(posX, posY, posZ, v2, boxRadius);
					if (entityBox.intersects(v2box)) {
						collidingBoxes.add(v2box);
					}

					final AxisAlignedBB v3box = createAndOffsetAxisAlignedBBForVertex(posX, posY, posZ, v3, boxRadius);
					if (entityBox.intersects(v3box)) {
						collidingBoxes.add(v3box);
					}

					v0.close();
					v1.close();
					v2.close();
					v3.close();

				} finally {
					face.close();
				}
			}
		}
	}

	public static boolean canUseSlope(final Entity entity) {
//		return entity instanceof EntityPlayer || entity instanceof EntityCreature;
		return true;
	}

	private static AxisAlignedBB createAndOffsetAxisAlignedBBForVertex(final int posX, final int posY, final int posZ, final Vec3 vec3, final float boxRadius) {
		return new AxisAlignedBB(
				posX + (vec3.x - boxRadius),
				posY + (vec3.y - boxRadius),
				posZ + (vec3.z - boxRadius),
				posX + (vec3.x + boxRadius),
				posY + (vec3.y + boxRadius),
				posZ + (vec3.z + boxRadius)
		);
	}

}
