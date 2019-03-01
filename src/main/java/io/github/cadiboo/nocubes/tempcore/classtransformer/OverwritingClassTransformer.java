package io.github.cadiboo.nocubes.tempcore.classtransformer;

import jdk.internal.org.objectweb.asm.Opcodes;
import net.minecraft.crash.CrashReport;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;

import static io.github.cadiboo.nocubes.tempcore.util.ObfuscationHelper.ObfuscationClass;
import static io.github.cadiboo.nocubes.tempcore.util.ObfuscationHelper.ObfuscationMethod.RENDER_CHUNK_REBUILD_CHUNK;
import static io.github.cadiboo.nocubes.tempcore.util.ObfuscationHelper.ObfuscationMethod.RENDER_CHUNK_RESORT_TRANSPARENCY;

/**
 * @author Cadiboo
 * @see "http://www.egtry.com/java/bytecode/asm/tree_transform"
 */
// useful links:
// https://text-compare.com
// http://www.minecraftforge.net/forum/topic/32600-1710-strange-error-with-custom-event-amp-event-handler/?do=findComment&comment=172480
public final class OverwritingClassTransformer implements IClassTransformer, Opcodes {

	private static final int CLASS_WRITER_FLAGS = ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES;
	// skip class reader reading frames if the class writer is going to compute them for us (if it is you should get a warning that this being 0 is dead code)
	private static final int CLASS_READER_FLAGS = (CLASS_WRITER_FLAGS & ClassWriter.COMPUTE_FRAMES) == ClassWriter.COMPUTE_FRAMES ? ClassReader.SKIP_FRAMES : 0;

	private static final Logger LOGGER = LogManager.getLogger();
	private static final Logger LOGGER_MINIFED = LogManager.getLogger("RCRCHClassTransformer");

	public static boolean DEBUG_EVERYTHING = false;

	public static boolean DEBUG_DUMP_BYTECODE = false;
	public static String DEBUG_DUMP_BYTECODE_DIR = null;

	public static boolean DEBUG_CLASSES = false;
	public static boolean DEBUG_TYPES = false;
	public static boolean DEBUG_STACKS = false;
	public static boolean DEBUG_METHODS = false;
	public static boolean DEBUG_INSTRUCTIONS = false;

	private static final Printer PRINTER = new Textifier();
	private static final TraceMethodVisitor TRACE_METHOD_VISITOR = new TraceMethodVisitor(PRINTER);
//	static {
//		if (DEBUG_STACKS) {
//			for (final Field field : IStacks.class.getFields()) {
//				Object value;
//				try {
//					value = field.get(IStacks.class);
//
//					LOGGER.info(field.getName() + ": " + value);
//
//				} catch (IllegalArgumentException | IllegalAccessException e) {
//					LOGGER_MINIFED.error("Error logging stacks!", e);
//				}
//			}
//		}
//
//	}

	public static String insnToString(final AbstractInsnNode insn) {
		insn.accept(TRACE_METHOD_VISITOR);
		final StringWriter sw = new StringWriter();
		PRINTER.print(new PrintWriter(sw));
		PRINTER.getText().clear();
		return sw.toString().trim();
	}

	@Override
	public byte[] transform(final String unTransformedName, final String transformedName, final byte[] basicClass) {

		if (DEBUG_CLASSES) {
			if ((unTransformedName.startsWith("b") || unTransformedName.startsWith("net.minecraft.client.renderer.chunk.")) || (transformedName.startsWith("b") || transformedName.startsWith("net.minecraft.client.renderer.chunk."))) {
				LOGGER.info("unTransformedName: " + unTransformedName + ", transformedName: " + transformedName + ", unTransformedName equals: " + unTransformedName.equals(ObfuscationClass.RENDER_CHUNK.getClassName()) + ", transformedName equals: " + transformedName.equals(ObfuscationClass.RENDER_CHUNK.getClassName()));
			}
		}

		if (!transformedName.equals(ObfuscationClass.RENDER_CHUNK.getClassName())) {
			return basicClass;
		}

//		if (DEBUG_DUMP_BYTECODE) {
//			try {
//				Preconditions.checkNotNull(DEBUG_DUMP_BYTECODE_DIR, "debug dump bytecode dir before");
//				final Path pathToFile = Paths.get(DEBUG_DUMP_BYTECODE_DIR + "before_hooks.txt");
//				pathToFile.toFile().getParentFile().mkdirs();
//				final PrintWriter filePrinter = new PrintWriter(pathToFile.toFile());
//				final ClassReader reader = new ClassReader(basicClass);
//				final TraceClassVisitor tracingVisitor = new TraceClassVisitor(filePrinter);
//				reader.accept(tracingVisitor, 0);
//
//				final Path pathToClass = Paths.get(DEBUG_DUMP_BYTECODE_DIR + "before_hooks.class");
//				pathToClass.toFile().getParentFile().mkdirs();
//				final FileOutputStream fileOutputStream = new FileOutputStream(pathToClass.toFile());
//				fileOutputStream.write(basicClass);
//				fileOutputStream.close();
//			} catch (final Exception e) {
//				LOGGER_MINIFED.error("Failed to dump bytecode of classes before injecting hooks!", e);
//			}
//		}

		LOGGER.info("Preparing to inject hooks into \"" + transformedName + "\" (RenderChunk)");

		// Build classNode & get instruction list
		final ClassNode classNode = new ClassNode();
		final ClassReader cr = new ClassReader(basicClass);
		cr.accept(classNode, CLASS_READER_FLAGS);

		if (DEBUG_TYPES) {
			LOGGER.info("RebuildChunk type: " + RENDER_CHUNK_REBUILD_CHUNK.getType());
			LOGGER.info("RebuildChunk descriptor: " + RENDER_CHUNK_REBUILD_CHUNK.getDescriptor());
		}

		for (final MethodNode method : classNode.methods) {            //TODO RENDER_CHUNK_REBUILD_CHUNK.matches()

			if (!method.desc.equals(RENDER_CHUNK_REBUILD_CHUNK.getDescriptor())) {
				if (DEBUG_METHODS) {
					LOGGER.info("Method with name \"" + method.name + "\" and description \"" + method.desc + "\" did not match");
				}
				continue;
			}

			if (DEBUG_METHODS) {
				LOGGER.info("Method with name \"" + method.name + "\" and description \"" + method.desc + "\" matched!");
			}

			// make sure not to overwrite resortTransparency (it has the same description but it's name is "a" while rebuildChunk's name is "b")
			if (method.name.equals(RENDER_CHUNK_RESORT_TRANSPARENCY.getName())) {
				if (DEBUG_METHODS) {
					LOGGER.info("Method with name \"" + method.name + "\" and description \"" + method.desc + "\" was rejected");
				}
				continue;
			}

			if (DEBUG_METHODS) {
				LOGGER.info("Method with name \"" + method.name + "\" and description \"" + method.desc + "\" matched and passed");
			}

			this.injectHooks(method.instructions);

		}

		// write classNode
		try {
			final ClassWriter out = new ClassWriter(CLASS_WRITER_FLAGS);

			// make the ClassWriter visit all the code in classNode
			classNode.accept(out);

			LOGGER.info("Injected hooks successfully!");

//			if (DEBUG_DUMP_BYTECODE) {
//				try {
//					Preconditions.checkNotNull(DEBUG_DUMP_BYTECODE_DIR, "debug dump bytecode dir after");
//					final byte[] bytes = out.toByteArray();
//
//					final Path pathToFile = Paths.get(DEBUG_DUMP_BYTECODE_DIR + "after_hooks.txt");
//					pathToFile.toFile().getParentFile().mkdirs();
//					final PrintWriter filePrinter = new PrintWriter(pathToFile.toFile());
//					final ClassReader reader = new ClassReader(bytes);
//					final TraceClassVisitor tracingVisitor = new TraceClassVisitor(filePrinter);
//					reader.accept(tracingVisitor, 0);
//
//					final Path pathToClass = Paths.get(DEBUG_DUMP_BYTECODE_DIR + "after_hooks.class");
//					pathToClass.toFile().getParentFile().mkdirs();
//					final FileOutputStream fileOutputStream = new FileOutputStream(pathToClass.toFile());
//					fileOutputStream.write(bytes);
//					fileOutputStream.close();
//				} catch (final Exception e) {
//					LOGGER_MINIFED.error("Failed to dump bytecode of classes after injecting hooks!", e);
//				}
//			}

			return out.toByteArray();
		} catch (final Exception e) {
			final CrashReport crashReport = new CrashReport("Error injecting hooks!", e);
			crashReport.makeCategory("Injecting hooks into RenderChunk#rebuildChunk");
			throw new ReportedException(crashReport);
		}

	}

	public void injectHooks(InsnList instructions) {

//		AbstractInsnNode NEW_CompiledChunk = null;
//
//		for (AbstractInsnNode instruction : instructions.toArray()) {
//			if (instruction.getOpcode() == Opcodes.NEW) {
//				NEW_CompiledChunk = instruction;
//				LOGGER.info("Found injection point " + instruction);
//				break;
//			}
//
//		}
//
//		if (NEW_CompiledChunk == null) {
//			LOGGER.info("Error: Couldn't find injection point!");
//			return;
//		}
//
//		int ALOAD_this = 0;
//		int FLOAD_x = 1;
//		int FLOAD_y = 2;
//		int FLOAD_z = 3;
//		int ALOAD_generator = 4;
//
//		instructions.insertBefore(NEW_CompiledChunk, new VarInsnNode(ALOAD, ALOAD_this)); // this
//		LOGGER.info("Injected instruction ALOAD this");
//		instructions.insertBefore(NEW_CompiledChunk, new VarInsnNode(FLOAD, FLOAD_x)); // x
//		LOGGER.info("Injected instruction FLOAD x");
//		instructions.insertBefore(NEW_CompiledChunk, new VarInsnNode(FLOAD, FLOAD_y)); // y
//		LOGGER.info("Injected instruction FLOAD y");
//		instructions.insertBefore(NEW_CompiledChunk, new VarInsnNode(FLOAD, FLOAD_z)); // z
//		LOGGER.info("Injected instruction FLOAD z");
//		instructions.insertBefore(NEW_CompiledChunk, new VarInsnNode(ALOAD, ALOAD_generator)); // generator
//		LOGGER.info("Injected instruction ALOAD generator");
//		instructions.insertBefore(NEW_CompiledChunk,
//				new MethodInsnNode(
//						//int opcode
//						INVOKESTATIC,
//						//String owner
//						"io/github/cadiboo/nocubes/tempcore/OverwriteHookTemp",
//						//String name
//						"rebuildChunk",
//						//String descriptor
//						"(Lnet/minecraft/client/renderer/chunk/RenderChunk;FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V",
//						//boolean isInterface
//						false
//				)
//		);
//		LOGGER.info("Injected instruction INVOKESTATIC io/github/cadiboo/nocubes/tempcore/OverwriteHookTemp rebuildChunk (Lnet/minecraft/client/renderer/chunk/RenderChunk;FFFLnet/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator;)V false");
//		instructions.insertBefore(NEW_CompiledChunk, new InsnNode(RETURN));
//		LOGGER.info("Injected instruction RETURN");
//
//		LOGGER.info("Successfully inserted instructions!");

	}

}
