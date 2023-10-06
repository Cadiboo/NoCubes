package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.LightCache;
import io.github.cadiboo.nocubes.client.LightmapInfo;
import io.github.cadiboo.nocubes.client.UVHelper;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher.ChunkRenderInfo;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.PoseStack;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.mesh.OldNoCubes;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.render.RenderDispatcher.quad;
import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.UP;
import static net.minecraft.util.EnumFacing.WEST;
import static net.minecraft.util.math.BlockPos.MutableBlockPos;

/**
 * @author Cadiboo
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class MeshRenderer {

	private static final Logger LOGGER = LogManager.getLogger("NoCubes MeshRenderer");

	public static boolean isSolidRender(IBlockState state) {
		return state.getMaterial().isOpaque() || state.getBlock() instanceof BlockGrassPath;
	}

	public static void runForSolidAndSeeThrough(Predicate<IBlockState> isSmoothable, Consumer<Predicate<IBlockState>> action) {
		action.accept(state -> isSmoothable.test(state) && isSolidRender(state));
		action.accept(state -> isSmoothable.test(state) && !isSolidRender(state));
	}

	public static void renderArea(ChunkRenderInfo renderer, Predicate<IBlockState> isSmoothableIn, Mesher mesher, Area area, LightCache light) {
		PoseStack matrix = new PoseStack();
		FaceInfo faceInfo = new FaceInfo();
		MutableObjects objects = new MutableObjects();
		Mesher.translateToMeshStart(matrix, area.start, renderer.chunkPos);
		matrix.x += renderer.chunkPos.getX();
		matrix.y += renderer.chunkPos.getY();
		matrix.z += renderer.chunkPos.getZ();
		runForSolidAndSeeThrough(isSmoothableIn, isSmoothable -> {
			mesher.generateGeometry(area, isSmoothable, (relativePos, face) -> {
				faceInfo.setup(face);
				RenderableState foundState;
				if (mesher instanceof OldNoCubes) {
					foundState = objects.foundState;
					foundState.state = area.getBlockState(relativePos);
					foundState.pos.setPos(relativePos);
				} else
					foundState = RenderableState.findAt(objects, area, faceInfo.normal, faceInfo.centre, isSmoothable);
				RenderableState renderState = RenderableState.findRenderFor(objects, foundState, area, faceInfo.approximateDirection);

				if (renderState.state.getRenderType() == EnumBlockRenderType.INVISIBLE)
					return true; // How?

				renderFaceWithConnectedTextures(renderer, matrix, objects, area, light, faceInfo, renderState);

//				// Draw grass tufts, plants etc.
//				renderExtras(renderer, objects, area, foundState, renderState, faceInfo);
				return true;
			});
		});
	}

	static void renderBreakingTexture(IBlockState state, BlockPos worldPos, PoseStack matrix, BufferBuilder buffer, TextureAtlasSprite sprite, Mesher mesher, Area area) {
		Mesher.translateToMeshStart(matrix, area.start, worldPos);
		matrix.x += worldPos.getX();
		matrix.y += worldPos.getY();
		matrix.z += worldPos.getZ();
		boolean stateSolidity = isSolidRender(state);
		Predicate<IBlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		FaceInfo faceInfo = new FaceInfo();

		float minU = UVHelper.getMinU(sprite);
		float maxU = UVHelper.getMaxU(sprite);
		float minV = UVHelper.getMinV(sprite);
		float maxV = UVHelper.getMaxV(sprite);
		Texture texture = new Texture(
			minU, minV,
			minU, maxV,
			maxU, maxV,
			maxU, minV
		);
		mesher.generateGeometry(area, s -> isSmoothable.test(s) && isSolidRender(s) == stateSolidity, (relativePos, face) -> {
			faceInfo.setup(face);
			boolean renderBothSides = false;
			// Don't need textures or lighting because the crumbling texture overwrites them
			renderQuad(buffer, matrix, faceInfo, Color.WHITE, texture, FaceLight.MAX_BRIGHTNESS, renderBothSides);
			return true;
		});
	}

	static void renderFaceWithConnectedTextures(ChunkRenderInfo renderer, PoseStack matrix, MutableObjects objects, Area area, LightCache areaLight, FaceInfo faceInfo, RenderableState renderState) {
		IBlockState state = renderState.state;
		BlockPos worldPos = objects.pos.setPos(renderState.relativePos()).add(area.start);

		Block block = state.getBlock();
		boolean renderBothSides = !(block instanceof BlockBreakable) && !(block instanceof BlockSnow) && !MeshRenderer.isSolidRender(state);

//		var light = renderer.light.get(area.start, faceInfo.face, faceInfo.normal, objects.light);
		FaceLight light = objects.light;
		try (LightmapInfo lightmapInfo = LightmapInfo.generateLightmapInfo(areaLight, faceInfo.face.v0, faceInfo.face.v1, faceInfo.face.v2, faceInfo.face.v3, objects.pos)) {
			light.v0 = (lightmapInfo.skylight0 & 0xFFFF) << 16 | lightmapInfo.blocklight0 & 0xFFFF;
			light.v1 = (lightmapInfo.skylight1 & 0xFFFF) << 16 | lightmapInfo.blocklight1 & 0xFFFF;
			light.v2 = (lightmapInfo.skylight2 & 0xFFFF) << 16 | lightmapInfo.blocklight2 & 0xFFFF;
			light.v3 = (lightmapInfo.skylight3 & 0xFFFF) << 16 | lightmapInfo.blocklight3 & 0xFFFF;
		}
		float shade = renderer.getShade(faceInfo.approximateDirection);

		renderer.forEachQuad(
			state, worldPos, faceInfo.approximateDirection,
			(colorState, colorWorldPos, quad) -> renderer.getColor(objects.color, quad, colorState, colorWorldPos, shade),
			(layer, buffer, quad, color, emissive) -> {
				Texture texture = Texture.forQuadRearranged(objects.texture, quad, faceInfo.approximateDirection);
				renderQuad(buffer, matrix, faceInfo, color, texture, emissive ? FaceLight.MAX_BRIGHTNESS : light, renderBothSides);
			}
		);
	}

//	static void renderExtras(ChunkRenderInfo renderer, MutableObjects objects, Area area, RenderableState foundState, RenderableState renderState, FaceInfo faceInfo) {
//		boolean renderPlantsOffset = NoCubesConfig.Client.fixPlantHeight;
//		boolean renderGrassTufts = NoCubesConfig.Client.grassTufts;
//		if (!renderPlantsOffset && !renderGrassTufts)
//			return;
//
//		if (faceInfo.approximateDirection != EnumFacing.UP)
//			return;
//
//		BlockPos relativeAbove = objects.pos.setPos(foundState.relativePos()).add(EnumFacing.UP);
//		IBlockState stateAbove = area.getBlockState(relativeAbove);
//		if (renderPlantsOffset && ModUtil.isShortPlant(stateAbove)) {
//			try (var ignored = renderer.matrix.push()) {
//				var worldAbove = relativeAbove.move(area.start);
//				var center = faceInfo.centre;
//				renderer.matrix.matrix().translate(center.x - 0.5F, center.y, center.z - 0.5F);
//				renderer.renderBlock(stateAbove, worldAbove);
//			}
//		}
//
//		if (renderGrassTufts && foundState.state.hasProperty(SNOWY) && !ModUtil.isPlant(stateAbove)) {
//			var grass = Blocks.GRASS.defaultBlockState();
//			var worldAbove = relativeAbove.move(area.start);
//			var renderBothSides = true;
//
//			var offset = grass.getOffset(renderer.world, worldAbove);
//			var xOff = (float) offset.x;
//			var zOff = (float) offset.z;
//			var yExt = 0.4F;
//			var snowy = isSnow(renderState.state) || (renderState.state.hasProperty(SNOWY) && renderState.state.getValue(SNOWY));
//			var face = faceInfo.face;
//
//			var grassTuft0 = objects.grassTuft0;
//			setupGrassTuft(grassTuft0.face, face.v2, face.v0, xOff, yExt, zOff);
//			var light0 = renderer.light.get(area.start, grassTuft0, objects.grassTuft0Light);
//			var shade0 = renderer.getShade(grassTuft0.approximateDirection);
//
//			var grassTuft1 = objects.grassTuft1;
//			setupGrassTuft(grassTuft1.face, face.v3, face.v1, xOff, yExt, zOff);
//			var light1 = renderer.light.get(area.start, grassTuft1, objects.grassTuft1Light);
//			var shade1 = renderer.getShade(grassTuft1.approximateDirection);
//
//			var matrix = renderer.matrix.matrix();
//			renderer.forEachQuad(
//				grass, worldAbove, null,
//				(state, worldPos, quad) -> snowy ? Color.WHITE : renderer.getColor(objects.color, quad, grass, worldAbove, 1F),
//				(layer, buffer, quad, color, emissive) -> {
//					// This is super ugly because Color is mutable. Will be fixed by Valhalla (color will be an inline type)
//					int argbTEMP = color.packToARGB();
//
//					color.multiplyUNSAFENEEDSVALHALLA(shade0);
//					renderQuad(buffer, matrix, grassTuft0, color, Texture.forQuadRearranged(objects.texture, quad, grassTuft0.approximateDirection), emissive ? FaceLight.MAX_BRIGHTNESS : light0, renderBothSides);
//
//					color.unpackFromARGB(argbTEMP);
//					color.multiplyUNSAFENEEDSVALHALLA(shade1);
//					renderQuad(buffer, matrix, grassTuft1, color, Texture.forQuadRearranged(objects.texture, quad, grassTuft1.approximateDirection), emissive ? FaceLight.MAX_BRIGHTNESS : light1, renderBothSides);
//
//					color.unpackFromARGB(argbTEMP);
//				}
//			);
//		}
//
//	}

	private static void setupGrassTuft(Face face, Vec v0, Vec v1, float xOff, float yExt, float zOff) {
		face.set(v0, v0, v1, v1);
		face.v1.y += yExt;
		face.v2.y += yExt;
		face.add(xOff, 0, zOff);
	}

	private static void renderQuad(
		BufferBuilder buffer, PoseStack matrix,
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
		public EnumFacing approximateDirection;

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
		final MutableBlockPos pos = new MutableBlockPos();
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

		private final MutableBlockPos pos = new MutableBlockPos();
		IBlockState state;

		public BlockPos relativePos() {
			return pos;
		}

		public static RenderableState findAt(MutableObjects objects, Area area, Vec faceNormal, Vec faceCentre, Predicate<IBlockState> isSmoothable) {
			RenderableState foundState = objects.foundState;
			MutableBlockPos faceBlockPos = posForFace(objects.vec, faceNormal, faceCentre).assignTo(foundState.pos);
			IBlockState state = area.getBlockState(faceBlockPos);

			// Has always been true in testing, so I changed this from a call to tryFindNearbyPosAndState on failure to an assertion
			// This HAS failed due to a race condition with the mesh being generated and then this getting called after
			// the state has been toggled to being un-smoothable with the keybind (so the state WAS smoothable).
			// assert isSmoothable.test(state)

			foundState.state = state;
			return foundState;
		}

		public static RenderableState findRenderFor(MutableObjects objects, RenderableState foundState, Area area, EnumFacing faceDirection) {
			boolean tryFindSnow = faceDirection == EnumFacing.UP || NoCubesConfig.Client.moreSnow;
			if (!tryFindSnow)
				return foundState;
			return findNearbyOrDefault(foundState, objects.renderState, area, MeshRenderer::isSnow);
		}

		private static Vec posForFace(Vec pos, Vec faceNormal, Vec faceCentre) {
			return pos.set(faceNormal).normalise().multiply(-0.5F).add(faceCentre);
		}

		private static RenderableState findNearbyOrDefault(RenderableState original, RenderableState toUse, Area area, Predicate<IBlockState> isSmoothable) {
			if (isSmoothable.test(original.state))
				return original;

			MutableBlockPos relativePos = toUse.pos;
			for (int i = 0, offsets_orderedLength = OFFSETS_ORDERED.length; i < offsets_orderedLength; i++) {
				BlockPos offset = OFFSETS_ORDERED[i];
				relativePos.setPos(original.pos).add(offset);
				IBlockState state = area.getBlockState(relativePos);
				if (isSmoothable.test(state)) {
					toUse.state = state;
					return toUse;
				}
			}
			return original;
		}

	}

	private static boolean isSnow(IBlockState state) {
		Block block = state.getBlock();
		return block == Blocks.SNOW_LAYER || block == Blocks.SNOW;
	}

	private static double max(double d0, final double d1, final double d2, final double d3) {
		if (d0 < d1) d0 = d1;
		if (d0 < d2) d0 = d2;
		return d0 < d3 ? d3 : d0;
	}

	private static double min(double d0, final double d1, final double d2, final double d3) {
		if (d0 > d1) d0 = d1;
		if (d0 > d2) d0 = d2;
		return d0 > d3 ? d3 : d0;
	}

	private static EnumFacing toSide(final double x, final double y, final double z) {
		if (Math.abs(x) > Math.abs(y)) {
			if (Math.abs(x) > Math.abs(z)) {
				if (x < 0) return WEST;
				return EAST;
			} else {
				if (z < 0) return NORTH;
				return SOUTH;
			}
		} else {
			if (Math.abs(y) > Math.abs(z)) {
				if (y < 0) return DOWN;
				return UP;
			} else {
				if (z < 0) return NORTH;
				return SOUTH;
			}
		}
	}

	public static float diffuseLight(final EnumFacing side) {
		if (side == UP) {
			return 1f;
		} else {
			return .97f;
		}
	}

}
