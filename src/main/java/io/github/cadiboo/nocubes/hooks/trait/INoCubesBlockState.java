package io.github.cadiboo.nocubes.hooks.trait;

/**
 * Adds extra functionality to {@link net.minecraft.world.level.block.state.BlockState}.
 * Implemented by {@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin}.
 * Inspired by <a href="https://forums.minecraftforge.net/topic/11596-add-a-field-to-a-base-class/?do=findComment&comment=61923">this post</a>.
 */
public interface INoCubesBlockState {

	void noCubes$setSmoothable(boolean value);

	boolean noCubes$isSmoothable();

}
