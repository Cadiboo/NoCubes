package io.github.cadiboo.nocubes.server;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.IProxy;

/**
 * The version of IProxy that gets injected into {@link NoCubes#PROXY} on a PHYSICAL/DEDICATED SERVER
 *
 * @author Cadiboo
 */
public final class ServerProxy implements IProxy {

	@Override
	public void markBlocksForUpdate(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final boolean updateImmediately) {
		// NOOP client only
	}

	@Override
	public void replaceFluidRendererCauseImBored() {
		// NOOP client only
	}

	@Override
	public void setupDecentGraphicsSettings() {
		// NOOP client only
	}

}
