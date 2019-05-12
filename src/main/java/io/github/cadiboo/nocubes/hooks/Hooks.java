package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.Config;
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
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeInt;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapePartBitSet;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.HashSet;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess") // Hooks are called with ASM
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

	//return all the voxel shapes that entityShape intersects inside area
	public static Stream<VoxelShape> getCollisionShapes(final IWorldReaderBase iWorldReaderBase, final VoxelShape area, final VoxelShape entityShape, final boolean isEntityInsideWorldBorder, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final VoxelShapePart voxelshapepart, final Predicate<VoxelShape> predicate) {
		try {
			return CollisionHandler.getCollisionShapes(iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, i, j, k, l, i1, j1, worldborder, flag, voxelshapepart, predicate);
		} catch (final Exception e) {
			NoCubes.LOGGER.error("Error with collisions! Falling back to vanilla.", e);
			return Stream.concat(
					CollisionHandler.getCollisionShapesExcludingSmoothable(null, iWorldReaderBase, area, entityShape, isEntityInsideWorldBorder, i, j, k, l, i1, j1, worldborder, flag, voxelshapepart, predicate),
					Stream.generate(() -> new VoxelShapeInt(voxelshapepart, i, k, i1))
							.limit(1L)
							.filter(predicate)
			);
		}
	}

	public static Stream<VoxelShape> getCollisionShapes(final IWorldReaderBase _this, final Entity movingEntity, final VoxelShape area, final VoxelShape entityShape, boolean isEntityInsideWorldBorder) {
		int i = MathHelper.floor(area.getStart(EnumFacing.Axis.X)) - 1;
		int j = MathHelper.ceil(area.getEnd(EnumFacing.Axis.X)) + 1;
		int k = MathHelper.floor(area.getStart(EnumFacing.Axis.Y)) - 1;
		int l = MathHelper.ceil(area.getEnd(EnumFacing.Axis.Y)) + 1;
		int i1 = MathHelper.floor(area.getStart(EnumFacing.Axis.Z)) - 1;
		int j1 = MathHelper.ceil(area.getEnd(EnumFacing.Axis.Z)) + 1;
		WorldBorder worldborder = _this.getWorldBorder();
		boolean flag = worldborder.minX() < (double) i && (double) j < worldborder.maxX() && worldborder.minZ() < (double) i1 && (double) j1 < worldborder.maxZ();
		VoxelShapePart voxelshapepart = new VoxelShapePartBitSet(j - i, l - k, j1 - i1);
		Predicate<VoxelShape> predicate = (p_212393_1_) -> {
			return !p_212393_1_.isEmpty() && VoxelShapes.compare(area, p_212393_1_, IBooleanFunction.AND);
		};
		// NoCubes Start
		// Commented out because its handled by the hook that calls _this
//		if (io.github.cadiboo.nocubes.config.Config.terrainCollisions && io.github.cadiboo.nocubes.NoCubes.isEnabled())
		if (io.github.cadiboo.nocubes.hooks.Hooks.shouldApplyCollisions(movingEntity))
			return Hooks.getCollisionShapes(_this, area, entityShape, isEntityInsideWorldBorder, i, j, k, l, i1, j1, worldborder, flag, voxelshapepart, predicate);
		// NoCubes End
		Stream<VoxelShape> stream = StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(i, k, i1, j - 1, l - 1, j1 - 1).spliterator(), false).map((p_212390_12_) -> {
			int k1 = p_212390_12_.getX();
			int l1 = p_212390_12_.getY();
			int i2 = p_212390_12_.getZ();
			boolean flag1 = k1 == i || k1 == j - 1;
			boolean flag2 = l1 == k || l1 == l - 1;
			boolean flag3 = i2 == i1 || i2 == j1 - 1;
			if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && _this.isBlockLoaded(p_212390_12_)) {
				VoxelShape voxelshape;
				if (isEntityInsideWorldBorder && !flag && !worldborder.contains(p_212390_12_)) {
					voxelshape = VoxelShapes.fullCube();
				} else {
					voxelshape = _this.getBlockState(p_212390_12_).getCollisionShape(_this, p_212390_12_);
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

	private static boolean shouldApplyCollisions(final Entity movingEntity) {
		return CollisionHandler.shouldApplyCollisions(movingEntity);
	}

}
