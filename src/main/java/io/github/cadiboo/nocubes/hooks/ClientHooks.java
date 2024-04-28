package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mixin.client.RenderChunkRebuildTaskMixin;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


/**
 * Client-only version of {@link Hooks} that contains references to classes that only exist on the minecraft client.
 */
@OnlyIn(Dist.CLIENT)
@SuppressWarnings("unused") // Called via ASM
public final class ClientHooks {

	/**
	 * Makes extended fluids render properly
	 * Called instead of {@link BlockState#getFluidState()}
	 */
	public static FluidState getRenderFluidState(BlockPos pos, BlockState state) {
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
	 * Helper function for use by other hooks/mixins.
	 *
	 * @see RenderChunkRebuildTaskMixin#noCubes$getRenderShape
	 */
	public static RenderShape getRenderShape(BlockState state) {
		// Invisible blocks are not rendered by vanilla
		return allowVanillaRenderingFor(state) ? state.getRenderShape() : RenderShape.INVISIBLE;
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
