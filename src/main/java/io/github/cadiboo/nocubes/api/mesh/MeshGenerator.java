package io.github.cadiboo.nocubes.api.mesh;

import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.api.util.pooled.Face;
import io.github.cadiboo.nocubes.api.util.pooled.FaceList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;

/**
 * Interface defining a generator.
 *
 * @author Cadiboo
 */
public interface MeshGenerator {

	/**
	 * Generates a {@link FaceList} for a block at a position in the world using the specified IsSmoothable function.
	 *
	 * @param pos                   The position of the block
	 * @param reader                The reader to use for queries
	 * @param isSmoothable          The {@link IsSmoothable} function to apply
	 * @param pooledMutableBlockPos The {@link BlockPos} to use for queries (Optimisation to reduce the number of new objects created)
	 * @return A {@link FaceList} containing the list of {@link Face}s for the block. Can be empty
	 */
	@Nonnull
	FaceList generateBlock(@Nonnull final BlockPos pos, @Nonnull final IBlockReader reader, @Nonnull final IsSmoothable isSmoothable, @Nonnull final BlockPos.PooledMutable pooledMutableBlockPos);

}
