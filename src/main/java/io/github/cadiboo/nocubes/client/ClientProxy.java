package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.SmoothLightingBlockFluidRenderer;
import io.github.cadiboo.nocubes.util.IProxy;
import io.github.cadiboo.nocubes.util.ObfuscationReflectionHelperCopy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;

/**
 * The version of IProxy that gets injected into {@link NoCubes#PROXY} on a PHYSICAL CLIENT
 *
 * @author Cadiboo
 */
public final class ClientProxy implements IProxy {

	private static final int KEY_CODE_N = 49;
	private static final int KEY_CODE_O = 24;
	private static final int KEY_CODE_P = 25;
	private static final int KEY_CODE_K = 37;

	public static final KeyBinding toggleTerrainSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleTerrainSmoothableBlockState", KeyConflictContext.IN_GAME, KEY_CODE_N, "key.categories." + MOD_ID);
	public static final KeyBinding toggleLeavesSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleLeavesSmoothableBlockState", KeyConflictContext.IN_GAME, KEY_CODE_K, "key.categories." + MOD_ID);
	public static final KeyBinding toggleEnabled = new KeyBinding(MOD_ID + ".key.toggleEnabled", KeyConflictContext.IN_GAME, KEY_CODE_O, "key.categories." + MOD_ID);
	public static final KeyBinding toggleProfilers = new KeyBinding(MOD_ID + ".key.toggleProfilers", KeyConflictContext.IN_GAME, KEY_CODE_P, "key.categories." + MOD_ID);

	public static SmoothLightingBlockFluidRenderer fluidRenderer;

	static {
		ClientRegistry.registerKeyBinding(toggleTerrainSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleLeavesSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleEnabled);
		ClientRegistry.registerKeyBinding(toggleProfilers);
	}
	private static final MethodHandle RenderGlobal_markBlocksForUpdate;
	static {
		try {
//			RenderGlobal_markBlocksForUpdate = MethodHandles.publicLookup().unreflect(
//					ObfuscationReflectionHelper.findMethod(RenderGlobal.class, "func_184385_a",
//							void.class,
//							int.class, int.class, int.class, int.class, int.class, int.class, boolean.class
//					)
//			);
			RenderGlobal_markBlocksForUpdate = MethodHandles.publicLookup().unreflect(
					ReflectionHelper.findMethod(RenderGlobal.class, "markBlocksForUpdate", "func_184385_a",
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

		final RenderGlobal renderGlobal = Minecraft.getMinecraft().renderGlobal;

		if (renderGlobal.world == null || renderGlobal.viewFrustum == null) {
			return;
		}

		try {
			RenderGlobal_markBlocksForUpdate.invokeExact(renderGlobal, minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
		} catch (ReportedException e) {
			throw e;
		} catch (Throwable throwable) {
			final CrashReport crashReport = new CrashReport("Exception invoking method RenderGlobal.markBlocksForUpdate", throwable);
			crashReport.makeCategory("Reflectively Invoking Method");
			throw new ReportedException(crashReport);
		}
	}

	public void replaceFluidRendererCauseImBored() {
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		final BlockFluidRenderer fluidRenderer = ObfuscationReflectionHelperCopy.getPrivateValue(BlockRendererDispatcher.class, blockRendererDispatcher, "field_175025_e");
		final SmoothLightingBlockFluidRenderer smoothLightingBlockFluidRenderer = new SmoothLightingBlockFluidRenderer(fluidRenderer);
		ObfuscationReflectionHelperCopy.setPrivateValue(BlockRendererDispatcher.class, blockRendererDispatcher, smoothLightingBlockFluidRenderer, "field_175025_e");
		ClientProxy.fluidRenderer = smoothLightingBlockFluidRenderer;
	}

	@Override
	public void setupDecentGraphicsSettings() {
		final GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
		boolean needsResave = false;
		if (gameSettings.ambientOcclusion < 1) {
			NoCubes.NO_CUBES_LOG.info("Smooth lighting was off. EW! Just set it to MINIMAL");
			gameSettings.ambientOcclusion = 1;
			needsResave = true;
		}
		if (!gameSettings.fancyGraphics) {
			NoCubes.NO_CUBES_LOG.info("Fancy graphics were off. Ew, who plays with black leaves??? Just turned it on");
			gameSettings.fancyGraphics = true;
			needsResave = true;
		}
		if (needsResave) {
			gameSettings.saveOptions();
			gameSettings.loadOptions();
		}
	}

}
