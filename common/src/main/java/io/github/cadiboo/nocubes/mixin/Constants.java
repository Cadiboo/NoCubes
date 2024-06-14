package io.github.cadiboo.nocubes.mixin;

public interface Constants {
	/**
	 * To make extended fluids work, we need to redirect all getFluidState calls.
	 * <pre>
	 *     var blockState = world.getBlockState(pos);
	 *     var fluidState = blockState.getFluidState();
	 * </pre>
	 * <pre>
	 *     var blockState = world.getBlockState(pos);
	 *     var fluidState = ClientHooks.getRenderFluidState(pos, blockState);
	 * </pre>
	 */
	String EXTENDED_FLUIDS_BLOCK_POS_REF_NAME = "extendedFluidsBlockPosRef";
}
