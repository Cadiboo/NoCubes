package io.github.cadiboo.nocubes.world;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.collision.CollisionHandler.CollisionsCache;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.github.cadiboo.nocubes.collision.CollisionHandler.CACHE;

/**
 * @author Cadiboo
 */
//TODO this for the server
public class ModWorldEventListener implements IWorldEventListener {

	private static final int BLOCK_UPDATE_EXTEND = 2;

	@Override
	public void notifyBlockUpdate(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState, int flags) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		final int posX = pos.getX();
		final int posY = pos.getY();
		final int posZ = pos.getZ();

		if (ModConfig.enableCollisions) {
			final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
			try {
				synchronized (CACHE) {
					for (int z = -1; z < 2; ++z) {
						for (int y = -1; y < 2; ++y) {
							for (int x = -1; x < 2; ++x) {
								pooledMutableBlockPos.setPos(posX + x, posY + y, posZ + z);
								final CollisionsCache collisionsCache = CACHE.get(pooledMutableBlockPos);
								if (collisionsCache != null) {
									final FaceList faces = collisionsCache.faces;
									for (final Face face : faces) {
										face.getVertex0().close();
										face.getVertex1().close();
										face.getVertex2().close();
										face.getVertex3().close();
										face.close();
									}
									faces.close();
									CACHE.remove(pooledMutableBlockPos);
								}
							}
						}
					}
				}
			} finally {
				pooledMutableBlockPos.release();
			}
		}

		NoCubes.PROXY.markBlocksForUpdate(posX - BLOCK_UPDATE_EXTEND, posY - BLOCK_UPDATE_EXTEND, posZ - BLOCK_UPDATE_EXTEND, posX + BLOCK_UPDATE_EXTEND, posY + BLOCK_UPDATE_EXTEND, posZ + BLOCK_UPDATE_EXTEND, (flags & 8) != 0);
	}

	@Override
	public void notifyLightSet(@Nonnull final BlockPos pos) {

	}

	@Override
	public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(@Nullable final EntityPlayer player, @Nonnull final SoundEvent soundIn, @Nonnull final SoundCategory category, final double x, final double y, final double z, final float volume, final float pitch) {

	}

	@Override
	public void playRecord(@Nonnull final SoundEvent soundIn, @Nonnull final BlockPos pos) {

	}

	@Override
	public void spawnParticle(final int particleID, final boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, final double xSpeed, final double ySpeed, final double zSpeed, @Nonnull final int... parameters) {

	}

	@Override
	public void spawnParticle(final int id, final boolean ignoreRange, final boolean minimiseParticleLevel, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed, @Nonnull final int... parameters) {

	}

	@Override
	public void onEntityAdded(@Nonnull final Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(@Nonnull final Entity entityIn) {

	}

	@Override
	public void broadcastSound(final int soundID, @Nonnull final BlockPos pos, final int data) {

	}

	@Override
	public void playEvent(@Nonnull final EntityPlayer player, final int type, @Nonnull final BlockPos blockPosIn, final int data) {

	}

	@Override
	public void sendBlockBreakProgress(final int breakerId, @Nonnull final BlockPos pos, final int progress) {

	}

}
