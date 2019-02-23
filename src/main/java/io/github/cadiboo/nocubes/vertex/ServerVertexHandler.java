package io.github.cadiboo.nocubes.vertex;

import io.github.cadiboo.nocubes.util.FaceList;
import io.github.cadiboo.nocubes.util.Vec3b;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReaderBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * @author Cadiboo
 */
public class ServerVertexHandler implements IVertexHandler {

	private static final HashMap<BlockPos, HashMap<Vec3b, FaceList>> VERTICES = new HashMap<>();

	@Nonnull
	@Override
	public HashMap<Vec3b, FaceList> generateChunkVertices(@Nonnull final IWorldReaderBase world, @Nonnull BlockPos chunkPos) {

		chunkPos = IVertexHandler.toImmutableCubeChunkPos(chunkPos);

		final HashMap<Vec3b, FaceList> chunkData = VERTICES.get(chunkPos);
		if (chunkData != null) {
			IVertexHandler.closeChunk(chunkData);
			VERTICES.remove(chunkPos);
		}

		final HashMap<Vec3b, FaceList> chunkVertices = IVertexHandler.calcChunkVertices(world, chunkPos);
		VERTICES.put(chunkPos, chunkVertices);
		return chunkVertices;
	}

	@Nullable
	@Override
	public FaceList getFaces(@Nonnull final IBlockReader world, @Nonnull final PooledMutableBlockPos pos) {
		try (PooledMutableBlockPos chunkPos = IVertexHandler.setCubeChunkPooledPos(PooledMutableBlockPos.retain(pos.getX(), pos.getY(), pos.getZ()))) {
			try (Vec3b vec3b = Vec3b.retain(
					(byte) (pos.getX() - chunkPos.getX()),
					(byte) (pos.getY() - chunkPos.getY()),
					(byte) (pos.getZ() - chunkPos.getZ())
			)) {
				final HashMap<Vec3b, FaceList> chunkData = VERTICES.get(chunkPos);
				if (chunkData == null) {
					return null;
				}
				return chunkData.get(vec3b);
			}
		}
	}

	@Nullable
	@Override
	public HashMap<Vec3b, FaceList> getChunkData(@Nonnull final IWorldReaderBase world, @Nonnull final BlockPos chunkPos) {
		return VERTICES.get(IVertexHandler.toImmutableCubeChunkPos(chunkPos));
	}

}
