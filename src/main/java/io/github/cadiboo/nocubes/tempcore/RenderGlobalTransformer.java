package io.github.cadiboo.nocubes.tempcore;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.List;

import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.finish;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.log;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.mapField;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.mapMethod;
import static io.github.cadiboo.nocubes.tempcore.NoCubesClassTransformer.start;

/**
 * @author Cadiboo
 */
final class RenderGlobalTransformer implements Opcodes {

	// Local variable indexes
	private static final int ALOCALVARIABLE_this = 0;
	private static final int ALOCALVARIABLE_tessellator = 1;
	private static final int ALOCALVARIABLE_bufferbuilder = 2;
	private static final int ALOCALVARIABLE_blockpos = 13;
	private static final int ALOCALVARIABLE_iblockstate = 23;
	private static final int ALOCALVARIABLE_textureatlassprite = 25;
	private static final int ALOCALVARIABLE_blockrendererdispatcher = 26;

	static void transform(final ClassNode classNode) {

		final List<MethodNode> methods = classNode.methods;

		//drawBlockDamageTexture
		{
			final String targetMethodDesc = "(Lnet/minecraft/client/renderer/Tessellator;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/entity/Entity;F)V";
			final String targetMethodName = mapMethod("net/minecraft/client/renderer/RenderGlobal", "func_174981_a", targetMethodDesc);

			start("Find " + targetMethodName);
			for (final MethodNode method : methods) {

				if (!method.name.equals(targetMethodName)) {
					log("Did not match method name " + targetMethodName + " - " + method.name);
					continue;
				} else if (!method.desc.equals(targetMethodDesc)) {
					log("Did not match method desc " + targetMethodDesc + " - " + method.desc);
					continue;
				}
				log("Matched method " + method.name + " " + method.desc);

				finish();

				start("Inject drawBlockDamageTexture hook");
				injectDrawBlockDamageTextureHook(method.instructions);
				finish();
				break;

			}
		}

	}

	// 1) find renderBlockDamage
// 2) find next label
// 3) insert right after renderBlockDamage label
	private static void injectDrawBlockDamageTextureHook(InsnList instructions) {

//	BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
//	blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, this.world);

//	BlockRendererDispatcher blockrendererdispatcher = this.mc.getBlockRendererDispatcher();
//	// NoCubes Start
//	if(io.github.cadiboo.nocubes.hooks.Hooks.renderBlockDamage(tessellatorIn, bufferBuilderIn, blockpos, iblockstate, this.world, textureatlassprite, blockrendererdispatcher))
//	// NoCubes End
//	blockrendererdispatcher.renderBlockDamage(iblockstate, blockpos, textureatlassprite, this.world);

//   L35
//    LINENUMBER 1586 L35
//    ALOAD 0
//    GETFIELD net/minecraft/client/renderer/RenderGlobal.mc : Lnet/minecraft/client/Minecraft;
//    INVOKEVIRTUAL net/minecraft/client/Minecraft.getBlockRendererDispatcher ()Lnet/minecraft/client/renderer/BlockRendererDispatcher;
//    ASTORE 26
//   L36
//    LINENUMBER 1587 L36
//    ALOAD 26
//    ALOAD 23
//    ALOAD 13
//    ALOAD 25
//    ALOAD 0
//    GETFIELD net/minecraft/client/renderer/RenderGlobal.world : Lnet/minecraft/client/multiplayer/WorldClient;
//    INVOKEVIRTUAL net/minecraft/client/renderer/BlockRendererDispatcher.renderBlockDamage (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IBlockAccess;)V
//   L25

//   L35
//    LINENUMBER 1626 L35
//    ALOAD 0
//    GETFIELD net/minecraft/client/renderer/RenderGlobal.mc : Lnet/minecraft/client/Minecraft;
//    INVOKEVIRTUAL net/minecraft/client/Minecraft.getBlockRendererDispatcher ()Lnet/minecraft/client/renderer/BlockRendererDispatcher;
//    ASTORE 26
//   L36
//    LINENUMBER 1628 L36
//    ALOAD 1
//    ALOAD 2
//    ALOAD 13
//    ALOAD 23
//    ALOAD 0
//    GETFIELD net/minecraft/client/renderer/RenderGlobal.world : Lnet/minecraft/client/multiplayer/WorldClient;
//    ALOAD 25
//    ALOAD 26
//    INVOKESTATIC io/github/cadiboo/nocubes/hooks/Hooks.renderBlockDamage (Lnet/minecraft/client/renderer/Tessellator;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/BlockRendererDispatcher;)Z
//    IFEQ L25
//   L37
//    LINENUMBER 1630 L37
//    ALOAD 26
//    ALOAD 23
//    ALOAD 13
//    ALOAD 25
//    ALOAD 0
//    GETFIELD net/minecraft/client/renderer/RenderGlobal.world : Lnet/minecraft/client/multiplayer/WorldClient;
//    INVOKEVIRTUAL net/minecraft/client/renderer/BlockRendererDispatcher.renderBlockDamage (Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IWorldReader;)V
//   L25

		boolean optifine = false;
		int arrayLength = instructions.size();
		for (int i = 0; i < arrayLength; ++i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == GETSTATIC) {
				FieldInsnNode instruction = (FieldInsnNode) insn;
				if (instruction.owner.equals("net/optifine/reflect/Reflector")) {
					if (instruction.name.equals("ForgeTileEntity_canRenderBreaking")) {
						optifine = true;
						log("Found OptiFine");
						break;
					}
				}
			}
		}

		final String BlockRendererDispatcher_renderBlockDamage_name = mapMethod("net/minecraft/client/renderer/BlockRendererDispatcher", "func_175020_a", "(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IBlockAccess;)V"); // renderBlockDamage

		AbstractInsnNode first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage = null;
//		int arrayLength = instructions.size();
		for (int i = 0; i < arrayLength; ++i) {
			AbstractInsnNode insn = instructions.get(i);
			if (insn.getOpcode() == INVOKEVIRTUAL) {
				MethodInsnNode instruction = (MethodInsnNode) insn;
				if (instruction.owner.equals("net/minecraft/client/renderer/BlockRendererDispatcher")) {
					if (instruction.name.equals(BlockRendererDispatcher_renderBlockDamage_name)) {
						if (instruction.desc.equals("(Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/world/IBlockAccess;)V")) {
							if (instruction.itf == false) {
								first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage = instruction;
								log("Found injection point " + instruction);
								break;
							}
						}
					}
				}
			}
		}
		if (first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage == null) {
			throw new RuntimeException("Error: Couldn't find injection point!");
		}

		AbstractInsnNode firstLabelBefore_first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage = null;
		for (int i = instructions.indexOf(first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage); i >= 0; --i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				firstLabelBefore_first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage = instruction;
				log("Found label " + instruction);
				break;
			}
		}
		if (firstLabelBefore_first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage == null) {
			throw new RuntimeException("Error: Couldn't find label!");
		}

		LabelNode firstLabelAfter_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage = null;
		for (int i = instructions.indexOf(first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage); i < arrayLength; ++i) {
			AbstractInsnNode instruction = instructions.get(i);
			if (instruction.getType() == AbstractInsnNode.LABEL) {
				firstLabelAfter_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage = (LabelNode) instruction;
				log("Found label " + instruction);
				break;
			}
		}
		if (firstLabelAfter_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage == null) {
			throw new RuntimeException("Error: Couldn't find label!");
		}

		InsnList toInject = new InsnList();

		// Labels n stuff
		LabelNode originalInstructionsLabel = new LabelNode();

		// Make list of instructions to inject
		if (!optifine) {
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_tessellator));
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_bufferbuilder));
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_blockpos));
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_iblockstate));
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
			toInject.add(new FieldInsnNode(
					GETFIELD,
					"net/minecraft/client/renderer/RenderGlobal",
					mapField("net/minecraft/client/renderer/RenderGlobal", "field_72769_h"), // world
					"Lnet/minecraft/client/multiplayer/WorldClient;"
			));
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_textureatlassprite));
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_blockrendererdispatcher));
		} else {
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_tessellator));
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_bufferbuilder));
			toInject.add(new VarInsnNode(ALOAD, 13)); // blockpos
			toInject.add(new VarInsnNode(ALOAD, 21)); // iblockstate
			toInject.add(new VarInsnNode(ALOAD, ALOCALVARIABLE_this));
			toInject.add(new FieldInsnNode(
					GETFIELD,
					"net/minecraft/client/renderer/RenderGlobal",
					mapField("net/minecraft/client/renderer/RenderGlobal", "field_72769_h"), // world
					"Lnet/minecraft/client/multiplayer/WorldClient;"
			));
			toInject.add(new VarInsnNode(ALOAD, 24)); // textureatlassprite
			toInject.add(new VarInsnNode(ALOAD, 25)); // blockrendererdispatcher
		}
		toInject.add(new MethodInsnNode(
				//int opcode
				INVOKESTATIC,
				//String owner
				"io/github/cadiboo/nocubes/hooks/Hooks",
				//String name
				"renderBlockDamage",
				//String descriptor
				"(Lnet/minecraft/client/renderer/Tessellator;Lnet/minecraft/client/renderer/BufferBuilder;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/client/multiplayer/WorldClient;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/BlockRendererDispatcher;)Z",
				//boolean isInterface
				false
		));
		toInject.add(new JumpInsnNode(IFEQ, firstLabelAfter_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage));

		toInject.add(originalInstructionsLabel);

		// Inject instructions
		instructions.insert(firstLabelBefore_first_INVOKEVIRTUAL_BlockRendererDispatcher_renderBlockDamage, toInject);

	}

}
