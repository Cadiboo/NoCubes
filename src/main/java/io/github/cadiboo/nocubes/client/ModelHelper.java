package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility.ENABLED;
import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;

/**
 * @author Cadiboo
 */
public final class ModelHelper {

	/**
	 * The order of {@link EnumFacing} and null used in getQuads
	 */
	public static final EnumFacing[] DIRECTION_QUADS_ORDERED = {
			UP, null, DOWN, NORTH, EAST, SOUTH, WEST,
	};
	public static final int DIRECTION_QUADS_ORDERED_LENGTH = DIRECTION_QUADS_ORDERED.length;

	@Nullable
	public static List<BakedQuad> getQuads(
			IBlockState state,
			final BlockPos pos,
			final BufferBuilder bufferBuilder,
			final IBlockAccess reader,
			final BlockRendererDispatcher blockRendererDispatcher,
//			final IModelData modelData,
//			final Random posRand,
			final long posRandLong,
			final BlockRenderLayer blockRenderLayer
	) {
		IBakedModel model = getModel(state, blockRendererDispatcher);

		Object renderEnv = null;

		if (ENABLED) {
//		    RenderEnv renderEnv = bufferBuilder.getRenderEnv(state, pos);
			renderEnv = OptiFineCompatibility.PROXY.getRenderEnv(bufferBuilder, state, pos);

			model = OptiFineCompatibility.PROXY.getRenderModel(model, state, renderEnv);
		}

		try (final ModProfiler ignored = ModProfiler.get().start("getExtendedState")) {
			state = state.getBlock().getExtendedState(state, reader, pos);
		}

		for (int directionIndex = 0; directionIndex < DIRECTION_QUADS_ORDERED_LENGTH; ++directionIndex) {
			final EnumFacing direction = DIRECTION_QUADS_ORDERED[directionIndex];
			List<BakedQuad> quads = model.getQuads(state, direction, posRandLong/*, modelData*/);
			if (quads.isEmpty()) {
				continue;
			}

			if (ENABLED) {
				try (final ModProfiler ignored = ModProfiler.get().start("getRenderQuads")) {
					quads = OptiFineCompatibility.PROXY.getRenderQuads(quads, reader, state, pos, direction, blockRenderLayer, posRandLong, renderEnv);
					if (quads.isEmpty()) {
						continue;
					}
				}
			}

			return quads;
		}

		return null;
	}

	/**
	 * Returns the model or the missing model if there isn't one
	 */
	@Nonnull
	public static IBakedModel getModel(final IBlockState state, final BlockRendererDispatcher blockRendererDispatcher) {
		try (final ModProfiler ignored = ModProfiler.get().start("getModel")) {
//			if (DynamicTreesCompatibility.isRootyBlock(unextendedState)) {
//				return blockRendererDispatcher.getModelForState(StateHolder.GRASS_BLOCK_DEFAULT);
//			}
			if (ClientUtil.isStateSnow(state)) {
				return blockRendererDispatcher.getModelForState(StateHolder.SNOW_LAYER_DEFAULT);
			}
			return blockRendererDispatcher.getModelForState(state);
		}
	}

}
