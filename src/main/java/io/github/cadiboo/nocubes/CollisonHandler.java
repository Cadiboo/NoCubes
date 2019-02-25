package io.github.cadiboo.nocubes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.List;

import static java.lang.Math.floor;

/**
 * @author Cadiboo
 */
public class CollisonHandler {

	public static AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockReader w, BlockPos pos) {
		AxisAlignedBB box = state.getCollisionBoundingBox(w, pos);
		if (box == null || box.maxY == 0)
			return null; // snow_layer with data 0 makes a 0-thickness box that still blocks side movement
		else return box;
	}

	public static void addCollisionBoxToList(IBlockState state, World w, BlockPos pos, AxisAlignedBB box,
	                                         List<AxisAlignedBB> intersectingBoxes, Entity collidingEntity, boolean flag) {
		if (state.getCollisionBoundingBox(w, pos) != null) { // optimization
			World world = w;
			if (collidingEntity.canUseSlope && state.canSlopeAt(pos))
				intersectingBoxes.add(state.slopingCollisionBoxes(pos).filter(box.intersects));
			else
				state.addCollisionBoxToList(w, pos, box, intersectingBoxes, collidingEntity, flag);
		}
	}

	public static boolean isEntityInsideOpaqueBlock(EntityLivingBase entity) { // doesn't work with top-level Entity
		World world = entity.world;
		for (int i = 0; i < 8; i++) {
			float dx = (((i >> 0) % 2).toFloat - 0.5F) * entity.width * 0.8F;
			float dy = (((i >> 1) % 2).toFloat - 0.5F) * 0.1F;
			float dz = (((i >> 2) % 2).toFloat - 0.5F) * entity.width * 0.8F;
			int x = floor(entity.posX + dx.toDouble).toInt;
			int y = floor(entity.posY + entity.getEyeHeight.toDouble + dy.toDouble).toInt;
			int z = floor(entity.posZ + dz.toDouble).toInt;
			BlockPos pos = new BlockPos(x, y, z);
			IBlockState state = world.getBlockState(pos);
			if (state.isNormalCube() && !(entity.canUseSlope && state.canSlopeAt(pos)))
				return true;
		}
		return false;
	}

	public static class SlopingBlockValue {

		private final IBlockState state;

		public SlopingBlockValue(IBlockState state) {
			this.state = state;
		}

//		public boolean canSlope: Boolean = (slopingBlocks.matches(granularBlocksChoice)     &&     reposeGranularBlocks.contains(state)) ||
//				(slopingBlocks.matches(naturalStoneBlocksChoice) && reposeNaturalStoneBlocks.contains(state))
//
//		public boolean canSlopeAt(pos: BlockPos)(implicit w: World): Boolean =
//				canSlope && Option(state.getCollisionBoundingBox(w, pos)).forall(
//						_.maxY > 0.5 && w.getBlockState(pos.up).getCollisionBoundingBox(w, pos.up) == null)
//
//		public slopingCollisionBoxes(pos: BlockPos)(implicit w: World): Seq[AxisAlignedBB] = {
//				val height = blockHeight(pos)
//				OrdinalDirections.map(cornerBox(pos, _, height))
//		}
//
//		private def cornerBox(pos: BlockPos, d: Direction, blockHeight: Double)(implicit w: World) = {
//			val stepHeight = blockHeight - 0.5
//			val height = if(stepHigh(pos.add(d.x, 0,  0 ), stepHeight) &&
//					stepHigh(pos.add( 0 , 0, d.z), stepHeight) &&
//					stepHigh(pos.add(d.x, 0, d.z), stepHeight)) blockHeight else stepHeight
//			new AxisAlignedBB(pos.getX + max(0.0, d.x/2.0), pos.getY         , pos.getZ + max(0.0, d.z/2.0),
//					pos.getX + max(0.5, d.x    ), pos.getY + height, pos.getZ + max(0.5, d.z    ))
//		}
//
//		private boolean stepHigh(BlockPos pos, double stepHeight)(implicit w: World) = {
//			IBlockState neighbor = w.getBlockState(pos)
//			return neighbor.getBlock().isSolid && blockHeight(pos) >= stepHeight
//		}
	}

	private static double blockHeight(BlockPos pos, World w) {
		AxisAlignedBB box = w.getBlockState(pos).getCollisionBoundingBox(w, pos)
		if (box == null) return 0;
		else return box.maxY;
	}

	public static class EntityValue {

		private final Entity entity;

		public EntityValue(Entity entity) {
			this.entity = entity;
		}

		public boolean canUseSlope() {
			return entity instanceof EntityPlayer || entity instanceof EntityCreature;
		}

	}

//	repose.block.SlopingBlockExtensions net.minecraft.block.state.IBlockState addCollisionBoxToList   func_185908_a (Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
//	repose.block.SlopingBlockExtensions net.minecraft.block.state.IBlockState getCollisionBoundingBox func_185890_d (Lnet/minecraft/world/IBlockAccess;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/util/math/AxisAlignedBB;
//
//	repose.block.SlopingBlockExtensions             net.minecraft.entity.EntityLivingBase isEntityInsideOpaqueBlock func_70094_T  ()Z

}


