package io.github.cadiboo.nocubes.hooks;

/**
 * Implemented by {@link io.github.cadiboo.nocubes.mixin.BlockStateBaseMixin}.
 * Inspired by <a href="https://forums.minecraftforge.net/topic/11596-add-a-field-to-a-base-class/?do=findComment&comment=61923">this post</a>.
 */
public interface INoCubesBlockState {

	void setSmoothable(boolean value);

	boolean isSmoothable();

}
