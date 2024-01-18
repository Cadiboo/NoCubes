package io.github.cadiboo.nocubes.hooks;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import io.github.cadiboo.nocubes.client.render.SodiumRenderer;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildContext;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.data.BuiltSectionInfo;
import me.jellysquid.mods.sodium.client.util.task.CancellationToken;
import me.jellysquid.mods.sodium.client.world.WorldSlice;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Set;

import static net.minecraft.world.level.block.state.BlockBehaviour.BlockStateBase;

/**
 * @author Cadiboo
 */
@SuppressWarnings("unused") // Hooks are called with ASM
public final class Hooks {

	public static boolean renderingEnabledFor(BlockStateBase state) {
		return NoCubesConfig.Client.render && NoCubes.smoothableHandler.isSmoothable(state);
	}

	public static boolean collisionsEnabledFor(BlockStateBase state) {
		return NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state);
	}

	// region Rendering

	/**
	 * Called from: {@link RebuildTask#compile} right before the BlockPos.getAllInBoxMutable iteration
	 * Calls: {@link RendererDispatcher#renderChunk} to render our fluids and smooth terrain
	 */
	@OnlyIn(Dist.CLIENT)
	public static void preIteration(
		// Fields (this, RenderChunk.this)
		RebuildTask rebuildTask, RenderChunk chunkRender,
		// Params (p_234471_)
		ChunkBufferBuilderPack buffers,
		// Local variables
		BlockPos chunkPos, BlockAndTintGetter world, PoseStack matrix,
		// Scoped local variables
		Set<RenderType> usedLayers, RandomSource random, BlockRenderDispatcher dispatcher
	) {
		SelfCheck.preIteration = true;
		RendererDispatcher.renderChunk(
			rebuildTask, chunkRender, buffers,
			chunkPos, world, matrix,
			usedLayers, random, dispatcher
		);
	}

	/**
	 * Same as {@link Hooks#preIteration} but for Sodium.
	 */
	@OnlyIn(Dist.CLIENT)
	public static void preIterationSodium(
		// Params
		/*ChunkBuildContext*/ Object buildContext, /*CancellationToken*/ Object cancellationToken,
		// Local variables
		/*BuiltSectionInfo.Builder*/ Object renderData,
		VisGraph occluder,
		/*ChunkBuildBuffers*/ Object buffers,
		/*BlockRenderCache*/ Object cache,
		/*WorldSlice*/ Object slice,
		int minX, int minY, int minZ,
		int maxX, int maxY, int maxZ,
		BlockPos.MutableBlockPos blockPos,
		BlockPos.MutableBlockPos modelOffset,
		/*BlockRenderContext*/ Object context
	) {
		SelfCheck.preIterationSodium = true;
		SodiumRenderer.renderChunk(
			// Params
			(ChunkBuildContext) buildContext, (CancellationToken) cancellationToken,
			// Local variables
			(BuiltSectionInfo.Builder) renderData,
			occluder,
			(ChunkBuildBuffers) buffers,
			(BlockRenderCache) cache,
			(WorldSlice) slice,
			minX, minY, minZ,
			maxX, maxY, maxZ,
			blockPos,
			modelOffset,
			(BlockRenderContext) context
		);
	}

	/**
	 * Called from: {@link RebuildTask#compile} and {@link LiquidBlockRenderer#tesselate} instead of {@link BlockState#getFluidState()}
	 * <p>
	 * Hooking this makes extended fluids render properly
	 */
	@OnlyIn(Dist.CLIENT)
	public static FluidState getRenderFluidState(BlockPos pos, BlockState state) {
		SelfCheck.getRenderFluidState = true;
		if (NoCubesConfig.Server.extendFluidsRange > 0)
			return ClientUtil.getExtendedFluidState(pos);
		return state.getFluidState();
	}

	/**
	 * Disables vanilla rendering for smoothable BlockStates.
	 * Also disables vanilla's rendering for plans (grass, flowers) so that
	 * we can make them render at the proper height in the smooth ground
	 */
	public static boolean allowVanillaRenderingFor(BlockState state) {
		if (!NoCubesConfig.Client.render)
			return true;

		if (NoCubes.smoothableHandler.isSmoothable(state))
			return false; // A smooth block, we'll render this in MeshRenderer
		if (NoCubesConfig.Client.fixPlantHeight && ModUtil.isShortPlant(state))
			return false; // We render plants ourselves in MeshRenderer in this case

		return true; // A non-smooth block we don't care about
	}

	/**
	 * Hooking this makes {@link Block#shouldRenderFace} return true and causes cubic terrain (including fluids) to be
	 * rendered when they are up against smooth terrain, stopping us from being able to see through the ground near
	 * smooth terrain.
	 */
	public static boolean shouldCancelOcclusion(BlockStateBase state) {
		return renderingEnabledFor(state);
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

	/**
	 * Helper function for use by other hooks/mixins.
	 */
	public static VoxelShape getSmoothCollisionShapeFor(Entity entity, BlockState state, BlockGetter world, BlockPos pos) {
		assert collisionsEnabledFor(state);
		return CollisionHandler.getCollisionShape(state, world, pos, CollisionContext.of(entity));
	}

	/**
	 * Helper function for use by other hooks/mixins.
	 */
	public static boolean collisionShapeOfSmoothBlockIntersectsEntityAABB(Entity entity, BlockState state, BlockGetter level, BlockPos pos) {
		assert collisionsEnabledFor(state);
		return Shapes.joinIsNotEmpty(
			getSmoothCollisionShapeFor(entity, state, level, pos).move(pos.getX(), pos.getY(), pos.getZ()),
			Shapes.create(entity.getBoundingBox()),
			BooleanOp.AND
		);
	}

	/**
	 * Helper function for use by other hooks/mixins.
	 *
	 * @see io.github.cadiboo.nocubes.mixin.RenderChunkRebuildTaskMixin#nocubes_getRenderShape
	 */
	public static RenderShape getRenderShape(BlockState state) {
		// Invisible blocks are not rendered by vanilla
		return Hooks.allowVanillaRenderingFor(state) ? state.getRenderShape() : RenderShape.INVISIBLE;
	}

	/**
	 * When a block is updated and marked for re-render, the renderer is told to rebuild everything inside a 'dirty' area.
	 * Extending the size of the area that gets updated fixes seams that appear when meshes along chunk borders change.
	 */
	public static int expandDirtyRenderAreaExtension(int originalDirtyAreaExtension) {
		// Math.max so if someone else also modifies the value (e.g. to 3) we don't overwrite their extension
		return NoCubesConfig.Client.render ? Math.max(2, originalDirtyAreaExtension) : originalDirtyAreaExtension;
	}

}
