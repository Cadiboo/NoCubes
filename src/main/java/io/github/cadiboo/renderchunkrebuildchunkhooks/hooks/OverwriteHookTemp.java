package io.github.cadiboo.renderchunkrebuildchunkhooks.hooks;

import com.google.common.collect.Sets;
import io.github.cadiboo.renderchunkrebuildchunkhooks.util.RenderChunkCacheReference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkCache;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static io.github.cadiboo.renderchunkrebuildchunkhooks.util.Utils.compiledChunk_setLayerUsed;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.util.Utils.renderChunk_postRenderBlocks;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.util.Utils.renderChunk_preRenderBlocks;

/**
 * @author Cadiboo
 */
public class OverwriteHookTemp {

// - The RebuildChunkPreEvent is called before any chunk rebuilding is done and allows access to the IWorldReader
// - The RebuildChunkPreRenderEvent is called before any chunk rendering is done and allows access to the BlockRendererDispatcher and the usedRenderLayer boolean array
// - The RebuildChunkFluidRenderInLayerEvent allows Modders to modify the BlockRenderLayers that fluids can render in
// - The RebuildChunkFluidEvent is called for every BlockRenderLayer for every fluid and allows Modders to add their own logic
// - The RebuildChunkBlockRenderInTypeEvent allows Modders to modify the EnumBlockRenderType that blocks can render in
// - The RebuildChunkBlockRenderInLayerEvent allows Modders to modify the BlockRenderLayers that blocks can render in
// - The RebuildChunkBlockEvent is called for every BlockRenderLayer for every block and allows Modders to add their own logic
// - The RebuildChunkPostRenderEvent is called after all chunk rebuilding logic is done but before Tile Entities are updated
// - The RebuildChunkPostEvent is called right before the method returns

	public static void rebuildChunk(
			final RenderChunk renderChunk,
			float x, float y, float z, ChunkRenderTask generator
	) {

//		LogManager.getLogger("RCRCH").info("rebuildChunk");

		CompiledChunk compiledchunk = new CompiledChunk();
		int i = 1;
		BlockPos blockpos = renderChunk.position.toImmutable();
		BlockPos blockpos1 = blockpos.add(15, 15, 15);
		World world = renderChunk.world;
		if (world != null) {
			generator.getLock().lock();

			try {
				if (generator.getStatus() != ChunkRenderTask.Status.COMPILING) {
					return;
				}

				generator.setCompiledChunk(compiledchunk);
			} finally {
				generator.getLock().unlock();
			}

			RenderChunkCache lvt_10_1_ = renderChunk.createRegionRenderCache(world, blockpos.add(-1, -1, -1), blockpos.add(16, 16, 16), 1);
			net.minecraftforge.client.MinecraftForgeClient.onRebuildChunk(renderChunk.world, renderChunk.position, lvt_10_1_);
			VisGraph lvt_11_1_ = new VisGraph();
			HashSet lvt_12_1_ = Sets.newHashSet();
			//START HOOK
			final RenderChunkCacheReference lvt_10_1_Reference = new RenderChunkCacheReference(lvt_10_1_);
			if (RenderChunkRebuildChunkHooksHooks.rebuildChunkCancelRenderingHook(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_Reference, lvt_11_1_, lvt_12_1_)) {
				return;
			}
			//END HOOK
			if (lvt_10_1_ != null) {
				++RenderChunk.renderChunksUpdated;
				boolean[] aboolean = new boolean[BlockRenderLayer.values().length];
				BlockModelRenderer.enableCache();
				Random random = new Random();
				BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
				//START HOOK
				RenderChunkRebuildChunkHooksHooks.rebuildChunkPreRenderHook(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher);
				//END HOOK

				for (BlockPos.MutableBlockPos blockpos$mutableblockpos : BlockPos.getAllInBoxMutable(blockpos, blockpos1)) {
					IBlockState iblockstate = lvt_10_1_.getBlockState(blockpos$mutableblockpos);
					Block block = iblockstate.getBlock();
					if (iblockstate.isOpaqueCube(lvt_10_1_, blockpos$mutableblockpos)) {
						lvt_11_1_.setOpaqueCube(blockpos$mutableblockpos);
					}

					if (iblockstate.hasTileEntity()) {
						TileEntity tileentity = lvt_10_1_.func_212399_a(blockpos$mutableblockpos, Chunk.EnumCreateEntityType.CHECK);
						if (tileentity != null) {
							TileEntityRenderer<TileEntity> tileentityrenderer = TileEntityRendererDispatcher.instance.getRenderer(tileentity);
							if (tileentityrenderer != null) {
								if (tileentityrenderer.isGlobalRenderer(tileentity)) {
									lvt_12_1_.add(tileentity);
								} else compiledchunk.addTileEntity(tileentity); // FORGE: Fix MC-112730
							}
						}
					}

					IFluidState ifluidstate = lvt_10_1_.getFluidState(blockpos$mutableblockpos);
					for (BlockRenderLayer blockrenderlayer1 : BlockRenderLayer.values()) {
						net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockrenderlayer1);
//						if (!ifluidstate.isEmpty() && ifluidstate.canRenderInLayer(blockrenderlayer1)) {
						if (!ifluidstate.isEmpty() && RenderChunkRebuildChunkHooksHooks.rebuildChunkCanFluidRenderInLayerHook(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher, blockpos$mutableblockpos, ifluidstate, blockrenderlayer1)) {
							int j = blockrenderlayer1.ordinal();
							BufferBuilder bufferbuilder = generator.getRegionRenderCacheBuilder().getBuilder(j);
							if (!compiledchunk.isLayerStarted(blockrenderlayer1)) {
								compiledchunk.setLayerStarted(blockrenderlayer1);
//								renderChunk.preRenderBlocks(bufferbuilder, blockpos);
								renderChunk_preRenderBlocks(renderChunk, bufferbuilder, blockpos);
							}

//							aboolean[j] |= blockrendererdispatcher.renderFluid(blockpos$mutableblockpos, lvt_10_1_, bufferbuilder, ifluidstate);
							if (RenderChunkRebuildChunkHooksHooks.rebuildChunkCanFluidBeRenderedHook(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher, blockpos$mutableblockpos, ifluidstate, blockrenderlayer1, j, bufferbuilder)) {
								aboolean[j] |= blockrendererdispatcher.renderFluid(blockpos$mutableblockpos, lvt_10_1_, bufferbuilder, ifluidstate);
							}
						}

//						if (iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE && iblockstate.canRenderInLayer(blockrenderlayer1)) {
						if (RenderChunkRebuildChunkHooksHooks.rebuildChunkCanBlockRenderWithTypeAndInLayerHook(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher, blockpos$mutableblockpos, iblockstate, block, blockrenderlayer1)) {
							int k = blockrenderlayer1.ordinal();
							BufferBuilder bufferbuilder1 = generator.getRegionRenderCacheBuilder().getBuilder(k);
							if (!compiledchunk.isLayerStarted(blockrenderlayer1)) {
								compiledchunk.setLayerStarted(blockrenderlayer1);
//								renderChunk.preRenderBlocks(bufferbuilder1, blockpos);
								renderChunk_preRenderBlocks(renderChunk, bufferbuilder1, blockpos);
							}

//							aboolean[k] |= blockrendererdispatcher.renderBlock(iblockstate, blockpos$mutableblockpos, lvt_10_1_, bufferbuilder1, random);
							if (RenderChunkRebuildChunkHooksHooks.rebuildChunkCanBlockBeRenderedHook(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher, blockpos$mutableblockpos, iblockstate, block, blockrenderlayer1, k, bufferbuilder1)) {
								aboolean[k] |= blockrendererdispatcher.renderBlock(iblockstate, blockpos$mutableblockpos, lvt_10_1_, bufferbuilder1, random);
							}
						}
					}
					net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
				}

				for (BlockRenderLayer blockrenderlayer : BlockRenderLayer.values()) {
					if (aboolean[blockrenderlayer.ordinal()]) {
//						compiledchunk.setLayerUsed(blockrenderlayer);
						compiledChunk_setLayerUsed(compiledchunk, blockrenderlayer);
					}

					if (compiledchunk.isLayerStarted(blockrenderlayer)) {
//						renderChunk.postRenderBlocks(blockrenderlayer, x, y, z, generator.getRegionRenderCacheBuilder().getBuilder(blockrenderlayer), compiledchunk);
						renderChunk_postRenderBlocks(renderChunk, blockrenderlayer, x, y, z, generator.getRegionRenderCacheBuilder().getBuilder(blockrenderlayer), compiledchunk);
					}
				}

				BlockModelRenderer.disableCache();
				//START HOOK
				RenderChunkRebuildChunkHooksHooks.rebuildChunkPostRenderHook(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockpos1, world, lvt_10_1_, lvt_11_1_, lvt_12_1_, aboolean, random, blockrendererdispatcher);
				//END HOOK
			}

			compiledchunk.setVisibility(lvt_11_1_.computeVisibility());
			renderChunk.lockCompileTask.lock();

			try {
				Set<TileEntity> set = Sets.newHashSet(lvt_12_1_);
				Set<TileEntity> set1 = Sets.newHashSet(renderChunk.setTileEntities);
				set.removeAll(renderChunk.setTileEntities);
				set1.removeAll(lvt_12_1_);
				renderChunk.setTileEntities.clear();
				renderChunk.setTileEntities.addAll(lvt_12_1_);
				renderChunk.renderGlobal.updateTileEntities(set1, set);
			} finally {
				renderChunk.lockCompileTask.unlock();
			}

		}

		//START HOOK
		RenderChunkRebuildChunkHooksHooks.rebuildChunkPostHook(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockpos1, world);
		//END HOOK
	}

}
