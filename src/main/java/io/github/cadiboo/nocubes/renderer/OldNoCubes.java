package io.github.cadiboo.nocubes.renderer;

import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

public class OldNoCubes {

	public static boolean renderBlock(IBlockState state, final BlockPos pos, final ChunkCache cache, final BufferBuilder bufferBuilder, final BlockRendererDispatcher blockRendererDispatcher) {

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();
		
		// The basic block color.
//		int color = block.colorMultiplier(world, x, y, z);
		final int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, cache, pos, 0);
		float colorRed = (float) (color >> 16 & 255) / 255.0F;
		float colorGreen = (float) (color >> 8 & 255) / 255.0F;
		float colorBlue = (float) (color & 255) / 255.0F;

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
//		final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();

		if (sprite == null) {
			return false;
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

		// The 8 points that make the block.
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
			if (!doesPointIntersectWithManufactured(world, points[point])) {
				// Check if the block's bottom side intersects with air.
				if (point < 4 && doesPointBottomIntersectWithAir(world, points[point]))
					points[point].yCoord = (double) y + 1.0D;
					// Check if the block's top side intersects with air.
				else if (point >= 4 && doesPointTopIntersectWithAir(world, points[point]))
					points[point].yCoord = (double) y;

				// Give the point some random offset.
				points[point] = givePointRoughness(points[point]);
			}
		}

		// Loop through all the sides of the block:
		for (int side = 0; side < 6; side++) {
			// The coordinates the side is facing to.
			int facingX = x;
			int facingY = y;
			int facingZ = z;
			if (side == 0)
				facingY--;
			else if (side == 1)
				facingY++;
			else if (side == 2)
				facingZ--;
			else if (side == 3)
				facingX++;
			else if (side == 4)
				facingZ++;
			else if (side == 5)
				facingX--;

			// Check if the side should be rendered:
			// This prevents a lot of lag!
			if (renderer.renderAllFaces || block.shouldSideBeRendered(world, facingX, facingY, facingZ, side)) {
				// When you lower this value the block will become darker.
				float colorFactor = 1.0F;

				// This are the vertices used for the side.
				Vec3 vertex0 = null;
				Vec3 vertex1 = null;
				Vec3 vertex2 = null;
				Vec3 vertex3 = null;
				if (side == 0) {
					// Side 0 is the bottom side.
					colorFactor = shadowBottom;
					vertex0 = points[0];
					vertex1 = points[1];
					vertex2 = points[2];
					vertex3 = points[3];

				} else if (side == 1) {
					// Side 1 is the top side.
					colorFactor = shadowTop;
					vertex0 = points[7];
					vertex1 = points[6];
					vertex2 = points[5];
					vertex3 = points[4];

				} else if (side == 2) {
					colorFactor = shadowLeft;
					vertex0 = points[1];
					vertex1 = points[0];
					vertex2 = points[4];
					vertex3 = points[5];

				} else if (side == 3) {
					colorFactor = shadowRight;
					vertex0 = points[2];
					vertex1 = points[1];
					vertex2 = points[5];
					vertex3 = points[6];
				} else if (side == 4) {
					colorFactor = shadowLeft;
					vertex0 = points[3];
					vertex1 = points[2];
					vertex2 = points[6];
					vertex3 = points[7];
				} else if (side == 5) {
					colorFactor = shadowRight;
					vertex0 = points[0];
					vertex1 = points[3];
					vertex2 = points[7];
					vertex3 = points[4];
				}

				// Here is the brightness of the block being set.
				tessellator.setBrightness(block.getMixedBrightnessForBlock(world, facingX, facingY, facingZ));
				// Here is the color of the block being set.
				tessellator.setColorOpaque_F(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen,
						shadowTop * colorFactor * colorBlue);

				// And finally the side is going to be rendered!
				tessellator.addVertexWithUV(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord, minU, maxV);
				tessellator.addVertexWithUV(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord, maxU, maxV);
				tessellator.addVertexWithUV(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord, maxU, minV);
				tessellator.addVertexWithUV(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord, minU, minV);
			}
		}

	}

	public static class Vec3 {

		public double xCoord;
		public double yCoord;
		public double zCoord;

		public Vec3(final double xCoord, final double yCoord, final double zCoord) {
			this.xCoord = xCoord;
			this.yCoord = yCoord;
			this.zCoord = zCoord;
		}

	}


	public static Vec3 givePointRoughness(Vec3 point) {
		long i = (long)(point.xCoord * 3129871.0D) ^ (long)point.yCoord * 116129781L ^ (long)point.zCoord;
		i = i * i * 42317861L + i * 11L;
		point.xCoord += (double)(((float)(i >> 16 & 15L) / 15.0F - 0.5F) * 0.5F);
		point.yCoord += (double)(((float)(i >> 20 & 15L) / 15.0F - 0.5F) * 0.5F);
		point.zCoord += (double)(((float)(i >> 24 & 15L) / 15.0F - 0.5F) * 0.5F);
		return point;
	}

	public static boolean isBlockAirOrPlant(Block block) {
		Material material = block.getMaterial();
		return material == Material.AIR || material == Material.PLANTS || material == Material.VINE;
	}

	public static boolean doesPointTopIntersectWithAir(IBlockAccess world, Vec3 point) {
		boolean intersects = false;

		for(int i = 0; i < 4; ++i) {
			int x1 = (int)(point.xCoord - (double)(i & 1));
			int z1 = (int)(point.zCoord - (double)(i >> 1 & 1));
			if (!isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord, z1))) {
				return false;
			}

			if (isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord - 1, z1))) {
				intersects = true;
			}
		}

		return intersects;
	}

	public static boolean doesPointBottomIntersectWithAir(IBlockAccess world, Vec3 point) {
		boolean intersects = false;
		boolean notOnly = false;

		for(int i = 0; i < 4; ++i) {
			int x1 = (int)(point.xCoord - (double)(i & 1));
			int z1 = (int)(point.zCoord - (double)(i >> 1 & 1));
			if (!isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord - 1, z1))) {
				return false;
			}

			if (!isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord + 1, z1))) {
				notOnly = true;
			}

			if (isBlockAirOrPlant(world.getBlock(x1, (int)point.yCoord, z1))) {
				intersects = true;
			}
		}

		return intersects && notOnly;
	}

	public static boolean doesPointIntersectWithManufactured(IBlockAccess world, Vec3 point) {
		for(int i = 0; i < 4; ++i) {
			int x1 = (int)(point.xCoord - (double)(i & 1));
			int z1 = (int)(point.yCoord - (double)(i >> 1 & 1));
			Block block = world.getBlock(x1, (int)point.yCoord, z1);
			if (!isBlockAirOrPlant(block) && !NoCubes_isBlockSmoothed(block)) {
				return true;
			}

			Block block1 = world.getBlock(x1, (int)point.yCoord - 1, z1);
			if (!isBlockAirOrPlant(block1) && !NoCubes_isBlockSmoothed(block1)) {
				return true;
			}
		}

		return false;
	}

	// Taken from NoCubes 0.3
	// public static final int renderId = RenderingRegistry.getNextAvailableRenderId();
	public static boolean NoCubes_isBlockSmoothed(Block block) {
			return block.getRenderType() == renderId;
	}

}