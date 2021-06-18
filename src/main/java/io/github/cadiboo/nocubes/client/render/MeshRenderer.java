package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.optifine.OptiFineProxy;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.PackedLight;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;
import net.optifine.model.BlockModelCustomizer;
import net.optifine.model.ListQuadsOverlay;
import net.optifine.render.RenderEnv;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.render.RendererDispatcher.quad;

// /tp @p 83.63 64.26 -112.34 -90.10 -6.33
public final class MeshRenderer {

	public static boolean isSolidRender(BlockState state) {
		return state.isSolidRender(EmptyBlockReader.INSTANCE, BlockPos.ZERO);
	}

	public static void runForSolidAndSeeThrough(Predicate<BlockState> isSmoothable, Consumer<Predicate<BlockState>> action) {
		action.accept(state -> isSmoothable.test(state) && isSolidRender(state));
		action.accept(state -> isSmoothable.test(state) && !isSolidRender(state));
	}

	public static void renderArea(ChunkRenderDispatcher.ChunkRender.RebuildTask rebuildTask, ChunkRenderDispatcher.ChunkRender chunkRender, ChunkRenderDispatcher.CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, BlockPos chunkPos, IBlockDisplayReader world, Random random, BlockRendererDispatcher dispatcher, FluentMatrixStack matrix, LightCache light, Predicate<BlockState> isSmoothableIn, OptiFineProxy optiFine, MeshGenerator generator, Area area) {
		FaceInfo renderInfo = new FaceInfo();
		MeshGenerator.translateToMeshStart(matrix.matrix, area.start, chunkPos);
		runForSolidAndSeeThrough(isSmoothableIn, isSmoothable -> {
			generator.generate(area, isSmoothable, (relativePos, face) -> {
				renderInfo.setup(face, area.start);
				BlockState state = TextureLocator.getTexturePosAndState(area, isSmoothable, renderInfo, relativePos);
				BlockPos.Mutable worldPos = relativePos.move(area.start);

				if (state.getRenderShape() == BlockRenderType.INVISIBLE)
					return true;
				long rand = optiFine.getSeed(state.getSeed(worldPos));
				IModelData modelData = rebuildTask.getModelData(worldPos);
				RendererDispatcher.renderInLayers(
					chunkRender, compiledChunk, buffers, optiFine,
					layer -> true,//RenderTypeLookup.canRenderInLayer(state, layer),
					(layer, buffer) -> optiFine.preRenderBlock(chunkRender, buffers, world, layer, buffer, state, worldPos),
					(layer, buffer, renderEnv) -> {
						renderFaceForLayer(world, random, dispatcher, matrix, light, optiFine, renderInfo, state, worldPos, rand, modelData, layer, buffer, renderEnv);
						return true;
					},
					(buffer, renderEnv) -> optiFine.postRenderBlock(renderEnv, buffer, chunkRender, buffers, compiledChunk)
				);
				return true;
			});
		});
		ForgeHooksClient.setRenderLayer(null);
	}

	static void renderBreakingTexture(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData, MeshGenerator generator, Area area) {
		FaceInfo renderInfo = new FaceInfo();
		Random random = dispatcher.random;
		MeshGenerator.translateToMeshStart(matrix, area.start, pos);
		boolean stateSolidity = isSolidRender(state);
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		generator.generate(area, s -> isSmoothable.test(s) && isSolidRender(s) == stateSolidity, (relativePos, face) -> {
			renderInfo.setup(face, area.start);

			// Don't need textures or lighting because the crumbling texture overwrites them
			renderInfo.assignMissingQuads(state, random, modelData);
			LightCache light = null;
			renderFace(renderInfo, buffer, matrix, world, state, pos, null, null, light, false);

			return true;
		});
	}

	static void renderFaceForLayer(IBlockDisplayReader world, Random random, BlockRendererDispatcher dispatcher, FluentMatrixStack matrix, LightCache light, OptiFineProxy optiFine, FaceInfo renderInfo, BlockState state, BlockPos.Mutable worldPos, long rand, IModelData modelData, RenderType layer, BufferBuilder buffer, Object renderEnv) {
		{
			IBakedModel modelIn = dispatcher.getBlockModel(state);
			modelIn = optiFine.getModel(renderEnv, modelIn, state);
			renderInfo.findAndAssignQuads(modelIn, rand, world, state, worldPos, random, modelData, layer, renderEnv);
			Material material = state.getMaterial();
			boolean renderBothSides = material != Material.GLASS && material != Material.PORTAL && material != Material.TOP_SNOW && !isSolidRender(state);
			renderFace(renderInfo, buffer, matrix.matrix, world, state, worldPos, light, optiFine, renderEnv, renderBothSides);
		}

		// Render grass tufts
		if ((layer == RenderType.cutout() || layer == RenderType.cutoutMipped()) && renderInfo.faceDirection == Direction.UP && state.hasProperty(GrassBlock.SNOWY) && !ModUtil.isPlant(world.getBlockState(worldPos.above()))) {
			state = Blocks.GRASS.defaultBlockState();
//			worldPos = worldPos.above().mutable();
			IBakedModel modelIn = dispatcher.getBlockModel(state);
			modelIn = optiFine.getModel(renderEnv, modelIn, state);
			renderInfo.findAndAssignQuads(modelIn, rand, world, state, worldPos, random, modelData, layer, renderEnv);
			boolean renderBothSides = true;

			Vector3d offset = state.getOffset(world, worldPos);
			Face face = renderInfo.face;
			face.add((float) offset.x, 0, (float) offset.z);
			// Disgustingly store the face's values in its normals to avoid allocating an object
			// Hopefully this doesn't come back to bite me
			Face original = renderInfo.vertexNormals;
			original.setValuesFrom(face);
			float yExt = 0.4F;

			face.v0.set(original.v0);
			face.v1.set(original.v0).add(0, yExt, 0);
			face.v2.set(original.v2).add(0, yExt, 0);
			face.v3.set(original.v2);
			renderFace(renderInfo, buffer, matrix.matrix, world, state, worldPos, light, optiFine, renderEnv, renderBothSides);

			face.v0.set(original.v1);
			face.v1.set(original.v1).add(0, yExt, 0);
			face.v2.set(original.v3).add(0, yExt, 0);
			face.v3.set(original.v3);
			renderFace(renderInfo, buffer, matrix.matrix, world, state, worldPos, light, optiFine, renderEnv, renderBothSides);

			face.setValuesFrom(original);
			face.subtract((float) offset.x, 0, (float) offset.z);
		}

	}

	static void renderFace(FaceInfo renderInfo, IVertexBuilder buffer, MatrixStack matrix, IBlockDisplayReader world, BlockState state, BlockPos pos, @Nullable LightCache light, @Nullable OptiFineProxy optiFine, @Nullable Object renderEnv, boolean doubleSided) {
		List<BakedQuad> quads = renderInfo.quads;
		for (int i = 0, l = quads.size(); i < l; ++i) {
			BakedQuad quad = quads.get(i);
			BakedQuad emissive = optiFine == null ? null : optiFine.getQuadEmissive(quad);
			if (emissive != null) {
				optiFine.preRenderQuad(renderEnv, emissive, state, pos);
				renderQuad(renderInfo, quad, buffer, matrix, world, state, pos, null, doubleSided);
			}
			if (optiFine != null)
				optiFine.preRenderQuad(renderEnv, quad, state, pos);
			renderQuad(renderInfo, quad, buffer, matrix, world, state, pos, light, doubleSided);
		}
	}

	private static void renderQuad(FaceInfo renderInfo, BakedQuad quad, IVertexBuilder buffer, MatrixStack matrix, IBlockDisplayReader world, BlockState state, BlockPos pos, @Nullable LightCache light, boolean doubleSided) {
		Color color = renderInfo.getColor(quad, state, world, pos);
		color.multiply(world.getShade(renderInfo.faceDirection, true));

		Texture uvs = renderInfo.texture;
		uvs.unpackFromQuad(quad);
		uvs.rearrangeForDirection(renderInfo.faceDirection);

		PackedLight packedLight = renderInfo.getPackedLight(light);

		quad(
			buffer, matrix, doubleSided,
			renderInfo.face, color, uvs, OverlayTexture.NO_OVERLAY, packedLight, renderInfo.faceNormal
		);
	}

	static final /* inline? */ class FaceInfo {
		public Face face;
		public BlockPos faceRelativeToWorldPos;
		public final Face vertexNormals = new Face();
		public final Vec faceNormal = new Vec();
		public Direction faceDirection;
		public final Texture texture = new Texture();
		public final Color color = new Color();
		public final PackedLight packedLight = new PackedLight(0, 0, 0, 0);
		public final List<BakedQuad> quads = new ArrayList<>();

		public void setup(Face face, BlockPos faceRelativeToWorldPos) {
			this.face = face;
			this.faceRelativeToWorldPos = faceRelativeToWorldPos;
			face.assignNormalTo(vertexNormals);
			vertexNormals.multiply(-1).assignAverageTo(faceNormal);
			faceDirection = faceNormal.getDirectionFromNormal();
		}

		public void findAndAssignQuads(IBakedModel model, long rand, IBlockDisplayReader world, BlockState state, BlockPos pos, Random random, IModelData modelData, RenderType layer, Object renderEnv) {
			quads.clear();

			random.setSeed(rand);
			List<BakedQuad> nullQuads = model.getQuads(state, null, random, modelData);
			nullQuads = BlockModelCustomizer.getRenderQuads(nullQuads, world, state, pos, null, layer, rand, (RenderEnv) renderEnv);
			quads.addAll(nullQuads);

			Direction direction;
			random.setSeed(rand);
			List<BakedQuad> dirQuads;
			if (!state.hasProperty(BlockStateProperties.SNOWY))
				dirQuads = model.getQuads(state, direction = faceDirection, random, modelData);
			else {
				// Make grass/snow/mycilium side faces be rendered with their top texture
				// Equivalent to OptiFine's Better Grass feature
				if (!state.getValue(BlockStateProperties.SNOWY))
					dirQuads = model.getQuads(state, (direction = NoCubesConfig.Client.betterGrassAndSnow ? Direction.UP : faceDirection), random, modelData);
				else {
					// The texture of grass underneath the snow (that normally never gets seen) is grey, we don't want that
					BlockState snow = Blocks.SNOW.defaultBlockState();
					dirQuads = Minecraft.getInstance().getBlockRenderer().getBlockModel(snow).getQuads(snow, direction = null, random, modelData);
				}
			}
			dirQuads = BlockModelCustomizer.getRenderQuads(dirQuads, world, state, pos, direction, layer, rand, (RenderEnv) renderEnv);
			quads.addAll(dirQuads);

			for (RenderType layer1 : RenderType.CHUNK_RENDER_TYPES) {
				ListQuadsOverlay listQuadsOverlay = ((RenderEnv) renderEnv).getListQuadsOverlay(layer1);
				int size = listQuadsOverlay.size();
				for (int i = 0; i < size; ++i)
					quads.add(listQuadsOverlay.getQuad(i));
				listQuadsOverlay.clear();
			}

			if (quads.isEmpty())
				assignMissingQuads(state, random, modelData);
		}

		public void assignMissingQuads(BlockState state, Random random, IModelData modelData) {
			quads.addAll(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager().getMissingModel().getQuads(state, faceDirection, random, modelData));
		}

		public PackedLight getPackedLight(LightCache light) {
			return light == null ? PackedLight.MAX_BRIGHTNESS : light.get(faceRelativeToWorldPos, face, faceNormal, packedLight);
		}

		public Color getColor(BakedQuad quad, BlockState state, IBlockDisplayReader world, BlockPos pos) {
			Color color = this.color;
			if (!quad.isTinted()) {
				color.red = 1.0F;
				color.green = 1.0F;
				color.blue = 1.0F;
//				color.alpha = 1.0F;
			} else {
				// TODO: Colors cache
				int packedColor = Minecraft.getInstance().getBlockColors().getColor(state, world, pos, quad.getTintIndex());
				color.red = (float) (packedColor >> 16 & 255) / 255.0F;
				color.green = (float) (packedColor >> 8 & 255) / 255.0F;
				color.blue = (float) (packedColor & 255) / 255.0F;
//				color.alpha = 1.0F;
			}
			return color;
		}
	}

	static class TextureLocator {

		private static final BlockPos[] OFFSETS_ORDERED = {
			// check 6 immediate neighbours
			new BlockPos(+0, -1, +0),
			new BlockPos(+0, +1, +0),
			new BlockPos(-1, +0, +0),
			new BlockPos(+1, +0, +0),
			new BlockPos(+0, +0, -1),
			new BlockPos(+0, +0, +1),
			// check 12 non-immediate, non-corner neighbours
			new BlockPos(-1, -1, +0),
			new BlockPos(-1, +0, -1),
			new BlockPos(-1, +0, +1),
			new BlockPos(-1, +1, +0),
			new BlockPos(+0, -1, -1),
			new BlockPos(+0, -1, +1),
			// new BlockPos(+0, +0, +0), // Don't check self
			new BlockPos(+0, +1, -1),
			new BlockPos(+0, +1, +1),
			new BlockPos(+1, -1, +0),
			new BlockPos(+1, +0, -1),
			new BlockPos(+1, +0, +1),
			new BlockPos(+1, +1, +0),
			// check 8 corner neighbours
			new BlockPos(+1, +1, +1),
			new BlockPos(+1, +1, -1),
			new BlockPos(-1, +1, +1),
			new BlockPos(-1, +1, -1),
			new BlockPos(+1, -1, +1),
			new BlockPos(+1, -1, -1),
			new BlockPos(-1, -1, +1),
			new BlockPos(-1, -1, -1),
		};

		private static @Nullable BlockState tryFindNearbyPosAndState(
			BlockState state,
			int relativeX, int relativeY, int relativeZ,
			BlockPos.Mutable relativePos, Area area, Predicate<BlockState> isSmoothable
		) {
			if (isSmoothable.test(state))
				return state;

			for (int i = 0, offsets_orderedLength = OFFSETS_ORDERED.length; i < offsets_orderedLength; i++) {
				BlockPos offset = OFFSETS_ORDERED[i];
				relativePos.set(relativeX, relativeY, relativeZ).move(offset);
				state = area.getBlockState(relativePos);
				if (isSmoothable.test(state))
					return state;
			}
			return null;
		}

		public static BlockState getTexturePosAndState(Area area, Predicate<BlockState> isSmoothable, FaceInfo faceInfo, BlockPos.Mutable relativePos) {
			Vec texturePos = calcTexturePos(faceInfo);
			relativePos.set(texturePos.x, texturePos.y, texturePos.z);
			BlockState state = area.getBlockState(relativePos);

			// Has always been true in testing so I changed this from a call to tryFindNearbyPosAndState on failure to an assertion
			// This HAS failed due to a race condition with the mesh being generated and then this getting called after
			// the state has been toggled to being un-smoothable with the keybind (so the state WAS smoothable).
			assert isSmoothable.test(state);

			boolean tryFindSnow = faceInfo.faceDirection == Direction.UP || NoCubesConfig.Client.betterGrassAndSnow;
			if (!tryFindSnow)
				return state;

			int x = relativePos.getX();
			int y = relativePos.getY();
			int z = relativePos.getZ();
			BlockState overrideState = tryFindNearbyPosAndState(state, x, y, z, relativePos, area, TextureLocator::isSnow);
			if (overrideState != null)
				return overrideState;

			// This will have been moved around by tryFindNearbyPosAndState, reset it
			relativePos.set(x, y, z);
			return state;
		}

		public static Vec calcTexturePos(FaceInfo faceInfo) {
			// TODO: Remove allocations
			Vec pos = new Vec();
			faceInfo.face.assignAverageTo(pos);
			Vec offset = new Vec(faceInfo.faceNormal).normalise().multiply(0.5F);
			pos.subtract(offset);
			return pos;
		}

		private static boolean isSnow(BlockState state) {
			Block block = state.getBlock();
			return block == Blocks.SNOW || block == Blocks.SNOW_BLOCK;
		}

	}

}
