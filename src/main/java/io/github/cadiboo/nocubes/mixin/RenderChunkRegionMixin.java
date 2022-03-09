package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRenderCache.class)
public class RenderChunkRegionMixin {

	/**
	 * Allows us to provide our extended fluids to the vanilla fluid renderer.
	 * Vanilla's implementation uses {@link BlockState#getFluidState()} so we need to change it.
	 */
	@Inject(
		method = "getFluidState",
		at = @At("HEAD"),
		cancellable = true
	)
	public void nocubes_getExtendedFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> ci) {
		if (NoCubesConfig.Server.extendFluidsRange > 0)
			ci.setReturnValue(ClientUtil.getExtendedFluidState(pos));
	}
}
