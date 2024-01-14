package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.collision.ShapeConsumer;
import io.github.cadiboo.nocubes.collision.SmoothShapes;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.PerformanceCriticalAllocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public class MarchingCubes extends SDFMesher {

	@PerformanceCriticalAllocation
	public static final ThreadLocal<int[]> EDGES_FIELD = ThreadLocal.withInitial(() -> new int[12]);

	public MarchingCubes(boolean smoothness2x) {
		super(smoothness2x);
	}

	@Override
	public BlockPos getPositiveAreaExtension() {
		// Need data about the area's direct neighbour blocks to check if they should be culled
		return smoothness2x ? ModUtil.VEC_TWO : ModUtil.VEC_ONE;
	}

	@Override
	public BlockPos getNegativeAreaExtension() {
		// Need data about the area's direct neighbour blocks to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public void generateCollisionsInternal(Area area, Predicate<BlockState> isSmoothable, ShapeConsumer action) {
		// Duplicated in SurfaceNets
		// Not in shared base class SDFMesher because I intend to implement custom logic for each mesher that takes advantage of the underlying algorithm
		generateOrThrow(
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
				return SmoothShapes.generateShapes(centre, faceNormal, action, face);
			}
		);
	}

//	@Override
//	public void generateCollisionsInternal(Area area, Predicate<BlockState> isSmoothable, ShapeConsumer action) {
//		var smoother = smoothness2x;
//		var data = generateDistanceField(area, isSmoothable, smoother);
//		var dims = smoother ? area.size.subtract(ModUtil.VEC_ONE) : area.size;
//
//		var cubeVerts = Lookup.CUBE_VERTS;
//
//		int n = 0;
//		var grid = new float[8];
//
//		//March over the volume
//		for (int z = 0; z < dims.getZ() - 1; ++z, n += dims.getX()) {
//			for (int y = 0; y < dims.getY() - 1; ++y, ++n) {
//				for (int x = 0; x < dims.getX() - 1; ++x, ++n) {
//					//For each cell, compute cube mask
//					short cube_index = 0;
//					for (byte i = 0; i < 8; ++i) {
//						byte[] v = cubeVerts[i];
//						float s = data[n + v[0] + dims.getX() * (v[1] + dims.getY() * v[2])];
//						grid[i] = s;
//						cube_index |= (s > 0) ? 1 << i : 0;
//					}
//
//					// Generate collision based on edge intersection (somehow)
//					// Easier to just generate cubes based on the value of the edge mask
//
//					if (cube_index == MASK_FULLY_OUTSIDE_ISOSURFACE)
//						continue;
//
////					if (cube_index == MASK_FULLY_INSIDE_ISOSURFACE) {
////						if (!ShapeConsumer.acceptFullCube(x, y, z, action))
////							return;
////						continue;
////					}
//
//					// For each corner that is inside the mesh,
//					// call the action with a shape stretching from the centre of the area to the corner
//					for (byte i = 0; i < 8; ++i) {
//						if ((cube_index & (1 << i)) == 0 || i != 0)
//							continue;
//						var v = cubeVerts[i];
//						// TODO: Use grid values to find edge intercept
//						var cornerX = x + v[0];
//						var cornerY = y + v[1];
//						var cornerZ = z + v[2];
//						var centreX = x + 0.5;
//						var centreY = y + 0.5;
//						var centreZ = z + 0.5;
//						if (!action.accept(
//							Math.min(centreX, cornerX), Math.min(centreY, cornerY), Math.min(centreZ, cornerZ),
//							Math.max(centreX, cornerX), Math.max(centreY, cornerY), Math.max(centreZ, cornerZ)
//						))
//							return;
//					}
//				}
//			}
//		}
//	}

	@Override
	public void generateGeometryInternal(Area area, Predicate<BlockState> isSmoothable, FaceAction action) {
		generateOrThrow(area, isSmoothable, FullCellAction.IGNORE, action);
	}

	private void generateOrThrow(Area area, Predicate<BlockState> isSmoothable, FullCellAction fullCellAction, FaceAction action) {
		// Duplicated in SurfaceNets
		@Nullable TestData.TestMesh testMesh = null; // TestData.SPHERE
		var smoother = smoothness2x;
		var distanceField = generateDistanceField(area, isSmoothable, smoother, testMesh);
		var dims = getDimensions(area, smoother, testMesh);
		// Because we are passing block densities instead of corner distances (see the NB comment in generateDistanceField) we need to offset the mesh
		float offset = smoother ? 1F : 0.5F;
		generateOrThrow2(
			distanceField, dims,
			(x, y, z) -> fullCellAction.apply(x + offset, y + offset, z + offset),
			(pos, face) -> action.apply(pos, face.add(offset))
		);
	}

	private static void generateOrThrow2(float[] data, BlockPos dims, FullCellAction fullCellAction, FaceAction action) {
		var pos = POS_INSTANCE.get();
		var face = FACE_INSTANCE.get();

		var cubeVerts = Lookup.CUBE_VERTS;
		var edgeTable = Lookup.EDGE_TABLE;
		var edgeIndex = Lookup.EDGE_INDEX;
		var triTable = Lookup.TRI_TABLE;

		int n = 0;
		var grid = NEIGHBOURS_FIELD.get();
		var edges = EDGES_FIELD.get();
		var vertexCount = 0;
		// Allocate enough memory for the worst-case scenario:
		// When we have a checkerboard of voxels, every second voxel generates 6 faces (4 vertices each)
		// This is over-allocating, it would be great to use a sliding buffer the way SurfaceNets works
		var vertices = VERTICES.takeArray(4 * 6 * dims.getX() * dims.getY() * dims.getZ() / 2);

		//March over the volume
		for (int z = 0; z < dims.getZ() - 1; ++z, n += dims.getX()) {
			for (int y = 0; y < dims.getY() - 1; ++y, ++n) {
				for (int x = 0; x < dims.getX() - 1; ++x, ++n) {
					//For each cell, compute cube mask
					short cube_index = 0;
					for (byte i = 0; i < 8; ++i) {
						var v = cubeVerts[i];
						float s = data[n + v[0] + dims.getX() * (v[1] + dims.getY() * v[2])];
						grid[i] = s;
						cube_index |= (s > 0) ? 1 << i : 0;
					}

					if (cube_index == MASK_FULLY_INSIDE_ISOSURFACE && !fullCellAction.apply(x, y, z))
						return;

					//Compute vertices
					short edge_mask = edgeTable[cube_index];
					if (edge_mask == 0)
						continue;

					for (byte i = 0; i < 12; ++i) {
						if ((edge_mask & (1 << i)) == 0)
							continue;
						edges[i] = vertexCount;

						var e = edgeIndex[i];
						var p0 = cubeVerts[e[0]];
						var p1 = cubeVerts[e[1]];
						var a = grid[e[0]];
						var b = grid[e[1]];
						var d = a - b;
						float t = 0;
						if (Math.abs(d) > 1e-6)
							t = a / d;
						vertices[vertexCount++].set(
							(x + p0[0]) + t * (p1[0] - p0[0]),
							(y + p0[1]) + t * (p1[1] - p0[1]),
							(z + p0[2]) + t * (p1[2] - p0[2])
						);
					}

					//Add faces
					var f = triTable[cube_index];
					for (byte i = 0; i < f.length; i += 3) {
						face.v0.set(vertices[edges[f[i + 0]]]);
						face.v1.set(vertices[edges[f[i + 1]]]);
						face.v2.set(vertices[edges[f[i + 2]]]);
						face.v3.set(face.v2);
						if (!action.apply(pos.set(x, y, z), face))
							return;
					}
				}
			}
		}
	}


	private interface Lookup {

		byte[][] EDGE_INDEX = {
			{0, 1},
			{1, 2},
			{2, 3},
			{3, 0},
			{4, 5},
			{5, 6},
			{6, 7},
			{7, 4},
			{0, 4},
			{1, 5},
			{2, 6},
			{3, 7}
		};

		byte[][] CUBE_VERTS = {
			{0, 0, 0},
			{1, 0, 0},
			{1, 1, 0},
			{0, 1, 0},
			{0, 0, 1},
			{1, 0, 1},
			{1, 1, 1},
			{0, 1, 1}
		};

		/**
		 * Maps the vertices under the isosurface to the intersecting edges.
		 * An 8 bit index is formed where each bit corresponds to a vertex.
		 * Contains the same data as TRI_TABLE, just unordered
		 */
		short[] EDGE_TABLE = {
			0b000000000000, 0b000100001001, 0b001000000011, 0b001100001010, 0b010000000110, 0b010100001111, 0b011000000101, 0b011100001100,
			0b100000001100, 0b100100000101, 0b101000001111, 0b101100000110, 0b110000001010, 0b110100000011, 0b111000001001, 0b111100000000,
			0b000110010000, 0b000010011001, 0b001110010011, 0b001010011010, 0b010110010110, 0b010010011111, 0b011110010101, 0b011010011100,
			0b100110011100, 0b100010010101, 0b101110011111, 0b101010010110, 0b110110011010, 0b110010010011, 0b111110011001, 0b111010010000,
			0b001000110000, 0b001100111001, 0b000000110011, 0b000100111010, 0b011000110110, 0b011100111111, 0b010000110101, 0b010100111100,
			0b101000111100, 0b101100110101, 0b100000111111, 0b100100110110, 0b111000111010, 0b111100110011, 0b110000111001, 0b110100110000,
			0b001110100000, 0b001010101001, 0b000110100011, 0b000010101010, 0b011110100110, 0b011010101111, 0b010110100101, 0b010010101100,
			0b101110101100, 0b101010100101, 0b100110101111, 0b100010100110, 0b111110101010, 0b111010100011, 0b110110101001, 0b110010100000,
			0b010001100000, 0b010101101001, 0b011001100011, 0b011101101010, 0b000001100110, 0b000101101111, 0b001001100101, 0b001101101100,
			0b110001101100, 0b110101100101, 0b111001101111, 0b111101100110, 0b100001101010, 0b100101100011, 0b101001101001, 0b101101100000,
			0b010111110000, 0b010011111001, 0b011111110011, 0b011011111010, 0b000111110110, 0b000011111111, 0b001111110101, 0b001011111100,
			0b110111111100, 0b110011110101, 0b111111111111, 0b111011110110, 0b100111111010, 0b100011110011, 0b101111111001, 0b101011110000,
			0b011001010000, 0b011101011001, 0b010001010011, 0b010101011010, 0b001001010110, 0b001101011111, 0b000001010101, 0b000101011100,
			0b111001011100, 0b111101010101, 0b110001011111, 0b110101010110, 0b101001011010, 0b101101010011, 0b100001011001, 0b100101010000,
			0b011111000000, 0b011011001001, 0b010111000011, 0b010011001010, 0b001111000110, 0b001011001111, 0b000111000101, 0b000011001100,
			0b111111001100, 0b111011000101, 0b110111001111, 0b110011000110, 0b101111001010, 0b101011000011, 0b100111001001, 0b100011000000,
			0b100011000000, 0b100111001001, 0b101011000011, 0b101111001010, 0b110011000110, 0b110111001111, 0b111011000101, 0b111111001100,
			0b000011001100, 0b000111000101, 0b001011001111, 0b001111000110, 0b010011001010, 0b010111000011, 0b011011001001, 0b011111000000,
			0b100101010000, 0b100001011001, 0b101101010011, 0b101001011010, 0b110101010110, 0b110001011111, 0b111101010101, 0b111001011100,
			0b000101011100, 0b000001010101, 0b001101011111, 0b001001010110, 0b010101011010, 0b010001010011, 0b011101011001, 0b011001010000,
			0b101011110000, 0b101111111001, 0b100011110011, 0b100111111010, 0b111011110110, 0b111111111111, 0b110011110101, 0b110111111100,
			0b001011111100, 0b001111110101, 0b000011111111, 0b000111110110, 0b011011111010, 0b011111110011, 0b010011111001, 0b010111110000,
			0b101101100000, 0b101001101001, 0b100101100011, 0b100001101010, 0b111101100110, 0b111001101111, 0b110101100101, 0b110001101100,
			0b001101101100, 0b001001100101, 0b000101101111, 0b000001100110, 0b011101101010, 0b011001100011, 0b010101101001, 0b010001100000,
			0b110010100000, 0b110110101001, 0b111010100011, 0b111110101010, 0b100010100110, 0b100110101111, 0b101010100101, 0b101110101100,
			0b010010101100, 0b010110100101, 0b011010101111, 0b011110100110, 0b000010101010, 0b000110100011, 0b001010101001, 0b001110100000,
			0b110100110000, 0b110000111001, 0b111100110011, 0b111000111010, 0b100100110110, 0b100000111111, 0b101100110101, 0b101000111100,
			0b010100111100, 0b010000110101, 0b011100111111, 0b011000110110, 0b000100111010, 0b000000110011, 0b001100111001, 0b001000110000,
			0b111010010000, 0b111110011001, 0b110010010011, 0b110110011010, 0b101010010110, 0b101110011111, 0b100010010101, 0b100110011100,
			0b011010011100, 0b011110010101, 0b010010011111, 0b010110010110, 0b001010011010, 0b001110010011, 0b000010011001, 0b000110010000,
			0b111100000000, 0b111000001001, 0b110100000011, 0b110000001010, 0b101100000110, 0b101000001111, 0b100100000101, 0b100000001100,
			0b011100001100, 0b011000000101, 0b010100001111, 0b010000000110, 0b001100001010, 0b001000000011, 0b000100001001, 0b000000000000
		};

		byte[][] TRI_TABLE = {
			{},
			{0, 8, 3},
			{0, 1, 9},
			{1, 8, 3, 9, 8, 1},
			{1, 2, 10},
			{0, 8, 3, 1, 2, 10},
			{9, 2, 10, 0, 2, 9},
			{2, 8, 3, 2, 10, 8, 10, 9, 8},
			{3, 11, 2},
			{0, 11, 2, 8, 11, 0},
			{1, 9, 0, 2, 3, 11},
			{1, 11, 2, 1, 9, 11, 9, 8, 11},
			{3, 10, 1, 11, 10, 3},
			{0, 10, 1, 0, 8, 10, 8, 11, 10},
			{3, 9, 0, 3, 11, 9, 11, 10, 9},
			{9, 8, 10, 10, 8, 11},
			{4, 7, 8},
			{4, 3, 0, 7, 3, 4},
			{0, 1, 9, 8, 4, 7},
			{4, 1, 9, 4, 7, 1, 7, 3, 1},
			{1, 2, 10, 8, 4, 7},
			{3, 4, 7, 3, 0, 4, 1, 2, 10},
			{9, 2, 10, 9, 0, 2, 8, 4, 7},
			{2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4},
			{8, 4, 7, 3, 11, 2},
			{11, 4, 7, 11, 2, 4, 2, 0, 4},
			{9, 0, 1, 8, 4, 7, 2, 3, 11},
			{4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1},
			{3, 10, 1, 3, 11, 10, 7, 8, 4},
			{1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4},
			{4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3},
			{4, 7, 11, 4, 11, 9, 9, 11, 10},
			{9, 5, 4},
			{9, 5, 4, 0, 8, 3},
			{0, 5, 4, 1, 5, 0},
			{8, 5, 4, 8, 3, 5, 3, 1, 5},
			{1, 2, 10, 9, 5, 4},
			{3, 0, 8, 1, 2, 10, 4, 9, 5},
			{5, 2, 10, 5, 4, 2, 4, 0, 2},
			{2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8},
			{9, 5, 4, 2, 3, 11},
			{0, 11, 2, 0, 8, 11, 4, 9, 5},
			{0, 5, 4, 0, 1, 5, 2, 3, 11},
			{2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5},
			{10, 3, 11, 10, 1, 3, 9, 5, 4},
			{4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10},
			{5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3},
			{5, 4, 8, 5, 8, 10, 10, 8, 11},
			{9, 7, 8, 5, 7, 9},
			{9, 3, 0, 9, 5, 3, 5, 7, 3},
			{0, 7, 8, 0, 1, 7, 1, 5, 7},
			{1, 5, 3, 3, 5, 7},
			{9, 7, 8, 9, 5, 7, 10, 1, 2},
			{10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3},
			{8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2},
			{2, 10, 5, 2, 5, 3, 3, 5, 7},
			{7, 9, 5, 7, 8, 9, 3, 11, 2},
			{9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11},
			{2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7},
			{11, 2, 1, 11, 1, 7, 7, 1, 5},
			{9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11},
			{5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0},
			{11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0},
			{11, 10, 5, 7, 11, 5},
			{10, 6, 5},
			{0, 8, 3, 5, 10, 6},
			{9, 0, 1, 5, 10, 6},
			{1, 8, 3, 1, 9, 8, 5, 10, 6},
			{1, 6, 5, 2, 6, 1},
			{1, 6, 5, 1, 2, 6, 3, 0, 8},
			{9, 6, 5, 9, 0, 6, 0, 2, 6},
			{5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8},
			{2, 3, 11, 10, 6, 5},
			{11, 0, 8, 11, 2, 0, 10, 6, 5},
			{0, 1, 9, 2, 3, 11, 5, 10, 6},
			{5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11},
			{6, 3, 11, 6, 5, 3, 5, 1, 3},
			{0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6},
			{3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9},
			{6, 5, 9, 6, 9, 11, 11, 9, 8},
			{5, 10, 6, 4, 7, 8},
			{4, 3, 0, 4, 7, 3, 6, 5, 10},
			{1, 9, 0, 5, 10, 6, 8, 4, 7},
			{10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4},
			{6, 1, 2, 6, 5, 1, 4, 7, 8},
			{1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7},
			{8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6},
			{7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9},
			{3, 11, 2, 7, 8, 4, 10, 6, 5},
			{5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11},
			{0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6},
			{9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6},
			{8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6},
			{5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11},
			{0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7},
			{6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9},
			{10, 4, 9, 6, 4, 10},
			{4, 10, 6, 4, 9, 10, 0, 8, 3},
			{10, 0, 1, 10, 6, 0, 6, 4, 0},
			{8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10},
			{1, 4, 9, 1, 2, 4, 2, 6, 4},
			{3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4},
			{0, 2, 4, 4, 2, 6},
			{8, 3, 2, 8, 2, 4, 4, 2, 6},
			{10, 4, 9, 10, 6, 4, 11, 2, 3},
			{0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6},
			{3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10},
			{6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1},
			{9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3},
			{8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1},
			{3, 11, 6, 3, 6, 0, 0, 6, 4},
			{6, 4, 8, 11, 6, 8},
			{7, 10, 6, 7, 8, 10, 8, 9, 10},
			{0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10},
			{10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0},
			{10, 6, 7, 10, 7, 1, 1, 7, 3},
			{1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7},
			{2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9},
			{7, 8, 0, 7, 0, 6, 6, 0, 2},
			{7, 3, 2, 6, 7, 2},
			{2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7},
			{2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7},
			{1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11},
			{11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1},
			{8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6},
			{0, 9, 1, 11, 6, 7},
			{7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0},
			{7, 11, 6},
			{7, 6, 11},
			{3, 0, 8, 11, 7, 6},
			{0, 1, 9, 11, 7, 6},
			{8, 1, 9, 8, 3, 1, 11, 7, 6},
			{10, 1, 2, 6, 11, 7},
			{1, 2, 10, 3, 0, 8, 6, 11, 7},
			{2, 9, 0, 2, 10, 9, 6, 11, 7},
			{6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8},
			{7, 2, 3, 6, 2, 7},
			{7, 0, 8, 7, 6, 0, 6, 2, 0},
			{2, 7, 6, 2, 3, 7, 0, 1, 9},
			{1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6},
			{10, 7, 6, 10, 1, 7, 1, 3, 7},
			{10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8},
			{0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7},
			{7, 6, 10, 7, 10, 8, 8, 10, 9},
			{6, 8, 4, 11, 8, 6},
			{3, 6, 11, 3, 0, 6, 0, 4, 6},
			{8, 6, 11, 8, 4, 6, 9, 0, 1},
			{9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6},
			{6, 8, 4, 6, 11, 8, 2, 10, 1},
			{1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6},
			{4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9},
			{10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3},
			{8, 2, 3, 8, 4, 2, 4, 6, 2},
			{0, 4, 2, 4, 6, 2},
			{1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8},
			{1, 9, 4, 1, 4, 2, 2, 4, 6},
			{8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1},
			{10, 1, 0, 10, 0, 6, 6, 0, 4},
			{4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3},
			{10, 9, 4, 6, 10, 4},
			{4, 9, 5, 7, 6, 11},
			{0, 8, 3, 4, 9, 5, 11, 7, 6},
			{5, 0, 1, 5, 4, 0, 7, 6, 11},
			{11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5},
			{9, 5, 4, 10, 1, 2, 7, 6, 11},
			{6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5},
			{7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2},
			{3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6},
			{7, 2, 3, 7, 6, 2, 5, 4, 9},
			{9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7},
			{3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0},
			{6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8},
			{9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7},
			{1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4},
			{4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10},
			{7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10},
			{6, 9, 5, 6, 11, 9, 11, 8, 9},
			{3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5},
			{0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11},
			{6, 11, 3, 6, 3, 5, 5, 3, 1},
			{1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6},
			{0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10},
			{11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5},
			{6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3},
			{5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2},
			{9, 5, 6, 9, 6, 0, 0, 6, 2},
			{1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8},
			{1, 5, 6, 2, 1, 6},
			{1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6},
			{10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0},
			{0, 3, 8, 5, 6, 10},
			{10, 5, 6},
			{11, 5, 10, 7, 5, 11},
			{11, 5, 10, 11, 7, 5, 8, 3, 0},
			{5, 11, 7, 5, 10, 11, 1, 9, 0},
			{10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1},
			{11, 1, 2, 11, 7, 1, 7, 5, 1},
			{0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11},
			{9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7},
			{7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2},
			{2, 5, 10, 2, 3, 5, 3, 7, 5},
			{8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5},
			{9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2},
			{9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2},
			{1, 3, 5, 3, 7, 5},
			{0, 8, 7, 0, 7, 1, 1, 7, 5},
			{9, 0, 3, 9, 3, 5, 5, 3, 7},
			{9, 8, 7, 5, 9, 7},
			{5, 8, 4, 5, 10, 8, 10, 11, 8},
			{5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0},
			{0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5},
			{10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4},
			{2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8},
			{0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11},
			{0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5},
			{9, 4, 5, 2, 11, 3},
			{2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4},
			{5, 10, 2, 5, 2, 4, 4, 2, 0},
			{3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9},
			{5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2},
			{8, 4, 5, 8, 5, 3, 3, 5, 1},
			{0, 4, 5, 1, 0, 5},
			{8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5},
			{9, 4, 5},
			{4, 11, 7, 4, 9, 11, 9, 10, 11},
			{0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11},
			{1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11},
			{3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4},
			{4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2},
			{9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3},
			{11, 7, 4, 11, 4, 2, 2, 4, 0},
			{11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4},
			{2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9},
			{9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7},
			{3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10},
			{1, 10, 2, 8, 7, 4},
			{4, 9, 1, 4, 1, 7, 7, 1, 3},
			{4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1},
			{4, 0, 3, 7, 4, 3},
			{4, 8, 7},
			{9, 10, 8, 10, 11, 8},
			{3, 0, 9, 3, 9, 11, 11, 9, 10},
			{0, 1, 10, 0, 10, 8, 8, 10, 11},
			{3, 1, 10, 11, 3, 10},
			{1, 2, 11, 1, 11, 9, 9, 11, 8},
			{3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9},
			{0, 2, 11, 8, 0, 11},
			{3, 2, 11},
			{2, 3, 8, 2, 8, 10, 10, 8, 9},
			{9, 10, 2, 0, 9, 2},
			{2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8},
			{1, 10, 2},
			{1, 3, 8, 9, 1, 8},
			{0, 9, 1},
			{0, 3, 8},
			{}
		};

	}
}
