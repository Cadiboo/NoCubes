package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
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
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;
import net.optifine.render.RenderTypes;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.render.RendererDispatcher.quad;
import static net.minecraft.block.GrassBlock.SNOWY;

// /tp @p 83.63 64.26 -112.34 -90.10 -6.33
public final class MeshRenderer {

	// From OptiFine's BlockModelRenderer
	public static final RenderType[] OVERLAY_LAYERS = new RenderType[]{RenderTypes.CUTOUT, RenderTypes.CUTOUT_MIPPED, RenderTypes.TRANSLUCENT};
	// Valhalla PLS
	private static final ThreadLocal<FaceLight> FACE_LIGHT = ThreadLocal.withInitial(FaceLight::new);
	private static final ThreadLocal<FaceInfo> GRASS_TUFT_0 = ThreadLocal.withInitial(FaceInfo::withFace);
	private static final ThreadLocal<FaceLight> GRASS_TUFT_0_LIGHT = ThreadLocal.withInitial(FaceLight::new);
	private static final ThreadLocal<FaceInfo> GRASS_TUFT_1 = ThreadLocal.withInitial(FaceInfo::withFace);
	private static final ThreadLocal<FaceLight> GRASS_TUFT_1_LIGHT = ThreadLocal.withInitial(FaceLight::new);
	private static final ThreadLocal<BlockPos.Mutable> MUTABLE = ThreadLocal.withInitial(BlockPos.Mutable::new);

	public static boolean isSolidRender(BlockState state) {
		return state.isSolidRender(EmptyBlockReader.INSTANCE, BlockPos.ZERO);
	}

	public static void runForSolidAndSeeThrough(Predicate<BlockState> isSmoothable, Consumer<Predicate<BlockState>> action) {
		action.accept(state -> isSmoothable.test(state) && isSolidRender(state));
		action.accept(state -> isSmoothable.test(state) && !isSolidRender(state));
	}

	static class RenderableState {

		private static final ThreadLocal<RenderableState> FIND_AT = ThreadLocal.withInitial(RenderableState::new);
		private static final ThreadLocal<RenderableState> FIND_RENDER = ThreadLocal.withInitial(RenderableState::new);
		private static final ThreadLocal<Vec> POS = ThreadLocal.withInitial(Vec::new);
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

		private final BlockPos.Mutable pos = new BlockPos.Mutable();
		BlockState state;

		public BlockPos relativePos() {
			return pos;
		}

		public static RenderableState findAt(Area area, Vec faceNormal, Vec faceCentre, Predicate<BlockState> isSmoothable) {
			RenderableState renderableState = FIND_AT.get();
			BlockPos.Mutable faceBlockPos = posForFace(faceNormal, faceCentre).assignTo(renderableState.pos);
			BlockState state = area.getBlockState(faceBlockPos);

			// Has always been true in testing so I changed this from a call to tryFindNearbyPosAndState on failure to an assertion
			// This HAS failed due to a race condition with the mesh being generated and then this getting called after
			// the state has been toggled to being un-smoothable with the keybind (so the state WAS smoothable).
			assert isSmoothable.test(state);
			renderableState.state = state;
			return renderableState;
		}

		public static RenderableState findRenderFor(RenderableState foundState, Area area, Direction faceDirection) {
			boolean tryFindSnow = faceDirection == Direction.UP || NoCubesConfig.Client.betterGrassAndSnow;
			if (!tryFindSnow)
				return foundState;
			return findNearbyOrDefault(foundState, FIND_RENDER.get(), area, MeshRenderer::isSnow);
		}

		private static Vec posForFace(Vec faceNormal, Vec faceCentre) {
			return POS.get().set(faceNormal).normalise().multiply(-0.5F).add(faceCentre);
		}

		private static RenderableState findNearbyOrDefault(RenderableState original, RenderableState toUse, Area area, Predicate<BlockState> isSmoothable) {
			if (isSmoothable.test(original.state))
				return original;

			BlockPos.Mutable origin = original.pos;
			BlockPos.Mutable relativePos = toUse.pos;
			for (int i = 0, offsets_orderedLength = OFFSETS_ORDERED.length; i < offsets_orderedLength; i++) {
				BlockPos offset = OFFSETS_ORDERED[i];
				relativePos.set(origin).move(offset);
				BlockState state = area.getBlockState(relativePos);
				if (isSmoothable.test(state)) {
					toUse.state = state;
					return toUse;
				}
			}
			return original;
		}

	}

	public static void renderArea(RendererDispatcher.ChunkRenderInfo renderer, Predicate<BlockState> isSmoothableIn, MeshGenerator generator, Area area) {
		FaceInfo faceInfo = new FaceInfo();
		MeshGenerator.translateToMeshStart(renderer.matrix.matrix, area.start, renderer.chunkPos);
		runForSolidAndSeeThrough(isSmoothableIn, isSmoothable -> {
			generator.generate(area, isSmoothable, (ignored, face) -> {
				faceInfo.setup(face);
				/*@Nullable*/
				RenderableState foundState = RenderableState.findAt(area, faceInfo.normal, faceInfo.centre, isSmoothable);
				/*@Nullable*/
				RenderableState renderState = RenderableState.findRenderFor(foundState, area, faceInfo.approximateDirection);

				assert renderState.state.getRenderShape() != BlockRenderType.INVISIBLE : "We should not have gotten air as a renderable state";

				renderFaceWithConnectedTextures(renderer, area, faceInfo, renderState);

				// Draw grass tufts, plants etc.
				renderExtras(renderer, area, foundState, faceInfo);
				return true;
			});
		});
		ForgeHooksClient.setRenderLayer(null);
	}

	static void renderFaceWithConnectedTextures(RendererDispatcher.ChunkRenderInfo renderer, Area area, FaceInfo faceInfo, RenderableState renderState) {
		BlockState state = renderState.state;
		BlockPos worldPos = MUTABLE.get().set(renderState.relativePos()).move(area.start);

		Material material = state.getMaterial();
		boolean renderBothSides = material != Material.GLASS && material != Material.PORTAL && material != Material.TOP_SNOW && !MeshRenderer.isSolidRender(state);

		FaceLight light = renderer.light.get(area.start, faceInfo.face, faceInfo.normal, FACE_LIGHT.get());
		float shade = renderer.world.getShade(faceInfo.approximateDirection, true);

		renderer.forEachQuad(state, worldPos, faceInfo.approximateDirection, (renderEnv, layer, buffer, quad, emissive) -> {
			Color color = renderer.getColor(quad, state, worldPos).multiply(shade);
			Texture texture = Texture.forQuadRearranged(quad, faceInfo.approximateDirection);
			renderQuad(buffer, renderer.matrix.matrix, faceInfo, color, texture, light, renderBothSides);
		});
	}

	static void renderBreakingTexture(BlockRendererDispatcher dispatcher, BlockState state, BlockPos worldPos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData, MeshGenerator generator, Area area) {
		MeshGenerator.translateToMeshStart(matrix, area.start, worldPos);
		boolean stateSolidity = isSolidRender(state);
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		FaceInfo faceInfo = new FaceInfo();
		generator.generate(area, s -> isSmoothable.test(s) && isSolidRender(s) == stateSolidity, (relativePos, face) -> {
			faceInfo.setup(face);
			boolean renderBothSides = false;
			// Don't need textures or lighting because the crumbling texture overwrites them
			renderQuad(buffer, matrix, faceInfo, Color.WHITE, Texture.EVERYTHING, FaceLight.MAX_BRIGHTNESS, renderBothSides);
			return true;
		});
	}

	static void renderExtras(RendererDispatcher.ChunkRenderInfo renderer, Area area, RenderableState foundState, FaceInfo faceInfo) {
		if (faceInfo.approximateDirection != Direction.UP)
			return;

		BlockPos.Mutable relativeAbove = MUTABLE.get().set(foundState.relativePos()).move(Direction.UP);
		// Render plants
		BlockState stateAbove = area.getBlockState(relativeAbove);
		if (ModUtil.isPlant(stateAbove)) {
			try (FluentMatrixStack ignored = renderer.matrix.push()) {
				BlockPos.Mutable worldAbove = relativeAbove.move(area.start);
				Vec center = faceInfo.centre;
				renderer.matrix.matrix.translate(center.x - 0.5F, center.y, center.z - 0.5F);
				renderer.renderBlock(stateAbove, worldAbove);
				return; // Don't want grass tufts rendering under the plants
			}
		}

		// Render grass tufts
		if (foundState.state.hasProperty(SNOWY)) {
			BlockState grass = Blocks.GRASS.defaultBlockState();
			BlockPos.Mutable worldAbove = relativeAbove.move(area.start);
			boolean renderBothSides = true;

			Vector3d offset = grass.getOffset(renderer.world, worldAbove);
			float xOff = (float) offset.x;
			float zOff = (float) offset.z;
			float yExt = 0.4F;
			boolean isSnow = foundState.state.getValue(SNOWY);
			Face face = faceInfo.face;

			FaceInfo grassTuft0 = GRASS_TUFT_0.get();
			setupGrassTuft(grassTuft0.face, face.v2, face.v0, xOff, yExt, zOff);
			FaceLight light0 = renderer.light.get(area.start, grassTuft0, GRASS_TUFT_0_LIGHT.get());

			FaceInfo grassTuft1 = GRASS_TUFT_1.get();
			setupGrassTuft(grassTuft1.face, face.v3, face.v1, xOff, yExt, zOff);
			FaceLight light1 = renderer.light.get(area.start, grassTuft1, GRASS_TUFT_1_LIGHT.get());

			MatrixStack matrix = renderer.matrix.matrix;
			renderer.forEachQuad(grass, worldAbove, null, (renderEnv, layer, buffer, quad, emissive) -> {
				Color color = isSnow ? Color.WHITE : renderer.getColor(quad, grass, worldAbove);
				Texture uvs = Texture.forQuad(quad);
				renderQuad(buffer, matrix, grassTuft0, color, uvs, emissive ? FaceLight.MAX_BRIGHTNESS : light0, renderBothSides);
				renderQuad(buffer, matrix, grassTuft1, color, uvs, emissive ? FaceLight.MAX_BRIGHTNESS : light1, renderBothSides);
			});
		}

	}

	private static void setupGrassTuft(Face face, Vec v0, Vec v1, float xOff, float yExt, float zOff) {
		face.set(v0, v0, v1, v1);
		face.v1.y += yExt;
		face.v2.y += yExt;
		face.add(xOff, 0, zOff);
	}

	static void renderFace(
		RendererDispatcher.ChunkRenderInfo renderer,
		BlockState state, BlockPos worldPos,
		FaceInfo faceInfo,
		IVertexBuilder buffer, MatrixStack matrix,
		float shade, FaceLight light,
		Object renderEnv, List<BakedQuad> quads, boolean doubleSided
	) {
		OptiFineProxy optiFine = renderer.optiFine;
		for (int i = 0, l = quads.size(); i < l; ++i) {
			BakedQuad quad = quads.get(i);
			Color color = renderer.getColor(quad, state, worldPos).multiply(shade);

			BakedQuad emissive = optiFine == null ? null : optiFine.getQuadEmissive(quad);
			if (emissive != null) {
				optiFine.preRenderQuad(renderEnv, emissive, state, worldPos);
				renderQuad(buffer, matrix, faceInfo, color, Texture.forQuadRearranged(quad, faceInfo.approximateDirection), FaceLight.MAX_BRIGHTNESS, doubleSided);
			}
			if (optiFine != null)
				optiFine.preRenderQuad(renderEnv, quad, state, worldPos);
			renderQuad(buffer, matrix, faceInfo, color, Texture.forQuadRearranged(quad, faceInfo.approximateDirection), light, doubleSided);
		}
	}

	private static void renderQuad(
		IVertexBuilder buffer, MatrixStack matrix,
		FaceInfo faceInfo,
		Color color,
		Texture uvs,
		FaceLight light,
		boolean doubleSided
	) {
		renderQuad(buffer, matrix, faceInfo.face, faceInfo.normal, color, uvs, light, doubleSided);
	}

	private static void renderQuad(
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

	public static final /* inline record */ class FaceInfo {
		public /* final */ Face face;
		public final Vec centre = new Vec();
		public final Face vertexNormals = new Face();
		public final Vec normal = new Vec();
		public Direction approximateDirection;

		public void setup(Face face) {
			this.face = face;
			face.assignAverageTo(centre);
			face.assignNormalTo(vertexNormals);
			vertexNormals.multiply(-1).assignAverageTo(normal);
			approximateDirection = normal.getDirectionFromNormal();
		}

		public static FaceInfo withFace() {
			FaceInfo faceInfo = new FaceInfo();
			faceInfo.setup(new Face());
			return faceInfo;
		}
	}

	private static boolean isSnow(BlockState state) {
		Block block = state.getBlock();
		return block == Blocks.SNOW || block == Blocks.SNOW_BLOCK;
	}

}
