package io.github.cadiboo.nocubes.util;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.BlockState;
import net.minecraft.block.SnowBlock;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.VersionChecker;

import javax.annotation.Nonnull;
import java.util.Random;

import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static net.minecraft.block.Blocks.BEDROCK;
import static net.minecraft.block.Blocks.SNOW;

/**
 * Util that is used on BOTH physical sides
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class ModUtil {

	private static final Random RANDOM = new Random();

	/**
	 * @return negative density if the block is smoothable (inside the isosurface), positive if it isn't
	 */
	public static float getIndividualBlockDensity(final boolean shouldSmooth, final BlockState state) {
		if (shouldSmooth) {
			if (state.getBlock() == SNOW) {
				final int value = state.get(SnowBlock.LAYERS);
				if (value == 1) { // zero-height snow layer
					return 1;
				} else { // snow height between 0-8 to between -0.25F and -1
					return -((value - 1) * 0.125F);
				}
			} else {
				return state.getBlock() == BEDROCK ? -1.0005F : -1;
			}
//		} else if (state.isNormalCube() || state.isBlockNormalCube()) {
//			return 0F;
		} else {
			return 1;
		}
	}

	/**
	 * Give the vec3 some (pseudo) random offset based on its location.
	 * This code is from {link MathHelper#getCoordinateRandom} and Block#getOffset
	 *
	 * @param vec3 the vec3
	 */
	public static Vec3 offsetVertex(Vec3 vec3) {
		long rand = (long) (vec3.x * 3129871.0D) ^ (long) vec3.z * 116129781L ^ (long) vec3.y;
		rand = rand * rand * 42317861L + rand * 11;
		vec3.x += ((double) ((float) (rand >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
		vec3.y += ((double) ((float) (rand >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
		vec3.z += ((double) ((float) (rand >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
		return vec3;
	}

	/**
	 * Ew
	 *
	 * @param modContainer the {@link ModContainer} for {@link NoCubes}
	 */
	public static void launchUpdateDaemon(@Nonnull final ModContainer modContainer) {

		new Thread(() -> {
			while (true) {

				final VersionChecker.CheckResult checkResult = VersionChecker.getResult(modContainer.getModInfo());
				switch (checkResult.status) {
					default:
					case PENDING:
						try {
							Thread.sleep(500L);
						} catch (InterruptedException var4) {
							Thread.currentThread().interrupt();
						}
						break;
					case OUTDATED:
						try {
							BadAutoUpdater.update(modContainer, checkResult.target.toString(), "Cadiboo");
						} catch (Exception var3) {
							throw new RuntimeException(var3);
						}
					case FAILED:
					case UP_TO_DATE:
					case AHEAD:
					case BETA:
					case BETA_OUTDATED:
						return;
				}
			}

		}, modContainer.getModInfo().getDisplayName() + " Update Daemon").start();

	}

	public static boolean isDeveloperWorkspace() {
		final String target = System.getenv().get("target");
		if (target == null) {
			return false;
		}
		return target.contains("userdev");
	}

	/**
	 * We add 1 because idk (it fixes seams in between chunks)
	 * and then surface nets needs another +1 because reasons
	 */
	public static byte getMeshSizeX(final int initialSize, final MeshGenerator meshGenerator) {
		return (byte) (initialSize + meshGenerator.getSizeXExtension());
	}

	/**
	 * We add 1 because idk (it fixes seams in between chunks)
	 * and then surface nets needs another +1 because reasons
	 */
	public static byte getMeshSizeY(final int initialSize, final MeshGenerator meshGenerator) {
		return (byte) (initialSize + meshGenerator.getSizeYExtension());
	}

	/**
	 * We add 1 because idk (it fixes seams in between chunks)
	 * and then surface nets needs another +1 because reasons
	 */
	public static byte getMeshSizeZ(final int initialSize, final MeshGenerator meshGenerator) {
		return (byte) (initialSize + meshGenerator.getSizeZExtension());
	}

	public static IFluidState getFluidState(final World world, final BlockPos pos) {
		final int posX = pos.getX();
		final int posY = pos.getY();
		final int posZ = pos.getZ();

		int currentChunkPosX = posX >> 4;
		int currentChunkPosZ = posZ >> 4;
		Chunk currentChunk = world.getChunk(currentChunkPosX, currentChunkPosZ);

		final int extendRange = Config.extendFluidsRange.getRange();

		if (extendRange == 0) {
			return currentChunk.getFluidState(posX, posY, posZ);
		}

		final BlockState state = currentChunk.getBlockState(pos);

		// Do not extend if not terrain smoothable
		if (!state.nocubes_isTerrainSmoothable()) {
			return state.getFluidState();
		}

		final IFluidState fluidState = state.getFluidState();
		if (!fluidState.isEmpty()) {
			return fluidState;
		}

		// For offset = -1 or -2 to offset = 1 or 2;
		final int maxXOffset = extendRange;
		final int maxZOffset = extendRange;

		// Check up
		{
			final IFluidState state1 = currentChunk.getFluidState(posX, posY + 1, posZ);
			if (state1.isSource()) {
				return state1;
			}
		}

		for (int xOffset = -maxXOffset; xOffset <= maxXOffset; ++xOffset) {
			for (int zOffset = -maxZOffset; zOffset <= maxZOffset; ++zOffset) {

				// No point in checking myself
				if (xOffset == 0 && zOffset == 0) {
					continue;
				}

				final int checkX = posX + xOffset;
				final int checkZ = posZ + zOffset;

				if (currentChunkPosX != checkX >> 4 || currentChunkPosZ != checkZ >> 4) {
					currentChunkPosX = checkX >> 4;
					currentChunkPosZ = checkZ >> 4;
					currentChunk = world.getChunk(currentChunkPosX, currentChunkPosZ);
				}

				final IFluidState state1 = currentChunk.getFluidState(checkX, posY, checkZ);
				if (state1.isSource()) {
					return state1;
				}

			}
		}
		return fluidState;
	}

	public static boolean doesTerrainCauseSuffocation(final IBlockReader reader, final BlockPos pos) {
		final float density;
		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			float density1 = 0;

//			final WorldBorder worldBorder = ((IWorldReader) reader).getWorldBorder();

			final int startX = pos.getX();
			final int startY = pos.getY();
			final int startZ = pos.getZ();

			for (int zOffset = 0; zOffset < 2; ++zOffset) {
				for (int yOffset = 0; yOffset < 2; ++yOffset) {
					for (int xOffset = 0; xOffset < 2; ++xOffset) {

						pooledMutableBlockPos.setPos(
								startX - xOffset,
								startY - yOffset,
								startZ - zOffset
						);

//						// Return a fully solid cube if its not loaded
//						if (!((IWorldReader) reader).isBlockLoaded(pooledMutableBlockPos) || !worldBorder.contains(pooledMutableBlockPos)) {
//							density1 += 1;
//							continue;
//						}

						final BlockState testState = reader.getBlockState(pooledMutableBlockPos);
						density1 += getIndividualBlockDensity(TERRAIN_SMOOTHABLE.apply(testState), testState);
					}
				}
			}
			density = density1;
		}

		// > 0 means outside isosurface
		// > -4 means mostly outside isosurface
		return density > -4;
	}

}
