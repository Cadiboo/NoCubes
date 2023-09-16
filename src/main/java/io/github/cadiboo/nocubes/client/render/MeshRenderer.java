package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.UVHelper;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.FaceLight;
import io.github.cadiboo.nocubes.client.render.struct.PoseStack;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

/**
 * @author Cadiboo
 */
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class MeshRenderer {

	private static final Logger LOGGER = LogManager.getLogger("NoCubes MeshRenderer");

	public static boolean isSolidRender(IBlockState state) {
		return !state.isTranslucent() || state.getBlock() instanceof BlockGrassPath;
	}

	public static void runForSolidAndSeeThrough(Predicate<IBlockState> isSmoothable, Consumer<Predicate<IBlockState>> action) {
		action.accept(state -> isSmoothable.test(state) && isSolidRender(state));
		action.accept(state -> isSmoothable.test(state) && !isSolidRender(state));
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
