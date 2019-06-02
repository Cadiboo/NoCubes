package io.github.cadiboo.nocubes.server;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.Proxy;
import net.minecraft.server.dedicated.ServerHangWatchdog;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * The version of IProxy that gets injected into {@link NoCubes#PROXY} on a PHYSICAL/DEDICATED SERVER
 *
 * @author Cadiboo
 */
public final class ServerProxy implements Proxy {

	public static final ServerHangWatchdog ignored = null;

	@Override
	public void markBlocksForUpdate(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final boolean updateImmediately) {
		// NOOP client only
	}

	@Override
	public void replaceFluidRendererCauseImBored() {
		// NOOP client only
	}

	@Override
	public void crashIfRCRCHInstalled() {
		FMLCommonHandler.instance().raiseException(new IllegalStateException("NoCubes Dependency Error! RenderChunk rebuildChunk Hooks CANNOT be installed! Remove RenderChunk rebuildChunk Hooks from the mods folder and then restart the game."), "NoCubes Dependency Error! RenderChunk rebuildChunk Hooks CANNOT be installed! Remove RenderChunk rebuildChunk Hooks from the mods folder and then restart the game.", true);
	}

}
