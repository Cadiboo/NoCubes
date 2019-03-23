package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.UVHelper;
import io.github.cadiboo.nocubes.client.optifine.OptifineCompatibility;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;

import static io.github.cadiboo.nocubes.config.FluidConfig.areNaturalFluidTexturesEnabled;
import static io.github.cadiboo.nocubes.config.FluidConfig.areSmoothFluidBiomeColorTransitionsEnabled;
import static io.github.cadiboo.nocubes.config.FluidConfig.isSmoothFluidLightingEnabled;
import static io.github.cadiboo.nocubes.util.ModUtil.TERRAIN_SMOOTHABLE;

/**
 * @author Cadiboo
 */
public class SmoothLightingBlockFluidRenderer extends BlockFluidRenderer {

	@Nonnull
	private final BlockFluidRenderer fluidRenderer;

	public SmoothLightingBlockFluidRenderer(@Nonnull final BlockFluidRenderer fluidRenderer) {
		super(fluidRenderer.blockColors);
		this.fluidRenderer = fluidRenderer;
	}

	@Override
	public boolean renderFluid(@Nonnull final IBlockAccess blockAccess, final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final BufferBuilder bufferBuilderIn) {
//		if (true) return super.renderFluid(blockAccess, blockStateIn, blockPosIn, bufferBuilderIn);

		OptifineCompatibility.pushShaderThing(blockStateIn, blockPosIn, blockAccess, bufferBuilderIn);
		try {
			BlockLiquid blockliquid = (BlockLiquid) blockStateIn.getBlock();
			boolean isLava = blockStateIn.getMaterial() == Material.LAVA;
			TextureAtlasSprite[] atextureatlassprite = isLava ? this.atlasSpritesLava : this.atlasSpritesWater;
			int color = this.blockColors.colorMultiplier(blockStateIn, blockAccess, blockPosIn, 0);
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
					blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.NORTH),
					blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.SOUTH),
					blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.WEST),
					blockStateIn.shouldSideBeRendered(blockAccess, blockPosIn, EnumFacing.EAST)
			};
			if (NoCubes.isEnabled()) {
				shouldHorizontalSideBeRendered[0] &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(blockPosIn.offset(EnumFacing.NORTH)));
				shouldHorizontalSideBeRendered[1] &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(blockPosIn.offset(EnumFacing.SOUTH)));
				shouldHorizontalSideBeRendered[2] &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(blockPosIn.offset(EnumFacing.WEST)));
				shouldHorizontalSideBeRendered[3] &= !TERRAIN_SMOOTHABLE.isSmoothable(blockAccess.getBlockState(blockPosIn.offset(EnumFacing.EAST)));
			}

			if (!shouldTopBeRendered && !shouldBottomBeRendered && !shouldHorizontalSideBeRendered[0] && !shouldHorizontalSideBeRendered[1] && !shouldHorizontalSideBeRendered[2] && !shouldHorizontalSideBeRendered[3]) {
				return false;
			} else {
				boolean wasAnythingRendered = false;
//		    	float f3 = 0.5F;
//		    	float f4 = 1.0F;
//		    	float f5 = 0.8F;
//		    	float f6 = 0.6F;
				Material material = blockStateIn.getMaterial();
				float fluidHeight = this.getFluidHeight(blockAccess, blockPosIn, material);
				float fluidHeightS = this.getFluidHeight(blockAccess, blockPosIn.south(), material);
				float fluidHeightES = this.getFluidHeight(blockAccess, blockPosIn.east().south(), material);
				float fluidHeightE = this.getFluidHeight(blockAccess, blockPosIn.east(), material);
				double posX = (double) blockPosIn.getX();
				double posY = (double) blockPosIn.getY();
				double posZ = (double) blockPosIn.getZ();
//		    	float f11 = 0.001F;

				if (shouldTopBeRendered) {
					wasAnythingRendered = true;
					float slopeAngle = BlockLiquid.getSlopeAngle(blockAccess, blockPosIn, material, blockStateIn);
					TextureAtlasSprite textureatlassprite = slopeAngle > -999.0F ? atextureatlassprite[1] : atextureatlassprite[0];
					fluidHeight -= 0.001F;
					fluidHeightS -= 0.001F;
					fluidHeightES -= 0.001F;
					fluidHeightE -= 0.001F;
					renderTop(blockAccess, blockStateIn, blockPosIn, bufferBuilderIn, blockliquid, isLava, redFloat, greenFloat, blueFloat, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, posX, posY, posZ, slopeAngle, textureatlassprite);
				}

				if (shouldBottomBeRendered) {
					wasAnythingRendered = true;
					renderBottom(blockAccess, blockStateIn, blockPosIn, bufferBuilderIn, atextureatlassprite, posX, posY, posZ, isLava);
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
							textureatlassprite1 = this.atlasSpriteWaterOverlay;
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

						final int skylightX0Z0;
						final int blocklightX0Z0;
						final int skylightX0Y0Z0;
						final int blocklightX0Y0Z0;
						final int skylightX1Z1;
						final int blocklightX1Z1;
						final int skylightX1Y0Z1;
						final int blocklightX1Y0Z1;

						if (!isLava && isSmoothFluidLightingEnabled()) {
							final int packedLightX0Z0 = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos.add(posX0 - posX, yAdd0, posZ0 - posZ));
							final int packedLightX0Y0Z0 = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos.add(posX0 - posX, 0, posZ0 - posZ));
							final int packedLightX1Z1 = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos.add(posX1 - posX, yAdd0, posZ1 - posZ));
							final int packedLightX1Y0Z1 = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos.add(posX1 - posX, 0, posZ1 - posZ));

							skylightX0Z0 = packedLightX0Z0 >> 16 & 65535;
							blocklightX0Z0 = packedLightX0Z0 & 65535;
							skylightX0Y0Z0 = packedLightX0Y0Z0 >> 16 & 65535;
							blocklightX0Y0Z0 = packedLightX0Y0Z0 & 65535;
							skylightX1Z1 = packedLightX1Z1 >> 16 & 65535;
							blocklightX1Z1 = packedLightX1Z1 & 65535;
							skylightX1Y0Z1 = packedLightX1Y0Z1 >> 16 & 65535;
							blocklightX1Y0Z1 = packedLightX1Y0Z1 & 65535;
						} else {
							int packedLight = blockStateIn.getPackedLightmapCoords(blockAccess, blockpos);
							int skylight = packedLight >> 16 & 65535;
							int blocklight = packedLight & 65535;

							skylightX0Z0 = skylight;
							blocklightX0Z0 = blocklight;
							skylightX0Y0Z0 = skylight;
							blocklightX0Y0Z0 = blocklight;
							skylightX1Z1 = skylight;
							blocklightX1Z1 = blocklight;
							skylightX1Y0Z1 = skylight;
							blocklightX1Y0Z1 = blocklight;
						}

						float diffuseLighting = horiontalIndex < 2 ? 0.8F : 0.6F;
						float red = 1.0F * diffuseLighting * redFloat;
						float green = 1.0F * diffuseLighting * greenFloat;
						float blue = 1.0F * diffuseLighting * blueFloat;
						bufferBuilderIn.pos(posX0, posY + (double) yAdd1, posZ0).color(red, green, blue, 1.0F).tex((double) u0, (double) v0).lightmap(skylightX0Z0, blocklightX0Z0).endVertex();
						bufferBuilderIn.pos(posX1, posY + (double) yAdd0, posZ1).color(red, green, blue, 1.0F).tex((double) u1, (double) v1).lightmap(skylightX1Z1, blocklightX1Z1).endVertex();
						bufferBuilderIn.pos(posX1, posY + 0.0D, posZ1).color(red, green, blue, 1.0F).tex((double) u1, (double) v2).lightmap(skylightX1Y0Z1, blocklightX1Y0Z1).endVertex();
						bufferBuilderIn.pos(posX0, posY + 0.0D, posZ0).color(red, green, blue, 1.0F).tex((double) u0, (double) v2).lightmap(skylightX0Y0Z0, blocklightX0Y0Z0).endVertex();

						if (textureatlassprite1 != this.atlasSpriteWaterOverlay) {
							bufferBuilderIn.pos(posX0, posY + 0.0D, posZ0).color(red, green, blue, 1.0F).tex((double) u0, (double) v2).lightmap(skylightX0Y0Z0, blocklightX0Y0Z0).endVertex();
							bufferBuilderIn.pos(posX1, posY + 0.0D, posZ1).color(red, green, blue, 1.0F).tex((double) u1, (double) v2).lightmap(skylightX1Y0Z1, blocklightX1Y0Z1).endVertex();
							bufferBuilderIn.pos(posX1, posY + (double) yAdd0, posZ1).color(red, green, blue, 1.0F).tex((double) u1, (double) v1).lightmap(skylightX1Z1, blocklightX1Z1).endVertex();
							bufferBuilderIn.pos(posX0, posY + (double) yAdd1, posZ0).color(red, green, blue, 1.0F).tex((double) u0, (double) v0).lightmap(skylightX0Z0, blocklightX0Z0).endVertex();
						}
					}
				}

				return wasAnythingRendered;
			}
		} finally {
			OptifineCompatibility.popShaderThing(bufferBuilderIn);
		}
	}

	public void renderBottom(@Nonnull final IBlockAccess blockAccess, final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final BufferBuilder bufferBuilderIn, final TextureAtlasSprite[] atextureatlassprite, final double posX, final double posY, final double posZ, final boolean isLava) {
		if (isLava) {
			final int packedLight = blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down()) | 240;
			renderBottomFlatLighting(packedLight, bufferBuilderIn, atextureatlassprite[0], posX, posY, posZ);
		} else {
			if (isSmoothFluidLightingEnabled()) {
				renderBottomSmoothLighting(blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down()), blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down().south()), blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down().south().east()), blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down().east()), bufferBuilderIn, atextureatlassprite[0], posX, posY, posZ);
			} else {
				renderBottomFlatLighting(blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.down()), bufferBuilderIn, atextureatlassprite[0], posX, posY, posZ);
			}
		}
	}

	public void renderTop(@Nonnull final IBlockAccess blockAccess, final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final BufferBuilder bufferBuilderIn, final BlockLiquid blockliquid, final boolean isLava, final float redFloat, final float greenFloat, final float blueFloat, final float fluidHeight, final float fluidHeightS, final float fluidHeightES, final float fluidHeightE, final double posX, final double posY, final double posZ, final float slopeAngle, final TextureAtlasSprite textureatlassprite) {
		float u0;
		float u1;
		float u2;
		float u3;
		float v0;
		float v1;
		float v2;
		float v3;

		if (slopeAngle < -999.0F) {
			if (areNaturalFluidTexturesEnabled()) {
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
			} else {
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
			}
		} else {
			float quarterSinSlopeAngle = MathHelper.sin(slopeAngle) * 0.25F;
			float quarterCosSlopeAngle = MathHelper.cos(slopeAngle) * 0.25F;
//					float f23 = 8.0F;
			u0 = textureatlassprite.getInterpolatedU((double) (8.0F + (-quarterCosSlopeAngle - quarterSinSlopeAngle) * 16.0F));
			v0 = textureatlassprite.getInterpolatedV((double) (8.0F + (-quarterCosSlopeAngle + quarterSinSlopeAngle) * 16.0F));
			u1 = textureatlassprite.getInterpolatedU((double) (8.0F + (-quarterCosSlopeAngle + quarterSinSlopeAngle) * 16.0F));
			v1 = textureatlassprite.getInterpolatedV((double) (8.0F + (quarterCosSlopeAngle + quarterSinSlopeAngle) * 16.0F));
			u2 = textureatlassprite.getInterpolatedU((double) (8.0F + (quarterCosSlopeAngle + quarterSinSlopeAngle) * 16.0F));
			v2 = textureatlassprite.getInterpolatedV((double) (8.0F + (quarterCosSlopeAngle - quarterSinSlopeAngle) * 16.0F));
			u3 = textureatlassprite.getInterpolatedU((double) (8.0F + (quarterCosSlopeAngle - quarterSinSlopeAngle) * 16.0F));
			v3 = textureatlassprite.getInterpolatedV((double) (8.0F + (-quarterCosSlopeAngle - quarterSinSlopeAngle) * 16.0F));
		}

		if (!isLava && isSmoothFluidLightingEnabled()) {
			renderTopSmoothLighting(blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn), blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.south()), blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.south().east()), blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn.east()), blockAccess, blockStateIn, blockPosIn, bufferBuilderIn, blockliquid, redFloat, greenFloat, blueFloat, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, posX, posY, posZ, u0, u1, u2, u3, v0, v1, v2, v3);
		} else {
			renderTopFlatLighting(blockStateIn.getPackedLightmapCoords(blockAccess, blockPosIn), blockAccess, blockPosIn, bufferBuilderIn, blockliquid, redFloat, greenFloat, blueFloat, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, posX, posY, posZ, u0, u1, u2, u3, v0, v1, v2, v3);
		}
	}

	public void renderTopFlatLighting(final int packedLight, @Nonnull final IBlockAccess blockAccess, @Nonnull final BlockPos blockPosIn, @Nonnull final BufferBuilder bufferBuilderIn, final BlockLiquid blockliquid, final float redFloat, final float greenFloat, final float blueFloat, final double fluidHeight, final double fluidHeightS, final double fluidHeightES, final double fluidHeightE, final double posX, final double posY, final double posZ, final double u0, final double u1, final double u2, final double u3, final double v0, final double v1, final double v2, final double v3) {
		int skyLight = packedLight >> 16 & 65535;
		int blockLight = packedLight & 65535;
		float red = 1.0F * redFloat;
		float green = 1.0F * greenFloat;
		float blue = 1.0F * blueFloat;
		bufferBuilderIn.pos(posX + 0.0D, posY + fluidHeight, posZ + 0.0D).color(red, green, blue, 1.0F).tex(u0, v0).lightmap(skyLight, blockLight).endVertex();
		bufferBuilderIn.pos(posX + 0.0D, posY + fluidHeightS, posZ + 1.0D).color(red, green, blue, 1.0F).tex(u1, v1).lightmap(skyLight, blockLight).endVertex();
		bufferBuilderIn.pos(posX + 1.0D, posY + fluidHeightES, posZ + 1.0D).color(red, green, blue, 1.0F).tex(u2, v2).lightmap(skyLight, blockLight).endVertex();
		bufferBuilderIn.pos(posX + 1.0D, posY + fluidHeightE, posZ + 0.0D).color(red, green, blue, 1.0F).tex(u3, v3).lightmap(skyLight, blockLight).endVertex();

		if (blockliquid.shouldRenderSides(blockAccess, blockPosIn.up())) {
			bufferBuilderIn.pos(posX + 0.0D, posY + fluidHeight, posZ + 0.0D).color(red, green, blue, 1.0F).tex(u0, v0).lightmap(skyLight, blockLight).endVertex();
			bufferBuilderIn.pos(posX + 1.0D, posY + fluidHeightE, posZ + 0.0D).color(red, green, blue, 1.0F).tex(u3, v3).lightmap(skyLight, blockLight).endVertex();
			bufferBuilderIn.pos(posX + 1.0D, posY + fluidHeightES, posZ + 1.0D).color(red, green, blue, 1.0F).tex(u2, v2).lightmap(skyLight, blockLight).endVertex();
			bufferBuilderIn.pos(posX + 0.0D, posY + fluidHeightS, posZ + 1.0D).color(red, green, blue, 1.0F).tex(u1, v1).lightmap(skyLight, blockLight).endVertex();
		}
	}

	public void renderTopSmoothLighting(final int packedLight, final int packedLightSouth, final int packedLightSouthEast, final int packedLightEast, @Nonnull final IBlockAccess blockAccess, final IBlockState blockStateIn, @Nonnull final BlockPos blockPosIn, @Nonnull final BufferBuilder bufferBuilderIn, final BlockLiquid blockliquid, final float redFloat, final float greenFloat, final float blueFloat, final double fluidHeight, final double fluidHeightS, final double fluidHeightES, final double fluidHeightE, final double posX, final double posY, final double posZ, final double u0, final double u1, final double u2, final double u3, final double v0, final double v1, final double v2, final double v3) {
		final int skyLight = packedLight >> 16 & 65535;
		final int blockLight = packedLight & 65535;
		int skyLightSouth = packedLightSouth >> 16 & 65535;
		int blockLightSouth = packedLightSouth & 65535;
		int skyLightSouthEast = packedLightSouthEast >> 16 & 65535;
		int blockLightSouthEast = packedLightSouthEast & 65535;
		int skyLightEast = packedLightEast >> 16 & 65535;
		int blockLightEast = packedLightEast & 65535;

		if (skyLightSouth == 0) skyLightSouth = skyLight;
		if (blockLightSouth == 0) blockLightSouth = blockLight;
		if (skyLightSouthEast == 0) skyLightSouthEast = skyLight;
		if (blockLightSouthEast == 0) blockLightSouthEast = blockLight;
		if (skyLightEast == 0) skyLightEast = skyLight;
		if (blockLightEast == 0) blockLightEast = blockLight;

		final boolean smoothBiomeColors = true;

//		float red = 1.0F * redFloat;
//		float green = 1.0F * greenFloat;
//		float blue = 1.0F * blueFloat;

		final float red0 = redFloat;
		final float green0 = greenFloat;
		final float blue0 = blueFloat;
		final float red1;
		final float green1;
		final float blue1;
		final float red2;
		final float green2;
		final float blue2;
		final float red3;
		final float green3;
		final float blue3;
		if (areSmoothFluidBiomeColorTransitionsEnabled()) {
			final int color1 = this.blockColors.colorMultiplier(blockStateIn, blockAccess, blockPosIn.south(), 0);
			red1 = (float) (color1 >> 16 & 255) / 255.0F;
			green1 = (float) (color1 >> 8 & 255) / 255.0F;
			blue1 = (float) (color1 & 255) / 255.0F;
			final int color2 = this.blockColors.colorMultiplier(blockStateIn, blockAccess, blockPosIn.south().east(), 0);
			red2 = (float) (color2 >> 16 & 255) / 255.0F;
			green2 = (float) (color2 >> 8 & 255) / 255.0F;
			blue2 = (float) (color2 & 255) / 255.0F;
			final int color3 = this.blockColors.colorMultiplier(blockStateIn, blockAccess, blockPosIn.east(), 0);
			red3 = (float) (color3 >> 16 & 255) / 255.0F;
			green3 = (float) (color3 >> 8 & 255) / 255.0F;
			blue3 = (float) (color3 & 255) / 255.0F;
		} else {
			red1 = redFloat;
			green1 = greenFloat;
			blue1 = blueFloat;
			red2 = redFloat;
			green2 = greenFloat;
			blue2 = blueFloat;
			red3 = redFloat;
			green3 = greenFloat;
			blue3 = blueFloat;
		}

		bufferBuilderIn.pos(posX + 0.0D, posY + fluidHeight, posZ + 0.0D).color(red0, green0, blue0, 1.0F).tex(u0, v0).lightmap(skyLight, blockLight).endVertex();
		bufferBuilderIn.pos(posX + 0.0D, posY + fluidHeightS, posZ + 1.0D).color(red1, green1, blue1, 1.0F).tex(u1, v1).lightmap(skyLightSouth, blockLightSouth).endVertex();
		bufferBuilderIn.pos(posX + 1.0D, posY + fluidHeightES, posZ + 1.0D).color(red2, green2, blue2, 1.0F).tex(u2, v2).lightmap(skyLightSouthEast, blockLightSouthEast).endVertex();
		bufferBuilderIn.pos(posX + 1.0D, posY + fluidHeightE, posZ + 0.0D).color(red3, green3, blue3, 1.0F).tex(u3, v3).lightmap(skyLightEast, blockLightEast).endVertex();

		// Render bottom of top
		if (blockliquid.shouldRenderSides(blockAccess, blockPosIn.up())) {
			bufferBuilderIn.pos(posX + 0.0D, posY + fluidHeight, posZ + 0.0D).color(red0, green0, blue0, 1.0F).tex(u0, v0).lightmap(skyLight, blockLight).endVertex();
			bufferBuilderIn.pos(posX + 1.0D, posY + fluidHeightE, posZ + 0.0D).color(red1, green1, blue1, 1.0F).tex(u3, v3).lightmap(skyLightEast, blockLightEast).endVertex();
			bufferBuilderIn.pos(posX + 1.0D, posY + fluidHeightES, posZ + 1.0D).color(red2, green2, blue2, 1.0F).tex(u2, v2).lightmap(skyLightSouthEast, blockLightSouthEast).endVertex();
			bufferBuilderIn.pos(posX + 0.0D, posY + fluidHeightS, posZ + 1.0D).color(red3, green3, blue3, 1.0F).tex(u1, v1).lightmap(skyLightSouth, blockLightSouth).endVertex();
		}
	}

	public void renderBottomFlatLighting(final int packedLightDown, final BufferBuilder bufferBuilderIn, final TextureAtlasSprite textureAtlasSprite, final double posX, final double posY, final double posZ) {
//		float minU = textureAtlasSprite.getMinU();
//		float maxU = textureAtlasSprite.getMaxU();
//		float minV = textureAtlasSprite.getMinV();
//		float maxV = textureAtlasSprite.getMaxV();
		final float minU = UVHelper.getMinU(textureAtlasSprite);
		final float maxU = UVHelper.getMaxU(textureAtlasSprite);
		final float minV = UVHelper.getMinV(textureAtlasSprite);
		final float maxV = UVHelper.getMaxV(textureAtlasSprite);

		int skylightDown = packedLightDown >> 16 & 65535;
		int blocklightDown = packedLightDown & 65535;

		bufferBuilderIn.pos(posX, posY, posZ + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) minU, (double) maxV).lightmap(skylightDown, blocklightDown).endVertex();
		bufferBuilderIn.pos(posX, posY, posZ).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) minU, (double) minV).lightmap(skylightDown, blocklightDown).endVertex();
		bufferBuilderIn.pos(posX + 1.0D, posY, posZ).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) maxU, (double) minV).lightmap(skylightDown, blocklightDown).endVertex();
		bufferBuilderIn.pos(posX + 1.0D, posY, posZ + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) maxU, (double) maxV).lightmap(skylightDown, blocklightDown).endVertex();
	}

	public void renderBottomSmoothLighting(final int packedLightDown, final int packedLightDownSouth, final int packedLightDownSouthEast, final int packedLightDownEast, final BufferBuilder bufferBuilderIn, final TextureAtlasSprite textureAtlasSprite, final double posX, final double posY, final double posZ) {
//		float minU = textureAtlasSprite.getMinU();
//		float maxU = textureAtlasSprite.getMaxU();
//		float minV = textureAtlasSprite.getMinV();
//		float maxV = textureAtlasSprite.getMaxV();
		final float minU = UVHelper.getMinU(textureAtlasSprite);
		final float maxU = UVHelper.getMaxU(textureAtlasSprite);
		final float minV = UVHelper.getMinV(textureAtlasSprite);
		final float maxV = UVHelper.getMaxV(textureAtlasSprite);

		int skylightDown = packedLightDown >> 16 & 65535;
		int blocklightDown = packedLightDown & 65535;
		int skylightDownSouth = packedLightDownSouth >> 16 & 65535;
		int blocklightDownSouth = packedLightDownSouth & 65535;
		int skylightDownSouthEast = packedLightDownSouthEast >> 16 & 65535;
		int blocklightDownSouthEast = packedLightDownSouthEast & 65535;
		int skylightDownEast = packedLightDownEast >> 16 & 65535;
		int blocklightDownEast = packedLightDownEast & 65535;

		bufferBuilderIn.pos(posX, posY, posZ + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) minU, (double) maxV).lightmap(skylightDown, blocklightDown).endVertex();
		bufferBuilderIn.pos(posX, posY, posZ).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) minU, (double) minV).lightmap(skylightDownSouth, blocklightDownSouth).endVertex();
		bufferBuilderIn.pos(posX + 1.0D, posY, posZ).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) maxU, (double) minV).lightmap(skylightDownSouthEast, blocklightDownSouthEast).endVertex();
		bufferBuilderIn.pos(posX + 1.0D, posY, posZ + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double) maxU, (double) maxV).lightmap(skylightDownEast, blocklightDownEast).endVertex();
	}

	@Nonnull
	public BlockFluidRenderer getOldFluidRenderer() {
		return fluidRenderer;
	}

	@Override
	public float getFluidHeight(final IBlockAccess blockAccess, final BlockPos blockPosIn, final Material blockMaterial) {
		return fluidRenderer.getFluidHeight(blockAccess, blockPosIn, blockMaterial);
	}

}
