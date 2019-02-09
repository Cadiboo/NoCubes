package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.IProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

/**
 * The version of IProxy that gets injected into {@link NoCubes#proxy} on a PHYSICAL CLIENT
 *
 * @author Cadiboo
 */
public final class ClientProxy implements IProxy {

	private static final int KEY_CODE_N = 49;

	public static final KeyBinding toggleSmoothableBlockstate = new KeyBinding(MOD_ID + ".key.toggleSmoothableBlockstate", KeyConflictContext.IN_GAME, KEY_CODE_N, "key.categories.misc");

	static {
		ClientRegistry.registerKeyBinding(toggleSmoothableBlockstate);
	}
	private static final MethodHandle RenderGlobal_markBlocksForUpdate;
	static {
		try {
			RenderGlobal_markBlocksForUpdate = MethodHandles.publicLookup().unreflect(
					ObfuscationReflectionHelper.findMethod(RenderGlobal.class, "func_184385_a",
							void.class,
							int.class, int.class, int.class, int.class, int.class, int.class, boolean.class
					)
			);
		} catch (IllegalAccessException e) {
			final CrashReport crashReport = new CrashReport("Unable to find method RenderGlobal.markBlocksForUpdate. Method does not exist!", e);
			crashReport.makeCategory("Finding Method");
			throw new ReportedException(crashReport);
		}
	}

	@Override
	public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately) {
		try {
			RenderGlobal_markBlocksForUpdate.invokeExact(Minecraft.getMinecraft().renderGlobal, minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
		} catch (ReportedException e) {
			throw e;
		} catch (Throwable throwable) {
			final CrashReport crashReport = new CrashReport("Exception invoking method RenderGlobal.markBlocksForUpdate", throwable);
			crashReport.makeCategory("Reflectively Invoking Method");
			throw new ReportedException(crashReport);
		}
	}

}
