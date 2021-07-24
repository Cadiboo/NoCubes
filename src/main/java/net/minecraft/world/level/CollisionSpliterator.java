package net.minecraft.world.level;

import java.util.Deque;
import java.util.Objects;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Cursor3D;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CollisionSpliterator extends AbstractSpliterator<VoxelShape> {
	@Nullable
	private final Entity source;
	private final AABB box;
	private final CollisionContext context;
	private final Cursor3D cursor;
	private final BlockPos.MutableBlockPos pos;
	private final VoxelShape entityShape;
	private final CollisionGetter collisionGetter;
	private boolean needsBorderCheck;
	private final BiPredicate<BlockState, BlockPos> predicate;
	private Deque<VoxelShape> nocubesShapes = null;

	public CollisionSpliterator(CollisionGetter p_45798_, @Nullable Entity p_45799_, AABB p_45800_) {
		this(p_45798_, p_45799_, p_45800_, (p_45810_, p_45811_) -> {
			return true;
		});
	}

	public CollisionSpliterator(CollisionGetter p_45802_, @Nullable Entity p_45803_, AABB p_45804_, BiPredicate<BlockState, BlockPos> p_45805_) {
		super(Long.MAX_VALUE, 1280);
		this.context = p_45803_ == null ? CollisionContext.empty() : CollisionContext.of(p_45803_);
		this.pos = new BlockPos.MutableBlockPos();
		this.entityShape = Shapes.create(p_45804_);
		this.collisionGetter = p_45802_;
		this.needsBorderCheck = p_45803_ != null;
		this.source = p_45803_;
		this.box = p_45804_;
		this.predicate = p_45805_;
		int i = Mth.floor(p_45804_.minX - 1.0E-7D) - 1;
		int j = Mth.floor(p_45804_.maxX + 1.0E-7D) + 1;
		int k = Mth.floor(p_45804_.minY - 1.0E-7D) - 1;
		int l = Mth.floor(p_45804_.maxY + 1.0E-7D) + 1;
		int i1 = Mth.floor(p_45804_.minZ - 1.0E-7D) - 1;
		int j1 = Mth.floor(p_45804_.maxZ + 1.0E-7D) + 1;
		this.cursor = new Cursor3D(i, k, i1, j, l, j1);
	}

	public boolean tryAdvance(Consumer<? super VoxelShape> p_45826_) {
		if (this.needsBorderCheck) if (this.worldBorderCheck(p_45826_)) return true;

		if (this.nocubesShapes == null)
			this.nocubesShapes = io.github.cadiboo.nocubes.hooks.Hooks.createNoCubesIntersectingCollisionList(this.collisionGetter, this.box, this.pos);
		while (!this.nocubesShapes.isEmpty()) {
			VoxelShape shape = this.nocubesShapes.pop();
//		   if (!this.box.intersects(
//		   	  shape.min(Direction.Axis.X), shape.min(Direction.Axis.Y), shape.min(Direction.Axis.Z),
//		   	  shape.max(Direction.Axis.X), shape.max(Direction.Axis.Y), shape.max(Direction.Axis.Z)
//		   ))
//			   continue;
			if (!Shapes.joinIsNotEmpty(shape, this.entityShape, BooleanOp.AND))
				continue;
			p_45826_.accept(shape);
			return true;
		}

		return this.collisionCheck(p_45826_);
	}

	boolean collisionCheck(Consumer<? super VoxelShape> p_45819_) {
		while(true) {
			if (this.cursor.advance()) {
				int i = this.cursor.nextX();
				int j = this.cursor.nextY();
				int k = this.cursor.nextZ();
				int l = this.cursor.getNextType();
				if (l == 3) {
					continue;
				}

				BlockGetter blockgetter = this.getChunk(i, k);
				if (blockgetter == null) {
					continue;
				}

				this.pos.set(i, j, k);
				BlockState blockstate = blockgetter.getBlockState(this.pos);
				if (!this.predicate.test(blockstate, this.pos) || l == 1 && !blockstate.hasLargeCollisionShape() || l == 2 && !blockstate.is(Blocks.MOVING_PISTON)) {
					continue;
				}

				VoxelShape voxelshape = blockstate.getCollisionShape(this.collisionGetter, this.pos, this.context);
				if (voxelshape == Shapes.block()) {
					if (!this.box.intersects((double)i, (double)j, (double)k, (double)i + 1.0D, (double)j + 1.0D, (double)k + 1.0D)) {
						continue;
					}

					p_45819_.accept(voxelshape.move((double)i, (double)j, (double)k));
					return true;
				}

				VoxelShape voxelshape1 = voxelshape.move((double)i, (double)j, (double)k);
				if (!Shapes.joinIsNotEmpty(voxelshape1, this.entityShape, BooleanOp.AND)) {
					continue;
				}

				p_45819_.accept(voxelshape1);
				return true;
			}

			return false;
		}
	}

	@Nullable
	private BlockGetter getChunk(int p_45807_, int p_45808_) {
		int i = SectionPos.blockToSectionCoord(p_45807_);
		int j = SectionPos.blockToSectionCoord(p_45808_);
		return this.collisionGetter.getChunkForCollisions(i, j);
	}

	boolean worldBorderCheck(Consumer<? super VoxelShape> p_45824_) {
		Objects.requireNonNull(this.source);
		this.needsBorderCheck = false;
		WorldBorder worldborder = this.collisionGetter.getWorldBorder();
		AABB aabb = this.source.getBoundingBox();
		if (!isBoxFullyWithinWorldBorder(worldborder, aabb)) {
			VoxelShape voxelshape = worldborder.getCollisionShape();
			if (!isOutsideBorder(voxelshape, aabb) && isCloseToBorder(voxelshape, aabb)) {
				p_45824_.accept(voxelshape);
				return true;
			}
		}

		return false;
	}

	private static boolean isCloseToBorder(VoxelShape p_45816_, AABB p_45817_) {
		return Shapes.joinIsNotEmpty(p_45816_, Shapes.create(p_45817_.inflate(1.0E-7D)), BooleanOp.AND);
	}

	private static boolean isOutsideBorder(VoxelShape p_45821_, AABB p_45822_) {
		return Shapes.joinIsNotEmpty(p_45821_, Shapes.create(p_45822_.deflate(1.0E-7D)), BooleanOp.AND);
	}

	public static boolean isBoxFullyWithinWorldBorder(WorldBorder p_45813_, AABB p_45814_) {
		double d0 = (double)Mth.floor(p_45813_.getMinX());
		double d1 = (double)Mth.floor(p_45813_.getMinZ());
		double d2 = (double)Mth.ceil(p_45813_.getMaxX());
		double d3 = (double)Mth.ceil(p_45813_.getMaxZ());
		return p_45814_.minX > d0 && p_45814_.minX < d2 && p_45814_.minZ > d1 && p_45814_.minZ < d3 && p_45814_.maxX > d0 && p_45814_.maxX < d2 && p_45814_.maxZ > d1 && p_45814_.maxZ < d3;
	}
}
