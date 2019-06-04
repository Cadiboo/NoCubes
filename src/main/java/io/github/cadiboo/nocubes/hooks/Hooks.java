package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkCompileTaskGenerator;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static io.github.cadiboo.nocubes.NoCubes.LOGGER;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess") // Hooks are called with ASM
public final class Hooks {

	public static void preIteration(final RenderChunk renderChunk, final float x, final float y, final float z, final ChunkCompileTaskGenerator generator, final CompiledChunk compiledchunk, final BlockPos blockpos, final BlockPos blockpos1, final World world, final IBlockAccess lvt_10_1_, final VisGraph lvt_11_1_, final HashSet lvt_12_1_, final boolean[] aboolean, final BlockRendererDispatcher blockrendererdispatcher) {
		RenderDispatcher.renderChunk(renderChunk, blockpos, generator, compiledchunk, world, lvt_10_1_, aboolean, new Random(), blockrendererdispatcher);
	}

	//return if normal rendering should happen
	public static boolean renderBlockDamage(final Tessellator tessellatorIn, final BufferBuilder bufferBuilderIn, final BlockPos blockpos, final IBlockState iblockstate, final WorldClient world, final TextureAtlasSprite textureatlassprite, final BlockRendererDispatcher blockrendererdispatcher) {
		if (!Config.renderSmoothTerrain || !iblockstate.nocubes_isTerrainSmoothable()) {
			if (!Config.renderSmoothLeaves || !iblockstate.nocubes_isLeavesSmoothable()) {
				return true;
			}
		}
		RenderDispatcher.renderSmoothBlockDamage(tessellatorIn, bufferBuilderIn, blockpos, iblockstate, world, textureatlassprite);
		return false;
	}

//	//return all the voxel shapes that entityShape intersects inside area
//	public static Stream<VoxelShape> getCollisionShapes(final IWorldReaderBase iWorldReaderBase, final Entity movingEntity, final VoxelShape area, final VoxelShape entityShape, final boolean isEntityInsideWorldBorder, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final VoxelShapePart voxelshapepart, final Predicate<VoxelShape> predicate) {
//		try {
//			return CollisionHandler.getCollisionShapes(iWorldReaderBase, movingEntity, area, entityShape, isEntityInsideWorldBorder, i, j, k, l, i1, j1, worldborder, flag, voxelshapepart, predicate);
//		} catch (final Exception e) {
//			NoCubes.LOGGER.error("Error with collisions! Falling back to vanilla.", e);
//			return Stream.concat(
//					CollisionHandler.getCollisionShapesExcludingSmoothable(null, iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, i, j, k, l, i1, j1, worldborder, flag, voxelshapepart, predicate),
//					Stream.generate(() -> new VoxelShapeInt(voxelshapepart, i, k, i1))
//							.limit(1L)
//							.filter(predicate)
//			);
//		}
//	}
//
//	public static Stream<VoxelShape> getCollisionShapes(final IWorldReaderBase _this, final Entity movingEntity, final VoxelShape area, final VoxelShape entityShape, boolean isEntityInsideWorldBorder) {
//		int i = MathHelper.floor(area.getStart(EnumFacing.Axis.X)) - 1;
//		int j = MathHelper.ceil(area.getEnd(EnumFacing.Axis.X)) + 1;
//		int k = MathHelper.floor(area.getStart(EnumFacing.Axis.Y)) - 1;
//		int l = MathHelper.ceil(area.getEnd(EnumFacing.Axis.Y)) + 1;
//		int i1 = MathHelper.floor(area.getStart(EnumFacing.Axis.Z)) - 1;
//		int j1 = MathHelper.ceil(area.getEnd(EnumFacing.Axis.Z)) + 1;
//		WorldBorder worldborder = _this.getWorldBorder();
//		boolean flag = worldborder.minX() < (double) i && (double) j < worldborder.maxX() && worldborder.minZ() < (double) i1 && (double) j1 < worldborder.maxZ();
//		VoxelShapePart voxelshapepart = new VoxelShapePartBitSet(j - i, l - k, j1 - i1);
//		Predicate<VoxelShape> predicate = (p_212393_1_) -> {
//			return !p_212393_1_.isEmpty() && VoxelShapes.compare(area, p_212393_1_, IBooleanFunction.AND);
//		};
//		// NoCubes Start
//		if (io.github.cadiboo.nocubes.hooks.Hooks.shouldApplyCollisions(movingEntity))
//			return Hooks.getCollisionShapes(_this, movingEntity, area, entityShape, isEntityInsideWorldBorder, i, j, k, l, i1, j1, worldborder, flag, voxelshapepart, predicate);
//		// NoCubes End
//		Stream<VoxelShape> stream = StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(i, k, i1, j - 1, l - 1, j1 - 1).spliterator(), false).map((p_212390_12_) -> {
//			int k1 = p_212390_12_.getX();
//			int l1 = p_212390_12_.getY();
//			int i2 = p_212390_12_.getZ();
//			boolean flag1 = k1 == i || k1 == j - 1;
//			boolean flag2 = l1 == k || l1 == l - 1;
//			boolean flag3 = i2 == i1 || i2 == j1 - 1;
//			if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && _this.isBlockLoaded(p_212390_12_)) {
//				VoxelShape voxelshape;
//				if (isEntityInsideWorldBorder && !flag && !worldborder.contains(p_212390_12_)) {
//					voxelshape = VoxelShapes.fullCube();
//				} else {
//					voxelshape = _this.getBlockState(p_212390_12_).getCollisionShape(_this, p_212390_12_);
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
//		return Stream.concat(stream, Stream.generate(() -> {
//			return new VoxelShapeInt(voxelshapepart, i, k, i1);
//		}).limit(1L).filter(predicate));
//	}

	private static boolean shouldApplyCollisions(final Entity movingEntity) {
		return CollisionHandler.shouldApplyMeshCollisions(movingEntity) || CollisionHandler.shouldApplyReposeCollisions(movingEntity);
	}

//	public static IFluidState getFluidState(final World world, final BlockPos pos) {
//
//		final int posX = pos.getX();
//		final int posY = pos.getY();
//		final int posZ = pos.getZ();
//
//		int currentChunkPosX = posX >> 4;
//		int currentChunkPosZ = posZ >> 4;
//		Chunk currentChunk = world.getChunk(currentChunkPosX, currentChunkPosZ);
//
//		final int extendRange = Config.extendFluidsRange.getRange();
//
//		if (extendRange == 0) {
//			return currentChunk.getFluidState(posX, posY, posZ);
//		}
//
//		final IBlockState state = currentChunk.getBlockState(posX, posY, posZ);
//
//		// terrain is serverside, leaves is clientside - lets see how this goes...
//		if (!state.nocubes_isTerrainSmoothable()) {
//			return state.getFluidState();
//		}
//
//		final IFluidState fluidState = state.getFluidState();
//		if (!fluidState.isEmpty()) {
//			return fluidState;
//		}
//
//		// For offset = -1 or -2 to offset = 1 or 2;
//		final int maxXOffset = extendRange;
//		final int maxZOffset = extendRange;
//
//		// Check up
//		{
//			final IFluidState state1 = currentChunk.getFluidState(posX, posY + 1, posZ);
//			if (state1.isSource()) {
//				return state1;
//			}
//		}
//
//		for (int xOffset = -maxXOffset; xOffset <= maxXOffset; ++xOffset) {
//			for (int zOffset = -maxZOffset; zOffset <= maxZOffset; ++zOffset) {
//
//				// No point in checking myself
//				if (xOffset == 0 && zOffset == 0) {
//					continue;
//				}
//
//				final int x = posX + xOffset;
//				final int z = posZ + zOffset;
//
//				if (currentChunkPosX != x >> 4 || currentChunkPosZ != z >> 4) {
//					currentChunkPosX = x >> 4;
//					currentChunkPosZ = z >> 4;
//					currentChunk = world.getChunk(currentChunkPosX, currentChunkPosZ);
//				}
//
//				final IFluidState state1 = currentChunk.getFluidState(x, posY, z);
//				if (state1.isSource()) {
//					return state1;
//				}
//
//			}
//		}
//
//		return fluidState;
//
//	}

	public static boolean getCollisionBoxes(final World _this, final Entity entityIn, final AxisAlignedBB aabb, final boolean p_191504_3_, final List<AxisAlignedBB> outList, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final boolean flag1) {

		// NoCubes Start
		if (Config.terrainCollisions && io.github.cadiboo.nocubes.hooks.Hooks.shouldApplyCollisions(entityIn)) {
			try {
				return CollisionHandler.getCollisionBoxes(_this, entityIn, aabb, p_191504_3_, outList, i, j, k, l, i1, j1, worldborder, flag, flag1);
			} catch (Exception e) {
//				LOGGER.error("Collisions error, falling back to vanilla", e);
				//Fallthrough to vanilla
			}
		}
		// NoCubes End

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

								iblockstate1.addCollisionBoxToList(_this, blockpos$pooledmutableblockpos, aabb, outList, entityIn, false);

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

}
