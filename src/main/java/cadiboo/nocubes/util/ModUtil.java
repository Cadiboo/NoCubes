package cadiboo.nocubes.util;

import cadiboo.nocubes.renderer.MarchingCubes;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlocksEvent;
import net.minecraft.block.BlockClay;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGlowstone;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockNetherrack;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

/**
 * Utility Methods for Common Code
 *
 * @author Cadiboo
 */
public class ModUtil {

	public static boolean shouldSmooth(final IBlockState state) {
		boolean smooth = false;

		smooth |= state.getBlock() instanceof BlockGrass;
		smooth |= state.getBlock() instanceof BlockStone;
		smooth |= state.getBlock() instanceof BlockSand;
//		smooth |= state.getBlock() instanceof BlockSandStone;
		smooth |= state == Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT);
		smooth |= state == Blocks.RED_SANDSTONE.getDefaultState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.DEFAULT);
		smooth |= state.getBlock() instanceof BlockGravel;
		smooth |= state.getBlock() instanceof BlockOre;
		smooth |= state.getBlock() instanceof BlockRedstoneOre;
		smooth |= state.getBlock() instanceof BlockSilverfish;
		smooth |= state.getBlock() instanceof BlockGrassPath;
		smooth |= state.getBlock() instanceof BlockDirt;
		smooth |= state.getBlock() instanceof BlockClay;
		smooth |= state.getBlock() instanceof BlockSnow;
		smooth |= state.getBlock() == Blocks.BEDROCK;

		smooth |= state.getBlock() instanceof BlockNetherrack;
		smooth |= state.getBlock() instanceof BlockGlowstone;

		smooth |= state.getBlock() == Blocks.END_STONE;

		smooth |= state.getBlock() instanceof BlockMycelium;

		return smooth;
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

	public static int renderChunkSurfaceNets(final RebuildChunkBlocksEvent event) {

		return 1;

	}

	public static void renderBlockSurfaceNets(final RebuildChunkBlockEvent event) {

		return;

	}

	public static int renderChunkMarchingCubes(final RebuildChunkBlocksEvent event) {

		final BlockPos renderChunkPosition = event.getRenderChunkPosition();
		final int chunkx = renderChunkPosition.getX();
		final int chunky = renderChunkPosition.getY();
		final int chunkz = renderChunkPosition.getZ();

		final ChunkCache cache = event.getWorldView();

		final BlockRendererDispatcher blockRendererDispatcher = event.getBlockRendererDispatcher();

		boolean solidUsed = false;

		for (int y = chunky; y < (chunky + 16); ++y) {
			for (int z = chunkz; z < (chunkz + 16); ++z) {
				for (int x = chunkx; x < (chunkx + 16); ++x) {
					final BlockPos pos = new BlockPos(x, y, z);
					final IBlockState state = cache.getBlockState(pos);
					boolean used = false;
					if (shouldSmooth(state)) {
						used = MarchingCubes.renderBlock(state, pos, cache, event.startOrContinueLayer(BlockRenderLayer.SOLID));
					}
					if (!shouldSmooth(state) || !used) {
						used = blockRendererDispatcher.renderBlock(state, pos, cache, event.startOrContinueLayer(BlockRenderLayer.SOLID));
					}

					solidUsed |= used;
				}
			}
		}

		event.setBlockRenderLayerUsed(BlockRenderLayer.SOLID, solidUsed);

		return 1;

	}

	public static void renderBlockMarchingCubes(final RebuildChunkBlockEvent event) {

		return;

	}

}
