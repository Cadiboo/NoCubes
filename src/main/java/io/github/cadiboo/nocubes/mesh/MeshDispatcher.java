package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
//TODO: clean up this class
public final class MeshDispatcher {

	// ++laziness
	@Nonnull
	public static FaceList generateBlockMeshOffset(
			@Nonnull final BlockPos pos,
			@Nonnull final IBlockReader reader,
			@Nonnull final IsSmoothable isSmoothable,
			@Nonnull final MeshGeneratorType meshGenerator
	) {
		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			return generateBlockMeshOffset(pos, reader, pooledMutableBlockPos, isSmoothable, meshGenerator);
		}
	}

	/**
	 * @return the offset vertices for the block
	 */
	@Nonnull
	public static FaceList generateBlockMeshOffset(
			@Nonnull final BlockPos pos,
			@Nonnull final IBlockReader reader,
			@Nonnull final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final IsSmoothable isSmoothable,
			@Nonnull final MeshGeneratorType meshGenerator
	) {

		if (meshGenerator == MeshGeneratorType.OldNoCubes) {
			return OldNoCubes.generateBlock(pos, reader, isSmoothable, pooledMutableBlockPos);
		}

		final FaceList chunkData = generateBlockMeshUnOffset(
				pos, reader, pooledMutableBlockPos,
				isSmoothable,
				meshGenerator
		);

		final int chunkPosX = (pos.getX() >> 4) << 4;
		final int chunkPosY = (pos.getY() >> 4) << 4;
		final int chunkPosZ = (pos.getZ() >> 4) << 4;

		return offsetFaceList(chunkPosX, chunkPosY, chunkPosZ, chunkData);
	}

	/**
	 * @return the un offset vertices for the block
	 */
	@Nonnull
	private static FaceList generateBlockMeshUnOffset(
			@Nonnull final BlockPos pos,
			@Nonnull final IBlockReader reader,
			final PooledMutableBlockPos pooledMutableBlockPos,
			@Nonnull final IsSmoothable isSmoothable,
			@Nonnull final MeshGeneratorType meshGeneratorType
	) {

		try (ModProfiler ignored = ModProfiler.get().start("generateBlock")) {
//			if(true)
//				return meshGeneratorType.generateBlock(pos, reader, isSmoothable);

			final int posX = pos.getX();
			final int posY = pos.getY();
			final int posZ = pos.getZ();

			// Convert block pos to relative block pos
			// For example 68 -> 4, 127 -> 15, 4 -> 4, 312312312 -> 8
			final int relativePosX = posX & 15;
			final int relativePosY = posY & 15;
			final int relativePosZ = posZ & 15;

			final byte addX;
			final byte addY;
			final byte addZ;
			final byte subX;
			final byte subY;
			final byte subZ;
			//FFS
			if (meshGeneratorType == MeshGeneratorType.MarchingCubes) {
				addX = 1;
				addY = 1;
				addZ = 1;
				subX = 0;
				subY = 0;
				subZ = 0;
			} else if (meshGeneratorType == MeshGeneratorType.MarchingTetrahedra) {
				addX = 1;
				addY = 1;
				addZ = 1;
				subX = 0;
				subY = 0;
				subZ = 0;
			} else if (meshGeneratorType == MeshGeneratorType.SurfaceNets) {
				addX = 0;
				addY = 0;
				addZ = 0;
				subX = 1;
				subY = 1;
				subZ = 1;
			} else {
				addX = 0;
				addY = 0;
				addZ = 0;
				subX = 0;
				subY = 0;
				subZ = 0;
			}

			final MeshGenerator meshGenerator = meshGeneratorType.getMeshGenerator();

			final byte meshSizeX = (byte) (2 + addX + subX + meshGenerator.getSizeXExtension());
			final byte meshSizeY = (byte) (2 + addY + subY + meshGenerator.getSizeYExtension());
			final byte meshSizeZ = (byte) (2 + addZ + subZ + meshGenerator.getSizeZExtension());

			final float[] densityData = new float[meshSizeX * meshSizeY * meshSizeZ];

			final int startPosX = posX - subX;
			final int startPosY = posY - subY;
			final int startPosZ = posZ - subZ;

			int index = 0;
			for (int z = 0; z < meshSizeX; ++z) {
				for (int y = 0; y < meshSizeY; ++y) {
					for (int x = 0; x < meshSizeZ; ++x, ++index) {

						float density = 0;
						for (int zOffset = 0; zOffset < 2; ++zOffset) {
							for (int yOffset = 0; yOffset < 2; ++yOffset) {
								for (int xOffset = 0; xOffset < 2; ++xOffset) {

									pooledMutableBlockPos.setPos(startPosX + x - xOffset, startPosY + y - yOffset, startPosZ + z - zOffset);
									final BlockState state = reader.getBlockState(pooledMutableBlockPos);
									density += ModUtil.getIndividualBlockDensity(isSmoothable.apply(state), state);
								}
							}
						}
						densityData[index] = density;

					}
				}
			}

			FaceList finalFaces = FaceList.retain();
			final HashMap<Vec3b, FaceList> vec3bFaceListHashMap = meshGenerator.generateChunk(densityData, new byte[]{meshSizeX, meshSizeY, meshSizeZ});
			for (final FaceList generatedFaceList : vec3bFaceListHashMap.values()) {
				finalFaces.addAll(generatedFaceList);
				generatedFaceList.close();
			}
			for (final Vec3b vec3b : vec3bFaceListHashMap.keySet()) {
				vec3b.close();
			}

			for (Face face : finalFaces) {
				final Vec3 vertex0 = face.getVertex0();
				final Vec3 vertex1 = face.getVertex1();
				final Vec3 vertex2 = face.getVertex2();
				final Vec3 vertex3 = face.getVertex3();

				vertex0.addOffset(relativePosX, relativePosY, relativePosZ);
				vertex1.addOffset(relativePosX, relativePosY, relativePosZ);
				vertex2.addOffset(relativePosX, relativePosY, relativePosZ);
				vertex3.addOffset(relativePosX, relativePosY, relativePosZ);

				vertex0.addOffset(-subX, -subY, -subZ);
				vertex1.addOffset(-subX, -subY, -subZ);
				vertex2.addOffset(-subX, -subY, -subZ);
				vertex3.addOffset(-subX, -subY, -subZ);

			}

			return finalFaces;
		}
	}

	/**
	 * Modifies the chunk data mesh in! Returns the offset mesh for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	public static HashMap<Vec3b, FaceList> offsetChunkMesh(
			@Nonnull final BlockPos chunkPos,
			@Nonnull final HashMap<Vec3b, FaceList> chunkData
	) {
		offsetMesh(chunkPos.getX(), chunkPos.getY(), chunkPos.getZ(), chunkData);
		return chunkData;
	}

	/**
	 * Modifies the chunk data mesh in! Returns the offset mesh for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	public static HashMap<Vec3b, FaceList> offsetMesh(
			final int offsetX, final int offsetY, final int offsetZ,
			@Nonnull final HashMap<Vec3b, FaceList> meshData
	) {
		for (FaceList faces : meshData.values()) {
			offsetFaceList(offsetX, offsetY, offsetZ, faces);
		}
		return meshData;
	}

	/**
	 * Modifies the block mesh passed in! Returns the offset mesh for convenience
	 * Offsets the data from relative pos to real pos and applies offsetVertices
	 */
	@Nonnull
	private static FaceList offsetFaceList(
			final int chunkPosX, final int chunkPosY, final int chunkPosZ,
			@Nonnull final FaceList faces
	) {
		final int size = faces.size();
		for (int i = 0; i < size; ++i) {
			final Face face = faces.get(i);
			final Vec3 vertex0 = face.getVertex0();
			final Vec3 vertex1 = face.getVertex1();
			final Vec3 vertex2 = face.getVertex2();
			final Vec3 vertex3 = face.getVertex3();

			vertex0.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex1.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex2.addOffset(chunkPosX, chunkPosY, chunkPosZ);
			vertex3.addOffset(chunkPosX, chunkPosY, chunkPosZ);

//			if (ModConfig.offsetVertices) {
//				ModUtil.offsetVertex(vertex0);
//				ModUtil.offsetVertex(vertex1);
//				ModUtil.offsetVertex(vertex2);
//				ModUtil.offsetVertex(vertex3);
//			}
		}
		return faces;
	}

}
