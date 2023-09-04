package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientCacheUtil;
import io.github.cadiboo.nocubes.client.LazyBlockColorCache;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.UVHelper;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineProxy;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.util.IsSmoothable.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeX;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeY;
import static io.github.cadiboo.nocubes.util.ModUtil.getMeshSizeZ;

/**
 * @author Cadiboo
 */
public final class RenderDispatcher {

	private static final Predicate<IBlockState> IS_BLOCK_STATE_GRASS = blockState -> ModUtil.isMaterialGrass(blockState.getMaterial());
	private static final Predicate<IBlockState> IS_BLOCK_STATE_LEAVES = blockState -> ModUtil.isMaterialLeaves(blockState.getMaterial());
	private static final Predicate<IBlockState> ALWAYS_TRUE = blockState -> true;


	/**
	 * A big blob of objects related to vanilla chunk rendering.
	 * Stops every method having lots of parameters.
	 */
	public static class ChunkRenderInfo {
		public final RenderChunk chunkRender;
		public final BlockPos chunkRenderPos;
		public final ChunkCompileTaskGenerator chunkRenderTask;
		public final CompiledChunk compiledChunk;
		public final World world;
		public final IBlockAccess chunkRenderCache;
		public final boolean[] usedLayers;
		public final Random random;
		public final BlockRendererDispatcher dispatcher;
		public final OptiFineProxy optiFine;
		public final BlockColors blockColors;

		public ChunkRenderInfo(
				RenderChunk chunkRender, BlockPos chunkRenderPos, ChunkCompileTaskGenerator chunkRenderTask,
				CompiledChunk compiledChunk, World world, IBlockAccess chunkRenderCache,
				boolean[] usedLayers, Random random, BlockRendererDispatcher dispatcher
		) {
			this.chunkRender = chunkRender;
			this.chunkRenderPos = chunkRenderPos;
			this.chunkRenderTask = chunkRenderTask;
			this.compiledChunk = compiledChunk;
			this.world = world;
			this.chunkRenderCache = chunkRenderCache;
			this.usedLayers = usedLayers;
			this.random = random;
			this.dispatcher = dispatcher;
			this.optiFine = OptiFineCompatibility.PROXY;
			this.blockColors = Minecraft.getMinecraft().getBlockColors();
		}

		interface RenderInLayer {
			void render(IBlockState state, BlockPos worldPos, BlockRenderLayer layer, BufferBuilder buffer, Object renderEnv);
		}

		public void renderInBlockLayers(IBlockState state, BlockPos worldPos, RenderInLayer render) {
			var layers = ItemBlockRenderTypes.getRenderLayers(state);
			for (var layer : layers) {
				var buffer = getAndStartBuffer(layer);
				var renderEnv = optiFine.preRenderBlock(chunkRender, buffers, world, layer, buffer, state, worldPos);
				render.render(state, worldPos, layer, buffer, renderEnv);

				optiFine.postRenderBlock(renderEnv, buffer, chunkRender, buffers, usedLayers);
				markLayerUsed(layer);
			}
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

		public void renderBlock(BlockState stateIn, MutableBlockPos worldPosIn) {
			renderInBlockLayers(
					stateIn, worldPosIn,
					(state, worldPos, modelData, layer, buffer, renderEnv) -> dispatcher.renderBatched(state, worldPos, world, matrix.matrix(), buffer, false, random, modelData, layer)
			);
		}

		public float getShade(Direction direction) {
			return world.getShade(direction, true);
		}

		public BufferBuilder getAndStartBuffer(RenderType layer) {
			return RendererDispatcher.getAndStartBuffer(chunkRender, buffers, usedLayers, layer);
		}

		public void markLayerUsed(RenderType layer) {
			usedLayers.add(layer);
		}

		public interface ColorSupplier {
			Color apply(BlockState state, BlockPos worldPos, BakedQuad quad);
		}

		public interface QuadConsumer {
			void accept(RenderType layer, VertexConsumer buffer, BakedQuad quad, Color color, boolean emissive);
		}

		public void forEachQuad(BlockState stateIn, BlockPos worldPosIn, Direction direction, ColorSupplier colorSupplier, QuadConsumer action) {
			var rand = optiFine.getSeed(stateIn.getSeed(worldPosIn));
			renderInBlockLayers(
					stateIn, worldPosIn,
					(state, worldPos, modelData, layer, buffer, renderEnv) -> {
						var model = getModel(state, renderEnv);

						var nullQuads = getQuadsAndStoreOverlays(state, worldPos, rand, modelData, layer, renderEnv, model, null);
						var anyQuadsFound = forEachQuad(nullQuads, state, worldPos, colorSupplier, layer, buffer, renderEnv, action);

						List<BakedQuad> dirQuads;
						if (!state.hasProperty(SNOWY))
							dirQuads = getQuadsAndStoreOverlays(state, worldPos, rand, modelData, layer, renderEnv, model, direction);
						else {
							// Make grass/snow/mycilium side faces be rendered with their top texture
							// Equivalent to OptiFine's Better Grass feature
							if (!state.getValue(SNOWY))
								dirQuads = getQuadsAndStoreOverlays(state, worldPos, rand, modelData, layer, renderEnv, model, NoCubesConfig.Client.betterGrassSides ? Direction.UP : direction);
							else {
								// The texture of grass underneath the snow (that normally never gets seen) is grey, we don't want that
								BlockState snow = Blocks.SNOW.defaultBlockState();
								BakedModel snowModel = getModel(snow, renderEnv);
								dirQuads = getQuadsAndStoreOverlays(snow, worldPos, rand, modelData, layer, renderEnv, snowModel, null);
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

		private BakedModel getModel(BlockState state, Object renderEnv) {
			var model = dispatcher.getBlockModel(state);
			model = optiFine.getModel(renderEnv, model, state);
			return model;
		}

		private List<BakedQuad> getQuadsAndStoreOverlays(BlockState state, BlockPos worldPos, long rand, ModelData modelData, RenderType layer, Object renderEnv, BakedModel model, Direction direction) {
			random.setSeed(rand);
			var quads = model.getQuads(state, direction, random, modelData, layer);
			quads = optiFine.getQuadsAndStoreOverlays(quads, world, state, worldPos, direction, layer, rand, renderEnv);
			return quads;
		}

		public boolean forEachQuad(List<BakedQuad> quads, BlockState state, BlockPos worldPos, ColorSupplier colorSupplier, RenderType layer, BufferBuilder buffer, Object renderEnv, QuadConsumer action) {
			int i = 0;
			for (; i < quads.size(); i++) {
				var quad = quads.get(i);
				var color = colorSupplier.apply(state, worldPos, quad);
				var emissive = optiFine == null ? null : optiFine.getQuadEmissive(quad);
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
			return dispatcher.getBlockModelShaper().getModelManager().getMissingModel().getQuads(Blocks.AIR.defaultBlockState(), Direction.UP, random);
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
			usedLayers, random, dispatcher
		);
		Predicate<IBlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		if (NoCubesConfig.Client.render)
			renderChunkMesh(renderer, isSmoothable);
	}

	public static void renderBreakingTexture(BlockRenderDispatcher dispatcher, BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack matrix, VertexConsumer buffer, ModelData modelData) {
		var mesher = NoCubesConfig.Server.mesher;
		try (var area = new Area(Minecraft.getInstance().level, pos, ModUtil.VEC_ONE, mesher)) {
			MeshRenderer.renderBreakingTexture(state, pos, matrix, buffer, mesher, area);
		}
	}

	private static void renderChunkMesh(ChunkRenderInfo renderer, Predicate<IBlockState> isSmoothable) {
		long start = System.nanoTime();
		var mesher = NoCubesConfig.Server.mesher;
		try (
				var area = new Area(Minecraft.getInstance().level, renderer.chunkPos, ModUtil.CHUNK_SIZE, mesher);
				var ignored = renderer.matrix.push()
		) {
			MeshRenderer.renderArea(renderer, isSmoothable, mesher, area);
		}
		meshProfiler.recordAndLogElapsedNanosChunk(start, "mesh");
	}

	public static BufferBuilder getAndStartBuffer(RenderChunk chunkRender, ChunkBufferBuilderPack buffers, Set<RenderType> usedLayers, RenderType layer) {
		var buffer = buffers.builder(layer);
		if (usedLayers.add(layer))
			chunkRender.beginLayer(buffer);
		return buffer;
	}

	static void quad(
			VertexConsumer buffer, PoseStack matrix,
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
			VertexConsumer buffer, PoseStack matrix, boolean doubleSided,
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
			VertexConsumer buffer, PoseStack matrix, boolean doubleSided,
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
			VertexConsumer buffer, PoseStack matrix, boolean doubleSided,
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
			vertex(buffer, matrix, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, overlay0, light0, n0x, n0y, n0z);
			vertex(buffer, matrix, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, overlay3, light3, n3x, n3y, n3z);
			vertex(buffer, matrix, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, overlay2, light2, n2x, n2y, n2z);
			vertex(buffer, matrix, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, overlay1, light1, n1x, n1y, n1z);
		}
	}

	public static void renderSmoothBlockDamage(final Tessellator tessellatorIn, final BufferBuilder bufferBuilderIn, final BlockPos blockpos, final IBlockState iblockstate, final IBlockAccess world, final TextureAtlasSprite textureatlassprite) {
		if (iblockstate.getRenderType() != EnumBlockRenderType.MODEL) {
			return;
		}

		final IsSmoothable isSmoothable;
		final MeshGeneratorType meshGeneratorType;
		if (Config.renderSmoothTerrain && TERRAIN_SMOOTHABLE.test(iblockstate)) {
			isSmoothable = TERRAIN_SMOOTHABLE;
			meshGeneratorType = Config.terrainMeshGenerator;
		} else if (Config.renderSmoothLeaves && LEAVES_SMOOTHABLE.test(iblockstate)) {
			isSmoothable = LEAVES_SMOOTHABLE;
			meshGeneratorType = Config.leavesMeshGenerator;
		} else {
			return;
		}

		// Draw tessellator and start again with color
		tessellatorIn.draw();
		bufferBuilderIn.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		try (FaceList faces = MeshDispatcher.generateBlockMeshOffset(blockpos, world, isSmoothable, meshGeneratorType)) {
			float minU = UVHelper.getMinU(textureatlassprite);
			float maxU = UVHelper.getMaxU(textureatlassprite);
			float minV = UVHelper.getMinV(textureatlassprite);
			float maxV = UVHelper.getMaxV(textureatlassprite);
			final int packed = iblockstate.getPackedLightmapCoords(world, blockpos);
			int lightmapSkyLight = (packed >> 16) & 0xFFFF;
			int lightmapBlockLight = packed & 0xFFFF;
			for (int faceIndex = 0, facesSize = faces.size(); faceIndex < facesSize; ++faceIndex) {
				try (Face face = faces.get(faceIndex)) {
					try (
							Vec3 v0 = face.getVertex0();
							Vec3 v1 = face.getVertex1();
							Vec3 v2 = face.getVertex2();
							Vec3 v3 = face.getVertex3()
					) {
						bufferBuilderIn.pos(v0.x, v0.y, v0.z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilderIn.pos(v1.x, v1.y, v1.z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilderIn.pos(v2.x, v2.y, v2.z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilderIn.pos(v3.x, v3.y, v3.z).color(0xFF, 0xFF, 0xFF, 0xFF).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					}
				}
			}
		}

		// Draw tessellator and start again without color
		tessellatorIn.draw();
		bufferBuilderIn.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		bufferBuilderIn.noColor();
	}

}
