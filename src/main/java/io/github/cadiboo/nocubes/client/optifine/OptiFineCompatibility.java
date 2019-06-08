package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.Region;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class OptiFineCompatibility {

	public static void pushShaderThing(final BlockState state, final BlockPos pos, final IWorldReader blockAccess, final BufferBuilder bufferBuilder) {
	}

	public static void pushShaderThing(final IFluidState state, final BlockPos pos, final IWorldReader blockAccess, final BufferBuilder bufferBuilder) {
		pushShaderThing(state.getBlockState(), pos, blockAccess, bufferBuilder);
	}

	public static void popShaderThing(@Nonnull final BufferBuilder bufferBuilder) {
	}

	public static boolean isChunkCacheOF(@Nonnull final IWorldReader reader) {
//		return reader instanceof ChunkCacheOF;
		return false;
	}

	@Nonnull
	public static Region getRegion(@Nonnull final IWorldReader reader) {
//		return ((ChunkCacheOF) reader).chunkCache;
		return null;
	}

}
