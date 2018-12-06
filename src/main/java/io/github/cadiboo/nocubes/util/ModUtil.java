package io.github.cadiboo.nocubes.util;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;

/**
 * @author Cadiboo
 */
public class ModUtil {

	public static final EnumFacing[] ENUMFACING_QUADS_ORDERED = {
			UP, null, DOWN, NORTH, EAST, SOUTH, WEST,
	};

	public static boolean shouldRenderInState(final IBlockState state) {
		return true;
	}

	public static boolean shouldSmooth(final IBlockState state) {
		return ModConfig.getSmoothableBlockStatesCache().contains(state);
	}

	public static float getBlockDensity(final BlockPos pos, final IBlockAccess cache) {

		float density = 0.0F;

		final MutableBlockPos mutablePos = new MutableBlockPos(pos);

		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				for (int z = 0; z < 2; ++z) {
					mutablePos.setPos(pos.getX() - x, pos.getY() - y, pos.getZ() - z);

					final IBlockState state = cache.getBlockState(mutablePos);

					if (ModUtil.shouldSmooth(state)) {
						density += 1;
						//					} else if (state.isNormalCube()) {
						//
						//					} else if (state.getMaterial() == Material.VINE) {
						//						density -= 0.75;
						//					} else {
						//						density -= 1;
					}

					if (state.getBlock() == Blocks.BEDROCK) {
						density += 0.000000000000000000000000000000000000000000001f;
					}

				}
			}
		}

		return density;
	}

	public static void renderBlockMarchingCubes(final RebuildChunkBlockEvent event) {

		// Marching Cubes is an algorithm for rendering isosurfaces in volumetric data.

		// The basic notion is that we can define a voxel(cube) by the pixel values at the eight corners of the cube.

		// If one or more pixels of a cube have values less than the user-specified isovalue,
		// and one or more have values greater than this value,
		// we know the voxel must contribute some component of the isosurface.

		// By determining which edges of the cube are intersected by the isosurface,
		// we can create triangular patches which divide the cube between regions within the isosurface and regions outside.

		// By connecting the patches from all cubes on the isosurface boundary,
		// we get a surface representation.

	}

	private static BakedQuad cachedQuad = null;

	@Nullable
	public static BakedQuad getQuad(final IBlockState state, final BlockPos pos, final BlockRendererDispatcher blockRendererDispatcher) {

		final long posRand = MathHelper.getPositionRandom(pos);

		final IBakedModel model = blockRendererDispatcher.getModelForState(state);
		List<BakedQuad> quads = new ArrayList<>();

		try {
			for (EnumFacing facing : ENUMFACING_QUADS_ORDERED) {
				if (!quads.isEmpty()) {
					break;
				}
				quads = model.getQuads(state, facing, posRand);
			}
			final BakedQuad quad = quads.get(0);
			if (ModConfig.cacheQuads) {
				if (quad == null) {
					return cachedQuad;
				} else {
					if (!quad.getSprite().equals(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite())) {
						cachedQuad = quad;
					}
					return quad;
				}
			}
			return quad;
		} catch (final Exception e) {
			if (ModConfig.cacheQuads) {
				return cachedQuad;
			} else {
				return null;
			}
		}
	}

	@Nullable
	public static TextureAtlasSprite getSprite(final IBlockState state, final BlockPos pos, final BlockRendererDispatcher blockRendererDispatcher) {

//		if (true) {
//			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/sand");
//		}

		TextureAtlasSprite sprite;

		try {
			sprite = getQuad(state, pos, blockRendererDispatcher).getSprite();
		} catch (final NullPointerException e) {
			sprite = null;
		}

		if (sprite == null || sprite.equals(Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite())) {
			if (ModConfig.cacheQuads) {
				if (cachedQuad != null) {
					sprite = cachedQuad.getSprite();
				}
			}
		}

		return sprite;

	}

	public static LightmapInfo getLightmapInfo(BlockPos pos, IBlockAccess cache) {

		switch (ModConfig.approximateLightingLevel) {
			default:
			case OFF:
				return new LightmapInfo(240, 0);
			case FAST:
				//the block above
				final BlockPos FAST_BrightnessPos = pos.up();
				final int FAST_PackedLightmapCoords = cache.getBlockState(FAST_BrightnessPos).getPackedLightmapCoords(cache, FAST_BrightnessPos);
				return new LightmapInfo(
						ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(FAST_PackedLightmapCoords),
						ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(FAST_PackedLightmapCoords)
				);
			case FANCYISH:
				final int[] skyLightBrightnesses = new int[EnumFacing.VALUES.length + 1];
				final int[] blockLightBrightnesses = new int[EnumFacing.VALUES.length + 1];

				//every neighbour
				for (EnumFacing facing : EnumFacing.VALUES) {
					final BlockPos brightnessPos = pos.offset(facing);
					final int FANCY_PackedLightmapCoords = cache.getBlockState(brightnessPos).getPackedLightmapCoords(cache, brightnessPos);

					skyLightBrightnesses[facing.ordinal()] = ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(FANCY_PackedLightmapCoords);
					blockLightBrightnesses[facing.ordinal()] = ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(FANCY_PackedLightmapCoords);
				}

				//this block
				final int FANCY_PackedLightmapCoords = cache.getBlockState(pos).getPackedLightmapCoords(cache, pos);
				skyLightBrightnesses[EnumFacing.VALUES.length] = ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(FANCY_PackedLightmapCoords);
				blockLightBrightnesses[EnumFacing.VALUES.length] = ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(FANCY_PackedLightmapCoords);

				return new LightmapInfo(
						(int) Arrays.stream(skyLightBrightnesses).average().getAsDouble(),
						(int) Arrays.stream(blockLightBrightnesses).average().getAsDouble()
				);
			case FANCY: //credit to MineAndCraft12
				int averageSkyLight = 0;
				int totalBlocksCheckedForSkyLight = 0;

				int averageBlockLight = 0;
				int totalBlocksCheckedForBlockLight = 0;

				//every neighbour
				for (EnumFacing facing : EnumFacing.VALUES) {
					final BlockPos FANCYISH_BrightnessPos = pos.offset(facing);
					final int FANCYISH_PackedLightmapCoords = cache.getBlockState(FANCYISH_BrightnessPos).getPackedLightmapCoords(cache, FANCYISH_BrightnessPos);
					final int skyLight = ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(FANCYISH_PackedLightmapCoords);
					if (skyLight > 0) {
						averageSkyLight += skyLight;
						totalBlocksCheckedForSkyLight++;
					}
					final int blockLight = ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(FANCYISH_PackedLightmapCoords);
					if (blockLight > 0) {
						averageBlockLight += blockLight;
						totalBlocksCheckedForBlockLight++;
					}
				}

				//this block
				final int FANCYISH_PackedLightmapCoords = cache.getBlockState(pos).getPackedLightmapCoords(cache, pos);
				final int skyLight = ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(FANCYISH_PackedLightmapCoords);
				if (skyLight > 0) {
					averageSkyLight += skyLight;
					totalBlocksCheckedForSkyLight++;
				}
				final int blockLight = ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(FANCYISH_PackedLightmapCoords);
				if (blockLight > 0) {
					averageBlockLight += blockLight;
					totalBlocksCheckedForBlockLight++;
				}

				return new LightmapInfo(
						totalBlocksCheckedForSkyLight > 0 ? averageSkyLight / totalBlocksCheckedForSkyLight : averageSkyLight,
						totalBlocksCheckedForBlockLight > 0 ? averageBlockLight / totalBlocksCheckedForBlockLight : averageBlockLight
				);

		}
	}

	public static int getLightmapSkyLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return (packedLightmapCoords >> 16) & 0xFFFF; // get upper 4 bytes
	}

	public static int getLightmapBlockLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return (packedLightmapCoords) & 0xFFFF; // get lower 4 bytes
	}

	public static int getColor(final BakedQuad quad, final IBlockState state, final ChunkCache cache, final BlockPos pos) {

		final int red;
		final int green;
		final int blue;

		if (quad.hasTintIndex()) {
			final int colorMultiplier = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, cache, pos, 0);
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
	 * @param red   the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @param green the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @param blue  the red value of the color, between 0x00 (decimal 0) and 0xFF (decimal 255)
	 * @return the color in ARGB format
	 */
	public static int color(int red, int green, int blue) {

		red = MathHelper.clamp(red, 0x00, 0xFF);
		green = MathHelper.clamp(green, 0x00, 0xFF);
		blue = MathHelper.clamp(blue, 0x00, 0xFF);

		final int alpha = 0xFF;

		// 0x alpha red green blue
		// 0xaarrggbb

		int colorRGBA = 0;
		colorRGBA |= red << 16;
		colorRGBA |= green << 8;
		colorRGBA |= blue << 0;
		colorRGBA |= alpha << 24;

		return colorRGBA;
	}

}
