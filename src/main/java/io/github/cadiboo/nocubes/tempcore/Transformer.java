package io.github.cadiboo.nocubes.tempcore;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Consumer;

import static net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindMethodException;

/**
 * @author Cadiboo
 */
public class Transformer implements IClassTransformer, Opcodes {

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
		final String methodName = ObfuscationHelper.remapName(srgName);
		for (final MethodNode method : classNode.methods) {
			if (method.name.equals(methodName)) {
				return method;
			}
		}
		StringBuilder names = new StringBuilder();
		for (MethodNode methodNode : classNode.methods) {
			names.append(methodNode.name).append(" | ").append(methodNode.desc).append("\n");
		}
		throw new UnableToFindMethodException(new Exception(srgName + " does not exist!", new Exception(names.toString())));
	}

	public static void redirect_addCollisionBoxToList(final ClassNode classNode) {

		final MethodNode addCollisionBoxToList = getMethod(classNode, "func_185908_a");
		final InsnList instructions = addCollisionBoxToList.instructions;
		final ListIterator<AbstractInsnNode> iterator = instructions.iterator();
		VarInsnNode ALOAD_0 = null;
		while (iterator.hasNext()) {
			final AbstractInsnNode insn = iterator.next();
			if (insn.getOpcode() != ALOAD) {
				continue;
			}
			if (insn.getType() != AbstractInsnNode.VAR_INSN) {
				continue;
			}
			if (((VarInsnNode) insn).var != 0) {
				continue;
			}
			ALOAD_0 = (VarInsnNode) insn;
			break;
		}

		if (ALOAD_0 == null) {
			return;
		}

//		L0
//		LINENUMBER 463 L0
//>		ALOAD 0
//>		ALOAD 1
//>		ALOAD 2
//>		ALOAD 3
//>		ALOAD 4
//>		ALOAD 5
//>		ILOAD 6
//>		INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.addCollisionBoxToList (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
//#		ALOAD 0
//		GETFIELD net/minecraft/block/state/BlockStateContainer$StateImplementation.block : Lnet/minecraft/block/Block;
//		ALOAD 0
//		ALOAD 1
//		ALOAD 2
//		ALOAD 3
//		ALOAD 4
//		ALOAD 5
//		ILOAD 6
//		INVOKEVIRTUAL net/minecraft/block/Block.addCollisionBoxToList (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V
//				L1
//		LINENUMBER 464 L1
//				RETURN

		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 0));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 1));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 2));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 3));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 4));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 5));
		instructions.insertBefore(ALOAD_0, new VarInsnNode(ALOAD, 6));
		instructions.insertBefore(ALOAD_0, new MethodInsnNode(
				INVOKESTATIC,
				"io/github/cadiboo/nocubes/hooks/Hooks",
				"addCollisionBoxToList",
				"(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lnet/minecraft/entity/Entity;Z)V",
				false
		));

	}

	//Coppied from 1.13 ObfuscationReflectionHelper

	private static class ObfuscationHelper {

		private static final Logger LOGGER = LogManager.getLogger();
		private static final Marker REFLECTION = MarkerManager.getMarker("REFLECTION");
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
					URL path = ClassLoader.getSystemResource(file); //We EXPLICITLY go through the SystemClassLoader here because this is dev-time only. Therefore we will be on the root classpath.
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
