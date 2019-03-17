package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.ModConfig;
import net.minecraft.entity.Entity;

/**
 * @author Cadiboo
 */
@SuppressWarnings({
		"unused", // Hooks get invoked by ASM redirects
		"WeakerAccess" // Hooks need to be public to be invoked
})
public final class IsEntityInsideOpaqueBlockHook {

	public static boolean isEntityInsideOpaqueBlock(final Entity entityIn) {
		if (ModConfig.enableCollisions) {
			return CollisionHandler.isEntityInsideOpaqueBlock(entityIn);
		} else {
			return isEntityInsideOpaqueBlockDefault(entityIn);
		}
	}

	public static boolean isEntityInsideOpaqueBlockDefault(final Entity entityIn) {
		runIsEntityInsideOpaqueBlockDefaultOnce( entityIn);
		return entityIn.isEntityInsideOpaqueBlock();
	}

	private static void runIsEntityInsideOpaqueBlockDefaultOnce(final Entity entity) {
		// Filled with ASM
//		entity.runIsEntityInsideOpaqueBlockDefaultOnce = true;
		throw new UnsupportedOperationException("This method should have been filled by ASM!");
	}

}
