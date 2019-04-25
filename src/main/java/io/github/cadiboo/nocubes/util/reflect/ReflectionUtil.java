package io.github.cadiboo.nocubes.util.reflect;

import com.google.common.base.Preconditions;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.ReportedException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;

/**
 * @author Cadiboo
 */
public final class ReflectionUtil {

	@Nonnull
	public static Field getField(@Nonnull final Class<?> clazz, @Nonnull final String fieldName) throws NoSuchFieldException {
		Preconditions.checkNotNull(clazz);
		Preconditions.checkArgument(StringUtils.isNotEmpty(fieldName), "Field name cannot be empty");
		final Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field;
	}

	@Nullable
	public static Field getFieldOrNull(@Nonnull final Class<?> clazz, @Nonnull final String fieldName) {
		try {
			return getField(clazz, fieldName);
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	@Nonnull
	public static Field getFieldOrCrash(@Nonnull final Class<?> clazz, @Nonnull final String fieldName) {
		try {
			return getField(clazz, fieldName);
		} catch (NoSuchFieldException e) {
			final CrashReport crashReport = new CrashReport("Unable to find field \"" + fieldName + "\" for class \"" + clazz + "\". Field does not exist!", e);
			crashReport.makeCategory("Finding Field");
			throw new ReportedException(crashReport);
		}
	}

	@Nonnull
	public static Class<?> getClass(@Nonnull final ClassLoader loader, @Nonnull final String className) throws ClassNotFoundException {
		return Class.forName(className, false, loader);
	}

}
