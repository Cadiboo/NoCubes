package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static net.minecraft.util.math.MathHelper.clamp;

/**
 * @author Cadiboo
 */
//TODO FIXME use AxisAlignedBBPart instead of tons of AxisAlignedBBs
public final class CollisionHandler {

//	public static Stream<AxisAlignedBB> getCollisionShapes(final World iWorldReaderBase, final Entity movingEntity, final AxisAlignedBB area, final AxisAlignedBB entityShape, final boolean isEntityInsideWorldBorder, final int minXm1, final int maxXp1, final int minYm1, final int maxYp1, final int minZm1, final int maxZp1, final WorldBorder worldborder, final boolean isAreaInsideWorldBorder, final AxisAlignedBBPart voxelshapepart, final Predicate<AxisAlignedBB> predicate) {
//
//		if (!Config.terrainCollisions) {
//			return Stream.concat(
//					getCollisionShapesExcludingSmoothable(null, iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, isAreaInsideWorldBorder, voxelshapepart, predicate),
//					Stream.generate(() -> new AxisAlignedBBInt(voxelshapepart, minXm1, minYm1, minZm1))
//							.limit(1L)
//							.filter(predicate)
//			);
//		}
//
//		if (!shouldApplyMeshCollisions(movingEntity)) {
//			if (!shouldApplyReposeCollisions(movingEntity)) {
//				return Stream.concat(
//						getCollisionShapesExcludingSmoothable(null, iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, isAreaInsideWorldBorder, voxelshapepart, predicate),
//						Stream.generate(() -> new AxisAlignedBBInt(voxelshapepart, minXm1, minYm1, minZm1))
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
//					Stream.generate(() -> new AxisAlignedBBInt(voxelshapepart, minXm1, minYm1, minZm1))
//							.limit(1L)
//							.filter(predicate)
//			);
//		}
//
//		final ModProfiler profiler = ModProfiler.get();
//		PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
//		try (
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
//				final List<AxisAlignedBB> collidingShapes = new ArrayList<>();
//
//				for (final Face face : finalFaces) {
//					try (
//							final Vec3 v0 = face.getVertex0();
//							final Vec3 v1 = face.getVertex1();
//							final Vec3 v2 = face.getVertex2();
//							final Vec3 v3 = face.getVertex3()
//					) {
//						final int approximateY;
//						final AxisAlignedBB originalBoxShape;
//
//						try (final ModProfiler ignored = profiler.start("Snap collisions to original")) {
//							// Snap collision AxisAlignedBBs max Y to max Y AxisAlignedBBs of original block at pos if smaller than original
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
//						), Stream.generate(() -> new AxisAlignedBBInt(voxelshapepart, minXm1, minYm1, minZm1))
//								.limit(1L)
//								.filter(predicate)
//				);
//
//			}
//		} finally {
//			pooledMutableBlockPos.release();
//		}
//	}

//	//TODO
//	private static Stream<AxisAlignedBB> getReposeCollisionShapes(final IWorldReaderBase iWorldReaderBase, final Entity movingEntity, final AxisAlignedBB area, final AxisAlignedBB entityShape, final boolean isEntityInsideWorldBorder, final int minXm1, final int maxXp1, final int minYm1, final int maxYp1, final int minZm1, final int maxZp1, final WorldBorder worldborder, final boolean isAreaInsideWorldBorder, final AxisAlignedBBPart voxelshapepart, final Predicate<AxisAlignedBB> predicate) {
//		return Stream.concat(
//				StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(minXm1, minYm1, minZm1, maxXp1 - 1, maxYp1 - 1, maxZp1 - 1).spliterator(), false).map((pos) -> {
//					int k1 = pos.getX();
//					int l1 = pos.getY();
//					int i2 = pos.getZ();
//					boolean flag1 = k1 == minXm1 || k1 == maxXp1 - 1;
//					boolean flag2 = l1 == minYm1 || l1 == maxYp1 - 1;
//					boolean flag3 = i2 == minZm1 || i2 == maxZp1 - 1;
//					if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && iWorldReaderBase.isBlockLoaded(pos)) {
//						final AxisAlignedBB voxelshape;
//						if (isEntityInsideWorldBorder && !isAreaInsideWorldBorder && !worldborder.contains(pos)) {
//							voxelshape = AxisAlignedBBs.fullCube();
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
//						AxisAlignedBB voxelshape1 = entityShape.withOffset((double) (-k1), (double) (-l1), (double) (-i2));
//						if (AxisAlignedBBs.compare(voxelshape1, voxelshape, IBooleanFunction.AND)) {
//							return AxisAlignedBBs.empty();
//						} else if (voxelshape == AxisAlignedBBs.fullCube()) {
//							voxelshapepart.setFilled(k1 - minXm1, l1 - minYm1, i2 - minZm1, true, true);
//							return AxisAlignedBBs.empty();
//						} else {
//							return voxelshape.withOffset((double) k1, (double) l1, (double) i2);
//						}
//					} else {
//						return AxisAlignedBBs.empty();
//					}
//				}).filter(predicate),
//				Stream.generate(() -> new AxisAlignedBBInt(voxelshapepart, minXm1, minYm1, minZm1))
//						.limit(1L)
//						.filter(predicate)
//		);
//	}

	private static int roundAvg(double d0, double d1, double d2, double d3) {
		return (int) ((Math.round(d0) + Math.round(d1) + Math.round(d2) + Math.round(d3)) / 4D);
	}

	//hmmm
	private static int floorAvg(double d0, double d1, double d2, double d3) {
		return MathHelper.floor((d0 + d1 + d2 + d3) / 4D);
	}

	//hmmm
	private static int average(final double d0, final double d1, final double d2, final double d3) {
		return (int) ((d0 + d1 + d2 + d3) / 4);
	}

//	public static Stream<AxisAlignedBB> getCollisionShapesExcludingSmoothable(@Nullable final IsSmoothable isSmoothable, final IWorldReaderBase iWorldReaderBase, final AxisAlignedBB area, final AxisAlignedBB entityShape, final boolean isEntityInsideWorldBorder, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean isAreaInsideWorldBorder, final AxisAlignedBBPart voxelshapepart, final Predicate<AxisAlignedBB> predicate) {
//		return StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(i, k, i1, j - 1, l - 1, j1 - 1).spliterator(), false).map((pos) -> {
//			int k1 = pos.getX();
//			int l1 = pos.getY();
//			int i2 = pos.getZ();
//			boolean flag1 = k1 == i || k1 == j - 1;
//			boolean flag2 = l1 == k || l1 == l - 1;
//			boolean flag3 = i2 == i1 || i2 == j1 - 1;
//			if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && iWorldReaderBase.isBlockLoaded(pos)) {
//				final AxisAlignedBB voxelshape;
//				if (isEntityInsideWorldBorder && !isAreaInsideWorldBorder && !worldborder.contains(pos)) {
//					voxelshape = AxisAlignedBBs.fullCube();
//				} else {
//					//Added stuff here
//					final IBlockState blockState = iWorldReaderBase.getBlockState(pos);
//					if (isSmoothable != null && (isSmoothable.apply(blockState))) {
//						voxelshape = AxisAlignedBBs.empty();
//					} else {
//						voxelshape = blockState.getCollisionShape(iWorldReaderBase, pos);
//					}
//					//End added stuff
//				}
//
//				AxisAlignedBB voxelshape1 = entityShape.withOffset((double) (-k1), (double) (-l1), (double) (-i2));
//				if (AxisAlignedBBs.compare(voxelshape1, voxelshape, IBooleanFunction.AND)) {
//					return AxisAlignedBBs.empty();
//				} else if (voxelshape == AxisAlignedBBs.fullCube()) {
//					voxelshapepart.setFilled(k1 - i, l1 - k, i2 - i1, true, true);
//					return AxisAlignedBBs.empty();
//				} else {
//					return voxelshape.withOffset((double) k1, (double) l1, (double) i2);
//				}
//			} else {
//				return AxisAlignedBBs.empty();
//			}
//		}).filter(predicate);
//	}

	private static void addIntersectingFaceBoxesToList(
			final List<AxisAlignedBB> outBoxes,
			final Face face,
			final ModProfiler profiler,
			final double maxYLevel,
			final float boxRadius,
			final Predicate<AxisAlignedBB> predicate,
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
//		final AxisAlignedBB v0box;
//		final AxisAlignedBB v1box;
//		final AxisAlignedBB v2box;
//		final AxisAlignedBB v3box;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final AxisAlignedBB v0v1box;
		final AxisAlignedBB v1v2box;
		final AxisAlignedBB v2v3box;
		final AxisAlignedBB v3v0box;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final AxisAlignedBB v0v1v0box;
//		final AxisAlignedBB v0v1v1box;
//		final AxisAlignedBB v1v2v1box;
//		final AxisAlignedBB v1v2v2box;
//		final AxisAlignedBB v2v3v2box;
//		final AxisAlignedBB v2v3v3box;
//		final AxisAlignedBB v3v0v3box;
//		final AxisAlignedBB v3v0v0box;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final AxisAlignedBB v0v1v1v2box;
		final AxisAlignedBB v1v2v2v3box;
		final AxisAlignedBB v2v3v3v0box;
		final AxisAlignedBB v3v0v0v1box;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final AxisAlignedBB v0v1v1v2v1v2v2v3box;
//		final AxisAlignedBB v1v2v2v3v2v3v3v0box;
//		final AxisAlignedBB v2v3v3v0v3v0v0v1box;
//		final AxisAlignedBB v3v0v0v1v0v1v1v2box;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final AxisAlignedBB v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box;
//		final AxisAlignedBB v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box;

		try (final ModProfiler ignored = profiler.start("createBoxes")) {
//			v0box = createAxisAlignedBBForVertex(v0, boxRadius, maxYLevel);
//			v1box = createAxisAlignedBBForVertex(v1, boxRadius, maxYLevel);
//			v2box = createAxisAlignedBBForVertex(v2, boxRadius, maxYLevel);
//			v3box = createAxisAlignedBBForVertex(v3, boxRadius, maxYLevel);
			v0v1box = createAxisAlignedBBForVertex(v0v1, boxRadius, maxYLevel);
			v1v2box = createAxisAlignedBBForVertex(v1v2, boxRadius, maxYLevel);
			v2v3box = createAxisAlignedBBForVertex(v2v3, boxRadius, maxYLevel);
			v3v0box = createAxisAlignedBBForVertex(v3v0, boxRadius, maxYLevel);
//			v0v1v0box = createAxisAlignedBBForVertex(v0v1v0, boxRadius, originalBoxOffset);
//			v0v1v1box = createAxisAlignedBBForVertex(v0v1v1, boxRadius, originalBoxOffset);
//			v1v2v1box = createAxisAlignedBBForVertex(v1v2v1, boxRadius, originalBoxOffset);
//			v1v2v2box = createAxisAlignedBBForVertex(v1v2v2, boxRadius, originalBoxOffset);
//			v2v3v2box = createAxisAlignedBBForVertex(v2v3v2, boxRadius, originalBoxOffset);
//			v2v3v3box = createAxisAlignedBBForVertex(v2v3v3, boxRadius, originalBoxOffset);
//			v3v0v3box = createAxisAlignedBBForVertex(v3v0v3, boxRadius, originalBoxOffset);
//			v3v0v0box = createAxisAlignedBBForVertex(v3v0v0, boxRadius, originalBoxOffset);
			v0v1v1v2box = createAxisAlignedBBForVertex(v0v1v1v2, boxRadius, maxYLevel);
			v1v2v2v3box = createAxisAlignedBBForVertex(v1v2v2v3, boxRadius, maxYLevel);
			v2v3v3v0box = createAxisAlignedBBForVertex(v2v3v3v0, boxRadius, maxYLevel);
			v3v0v0v1box = createAxisAlignedBBForVertex(v3v0v0v1, boxRadius, maxYLevel);
//			v0v1v1v2v1v2v2v3box = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0box = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0, boxRadius, originalBoxOffset);
//			v2v3v3v0v3v0v0v1box = createAxisAlignedBBForVertex(v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v3v0v0v1v0v1v1v2box = createAxisAlignedBBForVertex(v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, boxRadius, originalBoxOffset);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, boxRadius, originalBoxOffset);
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
			final List<AxisAlignedBB> collidingBoxes,
			final AxisAlignedBB box,
			final Predicate<AxisAlignedBB> predicate,
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

	private static AxisAlignedBB createAxisAlignedBBForVertex(final Vec3 vec3, final float boxRadius, final double maxY) {

		final double vy = vec3.y;
		final double vx = vec3.x;
		final double vz = vec3.z;

		final boolean isOverMax = vy + boxRadius > maxY;
		return new AxisAlignedBB(
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
		return entity instanceof EntityPlayer;
	}

	public static boolean shouldApplyReposeCollisions(@Nullable final Entity entity) {
		return entity instanceof EntityItem || entity instanceof EntityLivingBase;
	}

	public static boolean getCollisionBoxes(final World world, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final boolean flag1) {

		if (!Config.terrainCollisions) {
			throw new IllegalStateException("Terrain collisions diabled");
		}

		if (shouldApplyMeshCollisions(entityIn)) {
			return getMeshCollisions(world, entityIn, aabb, p_191504_3_, outList, i, j, k, l, i1, j1, worldborder, flag, flag1);
		} else if (shouldApplyReposeCollisions(entityIn)) {
			return getReposeCollisions(world, entityIn, aabb, p_191504_3_, outList, i, j, k, l, i1, j1, worldborder, flag, flag1);
		} else {
			throw new IllegalStateException("ShouldNotReachHere");
		}

	}

	private static boolean getReposeCollisions(final World _this, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int startX, final int j, final int k, final int l, final int startZ, final int maxZ, final WorldBorder worldborder, final boolean flag, final boolean flag1) {
		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
			if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList))
				return true;
			for (int posX = startX; posX < j; ++posX) {
				for (int posZ = startZ; posZ < maxZ; ++posZ) {
					boolean flag2 = posX == startX || posX == j - 1;
					boolean flag3 = posZ == startZ || posZ == maxZ - 1;

					if ((!flag2 || !flag3) && _this.isBlockLoaded(pooledMutableBlockPos.setPos(posX, 64, posZ))) {
						for (int i2 = k; i2 < l; ++i2) {
							if (!flag2 && !flag3 || i2 != l - 1) {
								if (p_191504_3_) {
									if (posX < -30000000 || posX >= 30000000 || posZ < -30000000 || posZ >= 30000000) {
										return true;
									}
								} else if (entityIn != null && flag == flag1) {
									entityIn.setOutsideBorder(!flag1);
								}

								pooledMutableBlockPos.setPos(posX, i2, posZ);
								final IBlockState state;

								if (!p_191504_3_ && !worldborder.contains(pooledMutableBlockPos) && flag1) {
									state = Blocks.STONE.getDefaultState();
								} else {
									state = _this.getBlockState(pooledMutableBlockPos);
								}

								if (TERRAIN_SMOOTHABLE.apply(state)) {
									StolenReposeCode.addCollisionBoxToList(state, _this, pooledMutableBlockPos, aabb, outList, entityIn, false);
								} else {
									state.addCollisionBoxToList(_this, pooledMutableBlockPos, aabb, outList, entityIn, false);
								}

								if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList)) {
									return true;
								}
							}
						}
					}
				}
			}
		} finally {
			pooledMutableBlockPos.release();
		}
		return !outList.isEmpty();
	}

	private static boolean getMeshCollisions(final World _this, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int minXm1, final int maxXp1, final int minYm1, final int maxYp1, final int minZm1, final int maxZp1, final WorldBorder worldborder, final boolean flag, final boolean flag1) {
		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {

//			int i = MathHelper.floor(aabb.minX) - 1;
//			int j = MathHelper.ceil(aabb.maxX) + 1;
//			int k = MathHelper.floor(aabb.minY) - 1;
//			int l = MathHelper.ceil(aabb.maxY) + 1;
//			int i1 = MathHelper.floor(aabb.minZ) - 1;
//			int j1 = MathHelper.ceil(aabb.maxZ) + 1;

			// Density calculation needs -1 on all axis
			final int additionalSizeNegX = 1;
			final int additionalSizeNegY = 1;
			final int additionalSizeNegZ = 1;

			final MeshGenerator meshGenerator = Config.terrainMeshGenerator.getMeshGenerator();

			final byte areaSizeX = (byte) (maxXp1 - minXm1);
			final byte areaSizeY = (byte) (maxYp1 - minYm1);
			final byte areaSizeZ = (byte) (maxZp1 - minZm1);

			final byte meshSizeX = (byte) (areaSizeX + meshGenerator.getSizeXExtension());
			final byte meshSizeY = (byte) (areaSizeY + meshGenerator.getSizeYExtension());
			final byte meshSizeZ = (byte) (areaSizeZ + meshGenerator.getSizeZExtension());

			final int sizeX = meshSizeX + additionalSizeNegX;
			final int sizeY = meshSizeY + additionalSizeNegY;
			final int sizeZ = meshSizeZ + additionalSizeNegZ;

			final int startPosX = minXm1 - additionalSizeNegX;
			final int startPosY = minYm1 - additionalSizeNegY;
			final int startPosZ = minZm1 - additionalSizeNegZ;

			if (!_this.isAreaLoaded(
					new StructureBoundingBox(
							startPosX, startPosY, startPosZ,
							startPosX + sizeX, startPosY + sizeY, startPosZ + sizeZ
					),
					true
			)) {
				throw new RuntimeException("area not loaded");
			}

			final ModProfiler profiler = ModProfiler.get();
			try (
					StateCache stateCache = CacheUtil.generateStateCache(
							startPosX, startPosY, startPosZ,
							sizeX, sizeY, sizeZ,
							_this, pooledMutableBlockPos
					);
					SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(stateCache, TERRAIN_SMOOTHABLE);
					DensityCache densityCache = CacheUtil.generateDensityCache(
							sizeX - 1, sizeY - 1, sizeZ - 1,
							0, 0, 0,
							stateCache, smoothableCache
					)
			) {

				final HashMap<Vec3b, FaceList> meshData;
				try (final ModProfiler ignored = profiler.start("Calculate collisions mesh")) {
					meshData = meshGenerator.generateChunk(densityCache.getDensityCache(), new byte[]{meshSizeX, meshSizeY, meshSizeZ});
				}

				try (final ModProfiler ignored = profiler.start("Offset collisions mesh")) {
					MeshDispatcher.offsetMesh(minXm1, minYm1, minZm1, meshData);
				}

				try (FaceList finalFaces = FaceList.retain()) {

					try (final ModProfiler ignored = profiler.start("Combine collisions faces")) {
						for (final FaceList generatedFaceList : meshData.values()) {
							finalFaces.addAll(generatedFaceList);
							generatedFaceList.close();
						}
						for (final Vec3b vec3b : meshData.keySet()) {
							vec3b.close();
						}
					}

					final List<AxisAlignedBB> collidingShapes = new ArrayList<>();

					for (final Face face : finalFaces) {
						try (
								final Vec3 v0 = face.getVertex0();
								final Vec3 v1 = face.getVertex1();
								final Vec3 v2 = face.getVertex2();
								final Vec3 v3 = face.getVertex3()
						) {
							final int approximateY;
							AxisAlignedBB originalBoxShape;

							try (final ModProfiler ignored = profiler.start("Snap collisions to original")) {
								// Snap collision AxisAlignedBBs max Y to max Y AxisAlignedBBs of original block at pos if smaller than original
								// To stop players falling down through the world when they enable collisions
								// (Only works on flat or near-flat surfaces)
								//TODO: remove
								final int approximateX = clamp(floorAvg(v0.x, v1.x, v2.x, v3.x), startPosX, startPosX + sizeX);
								approximateY = clamp(floorAvg(v0.y - 0.5, v1.y - 0.5, v2.y - 0.5, v3.y - 0.5), startPosY, startPosY + sizeY);
								final int approximateZ = clamp(floorAvg(v0.z, v1.z, v2.z, v3.z), startPosZ, startPosZ + sizeZ);
								final IBlockState state = stateCache.getBlockStates()[stateCache.getIndex(
										approximateX - startPosX,
										approximateY - startPosY,
										approximateZ - startPosZ
								)];
								originalBoxShape = state.getCollisionBoundingBox(_this, pooledMutableBlockPos.setPos(
										approximateX, approximateY, approximateZ
								));
								originalBoxShape = originalBoxShape == null ? null : originalBoxShape.offset(pooledMutableBlockPos);
							}
							addIntersectingFaceBoxesToList(collidingShapes, face, profiler, originalBoxShape == null ? 0 : originalBoxShape.maxY, 0.15F, aabb::intersects, false);
						}
						face.close();
					}

					outList.addAll(collidingShapes);

					if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList))
						return true;
					for (int posX = minXm1; posX < maxXp1; ++posX) {
						for (int posZ = minZm1; posZ < maxZp1; ++posZ) {
							boolean flag2 = posX == minXm1 || posX == maxXp1 - 1;
							boolean flag3 = posZ == minZm1 || posZ == maxZp1 - 1;

							if ((!flag2 || !flag3) && _this.isBlockLoaded(pooledMutableBlockPos.setPos(posX, 64, posZ))) {
								for (int posY = minYm1; posY < maxYp1; ++posY) {
									if (!flag2 && !flag3 || posY != maxYp1 - 1) {
										if (p_191504_3_) {
											if (posX < -30000000 || posX >= 30000000 || posZ < -30000000 || posZ >= 30000000) {
												return true;
											}
										} else if (entityIn != null && flag == flag1) {
											entityIn.setOutsideBorder(!flag1);
										}

										pooledMutableBlockPos.setPos(posX, posY, posZ);
										final IBlockState state;

										if (!p_191504_3_ && !worldborder.contains(pooledMutableBlockPos) && flag1) {
											state = Blocks.STONE.getDefaultState();
										} else {
											state = _this.getBlockState(pooledMutableBlockPos);
										}

										if (!state.nocubes_isTerrainSmoothable()) {
											state.addCollisionBoxToList(_this, pooledMutableBlockPos, aabb, outList, entityIn, false);
										}

										if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList)) {
											return true;
										}
									}
								}
							}
						}
					}

				}
			}
		} finally {
			pooledMutableBlockPos.release();
		}
		return !outList.isEmpty();
	}

}
