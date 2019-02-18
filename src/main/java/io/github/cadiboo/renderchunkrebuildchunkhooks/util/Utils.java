package io.github.cadiboo.renderchunkrebuildchunkhooks.util;

import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public class Utils {

	public static final MethodHandle renderChunk_preRenderBlocks;
	public static final MethodHandle compiledChunk_setLayerUsed;
	public static final MethodHandle renderChunk_postRenderBlocks;

	static {
		try {
			renderChunk_preRenderBlocks = MethodHandles.publicLookup().unreflect(ObfuscationReflectionHelper_findMethod(RenderChunk.class, "func_178573_a",
					BufferBuilder.class, BlockPos.class
			));
		} catch (ReportedException e) {
			throw e;
		} catch (Throwable throwable) {
			CrashReport crashReport = new CrashReport("Error getting method handle for RenderChunk#preRenderBlocks!", throwable);
			crashReport.makeCategory("Reflectively Accessing RenderChunk#preRenderBlocks");
			throw new ReportedException(crashReport);
		}
		try {
			compiledChunk_setLayerUsed = MethodHandles.publicLookup().unreflect(ObfuscationReflectionHelper_findMethod(CompiledChunk.class, "func_178486_a",
					BlockRenderLayer.class
			));
		} catch (ReportedException e) {
			throw e;
		} catch (Throwable throwable) {
			CrashReport crashReport = new CrashReport("Error getting method handle for CompiledChunk#setLayerUsed!", throwable);
			crashReport.makeCategory("Reflectively Accessing CompiledChunk#setLayerUsed");
			throw new ReportedException(crashReport);
		}
		try {
			renderChunk_postRenderBlocks = MethodHandles.publicLookup().unreflect(ObfuscationReflectionHelper_findMethod(RenderChunk.class, "func_178584_a",
					BlockRenderLayer.class, float.class, float.class, float.class, BufferBuilder.class, CompiledChunk.class
			));
		} catch (ReportedException e) {
			throw e;
		} catch (Throwable throwable) {
			CrashReport crashReport = new CrashReport("Error getting method handle for RenderChunk#postRenderBlocks!", throwable);
			crashReport.makeCategory("Reflectively Accessing RenderChunk#postRenderBlocks");
			throw new ReportedException(crashReport);
		}
	}

	//FFS
	public static Method ObfuscationReflectionHelper_findMethod(@Nonnull Class<?> clazz, @Nonnull String methodName, Class<?>... parameterTypes) {

		Preconditions.checkNotNull(clazz);
		Preconditions.checkNotNull(methodName);
//		Preconditions.checkArgument(methodName.isEmpty(), "Method name cannot be empty");
		//FIX "methodName cannot be null, but MUST be empty"
		Preconditions.checkArgument(!methodName.isEmpty(), "Method name cannot be empty");

		try {
			Method m = clazz.getDeclaredMethod(ObfuscationReflectionHelper.remapName(methodName), parameterTypes);
			m.setAccessible(true);
			return m;
		} catch (Exception e) {
			throw new ObfuscationReflectionHelper.UnableToFindMethodException(e);
		}

	}

	/**
	 * Invokes {@link CompiledChunk#setLayerUsed(BlockRenderLayer)}
	 *
	 * @param compiledChunk    the instance of {@link CompiledChunk}
	 * @param blockRenderLayer the layer param
	 * @deprecated You probably don't want this, you probably want something like <code>event.getUsedBlockRenderLayers()[blockRenderLayer.ordinal()] = true;</code>
	 */
	@Deprecated
	public static void compiledChunk_setLayerUsed(final CompiledChunk compiledChunk, final BlockRenderLayer blockRenderLayer) {
		try {
			compiledChunk_setLayerUsed.invokeExact(compiledChunk, blockRenderLayer);
		} catch (final Throwable throwable) {
			CrashReport crashReport = new CrashReport("Error invoking method handle for CompiledChunk#setLayerUsed!", throwable);
			crashReport.makeCategory("Reflectively Accessing CompiledChunk#setLayerUsed");
			throw new ReportedException(crashReport);
		}
	}

	/**
	 * Invokes {@link RenderChunk#preRenderBlocks(BufferBuilder, BlockPos)}
	 *
	 * @param renderChunk   the instance of {@link RenderChunk}
	 * @param bufferBuilder the bufferBuilderIn param
	 * @param pos           the pos param
	 */
	public static void renderChunk_preRenderBlocks(final RenderChunk renderChunk, final BufferBuilder bufferBuilder, final BlockPos pos) {
		try {
			renderChunk_preRenderBlocks.invokeExact(renderChunk, bufferBuilder, pos);
		} catch (Throwable throwable) {
			CrashReport crashReport = new CrashReport("Error invoking method handle for RenderChunk#preRenderBlocks!", throwable);
			crashReport.makeCategory("Reflectively Accessing RenderChunk#preRenderBlocks");
			throw new ReportedException(crashReport);
		}
	}

	/**
	 * Invokes {@link RenderChunk#postRenderBlocks(BlockRenderLayer, float, float, float, BufferBuilder, CompiledChunk)}
	 *
	 * @param renderChunk      the instance of {@link RenderChunk}
	 * @param blockRenderLayer the layer param
	 * @param x                the x param
	 * @param y                the y param
	 * @param z                the z param
	 * @param bufferBuilder    the bufferBuilderIn param
	 * @param compiledChunk    the compiledChunkIn param
	 */
	public static void renderChunk_postRenderBlocks(final RenderChunk renderChunk, BlockRenderLayer blockRenderLayer, float x, float y, float z, BufferBuilder bufferBuilder, CompiledChunk compiledChunk) {
		try {
			renderChunk_postRenderBlocks.invokeExact(renderChunk, blockRenderLayer, x, y, z, bufferBuilder, compiledChunk);
		} catch (Throwable throwable) {
			CrashReport crashReport = new CrashReport("Error invoking method handle for RenderChunk#postRenderBlocks!", throwable);
			crashReport.makeCategory("Reflectively Accessing RenderChunk#postRenderBlocks");
			throw new ReportedException(crashReport);
		}
	}

	/**
	 * Returns a started {@link BufferBuilder}
	 *
	 * @param blockRenderLayer the {@link BlockRenderLayer}
	 * @param generator        the generator to get the {@link BufferBuilder} from
	 * @param renderChunk      the instance of {@link RenderChunk}
	 * @param compiledChunk    the {@link CompiledChunk}
	 * @param renderChunkPos   the position of the render chunk
	 * @return the {@link BufferBuilder} for the {@link BlockRenderLayer}
	 */
	public BufferBuilder startOrContinueLayer(final BlockRenderLayer blockRenderLayer, final ChunkRenderTask generator, RenderChunk renderChunk, CompiledChunk compiledChunk, BlockPos.MutableBlockPos renderChunkPos) {
		return useAndStartOrContinueLayer(blockRenderLayer, generator, renderChunk, compiledChunk, renderChunkPos, false);
	}

	/**
	 * Returns a started {@link BufferBuilder} and sets the appropriate index of usedBlockRenderLayers to true
	 *
	 * @param usedBlockRenderLayers the boolean array of used {@link BlockRenderLayer}s
	 * @param blockRenderLayer      the {@link BlockRenderLayer}
	 * @param generator             the generator to get the {@link BufferBuilder} from
	 * @param renderChunk           the instance of {@link RenderChunk}
	 * @param compiledChunk         the {@link CompiledChunk}
	 * @param renderChunkPos        the position of the render chunk
	 * @return the {@link BufferBuilder} for the {@link BlockRenderLayer}
	 */
	public BufferBuilder useAndStartOrContinueLayer(final boolean[] usedBlockRenderLayers, final BlockRenderLayer blockRenderLayer, final ChunkRenderTask generator, RenderChunk renderChunk, CompiledChunk compiledChunk, BlockPos.MutableBlockPos renderChunkPos) {
		usedBlockRenderLayers[blockRenderLayer.ordinal()] = true;
		return startOrContinueLayer(blockRenderLayer, generator, renderChunk, compiledChunk, renderChunkPos);
	}

	/**
	 * Returns a started {@link BufferBuilder} and invokes compiledChunk_setLayerUsed directly.
	 * Less efficient than other methods as this gets done for all layers who's index in usedBlockRenderLayers is true
	 *
	 * @param blockRenderLayer the {@link BlockRenderLayer}
	 * @param generator        the generator to get the {@link BufferBuilder} from
	 * @param renderChunk      the instance of {@link RenderChunk}
	 * @param compiledChunk    the {@link CompiledChunk}
	 * @param renderChunkPos   the position of the render chunk
	 * @return the {@link BufferBuilder} for the {@link BlockRenderLayer}
	 * @deprecated You probably don't want this, you probably want something like <code>event.getUsedBlockRenderLayers()[blockRenderLayer.ordinal()] = true;</code>
	 */
	@Deprecated
	public BufferBuilder useAndStartOrContinueLayerDirect(final BlockRenderLayer blockRenderLayer, final ChunkRenderTask generator, RenderChunk renderChunk, CompiledChunk compiledChunk, BlockPos.MutableBlockPos renderChunkPos) {
		return useAndStartOrContinueLayer(blockRenderLayer, generator, renderChunk, compiledChunk, renderChunkPos, true);
	}

	private BufferBuilder useAndStartOrContinueLayer(final BlockRenderLayer blockRenderLayer, final ChunkRenderTask generator, RenderChunk renderChunk, CompiledChunk compiledChunk, BlockPos.MutableBlockPos renderChunkPos, final boolean setLayerUsedDirect) {
		final BufferBuilder bufferBuilder = generator.getRegionRenderCacheBuilder().getBuilder(blockRenderLayer);
		if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
			compiledChunk.setLayerStarted(blockRenderLayer);
			if (setLayerUsedDirect) {
				compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
			}
			renderChunk_preRenderBlocks(renderChunk, bufferBuilder, renderChunkPos);
		}

		return bufferBuilder;
	}

}
