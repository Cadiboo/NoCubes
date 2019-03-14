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

package io.github.cadiboo.nocubes.util.reflect;

import com.google.common.base.Preconditions;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToAccessFieldException;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnknownConstructorException;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import static net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindClassException;
import static net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindFieldException;
import static net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

/**
 * Some reflection helper code.
 * <p>
 * Because I need to support Forge#2768 (where ObfuscationReflectionHelper didn't exist) I have a copy of the class
 *
 * @author cpw
 * @author Cadiboo
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ObfuscationReflectionHelperCopy {

	public static String remapFieldName(Class<?> clazz, String fieldName) {
		String internalClassName = FMLDeobfuscatingRemapper.INSTANCE.unmap(Type.getInternalName(clazz));
		return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(internalClassName, fieldName, null);
	}

	public static String remapMethodName(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) {
		String internalClassName = FMLDeobfuscatingRemapper.INSTANCE.unmap(Type.getInternalName(clazz));
		Type[] params = Arrays.stream(parameterTypes).map(Type::getType).toArray(Type[]::new);
		String desc = Type.getMethodDescriptor(Type.getType(returnType), params);
		return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(internalClassName, methodName, desc);
	}

	@SuppressWarnings("unchecked")
	public static <T, E> T getPrivateValue(Class<? super E> classToAccess, @Nullable E instance, String srgName) {
		final String fieldName = remapFieldName(classToAccess, srgName);
		try {
			return (T) findField(classToAccess, fieldName).get(instance);
		} catch (Exception e) {
			throw new UnableToAccessFieldException(e);
		}
	}

	public static <T, E> void setPrivateValue(Class<? super T> classToAccess, @Nullable T instance, @Nullable E value, String srgName) {
		final String fieldName = remapFieldName(classToAccess, srgName);
		try {
			findField(classToAccess, fieldName).set(instance, value);
		} catch (Exception e) {
			throw new UnableToAccessFieldException(e);
		}
	}

	/**
	 * Finds a field with the specified name in the given class and makes it accessible.
	 * Note: for performance, store the returned value and avoid calling this repeatedly.
	 * <p>
	 * Throws an exception if the field is not found.
	 *
	 * @param clazz   The class to find the field on.
	 * @param srgName The obfuscated name of the field to find.
	 * @return The field with the specified name in the given class.
	 */
	public static Field findField(Class<?> clazz, String srgName) {
		Preconditions.checkNotNull(clazz);
		Preconditions.checkArgument(StringUtils.isNotEmpty(srgName), "Field name cannot be empty");

		String nameToFind = remapFieldName(clazz, srgName);

		try {
			Field f = clazz.getDeclaredField(nameToFind);
			f.setAccessible(true);
			return f;
		} catch (Exception e) {
			throw new UnableToFindFieldException(e);
		}
	}

	/**
	 * Finds a method with the specified name and parameters in the given class and makes it accessible.
	 * Note: for performance, store the returned value and avoid calling this repeatedly.
	 * <p>
	 * Throws an exception if the method is not found.
	 *
	 * @param clazz          The class to find the method on.
	 * @param srgName        The obfuscated name of the method to find.
	 * @param returnType     The return type of the method to find.
	 * @param parameterTypes The parameter types of the method to find.
	 * @return The method with the specified name and type signature in the given class.
	 */
	public static Method findMethod(@Nonnull final Class<?> clazz, @Nonnull final String srgName, @Nonnull final Class<?> returnType, @Nullable final Class<?>... parameterTypes) {

		Preconditions.checkNotNull(clazz);
		Preconditions.checkArgument(StringUtils.isNotEmpty(srgName), "Method name cannot be empty");
		Preconditions.checkNotNull(returnType);

		final String nameToFind = remapMethodName(clazz, srgName, returnType, parameterTypes);

		try {
			Method m = clazz.getDeclaredMethod(nameToFind, parameterTypes);
			m.setAccessible(true);
			return m;
		} catch (Exception e) {
			throw new UnableToFindMethodException(e);
		}
	}

	/**
	 * Finds a constructor in the specified class that has matching parameter types.
	 *
	 * @param clazz          The class to find the constructor in
	 * @param parameterTypes The parameter types of the constructor.
	 * @param <T>            The type
	 * @return The constructor
	 * @throws NullPointerException        if {@code clazz} is null
	 * @throws NullPointerException        if {@code parameterTypes} is null
	 * @throws UnknownConstructorException if the constructor could not be found
	 */
	@Nonnull
	public static <T> Constructor<T> findConstructor(@Nonnull final Class<T> clazz, @Nonnull final Class<?>... parameterTypes) {
		return ReflectionHelper.findConstructor(clazz, parameterTypes);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public static Class<? super Object> getClass(@Nonnull final ClassLoader loader, @Nonnull final String className) {
		try {
			return (Class<? super Object>) Class.forName(className, false, loader);
		} catch (Exception e) {
			throw new UnableToFindClassException(new String[]{className}, e);
		}
	}

}
