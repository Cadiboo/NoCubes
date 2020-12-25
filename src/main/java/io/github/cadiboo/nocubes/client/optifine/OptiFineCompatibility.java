package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author Cadiboo
 */
public final class OptiFineCompatibility {

	public static final boolean OPTIFINE_INSTALLED = OptiFineLocator.isOptiFineInstalledAndCompatible();

	public static void pushShaderThing(
			@Nonnull final IBlockState blockState,
			@Nonnull final BlockPos pos,
			@Nonnull final IBlockAccess reader,
			@Nonnull final BufferBuilder bufferBuilder
	) {
		if (!OPTIFINE_INSTALLED)
			return;
//		HardOptiFineCompatibility.pushShaderThing(blockState, pos, reader, bufferBuilder);
	}

//	public static void pushShaderThing(
//			@Nonnull final IFluidState fluidState,
//			@Nonnull final BlockPos pos,
//			@Nonnull final IBlockAccess reader,
//			@Nonnull final BufferBuilder bufferBuilder
//	) {
//		pushShaderThing(fluidState.getIBlockState(), pos, reader, bufferBuilder);
//	}

	public static void popShaderThing(@Nonnull final BufferBuilder bufferBuilder) {
//		if (!OPTIFINE_INSTALLED)
		return;
//		HardOptiFineCompatibility.popShaderThing(bufferBuilder);
	}

	public static boolean isChunkCacheOF(@Nonnull final IBlockAccess reader) {
//		if (!OPTIFINE_INSTALLED)
		return false;
//		return HardOptiFineCompatibility.isChunkCacheOF(reader);
	}

	@Nonnull
	public static ChunkCache getChunkRenderCache(@Nonnull final IBlockAccess reader) {
//		if (!OPTIFINE_INSTALLED)
		throw new OptiFineNotPresentException();
//		return HardOptiFineCompatibility.getChunkRenderCache(reader);
	}

	public static final class BufferBuilderOF {

		@Nonnull
		public static Object getRenderEnv(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
//			if (!OPTIFINE_INSTALLED)
			throw new OptiFineNotPresentException();
//			return HardOptiFineCompatibility.BufferBuilderOF.getRenderEnv(bufferBuilder, state, pos);
		}

	}

	public static final class BlockModelCustomizer {

		@Nonnull
		public static IBakedModel getRenderModel(
				@Nonnull final IBakedModel model,
				@Nonnull final IBlockState blockState,
				@Nonnull final Object renderEnv
		) {
//			if (!OPTIFINE_INSTALLED)
			throw new OptiFineNotPresentException();
//			return HardOptiFineCompatibility.BlockModelCustomizerOF.getRenderModel(model, blockState, renderEnv);
		}

		@Nonnull
		public static List<BakedQuad> getRenderQuads(
				@Nonnull final List<BakedQuad> quads,
				@Nonnull final IBlockAccess reader,
				@Nonnull final IBlockState blockState,
				@Nonnull final BlockPos pos,
				@Nonnull final EnumFacing direction,
				@Nonnull final BlockRenderLayer blockRenderLayer,
				final long rand,
				@Nonnull final Object renderEnv
		) {
//			if (!OPTIFINE_INSTALLED)
			throw new OptiFineNotPresentException();
//			return HardOptiFineCompatibility.BlockModelCustomizerOF.getRenderQuads(quads, reader, blockState, pos, direction, blockRenderLayer, rand, renderEnv);
		}

	}

}
