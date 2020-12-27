package io.github.cadiboo.nocubes.client.world;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.future.DistExecutor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nullable;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = NoCubes.MOD_ID, value = Side.CLIENT)
public final class ClientBlockRenderUpdateRangeExtender implements IWorldEventListener {

	private static final int BLOCK_UPDATE_EXTEND = 2;

	@SubscribeEvent
	public static void onWorldLoadEvent(final WorldEvent.Load event) {
		event.getWorld().addEventListener(new ClientBlockRenderUpdateRangeExtender());
	}

	@Override
	public void notifyBlockUpdate(final World worldIn, final BlockPos pos, final IBlockState oldState, final IBlockState newState, final int flags) {
		DistExecutor.runWhenOn(Side.CLIENT, () -> () -> {
			final int posX = pos.getX();
			final int posY = pos.getY();
			final int posZ = pos.getZ();
			ClientUtil.markBlocksForUpdate(
					posX - BLOCK_UPDATE_EXTEND, posY - BLOCK_UPDATE_EXTEND, posZ - BLOCK_UPDATE_EXTEND,
					posX + BLOCK_UPDATE_EXTEND, posY + BLOCK_UPDATE_EXTEND, posZ + BLOCK_UPDATE_EXTEND,
					(flags & 8) != 0
			);
		});
	}

	@Override
	public void notifyLightSet(final BlockPos pos) { }

	@Override
	public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) { }

	@Override
	public void playSoundToAllNearExcept(@Nullable final EntityPlayer player, final SoundEvent soundIn, final SoundCategory category, final double x, final double y, final double z, final float volume, final float pitch) { }

	@Override
	public void playRecord(final SoundEvent soundIn, final BlockPos pos) { }

	@Override
	public void spawnParticle(final int particleID, final boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, final double xSpeed, final double ySpeed, final double zSpeed, final int... parameters) { }

	@Override
	public void spawnParticle(final int id, final boolean ignoreRange, final boolean minimiseParticleLevel, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed, final int... parameters) { }

	@Override
	public void onEntityAdded(final Entity entityIn) { }

	@Override
	public void onEntityRemoved(final Entity entityIn) { }

	@Override
	public void broadcastSound(final int soundID, final BlockPos pos, final int data) { }

	@Override
	public void playEvent(final EntityPlayer player, final int type, final BlockPos blockPosIn, final int data) { }

	@Override
	public void sendBlockBreakProgress(final int breakerId, final BlockPos pos, final int progress) { }

}
