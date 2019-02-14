package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.render.block.BlockColorMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ExtendedBlockView;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class ExtendedLiquidBlockRenderer {

	public static boolean renderExtendedLiquid(
			@Nonnull final SpriteAtlasTexture textureMap,
			@Nonnull final BlockColorMap blockColors,
			final double renderPosX, final double renderPosY, final double renderPosZ,
			@Nonnull final BlockPos liquidPos,
			@Nonnull final ExtendedBlockView blockAccess,
			//TODO: eventually do better liquid rendering for 0.3.0
//			@Nonnull final IBlockState smoothableState,
			@Nonnull final BlockState liquidState,
			@Nonnull final BufferBuilder bufferBuilder) {

		final TextureAtlasSprite[] atlasSpritesLava = {
				textureMap.getAtlasSprite("minecraft:blocks/lava_still"),
				textureMap.getAtlasSprite("minecraft:blocks/lava_flow")
		};
		final TextureAtlasSprite[] atlasSpritesWater = {
				textureMap.getAtlasSprite("minecraft:blocks/water_still"),
				textureMap.getAtlasSprite("minecraft:blocks/water_flow")
		};
		final TextureAtlasSprite atlasSpriteWaterOverlay = textureMap.getAtlasSprite("minecraft:blocks/water_overlay");

		BlockLiquid blockliquid = (BlockLiquid) liquidState.getBlock();
		boolean isLava = liquidState.getMaterial() == Material.LAVA;
		TextureAtlasSprite[] sprites = isLava ? atlasSpritesLava : atlasSpritesWater;

		int colorMultiplier = blockColors.colorMultiplier(liquidState, blockAccess, liquidPos, 0);
		float red = (float) (colorMultiplier >> 16 & 255) / 255.0F;
		float green = (float) (colorMultiplier >> 8 & 255) / 255.0F;
		float blue = (float) (colorMultiplier & 255) / 255.0F;

		boolean shouldRenderUp = liquidState.shouldSideBeRendered(blockAccess, liquidPos, EnumFacing.UP);
		boolean shouldRenderDown = liquidState.shouldSideBeRendered(blockAccess, liquidPos, EnumFacing.DOWN);
		boolean[] renderSides = new boolean[]{
				liquidState.shouldSideBeRendered(blockAccess, liquidPos, EnumFacing.NORTH),
				liquidState.shouldSideBeRendered(blockAccess, liquidPos, EnumFacing.SOUTH),
				liquidState.shouldSideBeRendered(blockAccess, liquidPos, EnumFacing.WEST),
				liquidState.shouldSideBeRendered(blockAccess, liquidPos, EnumFacing.EAST)
		};

		if (!shouldRenderUp && !shouldRenderDown && !renderSides[0] && !renderSides[1] && !renderSides[2] && !renderSides[3]) {
			return false;
		} else {
			boolean wasAnythingRendered = false;
//				float f3 = 0.5F;
//				float f4 = 1.0F;
//				float f5 = 0.8F;
//				float f6 = 0.6F;
			Material material = liquidState.getMaterial();
			float fluidHeight = getFluidHeight(blockAccess, liquidPos, material);
			float fluidHeightSouth = getFluidHeight(blockAccess, liquidPos.south(), material);
			float fluidHeightEastSouth = getFluidHeight(blockAccess, liquidPos.east().south(), material);
			float fluidHeightEast = getFluidHeight(blockAccess, liquidPos.east(), material);
//			double x = (double) renderPos.getX();
//			double y = (double) renderPos.getY();
//			double z = (double) renderPos.getZ();
			final double x = renderPosX;
			final double y = renderPosY;
			final double z = renderPosZ;
//				float f11 = 0.001F;

			if (shouldRenderUp) {
				wasAnythingRendered |= renderFluidUp(blockAccess, liquidPos, material, liquidState, blockliquid, sprites, fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast, red, green, blue, bufferBuilder, x, y, z);
			}

			if (shouldRenderDown) {
				wasAnythingRendered |= renderFluidDown(sprites, liquidState, blockAccess, liquidPos, bufferBuilder, x, y, z);
			}

			for (int posAddIndex = 0; posAddIndex < 4; ++posAddIndex) {
				int addX = 0;
				int addY = 0;

				if (posAddIndex == 0) {
					--addY;
				}

				if (posAddIndex == 1) {
					++addY;
				}

				if (posAddIndex == 2) {
					--addX;
				}

				if (posAddIndex == 3) {
					++addX;
				}

				BlockPos blockpos = liquidPos.add(addX, 0, addY);
				TextureAtlasSprite textureatlassprite1 = sprites[1];

				if (!isLava) {
					IBlockState state = blockAccess.getBlockState(blockpos);

					if (state.getBlockFaceShape(blockAccess, blockpos, EnumFacing.VALUES[posAddIndex + 2].getOpposite()) == BlockFaceShape.SOLID) {
						textureatlassprite1 = atlasSpriteWaterOverlay;
					}
				}

				if (renderSides[posAddIndex]) {
					float yAdd_f39;
					float yAdd_f40;
					double x_d3;
					double z_d4;
					double x_d5;
					double z_d6;

					if (posAddIndex == 0) {
						yAdd_f39 = fluidHeight;
						yAdd_f40 = fluidHeightEast;
						x_d3 = x;
						x_d5 = x + 1.0D;
						z_d4 = z + 0.0010000000474974513D;
						z_d6 = z + 0.0010000000474974513D;
					} else if (posAddIndex == 1) {
						yAdd_f39 = fluidHeightEastSouth;
						yAdd_f40 = fluidHeightSouth;
						x_d3 = x + 1.0D;
						x_d5 = x;
						z_d4 = z + 1.0D - 0.0010000000474974513D;
						z_d6 = z + 1.0D - 0.0010000000474974513D;
					} else if (posAddIndex == 2) {
						yAdd_f39 = fluidHeightSouth;
						yAdd_f40 = fluidHeight;
						x_d3 = x + 0.0010000000474974513D;
						x_d5 = x + 0.0010000000474974513D;
						z_d4 = z + 1.0D;
						z_d6 = z;
					} else {
						yAdd_f39 = fluidHeightEast;
						yAdd_f40 = fluidHeightEastSouth;
						x_d3 = x + 1.0D - 0.0010000000474974513D;
						x_d5 = x + 1.0D - 0.0010000000474974513D;
						z_d4 = z;
						z_d6 = z + 1.0D;
					}

					wasAnythingRendered = true;
//					float u_f41 = textureatlassprite1.getInterpolatedU(0.0D);
					final float u_f41 = ClientUtil.getMinU(textureatlassprite1);
					float u_f27 = textureatlassprite1.getInterpolatedU(8.0D);
					float f28 = textureatlassprite1.getInterpolatedV((double) ((1.0F - yAdd_f39) * 16.0F * 0.5F));
					float v_f29 = textureatlassprite1.getInterpolatedV((double) ((1.0F - yAdd_f40) * 16.0F * 0.5F));
					float v_f30 = textureatlassprite1.getInterpolatedV(8.0D);
					int packedLightmapCoords = liquidState.getPackedLightmapCoords(blockAccess, blockpos);
					int skyLight = packedLightmapCoords >> 16 & 65535;
					int blockLight = packedLightmapCoords & 65535;
					float colorFloatMultiplier = posAddIndex < 2 ? 0.8F : 0.6F;
					float redFloat = 1.0F * colorFloatMultiplier * red;
					float greenFloat = 1.0F * colorFloatMultiplier * green;
					float blueFloat = 1.0F * colorFloatMultiplier * blue;
					bufferBuilder.pos(x_d3, y + (double) yAdd_f39, z_d4).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f41, (double) f28).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(x_d5, y + (double) yAdd_f40, z_d6).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f27, (double) v_f29).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(x_d5, y + 0.0D, z_d6).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f27, (double) v_f30).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(x_d3, y + 0.0D, z_d4).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f41, (double) v_f30).lightmap(skyLight, blockLight).endVertex();

					if (textureatlassprite1 != atlasSpriteWaterOverlay) {
						bufferBuilder.pos(x_d3, y + 0.0D, z_d4).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f41, (double) v_f30).lightmap(skyLight, blockLight).endVertex();
						bufferBuilder.pos(x_d5, y + 0.0D, z_d6).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f27, (double) v_f30).lightmap(skyLight, blockLight).endVertex();
						bufferBuilder.pos(x_d5, y + (double) yAdd_f40, z_d6).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f27, (double) v_f29).lightmap(skyLight, blockLight).endVertex();
						bufferBuilder.pos(x_d3, y + (double) yAdd_f39, z_d4).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f41, (double) f28).lightmap(skyLight, blockLight).endVertex();
					}
				}
			}

			return wasAnythingRendered;
		}
	}

	private static boolean renderFluidUp(final IBlockAccess blockAccess, final BlockPos liquidPos, final Material material, final IBlockState liquidState, final BlockLiquid blockliquid, final TextureAtlasSprite[] sprites, float fluidHeight, float fluidHeightSouth, float fluidHeightEastSouth, float fluidHeightEast, final float red, final float green, final float blue, final BufferBuilder bufferBuilder, double x, double y, double z) {
		float slopeAngle = BlockLiquid.getSlopeAngle(blockAccess, liquidPos, material, liquidState);
		TextureAtlasSprite textureAtlasSprite = slopeAngle > -999.0F ? sprites[1] : sprites[0];
		fluidHeight -= 0.001F;
		fluidHeightSouth -= 0.001F;
		fluidHeightEastSouth -= 0.001F;
		fluidHeightEast -= 0.001F;
		float minU;
		float u_f14;
		float maxU;
		float u_f16;
		float minV;
		float maxV;
		float v_f19;
		float v_f20;

		if (slopeAngle < -999.0F) {
//			minU = textureAtlasSprite.getInterpolatedU(0.0D);
			minU = ClientUtil.getMinU(textureAtlasSprite);
//			minV = textureAtlasSprite.getInterpolatedV(0.0D);
			minV = ClientUtil.getMinV(textureAtlasSprite);
			u_f14 = minU;
//			maxV = textureAtlasSprite.getInterpolatedV(16.0D);
			maxV = ClientUtil.getMaxV(textureAtlasSprite);
//			maxU = textureAtlasSprite.getInterpolatedU(16.0D);
			maxU = ClientUtil.getMaxU(textureAtlasSprite);
			v_f19 = maxV;
			u_f16 = maxU;
			v_f20 = minV;
		} else {
			float sinSlopeAngle = MathHelper.sin(slopeAngle) * 0.25F;
			float cosSlopeAngle = MathHelper.cos(slopeAngle) * 0.25F;
//						float f23 = 8.0F;
			minU = textureAtlasSprite.getInterpolatedU((double) (8.0F + (-cosSlopeAngle - sinSlopeAngle) * 16.0F));
			minV = textureAtlasSprite.getInterpolatedV((double) (8.0F + (-cosSlopeAngle + sinSlopeAngle) * 16.0F));
			u_f14 = textureAtlasSprite.getInterpolatedU((double) (8.0F + (-cosSlopeAngle + sinSlopeAngle) * 16.0F));
			maxV = textureAtlasSprite.getInterpolatedV((double) (8.0F + (cosSlopeAngle + sinSlopeAngle) * 16.0F));
			maxU = textureAtlasSprite.getInterpolatedU((double) (8.0F + (cosSlopeAngle + sinSlopeAngle) * 16.0F));
			v_f19 = textureAtlasSprite.getInterpolatedV((double) (8.0F + (cosSlopeAngle - sinSlopeAngle) * 16.0F));
			u_f16 = textureAtlasSprite.getInterpolatedU((double) (8.0F + (cosSlopeAngle - sinSlopeAngle) * 16.0F));
			v_f20 = textureAtlasSprite.getInterpolatedV((double) (8.0F + (-cosSlopeAngle - sinSlopeAngle) * 16.0F));
		}

		int packedLightmapCoords = liquidState.getPackedLightmapCoords(blockAccess, liquidPos);
		int skyLight = packedLightmapCoords >> 16 & 65535;
		int blockLight = packedLightmapCoords & 65535;
		float redFloat = 1.0F * red;
		float greenFloat = 1.0F * green;
		float blueFloat = 1.0F * blue;
		bufferBuilder.pos(x + 0.0D, y + (double) fluidHeight, z + 0.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
		bufferBuilder.pos(x + 0.0D, y + (double) fluidHeightSouth, z + 1.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f14, (double) maxV).lightmap(skyLight, blockLight).endVertex();
		bufferBuilder.pos(x + 1.0D, y + (double) fluidHeightEastSouth, z + 1.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) maxU, (double) v_f19).lightmap(skyLight, blockLight).endVertex();
		bufferBuilder.pos(x + 1.0D, y + (double) fluidHeightEast, z + 0.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f16, (double) v_f20).lightmap(skyLight, blockLight).endVertex();

		if (blockliquid.shouldRenderSides(blockAccess, liquidPos.up())) {
			bufferBuilder.pos(x + 0.0D, y + (double) fluidHeight, z + 0.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 1.0D, y + (double) fluidHeightEast, z + 0.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f16, (double) v_f20).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 1.0D, y + (double) fluidHeightEastSouth, z + 1.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) maxU, (double) v_f19).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 0.0D, y + (double) fluidHeightSouth, z + 1.0D).color(redFloat, greenFloat, blueFloat, 1.0F).tex((double) u_f14, (double) maxV).lightmap(skyLight, blockLight).endVertex();
		}
		return true;
	}

	private static boolean renderFluidDown(TextureAtlasSprite[] sprites, IBlockState blockStateIn, IBlockAccess blockAccess, BlockPos liquidPos, BufferBuilder bufferBuilderIn, double x, double y, double z) {
		float minU = sprites[0].getMinU();
		float maxU = sprites[0].getMaxU();
		float minV = sprites[0].getMinV();
		float maxV = sprites[0].getMaxV();
		int packedLightmapCoords = blockStateIn.getPackedLightmapCoords(blockAccess, liquidPos.down());
		int skyLight = packedLightmapCoords >> 16 & 65535;
		int blockLight = packedLightmapCoords & 65535;
		bufferBuilderIn.pos(x, y, z + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) minU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
		bufferBuilderIn.pos(x, y, z).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
		bufferBuilderIn.pos(x + 1.0D, y, z).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) maxU, (double) minV).lightmap(skyLight, blockLight).endVertex();
		bufferBuilderIn.pos(x + 1.0D, y, z + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) maxU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
		return true;
	}

	private static float getFluidHeight(IBlockAccess blockAccess, BlockPos blockPosIn, Material blockMaterial) {
		int divisor = 0;
		float liquidHeightPercentage = 0.0F;

		for (int posAdd = 0; posAdd < 4; ++posAdd) {
			BlockPos blockpos = blockPosIn.add(-(posAdd & 1), 0, -(posAdd >> 1 & 1));

			if (blockAccess.getBlockState(blockpos.up()).getMaterial() == blockMaterial) {
				return 1.0F;
			}

			IBlockState iblockstate = blockAccess.getBlockState(blockpos);
			Material material = iblockstate.getMaterial();

			if (material != blockMaterial) {
				if (!material.isSolid()) {
					++liquidHeightPercentage;
					++divisor;
				}
			} else {
				int liquidLevel = iblockstate.getValue(BlockLiquid.LEVEL);

				if (liquidLevel >= 8 || liquidLevel == 0) {
					liquidHeightPercentage += BlockLiquid.getLiquidHeightPercent(liquidLevel) * 10.0F;
					divisor += 10;
				}

				liquidHeightPercentage += BlockLiquid.getLiquidHeightPercent(liquidLevel);
				++divisor;
			}
		}

		return 1.0F - liquidHeightPercentage / (float) divisor;
	}

}
