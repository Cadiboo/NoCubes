package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.CollisionSpliterator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

public class NoCubesCollisionSpliterator extends CollisionSpliterator {

	private final BiPredicate<BlockState, BlockPos> originalPredicate;
	private Deque<VoxelShape> shapes = null;

	// Called via ASM
	public NoCubesCollisionSpliterator(CollisionGetter world, @Nullable Entity entity, AABB area) {
		this(world, entity, area, (a, b) -> true);
	}

	// Called via ASM
	public NoCubesCollisionSpliterator(CollisionGetter world, @Nullable Entity entity, AABB area, BiPredicate<BlockState, BlockPos> predicate) {
		super(world, entity, area, predicate.and((state, pos) -> !NoCubes.smoothableHandler.isSmoothable(state)));
		this.originalPredicate = predicate;
	}

	@Override
	public boolean tryAdvance(@Nonnull Consumer<? super VoxelShape> action) {
		if (this.needsBorderCheck && this.worldBorderCheck(action))
			return true;

		if (NoCubesConfig.Server.collisionsEnabled) {
			if (shapes == null)
				shapes = CollisionHandler.createNoCubesIntersectingCollisionList(this.collisionGetter, this.box, this.pos);
			while (!shapes.isEmpty()) {
				VoxelShape shape = shapes.pop();
//			   if (!this.box.intersects(
//				  shape.min(Direction.Axis.X), shape.min(Direction.Axis.Y), shape.min(Direction.Axis.Z),
//				  shape.max(Direction.Axis.X), shape.max(Direction.Axis.Y), shape.max(Direction.Axis.Z)
//			   ))
//				   continue;
				if (!Shapes.joinIsNotEmpty(shape, this.entityShape, BooleanOp.AND))
					continue;
				action.accept(shape);
				return true;
			}
		}
		return this.collisionCheck(action);
	}
}
