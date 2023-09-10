package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import javax.annotation.Nullable;
import java.util.List;

import static io.github.cadiboo.nocubes.collision.MeshCollisionUtil.addShapeToListIfIntersects;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public final class CollisionHandler {

	public static boolean shouldApplyMeshCollisions(@Nullable final Entity entity) {
		return entity instanceof EntityPlayer;
	}

	public static boolean shouldApplyReposeCollisions(@Nullable final Entity entity) {
		if (NoCubesConfig.Server.tempMobCollisionsDisabled)
			return false;
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
		if (!NoCubesConfig.Server.collisionsEnabled) {
			return getVanillaCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else if (shouldApplyMeshCollisions(entityIn)) {
			return getMeshCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else if (shouldApplyReposeCollisions(entityIn)) {
			return getReposeCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		} else {
			return getVanillaCollisions(_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1);
		}
	}

	private static boolean getVanillaCollisions(final World _this, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final boolean flag1) {
		return getCollisions(
				_this, entityIn, aabb, p_191504_3_, outList, i, j, k, l, i1, j1, worldborder, flag, flag1,
				(iblockstate1, _this1, blockpos$pooledmutableblockpos, aabb1, outList1, entityIn1) -> iblockstate1.addCollisionBoxToList(_this1, blockpos$pooledmutableblockpos, aabb1, outList1, entityIn1, false)
		);
	}

	@FunctionalInterface
	interface CollisionAdder {
		void add(IBlockState iblockstate1, World _this, PooledMutableBlockPos blockpos$pooledmutableblockpos, AxisAlignedBB aabb, List<AxisAlignedBB> outList, Entity entityIn
		);
	}

	private static boolean getCollisions(
			final World _this, final Entity entityIn, final AxisAlignedBB aabb,
			final boolean p_191504_3_, final List<AxisAlignedBB> outList,
			final int i, final int j, final int k,
			final int l, final int i1, final int j1,
			final WorldBorder worldborder, final boolean flag, final boolean flag1,
			CollisionAdder adder
	) {
		IBlockState iblockstate = Blocks.STONE.getDefaultState();
		BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain();

		// NoCubes: fix forge not closing pooled mutable block pos
		try {
			if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList))
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

								adder.add(iblockstate1, _this, blockpos$pooledmutableblockpos, aabb, outList, entityIn);

								if (p_191504_3_ && !net.minecraftforge.event.ForgeEventFactory.gatherCollisionBoxes(_this, entityIn, aabb, outList)) {
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

	private static boolean getReposeCollisions(final World _this, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final boolean flag1) {
		return getCollisions(
				_this, entityIn, aabb, p_191504_3_, outList, i, j, k, l, i1, j1, worldborder, flag, flag1,
				(state, _this1, pooledMutableBlockPos, aabb1, outList1, entityIn1) -> {
					if (NoCubes.smoothableHandler.isSmoothable(state)) {
						StolenReposeCode.addCollisionBoxToList(state, _this1, pooledMutableBlockPos, aabb1, outList1, entityIn1, false);
					} else {
						state.addCollisionBoxToList(_this1, pooledMutableBlockPos, aabb1, outList1, entityIn1, false);
					}
				}
		);
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
		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
			forEachCollisionRelativeToStart(
				_this, pooledMutableBlockPos,
				 minXm1,maxXp1,
				 minYm1,maxYp1,
				 minZm1,maxZp1,
				(x0, y0, z0, x1, y1, z1) -> {
					if (aabb.intersects(x0, y0, z0, x1, y1, z1))
						outList.add(new AxisAlignedBB(x0, y0, z0, x1, y1, z1));
					return true;
				}
			);
			return getCollisions(
					_this, entityIn, aabb, p_191504_3_, outList, minXm1, maxXp1, minYm1, maxYp1, minZm1, maxZp1, worldborder, flag, flag1,
					(state, _this1, pooledMutableBlockPos1, aabb1, outList1, entityIn1) -> {
						if (!NoCubes.smoothableHandler.isSmoothable(state))
							state.addCollisionBoxToList(_this1, pooledMutableBlockPos1, aabb1, outList1, entityIn1, false);
					}
			);
		} finally {
			pooledMutableBlockPos.release();
		}
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

	public static void forEachCollisionRelativeToStart(IBlockAccess world, MutableBlockPos pos, int minX, int maxX, int minY, int maxY, int minZ, int maxZ, ShapeConsumer consumer) {
		Mesher mesher = Config.terrainMeshGenerator;
		BlockPos start = new BlockPos(minX, minY, minZ);
		// Size is mutable and only correct until the Area constructor call
		BlockPos size = pos.setPos(
			maxX - minX,
			maxY - minY,
			maxZ - minZ
		);
		try (Area area = new Area(world, start, size, mesher)) {
			// See Mesher#translateToMeshStart for an explanation of this
			int dx = Mesher.validateMeshOffset(area.start.getX() - start.getX());
			int dy = Mesher.validateMeshOffset(area.start.getY() - start.getY());
			int dz = Mesher.validateMeshOffset(area.start.getZ() - start.getZ());
			generate(area, mesher, (x0, y0, z0, x1, y1, z1) -> consumer.accept(
					dx + x0, dy + y0, dz + z0,
					dx + x1, dy + y1, dz + z1
			));
		}
	}

	public static void generate(Area area, Mesher mesher, ShapeConsumer consumer) {
		mesher.generateCollisions(area, NoCubes.smoothableHandler::isSmoothable, consumer);
	}

	public static boolean generateShapes(Vec centre, Vec faceNormal, ShapeConsumer consumer, Face face) {
		if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v0))
			return false;
		if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v1))
			return false;
		if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v2))
			return false;
		if (!CollisionHandler.generateShape(centre, faceNormal, consumer, face.v3))
			return false;
		return true;
	}

	private static boolean generateShape(Vec centre, Vec faceNormal, ShapeConsumer consumer, Vec v) {
		float vX = v.x;
		float vY = v.y;
		float vZ = v.z;
		float extX = centre.x + faceNormal.x;
		float extY = centre.y + faceNormal.y;
		float extZ = centre.z + faceNormal.z;
		return consumer.accept(
				Math.min(vX, extX), Math.min(vY, extY), Math.min(vZ, extZ),
				Math.max(vX, extX), Math.max(vY, extY), Math.max(vZ, extZ)
		);
	}
}
