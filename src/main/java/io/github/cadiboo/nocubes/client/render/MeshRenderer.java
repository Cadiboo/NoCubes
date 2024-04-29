package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.mesh.OldNoCubes;
import io.github.cadiboo.nocubes.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.render.RendererDispatcher.ChunkRenderInfo;
import static io.github.cadiboo.nocubes.client.render.RendererDispatcher.quad;
import static net.minecraft.world.level.block.GrassBlock.SNOWY;

public final class MeshRenderer {

	public static boolean isSolidRender(BlockState state) {
		return state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) || state.getBlock() instanceof DirtPathBlock;
	}

	public static void runForSolidAndSeeThrough(Predicate<BlockState> isSmoothable, Consumer<Predicate<BlockState>> action) {
		action.accept(state -> isSmoothable.test(state) && isSolidRender(state));
		action.accept(state -> isSmoothable.test(state) && !isSolidRender(state));
	}

	public static void renderArea(ChunkRenderInfo renderer, Predicate<BlockState> isSmoothableIn, Mesher mesher, Area area) {
		var faceInfo = FaceInfo.INSTANCE.get();
		var objects = MutableObjects.INSTANCE.get();
		Mesher.translateToMeshStart(renderer.matrix.matrix(), area.start, renderer.chunkPos);
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

				renderFaceWithConnectedTextures(renderer, objects, area, faceInfo, renderState);

				// Draw grass tufts, plants etc.
				renderExtras(renderer, objects, area, foundState, renderState, faceInfo);
				return true;
			});
		});
	}

	static void renderBreakingTexture(BlockState state, BlockPos worldPos, PoseStack matrix, VertexConsumer buffer, Mesher mesher, Area area) {
		Mesher.translateToMeshStart(matrix, area.start, worldPos);
		var stateSolidity = isSolidRender(state);
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		var faceInfo = FaceInfo.INSTANCE.get();
		mesher.generateGeometry(area, s -> isSmoothable.test(s) && isSolidRender(s) == stateSolidity, (relativePos, face) -> {
			faceInfo.setup(face);
			var renderBothSides = false;
			// Don't need textures or lighting because the crumbling texture overwrites them
			renderQuad(buffer, matrix, faceInfo, Color.WHITE, Texture.EVERYTHING, FaceLight.MAX_BRIGHTNESS, renderBothSides);
			return true;
		});
	}

	static void renderFaceWithConnectedTextures(ChunkRenderInfo renderer, MutableObjects objects, Area area, FaceInfo faceInfo, RenderableState renderState) {
		var state = renderState.state;
		var worldPos = objects.pos.set(renderState.relativePos()).move(area.start);

		var block = state.getBlock();
		var renderBothSides = !(block instanceof BeaconBeamBlock) && !(block instanceof NetherPortalBlock || block instanceof  EndPortalBlock) && !(block instanceof SnowLayerBlock) && !MeshRenderer.isSolidRender(state);

		var light = renderer.light.get(area.start, faceInfo.face, faceInfo.normal, objects.light);
		var shade = renderer.getShade(faceInfo.approximateDirection);

		renderer.forEachQuad(
			state, worldPos, faceInfo.approximateDirection,
			(colorState, colorWorldPos, quad) -> renderer.getColor(objects.color, quad, colorState, colorWorldPos, shade),
			(layer, buffer, quad, color, emissive) -> {
				var texture = Texture.forQuadRearranged(objects.texture, quad, faceInfo.approximateDirection);
				renderQuad(buffer, renderer.matrix.matrix(), faceInfo, color, texture, emissive ? FaceLight.MAX_BRIGHTNESS : light, renderBothSides);
			}
		);
	}

	static void renderExtras(ChunkRenderInfo renderer, MutableObjects objects, Area area, RenderableState foundState, RenderableState renderState, FaceInfo faceInfo) {
		var renderPlantsOffset = NoCubesConfig.Client.fixPlantHeight;
		var renderGrassTufts = NoCubesConfig.Client.grassTufts;
		if (!renderPlantsOffset && !renderGrassTufts)
			return;

		if (faceInfo.approximateDirection != Direction.UP)
			return;

		var relativeAbove = objects.pos.set(foundState.relativePos()).move(Direction.UP);
		var stateAbove = area.getBlockStateFaultTolerant(relativeAbove);
		if (renderPlantsOffset && ModUtil.isShortPlant(stateAbove)) {
			try (var ignored = renderer.matrix.push()) {
				var worldAbove = relativeAbove.move(area.start);
				var center = faceInfo.centre;
				renderer.matrix.matrix().translate(center.x - 0.5F, center.y, center.z - 0.5F);
				renderer.renderBlock(stateAbove, worldAbove);
			}
		}

		if (renderGrassTufts && foundState.state.hasProperty(SNOWY) && !ModUtil.isPlant(stateAbove)) {
			var grass = Blocks.GRASS.defaultBlockState();
			var worldAbove = relativeAbove.move(area.start);
			var renderBothSides = true;

			var offset = grass.getOffset(renderer.world, worldAbove);
			var xOff = (float) offset.x;
			var zOff = (float) offset.z;
			var yExt = 0.4F;
			var snowy = isSnow(renderState.state) || (renderState.state.hasProperty(SNOWY) && renderState.state.getValue(SNOWY));
			var face = faceInfo.face;

			var grassTuft0 = objects.grassTuft0;
			setupGrassTuft(grassTuft0.face, face.v2, face.v0, xOff, yExt, zOff);
			var light0 = renderer.light.get(area.start, grassTuft0, objects.grassTuft0Light);
			var shade0 = renderer.getShade(grassTuft0.approximateDirection);

			var grassTuft1 = objects.grassTuft1;
			setupGrassTuft(grassTuft1.face, face.v3, face.v1, xOff, yExt, zOff);
			var light1 = renderer.light.get(area.start, grassTuft1, objects.grassTuft1Light);
			var shade1 = renderer.getShade(grassTuft1.approximateDirection);

			var matrix = renderer.matrix.matrix();
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
		VertexConsumer buffer, PoseStack matrix,
		FaceInfo faceInfo,
		Color color,
		Texture uvs,
		FaceLight light,
		boolean renderBothSides
	) {
		quad(buffer, matrix, faceInfo.face, faceInfo.normal, color, uvs, light, renderBothSides);
	}

	@PerformanceCriticalAllocation
	public static final /* inline record */ class FaceInfo {
		public static final ThreadLocal<FaceInfo> INSTANCE = ThreadLocal.withInitial(FaceInfo::new);

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
			var faceInfo = new FaceInfo();
			faceInfo.setup(new Face());
			return faceInfo;
		}
	}

	/**
	 * Until Project Valhalla is complete and inline types exist we pass around a bunch of mutable objects.
	 */
	@PerformanceCriticalAllocation
	static final class MutableObjects {
		public static final ThreadLocal<MutableObjects> INSTANCE = ThreadLocal.withInitial(MutableObjects::new);
		final FaceLight light = new FaceLight();
		final RenderableState foundState = new RenderableState();
		final RenderableState renderState = new RenderableState();
		final Vec vec = new Vec();
		final FaceInfo grassTuft0 = FaceInfo.withFace();
		final FaceLight grassTuft0Light = new FaceLight();
		final FaceInfo grassTuft1 = FaceInfo.withFace();
		final FaceLight grassTuft1Light = new FaceLight();
		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		final Color color = new Color();
		final Texture texture = new Texture();
	}

	@PerformanceCriticalAllocation
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

		final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		BlockState state;

		public BlockPos relativePos() {
			return pos;
		}

		public static RenderableState findAt(MutableObjects objects, Area area, Vec faceNormal, Vec faceCentre, Predicate<BlockState> isSmoothable) {
			var foundState = objects.foundState;
			var faceBlockPos = posForFace(objects.vec, faceNormal, faceCentre).assignTo(foundState.pos);
			var state = area.getBlockStateFaultTolerant(faceBlockPos);

			// Has always been true in testing, so I changed this from a call to tryFindNearbyPosAndState on failure to an assertion
			// This HAS failed due to a race condition with the mesh being generated and then this getting called after
			// the state has been toggled to being un-smoothable with the keybind (so the state WAS smoothable).
			// assert isSmoothable.test(state)

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

			var relativePos = toUse.pos;
			for (int i = 0, offsets_orderedLength = OFFSETS_ORDERED.length; i < offsets_orderedLength; i++) {
				var offset = OFFSETS_ORDERED[i];
				relativePos.set(original.pos).move(offset);
				var state = area.getBlockStateFaultTolerant(relativePos);
				if (isSmoothable.test(state)) {
					toUse.state = state;
					return toUse;
				}
			}
			return original;
		}

	}

	static boolean isSnow(BlockState state) {
		var block = state.getBlock();
		return block == Blocks.SNOW || block == Blocks.SNOW_BLOCK;
	}

}
