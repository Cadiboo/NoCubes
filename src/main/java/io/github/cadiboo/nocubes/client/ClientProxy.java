package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.SmoothLightingBlockFluidRenderer;
import io.github.cadiboo.nocubes.util.IProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_N;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;

/**
 * The version of IProxy that gets injected into {@link NoCubes#PROXY} on a PHYSICAL CLIENT
 *
 * @author Cadiboo
 */
public final class ClientProxy implements IProxy {

	public static final KeyBinding toggleTerrainSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleTerrainSmoothableBlockState", GLFW_KEY_N, "key.categories." + MOD_ID);
	public static final KeyBinding toggleEnabled = new KeyBinding(MOD_ID + ".key.toggleEnabled", GLFW_KEY_O, "key.categories." + MOD_ID);
	public static final KeyBinding toggleProfilers = new KeyBinding(MOD_ID + ".key.toggleProfilers", GLFW_KEY_P, "key.categories." + MOD_ID);
	public static final KeyBinding toggleLeavesSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleLeavesSmoothableBlockState", GLFW_KEY_K, "key.categories." + MOD_ID);

	private static final MethodHandle WorldRenderer_markBlocksForUpdate;

	public static SmoothLightingBlockFluidRenderer fluidRenderer;

	static {
		ClientRegistry.registerKeyBinding(toggleTerrainSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleLeavesSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleEnabled);
		ClientRegistry.registerKeyBinding(toggleProfilers);
	}

	static {
		try {
			WorldRenderer_markBlocksForUpdate = MethodHandles.publicLookup().unreflect(
					ObfuscationReflectionHelper.findMethod(WorldRenderer.class, "func_184385_a",
							int.class, int.class, int.class, int.class, int.class, int.class, boolean.class
					)
			);
		} catch (Exception e) {
			final CrashReport crashReport = new CrashReport("Unable to find method WorldRenderer.markBlocksForUpdate!", e);
			crashReport.makeCategory("Finding Method");
			throw new ReportedException(crashReport);
		}
	}

	@Override
	public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately) {

		final WorldRenderer worldRenderer = Minecraft.getInstance().worldRenderer;

		if (worldRenderer.world == null || worldRenderer.viewFrustum == null) {
			return;
		}

		try {
			WorldRenderer_markBlocksForUpdate.invokeExact(worldRenderer, minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
		} catch (ReportedException e) {
			throw e;
		} catch (Throwable throwable) {
			final CrashReport crashReport = new CrashReport("Exception invoking method WorldRenderer.markBlocksForUpdate", throwable);
			crashReport.makeCategory("Reflectively Invoking Method");
			throw new ReportedException(crashReport);
		}
	}

	public void replaceFluidRendererCauseImBored() {
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		final SmoothLightingBlockFluidRenderer smoothLightingBlockFluidRenderer = new SmoothLightingBlockFluidRenderer(blockRendererDispatcher.fluidRenderer);
		blockRendererDispatcher.fluidRenderer = smoothLightingBlockFluidRenderer;
		ClientProxy.fluidRenderer = smoothLightingBlockFluidRenderer;
	}

}
