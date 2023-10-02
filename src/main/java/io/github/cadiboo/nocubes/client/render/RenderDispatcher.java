package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.LightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineProxy;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.PoseStack;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES;
import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES_LENGTH;
import static io.github.cadiboo.nocubes.client.RenderHelper.vertex;

/**
 * @author Cadiboo
 */
public final class RenderDispatcher {

	/**
	 * A big blob of objects related to vanilla chunk rendering.
	 * Stops every method having lots of parameters.
	 */
	public static class ChunkRenderInfo {
		public final RenderChunk chunkRender;
		public final BlockPos chunkPos;
		public final ChunkCompileTaskGenerator chunkRenderTask;
		public final CompiledChunk compiledChunk;
		public final World world;
		public final IBlockAccess chunkRenderCache;
		public final boolean[] usedLayers;
		public final long random;
		public final BlockRendererDispatcher dispatcher;
		public final OptiFineProxy optiFine;
		public final BlockColors blockColors;

		public ChunkRenderInfo(
				RenderChunk chunkRender, BlockPos chunkPos, ChunkCompileTaskGenerator chunkRenderTask,
				CompiledChunk compiledChunk, World world, IBlockAccess chunkRenderCache,
				boolean[] usedLayers, long random, BlockRendererDispatcher dispatcher
		) {
			this.chunkRender = chunkRender;
			this.chunkPos = chunkPos;
			this.chunkRenderTask = chunkRenderTask;
			this.compiledChunk = compiledChunk;
			this.world = world;
			this.chunkRenderCache = chunkRenderCache;
			this.usedLayers = usedLayers;
			this.random = random;
			this.dispatcher = dispatcher;
			this.optiFine = OptiFineCompatibility.proxy();
			this.blockColors = Minecraft.getMinecraft().getBlockColors();
		}

		interface RenderInLayer {
			void render(IBlockState state, BlockPos worldPos, BlockRenderLayer layer, BufferBuilder buffer, Object renderEnv);
		}

		public void renderInBlockLayers(IBlockState state, BlockPos worldPos, RenderInLayer render) {
			for (int i = 0; i < BLOCK_RENDER_LAYER_VALUES_LENGTH; ++i) {
				BlockRenderLayer initialBlockRenderLayer = BLOCK_RENDER_LAYER_VALUES[i];
				if (!state.getBlock().canRenderInLayer(state, initialBlockRenderLayer)) {
					continue;
				}
				BlockRenderLayer correctedBlockRenderLayer = ClientUtil.getCorrectRenderLayer(initialBlockRenderLayer);
				int correctedBlockRenderLayerOrdinal = correctedBlockRenderLayer.ordinal();
				ForgeHooksClient.setRenderLayer(correctedBlockRenderLayer);
				BufferBuilder buffer = getAndStartBuffer(correctedBlockRenderLayerOrdinal, correctedBlockRenderLayer);

				Object renderEnv = optiFine.preRenderBlock(chunkRender, chunkRenderTask.getRegionRenderCacheBuilder(), world, correctedBlockRenderLayer, buffer, state, worldPos);
				render.render(state, worldPos, correctedBlockRenderLayer, buffer, renderEnv);

				optiFine.postRenderBlock(renderEnv, buffer, chunkRender, compiledChunk, chunkRenderTask.getRegionRenderCacheBuilder(), usedLayers);
				markLayerUsed(correctedBlockRenderLayer);
			}
			ForgeHooksClient.setRenderLayer(null);
		}

		public Color getColor(Color color, BakedQuad quad, IBlockState state, BlockPos pos, float shade) {
			if (!quad.hasTintIndex()) {
				color.red = shade;
				color.green = shade;
				color.blue = shade;
//				color.alpha = 1.0F;
				return color;
			}
			int packedColor = blockColors.colorMultiplier(state, world, pos, quad.getTintIndex());
			color.red = (float) (packedColor >> 16 & 255) / 255.0F * shade;
			color.green = (float) (packedColor >> 8 & 255) / 255.0F * shade;
			color.blue = (float) (packedColor & 255) / 255.0F * shade;
//			color.alpha = 1.0F;
			return color;
		}

		public void renderBlock(IBlockState stateIn, MutableBlockPos worldPosIn) {
			renderInBlockLayers(
					stateIn, worldPosIn,
					(state, worldPos, layer, buffer, renderEnv) -> dispatcher.renderBlock(state, worldPos, world, buffer)
			);
		}

		public float getShade(EnumFacing direction) {
//			return world.get(direction, true);
			return MeshRenderer.diffuseLight(direction);
		}

		public BufferBuilder getAndStartBuffer(int correctedBlockRenderLayerOrdinal, BlockRenderLayer correctedBlockRenderLayer) {
//			return RendererDispatcher.getAndStartBuffer(chunkRender, buffers, usedLayers, layer);
			return ClientUtil.startOrContinueBufferBuilder(chunkRenderTask, correctedBlockRenderLayerOrdinal, compiledChunk, correctedBlockRenderLayer, chunkRender, chunkPos);
		}

		public void markLayerUsed(BlockRenderLayer layer) {
			usedLayers[layer.ordinal()] = true;
		}

		public interface ColorSupplier {
			Color apply(IBlockState state, BlockPos worldPos, BakedQuad quad);
		}

		public interface QuadConsumer {
			void accept(BlockRenderLayer layer, BufferBuilder buffer, BakedQuad quad, Color color, boolean emissive);
		}

		public void forEachQuad(IBlockState stateIn, BlockPos worldPosIn, EnumFacing direction, ColorSupplier colorSupplier, QuadConsumer action) {
			long rand = random;
			renderInBlockLayers(
					stateIn, worldPosIn,
					(state, worldPos, layer, buffer, renderEnv) -> {
						IBakedModel model = getModel(state, renderEnv);

						List<BakedQuad> nullQuads = getQuadsAndStoreOverlays(state, worldPos, rand, layer, renderEnv, model, null);
						boolean anyQuadsFound = forEachQuad(nullQuads, state, worldPos, colorSupplier, layer, buffer, renderEnv, action);

						List<BakedQuad> dirQuads;
						if (!state.getProperties().containsKey(BlockGrass.SNOWY))
							dirQuads = getQuadsAndStoreOverlays(state, worldPos, rand, layer, renderEnv, model, direction);
						else {
							// Make grass/snow/mycilium side faces be rendered with their top texture
							// Equivalent to OptiFine's Better Grass feature
							if (!state.getValue(BlockGrass.SNOWY))
								dirQuads = getQuadsAndStoreOverlays(state, worldPos, rand, layer, renderEnv, model, NoCubesConfig.Client.betterGrassSides ? EnumFacing.UP : direction);
							else {
								// The texture of grass underneath the snow (that normally never gets seen) is grey, we don't want that
								IBlockState snow = Blocks.SNOW.getDefaultState();
								IBakedModel snowModel = getModel(snow, renderEnv);
								dirQuads = getQuadsAndStoreOverlays(snow, worldPos, rand, layer, renderEnv, snowModel, null);
							}
						}
						anyQuadsFound |= forEachQuad(dirQuads, state, worldPos, colorSupplier, layer, buffer, renderEnv, action);

						int numOverlaysRendered = optiFine.forEachOverlayQuad(this, state, worldPos, colorSupplier, action, renderEnv);
						anyQuadsFound |= numOverlaysRendered > 0;

						if (!anyQuadsFound)
							forEachQuad(getMissingQuads(), state, worldPos, colorSupplier, layer, buffer, renderEnv, action);
					}
			);
		}

		private IBakedModel getModel(IBlockState state, Object renderEnv) {
			IBakedModel model = dispatcher.getModelForState(state);
			model = optiFine.getModel(renderEnv, model, state);
			return model;
		}

		private List<BakedQuad> getQuadsAndStoreOverlays(IBlockState state, BlockPos worldPos, long rand, BlockRenderLayer layer, Object renderEnv, IBakedModel model, EnumFacing direction) {
//			random.setSeed(rand);
			List<BakedQuad> quads = model.getQuads(state, direction, random);
			quads = optiFine.getQuadsAndStoreOverlays(quads, world, state, worldPos, direction, layer, rand, renderEnv);
			return quads;
		}

		public boolean forEachQuad(List<BakedQuad> quads, IBlockState state, BlockPos worldPos, ColorSupplier colorSupplier, BlockRenderLayer layer, BufferBuilder buffer, Object renderEnv, QuadConsumer action) {
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
			return dispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(Blocks.AIR.getDefaultState(), EnumFacing.UP, random);
		}
	}

	// Use World for eagerly generated caches
	// Use RenderChunkCache for lazily generated caches
	public static void renderChunk(
			RenderChunk chunkRender, BlockPos chunkRenderPos, ChunkCompileTaskGenerator chunkRenderTask,
			CompiledChunk compiledChunk, World world, IBlockAccess chunkRenderCache,
			boolean[] usedLayers, Random random, BlockRendererDispatcher dispatcher
	) {
		ChunkRenderInfo renderer = new ChunkRenderInfo(
			chunkRender, chunkRenderPos, chunkRenderTask,
			compiledChunk, world, chunkRenderCache,
			usedLayers, 0, dispatcher
		);
		Predicate<IBlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		if (NoCubesConfig.Client.render)
			renderChunkMesh(renderer, isSmoothable);
	}

	public static void renderBreakingTexture(Tessellator tessellatorIn, BufferBuilder buffer, BlockPos pos, IBlockState state, IBlockAccess world, TextureAtlasSprite texture) {
		if (state.getRenderType() != EnumBlockRenderType.MODEL)
			return;

		// Draw tessellator and start again with color
		tessellatorIn.draw();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		Mesher mesher = NoCubesConfig.Server.mesher;
		try (Area area = new Area(world, pos, ModUtil.VEC_ONE, mesher)) {
			MeshRenderer.renderBreakingTexture(state, pos, new PoseStack(), buffer, texture, mesher, area);
		} finally {
			// Draw tessellator and start again without color
			tessellatorIn.draw();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			buffer.noColor();
		}
	}

	private static void renderChunkMesh(ChunkRenderInfo renderer, Predicate<IBlockState> isSmoothable) {
		long start = System.nanoTime();
		Mesher mesher = NoCubesConfig.Server.mesher;
		try (
				Area area = new Area(Minecraft.getMinecraft().world, renderer.chunkPos, ModUtil.CHUNK_SIZE, mesher);
				LightCache light = LightCache.retain(area);
				//var ignored = renderer.matrix.push()
		) {
			ExtendedFluidChunkRenderer.renderArea(renderer, isSmoothable, mesher, area, light);
			MeshRenderer.renderArea(renderer, isSmoothable, mesher, area, light);
		}
//		meshProfiler.recordAndLogElapsedNanosChunk(start, "mesh");
	}

//	public static BufferBuilder getAndStartBuffer(RenderChunk chunkRender, ChunkBufferBuilderPack buffers, Set<RenderType> usedLayers, RenderType layer) {
//		BufferBuilder buffer = buffers.builder(layer);
//		if (usedLayers.add(layer))
//			chunkRender.beginLayer(buffer);
//		return buffer;
//	}

	public static void quad(
			BufferBuilder buffer, PoseStack matrix,
			Face face, Vec faceNormal,
			Color color,
			Texture uvs,
			FaceLight light,
			boolean doubleSided
	) {
		quad(
				buffer, matrix, doubleSided,
				face, color, uvs, light, faceNormal
		);
	}

	static void quad(
			BufferBuilder buffer, PoseStack matrix, boolean doubleSided,
			Face face, Color color, Texture texture, FaceLight light, Vec normal
	) {
		quad(
				buffer, matrix, doubleSided,
				face.v0, color, texture.u0, texture.v0, light.v0,
				face.v1, color, texture.u1, texture.v1, light.v1,
				face.v2, color, texture.u2, texture.v2, light.v2,
				face.v3, color, texture.u3, texture.v3, light.v3
		);
	}

	static void quad(
			BufferBuilder buffer, PoseStack matrix, boolean doubleSided,
			Vec vec0, Color color0, float u0, float v0, int light0,
			Vec vec1, Color color1, float u1, float v1, int light1,
			Vec vec2, Color color2, float u2, float v2, int light2,
			Vec Vec, Color color3, float u3, float v3, int light3
	) {
		quad(
				buffer, matrix, doubleSided,
				vec0.x, vec0.y, vec0.z, color0.red, color0.green, color0.blue, color0.alpha, u0, v0, light0,
				vec1.x, vec1.y, vec1.z, color1.red, color1.green, color1.blue, color1.alpha, u1, v1, light1,
				vec2.x, vec2.y, vec2.z, color2.red, color2.green, color2.blue, color2.alpha, u2, v2, light2,
				Vec.x, Vec.y, Vec.z, color3.red, color3.green, color3.blue, color3.alpha, u3, v3, light3
		);
	}

	static void quad(
			BufferBuilder buffer, PoseStack matrix, boolean doubleSided,
			float v0x, float v0y, float v0z, float red0, float green0, float blue0, float alpha0, float u0, float v0, int light0,
			float v1x, float v1y, float v1z, float red1, float green1, float blue1, float alpha1, float u1, float v1, int light1,
			float v2x, float v2y, float v2z, float red2, float green2, float blue2, float alpha2, float u2, float v2, int light2,
			float v3x, float v3y, float v3z, float red3, float green3, float blue3, float alpha3, float u3, float v3, int light3
	) {
		vertex(buffer, matrix, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, light0);
		vertex(buffer, matrix, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, light1);
		vertex(buffer, matrix, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, light2);
		vertex(buffer, matrix, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, light3);
		if (doubleSided) {
			vertex(buffer, matrix, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, light0);
			vertex(buffer, matrix, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, light3);
			vertex(buffer, matrix, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, light2);
			vertex(buffer, matrix, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, light1);
		}
	}

}
