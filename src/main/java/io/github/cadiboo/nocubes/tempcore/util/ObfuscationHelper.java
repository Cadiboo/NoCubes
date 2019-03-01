package io.github.cadiboo.nocubes.tempcore.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import static org.objectweb.asm.Type.BOOLEAN_TYPE;
import static org.objectweb.asm.Type.FLOAT_TYPE;
import static org.objectweb.asm.Type.INT_TYPE;
import static org.objectweb.asm.Type.VOID_TYPE;

public class ObfuscationHelper implements Opcodes {

	public enum ObfuscationLevel {
		DEOBFUSCATED,
		SRG,
		OBFUSCATED;

	}

	public enum ObfuscationClass {
		//um, MCPBot says that RenderChunk is bxp (on 1.12 mappings), but when I run Minecraft (on 1.12.2) it says that RenderChunk is bxr. Using Minecraft 1.12.2 obfname
		RENDER_CHUNK("net/minecraft/client/renderer/chunk/RenderChunk", "net/minecraft/client/renderer/chunk/RenderChunk", "bxr"),
		CHUNK_COMPILE_TASK_GENERATOR("net/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator", "net/minecraft/client/renderer/chunk/ChunkCompileTaskGenerator", "bxl"),
		VIS_GRAPH("net/minecraft/client/renderer/chunk/VisGraph", "net/minecraft/client/renderer/chunk/VisGraph", "bxs"),
		RENDER_GLOBAL("net/minecraft/client/renderer/RenderGlobal", "net/minecraft/client/renderer/RenderGlobal", "buw"),
		CHUNK_CACHE("net/minecraft/world/ChunkCache", "net/minecraft/world/ChunkCache", "anb"),
		BLOCK_POS("net/minecraft/util/math/BlockPos", "net/minecraft/util/math/BlockPos", "et"),
		MUTABLE_BLOCK_POS(BLOCK_POS.deobfuscatedName + "$MutableBlockPos", BLOCK_POS.srgName + "$MutableBlockPos", BLOCK_POS.obfuscatedName + "$a"),
		COMPILED_CHUNK("net/minecraft/client/renderer/chunk/CompiledChunk", "net/minecraft/client/renderer/chunk/CompiledChunk", "bxm"),
		BLOCK_RENDERER_DISPATCHER("net/minecraft/client/renderer/BlockRendererDispatcher", "net/minecraft/client/renderer/BlockRendererDispatcher", "bvk"),
		I_BLOCK_STATE("net/minecraft/block/state/IBlockState", "net/minecraft/block/state/IBlockState", "awr"),
		BLOCK_RENDER_LAYER("net/minecraft/util/BlockRenderLayer", "net/minecraft/util/BlockRenderLayer", "amk"),
		I_BLOCK_ACCESS("net/minecraft/world/IBlockAccess", "net/minecraft/world/IBlockAccess", "amw"),
		BUFFER_BUILDER("net/minecraft/client/renderer/BufferBuilder", "net/minecraft/client/renderer/BufferBuilder", "bui"),
		TILE_ENTITY_RENDERER_DISPATCHER("net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "net/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher", "bwv"),
		BLOCK("net/minecraft/block/Block", "net/minecraft/block/Block", "aou"),
		BETTER_FOLIAGE_HOOKS("mods/betterfoliage/client/Hooks", "mods/betterfoliage/client/Hooks", "mods/betterfoliage/client/Hooks"),
		OPTIFINE_REFLECTOR_METHOD("net/optifine/reflect/ReflectorMethod", "net/optifine/reflect/ReflectorMethod", "net/optifine/reflect/ReflectorMethod"),
		OPTIFINE_REFLECTOR("net/optifine/reflect/Reflector", "net/optifine/reflect/Reflector", "net/optifine/reflect/Reflector"),
		ENUM_BLOCK_RENDER_TYPE("net/minecraft/util/EnumBlockRenderType", "net/minecraft/util/EnumBlockRenderType", "ath"),

		BLOCK_POS_M("net/optifine/BlockPosM", "net/optifine/BlockPosM", "net/optifine/BlockPosM"),
		CHUNK_CACHE_OF("net/optifine/override/ChunkCacheOF", "net/optifine/override/ChunkCacheOF", "net/optifine/override/ChunkCacheOF"),

		RCRCH_HOOKS("io/github/cadiboo/renderchunkrebuildchunkhooks/hooks/RenderChunkRebuildChunkHooksHooks", "io/github/cadiboo/renderchunkrebuildchunkhooks/hooks/RenderChunkRebuildChunkHooksHooks", "io/github/cadiboo/renderchunkrebuildchunkhooks/hooks/RenderChunkRebuildChunkHooksHooks"),
		RCRCH_HOOKS_OPTIFINE("io/github/cadiboo/renderchunkrebuildchunkhooks/hooks/RenderChunkRebuildChunkHooksHooksOptifine", "io/github/cadiboo/renderchunkrebuildchunkhooks/hooks/RenderChunkRebuildChunkHooksHooksOptifine", "io/github/cadiboo/renderchunkrebuildchunkhooks/hooks/RenderChunkRebuildChunkHooksHooksOptifine"),

		HASH_SET("java/util/HashSet", "java/util/HashSet", "java/util/HashSet");

		private final String deobfuscatedName;
		private final String srgName;
		private final String obfuscatedName;

		ObfuscationClass(String deobfuscatedName, String srgName, String obfuscatedName) {
			this.deobfuscatedName = deobfuscatedName;
			this.srgName = srgName;
			this.obfuscatedName = obfuscatedName;
		}

		/**
		 * gets the internal name for the ObfuscationClass based on the current environment (path/to/class)
		 *
		 * @return the correct internal name for the current environment
		 */
		public String getInternalName() {
//			switch (OBFUSCATION_LEVEL) {
//				case DEOBFUSCATED:
					return this.deobfuscatedName;
//				default: //1.12.2
//				case SRG:
//					return this.srgName;
//				// default: //1.13
//				case OBFUSCATED:
//					return this.obfuscatedName;
//			}

		}

		/**
		 * gets the name for the ObfuscationClass based on the current environment (path.to.class)
		 *
		 * @return the correct name for the current environment
		 */
		public String getClassName() {
			return Type.getObjectType(this.getInternalName()).getClassName();

		}
	}

	public enum ObfuscationField {
		// instance fields
//		RENDER_CHUNK_POSITION(false, RENDER_CHUNK, "position", "field_178586_f", "o", MUTABLE_BLOCK_POS),
		RENDER_CHUNK_RENDER_GLOBAL(false, ObfuscationClass.RENDER_CHUNK, "renderGlobal", "field_178589_e", "e", ObfuscationClass.RENDER_GLOBAL),
		RENDER_CHUNK_WORLD_VIEW(false, ObfuscationClass.RENDER_CHUNK, "worldView", "field_189564_r", "r", ObfuscationClass.CHUNK_CACHE),
		RENDER_CHUNK_LOCK_COMPILE_TASK(false, ObfuscationClass.RENDER_CHUNK, "lockCompileTask", "field_178587_g", "f", Type.getObjectType(Type.getInternalName(ReentrantLock.class))),
		RENDER_CHUNK_SET_TILE_ENTITIES(false, ObfuscationClass.RENDER_CHUNK, "setTileEntities", "field_181056_j", "i", Type.getObjectType(Type.getInternalName(HashSet.class))),

		// static fields
		RENDER_CHUNK_RENDER_CHUNKS_UPDATED(true, ObfuscationClass.RENDER_CHUNK, "renderChunksUpdated", "field_178592_a", "a", INT_TYPE),
		TILE_ENTITY_RENDERER_DISPATCHER_INSTANCE(true, ObfuscationClass.TILE_ENTITY_RENDERER_DISPATCHER, "instance", "field_147556_a", "a", ObfuscationClass.TILE_ENTITY_RENDERER_DISPATCHER),

		OPTIFINE_FORGE_BLOCK_CAN_RENDER_IN_LAYER(true, ObfuscationClass.OPTIFINE_REFLECTOR, "ForgeBlock_canRenderInLayer", "ForgeBlock_canRenderInLayer", "ForgeBlock_canRenderInLayer", ObfuscationClass.OPTIFINE_REFLECTOR_METHOD),

		ENUM_BLOCK_RENDER_TYPE_INVISIBLE(true, ObfuscationClass.ENUM_BLOCK_RENDER_TYPE, "INVISIBLE", "INVISIBLE", "INVISIBLE", ObfuscationClass.ENUM_BLOCK_RENDER_TYPE),

		;

		private final boolean isStatic;
		private final ObfuscationClass owner;
		private final String deobfuscatedName;
		private final String srgName;
		private final String obfuscatedName;
		private final Object type;

		ObfuscationField(final boolean isStatic, ObfuscationClass owner, String deobfuscatedName, String srgName, String obfuscatedName, Object type) {
			this.isStatic = isStatic;
			this.owner = owner;
			this.deobfuscatedName = deobfuscatedName;
			this.srgName = srgName;
			this.obfuscatedName = obfuscatedName;
			this.type = type;

		}

		public ObfuscationClass getOwner() {
			return owner;
		}

		/**
		 * gets the name based on the current environment
		 *
		 * @return the correct name for the current environment
		 */
		public String getName() {
//			switch (OBFUSCATION_LEVEL) {
//				case DEOBFUSCATED:
					return this.deobfuscatedName;
//				default: //1.12.2
//				case SRG:
//					return this.srgName;
//				// default: //1.13
//				case OBFUSCATED:
//					return this.obfuscatedName;
//			}

		}

		public String getDescriptor() {
			final Type type;
			if (this.type instanceof ObfuscationClass) {
				type = Type.getObjectType(((ObfuscationClass) this.type).getInternalName());
			} else if (this.type instanceof Type) {
				type = (Type) this.type;
			} else {
				throw new RuntimeException("Illegal Field Type!");
			}

			return type.getDescriptor();

		}

		public boolean matches(AbstractInsnNode node) {

			if (!(node instanceof FieldInsnNode)) {
				return false;
			}

			FieldInsnNode field = (FieldInsnNode) node;

			if (field.getOpcode() != getGetOpcode() && field.getOpcode() != getPutOpcode()) {
				return false;
			}

			if (!field.owner.equals(this.getOwner().getInternalName())) {
				return false;
			}

			if (!field.name.equals(this.getName())) {
				return false;
			}

			if (!field.desc.equals(this.getDescriptor())) {
				return false;
			}

			return true;

		}

		private int getPutOpcode() {
			return isStatic ? PUTSTATIC : PUTFIELD;
		}

		private int getGetOpcode() {
			return isStatic ? GETSTATIC : GETFIELD;
		}

		public void get(final InsnList insnList) {
			if (!isStatic) {
				insnList.add(new VarInsnNode(ALOAD, 0)); // load `this`
			}
			insnList.add(new FieldInsnNode(getGetOpcode(), getOwner().getInternalName(), getName(), getDescriptor()));
		}

		public void put(final InsnList insnList) {
			if (!isStatic) {
				insnList.add(new VarInsnNode(ALOAD, 0)); // load `this`
			}
			insnList.add(new FieldInsnNode(getPutOpcode(), getOwner().getInternalName(), getName(), getDescriptor()));
		}

	}

	public enum ObfuscationMethod {
		RENDER_CHUNK_REBUILD_CHUNK(INVOKEVIRTUAL, ObfuscationClass.RENDER_CHUNK, "rebuildChunk", "func_178581_b", "b", VOID_TYPE, new Object[]{
				FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR
		}, false),
		RENDER_CHUNK_RESORT_TRANSPARENCY(Integer.MIN_VALUE, ObfuscationClass.RENDER_CHUNK, "resortTransparency", "func_178570_a", "a", VOID_TYPE, new Object[]{
				FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR
		}, false),
		RENDER_CHUNK_PRE_RENDER_BLOCKS(INVOKESPECIAL, ObfuscationClass.RENDER_CHUNK, "preRenderBlocks", "func_178573_a", "a", VOID_TYPE, new Object[]{
				ObfuscationClass.BUFFER_BUILDER, ObfuscationClass.BLOCK_POS
		}, false),
		RENDER_CHUNK_POST_RENDER_BLOCKS(INVOKESPECIAL, ObfuscationClass.RENDER_CHUNK, "postRenderBlocks", "func_178584_a", "a", VOID_TYPE, new Object[]{
				ObfuscationClass.BLOCK_RENDER_LAYER, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.BUFFER_BUILDER, ObfuscationClass.COMPILED_CHUNK
		}, false),
		BLOCK_RENDERER_DISPATCHER_RENDER_BLOCK(INVOKEVIRTUAL, ObfuscationClass.BLOCK_RENDERER_DISPATCHER, "renderBlock", "func_175018_a", "a", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK_POS, ObfuscationClass.I_BLOCK_ACCESS, ObfuscationClass.BUFFER_BUILDER
		}, false),
		// forge added method
		BLOCK_CAN_RENDER_IN_LAYER(INVOKEVIRTUAL, ObfuscationClass.BLOCK, "canRenderInLayer", "canRenderInLayer", "canRenderInLayer", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK_RENDER_LAYER
		}, false),
		OPTIFINE_REFLECTOR_METHOD_EXISTS(INVOKEVIRTUAL, ObfuscationClass.OPTIFINE_REFLECTOR_METHOD, "exists", "exists", "exists", BOOLEAN_TYPE, new Object[]{}, false),
		BETTER_FOLIAGE_CAN_BLOCK_RENDER_IN_LAYER(INVOKESTATIC, ObfuscationClass.BETTER_FOLIAGE_HOOKS, "canRenderBlockInLayer", "canRenderBlockInLayer", "canRenderBlockInLayer", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.BLOCK, ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK_RENDER_LAYER
		}, false),
		BETTER_FOLIAGE_RENDER_WORLD_BLOCK(INVOKESTATIC, ObfuscationClass.BETTER_FOLIAGE_HOOKS, "renderWorldBlock", "renderWorldBlock", "renderWorldBlock", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.BLOCK_RENDERER_DISPATCHER, ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK_POS, ObfuscationClass.I_BLOCK_ACCESS, ObfuscationClass.BUFFER_BUILDER, ObfuscationClass.BLOCK_RENDER_LAYER
		}, false),

		ON_REBUILD_CHUNK_PRE_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS, "rebuildChunkPreHook", "rebuildChunkPreHook", "rebuildChunkPreHook", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL
		}, false),

		CAN_BLOCK_RENDER_IN_LAYER_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS, "canBlockRenderInLayerHook", "canBlockRenderInLayerHook", "canBlockRenderInLayerHook", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL,
				Type.getType(boolean[].class), ObfuscationClass.BLOCK_RENDERER_DISPATCHER, ObfuscationClass.BLOCK_POS, ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK, ObfuscationClass.BLOCK_RENDER_LAYER
		}, false),

		DOES_BLOCK_TYPE_ALLOW_RENDER_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS, "doesBlockTypeAllowRenderHook", "doesBlockTypeAllowRenderHook", "doesBlockTypeAllowRenderHook", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL,
				Type.getType(boolean[].class), ObfuscationClass.BLOCK_RENDERER_DISPATCHER, ObfuscationClass.BLOCK_POS, ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK, ObfuscationClass.BLOCK_RENDER_LAYER, INT_TYPE
		}, false),

		REBUILD_CHUNK_BLOCK_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS, "rebuildChunkBlockHook", "rebuildChunkBlockHook", "rebuildChunkBlockHook", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL,
				Type.getType(boolean[].class), ObfuscationClass.BLOCK_RENDERER_DISPATCHER, ObfuscationClass.BLOCK_POS, ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK, ObfuscationClass.BLOCK_RENDER_LAYER, INT_TYPE, ObfuscationClass.BUFFER_BUILDER
		}, false),

		REBUILD_CHUNK_POST_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS, "rebuildChunkPostHook", "rebuildChunkPostHook", "rebuildChunkPostHook", VOID_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL
		}, false),

		ON_REBUILD_CHUNK_PRE_OPTIFINE_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS_OPTIFINE, "rebuildChunkPreHook", "rebuildChunkPreHook", "rebuildChunkPreHook", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE_OF, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL
		}, false),

		CAN_BLOCK_RENDER_IN_LAYER_OPTIFINE_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS_OPTIFINE, "canBlockRenderInLayerHook", "canBlockRenderInLayerHook", "canBlockRenderInLayerHook", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE_OF, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL,
				Type.getType(boolean[].class), ObfuscationClass.BLOCK_RENDERER_DISPATCHER, ObfuscationClass.BLOCK_POS_M, ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK, ObfuscationClass.BLOCK_RENDER_LAYER
		}, false),

		DOES_BLOCK_TYPE_ALLOW_RENDER_OPTIFINE_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS_OPTIFINE, "doesBlockTypeAllowRenderHook", "doesBlockTypeAllowRenderHook", "doesBlockTypeAllowRenderHook", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE_OF, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL,
				Type.getType(boolean[].class), ObfuscationClass.BLOCK_RENDERER_DISPATCHER, ObfuscationClass.BLOCK_POS_M, ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK, ObfuscationClass.BLOCK_RENDER_LAYER, INT_TYPE
		}, false),

		REBUILD_CHUNK_BLOCK_OPTIFINE_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS_OPTIFINE, "rebuildChunkBlockHook", "rebuildChunkBlockHook", "rebuildChunkBlockHook", BOOLEAN_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE_OF, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL,
				Type.getType(boolean[].class), ObfuscationClass.BLOCK_RENDERER_DISPATCHER, ObfuscationClass.BLOCK_POS_M, ObfuscationClass.I_BLOCK_STATE, ObfuscationClass.BLOCK, ObfuscationClass.BLOCK_RENDER_LAYER, INT_TYPE, ObfuscationClass.BUFFER_BUILDER
		}, false),

		RENDER_CHUNK_MAKE_CHUNK_CACHE_OF(INVOKESPECIAL, ObfuscationClass.RENDER_CHUNK, "makeChunkCacheOF", "makeChunkCacheOF", "makeChunkCacheOF", ObfuscationClass.CHUNK_CACHE_OF, new Object[]{
				ObfuscationClass.BLOCK_POS
		}, false),

		REBUILD_CHUNK_POST_OPTIFINE_HOOK(INVOKESTATIC, ObfuscationClass.RCRCH_HOOKS_OPTIFINE, "rebuildChunkPostHook", "rebuildChunkPostHook", "rebuildChunkPostHook", VOID_TYPE, new Object[]{
				ObfuscationClass.RENDER_CHUNK, FLOAT_TYPE, FLOAT_TYPE, FLOAT_TYPE, ObfuscationClass.CHUNK_COMPILE_TASK_GENERATOR, ObfuscationClass.COMPILED_CHUNK, ObfuscationClass.BLOCK_POS, ObfuscationClass.CHUNK_CACHE_OF, ObfuscationClass.VIS_GRAPH, ObfuscationClass.HASH_SET, ObfuscationClass.RENDER_GLOBAL
		}, false),
		;

		private final int opcode;
		private final ObfuscationClass owner;
		private final String deobfuscatedName;
		private final String srgName;
		private final String obfuscatedName;
		private final Object returnType;
		private final Object[] params;
		private final boolean isInterface;

		ObfuscationMethod(final int opcode, ObfuscationClass owner, String deobfuscatedName, String srgName, String obfuscatedName, Object returnType, Object[] params, boolean isInterface) {
			this.opcode = opcode;
			this.owner = owner;
			this.deobfuscatedName = deobfuscatedName;
			this.srgName = srgName;
			this.obfuscatedName = obfuscatedName;
			this.returnType = returnType;
			this.params = params;
			this.isInterface = isInterface;
		}

		public ObfuscationClass getOwner() {
			return owner;

		}

		/**
		 * gets the name based on the current environment
		 *
		 * @return the correct name for the current environment
		 */
		public String getName() {
//			switch (OBFUSCATION_LEVEL) {
//				case DEOBFUSCATED:
			return this.deobfuscatedName;
//				default: //1.12.2
//				case SRG:
//					return this.srgName;
//				// default: //1.13
//				case OBFUSCATED:
//					return this.obfuscatedName;
//			}

		}

		public String getDescriptor() {
			final Type returnType;
			if (this.returnType instanceof ObfuscationClass) {
				returnType = Type.getObjectType(((ObfuscationClass) this.returnType).getInternalName());
			} else if (this.returnType instanceof Type) {
				returnType = (Type) this.returnType;
			} else {
				throw new RuntimeException("Illegal Return Type!");
			}

			final ArrayList<Type> params = new ArrayList<>();

			for (Object paramObject : this.params) {
				final Type param;
				if (paramObject instanceof ObfuscationClass) {
					param = Type.getObjectType(((ObfuscationClass) paramObject).getInternalName());
				} else if (paramObject instanceof Type) {
					param = (Type) paramObject;
				} else {
					throw new RuntimeException("Illegal Parameter!");
				}

				params.add(param);

			}

			return Type.getMethodDescriptor(returnType, params.toArray(new Type[0]));

		}

		public Type getType() {
			return Type.getMethodType(this.getDescriptor());

		}

		public boolean isInterface() {
			return isInterface;

		}

		public boolean matches(AbstractInsnNode node) {

			if (!(node instanceof MethodInsnNode)) {
				return false;
			}

			MethodInsnNode method = (MethodInsnNode) node;

			if (method.getOpcode() != getOpcode()) {
				return false;
			}

			if (!method.owner.equals(this.getOwner().getInternalName())) {
				return false;
			}

			if (!method.name.equals(this.getName())) {
				return false;
			}

			if (!method.desc.equals(this.getDescriptor())) {
				return false;
			}

			if (!method.itf == this.isInterface()) {
				return false;
			}

			return true;
		}

		public int getOpcode() {
			return this.opcode;
		}

		public void visit(final InsnList insnList) {
			insnList.add(this.visit());
		}

		public MethodInsnNode visit() {
			return new MethodInsnNode(getOpcode(), getOwner().getInternalName(), getName(), getDescriptor(), isInterface());
		}
	}

}
