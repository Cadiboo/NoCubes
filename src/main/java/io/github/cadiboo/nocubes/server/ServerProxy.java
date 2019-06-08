package io.github.cadiboo.nocubes.server;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.Proxy;
import net.minecraft.server.dedicated.ServerHangWatchdog;

/**
 * The version of IProxy that gets injected into {@link NoCubes#PROXY} on a PHYSICAL/DEDICATED SERVER
 *
 * @author Cadiboo
 */
public final class ServerProxy implements Proxy {

	public static final ServerHangWatchdog ignored = null;

	@Override
	public void markBlocksForUpdate(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ) {
		// NOOP client only
	}

	@Override
	public void replaceFluidRendererCauseImBored() {
		// NOOP client only
	}

}
