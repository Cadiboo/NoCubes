package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

import static io.github.cadiboo.nocubes.mesh.MarchingCubes.Lookup.*;

public class MarchingCubesRenderer {

	public static final float ISO_LEVEL = 0.5F;

	public static void renderChunk(
		RenderChunk renderChunk,
		BlockPos chunkStartPos,
		ChunkCompileTaskGenerator generator,
		CompiledChunk compiledChunk,
		// Minecraft#world, chunk updates can occur while we are rendering an older change so maybe we should not use this
		// Also isn't thread local
		World world,
		// The ChunkCache that has been filled with the blocks that we should be rendering
		IBlockAccess chunkView,
		boolean[] usedRenderLayers, Random random,
		BlockRendererDispatcher blockRendererDispatcher
	) {
		marchChunk(chunkStartPos, world, (a, b) -> true);
	}

	public static void marchChunk(BlockPos chunkStartPos, World world, SurfaceNets.FaceAction faceAction) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

		// The dimensions of the resulting mesh
		final int meshSize = 16;
		// We need to know about the data of each cells positive neighbours so we need to sample +1 on each axis
		// I.e. to compute the mask for the block at the edge of the chunk we need to know about the densities of it's positive neighbours
		// These neighbours will be in other chunks
		final int sampleSize = meshSize + 1;


		boolean[] binaryField = new boolean[sampleSize * sampleSize * sampleSize];
		{
			int i = 0;
			for (int z = 0; z < sampleSize; z++) {
				for (int y = 0; y < sampleSize; y++) {
					for (int x = 0; x < sampleSize; x++, ++i) {
						mutable.setPos(chunkStartPos.getX() + x, chunkStartPos.getY() + y, chunkStartPos.getZ() + z);
						IBlockState state = world.getBlockState(chunkStartPos.add(x, y, z));
						boolean shouldSmooth = IsSmoothable.TERRAIN.test(state);

						float density = ModUtil.getIndividualBlockDensity(shouldSmooth, state);
						// true where the data value is above the isovalue
						// false where the data value is below the isovalue
						binaryField[i] = density > ISO_LEVEL;
					}
				}
			}
		}

		{
			Face face = new Face(new Vec(), new Vec(), new Vec(), new Vec());
			float[] neighbourDensities = new float[8];

			Vec[] currentVoxelVertices = new Vec[12];
			for (int i = 0; i < currentVoxelVertices.length; i++)
				currentVoxelVertices[i] = new Vec();

			for (int z = 0; z < meshSize; z++) {
				for (int y = 0; y < meshSize; y++) {
					for (int x = 0; x < meshSize; x++) {

						int neighbourMask = 0;
						for (int bit = 0; bit < CUBE_VERTS.length; bit++) {
							Vec v = CUBE_VERTS[bit];
							int index = ModUtil.get3dIndexInto1dArray(x + (int) v.x, y + (int) v.y, z + (int) v.z, sampleSize, sampleSize);
							boolean isSmoothable = binaryField[index];
							if (isSmoothable)
								neighbourMask |= 1 << bit;

							// This is the bit that will fix snow
							float density = isSmoothable ? 1 : -1;
							neighbourDensities[bit] = density;
						}
//						// Look at it's eight neighbours
//						int bit = 0;
//						for (int nz = 0; nz < 2; ++nz) {
//							for (int ny = 0; ny < 2; ny++) {
//								for (int nx = 0; nx < 2; nx++, ++bit) {
//									int index = ModUtil.get3dIndexInto1dArray(x + nx, y + ny, z + nz, sampleSize, sampleSize);
//									boolean isSmoothable = binaryField[index];
//									if (isSmoothable)
//										neighbourMask |= 1 << bit;
//
//									// This is the bit that will fix snow
//									float density = isSmoothable ? 1 : -1;
//									neighbourDensities[bit] = density;
//								}
//							}
//						}

						// Use the cell index to access a pre-built lookup table with 16 entries listing the edges needed to represent the cell
						int edgeMask = EDGE_TABLE[neighbourMask];

						// 12 edges on a cube
						for (int edge = 0; edge < 12; edge++) {
							if ((edgeMask & edge) == edge) {
								byte[] vertexIndices = EDGE_INDEX[edge];

								byte startVertexIndex = vertexIndices[0];
								byte endVertexIndex = vertexIndices[1];

								Vec startVertex = CUBE_VERTS[startVertexIndex];
								Vec endVertex = CUBE_VERTS[endVertexIndex];

								float densityAtStart = neighbourDensities[startVertexIndex];
								float densityAtEnd = neighbourDensities[endVertexIndex];

								Vec vertex = currentVoxelVertices[edge];
								// Mutates vertex
								interpolate(ISO_LEVEL, startVertex, endVertex, densityAtStart, densityAtEnd, vertex);
							}

						}

						mutable.setPos(chunkStartPos.getX() + x, chunkStartPos.getY() + y, chunkStartPos.getZ() + z);

						byte[] trianglesForVoxel = TRI_TABLE[neighbourMask];
						for (int i = 0; i < trianglesForVoxel.length; i += 3) {
							face.v0.copyFrom(currentVoxelVertices[trianglesForVoxel[i + 0]]);
							face.v1.copyFrom(currentVoxelVertices[trianglesForVoxel[i + 1]]);
							face.v2.copyFrom(currentVoxelVertices[trianglesForVoxel[i + 2]]);
							face.v3.copyFrom(currentVoxelVertices[trianglesForVoxel[i + 2]]);
							face.add(x, y, z);
							faceAction.apply(face, mutable);
						}

					}
				}
			}
		}
	}

	private static void interpolate(float isoLevel, Vec v0, Vec v1, float densityAtv0, float densityAtv1, Vec toUse) {
		float mu = (isoLevel - densityAtv0) / (densityAtv0 - densityAtv1);
		toUse.interpolate(mu, v0, v1);
	}

}
