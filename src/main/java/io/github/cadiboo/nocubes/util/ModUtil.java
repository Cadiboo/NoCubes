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
import net.minecraft.world.IBlockAccess;

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

	public static BakedQuad getQuad(final IBlockState state, final BlockPos pos, final BlockRendererDispatcher blockRendererDispatcher) {
		try {
			final long posRand = MathHelper.getPositionRandom(pos);

			final IBakedModel model = blockRendererDispatcher.getModelForState(state);
			List<BakedQuad> quads = new ArrayList<>();
			for (EnumFacing facing : ENUMFACING_QUADS_ORDERED) {
				if (!quads.isEmpty()) {
					break;
				}
				quads = model.getQuads(state, facing, posRand);
			}
			final BakedQuad quad = quads.get(0);
			return quad;
		} catch (final Exception e) {
			return null;
		}
	}

	public static TextureAtlasSprite getSprite(final IBlockState state, final BlockPos pos, final BlockRendererDispatcher blockRendererDispatcher) {

		if (true) {
			return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/sand");
		}

		try {
			return getQuad(state, pos, blockRendererDispatcher).getSprite();
		} catch (final Exception e) {
			return null;
		}

	}

	public static LightmapInfo getLightmapInfo(BlockPos pos, IBlockAccess cache) {

		switch (ModConfig.approximateLightingLevel) {
			default:
			case OFF:
				return new LightmapInfo(240, 0);
			case FAST:
				final int FASTpackedLightmapCoords = cache.getBlockState(pos).getPackedLightmapCoords(cache, pos.up());
				return new LightmapInfo(
						ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(FASTpackedLightmapCoords),
						ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(FASTpackedLightmapCoords)
				);
			case FANCY:
				final int[] skyLightBrightnesses = new int[EnumFacing.VALUES.length + 1];
				final int[] blockLightBrightnesses = new int[EnumFacing.VALUES.length + 1];

				for (EnumFacing facing : EnumFacing.VALUES) {
					final BlockPos brightnessPos = pos.offset(facing);
					final int packedLightmapCoords = cache.getBlockState(brightnessPos).getPackedLightmapCoords(cache, brightnessPos);

					skyLightBrightnesses[facing.ordinal()] = ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(packedLightmapCoords);
					blockLightBrightnesses[facing.ordinal()] = ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(packedLightmapCoords);
				}

				final int packedLightmapCoords = cache.getBlockState(pos).getPackedLightmapCoords(cache, pos);

				skyLightBrightnesses[EnumFacing.VALUES.length] = ModUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(packedLightmapCoords);
				blockLightBrightnesses[EnumFacing.VALUES.length] = ModUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(packedLightmapCoords);

				return new LightmapInfo(
						(int) Arrays.stream(skyLightBrightnesses).average().getAsDouble(),
						(int) Arrays.stream(blockLightBrightnesses).average().getAsDouble()
				);
		}
	}

	public static int getLightmapSkyLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return (packedLightmapCoords >> 16) & 0xFFFF; // get upper 4 bytes
	}

	public static int getLightmapBlockLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {
		return (packedLightmapCoords) & 0xFFFF; // get lower 4 bytes
	}

}
