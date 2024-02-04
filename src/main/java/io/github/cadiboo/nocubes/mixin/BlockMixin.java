package io.github.cadiboo.nocubes.mixin;

import io.github.cadiboo.nocubes.hooks.trait.INoCubesBlockType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockBehaviour.class)
public class BlockMixin implements INoCubesBlockType {

	@Final
	@Shadow
	protected boolean hasCollision;

	@Override
	public boolean noCubes$hasCollision() {
		return hasCollision;
	}
}
