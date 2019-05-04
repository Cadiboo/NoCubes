package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkCache;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeInt;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static io.github.cadiboo.nocubes.util.IIsSmoothable.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public final class Hooks {

	public static void preIteration(final RenderChunk renderChunk, final float x, final float y, final float z, final ChunkRenderTask generator, final CompiledChunk compiledchunk, final BlockPos blockpos, final BlockPos blockpos1, final World world, final RenderChunkCache lvt_10_1_, final VisGraph lvt_11_1_, final HashSet lvt_12_1_, final boolean[] aboolean, final Random random, final BlockRendererDispatcher blockrendererdispatcher) {
		if (NoCubes.isEnabled()) {
			RenderDispatcher.renderChunk(renderChunk, blockpos, generator, compiledchunk, world, aboolean, random, blockrendererdispatcher);
		}
	}

	//return if normal rendering should happen
	public static boolean renderBlockDamage(final Tessellator tessellatorIn, final BufferBuilder bufferBuilderIn, final BlockPos blockpos, final IBlockState iblockstate, final WorldClient world, final TextureAtlasSprite textureatlassprite, final BlockRendererDispatcher blockrendererdispatcher) {
		if (!NoCubes.isEnabled() || !Config.renderSmoothTerrain || !iblockstate.nocubes_isTerrainSmoothable()) {
			return true;
		}
		RenderDispatcher.renderSmoothBlockDamage(tessellatorIn, bufferBuilderIn, blockpos, iblockstate, world, textureatlassprite);
		return false;
	}

	public static Stream<VoxelShape> getCollisionBoxes(final IWorldReaderBase iWorldReaderBase, final VoxelShape area, final VoxelShape entityShape, final boolean isEntityInsideWorldBorder, final int minXm1, final int maxXp1, final int minYm1, final int maxYp1, final int minZm1, final int maxZp1, final WorldBorder worldborder, final boolean isAreaInsideWorldBorder, final VoxelShapePart voxelshapepart, final Predicate<VoxelShape> predicate) {

		int i = minXm1;
		int j = maxXp1;
		int k = minYm1;
		int l = maxYp1;
		int i1 = minZm1;
		int j1 = maxZp1;

		final float boxRadius = 0.125F;

		final boolean ignoreIntersects = false;

		final ModProfiler profiler = ModProfiler.get();

		final ArrayList<VoxelShape> finalCollidingBoxes = new ArrayList<>();
		final ArrayList<VoxelShape> boxes = new ArrayList<>();

		try (
				PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()
		) {

			final MeshGenerator meshGenerator = Config.terrainMeshGenerator;

//			final int posX = ((BlockPos) pos).getX();
//			final int posY = ((BlockPos) pos).getY();
//			final int posZ = ((BlockPos) pos).getZ();

			final int posX = minXm1;
			final int posY = minZm1;
			final int posZ = minZm1;

			final int chunkPosX = (posX >> 4) << 4;
			final int chunkPosY = (posY >> 4) << 4;
			final int chunkPosZ = (posZ >> 4) << 4;

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
			if (meshGenerator == MeshGenerator.MarchingCubes) {
				addX = 1;
				addY = 1;
				addZ = 1;
				subX = 0;
				subY = 0;
				subZ = 0;
			} else if (meshGenerator == MeshGenerator.MarchingTetrahedra) {
				addX = 1;
				addY = 1;
				addZ = 1;
				subX = 0;
				subY = 0;
				subZ = 0;
			} else if (meshGenerator == MeshGenerator.SurfaceNets) {
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

			final byte meshSizeX = (byte) ((j - i) + 2 + addX + subX + meshGenerator.getSizeXExtension());
			final byte meshSizeY = (byte) ((l - k) + 2 + addY + subY + meshGenerator.getSizeYExtension());
			final byte meshSizeZ = (byte) ((j1 - i1) + 2 + addZ + subZ + meshGenerator.getSizeZExtension());

			profiler.start("generateCollisionMeshStateCache");
			try (
					final StateCache stateCache = null
//					CacheUtil.generateStateCache(
//							minXm1 , minYm1 - 1, minZm1 - 1,
//							meshSizeX + 3, meshSizeY + 3, meshSizeZ + 3,
//							iWorldReaderBase, pooledMutableBlockPos
//					)
			) {
				profiler.end();

				profiler.start("generateCollisionMeshDensityCache");
//				final IBlockState[] stateCacheArray = stateCache.getBlockStates();
				final IIsSmoothable isSmoothable = TERRAIN_SMOOTHABLE;

				final float[] densityData = new float[meshSizeX * meshSizeY * meshSizeZ];

				final int startPosX = posX - subX;
				final int startPosY = posY - subY;
				final int startPosZ = posZ - subZ;

				int index = 0;
				for (int z = 0; z < meshSizeZ; ++z) {
					for (int y = 0; y < meshSizeY; ++y) {
						for (int x = 0; x < meshSizeX; ++x, ++index) {

							float density = 0;
							for (int zOffset = 0; zOffset < 2; ++zOffset) {
								for (int yOffset = 0; yOffset < 2; ++yOffset) {
									for (int xOffset = 0; xOffset < 2; ++xOffset) {

//										final IBlockState state = stateCacheArray[stateCache.getIndex(1 + x - xOffset, 1 + y - yOffset, 1 + z - zOffset)];
										pooledMutableBlockPos.setPos(startPosX + x - xOffset, startPosY + y - yOffset, startPosZ + z - zOffset);
										final IBlockState state = iWorldReaderBase.getBlockState(pooledMutableBlockPos);
										density += ModUtil.getIndividualBlockDensity(isSmoothable.isSmoothable(state), state);
									}
								}
							}
							densityData[index] = density;

						}
					}
				}
				profiler.end();

				FaceList finalFaces = FaceList.retain();
				final HashMap<Vec3b, FaceList> vec3bFaceListHashMap = meshGenerator.generateChunk(densityData, meshSizeX, meshSizeY, meshSizeZ);
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

				MeshDispatcher.offsetBlockMesh(chunkPosX, chunkPosY, chunkPosZ, finalFaces);

				for (final BlockPos.MutableBlockPos pos : BlockPos.MutableBlockPos.getAllInBoxMutable(minXm1, minYm1, minZm1, maxXp1 - 1, maxYp1 - 1, maxZp1 - 1)) {

//					try (FaceList faces = generateBlockMeshOffset(pooledMutableBlockPos, meshSizeX, meshSizeY, meshSizeZ, stateCache, stateCacheArray, densityData, pos, iWorldReaderBase, isSmoothable, meshGenerator)) {
					//		final AxisAlignedBB originalBox = state.getCollisionBoundingBox(worldIn, pos);
//		final AxisAlignedBB originalBoxOffset = originalBox == null ? null : originalBox.offset(pos);
//
					for (Face face : finalFaces) {
//							try {
						addFaceBoxesToList(boxes, face, profiler, null, boxRadius);
//							} finally {
//								face.close();
//							}
					}

					for (final VoxelShape box : boxes) {
						addCollisionBoxToList(finalCollidingBoxes, box, predicate, ignoreIntersects);
					}
//					}

				}

				for (final Face face : finalFaces) {
					face.getVertex0().close();
					face.getVertex1().close();
					face.getVertex2().close();
					face.getVertex3().close();
					face.close();
				}
				finalFaces.close();

			}
		}

		Stream<VoxelShape> stream = StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(i, k, i1, j - 1, l - 1, j1 - 1).spliterator(), false).map((pos) -> {
			int k1 = pos.getX();
			int l1 = pos.getY();
			int i2 = pos.getZ();
			boolean flag1 = k1 == i || k1 == j - 1;
			boolean flag2 = l1 == k || l1 == l - 1;
			boolean flag3 = i2 == i1 || i2 == j1 - 1;
			if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && iWorldReaderBase.isBlockLoaded(pos)) {
				final VoxelShape voxelshape;
				if (isEntityInsideWorldBorder && !isAreaInsideWorldBorder && !worldborder.contains(pos)) {
					voxelshape = VoxelShapes.fullCube();
				} else {
					final IBlockState blockState = iWorldReaderBase.getBlockState(pos);
					if (blockState.nocubes_isTerrainSmoothable()) {
						voxelshape = VoxelShapes.empty();
					} else {
						voxelshape = blockState.getCollisionShape(iWorldReaderBase, pos);
					}
				}

				VoxelShape voxelshape1 = entityShape.withOffset((double) (-k1), (double) (-l1), (double) (-i2));
				if (VoxelShapes.compare(voxelshape1, voxelshape, IBooleanFunction.AND)) {
					return VoxelShapes.empty();
				} else if (voxelshape == VoxelShapes.fullCube()) {
					voxelshapepart.setFilled(k1 - i, l1 - k, i2 - i1, true, true);
					return VoxelShapes.empty();
				} else {
					return voxelshape.withOffset((double) k1, (double) l1, (double) i2);
				}
			} else {
				return VoxelShapes.empty();
			}
		}).filter(predicate);

//		return Stream.concat(finalCollidingBoxes.stream(), Stream.generate(() -> new VoxelShapeInt(voxelshapepart, minXm1, minYm1, minZm1)).limit(1L).filter(predicate));
		return Stream.concat(Stream.concat(stream, finalCollidingBoxes.stream()), Stream.generate(() -> new VoxelShapeInt(voxelshapepart, minXm1, minYm1, minZm1)).limit(1L).filter(predicate));
//		return Stream.concat(stream, Stream.generate(() -> new VoxelShapeInt(voxelshapepart, minXm1, minYm1, minZm1)).limit(1L).filter(predicate));
	}

	private static FaceList generateBlockMeshOffset(final PooledMutableBlockPos pooledMutableBlockPos, final byte meshSizeX, final byte meshSizeY, final byte meshSizeZ, final StateCache stateCache, final IBlockState[] stateCacheArray, final float[] densityData, final BlockPos.MutableBlockPos pos, final IWorldReaderBase iWorldReaderBase, final IIsSmoothable isSmoothable, final MeshGenerator meshGenerator) {
		return null;
//		final FaceList chunkData;
//
//		try (ModProfiler ignored = ModProfiler.get().start("generateBlockMeshOffsetInlined")) {
////			if(true)
////				return meshGenerator.generateBlock(pos, blockAccess, isSmoothable);
//
//
//
//
//
//			final float[] densityData1 = new float[meshSizeX1 * meshSizeY1 * meshSizeZ1];
//
//			final int startPosX = posX - subX;
//			final int startPosY = posY - subY;
//			final int startPosZ = posZ - subZ;
//
//			int index = 0;
//			for (int z = 0; z < meshSizeX1; ++z) {
//				for (int y = 0; y < meshSizeY1; ++y) {
//					for (int x = 0; x < meshSizeZ1; ++x, ++index) {
//
//						float density = 0;
//						for (int zOffset = 0; zOffset < 2; ++zOffset) {
//							for (int yOffset = 0; yOffset < 2; ++yOffset) {
//								for (int xOffset = 0; xOffset < 2; ++xOffset) {
//
////									final IBlockState state = stateCacheArray[stateCache.getIndex(1 + x - xOffset, 1 + y - yOffset, 1 + z - zOffset)];
//									pooledMutableBlockPos.setPos(startPosX + x - xOffset, startPosY + y - yOffset, startPosZ + z - zOffset);
//									final IBlockState state = iWorldReaderBase.getBlockState(pooledMutableBlockPos);
//									density += ModUtil.getIndividualBlockDensity(isSmoothable.isSmoothable(state), state);
//								}
//							}
//						}
//						densityData1[index] = density;
//
//					}
//				}
//			}
//
//
//
//			chunkData = finalFaces;
//		}
//
//		final int chunkPosX = (pos.getX() >> 4) << 4;
//		final int chunkPosY = (pos.getY() >> 4) << 4;
//		final int chunkPosZ = (pos.getZ() >> 4) << 4;
//
//		return MeshDispatcher.offsetBlockMesh(chunkPosX, chunkPosY, chunkPosZ, chunkData);
//
////		final int posX = pos.getX();
////		final int posY = pos.getY();
////		final int posZ = pos.getZ();
////
////		// Convert block pos to relative block pos
////		// For example 68 -> 4, 127 -> 15, 4 -> 4, 312312312 -> 8
////		final int relativePosX = posX & 15;
////		final int relativePosY = posY & 15;
////		final int relativePosZ = posZ & 15;
////
////		final int chunkPosX = (posX >> 4) << 4;
////		final int chunkPosY = (posY >> 4) << 4;
////		final int chunkPosZ = (posZ >> 4) << 4;
////
////		FaceList finalFaces = FaceList.retain();
////		final HashMap<Vec3b, FaceList> vec3bFaceListHashMap = meshGenerator.generateChunk(densityData, meshSizeX, meshSizeY, meshSizeZ);
////		for (final FaceList generatedFaceList : vec3bFaceListHashMap.values()) {
////			finalFaces.addAll(generatedFaceList);
////			generatedFaceList.close();
////		}
////		for (final Vec3b vec3b : vec3bFaceListHashMap.keySet()) {
////			vec3b.close();
////		}
////
////		for (Face face : finalFaces) {
////			final Vec3 vertex0 = face.getVertex0();
////			final Vec3 vertex1 = face.getVertex1();
////			final Vec3 vertex2 = face.getVertex2();
////			final Vec3 vertex3 = face.getVertex3();
////
////			vertex0.addOffset(relativePosX, relativePosY, relativePosZ);
////			vertex1.addOffset(relativePosX, relativePosY, relativePosZ);
////			vertex2.addOffset(relativePosX, relativePosY, relativePosZ);
////			vertex3.addOffset(relativePosX, relativePosY, relativePosZ);
////
////			vertex0.addOffset(chunkPosX, chunkPosY, chunkPosZ);
////			vertex1.addOffset(chunkPosX, chunkPosY, chunkPosZ);
////			vertex2.addOffset(chunkPosX, chunkPosY, chunkPosZ);
////			vertex3.addOffset(chunkPosX, chunkPosY, chunkPosZ);
////
//////			vertex0.addOffset(-subX, -subY, -subZ);
//////			vertex1.addOffset(-subX, -subY, -subZ);
//////			vertex2.addOffset(-subX, -subY, -subZ);
//////			vertex3.addOffset(-subX, -subY, -subZ);
////
////		}
////
////		return finalFaces;

	}

	private static int average(final double d0, final double d1, final double d2, final double d3) {
		return (int) ((d0 + d1 + d2 + d3) / 4);
	}

	private static void addFaceBoxesToList(final List<VoxelShape> outBoxes, final Face face, final ModProfiler profiler, final VoxelShape originalBoxOffset, final float boxRadius) {

		//0___3
		//_____
		//_____
		//_____
		//1___2
		final Vec3 v0;
		final Vec3 v1;
		final Vec3 v2;
		final Vec3 v3;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final Vec3 v0v1;
		final Vec3 v1v2;
		final Vec3 v2v3;
		final Vec3 v3v0;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final Vec3 v0v1v0;
//		final Vec3 v0v1v1;
//		final Vec3 v1v2v1;
//		final Vec3 v1v2v2;
//		final Vec3 v2v3v2;
//		final Vec3 v2v3v3;
//		final Vec3 v3v0v3;
//		final Vec3 v3v0v0;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final Vec3 v0v1v1v2;
		final Vec3 v1v2v2v3;
		final Vec3 v2v3v3v0;
		final Vec3 v3v0v0v1;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3;
//		final Vec3 v1v2v2v3v2v3v3v0;
//		final Vec3 v2v3v3v0v3v0v0v1;
//		final Vec3 v3v0v0v1v0v1v1v2;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1;
//		final Vec3 v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2;

		try (final ModProfiler ignored = profiler.start("interpolate")) {
			v0 = face.getVertex0();
			v1 = face.getVertex1();
			v2 = face.getVertex2();
			v3 = face.getVertex3();
			v0v1 = interp(v0, v1, 0.5F);
			v1v2 = interp(v1, v2, 0.5F);
			v2v3 = interp(v2, v3, 0.5F);
			v3v0 = interp(v3, v0, 0.5F);
//			v0v1v0 = interp(v0v1, v0, 0.5F);
//			v0v1v1 = interp(v0v1, v1, 0.5F);
//			v1v2v1 = interp(v1v2, v1, 0.5F);
//			v1v2v2 = interp(v1v2, v2, 0.5F);
//			v2v3v2 = interp(v2v3, v2, 0.5F);
//			v2v3v3 = interp(v2v3, v3, 0.5F);
//			v3v0v3 = interp(v3v0, v3, 0.5F);
//			v3v0v0 = interp(v3v0, v0, 0.5F);
			v0v1v1v2 = interp(v0v1, v1v2, 0.5F);
			v1v2v2v3 = interp(v1v2, v2v3, 0.5F);
			v2v3v3v0 = interp(v2v3, v3v0, 0.5F);
			v3v0v0v1 = interp(v3v0, v0v1, 0.5F);
//			v0v1v1v2v1v2v2v3 = interp(v0v1v1v2, v1v2v2v3, 0.5F);
//			v1v2v2v3v2v3v3v0 = interp(v1v2v2v3, v2v3v3v0, 0.5F);
//			v2v3v3v0v3v0v0v1 = interp(v2v3v3v0, v3v0v0v1, 0.5F);
//			v3v0v0v1v0v1v1v2 = interp(v3v0v0v1, v0v1v1v2, 0.5F);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1 = interp(v0v1v1v2v1v2v2v3, v2v3v3v0v3v0v0v1, 0.5F);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2 = interp(v1v2v2v3v2v3v3v0, v3v0v0v1v0v1v1v2, 0.5F);
		}

		//0___3
		//_____
		//_____
		//_____
		//1___2
		final VoxelShape v0box;
		final VoxelShape v1box;
		final VoxelShape v2box;
		final VoxelShape v3box;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final VoxelShape v0v1box;
		final VoxelShape v1v2box;
		final VoxelShape v2v3box;
		final VoxelShape v3v0box;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final VoxelShape v0v1v0box;
//		final VoxelShape v0v1v1box;
//		final VoxelShape v1v2v1box;
//		final VoxelShape v1v2v2box;
//		final VoxelShape v2v3v2box;
//		final VoxelShape v2v3v3box;
//		final VoxelShape v3v0v3box;
//		final VoxelShape v3v0v0box;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final VoxelShape v0v1v1v2box;
		final VoxelShape v1v2v2v3box;
		final VoxelShape v2v3v3v0box;
		final VoxelShape v3v0v0v1box;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final VoxelShape v0v1v1v2v1v2v2v3box;
//		final VoxelShape v1v2v2v3v2v3v3v0box;
//		final VoxelShape v2v3v3v0v3v0v0v1box;
//		final VoxelShape v3v0v0v1v0v1v1v2box;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final VoxelShape v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box;
//		final VoxelShape v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box;

		try (final ModProfiler ignored = profiler.start("createBoxes")) {
			v0box = createVoxelShapeForVertex(v0, boxRadius, originalBoxOffset);
			v1box = createVoxelShapeForVertex(v1, boxRadius, originalBoxOffset);
			v2box = createVoxelShapeForVertex(v2, boxRadius, originalBoxOffset);
			v3box = createVoxelShapeForVertex(v3, boxRadius, originalBoxOffset);
			v0v1box = createVoxelShapeForVertex(v0v1, boxRadius, originalBoxOffset);
			v1v2box = createVoxelShapeForVertex(v1v2, boxRadius, originalBoxOffset);
			v2v3box = createVoxelShapeForVertex(v2v3, boxRadius, originalBoxOffset);
			v3v0box = createVoxelShapeForVertex(v3v0, boxRadius, originalBoxOffset);
//			v0v1v0box = createVoxelShapeForVertex(v0v1v0, boxRadius, originalBoxOffset);
//			v0v1v1box = createVoxelShapeForVertex(v0v1v1, boxRadius, originalBoxOffset);
//			v1v2v1box = createVoxelShapeForVertex(v1v2v1, boxRadius, originalBoxOffset);
//			v1v2v2box = createVoxelShapeForVertex(v1v2v2, boxRadius, originalBoxOffset);
//			v2v3v2box = createVoxelShapeForVertex(v2v3v2, boxRadius, originalBoxOffset);
//			v2v3v3box = createVoxelShapeForVertex(v2v3v3, boxRadius, originalBoxOffset);
//			v3v0v3box = createVoxelShapeForVertex(v3v0v3, boxRadius, originalBoxOffset);
//			v3v0v0box = createVoxelShapeForVertex(v3v0v0, boxRadius, originalBoxOffset);
			v0v1v1v2box = createVoxelShapeForVertex(v0v1v1v2, boxRadius, originalBoxOffset);
			v1v2v2v3box = createVoxelShapeForVertex(v1v2v2v3, boxRadius, originalBoxOffset);
			v2v3v3v0box = createVoxelShapeForVertex(v2v3v3v0, boxRadius, originalBoxOffset);
			v3v0v0v1box = createVoxelShapeForVertex(v3v0v0v1, boxRadius, originalBoxOffset);
//			v0v1v1v2v1v2v2v3box = createVoxelShapeForVertex(v0v1v1v2v1v2v2v3, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0box = createVoxelShapeForVertex(v1v2v2v3v2v3v3v0, boxRadius, originalBoxOffset);
//			v2v3v3v0v3v0v0v1box = createVoxelShapeForVertex(v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v3v0v0v1v0v1v1v2box = createVoxelShapeForVertex(v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box = createVoxelShapeForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box = createVoxelShapeForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
		}

		try (final ModProfiler ignored = profiler.start("addBoxes")) {
			outBoxes.add(v0box);
			outBoxes.add(v1box);
			outBoxes.add(v2box);
			outBoxes.add(v3box);
			outBoxes.add(v0v1box);
			outBoxes.add(v1v2box);
			outBoxes.add(v2v3box);
			outBoxes.add(v3v0box);
//			outBoxes.add(v0v1v0box);
//			outBoxes.add(v0v1v1box);
//			outBoxes.add(v1v2v1box);
//			outBoxes.add(v1v2v2box);
//			outBoxes.add(v2v3v2box);
//			outBoxes.add(v2v3v3box);
//			outBoxes.add(v3v0v3box);
//			outBoxes.add(v3v0v0box);
			outBoxes.add(v0v1v1v2box);
			outBoxes.add(v1v2v2v3box);
			outBoxes.add(v2v3v3v0box);
			outBoxes.add(v3v0v0v1box);
//			outBoxes.add(v0v1v1v2v1v2v2v3box);
//			outBoxes.add(v1v2v2v3v2v3v3v0box);
//			outBoxes.add(v2v3v3v0v3v0v0v1box);
//			outBoxes.add(v3v0v0v1v0v1v1v2box);
//			outBoxes.add(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box);
//			outBoxes.add(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box);
		}

		//DO NOT CLOSE original face vectors
		{
//			v0.close();
//			v1.close();
//			v2.close();
//			v3.close();
		}
		v0v1.close();
		v1v2.close();
		v2v3.close();
		v3v0.close();
//		v0v1v0.close();
//		v0v1v1.close();
//		v1v2v1.close();
//		v1v2v2.close();
//		v2v3v2.close();
//		v2v3v3.close();
//		v3v0v3.close();
//		v3v0v0.close();
		v0v1v1v2.close();
		v1v2v2v3.close();
		v2v3v3v0.close();
		v3v0v0v1.close();
//		v0v1v1v2v1v2v2v3.close();
//		v1v2v2v3v2v3v3v0.close();
//		v2v3v3v0v3v0v0v1.close();
//		v3v0v0v1v0v1v1v2.close();
//		v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1.close();
//		v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2.close();

	}

	private static void addCollisionBoxToList(final List<VoxelShape> collidingBoxes, final VoxelShape box,
	                                          final Predicate<VoxelShape> predicate, final boolean ignoreIntersects) {
		if (ignoreIntersects || predicate.test(box)) {
			collidingBoxes.add(box);
		}
	}

	private static Vec3 interp(final Vec3 v0, final Vec3 v1, final float t) {
		return Vec3.retain(
				v0.x + t * (v1.x - v0.x),
				v0.y + t * (v1.y - v0.y),
				v0.z + t * (v1.z - v0.z)
		);

	}

	private static VoxelShape createVoxelShapeForVertex(final Vec3 vec3, final float boxRadius,
	                                                    @Nullable final VoxelShape originalBox) {

		final double vy = vec3.y;
		final double vx = vec3.x;
		final double vz = vec3.z;

		final boolean originalBoxMaxYGreaterThanVertex = originalBox != null && originalBox.getEnd(EnumFacing.Axis.Y) >= vy;

		return VoxelShapes.create(
				new AxisAlignedBB(
						//min
						vx - boxRadius,
						originalBoxMaxYGreaterThanVertex ? vy - boxRadius - boxRadius : vy - boxRadius,
						vz - boxRadius,
						//max
						vx + boxRadius,
						originalBoxMaxYGreaterThanVertex ? vy : vy + boxRadius,
						vz + boxRadius
				)
		);

	}

}
