package io.github.cadiboo.renderchunkrebuildchunkhooks.hooks;//package io.github.cadiboo.renderchunkrebuildchunkhooks.hooks;
//
//import io.github.cadiboo.renderchunkrebuildchunkhooks.config.RenderChunkRebuildChunkHooksConfig;
//import io.github.cadiboo.renderchunkrebuildchunkhooks.event.optifine.RebuildChunkBlockOptifineEvent;
//import io.github.cadiboo.renderchunkrebuildchunkhooks.event.optifine.RebuildChunkBlockRenderInLayerOptifineEvent;
//import io.github.cadiboo.renderchunkrebuildchunkhooks.event.optifine.RebuildChunkBlockRenderTypeAllowsRenderOptifineEvent;
//import io.github.cadiboo.renderchunkrebuildchunkhooks.event.optifine.RebuildChunkPostOptifineEvent;
//import io.github.cadiboo.renderchunkrebuildchunkhooks.event.optifine.RebuildChunkPreOptifineEvent;
//import net.minecraft.block.Block;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.client.renderer.BlockRendererDispatcher;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.RenderGlobal;
//import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
//import net.minecraft.client.renderer.chunk.CompiledChunk;
//import net.minecraft.client.renderer.chunk.RenderChunk;
//import net.minecraft.client.renderer.chunk.VisGraph;
//import net.minecraft.crash.CrashReport;
//import net.minecraft.tileentity.TileEntity;
//import net.minecraft.util.BlockRenderLayer;
//import net.minecraft.util.EnumBlockRenderType;
//import net.minecraft.util.ReportedException;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.ChunkCache;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.eventhandler.Event;
//import net.minecraftforge.fml.relauncher.ReflectionHelper;
//import net.optifine.BlockPosM;
//import net.optifine.override.ChunkCacheOF;
//
//import javax.annotation.Nonnull;
//import java.lang.reflect.Field;
//import java.util.HashSet;
//
///**
// * @author Cadiboo
// */
//public final class RenderChunkRebuildChunkHooksHooksOptifine {
//
//	private static final Field chunkCacheOF_chunkCache;
//	static {
//		try {
//			//grr, backwards compatibility
//			chunkCacheOF_chunkCache = ReflectionHelper.findField(ChunkCacheOF.class, "chunkCache");
////			chunkCacheOF_chunkCache = ObfuscationReflectionHelper.findField(ChunkCacheOF.class, "chunkCache");
//		} catch (final ReflectionHelper.UnableToFindMethodException e) {
//			final CrashReport crashReport = new CrashReport("Error getting Field for ChunkCacheOF#chunkCache!", e);
//			crashReport.makeCategory("Reflectively Accessing ChunkCacheOF#chunkCache");
//			throw new ReportedException(crashReport);
//		}
//	}
//
//	public static ChunkCache getChunkCacheFromChunkCacheOF(ChunkCacheOF chunkCacheOF) {
//		try {
//			return (ChunkCache) chunkCacheOF_chunkCache.get(chunkCacheOF);
//		} catch (IllegalAccessException e) {
//			final CrashReport crashReport = new CrashReport("Unable get ChunkCacheOF#chunkCache", e);
//			crashReport.makeCategory("Reflectively accessing ChunkCacheOF#chunkCache");
//			throw new ReportedException(crashReport);
//		}
//	}
//
//	/**
//	 * @param renderChunk   the instance of {@link RenderChunk}
//	 * @param x             the translation X passed in from RenderChunk#rebuildChunk
//	 * @param y             the translation Y passed in from RenderChunk#rebuildChunk
//	 * @param z             the translation Z passed in from RenderChunk#rebuildChunk
//	 * @param generator     the {@link ChunkCompileTaskGenerator} passed in from RenderChunk#rebuildChunk
//	 * @param compiledchunk the {@link CompiledChunk} passed in from RenderChunk#rebuildChunk
//	 * @param blockpos      the {@link BlockPos position} stored in a local variable and passed in from RenderChunk#rebuildChunk
//	 * @param blockAccess   the {@link ChunkCacheOF} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_9_1_      the {@link VisGraph} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_10_1_     the {@link HashSet} of {@link TileEntity TileEntities} with global renderers passed in from RenderChunk#rebuildChunk
//	 * @param renderGlobal  the {@link RenderGlobal} passed in from RenderChunk#rebuildChunk
//	 * @return If vanilla rendering should be stopped
//	 * @see io.github.cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunk_diff and cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunkOptifine_diff
//	 */
//	public static boolean rebuildChunkCancelRenderingPreGeneratingCompiledChunkHook(
//			@Nonnull final RenderChunk renderChunk,
//			final float x,
//			final float y,
//			final float z,
//			@Nonnull final ChunkCompileTaskGenerator generator,
//			@Nonnull final CompiledChunk compiledchunk,
//			@Nonnull final BlockPos blockpos,
//			@Nonnull final ChunkCacheOF blockAccess,
//			@Nonnull final VisGraph lvt_9_1_,
//			@Nonnull final HashSet<TileEntity> lvt_10_1_,
//			@Nonnull final RenderGlobal renderGlobal
//	) {
//		final RebuildChunkPreOptifineEvent event = new RebuildChunkPreOptifineEvent(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockAccess, lvt_9_1_, lvt_10_1_, renderGlobal);
//
//		if (RenderChunkRebuildChunkHooksConfig.shouldPostRebuildChunkPreEvent()) {
//			MinecraftForge.EVENT_BUS.post(event);
//		}
//
//		return event.isCanceled();
//	}
//
//	/**
//	 * @param renderChunk              the instance of {@link RenderChunk}
//	 * @param x                        the translation X passed in from RenderChunk#rebuildChunk
//	 * @param y                        the translation Y passed in from RenderChunk#rebuildChunk
//	 * @param z                        the translation Z passed in from RenderChunk#rebuildChunk
//	 * @param generator                the {@link ChunkCompileTaskGenerator} passed in from RenderChunk#rebuildChunk
//	 * @param compiledchunk            the {@link CompiledChunk} passed in from RenderChunk#rebuildChunk
//	 * @param blockpos                 the {@link BlockPos position} stored in a local variable and passed in from RenderChunk#rebuildChunk
//	 * @param blockAccess              the {@link ChunkCacheOF} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_9_1_                 the {@link VisGraph} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_10_1_                the {@link HashSet} of {@link TileEntity TileEntities} with global renderers passed in from RenderChunk#rebuildChunk
//	 * @param renderGlobal             the {@link RenderGlobal} passed in from RenderChunk#rebuildChunk
//	 * @param aboolean                 the boolean[] of used {@link BlockRenderLayer}s
//	 * @param blockrendererdispatcher  the {@link BlockRendererDispatcher}
//	 * @param blockpos$mutableblockpos the {@link BlockPosM} of the block the event is firing for
//	 * @param iblockstate              the {@link IBlockState} of the block the event is firing for
//	 * @param block                    the {@link Block} the event is firing for
//	 * @param blockrenderlayer1        the {@link BlockRenderLayer} the event is firing for
//	 * @return If the block can render in the layer
//	 * @see io.github.cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunk_diff and cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunkOptifine_diff
//	 */
//	public static boolean canBlockRenderInLayerHook(
//			@Nonnull final RenderChunk renderChunk,
//			final float x,
//			final float y,
//			final float z,
//			@Nonnull final ChunkCompileTaskGenerator generator,
//			@Nonnull final CompiledChunk compiledchunk,
//			@Nonnull final BlockPos blockpos,
//			@Nonnull final ChunkCacheOF blockAccess,
//			@Nonnull final VisGraph lvt_9_1_,
//			@Nonnull final HashSet<TileEntity> lvt_10_1_,
//			@Nonnull final RenderGlobal renderGlobal,
//			@Nonnull final boolean[] aboolean,
//			@Nonnull final BlockRendererDispatcher blockrendererdispatcher,
//			@Nonnull final BlockPosM blockpos$mutableblockpos,
//			@Nonnull final IBlockState iblockstate,
//			@Nonnull final Block block,
//			@Nonnull final BlockRenderLayer blockrenderlayer1) {
//		final RebuildChunkBlockRenderInLayerOptifineEvent event = new RebuildChunkBlockRenderInLayerOptifineEvent(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockAccess, lvt_9_1_, lvt_10_1_, renderGlobal, aboolean, blockrendererdispatcher, blockpos$mutableblockpos, iblockstate, block, blockrenderlayer1);
//
//		if (RenderChunkRebuildChunkHooksConfig.shouldPostRebuildChunkBlockRenderInLayerEvent()) {
//			MinecraftForge.EVENT_BUS.post(event);
//		}
//
//		if (event.getResult() == Event.Result.ALLOW) {
//			return true;
//		} else if (event.getResult() == Event.Result.DEFAULT) {
//			//TODO: keep this in sync with non-hooked behaviour
//			return block.canRenderInLayer(iblockstate, blockrenderlayer1);
//		} else {
//			return false;
//		}
//	}
//
//	/**
//	 * @param renderChunk              the instance of {@link RenderChunk}
//	 * @param x                        the translation X passed in from RenderChunk#rebuildChunk
//	 * @param y                        the translation Y passed in from RenderChunk#rebuildChunk
//	 * @param z                        the translation Z passed in from RenderChunk#rebuildChunk
//	 * @param generator                the {@link ChunkCompileTaskGenerator} passed in from RenderChunk#rebuildChunk
//	 * @param compiledchunk            the {@link CompiledChunk} passed in from RenderChunk#rebuildChunk
//	 * @param blockpos                 the {@link BlockPos position} stored in a local variable and passed in from RenderChunk#rebuildChunk
//	 * @param blockAccess              the {@link ChunkCacheOF} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_9_1_                 the {@link VisGraph} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_10_1_                the {@link HashSet} of {@link TileEntity TileEntities} with global renderers passed in from RenderChunk#rebuildChunk
//	 * @param renderGlobal             the {@link RenderGlobal} passed in from RenderChunk#rebuildChunk
//	 * @param aboolean                 the boolean[] of used {@link BlockRenderLayer}s
//	 * @param blockrendererdispatcher  the {@link BlockRendererDispatcher}
//	 * @param blockpos$mutableblockpos the {@link BlockPosM} of the block the event is firing for
//	 * @param iblockstate              the {@link IBlockState} of the block the event is firing for
//	 * @param block                    the {@link Block} the event is firing for
//	 * @param blockrenderlayer1        the {@link BlockRenderLayer} the event is firing for
//	 * @param j                        the ordinal of the {@link BlockRenderLayer} the event is firing for
//	 * @return if the block should be rendered
//	 * @see io.github.cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunk_diff and cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunkOptifine_diff
//	 */
//	public static boolean doesBlockTypeAllowRenderHook(
//			@Nonnull final RenderChunk renderChunk,
//			final float x,
//			final float y,
//			final float z,
//			@Nonnull final ChunkCompileTaskGenerator generator,
//			@Nonnull final CompiledChunk compiledchunk,
//			@Nonnull final BlockPos blockpos,
//			@Nonnull final ChunkCacheOF blockAccess,
//			@Nonnull final VisGraph lvt_9_1_,
//			@Nonnull final HashSet<TileEntity> lvt_10_1_,
//			@Nonnull final RenderGlobal renderGlobal,
//			@Nonnull final boolean[] aboolean,
//			@Nonnull final BlockRendererDispatcher blockrendererdispatcher,
//			@Nonnull final BlockPosM blockpos$mutableblockpos,
//			@Nonnull final IBlockState iblockstate,
//			@Nonnull final Block block,
//			@Nonnull final BlockRenderLayer blockrenderlayer1,
//			final int j
//	) {
//		final RebuildChunkBlockRenderTypeAllowsRenderOptifineEvent event = new RebuildChunkBlockRenderTypeAllowsRenderOptifineEvent(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockAccess, lvt_9_1_, lvt_10_1_, renderGlobal, aboolean, blockrendererdispatcher, blockpos$mutableblockpos, iblockstate, block, blockrenderlayer1, j);
//
//		if (RenderChunkRebuildChunkHooksConfig.shouldPostRebuildChunkBlockRenderInTypeEvent()) {
//			MinecraftForge.EVENT_BUS.post(event);
//		}
//
//		if (event.getResult() == Event.Result.ALLOW) {
//			return true;
//		} else if (event.getResult() == Event.Result.DEFAULT) {
//			if (RenderChunkRebuildChunkHooksConfig.shouldTweakCanBlockRenderInType()) {
//				return iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE;
//			} else {
//				//TODO: keep this in sync with non-hooked behaviour
//				return block.getDefaultState().getRenderType() != EnumBlockRenderType.INVISIBLE;
//			}
//		} else {
//			return false;
//		}
//	}
//
//	/**
//	 * @param renderChunk              the instance of {@link RenderChunk}
//	 * @param x                        the translation X passed in from RenderChunk#rebuildChunk
//	 * @param y                        the translation Y passed in from RenderChunk#rebuildChunk
//	 * @param z                        the translation Z passed in from RenderChunk#rebuildChunk
//	 * @param generator                the {@link ChunkCompileTaskGenerator} passed in from RenderChunk#rebuildChunk
//	 * @param compiledchunk            the {@link CompiledChunk} passed in from RenderChunk#rebuildChunk
//	 * @param blockpos                 the {@link BlockPos position} stored in a local variable and passed in from RenderChunk#rebuildChunk
//	 * @param blockAccess              the {@link ChunkCacheOF} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_9_1_                 the {@link VisGraph} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_10_1_                the {@link HashSet} of {@link TileEntity TileEntities} with global renderers passed in from RenderChunk#rebuildChunk
//	 * @param renderGlobal             the {@link RenderGlobal} passed in from RenderChunk#rebuildChunk
//	 * @param aboolean                 the boolean[] of used {@link BlockRenderLayer}s
//	 * @param blockrendererdispatcher  the {@link BlockRendererDispatcher}
//	 * @param blockpos$mutableblockpos the {@link BlockPosM} of the block the event is firing for
//	 * @param iblockstate              the {@link IBlockState} of the block the event is firing for
//	 * @param block                    the {@link Block} the event is firing for
//	 * @param blockrenderlayer1        the {@link BlockRenderLayer} the event is firing for
//	 * @param j                        the ordinal of the {@link BlockRenderLayer} the event is firing for
//	 * @param bufferbuilder            the {@link BufferBuilder} for the {@link BlockRenderLayer} the event is firing for
//	 * @return If the block should NOT be rebuilt to the chunk by vanilla
//	 * @see io.github.cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunk_diff and cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunkOptifine_diff
//	 */
//	public static boolean rebuildChunkBlockHook(
//			@Nonnull final RenderChunk renderChunk,
//			final float x,
//			final float y,
//			final float z,
//			@Nonnull final ChunkCompileTaskGenerator generator,
//			@Nonnull final CompiledChunk compiledchunk,
//			@Nonnull final BlockPos blockpos,
//			@Nonnull final ChunkCacheOF blockAccess,
//			@Nonnull final VisGraph lvt_9_1_,
//			@Nonnull final HashSet<TileEntity> lvt_10_1_,
//			@Nonnull final RenderGlobal renderGlobal,
//			@Nonnull final boolean[] aboolean,
//			@Nonnull final BlockRendererDispatcher blockrendererdispatcher,
//			@Nonnull final BlockPosM blockpos$mutableblockpos,
//			@Nonnull final IBlockState iblockstate,
//			@Nonnull final Block block,
//			@Nonnull final BlockRenderLayer blockrenderlayer1,
//			final int j,
//			@Nonnull final BufferBuilder bufferbuilder
//	) {
//		final RebuildChunkBlockOptifineEvent event = new RebuildChunkBlockOptifineEvent(renderChunk, x, y, z, generator, compiledchunk, blockpos, blockAccess, lvt_9_1_, lvt_10_1_, renderGlobal, aboolean, blockrendererdispatcher, blockpos$mutableblockpos, iblockstate, block, blockrenderlayer1, j, bufferbuilder);
//
//		if (RenderChunkRebuildChunkHooksConfig.shouldPostRebuildChunkBlockEvent()) {
//			MinecraftForge.EVENT_BUS.post(event);
//		}
//
//		return event.isCanceled();
//	}
//
//	/**
//	 * @param renderChunk            the instance of {@link RenderChunk}
//	 * @param x                      the translation X passed in from RenderChunk#rebuildChunk
//	 * @param y                      the translation Y passed in from RenderChunk#rebuildChunk
//	 * @param z                      the translation Z passed in from RenderChunk#rebuildChunk
//	 * @param generator              the {@link ChunkCompileTaskGenerator} passed in from RenderChunk#rebuildChunk
//	 * @param compiledchunk          the {@link CompiledChunk} passed in from RenderChunk#rebuildChunk
//	 * @param blockpos               the {@link BlockPos position} stored in a local variable and passed in from RenderChunk#rebuildChunk
//	 * @param asmCreatedChunkCacheOf the {@link ChunkCacheOF} created with ASM and passed in from RenderChunk#rebuildChunk
//	 * @param lvt_9_1_               the {@link VisGraph} passed in from RenderChunk#rebuildChunk
//	 * @param lvt_10_1_              the {@link HashSet} of {@link TileEntity TileEntities} with global renderers passed in from RenderChunk#rebuildChunk
//	 * @param renderGlobal           the {@link RenderGlobal} passed in from RenderChunk#rebuildChunk
//	 * @see io.github.cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunk_diff and cadiboo.renderchunkrebuildchunkhooks.core.util.rebuildChunkOptifine_diff
//	 */
//	public static void rebuildChunkPostHook(
//			@Nonnull final RenderChunk renderChunk,
//			final float x,
//			final float y,
//			final float z,
//			@Nonnull final ChunkCompileTaskGenerator generator,
//			@Nonnull final CompiledChunk compiledchunk,
//			@Nonnull final BlockPos blockpos,
//			@Nonnull final ChunkCacheOF asmCreatedChunkCacheOf,
//			@Nonnull final VisGraph lvt_9_1_,
//			@Nonnull final HashSet<TileEntity> lvt_10_1_,
//			@Nonnull final RenderGlobal renderGlobal
//	) {
//		final RebuildChunkPostOptifineEvent event = new RebuildChunkPostOptifineEvent(renderChunk, x, y, z, generator, compiledchunk, blockpos, asmCreatedChunkCacheOf, lvt_9_1_, lvt_10_1_, renderGlobal);
//
//		if (RenderChunkRebuildChunkHooksConfig.shouldPostRebuildChunkPostEvent()) {
//			MinecraftForge.EVENT_BUS.post(event);
//		}
//	}
//
//}
