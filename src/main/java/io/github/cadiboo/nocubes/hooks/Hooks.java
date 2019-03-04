package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.tempcompatibility.ReposeCompatibility;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;
import static java.lang.Math.max;

/**
 * @author Cadiboo
 */
@SuppressWarnings({
		"unused", // Hooks get invoked by ASM redirects
		"weakerAccess" // Hooks need to be public to be invoked
})
public class Hooks {

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
		public static final List<Direction> OrdinalDirections = Arrays.asList(NorthEast, NorthWest, SouthEast, SouthWest);

		private final int x;
		private final int z;

		Direction(final int x, final int z) {
			this.x = x;
			this.z = z;
		}
	}

	private static final boolean REPOSE_INSTALLED = false;

	public static boolean isEntityInsideOpaqueBlock(Entity entity) {
		if (!NoCubes.isEnabled()) {
			return isEntityInsideOpaqueBlockDefault(entity);
		}
		return false;
	}

	private static boolean isEntityInsideOpaqueBlockDefault(final Entity entity) {
		if (entity.noClip) {
			return false;
		} else {
			BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

			for (int i = 0; i < 8; ++i) {
				int j = MathHelper.floor(entity.posY + (double) (((float) ((i >> 0) % 2) - 0.5F) * 0.1F) + (double) entity.getEyeHeight());
				int k = MathHelper.floor(entity.posX + (double) (((float) ((i >> 1) % 2) - 0.5F) * entity.width * 0.8F));
				int l = MathHelper.floor(entity.posZ + (double) (((float) ((i >> 2) % 2) - 0.5F) * entity.width * 0.8F));

				if (blockpos$pooledmutableblockpos.getX() != k || blockpos$pooledmutableblockpos.getY() != j || blockpos$pooledmutableblockpos.getZ() != l) {
					blockpos$pooledmutableblockpos.setPos(k, j, l);

					if (entity.world.getBlockState(blockpos$pooledmutableblockpos).causesSuffocation()) {
						blockpos$pooledmutableblockpos.release();
						return true;
					}
				}
			}

			blockpos$pooledmutableblockpos.release();
			return false;
		}
	}

	@SideOnly(Side.CLIENT)
	public static boolean shouldSideBeRendered(final Block block, final IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing facing) {
		if (!NoCubes.isEnabled()) {
			return shouldSideBeRenderedDefault(block, state, blockAccess, pos, facing);
		}
		return !ModUtil.isLiquidSource(state) && TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(pos.offset(facing))) || shouldSideBeRenderedDefault(block, state, blockAccess, pos, facing);
	}

	@SideOnly(Side.CLIENT)
	private static boolean shouldSideBeRenderedDefault(final Block block, final IBlockState state, final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing facing) {
		return block.shouldSideBeRendered(state, blockAccess, pos, facing);
	}

	@Nullable
	public static AxisAlignedBB getCollisionBoundingBox(Block block, IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		if (!NoCubes.isEnabled() || !ModConfig.collisionsEnabled) {
			return getCollisionBoundingBoxDefault(block, state, worldIn, pos);
		}
//		if (!TERRAIN_SMOOTHABLE.isSmoothable(state)) {
//			if (LEAVES_SMOOTHABLE.isSmoothable(state)) {
//				return null;
//			} else {
//				return getCollisionBoundingBoxDefault(block, state, worldIn, pos);
//			}
//		}

		final AxisAlignedBB box = getCollisionBoundingBoxDefault(block, state, worldIn, pos);
		return box == null || box.maxY == 0 ? null : box;

//		return null;
	}

	@Nullable
	private static AxisAlignedBB getCollisionBoundingBoxDefault(final Block block, final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
		if (REPOSE_INSTALLED) {
			return ReposeCompatibility.getCollisionBoundingBox(state, worldIn, pos);
		} else {
			return block.getCollisionBoundingBox(state, worldIn, pos);
		}
	}

	public static void addCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		if (!NoCubes.isEnabled() || !ModConfig.collisionsEnabled) {
			addCollisionBoxToListDefault(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			return;
		}

		if (!TERRAIN_SMOOTHABLE.isSmoothable(state)) {
			if (LEAVES_SMOOTHABLE.isSmoothable(state)) {
				if (entityIn != null) {
					entityIn.motionX *= 0.9;
					entityIn.motionY *= 0.9;
					entityIn.motionZ *= 0.9;
				}
				return;
			} else {
				addCollisionBoxToListDefault(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
				return;
			}
		}

		if (state.getCollisionBoundingBox(worldIn, pos) != null) { // optimization
			if (canUseSlope(entityIn) && canSlopeAt(state, worldIn, pos)) {
				collidingBoxes.addAll(slopingCollisionBoxes(state, worldIn, pos).stream().filter(entityBox::intersects).collect(Collectors.toList()));
			} else {
				addCollisionBoxToListDefault(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			}
		}

//		addMeshCollisionBoxesToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	private static List<AxisAlignedBB> slopingCollisionBoxes(final IBlockState state, World world, final BlockPos pos) {
		final double height = blockHeight(pos, world);
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
		IBlockState neighbor = world.getBlockState(offsetPos);
		return neighbor.getBlock().isTopSolid(neighbor) && blockHeight(offsetPos, world) >= stepHeight;
	}

//	private def cornerBox(pos: BlockPos, d: Direction, blockHeight: Double)(implicit w: World) = {
//		val stepHeight = blockHeight - 0.5
//		val height = if(stepHigh(pos.add(d.x, 0,  0 ), stepHeight) &&
//				stepHigh(pos.add( 0 , 0, d.z), stepHeight) &&
//				stepHigh(pos.add(d.x, 0, d.z), stepHeight)) blockHeight else stepHeight
//		new AxisAlignedBB(pos.getX + max(0.0, d.x/2.0), pos.getY         , pos.getZ + max(0.0, d.z/2.0),
//				pos.getX + max(0.5, d.x    ), pos.getY + height, pos.getZ + max(0.5, d.z    ))
//	}

	private static double blockHeight(final BlockPos pos, World world) {
		AxisAlignedBB box = world.getBlockState(pos).getCollisionBoundingBox(world, pos);
		return box == null ? 0 : box.maxY;
	}

	private static boolean canSlopeAt(final IBlockState state, World worldIn, final BlockPos pos) {
		final AxisAlignedBB collisionBoundingBox = state.getCollisionBoundingBox(worldIn, pos);
		boolean flag = collisionBoundingBox != null && collisionBoundingBox.maxY > 0.5;
		return TERRAIN_SMOOTHABLE.isSmoothable(state) && flag && worldIn.getBlockState(pos.up()).getCollisionBoundingBox(worldIn, pos.up()) == null;
	}

	private static void addMeshCollisionBoxesToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		final float boxRadius = 0.05F;

		try (final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlock(pos, worldIn, TERRAIN_SMOOTHABLE)) {
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

	private static boolean canUseSlope(final Entity entity) {
		return entity instanceof EntityPlayer || entity instanceof EntityCreature;
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

	private static void addCollisionBoxToListDefault(final Block block, final IBlockState state, final World worldIn, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, final Entity entityIn, final boolean isActualState) {
		if (REPOSE_INSTALLED) {
			ReposeCompatibility.addCollisionBoxToList(state, worldIn, entityBox, collidingBoxes, entityIn, isActualState);
		} else {
			block.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
		}
	}

}
