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

import javax.annotation.Nonnull;

import static net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindClassException;

public class ReflectionClassHelper {

	@SuppressWarnings("unchecked")
	@Nonnull
	public static Class<? super Object> findClass(@Nonnull final ClassLoader loader, @Nonnull final String className) {
		try {
			return (Class<? super Object>) Class.forName(className, false, loader);
		} catch (Exception e) {
			throw new UnableToFindClassException(new String[]{className}, e);
		}
	}

}
