package io.github.cadiboo.nocubes.tempcore;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Cadiboo
 */
public class Transformer implements IClassTransformer {

	@Override
	public byte[] transform(final String name, final String transformedName, final byte[] basicClass) {
		if (transformedName.equals("net.minecraft.block.state.IBlockState")) {
			return transformClass(basicClass, Transformer::redirect_addCollisionBoxToList);
		}

		return basicClass;
	}

	private byte[] transformClass(final byte[] basicClass, final Consumer<ClassNode> classNodeAcceptor) {
		ClassNode classNode = new ClassNode();
		ClassReader cr = new ClassReader(basicClass);
		cr.accept(classNode, 4);
		classNodeAcceptor.accept(classNode);
		ClassWriter out = new ClassWriter(3);
		classNode.accept(out);
		return out.toByteArray();
	}

	private static MethodNode getMethod(ClassNode classNode, String srgName) {
		final String methodName = ObfuscationReflectionHelper.remapFieldNames()
		for (final MethodNode method : classNode.methods) {

		}
	}

	public static void redirect_addCollisionBoxToList(final ClassNode classNode) {

		classNode

	}

	//Coppied from 1.13 ObfuscationReflectionHelper

	private static class ObfuscationHelper {

		private static final Map<String, String> map = new HashMap<>();
		private static boolean loaded = false;

		public static String remapName(String name) {
			loadMappings();
			if (map.isEmpty())
				return name;
			return map.getOrDefault(name, name);
		}

		private static void loadMappings() {
			if (loaded)
				return;

			synchronized (map) //Just in case?
			{
				if (loaded) //Incase something else loaded while we were here, jump out
					return;
				for (String file : new String[]{"fields.csv", "methods.csv"}) {
					URL path = ClassLoader.getSystemResource(file); //We EXPLICITLY go through the SystemClassLoader here because this is dev-time only. And will be on the root classpath.
					if (path == null)
						continue;

					int count = map.size();
					LOGGER.info(REFLECTION, "Loading Mappings: {}", path);
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(path.openStream()))) {
						reader.lines().skip(1).map(e -> e.split(",")).forEach(e -> map.put(e[0], e[1]));
					} catch (IOException e1) {
						LOGGER.error(REFLECTION, "Error reading mappings", e1);
					}
					LOGGER.info(REFLECTION, "Loaded {} entries", map.size() - count);
				}
				loaded = true;
			}
		}

	}

}
