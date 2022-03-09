package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.mesh.TestData.TestMesh;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.ThreadLocalArrayCache;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * SDF stands for Signed Distance Field.
 */
abstract class SDFMesher implements Mesher {

	// These two really belong to MarchingCubes and SurfaceNets but are here for convenience
	public static final short MASK_FULLY_OUTSIDE_ISOSURFACE = 0b0000_0000;
	public static final short MASK_FULLY_INSIDE_ISOSURFACE = 0b1111_1111;
	private static final ThreadLocalArrayCache<float[]> CACHE = new ThreadLocalArrayCache<>(float[]::new, array -> array.length);

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
		return smoother ? generateDistanceField(area, isSmoothable) : generateNegativeDensityField(area, isSmoothable);
	}

	private static float[] generateDistanceField(Area area, Predicate<BlockState> isSmoothable) {
		BlockState[] states = area.getAndCacheBlocks();
		int areaX = area.size.getX();
		int areaY = area.size.getY();
		int areaZ = area.size.getZ();

		int distanceFieldSizeX = areaX - 1;
		int distanceFieldSizeY = areaY - 1;
		int distanceFieldSizeZ = areaZ - 1;
		int distanceFieldSize = distanceFieldSizeX * distanceFieldSizeY * distanceFieldSizeZ;
		float[] distanceField = CACHE.takeArray(distanceFieldSize);

		int index = 0;
		for (int z = 0; z < areaZ; ++z) {
			for (int y = 0; y < areaY; ++y) {
				for (int x = 0; x < areaX; ++x, ++index) {
					if (z == distanceFieldSizeZ || y == distanceFieldSizeY || x == distanceFieldSizeX)
						continue;
					float combinedDensity = 0;
					int neighbourIndex = index;
					for (int neighbourZ = 0; neighbourZ < 2; ++neighbourZ, neighbourIndex += areaX * (areaY - 2))
						for (int neighbourY = 0; neighbourY < 2; ++neighbourY, neighbourIndex += areaX - 2)
							for (int neighbourX = 0; neighbourX < 2; ++neighbourX, ++neighbourIndex)
								combinedDensity += ModUtil.getBlockDensity(isSmoothable, states[neighbourIndex]);
					int distanceFieldIndex = ModUtil.get3dIndexInto1dArray(x, y, z, distanceFieldSizeX, distanceFieldSizeY);
					distanceField[distanceFieldIndex] = -combinedDensity / 8F;
				}
			}
		}
		return distanceField;
	}

	static float[] generateNegativeDensityField(Area area, Predicate<BlockState> isSmoothable) {
		BlockState[] states = area.getAndCacheBlocks();
		int length = area.numBlocks();
		float[] densityField = CACHE.takeArray(length);
		for (int i = 0; i < length; ++i)
			densityField[i] = -ModUtil.getBlockDensity(isSmoothable, states[i]);
		return densityField;
	}

	interface FullCellAction {
		FullCellAction IGNORE = (x, y, z) -> true;

		boolean apply(double x, double y, double z);
	}

}
