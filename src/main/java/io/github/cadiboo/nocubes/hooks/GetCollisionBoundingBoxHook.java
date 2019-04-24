//package io.github.cadiboo.nocubes.hooks;
//
//import io.github.cadiboo.nocubes.collision.CollisionHandler;
//import io.github.cadiboo.nocubes.config.ModConfig;
//import net.minecraft.block.Block;
//import net.minecraft.block.state.BlockStateContainer.StateImplementation;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.util.math.AxisAlignedBB;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
//
//import javax.annotation.Nullable;
//
///**
// * @author Cadiboo
// */
//@SuppressWarnings({
//		"unused", // Hooks get invoked by ASM redirects
//		"WeakerAccess" // Hooks need to be public to be invoked
//})
//public final class GetCollisionBoundingBoxHook {
//
//	@Nullable
//	public static AxisAlignedBB getCollisionBoundingBox(final Block block, final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
//		if (!ModConfig.enableCollisions) {
//			return getCollisionBoundingBoxDefault(state, worldIn, pos);
//		} else {
//			return CollisionHandler.getCollisionBoundingBox(block, state, worldIn, pos);
//		}
//	}
//
//	@Nullable
//	public static AxisAlignedBB getCollisionBoundingBoxDefault(final IBlockState state, final IBlockAccess worldIn, final BlockPos pos) {
//		runGetCollisionBoundingBoxDefaultOnce((StateImplementation) state);
//		return state.getCollisionBoundingBox(worldIn, pos);
//	}
//
//	private static void runGetCollisionBoundingBoxDefaultOnce(final StateImplementation state) {
//		// Filled with ASM
////		state.runGetCollisionBoundingBoxDefaultOnce = true;
//		throw new UnsupportedOperationException("This method should have been filled by ASM!");
//	}
//
//}
