package io.github.cadiboo.nocubes.hooks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender.RebuildTask;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeSpliterator;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

/**
 * @author Cadiboo
 */
@SuppressWarnings("unused") // Hooks are called with ASM
public final class Hooks {

	// region Rendering

	/**
	 * Called from: {@link RebuildTask#compile} right before the BlockPos.getAllInBoxMutable iteration
	 * Calls: {@link RendererDispatcher#renderChunk} to render our fluids and smooth terrain
	 */
	@OnlyIn(Dist.CLIENT)
	public static void preIteration(RebuildTask rebuildTask, ChunkRenderDispatcher.ChunkRender chunkRender, ChunkRenderDispatcher.CompiledChunk compiledChunkIn, RegionRenderCacheBuilder builderIn, BlockPos blockpos, IBlockDisplayReader chunkrendercache, MatrixStack matrixstack, Random random, BlockRendererDispatcher blockrendererdispatcher) {
		SelfCheck.preIteration = true;
		RendererDispatcher.renderChunk(rebuildTask, chunkRender, compiledChunkIn, builderIn, blockpos, chunkrendercache, matrixstack, random, blockrendererdispatcher);
	}

	/**
	 * Called from: {@link RebuildTask#compile} instead of {@link ChunkRenderCache#getFluidState(BlockPos)}
	 * Called from: {@link ChunkRenderCache#getFluidState(BlockPos)} instead of {@link BlockState#getFluidState()}
	 * <p>
	 * Hooking this allows us to control vanilla's fluids rendering which lets us cancel it and do our own rendering or
	 * change where fluids are rendered (to make extended fluids work).
	 */
	@OnlyIn(Dist.CLIENT)
	public static FluidState getRenderFluidState(BlockPos pos) {
		SelfCheck.getRenderFluidState = true;
		ClientWorld world = Minecraft.getInstance().level;
		if (world == null)
			return Fluids.EMPTY.defaultFluidState();
		// We hook this too, see 'getFluidStateOverride' below
 		return world.getFluidState(pos);
	}

	/**
	 * Called from: {@link RebuildTask#compile} right before {@link BlockState#getRenderShape()} is called
	 * <p>
	 * Hooking this disables vanilla rendering for smoothable BlockStates.
	 * Also disables vanilla's rendering for plans (grass, flowers) so that we can make
	 * them render at the proper height in the smooth ground
	 *
	 * @return If the state can render
	 */
	@OnlyIn(Dist.CLIENT)
	public static boolean canBlockStateRender(BlockState state) {
		SelfCheck.canBlockStateRender = true;
		return !NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state) && (!NoCubesConfig.Client.fixPlantHeight || !ModUtil.isShortPlant(state));
	}

	/**
	 * Called from: {@link BlockRendererDispatcher#renderBlockDamage} before any other logic
	 * Calls: {@link RendererDispatcher#renderBreakingTexture} if the blockstate is smoothable
	 * <p>
	 * Renders our own smoothed cracking/breaking/damage animation.
	 *
	 * @return If normal rendering should be cancelled (i.e. normal rendering should NOT happen)
	 */
	@OnlyIn(Dist.CLIENT)
	public static boolean renderBlockDamage(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData) {
		SelfCheck.renderBlockDamage = true;
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state))
			return false;
		RendererDispatcher.renderBreakingTexture(dispatcher, state, pos, world, matrix, buffer, modelData);
		return true;
	}

	/**
	 * Called from: {@link ClientWorld#setBlocksDirty(BlockPos, BlockState, BlockState)} before any other logic
	 * <p>
	 * The method 'setBlocksDirty' gets called when a block is updated and marked for re-render.
	 * Calls {@link WorldRenderer#setBlocksDirty(int, int, int, int, int, int)}  with a range of 2 instead of the normal 1.
	 * This fixes seams that appear when meshes along chunk borders change.
	 */
	@OnlyIn(Dist.CLIENT)
	public static void setBlocksDirty(Minecraft minecraft, WorldRenderer worldRenderer, BlockPos pos, BlockState oldState, BlockState newState) {
		SelfCheck.setBlocksDirty = true;
		if (minecraft.getModelManager().requiresRender(oldState, newState)) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			int extension = NoCubesConfig.Client.render ? 2 : 1;
			worldRenderer.setBlocksDirty(x - extension, y - extension, z - extension, x + extension, y + extension, z + extension);
		}
	}

	/**
	 * Called from: {@link BlockState#canOcclude()} before any other logic
	 * Called from: BlockState#isCacheOpaqueCube() (OptiFine) before any other logic
	 * <p>
	 * Hooking this makes {@link Block#shouldRenderFace(BlockState, IBlockReader, BlockPos, Direction)} return true and
	 * causes cubic terrain (including fluids) to be rendered when they are up against smooth terrain, stopping us from
	 * being able to see through the ground near smooth terrain.
	 *
	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
	 */
	public static @Nullable Boolean canOccludeOverride(BlockState state) {
		SelfCheck.canOccludeOverride = true;
		if (NoCubesConfig.Client.render && NoCubes.smoothableHandler.isSmoothable(state))
			return false;
		return null;
	}

	/**
	 * Called from: {@link BlockRendererDispatcher#BlockRendererDispatcher(BlockModelShapes, BlockColors)} before the FluidBlockRenderer is stored in the field
	 * <p>
	 * Hooking this lets us have extended fluids when OptiFine is installed.
	 *
	 * @return A fluid block renderer that works
	 */
	@OnlyIn(Dist.CLIENT)
	public static FluidBlockRenderer createFluidBlockRenderer(FluidBlockRenderer original) {
		SelfCheck.createFluidBlockRenderer = true;
		return new FluidBlockRenderer() {
			@Override
			public boolean tesselate(IBlockDisplayReader ignored, BlockPos posIn, IVertexBuilder vertexBuilderIn, FluidState fluidStateIn) {
				return super.tesselate(Minecraft.getInstance().level, posIn, vertexBuilderIn, fluidStateIn);
			}
		};
	}
	// endregion Rendering

	// region Collisions

	/**
	 * Called from: {@link VoxelShapes#collide} right before {@link BlockState#hasLargeCollisionShape()} is called
	 * Called from: {@link VoxelShapeSpliterator#tryAdvance} right before {@link BlockState#hasLargeCollisionShape()} is called
	 * <p>
	 * Hooking this disables vanilla collisions for smoothable BlockStates.
	 *
	 * @return If the state can be collided with
	 */
	@OnlyIn(Dist.CLIENT)
	public static boolean canBlockStateCollide(BlockState state) {
//		SelfCheck.canBlockStateCollide = true;
		return !NoCubesConfig.Server.collisionsEnabled || !NoCubes.smoothableHandler.isSmoothable(state);
	}

	/**
	 * Called from: {@link VoxelShapes#collide} right before {@link BlockState#hasLargeCollisionShape()} is called
	 * <p>
	 * Hooking this disables vanilla collisions for smoothable BlockStates.
	 *
	 * @return If the state can be collided with
	 */
	public static double collide(
		AxisAlignedBB aabb, IWorldReader world, double motion, ISelectionContext ctx,
		AxisRotation rotation, AxisRotation inverseRotation, BlockPos.Mutable pos,
		int minX, int maxX, int minY, int maxY, int minZ, int maxZ
	) {
		if (!NoCubesConfig.Server.collisionsEnabled)
			return motion;
		// NB: minZ and maxZ may be swapped depending on if the motion is positive or not
		return CollisionHandler.collideAxisInArea(
			aabb, world, motion, ctx,
			rotation, inverseRotation, pos,
			minX, maxX, minY, maxY, minZ, maxZ
		);
	}

	public static Deque<VoxelShape> createNoCubesCollisionList(ICollisionReader world, AxisAlignedBB area, BlockPos.Mutable pos) {
		Deque<VoxelShape> shapes = new ArrayDeque<>();
		int i = MathHelper.floor(area.minX - 1.0E-7D) - 1;
		int j = MathHelper.floor(area.maxX + 1.0E-7D) + 1;
		int k = MathHelper.floor(area.minY - 1.0E-7D) - 1;
		int l = MathHelper.floor(area.maxY + 1.0E-7D) + 1;
		int i1 = MathHelper.floor(area.minZ - 1.0E-7D) - 1;
		int j1 = MathHelper.floor(area.maxZ + 1.0E-7D) + 1;
		CollisionHandler.forEachCollisionRelativeToStart(world, pos, i, j, k, l, i1, j1, shape -> {
			shapes.add(shape.move(i, k, i1));
			return true;
		});
		return shapes;
	}

	/**
	 * Called from: {@link BlockState#isSuffocating(IBlockReader, BlockPos)} before any other logic
	 * <p>
	 * Hooking this stops grass path collisions being broken.
	 *
	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
	 */
	public static @Nullable Boolean isSuffocatingOverride(BlockState state, IBlockReader world, BlockPos pos) {
		SelfCheck.isSuffocatingOverride = true;
		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
			return false;
		return null;
	}

	// endregion Collisions

	/**
	 * Called from: World#getFluidState after the world bounds check in place of the normal getFluidState logic
	 *
	 * @return a fluid state that may not actually exist in the position
	 */
	public static @Nullable FluidState getFluidStateOverride(World world, BlockPos pos) {
		SelfCheck.getFluidStateOverride = true;
		if (NoCubesConfig.Server.extendFluidsRange <= 0)
			return null;
		return ModUtil.getExtendedFluidState(world, pos);
	}

//
//	/**
//	 * Called from: ChunkRenderCache#<init> right after ChunkRenderCache#cacheStartPos is set
//	 * Calls: ClientUtil.setupChunkRenderCache to set up the cache in an optimised way
//	 */
//	@OnlyIn(Dist.CLIENT)
//	public static void initChunkRenderCache(final ChunkRenderCache _this, final int chunkStartX, final int chunkStartZ, final Chunk[][] chunks, final BlockPos start, final BlockPos end) {
//		ClientUtil.setupChunkRenderCache(_this, chunkStartX, chunkStartZ, chunks, start, end);
//	}

	/**
	 * Load classes that we modify to get errors sooner.
	 */
	public static void loadClasses(Dist dist) {
		loadClass("net.minecraft.block.AbstractBlock$AbstractBlockState");
		loadClass("net.minecraft.block.BlockState");
		loadClass("net.minecraft.world.World");
		if (dist.isClient()) {
			loadClass("net.minecraft.client.renderer.BlockRendererDispatcher");
			loadClass("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender$RebuildTask");
			loadClass("net.minecraft.client.world.ClientWorld");
			loadClass("net.minecraft.client.renderer.chunk.ChunkRenderCache");
//		} else {

		}
	}

	private static void loadClass(String className) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
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
