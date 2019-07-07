package io.github.cadiboo.nocubes.tempcore.classwriter;

import io.github.cadiboo.nocubes.tempcore.NoCubesLoadingPlugin;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;

public final class ObfHelper {

	public static Boolean obfuscated = null;

	/**
	 * @return Whether or not the current environment contains obfuscated Minecraft code
	 */
	public static boolean isObfuscated() {
		if (obfuscated == null) {
			obfuscated = !NoCubesLoadingPlugin.DEVELOPER_ENVIRONMENT;
		}
		return obfuscated;
	}

	/**
	 * Deobfuscates an obfuscated class name if {@link #isObfuscated()}.
	 */
	public static String toDeobfClassName(String obfClassName) {
		if (!isObfuscated())
			return forceToDeobfClassName(obfClassName);
		else
			return obfClassName.replace('.', '/');
	}

	/**
	 * Obfuscates a deobfuscated class name if {@link #isObfuscated()}.
	 */
	public static String toObfClassName(String deobfClassName) {
		if (isObfuscated())
			return forceToObfClassName(deobfClassName);
		else
			return deobfClassName.replace('.', '/');
	}

	/**
	 * Deobfuscates an obfuscated class name regardless of {@link #isObfuscated()}.
	 */
	public static String forceToDeobfClassName(String obfClassName) {
		return FMLDeobfuscatingRemapper.INSTANCE.map(obfClassName.replace('.', '/'));
	}

	/**
	 * Obfuscates a deobfuscated class name regardless of {@link #isObfuscated()}.
	 */
	public static String forceToObfClassName(String deobfClassName) {
		return FMLDeobfuscatingRemapper.INSTANCE.unmap(deobfClassName.replace('.', '/'));
	}

}
