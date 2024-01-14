package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.collision.SmoothShapes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SDFMesher.CollisionObjects;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;


/**
 * @author Cadiboo
 * @author Click_Me
 */
public final class OldNoCubes extends SimpleMesher {

	// Points order
	public static final int X0Y0Z0 = 0;
	public static final int X1Y0Z0 = 1;
	public static final int X1Y0Z1 = 2;
	public static final int X0Y0Z1 = 3;
	public static final int X0Y1Z0 = 4;
	public static final int X1Y1Z0 = 5;
	public static final int X1Y1Z1 = 6;
	public static final int X0Y1Z1 = 7;

	private static void resetPoints(Vec[] points) {
		// The 8 points that make the block.
		// 1 point for each corner
		points[0].set(0, 0, 0);
		points[1].set(1, 0, 0);
		points[2].set(1, 0, 1);
		points[3].set(0, 0, 1);
		points[4].set(0, 1, 0);
		points[5].set(1, 1, 0);
		points[6].set(1, 1, 1);
		points[7].set(0, 1, 1);
	}

	@Override
	public BlockPos getPositiveAreaExtension() {
		// Need data about the area's direct neighbour blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public BlockPos getNegativeAreaExtension() {
		// Need data about the area's direct neighbour blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public void generateCollisionsInternal(Area area, Predicate<BlockState> isSmoothable, ShapeConsumer action) {
		generateInternal(
			area, isSmoothable,
			(x, y, z) -> ShapeConsumer.acceptFullCube(x, y, z, action),
			(pos, face) -> {
				var objects = CollisionObjects.INSTANCE.get();
				var vertexNormals = objects.vertexNormals;
				var centre = objects.centre;
				var faceNormal = objects.faceNormal;
				face.assignAverageTo(centre);
				face.assignNormalTo(vertexNormals);
				vertexNormals.assignAverageTo(faceNormal);

				// Keeps flat surfaces collidable but also allows super rough terrain
				faceNormal.multiply(0.00001F);

				return SmoothShapes.generateShapes(centre, faceNormal, action, face);
			}
		);
	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		generateInternal(area, isSmoothable, FullBlockAction.IGNORE, action);
	}

	private interface FullBlockAction {
		FullBlockAction IGNORE = (x, y, z) -> true;

		boolean apply(int x, int y, int z);
	}

	private void generateInternal(Area area, Predicate<BlockState> isSmoothable, FullBlockAction fullBlockAction, FaceAction faceAction) {
		var pos = POS_INSTANCE.get();
		var face = FACE_INSTANCE.get();
		// The 8 points that make the block.
		// 1 point for each corner
		var points = SDFMesher.VERTICES.takeArray(8);
		var directions = ModUtil.DIRECTIONS;
		var directionsLength = directions.length;
		var directionOffsets = area.generateDirectionOffsetsLookup();
		var neighboursSmoothness = SDFMesher.NEIGHBOURS_FIELD.get();
		var roughness = NoCubesConfig.Server.oldNoCubesRoughness;
		iterateSmoothBlocksInsideMesh(area, isSmoothable, (x, y, z, index) -> {
			var blocks = area.getAndCacheBlocks();
			var state = blocks[index];

			var combinedNeighboursSmoothness = 0F;
			for (var i = 0; i < directionsLength; ++i) {
				var neighbour = blocks[area.index(x, y, z) + directionOffsets[i]];
				var density = ModUtil.getBlockDensity(isSmoothable, neighbour);
				combinedNeighboursSmoothness += density;
				neighboursSmoothness[i] = density;
			}

			var amountInsideIsosurface = (combinedNeighboursSmoothness / directionsLength) / 2 + 0.5F;
			if (amountInsideIsosurface == 1 && !fullBlockAction.apply(x, y, z))
				return false;
			if (amountInsideIsosurface == 0 || ModUtil.isSnowLayer(state))
				return true;

			resetPoints(points);
			// Loop through all the points:
			// Here everything will be 'smoothed'.
			for (byte pointIndex = 0; pointIndex < 8; ++pointIndex) {
				var point = points[pointIndex];

				// Give the point the block's coordinates.
				point.x += x;
				point.y += y;
				point.z += z;

				if (!doesPointIntersectWithSolid(area, point, isSmoothable)) {
					if (NoCubesConfig.Server.oldNoCubesSlopes) {
						if (pointIndex < 4 && doesPointBottomIntersectWithNonSolid(area, point))
							point.y = y + 1.0F - 0.0001F; // - 0.0001F to prevent z-fighting
						else if (pointIndex >= 4 && doesPointTopIntersectWithNonSolid(area, point))
							point.y = y + 0.0F + 0.0001F; // + 0.0001F to prevent z-fighting
					}
					givePointRoughness(roughness, area, point);
				}
			}

			for (int i = 0; i < directionsLength; ++i) {
				if (neighboursSmoothness[i] == ModUtil.FULLY_SMOOTHABLE)
					continue;
				//0-3
				//1-2
				//0,0-1,0
				//0,1-1,1
				if (!faceAction.apply(pos.set(x, y, z), switch (directions[i]) {
					case DOWN -> face.set(
						points[X1Y0Z1],
						points[X0Y0Z1],
						points[X0Y0Z0],
						points[X1Y0Z0]);
					case UP -> face.set(
						points[X1Y1Z1],
						points[X1Y1Z0],
						points[X0Y1Z0],
						points[X0Y1Z1]);
					case NORTH -> face.set(
						points[X1Y1Z0],
						points[X1Y0Z0],
						points[X0Y0Z0],
						points[X0Y1Z0]);
					case SOUTH -> face.set(
						points[X1Y1Z1],
						points[X0Y1Z1],
						points[X0Y0Z1],
						points[X1Y0Z1]);
					case WEST -> face.set(
						points[X0Y1Z1],
						points[X0Y1Z0],
						points[X0Y0Z0],
						points[X0Y0Z1]);
					case EAST -> face.set(
						points[X1Y1Z1],
						points[X1Y0Z1],
						points[X1Y0Z0],
						points[X1Y1Z0]);
				}))
					return false;
			}
			return true;
		});
	}

	private static float max(float a, float b, float c, float d, float e, float f, float g, float h) {
		float max = a;
		if (b > max)
			max = b;
		if (c > max)
			max = c;
		if (d > max)
			max = d;
		if (e > max)
			max = e;
		if (f > max)
			max = f;
		if (g > max)
			max = g;
		if (h > max)
			max = h;
		return max;
	}

	private static float min(float a, float b, float c, float d, float e, float f, float g, float h) {
		float min = a;
		if (b < min)
			min = b;
		if (c < min)
			min = c;
		if (d < min)
			min = d;
		if (e < min)
			min = e;
		if (f < min)
			min = f;
		if (g < min)
			min = g;
		if (h < min)
			min = h;
		return min;
	}

	public static void givePointRoughness(float roughness, Area area, Vec point) {
		double worldX = area.start.getX() + point.x;
		double worldY = area.start.getY() + point.y;
		double worldZ = area.start.getZ() + point.z;
		long i = (long) (worldX * 3129871d) ^ (long) worldY * 116129781L ^ (long) worldZ;

		i = i * i * 42317861L + i * 11L;
		point.x += ((float) (i >> 16 & 0xF) / 15.0F - 0.5F) * roughness;
		point.y += ((float) (i >> 20 & 0xF) / 15.0F - 0.5F) * roughness;
		point.z += ((float) (i >> 24 & 0xF) / 15.0F - 0.5F) * roughness;
	}

	/**
	 * Checks if the state should be connected to with a slope (rather than a full side) when it is next to a smooth block if {@link NoCubesConfig.Server#oldNoCubesSlopes} is enabled.
	 * Also used to decide if parts of a neighbouring smoothable block can extend into it if {@link NoCubesConfig.Server#oldNoCubesRoughness} is enabled.
	 */
	public static boolean isNonSolid(BlockState state) {
		return state.isAir() || (NoCubesConfig.Server.oldNoCubesInFluids && state.getBlock() instanceof LiquidBlock && state.getFluidState().isSource()) || ModUtil.isPlant(state) || ModUtil.isSnowLayer(state);
	}

	public static boolean doesPointTopIntersectWithNonSolid(Area area, Vec point) {
		boolean intersects = false;
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			if (!isNonSolid(area.getAndCacheBlocks()[area.index(x, y, z)]))
				return false;
			if (isNonSolid(area.getAndCacheBlocks()[area.index(x, y - 1, z)]))
				intersects = true;
		}
		return intersects;
	}

	public static boolean doesPointBottomIntersectWithNonSolid(Area area, Vec point) {
		boolean intersects = false;
		boolean notOnly = false;
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			if (!isNonSolid(area.getAndCacheBlocks()[area.index(x, y - 1, z)]))
				return false;
			if (!isNonSolid(area.getAndCacheBlocks()[area.index(x, y + 1, z)]))
				notOnly = true;
			if (isNonSolid(area.getAndCacheBlocks()[area.index(x, y, z)]))
				intersects = true;
		}
		return intersects && notOnly;
	}

	public static boolean doesPointIntersectWithSolid(Area area, Vec point, Predicate<BlockState> isSmoothable) {
		for (int i = 0; i < 4; i++) {
			int x = (int) (point.x - (i & 0x1));
			int y = (int) point.y;
			int z = (int) (point.z - (i >> 1 & 0x1));
			var state0 = area.getAndCacheBlocks()[area.index(x, y, z)];
			if (!isNonSolid(state0) && !isSmoothable.test(state0))
				return true;
			var state1 = area.getAndCacheBlocks()[area.index(x, y - 1, z)];
			if (!isNonSolid(state1) && !isSmoothable.test(state1))
				return true;
		}
		return false;
	}

}
