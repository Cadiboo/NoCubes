package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.IMeshGenerator;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.IIsSmoothable;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.nocubes.util.Vec3b;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * @author Cadiboo
 * @author Click_Me
 */
public class OldNoCubes implements IMeshGenerator {

	@Nonnull
	@Override
	public HashMap<Vec3b, FaceList> generateChunk(final float[] scalarFieldData, final byte[] dimensions) {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public FaceList generateBlock(final byte[] position, final float[] neighbourDensityGrid) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @param chunkPos              the position of the chunk
	 * @param blockAccess           the IBlockAccess
	 * @param isSmoothable          the smoothable function
	 * @param pooledMutableBlockPos
	 * @return the chunk data
	 */
	// TODO: state caches etc.
	@Nonnull
	public static HashMap<Vec3b, FaceList> generateChunk(@Nonnull final BlockPos chunkPos, @Nonnull final IBlockAccess blockAccess, @Nonnull final IIsSmoothable isSmoothable, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {
		final HashMap<Vec3b, FaceList> map = new HashMap<>();
		for (final MutableBlockPos pos : BlockPos.getAllInBoxMutable(chunkPos, chunkPos.add(15, 15, 15))) {

			final int posX = pos.getX();
			final int posY = pos.getY();
			final int posZ = pos.getZ();

			// Convert block pos to relative block pos
			// For example 68 -> 4, 127 -> 15, 4 -> 4, 312312312 -> 8
			final int relativePosX = posX & 15;
			final int relativePosY = posY & 15;
			final int relativePosZ = posZ & 15;

			final IBlockState state = blockAccess.getBlockState(pos);
			final Vec3[] points = getPoints(posX, posY, posZ, relativePosX, relativePosY, relativePosZ, state, blockAccess, isSmoothable, pooledMutableBlockPos);
			final FaceList faces = FaceList.retain();

			if (points != null) {
				for (final EnumFacing facing : EnumFacing.VALUES) {
					if (isSmoothable.isSmoothable(blockAccess.getBlockState(pooledMutableBlockPos.setPos(pos).offset(facing)))) {
						continue;
					}

					final Vec3 vertex0;
					final Vec3 vertex1;
					final Vec3 vertex2;
					final Vec3 vertex3;

					switch (facing) {
						default:
						case DOWN:
							vertex0 = points[0];
							vertex1 = points[1];
							vertex2 = points[2];
							vertex3 = points[3];
							break;
						case UP:
							vertex0 = points[7];
							vertex1 = points[6];
							vertex2 = points[5];
							vertex3 = points[4];
							break;
						case NORTH:
							vertex0 = points[1];
							vertex1 = points[0];
							vertex2 = points[4];
							vertex3 = points[5];
							break;
						case SOUTH:
							vertex0 = points[6];
							vertex1 = points[7];
							vertex2 = points[3];
							vertex3 = points[2];
							break;
						case WEST:
							vertex0 = points[0];
							vertex1 = points[3];
							vertex2 = points[7];
							vertex3 = points[4];
							break;
						case EAST:
							vertex0 = points[5];
							vertex1 = points[6];
							vertex2 = points[2];
							vertex3 = points[1];
							break;
					}

					faces.add(Face.retain(
							Vec3.retain(vertex0.x, vertex0.y, vertex0.z),
							Vec3.retain(vertex1.x, vertex1.y, vertex1.z),
							Vec3.retain(vertex2.x, vertex2.y, vertex2.z),
							Vec3.retain(vertex3.x, vertex3.y, vertex3.z)
					));
				}

				for (final Vec3 point : points) {
					point.close();
				}

			}
			map.put(Vec3b.retain((byte) relativePosX, (byte) relativePosY, (byte) relativePosZ), faces);
		}
		return map;
	}

	@Nullable
	public static Vec3[] getPoints(final int posX, final int posY, final int posZ, int relativePosX, int relativePosY, int relativePosZ, @Nonnull final IBlockState state, @Nonnull final IBlockAccess cache, @Nonnull final IIsSmoothable isSmoothable, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {

		if (!isSmoothable.isSmoothable(state)) {
			return null;
		}

		// The 8 points that make the block.
		// 1 point for each corner
		final Vec3[] points = {
				Vec3.retain(0, 0, 0),
				Vec3.retain(1, 0, 0),
				Vec3.retain(1, 0, 1),
				Vec3.retain(0, 0, 1),
				Vec3.retain(0, 1, 0),
				Vec3.retain(1, 1, 0),
				Vec3.retain(1, 1, 1),
				Vec3.retain(0, 1, 1),
		};

		// Loop through all the points:
		// Here everything will be 'smoothed'.
		for (int pointIndex = 0; pointIndex < 8; ++pointIndex) {

			final Vec3 point = points[pointIndex];

			// Give the point the block's coordinates.
			point.x += (double) posX;
			point.y += (double) posY;
			point.z += (double) posZ;

			// Check if the point is intersecting with a smoothable block.
			if (doesPointIntersectWithSmoothable(cache, point, isSmoothable, pooledMutableBlockPos)) {
				if (pointIndex < 4 && doesPointBottomIntersectWithAir(cache, point, pooledMutableBlockPos)) {
					point.y = (float) posY + 1.0F;
				} else if (pointIndex >= 4 && doesPointTopIntersectWithAir(cache, point, pooledMutableBlockPos)) {
					point.y = (float) posY + 0.0002F; // + 0.0002F to prevent z-fighting
				}
			}

			// Subtract the block's coordinates.
			point.x -= (double) posX;
			point.y -= (double) posY;
			point.z -= (double) posZ;

			// Add the block's relative coordinates
			point.addOffset(relativePosX, relativePosY, relativePosZ);

		}

		return points;

	}

	/**
	 * Check if the state is AIR or PLANT or VINE
	 *
	 * @param state the state
	 * @return if the state is AIR or PLANT or VINE
	 */
	public static boolean isBlockAirOrPlant(IBlockState state) {
		Material material = state.getMaterial();
		return material == Material.AIR || material == Material.PLANTS || material == Material.VINE;
	}

	/**
	 * Check if the block's top side intersects with air.
	 *
	 * @param world                 the world
	 * @param point                 the point
	 * @param pooledMutableBlockPos
	 * @return if the block's top side intersects with air.
	 */
	public static boolean doesPointTopIntersectWithAir(@Nonnull final IBlockAccess world, @Nonnull final Vec3 point, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {
		boolean intersects = false;

		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (point.x - (double) (i & 1));
			int z1 = (int) (point.z - (double) (i >> 1 & 1));
			if (!isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y, z1)))) {
				return false;
			}

			if (isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y - 1, z1)))) {
				intersects = true;
			}
		}

		return intersects;
	}

	/**
	 * Check if the block's bottom side intersects with air.
	 *
	 * @param world                 the world
	 * @param point                 the point
	 * @param pooledMutableBlockPos
	 * @return if the block's bottom side intersects with air.
	 */
	public static boolean doesPointBottomIntersectWithAir(@Nonnull final IBlockAccess world, @Nonnull final Vec3 point, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {
		boolean intersects = false;
		boolean notOnly = false;

		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (point.x - (double) (i & 1));
			int z1 = (int) (point.z - (double) (i >> 1 & 1));
			if (!isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y - 1, z1)))) {
				return false;
			}

			if (!isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y + 1, z1)))) {
				notOnly = true;
			}

			if (isBlockAirOrPlant(world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y, z1)))) {
				intersects = true;
			}
		}

		return intersects && notOnly;
	}

	/**
	 * Check if the point is intersecting with a smoothable block.
	 *
	 * @param world        the world
	 * @param point        the point
	 * @param isSmoothable the smoothable function
	 * @return if the point is intersecting with a smoothable block.
	 */
	public static boolean doesPointIntersectWithSmoothable(@Nonnull final IBlockAccess world, @Nonnull final Vec3 point, @Nonnull final IIsSmoothable isSmoothable, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {
		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (point.x - (float) (i & 1));
			int z1 = (int) (point.y - (float) (i >> 1 & 1));
			IBlockState block = world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y, z1));
			if (!isBlockAirOrPlant(block) && !isSmoothable.isSmoothable(block)) {
				return false;
			}

			IBlockState block1 = world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y - 1, z1));
			if (!isBlockAirOrPlant(block1) && !isSmoothable.isSmoothable(block1)) {
				return false;
			}
		}
		return true;
	}

}
