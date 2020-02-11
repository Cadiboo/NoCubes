package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Cadiboo
 */
@SuppressWarnings("unused") // Hooks are called with ASM
public final class Hooks {

//	/**
//	 * Called from: ChunkRender#rebuildChunk right before the BlockPos.getAllInBoxMutable iteration
//	 * Calls: RenderDispatcher.renderChunk to render all our fluids and smooth terrain
//	 */
//	@OnlyIn(Dist.CLIENT)
//	public static void preIteration(final ChunkRender renderChunk, final float x, final float y, final float z, final ChunkRenderTask generator, final CompiledChunk compiledchunk, final BlockPos blockpos, final BlockPos blockpos1, final World world, final VisGraph lvt_10_1_, final HashSet lvt_11_1_, final IEnviromentBlockReader lvt_12_1_, final boolean[] aboolean, final Random random, final BlockRendererDispatcher blockrendererdispatcher) {
//		RenderDispatcher.renderChunk(renderChunk, blockpos, generator, compiledchunk, world, lvt_12_1_, aboolean, random, blockrendererdispatcher);
//	}

//	/**
//	 * Called from: BlockRendererDispatcher#renderBlockDamage before any other logic
//	 * Calls: RenderDispatcher.renderSmoothBlockDamage if the blockstate is smoothable
//	 *
//	 * @return If normal rendering should be cancelled (i.e. normal rendering should NOT happen)
//	 */
//	@OnlyIn(Dist.CLIENT)
//	public static boolean renderBlockDamage(final BlockRendererDispatcher blockrendererdispatcher, final BlockState iblockstate, final BlockPos blockpos, final TextureAtlasSprite textureatlassprite, final IEnviromentBlockReader world) {
//		if (!NoCubesConfig.Client.renderSmoothTerrain || !iblockstate.nocubes_isTerrainSmoothable) {
//			if (!NoCubesConfig.Client.renderSmoothLeaves || !iblockstate.nocubes_isLeavesSmoothable) {
//				return false;
//			}
//		}
//		final Tessellator tessellator = Tessellator.getInstance();
//		RenderDispatcher.renderSmoothBlockDamage(tessellator, tessellator.getBuffer(), blockpos, iblockstate, world, textureatlassprite);
//		return true;
//	}

//	/**
//	 * Called from: World#getFluidState after the bounds check in place of the normal getFluidState logic
//	 * Calls: ModUtil.getFluidState to handle extended fluids
//	 *
//	 * @return An IFluidState
//	 */
//	public static IFluidState getFluidState(final World world, final BlockPos pos) {
//		return ModUtil.getFluidState(world, pos);
//	}

	/**
	 * Called from: BlockState#getCollisionShape(IBlockReader, BlockPos, ISelectionContext) before any other logic
	 * Calls: CollisionHandler.getCollisionShape to handle mesh collisions
	 *
	 * @return The collision shape for the BlockState or null if vanilla should handle it
	 */
	public static VoxelShape getCollisionShape(final BlockState _this, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (NoCubesConfig.Server.terrainCollisions && IsSmoothable.TERRAIN_SMOOTHABLE.test(_this))
			return CollisionHandler.getCollisionShape(_this, worldIn, pos);
		else
			return null;
	}

	/**
	 * Called from: BlockState#getCollisionShape(IBlockReader, BlockPos) before any other logic
	 * Calls: CollisionHandler.getCollisionShape to handle mesh collisions
	 *
	 * @return The collision shape for the BlockState or null if vanilla should handle it
	 */
	public static VoxelShape getCollisionShape(final BlockState _this, IBlockReader worldIn, BlockPos pos) {
		if (NoCubesConfig.Server.terrainCollisions && IsSmoothable.TERRAIN_SMOOTHABLE.test(_this))
			return CollisionHandler.getCollisionShape(_this, worldIn, pos);
		else
			return null;
	}

//	/**
//	 * Called from: ChunkRender#rebuildChunk right before BlockState#getRenderType is called
//	 * Calls: Nothing
//	 * Disables vanilla rendering for smoothable BlockStates
//	 *
//	 * @return If the state can render
//	 */
//	@OnlyIn(Dist.CLIENT)
//	public static boolean canBlockStateRender(final BlockState blockstate) {
////		if (blockstate.nocubes_isTerrainSmoothable && Config.renderSmoothTerrain) return false;
////		if (blockstate.nocubes_isLeavesSmoothable) {
////			if (Config.renderSmoothLeaves)
////				return Config.renderSmoothAndVanillaLeaves;
////			return true;
////		}
//		return true;
//	}

//	/**
//	 * Called from: BlockState#causesSuffocation before any other logic
//	 * Calls: ModUtil.doesTerrainCauseSuffocation
//	 *
//	 * @return If the state does NOT cause suffocation (If normal suffocation checks should be bypassed and false returned)
//	 */
//	public static boolean doesNotCauseSuffocation(final BlockState blockState, final IBlockReader reader, final BlockPos pos) {
////		if (Config.terrainCollisions) {
////			if (!blockState.nocubes_isTerrainSmoothable) {
////				return false; // Let vanilla handle suffocation normally
////			} else {
////				return ModUtil.doesTerrainCauseSuffocation(reader, pos);
////			}
////		}
//		return false;
//	}

	/**
	 * Called from: ChunkRenderCache#<init> right after ChunkRenderCache#cacheStartPos is set
	 * Calls: ClientUtil.setupChunkRenderCache to set up the cache in an optimised way
	 */
	@OnlyIn(Dist.CLIENT)
	public static void initChunkRenderCache(final ChunkRenderCache _this, final int chunkStartX, final int chunkStartZ, final Chunk[][] chunks, final BlockPos start, final BlockPos end) {
		ClientUtil.setupChunkRenderCache(_this, chunkStartX, chunkStartZ, chunks, start, end);
	}

//	/**
//	 * Called from: ClientWorld#func_225319_b(BlockPos, BlockState, BlockState) (markForRerender)
//	 * Calls: WorldRenderer#markForRerender with a range of 2 instead of the normal 1
//	 * Replicates the behaviour of WorldRenderer#func_224746_a(BlockPos, BlockState, BlockState) (markForRerender)
//	 * and calls ModelManager#func_224742_a(BlockState, BlockState) (areUnequal)
//	 * This fixes seams that appear when meshes along chunk borders change
//	 */
//	@OnlyIn(Dist.CLIENT)
//	public static void markForRerender(final Minecraft minecraft, final WorldRenderer worldRenderer, final BlockPos pos, final BlockState oldState, final BlockState newState) {
//		if (minecraft.getModelManager().func_224742_a(oldState, newState)) {
//			final int posX = pos.getX();
//			final int posY = pos.getY();
//			final int posZ = pos.getZ();
//			final int maxX = posX + 2;
//			final int maxY = posY + 2;
//			final int maxZ = posZ + 2;
//			for (int z = posZ - 2; z <= maxZ; ++z) {
//				for (int y = posY - 2; y <= maxY; ++y) {
//					for (int x = posX - 2; x <= maxX; ++x) {
//						worldRenderer.markForRerender(x >> 4, y >> 4, z >> 4);
//					}
//				}
//			}
//		}
//	}

}
