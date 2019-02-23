package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.IProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.renderchunkrebuildchunkhooks.util.Utils.ObfuscationReflectionHelper_findMethod;

/**
 * The version of IProxy that gets injected into {@link NoCubes#PROXY} on a PHYSICAL CLIENT
 *
 * @author Cadiboo
 */
public final class ClientProxy implements IProxy {

	private static final int KEY_CODE_N = 78;

	public static final KeyBinding toggleSmoothableBlockstate = new KeyBinding(MOD_ID + ".key.toggleSmoothableBlockstate", KEY_CODE_N, "key.categories.nocubes");

	static {
		ClientRegistry.registerKeyBinding(toggleSmoothableBlockstate);
	}
	private static final MethodHandle WorldRenderer_markBlocksForUpdate;
	static {
		try {
			WorldRenderer_markBlocksForUpdate = MethodHandles.publicLookup().unreflect(
					//TODO change back once forge isn't bugged
					ObfuscationReflectionHelper_findMethod(WorldRenderer.class, "func_184385_a",
							int.class, int.class, int.class, int.class, int.class, int.class, boolean.class
					)
			);
		} catch (Exception e) {
			final CrashReport crashReport = new CrashReport("Unable to find method WorldRenderer#markBlocksForUpdate!", e);
			crashReport.makeCategory("Finding Method");
			throw new ReportedException(crashReport);
		}
	}

	@Override
	public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately) {

		final WorldRenderer renderGlobal = Minecraft.getInstance().renderGlobal;

		if (renderGlobal.world == null || renderGlobal.viewFrustum == null) {
			return;
		}

		renderGlobal.viewFrustum.markBlocksForUpdate(minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);

		try {
			WorldRenderer_markBlocksForUpdate.invokeExact(renderGlobal, minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
		} catch (ReportedException e) {
			throw e;
		} catch (Throwable throwable) {
			final CrashReport crashReport = new CrashReport("Exception invoking method WorldRenderer#markBlocksForUpdate", throwable);
			crashReport.makeCategory("Reflectively Invoking Method");
			throw new ReportedException(crashReport);
		}
	}

}
