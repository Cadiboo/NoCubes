package io.github.cadiboo.nocubes.server;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.Proxy;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The version of IProxy that gets injected into {@link NoCubes#PROXY} on a PHYSICAL/DEDICATED SERVER
 *
 * @author Cadiboo
 */
// OnlyIn is here so that we explicitly crash if the class gets loaded when not on the server
@OnlyIn(Dist.DEDICATED_SERVER)
public final class ServerProxy implements Proxy {

	@Override
	public void replaceFluidRenderer() {
		// NOOP client only
	}

}
