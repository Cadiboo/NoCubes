package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class WulferisMesher extends CullingCubic {

	@Override
	public Vec3i getPositiveAreaExtension() {
		// Need +1 to check neighbours of max block for culling (see CullingCubic)
		// Need +1+1 on top of that to check neighbours of the neighbours for density
		return ModUtil.VEC_THREE;
	}

	@Override
	public Vec3i getNegativeAreaExtension() {
		// Need -1 to check neighbours of min block for culling (see CullingCubic)
		// Need -0.5-0.5 on top of that to check neighbours of the neighbours for density
		return ModUtil.VEC_TWO;
	}

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
		var mut = SDFMesher.CollisionObjects.INSTANCE.get().centre;
		super.generateGeometryInternal(area, isSmoothable, (relativePos, face) -> {
			var x = relativePos.getX();
			var y = relativePos.getY();
			var z = relativePos.getZ();
			getOffsetToSurfaceToVertex(face.v0, mut, relativePos, area, isSmoothable);
			getOffsetToSurfaceToVertex(face.v1, mut, relativePos, area, isSmoothable);
			getOffsetToSurfaceToVertex(face.v2, mut, relativePos, area, isSmoothable);
			getOffsetToSurfaceToVertex(face.v3, mut, relativePos, area, isSmoothable);
			relativePos.set(x, y, z);
			return action.apply(relativePos, face);
		});
	}

	boolean shouldSmooth(int x, int y, int z, MutableBlockPos pos, Area area, Predicate<BlockState> isSmoothable) {
		return isSmoothable.test(area.getBlockState(pos.set(x, y, z)));
	}

	float getDistance(int x, int y, int z, MutableBlockPos pos, Area area, Predicate<BlockState> isSmoothable)
	{
		final float scalar = 1f;
		final float v = 1f * scalar;
		final float defaultV = 1.4142135f * scalar;

		if (!shouldSmooth(x, y, z, pos, area, isSmoothable))
			return -v;

//		// No Check - Fastest
//		return 1f;
		// Main Axis Check slower but better with 1 block pillars
		float result = defaultV;
		if (!shouldSmooth(x + 1, y, z, pos, area, isSmoothable)) result = v;
		else if (!shouldSmooth(x, y + 1, z, pos, area, isSmoothable)) result = v;
		else if (!shouldSmooth(x, y, z + 1, pos, area, isSmoothable)) result = v;
		else if (!shouldSmooth(x - 1, y, z, pos, area, isSmoothable)) result = v;
		else if (!shouldSmooth(x, y - 1, z, pos, area, isSmoothable)) result = v;
		else if (!shouldSmooth(x, y, z - 1, pos, area, isSmoothable)) result = v;
		return result;
	}

	float sampleDensity(Vec p, MutableBlockPos pos, Area area, Predicate<BlockState> isSmoothable)
	{
		// Mesher assumes voxels are centred around a position
		// This is not the case - mc blocks have their smallest corner in their block pos
		// To fix this we translate
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

		float d000 = getDistance(x0, y0, z0, pos, area, isSmoothable);
		float d100 = getDistance(x0 + 1, y0, z0, pos, area, isSmoothable);
		float d001 = getDistance(x0, y0, z0 + 1, pos, area, isSmoothable);
		float d101 = getDistance(x0 + 1, y0, z0 + 1, pos, area, isSmoothable);
		float d010 = getDistance(x0, y0 + 1, z0, pos, area, isSmoothable);
		float d110 = getDistance(x0 + 1, y0 + 1, z0, pos, area, isSmoothable);
		float d011 = getDistance(x0, y0 + 1, z0 + 1, pos, area, isSmoothable);
		float d111 = getDistance(x0 + 1, y0 + 1, z0 + 1, pos, area, isSmoothable);

		float c00 = lerp(d000, d100, dx);
		float c01 = lerp(d001, d101, dx);
		float c10 = lerp(d010, d110, dx);
		float c11 = lerp(d011, d111, dx);

		float c0 = lerp(c00, c10, dy);
		float c1 = lerp(c01, c11, dy);
		return lerp(c0, c1, dz);
	}

	private static float lerp(float start, float end, float time) {
		return Mth.lerp(time, start, end);
	}

	void getOffsetToSurfaceToVertex(Vec p, Vec mut, MutableBlockPos pos, Area area, Predicate<BlockState> isSmoothable)
	{
        final float E = 0.5f;
		var dx0 = sampleDensity(mut.set(p.x + E, p.y, p.z), pos, area, isSmoothable);
		float dx1 = sampleDensity(mut.set(p.x - E, p.y, p.z), pos, area, isSmoothable);
		float dy0 = sampleDensity(mut.set(p.x, p.y + E, p.z), pos, area, isSmoothable);
		float dy1 = sampleDensity(mut.set(p.x, p.y - E, p.z), pos, area, isSmoothable);
		float dz0 = sampleDensity(mut.set(p.x, p.y, p.z + E), pos, area, isSmoothable);
		float dz1 = sampleDensity(mut.set(p.x, p.y, p.z - E), pos, area, isSmoothable);
		var scale = sampleDensity(mut.set(p.x, p.y, p.z), pos, area, isSmoothable);
		var x = dx0 - dx1;
		var y = dy0 - dy1;
		var z = dz0 - dz1;
		p.add(mut.set(x, y, z).normalise().multiply(-scale * 0.75f));
	}

}
