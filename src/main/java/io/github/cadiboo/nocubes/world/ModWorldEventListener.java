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

import javax.annotation.Nullable;

/**
 * @author Cadiboo
 */
public class ModWorldEventListener implements IWorldEventListener {

	private static final int BLOCK_UPDATE_EXTEND = 2;

	@Override
	public void notifyBlockUpdate(final IBlockReader worldIn, final BlockPos pos, final IBlockState oldState, final IBlockState newState, final int flags) {
		int posX = pos.getX();
		int posY = pos.getY();
		int posZ = pos.getZ();
		NoCubes.PROXY.markBlocksForUpdate(
				posX - BLOCK_UPDATE_EXTEND, posY - BLOCK_UPDATE_EXTEND, posZ - BLOCK_UPDATE_EXTEND,
				posX + BLOCK_UPDATE_EXTEND, posY + BLOCK_UPDATE_EXTEND, posZ + BLOCK_UPDATE_EXTEND,
				(flags & 8) != 0
		);
	}

	@Override
	public void notifyLightSet(final BlockPos pos) {

	}

	@Override
	public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {

	}

	@Override
	public void playSoundToAllNearExcept(@Nullable final EntityPlayer player, final SoundEvent soundIn, final SoundCategory category, final double x, final double y, final double z, final float volume, final float pitch) {

	}

	@Override
	public void playRecord(final SoundEvent soundIn, final BlockPos pos) {

	}

	@Override
	public void addParticle(final IParticleData particleData, final boolean alwaysRender, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed) {

	}

	@Override
	public void addParticle(final IParticleData particleData, final boolean ignoreRange, final boolean minimizeLevel, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed) {

	}

	@Override
	public void onEntityAdded(final Entity entityIn) {

	}

	@Override
	public void onEntityRemoved(final Entity entityIn) {

	}

	@Override
	public void broadcastSound(final int soundID, final BlockPos pos, final int data) {

	}

	@Override
	public void playEvent(final EntityPlayer player, final int type, final BlockPos blockPosIn, final int data) {

	}

	@Override
	public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress) {

	}

}
