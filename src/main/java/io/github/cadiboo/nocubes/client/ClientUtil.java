package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.BlockDirtSnowy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;

import javax.annotation.Nonnull;

import static io.github.cadiboo.nocubes.client.ClientUtil.StateHolder.GRASS_BLOCK_DEFAULT;
import static io.github.cadiboo.nocubes.client.ClientUtil.StateHolder.GRASS_BLOCK_SNOWY;
import static io.github.cadiboo.nocubes.client.ClientUtil.StateHolder.PODZOL_SNOWY;
import static io.github.cadiboo.nocubes.client.ClientUtil.StateHolder.SNOW_LAYER_DEFAULT;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static net.minecraft.init.Blocks.GRASS_BLOCK;
import static net.minecraft.init.Blocks.PACKED_ICE;
import static net.minecraft.init.Blocks.PODZOL;
import static net.minecraft.init.Blocks.SNOW;
import static net.minecraft.util.BlockRenderLayer.CUTOUT;
import static net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
import static net.minecraft.util.math.MathHelper.clamp;

/**
 * Util that is only used on the Physical Client i.e. Rendering code
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ClientUtil {

	private static final int[][] OFFSETS_ORDERED = {
			// check 6 immediate neighbours
			{+0, -1, +0},
			{+0, +1, +0},
			{-1, +0, +0},
			{+1, +0, +0},
			{+0, +0, -1},
			{+0, +0, +1},
			// check 12 non-immediate, non-corner neighbours
			{-1, -1, +0},
			{-1, +0, -1},
			{-1, +0, +1},
			{-1, +1, +0},
			{+0, -1, -1},
			{+0, -1, +1},
			// {+0, +0, +0}, // Don't check self
			{+0, +1, -1},
			{+0, +1, +1},
			{+1, -1, +0},
			{+1, +0, -1},
			{+1, +0, +1},
			{+1, +1, +0},
			// check 8 corner neighbours
			{+1, +1, +1},
			{+1, +1, -1},
			{-1, +1, +1},
			{-1, +1, -1},
			{+1, -1, +1},
			{+1, -1, -1},
			{-1, -1, +1},
			{-1, -1, -1},
	};

	/**
	 * @param red   the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @param green the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @param blue  the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @return the color in ARGB format
	 */
	public static int colori(int red, int green, int blue) {

		red = clamp(red, 0x00, 0xFF);
		green = clamp(green, 0x00, 0xFF);
		blue = clamp(blue, 0x00, 0xFF);

		final int alpha = 0xFF;

		// 0x alpha red green blue
		// 0xaarrggbb

		// int colorRGBA = 0;
		// colorRGBA |= red << 16;
		// colorRGBA |= green << 8;
		// colorRGBA |= blue << 0;
		// colorRGBA |= alpha << 24;

		return blue | red << 16 | green << 8 | alpha << 24;

	}

	/**
	 * @param red   the red value of the color, 0F and 1F
	 * @param green the green value of the color, 0F and 1F
	 * @param blue  the blue value of the color, 0F and 1F
	 * @return the color in ARGB format
	 */
	public static int colorf(final float red, final float green, final float blue) {
		final int redInt = max(0, min(255, round(red * 255)));
		final int greenInt = max(0, min(255, round(green * 255)));
		final int blueInt = max(0, min(255, round(blue * 255)));
		return colori(redInt, greenInt, blueInt);
	}

	public static int getLightmapSkyLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return (packedLightmapCoords >> 16) & 0xFFFF; // get upper 4 bytes
	}

	public static int getLightmapBlockLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return packedLightmapCoords & 0xFFFF; // get lower 4 bytes
	}

	/**
	 * Returns a state and sets the texturePooledMutablePos to the pos it found
	 *
	 * @return a state
	 */
	@Nonnull
	public static IBlockState getTexturePosAndState(
			final int posX, final int posY, final int posZ,
			@Nonnull final PooledMutableBlockPos texturePooledMutablePos,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache,
			final int cacheAddX, final int cacheAddY, final int cacheAddZ,
			final byte relativePosX, final byte relativePosY, final byte relativePosZ,
			final boolean tryForBetterTexturesSnow, final boolean tryForBetterTexturesGrass
	) {

		final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();
		final IBlockState[] blockCacheArray = stateCache.getBlockStates();

		if (Config.betterTextures) {
			if (tryForBetterTexturesSnow) {
				try (final ModProfiler ignored = ModProfiler.get().start("getTexturePosAndState-tryForBetterTextures-snow")) {
					IBlockState betterTextureState = blockCacheArray[stateCache.getIndex(
							relativePosX + cacheAddX,
							relativePosY + cacheAddY,
							relativePosZ + cacheAddZ
					)];

					if (isStateSnow(betterTextureState)) {
						texturePooledMutablePos.setPos(posX, posY, posZ);
						return betterTextureState;
					}
					for (int[] offset : OFFSETS_ORDERED) {
						betterTextureState = blockCacheArray[stateCache.getIndex(
								relativePosX + offset[0] + cacheAddX,
								relativePosY + offset[1] + cacheAddY,
								relativePosZ + offset[2] + cacheAddZ
						)];
						if (isStateSnow(betterTextureState)) {
							texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
							return betterTextureState;
						}
					}
				}
			}
			if (tryForBetterTexturesGrass) {
				try (final ModProfiler ignored = ModProfiler.get().start("getTexturePosAndState-tryForBetterTextures-grass")) {
					IBlockState betterTextureState = blockCacheArray[stateCache.getIndex(
							relativePosX + cacheAddX,
							relativePosY + cacheAddY,
							relativePosZ + cacheAddZ
					)];

					if (isStateGrass(betterTextureState)) {
						texturePooledMutablePos.setPos(posX, posY, posZ);
						return betterTextureState;
					}
					for (int[] offset : OFFSETS_ORDERED) {
						betterTextureState = blockCacheArray[stateCache.getIndex(
								relativePosX + offset[0] + cacheAddX,
								relativePosY + offset[1] + cacheAddY,
								relativePosZ + offset[2] + cacheAddZ
						)];
						if (isStateGrass(betterTextureState)) {
							texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
							return betterTextureState;
						}
					}
				}
			}
		}

		try (final ModProfiler ignored = ModProfiler.get().start("getTexturePosAndState")) {

			// If pos passed in is smoothable return state from that pos
			if (smoothableCacheArray[smoothableCache.getIndex(
					relativePosX + cacheAddX,
					relativePosY + cacheAddY,
					relativePosZ + cacheAddZ
			)]) {
				texturePooledMutablePos.setPos(posX, posY, posZ);
				return blockCacheArray[stateCache.getIndex(
						relativePosX + cacheAddX,
						relativePosY + cacheAddY,
						relativePosZ + cacheAddZ
				)];
			}

			// Start at state of pos passed in
			IBlockState state = blockCacheArray[stateCache.getIndex(
					relativePosX + cacheAddX,
					relativePosY + cacheAddY,
					relativePosZ + cacheAddZ
			)];

			for (int[] offset : OFFSETS_ORDERED) {
				if (smoothableCacheArray[smoothableCache.getIndex(
						relativePosX + offset[0] + cacheAddX,
						relativePosY + offset[1] + cacheAddY,
						relativePosZ + offset[2] + cacheAddZ
				)]) {
					texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
					state = blockCacheArray[stateCache.getIndex(
							relativePosX + offset[0] + cacheAddX,
							relativePosY + offset[1] + cacheAddY,
							relativePosZ + offset[2] + cacheAddZ
					)];
					break;
				}
			}
			return state;
		}
	}

	public static boolean isStateSnow(final IBlockState checkState) {
		if (checkState == SNOW_LAYER_DEFAULT) return true;
		if (checkState == GRASS_BLOCK_SNOWY) return true;
		return checkState == PODZOL_SNOWY;
	}

	private static boolean isStateGrass(final IBlockState checkState) {
		return checkState == GRASS_BLOCK_DEFAULT;
	}

	@Nonnull
	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final IBlockState state) {
		return getCorrectRenderLayer(state.getBlock().getRenderLayer());
	}

	@Nonnull
	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final IFluidState state) {
		return getCorrectRenderLayer(state.getRenderLayer());
	}

	@Nonnull
	public static BlockRenderLayer getCorrectRenderLayer(@Nonnull final BlockRenderLayer blockRenderLayer) {
		switch (blockRenderLayer) {
			default:
			case SOLID:
			case TRANSLUCENT:
				return blockRenderLayer;
			case CUTOUT_MIPPED:
				return Minecraft.getInstance().gameSettings.mipmapLevels == 0 ? CUTOUT : CUTOUT_MIPPED;
			case CUTOUT:
				return Minecraft.getInstance().gameSettings.mipmapLevels != 0 ? CUTOUT_MIPPED : CUTOUT;
		}
	}

	public static BufferBuilder startOrContinueBufferBuilder(final ChunkRenderTask generator, final int blockRenderLayerOrdinal, final CompiledChunk compiledChunk, final BlockRenderLayer blockRenderLayer, RenderChunk renderChunk, BlockPos renderChunkPosition) {
		final BufferBuilder bufferBuilder = generator.getRegionRenderCacheBuilder().getBuilder(blockRenderLayerOrdinal);
		if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
			compiledChunk.setLayerStarted(blockRenderLayer);
			renderChunk.preRenderBlocks(bufferBuilder, renderChunkPosition);
		}
		return bufferBuilder;
	}

	public static void tryReloadRenderers() {
		final WorldRenderer worldRenderer = Minecraft.getInstance().worldRenderer;
		if (worldRenderer != null) {
			worldRenderer.loadRenderers();
		}
	}

	/**
	 * @param chunkPos the chunk position as a {@link BlockPos}
	 * @param blockPos the {@link BlockPos}
	 * @return the position relative to the chunkPos
	 */
	public static byte getRelativePos(final int chunkPos, final int blockPos) {
		final int blockPosChunkPos = (blockPos >> 4) << 4;
		if (chunkPos == blockPosChunkPos) { // if blockpos is in chunkpos's chunk
			return getRelativePos(blockPos);
		} else {
			// can be anything. usually between -1 and 16
			return (byte) (blockPos - chunkPos);
		}
	}

	/**
	 * @param blockPos the {@link BlockPos}
	 * @return the position (between 0-15) relative to the blockPos's chunk position
	 */
	public static byte getRelativePos(final int blockPos) {
		return (byte) (blockPos & 15);
	}

	public static class StateHolder {

		public static final IBlockState SNOW_LAYER_DEFAULT = SNOW.getDefaultState();
		public static final IBlockState GRASS_BLOCK_SNOWY = GRASS_BLOCK.getDefaultState().with(BlockDirtSnowy.SNOWY, true);
		public static final IBlockState PODZOL_SNOWY = PODZOL.getDefaultState().with(BlockDirtSnowy.SNOWY, true);

		public static final IBlockState GRASS_BLOCK_DEFAULT = GRASS_BLOCK.getDefaultState();

		public static final IBlockState PACKED_ICE_DEFAULT = PACKED_ICE.getDefaultState();

	}

}
