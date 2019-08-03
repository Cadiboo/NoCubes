package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.collision.MeshCollisionUtil.VoxelShapeConsumer;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.CacheUtil;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.cache.DensityCache;
import io.github.cadiboo.nocubes.util.pooled.cache.SmoothableCache;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisRotation;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorldReader;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableDouble;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.github.cadiboo.nocubes.collision.CollisionHandler.shouldApplyMeshCollisions;
import static io.github.cadiboo.nocubes.collision.CollisionHandler.shouldApplyReposeCollisions;
import static io.github.cadiboo.nocubes.collision.MeshCollisionUtil.floorAvg;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static net.minecraft.util.math.MathHelper.clamp;
import static net.minecraft.util.math.MathHelper.floor;

/**
 * @author Cadiboo
 */
public final class VoxelShapesHandler {

	// I think that I can use MeshDispatcher.getBlockMesh or whatever
	// And StolenReposeCode.getCollisionShape
	// TODO: Fix later

	/**
	 * FIXME: This is horrible and it doesn't work right but I think its better than nothing
	 */
	public static double getAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, double desiredOffset, final ISelectionContext context, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Axis rotX, final Axis rotY, final Axis rotZ) {
		@Nullable final Entity entity = context.getEntity();
		if (!Config.terrainCollisions) {
			return getVanillaAllowedOffset(collisionBox, worldReader, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
		} else if (shouldApplyMeshCollisions(entity)) {
			return getMeshAllowedOffset(collisionBox, worldReader, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
		} else if (shouldApplyReposeCollisions(entity)) {
			return getReposeAllowedOffset(collisionBox, worldReader, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
		} else {
			return getVanillaAllowedOffset(collisionBox, worldReader, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
		}
	}

	private static double getMeshAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader _this, double desiredOffset, final ISelectionContext context, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Axis rotX, final Axis rotY, final Axis rotZ) {

		final int minXm1 = floor(collisionBox.getMin(rotX) - 0.0000001) - 1;
		final int maxXp1 = floor(collisionBox.getMax(rotX) + 0.0000001) + 1;
		final int minYm1 = floor(collisionBox.getMin(rotY) - 0.0000001) - 1;
		final int maxYp1 = floor(collisionBox.getMax(rotY) + 0.0000001) + 1;
		final int minZm1 = floor(collisionBox.getMin(rotZ) - 0.0000001) - 1;
		final int maxZp1 = floor(collisionBox.getMax(rotZ) + 0.0000001) + 1;
		final double minZ = collisionBox.getMin(rotZ) - 0.0000001;
		final double maxZ = collisionBox.getMax(rotZ) + 0.0000001;
		final boolean over0 = desiredOffset > 0.0D;
		int diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
		final int p1orm1 = over0 ? 1 : -1;

		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			final MeshGenerator meshGenerator = Config.terrainMeshGenerator.getMeshGenerator();

			// DensityCache needs -1 on each NEGATIVE axis
			final int startPosX = minXm1 - 1;
			final int startPosY = minYm1 - 1;
			final int startPosZ = minZm1 - 1;

			// StateCache needs +1 on each POSITIVE axis
			final int endPosX = maxXp1 + 1;
			final int endPosY = maxYp1 + 1;
			final int endPosZ = maxZp1 + 1;

			if (!_this.isAreaLoaded(
					startPosX, startPosY, startPosZ,
					endPosX, endPosY, endPosZ
			)) {
				return getVanillaAllowedOffset(collisionBox, _this, desiredOffset, context, rotationAxis, possibleHits, reversedRotation, rotX, rotY, rotZ);
			}

			final ModProfiler profiler = ModProfiler.get();
			try (
					// DensityCache needs -1 on each NEGATIVE axis
					// StateCache needs +1 on each POSITIVE axis
					// Density calculation needs +1 on ALL axis, 1+1=2
					StateCache stateCache = CacheUtil.generateStateCache(
							startPosX, startPosY, startPosZ,
							endPosX, endPosY, endPosZ,
							1, 1, 1,
							_this, pooledMutableBlockPos
					);
					SmoothableCache smoothableCache = CacheUtil.generateSmoothableCache(
							startPosX, startPosY, startPosZ,
							// StateCache needs +1 on each POSITIVE axis
							endPosX, endPosY, endPosZ,
							1, 1, 1,
							stateCache, TERRAIN_SMOOTHABLE
					);
					DensityCache densityCache = CacheUtil.generateDensityCache(
							startPosX, startPosY, startPosZ,
							// DensityCache needs -1 on each NEGATIVE axis (not +1 on each positive axis as well)
							endPosX - 1, endPosY - 1, endPosZ - 1,
							1, 1, 1,
							stateCache, smoothableCache
					)
			) {

				final float[] densityCacheArray = densityCache.getDensityCache();

				final BlockState[] blockStateArray = stateCache.getBlockStates();

				final int stateOffsetX = stateCache.startPaddingX;
				final int stateOffsetY = stateCache.startPaddingY;
				final int stateOffsetZ = stateCache.startPaddingZ;
				final int stateCacheSizeX = stateCache.sizeX;
				final int stateCacheSizeY = stateCache.sizeY;

				final VoxelShape aabbShape = VoxelShapes.create(collisionBox);

				final MutableDouble desiredOffsetMutable = new MutableDouble();
				final MutableBoolean shouldReturn = new MutableBoolean();
				final VoxelShapeConsumer voxelShapeConsumer = new VoxelShapeConsumer() {
					@Override
					void accept(final List<VoxelShape> outShapes, final Vec3 v, final VoxelShape shape, final Predicate<VoxelShape> doesShapeIntersect, final boolean ignoreIntersects) {
						if (shouldReturn.getValue()) {
							return;
						}
						final double allowedOffset = shape.getAllowedOffset(rotZ, collisionBox.offset(-v.x, -v.y, -v.z), desiredOffsetMutable.getValue());
						desiredOffsetMutable.setValue(allowedOffset);
						if (Math.abs(allowedOffset) < 0.0000001) {
							shouldReturn.setTrue();
						}
					}
				};

				// Get vanilla collisions (taking density into account)
				{
					final int sizeX = maxXp1 - minXm1;
					final int sizeY = maxYp1 - minYm1;
					final int sizeZ = maxZp1 - minZm1;

					final int densityOffsetX = densityCache.startPaddingX;
					final int densityOffsetY = densityCache.startPaddingY;
					final int densityOffsetZ = densityCache.startPaddingZ;
					final int densityCacheSizeX = densityCache.sizeX;
					final int densityCacheSizeY = densityCache.sizeY;

					for (int z = 0; z < sizeZ; ++z) {
						for (int y = 0; y < sizeY; ++y) {
							for (int x = 0; x < sizeX; ++x) {
								if (over0) {
									if (z > diffFloored) {
										break;
									}
								} else if (z < diffFloored) {
									break;
								}

								final BlockState blockState = blockStateArray[stateCache.getIndex(
										stateOffsetX + x,
										stateOffsetY + y,
										stateOffsetZ + z,
										stateCacheSizeX, stateCacheSizeY
								)];
								if (blockState.nocubes_isTerrainSmoothable) {

									final int posX = minXm1 + x;
									final int posY = minYm1 + y;
									final int posZ = minZm1 + z;
									pooledMutableBlockPos.setPos(posX, posY, posZ);

									final FaceList faces = MeshDispatcher.generateBlockMeshOffset(pooledMutableBlockPos, _this, TERRAIN_SMOOTHABLE, Config.terrainMeshGenerator);
									for (int i = 0, finalFacesSize = faces.size(); i < finalFacesSize; ++i) {
										try (
												final Face face = faces.get(i);
												final Vec3 v0 = face.getVertex0();
												final Vec3 v1 = face.getVertex1();
												final Vec3 v2 = face.getVertex2();
												final Vec3 v3 = face.getVertex3()
										) {
											final double maxY;
											try (ModProfiler ignored = profiler.start("Snap collisions to original")) {
												// Snap collision VoxelShapes max Y to max Y VoxelShapes of original block at pos if smaller than original
												// To stop players falling down through the world when they enable collisions
												// (Only works on flat or near-flat surfaces)
												// TODO: remove
												final int approximateX = clamp(floorAvg(v0.x, v1.x, v2.x, v3.x), startPosX, endPosX);
												final int approximateY = clamp(floorAvg(v0.y - 0.5, v1.y - 0.5, v2.y - 0.5, v3.y - 0.5), startPosY, endPosY);
												final int approximateZ = clamp(floorAvg(v0.z, v1.z, v2.z, v3.z), startPosZ, endPosZ);
												final BlockState state = blockStateArray[stateCache.getIndex(
														approximateX - startPosX,
														approximateY - startPosY,
														approximateZ - startPosZ,
														stateCacheSizeX, stateCacheSizeY
												)];
												final VoxelShape originalCollisionShape = state.getCollisionShape(_this, pooledMutableBlockPos.setPos(
														approximateX, approximateY, approximateZ
												), context);
												maxY = approximateY + originalCollisionShape.getEnd(Axis.Y);
											}
											MeshCollisionUtil.doWithEachCollisionShape(null, face, profiler, maxY, 0.15F, checkShape -> VoxelShapes.compare(aabbShape, checkShape, IBooleanFunction.AND), false, voxelShapeConsumer);
											if (shouldReturn.booleanValue()) {
												return 0;
											}
										}
									}
								}
								// if not smoothable or inside terrain
								else if (
										densityCacheArray[densityCache.getIndex(
												densityOffsetX + x,
												densityOffsetY + y,
												densityOffsetZ + z,
												densityCacheSizeX, densityCacheSizeY
										)] < -6 // -6 is very likely to be inside the isosurface (-8 is entirely inside)
								) {
									final int posX = minXm1 + x;
									final int posY = minYm1 + y;
									final int posZ = minZm1 + z;
									pooledMutableBlockPos.setPos(posX, posY, posZ);

									desiredOffset = blockState.getCollisionShape(_this, pooledMutableBlockPos, context).getAllowedOffset(rotZ, collisionBox.offset(-posX, -posY, -posZ), desiredOffset);
									if (Math.abs(desiredOffset) < 0.0000001) {
										return 0.0D;
									}
									desiredOffsetMutable.setValue(desiredOffset);
									diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);

								}
							}
						}
					}
				}

				return desiredOffsetMutable.getValue();
			}
		}
	}

	private static double getVanillaAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, double desiredOffset, final ISelectionContext selectionContext, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Axis rotX, final Axis rotY, final Axis rotZ) {
		try (PooledMutableBlockPos pos = PooledMutableBlockPos.retain()) {
			final int minXm1 = floor(collisionBox.getMin(rotX) - 0.0000001) - 1;
			final int maxXp1 = floor(collisionBox.getMax(rotX) + 0.0000001) + 1;
			final int minYm1 = floor(collisionBox.getMin(rotY) - 0.0000001) - 1;
			final int maxYp1 = floor(collisionBox.getMax(rotY) + 0.0000001) + 1;
			final double minZ = collisionBox.getMin(rotZ) - 0.0000001;
			final double maxZ = collisionBox.getMax(rotZ) + 0.0000001;
			final boolean over0 = desiredOffset > 0.0D;
			final int initialZ = over0 ? floor(collisionBox.getMax(rotZ) - 0.0000001) - 1 : floor(collisionBox.getMin(rotZ) + 0.0000001) + 1;
			int diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
			final int p1orm1 = over0 ? 1 : -1;
			int z = initialZ;
			while (true) {
				if (over0) {
					if (z > diffFloored) {
						break;
					}
				} else if (z < diffFloored) {
					break;
				}
				for (int x = minXm1; x <= maxXp1; ++x) {
					for (int y = minYm1; y <= maxYp1; ++y) {
						int flag = 0;
						if (x == minXm1 || x == maxXp1) {
							++flag;
						}
						if (y == minYm1 || y == maxYp1) {
							++flag;
						}
						if (z == initialZ || z == diffFloored) {
							++flag;
						}
						if (flag < 3) {
							pos.func_218295_a(reversedRotation, x, y, z);
							BlockState blockstate = worldReader.getBlockState(pos);
							// func_215704_f -> isCollisionShapeLargerThanFullBlock
							if (flag != 1 || blockstate.func_215704_f()) {
								if (flag != 2 || blockstate.getBlock() == Blocks.MOVING_PISTON) {
									desiredOffset = blockstate.getCollisionShape(worldReader, pos, selectionContext).getAllowedOffset(rotZ, collisionBox.offset(-pos.getX(), -pos.getY(), -pos.getZ()), desiredOffset);
									if (Math.abs(desiredOffset) < 0.0000001) {
										return 0.0D;
									}
									diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
								}
							}
						}
					}
				}
				z += p1orm1;
			}
		}
		double[] adouble = new double[]{desiredOffset};
		possibleHits.forEach((shape) -> adouble[0] = shape.getAllowedOffset(rotZ, collisionBox, adouble[0]));
		return adouble[0];
	}

	private static double getReposeAllowedOffset(final AxisAlignedBB collisionBox, final IWorldReader worldReader, double desiredOffset, final ISelectionContext selectionContext, final AxisRotation rotationAxis, final Stream<VoxelShape> possibleHits, final AxisRotation reversedRotation, final Axis rotX, final Axis rotY, final Axis rotZ) {
		try (PooledMutableBlockPos pos = PooledMutableBlockPos.retain()) {
			final int minXm1 = floor(collisionBox.getMin(rotX) - 0.0000001) - 1;
			final int maxXp1 = floor(collisionBox.getMax(rotX) + 0.0000001) + 1;
			final int minYm1 = floor(collisionBox.getMin(rotY) - 0.0000001) - 1;
			final int maxYp1 = floor(collisionBox.getMax(rotY) + 0.0000001) + 1;
			final double minZ = collisionBox.getMin(rotZ) - 0.0000001;
			final double maxZ = collisionBox.getMax(rotZ) + 0.0000001;
			final boolean over0 = desiredOffset > 0.0D;
			final int initialZ = over0 ? floor(collisionBox.getMax(rotZ) - 0.0000001) - 1 : floor(collisionBox.getMin(rotZ) + 0.0000001) + 1;
			int diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
			final int p1orm1 = over0 ? 1 : -1;
			int z = initialZ;
			while (true) {
				if (over0) {
					if (z > diffFloored) {
						break;
					}
				} else if (z < diffFloored) {
					break;
				}
				for (int x = minXm1; x <= maxXp1; ++x) {
					for (int y = minYm1; y <= maxYp1; ++y) {
						int flag = 0;
						if (x == minXm1 || x == maxXp1) {
							++flag;
						}
						if (y == minYm1 || y == maxYp1) {
							++flag;
						}
						if (z == initialZ || z == diffFloored) {
							++flag;
						}
						if (flag < 3) {
							pos.func_218295_a(reversedRotation, x, y, z);
							BlockState blockstate = worldReader.getBlockState(pos);
							// func_215704_f -> isCollisionShapeLargerThanFullBlock
							if (flag != 1 || blockstate.func_215704_f()) {
								if (flag != 2 || blockstate.getBlock() == Blocks.MOVING_PISTON) {
									desiredOffset = StolenReposeCode.getCollisionShape(blockstate, worldReader, pos, selectionContext).getAllowedOffset(rotZ, collisionBox/*.offset(-pos.getX(), -pos.getY(), -pos.getZ())*/, desiredOffset);
									if (Math.abs(desiredOffset) < 0.0000001) {
										return 0.0D;
									}
									diffFloored = getDifferenceFloored(desiredOffset, minZ, maxZ);
								}
							}
						}
					}
				}
				z += p1orm1;
			}
		}
		double[] adouble = new double[]{desiredOffset};
		possibleHits.forEach((shape) -> adouble[0] = shape.getAllowedOffset(rotZ, collisionBox, adouble[0]));
		return adouble[0];
	}

	/**
	 * Copy of VoxelShapes.getDifferenceFloored
	 * TODO: AT
	 */
	private static int getDifferenceFloored(final double desiredOffset, final double minZ, final double maxZ) {
		if (desiredOffset > 0.0D) {
			return floor(maxZ + desiredOffset) + 1;
		} else {
			return floor(minZ + desiredOffset) - 1;
		}
	}

}
