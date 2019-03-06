package io.github.cadiboo.nocubes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Cadiboo
 */
public final class AmbientOcclusionHelper {

	private static final Constructor<BlockModelRenderer.AmbientOcclusionFace> ambientOcclusionFace;
	private static final boolean ambientOcclusionFaceNeedsBlockModelRenderer;
	static {
		Constructor<BlockModelRenderer.AmbientOcclusionFace> ambientOcclusionFaceConstructor = null;
		boolean needsBlockModelRenderer = false;
		try {
			try {
				//TODO: stop using ReflectionHelper
				ambientOcclusionFaceConstructor = ReflectionHelper.findConstructor(BlockModelRenderer.AmbientOcclusionFace.class);
			} catch (ReflectionHelper.UnknownConstructorException e) {
				//TODO: stop using ReflectionHelper
				ambientOcclusionFaceConstructor = ReflectionHelper.findConstructor(BlockModelRenderer.AmbientOcclusionFace.class, BlockModelRenderer.class);
				needsBlockModelRenderer = true;
			}
		} catch (Exception e) {
			final CrashReport crashReport = new CrashReport("Unable to find constructor for BlockModelRenderer$AmbientOcclusionFace", e);
			crashReport.makeCategory("Finding Constructor");
			throw new ReportedException(crashReport);
		}
		ambientOcclusionFace = ambientOcclusionFaceConstructor;
		ambientOcclusionFaceNeedsBlockModelRenderer = needsBlockModelRenderer;
	}

	public static BlockModelRenderer.AmbientOcclusionFace makeAmbientOcclusionFace() {
		try {
			if (ambientOcclusionFaceNeedsBlockModelRenderer) {
				return ambientOcclusionFace.newInstance(Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer());
			} else {
				return ambientOcclusionFace.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			CrashReport crashReport = new CrashReport("Instantiating BlockModelRenderer$AmbientOcclusionFace!", e);
			final CrashReportCategory crashReportCategory = crashReport.makeCategory("Reflectively Accessing BlockModelRenderer$AmbientOcclusionFace");
			crashReportCategory.addCrashSection("Needs BlockModelRenderer", ambientOcclusionFaceNeedsBlockModelRenderer);
			throw new ReportedException(crashReport);
		}
	}

}
