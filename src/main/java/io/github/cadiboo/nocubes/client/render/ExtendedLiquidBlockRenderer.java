package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.client.SmoothLightingBlockFluidRenderer;
import io.github.cadiboo.nocubes.client.UVHelper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

import static io.github.cadiboo.nocubes.config.ModConfig.fluid;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public class ExtendedLiquidBlockRenderer {

	public static boolean renderExtendedLiquid(
			final double renderPosX, final double renderPosY, final double renderPosZ,
			@Nonnull final BlockPos liquidPos,
			@Nonnull final IBlockAccess blockAccess,
			//TODO: eventually do better liquid rendering for 0.3.0
//			@Nonnull final IBlockState smoothableState,
			@Nonnull final IBlockState liquidState,
			@Nonnull final BufferBuilder bufferBuilderIn
	) {

		final PooledMutableBlockPos renderPos = PooledMutableBlockPos.retain(renderPosX, renderPosY, renderPosZ);
		try {
			final SmoothLightingBlockFluidRenderer fluidRenderer = ClientProxy.fluidRenderer;

			final IBlockState blockStateIn = liquidState;
			final BlockPos blockPosIn = liquidPos;

			BlockLiquid blockliquid = (BlockLiquid) blockStateIn.getBlock();
			boolean isLava = blockStateIn.getMaterial() == Material.LAVA;
			TextureAtlasSprite[] atextureatlassprite = isLava ? fluidRenderer.atlasSpritesLava : fluidRenderer.atlasSpritesWater;
			int color = fluidRenderer.blockColors.colorMultiplier(blockStateIn, blockAccess, blockPosIn, 0);
			float redFloat = (float) (color >> 16 & 255) / 255.0F;
			float greenFloat = (float) (color >> 8 & 255) / 255.0F;
			float blueFloat = (float) (color & 255) / 255.0F;
			boolean shouldTopBeRendered = blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.UP);
			boolean shouldBottomBeRendered = blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.DOWN);
			if (NoCubes.isEnabled()) {
//				shouldTopBeRendered &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(blockPosIn.offset(EnumFacing.UP)));
				shouldBottomBeRendered &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(blockPosIn.offset(EnumFacing.DOWN)));
			}
			boolean[] shouldHorizontalSideBeRendered = new boolean[]{
					blockStateIn.shouldSideBeRendered(blockAccess, renderPos, EnumFacing.NORTH),
					blockStateIn.shouldSideBeRendered(blockAccess, renderPos, EnumFacing.SOUTH),
					blockStateIn.shouldSideBeRendered(blockAccess, renderPos, EnumFacing.WEST),
					blockStateIn.shouldSideBeRendered(blockAccess, renderPos, EnumFacing.EAST)
			};
			if (NoCubes.isEnabled()) {
				shouldHorizontalSideBeRendered[0] &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(renderPos.offset(EnumFacing.NORTH)));
				shouldHorizontalSideBeRendered[1] &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(renderPos.offset(EnumFacing.SOUTH)));
				shouldHorizontalSideBeRendered[2] &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(renderPos.offset(EnumFacing.WEST)));
				shouldHorizontalSideBeRendered[3] &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(renderPos.offset(EnumFacing.EAST)));
			}

			if (!shouldTopBeRendered && !shouldBottomBeRendered && !shouldHorizontalSideBeRendered[0] && !shouldHorizontalSideBeRendered[1] && !shouldHorizontalSideBeRendered[2] && !shouldHorizontalSideBeRendered[3]) {
				return false;
			} else {
				boolean wasAnythingRendered = false;
//				float f3 = 0.5F;
//				float f4 = 1.0F;
//				float f5 = 0.8F;
//				float f6 = 0.6F;
				Material material = blockStateIn.getMaterial();
				float fluidHeight = fluidRenderer.getFluidHeight(blockAccess, blockPosIn, material);
				float fluidHeightS = fluidRenderer.getFluidHeight(blockAccess, blockPosIn.south(), material);
				float fluidHeightES = fluidRenderer.getFluidHeight(blockAccess, blockPosIn.east().south(), material);
				float fluidHeightE = fluidRenderer.getFluidHeight(blockAccess, blockPosIn.east(), material);

//				double posX = (double) blockPosIn.getX();
//				double posY = (double) blockPosIn.getY();
//				double posZ = (double) blockPosIn.getZ();
				final double posX = renderPosX;
				final double posY = renderPosY;
				final double posZ = renderPosZ;

//			    float f11 = 0.001F;

				if (shouldTopBeRendered) {
					wasAnythingRendered = true;
					float slopeAngle = BlockLiquid.getSlopeAngle(blockAccess, blockPosIn, material, blockStateIn);
					TextureAtlasSprite textureatlassprite = slopeAngle > -999.0F ? atextureatlassprite[1] : atextureatlassprite[0];
					fluidHeight -= 0.001F;
					fluidHeightS -= 0.001F;
					fluidHeightES -= 0.001F;
					fluidHeightE -= 0.001F;
//					fluidRenderer.renderTop(blockAccess, blockStateIn, blockPosIn, bufferBuilderIn, blockliquid, redFloat, greenFloat, blueFloat, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, posX, posY, posZ, slopeAngle, textureatlassprite);
					renderTop(blockAccess, blockStateIn, blockPosIn, bufferBuilderIn, blockliquid, redFloat, greenFloat, blueFloat, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, posX, posY, posZ, slopeAngle, textureatlassprite, fluidRenderer, renderPos);
				}

				if (shouldBottomBeRendered) {
					wasAnythingRendered = true;
//					fluidRenderer.renderBottom(blockAccess, blockStateIn, blockPosIn, bufferBuilderIn, atextureatlassprite, posX, posY, posZ);
					fluidRenderer.renderBottom(blockAccess, blockStateIn, renderPos, bufferBuilderIn, atextureatlassprite, posX, posY, posZ);
				}

				for (int horiontalIndex = 0; horiontalIndex < 4; ++horiontalIndex) {
					int xAdd = 0;
					int zAdd = 0;

					if (horiontalIndex == 0) {
						--zAdd;
					}

					if (horiontalIndex == 1) {
						++zAdd;
					}

					if (horiontalIndex == 2) {
						--xAdd;
					}

					if (horiontalIndex == 3) {
						++xAdd;
					}

					BlockPos blockpos = blockPosIn.add(xAdd, 0, zAdd);
					TextureAtlasSprite textureatlassprite1 = atextureatlassprite[1];

					if (!isLava) {
						IBlockState state = blockAccess.getBlockState(blockpos);

						if (state.getBlockFaceShape(blockAccess, blockpos, EnumFacing.VALUES[horiontalIndex + 2].getOpposite()) == net.minecraft.block.state.BlockFaceShape.SOLID) {
							textureatlassprite1 = fluidRenderer.atlasSpriteWaterOverlay;
						}
					}

					if (shouldHorizontalSideBeRendered[horiontalIndex]) {
						float yAdd1;
						float yAdd0;
						double posX0;
						double posZ0;
						double posX1;
						double posZ1;

						if (horiontalIndex == 0) {
							yAdd1 = fluidHeight;
							yAdd0 = fluidHeightE;
							posX0 = posX;
							posX1 = posX + 1.0D;
							posZ0 = posZ + 0.0010000000474974513D;
							posZ1 = posZ + 0.0010000000474974513D;
						} else if (horiontalIndex == 1) {
							yAdd1 = fluidHeightES;
							yAdd0 = fluidHeightS;
							posX0 = posX + 1.0D;
							posX1 = posX;
							posZ0 = posZ + 1.0D - 0.0010000000474974513D;
							posZ1 = posZ + 1.0D - 0.0010000000474974513D;
						} else if (horiontalIndex == 2) {
							yAdd1 = fluidHeightS;
							yAdd0 = fluidHeight;
							posX0 = posX + 0.0010000000474974513D;
							posX1 = posX + 0.0010000000474974513D;
							posZ0 = posZ + 1.0D;
							posZ1 = posZ;
						} else {
							yAdd1 = fluidHeightE;
							yAdd0 = fluidHeightES;
							posX0 = posX + 1.0D - 0.0010000000474974513D;
							posX1 = posX + 1.0D - 0.0010000000474974513D;
							posZ0 = posZ;
							posZ1 = posZ + 1.0D;
						}

						wasAnythingRendered = true;
//					    float u0 = textureatlassprite1.getInterpolatedU(0.0D);
						float u0 = UVHelper.getMinU(textureatlassprite1);
						float u1 = textureatlassprite1.getInterpolatedU(8.0D);
						float v0 = textureatlassprite1.getInterpolatedV((double) ((1.0F - yAdd1) * 16.0F * 0.5F));
						float v1 = textureatlassprite1.getInterpolatedV((double) ((1.0F - yAdd0) * 16.0F * 0.5F));
						float v2 = textureatlassprite1.getInterpolatedV(8.0D);

						// Nope, not dealing with smooth side lighting.
						int packedLight = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos);
						int skylight = packedLight >> 16 & 65535;
						int blocklight = packedLight & 65535;

						float diffuseLighting = horiontalIndex < 2 ? 0.8F : 0.6F;
						float red = 1.0F * diffuseLighting * redFloat;
						float green = 1.0F * diffuseLighting * greenFloat;
						float blue = 1.0F * diffuseLighting * blueFloat;
						bufferBuilderIn.pos(posX0, posY + (double) yAdd1, posZ0).color(red, green, blue, 1.0F).tex((double) u0, (double) v0).lightmap(skylight, blocklight).endVertex();
						bufferBuilderIn.pos(posX1, posY + (double) yAdd0, posZ1).color(red, green, blue, 1.0F).tex((double) u1, (double) v1).lightmap(skylight, blocklight).endVertex();
						bufferBuilderIn.pos(posX1, posY + 0.0D, posZ1).color(red, green, blue, 1.0F).tex((double) u1, (double) v2).lightmap(skylight, blocklight).endVertex();
						bufferBuilderIn.pos(posX0, posY + 0.0D, posZ0).color(red, green, blue, 1.0F).tex((double) u0, (double) v2).lightmap(skylight, blocklight).endVertex();

						if (textureatlassprite1 != fluidRenderer.atlasSpriteWaterOverlay) {
							bufferBuilderIn.pos(posX0, posY + 0.0D, posZ0).color(red, green, blue, 1.0F).tex((double) u0, (double) v2).lightmap(skylight, blocklight).endVertex();
							bufferBuilderIn.pos(posX1, posY + 0.0D, posZ1).color(red, green, blue, 1.0F).tex((double) u1, (double) v2).lightmap(skylight, blocklight).endVertex();
							bufferBuilderIn.pos(posX1, posY + (double) yAdd0, posZ1).color(red, green, blue, 1.0F).tex((double) u1, (double) v1).lightmap(skylight, blocklight).endVertex();
							bufferBuilderIn.pos(posX0, posY + (double) yAdd1, posZ0).color(red, green, blue, 1.0F).tex((double) u0, (double) v0).lightmap(skylight, blocklight).endVertex();
						}
					}
				}

				return wasAnythingRendered;
			}
		} finally {
			renderPos.release();
		}
	}

	private static void renderTop(@Nonnull final IBlockAccess blockAccess, final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final BufferBuilder bufferBuilderIn, final BlockLiquid blockliquid, final float redFloat, final float greenFloat, final float blueFloat, final float fluidHeight, final float fluidHeightS, final float fluidHeightES, final float fluidHeightE, final double posX, final double posY, final double posZ, final float slopeAngle, final TextureAtlasSprite textureatlassprite, final SmoothLightingBlockFluidRenderer fluidRenderer, final BlockPos renderPos) {
		float u0;
		float u1;
		float u2;
		float u3;
		float v0;
		float v1;
		float v2;
		float v3;

		if (slopeAngle < -999.0F) {
			if (!fluid.naturalFluidTextures) {
//				u0 = textureatlassprite.getInterpolatedU(0.0D);
//				v0 = textureatlassprite.getInterpolatedV(0.0D);
				u0 = UVHelper.getMinU(textureatlassprite);
				v0 = UVHelper.getMinV(textureatlassprite);
				u1 = u0;
//				v1 = textureatlassprite.getInterpolatedV(16.0D);
//				u2 = textureatlassprite.getInterpolatedU(16.0D);
				v1 = UVHelper.getMaxV(textureatlassprite);
				u2 = UVHelper.getMaxU(textureatlassprite);
				v2 = v1;
				u3 = u2;
				v3 = v0;
			} else {
				final int rand = (int) (MathHelper.getPositionRandom(blockPosIn) % 7);
				switch (rand) {
					default:
					case 0:
						u0 = UVHelper.getMinU(textureatlassprite);
						v0 = UVHelper.getMinV(textureatlassprite);
						v1 = UVHelper.getMaxV(textureatlassprite);
						u2 = UVHelper.getMaxU(textureatlassprite);
						break;
					case 1:
					case 2:
						u0 = UVHelper.getMaxU(textureatlassprite);
						v0 = UVHelper.getMaxV(textureatlassprite);
						v1 = UVHelper.getMinV(textureatlassprite);
						u2 = UVHelper.getMinU(textureatlassprite);
						break;
					case 3:
					case 4:
						u0 = UVHelper.getMinU(textureatlassprite);
						v0 = UVHelper.getMinV(textureatlassprite);
						v1 = UVHelper.getMaxV(textureatlassprite);
						u2 = UVHelper.getMaxU(textureatlassprite);
						break;
					case 5:
					case 6:
						u0 = UVHelper.getMaxU(textureatlassprite);
						v0 = UVHelper.getMaxV(textureatlassprite);
						v1 = UVHelper.getMinV(textureatlassprite);
						u2 = UVHelper.getMinU(textureatlassprite);
						break;
				}
				u1 = u0;
				v2 = v1;
				u3 = u2;
				v3 = v0;
			}
		} else {
			float quarterSinSlopeAngle = MathHelper.sin(slopeAngle) * 0.25F;
			float quarterCosSlopeAngle = MathHelper.cos(slopeAngle) * 0.25F;
//			float f23 = 8.0F;
			u0 = textureatlassprite.getInterpolatedU((double) (8.0F + (-quarterCosSlopeAngle - quarterSinSlopeAngle) * 16.0F));
			v0 = textureatlassprite.getInterpolatedV((double) (8.0F + (-quarterCosSlopeAngle + quarterSinSlopeAngle) * 16.0F));
			u1 = textureatlassprite.getInterpolatedU((double) (8.0F + (-quarterCosSlopeAngle + quarterSinSlopeAngle) * 16.0F));
			v1 = textureatlassprite.getInterpolatedV((double) (8.0F + (quarterCosSlopeAngle + quarterSinSlopeAngle) * 16.0F));
			u2 = textureatlassprite.getInterpolatedU((double) (8.0F + (quarterCosSlopeAngle + quarterSinSlopeAngle) * 16.0F));
			v2 = textureatlassprite.getInterpolatedV((double) (8.0F + (quarterCosSlopeAngle - quarterSinSlopeAngle) * 16.0F));
			u3 = textureatlassprite.getInterpolatedU((double) (8.0F + (quarterCosSlopeAngle - quarterSinSlopeAngle) * 16.0F));
			v3 = textureatlassprite.getInterpolatedV((double) (8.0F + (-quarterCosSlopeAngle - quarterSinSlopeAngle) * 16.0F));
		}

		final int realPackedLightmapCoords = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn);
		final int renderPackedLightmapCoords = blockStateIn.getPackedLightmapCoords(blockAccess, renderPos);
		final int skyLightmapCoords = (renderPackedLightmapCoords >> 16 & 65535) == 0 ? realPackedLightmapCoords >> 16 & 65535 : renderPackedLightmapCoords >> 16 & 65535;
		final int blockLightmapCoords = (renderPackedLightmapCoords & 65535) == 0 ? realPackedLightmapCoords & 65535 : renderPackedLightmapCoords & 65535;
		final int packedLightmapCoords = (skyLightmapCoords << 16) | blockLightmapCoords;

		if (fluid.smoothFluidLighting) {
			final int realPackedLightmapCoordsSouth = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.south());
			final int renderPackedLightmapCoordsSouth = blockStateIn.getPackedLightmapCoords(blockAccess, renderPos.south());
			final int realPackedLightmapCoordsSouthEast = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.south().east());
			final int renderPackedLightmapCoordsSouthEast = blockStateIn.getPackedLightmapCoords(blockAccess, renderPos.south().east());
			final int realPackedLightmapCoordsEast = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.east());
			final int renderPackedLightmapCoordsEast = blockStateIn.getPackedLightmapCoords(blockAccess, renderPos.east());

//			final int packedLightmapCoordsSouth = renderPackedLightmapCoordsSouth;
//			final int packedLightmapCoordsSouthEast = renderPackedLightmapCoordsSouthEast;
//			final int packedLightmapCoordsEast = renderPackedLightmapCoordsEast;

//			final int packedLightmapCoordsSouth = (realPackedLightmapCoordsSouth & 65535) > (renderPackedLightmapCoordsSouth & 65535) ? realPackedLightmapCoordsSouth : renderPackedLightmapCoordsSouth;
//			final int packedLightmapCoordsSouthEast = (realPackedLightmapCoordsSouthEast & 65535) > (renderPackedLightmapCoordsSouthEast & 65535) ? realPackedLightmapCoordsSouthEast : renderPackedLightmapCoordsSouthEast;
//			final int packedLightmapCoordsEast = (realPackedLightmapCoordsEast & 65535) > (renderPackedLightmapCoordsEast & 65535) ? realPackedLightmapCoordsEast : renderPackedLightmapCoordsEast;

			final int skyLightmapCoordsSouth = (renderPackedLightmapCoordsSouth >> 16 & 65535) == 0 ? realPackedLightmapCoordsSouth >> 16 & 65535 : renderPackedLightmapCoordsSouth >> 16 & 65535;
			final int blockLightmapCoordsSouth = (renderPackedLightmapCoordsSouth & 65535) == 0 ? realPackedLightmapCoordsSouth & 65535 : renderPackedLightmapCoordsSouth & 65535;
			final int packedLightmapCoordsSouth = (skyLightmapCoordsSouth << 16) | blockLightmapCoordsSouth;

			final int skyLightmapCoordsSouthEast = (renderPackedLightmapCoordsSouthEast >> 16 & 65535) == 0 ? realPackedLightmapCoordsSouthEast >> 16 & 65535 : renderPackedLightmapCoordsSouthEast >> 16 & 65535;
			final int blockLightmapCoordsSouthEast = (renderPackedLightmapCoordsSouthEast & 65535) == 0 ? realPackedLightmapCoordsSouthEast & 65535 : renderPackedLightmapCoordsSouthEast & 65535;
			final int packedLightmapCoordsSouthEast = (skyLightmapCoordsSouthEast << 16) | blockLightmapCoordsSouthEast;

			final int skyLightmapCoordsEast = (renderPackedLightmapCoordsEast >> 16 & 65535) == 0 ? realPackedLightmapCoordsEast >> 16 & 65535 : renderPackedLightmapCoordsEast >> 16 & 65535;
			final int blockLightmapCoordsEast = (renderPackedLightmapCoordsEast & 65535) == 0 ? realPackedLightmapCoordsEast & 65535 : renderPackedLightmapCoordsEast & 65535;
			final int packedLightmapCoordsEast = (skyLightmapCoordsEast << 16) | blockLightmapCoordsEast;

//			
			fluidRenderer.renderTopSmoothLighting(packedLightmapCoords, packedLightmapCoordsSouth, packedLightmapCoordsSouthEast, packedLightmapCoordsEast, blockAccess, blockStateIn, blockPosIn, bufferBuilderIn, blockliquid, redFloat, greenFloat, blueFloat, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, posX, posY, posZ, u0, u1, u2, u3, v0, v1, v2, v3);
		} else {
			fluidRenderer.renderTopFlatLighting(packedLightmapCoords, blockAccess, blockPosIn, bufferBuilderIn, blockliquid, redFloat, greenFloat, blueFloat, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, posX, posY, posZ, u0, u1, u2, u3, v0, v1, v2, v3);
		}
	}

}
