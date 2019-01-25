package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.OptifineCompatibility;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.PooledStateCache;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.hooks.RenderChunkRebuildChunkHooksHooks.renderChunk_preRenderBlocks;

/**
 * @author Cadiboo
 */
public class ExtendedLiquidChunkRenderer {

	private static final int liquidCacheAddX = 1;

	private static final int liquidCacheAddY = 1;

	private static final int liquidCacheAddZ = 1;

	private static final int liquidCacheSizeX = 16 + liquidCacheAddX + liquidCacheAddX;

	private static final int liquidCacheSizeY = 16 + liquidCacheAddY + liquidCacheAddY;

	private static final int liquidCacheSizeZ = 16 + liquidCacheAddZ + liquidCacheAddZ;

	private static final ThreadLocal<PooledStateCache> LIQUID_CACHE = new ThreadLocal<>();

	public static void extendLiquidsPre(final RebuildChunkPreEvent event) {

		if (true) return;

		final BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain();

		try {
			final BlockPos renderChunkPosition = event.getRenderChunkPosition();
			final int renderChunkPositionX = renderChunkPosition.getX();
			final int renderChunkPositionY = renderChunkPosition.getY();
			final int renderChunkPositionZ = renderChunkPosition.getZ();

			final int startPosX = renderChunkPositionX - liquidCacheAddX;
			final int startPosY = renderChunkPositionY - liquidCacheAddY;
			final int startPosZ = renderChunkPositionZ - liquidCacheAddZ;

			final PooledStateCache stateCache = CacheUtil.generateStateCache(startPosX, startPosY, startPosZ, liquidCacheSizeX, liquidCacheSizeY, liquidCacheSizeZ, event.getIBlockAccess(), pooledMutableBlockPos);
			LIQUID_CACHE.set(stateCache);

		} finally {
			pooledMutableBlockPos.release();
		}

	}

	public static boolean isLiquid(final IBlockState state) {
		return state.getBlock() instanceof BlockLiquid;
	}

	public static void extendLiquidsBlock(final RebuildChunkBlockEvent event) {

		if (true) return;
		final IBlockState state = event.getBlockState();
		if (TERRAIN_SMOOTHABLE.isSmoothable(state)) {
			final IBlockState[] stateCache = LIQUID_CACHE.get().getStateCache();
			final BlockPos pos = event.getBlockPos();
			final int posX = pos.getX();
			final int posY = pos.getY();
			final int posZ = pos.getZ();
			final BlockPos renderChunkPosition = event.getRenderChunkPosition();
			final int renderChunkPositionX = renderChunkPosition.getX();
			final int renderChunkPositionY = renderChunkPosition.getY();
			final int renderChunkPositionZ = renderChunkPosition.getZ();

			for (final int[] offsetPos : new int[][]{
					{+1, +0, +0},
					{-1, +0, +0},
					{+0, +0, +1},
					{+0, +0, -1},
					{+1, +0, +1},
					{+1, +0, -1},
					{-1, +0, +1},
					{-1, +0, -1},
			}) {

				// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
				final int index = (posX - renderChunkPositionX + offsetPos[0] + liquidCacheAddX) + liquidCacheSizeX * ((posY - renderChunkPositionY + offsetPos[1] + liquidCacheAddY) + liquidCacheSizeY * (posZ - renderChunkPositionZ + offsetPos[2] + liquidCacheAddZ));

				if (index > stateCache.length) {
					// Fix crash on TP when renderChunkPosition is changed mid-render (somehow)
					return;
				}

				final IBlockState potentialLiquidState = stateCache[index];
				if (!isLiquid(potentialLiquidState)) {
					continue;
				}

				// if not source block
				if (potentialLiquidState.getValue(BlockLiquid.LEVEL) != 0) {
					continue;
				}

				final BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain(posX + offsetPos[0], posY + offsetPos[1], posZ + offsetPos[2]);
				try {
					final Minecraft minecraft = Minecraft.getMinecraft();
					final BlockRenderLayer blockRenderLayer = ClientUtil.getRenderLayer(potentialLiquidState);
					final int ordinal = blockRenderLayer.ordinal();
					final IBlockAccess cache = event.getIBlockAccess();
					final CompiledChunk compiledChunk = event.getCompiledChunk();
					final BufferBuilder bufferBuilder = event.getGenerator().getRegionRenderCacheBuilder().getWorldRendererByLayerId(ordinal);

					if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
						compiledChunk.setLayerStarted(blockRenderLayer);
						renderChunk_preRenderBlocks(event.getRenderChunk(), bufferBuilder, renderChunkPosition);
					}

					OptifineCompatibility.pushShaderThing(potentialLiquidState, pooledMutableBlockPos, cache, bufferBuilder);
					event.getUsedBlockRenderLayers()[ordinal] =
							ExtendedLiquidBlockRenderer.renderExtendedLiquid(minecraft.getTextureMapBlocks(), minecraft.getBlockColors(), pos, pooledMutableBlockPos, cache, state, potentialLiquidState, bufferBuilder);
					OptifineCompatibility.popShaderThing(bufferBuilder);
				} finally {
					pooledMutableBlockPos.release();
				}
				return;

			}
		}

	}

	public static void extendLiquidsPost(final RebuildChunkPostEvent event) {
		final PooledStateCache pooledStateCache = LIQUID_CACHE.get();
		//it only gets generated if the worldview is not empty, this gets called even if the worldview was empty
		if (pooledStateCache != null)
			pooledStateCache.release();
	}

	public static void renderChunkLiquids() {

	}

	public static void renderChunk() {

	}

//	public static void extendLiquids(final RebuildChunkBlockEvent event) {
//
//		final IBlockState state = event.getBlockState();
//		if (!ModUtil.shouldSmooth(state)) {
//			return;
//		}
//		final ChunkCache cache = event.getChunkCache();
//		final BlockPos pos = event.getBlockPos();
//
//		MutableBlockPos liquidPos = null;
//		IBlockState liquidState = null;
//
//		for (final MutableBlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
//			final IBlockState tempState = cache.getBlockState(mutablePos);
//			if (tempState.getBlock() instanceof BlockLiquid) {
//				//usually we would make it immutable, but since it wont be changed anymore we can just reference it without worrying about that
//				liquidPos = mutablePos;
//				liquidState = tempState;
//				break;
//			}
//		}
//
//		// set at same time so can skip
//		if (liquidPos == null /*|| liquidState == null*/) {
//			return;
//		}
//
//		event.getBlockRendererDispatcher().renderBlock(liquidState, pos, cache, event.getBufferBuilder());
//
//	}

//	@Deprecated
//	//FIXME
//	public static void calculateExtendedLiquids(final RebuildChunkPreEvent event) {
//
//		Minecraft.getMinecraft().PROFILER.startSection("Rendering smooth world liquids in Pre");
//
//		final BlockPos renderChunkPosition = event.getRenderChunkPosition();
//		final ChunkCache cache = event.getChunkCache();
//
//		final HashMap<BlockPos, Object[]> map = new HashMap<>();
//		RENDER_LIQUID_POSITIONS.get().put(renderChunkPosition, map);
//
//		// 18 * 18 * 18 add 1 block on each side of chunk
//		final boolean[] isLiquid = new boolean[5832];
//
//		for (MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(renderChunkPosition.add(-1, -1, -1), renderChunkPosition.add(16, 16, 16))) {
//			final BlockPos sub = mutableBlockPos.subtract(renderChunkPosition);
//			final int x = sub.getX() + 1;
//			final int y = sub.getY() + 1;
//			final int z = sub.getZ() + 1;
//			// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
//			isLiquid[x + 18 * (y + 18 * z)] = cache.getBlockState(mutableBlockPos).getBlock() instanceof BlockLiquid && !(cache.getBlockState(mutableBlockPos.up()).getBlock() instanceof BlockLiquid);
//		}
//
//		for (MutableBlockPos mutableBlockPos : BlockPos.getAllInBoxMutable(renderChunkPosition, renderChunkPosition.add(15, 15, 15))) {
//			IF:
//			if (ModUtil.shouldSmooth(cache.getBlockState(mutableBlockPos))) {
//				final BlockPos sub = mutableBlockPos.subtract(renderChunkPosition);
//				final int x = sub.getX() + 1;
//				final int y = sub.getY() + 1;
//				final int z = sub.getZ() + 1;
//				for (int xOff = -1; xOff <= 1; xOff++) {
//					for (int zOff = -1; zOff <= 1; zOff++) {
//						if (isLiquid[(x + xOff) + 18 * (y + 18 * (z + zOff))]) {
//
//							final BlockPos potentialLiquidPos = mutableBlockPos.add(xOff, 0, zOff);
//							final IBlockState liquidState = cache.getBlockState(potentialLiquidPos);
//
//							if (!(liquidState.getBlock() instanceof BlockLiquid)) {
//								continue;
//							}
//
//							// if not source block
//							if (liquidState.getValue(BlockLiquid.LEVEL) != 0) {
//								continue;
//							}
//							map.put(mutableBlockPos.toImmutable(), new Object[]{potentialLiquidPos.toImmutable(), liquidState});
//							break IF;
//						}
//					}
//				}
//			}
//		}
//
//		Minecraft.getMinecraft().PROFILER.endSection();
//
//	}
//
//	public static void handleExtendedLiquidRender(final RebuildChunkBlockEvent event) {
//
//		final BlockPos renderChunkPos = event.getRenderChunkPosition();
//		final HashMap<BlockPos, Object[]> map = RENDER_LIQUID_POSITIONS.get().get(renderChunkPos);
//		final BlockPos pos = event.getBlockPos();
//		final Object[] data = map.get(pos);
//
//		if (data == null) {
//			return;
//		}
//
//		Minecraft.getMinecraft().PROFILER.startSection("Rendering smooth world liquid in Block");
//
//		final BlockPos liquidPos = (BlockPos) data[0];
//		final IBlockState liquidState = (IBlockState) data[1];
//		final ChunkCache cache = event.getChunkCache();
//
//		final BlockRenderLayer blockRenderLayer = getRenderLayer(liquidState);
//		final BufferBuilder bufferBuilder = event.getGenerator().getRegionRenderCacheBuilder().getWorldRendererByLayer(blockRenderLayer);
//		final CompiledChunk compiledChunk = event.getCompiledChunk();
//		final RenderChunk renderChunk = event.getRenderChunk();
//
//		if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
//			compiledChunk.setLayerStarted(blockRenderLayer);
//			event.getUsedBlockRenderLayers()[blockRenderLayer.ordinal()] = true;
//			renderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPos);
//		}
//
//		OptifineCompatibility.pushShaderThing(liquidState, liquidPos, cache, bufferBuilder);
//
////		FluidInBlockRenderer.renderLiquidInBlock(liquidState, liquidPos, pos, cache, bufferBuilder);
//
//		OptifineCompatibility.popShaderThing(bufferBuilder);
//
//		map.remove(pos);
//
//		Minecraft.getMinecraft().PROFILER.endSection();
//
//	}
//
//	public static void cleanupExtendedLiquids(final RebuildChunkPostEvent event) {
//
//		Minecraft.getMinecraft().PROFILER.startSection("Rendering smooth world liquids in Post");
//
//		final MutableBlockPos renderChunkPos = event.getRenderChunkPosition();
//		RENDER_LIQUID_POSITIONS.get().remove(renderChunkPos);
//
//		Minecraft.getMinecraft().PROFILER.endSection();
//
//	}

//	public static BlockRenderData getBlockRenderData(final BlockPos pos, final IBlockAccess cache) {
//
//		final IBlockState state = cache.getBlockState(pos);
//		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
//
//		final Object[] texturePosAndState = ClientUtil.getTexturePosAndState(cache, pos, state);
//		final BlockPos texturePos = (BlockPos) texturePosAndState[0];
//		final IBlockState textureState = (IBlockState) texturePosAndState[1];
//
//		BakedQuad quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher);
//		if (quad == null) {
//			quad = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, null, 0L).get(0);
//		}
//		final TextureAtlasSprite sprite = quad.getSprite();
//		final int color = ClientUtil.getColor(quad, textureState, cache, texturePos);
//		final int red = (color >> 16) & 255;
//		final int green = (color >> 8) & 255;
//		final int blue = color & 255;
//		final int alpha = 0xFF;
//
//		final float minU = ClientUtil.getMinU(sprite);
//		final float minV = ClientUtil.getMinV(sprite);
//		final float maxU = ClientUtil.getMaxU(sprite);
//		final float maxV = ClientUtil.getMaxV(sprite);
//
//		//real pos not texture pos
//		final LightmapInfo lightmapInfo = ClientUtil.getLightmapInfo(pos, cache);
//		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
//		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();
//
//		final BlockRenderLayer blockRenderLayer = state.getBlock().getRenderLayer();
//
//		return new BlockRenderData(blockRenderLayer, red, green, blue, alpha, minU, maxU, minV, maxV, lightmapSkyLight, lightmapBlockLight);
//
//	}

}
