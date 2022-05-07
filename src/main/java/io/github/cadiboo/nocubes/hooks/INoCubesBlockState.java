package io.github.cadiboo.nocubes.hooks;

/**
 * Implemented (by Mixin) on BlockStateBase.
 * Inspired by "https://forums.minecraftforge.net/topic/11596-add-a-field-to-a-base-class/?do=findComment&comment=61923".
 */
public interface INoCubesBlockState {

	void setTerrainSmoothable(boolean value);

	boolean isTerrainSmoothable();

}
