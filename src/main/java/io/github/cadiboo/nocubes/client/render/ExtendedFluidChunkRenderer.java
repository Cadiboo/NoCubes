package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.LightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineProxy;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ThreadLocalArrayCache;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;

import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public final class ExtendedFluidChunkRenderer {

	private static final ThreadLocalArrayCache<boolean[]> IS_FLUID_SOURCE_THREAD_LOCAL = new ThreadLocalArrayCache<>(boolean[]::new, array -> array.length);

	public static void renderArea(RenderDispatcher.ChunkRenderInfo renderer, Predicate<IBlockState> isSmoothable, Mesher mesher, Area area, LightCache light) {
		PooledMutableBlockPos pos = PooledMutableBlockPos.retain();
		try (final ModProfiler ignored = ModProfiler.get().start("Render extended fluid chunk")) {
			final IBlockState[] blockCacheArray = area.getAndCacheBlocks();
//			final IFluidState[] fluidCacheArray = stateCache.getFluidStates();

			final int fluidCacheLength = blockCacheArray.length;

			boolean[] isFluidSource = IS_FLUID_SOURCE_THREAD_LOCAL.takeArray(fluidCacheLength);
			for (int i = 0; i < fluidCacheLength; ++i) {
//				isFluidSource[i] = fluidCacheArray[i].isSource();
				isFluidSource[i] = isSource(blockCacheArray[i]);
			}

			final int extendRange = NoCubesConfig.Server.extendFluidsRange;

			// For offset = -1 or -2 to offset = 1 or 2;
			final int maxXOffset = extendRange;
			final int maxZOffset = extendRange;

			final OptiFineProxy optiFine = OptiFineCompatibility.proxy();
			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 16; ++y) {
					for (int x = 0; x < 16; ++x) {
						pos.setPos(x, y, z);
						if (!isSmoothable.test(area.getBlockState(pos))) {
							continue;
						}

						OFFSET:
						for (int xOffset = -maxXOffset; xOffset <= maxXOffset; ++xOffset) {
							for (int zOffset = -maxZOffset; zOffset <= maxZOffset; ++zOffset) {

								//no point in checking myself
								if (xOffset == 0 && zOffset == 0) {
									continue;
								}

								int index = area.indexIfInsideCache(x + xOffset, y, z + zOffset);
								if (index == -1)
									continue;
								if (!isFluidSource[index]) {
									continue;
								}

								// Only render if block up is not solid
								pos.setPos(x + xOffset, y + 1, z + zOffset);
								if (area.getBlockState(pos).isOpaqueCube()) {
									continue;
								}

//								final IFluidState fluidState = fluidCacheArray[fluidStateIndex];
								final IBlockState fluidState = blockCacheArray[index];

								final BlockRenderLayer blockRenderLayer = ClientUtil.getCorrectRenderLayer(fluidState);
								final int blockRenderLayerOrdinal = blockRenderLayer.ordinal();

								BufferBuilder buffer = renderer.getAndStartBuffer(blockRenderLayerOrdinal, blockRenderLayer);
								final int worldX = area.start.getX() + x;
								final int worldY = area.start.getY() + y;
								final int worldZ = area.start.getZ() + z;
								optiFine.preRenderFluid(fluidState, pos.setPos(
										worldX,
										worldY,
										worldZ
								), renderer.world, buffer);
								try {
									renderer.usedLayers[blockRenderLayerOrdinal] |= ExtendedFluidBlockRenderer.renderExtendedFluid(
											worldX,
											worldY,
											worldZ,
											pos.setPos(
													worldX + xOffset,
													worldY,
													worldZ + zOffset
											),
											renderer.world,
											fluidState,
											buffer,
											renderer.dispatcher,
											light
									);
								} finally {
									optiFine.postRenderFluid(buffer);
								}

								break OFFSET;
							}
						}

					}
				}
			}
		} finally {
			pos.release();
		}
	}

	public static boolean isSource(final IBlockState state) {
		return state.getBlock() instanceof BlockLiquid && state.getValue(BlockLiquid.LEVEL) == 0;
	}

}
