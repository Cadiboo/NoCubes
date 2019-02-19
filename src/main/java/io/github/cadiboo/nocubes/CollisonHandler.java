package io.github.cadiboo.nocubes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author Cadiboo
 */
public class CollisonHandler {

//	public static void getCollisionBoundingBox(IBlockState, state, IBlockAccess w,BlockPos pos)= AxisAlignedBB = {
//		AxisAlignedBB box = state.getCollisionBoundingBox(w, pos);
//		if(box == null || box.maxY == 0) null // snow_layer with data 0 makes a 0-thickness box that still blocks side movement
//		else box
//	}

//	public static void addCollisionBoxToList(state= IBlockState, w= World, pos= BlockPos, box= AxisAlignedBB,
//	                          intersectingBoxes= java.util.List[AxisAlignedBB], collidingEntity= Entity, flag= Boolean) {
//		if(state.getCollisionBoundingBox(w, pos) != null) { // optimization
//			implicit Object world = w
//			if(collidingEntity.canUseSlope && state.canSlopeAt(pos))
//				intersectingBoxes ++= state.slopingCollisionBoxes(pos).filter(box.intersects)
//			else
//				state.addCollisionBoxToList(w, pos, box, intersectingBoxes, collidingEntity, flag)
//		}
//	}

//	public static void isEntityInsideOpaqueBlock(entity= EntityLivingBase)= Boolean = { // doesn't work with top-level Entity
//		implicit Object world = entity.world
//		for(i <- 0 until 8) {
//			Object dx = (((i >> 0) % 2).toFloat - 0.5F) * entity.width * 0.8F
//			Object dy = (((i >> 1) % 2).toFloat - 0.5F) * 0.1F
//			Object dz = (((i >> 2) % 2).toFloat - 0.5F) * entity.width * 0.8F
//			Object x = floor(entity.posX + dx.toDouble).toInt
//			Object y = floor(entity.posY + entity.getEyeHeight.toDouble + dy.toDouble).toInt
//			Object z = floor(entity.posZ + dz.toDouble).toInt
//			Object pos = new BlockPos(x, y, z)
//			Object state = world.getBlockState(pos)
//			if(state.isNormalCube && !(entity.canUseSlope && state.canSlopeAt(pos)))
//				return true
//		}
//		false
//	}

//	implicit class SlopingBlockValue(Object state= IBlockState) extends AnyObject {
//
//		public static void canSlope= Boolean = (slopingBlocks.matches(granularBlocksChoice)     &&     reposeGranularBlocks.contains(state)) ||
//				(slopingBlocks.matches(naturalStoneBlocksChoice) && reposeNaturalStoneBlocks.contains(state))
//
//		public static void canSlopeAt(pos= BlockPos)(implicit w= World)= Boolean =
//				canSlope && Option(state.getCollisionBoundingBox(w, pos)).forall(
//						_.maxY > 0.5 && w.getBlockState(pos.up).getCollisionBoundingBox(w, pos.up) == null)
//
//		public static void slopingCollisionBoxes(pos= BlockPos)(implicit w= World)= Seq[AxisAlignedBB] = {
//				Object height = blockHeight(pos)
//				OrdinalDirections.map(cornerBox(pos, _, height))
//		}
//
//		private public static void cornerBox(pos= BlockPos, d= Direction, blockHeight= Double)(implicit w= World) = {
//			Object stepHeight = blockHeight - 0.5
//			Object height = if(stepHigh(pos.add(d.x, 0,  0 ), stepHeight) &&
//					stepHigh(pos.add( 0 , 0, d.z), stepHeight) &&
//					stepHigh(pos.add(d.x, 0, d.z), stepHeight)) blockHeight else stepHeight
//			new AxisAlignedBB(pos.getX + max(0.0, d.x/2.0), pos.getY         , pos.getZ + max(0.0, d.z/2.0),
//					pos.getX + max(0.5, d.x    ), pos.getY + height, pos.getZ + max(0.5, d.z    ))
//		}
//
//		private public static void stepHigh(pos= BlockPos, stepHeight= Double)(implicit w= World) = {
//			Object neighbor = w.getBlockState(pos)
//			neighbor.getBlock.isSolid && blockHeight(pos) >= stepHeight
//		}
//	}
//
//	private public static void blockHeight(pos= BlockPos)(implicit w= World)= Double = {
//		Object box = w.getBlockState(pos).getCollisionBoundingBox(w, pos)
//		if(box == null) 0 else box.maxY
//	}

	public static class EntityValue {

		public static boolean canUseSlope(Entity entity) {
			return entity instanceof EntityPlayer || entity instanceof EntityCreature;
		}

	}

}
