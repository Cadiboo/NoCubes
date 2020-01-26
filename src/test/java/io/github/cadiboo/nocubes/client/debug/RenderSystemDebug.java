package io.github.cadiboo.nocubes.client.debug;

import com.mojang.blaze3d.platform.GlStateManager;

import java.lang.reflect.Field;

/**
 * Used for debugging issues with rendering behaving differently
 * than intended (i.e. like vanilla) due to different GL State
 *
 * @author Cadiboo
 */
public final class RenderSystemDebug {

	public static void printGlStateManager() throws IllegalAccessException {
		printClassFields(GlStateManager.class, null, 15, 0);
	}

	private static void printClassFields(final Class<GlStateManager> clazz, final Object instance, final int maxIterations, final int currentIteration) throws IllegalAccessException {
		final StringBuilder stringBuilder = new StringBuilder();
		appendClassFields(stringBuilder, clazz, instance, maxIterations, currentIteration);
		System.out.println(stringBuilder.toString());
	}

	public static void appendClassFields(StringBuilder stringBuilder, Class<?> clazz, Object instance, int maxIterations, int currentIteration) throws IllegalAccessException {
		if (maxIterations <= currentIteration)
			return;
		for (final Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			for (int i = 0; i < currentIteration; ++i)
				stringBuilder.append("  ");
			if (field.getType().isPrimitive())
				stringBuilder
						.append(field.getName())
						.append(": ")
						.append(field.get(instance))
						.append("\n");
			else {
				stringBuilder
						.append(field.getName())
						.append(": ")
						.append(field.getType().getSimpleName())
						.append("\n");
				appendClassFields(stringBuilder, field.getType(), field.get(instance), maxIterations, currentIteration + 1);
			}
		}
	}

}
