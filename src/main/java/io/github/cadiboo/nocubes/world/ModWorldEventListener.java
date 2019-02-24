package io.github.cadiboo.nocubes.world;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldEventListener;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Cadiboo
 */
//TODO this for the server
public class ModWorldEventListener implements IWorldEventListener {

	private static final int BLOCK_UPDATE_EXTEND = 2;

	@Override
	public void notifyBlockUpdate(@Nonnull IBlockReader worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState, int flags) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		int k1 = pos.getX();
		int l1 = pos.getY();
		int i2 = pos.getZ();
		NoCubes.proxy.markBlocksForUpdate(k1 - BLOCK_UPDATE_EXTEND, l1 - BLOCK_UPDATE_EXTEND, i2 - BLOCK_UPDATE_EXTEND, k1 + BLOCK_UPDATE_EXTEND, l1 + BLOCK_UPDATE_EXTEND, i2 + BLOCK_UPDATE_EXTEND, (flags & 8) != 0);
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
	public void addParticle(@Nonnull final IParticleData particleData, final boolean alwaysRender, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed) {

	}

	@Override
	public void addParticle(@Nonnull final IParticleData particleData, final boolean ignoreRange, final boolean minimizeLevel, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed) {

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
