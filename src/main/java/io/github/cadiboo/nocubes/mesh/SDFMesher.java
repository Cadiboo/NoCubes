package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.mesh.TestData.TestMesh;
import io.github.cadiboo.nocubes.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * SDF stands for Signed Distance Field.
 */
abstract class SDFMesher implements Mesher {

	// These two really belong to MarchingCubes and SurfaceNets but are here for convenience
	public static final short MASK_FULLY_OUTSIDE_ISOSURFACE = 0b0000_0000;
	public static final short MASK_FULLY_INSIDE_ISOSURFACE = 0b1111_1111;
	@PerformanceCriticalAllocation
	private static final ThreadLocalArrayCache<float[]> FIELD = new ThreadLocalArrayCache<>(float[]::new, array -> array.length);
	@PerformanceCriticalAllocation
	public static final ThreadLocal<float[]> NEIGHBOURS_FIELD = ThreadLocal.withInitial(() -> new float[8]);
	@PerformanceCriticalAllocation
	public static final ThreadLocalArrayCache<Vec[]> VERTICES = new ThreadLocalArrayCache<>(length -> {
		var array = new Vec[length];
		for (var i = 0; i < length; ++i)
			array[i] = new Vec();
		return array;
	}, array -> array.length);

	protected final boolean smoothness2x;

	protected SDFMesher(boolean smoothness2x) {
		this.smoothness2x = smoothness2x;
	}

	protected static BlockPos getDimensions(Area area, boolean smoother, @Nullable TestMesh testMesh) {
		return testMesh == null ? getDimensions(area, smoother) : testMesh.dimensions;
	}

	protected static float[] generateDistanceField(Area area, Predicate<BlockState> isSmoothable, boolean smoother, @Nullable TestMesh testMesh) {
		return testMesh == null ? generateDistanceField(area, isSmoothable, smoother) : testMesh.generateDistanceField(area.start.getX(), area.start.getY(), area.start.getZ());
	}

	private static BlockPos getDimensions(Area area, boolean smoother) {
		return smoother ? area.size.subtract(ModUtil.VEC_ONE) : area.size;
	}

	private static float[] generateDistanceField(Area area, Predicate<BlockState> isSmoothable, boolean smoother) {
		// The area, converted from a BlockState[] to an isSmoothable[]
		// densityField[x, y, z] = isSmoothable(chunk[x, y, z]);
		// NB: SurfaceNets expects to be working on the signed distance at the corner of each block
		// To get this we would have to average the densities of each block & its neighbours
		// Doing this results in loss of terrain features (one-block large features effectively disappear)
		// Because we want to preserve these features, we feed SurfaceNets the inverted block densities, pretending that they
		// are the corner distances and then offset the resulting mesh by 0.5
		return smoother ? generateAveragedDistanceField(area, isSmoothable) : generateDistanceField(area, isSmoothable);
	}

	static float[] generateDistanceField(Area area, Predicate<BlockState> isSmoothable) {
		var states = area.getAndCacheBlocks();
		var length = area.numBlocks();
		var densityField = FIELD.takeArray(length);
		for (var i = 0; i < length; ++i)
			densityField[i] = densityToSignedDistance(ModUtil.getBlockDensity(isSmoothable, states[i]));
		return densityField;
	}

	private static float[] generateAveragedDistanceField(Area area, Predicate<BlockState> isSmoothable) {
		var states = area.getAndCacheBlocks();
		var areaX = area.size.getX();
		var areaY = area.size.getY();
		var areaZ = area.size.getZ();

		var distanceFieldSizeX = areaX - 1;
		var distanceFieldSizeY = areaY - 1;
		var distanceFieldSizeZ = areaZ - 1;
		var distanceFieldSize = distanceFieldSizeX * distanceFieldSizeY * distanceFieldSizeZ;
		var distanceField = FIELD.takeArray(distanceFieldSize);

		var index = 0;
		for (var z = 0; z < areaZ; ++z) {
			for (var y = 0; y < areaY; ++y) {
				for (var x = 0; x < areaX; ++x, ++index) {
					if (z == distanceFieldSizeZ || y == distanceFieldSizeY || x == distanceFieldSizeX)
						continue;
					var combinedDensity = 0f;
					var neighbourIndex = index;
					for (var neighbourZ = 0; neighbourZ < 2; ++neighbourZ, neighbourIndex += areaX * (areaY - 2))
						for (var neighbourY = 0; neighbourY < 2; ++neighbourY, neighbourIndex += areaX - 2)
							for (var neighbourX = 0; neighbourX < 2; ++neighbourX, ++neighbourIndex)
								combinedDensity += ModUtil.getBlockDensity(isSmoothable, states[neighbourIndex]);
					var distanceFieldIndex = ModUtil.get3dIndexInto1dArray(x, y, z, distanceFieldSizeX, distanceFieldSizeY);
					distanceField[distanceFieldIndex] = densityToSignedDistance(combinedDensity / 8F);
				}
			}
		}
		return distanceField;
	}

	private static float densityToSignedDistance(float density) {
		// Densities are POSITIVE if the block is smoothable, NEGATIVE if not
		// Distance fields are NEGATIVE inside, POSITIVE outside
		// We can get away with just inverting the value, so we don't actually calculate a distance to the isosurface
		return -density;
	}

	interface FullCellAction {
		FullCellAction IGNORE = (x, y, z) -> true;

		boolean apply(double x, double y, double z);
	}

	@PerformanceCriticalAllocation
	public static class CollisionObjects {
		public static final ThreadLocal<CollisionObjects> INSTANCE = ThreadLocal.withInitial(CollisionObjects::new);
		public final Face vertexNormals = new Face();
		public final Vec centre = new Vec();
		public final Vec faceNormal = new Vec();
	}

}
