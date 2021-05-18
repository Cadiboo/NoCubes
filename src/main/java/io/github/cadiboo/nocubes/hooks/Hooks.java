package io.github.cadiboo.nocubes.hooks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.MeshRenderer;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender.RebuildTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
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
	 * Calls: RenderDispatcher.renderChunk to render our ~~fluids and~~ smooth terrain
	 * Calls: MeshRenderer.renderChunk to render smooth terrain
	 */
	@OnlyIn(Dist.CLIENT)
	public static void preIteration(RebuildTask rebuildTask, ChunkRenderDispatcher.ChunkRender chunkRender, ChunkRenderDispatcher.CompiledChunk compiledChunkIn, RegionRenderCacheBuilder builderIn, BlockPos blockpos, IBlockDisplayReader chunkrendercache, MatrixStack matrixstack, Random random, BlockRendererDispatcher blockrendererdispatcher) {
		SelfCheck.preIteration = true;
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
	public static boolean canBlockStateRender(BlockState state) {
		SelfCheck.canBlockStateRender = true;
		return !NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state);
	}

	/**
	 * Called from: {@link BlockRendererDispatcher#renderBlockDamage(BlockState, BlockPos, IBlockDisplayReader, MatrixStack, IVertexBuilder, IModelData)} before any other logic
	 * Calls: RenderDispatcher.renderSmoothBlockDamage if the blockstate is smoothable
	 *
	 * @return If normal rendering should be cancelled (i.e. normal rendering should NOT happen)
	 */
	@OnlyIn(Dist.CLIENT)
	public static boolean renderBlockDamage(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData) {
		SelfCheck.renderBlockDamage = true;
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state))
			return false;
		MeshRenderer.renderSmoothBlockDamage(dispatcher, state, pos, world, matrix, buffer, modelData);
		return true;
	}

	/**
	 * Hook this so that collisions work for blockstates with a cache.
	 */
	public static VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos) {
		SelfCheck.getCollisionShapeNoContext = true;
//		return _this.cache != null ? _this.cache.collisionShape : _this.getCollisionShape(worldIn, pos, ISelectionContext.dummy());
		if (state.cache != null && !NoCubes.smoothableHandler.isSmoothable(state))
			return state.cache.collisionShape;
		return getCollisionShape(state, world, pos, ISelectionContext.empty());
	}

	/**
	 * Hook this so collisions work.
	 */
	public static VoxelShape getCollisionShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		SelfCheck.getCollisionShapeWithContext = true;
		if (NoCubes.smoothableHandler.isSmoothable(state))
			return CollisionHandler.getCollisionShape(state.getBlock().hasCollision, state, world, pos, context);

//		return _this.getBlock().getCollisionShape(_this.getSelf(), worldIn, pos, context);
		return state.getBlock().getCollisionShape(state, world, pos, context);
	}

	/**
	 * Hook this so that collisions work for normally solid blocks like stone.
	 * TODO: This is used by {@link Block#getShadeBrightness(BlockState, IBlockReader, BlockPos)} so always returning false breaks AO when collisions are on.
	 *  Possible fix: Check if we are on the server or the client thread before running the check?
	 * TODO: Clean this hook up, make it more like hasLargeCollisionShape (return true for vanilla handling, false for false)
	 */
	public static boolean isCollisionShapeFullBlock(BlockState state, IBlockReader reader, BlockPos pos) {
		SelfCheck.isCollisionShapeFullBlock = true;
//		return _this.cache != null ? _this.cache.opaqueCollisionShape : Block.isOpaque(_this.getCollisionShape(reader, pos));
		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
			return false;
		if (state.cache != null)
			return state.cache.isCollisionShapeFullBlock;
		return Block.isShapeFullBlock(state.getCollisionShape(reader, pos));
	}

	/**
	 * Somehow stops us falling through 1 block wide holes and under the ground.
	 *
	 * @return true for vanilla handling, false for the block to be marked as having a large collision shape.
	 */
	public static boolean hasLargeCollisionShape(BlockState state) {
		SelfCheck.hasLargeCollisionShape = true;
		return !NoCubesConfig.Server.collisionsEnabled || !NoCubes.smoothableHandler.isSmoothable(state);
	}

	/**
	 * Fixes being able to see through the ground near smooth terrain.
	 *
	 * @return true for vanilla handling, false for the block not being able to occlude.
	 */
	public static boolean canOcclude(BlockState state) {
		SelfCheck.canOcclude = true;
		return !NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state);
	}

	/**
	 * Called from: ClientWorld#func_225319_b(BlockPos, BlockState, BlockState) (markForRerender, setBlocksDirty)
	 * Calls: WorldRenderer#markForRerender with a range of 2 instead of the normal 1
	 * Replicates the behaviour of WorldRenderer#func_224746_a(BlockPos, BlockState, BlockState) (markForRerender, setBlocksDirty)
	 * and calls ModelManager#func_224742_a(BlockState, BlockState) (needsRenderUpdate, requiresRender)
	 * This fixes seams that appear when meshes along chunk borders change.
	 */
	@OnlyIn(Dist.CLIENT)
	public static void markForRerender(Minecraft minecraft, WorldRenderer worldRenderer, BlockPos pos, BlockState oldState, BlockState newState) {
		SelfCheck.markForRerender = true;
		if (minecraft.getModelManager().requiresRender(oldState, newState)) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			int extension = NoCubesConfig.Client.render ? 2 : 1;
			worldRenderer.setBlocksDirty(x - extension, y - extension, z - extension, x + extension, y + extension, z + extension);
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
//	 * Called from: VoxelShapes.getAllowedOffset(AxisAlignedBB, IWorldReader, double, ISelectionContext, AxisRotation, Stream) before the MutableBlockPos is created
//	 * Calls: VoxelShapesHandler.getAllowedOffset to handle mesh, repose and vanilla collisions offsets
//	 */
//	public static double getAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, final double desiredOffset, final ISelectionContext selectionContext, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Direction.Axis rotX, final Direction.Axis rotY, final Direction.Axis rotZ) {
//		return VoxelShapesHandler.getAllowedOffset(collisionBox, worldReader, desiredOffset, selectionContext, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
//	}

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

}
