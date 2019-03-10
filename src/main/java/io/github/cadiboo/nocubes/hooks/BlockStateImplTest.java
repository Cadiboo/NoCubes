package io.github.cadiboo.nocubes.hooks;

import com.google.common.collect.ImmutableMap;
import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * @author Cadiboo
 */
public class BlockStateImplTest implements IBlockState {

	private Block block;

	public BlockStateImplTest(Block block) {
		this.block = block;
	}

	@Override
	public Collection<IProperty<?>> getPropertyKeys() {
		return null;
	}

	@Override
	public <T extends Comparable<T>> T getValue(final IProperty<T> property) {
		return null;
	}

	@Override
	public <T extends Comparable<T>, V extends T> IBlockState withProperty(final IProperty<T> property, final V value) {
		return null;
	}

	@Override
	public <T extends Comparable<T>> IBlockState cycleProperty(final IProperty<T> property) {
		return null;
	}

	@Override
	public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
		return null;
	}

	@Override
	public Block getBlock() {
		return null;
	}

	@Override
	public boolean onBlockEventReceived(final World worldIn, final BlockPos pos, final int id, final int param) {
		return false;
	}

	@Override
	public void neighborChanged(final World worldIn, final BlockPos pos, final Block blockIn, final BlockPos fromPos) {

	}

	@Override
	public Material getMaterial() {
		return null;
	}

	@Override
	public boolean isFullBlock() {
		return false;
	}

	@Override
	public boolean canEntitySpawn(final Entity entityIn) {
		return false;
	}

	@Override
	public int getLightOpacity() {
		return 0;
	}

	@Override
	public int getLightOpacity(final IBlockAccess world, final BlockPos pos) {
		return 0;
	}

	@Override
	public int getLightValue() {
		return 0;
	}

	@Override
	public int getLightValue(final IBlockAccess world, final BlockPos pos) {
		return 0;
	}

	@Override
	public boolean isTranslucent() {
		return false;
	}

	@Override
	public boolean useNeighborBrightness() {
		return false;
	}

	@Override
	public MapColor getMapColor(final IBlockAccess p_185909_1_, final BlockPos p_185909_2_) {
		return null;
	}

	@Override
	public IBlockState withRotation(final Rotation rot) {
		return null;
	}

	@Override
	public IBlockState withMirror(final Mirror mirrorIn) {
		return null;
	}

	@Override
	public boolean isFullCube() {
		return false;
	}

	@Override
	public boolean hasCustomBreakingProgress() {
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType() {
		return null;
	}

	@Override
	public int getPackedLightmapCoords(final IBlockAccess source, final BlockPos pos) {
		return 0;
	}

	@Override
	public float getAmbientOcclusionLightValue() {
		return 0;
	}

	@Override
	public boolean isBlockNormalCube() {
		return false;
	}

	@Override
	public boolean isNormalCube() {
		return false;
	}

	@Override
	public boolean canProvidePower() {
		return false;
	}

	@Override
	public int getWeakPower(final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing side) {
		return 0;
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return false;
	}

	@Override
	public int getComparatorInputOverride(final World worldIn, final BlockPos pos) {
		return 0;
	}

	@Override
	public float getBlockHardness(final World worldIn, final BlockPos pos) {
		return 0;
	}

	@Override
	public float getPlayerRelativeBlockHardness(final EntityPlayer player, final World worldIn, final BlockPos pos) {
		return 0;
	}

	@Override
	public int getStrongPower(final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing side) {
		return 0;
	}

	@Override
	public EnumPushReaction getPushReaction() {
		return null;
	}

	@Override
	public IBlockState getActualState(final IBlockAccess blockAccess, final BlockPos pos) {
		return null;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(final World worldIn, final BlockPos pos) {
		return null;
	}

	@Override
	public boolean shouldSideBeRendered(final IBlockAccess blockAccess, final BlockPos pos, final EnumFacing facing) {
		return false;
	}

	boolean runIsOpaqueCubeDefaultOnce = false;
	boolean runGetCollisionBoundingBoxDefaultOnce = false;
	boolean runAddCollisionBoxToListDefaultOnce = false;

	@Override
	public boolean isOpaqueCube() {
		if (!runAddCollisionBoxToListDefaultOnce && NoCubes.areHooksEnabled()) {
			return IsOpaqueCubeHook.isOpaqueCube(block, this);
		}
		runIsOpaqueCubeDefaultOnce = false;
		// Or whatever code that other coremods have injected
		return block.isOpaqueCube(this);
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(final IBlockAccess worldIn, final BlockPos pos) {
		if (!runAddCollisionBoxToListDefaultOnce && NoCubes.areHooksEnabled()) {
			return GetCollisionBoundingBoxHook.getCollisionBoundingBox(block, this, worldIn, pos);
		}
		runGetCollisionBoundingBoxDefaultOnce = false;
		// Or whatever code that other coremods have injected
		return block.getCollisionBoundingBox(this, worldIn, pos);
	}

	@Override
	public void addCollisionBoxToList(final World worldIn, final BlockPos pos, final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes, @Nullable final Entity entityIn, final boolean p_185908_6_) {
		if (!runAddCollisionBoxToListDefaultOnce && NoCubes.areHooksEnabled()) {
			AddCollisionBoxToListHook.addCollisionBoxToList(block, this, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185908_6_);
			return;
		}
		runAddCollisionBoxToListDefaultOnce = false;
		// Or whatever code that other coremods have injected
		block.addCollisionBoxToList(this, worldIn, pos, entityBox, collidingBoxes, entityIn, p_185908_6_);
	}

	@Override
	public AxisAlignedBB getBoundingBox(final IBlockAccess blockAccess, final BlockPos pos) {
		return null;
	}

	@Override
	public RayTraceResult collisionRayTrace(final World worldIn, final BlockPos pos, final Vec3d start, final Vec3d end) {
		return null;
	}

	@Override
	public boolean isTopSolid() {
		return false;
	}

	@Override
	public boolean doesSideBlockRendering(final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
		return false;
	}

	@Override
	public boolean isSideSolid(final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
		return false;
	}

	@Override
	public boolean doesSideBlockChestOpening(final IBlockAccess world, final BlockPos pos, final EnumFacing side) {
		return false;
	}

	@Override
	public Vec3d getOffset(final IBlockAccess access, final BlockPos pos) {
		return null;
	}

	@Override
	public boolean causesSuffocation() {
		return false;
	}

	@Override
	public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final BlockPos pos, final EnumFacing facing) {
		return null;
	}

}
