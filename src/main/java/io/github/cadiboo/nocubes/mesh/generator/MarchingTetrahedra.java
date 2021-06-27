package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public final class MarchingTetrahedra implements MeshGenerator {

	@Override
	public BlockPos getPositiveAreaExtension() {
		// Need data about the each block's direct neighbours to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	@Override
	public BlockPos getNegativeAreaExtension() {
		// Need data about the each block's direct neighbours to check if they should be culled
		return ModUtil.VEC_ONE;
	}

	private final byte[][] CUBE_VERTICES = {
		{0, 0, 0},
		{1, 0, 0},
		{1, 1, 0},
		{0, 1, 0},
		{0, 0, 1},
		{1, 0, 1},
		{1, 1, 1},
		{0, 1, 1}
	};

	private final byte[][] TETRA_LIST = {
		{0, 2, 3, 7},
		{0, 6, 2, 7},
		{0, 4, 6, 7},
		{0, 6, 1, 2},
		{0, 1, 6, 4},
		{5, 6, 1, 4}
	};

	@Override
	public void generate(Area area, Predicate<IBlockState> isSmoothable, VoxelAction voxelAction, FaceAction faceAction) {
		byte[] dims = {(byte) (area.size.getX()), (byte) (area.size.getY()), (byte) (area.size.getZ())};
		MutableBlockPos pos = new MutableBlockPos();
		Face face = new Face();
		float[] data = MeshGenerator.generateCornerDistanceField(area, isSmoothable);

		final byte[][] cube_vertices = CUBE_VERTICES;
		final byte[][] tetra_list = TETRA_LIST;

		final byte[] x = {0, 0, 0};
		short n = 0;
		final float[] grid = new float[8];

		//March over the volume
		for (x[2] = 0; x[2] < dims[2] - 1; ++x[2], n += dims[0])
			for (x[1] = 0; x[1] < dims[1] - 1; ++x[1], ++n)
				for (x[0] = 0; x[0] < dims[0] - 1; ++x[0], ++n) {
					//Read in cube
					for (byte i = 0; i < 8; ++i) {
						grid[i] = data[n + cube_vertices[i][0] + dims[0] * (cube_vertices[i][1] + dims[1] * cube_vertices[i][2])];
					}
					for (byte i = 0; i < tetra_list.length; ++i) {
						byte[] T = tetra_list[i];
						byte triindex = 0;
						if (grid[T[0]] < 0) triindex |= 1;
						if (grid[T[1]] < 0) triindex |= 2;
						if (grid[T[2]] < 0) triindex |= 4;
						if (grid[T[3]] < 0) triindex |= 8;

						if (!voxelAction.apply(pos.setPos(x[0], x[1], x[2]), triindex / 15F))
							return;

						//Handle each case
						switch (triindex) {
							case 0x00:
							case 0x0F:
								break;
							case 0x0E:
								interp(face.v0, T[0], T[1], grid, x);
								interp(face.v1, T[0], T[3], grid, x);
								interp(face.v2, T[0], T[2], grid, x);
								triToQuad(face);
								break;
							case 0x01:
								interp(face.v0, T[0], T[1], grid, x);
								interp(face.v1, T[0], T[2], grid, x);
								interp(face.v2, T[0], T[3], grid, x);
								triToQuad(face);
								break;
							case 0x0D:
								interp(face.v0, T[1], T[0], grid, x);
								interp(face.v1, T[1], T[2], grid, x);
								interp(face.v2, T[1], T[3], grid, x);
								triToQuad(face);
								break;
							case 0x02:
								interp(face.v0, T[1], T[0], grid, x);
								interp(face.v1, T[1], T[3], grid, x);
								interp(face.v2, T[1], T[2], grid, x);
								triToQuad(face);
								break;
							case 0x0C:
								interp(face.v0, T[1], T[2], grid, x);
								interp(face.v1, T[1], T[3], grid, x);
								interp(face.v2, T[0], T[3], grid, x);
								interp(face.v3, T[0], T[2], grid, x);
								break;
							case 0x03:
								interp(face.v0, T[1], T[2], grid, x);
								interp(face.v1, T[0], T[2], grid, x);
								interp(face.v2, T[0], T[3], grid, x);
								interp(face.v3, T[1], T[3], grid, x);
								break;
							case 0x04:
								interp(face.v0, T[2], T[0], grid, x);
								interp(face.v1, T[2], T[1], grid, x);
								interp(face.v2, T[2], T[3], grid, x);
								triToQuad(face);
								break;
							case 0x0B:
								interp(face.v0, T[2], T[0], grid, x);
								interp(face.v1, T[2], T[3], grid, x);
								interp(face.v2, T[2], T[1], grid, x);
								triToQuad(face);
								break;
							case 0x05:
								interp(face.v0, T[0], T[1], grid, x);
								interp(face.v1, T[1], T[2], grid, x);
								interp(face.v2, T[2], T[3], grid, x);
								interp(face.v3, T[0], T[3], grid, x);
								break;
							case 0x0A:
								interp(face.v0, T[0], T[1], grid, x);
								interp(face.v1, T[0], T[3], grid, x);
								interp(face.v2, T[2], T[3], grid, x);
								interp(face.v3, T[1], T[2], grid, x);
								break;
							case 0x06:
								interp(face.v0, T[2], T[3], grid, x);
								interp(face.v1, T[0], T[2], grid, x);
								interp(face.v2, T[0], T[1], grid, x);
								interp(face.v3, T[1], T[3], grid, x);
								break;
							case 0x09:
								interp(face.v0, T[2], T[3], grid, x);
								interp(face.v1, T[1], T[3], grid, x);
								interp(face.v2, T[0], T[1], grid, x);
								interp(face.v3, T[0], T[2], grid, x);
								break;
							case 0x07:
								interp(face.v0, T[3], T[0], grid, x);
								interp(face.v1, T[3], T[1], grid, x);
								interp(face.v2, T[3], T[2], grid, x);
								triToQuad(face);
								break;
							case 0x08:
								interp(face.v0, T[3], T[0], grid, x);
								interp(face.v1, T[3], T[2], grid, x);
								interp(face.v2, T[3], T[1], grid, x);
								triToQuad(face);
								break;
						}

						if (!faceAction.apply(pos.setPos(x[0], x[1], x[2]), face))
							return;
					}
				}
	}

	private void triToQuad(Face face) {
		face.v3.set(face.v2);
	}

	private Vec interp(Vec v, byte i0, byte i1, float[] grid, byte[] x) {
		float g0 = grid[i0];
		float g1 = grid[i1];
		byte[] p0 = CUBE_VERTICES[i0];
		byte[] p1 = CUBE_VERTICES[i1];
		v.set(x[0], x[1], x[2]);
		float t = g0 - g1;
		if (Math.abs(t) > 1e-6)
			t = g0 / t;
		v.x += p0[0] + t * (p1[0] - p0[0]);
		v.y += p0[1] + t * (p1[1] - p0[1]);
		v.z += p0[2] + t * (p1[2] - p0[2]);
		return v;
	}

}
