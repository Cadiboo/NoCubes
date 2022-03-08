package io.github.cadiboo.nocubes.client.optifine;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public interface Reflector {

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
