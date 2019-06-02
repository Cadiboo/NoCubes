package io.github.cadiboo.nocubes.tempcore;

import io.github.cadiboo.nocubes.tempcore.classwriter.MCWriter;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import javax.annotation.Nullable;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static io.github.cadiboo.nocubes.tempcore.NoCubesLoadingPlugin.DUMP_BYTECODE_DIR;
import static io.github.cadiboo.nocubes.tempcore.NoCubesLoadingPlugin.MOD_LOCATION;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

/**
 * @author Cadiboo
 */
public final class NoCubesClassTransformer implements IClassTransformer, Opcodes {

	private static final Logger LOGGER = LogManager.getLogger();
	private static String currentlyRunning;
	private static String transformerName;
	private static boolean DUMP_BYTECODE = true;
	static {
		DUMP_BYTECODE &= (MOD_LOCATION == null || !MOD_LOCATION.isFile() || !MOD_LOCATION.getName().endsWith(".jar"));
	}

	static void start(String name) {
		log("Starting " + name);
		currentlyRunning = name;
	}

	static void finish() {
		String name = currentlyRunning;
		currentlyRunning = null;
		log("Finished " + name);
	}

	static void log(String msg) {
		if (currentlyRunning == null) {
			print("[" + transformerName + "]: " + msg);
		} else {
			print("[" + transformerName + "] [" + currentlyRunning + "]: " + msg);
		}
	}

	private static void print(final String s) {
		LOGGER.info(s);
	}

	static String mapMethod(final String internalClassName, final String srgName, final String desc) {
		return ObfuscationHelper.remapMethodName(internalClassName, srgName, desc);
	}

	static String mapField(final String internalClassName, final String srgName) {
		return ObfuscationHelper.remapFieldName(internalClassName, srgName);
	}

	@Override
	public byte[] transform(final String name, final String transformedName, @Nullable final byte[] basicClass) {
		if (basicClass == null) {
			return null;
		}
		switch (transformedName) {
			case "net.minecraft.block.state.BlockStateContainer$StateImplementation":
				return transformClass(basicClass, transformedName,
						StateImplementationTransformer::transform
				);
			case "net.minecraft.block.state.IBlockProperties":
				return transformClass(basicClass, transformedName,
						IBlockPropertiesTransformer::transform
				);
			case "net.minecraft.client.renderer.chunk.RenderChunk":
				return transformClass(basicClass, transformedName,
						RenderChunkTransformer::transform
				);
			case "net.minecraft.world.World":
				return transformClass(basicClass, transformedName,
						WorldTransformer::transform
				);
			case "net.minecraft.client.renderer.RenderGlobal":
				return transformClass(basicClass, transformedName,
						RenderGlobalTransformer::transform
				);
		}
		return basicClass;
	}

	@SafeVarargs
	private final byte[] transformClass(final byte[] basicClass, final String transformedName, final Consumer<ClassNode>... classNodeAcceptors) {
		ClassNode classNode = new ClassNode();
		ClassReader cr = new ClassReader(basicClass);
		LOGGER.info("Starting transforming " + transformedName);
		if (DUMP_BYTECODE) try {
			Path pathToFile = Paths.get(DUMP_BYTECODE_DIR + transformedName + "_before_hooks.txt");
			pathToFile.toFile().getParentFile().mkdirs();
			PrintWriter filePrinter = new PrintWriter(pathToFile.toFile());
			ClassReader reader = new ClassReader(basicClass);
			TraceClassVisitor tracingVisitor = new TraceClassVisitor(filePrinter);
			reader.accept(tracingVisitor, 0);
			pathToFile = Paths.get(DUMP_BYTECODE_DIR + transformedName + "_before_hooks.class");
			pathToFile.toFile().getParentFile().mkdirs();
			FileOutputStream fileOutputStream = new FileOutputStream(pathToFile.toFile());
			fileOutputStream.write(basicClass);
			fileOutputStream.close();
		} catch (Exception e) {
			LogManager.getLogger().error("Failed to dump bytecode of classes before injecting hooks!", e);
		}
		cr.accept(classNode, SKIP_FRAMES); // We compute frames in the class writer
		for (final Consumer<ClassNode> classNodeAcceptor : classNodeAcceptors) {
			transformerName = classNodeAcceptor.getClass().getSimpleName();
			classNodeAcceptor.accept(classNode);
		}
		LOGGER.info("Finished transforming " + transformedName);
		ClassWriter out = new MCWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
		classNode.accept(out);
		if (DUMP_BYTECODE) {
			try {
				byte[] bytes = out.toByteArray();
				Path pathToFile = Paths.get(DUMP_BYTECODE_DIR + transformedName + "_after_hooks.txt");
				pathToFile.toFile().getParentFile().mkdirs();
				PrintWriter filePrinter = new PrintWriter(pathToFile.toFile());
				ClassReader reader = new ClassReader(bytes);
				TraceClassVisitor tracingVisitor = new TraceClassVisitor(filePrinter);
				reader.accept(tracingVisitor, 0);
				Path pathToClass = Paths.get(DUMP_BYTECODE_DIR + transformedName + "_after_hooks.class");
				pathToClass.toFile().getParentFile().mkdirs();
				FileOutputStream fileOutputStream = new FileOutputStream(pathToClass.toFile());
				fileOutputStream.write(bytes);
				fileOutputStream.close();
			} catch (Exception e) {
				LogManager.getLogger().error("Failed to dump bytecode of classes after injecting hooks!", e);
			}
		}
		return out.toByteArray();
	}

	// Copied from ObfuscationReflectionHelper
	static class ObfuscationHelper {

		static String remapMethodName(final String internalClassName, final String methodName, final String desc) {
			final String remappedName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(internalClassName, methodName, desc);
			LOGGER.info("Remapped method name " + methodName + " to " + remappedName);
			return remappedName;
		}

		static String remapFieldName(final String internalClassName, final String fieldName) {
			final String remappedName = FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(internalClassName, fieldName, null);
			LOGGER.info("Remapped field name " + fieldName + " to " + remappedName);
			return remappedName;
		}

	}

}
