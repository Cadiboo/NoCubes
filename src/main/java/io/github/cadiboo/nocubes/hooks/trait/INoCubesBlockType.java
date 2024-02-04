package io.github.cadiboo.nocubes.hooks.trait;

/**
 * Adds extra functionality to {@link net.minecraft.world.level.block.Block}.
 * Implemented by {@link io.github.cadiboo.nocubes.mixin.BlockMixin}.
 */
public interface INoCubesBlockType {
	boolean noCubes$hasCollision();
}
