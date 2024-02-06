package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.RollingProfiler;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.trait.INoCubesChunkSectionRenderBuilderSodium;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.mesh.OldNoCubes;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import me.jellysquid.mods.sodium.client.model.quad.properties.ModelQuadFacing;
import me.jellysquid.mods.sodium.client.render.chunk.compile.ChunkBuildBuffers;
import me.jellysquid.mods.sodium.client.render.chunk.compile.buffers.ChunkModelBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderCache;
import me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockRenderContext;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.DefaultMaterials;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.render.MeshRenderer.*;
import static io.github.cadiboo.nocubes.mesh.Mesher.getMeshOffset;
import static net.minecraft.world.level.block.GrassBlock.SNOWY;

public final class SodiumRenderer {

	private static final RollingProfiler totalProfiler = new RollingProfiler(256);
	private static final RollingProfiler fluidsProfiler = new RollingProfiler(256);
	private static final RollingProfiler meshProfiler = new RollingProfiler(256);

	public static class Helper {
		public interface RenderInLayer {
			void render(BlockState state, BlockPos worldPos, BakedModel model, long seed, ModelData modelData, RenderType layer, Material material, ChunkModelBuilder buffer, Object renderEnv);
		}

		public static void renderInBlockLayers(
			INoCubesChunkSectionRenderBuilderSodium task,
			ChunkBuildBuffers buffers,
			BlockRenderCache cache,
			BlockPos.MutableBlockPos modelOffset,
			BlockRenderContext context,
			BlockState state, BlockPos worldPos, RenderInLayer render
		) {
			var model = cache.getBlockModels().getBlockModel(state);
			var seed = state.getSeed(worldPos);
			var modelData = model.getModelData(context.localSlice(), worldPos, state, task.noCubes$getModelData(worldPos));
			var layers = ItemBlockRenderTypes.getRenderLayers(state);
			for (var layer : layers) {
				context.update(worldPos, modelOffset, state, model, seed, modelData, layer);
				var material = DefaultMaterials.forRenderLayer(layer);
				var buffer = buffers.get(material);
//				var buffer = getAndStartBuffer(layer);
				var renderEnv = (Object) null;//optiFine.preRenderBlock(chunkRender, buffers, world, layer, buffer, state, worldPos);
				render.render(state, worldPos, model, seed, modelData, layer, material, buffer, renderEnv);

//				optiFine.postRenderBlock(renderEnv, buffer, chunkRender, buffers, usedLayers);
//				markLayerUsed(layer);
			}
		}

		public static Color getColor(Color color, BakedQuad quad, BlockState state, BlockAndTintGetter world, BlockPos pos, float shade) {
			if (!quad.isTinted()) {
				color.red = shade;
				color.green = shade;
				color.blue = shade;
//				color.alpha = 1.0F;
				return color;
			}
			int packedColor = Minecraft.getInstance().getBlockColors().getColor(state, world, pos, quad.getTintIndex());
			color.red = (float) (packedColor >> 16 & 255) / 255.0F * shade;
			color.green = (float) (packedColor >> 8 & 255) / 255.0F * shade;
			color.blue = (float) (packedColor & 255) / 255.0F * shade;
//			color.alpha = 1.0F;
			return color;
		}

		public static float getShade(BlockAndTintGetter world, Direction direction) {
			return world.getShade(direction, true);
		}

		public interface ColorSupplier {
			Color apply(BlockState state, BlockPos worldPos, BakedQuad quad);
		}

		public interface QuadConsumer {
			void accept(RenderType layer, Material material, ChunkModelBuilder buffer, BakedQuad quad, Color color, boolean emissive);
		}

		public static void forEachQuad(
			INoCubesChunkSectionRenderBuilderSodium task,
			ChunkBuildBuffers buffers,
			BlockRenderCache cache,
			BlockPos.MutableBlockPos modelOffset,
			BlockRenderContext context,
			RandomSource random,
			BlockState stateIn, BlockPos worldPosIn, Direction direction, ColorSupplier colorSupplier, QuadConsumer action
		) {
			renderInBlockLayers(
				task, buffers, cache, modelOffset, context,
				stateIn, worldPosIn,
				(state, worldPos, model, seed, modelData, layer, material, buffer, renderEnv) -> {
					var nullQuads = getQuadsAndStoreOverlays(random, state, worldPos, seed, modelData, layer, renderEnv, model, null);
					var anyQuadsFound = forEachQuad(nullQuads, state, worldPos, colorSupplier, layer, material, buffer, renderEnv, action);

					List<BakedQuad> dirQuads;
					if (!state.hasProperty(SNOWY))
						dirQuads = getQuadsAndStoreOverlays(random, state, worldPos, seed, modelData, layer, renderEnv, model, direction);
					else {
						// Make grass/snow/mycilium side faces be rendered with their top texture
						// Equivalent to OptiFine's Better Grass feature
						if (!state.getValue(SNOWY))
							dirQuads = getQuadsAndStoreOverlays(random, state, worldPos, seed, modelData, layer, renderEnv, model, NoCubesConfig.Client.betterGrassSides ? Direction.UP : direction);
						else {
							// The texture of grass underneath the snow (that normally never gets seen) is grey, we don't want that
							var snow = Blocks.SNOW.defaultBlockState();
							var snowModel = cache.getBlockModels().getBlockModel(snow);
							dirQuads = getQuadsAndStoreOverlays(random, snow, worldPos, seed, modelData, layer, renderEnv, snowModel, null);
						}
					}
					anyQuadsFound |= forEachQuad(dirQuads, state, worldPos, colorSupplier, layer, material, buffer, renderEnv, action);

//					int numOverlaysRendered = optiFine.forEachOverlayQuad(this, state, worldPos, colorSupplier, action, renderEnv);
//					anyQuadsFound |= numOverlaysRendered > 0;

					if (!anyQuadsFound)
						forEachQuad(getMissingQuads(Minecraft.getInstance().getBlockRenderer(), random), state, worldPos, colorSupplier, layer, material, buffer, renderEnv, action);
				}
			);
		}

		private static List<BakedQuad> getQuadsAndStoreOverlays(RandomSource random, BlockState state, BlockPos worldPos, long seed, ModelData modelData, RenderType layer, Object renderEnv, BakedModel model, Direction direction) {
			random.setSeed(seed);
			var quads = model.getQuads(state, direction, random, modelData, layer);
//			quads = optiFine.getQuadsAndStoreOverlays(quads, world, state, worldPos, direction, layer, seed, renderEnv);
			return quads;
		}

		private static boolean forEachQuad(List<BakedQuad> quads, BlockState state, BlockPos worldPos, ColorSupplier colorSupplier, RenderType layer, Material material, ChunkModelBuilder buffer, Object renderEnv, QuadConsumer action) {
			int i = 0;
			for (; i < quads.size(); i++) {
				var quad = quads.get(i);
				var color = colorSupplier.apply(state, worldPos, quad);
//				var emissive = optiFine == null ? null : optiFine.getQuadEmissive(quad);
//				if (emissive != null) {
//					optiFine.preRenderQuad(renderEnv, emissive, state, worldPos);
//					action.accept(layer, buffer, quad, color, true);
//				}
//				if (optiFine != null)
//					optiFine.preRenderQuad(renderEnv, quad, state, worldPos);

				action.accept(layer, material, buffer, quad, color, false);
			}
			return i > 0;
		}

		private static List<BakedQuad> getMissingQuads(BlockRenderDispatcher dispatcher, RandomSource random) {
			return dispatcher.getBlockModelShaper().getModelManager().getMissingModel().getQuads(Blocks.AIR.defaultBlockState(), Direction.UP, random);
		}
	}

	public static void renderChunk(
		INoCubesChunkSectionRenderBuilderSodium task,
		/*ChunkBuildBuffers*/ Object buffers,
		/*BlockRenderCache*/ Object cache,
		BlockPos.MutableBlockPos blockPos,
		BlockPos.MutableBlockPos modelOffset,
		/*BlockRenderContext*/ Object context
	) {
		if (NoCubesConfig.Client.debugSkipNoCubesRendering)
			return;

		var start = System.nanoTime();
		try (
			var light = new LightCache(Minecraft.getInstance().level, blockPos, ModUtil.CHUNK_SIZE);
		) {
			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;

			if (NoCubesConfig.Client.render)
				renderChunkMesh(
					task,
					(ChunkBuildBuffers) buffers,
					(BlockRenderCache) cache,
					blockPos,
					modelOffset,
					(BlockRenderContext) context,
					light, isSmoothable
				);
		}
		totalProfiler.recordAndLogElapsedNanosChunk(start, "total");
	}

	private static void renderChunkMesh(
		INoCubesChunkSectionRenderBuilderSodium task,
		ChunkBuildBuffers buffers,
		BlockRenderCache cache,
		BlockPos.MutableBlockPos blockPos,
		BlockPos.MutableBlockPos modelOffset,
		BlockRenderContext context,
		LightCache light, Predicate<BlockState> isSmoothable
	) {
		var start = System.nanoTime();
		var mesher = NoCubesConfig.Server.mesher;
		try (
			var area = new Area(Minecraft.getInstance().level, blockPos, ModUtil.CHUNK_SIZE, mesher);
		) {
			renderArea(
				task,
				buffers,
				cache,
				blockPos,
				modelOffset,
				context,
				light,
				isSmoothable, mesher, area
			);
		}
		meshProfiler.recordAndLogElapsedNanosChunk(start, "mesh");
	}

	static void quad(
		ChunkMeshBufferBuilder vertexBuffer, Material material,
		ChunkVertexEncoder.Vertex[] vertices, BlockRenderContext context,
		Face face, Vec faceNormal,
		Color color,
		Texture uvs,
		FaceLight light,
		boolean doubleSided
	) {
		quad(
			vertexBuffer, material,
			vertices, context, doubleSided,
			face, color, uvs, OverlayTexture.NO_OVERLAY, light, faceNormal
		);
	}

	static void quad(
		ChunkMeshBufferBuilder vertexBuffer, Material material,
		ChunkVertexEncoder.Vertex[] vertices, BlockRenderContext context, boolean doubleSided,
		Face face, Color color, Texture texture, int overlay, FaceLight light, Vec normal
	) {
		quad(
			vertexBuffer, material,
			vertices, context, doubleSided,
			face.v0, color, texture.u0, texture.v0, overlay, light.v0, normal,
			face.v1, color, texture.u1, texture.v1, overlay, light.v1, normal,
			face.v2, color, texture.u2, texture.v2, overlay, light.v2, normal,
			face.v3, color, texture.u3, texture.v3, overlay, light.v3, normal
		);
	}

	static void quad(
		ChunkMeshBufferBuilder vertexBuffer, Material material,
		ChunkVertexEncoder.Vertex[] vertices, BlockRenderContext context, boolean doubleSided,
		Vec vec0, Color color0, float u0, float v0, int overlay0, int light0, Vec normal0,
		Vec vec1, Color color1, float u1, float v1, int overlay1, int light1, Vec normal1,
		Vec vec2, Color color2, float u2, float v2, int overlay2, int light2, Vec normal2,
		Vec vec3, Color color3, float u3, float v3, int overlay3, int light3, Vec normal3
	) {
		quad(
			vertexBuffer, material,
			vertices, context, doubleSided,
			vec0.x, vec0.y, vec0.z, color0.red, color0.green, color0.blue, color0.alpha, u0, v0, overlay0, light0, normal0.x, normal0.y, normal0.z,
			vec1.x, vec1.y, vec1.z, color1.red, color1.green, color1.blue, color1.alpha, u1, v1, overlay1, light1, normal1.x, normal1.y, normal1.z,
			vec2.x, vec2.y, vec2.z, color2.red, color2.green, color2.blue, color2.alpha, u2, v2, overlay2, light2, normal2.x, normal2.y, normal2.z,
			vec3.x, vec3.y, vec3.z, color3.red, color3.green, color3.blue, color3.alpha, u3, v3, overlay3, light3, normal3.x, normal3.y, normal3.z
		);
	}

	static void quad(
		ChunkMeshBufferBuilder vertexBuffer, Material material,
		ChunkVertexEncoder.Vertex[] vertices, BlockRenderContext context, boolean doubleSided,
		float v0x, float v0y, float v0z, float red0, float green0, float blue0, float alpha0, float u0, float v0, int overlay0, int light0, float n0x, float n0y, float n0z,
		float v1x, float v1y, float v1z, float red1, float green1, float blue1, float alpha1, float u1, float v1, int overlay1, int light1, float n1x, float n1y, float n1z,
		float v2x, float v2y, float v2z, float red2, float green2, float blue2, float alpha2, float u2, float v2, int overlay2, int light2, float n2x, float n2y, float n2z,
		float v3x, float v3y, float v3z, float red3, float green3, float blue3, float alpha3, float u3, float v3, int overlay3, int light3, float n3x, float n3y, float n3z
	) {
		vertex(vertices[0], context, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, overlay0, light0, n0x, n0y, n0z);
		vertex(vertices[1], context, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, overlay1, light1, n1x, n1y, n1z);
		vertex(vertices[2], context, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, overlay2, light2, n2x, n2y, n2z);
		vertex(vertices[3], context, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, overlay3, light3, n3x, n3y, n3z);
		vertexBuffer.push(vertices, material);

		if (doubleSided) {
			vertex(vertices[0], context, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, overlay0, light0, n0x, n0y, n0z);
			vertex(vertices[1], context, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, overlay3, light3, n3x, n3y, n3z);
			vertex(vertices[2], context, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, overlay2, light2, n2x, n2y, n2z);
			vertex(vertices[3], context, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, overlay1, light1, n1x, n1y, n1z);
			vertexBuffer.push(vertices, material);
		}
	}

	private static void vertex(
		ChunkVertexEncoder.Vertex vertex, BlockRenderContext context,
		float vx, float vy, float vz,
		float red, float green, float blue, float alpha,
		float u, float v,
		int overlay,
		int light,
		float nx, float ny, float nz
	) {
		vertex.x = context.origin().x() + vx;
		vertex.y = context.origin().y() + vy;
		vertex.z = context.origin().z() + vz;
		// Alpha is shade value
//		vertex.color = ColorABGR.withAlpha(colors != null ? colors[srcIndex] : quad.getColor(srcIndex), light.br[srcIndex]);
		vertex.color = ColorABGR.pack(red, green, blue, alpha);
		vertex.u = u;
		vertex.v = v;
		// Lightmap coordinates
		vertex.light = light;
	}

	public static boolean isSolidRender(BlockState state) {
		return state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) || state.getBlock() instanceof DirtPathBlock;
	}

	public static void runForSolidAndSeeThrough(Predicate<BlockState> isSmoothable, Consumer<Predicate<BlockState>> action) {
		action.accept(state -> isSmoothable.test(state) && isSolidRender(state));
		action.accept(state -> isSmoothable.test(state) && !isSolidRender(state));
	}

	public static void renderArea(
		INoCubesChunkSectionRenderBuilderSodium task,
		ChunkBuildBuffers buffers,
		BlockRenderCache cache,
		BlockPos.MutableBlockPos blockPos,
		BlockPos.MutableBlockPos modelOffset,
		BlockRenderContext context,
		LightCache light,
		Predicate<BlockState> isSmoothableIn, Mesher mesher, Area area
	) {
		var faceInfo = FaceInfo.INSTANCE.get();
		var objects = MutableObjects.INSTANCE.get();
		var random = new SingleThreadedRandomSource(42L);
		var vertices = ChunkVertexEncoder.Vertex.uninitializedQuad();

//		Mesher.translateToMeshStart(renderer.matrix.matrix(), area.start, renderer.chunkPos);
		modelOffset.set(
			getMeshOffset(area.start.getX(), blockPos.getX()),
			getMeshOffset(area.start.getY(), blockPos.getY()),
			getMeshOffset(area.start.getZ(), blockPos.getZ())
		);

		runForSolidAndSeeThrough(isSmoothableIn, isSmoothable -> {
			mesher.generateGeometry(area, isSmoothable, (ignored, face) -> {
				faceInfo.setup(face);
				RenderableState foundState;
				if (mesher instanceof OldNoCubes) {
					foundState = objects.foundState;
					foundState.state = area.getBlockStateFaultTolerant(ignored);
					foundState.pos.set(ignored);
				} else
					foundState = RenderableState.findAt(objects, area, faceInfo.normal, faceInfo.centre, isSmoothable);
				var renderState = RenderableState.findRenderFor(objects, foundState, area, faceInfo.approximateDirection);

				if (renderState.state.getRenderShape() == RenderShape.INVISIBLE) {
//					renderState.state = Blocks.STONE.defaultBlockState();
					return true; // How?
				}

				renderFaceWithConnectedTextures(
					task, buffers, cache, modelOffset, context,
					light, random, vertices,
					objects, area, faceInfo, renderState
				);

				// Draw grass tufts, plants etc.
				renderExtras(
					task, buffers, cache, modelOffset, context,
					light, random, vertices,
					objects, area, foundState, renderState, faceInfo
				);
				return true;
			});
		});
	}

	static void renderFaceWithConnectedTextures(
		INoCubesChunkSectionRenderBuilderSodium task,
		ChunkBuildBuffers buffers,
		BlockRenderCache cache,
		BlockPos.MutableBlockPos modelOffset,
		BlockRenderContext context,
		LightCache lightCache,
		RandomSource random, ChunkVertexEncoder.Vertex[] vertices,
		MutableObjects objects, Area area, FaceInfo faceInfo, RenderableState renderState
	) {
		var state = renderState.state;
		var worldPos = objects.pos.set(renderState.relativePos()).move(area.start);

		var block = state.getBlock();
		var renderBothSides = !(block instanceof BeaconBeamBlock) && !(block instanceof NetherPortalBlock || block instanceof EndPortalBlock) && !(block instanceof SnowLayerBlock) && !MeshRenderer.isSolidRender(state);

		var light = lightCache.get(area.start, faceInfo.face, faceInfo.normal, objects.light);
		var shade = Helper.getShade(cache.getWorldSlice(), faceInfo.approximateDirection);

		Helper.forEachQuad(
			task, buffers, cache, modelOffset, context, random,
			state, worldPos, faceInfo.approximateDirection,
			(colorState, colorWorldPos, quad) -> Helper.getColor(objects.color, quad, colorState, cache.getWorldSlice(), colorWorldPos, shade),
			(layer, material, buffer, quad, color, emissive) -> {
				var sprite = quad.getSprite();
				if (sprite != null) {
					buffer.addSprite(sprite);
				}

				var normal = faceInfo.normal;
				var isApproximatelyFlat = Math.abs(normal.x) >= 0.9 || Math.abs(normal.y) >= 0.9 || Math.abs(normal.z) >= 0.9;

				var vertexBuffer = buffer.getVertexBuffer(isApproximatelyFlat ? ModelQuadFacing.fromDirection(faceInfo.approximateDirection) : ModelQuadFacing.UNASSIGNED);

				var texture = Texture.forQuadRearranged(objects.texture, quad, faceInfo.approximateDirection);
				renderQuad(vertexBuffer, material, vertices, context, faceInfo, color, texture, emissive ? FaceLight.MAX_BRIGHTNESS : light, renderBothSides);
			}
		);
	}

	static void renderExtras(
		INoCubesChunkSectionRenderBuilderSodium task,
		ChunkBuildBuffers buffers,
		BlockRenderCache cache,
		BlockPos.MutableBlockPos modelOffset,
		BlockRenderContext context,

		LightCache light,

		RandomSource random, ChunkVertexEncoder.Vertex[] vertices,

		MutableObjects objects, Area area, RenderableState foundState, RenderableState renderState, FaceInfo faceInfo
	) {
		var renderPlantsOffset = NoCubesConfig.Client.fixPlantHeight;
		var renderGrassTufts = NoCubesConfig.Client.grassTufts;
		if (!renderPlantsOffset && !renderGrassTufts)
			return;

		if (faceInfo.approximateDirection != Direction.UP)
			return;

		var relativeAbove = objects.pos.set(foundState.relativePos()).move(Direction.UP);
		var stateAbove = area.getBlockStateFaultTolerant(relativeAbove);
		if (renderPlantsOffset && ModUtil.isShortPlant(stateAbove)) {
			var oldX = context.origin().x();
			var oldY = context.origin().y();
			var oldZ = context.origin().z();

			{
				var worldAbove = relativeAbove.move(area.start);
				var center = faceInfo.centre;

				Helper.renderInBlockLayers(
					task, buffers, cache, modelOffset, context,
					stateAbove, worldAbove,
					(state, worldPos, model, seed, modelData, layer, material, buffer, renderEnv) -> {
						((Vector3f) context.origin()).set(
							oldX + center.x - 0.5F,
							oldY + center.y,
							oldZ + center.z - 0.5F
						);
						cache.getBlockRenderer().renderModel(context, buffers);
					}
				);
			}
			((Vector3f) context.origin()).set(oldX, oldY, oldZ);
		}

		if (renderGrassTufts && foundState.state.hasProperty(SNOWY) && !ModUtil.isPlant(stateAbove)) {
			var grass = Blocks.GRASS.defaultBlockState();
			var worldAbove = relativeAbove.move(area.start);
			var renderBothSides = true;
			var world = cache.getWorldSlice();

			var offset = grass.getOffset(world, worldAbove);
			var xOff = (float) offset.x;
			var zOff = (float) offset.z;
			var yExt = 0.4F;
			var snowy = isSnow(renderState.state) || (renderState.state.hasProperty(SNOWY) && renderState.state.getValue(SNOWY));
			var face = faceInfo.face;

			var grassTuft0 = objects.grassTuft0;
			setupGrassTuft(grassTuft0.face, face.v2, face.v0, xOff, yExt, zOff);
			var light0 = light.get(area.start, grassTuft0, objects.grassTuft0Light);
			var shade0 = Helper.getShade(world, grassTuft0.approximateDirection);

			var grassTuft1 = objects.grassTuft1;
			setupGrassTuft(grassTuft1.face, face.v3, face.v1, xOff, yExt, zOff);
			var light1 = light.get(area.start, grassTuft1, objects.grassTuft1Light);
			var shade1 = Helper.getShade(world, grassTuft1.approximateDirection);

			Helper.forEachQuad(
				task, buffers, cache, modelOffset, context, random,
				grass, worldAbove, null,
				(state, worldPos, quad) -> snowy ? Color.WHITE : Helper.getColor(objects.color, quad, grass, world, worldAbove, 1F),
				(layer, material, buffer, quad, color, emissive) -> {
					var sprite = quad.getSprite();
					if (sprite != null) {
						buffer.addSprite(sprite);
					}

					var normal = faceInfo.normal;
					var isApproximatelyFlat = Math.abs(normal.x) >= 0.9 || Math.abs(normal.y) >= 0.9 || Math.abs(normal.z) >= 0.9;

					var vertexBuffer = buffer.getVertexBuffer(isApproximatelyFlat ? ModelQuadFacing.fromDirection(faceInfo.approximateDirection) : ModelQuadFacing.UNASSIGNED);

					// This is super ugly because Color is mutable. Will be fixed by Valhalla (color will be an inline type)
					int argbTEMP = color.packToARGB();

					color.multiplyUNSAFENEEDSVALHALLA(shade0);
					renderQuad(vertexBuffer, material, vertices, context, grassTuft0, color, Texture.forQuadRearranged(objects.texture, quad, grassTuft0.approximateDirection), emissive ? FaceLight.MAX_BRIGHTNESS : light0, renderBothSides);

					color.unpackFromARGB(argbTEMP);
					color.multiplyUNSAFENEEDSVALHALLA(shade1);
					renderQuad(vertexBuffer, material, vertices, context, grassTuft1, color, Texture.forQuadRearranged(objects.texture, quad, grassTuft1.approximateDirection), emissive ? FaceLight.MAX_BRIGHTNESS : light1, renderBothSides);

					color.unpackFromARGB(argbTEMP);
				}
			);
		}

	}

	private static void setupGrassTuft(Face face, Vec v0, Vec v1, float xOff, float yExt, float zOff) {
		face.set(v0, v0, v1, v1);
		face.v1.y += yExt;
		face.v2.y += yExt;
		face.add(xOff, 0, zOff);
	}

	private static void renderQuad(
		ChunkMeshBufferBuilder vertexBuffer, Material material,
		ChunkVertexEncoder.Vertex[] vertices, BlockRenderContext context,
		FaceInfo faceInfo,
		Color color,
		Texture uvs,
		FaceLight light,
		boolean renderBothSides
	) {
		quad(vertexBuffer, material, vertices, context, faceInfo.face, faceInfo.normal, color, uvs, light, renderBothSides);
	}

}

