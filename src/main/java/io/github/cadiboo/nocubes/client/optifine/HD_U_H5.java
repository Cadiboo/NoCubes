package io.github.cadiboo.nocubes.client.optifine;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.cadiboo.nocubes.client.render.RendererDispatcher;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.cadiboo.nocubes.client.optifine.HD_U_G8.OVERLAY_LAYERS;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G8.Reflect.*;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_H5.Reflect.BakedQuad_getQuadEmissive;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_H5.Reflect.BufferBuilder_getRenderEnv;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_H5.Reflect.BufferBuilder_setBlockLayer;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_H5.Reflect.BufferBuilder_setMidBlock;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_H5.Reflect.ChunkRender_postRenderOverlays;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_H5.Reflect.ChunkRender_regionDX;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_H5.Reflect.ChunkRender_regionDY;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_H5.Reflect.ChunkRender_regionDZ;
import static io.github.cadiboo.nocubes.client.optifine.Reflector.tryGetField;
import static io.github.cadiboo.nocubes.client.optifine.Reflector.tryGetMethod;

/**
 * This is a copy of HD_U_G8 but with reflection names updated to 1.18.
 */
class HD_U_H5 implements OptiFineProxy {

	@Override
	public @Nullable String notUsableBecause() {
		var declaredFields = Arrays.stream(Reflect.class.getDeclaredFields()).collect(Collectors.toMap(Field::getName, f -> f));
		for (var field : Reflect.class.getFields()) {
			try {
				if (field.get(null) == null) {
					if (field.getDeclaringClass() != Reflect.class) {
						var declaredField = declaredFields.get(field.getName());
						if (declaredField != null && declaredField.get(null) != null)
							continue; // "Overridden" field
					}
					return "reflection was unable to find field " + field.getName();
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Can't access my own fields...?", e);
			}
		}
		return null;
	}

	@Override
	public void preRenderChunk(RenderChunk chunkRender, BlockPos chunkPos, PoseStack matrix) {
		// Support Render Regions
		matrix.translate(
			ChunkRender_regionDX(chunkRender),
			ChunkRender_regionDY(chunkRender),
			ChunkRender_regionDZ(chunkRender)
		);
	}

	@Override
	public long getSeed(long originalSeed) {
		return Config_isAlternateBlocks() ? 0 : originalSeed;
	}

	@Override
	public Object preRenderBlock(RenderChunk chunkRender, ChunkBufferBuilderPack builder, BlockAndTintGetter chunkCacheOF, RenderType renderType, BufferBuilder buffer, BlockState state, BlockPos pos) {
		BufferBuilder_setBlockLayer(buffer, renderType);
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
	public Object preRenderFluid(RenderChunk chunkRender, ChunkBufferBuilderPack buffers, BlockAndTintGetter chunkCache, RenderType layer, BufferBuilder buffer, BlockState block, FluidState fluid, BlockPos worldPos) {
		return this.preRenderBlock(chunkRender, buffers, chunkCache, layer, buffer, block, worldPos);
	}

	@Override
	public BakedModel getModel(Object renderEnv, BakedModel originalModel, BlockState state) {
		return BlockModelCustomizer_getRenderModel(originalModel, state, renderEnv);
	}

	@Override
	public void postRenderBlock(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, ChunkBufferBuilderPack builder, CompiledChunk compiledChunk) {
		if (Config_isShaders())
			SVertexBuilder_popEntity(buffer);

		if (RenderEnv_isOverlaysRendered(renderEnv)) {
			ChunkRender_postRenderOverlays(chunkRender, builder, compiledChunk);
			RenderEnv_setOverlaysRendered(renderEnv, false);
		}
	}

	@Override
	public void postRenderFluid(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, ChunkBufferBuilderPack builder, CompiledChunk compiledChunk) {
		this.postRenderBlock(renderEnv, buffer, chunkRender, builder, compiledChunk);
	}

	@Override
	public @Nullable BakedQuad getQuadEmissive(BakedQuad quad) {
		return BakedQuad_getQuadEmissive(quad);
	}

	@Override
	public void preRenderQuad(Object renderEnv, BakedQuad quad, BlockState state, BlockPos pos) {
		RenderEnv_reset(renderEnv, state, pos);
	}

	@Override
	public void markRenderLayerUsed(CompiledChunk compiledChunk, RenderType renderType) {
		CompiledChunk_hasBlocks(compiledChunk).add(renderType);
		ChunkLayerSet_add(CompiledChunk_hasBlocks(compiledChunk), renderType);
	}

	@Override
	public List<BakedQuad> getQuadsAndStoreOverlays(List<BakedQuad> quads, BlockAndTintGetter world, BlockState state, BlockPos worldPos, Direction direction, RenderType layer, long rand, Object renderEnv) {
		return BlockModelCustomizer_getRenderQuads(quads, world, state, worldPos, direction, layer, rand, renderEnv);
	}

	@Override public int forEachOverlayQuad(RendererDispatcher.ChunkRenderInfo renderer, BlockState state, BlockPos worldPos, RendererDispatcher.ChunkRenderInfo.ColorSupplier colorSupplier, RendererDispatcher.ChunkRenderInfo.QuadConsumer action, Object renderEnv) {
		int totalSize = 0;
		for (int i = 0; i < OVERLAY_LAYERS.length; i++) {
			RenderType overlayLayer = OVERLAY_LAYERS[i];
			Object overlay = RenderEnv_getListQuadsOverlay(renderEnv, overlayLayer);
			int size = ListQuadsOverlay_size(overlay);
			if (size <= 0)
				continue;
			totalSize += size;
			BufferBuilder overlayBuffer = renderer.getAndStartBuffer(overlayLayer);
			for (int j = 0; j < size; ++j) {
				List<BakedQuad> quads = ListQuadsOverlay_getListQuadsSingle(overlay, ListQuadsOverlay_getQuad(overlay, j));
				BlockState overlayState = ListQuadsOverlay_getBlockState(overlay, j);
				renderer.forEachQuad(quads, overlayState, worldPos, colorSupplier, overlayLayer, overlayBuffer, renderEnv, action);
				RenderEnv_reset(renderEnv, overlayState, worldPos);
			}
			ListQuadsOverlay_clear(overlay);
			renderer.markLayerUsed(overlayLayer);
		}
		return totalSize;
	}

	// All reflection stuff can be null but we check beforehand
	@SuppressWarnings("ConstantConditions")
	interface Reflect extends HD_U_G8.Reflect {

		MethodHandle postRenderOverlays = tryGetMethod("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk", "postRenderOverlays", ChunkBufferBuilderPack.class, CompiledChunk.class);
		Field regionDX = tryGetField("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk", "regionDX");
		Field regionDY = tryGetField("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk", "regionDY");
		Field regionDZ = tryGetField("net.minecraft.client.renderer.chunk.ChunkRenderDispatcher$RenderChunk", "regionDZ");

		MethodHandle getQuadEmissive = tryGetMethod("net.minecraft.client.renderer.block.model.BakedQuad", "getQuadEmissive");
		MethodHandle setBlockLayer = tryGetMethod("com.mojang.blaze3d.vertex.BufferBuilder", "setBlockLayer", RenderType.class);
		MethodHandle setMidBlock = tryGetMethod("com.mojang.blaze3d.vertex.BufferBuilder", "setMidBlock", float.class, float.class, float.class);
		MethodHandle getRenderEnv = tryGetMethod("com.mojang.blaze3d.vertex.BufferBuilder", "getRenderEnv", BlockState.class, BlockPos.class);

		static int ChunkRender_regionDX(RenderChunk chunkRender) {
			try {
				return regionDX.getInt(chunkRender);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static int ChunkRender_regionDY(RenderChunk chunkRender) {
			try {
				return regionDY.getInt(chunkRender);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static int ChunkRender_regionDZ(RenderChunk chunkRender) {
			try {
				return regionDZ.getInt(chunkRender);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static @Nullable BakedQuad BakedQuad_getQuadEmissive(BakedQuad quad) {
			try {
				return (BakedQuad) getQuadEmissive.invokeExact(quad);
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

		static Object BufferBuilder_getRenderEnv(BufferBuilder buffer, BlockState state, BlockPos pos) {
//			return buffer.getRenderEnv(state, pos);
			try {
				return getRenderEnv.invoke(buffer, state, pos);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void BufferBuilder_setBlockLayer(BufferBuilder buffer, RenderType renderType) {
//			buffer.setBlockLayer(renderType);
			try {
				setBlockLayer.invokeExact(buffer, renderType);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void ChunkRender_postRenderOverlays(RenderChunk chunkRender, ChunkBufferBuilderPack builder, CompiledChunk compiledChunk) {
//			chunkRender.postRenderOverlays(builder, compiledChunk);
			try {
				postRenderOverlays.invokeExact(chunkRender, builder, compiledChunk);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

	}

}
