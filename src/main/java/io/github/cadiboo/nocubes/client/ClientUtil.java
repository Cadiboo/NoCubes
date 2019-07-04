package io.github.cadiboo.nocubes.client;

import cpw.mods.modlauncher.api.INameMappingService;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.Region;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.registries.IRegistryDelegate;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import static io.github.cadiboo.nocubes.util.StateHolder.GRASS_BLOCK_DEFAULT;
import static io.github.cadiboo.nocubes.util.StateHolder.GRASS_BLOCK_SNOWY;
import static io.github.cadiboo.nocubes.util.StateHolder.PODZOL_SNOWY;
import static io.github.cadiboo.nocubes.util.StateHolder.SNOW_LAYER_DEFAULT;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static net.minecraft.util.BlockRenderLayer.CUTOUT;
import static net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraftforge.fml.common.ObfuscationReflectionHelper.remapName;

/**
 * Util that is only used on the Physical Client i.e. Rendering code
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ClientUtil {

	static final int[] NEGATIVE_1_8000 = new int[8000];
	public static final BlockRenderLayer[] BLOCK_RENDER_LAYER_VALUES = BlockRenderLayer.values();
	public static final int BLOCK_RENDER_LAYER_VALUES_LENGTH = BLOCK_RENDER_LAYER_VALUES.length;
	private static final Field BLOCK_COLORS_REGISTRY = findBlockColorsRegistryField();
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
	static {
		Arrays.fill(ClientUtil.NEGATIVE_1_8000, -1);
	}

	/**
	 * Returns a state and sets the texturePooledMutablePos to the pos it found
	 *
	 * @return a state
	 */
	@Nonnull
	public static BlockState getTexturePosAndState(
			final int posX, final int posY, final int posZ,
			@Nonnull final PooledMutableBlockPos texturePooledMutablePos,
			@Nonnull final StateCache stateCache,
			@Nonnull final SmoothableCache smoothableCache,
			final byte relativePosX, final byte relativePosY, final byte relativePosZ,
			final boolean tryForBetterTexturesSnow, final boolean tryForBetterTexturesGrass
	) {

		final boolean[] smoothableCacheArray = smoothableCache.getSmoothableCache();
		final BlockState[] blockCacheArray = stateCache.getBlockStates();

		final int stateCacheStartPaddingX = stateCache.startPaddingX;
		final int stateCacheStartPaddingY = stateCache.startPaddingY;
		final int stateCacheStartPaddingZ = stateCache.startPaddingZ;

		final int stateCacheSizeX = stateCache.sizeX;
		final int stateCacheSizeY = stateCache.sizeY;

		if (Config.betterTextures) {
			if (tryForBetterTexturesSnow) {
				try (final ModProfiler ignored = ModProfiler.get().start("getTexturePosAndState-tryForBetterTextures-snow")) {
					BlockState betterTextureState = blockCacheArray[stateCache.getIndex(
							relativePosX + stateCacheStartPaddingX,
							relativePosY + stateCacheStartPaddingY,
							relativePosZ + stateCacheStartPaddingZ,
							stateCacheSizeX, stateCacheSizeY
					)];

					if (isStateSnow(betterTextureState)) {
						texturePooledMutablePos.setPos(posX, posY, posZ);
						return betterTextureState;
					}
					for (int[] offset : OFFSETS_ORDERED) {
						betterTextureState = blockCacheArray[stateCache.getIndex(
								relativePosX + offset[0] + stateCacheStartPaddingX,
								relativePosY + offset[1] + stateCacheStartPaddingY,
								relativePosZ + offset[2] + stateCacheStartPaddingZ,
								stateCacheSizeX, stateCacheSizeY
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
					BlockState betterTextureState = blockCacheArray[stateCache.getIndex(
							relativePosX + stateCacheStartPaddingX,
							relativePosY + stateCacheStartPaddingY,
							relativePosZ + stateCacheStartPaddingZ,
							stateCacheSizeX, stateCacheSizeY
					)];

					if (isStateGrass(betterTextureState)) {
						texturePooledMutablePos.setPos(posX, posY, posZ);
						return betterTextureState;
					}
					for (int[] offset : OFFSETS_ORDERED) {
						betterTextureState = blockCacheArray[stateCache.getIndex(
								relativePosX + offset[0] + stateCacheStartPaddingX,
								relativePosY + offset[1] + stateCacheStartPaddingY,
								relativePosZ + offset[2] + stateCacheStartPaddingZ,
								stateCacheSizeX, stateCacheSizeY
						)];
						if (isStateGrass(betterTextureState)) {
							texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
							return betterTextureState;
						}
					}
				}
			}
		}

		final int smoothableCacheStartPaddingX = smoothableCache.startPaddingX;
		final int smoothableCacheStartPaddingY = smoothableCache.startPaddingY;
		final int smoothableCacheStartPaddingZ = smoothableCache.startPaddingZ;

		final int smoothableCacheSizeX = smoothableCache.sizeX;
		final int smoothableCacheSizeY = smoothableCache.sizeY;

		try (final ModProfiler ignored = ModProfiler.get().start("getTexturePosAndState")) {

			// If pos passed in is smoothable return state from that pos
			if (smoothableCacheArray[smoothableCache.getIndex(
					relativePosX + smoothableCacheStartPaddingX,
					relativePosY + smoothableCacheStartPaddingY,
					relativePosZ + smoothableCacheStartPaddingZ,
					smoothableCacheSizeX, smoothableCacheSizeY
			)]) {
				texturePooledMutablePos.setPos(posX, posY, posZ);
				return blockCacheArray[stateCache.getIndex(
						relativePosX + stateCacheStartPaddingX,
						relativePosY + stateCacheStartPaddingY,
						relativePosZ + stateCacheStartPaddingZ,
						stateCacheSizeX, stateCacheSizeY
				)];
			}

			// Start at state of pos passed in
			BlockState state = blockCacheArray[stateCache.getIndex(
					relativePosX + stateCacheStartPaddingX,
					relativePosY + stateCacheStartPaddingY,
					relativePosZ + stateCacheStartPaddingZ,
					stateCacheSizeX, stateCacheSizeY
			)];

			for (int[] offset : OFFSETS_ORDERED) {
				if (smoothableCacheArray[smoothableCache.getIndex(
						relativePosX + offset[0] + smoothableCacheStartPaddingX,
						relativePosY + offset[1] + smoothableCacheStartPaddingY,
						relativePosZ + offset[2] + smoothableCacheStartPaddingZ,
						smoothableCacheSizeX, smoothableCacheSizeY
				)]) {
					texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
					state = blockCacheArray[stateCache.getIndex(
							relativePosX + offset[0] + stateCacheStartPaddingX,
							relativePosY + offset[1] + stateCacheStartPaddingY,
							relativePosZ + offset[2] + stateCacheStartPaddingZ,
							stateCacheSizeX, stateCacheSizeY
					)];
					break;
				}
			}
			return state;
		}
	}

	public static boolean isStateSnow(final BlockState checkState) {
		if (checkState == SNOW_LAYER_DEFAULT) return true;
		if (checkState == GRASS_BLOCK_SNOWY) return true;
		return checkState == PODZOL_SNOWY;
	}

	private static boolean isStateGrass(final BlockState checkState) {
		return checkState == GRASS_BLOCK_DEFAULT;
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

	public static BufferBuilder startOrContinueBufferBuilder(final ChunkRenderTask generator, final int blockRenderLayerOrdinal, final CompiledChunk compiledChunk, final BlockRenderLayer blockRenderLayer, ChunkRender renderChunk, BlockPos renderChunkPosition) {
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

	public static IChunk getChunk(final int currentChunkPosX, final int currentChunkPosZ, final IEnviromentBlockReader reader) {
//		if (reader instanceof IWorld) { // This should never be the case...
//			return ((IWorld) reader).getChunk(currentChunkPosX, currentChunkPosZ);
//		} else
		if (reader instanceof ChunkRenderCache) {
			ChunkRenderCache renderChunkCache = (ChunkRenderCache) reader;
			final int x = currentChunkPosX - renderChunkCache.chunkStartX;
			final int z = currentChunkPosZ - renderChunkCache.chunkStartZ;
			return renderChunkCache.chunks[x][z];
		} else if (OptiFineCompatibility.isChunkCacheOF(reader)) {
			Region region = OptiFineCompatibility.getRegion(reader);
			final int x = currentChunkPosX - region.chunkX;
			final int z = currentChunkPosZ - region.chunkZ;
			return region.chunks[x][z];
		}
		throw new IllegalStateException("Should Not Reach Here!");
	}

	public static void setupChunkRenderCache(final ChunkRenderCache _this, final int chunkStartX, final int chunkStartZ, final Chunk[][] chunks, final BlockPos start, final BlockPos end) {
		final int startX = start.getX();
		final int startY = start.getY();
		final int startZ = start.getZ();

		final int cacheSizeX = end.getX() - startX + 1;
		final int cacheSizeY = end.getY() - startY + 1;
		final int cacheSizeZ = end.getZ() - startZ + 1;

		final int size = cacheSizeX * cacheSizeY * cacheSizeZ;
		final BlockState[] blockStates = new BlockState[size];
		final IFluidState[] fluidStates = new IFluidState[size];

		int cx = (startX >> 4) - chunkStartX;
		int cz = (startZ >> 4) - chunkStartZ;
		Chunk currentChunk = chunks[cx][cz];

		try (BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain()) {
			int index = 0;
			for (int z = 0; z < cacheSizeZ; ++z) {
				for (int y = 0; y < cacheSizeY; ++y) {
					for (int x = 0; x < cacheSizeX; ++x, ++index) {

						final int posX = startX + x;
						final int posY = startY + y;
						final int posZ = startZ + z;

						final int ccx = ((startX + x) >> 4) - chunkStartX;
						final int ccz = ((startZ + z) >> 4) - chunkStartZ;

						boolean changed = false;
						if (cx != ccx) {
							cx = ccx;
							changed = true;
						}
						if (cz != ccz) {
							cz = ccz;
							changed = true;
						}
						if (changed) {
							currentChunk = chunks[cx][cz];
						}

						// TODO: Use System.arrayCopy on the chunk sections
						pooledMutableBlockPos.setPos(posX, posY, posZ);
//						blockStates[index] = currentChunk.getBlockState(pooledMutableBlockPos);
//						fluidStates[index] = currentChunk.getFluidState(posX, posY, posZ);
						final BlockState blockState = currentChunk.getBlockState(pooledMutableBlockPos);
						blockStates[index] = blockState;
						fluidStates[index] = blockState.getFluidState();
					}
				}
			}
		}

		_this.cacheSizeX = cacheSizeX;
		_this.cacheSizeY = cacheSizeY;
		_this.cacheSizeZ = cacheSizeZ;

		_this.blockStates = blockStates;
		_this.fluidStates = fluidStates;
	}

	private static Field findBlockColorsRegistryField() {
		final String fieldName = remapName(INameMappingService.Domain.FIELD, "field_186725_a");
		try {
			final Field field = BlockColors.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			return field;
		} catch (NoSuchFieldException e) {
			final CrashReport crashReport = new CrashReport("Unable to find field \"" + fieldName + "\". Field does not exist!", e);
			crashReport.makeCategory("Finding Field");
			throw new ReportedException(crashReport);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<IRegistryDelegate<Block>, IBlockColor> getBlockColorsRegistry(final BlockColors blockColors) {
//		return blockColors.colors;
		try {
			return (Map<IRegistryDelegate<Block>, IBlockColor>) BLOCK_COLORS_REGISTRY.get(blockColors);
		} catch (IllegalAccessException e) {
			final CrashReport crashReport = new CrashReport("Unable to access field!", e);
			crashReport.makeCategory("Accessing Field");
			throw new ReportedException(crashReport);
		}
	}

}
