package io.github.cadiboo.nocubes_mmd_winterjam.client.render;

import io.github.cadiboo.nocubes_mmd_winterjam.client.ClientUtil;
import io.github.cadiboo.nocubes_mmd_winterjam.util.LightmapInfo;
import io.github.cadiboo.nocubes_mmd_winterjam.util.ModUtil;
import io.github.cadiboo.nocubes_mmd_winterjam.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Reimplementation of the NoCubes algorithm for NoCubes 0.3 (by Click_Me)
 *
 * @author Click_Me
 * @author Cadiboo
 */
public final class OldNoCubes {

	/* Begin Click_Me's code */
	// The shadow values.
	private static final float SHADOW_BOTTOM = 0.6F;
	private static final float SHADOW_TOP = 1.0F;
	private static final float SHADOW_LEFT = 0.9F;
	private static final float SHADOW_RIGHT = 0.8F;
	/* End Click_Me's code */

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		final IBlockState state = event.getBlockState();
		if (!ModUtil.shouldSmooth(state)) {
			return;
		}
		final ChunkCache cache = event.getChunkCache();
		final BlockPos pos = event.getBlockPos();
		if (cache.getBlockState(pos.up()).getBlock() instanceof BlockLiquid) {
			return;
		}

//		final BakedQuad quad = ClientUtil.getQuad(state, pos, blockRendererDispatcher);
//		if (quad == null) {
//			return;
//		}
		final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/snow");

		final float colorRed = 0xFF / 255.0F;
		final float colorGreen = 0xFF / 255.0F;
		final float colorBlue = 0xFF / 255.0F;

		final double minU = ClientUtil.getMinU(sprite);
		final double minV = ClientUtil.getMinV(sprite);
		final double maxU = ClientUtil.getMaxU(sprite);
		final double maxV = ClientUtil.getMaxV(sprite);

		final LightmapInfo lightmapInfo = ClientUtil.getLightmapInfo(pos, cache);
		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

		final BufferBuilder bufferBuilder = event.getBufferBuilder();

		final Vec3[] points = getPoints(pos, cache);

		if (points == null) return;

//		boolean cancelEvent = true;

//		if (ModConfig.betterFoliageGrassCompatibility) {
//			//render BF grass if not near air
//			if (state.getBlock() instanceof BlockGrass) {
//				cancelEvent = false;
//				for (BlockPos mutablePos : BlockPos.getAllInBoxMutable(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
//					if (!cache.getBlockState(mutablePos).getMaterial().isSolid()) {
//						cancelEvent = true;
//						break;
//					}
//				}
//			}
//		}

		boolean wasAnythingRendered = false;

		final EnumFacing[] EnumFacing_VALUES = EnumFacing.VALUES;
		for (EnumFacing facing : EnumFacing_VALUES) {

			// Check if the side should be rendered:
			// This prevents a lot of lag!
			if (!state.shouldSideBeRendered(cache, pos, facing)) {
				continue;
			}

			wasAnythingRendered = true;

			/* Begin Click_Me's code */
			// When you lower this value the block will become darker.
			final float colorFactor;

			// This are the vertices used for the side.
			final Vec3 vertex0;
			final Vec3 vertex1;
			final Vec3 vertex2;
			final Vec3 vertex3;
			/* End Click_Me's code */

			//to find the vertices for a side get the points in this order
			// 1➡️2
			//   ⬇️
			// 4⬅️3

			// to find the vertices for the opposite side
			// just get the points with the opposite axis for the side (for example east is +x and west is -x)
			// and then flip the order

			//example
			// west:
			// (0,0,0) (0,0,1)
			// (0,1,1) (0,1,0)

			// to get east you add 1 to the x coord for every point (east is +x and west is -x)
			// (1,0,0) (1,0,1)
			// (1,1,1) (1,1,0)

			// then flip the order (1234 -> 4321)
			// (1,1,0) (1,1,1)
			// (1,0,1) (1,0,0)

			/* Begin Click_Me's code */
			switch (facing) {
				default:
				case DOWN:
					colorFactor = SHADOW_BOTTOM;
					vertex0 = points[0];
					vertex1 = points[1];
					vertex2 = points[2];
					vertex3 = points[3];
					break;
				case UP:
					colorFactor = SHADOW_TOP;
					vertex0 = points[7];
					vertex1 = points[6];
					vertex2 = points[5];
					vertex3 = points[4];
					break;
				case NORTH:
					colorFactor = SHADOW_LEFT;
					vertex0 = points[1];
					vertex1 = points[0];
					vertex2 = points[4];
					vertex3 = points[5];
					break;
				/* End Click_Me's code */
				case SOUTH:
					colorFactor = SHADOW_RIGHT;
					vertex0 = points[6];
					vertex1 = points[7];
					vertex2 = points[3];
					vertex3 = points[2];
					break;
				/* Begin Click_Me's code */
				case WEST:
					colorFactor = SHADOW_LEFT;
					vertex0 = points[0];
					vertex1 = points[3];
					vertex2 = points[7];
					vertex3 = points[4];
					break;
				/* End Click_Me's code */
				case EAST:
					colorFactor = SHADOW_RIGHT;
					vertex0 = points[5];
					vertex1 = points[6];
					vertex2 = points[2];
					vertex3 = points[1];
					break;
			}

			final float
					/* Begin Click_Me's code */
					red = SHADOW_TOP * colorFactor * colorRed,
					green = SHADOW_TOP * colorFactor * colorGreen,
					blue = SHADOW_TOP * colorFactor * colorBlue,
					/* End Click_Me's code */
					alpha = 1.0F;

			// And finally the side is going to be rendered!
			bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
			bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

			//TODO: smooth lighting

		}

		event.getUsedBlockRenderLayers()[event.getBlockRenderLayer().ordinal()] |= wasAnythingRendered;

//		event.setCanceled(cancelEvent);

	}

	@Nullable
	private static Vec3[] getPoints(@Nonnull final BlockPos pos, @Nonnull final IBlockAccess cache) {

		if (!ModUtil.shouldSmooth(cache.getBlockState(pos))) {
			return null;
		}

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		// The 8 points that make the block.
		// 1 point for each corner
		final Vec3[] points = {
				new Vec3(0, 0, 0),
				new Vec3(1, 0, 0),
				new Vec3(1, 0, 1),
				new Vec3(0, 0, 1),
				new Vec3(0, 1, 0),
				new Vec3(1, 1, 0),
				new Vec3(1, 1, 1),
				new Vec3(0, 1, 1),
		};

		// Loop through all the points:
		// Here everything will be 'smoothed'.
		for (int pointIndex = 0; pointIndex < 8; pointIndex++) {

			final Vec3 point = points[pointIndex];

			// Give the point the block's coordinates.
			point.xCoord += (double) x;
			point.yCoord += (double) y;
			point.zCoord += (double) z;

			// Check if the point is intersecting with a smoothable block.
			if (doesPointIntersectWithSmoothable(cache, point)) {
				if (pointIndex < 4 && doesPointBottomIntersectWithAir(cache, point)) {
					point.yCoord = (double) y + 1.0D;
				} else if (pointIndex >= 4 && doesPointTopIntersectWithAir(cache, point)) {
					point.yCoord = (double) y;
				}

				ModUtil.givePointRoughness(point);

			}
		}

		return points;

	}

	/**
	 * Check if the state is AIR or PLANT or VINE
	 *
	 * @param state the state
	 * @return if the state is AIR or PLANT or VINE
	 */
	private static boolean isBlockAirOrPlant(IBlockState state) {
		Material material = state.getMaterial();
		return material == Material.AIR || material == Material.PLANTS || material == Material.VINE;
	}

	/**
	 * Check if the block's top side intersects with air.
	 *
	 * @param world the world
	 * @param point the point
	 * @return if the block's top side intersects with air.
	 */
	private static boolean doesPointTopIntersectWithAir(IBlockAccess world, Vec3 point) {
		boolean intersects = false;

		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (point.xCoord - (double) (i & 1));
			int z1 = (int) (point.zCoord - (double) (i >> 1 & 1));
			if (!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) point.yCoord, z1)))) {
				return false;
			}

			if (isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) point.yCoord - 1, z1)))) {
				intersects = true;
			}
		}

		return intersects;
	}

	/**
	 * Check if the block's bottom side intersects with air.
	 *
	 * @param world the world
	 * @param point the point
	 * @return if the block's bottom side intersects with air.
	 */
	private static boolean doesPointBottomIntersectWithAir(IBlockAccess world, Vec3 point) {
		boolean intersects = false;
		boolean notOnly = false;

		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (point.xCoord - (double) (i & 1));
			int z1 = (int) (point.zCoord - (double) (i >> 1 & 1));
			if (!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) point.yCoord - 1, z1)))) {
				return false;
			}

			if (!isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) point.yCoord + 1, z1)))) {
				notOnly = true;
			}

			if (isBlockAirOrPlant(world.getBlockState(new BlockPos(x1, (int) point.yCoord, z1)))) {
				intersects = true;
			}
		}

		return intersects && notOnly;
	}

	/**
	 * Check if the point is intersecting with a smoothable block.
	 *
	 * @param world the world
	 * @param point the point
	 * @return if the point is intersecting with a smoothable block.
	 */
	private static boolean doesPointIntersectWithSmoothable(IBlockAccess world, Vec3 point) {
		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (point.xCoord - (double) (i & 1));
			int z1 = (int) (point.yCoord - (double) (i >> 1 & 1));
			IBlockState block = world.getBlockState(new BlockPos(x1, (int) point.yCoord, z1));
			if (!isBlockAirOrPlant(block) && !ModUtil.shouldSmooth(block)) {
				return false;
			}

			IBlockState block1 = world.getBlockState(new BlockPos(x1, (int) point.yCoord - 1, z1));
			if (!isBlockAirOrPlant(block1) && !ModUtil.shouldSmooth(block1)) {
				return false;
			}
		}
		return true;
	}

}
