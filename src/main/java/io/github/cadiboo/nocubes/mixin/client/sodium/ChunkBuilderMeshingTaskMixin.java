package io.github.cadiboo.nocubes.mixin.client.sodium;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.cadiboo.nocubes.client.render.SodiumRenderer;
import io.github.cadiboo.nocubes.hooks.ClientHooks;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderBuilderSodium;
import io.github.cadiboo.nocubes.mixin.Constants;
import io.github.cadiboo.nocubes.mixin.client.RenderChunkRebuildTaskMixin;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

/**
 * Sodium version of {@link RenderChunkRebuildTaskMixin}.
 */
@Pseudo // Sodium may not be installed
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.chunk.compile.tasks.ChunkBuilderMeshingTask")
public class ChunkBuilderMeshingTaskMixin implements INoCubesChunkSectionRenderBuilderSodium {

//	@Shadow
//	@Final
//	private Map<BlockPos, ModelData> modelDataMap;


	@Shadow @Final private RandomSource random;

	/**
	 * @see RenderChunkRebuildTaskMixin#noCubes$renderChunk
	 */
	@ModifyExpressionValue(
		method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
		at = @At(
			value = "NEW",
			target = "(Lme/jellysquid/mods/sodium/client/world/WorldSlice;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/pipeline/BlockRenderContext;"
		)
	)
	private BlockRenderContext noCubes$renderChunk(
		BlockRenderContext context,
		@Local(ordinal = 0) ChunkBuildBuffers buffers,
		@Local(ordinal = 0) BlockRenderCache cache,
		@Local(ordinal = 0) BlockPos.MutableBlockPos blockPos,
		@Local(ordinal = 1) BlockPos.MutableBlockPos modelOffset
	) {
		SodiumRenderer.renderChunk(
			this,
			this.random,
			buffers,
			cache,
			blockPos,
			modelOffset,
			context
		);
		return context;
	}

	/**
	 * @see RenderChunkRebuildTaskMixin#noCubes$getRenderShape
	 */
	@Redirect(
		method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getRenderShape()Lnet/minecraft/world/level/block/RenderShape;"
		)
	)
	public RenderShape noCubes$getRenderShape(BlockState state) {
		return ClientHooks.getRenderShape(state);
	}

	@Override
	public ModelData noCubes$getModelData(BlockPos worldPos) {
		return ModelData.EMPTY;
//		return this.modelDataMap.getOrDefault(worldPos, ModelData.EMPTY);
	}

	/**
	 * See documentation on {@link Constants#EXTENDED_FLUIDS_BLOCK_POS_REF_NAME}
	 */
	@Redirect(
		method = "execute(Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildContext;Lme/jellysquid/mods/sodium/client/util/task/CancellationToken;)Lme/jellysquid/mods/sodium/client/render/chunk/compile/ChunkBuildOutput;",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/block/state/BlockState;getFluidState()Lnet/minecraft/world/level/material/FluidState;"
		)
	)
	private FluidState getRenderFluidState(
		BlockState instance,
		@Local(ordinal = 0) BlockPos.MutableBlockPos pos
	) {
		return ClientHooks.getRenderFluidState(pos, instance);
	}
}
