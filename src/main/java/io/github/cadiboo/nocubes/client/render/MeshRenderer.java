package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.OldNoCubes;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EmptyBlockReader;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.render.RendererDispatcher.ChunkRenderInfo;
import static io.github.cadiboo.nocubes.client.render.RendererDispatcher.quad;
import static net.minecraft.block.GrassBlock.SNOWY;

// /tp @p 83.63 64.26 -112.34 -90.10 -6.33 Wall
// /tp @p 87.64 75.43 -180.15 -158.48 33.15 Hillside
public final class MeshRenderer {

	public static boolean isSolidRender(BlockState state) {
		return state.isSolidRender(EmptyBlockReader.INSTANCE, BlockPos.ZERO);
	}

	public static void runForSolidAndSeeThrough(Predicate<BlockState> isSmoothable, Consumer<Predicate<BlockState>> action) {
		action.accept(state -> isSmoothable.test(state) && isSolidRender(state));
		action.accept(state -> isSmoothable.test(state) && !isSolidRender(state));
	}

	public static void renderArea(ChunkRenderInfo renderer, Predicate<BlockState> isSmoothableIn, MeshGenerator generator, Area area) {
		FaceInfo faceInfo = new FaceInfo();
		MutableObjects objects = new MutableObjects();
		MeshGenerator.translateToMeshStart(renderer.matrix.matrix, area.start, renderer.chunkPos);
		runForSolidAndSeeThrough(isSmoothableIn, isSmoothable -> {
			generator.generate(area, isSmoothable, (ignored, face) -> {
				faceInfo.setup(face);
				RenderableState foundState;
				if (generator instanceof OldNoCubes) {
					foundState = objects.foundState;
					foundState.state = area.getBlockState(ignored);
					foundState.pos.set(ignored);
				} else
					foundState = RenderableState.findAt(objects, area, faceInfo.normal, faceInfo.centre, isSmoothable);
				RenderableState renderState = RenderableState.findRenderFor(objects, foundState, area, faceInfo.approximateDirection);

				assert renderState.state.getRenderShape() != BlockRenderType.INVISIBLE : "We should not have gotten air as a renderable state";

				renderFaceWithConnectedTextures(renderer, objects, area, faceInfo, renderState);

				// Draw grass tufts, plants etc.
				renderExtras(renderer, objects, area, foundState, renderState, faceInfo);
				return true;
			});
		});
		ForgeHooksClient.setRenderLayer(null);
	}

	static void renderBreakingTexture(BlockState state, BlockPos worldPos, MatrixStack matrix, IVertexBuilder buffer, MeshGenerator generator, Area area) {
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

	static void renderFaceWithConnectedTextures(ChunkRenderInfo renderer, MutableObjects objects, Area area, FaceInfo faceInfo, RenderableState renderState) {
		BlockState state = renderState.state;
		BlockPos worldPos = objects.pos.set(renderState.relativePos()).move(area.start);

		Material material = state.getMaterial();
		boolean renderBothSides = material != Material.GLASS && material != Material.PORTAL && material != Material.TOP_SNOW && !MeshRenderer.isSolidRender(state);

		FaceLight light = renderer.light.get(area.start, faceInfo.face, faceInfo.normal, objects.light);
		float shade = renderer.getShade(faceInfo.approximateDirection);

		renderer.forEachQuad(
			state, worldPos, faceInfo.approximateDirection,
			(colorState, colorWorldPos, quad) -> renderer.getColor(objects.color, quad, colorState, colorWorldPos, shade),
			(layer, buffer, quad, color, emissive) -> {
				Texture texture = Texture.forQuadRearranged(objects.texture, quad, faceInfo.approximateDirection);
				renderQuad(buffer, renderer.matrix.matrix, faceInfo, color, texture, emissive ? FaceLight.MAX_BRIGHTNESS : light, renderBothSides);
			}
		);
	}

	static void renderExtras(ChunkRenderInfo renderer, MutableObjects objects, Area area, RenderableState foundState, RenderableState renderState, FaceInfo faceInfo) {
		boolean renderPlantsOffset = NoCubesConfig.Client.fixPlantHeight;
		boolean renderGrassTufts = NoCubesConfig.Client.grassTufts;
		if (!renderPlantsOffset && !renderGrassTufts)
			return;

		if (faceInfo.approximateDirection != Direction.UP)
			return;

		BlockPos.Mutable relativeAbove = objects.pos.set(foundState.relativePos()).move(Direction.UP);
		if (renderPlantsOffset) {
			BlockState stateAbove = area.getBlockState(relativeAbove);
			if (ModUtil.isShortPlant(stateAbove)) {
				try (FluentMatrixStack ignored = renderer.matrix.push()) {
					BlockPos.Mutable worldAbove = relativeAbove.move(area.start);
					Vec center = faceInfo.centre;
					renderer.matrix.matrix.translate(center.x - 0.5F, center.y, center.z - 0.5F);
					renderer.renderBlock(stateAbove, worldAbove);
					return; // Don't want grass tufts rendering under the plants
				}
			}
		}

		if (renderGrassTufts && foundState.state.hasProperty(SNOWY)) {
			BlockState grass = Blocks.GRASS.defaultBlockState();
			BlockPos.Mutable worldAbove = relativeAbove.move(area.start);
			boolean renderBothSides = true;

			Vector3d offset = grass.getOffset(renderer.world, worldAbove);
			float xOff = (float) offset.x;
			float zOff = (float) offset.z;
			float yExt = 0.4F;
			boolean snowy = isSnow(renderState.state) || (renderState.state.hasProperty(SNOWY) && renderState.state.getValue(SNOWY));
			Face face = faceInfo.face;

			FaceInfo grassTuft0 = objects.grassTuft0;
			setupGrassTuft(grassTuft0.face, face.v2, face.v0, xOff, yExt, zOff);
			FaceLight light0 = renderer.light.get(area.start, grassTuft0, objects.grassTuft0Light);
			float shade0 = renderer.getShade(grassTuft0.approximateDirection);

			FaceInfo grassTuft1 = objects.grassTuft1;
			setupGrassTuft(grassTuft1.face, face.v3, face.v1, xOff, yExt, zOff);
			FaceLight light1 = renderer.light.get(area.start, grassTuft1, objects.grassTuft1Light);
			float shade1 = renderer.getShade(grassTuft1.approximateDirection);

			MatrixStack matrix = renderer.matrix.matrix;
			renderer.forEachQuad(
				grass, worldAbove, null,
				(state, worldPos, quad) -> snowy ? Color.WHITE : renderer.getColor(objects.color, quad, grass, worldAbove, 1F),
				(layer, buffer, quad, color, emissive) -> {
					// This is super ugly because Color is mutable. Will be fixed by Valhalla (color will be an inline type)
					int argbTEMP = color.packToARGB();

					color.multiplyUNSAFENEEDSVALHALLA(shade0);
					renderQuad(buffer, matrix, grassTuft0, color, Texture.forQuadRearranged(objects.texture, quad, grassTuft0.approximateDirection), emissive ? FaceLight.MAX_BRIGHTNESS : light0, renderBothSides);

					color.unpackFromARGB(argbTEMP);
					color.multiplyUNSAFENEEDSVALHALLA(shade1);
					renderQuad(buffer, matrix, grassTuft1, color, Texture.forQuadRearranged(objects.texture, quad, grassTuft1.approximateDirection), emissive ? FaceLight.MAX_BRIGHTNESS : light1, renderBothSides);

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
		IVertexBuilder buffer, MatrixStack matrix,
		FaceInfo faceInfo,
		Color color,
		Texture uvs,
		FaceLight light,
		boolean renderBothSides
	) {
		quad(buffer, matrix, faceInfo.face, faceInfo.normal, color, uvs, light, renderBothSides);
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

	/**
	 * Until Project Valhalla is complete and inline types exist we pass around a bunch of mutable objects.
	 */
	static final class MutableObjects {
		final FaceLight light = new FaceLight();
		final RenderableState foundState = new RenderableState();
		final RenderableState renderState = new RenderableState();
		final Vec vec = new Vec();
		final FaceInfo grassTuft0 = FaceInfo.withFace();
		final FaceLight grassTuft0Light = new FaceLight();
		final FaceInfo grassTuft1 = FaceInfo.withFace();
		final FaceLight grassTuft1Light = new FaceLight();
		final BlockPos.Mutable pos = new BlockPos.Mutable();
		final Color color = new Color();
		final Texture texture = new Texture();
	}

	static final class RenderableState {

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

		public static RenderableState findAt(MutableObjects objects, Area area, Vec faceNormal, Vec faceCentre, Predicate<BlockState> isSmoothable) {
			RenderableState foundState = objects.foundState;
			BlockPos.Mutable faceBlockPos = posForFace(objects.vec, faceNormal, faceCentre).assignTo(foundState.pos);
			BlockState state = area.getBlockState(faceBlockPos);

			// Has always been true in testing so I changed this from a call to tryFindNearbyPosAndState on failure to an assertion
			// This HAS failed due to a race condition with the mesh being generated and then this getting called after
			// the state has been toggled to being un-smoothable with the keybind (so the state WAS smoothable).
			assert "true".equals("true");
			foundState.state = state;
			return foundState;
		}

		public static RenderableState findRenderFor(MutableObjects objects, RenderableState foundState, Area area, Direction faceDirection) {
			boolean tryFindSnow = faceDirection == Direction.UP || NoCubesConfig.Client.moreSnow;
			if (!tryFindSnow)
				return foundState;
			return findNearbyOrDefault(foundState, objects.renderState, area, MeshRenderer::isSnow);
		}

		private static Vec posForFace(Vec pos, Vec faceNormal, Vec faceCentre) {
			return pos.set(faceNormal).normalise().multiply(-0.5F).add(faceCentre);
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

	private static boolean isSnow(BlockState state) {
		Block block = state.getBlock();
		return block == Blocks.SNOW || block == Blocks.SNOW_BLOCK;
	}

}
