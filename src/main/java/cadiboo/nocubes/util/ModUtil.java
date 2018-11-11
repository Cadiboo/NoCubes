package cadiboo.nocubes.util;

import cadiboo.nocubes.renderer.MarchingCubes;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlocksEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;

/**
 * Utility Methods for Common Code
 *
 * @author Cadiboo
 */
public class ModUtil {

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

		for (int y = chunky; y < (chunky + 16); ++y) {
			for (int z = chunkz; z < (chunkz + 16); ++z) {
				for (int x = chunkx; x < (chunkx + 16); ++x) {
					final IBlockState state = cache.getBlockState(new BlockPos(x, y, z));
//					rendered |= renderGrid(state, x, y, z, cache);
					MarchingCubes.renderGrid(state, x, y, z, cache);
				}
			}
		}

		return 1;

	}

	public static void renderBlockMarchingCubes(final RebuildChunkBlockEvent event) {

		return;

	}

}
