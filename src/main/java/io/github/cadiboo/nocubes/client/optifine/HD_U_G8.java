package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.coremod.api.ASMAPI;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static io.github.cadiboo.nocubes.client.optifine.HD_U_G8.Reflect.*;
import static net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;

class HD_U_G8 implements OptiFineProxy {

	@Override
	public boolean initialisedAndUsable() {
		for (Field field : Reflect.class.getDeclaredFields()) {
			try {
				if (field.get(null) == null)
					return false;
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Can't access my own fields...?", e);
			}
		}
		return true;
	}

	@Override
	public void preRenderChunk(BlockPos blockpos) {
	}

	@Override
	public long getSeed(long originalSeed) {
		return Config_isAlternateBlocks() ? 0 : originalSeed;
	}

	@Override
	public Object preRenderBlock(ChunkRender chunkRender, RegionRenderCacheBuilder builder, IBlockDisplayReader chunkCacheOF, RenderType renderType, BufferBuilder buffer, BlockState state, BlockPos pos) {
		BufferBuilder_setBlockLater(buffer, renderType);
		Object renderEnv = BufferBuilder_getRenderEnv(buffer, state, pos);
		RenderEnv_setRegionRenderCacheBuilder(renderEnv, builder);
		ChunkCacheOF_setRenderEnv(chunkCacheOF, renderEnv);

		boolean shaders = Config_isShaders();
		boolean shadersMidBlock = shaders && Shaders_useMidBlockAttrib();
		if (shadersMidBlock)
			BufferBuilder_setMidBlock(
				buffer,
				0.5F + (float) ChunkRender_regionDX(chunkRender) + (float) (pos.getX() & 15),
				0.5F + (float) ChunkRender_regionDY(chunkRender) + (float) (pos.getY() & 15),
				0.5F + (float) ChunkRender_regionDZ(chunkRender) + (float) (pos.getZ() & 15)
			);
		if (shaders)
			SVertexBuilder_pushEntity(state, buffer);
		return renderEnv;
	}

	@Override
	public IBakedModel getModel(Object renderEnv, IBakedModel originalModel, BlockState state) {
		return BlockModelCustomizer_getRenderModel(originalModel, state, renderEnv);
	}

	@Override
	public void postRenderBlock(Object renderEnv, BufferBuilder buffer, ChunkRender chunkRender, RegionRenderCacheBuilder builder, CompiledChunk compiledChunk) {
		if (Config_isShaders())
			SVertexBuilder_popEntity(buffer);

		if (RenderEnv_isOverlaysRendered(renderEnv)) {
			ChunkRender_postRenderOverlays(chunkRender, builder, compiledChunk);
			RenderEnv_setOverlaysRendered(renderEnv, false);
		}
	}

	@Nullable
	@Override
	public BakedQuad getQuadEmissive(BakedQuad quad) {
		return BakedQuad_getQuadEmissive(quad);
	}

	@Override
	public void preRenderQuad(Object renderEnv, BakedQuad emissiveQuad, BlockState state, BlockPos pos) {
		RenderEnv_reset(renderEnv, state, pos);
	}

	// All reflection stuff can be null but we check beforehand
	@SuppressWarnings("ConstantConditions")
	interface Reflect {

		MethodHandle isShaders = getMethod("net.optifine.Config", "isShaders");
		MethodHandle isAlternateBlocks = getMethod("net.optifine.Config", "isAlternateBlocks");

		Field useMidBlockAttrib = getField("net.optifine.shaders.Shaders", "useMidBlockAttrib");
		MethodHandle pushEntity = getMethod("net.optifine.shaders.SVertexBuilder", "pushEntity", BlockState.class, IVertexBuilder.class);
		MethodHandle popEntity = getMethod("net.optifine.shaders.SVertexBuilder", "popEntity", IVertexBuilder.class);

		MethodHandle postRenderOverlays = getMethod("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender", "postRenderOverlays", RegionRenderCacheBuilder.class, CompiledChunk.class);
		Field regionDX = getField("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender", "regionDX");
		Field regionDY = getField("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender", "regionDY");
		Field regionDZ = getField("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$ChunkRender", "regionDZ");

		MethodHandle getQuadEmissive = getMethod("net.minecraft.client.renderer.model.BakedQuad", "getQuadEmissive");
		MethodHandle setBlockLayer = getMethod("net.minecraft.client.renderer.BufferBuilder", "setBlockLayer", RenderType.class);
		MethodHandle setMidBlock = getMethod("net.minecraft.client.renderer.BufferBuilder", "setMidBlock", float.class, float.class, float.class);
		MethodHandle getRenderEnv = getMethod("net.minecraft.client.renderer.BufferBuilder", "getRenderEnv", BlockState.class, BlockPos.class);

		MethodHandle reset = getMethod("net.optifine.render.RenderEnv", "reset", BlockState.class, BlockPos.class);
		MethodHandle setRegionRenderCacheBuilder = getMethod("net.optifine.render.RenderEnv", "setRegionRenderCacheBuilder", RegionRenderCacheBuilder.class);
		MethodHandle isOverlaysRendered = getMethod("net.optifine.render.RenderEnv", "isOverlaysRendered");
		MethodHandle setOverlaysRendered = getMethod("net.optifine.render.RenderEnv", "setOverlaysRendered", boolean.class);
		MethodHandle setRenderEnv = getMethod("net.optifine.override.ChunkCacheOF", "setRenderEnv", "net.optifine.render.RenderEnv");
		MethodHandle getRenderModel = getMethod("net.optifine.model.BlockModelCustomizer", "getRenderModel", IBakedModel.class, BlockState.class, "net.optifine.render.RenderEnv");
		MethodHandle chunkLayerSet_add = getMethod("net.optifine.render.ChunkLayerSet", "add", RenderType.class);
		Field hasBlocks = getField("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$CompiledChunk", ASMAPI.mapField("field_178500_b"));

		@Nullable
		static MethodHandle getMethod(String clazz, String name, Object... paramClasses) {
			try {
				MethodHandles.Lookup lookup = MethodHandles.publicLookup();
				Class<?> klass = Class.forName(clazz);
				Class<?>[] params = new Class[paramClasses.length];
				for (int i = 0; i < paramClasses.length; i++) {
					Object param = paramClasses[i];
					params[i] = param instanceof Class<?> ? (Class<?>) param : Class.forName((String) param);
				}
				Method method = klass.getDeclaredMethod(name, params);
				method.setAccessible(true);
				return lookup.unreflect(method);
			} catch (Exception e) {
				return null;
			}
		}

		@Nullable
		static Field getField(String clazz, String name) {
			try {
				Class<?> klass = Class.forName(clazz);
				return klass.getDeclaredField(name);
			} catch (Exception e) {
				return null;
			}
		}

		static boolean Config_isShaders() {
			try {
				return (boolean) isShaders.invokeExact();
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static boolean Config_isAlternateBlocks() {
			try {
				return (boolean) isAlternateBlocks.invokeExact();
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static boolean Shaders_useMidBlockAttrib() {
			try {
				return useMidBlockAttrib.getBoolean(null);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static int ChunkRender_regionDX(ChunkRender chunkRender) {
			try {
				return regionDX.getInt(chunkRender);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static int ChunkRender_regionDY(ChunkRender chunkRender) {
			try {
				return regionDY.getInt(chunkRender);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static int ChunkRender_regionDZ(ChunkRender chunkRender) {
			try {
				return regionDZ.getInt(chunkRender);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static Object CompiledChunk_hasBlocks(CompiledChunk compiledChunk) {
			try {
				return hasBlocks.get(compiledChunk);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		@Nullable
		static BakedQuad BakedQuad_getQuadEmissive(BakedQuad quad) {
			try {
				return (BakedQuad) getQuadEmissive.invokeExact(quad);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void SVertexBuilder_pushEntity(BlockState state, IVertexBuilder buffer) {
			try {
				pushEntity.invokeExact(state, buffer);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void SVertexBuilder_popEntity(IVertexBuilder buffer) {
			try {
				popEntity.invokeExact(buffer);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void RenderEnv_reset(Object renderEnv, BlockState state, BlockPos pos) {
//			((RenderEnv) renderEnv).reset(state, pos);
			try {
				reset.invoke(renderEnv, state, pos);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static IBakedModel BlockModelCustomizer_getRenderModel(IBakedModel originalModel, BlockState state, Object renderEnv) {
//			return BlockModelCustomizer.getRenderModel(originalModel, state, (RenderEnv) renderEnv);
			try {
				return (IBakedModel) getRenderModel.invoke(originalModel, state, renderEnv);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void BufferBuilder_setMidBlock(BufferBuilder buffer, float x, float y, float z) {
//			buffer.setMidBlock(x, y, z);
			try {
				setMidBlock.invokeExact(buffer, x, y, z);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void ChunkCacheOF_setRenderEnv(IBlockDisplayReader chunkCacheOF, Object renderEnv) {
//			((ChunkCacheOF) chunkCacheOF).setRenderEnv((RenderEnv) renderEnv);
			try {
				setRenderEnv.invoke(chunkCacheOF, renderEnv);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void RenderEnv_setRegionRenderCacheBuilder(Object renderEnv, RegionRenderCacheBuilder builder) {
//			((RenderEnv) renderEnv).setRegionRenderCacheBuilder(builder);
			try {
				setRegionRenderCacheBuilder.invoke(renderEnv, builder);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static Object BufferBuilder_getRenderEnv(BufferBuilder buffer, BlockState state, BlockPos pos) {
//			return buffer.getRenderEnv(state, pos);
			try {
				return getRenderEnv.invoke(buffer, state, pos);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void BufferBuilder_setBlockLater(BufferBuilder buffer, RenderType renderType) {
//			buffer.setBlockLayer(renderType);
			try {
				setBlockLayer.invokeExact(buffer, renderType);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static boolean RenderEnv_isOverlaysRendered(Object renderEnv) {
//			return ((RenderEnv) renderEnv).isOverlaysRendered();
			try {
				return (boolean) isOverlaysRendered.invoke(renderEnv);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void ChunkRender_postRenderOverlays(ChunkRender chunkRender, RegionRenderCacheBuilder builder, CompiledChunk compiledChunk) {
//			chunkRender.postRenderOverlays(builder, compiledChunk);
			try {
				postRenderOverlays.invokeExact(chunkRender, builder, compiledChunk);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void RenderEnv_setOverlaysRendered(Object renderEnv, boolean rendered) {
//			((RenderEnv) renderEnv).setOverlaysRendered(rendered);
			try {
				setOverlaysRendered.invoke(renderEnv, rendered);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void ChunkLayerSet_add(Object chunkLayerSet, RenderType renderType) {
			try {
				chunkLayerSet_add.invoke(chunkLayerSet, renderType);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

	}

}
