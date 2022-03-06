package io.github.cadiboo.nocubes.hooks;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
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
	public static void preIteration(RebuildTask rebuildTask, RenderChunk chunkRender, ChunkRenderDispatcher.CompiledChunk compiledChunkIn, ChunkBufferBuilderPack builderIn, BlockPos blockpos, BlockAndTintGetter chunkrendercache, PoseStack matrixstack, Random random, BlockRenderDispatcher blockrendererdispatcher) {
		SelfCheck.preIteration = true;
		RendererDispatcher.renderChunk(rebuildTask, chunkRender, compiledChunkIn, builderIn, blockpos, chunkrendercache, matrixstack, random, blockrendererdispatcher);
	}

	/**
	 * Called from: {@link RebuildTask#compile} instead of {@link BlockState#getFluidState()} when OptiFine is present
	 * <p>
	 * Hooking this makes extended fluids render properly
	 */
	@OnlyIn(Dist.CLIENT)
	public static FluidState getRenderFluidStateOptiFine(BlockPos pos, BlockState state) {
		SelfCheck.getRenderFluidState = true;
		if (NoCubesConfig.Server.extendFluidsRange > 0)
			return ClientUtil.getExtendedFluidState(pos);
		return state.getFluidState();
	}

	/**
	 * Called from: {@link BlockState#canOcclude()} before any other logic
	 * Called from: BlockState#isCacheOpaqueCube() (OptiFine) before any other logic
	 * <p>
	 * Hooking this makes {@link Block#shouldRenderFace} return true and
	 * causes cubic terrain (including fluids) to be rendered when they are up against smooth terrain, stopping us from
	 * being able to see through the ground near smooth terrain.
	 */
	public static boolean shouldCancelOcclusion(BlockBehaviour.BlockStateBase state) {
		return NoCubesConfig.Client.render && NoCubes.smoothableHandler.isSmoothable(state);
	}
	// endregion Rendering

	// region Indev-Collisions
//
//	/**
//	 * Called from: {@link Shapes#collide(AABB, LevelReader, double, CollisionContext, AxisCycle, Stream)} right before {@link BlockState#hasLargeCollisionShape()} is called
//	 * Called from: {@link CollisionSpliterator#tryAdvance} right before {@link BlockState#hasLargeCollisionShape()} is called
//	 * <p>
//	 * Hooking this disables vanilla collisions for smoothable BlockStates.
//	 *
//	 * @return If the state can be collided with
//	 */
//	@OnlyIn(Dist.CLIENT)
//	public static boolean canBlockStateCollide(BlockState state) {
////		SelfCheck.canBlockStateCollide = true;
//		return !NoCubesConfig.Server.collisionsEnabled || !NoCubes.smoothableHandler.isSmoothable(state);
//	}
//
//	/**
//	 * Called from: {@link Shapes#collide(AABB, LevelReader, double, CollisionContext, AxisCycle, Stream)} right before the first invocation of {@link Shapes#lastC}
//	 * <p>
//	 * Hooking this disables vanilla collisions for smoothable BlockStates.
//	 *
//	 * @return If the state can be collided with
//	 */
//	public static double collide(
//		AABB aabb, LevelReader world, double motion, CollisionContext ctx,
//		AxisCycle rotation, AxisCycle inverseRotation, BlockPos.MutableBlockPos pos,
//		int minX, int maxX, int minY, int maxY, int minZ, int maxZ
//	) {
////		SelfCheck.collide = true;
//		if (!NoCubesConfig.Server.collisionsEnabled)
//			return motion;
//		// NB: minZ and maxZ may be swapped depending on if the motion is positive or not
//		return CollisionHandler.collideAxisInArea(
//			aabb, world, motion, ctx,
//			rotation, inverseRotation, pos,
//			minX, maxX, minY, maxY, minZ, maxZ
//		);
//	}

	// endregion Indev-Collisions

	// region Collisions

	/**
	 * Called from: {@link BlockState#getCollisionShape(BlockGetter, BlockPos)}} before any other logic
	 * <p>
	 * Hooking this makes that collisions work for blockstates with a cache.
	 *
	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
	 */
	public static @Nullable VoxelShape getCollisionShapeOverride(BlockState state, BlockGetter world, BlockPos pos) {
		SelfCheck.getCollisionShapeNoContextOverride = true;
		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
			return CollisionHandler.getCollisionShape(state, world, pos, CollisionContext.empty());
		return null;
	}

	/**
	 * Called from: {@link BlockState#getCollisionShape(BlockGetter, BlockPos, CollisionContext)}} before any other logic
	 * <p>
	 * Hooking this makes collisions work.
	 *
	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
	 */
	public static @Nullable VoxelShape getCollisionShapeOverride(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		SelfCheck.getCollisionShapeWithContextOverride = true;
		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
			return CollisionHandler.getCollisionShape(state, world, pos, context);
		return null;
	}

	/**
	 * Called from: {@link BlockState#isCollisionShapeFullBlock(BlockGetter, BlockPos)}} before any other logic
	 * <p>
	 * Hooking this makes collisions work for normally solid blocks like stone.
	 * <p>
	 * TODO: This is used by {@link Block#getShadeBrightness(BlockState, BlockGetter, BlockPos)} so always returning false breaks AO when collisions are on.
	 * Possible fix: Check if we are on the server or the client thread before running the check?
	 *
	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
	 */
	public static @Nullable Boolean isCollisionShapeFullBlockOverride(BlockState state, BlockGetter reader, BlockPos pos) {
		SelfCheck.isCollisionShapeFullBlockOverride = true;
		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
			return false;
		return null;
	}

	/**
	 * Called from: {@link BlockState#hasLargeCollisionShape()} before any other logic
	 * <p>
	 * Hooking this somehow stops us falling through 1 block wide holes and under the ground.
	 *
	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
	 */
	public static @Nullable Boolean hasLargeCollisionShapeOverride(BlockState state) {
		SelfCheck.hasLargeCollisionShapeOverride = true;
		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
			return true;
		return null;
	}

	/**
	 * Called from: {@link BlockState#isSuffocating(BlockGetter, BlockPos)} before any other logic
	 * <p>
	 * Hooking this stops grass path collisions being broken.
	 *
	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
	 */
	public static @Nullable Boolean isSuffocatingOverride(BlockState state, BlockGetter world, BlockPos pos) {
		SelfCheck.isSuffocatingOverride = true;
		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
			return false;
		return null;
	}

	// endregion Collisions

}
