package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockRenderLayer;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.chunk.ChunkRenderData;
import net.minecraft.client.render.chunk.ChunkRenderTask;
import net.minecraft.client.render.chunk.ChunkRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutable;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ExtendedBlockView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static net.minecraft.block.BlockRenderLayer.CUTOUT;
import static net.minecraft.block.BlockRenderLayer.MIPPED_CUTOUT;
import static net.minecraft.util.math.Direction.DOWN;
import static net.minecraft.util.math.Direction.EAST;
import static net.minecraft.util.math.Direction.NORTH;
import static net.minecraft.util.math.Direction.SOUTH;
import static net.minecraft.util.math.Direction.UP;
import static net.minecraft.util.math.Direction.WEST;
import static net.minecraft.util.math.MathHelper.clamp;

/**
 * Util that is only used on the Physical Client i.e. Rendering code
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
@Environment(EnvType.CLIENT)
public final class ClientUtil {

	public static final MethodHandle renderChunk_preRenderBlocks;
	static {
		renderChunk_preRenderBlocks = MethodHandles.publicLookup().unreflect(

		)
	}

	/**
	 * The order of {@link Direction} and null used in {@link #getQuad(BlockState, BlockPos, BlockRenderManager)}
	 */
	public static final Direction[] ENUMFACING_QUADS_ORDERED = {
			UP, null, DOWN, NORTH, EAST, SOUTH, WEST,
	};

	// add or subtract from the sprites UV location to remove transparent lines in between textures
	public static final float UV_CORRECT = 1 / 10000F;

//	private static final ThreadLocal<HashMap<BlockPos, HashMap<BlockPos, Object[]>>> RENDER_LIQUID_POSITIONS = ThreadLocal.withInitial(HashMap::new);

//	private static final Constructor<AmbientOcclusionFace> ambientOcclusionFace;
//	private static final boolean ambientOcclusionFaceNeedsBlockModelRenderer;
//	static {
//		Constructor<AmbientOcclusionFace> ambientOcclusionFaceConstructor = null;
//		boolean needsBlockModelRenderer = false;
//		try {
//			try {
//				//TODO: stop using ReflectionHelper
//				ambientOcclusionFaceConstructor = ReflectionHelper.findConstructor(AmbientOcclusionFace.class);
//			} catch (UnknownConstructorException e) {
//				//TODO: stop using ReflectionHelper
//				ambientOcclusionFaceConstructor = ReflectionHelper.findConstructor(AmbientOcclusionFace.class, BlockRenderManager.class);
//				needsBlockModelRenderer = true;
//			}
//		} catch (Exception e) {
//			final CrashReport crashReport = new CrashReport("Unable to find constructor for BlockModelRenderer$AmbientOcclusionFace", e);
//			crashReport.addElement("Finding Constructor");
//			throw new CrashException(crashReport);
//		}
//		ambientOcclusionFace = ambientOcclusionFaceConstructor;
//		ambientOcclusionFaceNeedsBlockModelRenderer = needsBlockModelRenderer;
//	}

	/**
	 * @param red   the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @param green the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @param blue  the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @return the color in ARGB format
	 */
	public static int color(int red, int green, int blue) {

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
		return color(redInt, greenInt, blueInt);
	}

	public static int getLightmapSkyLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return (packedLightmapCoords >> 16) & 0xFFFF; // get upper 4 bytes
	}

	public static int getLightmapBlockLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return packedLightmapCoords & 0xFFFF; // get lower 4 bytes
	}

	/**
	 * Gets The first quad of a model for a pos & state or null if the model has no quads
	 *
	 * @param state                   the state
	 * @param pos                     the position used in {@link MathHelper#hashCode(Vec3i)}
	 * @param blockRendererDispatcher the {@link BlockRenderManager} to get the model from
	 * @return The first quad or null if the model has no quads
	 */
	@Nullable
	public static BakedQuad getQuad(final BlockState state, final BlockPos pos, final BlockRenderManager blockRendererDispatcher) {
		final long posRand = MathHelper.hashCode(pos);
		final BakedModel model = blockRendererDispatcher.getModel(state);
		return getQuad(state, pos, posRand, model, ENUMFACING_QUADS_ORDERED);
	}

	/**
	 * Gets The first quad of a model for a pos & state or null if the model has no quads
	 *
	 * @param state                   the state
	 * @param pos                     the position used in {@link MathHelper#hashCode(Vec3i)}
	 * @param blockRendererDispatcher the {@link BlockRenderManager} to get the model from
	 * @param facing                  the {@link Direction to check first}
	 * @return The first quad or null if the model has no quads
	 */
	@Nullable
	public static BakedQuad getQuad(final BlockState state, final BlockPos pos, final BlockRenderManager blockRendererDispatcher, Direction facing) {
		final long posRand = MathHelper.hashCode(pos);
		final BakedModel model = blockRendererDispatcher.getModel(state);
		final BakedQuad quad = getQuad(state, pos, posRand, model, facing);
		if (quad != null) {
			return quad;
		} else {
			return getQuad(state, pos, posRand, model, ENUMFACING_QUADS_ORDERED);
		}
	}

	/**
	 * helper method to actually get the quads
	 */
	@Nullable
	private static BakedQuad getQuad(final BlockState state, final BlockPos pos, final long posRand, final BakedModel model, final Direction... facings) {
		for (Direction facing : facings) {
			//TODO fix Random
			final List<BakedQuad> quads = model.getQuads(state, facing, new Random(posRand));
			if (!quads.isEmpty()) {
				return quads.get(0);
			}
		}
		return null;
	}

	/**
	 * Gets the color of a quad through a block at a pos
	 *
	 * @param quad  the quad
	 * @param state the state
	 * @param cache the cache
	 * @param pos   the pos
	 * @return the color
	 */
	public static int getColor(final BakedQuad quad, final BlockState state, final ExtendedBlockView cache, final BlockPos pos) {
		final int red;
		final int green;
		final int blue;

		if (quad.hasColor()) {
			final int colorMultiplier = MinecraftClient.getInstance().getBlockColorMap().getRenderColor(state, cache, pos, 0);
			red = (colorMultiplier >> 16) & 255;
			green = (colorMultiplier >> 8) & 255;
			blue = colorMultiplier & 255;
		} else {
			red = 0xFF;
			green = 0xFF;
			blue = 0xFF;
		}
		return color(red, green, blue);
	}

	/**
	 * Gets the fixed minimum U coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The fixed minimum U coordinate to use when rendering the sprite
	 */
	public static float getMinU(final Sprite sprite) {
		return sprite.getMinU() + UV_CORRECT;
	}

	/**
	 * Gets the fixed maximum U coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The fixed maximum U coordinate to use when rendering the sprite
	 */
	public static float getMaxU(final Sprite sprite) {
		return sprite.getMaxU() - UV_CORRECT;
	}

	/**
	 * Gets the fixed minimum V coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The fixed minimum V coordinate to use when rendering the sprite
	 */
	public static float getMinV(final Sprite sprite) {
		return sprite.getMinV() + UV_CORRECT;
	}

	/**
	 * Gets the fixed maximum V coordinate to use when rendering the sprite.
	 *
	 * @param sprite the sprite
	 * @return The fixed maximum V coordinate to use when rendering the sprite
	 */
	public static float getMaxV(final Sprite sprite) {
		return sprite.getMaxV() - UV_CORRECT;
	}

	//TODO
	private static final int[][] OFFSETS_ORDERED = {
			// check 6 immediate neighbours
			{+0, -1, +0},
			{+0, +1, +0},
			{+1, +0, +0},
			{-1, +0, +0},
			{+0, +0, +1},
			{+0, +0, -1},
			// check 8 corner neighbours
			{+1, +1, +1},
			{+1, +1, -1},
			{-1, +1, +1},
			{-1, +1, -1},
			{+1, -1, +1},
			{+1, -1, -1},
			{-1, -1, +1},
			{-1, -1, -1},
//			// check 6 immediate neighbours
//			{0, -1, 0},
//			{0, +1, 0},
//			{-1, 0, 0},
//			{+1, 0, 0},
//			{0, 0, -1},
//			{0, 0, +1},
//			// check 8 corner neighbours
//			{-1, -1, -1},
//			{-1, -1, +1},
//			{+1, -1, -1},
//			{+1, -1, +1},
//			{-1, +1, -1},
//			{-1, +1, +1},
//			{+1, +1, -1},
//			{+1, +1, +1},
	};

	/**
	 * @param cache
	 * @param pos
	 * @param state
	 * @param pooledMutableBlockPos
	 * @return a state and a texture pos which is guaranteed to be immutable
	 */
	//TODO: state cache?
	public static Object[] getTexturePosAndState(
			@Nonnull final ExtendedBlockView cache,
			@Nonnull final BlockPos pos,
			@Nonnull final BlockState state,
			@Nonnull final IIsSmoothable isStateSmoothable,
			@Nonnull PooledMutable pooledMutableBlockPos
	) {

		BlockState textureState = state;
		BlockPos texturePos = pos;

		//check pos first
		if (isStateSmoothable.isSmoothable(cache.getBlockState(pos))) {
			return new Object[]{
					texturePos,
					textureState
			};
		}

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

//			if (ModConfig.beautifyTexturesLevel == FANCY) {
//
//				for (int[] withOffset : OFFSETS_ORDERED) {
//					final BlockState tempState = cache.getBlockState(pooledMutableBlockPos.setPos(x + withOffset[0], y + withOffset[1], z + withOffset[2]));
//					if (tempState.getBlock() == Blocks.SNOW_LAYER) {
//						textureState = tempState;
//						texturePos = pooledMutableBlockPos.toImmutable();
//						break;
//					}
//				}
//
//				for (int[] withOffset : OFFSETS_ORDERED) {
//					final BlockState tempState = cache.getBlockState(pooledMutableBlockPos.setPos(x + withOffset[0], y + withOffset[1], z + withOffset[2]));
//					if (tempState.getBlock() == Blocks.GRASS) {
//						textureState = tempState;
//						texturePos = pooledMutableBlockPos.toImmutable();
//						break;
//					}
//				}
//			}

		for (int[] offset : OFFSETS_ORDERED) {
			final BlockState tempState = cache.getBlockState(pooledMutableBlockPos.set(x + offset[0], y + offset[1], z + offset[2]));
			if (isStateSmoothable.isSmoothable(tempState)) {
				textureState = tempState;
				texturePos = pooledMutableBlockPos.toImmutable();
				break;
			}
		}

		return new Object[]{
				texturePos,
				textureState
		};

	}

//	public static AmbientOcclusionFace makeAmbientOcclusionFace() {
//		try {
//			if (ambientOcclusionFaceNeedsBlockModelRenderer) {
//				return ambientOcclusionFace.newInstance(MinecraftClient.getInstance().getBlockRenderManager().getModelRenderer());
//			} else {
//				return ambientOcclusionFace.newInstance();
//			}
//		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
//			CrashReport crashReport = new CrashReport("Instantiating BlockModelRenderer$AmbientOcclusionFace!", e);
//			final CrashReportSection crashReportCategory = crashReport.addElement("Reflectively Accessing BlockModelRenderer$AmbientOcclusionFace");
//			crashReportCategory.add("Needs BlockModelRenderer", ambientOcclusionFaceNeedsBlockModelRenderer);
//			throw new CrashException(crashReport);
//		}
//	}

	public static BlockRenderLayer getRenderLayer(BlockState state) {
		final BlockRenderLayer blockRenderLayer = state.getBlock().getRenderLayer();
		switch (blockRenderLayer) {
			default:
			case SOLID:
			case TRANSLUCENT:
				return blockRenderLayer;
			case MIPPED_CUTOUT:
				return MinecraftClient.getInstance().options.mipmapLevels == 0 ? CUTOUT : MIPPED_CUTOUT;
			case CUTOUT:
				return MinecraftClient.getInstance().options.mipmapLevels != 0 ? MIPPED_CUTOUT : CUTOUT;
		}
	}

	public static BufferBuilder startOrContinueBufferBuilder(final ChunkRenderTask generator, final int blockRenderLayerOrdinal, final ChunkRenderData compiledChunk, final BlockRenderLayer blockRenderLayer, ChunkRenderer renderChunk, BlockPos renderChunkPosition) {
		final BufferBuilder bufferBuilder = generator.getBufferBuilders().get(blockRenderLayerOrdinal);
		if (!compiledChunk.isBufferInitialized(blockRenderLayer)) {
			compiledChunk.markBufferInitialized(blockRenderLayer);
			renderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPosition);
		}
		return bufferBuilder;
	}

	public static int getExtendLiquidsRange() {
		switch (ModConfig.extendLiquids) {
			default:
			case Off:
				return 0;
			case OneBlock:
				return 1;
			case TwoBlocks:
				return 2;
		}
	}

}
