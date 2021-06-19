package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.RollingProfiler;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineProxy;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender.RebuildTask;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.model.ListQuadsOverlay;
import net.optifine.render.RenderEnv;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.ClientUtil.vertex;
import static io.github.cadiboo.nocubes.client.render.MeshRenderer.OVERLAY_LAYERS;
import static net.minecraft.state.properties.BlockStateProperties.SNOWY;

/**
 * @author Cadiboo
 */
public final class RendererDispatcher {

	private static final RollingProfiler totalProfiler = new RollingProfiler(256);
	private static final RollingProfiler fluidsProfiler = new RollingProfiler(256);
	private static final RollingProfiler meshProfiler = new RollingProfiler(256);

	public static class ChunkRenderInfo {
		public final RebuildTask rebuildTask;
		public final ChunkRender chunkRender;
		public final CompiledChunk compiledChunk;
		public final RegionRenderCacheBuilder buffers;
		public final BlockPos chunkPos;
		public final IBlockDisplayReader world;
		public final FluentMatrixStack matrix;
		public final Random random;
		public final BlockRendererDispatcher dispatcher;
		public final LightCache light;
		public final OptiFineProxy optiFine;
		public final BlockColors blockColors;

		public ChunkRenderInfo(RebuildTask rebuildTask, ChunkRender chunkRender, CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, BlockPos chunkPos, IBlockDisplayReader world, FluentMatrixStack matrix, Random random, BlockRendererDispatcher dispatcher, LightCache light, OptiFineProxy optiFine) {
			this.rebuildTask = rebuildTask;
			this.chunkRender = chunkRender;
			this.compiledChunk = compiledChunk;
			this.buffers = buffers;
			this.chunkPos = chunkPos;
			this.world = world;
			this.matrix = matrix;
			this.random = random;
			this.dispatcher = dispatcher;
			this.light = light;
			this.optiFine = optiFine;
			this.blockColors = Minecraft.getInstance().getBlockColors();
		}

		public void renderInLayers(
			Predicate<RenderType> predicate,
			BiFunction<RenderType, BufferBuilder, Object> preRender,
			RenderInLayer render,
			BiConsumer<BufferBuilder, Object> postRender
		) {
			RendererDispatcher.renderInLayers(
				chunkRender, compiledChunk, buffers, optiFine,
				predicate,
				preRender,
				render,
				postRender
			);
		}

		public Color getColor(Color color, BakedQuad quad, BlockState state, BlockPos pos, float shade) {
			if (!quad.isTinted()) {
				color.red = shade;
				color.green = shade;
				color.blue = shade;
//				color.alpha = 1.0F;
				return color;
			}
			int packedColor = blockColors.getColor(state, world, pos, quad.getTintIndex());
			color.red = (float) (packedColor >> 16 & 255) / 255.0F * shade;
			color.green = (float) (packedColor >> 8 & 255) / 255.0F * shade;
			color.blue = (float) (packedColor & 255) / 255.0F * shade;
//			color.alpha = 1.0F;
			return color;
		}

		public void renderBlock(BlockState state, BlockPos.Mutable worldPos) {
			IModelData modelData = rebuildTask.getModelData(worldPos);
			RendererDispatcher.renderInLayers(
				chunkRender, compiledChunk, buffers, optiFine,
				layer -> RenderTypeLookup.canRenderInLayer(state, layer),
				(layer, buffer) -> optiFine.preRenderBlock(chunkRender, buffers, world, layer, buffer, state, worldPos),
				(layer, buffer, renderEnv) -> dispatcher.renderModel(state, worldPos, world, matrix.matrix, buffer, false, random, modelData),
				(buffer, renderEnv) -> optiFine.postRenderBlock(renderEnv, buffer, chunkRender, buffers, compiledChunk)
			);
		}

		public float getShade(Direction direction) {
			return world.getShade(direction, true);
		}

		interface ColorSupplier {
			Color apply(BlockState state, BlockPos worldPos, BakedQuad quad);
		}

		interface QuadConsumer {
			void accept(RenderType layer, IVertexBuilder buffer, BakedQuad quad, Color color, boolean emissive);
		}

		public void forEachQuad(BlockState state, BlockPos worldPos, Direction direction, ColorSupplier colorSupplier, QuadConsumer action) {
			long rand = optiFine.getSeed(state.getSeed(worldPos));
			IModelData modelData = rebuildTask.getModelData(worldPos);
			renderInLayers(
				layer -> RenderTypeLookup.canRenderInLayer(state, layer),
				(layer, buffer) -> optiFine.preRenderBlock(chunkRender, buffers, world, layer, buffer, state, worldPos),
				(layer, buffer, renderEnv) -> {
					IBakedModel model = dispatcher.getBlockModel(state);
					model = optiFine.getModel(renderEnv, model, state);

					random.setSeed(rand);
					List<BakedQuad> nullQuads = model.getQuads(state, null, random, modelData);
					nullQuads = BlockModelCustomizer.getRenderQuads(nullQuads, world, state, worldPos, null, layer, rand, (RenderEnv) renderEnv);
					boolean anyQuadsFound = forEachQuad(nullQuads, state, worldPos, colorSupplier, layer, buffer, renderEnv, action);

					random.setSeed(rand);
					Direction renderDir;
					List<BakedQuad> dirQuads;
					if (!state.hasProperty(SNOWY))
						dirQuads = model.getQuads(state, renderDir = direction, random, modelData);
					else {
						// Make grass/snow/mycilium side faces be rendered with their top texture
						// Equivalent to OptiFine's Better Grass feature
						if (!state.getValue(SNOWY))
							dirQuads = model.getQuads(state, (renderDir = NoCubesConfig.Client.betterGrassSides ? Direction.UP : direction), random, modelData);
						else {
							// The texture of grass underneath the snow (that normally never gets seen) is grey, we don't want that
							BlockState snow = Blocks.SNOW.defaultBlockState();
							dirQuads = Minecraft.getInstance().getBlockRenderer().getBlockModel(snow).getQuads(snow, renderDir = null, random, modelData);
						}
					}
					dirQuads = BlockModelCustomizer.getRenderQuads(dirQuads, world, state, worldPos, renderDir, layer, rand, (RenderEnv) renderEnv);
					anyQuadsFound |= forEachQuad(dirQuads, state, worldPos, colorSupplier, layer, buffer, renderEnv, action);

					for (int i = 0; i < OVERLAY_LAYERS.length; i++) {
						RenderType overlayLayer = OVERLAY_LAYERS[i];
						ListQuadsOverlay listQuadsOverlay = ((RenderEnv) renderEnv).getListQuadsOverlay(overlayLayer);
						int size = listQuadsOverlay.size();
						if (size <= 0)
							continue;
						BufferBuilder overlayBuffer = getAndStartBuffer(chunkRender, compiledChunk, buffers, overlayLayer);
						for (int j = 0; j < size; ++j) {
							List<BakedQuad> quads = listQuadsOverlay.getListQuadsSingle(listQuadsOverlay.getQuad(j));
							anyQuadsFound |= forEachQuad(quads, listQuadsOverlay.getBlockState(j), worldPos, colorSupplier, overlayLayer, overlayBuffer, renderEnv, action);
							((RenderEnv) renderEnv).reset(state, worldPos);
						}
						listQuadsOverlay.clear();
					}

					if (!anyQuadsFound)
						forEachQuad(getMissingQuads(), state, worldPos, colorSupplier, layer, buffer, renderEnv, action);
					return true;
				},
				(buffer, renderEnv) -> optiFine.postRenderBlock(renderEnv, buffer, chunkRender, buffers, compiledChunk)
			);

		}

		private boolean forEachQuad(List<BakedQuad> quads, BlockState state, BlockPos worldPos, ColorSupplier colorSupplier, RenderType layer, BufferBuilder buffer, Object renderEnv, QuadConsumer action) {
			int i = 0;
			for (; i < quads.size(); i++) {
				BakedQuad quad = quads.get(i);
				Color color = colorSupplier.apply(state, worldPos, quad);
				BakedQuad emissive = optiFine == null ? null : optiFine.getQuadEmissive(quad);
				if (emissive != null) {
					optiFine.preRenderQuad(renderEnv, emissive, state, worldPos);
					action.accept(layer, buffer, quad, color, true);
				}
				if (optiFine != null)
					optiFine.preRenderQuad(renderEnv, quad, state, worldPos);
				action.accept(layer, buffer, quad, color, false);
			}
			return i > 0;
		}

		private List<BakedQuad> getMissingQuads() {
			return dispatcher.getBlockModelShaper().getModelManager().getMissingModel().getQuads(Blocks.AIR.defaultBlockState(), null, random);
		}
	}

	public static void renderChunk(
		RebuildTask rebuildTask,
		ChunkRender chunkRender,
		CompiledChunk compiledChunk,
		RegionRenderCacheBuilder buffers,
		BlockPos chunkPos,
		IBlockDisplayReader world,
		MatrixStack matrixStack,
		Random random,
		BlockRendererDispatcher dispatcher
	) {
		long start = System.nanoTime();
		FluentMatrixStack matrix = new FluentMatrixStack(matrixStack);
		try (
			LightCache light = new LightCache(Minecraft.getInstance().level, chunkPos, ModUtil.CHUNK_SIZE);
			FluentMatrixStack ignored = matrix.push()
		) {
			OptiFineProxy optiFine = OptiFineCompatibility.proxy();
			// Matrix stack is translated to the start of the chunk
			optiFine.preRenderChunk(chunkRender, chunkPos, matrixStack);
			ChunkRenderInfo renderer = new ChunkRenderInfo(rebuildTask, chunkRender, compiledChunk, buffers, chunkPos, world, matrix, random, dispatcher, light, optiFine);
			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;

			renderChunkFluids(renderer);

			if (NoCubesConfig.Client.render)
				renderChunkMesh(renderer, isSmoothable);
		}
		totalProfiler.recordAndLogElapsedNanosChunk(start, "total");
	}

	public static void renderBreakingTexture(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData) {
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		try (Area area = new Area(Minecraft.getInstance().level, pos, ModUtil.VEC_ONE, generator)) {
			MeshRenderer.renderBreakingTexture(state, pos, matrix, buffer, generator, area);
		}
	}

	private static void renderChunkFluids(ChunkRenderInfo renderer) {
//		long start = System.nanoTime();
//		try (Area area = new Area(Minecraft.getInstance().level, chunkPos, ModUtil.CHUNK_SIZE)) {
//			FluidRenderer.render(area, light, (relativePos, fluid, block, fluidRenderer) -> {
//				renderInLayers(
//					chunkRender, compiledChunk, buffers, world, matrix, light, optiFine,
//					layer -> RenderTypeLookup.canRenderInLayer(fluid, layer),
//					(layer, buffer) -> optiFine.preRenderFluid(chunkRender, buffers, world, layer, buffer, fluid, block, worldPos),
//					(buffer, renderEnv) -> fluidRenderer.calcFaces((face, uvs) -> {
//						quad(
//							buffer, matrix, false,
//							face.v0.x, face.v0.y, face.v0.z
//						);
//					},
//					(buffer, renderEnv) -> optiFine.postRenderFluid(renderEnv, buffer, chunkRender, buffers, compiledChunk)
//				);
//			});
//		}
//		fluidsProfiler.recordAndLogElapsedNanosChunk(start, "fluids");
	}

	private static void renderChunkMesh(ChunkRenderInfo renderer, Predicate<BlockState> isSmoothable) {
		long start = System.nanoTime();
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		try (
			Area area = new Area(Minecraft.getInstance().level, renderer.chunkPos, ModUtil.CHUNK_SIZE, generator);
			FluentMatrixStack ignored = renderer.matrix.push()
		) {
			MeshRenderer.renderArea(renderer, isSmoothable, generator, area);
		}
		meshProfiler.recordAndLogElapsedNanosChunk(start, "mesh");
	}

	interface RenderInLayer {
		boolean render(RenderType layer, BufferBuilder buffer, Object renderEnv);
	}

	public static void renderInLayers(
		ChunkRender chunkRender, CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, OptiFineProxy optiFine,
		Predicate<RenderType> predicate,
		BiFunction<RenderType, BufferBuilder, Object> preRender,
		RenderInLayer render,
		BiConsumer<BufferBuilder, Object> postRender
	) {
		List<RenderType> chunkBufferLayers = RenderType.chunkBufferLayers();
		for (int i = 0, chunkBufferLayersSize = chunkBufferLayers.size(); i < chunkBufferLayersSize; i++) {
			RenderType layer = chunkBufferLayers.get(i);
			if (!predicate.test(layer))
				continue;

			BufferBuilder buffer = getAndStartBuffer(chunkRender, compiledChunk, buffers, layer);
			Object renderEnv = preRender.apply(layer, buffer);
			boolean used = render.render(layer, buffer, renderEnv);

			postRender.accept(buffer, renderEnv);
			if (used)
				markLayerUsed(compiledChunk, optiFine, layer);
		}
	}

	static BufferBuilder getAndStartBuffer(ChunkRender chunkRender, CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, RenderType layer) {
		ForgeHooksClient.setRenderLayer(layer);
		BufferBuilder buffer = buffers.builder(layer);

		if (compiledChunk.hasLayer.add(layer))
			chunkRender.beginLayer(buffer);
		return buffer;
	}

	static void markLayerUsed(CompiledChunk compiledChunk, OptiFineProxy optiFine, RenderType layer) {
		compiledChunk.isCompletelyEmpty = false;
		optiFine.markRenderLayerUsed(compiledChunk, layer);
	}

	static void quad(
		IVertexBuilder buffer, MatrixStack matrix,
		Face face, Vec faceNormal,
		Color color,
		Texture uvs,
		FaceLight light,
		boolean doubleSided
	) {
		quad(
			buffer, matrix, doubleSided,
			face, color, uvs, OverlayTexture.NO_OVERLAY, light, faceNormal
		);
	}

	static void quad(
		IVertexBuilder buffer, MatrixStack matrix, boolean doubleSided,
		Face face, Color color, Texture texture, int overlay, FaceLight light, Vec normal
	) {
		quad(
			buffer, matrix, doubleSided,
			face.v0, color, texture.u0, texture.v0, overlay, light.v0, normal,
			face.v1, color, texture.u1, texture.v1, overlay, light.v1, normal,
			face.v2, color, texture.u2, texture.v2, overlay, light.v2, normal,
			face.v3, color, texture.u3, texture.v3, overlay, light.v3, normal
		);
	}

	static void quad(
		IVertexBuilder buffer, MatrixStack matrix, boolean doubleSided,
		Vec vec0, Color color0, float u0, float v0, int overlay0, int light0, Vec normal0,
		Vec vec1, Color color1, float u1, float v1, int overlay1, int light1, Vec normal1,
		Vec vec2, Color color2, float u2, float v2, int overlay2, int light2, Vec normal2,
		Vec vec3, Color color3, float u3, float v3, int overlay3, int light3, Vec normal3
	) {
		quad(
			buffer, matrix, doubleSided,
			vec0.x, vec0.y, vec0.z, color0.red, color0.green, color0.blue, color0.alpha, u0, v0, overlay0, light0, normal0.x, normal0.y, normal0.z,
			vec1.x, vec1.y, vec1.z, color1.red, color1.green, color1.blue, color1.alpha, u1, v1, overlay1, light1, normal1.x, normal1.y, normal1.z,
			vec2.x, vec2.y, vec2.z, color2.red, color2.green, color2.blue, color2.alpha, u2, v2, overlay2, light2, normal2.x, normal2.y, normal2.z,
			vec3.x, vec3.y, vec3.z, color3.red, color3.green, color3.blue, color3.alpha, u3, v3, overlay3, light3, normal3.x, normal3.y, normal3.z
		);
	}

	static void quad(
		IVertexBuilder buffer, MatrixStack matrix, boolean doubleSided,
		float v0x, float v0y, float v0z, float red0, float green0, float blue0, float alpha0, float u0, float v0, int overlay0, int light0, float n0x, float n0y, float n0z,
		float v1x, float v1y, float v1z, float red1, float green1, float blue1, float alpha1, float u1, float v1, int overlay1, int light1, float n1x, float n1y, float n1z,
		float v2x, float v2y, float v2z, float red2, float green2, float blue2, float alpha2, float u2, float v2, int overlay2, int light2, float n2x, float n2y, float n2z,
		float v3x, float v3y, float v3z, float red3, float green3, float blue3, float alpha3, float u3, float v3, int overlay3, int light3, float n3x, float n3y, float n3z
	) {
		vertex(buffer, matrix, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, overlay0, light0, n0x, n0y, n0z);
		vertex(buffer, matrix, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, overlay1, light1, n1x, n1y, n1z);
		vertex(buffer, matrix, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, overlay2, light2, n2x, n2y, n2z);
		vertex(buffer, matrix, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, overlay3, light3, n3x, n3y, n3z);
		if (doubleSided) {
			vertex(buffer, matrix, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, overlay3, light3, n3x, n3y, n3z);
			vertex(buffer, matrix, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, overlay2, light2, n2x, n2y, n2z);
			vertex(buffer, matrix, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, overlay1, light1, n1x, n1y, n1z);
			vertex(buffer, matrix, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, overlay0, light0, n0x, n0y, n0z);
		}
	}

}
