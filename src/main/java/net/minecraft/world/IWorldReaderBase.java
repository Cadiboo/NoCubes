package net.minecraft.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeInt;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapePartBitSet;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface IWorldReaderBase extends IBlockReader {

	/**
	 * Checks to see if an air block exists at the provided location. Note that this only checks to see if the blocks
	 * material is set to air, meaning it is possible for non-vanilla blocks to still pass this check.
	 */
	boolean isAirBlock(BlockPos pos);

	Biome getBiome(BlockPos pos);

	int getLightFor(EnumLightType type, BlockPos pos);

	default boolean canBlockSeeSky(BlockPos pos) {
		if (pos.getY() >= this.getSeaLevel()) {
			return this.canSeeSky(pos);
		} else {
			BlockPos blockpos = new BlockPos(pos.getX(), this.getSeaLevel(), pos.getZ());
			if (!this.canSeeSky(blockpos)) {
				return false;
			} else {
				for (BlockPos blockpos1 = blockpos.down(); blockpos1.getY() > pos.getY(); blockpos1 = blockpos1.down()) {
					IBlockState iblockstate = this.getBlockState(blockpos1);
					if (iblockstate.getOpacity(this, blockpos1) > 0 && !iblockstate.getMaterial().isLiquid()) {
						return false;
					}
				}

				return true;
			}
		}
	}

	int getLightSubtracted(BlockPos pos, int amount);

	boolean isChunkLoaded(int x, int z, boolean allowEmpty);

	boolean canSeeSky(BlockPos pos);

	default BlockPos getHeight(Heightmap.Type heightmapType, BlockPos pos) {
		return new BlockPos(pos.getX(), this.getHeight(heightmapType, pos.getX(), pos.getZ()), pos.getZ());
	}

	int getHeight(Heightmap.Type heightmapType, int x, int z);

	default float getBrightness(BlockPos pos) {
		return this.getDimension().getLightBrightnessTable()[this.getLight(pos)];
	}

	/**
	 * Gets the closest player to the entity within the specified distance.
	 */
	@Nullable
	default EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
		return this.getClosestPlayer(entityIn.posX, entityIn.posY, entityIn.posZ, distance, false);
	}

	@Nullable
	default EntityPlayer getNearestPlayerNotCreative(Entity entityIn, double distance) {
		return this.getClosestPlayer(entityIn.posX, entityIn.posY, entityIn.posZ, distance, true);
	}

	@Nullable
	default EntityPlayer getClosestPlayer(double posX, double posY, double posZ, double distance, boolean spectator) {
		Predicate<Entity> predicate = spectator ? EntitySelectors.CAN_AI_TARGET : EntitySelectors.NOT_SPECTATING;
		return this.getClosestPlayer(posX, posY, posZ, distance, predicate);
	}

	@Nullable
	EntityPlayer getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate);

	int getSkylightSubtracted();

	WorldBorder getWorldBorder();

	boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape);

	int getStrongPower(BlockPos pos, EnumFacing direction);

	boolean isRemote();

	int getSeaLevel();

	default boolean checkNoEntityCollision(IBlockState state, BlockPos pos) {
		VoxelShape voxelshape = state.getCollisionShape(this, pos);
		return voxelshape.isEmpty() || this.checkNoEntityCollision((Entity) null, voxelshape.withOffset((double) pos.getX(), (double) pos.getY(), (double) pos.getZ()));
	}

	default boolean checkNoEntityCollision(@Nullable Entity entityIn, AxisAlignedBB aabb) {
		return this.checkNoEntityCollision(entityIn, VoxelShapes.create(aabb));
	}

	default Stream<VoxelShape> getCollisionBoxes(VoxelShape area, VoxelShape entityShape, boolean isEntityInsideWorldBorder) {
		int i = MathHelper.floor(area.getStart(EnumFacing.Axis.X)) - 1;
		int j = MathHelper.ceil(area.getEnd(EnumFacing.Axis.X)) + 1;
		int k = MathHelper.floor(area.getStart(EnumFacing.Axis.Y)) - 1;
		int l = MathHelper.ceil(area.getEnd(EnumFacing.Axis.Y)) + 1;
		int i1 = MathHelper.floor(area.getStart(EnumFacing.Axis.Z)) - 1;
		int j1 = MathHelper.ceil(area.getEnd(EnumFacing.Axis.Z)) + 1;
		WorldBorder worldborder = this.getWorldBorder();
		boolean flag = worldborder.minX() < (double) i && (double) j < worldborder.maxX() && worldborder.minZ() < (double) i1 && (double) j1 < worldborder.maxZ();
		VoxelShapePart voxelshapepart = new VoxelShapePartBitSet(j - i, l - k, j1 - i1);
		Predicate<VoxelShape> predicate = (p_212393_1_) -> {
			return !p_212393_1_.isEmpty() && VoxelShapes.compare(area, p_212393_1_, IBooleanFunction.AND);
		};
		Stream<VoxelShape> stream = StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(i, k, i1, j - 1, l - 1, j1 - 1).spliterator(), false).map((p_212390_12_) -> {
			int k1 = p_212390_12_.getX();
			int l1 = p_212390_12_.getY();
			int i2 = p_212390_12_.getZ();
			boolean flag1 = k1 == i || k1 == j - 1;
			boolean flag2 = l1 == k || l1 == l - 1;
			boolean flag3 = i2 == i1 || i2 == j1 - 1;
			if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && this.isBlockLoaded(p_212390_12_)) {
				VoxelShape voxelshape;
				if (isEntityInsideWorldBorder && !flag && !worldborder.contains(p_212390_12_)) {
					voxelshape = VoxelShapes.fullCube();
				} else {
					voxelshape = this.getBlockState(p_212390_12_).getCollisionShape(this, p_212390_12_);
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
		return Stream.concat(stream, Stream.generate(() -> {
			return new VoxelShapeInt(voxelshapepart, i, k, i1);
		}).limit(1L).filter(predicate));
	}

	default Stream<VoxelShape> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB entityBB, double x, double y, double z) {
		return this.getCollisionBoxes(entityIn, entityBB, Collections.emptySet(), x, y, z);
	}

	default Stream<VoxelShape> getCollisionBoxes(@Nullable Entity entityIn, AxisAlignedBB entityBB, Set<Entity> entitiesToIgnore, double dx, double dy, double dz) {
		double d0 = 1.0E-7D;
		VoxelShape voxelshape = VoxelShapes.create(entityBB);
		VoxelShape voxelshape1 = VoxelShapes.create(entityBB.offset(dx > 0.0D ? -1.0E-7D : 1.0E-7D, dy > 0.0D ? -1.0E-7D : 1.0E-7D, dz > 0.0D ? -1.0E-7D : 1.0E-7D));
		VoxelShape voxelshape2 = VoxelShapes.combine(VoxelShapes.create(entityBB.expand(dx, dy, dz).grow(1.0E-7D)), voxelshape1, IBooleanFunction.ONLY_FIRST);
		return this.getCollisionBoxes(entityIn, voxelshape2, voxelshape, entitiesToIgnore);
	}

	/**
	 * Returns all hit boxes in the defined area and refreshes the Entitys "isOutsideBorder" flag
	 */
	default Stream<VoxelShape> getCollisionBoxes(@Nullable Entity movingEntity, AxisAlignedBB area) {
		return this.getCollisionBoxes(movingEntity, VoxelShapes.create(area), VoxelShapes.empty(), Collections.emptySet());
	}

	/**
	 * Returns all hit boxes in the defined area and refreshes the Entitys "isOutsideBorder" flag
	 */
	default Stream<VoxelShape> getCollisionBoxes(@Nullable Entity movingEntity, VoxelShape area, VoxelShape p_212392_3_, Set<Entity> entitiesToIgnore) {
		boolean flag = movingEntity != null && movingEntity.isOutsideBorder();
		boolean flag1 = movingEntity != null && this.isInsideWorldBorder(movingEntity);
		if (movingEntity != null && flag == flag1) {
			movingEntity.setOutsideBorder(!flag1);
		}
		// NoCubes Start
		if (io.github.cadiboo.nocubes.config.Config.terrainCollisions) {
			return io.github.cadiboo.nocubes.hooks.Hooks.getCollisionShapes(this, movingEntity, area, p_212392_3_, flag1);
		} else
		// NoCubes End

		return this.getCollisionBoxes(area, p_212392_3_, flag1);
	}

	default boolean isInsideWorldBorder(Entity entityToCheck) {
		WorldBorder worldborder = this.getWorldBorder();
		double d0 = worldborder.minX();
		double d1 = worldborder.minZ();
		double d2 = worldborder.maxX();
		double d3 = worldborder.maxZ();
		if (entityToCheck.isOutsideBorder()) {
			++d0;
			++d1;
			--d2;
			--d3;
		} else {
			--d0;
			--d1;
			++d2;
			++d3;
		}

		return entityToCheck.posX > d0 && entityToCheck.posX < d2 && entityToCheck.posZ > d1 && entityToCheck.posZ < d3;
	}

	default boolean isCollisionBoxesEmpty(@Nullable Entity entityIn, AxisAlignedBB aabb, Set<Entity> entitiesToIgnore) {
		return this.getCollisionBoxes(entityIn, VoxelShapes.create(aabb), VoxelShapes.empty(), entitiesToIgnore).allMatch(VoxelShape::isEmpty);
	}

	default boolean isCollisionBoxesEmpty(@Nullable Entity entityIn, AxisAlignedBB aabb) {
		return this.isCollisionBoxesEmpty(entityIn, aabb, Collections.emptySet());
	}

	default boolean hasWater(BlockPos pos) {
		return this.getFluidState(pos).isTagged(FluidTags.WATER);
	}

	/**
	 * Checks if any of the blocks within the aabb are liquids.
	 */
	default boolean containsAnyLiquid(AxisAlignedBB bb) {
		int i = MathHelper.floor(bb.minX);
		int j = MathHelper.ceil(bb.maxX);
		int k = MathHelper.floor(bb.minY);
		int l = MathHelper.ceil(bb.maxY);
		int i1 = MathHelper.floor(bb.minZ);
		int j1 = MathHelper.ceil(bb.maxZ);

		try (BlockPos.PooledMutableBlockPos blockpos$pooledmutableblockpos = BlockPos.PooledMutableBlockPos.retain()) {
			for (int k1 = i; k1 < j; ++k1) {
				for (int l1 = k; l1 < l; ++l1) {
					for (int i2 = i1; i2 < j1; ++i2) {
						IBlockState iblockstate = this.getBlockState(blockpos$pooledmutableblockpos.setPos(k1, l1, i2));
						if (!iblockstate.getFluidState().isEmpty()) {
							boolean flag = true;
							return flag;
						}
					}
				}
			}

			return false;
		}
	}

	default int getLight(BlockPos pos) {
		return this.getNeighborAwareLightSubtracted(pos, this.getSkylightSubtracted());
	}

	default int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
		if (pos.getX() >= -30000000 && pos.getZ() >= -30000000 && pos.getX() < 30000000 && pos.getZ() < 30000000) {
			if (this.getBlockState(pos).useNeighborBrightness(this, pos)) {
				int i = this.getLightSubtracted(pos.up(), amount);
				int j = this.getLightSubtracted(pos.east(), amount);
				int k = this.getLightSubtracted(pos.west(), amount);
				int l = this.getLightSubtracted(pos.south(), amount);
				int i1 = this.getLightSubtracted(pos.north(), amount);
				if (j > i) {
					i = j;
				}

				if (k > i) {
					i = k;
				}

				if (l > i) {
					i = l;
				}

				if (i1 > i) {
					i = i1;
				}

				return i;
			} else {
				return this.getLightSubtracted(pos, amount);
			}
		} else {
			return 15;
		}
	}

	default boolean isBlockLoaded(BlockPos pos) {
		return this.isBlockLoaded(pos, true);
	}

	default boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
		return this.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4, allowEmpty);
	}

	default boolean isAreaLoaded(BlockPos pos, int radius) {
		return this.isAreaLoaded(pos, radius, true);
	}

	default boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty) {
		return this.isAreaLoaded(center.getX() - radius, center.getY() - radius, center.getZ() - radius, center.getX() + radius, center.getY() + radius, center.getZ() + radius, allowEmpty);
	}

	default boolean isAreaLoaded(BlockPos from, BlockPos to) {
		return this.isAreaLoaded(from, to, true);
	}

	default boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty) {
		return this.isAreaLoaded(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), allowEmpty);
	}

	default boolean isAreaLoaded(MutableBoundingBox box) {
		return this.isAreaLoaded(box, true);
	}

	default boolean isAreaLoaded(MutableBoundingBox box, boolean allowEmpty) {
		return this.isAreaLoaded(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, allowEmpty);
	}

	default boolean isAreaLoaded(int xStart, int yStart, int zStart, int xEnd, int yEnd, int zEnd, boolean allowEmpty) {
		if (yEnd >= 0 && yStart < 256) {
			xStart = xStart >> 4;
			zStart = zStart >> 4;
			xEnd = xEnd >> 4;
			zEnd = zEnd >> 4;

			for (int i = xStart; i <= xEnd; ++i) {
				for (int j = zStart; j <= zEnd; ++j) {
					if (!this.isChunkLoaded(i, j, allowEmpty)) {
						return false;
					}
				}
			}

			return true;
		} else {
			return false;
		}
	}

	Dimension getDimension();

}
