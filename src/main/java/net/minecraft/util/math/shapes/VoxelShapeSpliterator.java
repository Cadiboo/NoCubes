package net.minecraft.util.math.shapes;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.CubeCoordinateIterator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ICollisionReader;
import net.minecraft.world.border.WorldBorder;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class VoxelShapeSpliterator extends AbstractSpliterator<VoxelShape> {

	@Nullable
	private final Entity entity;
	private final AxisAlignedBB aabb;
	private final ISelectionContext context;
	private final CubeCoordinateIterator cubeIterator;
	private final BlockPos.Mutable pos;
	private final VoxelShape aabbShape;
	private final ICollisionReader world;
	private final BiPredicate<BlockState, BlockPos> predicate;
	private boolean entityIsNonNull;

	public VoxelShapeSpliterator(ICollisionReader p_i231606_1_, @Nullable Entity p_i231606_2_, AxisAlignedBB p_i231606_3_) {
		this(p_i231606_1_, p_i231606_2_, p_i231606_3_, (p_241459_0_, p_241459_1_) -> {
			return true;
		});
	}

	public VoxelShapeSpliterator(ICollisionReader p_i241238_1_, @Nullable Entity p_i241238_2_, AxisAlignedBB p_i241238_3_, BiPredicate<BlockState, BlockPos> p_i241238_4_) {
		super(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE);
		this.context = p_i241238_2_ == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(p_i241238_2_);
		this.pos = new BlockPos.Mutable();
		this.aabbShape = VoxelShapes.create(p_i241238_3_);
		this.world = p_i241238_1_;
		this.entityIsNonNull = p_i241238_2_ != null;
		this.entity = p_i241238_2_;
		this.aabb = p_i241238_3_;
		this.predicate = p_i241238_4_;
		int i = MathHelper.floor(p_i241238_3_.minX - 1.0E-7D) - 1;
		int j = MathHelper.floor(p_i241238_3_.maxX + 1.0E-7D) + 1;
		int k = MathHelper.floor(p_i241238_3_.minY - 1.0E-7D) - 1;
		int l = MathHelper.floor(p_i241238_3_.maxY + 1.0E-7D) + 1;
		int i1 = MathHelper.floor(p_i241238_3_.minZ - 1.0E-7D) - 1;
		int j1 = MathHelper.floor(p_i241238_3_.maxZ + 1.0E-7D) + 1;
		this.cubeIterator = new CubeCoordinateIterator(i, k, i1, j, l, j1);
	}

	private static boolean compareGrow(VoxelShape shape, AxisAlignedBB aabb) {
		return VoxelShapes.compare(shape, VoxelShapes.create(aabb.grow(0.0000001)), IBooleanFunction.AND);
	}

	private static boolean compareShrink(VoxelShape shape, AxisAlignedBB aabb) {
		return VoxelShapes.compare(shape, VoxelShapes.create(aabb.shrink(0.0000001)), IBooleanFunction.AND);
	}

	public static boolean isInsideWorldBorder(WorldBorder border, AxisAlignedBB aabb) {
		double minX = MathHelper.floor(border.minX());
		double minZ = MathHelper.floor(border.minZ());
		double maxX = MathHelper.ceil(border.maxX());
		double maxZ = MathHelper.ceil(border.maxZ());
		return aabb.minX > minX && aabb.minX < maxX && aabb.minZ > minZ && aabb.minZ < maxZ && aabb.maxX > minX && aabb.maxX < maxX && aabb.maxZ > minZ && aabb.maxZ < maxZ;
	}

	/**
	 * If a remaining element exists, performs the given action on it,
	 * returning {@code true}; else returns {@code false}.  If this
	 * Spliterator is {@link #ORDERED} the action is performed on the
	 * next element in encounter order.  Exceptions thrown by the
	 * action are relayed to the caller.
	 *
	 * @param action The action
	 * @return {@code false} if no remaining elements existed
	 * upon entry to this method, else {@code true}.
	 * @throws NullPointerException if the specified action is null
	 */
	public boolean tryAdvance(Consumer<? super VoxelShape> action) {
		return this.entityIsNonNull && this.runIfTouchingWorldBorder(action) || this.tryAdvanceSafe(action);
	}

	boolean tryAdvanceSafe(Consumer<? super VoxelShape> action) {
		while (true) {
			if (this.cubeIterator.hasNext()) {
				int x = this.cubeIterator.getX();
				int y = this.cubeIterator.getY();
				int z = this.cubeIterator.getZ();
				int boundariesTouched = this.cubeIterator.numBoundariesTouched();
				if (boundariesTouched == 3) {
					continue;
				}

				IBlockReader iblockreader = this.getChunk(x, z);
				if (iblockreader == null) {
					continue;
				}

				this.pos.setPos(x, y, z);
				BlockState blockstate = iblockreader.getBlockState(this.pos);
				if (!this.predicate.test(blockstate, this.pos)) {
					continue;
				} else if (boundariesTouched == 1 && !blockstate.isCollisionShapeLargerThanFullBlock()) {
					continue;
				} else if (boundariesTouched == 2 && !blockstate.isIn(Blocks.MOVING_PISTON)) {
					continue;
				}

				VoxelShape voxelshape = blockstate.getCollisionShape(this.world, this.pos, this.context);
				if (voxelshape == VoxelShapes.fullCube()) {
					if (!this.aabb.intersects(x, y, z, x + 1, y + 1, z + 1)) {
						continue;
					}

					action.accept(voxelshape.withOffset(x, y, z));
					return true;
				}

				VoxelShape voxelshape1 = voxelshape.withOffset(x, y, z);
				if (!VoxelShapes.compare(voxelshape1, this.aabbShape, IBooleanFunction.AND)) {
					continue;
				}

				action.accept(voxelshape1);
				return true;
			}

			return false;
		}
	}

	@Nullable
	private IBlockReader getChunk(int x, int z) {
		int chunkX = x >> 4;
		int chunkZ = z >> 4;
		return this.world.getBlockReader(chunkX, chunkZ);
	}

	boolean runIfTouchingWorldBorder(Consumer<? super VoxelShape> action) {
		Objects.requireNonNull(this.entity);
		this.entityIsNonNull = false;
		WorldBorder worldborder = this.world.getWorldBorder();
		AxisAlignedBB axisalignedbb = this.entity.getBoundingBox();
		if (!isInsideWorldBorder(worldborder, axisalignedbb)) {
			VoxelShape voxelshape = worldborder.getShape();
			if (!compareShrink(voxelshape, axisalignedbb) && compareGrow(voxelshape, axisalignedbb)) {
				action.accept(voxelshape);
				return true;
			}
		}

		return false;
	}

}
