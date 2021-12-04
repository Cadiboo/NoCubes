package io.github.cadiboo.nocubes.hooks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk.RebuildTask;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.AxisCycle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.CollisionSpliterator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.Stream;

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
	 * Called from: {@link RebuildTask#compile} instead of {@link RenderChunkRegion#getFluidState(BlockPos)}
	 * Called from: {@link RenderChunkRegion#getFluidState(BlockPos)} instead of {@link BlockState#getFluidState()}
	 * <p>
	 * Hooking this allows us to control vanilla's fluids rendering which lets us cancel it and do our own rendering or
	 * change where fluids are rendered (to make extended fluids work).
	 */
	@OnlyIn(Dist.CLIENT)
	public static FluidState getRenderFluidState(BlockPos pos) {
		SelfCheck.getRenderFluidState = true;
		ClientLevel world = Minecraft.getInstance().level;
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
		if (!NoCubesConfig.Client.render) return true;
		if (!NoCubes.smoothableHandler.isSmoothable(state)) {
			if (!NoCubesConfig.Client.fixPlantHeight) return true;
			if (!ModUtil.isShortPlant(state)) return true;
//			if (!Minecraft.getInstance().level.getBlockState(pos.down()).isSmoothable()) return true;
			return false;
		}
		return false;
	}

	/**
	 * Called from: {@link BlockRenderDispatcher#renderBreakingTexture} before any other logic
	 * Calls: {@link RendererDispatcher#renderBreakingTexture} if the blockstate is smoothable
	 * <p>
	 * Renders our own smoothed cracking/breaking/damage animation.
	 *
	 * @return If normal rendering should be cancelled (i.e. normal rendering should NOT happen)
	 */
	@OnlyIn(Dist.CLIENT)
	public static boolean renderBreakingTexture(BlockRenderDispatcher dispatcher, BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack matrix, VertexConsumer buffer, IModelData modelData) {
		SelfCheck.renderBreakingTexture = true;
		if (!NoCubesConfig.Client.render || !NoCubes.smoothableHandler.isSmoothable(state))
			return false;
		RendererDispatcher.renderBreakingTexture(dispatcher, state, pos, world, matrix, buffer, modelData);
		return true;
	}

	/**
	 * Called from: {@link ClientLevel#setBlocksDirty(BlockPos, BlockState, BlockState)} before any other logic
	 * <p>
	 * The method 'setBlocksDirty' gets called when a block is updated and marked for re-render.
	 * Calls {@link LevelRenderer#setBlocksDirty(int, int, int, int, int, int)}  with a range of 2 instead of the normal 1.
	 * This fixes seams that appear when meshes along chunk borders change.
	 */
	@OnlyIn(Dist.CLIENT)
	public static void setBlocksDirty(Minecraft minecraft, LevelRenderer worldRenderer, BlockPos pos, BlockState oldState, BlockState newState) {
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
	 * Hooking this makes {@link Block#shouldRenderFace} return true and
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
	 * Called from: {@link BlockRenderDispatcher#BlockRenderDispatcher(BlockModelShaper, BlockEntityWithoutLevelRenderer, BlockColors)} before the FluidBlockRenderer is stored in the field
	 * <p>
	 * Hooking this lets us have extended fluids when OptiFine is installed.
	 *
	 * @return A fluid block renderer that works
	 */
	@OnlyIn(Dist.CLIENT)
	public static LiquidBlockRenderer createFluidBlockRenderer(LiquidBlockRenderer original) {
		SelfCheck.createFluidBlockRenderer = true;
		return new LiquidBlockRenderer() {
			@Override
			public boolean tesselate(BlockAndTintGetter ignored, BlockPos posIn, VertexConsumer vertexBuilderIn, FluidState fluidStateIn) {
				return super.tesselate(Minecraft.getInstance().level, posIn, vertexBuilderIn, fluidStateIn);
			}
		};
	}
	// endregion Rendering

	// region Indev-Collisions

	/**
	 * Called from: {@link Shapes#collide(AABB, LevelReader, double, CollisionContext, AxisCycle, Stream)} right before {@link BlockState#hasLargeCollisionShape()} is called
	 * Called from: {@link CollisionSpliterator#tryAdvance} right before {@link BlockState#hasLargeCollisionShape()} is called
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
	 * Called from: {@link Shapes#collide(AABB, LevelReader, double, CollisionContext, AxisCycle, Stream)} right before the first invocation of {@link Shapes#lastC}
	 */
	public static double collide(
		AABB aabb, LevelReader world, double motion, CollisionContext ctx,
		AxisCycle rotation, AxisCycle inverseRotation, BlockPos.MutableBlockPos pos,
		int minX, int maxX, int minY, int maxY, int minZ, int maxZ
	) {
//		SelfCheck.collide = true;
		if (!NoCubesConfig.Server.collisionsEnabled)
			return motion;
		// NB: minZ and maxZ may be swapped depending on if the motion is positive or not
		return CollisionHandler.collideAxisInArea(
			aabb, world, motion, ctx,
			rotation, inverseRotation, pos,
			minX, maxX, minY, maxY, minZ, maxZ
		);
	}

	// endregion Indev-Collisions

	// region Collisions
//
//	/**
//	 * Called from: {@link BlockState#getCollisionShape(BlockGetter, BlockPos)}} before any other logic
//	 * <p>
//	 * Hooking this makes that collisions work for blockstates with a cache.
//	 *
//	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
//	 */
//	public static @Nullable VoxelShape getCollisionShapeOverride(BlockState state, BlockGetter world, BlockPos pos) {
//		SelfCheck.getCollisionShapeNoContextOverride = true;
//		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
//			return CollisionHandler.getCollisionShape(state, world, pos, CollisionContext.empty());
//		return null;
//	}
//
//	/**
//	 * Called from: {@link BlockState#getCollisionShape(BlockGetter, BlockPos, CollisionContext)}} before any other logic
//	 * <p>
//	 * Hooking this makes collisions work.
//	 *
//	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
//	 */
//	public static @Nullable VoxelShape getCollisionShapeOverride(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
//		SelfCheck.getCollisionShapeWithContextOverride = true;
//		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
//			return CollisionHandler.getCollisionShape(state, world, pos, context);
//		return null;
//	}
//
//	/**
//	 * Called from: {@link BlockState#isCollisionShapeFullBlock(BlockGetter, BlockPos)}} before any other logic
//	 * <p>
//	 * Hooking this makes collisions work for normally solid blocks like stone.
//	 * <p>
//	 * TODO: This is used by {@link Block#getShadeBrightness(BlockState, BlockGetter, BlockPos)} so always returning false breaks AO when collisions are on.
//	 * Possible fix: Check if we are on the server or the client thread before running the check?
//	 *
//	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
//	 */
//	public static @Nullable Boolean isCollisionShapeFullBlockOverride(BlockState state, BlockGetter reader, BlockPos pos) {
//		SelfCheck.isCollisionShapeFullBlockOverride = true;
//		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
//			return false;
//		return null;
//	}
//
//	/**
//	 * Called from: {@link BlockState#hasLargeCollisionShape()} before any other logic
//	 * <p>
//	 * Hooking this somehow stops us falling through 1 block wide holes and under the ground.
//	 *
//	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
//	 */
//	public static @Nullable Boolean hasLargeCollisionShapeOverride(BlockState state) {
//		SelfCheck.hasLargeCollisionShapeOverride = true;
//		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
//			return true;
//		return null;
//	}
//
//	/**
//	 * Called from: {@link BlockState#isSuffocating(BlockGetter, BlockPos)} before any other logic
//	 * <p>
//	 * Hooking this stops grass path collisions being broken.
//	 *
//	 * @return A value to override vanilla's handing or <code>null</code> to use vanilla's handing
//	 */
//	public static @Nullable Boolean isSuffocatingOverride(BlockState state, BlockGetter world, BlockPos pos) {
//		SelfCheck.isSuffocatingOverride = true;
//		if (NoCubesConfig.Server.collisionsEnabled && NoCubes.smoothableHandler.isSmoothable(state))
//			return false;
//		return null;
//	}
//
//	// endregion Collisions

	/**
	 * Called from: World#getFluidState after the world bounds check in place of the normal getFluidState logic
	 *
	 * @return a fluid state that may not actually exist in the position
	 */
	public static @Nullable FluidState getFluidStateOverride(Level world, BlockPos pos) {
		SelfCheck.getFluidStateOverride = true;
		if (NoCubesConfig.Server.extendFluidsRange <= 0)
			return null;
		return ModUtil.getExtendedFluidState(world, pos);
	}

	/**
	 * Called from: The end of {@link NetworkHooks#handleClientLoginSuccess(Connection)}
	 *
	 * Hooking this lets NoCubes load properly on modded servers that don't have it installed
	 */
	public static void afterClientLoginSuccess(Connection manager) {
		SelfCheck.afterClientLoginSuccess = true;
		if (NetworkHooks.isVanillaConnection(manager))
			return; // Forge loads the default server configs for us
		var logger = LogManager.getLogger("NoCubes Client-only features");
		logger.info("Connected to a modded server that doesn't have NoCubes installed, loading default server config");
		NoCubesConfig.Hacks.loadDefaultServerConfig();
		logger.debug("Done loading default server config");
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
		loadClass("net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase");
		loadClass("net.minecraft.world.level.block.state.BlockState");
		loadClass("net.minecraft.world.level.Level");
		loadClass("net.minecraftforge.fmllegacy.network.NetworkHooks");
		if (dist.isClient()) {
			loadClass("net.minecraft.client.renderer.block.BlockRenderDispatcher");
			loadClass("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk$RebuildTask");
			loadClass("net.minecraft.client.multiplayer.ClientLevel");
			loadClass("net.minecraft.client.renderer.chunk.RenderChunkRegion");
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
