package io.github.cadiboo.rcrch.util;

import net.minecraft.world.World;

/**
 * @author Cadiboo
 */
public final class WorldReference {

	private World reference;

	public WorldReference(final World reference) {
		this.reference = reference;
	}

	public World get() {
		return reference;
	}

	public void set(final World reference) {
		this.reference = reference;
	}

}
