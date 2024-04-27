package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.mesh.OldNoCubes;
import io.github.cadiboo.nocubes.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.mesh.Mesher.getMeshOffset;
import static net.minecraft.world.level.block.SnowyDirtBlock.SNOWY;

public class MeshRenderer {

	public interface INoCubesAreaRenderer {
		void quad(
			BlockState state, BlockPos worldPos,
			FaceInfo faceInfo, boolean renderBothSides,
			Color colorOverride
		);

		void block(
			BlockState state, BlockPos worldPos,
			float relativeX, float relativeY, float relativeZ
		);
	}

	public static boolean isSolidRender(BlockState state) {
		return state.isSolidRender(EmptyBlockGetter.INSTANCE, BlockPos.ZERO) || state.getBlock() instanceof DirtPathBlock;
	}

	public static boolean renderSingleBlock(
		BlockGetter world, BlockPos pos, BlockState state,
		INoCubesAreaRenderer renderer
	) {
		var smoothable = NoCubes.smoothableHandler.isSmoothable(state);
		var plant = NoCubesConfig.Client.fixPlantHeight && ModUtil.isShortPlant(state);
		if (!smoothable && !plant)
			return false;
		var mesher = NoCubesConfig.Server.mesher;
		var stateSolidity = MeshRenderer.isSolidRender(state);
		try (var area = new Area(world, pos, ModUtil.VEC_ONE, mesher)) {
			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
			MeshRenderer.renderArea(
				world, pos,
				s -> isSmoothable.test(s) && (plant || MeshRenderer.isSolidRender(s) == stateSolidity), mesher, area,
				new INoCubesAreaRenderer() {
					@Override
					public void quad(BlockState state, BlockPos worldPos, FaceInfo faceInfo, boolean renderBothSides, Color colorOverride) {
						if (ModUtil.isShortPlant(state) != plant)
							return;
						renderer.quad(state, worldPos, faceInfo, renderBothSides, colorOverride);
					}

					@Override
					public void block(BlockState state, BlockPos worldPos, float relativeX, float relativeY, float relativeZ) {
						if (ModUtil.isShortPlant(state) != plant)
							return;
						renderer.block(state, worldPos, relativeX, relativeY, relativeZ);
					}
				}
			);
		}
		return true;
	}

	public static void renderChunk(
		BlockAndTintGetter world, BlockPos chunkPos,
		INoCubesAreaRenderer renderer
	) {
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		var mesher = NoCubesConfig.Server.mesher;
		try (
			var area = new Area(Minecraft.getInstance().level, chunkPos, ModUtil.CHUNK_SIZE, mesher);
		) {
			renderArea(world, chunkPos, isSmoothable, mesher, area, renderer);
		}
	}
	public static void renderArea(
		BlockGetter world, BlockPos renderStartPos,
		Predicate<BlockState> isSmoothableIn, Mesher mesher, Area area,
		INoCubesAreaRenderer renderer
	) {
		var faceInfo = FaceInfo.INSTANCE.get();
		var objects = MutableObjects.INSTANCE.get();
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

				var state = renderState.state;
				var worldPos = objects.pos.set(renderState.relativePos()).move(area.start);

				var block = state.getBlock();
				var renderBothSides = !(block instanceof BeaconBeamBlock) && !(block instanceof NetherPortalBlock || block instanceof EndPortalBlock) && !(block instanceof SnowLayerBlock) && !isSolidRender(state);

				faceInfo.reverseMeshOffset(renderStartPos, area.start);
				renderer.quad(
					state, worldPos,
					faceInfo, renderBothSides,
					null
				);

				// Draw grass tufts, plants etc.
				renderExtras(renderer, objects, world, area, foundState, renderState, faceInfo);
				return true;
			});
		});
	}

	private static void runForSolidAndSeeThrough(Predicate<BlockState> isSmoothable, Consumer<Predicate<BlockState>> action) {
		action.accept(state -> isSmoothable.test(state) && isSolidRender(state));
		action.accept(state -> isSmoothable.test(state) && !isSolidRender(state));
	}

	private static void renderExtras(
		INoCubesAreaRenderer renderer, MutableObjects objects,
		BlockGetter world,
		Area area, RenderableState foundState,
		RenderableState renderState,
		FaceInfo faceInfo
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
			var worldAbove = relativeAbove.move(area.start);
			var center = faceInfo.centre;
			renderer.block(
				stateAbove, worldAbove,
				center.x - 0.5F, center.y, center.z - 0.5F
			);
		}

		if (renderGrassTufts && foundState.state.hasProperty(SNOWY) && !ModUtil.isPlant(stateAbove)) {
			var grass = Blocks.GRASS.defaultBlockState();
			var worldAbove = relativeAbove.move(area.start);
			var renderBothSides = true;

			var offset = grass.getOffset(world, worldAbove);
			var xOff = (float) offset.x;
			var zOff = (float) offset.z;
			var yExt = 0.4F;
			var snowy = isSnow(renderState.state) || (renderState.state.hasProperty(SNOWY) && renderState.state.getValue(SNOWY));
			var colorOverride = snowy ? Color.WHITE : null;
			var face = faceInfo.face;
			var grassTuft = objects.grassTuft;

			setupGrassTuft(grassTuft.face, face.v2, face.v0, xOff, yExt, zOff);
			renderer.quad(
				grass, worldAbove,
				grassTuft, renderBothSides,
				colorOverride
			);

			setupGrassTuft(grassTuft.face, face.v3, face.v1, xOff, yExt, zOff);
			renderer.quad(
				grass, worldAbove,
				grassTuft, renderBothSides,
				colorOverride
			);
		}

	}

	private static void setupGrassTuft(Face face, Vec v0, Vec v1, float xOff, float yExt, float zOff) {
		face.set(v0, v0, v1, v1);
		face.v1.y += yExt;
		face.v2.y += yExt;
		face.add(xOff, 0, zOff);
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

		public void reverseMeshOffset(BlockPos renderStartPos, BlockPos areaStart) {
			face.add(
				getMeshOffset(areaStart.getX(), renderStartPos.getX()),
				getMeshOffset(areaStart.getY(), renderStartPos.getY()),
				getMeshOffset(areaStart.getZ(), renderStartPos.getZ())
			);
			centre.add(
				getMeshOffset(areaStart.getX(), renderStartPos.getX()),
				getMeshOffset(areaStart.getY(), renderStartPos.getY()),
				getMeshOffset(areaStart.getZ(), renderStartPos.getZ())
			);
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
		final FaceInfo grassTuft = FaceInfo.withFace();
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
