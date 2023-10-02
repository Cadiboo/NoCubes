package io.github.cadiboo.nocubes.client.optifine;

import io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo;
import io.github.cadiboo.nocubes.client.render.struct.PoseStack;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.util.List;

import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.BakedQuad_getQuadEmissive;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.BlockModelCustomizer_getRenderModel;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.BlockModelCustomizer_getRenderQuads;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.BufferBuilder_getRenderEnv;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.BufferBuilder_setBlockLayer;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.ChunkRender_postRenderOverlays;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.Config_isAlternateBlocks;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.Config_isShaders;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.ListQuadsOverlay_clear;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.ListQuadsOverlay_getBlockState;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.ListQuadsOverlay_getListQuadsSingle;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.ListQuadsOverlay_getQuad;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.ListQuadsOverlay_size;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.RenderEnv_getListQuadsOverlay;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.RenderEnv_isOverlaysRendered;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.RenderEnv_reset;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.RenderEnv_setOverlaysRendered;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.RenderEnv_setRegionRenderCacheBuilder;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.SVertexBuilder_popEntity;
import static io.github.cadiboo.nocubes.client.optifine.HD_U_G6.Reflect.SVertexBuilder_pushEntity;
import static io.github.cadiboo.nocubes.client.optifine.Reflector.tryGetMethod;
import static io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo.ColorSupplier;
import static io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo.QuadConsumer;

class HD_U_G6 implements OptiFineProxy {

	// From OptiFine's BlockModelRenderer
	static final BlockRenderLayer[] OVERLAY_LAYERS = new BlockRenderLayer[]{BlockRenderLayer.CUTOUT, BlockRenderLayer.CUTOUT_MIPPED, BlockRenderLayer.TRANSLUCENT};

	// If refactoring this to make a common base class, use reflection from HD_U_H5 proxy from before it was deleted 8/3/2022
	@Override
	public @Nullable String notUsableBecause() {
		for (Field field : Reflect.class.getDeclaredFields()) {
			try {
				if (field.get(null) == null)
					return "reflection was unable to find " + field.getName();
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Can't access my own fields...?", e);
			}
		}
		return null;
	}

	@Override
	public void preRenderChunk(RenderChunk chunkRender, BlockPos chunkPos, PoseStack matrix) {
		// Support Render Regions
//		matrix.translate(
//			ChunkRender_regionDX(chunkRender),
//			ChunkRender_regionDY(chunkRender),
//			ChunkRender_regionDZ(chunkRender)
//		);
	}

	@Override
	public long getSeed(long originalSeed) {
		return Config_isAlternateBlocks() ? 0 : originalSeed;
	}

	@Override
	public Object preRenderBlock(RenderChunk chunkRender, RegionRenderCacheBuilder builder, IBlockAccess chunkCacheOF, BlockRenderLayer renderType, BufferBuilder buffer, IBlockState state, BlockPos pos) {
		BufferBuilder_setBlockLayer(buffer, renderType);
		Object renderEnv = BufferBuilder_getRenderEnv(buffer, state, pos);
		RenderEnv_setRegionRenderCacheBuilder(renderEnv, builder);

		if (Config_isShaders()) {
			prePushShaderEntity(chunkRender, buffer, pos);
			SVertexBuilder_pushEntity(state, pos, chunkCacheOF, buffer);
		}
		return renderEnv;
	}

	protected void prePushShaderEntity(RenderChunk chunkRender, BufferBuilder buffer, BlockPos pos) {
	}

	@Override
	public void preRenderFluid(IBlockState state, BlockPos worldPos, IBlockAccess world, BufferBuilder buffer) {
		if (Config_isShaders())
			SVertexBuilder_pushEntity(state, worldPos, world, buffer);
	}

	@Override
	public IBakedModel getModel(Object renderEnv, IBakedModel originalModel, IBlockState state) {
		return BlockModelCustomizer_getRenderModel(originalModel, state, renderEnv);
	}

	@Override
	public void postRenderBlock(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, RegionRenderCacheBuilder buffers, boolean[] usedLayers) {
		if (Config_isShaders())
			SVertexBuilder_popEntity(buffer);

		if (RenderEnv_isOverlaysRendered(renderEnv)) {
			ChunkRender_postRenderOverlays(chunkRender, buffers, usedLayers);
			RenderEnv_setOverlaysRendered(renderEnv, false);
		}
	}

	@Override
	public void postRenderFluid(BufferBuilder buffer) {
		if (Config_isShaders())
			SVertexBuilder_popEntity(buffer);
	}

//	@Override
//	public void postRenderFluid(Object renderEnv, BufferBuilder buffer, RenderChunk chunkRender, RegionRenderCacheBuilder buffers, boolean[] usedLayers) {
//		this.postRenderBlock(renderEnv, buffer, chunkRender, buffers, usedLayers);
//	}

	@Override
	public @Nullable BakedQuad getQuadEmissive(BakedQuad quad) {
		return BakedQuad_getQuadEmissive(quad);
	}

	@Override
	public void preRenderQuad(Object renderEnv, BakedQuad quad, IBlockState state, BlockPos pos) {
		RenderEnv_reset(renderEnv, state, pos);
	}

	@Override
	public List<BakedQuad> getQuadsAndStoreOverlays(List<BakedQuad> quads, IBlockAccess world, IBlockState state, BlockPos worldPos, EnumFacing direction, BlockRenderLayer layer, long rand, Object renderEnv) {
		return BlockModelCustomizer_getRenderQuads(quads, world, state, worldPos, direction, layer, rand, renderEnv);
	}

	@Override
	public int forEachOverlayQuad(ChunkRenderInfo renderer, IBlockState state, BlockPos worldPos, ColorSupplier colorSupplier, QuadConsumer action, Object renderEnv) {
		int totalSize = 0;
		for (int i = 0; i < OVERLAY_LAYERS.length; i++) {
			BlockRenderLayer overlayLayer = OVERLAY_LAYERS[i];
			Object overlay = RenderEnv_getListQuadsOverlay(renderEnv, overlayLayer);
			int size = ListQuadsOverlay_size(overlay);
			if (size <= 0)
				continue;
			totalSize += size;
			BufferBuilder overlayBuffer = renderer.getAndStartBuffer(i, overlayLayer);
			for (int j = 0; j < size; ++j) {
				List<BakedQuad> quads = ListQuadsOverlay_getListQuadsSingle(overlay, ListQuadsOverlay_getQuad(overlay, j));
				IBlockState overlayState = ListQuadsOverlay_getBlockState(overlay, j);
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
	interface Reflect {

		MethodHandle isShaders = tryGetMethod("Config", "isShaders");
		MethodHandle isAlternateBlocks = tryGetMethod("Config", "isAlternateBlocks");

		MethodHandle pushEntity = tryGetMethod("net.optifine.shaders.SVertexBuilder", "pushEntity", IBlockState.class, BlockPos.class, IBlockAccess.class, BufferBuilder.class);
		MethodHandle popEntity = tryGetMethod("net.optifine.shaders.SVertexBuilder", "popEntity", BufferBuilder.class);

		MethodHandle postRenderOverlays = tryGetMethod(RenderChunk.class.getName(), "postRenderOverlays", RegionRenderCacheBuilder.class, CompiledChunk.class, boolean[].class);

		MethodHandle getQuadEmissive = tryGetMethod(BakedQuad.class.getName(), "getQuadEmissive");
		MethodHandle setBlockLayer = tryGetMethod(BufferBuilder.class.getName(), "setBlockLayer", BlockRenderLayer.class);
		MethodHandle getRenderEnv = tryGetMethod(BufferBuilder.class.getName(), "getRenderEnv", IBlockState.class, BlockPos.class);

		MethodHandle reset = tryGetMethod("net.optifine.render.RenderEnv", "reset", IBlockState.class, BlockPos.class);
		MethodHandle setRegionRenderCacheBuilder = tryGetMethod("net.optifine.render.RenderEnv", "setRegionRenderCacheBuilder", RegionRenderCacheBuilder.class);
		MethodHandle isOverlaysRendered = tryGetMethod("net.optifine.render.RenderEnv", "isOverlaysRendered");
		MethodHandle setOverlaysRendered = tryGetMethod("net.optifine.render.RenderEnv", "setOverlaysRendered", boolean.class);
		MethodHandle getListQuadsOverlay = tryGetMethod("net.optifine.render.RenderEnv", "getListQuadsOverlay", BlockRenderLayer.class);

		MethodHandle size = tryGetMethod("net.optifine.model.ListQuadsOverlay", "size");
		MethodHandle clear = tryGetMethod("net.optifine.model.ListQuadsOverlay", "clear");
		MethodHandle getQuad = tryGetMethod("net.optifine.model.ListQuadsOverlay", "getQuad", int.class);
		MethodHandle getBlockState = tryGetMethod("net.optifine.model.ListQuadsOverlay", "getBlockState", int.class);
		MethodHandle getListQuadsSingle = tryGetMethod("net.optifine.model.ListQuadsOverlay", "getListQuadsSingle", BakedQuad.class);

		MethodHandle getRenderModel = tryGetMethod("net.optifine.model.BlockModelCustomizer", "getRenderModel", IBakedModel.class, IBlockState.class, "net.optifine.render.RenderEnv");
		MethodHandle getRenderQuads = tryGetMethod("net.optifine.model.BlockModelCustomizer", "getRenderQuads", List.class, IBlockAccess.class, IBlockState.class, BlockPos.class, EnumFacing.class, BlockRenderLayer.class, long.class, "net.optifine.render.RenderEnv");

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

		static @Nullable BakedQuad BakedQuad_getQuadEmissive(BakedQuad quad) {
			try {
				return (BakedQuad) getQuadEmissive.invokeExact(quad);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void SVertexBuilder_pushEntity(IBlockState state, BlockPos worldPos, IBlockAccess world, BufferBuilder buffer) {
			try {
				pushEntity.invokeExact(state, worldPos, world, buffer);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void SVertexBuilder_popEntity(BufferBuilder buffer) {
			try {
				popEntity.invokeExact(buffer);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void RenderEnv_reset(Object renderEnv, IBlockState state, BlockPos pos) {
//			((RenderEnv) renderEnv).reset(state, pos);
			try {
				reset.invoke(renderEnv, state, pos);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static IBakedModel BlockModelCustomizer_getRenderModel(IBakedModel originalModel, IBlockState state, Object renderEnv) {
//			return BlockModelCustomizer.getRenderModel(originalModel, state, (RenderEnv) renderEnv);
			try {
				return (IBakedModel) getRenderModel.invoke(originalModel, state, renderEnv);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static List<BakedQuad> BlockModelCustomizer_getRenderQuads(List<BakedQuad> quads, IBlockAccess world, IBlockState state, BlockPos worldPos, EnumFacing direction, BlockRenderLayer layer, long rand, Object renderEnv) {
//			return BlockModelCustomizer.getRenderQuads(quads, world, state, worldPos, direction, layer, rand, renderEnv)
			try {
				return (List<BakedQuad>) getRenderQuads.invoke(quads, world, state, worldPos, direction, layer, rand, renderEnv);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void RenderEnv_setRegionRenderCacheBuilder(Object renderEnv, RegionRenderCacheBuilder buffers) {
//			((RenderEnv) renderEnv).setRegionRenderCacheBuilder(buffers);
			try {
				setRegionRenderCacheBuilder.invoke(renderEnv, buffers);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static Object BufferBuilder_getRenderEnv(BufferBuilder buffer, IBlockState state, BlockPos pos) {
//			return buffer.getRenderEnv(state, pos);
			try {
				return getRenderEnv.invoke(buffer, state, pos);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void BufferBuilder_setBlockLayer(BufferBuilder buffer, BlockRenderLayer renderType) {
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

		static void ChunkRender_postRenderOverlays(RenderChunk chunkRender, RegionRenderCacheBuilder buffers, boolean[] usedLayers) {
//			chunkRender.postRenderOverlays(buffers, compiledChunk, usedLayers);
			try {
				postRenderOverlays.invokeExact(chunkRender, buffers, chunkRender.compiledChunk, usedLayers);
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

		static Object RenderEnv_getListQuadsOverlay(Object renderEnv, BlockRenderLayer layer) {
//			return ((RenderEnv) renderEnv).getListQuadsOverlay(layer);
			try {
				return getListQuadsOverlay.invoke(renderEnv, layer);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static int ListQuadsOverlay_size(Object overlay) {
//			return overlay.size();
			try {
				return (int) size.invoke(overlay);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static List<BakedQuad> ListQuadsOverlay_getListQuadsSingle(Object overlay, BakedQuad quad) {
//			return overlay.getListQuadsSingle(quad);
			try {
				return (List<BakedQuad>) getListQuadsSingle.invoke(overlay, quad);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static BakedQuad ListQuadsOverlay_getQuad(Object overlay, int index) {
//			return overlay.getQuad(index);
			try {
				return (BakedQuad) getQuad.invoke(overlay, index);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static IBlockState ListQuadsOverlay_getBlockState(Object overlay, int index) {
//			return overlay.getBlockState(index);
			try {
				return (IBlockState) getBlockState.invoke(overlay, index);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

		static void ListQuadsOverlay_clear(Object overlay) {
//			overlay.clear();
			try {
				clear.invoke(overlay);
			} catch (Throwable t) {
				throw new RuntimeException(t);
			}
		}

	}

}
