package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

public class OldNoCubes {

	public static void renderPre(final RebuildChunkPreEvent event) {
	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

		IBlockState state = event.getBlockState();
		final BlockPos pos = event.getBlockPos();
		final ChunkCache cache = event.getChunkCache();
		final BlockRendererDispatcher blockRendererDispatcher = event.getBlockRendererDispatcher();
		final BufferBuilder bufferBuilder = event.getBufferBuilder();

		if (!ModUtil.shouldSmooth(state)) {
			return;
		}

		if (state.getBlock() instanceof BlockGrass) {
			event.setCanceled(false);
			return;
		}

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		// The basic block color.
//		int color = block.colorMultiplier(world, x, y, z);
//		final int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, cache, pos, 0);
//		float colorRed = (float) (color >> 16 & 255) / 255.0F;
//		float colorGreen = (float) (color >> 8 & 255) / 255.0F;
//		float colorBlue = (float) (color & 255) / 255.0F;

		float colorRed = 1.0F;
		float colorGreen = 1.0F;
		float colorBlue = 1.0F;

		final LightmapInfo lightmapInfo = ModUtil.getLightmapInfo(pos, cache);

		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

		// The shadow values.
		float shadowBottom = 0.6F;
		float shadowTop = 1.0F;
		float shadowLeft = 0.9F;
		float shadowRight = 0.8F;

//		// The block's icon
//		IIcon icon;
//		if (!renderer.hasOverrideBlockTexture())
//			icon = renderer.getBlockIconFromSideAndMetadata(block, 1, meta);
//		else
//			// Used for the crack texture
//			icon = renderer.overrideBlockTexture;

		final TextureAtlasSprite sprite = ModUtil.getSprite(state, pos, blockRendererDispatcher);
//		final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/sand");
		if (sprite == null) {
			return;
		}

//		// The icon's UVs
//		double minU = (double) icon.getMinU();
//		double minV = (double) icon.getMinV();
//		double maxU = (double) icon.getMaxU();
//		double maxV = (double) icon.getMaxV();

		final double minU = sprite.getMinU();
		final double minV = sprite.getMinV();
		final double maxU = sprite.getMaxU();
		final double maxV = sprite.getMaxV();

		// The 8 points (corners) that make the block.
		Vec3[] points = new Vec3[8];
		points[0] = new Vec3(0.0D, 0.0D, 0.0D);
		points[1] = new Vec3(1.0D, 0.0D, 0.0D);
		points[2] = new Vec3(1.0D, 0.0D, 1.0D);
		points[3] = new Vec3(0.0D, 0.0D, 1.0D);
		points[4] = new Vec3(0.0D, 1.0D, 0.0D);
		points[5] = new Vec3(1.0D, 1.0D, 0.0D);
		points[6] = new Vec3(1.0D, 1.0D, 1.0D);
		points[7] = new Vec3(0.0D, 1.0D, 1.0D);

		// Loop through all the points:
		// Here everything will be 'smoothed'.
		for (int point = 0; point < 8; point++) {
			// Give the point the block's coordinates.
			points[point].xCoord += (double) x;
			points[point].yCoord += (double) y;
			points[point].zCoord += (double) z;

			// Check if the point is NOT intersecting with a manufactured block.
			if (!doesPointIntersectWithManufactured(cache, points[point])) {
				// Check if the block's bottom side intersects with air.
				if (point < 4 && doesPointBottomIntersectWithAir(cache, points[point]))
					points[point].yCoord = (double) y + 1.0D;
					// Check if the block's top side intersects with air.
				else if (point >= 4 && doesPointTopIntersectWithAir(cache, points[point]))
					points[point].yCoord = (double) y;

				// Give the point some random offset.
				points[point] = givePointRoughness(points[point]);
			}
		}

		boolean wasAnythingRendered = false;

		// Loop through all the sides of the block:
		for (EnumFacing side : EnumFacing.VALUES) {

			// Check if the side should be rendered:
			// This prevents a lot of lag!
			if (state.shouldSideBeRendered(cache, pos, side)) {
				wasAnythingRendered = true;

				// When you lower this value the block will become darker.
				float colorFactor = 1.0F;

				// These are the vertices used for the side.
				Vec3 vertex0 = null;
				Vec3 vertex1 = null;
				Vec3 vertex2 = null;
				Vec3 vertex3 = null;

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

				switch (side) {
					case DOWN:
						colorFactor = shadowBottom;
						vertex0 = points[0];
						vertex1 = points[1];
						vertex2 = points[2];
						vertex3 = points[3];
						break;
					case UP:
						colorFactor = shadowTop;
						vertex0 = points[7];
						vertex1 = points[6];
						vertex2 = points[5];
						vertex3 = points[4];
						break;
					case NORTH:
						colorFactor = shadowLeft;
						vertex0 = points[1];
						vertex1 = points[0];
						vertex2 = points[4];
						vertex3 = points[5];
						break;
					case SOUTH:
						colorFactor = shadowRight;
						vertex0 = points[6];
						vertex1 = points[7];
						vertex2 = points[3];
						vertex3 = points[2];
						break;
					case WEST:
						colorFactor = shadowLeft;
						vertex0 = points[0];
						vertex1 = points[3];
						vertex2 = points[7];
						vertex3 = points[4];
						break;
					case EAST:
						colorFactor = shadowRight;
						vertex0 = points[5];
						vertex1 = points[6];
						vertex2 = points[2];
						vertex3 = points[1];
						break;
				}

				// And finally the side is going to be rendered!
				bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue, 0xFF).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue, 0xFF).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue, 0xFF).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue, 0xFF).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

			}

		}

		event.getUsedBlockRenderLayers()[event.getBlockRenderLayer().ordinal()] |= wasAnythingRendered;
		event.setCanceled(wasAnythingRendered);

	}

	public static Vec3 givePointRoughness(Vec3 point) {
		long i = (long) (point.xCoord * 3129871.0D) ^ (long) point.yCoord * 116129781L ^ (long) point.zCoord;
		i = i * i * 42317861L + i * 11L;
		point.xCoord += (double) (((float) (i >> 16 & 15L) / 15.0F - 0.5F) * 0.5F);
		point.yCoord += (double) (((float) (i >> 20 & 15L) / 15.0F - 0.5F) * 0.5F);
		point.zCoord += (double) (((float) (i >> 24 & 15L) / 15.0F - 0.5F) * 0.5F);
		return point;
	}

	public static boolean isBlockAirOrPlant(IBlockState state) {
		Material material = state.getMaterial();
		return material == Material.AIR || material == Material.PLANTS || material == Material.VINE;
	}

	public static boolean doesPointTopIntersectWithAir(IBlockAccess world, Vec3 point) {
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

	public static boolean doesPointBottomIntersectWithAir(IBlockAccess world, Vec3 point) {
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

	public static boolean doesPointIntersectWithManufactured(IBlockAccess world, Vec3 point) {
		for (int i = 0; i < 4; ++i) {
			int x1 = (int) (point.xCoord - (double) (i & 1));
			int z1 = (int) (point.yCoord - (double) (i >> 1 & 1));
			IBlockState block = world.getBlockState(new BlockPos(x1, (int) point.yCoord, z1));
			if (!isBlockAirOrPlant(block) && !ModUtil.shouldSmooth(block)) {
				return true;
			}

			IBlockState block1 = world.getBlockState(new BlockPos(x1, (int) point.yCoord - 1, z1));
			if (!isBlockAirOrPlant(block1) && !ModUtil.shouldSmooth(block1)) {
				return true;
			}
		}
		return false;
	}

	public static void renderPost(final RebuildChunkPostEvent event) {
	}

}
