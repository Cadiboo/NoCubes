package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

/**
 * @author Cadiboo
 */
public final class OptiFineCompatibility {

	public static void pushShaderThing(final IBlockState state, final BlockPos pos, final IWorldReader blockAccess, final BufferBuilder bufferBuilder) {
	}

	public static void pushShaderThing(final IFluidState state, final BlockPos pos, final IWorldReader blockAccess, final BufferBuilder bufferBuilder) {
		pushShaderThing(state.getBlockState(), pos, blockAccess, bufferBuilder);
	}

	public static void popShaderThing(final BufferBuilder bufferBuilder) {
	}

}
