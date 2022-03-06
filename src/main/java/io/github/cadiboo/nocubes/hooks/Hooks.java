package io.github.cadiboo.nocubes.hooks;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Random;

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

}
