package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Cadiboo
 */
final class HD_U_F5 implements OptiFineProxy {

	private final MethodHandle IS_SHADERS;
	private final Class<?> CHUNK_CACHE_OF;
	private final Field CHUNK_CACHE_OF_CHUNK_CACHE;
	private final MethodHandle PUSH_ENTITY;
	private final MethodHandle POP_ENTITY;
	private final MethodHandle GET_RENDER_MODEL;
	private final MethodHandle GET_RENDER_QUADS;
	private final MethodHandle GET_RENDER_ENV;

	HD_U_F5() throws Exception {
		MethodHandles.Lookup lookup = MethodHandles.publicLookup();

		Class<?> config = OptiFineLocator.findConfigClass();
		IS_SHADERS = lookup.unreflect(config.getMethod("isShaders"));

		CHUNK_CACHE_OF = Class.forName("net.optifine.override.ChunkCacheOF");
		// OptiFine added classes, these methods & fields have no SRG names
		CHUNK_CACHE_OF_CHUNK_CACHE = ObfuscationReflectionHelper.findField(CHUNK_CACHE_OF, "chunkCache");

		Class<?> sVertexBuilder = Class.forName("net.optifine.shaders.SVertexBuilder");
		PUSH_ENTITY = lookup.unreflect(ReflectionHelper.findMethod(sVertexBuilder, "pushEntity", null, IBlockState.class, BlockPos.class, IBlockAccess.class, BufferBuilder.class));
		POP_ENTITY = lookup.unreflect(ReflectionHelper.findMethod(sVertexBuilder, "popEntity", null, BufferBuilder.class));

		Class<?> blockModelCustomizer = Class.forName("net.optifine.model.BlockModelCustomizer");
		Class<?> renderEnv = Class.forName("net.optifine.render.RenderEnv");
		GET_RENDER_MODEL = lookup.unreflect(ReflectionHelper.findMethod(blockModelCustomizer, "getRenderModel", null, IBakedModel.class, IBlockState.class, renderEnv));
		GET_RENDER_QUADS = lookup.unreflect(ReflectionHelper.findMethod(blockModelCustomizer, "getRenderQuads", null, List.class, IBlockAccess.class, IBlockState.class, BlockPos.class, EnumFacing.class, BlockRenderLayer.class, long.class, renderEnv));

		GET_RENDER_ENV = lookup.unreflect(ReflectionHelper.findMethod(BufferBuilder.class, "getRenderEnv", null, IBlockState.class, BlockPos.class));
	}

	private boolean Config_isShaders() {
		try {
			return (boolean) IS_SHADERS.invokeExact();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public boolean isChunkCacheOF(@Nullable Object obj) {
		return CHUNK_CACHE_OF.isInstance(obj);
	}

	@Override
	public ChunkCache getChunkRenderCache(IBlockAccess reader) {
		try {
			return (ChunkCache) CHUNK_CACHE_OF_CHUNK_CACHE.get(reader);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void pushShaderThing(
			IBlockState blockState,
			BlockPos pos,
			IBlockAccess reader,
			BufferBuilder bufferBuilder
	) {
		if (!Config_isShaders())
			return;
		try {
			PUSH_ENTITY.invokeExact(blockState, pos, reader, bufferBuilder);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public void popShaderThing(BufferBuilder bufferBuilder) {
		if (!Config_isShaders())
			return;
		try {
			POP_ENTITY.invokeExact(bufferBuilder);
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	@Override
	public Object getRenderEnv(BufferBuilder bufferBuilder, IBlockState blockState, BlockPos pos) {
		try {
			return GET_RENDER_ENV.invoke(bufferBuilder, blockState, pos);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IBakedModel getRenderModel(
			IBakedModel modelIn,
			IBlockState stateIn,
			Object renderEnv
	) {
		try {
			return (IBakedModel) GET_RENDER_MODEL.invoke(modelIn, stateIn, renderEnv);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<BakedQuad> getRenderQuads(
			List<BakedQuad> quads,
			IBlockAccess worldIn,
			IBlockState stateIn,
			BlockPos posIn,
			EnumFacing enumfacing,
			BlockRenderLayer layer,
			long rand,
			Object renderEnv
	) {
		try {
			return (List<BakedQuad>) GET_RENDER_QUADS.invoke(quads, worldIn, stateIn, posIn, enumfacing, layer, rand, renderEnv);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

}
