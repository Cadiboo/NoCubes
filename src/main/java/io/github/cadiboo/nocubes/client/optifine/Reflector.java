package io.github.cadiboo.nocubes.client.optifine;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface Reflector {

	static @Nullable MethodHandle tryGetMethod(String clazz, String name, Object... paramClasses) {
		try {
			MethodHandles.Lookup lookup = MethodHandles.publicLookup();
			Class<?> klass = Class.forName(clazz);
			Class[] params = new Class[paramClasses.length];
			for (int i = 0; i < paramClasses.length; i++) {
				Object param = paramClasses[i];
				params[i] = param instanceof Class<?> ? (Class<?>) param : Class.forName((String) param);
			}
			Method method = klass.getDeclaredMethod(name, params);
			method.setAccessible(true);
			return lookup.unreflect(method);
		} catch (Exception e) {
			return null;
		}
	}

	static @Nullable Field tryGetField(String clazz, String name) {
		try {
			Class<?> klass = Class.forName(clazz);
			Field field = klass.getDeclaredField(name);
			field.setAccessible(true);
			return field;
		} catch (Exception e) {
			return null;
		}
	}

}
