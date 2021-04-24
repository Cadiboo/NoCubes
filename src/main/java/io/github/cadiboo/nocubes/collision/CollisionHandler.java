package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.generator.SurfaceNets;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public final class CollisionHandler {

//	private static void generateShape(Vec centre, Vec averageOfNormal, IShapeConsumer consumer, Vec v) {
//		consumer.accept(
//			v.x, v.y, v.z,
//			centre.x + averageOfNormal.x, centre.y + averageOfNormal.y, centre.z + averageOfNormal.z
//		);
//	}

	private static AxisAlignedBB generateShape(Vec centre, Vec averageOfNormal, Vec v) {
		return new AxisAlignedBB(
			v.x, v.y, v.z,
			centre.x + averageOfNormal.x, centre.y + averageOfNormal.y, centre.z + averageOfNormal.z
		);
	}

	private static boolean shouldApplyMeshCollisions(@Nullable final Entity entity) {
		return entity instanceof EntityPlayer;
	}

	private static boolean shouldApplyReposeCollisions(@Nullable final Entity entity) {
		return entity instanceof EntityItem || entity instanceof EntityLivingBase;
	}

	public static boolean getCollisionBoxes(
		final World _this,
		final Entity entityIn,
		final AxisAlignedBB aabb,
		final boolean p_191504_3_,
		final List<AxisAlignedBB> outList,
		final int minXm1, final int maxXp1,
		final int minYm1, final int maxYp1,
		final int minZm1, final int maxZp1,
		final WorldBorder worldborder,
		final boolean flag,
		final boolean flag1
	) {
		if (!Config.terrainCollisions) {
			return getVanillaCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else if (shouldApplyMeshCollisions(entityIn)) {
			return getMeshCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else if (shouldApplyReposeCollisions(entityIn)) {
			return getReposeCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else {
			return getVanillaCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		}
	}

	private static boolean getVanillaCollisions(final World _this, final Entity entityIn, final AxisAlignedBB areaAABB, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final boolean flag1) {
		IBlockState iblockstate = Blocks.STONE.getDefaultState();
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

		// NoCubes: fix forge not closing pooled mutable block pos
		try {
			if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, areaAABB, outList))
				return true;
			for (int k1 = i; k1 < j; ++k1) {
				for (int l1 = i1; l1 < j1; ++l1) {
					boolean flag2 = k1 == i || k1 == j - 1;
					boolean flag3 = l1 == i1 || l1 == j1 - 1;

					if ((!flag2 || !flag3) && _this.isBlockLoaded(blockpos$pooledmutableblockpos.setPos(k1, 64, l1))) {
						for (int i2 = k; i2 < l; ++i2) {
							if (!flag2 && !flag3 || i2 != l - 1) {
								if (p_191504_3_) {
									if (k1 < -30000000 || k1 >= 30000000 || l1 < -30000000 || l1 >= 30000000) {
										return true;
									}
								} else if (entityIn != null && flag == flag1) {
									entityIn.setOutsideBorder(!flag1);
								}

								blockpos$pooledmutableblockpos.setPos(k1, i2, l1);
								IBlockState iblockstate1;

								if (!p_191504_3_ && !worldborder.contains(blockpos$pooledmutableblockpos) && flag1) {
									iblockstate1 = iblockstate;
								} else {
									iblockstate1 = _this.getBlockState(blockpos$pooledmutableblockpos);
								}

								iblockstate1.addCollisionBoxToList(_this, blockpos$pooledmutableblockpos, areaAABB, outList, entityIn, false);

								if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, areaAABB, outList)) {
									return true;
								}
							}
						}
					}
				}
			}
		} finally {
			blockpos$pooledmutableblockpos.release();
		}

		return !outList.isEmpty();
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

								if (TERRAIN_SMOOTHABLE.test(state)) {
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

	private static boolean getMeshCollisions(
		final World _this,
		final Entity entityIn,
		final AxisAlignedBB aabb,
		final boolean p_191504_3_,
		final List<AxisAlignedBB> outList,
		final int minXm1, final int maxXp1,
		final int minYm1, final int maxYp1,
		final int minZm1, final int maxZp1,
		final WorldBorder worldborder,
		final boolean flag,
		final boolean flag1
	) {
		if (!_this.isAreaLoaded(
			new StructureBoundingBox(
				minXm1, minYm1, minZm1,
				maxXp1, maxYp1, maxZp1
			),
			true
		)) {
			return getFallbackMeshCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		}

		MeshGenerator generator = Config.terrainMeshGenerator.getMeshGenerator();

		Vec3i negativeAreaExtension = generator.getNegativeAreaExtension();
		BlockPos blockPos = new BlockPos(minXm1, minYm1, minZm1);
		BlockPos start = blockPos.subtract(negativeAreaExtension);
		BlockPos end = new BlockPos(maxXp1, maxYp1, maxZp1).add(generator.getPositiveAreaExtension());
		ModProfiler.get().start("collision setup");
		IShapeConsumer consumer = (x0f, y0f, z0f, x1f, y1f, z1f) -> {
			double x = start.getX();
			double y = start.getY();
			double z = start.getZ();
			double x0 = x + x0f;
			double x1 = x + x1f;
			double y0 = y + y0f;
			double y1 = y + y1f;
			double z0 = z + z0f;
			double z1 = z + z1f;
			if (aabb.intersects(x0, y0, z0, x1, y1, z1))
				outList.add(new AxisAlignedBB(x0, y0, z0, x1, y1, z1));

		};
		try (Area area = new Area(_this, start, end)) {
			Face normal = new Face();
			Vec centre = new Vec();
			Vec averageOfNormal = new Vec();
			ModProfiler.get().endStart("collision generation");
			generator.generate(area, TERRAIN_SMOOTHABLE, (pos, amount) -> {
				// Generate collisions for blocks that are fully inside the isosurface
				// The face handler will generate collisions for the surface
				if (amount == 1) {
					float x0 = pos.getX();
					float y0 = pos.getY();
					float z0 = pos.getZ();
					if (generator instanceof SurfaceNets) {
						// Pretty disgusting, see the comments in SurfaceNets about densities and corners for why this offset exists
						x0 += 0.5F;
						y0 += 0.5F;
						z0 += 0.5F;
					}
					consumer.accept(
						x0, y0, z0,
						x0 + 1, y0 + 1, z0 + 1
					);
				}
				return true;
			}, (pos, face) -> {
				face.assignNormalTo(normal);
				face.assignAverageTo(centre);

				normal.assignAverageTo(averageOfNormal);

				generateShape(centre, averageOfNormal, consumer, face.v0);
				generateShape(centre, averageOfNormal, consumer, face.v1);
				generateShape(centre, averageOfNormal, consumer, face.v2);
				generateShape(centre, averageOfNormal, consumer, face.v3);
				return true;
			});
			ModProfiler.get().endStart("collision computation");
		}

		PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
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

								if (!TERRAIN_SMOOTHABLE.test(state))
									state.addCollisionBoxToList(_this, pooledMutableBlockPos, aabb, outList, entityIn, false);

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
			ModProfiler.get().end();
		}
		return !outList.isEmpty();
	}

	private static boolean getFallbackMeshCollisions(
		final World _this,
		final Entity entityIn,
		final AxisAlignedBB aabb,
		final boolean p_191504_3_,
		final List<AxisAlignedBB> outList,
		final int minXm1, final int maxXp1,
		final int minYm1, final int maxYp1,
		final int minZm1, final int maxZp1,
		final WorldBorder worldborder,
		final boolean flag,
		final boolean flag1
	) {
		return getVanillaCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
	}

	private static void generateShape(Vec centre, Vec averageOfNormal, IShapeConsumer consumer, Vec v) {
		consumer.accept(
			v.x, v.y, v.z,
			centre.x + averageOfNormal.x, centre.y + averageOfNormal.y, centre.z + averageOfNormal.z
		);
	}

	public interface IShapeConsumer {

		void accept(
			float x0, float y0, float z0,
			float x1, float y1, float z1
		);

	}

}
