package cadiboo.nocubes.util;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.nocubes.renderer.MarchingCubes;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.material.Material;
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

import java.util.List;

/**
 * Utility Methods for Common Code
 *
 * @author Cadiboo
 */
public class ModUtil {

	public static boolean shouldSmooth(final IBlockState state) {

		for (String smoothableStateString : ModConfig.smoothableBlockStates) {
			if (state.toString().equals(smoothableStateString)) {
				return true;
			}
		}

		return false;
	}

	public static float getBlockDensity(final BlockPos pos, final IBlockAccess cache) {

		float density = 0.0F;

		final MutableBlockPos mutablePos = new MutableBlockPos(pos);

		for (int x = 0; x < 2; ++ x) {
			for (int y = 0; y < 2; ++ y) {
				for (int z = 0; z < 2; ++ z) {
					mutablePos.setPos(pos.getX() - x, pos.getY() - y, pos.getZ() - z);

					final IBlockState state = cache.getBlockState(mutablePos);

					if (ModUtil.shouldSmooth(state)) {
						density += 1;
					} else if (state.isNormalCube()) {

					} else if (state.getMaterial() == Material.VINE) {
						density -= 0.75;
					} else {
						density -= 1;
					}

					if (state.getBlock() == Blocks.BEDROCK) {
						density += 0.000000000000000000000000000000000000000000001f;
					}

				}
			}
		}

		return density;
	}

	public static void renderChunkSurfaceNets(final RebuildChunkPreEvent event) {

		return;

	}

	public static void renderBlockSurfaceNets(final RebuildChunkBlockEvent event) {

		return;

	}

	public static void renderChunkMarchingCubes(final RebuildChunkPreEvent event) {

		final BlockPos renderChunkPosition = event.getRenderChunkPosition();
		final int chunkx = renderChunkPosition.getX();
		final int chunky = renderChunkPosition.getY();
		final int chunkz = renderChunkPosition.getZ();

		final ChunkCache cache = event.getWorldView();

		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

		//		for (int y = chunky; y < (chunky + 16); ++ y) {
		//			for (int z = chunkz; z < (chunkz + 16); ++ z) {
		//				for (int x = chunkx; x < (chunkx + 16); ++ x) {
		//					final BlockPos pos = new BlockPos(x, y, z);
		//					final IBlockState state = cache.getBlockState(pos);
		//
		//					blockRenderLayers:
		//					for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
		//						if (! state.getBlock().canRenderInLayer(state, blockRenderLayer)) {
		//							continue blockRenderLayers;
		//						}
		//
		////						final BufferBuilder blockRenderLayerBufferBuilder = event.startOrContinueLayer(blockRenderLayer);
		//
		//						boolean used = false;
		//						if (shouldSmooth(state)) {
		//							used = MarchingCubes.renderBlock(state, pos, cache, blockRenderLayerBufferBuilder, blockRendererDispatcher);
		//						}
		//						if (! shouldSmooth(state) || ! used) {
		//							used = blockRendererDispatcher.renderBlock(state, pos, cache, blockRenderLayerBufferBuilder);
		//						}
		//
		////						event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, used);
		//					}
		//				}
		//			}
		//		}

		return;

	}

	public static void renderBlockMarchingCubes(final RebuildChunkBlockEvent event) {

		//		event.getUsedBlockRenderLayers()[event.getBlockRenderLayer().ordinal()] |= event.getBlockRendererDispatcher().renderBlock(event.getBlockState(), event.getBlockPos(), event.getWorldView(), event.getBufferBuilder());



		boolean used = false;
		used = MarchingCubes.renderBlock(event.getBlockState(), event.getBlockPos(), event.getWorldView(), event.getBufferBuilder(), event.getBlockRendererDispatcher());
		//TODO event.setCancelled(false);
		if (! used) {
			event.setCanceled(false);
			return;
//			used = event.getBlockRendererDispatcher().renderBlock(event.getBlockState(), event.getBlockPos(), event.getWorldView(), event.getBufferBuilder());
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
