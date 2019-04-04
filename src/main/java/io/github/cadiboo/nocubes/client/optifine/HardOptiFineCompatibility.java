package io.github.cadiboo.nocubes.client.optifine;

import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.render.RenderEnv;
import net.optifine.shaders.SVertexBuilder;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

/**
 * @author Cadiboo
 */
final class HardOptiFineCompatibility {

	static final class SVertexBuilderOF {

		static void pushEntity(@Nonnull final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final IBlockReader blockAccess, @Nonnull final BufferBuilder worldRendererIn) {
			SVertexBuilder.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
		}

		static void popEntity(@Nonnull final BufferBuilder worldRendererIn) {
			SVertexBuilder.popEntity(worldRendererIn);
		}

	}

	static final class BufferBuilderOF {

		@Nonnull
		static Object getRenderEnv(@Nonnull final BufferBuilder bufferBuilder, @Nonnull final IBlockReader blockAccess, @Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
			return bufferBuilder.getRenderEnv(blockAccess, state, pos);
		}

	}

	static final class BlockModelCustomizerOF {

		static IBakedModel getRenderModel(@Nonnull final IBakedModel model, @Nonnull final IBlockState state, @Nonnull final Object renderEnv) {
			return BlockModelCustomizer.getRenderModel(model, state, (RenderEnv) renderEnv);
		}

		static List<BakedQuad> getRenderQuads(@Nonnull final List<BakedQuad> quads, @Nonnull final IBlockReader blockAccess, @Nonnull final IBlockState state, @Nonnull final BlockPos pos, @Nonnull final EnumFacing facing, @Nonnull final BlockRenderLayer blockRenderLayer, @Nonnull final Random rand, @Nonnull final Object renderEnv) {
			return BlockModelCustomizer.getRenderQuads(quads, blockAccess, state, pos, facing, blockRenderLayer, rand, (RenderEnv) renderEnv);
		}

	}

	static void pushShaderThing(@Nonnull final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final IBlockReader blockAccess, @Nonnull final BufferBuilder worldRendererIn) {
		if (Config.isShaders()) {
			SVertexBuilderOF.pushEntity(blockStateIn, blockPosIn, blockAccess, worldRendererIn);
		}
	}

	static void popShaderThing(@Nonnull final BufferBuilder worldRendererIn) {
		if (Config.isShaders()) {
			SVertexBuilderOF.popEntity(worldRendererIn);
		}
	}

}
