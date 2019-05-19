package net.minecraft.block.state;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IBlockProperties {
	Material getMaterial();

	boolean isFullBlock();

	boolean canEntitySpawn(Entity var1);

	/** @deprecated */
	@Deprecated
	int getLightOpacity();

	int getLightOpacity(IBlockAccess var1, BlockPos var2);

	/** @deprecated */
	@Deprecated
	int getLightValue();

	int getLightValue(IBlockAccess var1, BlockPos var2);

	@SideOnly(Side.CLIENT)
	boolean isTranslucent();

	boolean useNeighborBrightness();

	MapColor getMapColor(IBlockAccess var1, BlockPos var2);

	IBlockState withRotation(Rotation var1);

	IBlockState withMirror(Mirror var1);

	boolean isFullCube();

	@SideOnly(Side.CLIENT)
	boolean hasCustomBreakingProgress();

	EnumBlockRenderType getRenderType();

	@SideOnly(Side.CLIENT)
	int getPackedLightmapCoords(IBlockAccess var1, BlockPos var2);

	@SideOnly(Side.CLIENT)
	float getAmbientOcclusionLightValue();

	boolean isBlockNormalCube();

	boolean isNormalCube();

	boolean canProvidePower();

	int getWeakPower(IBlockAccess var1, BlockPos var2, EnumFacing var3);

	boolean hasComparatorInputOverride();

	int getComparatorInputOverride(World var1, BlockPos var2);

	float getBlockHardness(World var1, BlockPos var2);

	float getPlayerRelativeBlockHardness(EntityPlayer var1, World var2, BlockPos var3);

	int getStrongPower(IBlockAccess var1, BlockPos var2, EnumFacing var3);

	EnumPushReaction getPushReaction();

	IBlockState getActualState(IBlockAccess var1, BlockPos var2);

	@SideOnly(Side.CLIENT)
	AxisAlignedBB getSelectedBoundingBox(World var1, BlockPos var2);

	@SideOnly(Side.CLIENT)
	boolean shouldSideBeRendered(IBlockAccess var1, BlockPos var2, EnumFacing var3);

	boolean isOpaqueCube();

	@Nullable
	AxisAlignedBB getCollisionBoundingBox(IBlockAccess var1, BlockPos var2);

	void addCollisionBoxToList(World var1, BlockPos var2, AxisAlignedBB var3, List<AxisAlignedBB> var4, @Nullable Entity var5, boolean var6);

	AxisAlignedBB getBoundingBox(IBlockAccess var1, BlockPos var2);

	RayTraceResult collisionRayTrace(World var1, BlockPos var2, Vec3d var3, Vec3d var4);

	/** @deprecated */
	@Deprecated
	boolean isTopSolid();

	boolean doesSideBlockRendering(IBlockAccess var1, BlockPos var2, EnumFacing var3);

	boolean isSideSolid(IBlockAccess var1, BlockPos var2, EnumFacing var3);

	boolean doesSideBlockChestOpening(IBlockAccess var1, BlockPos var2, EnumFacing var3);

	Vec3d getOffset(IBlockAccess var1, BlockPos var2);

	boolean causesSuffocation();

	BlockFaceShape getBlockFaceShape(IBlockAccess var1, BlockPos var2, EnumFacing var3);

	// ******** NoCubes Start ******** //

	/**
	 * does NOT take into account whether NoCubes is enabled or not
	 */
	default boolean nocubes_isTerrainSmoothable() {
		return false;
	}

	default void nocubes_setTerrainSmoothable(final boolean isTerrainSmoothable) {
		return;
	}

	/**
	 * does NOT take into account whether NoCubes is enabled or not
	 */
	default boolean nocubes_isLeavesSmoothable() {
		return false;
	}

	default void nocubes_setLeavesSmoothable(final boolean isLeavesSmoothable) {
		return;
	}

}
