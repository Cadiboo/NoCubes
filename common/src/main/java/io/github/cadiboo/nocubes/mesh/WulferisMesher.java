package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public class WulferisMesher extends SimpleMesher {

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
		generate(
			area, isSmoothable,
			(x, y, z) -> ShapeConsumer.acceptFullCube(x, y, z, action),
			(pos, face) -> {
				var objects = SDFMesher.CollisionObjects.INSTANCE.get();
				var vertexNormals = objects.vertexNormals;
				var centre = objects.centre;
				var faceNormal = objects.faceNormal;
				face.assignAverageTo(centre);
				face.assignNormalTo(vertexNormals);
				vertexNormals.assignAverageTo(faceNormal);
				return CollisionHandler.generateShapes(centre, faceNormal, action, face);
			}
		);
	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		generate(area, isSmoothable, SDFMesher.FullCellAction.IGNORE, action);
	}

	// Copied and modified from CullingCubic
	void generate(Area area, Predicate<BlockState> isSmoothable, SDFMesher.FullCellAction fullCellAction, FaceAction action) {
		final float min = 0F;
		final float max = 1F - min;

		var offsetLookup = area.generateDirectionOffsetsLookup();
		var blocks = area.getAndCacheBlocks();
		final var directions = ModUtil.DIRECTIONS;

		var pos = POS_INSTANCE.get();
		var face = FACE_INSTANCE.get();
		iterateSmoothBlocksInsideMesh(area, isSmoothable, (x, y, z, index) -> {
			// Never render snow
			// If vanilla is rendering it then everything works great.
			// If we are meant to be rendering it, we should just render white on top of the block
			// TODO: This doesn't work for multi-layer snow
			if (blocks[index].getBlock() == Blocks.SNOW)
				return true;

			var mut = SDFMesher.CollisionObjects.INSTANCE.get().centre;
			var anyFaces = false;
			for (int directionOrdinal = 0; directionOrdinal < directions.length; directionOrdinal++) {
				if (isSmoothable.test(blocks[index + offsetLookup[directionOrdinal]]))
					continue;
				anyFaces = true;
				StupidCubic.dirFace(directions[directionOrdinal], face, x, y, z, min, max);

				// Apply the offset
				{
					getOffsetToSurfaceAndAddToVertex(face.v0, mut, area, isSmoothable);
					getOffsetToSurfaceAndAddToVertex(face.v1, mut, area, isSmoothable);
					getOffsetToSurfaceAndAddToVertex(face.v2, mut, area, isSmoothable);
					getOffsetToSurfaceAndAddToVertex(face.v3, mut, area, isSmoothable);
				}

				if (!action.apply(pos.set(x, y, z), face))
					return false;
			}
			if (!anyFaces && !fullCellAction.apply(x, y, z))
				return false;
			return true;
		});
	}

	boolean shouldSmooth(int x, int y, int z, Area area, Predicate<BlockState> isSmoothable) {
		var state = area.getAndCacheBlocks()[area.index(x, y, z)];
		return isSmoothable.test(state);
	}

	float getSignedDistanceInsideSmoothTerrain(int x, int y, int z, Area area, Predicate<BlockState> isSmoothable) {
		var state = area.getAndCacheBlocks()[area.index(x, y, z)];

		if (!isSmoothable.test(state)) {
			if (!state.canBeReplaced() && !NoCubes.smoothableHandler.isSmoothable(state) && !ModUtil.isShortPlant(state) || state.getBlock() instanceof VineBlock)
				return -1000f;
//		if (!shouldSmooth(x, y, z, area, isSmoothable))
			return ModUtil.NOT_SMOOTHABLE; // Outside the surface (air/water/door/non-smoothable)
		}

		// No Check - Fastest
		return ModUtil.FULLY_SMOOTHABLE;

//		// Main Axis Check slower but better with 1 block pillars
//		if (!shouldSmooth(x + 1, y, z, area, isSmoothable))
//			return 1f;
//		if (!shouldSmooth(x, y + 1, z, area, isSmoothable))
//			return 1f;
//		if (!shouldSmooth(x, y, z + 1, area, isSmoothable))
//			return 1f;
//		if (!shouldSmooth(x - 1, y, z, area, isSmoothable))
//			return 1f;
//		if (!shouldSmooth(x, y - 1, z, area, isSmoothable))
//			return 1f;
//		if (!shouldSmooth(x, y, z - 1, area, isSmoothable))
//			return 1f;
//
//		return 1.4142135f; // Math.sqrt(2)
	}

	float sampleDensity(Vec p, Area area, Predicate<BlockState> isSmoothable) {
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

		float d000 = getSignedDistanceInsideSmoothTerrain(x0, y0, z0, area, isSmoothable);
		float d100 = getSignedDistanceInsideSmoothTerrain(x0 + 1, y0, z0, area, isSmoothable);
		float d001 = getSignedDistanceInsideSmoothTerrain(x0, y0, z0 + 1, area, isSmoothable);
		float d101 = getSignedDistanceInsideSmoothTerrain(x0 + 1, y0, z0 + 1, area, isSmoothable);
		float d010 = getSignedDistanceInsideSmoothTerrain(x0, y0 + 1, z0, area, isSmoothable);
		float d110 = getSignedDistanceInsideSmoothTerrain(x0 + 1, y0 + 1, z0, area, isSmoothable);
		float d011 = getSignedDistanceInsideSmoothTerrain(x0, y0 + 1, z0 + 1, area, isSmoothable);
		float d111 = getSignedDistanceInsideSmoothTerrain(x0 + 1, y0 + 1, z0 + 1, area, isSmoothable);

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

	void getOffsetToSurfaceAndAddToVertex(Vec p, Vec mut, Area area, Predicate<BlockState> isSmoothable) {
		final float E = 0.5f;
		float dx0 = sampleDensity(mut.set(p.x + E, p.y, p.z), area, isSmoothable);
		float dx1 = sampleDensity(mut.set(p.x - E, p.y, p.z), area, isSmoothable);
		float dy0 = sampleDensity(mut.set(p.x, p.y + E, p.z), area, isSmoothable);
		float dy1 = sampleDensity(mut.set(p.x, p.y - E, p.z), area, isSmoothable);
		float dz0 = sampleDensity(mut.set(p.x, p.y, p.z + E), area, isSmoothable);
		float dz1 = sampleDensity(mut.set(p.x, p.y, p.z - E), area, isSmoothable);
		if (Math.abs(dx0) > 5 || Math.abs(dx1) > 5 || Math.abs(dy0) > 5 || Math.abs(dy1) > 5 || Math.abs(dz0) > 5 || Math.abs(dz1) > 5)
			return;
		float scale = sampleDensity(mut.set(p.x, p.y, p.z), area, isSmoothable);
		var x = dx0 - dx1;
		var y = dy0 - dy1;
		var z = dz0 - dz1;
		p.add(mut.set(x, y, z).normalise().multiply(-scale * 0.75f));
	}

}
