package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientEventSubscriber;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.LazyBlockColorCache;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.FluidBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.EmptyFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.biome.BiomeColors.IColorResolver;
import net.minecraftforge.client.ForgeHooksClient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES;
import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES_LENGTH;
import static net.minecraft.util.Direction.EAST;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Direction.WEST;

/**
 * @author Cadiboo
 */
public final class OptimisedFluidBlockRenderer {

	public static void renderChunk(
			@Nonnull final ChunkRender chunkRender, @Nonnull final BlockPos chunkRenderPos, @Nonnull final ChunkRenderTask generator, @Nonnull final CompiledChunk compiledChunk, @Nonnull final IEnviromentBlockReader chunkRenderCache, @Nonnull final boolean[] usedBlockRenderLayers, @Nonnull final Random random, @Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			final PooledMutableBlockPos pooledMutableBlockPos,
			final StateCache stateCache, final LazyPackedLightCache lazyPackedLightCache, final LazyBlockColorCache lazyBlockColorsCache,
			@Nullable final DensityCache densityCache
	) {
		final IFluidState[] fluidStateArray = stateCache.getFluidStates();
		final BlockState[] blockStateArray = stateCache.getBlockStates();
		final int stateCacheSizeX = stateCache.sizeX;
		final int stateCacheSizeY = stateCache.sizeY;
		final int stateCacheStartPaddingX = stateCache.startPaddingX;
		final int stateCacheStartPaddingY = stateCache.startPaddingY;
		final int stateCacheStartPaddingZ = stateCache.startPaddingZ;

		final float[] densityArray;
		final int densityCacheSizeX;
		final int densityCacheSizeY;
		final int densityCacheStartPaddingX;
		final int densityCacheStartPaddingY;
		final int densityCacheStartPaddingZ;
		if (densityCache == null) {
			densityArray = null;
			densityCacheSizeX = densityCacheSizeY = densityCacheStartPaddingX = densityCacheStartPaddingY = densityCacheStartPaddingZ = 0;
		} else {
			densityArray = densityCache.getDensityCache();
			densityCacheSizeX = densityCache.sizeX;
			densityCacheSizeY = densityCache.sizeY;
			densityCacheStartPaddingX = densityCache.startPaddingX;
			densityCacheStartPaddingY = densityCache.startPaddingY;
			densityCacheStartPaddingZ = densityCache.startPaddingZ;
		}

		final SmoothLightingFluidBlockRenderer smoothLightingFluidBlockRenderer = ClientEventSubscriber.smoothLightingBlockFluidRenderer;
		final FluidBlockRenderer fluidRenderer = blockRendererDispatcher.fluidRenderer;
		if (smoothLightingFluidBlockRenderer == null || fluidRenderer == null) {
			final String renderer = fluidRenderer == null ? "Vanilla Fluid Renderer" : "Smooth Lighting Fluid Renderer";
			final CrashReport crashReport = CrashReport.makeCrashReport(new NullPointerException(), renderer + " is null!");
			crashReport.makeCategory("Rendering chunk");
			throw new ReportedException(crashReport);
		}

		// Use fluidRenderer sprites instead of smoothLightingFluidBlockRenderer sprites for compatibility
		final TextureAtlasSprite atlasSpriteWaterOverlay = fluidRenderer.atlasSpriteWaterOverlay;
		final TextureAtlasSprite[] atlasSpritesLava = fluidRenderer.atlasSpritesLava;
		final TextureAtlasSprite[] atlasSpritesWater = fluidRenderer.atlasSpritesWater;

		final int[] lazyPackedLightCacheCache = lazyPackedLightCache.cache;
		final int lazyPackedLightCacheSizeX = lazyPackedLightCache.sizeX;
		final int lazyPackedLightCacheSizeY = lazyPackedLightCache.sizeY;
		final int lazyPackedLightCacheStartPaddingX = lazyPackedLightCache.startPaddingX;
		final int lazyPackedLightCacheStartPaddingZ = lazyPackedLightCache.startPaddingZ;
		final int lazyPackedLightCacheStartPaddingY = lazyPackedLightCache.startPaddingY;
		final int lazyPackedLightCacheDiffX = stateCache.startPaddingX - lazyPackedLightCacheStartPaddingX;
		final int lazyPackedLightCacheDiffY = stateCache.startPaddingY - lazyPackedLightCacheStartPaddingY;
		final int lazyPackedLightCacheDiffZ = stateCache.startPaddingZ - lazyPackedLightCacheStartPaddingZ;
		final IEnviromentBlockReader lazyPackedLightCacheReader = lazyPackedLightCache.reader;

		final int[] lazyBlockColorsCacheCache = lazyBlockColorsCache.cache;
		final int lazyBlockColorsCacheSizeX = lazyBlockColorsCache.sizeX;
		final int lazyBlockColorsCacheSizeY = lazyBlockColorsCache.sizeY;
		final int lazyBlockColorsCacheStartPaddingX = lazyBlockColorsCache.startPaddingX;
		final int lazyBlockColorsCacheStartPaddingZ = lazyBlockColorsCache.startPaddingZ;
		final int lazyBlockColorsCacheStartPaddingY = lazyBlockColorsCache.startPaddingY;
		final int biomeBlendRadius = Minecraft.getInstance().gameSettings.biomeBlendRadius;
		final int d = biomeBlendRadius * 2 + 1;
		final int lazyBlockColorsCacheArea = d * d;
		final int lazyBlockColorsCacheMax = biomeBlendRadius + 1;
		final IEnviromentBlockReader lazyBlockColorsCacheReader = lazyBlockColorsCache.reader;
		final IColorResolver lazyBlockColorsCacheColorResolver = lazyBlockColorsCache.colorResolver;

		for (int z = 0; z < 16; ++z) {
			for (int y = 0; y < 16; ++y) {
				for (int x = 0; x < 16; ++x) {
					final int index = stateCache.getIndex(
							stateCacheStartPaddingX + x,
							stateCacheStartPaddingY + y,
							stateCacheStartPaddingZ + z,
							stateCacheSizeX, stateCacheSizeY
					);
					final IFluidState fluidState = fluidStateArray[index];
					if (fluidState.isEmpty()) {
						continue;
					}

					pooledMutableBlockPos.setPos(chunkRenderPosX + x, chunkRenderPosY + y, chunkRenderPosZ + z);

					for (int i = 0; i < BLOCK_RENDER_LAYER_VALUES_LENGTH; i++) {
						final BlockRenderLayer initialBlockRenderLayer = BLOCK_RENDER_LAYER_VALUES[i];
						if (!fluidState.canRenderInLayer(initialBlockRenderLayer)) {
							continue;
						}
						final BlockRenderLayer correctedBlockRenderLayer = ClientUtil.getCorrectRenderLayer(initialBlockRenderLayer);
						final int correctedBlockRenderLayerOrdinal = correctedBlockRenderLayer.ordinal();
						ForgeHooksClient.setRenderLayer(correctedBlockRenderLayer);

						final BufferBuilder bufferBuilder = ClientUtil.startOrContinueBufferBuilder(generator, correctedBlockRenderLayerOrdinal, compiledChunk, correctedBlockRenderLayer, chunkRender, chunkRenderPos);

						OptiFineCompatibility.get().pushShaderEntity(fluidState, pooledMutableBlockPos, chunkRenderCache, bufferBuilder);
						try {
							usedBlockRenderLayers[correctedBlockRenderLayerOrdinal] |= renderBlock(
									smoothLightingFluidBlockRenderer,
									chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
									x, y, z,
//									index,
									fluidStateArray, blockStateArray,
									stateCacheSizeX, stateCacheSizeY,
									stateCacheStartPaddingX, stateCacheStartPaddingY, stateCacheStartPaddingZ,
									stateCache,

									densityArray,
									densityCacheSizeX, densityCacheSizeY,
									densityCacheStartPaddingX, densityCacheStartPaddingY, densityCacheStartPaddingZ,
									densityCache,

									lazyPackedLightCache,
									lazyPackedLightCacheCache,
									lazyPackedLightCacheSizeX,
									lazyPackedLightCacheSizeY,
									lazyPackedLightCacheStartPaddingX,
									lazyPackedLightCacheStartPaddingY,
									lazyPackedLightCacheStartPaddingZ,
									lazyPackedLightCacheDiffX,
									lazyPackedLightCacheDiffY,
									lazyPackedLightCacheDiffZ,
									lazyPackedLightCacheReader,

									lazyBlockColorsCache,
									lazyBlockColorsCacheCache,
									lazyBlockColorsCacheSizeX,
									lazyBlockColorsCacheSizeY,
									lazyBlockColorsCacheStartPaddingX,
									lazyBlockColorsCacheStartPaddingY,
									lazyBlockColorsCacheStartPaddingZ,
									biomeBlendRadius,
									lazyBlockColorsCacheArea,
									lazyBlockColorsCacheMax,
									lazyBlockColorsCacheReader,
									lazyBlockColorsCacheColorResolver,

									fluidState,
//									random,
									bufferBuilder,
									chunkRenderCache,
									pooledMutableBlockPos,
									atlasSpriteWaterOverlay, atlasSpritesLava, atlasSpritesWater
							);
						} finally {
							OptiFineCompatibility.get().popShaderEntity(bufferBuilder);
						}
					}

				}
			}
		}

	}

	private static boolean renderBlock(
			final SmoothLightingFluidBlockRenderer fluidRenderer,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			final int relativeX, final int relativeY, final int relativeZ,
//			final int fluidStateArrayIndex,
			final IFluidState[] fluidStateArray,
			final BlockState[] blockStateArray,
			final int stateCacheSizeX, final int stateCacheSizeY,
			final int stateCacheStartPaddingX, final int stateCacheStartPaddingY, final int stateCacheStartPaddingZ,
			final StateCache stateCache,

			final float[] densityArray,
			final int densityCacheSizeX, final int densityCacheSizeY,
			final int densityCacheStartPaddingX, final int densityCacheStartPaddingY, final int densityCacheStartPaddingZ,
			@Nullable final DensityCache densityCache,

			final LazyPackedLightCache lazyPackedLightCache,
			final int[] lazyPackedLightCacheCache,
			final int lazyPackedLightCacheSizeX,
			final int lazyPackedLightCacheSizeY,
			final int lazyPackedLightCacheStartPaddingX,
			final int lazyPackedLightCacheStartPaddingY,
			final int lazyPackedLightCacheStartPaddingZ,
			final int lazyPackedLightCacheDiffX,
			final int lazyPackedLightCacheDiffY,
			final int lazyPackedLightCacheDiffZ,
			final IEnviromentBlockReader lazyPackedLightCacheReader,

			final LazyBlockColorCache lazyBlockColorsCache,
			final int[] lazyBlockColorsCacheCache,
			final int lazyBlockColorsCacheSizeX,
			final int lazyBlockColorsCacheSizeY,
			final int lazyBlockColorsCacheStartPaddingX,
			final int lazyBlockColorsCacheStartPaddingY,
			final int lazyBlockColorsCacheStartPaddingZ,
			final int biomeBlendRadius,
			final int lazyBlockColorsCacheArea,
			final int lazyBlockColorsCacheMax,
			final IEnviromentBlockReader lazyBlockColorsCacheReader,
			final IColorResolver lazyBlockColorsCacheColorResolver,

			final IFluidState state,
//			final Random random,
			final BufferBuilder buffer,
			final IEnviromentBlockReader chunkRenderCache,
			final PooledMutableBlockPos pooledMutableBlockPos,
			final TextureAtlasSprite atlasSpriteWaterOverlay, final TextureAtlasSprite[] atlasSpritesLava, final TextureAtlasSprite[] atlasSpritesWater
	) {
		final int x = chunkRenderPosX + relativeX;
		final int y = chunkRenderPosY + relativeY;
		final int z = chunkRenderPosZ + relativeZ;

		final int stateCacheOffsetX = stateCacheStartPaddingX + relativeX;
		final int stateCacheOffsetY = stateCacheStartPaddingY + relativeY;
		final int stateCacheOffsetZ = stateCacheStartPaddingZ + relativeZ;

		final int lazyPackedLightCacheOffsetX = lazyPackedLightCacheStartPaddingX + relativeX;
		final int lazyPackedLightCacheOffsetY = lazyPackedLightCacheStartPaddingY + relativeY;
		final int lazyPackedLightCacheOffsetZ = lazyPackedLightCacheStartPaddingZ + relativeZ;

		final int lazyBlockColorsCacheOffsetX = lazyBlockColorsCacheStartPaddingX + relativeX;
		final int lazyBlockColorsCacheOffsetY = lazyBlockColorsCacheStartPaddingY + relativeY;
		final int lazyBlockColorsCacheOffsetZ = lazyBlockColorsCacheStartPaddingZ + relativeZ;

		final int densityCacheOffsetX = densityCacheStartPaddingX + relativeX;
		final int densityCacheOffsetY = densityCacheStartPaddingY + relativeY;
		final int densityCacheOffsetZ = densityCacheStartPaddingZ + relativeZ;

		final Fluid fluid = state.getFluid();

		final int upIndex = stateCache.getIndex(
				stateCacheOffsetX,
				stateCacheOffsetY + 1,
				stateCacheOffsetZ,
				stateCacheSizeX, stateCacheSizeY
		);
		final IFluidState upFluidState = fluidStateArray[upIndex];
		final BlockState upBlockState = blockStateArray[upIndex];
		final int downIndex = stateCache.getIndex(
				stateCacheOffsetX,
				stateCacheOffsetY - 1,
				stateCacheOffsetZ,
				stateCacheSizeX, stateCacheSizeY
		);
		final IFluidState downFluidState = fluidStateArray[downIndex];
		final BlockState downBlockState = blockStateArray[downIndex];
		final int northIndex = stateCache.getIndex(
				stateCacheOffsetX,
				stateCacheOffsetY,
				stateCacheOffsetZ - 1,
				stateCacheSizeX, stateCacheSizeY
		);
		final IFluidState northFluidState = fluidStateArray[northIndex];
		final BlockState northBlockState = blockStateArray[northIndex];
		final int southIndex = stateCache.getIndex(
				stateCacheOffsetX,
				stateCacheOffsetY,
				stateCacheOffsetZ + 1,
				stateCacheSizeX, stateCacheSizeY
		);
		final IFluidState southFluidState = fluidStateArray[southIndex];
		final BlockState southBlockState = blockStateArray[southIndex];
		final int westIndex = stateCache.getIndex(
				stateCacheOffsetX - 1,
				stateCacheOffsetY,
				stateCacheOffsetZ,
				stateCacheSizeX, stateCacheSizeY
		);
		final IFluidState westFluidState = fluidStateArray[westIndex];
		final BlockState westBlockState = blockStateArray[westIndex];
		final int eastIndex = stateCache.getIndex(
				stateCacheOffsetX + 1,
				stateCacheOffsetY,
				stateCacheOffsetZ,
				stateCacheSizeX, stateCacheSizeY
		);
		final IFluidState eastFluidState = fluidStateArray[eastIndex];
		final BlockState eastBlockState = blockStateArray[eastIndex];

		boolean shouldRenderDown = !downFluidState.getFluid().isEquivalentTo(fluid) && !downBlockState.isSolid();
		boolean shouldRenderUp = !upFluidState.getFluid().isEquivalentTo(fluid) && !upBlockState.isSolid();
		boolean shouldRenderNorth = !northFluidState.getFluid().isEquivalentTo(fluid) && !northBlockState.isSolid();
		boolean shouldRenderSouth = !southFluidState.getFluid().isEquivalentTo(fluid) && !southBlockState.isSolid();
		boolean shouldRenderWest = !westFluidState.getFluid().isEquivalentTo(fluid) && !westBlockState.isSolid();
		boolean shouldRenderEast = !eastFluidState.getFluid().isEquivalentTo(fluid) && !eastBlockState.isSolid();

		if (densityCache != null) {
			// > -6 means that the block is very outside the isosurface
			shouldRenderDown &= densityArray[densityCache.getIndex(
					densityCacheOffsetX,
					densityCacheOffsetY - 1,
					densityCacheOffsetZ,
					densityCacheSizeX, densityCacheSizeY
			)] > -6;
			shouldRenderUp &= densityArray[densityCache.getIndex(
					densityCacheOffsetX,
					densityCacheOffsetY + 1,
					densityCacheOffsetZ,
					densityCacheSizeX, densityCacheSizeY
			)] > -6;
			shouldRenderNorth &= densityArray[densityCache.getIndex(
					densityCacheOffsetX,
					densityCacheOffsetY,
					densityCacheOffsetZ - 1,
					densityCacheSizeX, densityCacheSizeY
			)] > -6;
			shouldRenderSouth &= densityArray[densityCache.getIndex(
					densityCacheOffsetX,
					densityCacheOffsetY,
					densityCacheOffsetZ + 1,
					densityCacheSizeX, densityCacheSizeY
			)] > -6;
			shouldRenderWest &= densityArray[densityCache.getIndex(
					densityCacheOffsetX - 1,
					densityCacheOffsetY,
					densityCacheOffsetZ,
					densityCacheSizeX, densityCacheSizeY
			)] > -6;
			shouldRenderEast &= densityArray[densityCache.getIndex(
					densityCacheOffsetX + 1,
					densityCacheOffsetY,
					densityCacheOffsetZ,
					densityCacheSizeX, densityCacheSizeY
			)] > -6;
		}

		if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
			return false;
		}

		final boolean isLava = state.isTagged(FluidTags.LAVA);
		final TextureAtlasSprite[] atextureatlassprite = isLava ? atlasSpritesLava : atlasSpritesWater;

		final float red;
		final float green;
		final float blue;
		if (isLava) {
			red = 1.0F;
			green = 1.0F;
			blue = 1.0F;
		} else {
			final int waterColor = getWaterColor(
					lazyBlockColorsCacheOffsetX, lazyBlockColorsCacheOffsetY, lazyBlockColorsCacheOffsetZ,
					chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
					lazyBlockColorsCache,
					lazyBlockColorsCacheCache,
					lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
					biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
					lazyBlockColorsCacheReader,
					lazyBlockColorsCacheColorResolver,
					pooledMutableBlockPos
			);
			red = (float) (waterColor >> 16 & 0xFF) / 255.0F;
			green = (float) (waterColor >> 8 & 0xFF) / 255.0F;
			blue = (float) (waterColor & 0xFF) / 255.0F;
		}

		boolean wasAnythingRendered = false;

		final float fluidHeight = getFluidHeight(
				fluidStateArray, blockStateArray, stateCache,
				stateCacheSizeX, stateCacheSizeY,
				stateCacheOffsetX, stateCacheOffsetY, stateCacheOffsetZ,
				fluid
		);
		final float fluidHeightSouth = getFluidHeight(
				fluidStateArray, blockStateArray, stateCache,
				stateCacheSizeX, stateCacheSizeY,
				stateCacheOffsetX, stateCacheOffsetY, stateCacheOffsetZ + 1,
				fluid
		);
		final float fluidHeightEastSouth = getFluidHeight(
				fluidStateArray, blockStateArray, stateCache,
				stateCacheSizeX, stateCacheSizeY,
				stateCacheOffsetX + 1, stateCacheOffsetY, stateCacheOffsetZ + 1,
				fluid
		);
		final float fluidHeightEast = getFluidHeight(
				fluidStateArray, blockStateArray, stateCache,
				stateCacheSizeX, stateCacheSizeY,
				stateCacheOffsetX + 1, stateCacheOffsetY, stateCacheOffsetZ,
				fluid
		);

//			final double x = x;
//			final double y = y;
//			final double z = z;

		final boolean smoothLighting = fluidRenderer.smoothLighting();
		final boolean colors = fluidRenderer.colors();

		if (shouldRenderUp/* && !func_209556_a_optimised(worldIn, Direction.UP, Math.min(Math.min(fluidHeight, fluidHeightSouth), Math.min(fluidHeightEastSouth, fluidHeightEast)), pooledMutableBlockPos.setPos(x, y + 1, z))*/) {

			// Commented out to fix transparent lines between bottom of sides.
			// The only reason that I can think of for this code to exist in the first place
			// is to try and solve z-fighting issues.
//				fluidHeight -= 0.001F;
//				fluidHeightSouth -= 0.001F;
//				fluidHeightEastSouth -= 0.001F;
//				fluidHeightEast -= 0.001F;

			final int light0;
			final int light1;
			final int light2;
			final int light3;

			final float red0;
			final float green0;
			final float blue0;
			final float red1;
			final float green1;
			final float blue1;
			final float red2;
			final float green2;
			final float blue2;
			final float red3;
			final float green3;
			final float blue3;

			if (isLava) {
				light0 = light1 = light2 = light3 = 0x00F000F0; // 240, 240
				red0 = red1 = red2 = red3 = 1.0F;
				green0 = green1 = green2 = green3 = 1.0F;
				blue0 = blue1 = blue2 = blue3 = 1.0F;
			} else {
				if (!smoothLighting) {
//					final int combinedLightUpMax = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, y, z));
					final int combinedLightUpMax = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX, lazyPackedLightCacheOffsetY, lazyPackedLightCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
					light0 = combinedLightUpMax;
					light1 = combinedLightUpMax;
					light2 = combinedLightUpMax;
					light3 = combinedLightUpMax;
				} else {
					light0 = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX, lazyPackedLightCacheOffsetY, lazyPackedLightCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
					// south
					light1 = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX, lazyPackedLightCacheOffsetY, lazyPackedLightCacheOffsetZ + 1,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
					// east south
					light2 = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX + 1, lazyPackedLightCacheOffsetY, lazyPackedLightCacheOffsetZ + 1,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
					// east
					light3 = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX + 1, lazyPackedLightCacheOffsetY, lazyPackedLightCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
				}
				if (!colors) {
					red0 = red1 = red2 = red3 = red;
					green0 = green1 = green2 = green3 = green;
					blue0 = blue1 = blue2 = blue3 = blue;
				} else {
//					final int waterColor0 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x, y, z));
//					red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
//					green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
//					blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
					red0 = red;
					green0 = green;
					blue0 = blue;
					// south
//					final int waterColor1 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x, y, z + 1));
					final int waterColor1 = getWaterColor(
							lazyBlockColorsCacheOffsetX, lazyBlockColorsCacheOffsetY, lazyBlockColorsCacheOffsetZ + 1,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							lazyBlockColorsCache,
							lazyBlockColorsCacheCache,
							lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
							biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
							lazyBlockColorsCacheReader,
							lazyBlockColorsCacheColorResolver,
							pooledMutableBlockPos
					);
					red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
					green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
					blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
					// east south
//					final int waterColor2 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x + 1, y, z + 1));
					final int waterColor2 = getWaterColor(
							lazyBlockColorsCacheOffsetX + 1, lazyBlockColorsCacheOffsetY, lazyBlockColorsCacheOffsetZ + 1,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							lazyBlockColorsCache,
							lazyBlockColorsCacheCache,
							lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
							biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
							lazyBlockColorsCacheReader,
							lazyBlockColorsCacheColorResolver,
							pooledMutableBlockPos
					);
					red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
					green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
					blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
					// east
//					final int waterColor3 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x + 1, y, z));
					final int waterColor3 = getWaterColor(
							lazyBlockColorsCacheOffsetX + 1, lazyBlockColorsCacheOffsetY, lazyBlockColorsCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							lazyBlockColorsCache,
							lazyBlockColorsCacheCache,
							lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
							biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
							lazyBlockColorsCacheReader,
							lazyBlockColorsCacheColorResolver,
							pooledMutableBlockPos
					);
					red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
					green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
					blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
				}
			}
			wasAnythingRendered |= fluidRenderer.renderUp(
					buffer, atextureatlassprite,
					red0, green0, blue0,
					red1, green1, blue1,
					red2, green2, blue2,
					red3, green3, blue3,
					fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
					x, y, z,
					light0, light1, light2, light3,
					state.shouldRenderSides(chunkRenderCache, pooledMutableBlockPos.setPos(x, y + 1, z)), state.getFlow(chunkRenderCache, pooledMutableBlockPos.setPos(x, y, z)), MathHelper.getCoordinateRandom(x, y, z)
//					true, Vec3d.ZERO, 0
			);
		}

		if (shouldRenderDown) {
			final int light0;
			final int light1;
			final int light2;
			final int light3;

			final float red0;
			final float green0;
			final float blue0;
			final float red1;
			final float green1;
			final float blue1;
			final float red2;
			final float green2;
			final float blue2;
			final float red3;
			final float green3;
			final float blue3;

			if (isLava) {
				light0 = light1 = light2 = light3 = 0x00F000F0; // 240, 240
				red0 = red1 = red2 = red3 = 1.0F;
				green0 = green1 = green2 = green3 = 1.0F;
				blue0 = blue1 = blue2 = blue3 = 1.0F;
			} else {
				final int lazyPackedLightCacheOffsetYMinus1 = lazyPackedLightCacheOffsetY - 1;
				if (!smoothLighting) {
//					final int downCombinedLightUpMax = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z));
					final int downCombinedLightUpMax = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX, lazyPackedLightCacheOffsetYMinus1, lazyPackedLightCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
					light0 = downCombinedLightUpMax;
					light1 = downCombinedLightUpMax;
					light2 = downCombinedLightUpMax;
					light3 = downCombinedLightUpMax;
				} else {
					// down south
//					light0 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z + 1));
					light0 = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX, lazyPackedLightCacheOffsetYMinus1, lazyPackedLightCacheOffsetZ + 1,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
					// down
//					light1 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z));
					light1 = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX, lazyPackedLightCacheOffsetYMinus1, lazyPackedLightCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
					// down east
//					light2 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, ym1, z));
					light2 = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX + 1, lazyPackedLightCacheOffsetYMinus1, lazyPackedLightCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
					// down east south
//					light3 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, ym1, z + 1));
					light3 = getCombinedLightUpMax(
							lazyPackedLightCacheOffsetX + 1, lazyPackedLightCacheOffsetYMinus1, lazyPackedLightCacheOffsetZ + 1,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							stateCache,
							stateCacheSizeX, stateCacheSizeY,
							lazyPackedLightCache,
							lazyPackedLightCacheCache,
							lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
							lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
							lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
							lazyPackedLightCacheReader,
							pooledMutableBlockPos
					);
				}
				if (!colors) {
					red0 = red1 = red2 = red3 = red;
					green0 = green1 = green2 = green3 = green;
					blue0 = blue1 = blue2 = blue3 = blue;
				} else {
					// down south
					final int waterColor0 = getWaterColor(
							lazyBlockColorsCacheOffsetX, lazyBlockColorsCacheOffsetY - 1, lazyBlockColorsCacheOffsetZ + 1,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							lazyBlockColorsCache,
							lazyBlockColorsCacheCache,
							lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
							biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
							lazyBlockColorsCacheReader,
							lazyBlockColorsCacheColorResolver,
							pooledMutableBlockPos
					);
					red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
					green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
					blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
					// down
					final int waterColor1 = getWaterColor(
							lazyBlockColorsCacheOffsetX, lazyBlockColorsCacheOffsetY - 1, lazyBlockColorsCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							lazyBlockColorsCache,
							lazyBlockColorsCacheCache,
							lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
							biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
							lazyBlockColorsCacheReader,
							lazyBlockColorsCacheColorResolver,
							pooledMutableBlockPos
					);
					red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
					green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
					blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
					// down east
					final int waterColor2 = getWaterColor(
							lazyBlockColorsCacheOffsetX + 1, lazyBlockColorsCacheOffsetY - 1, lazyBlockColorsCacheOffsetZ,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							lazyBlockColorsCache,
							lazyBlockColorsCacheCache,
							lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
							biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
							lazyBlockColorsCacheReader,
							lazyBlockColorsCacheColorResolver,
							pooledMutableBlockPos
					);
					red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
					green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
					blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
					// down east south
					final int waterColor3 = getWaterColor(
							lazyBlockColorsCacheOffsetX + 1, lazyBlockColorsCacheOffsetY - 1, lazyBlockColorsCacheOffsetZ + 1,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							lazyBlockColorsCache,
							lazyBlockColorsCacheCache,
							lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
							biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
							lazyBlockColorsCacheReader,
							lazyBlockColorsCacheColorResolver,
							pooledMutableBlockPos
					);
					red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
					green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
					blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
				}
			}
			wasAnythingRendered |= fluidRenderer.renderDown(
					light0, light1, light2, light3,
					buffer, atextureatlassprite[0],
					red0, green0, blue0,
					red1, green1, blue1,
					red2, green2, blue2,
					red3, green3, blue3,
					x, y, z
			);
		}

		for (int facingIndex = 0; facingIndex < 4; ++facingIndex) {
			final float y0;
			final float y1;
			final int x0;
			final int z0;
			final int x1;
			final int z1;
			final Direction direction;
			final boolean shouldRenderSide;
			if (facingIndex == 0) {
				y0 = fluidHeight;
				y1 = fluidHeightEast;
				x0 = x;
				x1 = x + 1;
				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for this code to exist in the first place
				// is to try and solve z-fighting issues.
				z0 = z;// + (double) 0.001F;
				z1 = z;// + (double) 0.001F;
				direction = NORTH;
				shouldRenderSide = shouldRenderNorth;
			} else if (facingIndex == 1) {
				y0 = fluidHeightEastSouth;
				y1 = fluidHeightSouth;
				x0 = x + 1;
				x1 = x;
				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for this code to exist in the first place
				// is to try and solve z-fighting issues.
				z0 = z + 1;// - (double) 0.001F;
				z1 = z + 1;// - (double) 0.001F;
				direction = SOUTH;
				shouldRenderSide = shouldRenderSouth;
			} else if (facingIndex == 2) {
				y0 = fluidHeightSouth;
				y1 = fluidHeight;
				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for this code to exist in the first place
				// is to try and solve z-fighting issues.
				x0 = x;// + (double) 0.001F;
				x1 = x;// + (double) 0.001F;
				z0 = z + 1;
				z1 = z;
				direction = WEST;
				shouldRenderSide = shouldRenderWest;
			} else {
				y0 = fluidHeightEast;
				y1 = fluidHeightEastSouth;
				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for this code to exist in the first place
				// is to try and solve z-fighting issues.
				x0 = x + 1;// - (double) 0.001F;
				x1 = x + 1;// - (double) 0.001F;
				z0 = z;
				z1 = z + 1;
				direction = EAST;
				shouldRenderSide = shouldRenderEast;
			}

			pooledMutableBlockPos.setPos(x, y, z).move(direction);

			final int offsetRelativeX = pooledMutableBlockPos.getX() - chunkRenderPosX;
			final int offsetRelativeY = pooledMutableBlockPos.getY() - chunkRenderPosY;
			final int offsetRelativeZ = pooledMutableBlockPos.getZ() - chunkRenderPosZ;

			if (shouldRenderSide) {// && !func_209556_a_optimised(worldIn, direction, Math.max(y0, y1), pooledMutableBlockPos)) {
				TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
				if (!isLava) {
					Block block = blockStateArray[stateCache.getIndex(
							stateCacheStartPaddingX + offsetRelativeX,
							stateCacheStartPaddingY + offsetRelativeY,
							stateCacheStartPaddingZ + offsetRelativeZ,
							stateCacheSizeX, stateCacheSizeY
					)].getBlock();
//					Block block = worldIn.getBlockState(pooledMutableBlockPos).getBlock();
					if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
						textureatlassprite2 = atlasSpriteWaterOverlay;
					}
				}

				final int light0;
				final int light1;
				final int light2;
				final int light3;

				final float red0;
				final float green0;
				final float blue0;
				final float red1;
				final float green1;
				final float blue1;
				final float red2;
				final float green2;
				final float blue2;
				final float red3;
				final float green3;
				final float blue3;

				if (isLava) {
					light0 = light1 = light2 = light3 = 0x00F000F0; // 240, 240
					red0 = red1 = red2 = red3 = 1.0F;
					green0 = green1 = green2 = green3 = 1.0F;
					blue0 = blue1 = blue2 = blue3 = 1.0F;
				} else {
					if (!smoothLighting) {
//						final int combinedLightUpMax = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos);
						final int combinedLightUpMax = getCombinedLightUpMax(
								lazyPackedLightCacheStartPaddingX + offsetRelativeX, lazyPackedLightCacheStartPaddingY + offsetRelativeY, lazyPackedLightCacheStartPaddingZ + offsetRelativeZ,
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								stateCache,
								stateCacheSizeX, stateCacheSizeY,
								lazyPackedLightCache,
								lazyPackedLightCacheCache,
								lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
								lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
								lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
								lazyPackedLightCacheReader,
								pooledMutableBlockPos
						);
						light0 = combinedLightUpMax;
						light1 = combinedLightUpMax;
						light2 = combinedLightUpMax;
						light3 = combinedLightUpMax;
					} else {
//						light0 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0));
						light0 = getCombinedLightUpMax(
								lazyPackedLightCacheStartPaddingX + (x0 - chunkRenderPosX), lazyPackedLightCacheStartPaddingY + (MathHelper.floor(y + y0) - chunkRenderPosY), lazyPackedLightCacheStartPaddingZ + (z0 - chunkRenderPosZ),
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								stateCache,
								stateCacheSizeX, stateCacheSizeY,
								lazyPackedLightCache,
								lazyPackedLightCacheCache,
								lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
								lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
								lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
								lazyPackedLightCacheReader,
								pooledMutableBlockPos
						);
//						light1 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1));
						light1 = getCombinedLightUpMax(lazyPackedLightCacheStartPaddingX + (x1 - chunkRenderPosX), lazyPackedLightCacheStartPaddingY + (MathHelper.floor(y + y1) - chunkRenderPosY), lazyPackedLightCacheStartPaddingZ + (z1 - chunkRenderPosZ),
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								stateCache,
								stateCacheSizeX, stateCacheSizeY,
								lazyPackedLightCache,
								lazyPackedLightCacheCache,
								lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
								lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
								lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
								lazyPackedLightCacheReader,
								pooledMutableBlockPos
						);
//						light2 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x1, y, z1));
						light2 = getCombinedLightUpMax(lazyPackedLightCacheStartPaddingX + (x1 - chunkRenderPosX), lazyPackedLightCacheStartPaddingY + (y - chunkRenderPosY), lazyPackedLightCacheStartPaddingZ + (z1 - chunkRenderPosZ),
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								stateCache,
								stateCacheSizeX, stateCacheSizeY,
								lazyPackedLightCache,
								lazyPackedLightCacheCache,
								lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
								lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
								lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
								lazyPackedLightCacheReader,
								pooledMutableBlockPos
						);
//						light3 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x0, y, z0));
						light3 = getCombinedLightUpMax(lazyPackedLightCacheStartPaddingX + (x0 - chunkRenderPosX), lazyPackedLightCacheStartPaddingY + (y - chunkRenderPosY), lazyPackedLightCacheStartPaddingZ + (z0 - chunkRenderPosZ),
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								stateCache,
								stateCacheSizeX, stateCacheSizeY,
								lazyPackedLightCache,
								lazyPackedLightCacheCache,
								lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY,
								lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ,
								lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ,
								lazyPackedLightCacheReader,
								pooledMutableBlockPos
						);
					}
					if (!colors) {
						red0 = red1 = red2 = red3 = red;
						green0 = green1 = green2 = green3 = green;
						blue0 = blue1 = blue2 = blue3 = blue;
					} else {
//						final int waterColor0 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0));
						final int waterColor0 = getWaterColor(lazyBlockColorsCacheStartPaddingX + (x0 - chunkRenderPosX), lazyBlockColorsCacheStartPaddingY + (MathHelper.floor(y + y0) - chunkRenderPosY), lazyBlockColorsCacheStartPaddingZ + (z0 - chunkRenderPosZ),
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								lazyBlockColorsCache,
								lazyBlockColorsCacheCache,
								lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
								biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
								lazyBlockColorsCacheReader,
								lazyBlockColorsCacheColorResolver,
								pooledMutableBlockPos
						);
						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
//						final int waterColor1 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1));
						final int waterColor1 = getWaterColor(lazyBlockColorsCacheStartPaddingX + (x1 - chunkRenderPosX), lazyBlockColorsCacheStartPaddingY + (MathHelper.floor(y + y1) - chunkRenderPosY), lazyBlockColorsCacheStartPaddingZ + (z1 - chunkRenderPosZ),
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								lazyBlockColorsCache,
								lazyBlockColorsCacheCache,
								lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
								biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
								lazyBlockColorsCacheReader,
								lazyBlockColorsCacheColorResolver,
								pooledMutableBlockPos
						);
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
//						final int waterColor2 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x1, y, z1));
						final int waterColor2 = getWaterColor(lazyBlockColorsCacheStartPaddingX + (x1 - chunkRenderPosX), lazyBlockColorsCacheStartPaddingY + (y - chunkRenderPosY), lazyBlockColorsCacheStartPaddingZ + (z1 - chunkRenderPosZ),
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								lazyBlockColorsCache,
								lazyBlockColorsCacheCache,
								lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
								biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
								lazyBlockColorsCacheReader,
								lazyBlockColorsCacheColorResolver,
								pooledMutableBlockPos
						);
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
//						final int waterColor3 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x0, y, z0));
						final int waterColor3 = getWaterColor(lazyBlockColorsCacheStartPaddingX + (x0 - chunkRenderPosX), lazyBlockColorsCacheStartPaddingY + (y - chunkRenderPosY), lazyBlockColorsCacheStartPaddingZ + (z0 - chunkRenderPosZ),
								chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
								lazyBlockColorsCache,
								lazyBlockColorsCacheCache,
								lazyBlockColorsCacheSizeX, lazyBlockColorsCacheSizeY,
								biomeBlendRadius, lazyBlockColorsCacheArea, lazyBlockColorsCacheMax,
								lazyBlockColorsCacheReader,
								lazyBlockColorsCacheColorResolver,
								pooledMutableBlockPos
						);
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}
				}
				wasAnythingRendered = fluidRenderer.renderSide(
						buffer, textureatlassprite2,
						red0, green0, blue0,
						red1, green1, blue1,
						red2, green2, blue2,
						red3, green3, blue3,
						facingIndex,
						y, y0, y1,
						x0, x1,
						z0, z1,
						light0, light1, light2, light3,
						textureatlassprite2 != atlasSpriteWaterOverlay
				);
			}
		}

		return wasAnythingRendered;
	}

	private static int getWaterColor(
			final int x, final int y, final int z,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			final LazyBlockColorCache lazyBlockColorsCache,
			final int[] cache,
			final int sizeX, final int sizeY,
			final int biomeBlendRadius,
			final int area,
			final int max,
			final IEnviromentBlockReader reader,
			final IColorResolver resolver,
			final PooledMutableBlockPos pooledMutableBlockPos
	) {
		return LazyBlockColorCache.get(x, y, z, cache, lazyBlockColorsCache.getIndex(x, y, z, sizeX, sizeY), biomeBlendRadius, area, max, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, pooledMutableBlockPos, reader, resolver, true);
	}

	private static int getCombinedLightUpMax(
			final int x, final int y, final int z,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			final StateCache stateCache,
			final int stateCacheSizeX, final int stateCacheSizeY,
			final LazyPackedLightCache lazyPackedLightCache,
			final int[] lazyPackedLightCacheCache,
			final int lazyPackedLightCacheSizeX, final int lazyPackedLightCacheSizeY,
			final int lazyPackedLightCacheStartPaddingX, final int lazyPackedLightCacheStartPaddingY, final int lazyPackedLightCacheStartPaddingZ,
			final int lazyPackedLightCacheDiffX, final int lazyPackedLightCacheDiffY, final int lazyPackedLightCacheDiffZ,
			final IEnviromentBlockReader lazyPackedLightCacheReader,
			final PooledMutableBlockPos pooledMutableBlockPos
	) {
		final int light = LazyPackedLightCache.get(x, y, z, lazyPackedLightCacheCache, lazyPackedLightCache.getIndex(x, y, z, lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY), stateCache, lazyPackedLightCacheReader, pooledMutableBlockPos, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ, lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ, stateCacheSizeX, stateCacheSizeY);
		final int lightUp = LazyPackedLightCache.get(x, y + 1, z, lazyPackedLightCacheCache, lazyPackedLightCache.getIndex(x, y + 1, z, lazyPackedLightCacheSizeX, lazyPackedLightCacheSizeY), stateCache, lazyPackedLightCacheReader, pooledMutableBlockPos, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, lazyPackedLightCacheStartPaddingX, lazyPackedLightCacheStartPaddingY, lazyPackedLightCacheStartPaddingZ, lazyPackedLightCacheDiffX, lazyPackedLightCacheDiffY, lazyPackedLightCacheDiffZ, stateCacheSizeX, stateCacheSizeY);
		final int blockLight = light & 0xFF;
		final int blockLightUp = lightUp & 0xFF;
		final int skyLight = light >> 0x10 & 0xFF;
		final int skyLightUp = lightUp >> 0x10 & 0xFF;
		return (blockLight > blockLightUp ? blockLight : blockLightUp) | (skyLight > skyLightUp ? skyLight : skyLightUp) << 0x10;
	}

	public static float getFluidHeight(
			final IFluidState[] fluidStateArray,
			final BlockState[] blockStateArray,
			final StateCache stateCache,
			final int stateCacheSizeX, final int stateCacheSizeY,
			final int stateCacheOffsetX, final int stateCacheOffsetY, final int stateCacheOffsetZ,
			final Fluid fluidIn
	) {
		int divisor = 0;
		float height = 0.0F;

//		for (int j = 0; j < 4; ++j) {
//			{
		for (int xOff = 0; xOff > -2; --xOff) {
			for (int zOff = 0; zOff > -2; --zOff) {
//				pooledMutableBlockPos.setPos(posX - (j & 1), posY + 1, posZ - (j >> 1 & 1));
//				pooledMutableBlockPos.setPos(posX + xOff, posY + 1, posZ + zOff);

//				if (reader.getFluidState(pooledMutableBlockPos).getFluid().isEquivalentTo(fluidIn)) {
				final int stateCacheOffsetXPlusOffset = stateCacheOffsetX + xOff;
				final int stateCacheOffsetZPlusOffset = stateCacheOffsetZ + zOff;
				final IFluidState upFluidState = fluidStateArray[stateCache.getIndex(
						stateCacheOffsetXPlusOffset,
						stateCacheOffsetY + 1,
						stateCacheOffsetZPlusOffset,
						stateCacheSizeX, stateCacheSizeY
				)];
				final Fluid upFluid = upFluidState.getFluid();
				if (upFluid.isEquivalentTo(fluidIn)) {
					return 1.0F;
				}

//				pooledMutableBlockPos.setPos(posX - (j & 1), posY, posZ - (j >> 1 & 1));
//				pooledMutableBlockPos.setPos(posX + xOff, posY, posZ + zOff);

//				IFluidState ifluidstate = reader.getFluidState(pooledMutableBlockPos);
				IFluidState ifluidstate = fluidStateArray[stateCache.getIndex(
						stateCacheOffsetXPlusOffset,
						stateCacheOffsetY,
						stateCacheOffsetZPlusOffset,
						stateCacheSizeX, stateCacheSizeY
				)];
				final Fluid fluid = ifluidstate.getFluid();
				if (fluid.isEquivalentTo(fluidIn)) {
//					float fluidHeight = ifluidstate.func_215679_a(reader, pooledMutableBlockPos);
					final float fluidHeight;
					if (fluid instanceof EmptyFluid) {
						fluidHeight = 0F;
					} else {

//						fluidHeight = func_215666_c(ifluidstate, reader, pos) ? 1.0F : (float)ifluidstate.getLevel() / 9.0F;
//						isUpFluidEquivalent
//						func_215666_c = return ifluidstate.getFluid().isEquivalentTo(reader.getFluidState(pos.up()).getFluid());
						if (fluid.isEquivalentTo(upFluid)) {
							fluidHeight = 1.0F;
						} else {
							fluidHeight = (float) ifluidstate.getLevel() / 9.0F;
						}
					}
					if (fluidHeight >= 0.8F) {
						height += fluidHeight * 10.0F;
						divisor += 10;
					} else {
						height += fluidHeight;
						++divisor;
					}
//				} else if (!reader.getBlockState(pooledMutableBlockPos).getMaterial().isSolid()) {
				} else if (!blockStateArray[stateCache.getIndex(
						stateCacheOffsetXPlusOffset,
						stateCacheOffsetY,
						stateCacheOffsetZPlusOffset,
						stateCacheSizeX, stateCacheSizeY
				)].getMaterial().isSolid()) {
					++divisor;
				}
			}
		}

		return height / (float) divisor;
	}

}
