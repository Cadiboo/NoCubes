package io.github.cadiboo.nocubes.mesh.generator;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

import static io.github.cadiboo.nocubes.util.ModUtil.DIRECTION_VALUES;
import static io.github.cadiboo.nocubes.util.ModUtil.DIRECTION_VALUES_LENGTH;

/**
 * @author Cadiboo
 * @author Click_Me
 */
public final class OldNoCubes implements MeshGenerator {

	// Points order
	public static final int X0Y0Z0 = 0;
	public static final int X1Y0Z0 = 1;
	public static final int X1Y0Z1 = 2;
	public static final int X0Y0Z1 = 3;
	public static final int X0Y1Z0 = 4;
	public static final int X1Y1Z0 = 5;
	public static final int X1Y1Z1 = 6;
	public static final int X0Y1Z1 = 7;

	/**
	 * @param chunkPos              The position of the chunk
	 * @param reader                The {@link IBlockAccess}
	 * @param isSmoothable          The {@link IsSmoothable} function
	 * @param pooledMutableBlockPos The {@link PooledMutableBlockPos} to use
	 * @return The chunk data
	 */
	// TODO: state caches etc.
	@Nonnull
	public static HashMap<Vec3b, FaceList> generateChunk(@Nonnull final BlockPos chunkPos, @Nonnull final IBlockAccess reader, @Nonnull final IsSmoothable isSmoothable, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {
		final HashMap<Vec3b, FaceList> map = new HashMap<>();
		for (final BlockPos pos : BlockPos.getAllInBoxMutable(chunkPos, chunkPos.add(15, 15, 15))) {

			final FaceList faces = generateBlock(pos, reader, isSmoothable, pooledMutableBlockPos);

			map.put(Vec3b.retain(
					// TODO: {@link ModUtil.getRelativePos}?
					// Convert block pos to relative block pos
					// For example 68 -> 4, 127 -> 15, 4 -> 4, 312312312 -> 8
					(byte) (pos.getX() & 15), (byte) (pos.getY() & 15), (byte) (pos.getZ() & 15)
			), faces);
		}
		return map;
	}

	/**
	 * @param pos                   The position of the block
	 * @param reader                The {@link IBlockAccess}
	 * @param isSmoothable          The {@link IsSmoothable} function
	 * @param pooledMutableBlockPos The {@link PooledMutableBlockPos} to use
	 * @return The face list for the pos
	 */
	@Nonnull
	public static FaceList generateBlock(@Nonnull final BlockPos pos, @Nonnull final IBlockAccess reader, @Nonnull final IsSmoothable isSmoothable, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {

		final int posX = pos.getX();
		final int posY = pos.getY();
		final int posZ = pos.getZ();

		// TODO: {@link ModUtil.getRelativePos}?
		// Convert block pos to relative block pos
		// For example 68 -> 4, 127 -> 15, 4 -> 4, 312312312 -> 8
		final int relativePosX = posX & 15;
		final int relativePosY = posY & 15;
		final int relativePosZ = posZ & 15;

		final IBlockState state = reader.getBlockState(pos);
		final Vec3[] points = getPoints(posX, posY, posZ, relativePosX, relativePosY, relativePosZ, state, reader, isSmoothable, pooledMutableBlockPos);
		final FaceList faces = FaceList.retain();

		if (points != null) {
			for (int i = 0; i < DIRECTION_VALUES_LENGTH; ++i) {
				final EnumFacing direction = DIRECTION_VALUES[i];
				if (isSmoothable.test(reader.getBlockState(pooledMutableBlockPos.setPos(pos).move(direction)))) {
					continue;
				}

				final Vec3 vertex0;
				final Vec3 vertex1;
				final Vec3 vertex2;
				final Vec3 vertex3;

				//0-3
				//1-2
				//0,0-1,0
				//0,1-1,1
				switch (direction) {
					default:
					case DOWN:
						vertex0 = points[X0Y0Z1];
						vertex1 = points[X0Y0Z0];
						vertex2 = points[X1Y0Z0];
						vertex3 = points[X1Y0Z1];
						break;
					case UP:
						vertex0 = points[X0Y1Z0];
						vertex1 = points[X0Y1Z1];
						vertex2 = points[X1Y1Z1];
						vertex3 = points[X1Y1Z0];
						break;
					case NORTH:
						vertex0 = points[X1Y1Z0];
						vertex1 = points[X1Y0Z0];
						vertex2 = points[X0Y0Z0];
						vertex3 = points[X0Y1Z0];
						break;
					case SOUTH:
						vertex0 = points[X0Y1Z1];
						vertex1 = points[X0Y0Z1];
						vertex2 = points[X1Y0Z1];
						vertex3 = points[X1Y1Z1];
						break;
					case WEST:
						vertex0 = points[X0Y1Z0];
						vertex1 = points[X0Y0Z0];
						vertex2 = points[X0Y0Z1];
						vertex3 = points[X0Y1Z1];
						break;
					case EAST:
						vertex0 = points[X1Y1Z1];
						vertex1 = points[X1Y0Z1];
						vertex2 = points[X1Y0Z0];
						vertex3 = points[X1Y1Z0];
						break;
				}

				faces.add(Face.retain(
						Vec3.retain(vertex0.x, vertex0.y, vertex0.z),
						Vec3.retain(vertex1.x, vertex1.y, vertex1.z),
						Vec3.retain(vertex2.x, vertex2.y, vertex2.z),
						Vec3.retain(vertex3.x, vertex3.y, vertex3.z)
				));
			}

			for (int i = 0, pointsLength = points.length; i < pointsLength; i++) {
				points[i].close();
			}

		}
		return faces;
	}

	/**
	 * @param posX                  The X position of the block
	 * @param posY                  The Y position of the block
	 * @param posZ                  The Z position of the block
	 * @param relativePosX          I've forgotten what, if anything, this is meant to be used for
	 * @param relativePosY          I've forgotten what, if anything, this is meant to be used for
	 * @param relativePosZ          I've forgotten what, if anything, this is meant to be used for
	 * @param state                 The {@link IBlockState}
	 * @param reader                The {@link IBlockAccess}
	 * @param isSmoothable          The {@link IsSmoothable} function
	 * @param pooledMutableBlockPos The {@link PooledMutableBlockPos} to use
	 * @return The (smoothed) 8 points that make the block
	 */
	@Nullable
	public static Vec3[] getPoints(final int posX, final int posY, final int posZ, int relativePosX, int relativePosY, int relativePosZ, @Nonnull final IBlockState state, @Nonnull final IBlockAccess reader, @Nonnull final IsSmoothable isSmoothable, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {

		if (!isSmoothable.test(state)) {
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
			point.x += posX;
			point.y += posY;
			point.z += posZ;

			if (!doesPointIntersectWithManufactured(reader, point, isSmoothable, pooledMutableBlockPos)) {
				if ((pointIndex < 4) && (doesPointBottomIntersectWithAir(reader, point, pooledMutableBlockPos))) {
					point.y = posY + 1.0F - 0.0001F; // - 0.0001F to prevent z-fighting
				} else if ((pointIndex >= 4) && (doesPointTopIntersectWithAir(reader, point, pooledMutableBlockPos))) {
					point.y = posY + 0.0F + 0.0001F; // + 0.0001F to prevent z-fighting
				}
//				if (ModConfig.offsetVertices) {
//					givePointRoughness(point);
//				}
			}

		}

		return points;
	}

	/**
	 * Click_Me's equivalent of our ModUtil.offsetVertex
	 */
	@Nonnull
	public static Vec3 givePointRoughness(@Nonnull final Vec3 point) {
		long i = (long) (point.x * 3129871.0D) ^ (long) point.y * 116129781L ^ (long) point.z;

		i = i * i * 42317861L + i * 11L;
		point.x += ((float) (i >> 16 & 0xF) / 15.0F - 0.5F) * 0.5F;
		point.y += ((float) (i >> 20 & 0xF) / 15.0F - 0.5F) * 0.5F;
		point.z += ((float) (i >> 24 & 0xF) / 15.0F - 0.5F) * 0.5F;
		return point;
	}

	/**
	 * Check if the state is AIR or PLANT or VINE
	 *
	 * @param state the state
	 * @return if the state is AIR or PLANT or VINE
	 */
	public static boolean isBlockAirOrPlant(@Nonnull final IBlockState state) {
		Material material = state.getMaterial();
		return (material == Material.AIR) || (material == Material.PLANTS) || (material == Material.VINE);
	}

	/**
	 * Check if the block's top side intersects with air.
	 *
	 * @param world                 the world to use
	 * @param point                 the point to use
	 * @param pooledMutableBlockPos the pooled mutable pos to use
	 * @return if the block's top side intersects with air.
	 */
	public static boolean doesPointTopIntersectWithAir(@Nonnull final IBlockAccess world, @Nonnull final Vec3 point, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {
		boolean intersects = false;
		for (int i = 0; i < 4; i++) {
			int x1 = (int) (point.x - (i & 0x1));
			int z1 = (int) (point.z - (i >> 1 & 0x1));
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
	 * @param world                 the world to use
	 * @param point                 the point to use
	 * @param pooledMutableBlockPos the pooled mutable pos to use
	 * @return if the block's bottom side intersects with air.
	 */
	public static boolean doesPointBottomIntersectWithAir(@Nonnull final IBlockAccess world, @Nonnull final Vec3 point, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {
		boolean intersects = false;
		boolean notOnly = false;
		for (int i = 0; i < 4; i++) {
			int x1 = (int) (point.x - (i & 0x1));
			int z1 = (int) (point.z - (i >> 1 & 0x1));
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
		return (intersects) && (notOnly);
	}

	public static boolean doesPointIntersectWithManufactured(@Nonnull final IBlockAccess world, @Nonnull final Vec3 point, @Nonnull final IsSmoothable isSmoothable, @Nonnull final PooledMutableBlockPos pooledMutableBlockPos) {
		for (int i = 0; i < 4; i++) {
			int x1 = (int) (point.x - (i & 0x1));
			int z1 = (int) (point.z - (i >> 1 & 0x1));
			IBlockState state0 = world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y, z1));
			if ((!isBlockAirOrPlant(state0)) && (!isSmoothable.test(state0))) {
				return true;
			}
			IBlockState state1 = world.getBlockState(pooledMutableBlockPos.setPos(x1, (int) point.y - 1, z1));
			if ((!isBlockAirOrPlant(state1)) && (!isSmoothable.test(state1))) {
				return true;
			}
		}
		return false;
	}

	@Override
	@Nonnull
	public HashMap<Vec3b, FaceList> generateChunk(@Nonnull final float[] scalarFieldData, @Nonnull final byte[] dimensions) {
		throw new UnsupportedOperationException();
	}

	@Override
	@Nonnull
	public FaceList generateBlock(@Nonnull final float[] scalarFieldData, @Nonnull final byte[] dimensions) {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public FaceList generateBlock(@Nonnull final BlockPos pos, @Nonnull final IBlockAccess reader, @Nonnull final IsSmoothable isSmoothable) {
		throw new UnsupportedOperationException();
	}

}
