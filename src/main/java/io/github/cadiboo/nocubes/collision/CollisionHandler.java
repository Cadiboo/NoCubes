package io.github.cadiboo.nocubes.collision;

import com.google.common.collect.ImmutableList;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.hooks.AddCollisionBoxToListHook;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static io.github.cadiboo.nocubes.util.ModUtil.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;
import static java.lang.Math.max;

/**
 * This is 95% coppied from Repose
 *
 * @author Cadiboo
 */
@SuppressWarnings({
		"unused", // Hooks get invoked by ASM redirects
		"weakerAccess" // Hooks need to be public to be invoked
})
public final class CollisionHandler {

	private enum Direction {

		North(0, -1),
		South(0, 1),
		East(1, 0),
		West(-1, 0),

		//val NorthEast = North + East
		NorthEast(North.x + East.x, North.z + East.z),
		NorthWest(North.x + West.x, North.z + West.z),
		SouthEast(South.x + East.x, South.z + East.z),
		SouthWest(South.x + West.x, South.z + West.z);
		public static final ImmutableList<Direction> OrdinalDirections = ImmutableList.of(NorthEast, NorthWest, SouthEast, SouthWest);

		private final int x;
		private final int z;

		Direction(final int x, final int z) {
			this.x = x;
			this.z = z;
		}
	}

	public static void addCollisionBoxToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {

		if (!TERRAIN_SMOOTHABLE.isSmoothable(state)) {
			if (LEAVES_SMOOTHABLE.isSmoothable(state)) {
				if (entityIn != null) {
					// TODO
					entityIn.motionX *= 0.99;
//					entityIn.motionY *= 0.99;
					entityIn.motionZ *= 0.99;
				}
			} else {
				AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
			}
			return;
		}

		//NOOOOOO
//		final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlock(pos, worldIn, TERRAIN_SMOOTHABLE);
//		if (faces != null) {
//			if (faces.isEmpty()) {
//				return;
//			}
//			faces.forEach(face -> {
//				face.getVertex0().close();
//				face.getVertex1().close();
//				face.getVertex2().close();
//				face.getVertex3().close();
//				face.close();
//			});
//			faces.close();
//		}

//		final AxisAlignedBB collisionBoundingBox = state.getCollisionBoundingBox(worldIn, pos);
//		if (collisionBoundingBox != null) { // optimization
//			if (canUseSlope(entityIn) && canSlopeAt(state, worldIn, pos, collisionBoundingBox)) {
//				collidingBoxes.addAll(slopingCollisionBoxes(state, worldIn, pos).stream().filter(entityBox::intersects).collect(Collectors.toList()));
//			} else {
//				AddCollisionBoxToListHook.addCollisionBoxToListDefault(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
//			}
//		}

		addMeshCollisionBoxesToList(block, state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
	}

	public static List<AxisAlignedBB> slopingCollisionBoxes(final IBlockState state, World world, final BlockPos pos) {
		final double height = blockHeight(pos, world, world.getBlockState(pos));
		final ArrayList<AxisAlignedBB> list = new ArrayList<>();
		for (Direction direction : Direction.OrdinalDirections) {
			list.add(cornerBox(pos, direction, height, world));
		}
		return list;
	}

	private static AxisAlignedBB cornerBox(final BlockPos pos, Direction direction, double blockHeight, World world) {
		double stepHeight = blockHeight - 0.5;
		final double height;
		if (stepHigh(pos.add(direction.x, 0, 0), stepHeight, world) &&
				stepHigh(pos.add(0, 0, direction.z), stepHeight, world) &&
				stepHigh(pos.add(direction.x, 0, direction.z), stepHeight, world)) {
			height = blockHeight;
		} else {
			height = stepHeight;
		}

		return new AxisAlignedBB(
				pos.getX() + max(0.0, direction.x / 2.0), pos.getY(), pos.getZ() + max(0.0, direction.z / 2.0),
				pos.getX() + max(0.5, direction.x), pos.getY() + height, pos.getZ() + max(0.5, direction.z)
		);
	}

	private static boolean stepHigh(final BlockPos offsetPos, final double stepHeight, World world) {
		final IBlockState neighbor = world.getBlockState(offsetPos);
		return neighbor.getBlock().isTopSolid(neighbor) && blockHeight(offsetPos, world, neighbor) >= stepHeight;
	}

	private static double blockHeight(final BlockPos pos, World world, final IBlockState blockState) {
		AxisAlignedBB box = blockState.getCollisionBoundingBox(world, pos);
		return box == null ? 0 : box.maxY;
	}

	public static boolean canSlopeAt(final IBlockState state, World worldIn, final BlockPos pos, final AxisAlignedBB collisionBoundingBox) {
		boolean flag = collisionBoundingBox != null && collisionBoundingBox.maxY > 0.5;
		final BlockPos posUp = pos.up();
		return TERRAIN_SMOOTHABLE.isSmoothable(state) && flag && worldIn.getBlockState(posUp).getCollisionBoundingBox(worldIn, posUp) == null;
	}

	private static void addMeshCollisionBoxesToList(Block block, IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {

		final float boxRadius = 0.0625F;
		final boolean ignoreIntersects = false;

		worldIn.profiler.startSection("addMeshCollisionBoxesToList");

		try (final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlockMeshOffset(pos, worldIn, TERRAIN_SMOOTHABLE, ModConfig.terrainMeshGenerator)) {
			for (Face face : faces) {
				// Ew, Yay, Java 8 variable try-with-resources support
				try {

					//0___3
					//_____
					//_____
					//_____
					//1___2
					final Vec3 v0 = face.getVertex0();
					final Vec3 v1 = face.getVertex1();
					final Vec3 v2 = face.getVertex2();
					final Vec3 v3 = face.getVertex3();

					//0_*_3
					//_____
					//*___*
					//_____
					//1_*_2
					final Vec3 v0v1 = interp(v0, v1, 0.5F);
					final Vec3 v1v2 = interp(v1, v2, 0.5F);
					final Vec3 v2v3 = interp(v2, v3, 0.5F);
					final Vec3 v3v0 = interp(v3, v0, 0.5F);

					//0x*x3
					//x___x
					//*___*
					//x___x
					//1x*x2
					final Vec3 v0v1v0 = interp(v0v1, v0, 0.5F);
					final Vec3 v0v1v1 = interp(v0v1, v1, 0.5F);
					final Vec3 v1v2v1 = interp(v1v2, v1, 0.5F);
					final Vec3 v1v2v2 = interp(v1v2, v2, 0.5F);
					final Vec3 v2v3v2 = interp(v2v3, v2, 0.5F);
					final Vec3 v2v3v3 = interp(v2v3, v3, 0.5F);
					final Vec3 v3v0v3 = interp(v3v0, v3, 0.5F);
					final Vec3 v3v0v0 = interp(v3v0, v0, 0.5F);

					//0x*x3
					//xa_ax
					//*___*
					//xa_ax
					//1x*x2
					final Vec3 v0v1v1v2 = interp(v0v1, v1v2, 0.5F);
					final Vec3 v1v2v2v3 = interp(v1v2, v2v3, 0.5F);
					final Vec3 v2v3v3v0 = interp(v2v3, v3v0, 0.5F);
					final Vec3 v3v0v0v1 = interp(v3v0, v0v1, 0.5F);

					//0x*x3
					//xabax
					//*b_b*
					//xabax
					//1x*x2
					final Vec3 v0v1v1v2v1v2v2v3 = interp(v0v1v1v2, v1v2v2v3, 0.5F);
					final Vec3 v1v2v2v3v2v3v3v0 = interp(v1v2v2v3, v2v3v3v0, 0.5F);
					final Vec3 v2v3v3v0v3v0v0v1 = interp(v2v3v3v0, v3v0v0v1, 0.5F);
					final Vec3 v3v0v0v1v0v1v1v2 = interp(v3v0v0v1, v0v1v1v2, 0.5F);

					//0x*x3
					//xabax
					//*bcb*
					//xabax
					//1x*x2
					final Vec3 v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1 = interp(v0v1v1v2v1v2v2v3, v2v3v3v0v3v0v0v1, 0.5F);
					final Vec3 v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2 = interp(v1v2v2v3v2v3v3v0, v3v0v0v1v0v1v1v2, 0.5F);

					{

						//0___3
						//_____
						//_____
						//_____
						//1___2
						final AxisAlignedBB v0box = createAxisAlignedBBForVertex(v0, boxRadius);
						final AxisAlignedBB v1box = createAxisAlignedBBForVertex(v1, boxRadius);
						final AxisAlignedBB v2box = createAxisAlignedBBForVertex(v2, boxRadius);
						final AxisAlignedBB v3box = createAxisAlignedBBForVertex(v3, boxRadius);

						//0_*_3
						//_____
						//*___*
						//_____
						//1_*_2
						final AxisAlignedBB v0v1box = createAxisAlignedBBForVertex(v0v1, boxRadius);
						final AxisAlignedBB v1v2box = createAxisAlignedBBForVertex(v1v2, boxRadius);
						final AxisAlignedBB v2v3box = createAxisAlignedBBForVertex(v2v3, boxRadius);
						final AxisAlignedBB v3v0box = createAxisAlignedBBForVertex(v3v0, boxRadius);

						//0x*x3
						//x___x
						//*___*
						//x___x
						//1x*x2
						final AxisAlignedBB v0v1v0box = createAxisAlignedBBForVertex(v0v1v0, boxRadius);
						final AxisAlignedBB v0v1v1box = createAxisAlignedBBForVertex(v0v1v1, boxRadius);
						final AxisAlignedBB v1v2v1box = createAxisAlignedBBForVertex(v1v2v1, boxRadius);
						final AxisAlignedBB v1v2v2box = createAxisAlignedBBForVertex(v1v2v2, boxRadius);
						final AxisAlignedBB v2v3v2box = createAxisAlignedBBForVertex(v2v3v2, boxRadius);
						final AxisAlignedBB v2v3v3box = createAxisAlignedBBForVertex(v2v3v3, boxRadius);
						final AxisAlignedBB v3v0v3box = createAxisAlignedBBForVertex(v3v0v3, boxRadius);
						final AxisAlignedBB v3v0v0box = createAxisAlignedBBForVertex(v3v0v0, boxRadius);

						//0x*x3
						//xa_ax
						//*___*
						//xa_ax
						//1x*x2
						final AxisAlignedBB v0v1v1v2box = createAxisAlignedBBForVertex(v0v1v1v2, boxRadius);
						final AxisAlignedBB v1v2v2v3box = createAxisAlignedBBForVertex(v1v2v2v3, boxRadius);
						final AxisAlignedBB v2v3v3v0box = createAxisAlignedBBForVertex(v2v3v3v0, boxRadius);
						final AxisAlignedBB v3v0v0v1box = createAxisAlignedBBForVertex(v3v0v0v1, boxRadius);

						//0x*x3
						//xabax
						//*b_b*
						//xabax
						//1x*x2
						final AxisAlignedBB v0v1v1v2v1v2v2v3box = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3, boxRadius);
						final AxisAlignedBB v1v2v2v3v2v3v3v0box = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0, boxRadius);
						final AxisAlignedBB v2v3v3v0v3v0v0v1box = createAxisAlignedBBForVertex(v2v3v3v0v3v0v0v1, boxRadius);
						final AxisAlignedBB v3v0v0v1v0v1v1v2box = createAxisAlignedBBForVertex(v3v0v0v1v0v1v1v2, boxRadius);

						//0x*x3
						//xabax
						//*bcb*
						//xabax
						//1x*x2
						final AxisAlignedBB v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, boxRadius);
						final AxisAlignedBB v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, boxRadius);

						{

							//0___3
							//_____
							//_____
							//_____
							//1___2
							if (ignoreIntersects || entityBox.intersects(v0box)) {
								collidingBoxes.add(v0box);
							}
							if (ignoreIntersects || entityBox.intersects(v1box)) {
								collidingBoxes.add(v1box);
							}
							if (ignoreIntersects || entityBox.intersects(v2box)) {
								collidingBoxes.add(v2box);
							}
							if (ignoreIntersects || entityBox.intersects(v3box)) {
								collidingBoxes.add(v3box);
							}

							//0_*_3
							//_____
							//*___*
							//_____
							//1_*_2
							if (ignoreIntersects || entityBox.intersects(v0v1box)) {
								collidingBoxes.add(v0v1box);
							}
							if (ignoreIntersects || entityBox.intersects(v1v2box)) {
								collidingBoxes.add(v1v2box);
							}
							if (ignoreIntersects || entityBox.intersects(v2v3box)) {
								collidingBoxes.add(v2v3box);
							}
							if (ignoreIntersects || entityBox.intersects(v3v0box)) {
								collidingBoxes.add(v3v0box);
							}

							//0x*x3
							//x___x
							//*___*
							//x___x
							//1x*x2
							if (ignoreIntersects || entityBox.intersects(v0v1v0box)) {
								collidingBoxes.add(v0v1v0box);
							}
							if (ignoreIntersects || entityBox.intersects(v0v1v1box)) {
								collidingBoxes.add(v0v1v1box);
							}
							if (ignoreIntersects || entityBox.intersects(v1v2v1box)) {
								collidingBoxes.add(v1v2v1box);
							}
							if (ignoreIntersects || entityBox.intersects(v1v2v2box)) {
								collidingBoxes.add(v1v2v2box);
							}
							if (ignoreIntersects || entityBox.intersects(v2v3v2box)) {
								collidingBoxes.add(v2v3v2box);
							}
							if (ignoreIntersects || entityBox.intersects(v2v3v3box)) {
								collidingBoxes.add(v2v3v3box);
							}
							if (ignoreIntersects || entityBox.intersects(v3v0v3box)) {
								collidingBoxes.add(v3v0v3box);
							}
							if (ignoreIntersects || entityBox.intersects(v3v0v0box)) {
								collidingBoxes.add(v3v0v0box);
							}

							//0x*x3
							//xa_ax
							//*___*
							//xa_ax
							//1x*x2
							if (ignoreIntersects || entityBox.intersects(v0v1v1v2box)) {
								collidingBoxes.add(v0v1v1v2box);
							}
							if (ignoreIntersects || entityBox.intersects(v1v2v2v3box)) {
								collidingBoxes.add(v1v2v2v3box);
							}
							if (ignoreIntersects || entityBox.intersects(v2v3v3v0box)) {
								collidingBoxes.add(v2v3v3v0box);
							}
							if (ignoreIntersects || entityBox.intersects(v3v0v0v1box)) {
								collidingBoxes.add(v3v0v0v1box);
							}

							//0x*x3
							//xabax
							//*b_b*
							//xabax
							//1x*x2
							if (ignoreIntersects || entityBox.intersects(v0v1v1v2v1v2v2v3box)) {
								collidingBoxes.add(v0v1v1v2v1v2v2v3box);
							}
							if (ignoreIntersects || entityBox.intersects(v1v2v2v3v2v3v3v0box)) {
								collidingBoxes.add(v1v2v2v3v2v3v3v0box);
							}
							if (ignoreIntersects || entityBox.intersects(v2v3v3v0v3v0v0v1box)) {
								collidingBoxes.add(v2v3v3v0v3v0v0v1box);
							}
							if (ignoreIntersects || entityBox.intersects(v3v0v0v1v0v1v1v2box)) {
								collidingBoxes.add(v3v0v0v1v0v1v1v2box);
							}

							//0x*x3
							//xabax
							//*bcb*
							//xabax
							//1x*x2
							if (ignoreIntersects || entityBox.intersects(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box)) {
								collidingBoxes.add(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1box);
							}
							if (ignoreIntersects || entityBox.intersects(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box)) {
								collidingBoxes.add(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2box);
							}

						}
					}

					//0___3
					//_____
					//_____
					//_____
					//1___2
					v0.close();
					v1.close();
					v2.close();
					v3.close();

					//0_*_3
					//_____
					//*___*
					//_____
					//1_*_2
					v0v1.close();
					v1v2.close();
					v2v3.close();
					v3v0.close();

					//0x*x3
					//x___x
					//*___*
					//x___x
					//1x*x2
					v0v1v0.close();
					v0v1v1.close();
					v1v2v1.close();
					v1v2v2.close();
					v2v3v2.close();
					v2v3v3.close();
					v3v0v3.close();
					v3v0v0.close();

					//0x*x3
					//xa_ax
					//*___*
					//xa_ax
					//1x*x2
					v0v1v1v2.close();
					v1v2v2v3.close();
					v2v3v3v0.close();
					v3v0v0v1.close();

					//0x*x3
					//xabax
					//*b_b*
					//xabax
					//1x*x2
					v0v1v1v2v1v2v2v3.close();
					v1v2v2v3v2v3v3v0.close();
					v2v3v3v0v3v0v0v1.close();
					v3v0v0v1v0v1v1v2.close();

					//0x*x3
					//xabax
					//*bcb*
					//xabax
					//1x*x2
					v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1.close();
					v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2.close();

				} finally {
					face.close();
				}
			}
		} finally {
			worldIn.profiler.endSection();
		}
//		if(collidingBoxes.isEmpty()) {
//			collidingBoxes.add(new AxisAlignedBB(0, -100, 0, 0, -101, 0));
//		}

	}

	private static Vec3 interp(final Vec3 v0, final Vec3 v1, final float t) {
		return Vec3.retain(
				v0.x + t * (v1.x - v0.x),
				v0.y + t * (v1.y - v0.y),
				v0.z + t * (v1.z - v0.z)
		);

	}

	public static boolean canUseSlope(final Entity entity) {
//		return entity instanceof EntityPlayer || entity instanceof EntityCreature;
		return true;
	}

	private static AxisAlignedBB createAxisAlignedBBForVertex(final Vec3 vec3, final float boxRadius) {
		return new AxisAlignedBB(
				vec3.x - boxRadius,
				vec3.y - boxRadius,
				vec3.z - boxRadius,
				vec3.x + boxRadius,
				vec3.y + boxRadius,
				vec3.z + boxRadius
		);
	}

}
