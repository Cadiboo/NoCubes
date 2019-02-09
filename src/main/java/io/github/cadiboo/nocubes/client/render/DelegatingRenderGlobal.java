package io.github.cadiboo.nocubes.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Delegates all PUBLIC methods
 *
 * @author Cadiboo
 */
public class DelegatingRenderGlobal extends RenderGlobal {

	private static final String RenderGlobal_mc = "field_72777_q";
	private final RenderGlobal delegate;

	public DelegatingRenderGlobal(final RenderGlobal delegateIn) {
		super(ObfuscationReflectionHelper.getPrivateValue(RenderGlobal.class, delegateIn, RenderGlobal_mc));
		this.delegate = delegateIn;
	}

	public RenderGlobal getDelegate() {
		return delegate;
	}

	@Override
	public void onResourceManagerReload(final IResourceManager resourceManager) {
		delegate.onResourceManagerReload(resourceManager);
	}

	@Override
	public void makeEntityOutlineShader() {
		delegate.makeEntityOutlineShader();
	}

	@Override
	public void renderEntityOutlineFramebuffer() {
		delegate.renderEntityOutlineFramebuffer();
	}

	@Override
	public void setWorldAndLoadRenderers(@Nullable final WorldClient worldClientIn) {
		delegate.setWorldAndLoadRenderers(worldClientIn);
	}

	@Override
	public void loadRenderers() {
		delegate.loadRenderers();
	}

	@Override
	public void createBindEntityOutlineFbs(final int width, final int height) {
		delegate.createBindEntityOutlineFbs(width, height);
	}

	@Override
	public void renderEntities(@Nonnull final Entity renderViewEntity, @Nonnull final ICamera camera, final float partialTicks) {
		delegate.renderEntities(renderViewEntity, camera, partialTicks);
	}

	@Override
	@Nonnull
	public String getDebugInfoRenders() {
		return delegate.getDebugInfoRenders();
	}

	@Override
	@Nonnull
	public String getDebugInfoEntities() {
		return delegate.getDebugInfoEntities();
	}

	@Override
	public void setupTerrain(final Entity viewEntity, final double partialTicks, @Nonnull final ICamera camera, final int frameCount, final boolean playerSpectator) {
		delegate.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
	}

	@Override
	public int renderBlockLayer(@Nonnull final BlockRenderLayer blockLayerIn, final double partialTicks, final int pass, @Nonnull final Entity entityIn) {
		return delegate.renderBlockLayer(blockLayerIn, partialTicks, pass, entityIn);
	}

	@Override
	public void updateClouds() {
		delegate.updateClouds();
	}

	@Override
	public void renderSky(final float partialTicks, final int pass) {
		delegate.renderSky(partialTicks, pass);
	}

	@Override
	public void renderClouds(final float partialTicks, final int pass, final double x, final double y, final double z) {
		delegate.renderClouds(partialTicks, pass, x, y, z);
	}

	@Override
	public boolean hasCloudFog(final double x, final double y, final double z, final float partialTicks) {
		return delegate.hasCloudFog(x, y, z, partialTicks);
	}

	@Override
	public void updateChunks(final long finishTimeNano) {
		delegate.updateChunks(finishTimeNano);
	}

	@Override
	public void renderWorldBorder(final Entity entityIn, final float partialTicks) {
		delegate.renderWorldBorder(entityIn, partialTicks);
	}

	@Override
	public void drawBlockDamageTexture(@Nonnull final Tessellator tessellatorIn, @Nonnull final BufferBuilder bufferBuilderIn, final Entity entityIn, final float partialTicks) {
		delegate.drawBlockDamageTexture(tessellatorIn, bufferBuilderIn, entityIn, partialTicks);
	}

	@Override
	public void drawSelectionBox(@Nonnull final EntityPlayer player, @Nonnull final RayTraceResult movingObjectPositionIn, final int execute, final float partialTicks) {
		delegate.drawSelectionBox(player, movingObjectPositionIn, execute, partialTicks);
	}

	@Override
	public void notifyBlockUpdate(final World worldIn, final BlockPos pos, final IBlockState oldState, final IBlockState newState, final int flags) {
		delegate.notifyBlockUpdate(worldIn, pos, oldState, newState, flags);
	}

	@Override
	public void notifyLightSet(final BlockPos pos) {
		delegate.notifyLightSet(pos);
	}

	@Override
	public void markBlockRangeForRenderUpdate(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2) {
		delegate.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
	}

	@Override
	public void playRecord(@Nullable final SoundEvent soundIn, final BlockPos pos) {
		delegate.playRecord(soundIn, pos);
	}

	@Override
	public void playSoundToAllNearExcept(@Nullable final EntityPlayer player, final SoundEvent soundIn, final SoundCategory category, final double x, final double y, final double z, final float volume, final float pitch) {
		delegate.playSoundToAllNearExcept(player, soundIn, category, x, y, z, volume, pitch);
	}

	@Override
	public void spawnParticle(final int particleID, final boolean ignoreRange, final double xCoord, final double yCoord, final double zCoord, final double xSpeed, final double ySpeed, final double zSpeed, @Nonnull final int... parameters) {
		delegate.spawnParticle(particleID, ignoreRange, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);
	}

	@Override
	public void spawnParticle(final int id, final boolean ignoreRange, final boolean minimiseParticleLevel, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed, @Nonnull final int... parameters) {
		delegate.spawnParticle(id, ignoreRange, minimiseParticleLevel, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
	}

	@Override
	public void onEntityAdded(final Entity entityIn) {
		delegate.onEntityAdded(entityIn);
	}

	@Override
	public void onEntityRemoved(final Entity entityIn) {
		delegate.onEntityRemoved(entityIn);
	}

	@Override
	public void deleteAllDisplayLists() {
		delegate.deleteAllDisplayLists();
	}

	@Override
	public void broadcastSound(final int soundID, @Nonnull final BlockPos pos, final int data) {
		delegate.broadcastSound(soundID, pos, data);
	}

	@Override
	public void playEvent(final EntityPlayer player, final int type, @Nonnull final BlockPos blockPosIn, final int data) {
		delegate.playEvent(player, type, blockPosIn, data);
	}

	@Override
	public void sendBlockBreakProgress(final int breakerId, @Nonnull final BlockPos pos, final int progress) {
		delegate.sendBlockBreakProgress(breakerId, pos, progress);
	}

	@Override
	public boolean hasNoChunkUpdates() {
		return delegate.hasNoChunkUpdates();
	}

	@Override
	public void setDisplayListEntitiesDirty() {
		delegate.setDisplayListEntitiesDirty();
	}

	@Override
	public void updateTileEntities(@Nonnull final Collection<TileEntity> tileEntitiesToRemove, @Nonnull final Collection<TileEntity> tileEntitiesToAdd) {
		delegate.updateTileEntities(tileEntitiesToRemove, tileEntitiesToAdd);
	}

}
