package io.github.cadiboo.nocubes.client.optifine;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface Reflector {

	static MethodHandle getMethod(String clazz, String name, Object... paramClasses) {
		var method = tryGetMethod(clazz, name, paramClasses);
		if (method != null)
			return method;
		throw new NullPointerException(String.format("Could not find method '%s' on class '%s'.", name, clazz));
	}

	static Field getField(String clazz, String name) {
		var field = tryGetField(clazz, name);
		if (field != null)
			return field;
		throw new NullPointerException(String.format("Could not find field '%s' on class '%s'.", name, clazz));
	}

	static @Nullable MethodHandle tryGetMethod(String clazz, String name, Object... paramClasses) {
		try {
			var lookup = MethodHandles.publicLookup();
			var klass = Class.forName(clazz);
			var params = new Class[paramClasses.length];
			for (int i = 0; i < paramClasses.length; i++) {
				var param = paramClasses[i];
				params[i] = param instanceof Class<?> ? (Class<?>) param : Class.forName((String) param);
			}
			var method = klass.getDeclaredMethod(name, params);
			method.setAccessible(true);
			return lookup.unreflect(method);
		} catch (Exception e) {
			return null;
		}
	}

	static @Nullable Field tryGetField(String clazz, String name) {
		try {
			var klass = Class.forName(clazz);
			var field = klass.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			return null;
		}
	}

}
