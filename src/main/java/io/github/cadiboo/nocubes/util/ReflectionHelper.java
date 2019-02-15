/*
 * Minecraft Forge
 * Copyright (c) 2016-2018.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package io.github.cadiboo.nocubes.util;

import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper.UnknownConstructorException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Some reflection helper code.
 *
 * @author cpw
 */
public class ReflectionHelper {

	public static Method findMethod(@Nonnull final Class<?> clazz, @Nonnull final String methodName, @Nullable final Class<?>... parameterTypes) {
		return ObfuscationReflectionHelper.findMethod(clazz, methodName, parameterTypes);
	}

	public static class UnableToFindMethodException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		//private String[] methodNames;

		public UnableToFindMethodException(String[] methodNames, Exception failed) {
			super(failed);
			//this.methodNames = methodNames;
		}

		public UnableToFindMethodException(Throwable failed) {
			super(failed);
		}

	}

	public static class UnableToFindClassException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		//private String[] classNames;

		public UnableToFindClassException(String[] classNames, @Nullable Exception err) {
			super(err);
			//this.classNames = classNames;
		}

	}

	public static class UnableToAccessFieldException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		//private String[] fieldNameList;

		public UnableToAccessFieldException(String[] fieldNames, Exception e) {
			super(e);
			//this.fieldNameList = fieldNames;
		}

	}

	public static class UnableToFindFieldException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		//private String[] fieldNameList;
		public UnableToFindFieldException(String[] fieldNameList, Exception e) {
			super(e);
			//this.fieldNameList = fieldNameList;
		}

	}

	public static Field findField(Class<?> clazz, String... fieldNames) {
		Exception failed = null;
		for (String fieldName : fieldNames) {
			try {
				Field f = clazz.getDeclaredField(fieldName);
				f.setAccessible(true);
				return f;
			} catch (Exception e) {
				failed = e;
			}
		}
		throw new UnableToFindFieldException(fieldNames, failed);
	}

	@SuppressWarnings("unchecked")
	public static <T, E> T getPrivateValue(Class<? super E> classToAccess, @Nullable E instance, int fieldIndex) {
		try {
			Field f = classToAccess.getDeclaredFields()[fieldIndex];
			f.setAccessible(true);
			return (T) f.get(instance);
		} catch (Exception e) {
			throw new UnableToAccessFieldException(new String[0], e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T, E> T getPrivateValue(Class<? super E> classToAccess, E instance, String... fieldNames) {
		try {
			return (T) findField(classToAccess, fieldNames).get(instance);
		} catch (Exception e) {
			throw new UnableToAccessFieldException(fieldNames, e);
		}
	}

	public static <T, E> void setPrivateValue(Class<? super T> classToAccess, T instance, E value, int fieldIndex) {
		try {
			Field f = classToAccess.getDeclaredFields()[fieldIndex];
			f.setAccessible(true);
			f.set(instance, value);
		} catch (Exception e) {
			throw new UnableToAccessFieldException(new String[0], e);
		}
	}

	public static <T, E> void setPrivateValue(Class<? super T> classToAccess, T instance, E value, String... fieldNames) {
		try {
			findField(classToAccess, fieldNames).set(instance, value);
		} catch (Exception e) {
			throw new UnableToAccessFieldException(fieldNames, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static Class<? super Object> getClass(ClassLoader loader, String... classNames) {
		Exception err = null;
		for (String className : classNames) {
			try {
				return (Class<? super Object>) Class.forName(className, false, loader);
			} catch (Exception e) {
				err = e;
			}
		}

		throw new UnableToFindClassException(classNames, err);
	}

	/**
	 * Finds a constructor in the specified class that has matching parameter types.
	 *
	 * @param klass          The class to find the constructor in
	 * @param parameterTypes The parameter types of the constructor.
	 * @param <T>            The type
	 * @return The constructor
	 * @throws NullPointerException        if {@code klass} is null
	 * @throws NullPointerException        if {@code parameterTypes} is null
	 * @throws UnknownConstructorException if the constructor could not be found
	 */
	@Nonnull
	public static <T> Constructor<T> findConstructor(@Nonnull final Class<T> klass, @Nonnull final Class<?>... parameterTypes) {
		Preconditions.checkNotNull(klass, "class");
		Preconditions.checkNotNull(parameterTypes, "parameter types");

		final Constructor<T> constructor;
		try {
			constructor = klass.getDeclaredConstructor(parameterTypes);
			constructor.setAccessible(true);
		} catch (final NoSuchMethodException e) {
			final StringBuilder desc = new StringBuilder();
			desc.append(klass.getSimpleName()).append('(');
			for (int i = 0, length = parameterTypes.length; i < length; i++) {
				desc.append(parameterTypes[i].getName());
				if (i > length) {
					desc.append(',').append(' ');
				}
			}
			desc.append(')');
			throw new UnknownConstructorException("Could not find constructor '" + desc.toString() + "' in " + klass);
		}
		return constructor;
	}

}
