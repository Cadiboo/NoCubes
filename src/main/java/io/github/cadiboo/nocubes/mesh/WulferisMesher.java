package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class WulferisMesher extends CullingCubic {

	@Override
	public void generateCollisionsInternal(Area area, Predicate<BlockState> isSmoothable, ShapeConsumer action) {
		// TODO: Generate collisions properly based on the voxel values, not the faces
		generateGeometryInternal(area, isSmoothable, (pos, face) -> {
			var objects = SDFMesher.CollisionObjects.INSTANCE.get();
			var vertexNormals = objects.vertexNormals;
			var centre = objects.centre;
			var faceNormal = objects.faceNormal;
			face.assignAverageTo(centre);
			face.assignNormalTo(vertexNormals);
			vertexNormals.assignAverageTo(faceNormal);
			return CollisionHandler.generateShapes(centre, faceNormal, action, face);
		});
	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		var pos = POS_INSTANCE.get();
		SmoothChecker shouldSmooth = (x, y, z) -> {
			pos.set(x, y, z);
			return isSmoothable.test(area.getBlockState(pos));
		};
		var mut = SDFMesher.CollisionObjects.INSTANCE.get().centre;
		super.generateGeometryInternal(area, isSmoothable, (relativePos, face) -> {
			face.v0.add(getOffsetToSurface(shouldSmooth, face.v0, mut));
			face.v1.add(getOffsetToSurface(shouldSmooth, face.v1, mut));
			face.v2.add(getOffsetToSurface(shouldSmooth, face.v2, mut));
			face.v3.add(getOffsetToSurface(shouldSmooth, face.v3, mut));
			return action.apply(relativePos, face);
		});
	}

	@FunctionalInterface
	interface SmoothChecker {
		boolean test(int x, int y, int z);
	}

	float getDistance(SmoothChecker shouldSmooth, int x, int y, int z)
	{
		if (!shouldSmooth.test(x, y, z))
			return -1f;

//		// No Check - Fastest
//		return 1f;
		// Main Axis Check slower but better with 1 block pillars
		float result = 1.4142135f;
		if (!shouldSmooth.test(x + 1, y, z)) result = 1f;
		else if (!shouldSmooth.test(x, y + 1, z)) result = 1f;
		else if (!shouldSmooth.test(x, y, z + 1)) result = 1f;
		else if (!shouldSmooth.test(x - 1, y, z)) result = 1f;
		else if (!shouldSmooth.test(x, y - 1, z)) result = 1f;
		else if (!shouldSmooth.test(x, y, z - 1)) result = 1f;
		return result;
	}

	float sampleDensity(SmoothChecker shouldSmooth, Vec p)
	{
		var px = p.x - 0.5f;
		var py = p.y - 0.5f;
		var pz = p.z - 0.5f;
		// First Obtain the Voxel Coordinates from the World position
		int x0 = Mth.floor(px);
		int y0 = Mth.floor(py);
		int z0 = Mth.floor(pz);

		// Now we need to get the Relative Position from the Voxel Coordinates, this should be values from 0-1
		float dx = px - x0;
		float dy = py - y0;
		float dz = pz - z0;

		float c00 = lerp(getDistance(shouldSmooth, x0, y0, z0), getDistance(shouldSmooth, x0 + 1, y0, z0), dx);
		float c01 = lerp(getDistance(shouldSmooth, x0, y0, z0 + 1), getDistance(shouldSmooth, x0 + 1, y0, z0 + 1), dx);
		float c10 = lerp(getDistance(shouldSmooth, x0, y0 + 1, z0), getDistance(shouldSmooth, x0 + 1, y0 + 1, z0), dx);
		float c11 = lerp(getDistance(shouldSmooth, x0, y0 + 1, z0 + 1), getDistance(shouldSmooth, x0 + 1, y0 + 1, z0 + 1), dx);

		float c0 = lerp(c00, c10, dy);
		float c1 = lerp(c01, c11, dy);
		return lerp(c0, c1, dz);
	}

	private static float lerp(float start, float end, float time) {
		return Mth.lerp(time, start, end);
	}

	Vec getOffsetToSurface(SmoothChecker shouldSmooth, Vec p, Vec mut)
	{
        final float E = 0.5f;
		var x = sampleDensity(shouldSmooth, mut.set(p.x + E, p.y, p.z)) - sampleDensity(shouldSmooth, mut.set(p.x - E, p.y, p.z));
		var y = sampleDensity(shouldSmooth, mut.set(p.x, p.y + E, p.z)) - sampleDensity(shouldSmooth, mut.set(p.x, p.y - E, p.z));
		var z = sampleDensity(shouldSmooth, mut.set(p.x, p.y, p.z + E)) - sampleDensity(shouldSmooth, mut.set(p.x, p.y, p.z - E));
		var scale = sampleDensity(shouldSmooth, mut.set(p.x, p.y, p.z));
		return mut.set(x, y, z).normalise().multiply(-scale * 0.75f);
	}

}
