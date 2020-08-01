package io.github.cadiboo.nocubes.hooks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.MeshRenderer;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender.RebuildTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

import java.util.Random;

/**
 * @author Cadiboo
 */
@SuppressWarnings("unused") // Hooks are called with ASM
public final class Hooks {

	/**
	 * Called from: {@link RebuildTask#compile} right before the BlockPos.getAllInBoxMutable iteration
//	 * Calls: RenderDispatcher.renderChunk to render all our fluids and smooth terrain
	 * Calls: MeshRenderer.renderChunk to render smooth terrain
	 */
	@OnlyIn(Dist.CLIENT)
	public static void preIteration(RebuildTask rebuildTask, ChunkRenderDispatcher.ChunkRender chunkRender, ChunkRenderDispatcher.CompiledChunk compiledChunkIn, RegionRenderCacheBuilder builderIn, BlockPos blockpos, ChunkRenderCache chunkrendercache, MatrixStack matrixstack, Random random, BlockRendererDispatcher blockrendererdispatcher) {
		MeshRenderer.renderChunk(rebuildTask, chunkRender, compiledChunkIn, builderIn, blockpos, chunkrendercache, matrixstack, random, blockrendererdispatcher);
	}

	/**
	 * Called from: {@link RebuildTask#compile} right before BlockState#getRenderType is called
	 * Calls: Nothing
	 * Disables vanilla rendering for smoothable BlockStates
	 *
	 * @return If the state can render
	 */
	@OnlyIn(Dist.CLIENT)
	public static boolean canBlockStateRender(BlockState blockstate) {
		return !NoCubes.smoothableHandler.isSmoothable(blockstate) || !NoCubesConfig.Client.render;
//		if (blockstate.nocubes_isTerrainSmoothable && Config.renderSmoothTerrain) return false;
//		if (blockstate.nocubes_isLeavesSmoothable) {
//			if (Config.renderSmoothLeaves)
//				return Config.renderSmoothAndVanillaLeaves;
//			return true;
//		}
//		return true;
	}

	/**
	 * Called from: {@link BlockRendererDispatcher#renderBlockDamage(BlockState, BlockPos, IBlockDisplayReader, MatrixStack, IVertexBuilder, IModelData)} before any other logic
	 * Calls: RenderDispatcher.renderSmoothBlockDamage if the blockstate is smoothable
	 *
	 * @return If normal rendering should be cancelled (i.e. normal rendering should NOT happen)
	 */
	@OnlyIn(Dist.CLIENT)
	public static boolean renderBlockDamage(BlockRendererDispatcher blockRendererDispatcher, BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, IModelData modelData) {
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(blockStateIn))
			return false;
		MeshRenderer.renderBlockDamage(blockRendererDispatcher, blockStateIn, posIn, lightReaderIn, matrixStackIn, vertexBuilderIn, modelData);
		return true;
	}

	/**
	 * Load classes that we modify to get errors sooner.
	 */
	public static void loadClasses(final Dist dist) {
		loadClass("net.minecraft.block.AbstractBlock$AbstractBlockState");
		loadClass("net.minecraft.block.BlockState");
		if (dist.isClient()) {
			loadClass("net.minecraft.client.renderer.BlockRendererDispatcher");
			loadClass("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender$RebuildTask");
//		} else {

		}
	}

	private static void loadClass(final String className) {
		final ClassLoader loader = Thread.currentThread().getContextClassLoader();
		try {
			Class.forName(className, false, loader);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Failed to load class \"" + className + "\", probably a coremod issue", e);
		}
		try {
			Class.forName(className, true, loader);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Failed to initialise class \"" + className + "\", probably a coremod issue", e);
		}
	}

//	/**
//	 * Called from: World#getFluidState after the bounds check in place of the normal getFluidState logic
//	 * Calls: ModUtil.getFluidState to handle extended fluids
//	 *
//	 * @return An IFluidState
//	 */
//	public static IFluidState getFluidState(final World world, final BlockPos pos) {
//		return ModUtil.getFluidState(world, pos);
//	}
//
//	/**
//	 * Called from: IWorldReader#getCollisionShapes(Entity, AxisAlignedBB) after the ISelectionContext is generated
//	 * Calls: CollisionHandler.getCollisionShapes to handle mesh, repose and vanilla collisions
//	 *
//	 * @return The collisions for the entity
//	 */
//	public static Stream<VoxelShape> getCollisionShapes(final IWorldReader _this, final Entity p_217352_1_, final AxisAlignedBB p_217352_2_, final int i, final int j, final int k, final int l, final int i1, final int j1, final ISelectionContext iselectioncontext) {
//		return CollisionHandler.getCollisionShapes(_this, p_217352_1_, p_217352_2_, i, j, k, l, i1, j1, iselectioncontext);
//	}
//
//
//	/**
//	 * Called from: BlockState#causesSuffocation before any other logic
//	 * Calls: ModUtil.doesTerrainCauseSuffocation
//	 *
//	 * @return If the state does NOT cause suffocation (If normal suffocation checks should be bypassed and false returned)
//	 */
//	public static boolean doesNotCauseSuffocation(final BlockState blockState, final IBlockReader reader, final BlockPos pos) {
//		if (Config.terrainCollisions) {
//			if (!blockState.nocubes_isTerrainSmoothable) {
//				return false; // Let vanilla handle suffocation normally
//			} else {
//				return ModUtil.doesTerrainCauseSuffocation(reader, pos);
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * Called from: ChunkRenderCache#<init> right after ChunkRenderCache#cacheStartPos is set
//	 * Calls: ClientUtil.setupChunkRenderCache to set up the cache in an optimised way
//	 */
//	@OnlyIn(Dist.CLIENT)
//	public static void initChunkRenderCache(final ChunkRenderCache _this, final int chunkStartX, final int chunkStartZ, final Chunk[][] chunks, final BlockPos start, final BlockPos end) {
//		ClientUtil.setupChunkRenderCache(_this, chunkStartX, chunkStartZ, chunks, start, end);
//	}
//
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
//
//	/**
//	 * Called from: VoxelShapes.getAllowedOffset(AxisAlignedBB, IWorldReader, double, ISelectionContext, AxisRotation, Stream) before the MutableBlockPos is created
//	 * Calls: VoxelShapesHandler.getAllowedOffset to handle mesh, repose and vanilla collisions offsets
//	 */
//	public static double getAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, final double desiredOffset, final ISelectionContext selectionContext, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Direction.Axis rotX, final Direction.Axis rotY, final Direction.Axis rotZ) {
//		return VoxelShapesHandler.getAllowedOffset(collisionBox, worldReader, desiredOffset, selectionContext, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
//	}

}
