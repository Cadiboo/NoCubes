package io.github.cadiboo.nocubes.world;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.collision.CollisionHandler.CollisionsCache;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldEventListener;

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
	public void notifyBlockUpdate(final IBlockReader iBlockReader, final BlockPos pos, final IBlockState iBlockState, final IBlockState iBlockState1, final int flags) {

		if (!NoCubes.isEnabled()) {
			return;
		}
		final int posX = pos.getX();
		final int posY = pos.getY();
		final int posZ = pos.getZ();

		final int blockUpdateExtend = BLOCK_UPDATE_EXTEND;

		NoCubes.PROXY.markBlocksForUpdate(posX - blockUpdateExtend, posY - blockUpdateExtend, posZ - blockUpdateExtend, posX + blockUpdateExtend, posY + blockUpdateExtend, posZ + blockUpdateExtend, (flags & 8) != 0);

		if (!ModConfig.enableCollisions) {
			return;
		}

		try (
				final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
				final ModProfiler ignored = NoCubes.getProfiler().start("ServerTickEvent")
		) {
			synchronized (CACHE) {

				for (int z = -blockUpdateExtend; z <= blockUpdateExtend; ++z) {
					for (int y = -blockUpdateExtend; y <= blockUpdateExtend; ++y) {
						for (int x = -blockUpdateExtend; x <= blockUpdateExtend; ++x) {

							pooledMutableBlockPos.setPos(posX + x, posY + y, posZ + z);

							final CollisionsCache collisionsCache = CACHE.remove(pooledMutableBlockPos);

							if (collisionsCache == null) {
								continue;
							}

							synchronized (collisionsCache.faces) {
								final FaceList faces = collisionsCache.faces;
								for (final Face face : faces) {
									{
										face.getVertex0().close();
										face.getVertex1().close();
										face.getVertex2().close();
										face.getVertex3().close();
									}
									face.close();
								}
								faces.close();
							}

						}

					}
				}
			}
		}

	}

	@Override
	public void notifyLightSet(@Nonnull final BlockPos pos) {

	}

	@Override
	public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2,
	                                          final int y2, final int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(@Nullable final EntityPlayer player,
	                                     @Nonnull final SoundEvent soundIn, @Nonnull final SoundCategory category, final double x, final double y,
	                                     final double z, final float volume, final float pitch) {

	}

	@Override
	public void playRecord(@Nonnull final SoundEvent soundIn, @Nonnull final BlockPos pos) {

	}

	@Override
	public void addParticle(final IParticleData iParticleData, final boolean b, final double v, final double v1, final double v2, final double v3, final double v4, final double v5) {

	}

	@Override
	public void addParticle(final IParticleData iParticleData, final boolean b, final boolean b1, final double v, final double v1, final double v2, final double v3, final double v4, final double v5) {

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
	public void playEvent(@Nonnull final EntityPlayer player, final int type,
	                      @Nonnull final BlockPos blockPosIn, final int data) {

	}

	@Override
	public void sendBlockBreakProgress(final int breakerId, @Nonnull final BlockPos pos, final int progress) {

	}

}
