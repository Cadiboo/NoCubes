package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
//TODO FIXME use VoxelShapePart instead of tons of VoxelShapes
public final class CollisionHandler {

//	public static Stream<VoxelShape> getCollisionShapes(final IWorldReaderBase iWorldReaderBase, final Entity movingEntity, final VoxelShape area, final VoxelShape entityShape, final boolean isEntityInsideWorldBorder, final int minXm1, final int maxXp1, final int minYm1, final int maxYp1, final int minZm1, final int maxZp1, final WorldBorder worldborder, final boolean isAreaInsideWorldBorder, final VoxelShapePart voxelshapepart, final Predicate<VoxelShape> predicate) {
//
//		if (!Config.terrainCollisions) {
//			return Stream.concat(
//					getCollisionShapesExcludingSmoothable(null, iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, isAreaInsideWorldBorder, voxelshapepart, predicate),
//					Stream.generate(() -> new VoxelShapeInt(voxelshapepart, minXm1, minYm1, minZm1))
//							.limit(1L)
//							.filter(predicate)
//			);
//		}
//
//		if (!shouldApplyMeshCollisions(movingEntity)) {
//			if (!shouldApplyReposeCollisions(movingEntity)) {
//				return Stream.concat(
//						getCollisionShapesExcludingSmoothable(null, iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, isAreaInsideWorldBorder, voxelshapepart, predicate),
//						Stream.generate(() -> new VoxelShapeInt(voxelshapepart, minXm1, minYm1, minZm1))
//								.limit(1L)
//								.filter(predicate)
//				);
//			} else {
//				return getReposeCollisionShapes(iWorldReaderBase, movingEntity, area, entityShape, isEntityInsideWorldBorder, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, isAreaInsideWorldBorder, voxelshapepart, predicate);
//			}
//		}
//
//		// Density calculation needs -1 on all axis
//		final int additionalSizeNegX = 1;
//		final int additionalSizeNegY = 1;
//		final int additionalSizeNegZ = 1;
//
//		final MeshGenerator meshGenerator = Config.terrainMeshGenerator.getMeshGenerator();
//
//		final byte areaSizeX = (byte) (maxXp1 - minXm1);
//		final byte areaSizeY = (byte) (maxYp1 - minYm1);
//		final byte areaSizeZ = (byte) (maxZp1 - minZm1);
//
//		final byte meshSizeX = (byte) (areaSizeX + meshGenerator.getSizeXExtension());
//		final byte meshSizeY = (byte) (areaSizeY + meshGenerator.getSizeYExtension());
//		final byte meshSizeZ = (byte) (areaSizeZ + meshGenerator.getSizeZExtension());
//
//		final int sizeX = meshSizeX + additionalSizeNegX;
//		final int sizeY = meshSizeY + additionalSizeNegY;
//		final int sizeZ = meshSizeZ + additionalSizeNegZ;
//
//		final int startPosX = minXm1 - additionalSizeNegX;
//		final int startPosY = minYm1 - additionalSizeNegY;
//		final int startPosZ = minZm1 - additionalSizeNegZ;
//
//		if (!iWorldReaderBase.isAreaLoaded(
//				startPosX, startPosY, startPosZ,
//				startPosX + sizeX, startPosY + sizeY, startPosZ + sizeZ,
//				true
//		)) {
//			return Stream.concat(
//					getCollisionShapesExcludingSmoothable(null, iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, isAreaInsideWorldBorder, voxelshapepart, predicate),
//					Stream.generate(() -> new VoxelShapeInt(voxelshapepart, minXm1, minYm1, minZm1))
//							.limit(1L)
//							.filter(predicate)
//			);
//		}
//
//		final ModProfiler profiler = ModProfiler.get();
//		try (
//				PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
//				StateCache stateCache = CacheUtil.generateStateCache(
//						startPosX, startPosY, startPosZ,
//						sizeX, sizeY, sizeZ,
//						iWorldReaderBase, pooledMutableBlockPos
//				);
//				SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(stateCache, TERRAIN_SMOOTHABLE);
//				DensityCache densityCache = CacheUtil.generateDensityCache(
//						sizeX - 1, sizeY - 1, sizeZ - 1,
//						0, 0, 0,
//						stateCache, smoothableCache
//				)
//		) {
//
//			final HashMap<Vec3b, FaceList> meshData;
//			try (final ModProfiler ignored = profiler.start("Calculate collisions mesh")) {
//				meshData = meshGenerator.generateChunk(densityCache.getDensityCache(), new byte[]{meshSizeX, meshSizeY, meshSizeZ});
//			}
//
//			try (final ModProfiler ignored = profiler.start("Offset collisions mesh")) {
//				MeshDispatcher.offsetMesh(minXm1, minYm1, minZm1, meshData);
//			}
//
//			try (FaceList finalFaces = FaceList.retain()) {
//
//				try (final ModProfiler ignored = profiler.start("Combine collisions faces")) {
//					for (final FaceList generatedFaceList : meshData.values()) {
//						finalFaces.addAll(generatedFaceList);
//						generatedFaceList.close();
//					}
//					for (final Vec3b vec3b : meshData.keySet()) {
//						vec3b.close();
//					}
//				}
//
//				final List<VoxelShape> collidingShapes = new ArrayList<>();
//
//				for (final Face face : finalFaces) {
//					try (
//							final Vec3 v0 = face.getVertex0();
//							final Vec3 v1 = face.getVertex1();
//							final Vec3 v2 = face.getVertex2();
//							final Vec3 v3 = face.getVertex3()
//					) {
//						final int approximateY;
//						final VoxelShape originalBoxShape;
//
//						try (final ModProfiler ignored = profiler.start("Snap collisions to original")) {
//							// Snap collision VoxelShapes max Y to max Y VoxelShapes of original block at pos if smaller than original
//							// To stop players falling down through the world when they enable collisions
//							// (Only works on flat or near-flat surfaces)
//							//TODO: remove
//							final int approximateX = clamp(floorAvg(v0.x, v1.x, v2.x, v3.x), startPosX, startPosX + sizeX);
//							approximateY = clamp(floorAvg(v0.y - 0.5, v1.y - 0.5, v2.y - 0.5, v3.y - 0.5), startPosY, startPosY + sizeY);
//							final int approximateZ = clamp(floorAvg(v0.z, v1.z, v2.z, v3.z), startPosZ, startPosZ + sizeZ);
//							final IBlockState state = stateCache.getBlockStates()[stateCache.getIndex(
//									approximateX - startPosX,
//									approximateY - startPosY,
//									approximateZ - startPosZ
//							)];
//							originalBoxShape = state.getCollisionShape(iWorldReaderBase, pooledMutableBlockPos.setPos(
//									approximateX, approximateY, approximateZ
//							));
//						}
//						addIntersectingFaceBoxesToList(collidingShapes, face, profiler, approximateY + originalBoxShape.getEnd(Axis.Y), 0.15F, predicate, false);
//					}
//					face.close();
//				}
//
//				return Stream.concat(
//						Stream.concat(
//								getCollisionShapesExcludingSmoothable(TERRAIN_SMOOTHABLE, iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, isAreaInsideWorldBorder, voxelshapepart, predicate),
//								collidingShapes.stream()
//						), Stream.generate(() -> new VoxelShapeInt(voxelshapepart, minXm1, minYm1, minZm1))
//								.limit(1L)
//								.filter(predicate)
//				);
//
//			}
//		}
//	}
//
//	//TODO
//	private static Stream<VoxelShape> getReposeCollisionShapes(final IWorldReaderBase iWorldReaderBase, final Entity movingEntity, final VoxelShape area, final VoxelShape entityShape, final boolean isEntityInsideWorldBorder, final int minXm1, final int maxXp1, final int minYm1, final int maxYp1, final int minZm1, final int maxZp1, final WorldBorder worldborder, final boolean isAreaInsideWorldBorder, final VoxelShapePart voxelshapepart, final Predicate<VoxelShape> predicate) {
//		return Stream.concat(
//				StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(minXm1, minYm1, minZm1, maxXp1 - 1, maxYp1 - 1, maxZp1 - 1).spliterator(), false).map((pos) -> {
//					int k1 = pos.getX();
//					int l1 = pos.getY();
//					int i2 = pos.getZ();
//					boolean flag1 = k1 == minXm1 || k1 == maxXp1 - 1;
//					boolean flag2 = l1 == minYm1 || l1 == maxYp1 - 1;
//					boolean flag3 = i2 == minZm1 || i2 == maxZp1 - 1;
//					if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && iWorldReaderBase.isBlockLoaded(pos)) {
//						final VoxelShape voxelshape;
//						if (isEntityInsideWorldBorder && !isAreaInsideWorldBorder && !worldborder.contains(pos)) {
//							voxelshape = VoxelShapes.fullCube();
//						} else {
//							//Added stuff here
//							final IBlockState blockState = iWorldReaderBase.getBlockState(pos);
//							if (TERRAIN_SMOOTHABLE.apply(blockState)) {
//								voxelshape = StolenReposeCode.getCollisionShape(blockState, iWorldReaderBase, pos);
//							} else {
//								voxelshape = blockState.getCollisionShape(iWorldReaderBase, pos);
//							}
//							//End added stuff
//						}
//
//						VoxelShape voxelshape1 = entityShape.withOffset((double) (-k1), (double) (-l1), (double) (-i2));
//						if (VoxelShapes.compare(voxelshape1, voxelshape, IBooleanFunction.AND)) {
//							return VoxelShapes.empty();
//						} else if (voxelshape == VoxelShapes.fullCube()) {
//							voxelshapepart.setFilled(k1 - minXm1, l1 - minYm1, i2 - minZm1, true, true);
//							return VoxelShapes.empty();
//						} else {
//							return voxelshape.withOffset((double) k1, (double) l1, (double) i2);
//						}
//					} else {
//						return VoxelShapes.empty();
//					}
//				}).filter(predicate),
//				Stream.generate(() -> new VoxelShapeInt(voxelshapepart, minXm1, minYm1, minZm1))
//						.limit(1L)
//						.filter(predicate)
//		);
//	}
//
//	private static int roundAvg(double d0, double d1, double d2, double d3) {
//		return (int) ((Math.round(d0) + Math.round(d1) + Math.round(d2) + Math.round(d3)) / 4D);
//	}
//
//	//hmmm
//	private static int floorAvg(double d0, double d1, double d2, double d3) {
//		return MathHelper.floor((d0 + d1 + d2 + d3) / 4D);
//	}
//
//	//hmmm
//	private static int average(final double d0, final double d1, final double d2, final double d3) {
//		return (int) ((d0 + d1 + d2 + d3) / 4);
//	}
//
//	public static Stream<VoxelShape> getCollisionShapesExcludingSmoothable(@Nullable final IsSmoothable isSmoothable, final IWorldReaderBase iWorldReaderBase, final VoxelShape area, final VoxelShape entityShape, final boolean isEntityInsideWorldBorder, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean isAreaInsideWorldBorder, final VoxelShapePart voxelshapepart, final Predicate<VoxelShape> predicate) {
//		return StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(i, k, i1, j - 1, l - 1, j1 - 1).spliterator(), false).map((pos) -> {
//			int k1 = pos.getX();
//			int l1 = pos.getY();
//			int i2 = pos.getZ();
//			boolean flag1 = k1 == i || k1 == j - 1;
//			boolean flag2 = l1 == k || l1 == l - 1;
//			boolean flag3 = i2 == i1 || i2 == j1 - 1;
//			if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && iWorldReaderBase.isBlockLoaded(pos)) {
//				final VoxelShape voxelshape;
//				if (isEntityInsideWorldBorder && !isAreaInsideWorldBorder && !worldborder.contains(pos)) {
//					voxelshape = VoxelShapes.fullCube();
//				} else {
//					//Added stuff here
//					final IBlockState blockState = iWorldReaderBase.getBlockState(pos);
//					if (isSmoothable != null && (isSmoothable.apply(blockState))) {
//						voxelshape = VoxelShapes.empty();
//					} else {
//						voxelshape = blockState.getCollisionShape(iWorldReaderBase, pos);
//					}
//					//End added stuff
//				}
//
//				VoxelShape voxelshape1 = entityShape.withOffset((double) (-k1), (double) (-l1), (double) (-i2));
//				if (VoxelShapes.compare(voxelshape1, voxelshape, IBooleanFunction.AND)) {
//					return VoxelShapes.empty();
//				} else if (voxelshape == VoxelShapes.fullCube()) {
//					voxelshapepart.setFilled(k1 - i, l1 - k, i2 - i1, true, true);
//					return VoxelShapes.empty();
//				} else {
//					return voxelshape.withOffset((double) k1, (double) l1, (double) i2);
//				}
//			} else {
//				return VoxelShapes.empty();
//			}
//		}).filter(predicate);
//	}

	private static void addIntersectingFaceBoxesToList(
			final List<VoxelShape> outBoxes,
			final Face face,
			final ModProfiler profiler,
			final double maxYLevel,
			final float boxRadius,
			final Predicate<VoxelShape> predicate,
			final boolean ignoreIntersects
	) {

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
//		final VoxelShape v0box;
//		final VoxelShape v1box;
//		final VoxelShape v2box;
//		final VoxelShape v3box;
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
//			v0box = createVoxelShapeForVertex(v0, boxRadius, maxYLevel);
//			v1box = createVoxelShapeForVertex(v1, boxRadius, maxYLevel);
//			v2box = createVoxelShapeForVertex(v2, boxRadius, maxYLevel);
//			v3box = createVoxelShapeForVertex(v3, boxRadius, maxYLevel);
			v0v1box = createVoxelShapeForVertex(v0v1, boxRadius, maxYLevel);
			v1v2box = createVoxelShapeForVertex(v1v2, boxRadius, maxYLevel);
			v2v3box = createVoxelShapeForVertex(v2v3, boxRadius, maxYLevel);
			v3v0box = createVoxelShapeForVertex(v3v0, boxRadius, maxYLevel);
//			v0v1v0box = createVoxelShapeForVertex(v0v1v0, boxRadius, originalBoxOffset);
//			v0v1v1box = createVoxelShapeForVertex(v0v1v1, boxRadius, originalBoxOffset);
//			v1v2v1box = createVoxelShapeForVertex(v1v2v1, boxRadius, originalBoxOffset);
//			v1v2v2box = createVoxelShapeForVertex(v1v2v2, boxRadius, originalBoxOffset);
//			v2v3v2box = createVoxelShapeForVertex(v2v3v2, boxRadius, originalBoxOffset);
//			v2v3v3box = createVoxelShapeForVertex(v2v3v3, boxRadius, originalBoxOffset);
//			v3v0v3box = createVoxelShapeForVertex(v3v0v3, boxRadius, originalBoxOffset);
//			v3v0v0box = createVoxelShapeForVertex(v3v0v0, boxRadius, originalBoxOffset);
			v0v1v1v2box = createVoxelShapeForVertex(v0v1v1v2, boxRadius, maxYLevel);
			v1v2v2v3box = createVoxelShapeForVertex(v1v2v2v3, boxRadius, maxYLevel);
			v2v3v3v0box = createVoxelShapeForVertex(v2v3v3v0, boxRadius, maxYLevel);
			v3v0v0v1box = createVoxelShapeForVertex(v3v0v0v1, boxRadius, maxYLevel);
//			v0v1v1v2v1v2v2v3box = createVoxelShapeForVertex(v0v1v1v2v1v2v2v3, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0box = createVoxelShapeForVertex(v1v2v2v3v2v3v3v0, boxRadius, originalBoxOffset);
//			v2v3v3v0v3v0v0v1box = createVoxelShapeForVertex(v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v3v0v0v1v0v1v1v2box = createVoxelShapeForVertex(v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box = createVoxelShapeForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box = createVoxelShapeForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
		}

		try (final ModProfiler ignored = profiler.start("addBoxes")) {
//			addCollisionBoxToList(outBoxes, v0box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v2box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v3box, predicate, ignoreIntersects);
			addCollisionBoxToList(outBoxes, v0v1box, predicate, ignoreIntersects);
			addCollisionBoxToList(outBoxes, v1v2box, predicate, ignoreIntersects);
			addCollisionBoxToList(outBoxes, v2v3box, predicate, ignoreIntersects);
			addCollisionBoxToList(outBoxes, v3v0box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v0v1v0box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v0v1v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1v2v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1v2v2box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v2v3v2box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v2v3v3box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v3v0v3box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v3v0v0box, predicate, ignoreIntersects);
			addCollisionBoxToList(outBoxes, v0v1v1v2box, predicate, ignoreIntersects);
			addCollisionBoxToList(outBoxes, v1v2v2v3box, predicate, ignoreIntersects);
			addCollisionBoxToList(outBoxes, v2v3v3v0box, predicate, ignoreIntersects);
			addCollisionBoxToList(outBoxes, v3v0v0v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v0v1v1v2v1v2v2v3box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1v2v2v3v2v3v3v0box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v2v3v3v0v3v0v0v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v3v0v0v1v0v1v1v2box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box, predicate, ignoreIntersects);
//			addCollisionBoxToList(outBoxes, v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box, predicate, ignoreIntersects);
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

	private static void addCollisionBoxToList(
			final List<VoxelShape> collidingBoxes,
			final VoxelShape box,
			final Predicate<VoxelShape> predicate,
			final boolean ignoreIntersects
	) {
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

	private static VoxelShape createVoxelShapeForVertex(final Vec3 vec3, final float boxRadius, final double maxY) {

		final double vy = vec3.y;
		final double vx = vec3.x;
		final double vz = vec3.z;

		final boolean isOverMax = vy + boxRadius > maxY;
		return VoxelShapes.create(
				//min
				vx - boxRadius,
				isOverMax ? vy - boxRadius - boxRadius : vy - boxRadius,
				vz - boxRadius,
				//max
				vx + boxRadius,
				isOverMax ? vy : vy + boxRadius,
				vz + boxRadius
		);

	}

	public static boolean shouldApplyMeshCollisions(@Nullable final Entity entity) {
		return entity instanceof PlayerEntity;
	}

	public static boolean shouldApplyReposeCollisions(@Nullable final Entity entity) {
		return entity instanceof ItemEntity || entity instanceof LivingEntity;
	}

}
