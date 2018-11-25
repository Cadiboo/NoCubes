package cadiboo.nocubes.util;

import cadiboo.nocubes.renderer.MarchingCubes;
import cadiboo.nocubes.renderer.SurfaceNets;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockStone;
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

import java.util.List;

/**
 * Utility Methods for Common Code
 *
 * @author Cadiboo
 */
public class ModUtil {

	public static boolean shouldSmooth(final IBlockState state) {

		return state.getBlock() instanceof BlockStone || state.getBlock() instanceof BlockAir;

		//		return ModConfig.getFastSmoothableBlockStates().contains(state);

	}

	public static boolean shouldSmoothS(final IBlockState state) {

		return state.getBlock() instanceof BlockStone;

		//		return ModConfig.getFastSmoothableBlockStates().contains(state);

	}

	public static float getBlockDensity(final BlockPos pos, final IBlockAccess cache) {

		float density = 0.0F;

		final MutableBlockPos mutablePos = new MutableBlockPos(pos);

		for (int x = 0; x < 2; ++ x) {
			for (int y = 0; y < 2; ++ y) {
				for (int z = 0; z < 2; ++ z) {
					mutablePos.setPos(pos.getX() - x, pos.getY() - y, pos.getZ() - z);

					final IBlockState state = cache.getBlockState(mutablePos);

					if (ModUtil.shouldSmoothS(state)) {
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

	public static void renderBlockSurfaceNets(final RebuildChunkBlockEvent event) {

		boolean used = false;
		if (!shouldSmoothS(event.getBlockState())) {
			used = SurfaceNets.renderBlock(event.getBlockState(), event.getBlockPos(), event.getChunkCache(), event.getBufferBuilder(), event.getBlockRendererDispatcher());
		}
		if (! used) {
			event.setCanceled(false);
			return;
		}
		event.getUsedBlockRenderLayers()[event.getBlockRenderLayer().ordinal()] |= used;

	}

	public static void renderBlockMarchingCubes(final RebuildChunkBlockEvent event) {

		boolean used = false;
		used = MarchingCubes.renderBlock1(event.getBlockState(), event.getBlockPos(), event.getChunkCache(), event.getBufferBuilder(), event.getBlockRendererDispatcher());
		if (! used) {
			event.setCanceled(false);
			return;
		}
		event.getUsedBlockRenderLayers()[event.getBlockRenderLayer().ordinal()] |= used;

	}

	public static TextureAtlasSprite getSprite(final IBlockState state, final BlockPos pos, final BlockRendererDispatcher blockRendererDispatcher) {

		try {
			final long posRand = MathHelper.getPositionRandom(pos);

			final IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
			List<BakedQuad> quads = model.getQuads(state, EnumFacing.UP, posRand);
			if (quads.isEmpty()) {
				getQuads:
				for (EnumFacing facing : EnumFacing.VALUES) {
					if (facing == EnumFacing.NORTH) {
						facing = null;
					}
					quads = model.getQuads(state, EnumFacing.UP, posRand);
					if (! quads.isEmpty()) {
						break getQuads;
					}
				}
			}
			final BakedQuad quad = quads.get(0);
			return quad.getSprite();
		} catch (final Exception e) {
			return null;
		}
	}

	public static int getLightmapSkyLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {

		return (packedLightmapCoords >> 16) & 0xFFFF; // get upper 4 bytes
	}

	public static int getLightmapBlockLightCoordsFromPackedLightmapCoords(int packedLightmapCoords) {

		return (packedLightmapCoords) & 0xFFFF; // get lower 4 bytes
	}

}
