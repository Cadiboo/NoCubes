package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.UVHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.biome.BiomeColors;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class SmoothLightingBlockFluidRenderer extends BlockFluidRenderer {

	@Nonnull
	private final BlockFluidRenderer fluidRenderer;

	public SmoothLightingBlockFluidRenderer(@Nonnull final BlockFluidRenderer fluidRenderer) {
		super();
		this.fluidRenderer = fluidRenderer;
	}

	@Override
	public boolean render(final IWorldReader worldIn, final BlockPos pos, final BufferBuilder buffer, final IFluidState state) {
//		OptiFineCompatibility.pushShaderThing(state, pos, worldIn, buffer);
		try {
			boolean isLava = state.isTagged(FluidTags.LAVA);

			TextureAtlasSprite[] atextureatlassprite = isLava ? this.atlasSpritesLava : this.atlasSpritesWater;

			boolean renderUp = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.UP, state);
			boolean renderDown = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.DOWN, state) && !func_209556_a(worldIn, pos, EnumFacing.DOWN, 0.8888889F);
			boolean renderNorth = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.NORTH, state);
			boolean renderSouth = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.SOUTH, state);
			boolean renderWest = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.WEST, state);
			boolean renderEast = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.EAST, state);

			if (!renderUp && !renderDown && !renderEast && !renderWest && !renderNorth && !renderSouth) {
				return false;
			}

			boolean wasAnythingRendered = false;

			float fluidHeight = this.getFluidHeight(worldIn, pos, state.getFluid());
			float fluidHeightS = this.getFluidHeight(worldIn, pos.south(), state.getFluid());
			float fluidHeightES = this.getFluidHeight(worldIn, pos.east().south(), state.getFluid());
			float fluidHeightE = this.getFluidHeight(worldIn, pos.east(), state.getFluid());

			double x = pos.getX();
			double y = pos.getY();
			double z = pos.getZ();

			int waterColor = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos);
			float red = ((waterColor >> 16) & 255) / 255.0F;
			float green = ((waterColor >> 8) & 255) / 255.0F;
			float blue = (waterColor & 255) / 255.0F;

			if (renderUp && !func_209556_a(worldIn, pos, EnumFacing.UP, Math.min(Math.min(fluidHeight, fluidHeightS), Math.min(fluidHeightES, fluidHeightE)))) {
				wasAnythingRendered = true;
				fluidHeight -= 0.001F;
				fluidHeightS -= 0.001F;
				fluidHeightES -= 0.001F;
				fluidHeightE -= 0.001F;
				renderUp(worldIn, pos, buffer, state, atextureatlassprite, red, green, blue, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, x, y, z, state.getFlow(worldIn, pos), isLava);
			}

			if (renderDown) {
				renderDown(buffer, atextureatlassprite[0], red, green, blue, x, y, z, worldIn, pos, isLava);
				wasAnythingRendered = true;
			}

			for (int facingIndex = 0; facingIndex < 4; ++facingIndex) {
				float yAdd0;
				float yadd1;
				double d3;
				double z0;
				double d5;
				double z1;
				EnumFacing enumfacing;
				boolean renderSide;
				if (facingIndex == 0) {
					yAdd0 = fluidHeight;
					yadd1 = fluidHeightE;
					d3 = x;
					d5 = x + 1.0D;
					z0 = z + (double) 0.001F;
					z1 = z + (double) 0.001F;
					enumfacing = EnumFacing.NORTH;
					renderSide = renderNorth;
				} else if (facingIndex == 1) {
					yAdd0 = fluidHeightES;
					yadd1 = fluidHeightS;
					d3 = x + 1.0D;
					d5 = x;
					z0 = z + 1.0D - (double) 0.001F;
					z1 = z + 1.0D - (double) 0.001F;
					enumfacing = EnumFacing.SOUTH;
					renderSide = renderSouth;
				} else if (facingIndex == 2) {
					yAdd0 = fluidHeightS;
					yadd1 = fluidHeight;
					d3 = x + (double) 0.001F;
					d5 = x + (double) 0.001F;
					z0 = z + 1.0D;
					z1 = z;
					enumfacing = EnumFacing.WEST;
					renderSide = renderWest;
				} else {
					yAdd0 = fluidHeightE;
					yadd1 = fluidHeightES;
					d3 = x + 1.0D - (double) 0.001F;
					d5 = x + 1.0D - (double) 0.001F;
					z0 = z;
					z1 = z + 1.0D;
					enumfacing = EnumFacing.EAST;
					renderSide = renderEast;
				}

				if (renderSide && !func_209556_a(worldIn, pos, enumfacing, Math.max(yAdd0, yadd1))) {
					wasAnythingRendered = true;
					BlockPos blockpos = pos.offset(enumfacing);
					TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
					if (!isLava) {
						IBlockState blockstate = worldIn.getBlockState(blockpos);
						if (blockstate.getBlockFaceShape(worldIn, blockpos, enumfacing) == net.minecraft.block.state.BlockFaceShape.SOLID) {
							textureatlassprite2 = this.atlasSpriteWaterOverlay;
						}
					}

					renderSide(worldIn, buffer, red, green, blue, y, facingIndex, yAdd0, yadd1, d3, z0, d5, z1, blockpos, textureatlassprite2, pos, isLava);
				}
			}

			return wasAnythingRendered;
		} finally {
//			OptiFineCompatibility.popShaderThing(buffer);
		}
	}

	@Override
	public int getCombinedLightUpMax(IWorldReader reader, BlockPos pos) {
		int i = reader.getCombinedLight(pos, 0);
		int j = reader.getCombinedLight(pos.up(), 0);
		int k = i & 255;
		int l = j & 255;
		int i1 = i >> 16 & 255;
		int j1 = j >> 16 & 255;
		return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
	}

	@Override
	public float getFluidHeight(IWorldReaderBase reader, BlockPos pos, Fluid fluidIn) {
		int i = 0;
		float f = 0.0F;

		for (int j = 0; j < 4; ++j) {
			BlockPos blockpos = pos.add(-(j & 1), 0, -(j >> 1 & 1));
			if (reader.getFluidState(blockpos.up()).getFluid().isEquivalentTo(fluidIn)) {
				return 1.0F;
			}

			IFluidState ifluidstate = reader.getFluidState(blockpos);
			if (ifluidstate.getFluid().isEquivalentTo(fluidIn)) {
				if (ifluidstate.getHeight() >= 0.8F) {
					f += ifluidstate.getHeight() * 10.0F;
					i += 10;
				} else {
					f += ifluidstate.getHeight();
					++i;
				}
			} else if (!reader.getBlockState(blockpos).getMaterial().isSolid()) {
				++i;
			}
		}

		return f / (float) i;
	}

	public void renderSide(final IWorldReader worldIn, final BufferBuilder buffer, final float red, final float green, final float blue, final double y, final int facingIndex, final float yAdd_0, final float yadd_1, final double d3, final double z0, final double d5, final double z1, final BlockPos blockpos, final TextureAtlasSprite textureatlassprite2, final BlockPos pos, final boolean isLava) {
		if (smoothLighting()) {
			renderSideSmooth(worldIn, buffer, red, green, blue, y, facingIndex, yAdd_0, yadd_1, d3, z0, d5, z1, blockpos, textureatlassprite2, pos, isLava);
		} else {
			renderSideFlat(worldIn, buffer, red, green, blue, y, facingIndex, yAdd_0, yadd_1, d3, z0, d5, z1, blockpos, textureatlassprite2, pos, isLava);
		}
	}

	public void renderDown(final BufferBuilder buffer, final TextureAtlasSprite textureatlassprite0, final float red, final float green, final float blue, final double x, final double y, final double z, final IWorldReader worldIn, final BlockPos pos, final boolean isLava) {
		if (smoothLighting()) {
			renderDownSmooth(buffer, textureatlassprite0, red, green, blue, x, y, z, worldIn, pos, isLava);
		} else {
			renderDownFlat(buffer, textureatlassprite0, red, green, blue, x, y, z, worldIn, pos, isLava);
		}
	}

	public void renderUp(final IWorldReader worldIn, final BlockPos pos, final BufferBuilder buffer, final IFluidState state, final TextureAtlasSprite[] atextureatlassprite, final float red, final float green, final float blue, final double fluidHeight, final double fluidHeightS, final double fluidHeightES, final double fluidHeightE, final double x, final double y, final double z, final Vec3d flow, final boolean isLava) {
		if (smoothLighting()) {
			renderUpSmooth(worldIn, pos, buffer, state, atextureatlassprite, red, green, blue, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, x, y, z, flow, isLava);
		} else {
			renderUpFlat(worldIn, pos, buffer, state, atextureatlassprite, red, green, blue, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, x, y, z, flow, isLava);
		}
	}

	public void renderSideFlat(final IWorldReader worldIn, final BufferBuilder buffer, final float red, final float green, final float blue, final double y, final int facingIndex, final float yAdd_0, final float yadd_1, final double d3, final double z0, final double d5, final double z1, final BlockPos blockpos, final TextureAtlasSprite textureatlassprite2, final BlockPos pos, final boolean isLava) {
		float minU = textureatlassprite2.getInterpolatedU(0.0D);
		float halfU = textureatlassprite2.getInterpolatedU(8.0D);
		float v0 = textureatlassprite2.getInterpolatedV((double) ((1.0F - yAdd_0) * 16.0F * 0.5F));
		float v1 = textureatlassprite2.getInterpolatedV((double) ((1.0F - yadd_1) * 16.0F * 0.5F));
		float halfV = textureatlassprite2.getInterpolatedV(8.0D);
		int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, blockpos);
		int skyLight = combinedLightUpMax >> 16 & '\uffff';
		int blockLight = combinedLightUpMax & '\uffff';
		float diffuse = facingIndex < 2 ? 0.8F : 0.6F;
		if (!colors()) {
			float r = 1.0F * diffuse * red;
			float g = 1.0F * diffuse * green;
			float b = 1.0F * diffuse * blue;
			buffer.pos(d3, y + (double) yAdd_0, z0).color(r, g, b, 1.0F).tex((double) minU, (double) v0).lightmap(skyLight, blockLight).endVertex();
			buffer.pos(d5, y + (double) yadd_1, z1).color(r, g, b, 1.0F).tex((double) halfU, (double) v1).lightmap(skyLight, blockLight).endVertex();
			buffer.pos(d5, y + 0.0D, z1).color(r, g, b, 1.0F).tex((double) halfU, (double) halfV).lightmap(skyLight, blockLight).endVertex();
			buffer.pos(d3, y + 0.0D, z0).color(r, g, b, 1.0F).tex((double) minU, (double) halfV).lightmap(skyLight, blockLight).endVertex();
			if (textureatlassprite2 != this.atlasSpriteWaterOverlay) {
				buffer.pos(d3, y + 0.0D, z0).color(r, g, b, 1.0F).tex((double) minU, (double) halfV).lightmap(skyLight, blockLight).endVertex();
				buffer.pos(d5, y + 0.0D, z1).color(r, g, b, 1.0F).tex((double) halfU, (double) halfV).lightmap(skyLight, blockLight).endVertex();
				buffer.pos(d5, y + (double) yadd_1, z1).color(r, g, b, 1.0F).tex((double) halfU, (double) v1).lightmap(skyLight, blockLight).endVertex();
				buffer.pos(d3, y + (double) yAdd_0, z0).color(r, g, b, 1.0F).tex((double) minU, (double) v0).lightmap(skyLight, blockLight).endVertex();

			}
		} else {
			float r0 = diffuse * red;
			float g0 = diffuse * green;
			float b0 = diffuse * blue;
			final int colorS = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south());
			float r1 = diffuse * ((colorS >> 16) & 255) / 255.0F;
			float g1 = diffuse * ((colorS >> 8) & 255) / 255.0F;
			float b1 = diffuse * ((colorS) & 255) / 255.0F;
			final int colorSE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south().east());
			float r2 = diffuse * ((colorSE >> 16) & 255) / 255.0F;
			float g2 = diffuse * ((colorSE >> 8) & 255) / 255.0F;
			float b2 = diffuse * ((colorSE) & 255) / 255.0F;
			final int colorE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.east());
			float r3 = diffuse * ((colorE >> 16) & 255) / 255.0F;
			float g3 = diffuse * ((colorE >> 8) & 255) / 255.0F;
			float b3 = diffuse * ((colorE) & 255) / 255.0F;
			buffer.pos(d3, y + (double) yAdd_0, z0).color(r0, g0, b0, 1.0F).tex((double) minU, (double) v0).lightmap(skyLight, blockLight).endVertex();
			buffer.pos(d5, y + (double) yadd_1, z1).color(r1, g1, b2, 1.0F).tex((double) halfU, (double) v1).lightmap(skyLight, blockLight).endVertex();
			buffer.pos(d5, y + 0.0D, z1).color(r2, g2, b2, 1.0F).tex((double) halfU, (double) halfV).lightmap(skyLight, blockLight).endVertex();
			buffer.pos(d3, y + 0.0D, z0).color(r3, g3, b3, 1.0F).tex((double) minU, (double) halfV).lightmap(skyLight, blockLight).endVertex();
			if (textureatlassprite2 != this.atlasSpriteWaterOverlay) {
				buffer.pos(d3, y + 0.0D, z0).color(r3, g3, b3, 1.0F).tex((double) minU, (double) halfV).lightmap(skyLight, blockLight).endVertex();
				buffer.pos(d5, y + 0.0D, z1).color(r2, g2, b2, 1.0F).tex((double) halfU, (double) halfV).lightmap(skyLight, blockLight).endVertex();
				buffer.pos(d5, y + (double) yadd_1, z1).color(r1, g1, b1, 1.0F).tex((double) halfU, (double) v1).lightmap(skyLight, blockLight).endVertex();
				buffer.pos(d3, y + (double) yAdd_0, z0).color(r0, g0, b0, 1.0F).tex((double) minU, (double) v0).lightmap(skyLight, blockLight).endVertex();
			}
		}
	}

	public void renderSideSmooth(final IWorldReader worldIn, final BufferBuilder buffer, final float red, final float green, final float blue, final double y, final int facingIndex, final float yAdd_0, final float yadd_1, final double d3, final double z0, final double d5, final double z1, final BlockPos blockpos, final TextureAtlasSprite textureatlassprite2, final BlockPos pos, final boolean isLava) {
		float minU = textureatlassprite2.getInterpolatedU(0.0D);
		float halfU = textureatlassprite2.getInterpolatedU(8.0D);
		float v0 = textureatlassprite2.getInterpolatedV((double) ((1.0F - yAdd_0) * 16.0F * 0.5F));
		float v1 = textureatlassprite2.getInterpolatedV((double) ((1.0F - yadd_1) * 16.0F * 0.5F));
		float halfV = textureatlassprite2.getInterpolatedV(8.0D);

		final int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, pos);
		final int combinedLightUpMaxSouth = this.getCombinedLightUpMax(worldIn, pos.south());
		final int combinedLightUpMaxSouthEast = this.getCombinedLightUpMax(worldIn, pos.south().east());
		final int combinedLightUpMaxEast = this.getCombinedLightUpMax(worldIn, pos);

		int skylightUp = combinedLightUpMax >> 16 & '\uffff';
		int blocklightUp = combinedLightUpMax & '\uffff';
		int skylightUpSouth = combinedLightUpMaxSouth >> 16 & '\uffff';
		int blocklightUpSouth = combinedLightUpMaxSouth & '\uffff';
		int skylightUpSouthEast = combinedLightUpMaxSouthEast >> 16 & '\uffff';
		int blocklightUpSouthEast = combinedLightUpMaxSouthEast & '\uffff';
		int skylightUpEast = combinedLightUpMaxEast >> 16 & '\uffff';
		int blocklightUpEast = combinedLightUpMaxEast & '\uffff';

		float diffuse = facingIndex < 2 ? 0.8F : 0.6F;
		if (!colors()) {
			float r = 1.0F * diffuse * red;
			float g = 1.0F * diffuse * green;
			float b = 1.0F * diffuse * blue;
			buffer.pos(d3, y + (double) yAdd_0, z0).color(r, g, b, 1.0F).tex((double) minU, (double) v0).lightmap(skylightUp, blocklightUp).endVertex();
			buffer.pos(d5, y + (double) yadd_1, z1).color(r, g, b, 1.0F).tex((double) halfU, (double) v1).lightmap(skylightUpSouth, blocklightUpSouth).endVertex();
			buffer.pos(d5, y + 0.0D, z1).color(r, g, b, 1.0F).tex((double) halfU, (double) halfV).lightmap(skylightUpSouthEast, blocklightUpSouthEast).endVertex();
			buffer.pos(d3, y + 0.0D, z0).color(r, g, b, 1.0F).tex((double) minU, (double) halfV).lightmap(skylightUpEast, blocklightUpEast).endVertex();
			if (textureatlassprite2 != this.atlasSpriteWaterOverlay) {
				buffer.pos(d3, y + 0.0D, z0).color(r, g, b, 1.0F).tex((double) minU, (double) halfV).lightmap(skylightUpEast, blocklightUpEast).endVertex();
				buffer.pos(d5, y + 0.0D, z1).color(r, g, b, 1.0F).tex((double) halfU, (double) halfV).lightmap(skylightUpSouthEast, blocklightUpSouthEast).endVertex();
				buffer.pos(d5, y + (double) yadd_1, z1).color(r, g, b, 1.0F).tex((double) halfU, (double) v1).lightmap(skylightUpSouth, blocklightUpSouth).endVertex();
				buffer.pos(d3, y + (double) yAdd_0, z0).color(r, g, b, 1.0F).tex((double) minU, (double) v0).lightmap(skylightUp, blocklightUp).endVertex();

			}
		} else {
			float r0 = diffuse * red;
			float g0 = diffuse * green;
			float b0 = diffuse * blue;
			final int colorS = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south());
			float r1 = diffuse * ((colorS >> 16) & 255) / 255.0F;
			float g1 = diffuse * ((colorS >> 8) & 255) / 255.0F;
			float b1 = diffuse * ((colorS) & 255) / 255.0F;
			final int colorSE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south().east());
			float r2 = diffuse * ((colorSE >> 16) & 255) / 255.0F;
			float g2 = diffuse * ((colorSE >> 8) & 255) / 255.0F;
			float b2 = diffuse * ((colorSE) & 255) / 255.0F;
			final int colorE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.east());
			float r3 = diffuse * ((colorE >> 16) & 255) / 255.0F;
			float g3 = diffuse * ((colorE >> 8) & 255) / 255.0F;
			float b3 = diffuse * ((colorE) & 255) / 255.0F;
			buffer.pos(d3, y + (double) yAdd_0, z0).color(r0, g0, b0, 1.0F).tex((double) minU, (double) v0).lightmap(skylightUp, blocklightUp).endVertex();
			buffer.pos(d5, y + (double) yadd_1, z1).color(r1, g1, b2, 1.0F).tex((double) halfU, (double) v1).lightmap(skylightUpSouth, blocklightUpSouth).endVertex();
			buffer.pos(d5, y + 0.0D, z1).color(r2, g2, b2, 1.0F).tex((double) halfU, (double) halfV).lightmap(skylightUpSouthEast, blocklightUpSouthEast).endVertex();
			buffer.pos(d3, y + 0.0D, z0).color(r3, g3, b3, 1.0F).tex((double) minU, (double) halfV).lightmap(skylightUpEast, blocklightUpEast).endVertex();
			if (textureatlassprite2 != this.atlasSpriteWaterOverlay) {
				buffer.pos(d3, y + 0.0D, z0).color(r3, g3, b3, 1.0F).tex((double) minU, (double) halfV).lightmap(skylightUpEast, blocklightUpEast).endVertex();
				buffer.pos(d5, y + 0.0D, z1).color(r2, g2, b2, 1.0F).tex((double) halfU, (double) halfV).lightmap(skylightUpSouthEast, blocklightUpSouthEast).endVertex();
				buffer.pos(d5, y + (double) yadd_1, z1).color(r1, g1, b1, 1.0F).tex((double) halfU, (double) v1).lightmap(skylightUpSouth, blocklightUpSouth).endVertex();
				buffer.pos(d3, y + (double) yAdd_0, z0).color(r0, g0, b0, 1.0F).tex((double) minU, (double) v0).lightmap(skylightUp, blocklightUp).endVertex();
			}
		}
	}

	public void renderUpFlat(final IWorldReader worldIn, final BlockPos pos, final BufferBuilder bufferBuilder, final IFluidState state, final TextureAtlasSprite[] atextureatlassprite, final float red, final float green, final float blue, final double fluidHeight, final double fluidHeightS, final double fluidHeightES, final double fluidHeightE, final double x, final double y, final double z, final Vec3d flow, final boolean isLava) {
		float u0;
		float u1;
		float u2;
		float u3;
		float v0;
		float v1;
		float v2;
		float v3;
		if (flow.x == 0.0D && flow.z == 0.0D) {
			TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
			if (textures()) {
				final int rand = (int) (MathHelper.getPositionRandom(pos) % 7);
				switch (rand) {
					default:
					case 0:
						u0 = UVHelper.getMinU(textureatlassprite1);
						v0 = UVHelper.getMinV(textureatlassprite1);
						v1 = UVHelper.getMaxV(textureatlassprite1);
						u2 = UVHelper.getMaxU(textureatlassprite1);
						break;
					case 1:
					case 2:
						u0 = UVHelper.getMaxU(textureatlassprite1);
						v0 = UVHelper.getMaxV(textureatlassprite1);
						v1 = UVHelper.getMinV(textureatlassprite1);
						u2 = UVHelper.getMinU(textureatlassprite1);
						break;
					case 3:
					case 4:
						u0 = UVHelper.getMinU(textureatlassprite1);
						v0 = UVHelper.getMinV(textureatlassprite1);
						v1 = UVHelper.getMaxV(textureatlassprite1);
						u2 = UVHelper.getMaxU(textureatlassprite1);
						break;
					case 5:
					case 6:
						u0 = UVHelper.getMaxU(textureatlassprite1);
						v0 = UVHelper.getMaxV(textureatlassprite1);
						v1 = UVHelper.getMinV(textureatlassprite1);
						u2 = UVHelper.getMinU(textureatlassprite1);
						break;
				}
				u1 = u0;
				v2 = v1;
				u3 = u2;
				v3 = v0;
			} else {
//				u0 = textureatlassprite1.getInterpolatedU(0.0D);
//				v0 = textureatlassprite1.getInterpolatedV(0.0D);
				u0 = UVHelper.getMinU(textureatlassprite1);
				v0 = UVHelper.getMinV(textureatlassprite1);
				u1 = u0;
//			    v1 = textureatlassprite1.getInterpolatedV(16.0D);
//			    u2 = textureatlassprite1.getInterpolatedU(16.0D);
				v1 = UVHelper.getMaxV(textureatlassprite1);
				u2 = UVHelper.getMaxU(textureatlassprite1);
				v2 = v1;
				u3 = u2;
				v3 = v0;
			}
		} else {
			TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
			float f20 = (float) MathHelper.atan2(flow.z, flow.x) - ((float) Math.PI / 2F);
			float f21 = MathHelper.sin(f20) * 0.25F;
			float f22 = MathHelper.cos(f20) * 0.25F;
//		    float f23 = 8.0F;
			u0 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f22 - f21) * 16.0F));
			v0 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f22 + f21) * 16.0F));
			u1 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f22 + f21) * 16.0F));
			v1 = textureatlassprite.getInterpolatedV((double) (8.0F + (f22 + f21) * 16.0F));
			u2 = textureatlassprite.getInterpolatedU((double) (8.0F + (f22 + f21) * 16.0F));
			v2 = textureatlassprite.getInterpolatedV((double) (8.0F + (f22 - f21) * 16.0F));
			u3 = textureatlassprite.getInterpolatedU((double) (8.0F + (f22 - f21) * 16.0F));
			v3 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f22 - f21) * 16.0F));
		}

		int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, pos);
		int skyLight = combinedLightUpMax >> 16 & '\uffff';
		int blockLight = combinedLightUpMax & '\uffff';
		if (!colors()) {
			float r = 1.0F * red;
			float g = 1.0F * green;
			float b = 1.0F * blue;
			bufferBuilder.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(r, g, b, 1.0F).tex((double) u0, (double) v0).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 0.0D, y + fluidHeightS, z + 1.0D).color(r, g, b, 1.0F).tex((double) u1, (double) v1).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 1.0D, y + fluidHeightES, z + 1.0D).color(r, g, b, 1.0F).tex((double) u2, (double) v2).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 1.0D, y + fluidHeightE, z + 0.0D).color(r, g, b, 1.0F).tex((double) u3, (double) v3).lightmap(skyLight, blockLight).endVertex();
			if (state.shouldRenderSides(worldIn, pos.up())) {
				bufferBuilder.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(r, g, b, 1.0F).tex((double) u0, (double) v0).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(x + 1.0D, y + fluidHeightS, z + 0.0D).color(r, g, b, 1.0F).tex((double) u3, (double) v3).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(x + 1.0D, y + fluidHeightES, z + 1.0D).color(r, g, b, 1.0F).tex((double) u2, (double) v2).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(x + 0.0D, y + fluidHeightE, z + 1.0D).color(r, g, b, 1.0F).tex((double) u1, (double) v1).lightmap(skyLight, blockLight).endVertex();
			}
		} else {
			float r0 = 1.0F * red;
			float g0 = 1.0F * green;
			float b0 = 1.0F * blue;
			final int colorS = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south());
			float r1 = 1.0F * ((colorS >> 16) & 255) / 255.0F;
			float g1 = 1.0F * ((colorS >> 8) & 255) / 255.0F;
			float b1 = 1.0F * ((colorS) & 255) / 255.0F;
			final int colorSE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south().east());
			float r2 = 1.0F * ((colorSE >> 16) & 255) / 255.0F;
			float g2 = 1.0F * ((colorSE >> 8) & 255) / 255.0F;
			float b2 = 1.0F * ((colorSE) & 255) / 255.0F;
			final int colorE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.east());
			float r3 = 1.0F * ((colorE >> 16) & 255) / 255.0F;
			float g3 = 1.0F * ((colorE >> 8) & 255) / 255.0F;
			float b3 = 1.0F * ((colorE) & 255) / 255.0F;
			bufferBuilder.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(r0, g0, b0, 1.0F).tex((double) u0, (double) v0).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 0.0D, y + fluidHeightS, z + 1.0D).color(r1, g1, b1, 1.0F).tex((double) u1, (double) v1).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 1.0D, y + fluidHeightES, z + 1.0D).color(r2, g2, b2, 1.0F).tex((double) u2, (double) v2).lightmap(skyLight, blockLight).endVertex();
			bufferBuilder.pos(x + 1.0D, y + fluidHeightE, z + 0.0D).color(r3, g3, b3, 1.0F).tex((double) u3, (double) v3).lightmap(skyLight, blockLight).endVertex();
			if (state.shouldRenderSides(worldIn, pos.up())) {
				bufferBuilder.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(r0, g0, b0, 1.0F).tex((double) u0, (double) v0).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(x + 1.0D, y + fluidHeightS, z + 0.0D).color(r3, g3, b3, 1.0F).tex((double) u3, (double) v3).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(x + 1.0D, y + fluidHeightES, z + 1.0D).color(r2, g2, b2, 1.0F).tex((double) u2, (double) v2).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(x + 0.0D, y + fluidHeightE, z + 1.0D).color(r1, g1, b1, 1.0F).tex((double) u1, (double) v1).lightmap(skyLight, blockLight).endVertex();
			}
		}
	}

	public void renderUpSmooth(final IWorldReader worldIn, final BlockPos pos, final BufferBuilder bufferBuilder, final IFluidState state, final TextureAtlasSprite[] atextureatlassprite, final float red, final float green, final float blue, final double fluidHeight, final double fluidHeightS, final double fluidHeightES, final double fluidHeightE, final double x, final double y, final double z, final Vec3d flow, final boolean isLava) {
		float u0;
		float u1;
		float u2;
		float u3;
		float v0;
		float v1;
		float v2;
		float v3;
		if (flow.x == 0.0D && flow.z == 0.0D) {
			TextureAtlasSprite textureatlassprite1 = atextureatlassprite[0];
			if (textures()) {
				final int rand = (int) (MathHelper.getPositionRandom(pos) % 7);
				switch (rand) {
					default:
					case 0:
						u0 = UVHelper.getMinU(textureatlassprite1);
						v0 = UVHelper.getMinV(textureatlassprite1);
						v1 = UVHelper.getMaxV(textureatlassprite1);
						u2 = UVHelper.getMaxU(textureatlassprite1);
						break;
					case 1:
					case 2:
						u0 = UVHelper.getMaxU(textureatlassprite1);
						v0 = UVHelper.getMaxV(textureatlassprite1);
						v1 = UVHelper.getMinV(textureatlassprite1);
						u2 = UVHelper.getMinU(textureatlassprite1);
						break;
					case 3:
					case 4:
						u0 = UVHelper.getMinU(textureatlassprite1);
						v0 = UVHelper.getMinV(textureatlassprite1);
						v1 = UVHelper.getMaxV(textureatlassprite1);
						u2 = UVHelper.getMaxU(textureatlassprite1);
						break;
					case 5:
					case 6:
						u0 = UVHelper.getMaxU(textureatlassprite1);
						v0 = UVHelper.getMaxV(textureatlassprite1);
						v1 = UVHelper.getMinV(textureatlassprite1);
						u2 = UVHelper.getMinU(textureatlassprite1);
						break;
				}
				u1 = u0;
				v2 = v1;
				u3 = u2;
				v3 = v0;
			} else {
//				u0 = textureatlassprite1.getInterpolatedU(0.0D);
//				v0 = textureatlassprite1.getInterpolatedV(0.0D);
				u0 = UVHelper.getMinU(textureatlassprite1);
				v0 = UVHelper.getMinV(textureatlassprite1);
				u1 = u0;
//			    v1 = textureatlassprite1.getInterpolatedV(16.0D);
//			    u2 = textureatlassprite1.getInterpolatedU(16.0D);
				v1 = UVHelper.getMaxV(textureatlassprite1);
				u2 = UVHelper.getMaxU(textureatlassprite1);
				v2 = v1;
				u3 = u2;
				v3 = v0;
			}
		} else {
			TextureAtlasSprite textureatlassprite = atextureatlassprite[1];
			float f20 = (float) MathHelper.atan2(flow.z, flow.x) - ((float) Math.PI / 2F);
			float f21 = MathHelper.sin(f20) * 0.25F;
			float f22 = MathHelper.cos(f20) * 0.25F;
//		    float f23 = 8.0F;
			u0 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f22 - f21) * 16.0F));
			v0 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f22 + f21) * 16.0F));
			u1 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f22 + f21) * 16.0F));
			v1 = textureatlassprite.getInterpolatedV((double) (8.0F + (f22 + f21) * 16.0F));
			u2 = textureatlassprite.getInterpolatedU((double) (8.0F + (f22 + f21) * 16.0F));
			v2 = textureatlassprite.getInterpolatedV((double) (8.0F + (f22 - f21) * 16.0F));
			u3 = textureatlassprite.getInterpolatedU((double) (8.0F + (f22 - f21) * 16.0F));
			v3 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f22 - f21) * 16.0F));
		}

		final int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, pos);
		final int combinedLightUpMaxSouth = this.getCombinedLightUpMax(worldIn, pos.south());
		final int combinedLightUpMaxSouthEast = this.getCombinedLightUpMax(worldIn, pos.south().east());
		final int combinedLightUpMaxEast = this.getCombinedLightUpMax(worldIn, pos.east());

		int skylightUp = combinedLightUpMax >> 16 & '\uffff';
		int blocklightUp = combinedLightUpMax & '\uffff';
		int skylightUpSouth = combinedLightUpMaxSouth >> 16 & '\uffff';
		int blocklightUpSouth = combinedLightUpMaxSouth & '\uffff';
		int skylightUpSouthEast = combinedLightUpMaxSouthEast >> 16 & '\uffff';
		int blocklightUpSouthEast = combinedLightUpMaxSouthEast & '\uffff';
		int skylightUpEast = combinedLightUpMaxEast >> 16 & '\uffff';
		int blocklightUpEast = combinedLightUpMaxEast & '\uffff';

		if (skylightUpSouth == 0) skylightUpSouth = skylightUp;
		if (blocklightUpSouth == 0) blocklightUpSouth = blocklightUp;
		if (skylightUpSouthEast == 0) skylightUpSouthEast = skylightUp;
		if (blocklightUpSouthEast == 0) blocklightUpSouthEast = blocklightUp;
		if (skylightUpEast == 0) skylightUpEast = skylightUp;
		if (blocklightUpEast == 0) blocklightUpEast = blocklightUp;

		if (!colors()) {
			float r = 1.0F * red;
			float g = 1.0F * green;
			float b = 1.0F * blue;
			bufferBuilder.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(r, g, b, 1.0F).tex((double) u0, (double) v0).lightmap(skylightUp, blocklightUp).endVertex();
			bufferBuilder.pos(x + 0.0D, y + fluidHeightS, z + 1.0D).color(r, g, b, 1.0F).tex((double) u1, (double) v1).lightmap(skylightUpSouth, blocklightUpSouth).endVertex();
			bufferBuilder.pos(x + 1.0D, y + fluidHeightES, z + 1.0D).color(r, g, b, 1.0F).tex((double) u2, (double) v2).lightmap(skylightUpSouthEast, blocklightUpSouthEast).endVertex();
			bufferBuilder.pos(x + 1.0D, y + fluidHeightE, z + 0.0D).color(r, g, b, 1.0F).tex((double) u3, (double) v3).lightmap(skylightUpEast, blocklightUpEast).endVertex();
			if (state.shouldRenderSides(worldIn, pos.up())) {
				bufferBuilder.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(r, g, b, 1.0F).tex((double) u0, (double) v0).lightmap(skylightUp, blocklightUp).endVertex();
				bufferBuilder.pos(x + 1.0D, y + fluidHeightS, z + 0.0D).color(r, g, b, 1.0F).tex((double) u3, (double) v3).lightmap(skylightUpEast, blocklightUpEast).endVertex();
				bufferBuilder.pos(x + 1.0D, y + fluidHeightES, z + 1.0D).color(r, g, b, 1.0F).tex((double) u2, (double) v2).lightmap(skylightUpSouthEast, blocklightUpSouthEast).endVertex();
				bufferBuilder.pos(x + 0.0D, y + fluidHeightE, z + 1.0D).color(r, g, b, 1.0F).tex((double) u1, (double) v1).lightmap(skylightUpSouth, blocklightUpSouth).endVertex();
			}
		} else {
			float r0 = 1.0F * red;
			float g0 = 1.0F * green;
			float b0 = 1.0F * blue;
			final int colorS = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south());
			float r1 = 1.0F * ((colorS >> 16) & 255) / 255.0F;
			float g1 = 1.0F * ((colorS >> 8) & 255) / 255.0F;
			float b1 = 1.0F * ((colorS) & 255) / 255.0F;
			final int colorSE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south().east());
			float r2 = 1.0F * ((colorSE >> 16) & 255) / 255.0F;
			float g2 = 1.0F * ((colorSE >> 8) & 255) / 255.0F;
			float b2 = 1.0F * ((colorSE) & 255) / 255.0F;
			final int colorE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.east());
			float r3 = 1.0F * ((colorE >> 16) & 255) / 255.0F;
			float g3 = 1.0F * ((colorE >> 8) & 255) / 255.0F;
			float b3 = 1.0F * ((colorE) & 255) / 255.0F;
			bufferBuilder.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(r0, g0, b0, 1.0F).tex((double) u0, (double) v0).lightmap(skylightUp, blocklightUp).endVertex();
			bufferBuilder.pos(x + 0.0D, y + fluidHeightS, z + 1.0D).color(r1, g1, b1, 1.0F).tex((double) u1, (double) v1).lightmap(skylightUpSouth, blocklightUpSouth).endVertex();
			bufferBuilder.pos(x + 1.0D, y + fluidHeightES, z + 1.0D).color(r2, g2, b2, 1.0F).tex((double) u2, (double) v2).lightmap(skylightUpSouthEast, blocklightUpSouthEast).endVertex();
			bufferBuilder.pos(x + 1.0D, y + fluidHeightE, z + 0.0D).color(r3, g3, b3, 1.0F).tex((double) u3, (double) v3).lightmap(skylightUpEast, blocklightUpEast).endVertex();
			if (state.shouldRenderSides(worldIn, pos.up())) {
				bufferBuilder.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(r0, g0, b0, 1.0F).tex((double) u0, (double) v0).lightmap(skylightUp, blocklightUp).endVertex();
				bufferBuilder.pos(x + 1.0D, y + fluidHeightS, z + 0.0D).color(r3, g3, b3, 1.0F).tex((double) u3, (double) v3).lightmap(skylightUpEast, blocklightUpEast).endVertex();
				bufferBuilder.pos(x + 1.0D, y + fluidHeightES, z + 1.0D).color(r2, g2, b2, 1.0F).tex((double) u2, (double) v2).lightmap(skylightUpSouthEast, blocklightUpSouthEast).endVertex();
				bufferBuilder.pos(x + 0.0D, y + fluidHeightE, z + 1.0D).color(r1, g1, b1, 1.0F).tex((double) u1, (double) v1).lightmap(skylightUpSouth, blocklightUpSouth).endVertex();
			}
		}
	}

	public void renderDownFlat(final BufferBuilder bufferBuilder, final TextureAtlasSprite sprite, final float red, final float green, final float blue, final double x, final double y, final double z, final IWorldReader worldIn, final BlockPos pos, final boolean isLava) {
		float minU = UVHelper.getMinU(sprite);
		float maxU = UVHelper.getMaxU(sprite);
		float minV = UVHelper.getMinV(sprite);
		float maxV = UVHelper.getMaxV(sprite);
		final int combinedLightDownMax = this.getCombinedLightDownMax(worldIn, pos);
		int skyLight = combinedLightDownMax >> 16 & '\uffff';
		int blockLight = combinedLightDownMax & '\uffff';
		if (!colors()) {
			float r = 0.5F * red;
			float g = 0.5F * green;
			float b = 0.5F * blue;
			bufferBuilder
					.pos(x, y, z + 1.0D)
					.color(r, g, b, 1.0F)
					.tex((double) minU, (double) maxV)
					.lightmap(skyLight, blockLight)
					.endVertex();
			bufferBuilder
					.pos(x, y, z)
					.color(r, g, b, 1.0F)
					.tex((double) minU, (double) minV)
					.lightmap(skyLight, blockLight)
					.endVertex();
			bufferBuilder
					.pos(x + 1.0D, y, z)
					.color(r, g, b, 1.0F)
					.tex((double) maxU, (double) minV)
					.lightmap(skyLight, blockLight)
					.endVertex();
			bufferBuilder
					.pos(x + 1.0D, y, z + 1.0D)
					.color(r, g, b, 1.0F)
					.tex((double) maxU, (double) maxV)
					.lightmap(skyLight, blockLight)
					.endVertex();
		} else {
			float r0 = 0.5F * red;
			float g0 = 0.5F * green;
			float b0 = 0.5F * blue;
			final int colorS = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south());
			float r1 = 0.5F * ((colorS >> 16) & 255) / 255.0F;
			float g1 = 0.5F * ((colorS >> 8) & 255) / 255.0F;
			float b1 = 0.5F * ((colorS) & 255) / 255.0F;
			final int colorSE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south().east());
			float r2 = 0.5F * ((colorSE >> 16) & 255) / 255.0F;
			float g2 = 0.5F * ((colorSE >> 8) & 255) / 255.0F;
			float b2 = 0.5F * ((colorSE) & 255) / 255.0F;
			final int colorE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.east());
			float r3 = 0.5F * ((colorE >> 16) & 255) / 255.0F;
			float g3 = 0.5F * ((colorE >> 8) & 255) / 255.0F;
			float b3 = 0.5F * ((colorE) & 255) / 255.0F;
			bufferBuilder
					.pos(x, y, z + 1.0D)
					.color(r0, g0, b0, 1.0F)
					.tex((double) minU, (double) maxV)
					.lightmap(skyLight, blockLight)
					.endVertex();
			bufferBuilder
					.pos(x, y, z)
					.color(r1, g1, b1, 1.0F)
					.tex((double) minU, (double) minV)
					.lightmap(skyLight, blockLight)
					.endVertex();
			bufferBuilder
					.pos(x + 1.0D, y, z)
					.color(r2, g2, b2, 1.0F)
					.tex((double) maxU, (double) minV)
					.lightmap(skyLight, blockLight)
					.endVertex();
			bufferBuilder
					.pos(x + 1.0D, y, z + 1.0D)
					.color(r3, g3, b3, 1.0F)
					.tex((double) maxU, (double) maxV)
					.lightmap(skyLight, blockLight)
					.endVertex();
		}
	}

	public void renderDownSmooth(final BufferBuilder bufferBuilder, final TextureAtlasSprite sprite, final float red, final float green, final float blue, final double x, final double y, final double z, final IWorldReader worldIn, final BlockPos pos, final boolean isLava) {
		float minU = UVHelper.getMinU(sprite);
		float maxU = UVHelper.getMaxU(sprite);
		float minV = UVHelper.getMinV(sprite);
		float maxV = UVHelper.getMaxV(sprite);

		final int combinedLightDownMax = this.getCombinedLightDownMax(worldIn, pos);
		final int combinedLightDownMaxSouth = this.getCombinedLightDownMax(worldIn, pos.south());
		final int combinedLightDownMaxSouthEast = this.getCombinedLightDownMax(worldIn, pos.south().east());
		final int combinedLightDownMaxEast = this.getCombinedLightDownMax(worldIn, pos);

		int skylightDown = combinedLightDownMax >> 16 & '\uffff';
		int blocklightDown = combinedLightDownMax & '\uffff';
		int skylightDownSouth = combinedLightDownMaxSouth >> 16 & '\uffff';
		int blocklightDownSouth = combinedLightDownMaxSouth & '\uffff';
		int skylightDownSouthEast = combinedLightDownMaxSouthEast >> 16 & '\uffff';
		int blocklightDownSouthEast = combinedLightDownMaxSouthEast & '\uffff';
		int skylightDownEast = combinedLightDownMaxEast >> 16 & '\uffff';
		int blocklightDownEast = combinedLightDownMaxEast & '\uffff';

		if (!colors()) {
			float r = 0.5F * red;
			float g = 0.5F * green;
			float b = 0.5F * blue;
			bufferBuilder
					.pos(x, y, z + 1.0D)
					.color(r, g, b, 1.0F)
					.tex((double) minU, (double) maxV)
					.lightmap(skylightDown, blocklightDown)
					.endVertex();
			bufferBuilder
					.pos(x, y, z)
					.color(r, g, b, 1.0F)
					.tex((double) minU, (double) minV)
					.lightmap(skylightDownSouth, blocklightDownSouth)
					.endVertex();
			bufferBuilder
					.pos(x + 1.0D, y, z)
					.color(r, g, b, 1.0F)
					.tex((double) maxU, (double) minV)
					.lightmap(skylightDownSouthEast, blocklightDownSouthEast)
					.endVertex();
			bufferBuilder
					.pos(x + 1.0D, y, z + 1.0D)
					.color(r, g, b, 1.0F)
					.tex((double) maxU, (double) maxV)
					.lightmap(skylightDownEast, blocklightDownEast)
					.endVertex();
		} else {
			float r0 = 0.5F * red;
			float g0 = 0.5F * green;
			float b0 = 0.5F * blue;
			final int colorS = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south());
			float r1 = 0.5F * ((colorS >> 16) & 255) / 255.0F;
			float g1 = 0.5F * ((colorS >> 8) & 255) / 255.0F;
			float b1 = 0.5F * ((colorS) & 255) / 255.0F;
			final int colorSE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.south().east());
			float r2 = 0.5F * ((colorSE >> 16) & 255) / 255.0F;
			float g2 = 0.5F * ((colorSE >> 8) & 255) / 255.0F;
			float b2 = 0.5F * ((colorSE) & 255) / 255.0F;
			final int colorE = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, pos.east());
			float r3 = 0.5F * ((colorE >> 16) & 255) / 255.0F;
			float g3 = 0.5F * ((colorE >> 8) & 255) / 255.0F;
			float b3 = 0.5F * ((colorE) & 255) / 255.0F;
			bufferBuilder
					.pos(x, y, z + 1.0D)
					.color(r0, g0, b0, 1.0F)
					.tex((double) minU, (double) maxV)
					.lightmap(skylightDown, blocklightDown)
					.endVertex();
			bufferBuilder
					.pos(x, y, z)
					.color(r1, g1, b1, 1.0F)
					.tex((double) minU, (double) minV)
					.lightmap(skylightDownSouth, blocklightDownSouth)
					.endVertex();
			bufferBuilder
					.pos(x + 1.0D, y, z)
					.color(r2, g2, b2, 1.0F)
					.tex((double) maxU, (double) minV)
					.lightmap(skylightDownSouthEast, blocklightDownSouthEast)
					.endVertex();
			bufferBuilder
					.pos(x + 1.0D, y, z + 1.0D)
					.color(r3, g3, b3, 1.0F)
					.tex((double) maxU, (double) maxV)
					.lightmap(skylightDownEast, blocklightDownEast)
					.endVertex();
		}
	}

	public int getCombinedLightDownMax(IWorldReader reader, BlockPos pos) {
		int i = reader.getCombinedLight(pos, 0);
		int j = reader.getCombinedLight(pos.down(), 0);
		int k = i & 255;
		int l = j & 255;
		int i1 = i >> 16 & 255;
		int j1 = j >> 16 & 255;
		return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
	}

	@Nonnull
	public BlockFluidRenderer getOldFluidRenderer() {
		return fluidRenderer;
	}

	public boolean smoothLighting() {
		return true;
	}

	public boolean colors() {
		return true;
	}

	public boolean textures() {
		return true;
	}

}
