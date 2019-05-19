package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.UVHelper;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldReader;
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

//		if (true) return fluidRenderer.render(worldIn, pos, buffer, state);

		OptiFineCompatibility.pushShaderThing(state, pos, worldIn, buffer);
		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			final boolean isLava = state.isTagged(FluidTags.LAVA);
			final TextureAtlasSprite[] atextureatlassprite = isLava ? this.atlasSpritesLava : this.atlasSpritesWater;

			final float red;
			final float green;
			final float blue;
			if (isLava) {
				red = 1.0F;
				green = 1.0F;
				blue = 1.0F;
			} else {
				final int waterColor = BiomeColors.getWaterColor(worldIn, pos);
				red = (float) (waterColor >> 16 & 0xFF) / 255.0F;
				green = (float) (waterColor >> 8 & 0xFF) / 255.0F;
				blue = (float) (waterColor & 0xFF) / 255.0F;
			}

			final boolean shouldRenderUp = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.UP, state);
			final boolean shouldRenderDown = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.DOWN, state) && !func_209556_a(worldIn, pos, EnumFacing.DOWN, 0.8888889F);
			final boolean shouldRenderNorth = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.NORTH, state);
			final boolean shouldRenderSouth = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.SOUTH, state);
			final boolean shouldRenderWest = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.WEST, state);
			final boolean shouldRenderEast = !isAdjacentFluidSameAs(worldIn, pos, EnumFacing.EAST, state);

			if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
				return false;
			}

			boolean wasAnythingRendered = false;

			float fluidHeight = this.getFluidHeight(worldIn, pos, state.getFluid());
			float fluidHeightSouth = this.getFluidHeight(worldIn, pos.south(), state.getFluid());
			float fluidHeightEastSouth = this.getFluidHeight(worldIn, pos.east().south(), state.getFluid());
			float fluidHeightEast = this.getFluidHeight(worldIn, pos.east(), state.getFluid());

			final double x = (double) pos.getX();
			final double y = (double) pos.getY();
			final double z = (double) pos.getZ();

			if (shouldRenderUp && !func_209556_a(worldIn, pos, EnumFacing.UP, Math.min(Math.min(fluidHeight, fluidHeightSouth), Math.min(fluidHeightEastSouth, fluidHeightEast)))) {

				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for this code to exist in the first place
				// is to try and solve z-fighting issues.
//				fluidHeight -= 0.001F;
//				fluidHeightSouth -= 0.001F;
//				fluidHeightEastSouth -= 0.001F;
//				fluidHeightEast -= 0.001F;

				if (!this.colors()) {
					if (!this.smoothLighting()) {
						final int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, pos);
						wasAnythingRendered |= this.renderUp(
								buffer, atextureatlassprite,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
								x, y, z,
								combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
								state.shouldRenderSides(worldIn, pos.up()), state.getFlow(worldIn, pos), MathHelper.getPositionRandom(pos)
						);
					} else {
						wasAnythingRendered |= this.renderUp(
								buffer, atextureatlassprite,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
								x, y, z,
								this.getCombinedLightUpMax(worldIn, pos), this.getCombinedLightUpMax(worldIn, pos.south()), this.getCombinedLightUpMax(worldIn, pos.east().south()), this.getCombinedLightUpMax(worldIn, pos.east()),
								state.shouldRenderSides(worldIn, pos.up()), state.getFlow(worldIn, pos), MathHelper.getPositionRandom(pos)
						);
					}
				} else {
					final float red0;
					final float green0;
					final float blue0;
					final float red1;
					final float green1;
					final float blue1;
					final float red2;
					final float green2;
					final float blue2;
					final float red3;
					final float green3;
					final float blue3;
					if (isLava) {
						red0 = 1.0F;
						green0 = 1.0F;
						blue0 = 1.0F;
						red1 = 1.0F;
						green1 = 1.0F;
						blue1 = 1.0F;
						red2 = 1.0F;
						green2 = 1.0F;
						blue2 = 1.0F;
						red3 = 1.0F;
						green3 = 1.0F;
						blue3 = 1.0F;
					} else {
//						final int waterColor0 = BiomeColors.getWaterColor(worldIn, pos);
//						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
//						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
//						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						red0 = red;
						green0 = green;
						blue0 = blue;
						final int waterColor1 = BiomeColors.getWaterColor(worldIn, pos.south());
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						final int waterColor2 = BiomeColors.getWaterColor(worldIn, pos.east().south());
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						final int waterColor3 = BiomeColors.getWaterColor(worldIn, pos.east());
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}
					if (!this.smoothLighting()) {
						final int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, pos);
						wasAnythingRendered |= this.renderUp(
								buffer, atextureatlassprite,
								red0, green0, blue0,
								red1, green1, blue1,
								red2, green2, blue2,
								red3, green3, blue3,
								fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
								x, y, z,
								combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
								state.shouldRenderSides(worldIn, pos.up()), state.getFlow(worldIn, pos), MathHelper.getPositionRandom(pos)
						);
					} else {
						wasAnythingRendered |= this.renderUp(
								buffer, atextureatlassprite,
								red0, green0, blue0,
								red1, green1, blue1,
								red2, green2, blue2,
								red3, green3, blue3,
								fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
								x, y, z,
								this.getCombinedLightUpMax(worldIn, pos), this.getCombinedLightUpMax(worldIn, pos.south()), this.getCombinedLightUpMax(worldIn, pos.east().south()), this.getCombinedLightUpMax(worldIn, pos.east()),
								state.shouldRenderSides(worldIn, pos.up()), state.getFlow(worldIn, pos), MathHelper.getPositionRandom(pos)
						);
					}
				}
			}

			if (shouldRenderDown) {
				if (!this.colors()) {
					if (!this.smoothLighting()) {
						final int downCombinedLightUpMax = this.getCombinedLightUpMax(worldIn, pos.down());
						wasAnythingRendered |= this.renderDown(
								downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax,
								buffer, atextureatlassprite[0],
								red, green, blue,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								x, y, z
						);
					} else {
						final BlockPos down = pos.down();
						wasAnythingRendered |= this.renderDown(
								this.getCombinedLightUpMax(worldIn, down), this.getCombinedLightUpMax(worldIn, down.south()), this.getCombinedLightUpMax(worldIn, down.east().south()), this.getCombinedLightUpMax(worldIn, down.east()),
								buffer, atextureatlassprite[0],
								red, green, blue,
								red, green, blue,
								red, green, blue,
								red, green, blue,
								x, y, z
						);
					}
				} else {

					final BlockPos down = pos.down();

					final float red0;
					final float green0;
					final float blue0;
					final float red1;
					final float green1;
					final float blue1;
					final float red2;
					final float green2;
					final float blue2;
					final float red3;
					final float green3;
					final float blue3;
					if (isLava) {
						red0 = 1.0F;
						green0 = 1.0F;
						blue0 = 1.0F;
						red1 = 1.0F;
						green1 = 1.0F;
						blue1 = 1.0F;
						red2 = 1.0F;
						green2 = 1.0F;
						blue2 = 1.0F;
						red3 = 1.0F;
						green3 = 1.0F;
						blue3 = 1.0F;
					} else {
						final int waterColor0 = BiomeColors.getWaterColor(worldIn, down);
						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						final int waterColor1 = BiomeColors.getWaterColor(worldIn, down.south());
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						final int waterColor2 = BiomeColors.getWaterColor(worldIn, down.east().south());
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						final int waterColor3 = BiomeColors.getWaterColor(worldIn, down.east());
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}

					if (!this.smoothLighting()) {
						final int downCombinedLightUpMax = this.getCombinedLightUpMax(worldIn, down);
						wasAnythingRendered |= this.renderDown(
								downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax,
								buffer, atextureatlassprite[0],
								red0, green0, blue0,
								red1, green1, blue1,
								red2, green2, blue2,
								red3, green3, blue3,
								x, y, z
						);
					} else {
						wasAnythingRendered |= this.renderDown(
								this.getCombinedLightUpMax(worldIn, down), this.getCombinedLightUpMax(worldIn, down.south()), this.getCombinedLightUpMax(worldIn, down.east().south()), this.getCombinedLightUpMax(worldIn, down.east()),
								buffer, atextureatlassprite[0],
								red0, green0, blue0,
								red1, green1, blue1,
								red2, green2, blue2,
								red3, green3, blue3,
								x, y, z
						);
					}
				}
			}

			for (int facingIndex = 0; facingIndex < 4; ++facingIndex) {
				final float y0;
				final float y1;
				final double x0;
				final double z0;
				final double x1;
				final double z1;
				final EnumFacing enumfacing;
				final boolean shouldRenderSide;
				if (facingIndex == 0) {
					y0 = fluidHeight;
					y1 = fluidHeightEast;
					x0 = x;
					x1 = x + 1.0D;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
					z0 = z;// + (double) 0.001F;
					z1 = z;// + (double) 0.001F;
					enumfacing = EnumFacing.NORTH;
					shouldRenderSide = shouldRenderNorth;
				} else if (facingIndex == 1) {
					y0 = fluidHeightEastSouth;
					y1 = fluidHeightSouth;
					x0 = x + 1.0D;
					x1 = x;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
					z0 = z + 1.0D;// - (double) 0.001F;
					z1 = z + 1.0D;// - (double) 0.001F;
					enumfacing = EnumFacing.SOUTH;
					shouldRenderSide = shouldRenderSouth;
				} else if (facingIndex == 2) {
					y0 = fluidHeightSouth;
					y1 = fluidHeight;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
					x0 = x;// + (double) 0.001F;
					x1 = x;// + (double) 0.001F;
					z0 = z + 1.0D;
					z1 = z;
					enumfacing = EnumFacing.WEST;
					shouldRenderSide = shouldRenderWest;
				} else {
					y0 = fluidHeightEast;
					y1 = fluidHeightEastSouth;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
					x0 = x + 1.0D;// - (double) 0.001F;
					x1 = x + 1.0D;// - (double) 0.001F;
					z0 = z;
					z1 = z + 1.0D;
					enumfacing = EnumFacing.EAST;
					shouldRenderSide = shouldRenderEast;
				}

				if (shouldRenderSide && !func_209556_a(worldIn, pos, enumfacing, Math.max(y0, y1))) {
					final BlockPos offset = pos.offset(enumfacing);
					TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
					if (!isLava) {
						IBlockState blockstate = worldIn.getBlockState(offset);
						if (blockstate.getBlockFaceShape(worldIn, offset, enumfacing) == net.minecraft.block.state.BlockFaceShape.SOLID) {
							textureatlassprite2 = this.atlasSpriteWaterOverlay;
						}
					}

					if (!this.colors()) {
						if (!this.smoothLighting()) {
							final int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, offset);
							wasAnythingRendered = this.renderSide(
									buffer, textureatlassprite2,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									facingIndex,
									y, y0, y1,
									x0, x1,
									z0, z1,
									combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
									textureatlassprite2 != this.atlasSpriteWaterOverlay
							);
						} else {
							wasAnythingRendered = this.renderSide(
									buffer, textureatlassprite2,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									facingIndex,
									y, y0, y1,
									x0, x1,
									z0, z1,
									this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0)),
									this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1)),
									this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y, z1)),
									this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y, z0)),
									textureatlassprite2 != this.atlasSpriteWaterOverlay
							);
						}
					} else {
						final float red0;
						final float green0;
						final float blue0;
						final float red1;
						final float green1;
						final float blue1;
						final float red2;
						final float green2;
						final float blue2;
						final float red3;
						final float green3;
						final float blue3;
						if (isLava) {
							red0 = 1.0F;
							green0 = 1.0F;
							blue0 = 1.0F;
							red1 = 1.0F;
							green1 = 1.0F;
							blue1 = 1.0F;
							red2 = 1.0F;
							green2 = 1.0F;
							blue2 = 1.0F;
							red3 = 1.0F;
							green3 = 1.0F;
							blue3 = 1.0F;
						} else {
							final int waterColor0 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0));
							red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
							green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
							blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
							final int waterColor1 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1));
							red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
							green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
							blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
							final int waterColor2 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x1, y, z1));
							red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
							green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
							blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
							final int waterColor3 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x0, y, z0));
							red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
							green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
							blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
						}

						if (!this.smoothLighting()) {
							final int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, offset);
							wasAnythingRendered = renderSide(
									buffer, textureatlassprite2,
									red0, green0, blue0,
									red1, green1, blue1,
									red2, green2, blue2,
									red3, green3, blue3,
									facingIndex,
									y, y0, y1,
									x0, x1,
									z0, z1,
									combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
									textureatlassprite2 != this.atlasSpriteWaterOverlay
							);
						} else {
							wasAnythingRendered = this.renderSide(
									buffer, textureatlassprite2,
									red0, green0, blue0,
									red1, green1, blue1,
									red2, green2, blue2,
									red3, green3, blue3,
									facingIndex,
									y, y0, y1,
									x0, x1,
									z0, z1,
									this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0)),
									this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1)),
									this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y, z1)),
									this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y, z0)),
									textureatlassprite2 != this.atlasSpriteWaterOverlay
							);
						}
					}
				}
			}

			return wasAnythingRendered;
		} finally {
			OptiFineCompatibility.popShaderThing(buffer);
		}
	}

	public boolean renderUp(
			final BufferBuilder buffer, final TextureAtlasSprite[] sprites,
			final float red0, final float green0, final float blue0,
			final float red1, final float green1, final float blue1,
			final float red2, final float green2, final float blue2,
			final float red3, final float green3, final float blue3,
			final double fluidHeight, final double fluidHeightSouth, final double fluidHeightEastSouth, final double fluidHeightEast,
			final double x, final double y, final double z,
			final int combinedLightUpMax0, final int combinedLightUpMax1, final int combinedLightUpMax2, final int combinedLightUpMax3,
			final boolean shouldRenderUpUndersideFace, final Vec3d flowVec, final long positionRandom
	) {
		float u0;
		float u1;
		float u2;
		float u3;
		float v0;
		float v1;
		float v2;
		float v3;
		if (flowVec.x == 0.0D && flowVec.z == 0.0D) {
			TextureAtlasSprite stillSprite = sprites[0];
			if (textures()) {
				final int rand = (int) (positionRandom % 7);
				switch (rand) {
					default:
					case 0:
						u0 = UVHelper.getMinU(stillSprite);
						v0 = UVHelper.getMinV(stillSprite);
						v1 = UVHelper.getMaxV(stillSprite);
						u2 = UVHelper.getMaxU(stillSprite);
						break;
					case 1:
					case 2:
						u0 = UVHelper.getMaxU(stillSprite);
						v0 = UVHelper.getMaxV(stillSprite);
						v1 = UVHelper.getMinV(stillSprite);
						u2 = UVHelper.getMinU(stillSprite);
						break;
					case 3:
					case 4:
						u0 = UVHelper.getMinU(stillSprite);
						v0 = UVHelper.getMinV(stillSprite);
						v1 = UVHelper.getMaxV(stillSprite);
						u2 = UVHelper.getMaxU(stillSprite);
						break;
					case 5:
					case 6:
						u0 = UVHelper.getMaxU(stillSprite);
						v0 = UVHelper.getMaxV(stillSprite);
						v1 = UVHelper.getMinV(stillSprite);
						u2 = UVHelper.getMinU(stillSprite);
						break;
				}
				u1 = u0;
				v2 = v1;
				u3 = u2;
				v3 = v0;
			} else {
				u0 = UVHelper.getMinU(stillSprite);
				v0 = UVHelper.getMinV(stillSprite);
				u1 = u0;
				v1 = UVHelper.getMaxV(stillSprite);
				u2 = UVHelper.getMaxU(stillSprite);
				v2 = v1;
				u3 = u2;
				v3 = v0;
			}
		} else {
			TextureAtlasSprite flowingSprite = sprites[1];
			float magicAtan2Flow = (float) MathHelper.atan2(flowVec.z, flowVec.x) - ((float) Math.PI / 2F);
			float sinMagicAtan2Flow = MathHelper.sin(magicAtan2Flow) * 0.25F;
			float cosMagicAtan2Flow = MathHelper.cos(magicAtan2Flow) * 0.25F;
			u0 = UVHelper.clampU(flowingSprite.getInterpolatedU((double) (8.0F + (-cosMagicAtan2Flow - sinMagicAtan2Flow) * 16.0F)), flowingSprite);
			v0 = UVHelper.clampV(flowingSprite.getInterpolatedV((double) (8.0F + (-cosMagicAtan2Flow + sinMagicAtan2Flow) * 16.0F)), flowingSprite);
			u1 = UVHelper.clampU(flowingSprite.getInterpolatedU((double) (8.0F + (-cosMagicAtan2Flow + sinMagicAtan2Flow) * 16.0F)), flowingSprite);
			v1 = UVHelper.clampV(flowingSprite.getInterpolatedV((double) (8.0F + (cosMagicAtan2Flow + sinMagicAtan2Flow) * 16.0F)), flowingSprite);
			u2 = UVHelper.clampU(flowingSprite.getInterpolatedU((double) (8.0F + (cosMagicAtan2Flow + sinMagicAtan2Flow) * 16.0F)), flowingSprite);
			v2 = UVHelper.clampV(flowingSprite.getInterpolatedV((double) (8.0F + (cosMagicAtan2Flow - sinMagicAtan2Flow) * 16.0F)), flowingSprite);
			u3 = UVHelper.clampU(flowingSprite.getInterpolatedU((double) (8.0F + (cosMagicAtan2Flow - sinMagicAtan2Flow) * 16.0F)), flowingSprite);
			v3 = UVHelper.clampV(flowingSprite.getInterpolatedV((double) (8.0F + (-cosMagicAtan2Flow - sinMagicAtan2Flow) * 16.0F)), flowingSprite);
		}

		final int skyLight0 = combinedLightUpMax0 >> 16 & '\uffff';
		final int blockLight0 = combinedLightUpMax0 & '\uffff';
		final int skyLight1 = combinedLightUpMax1 >> 16 & '\uffff';
		final int blockLight1 = combinedLightUpMax1 & '\uffff';
		final int skyLight2 = combinedLightUpMax2 >> 16 & '\uffff';
		final int blockLight2 = combinedLightUpMax2 & '\uffff';
		final int skyLight3 = combinedLightUpMax3 >> 16 & '\uffff';
		final int blockLight3 = combinedLightUpMax3 & '\uffff';

		buffer.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(red0, green0, blue0, 1.0F).tex((double) u0, (double) v0).lightmap(skyLight0, blockLight0).endVertex();
		buffer.pos(x + 0.0D, y + fluidHeightSouth, z + 1.0D).color(red1, green1, blue1, 1.0F).tex((double) u1, (double) v1).lightmap(skyLight1, blockLight1).endVertex();
		buffer.pos(x + 1.0D, y + fluidHeightEastSouth, z + 1.0D).color(red2, green2, blue2, 1.0F).tex((double) u2, (double) v2).lightmap(skyLight2, blockLight2).endVertex();
		buffer.pos(x + 1.0D, y + fluidHeightEast, z + 0.0D).color(red3, green3, blue3, 1.0F).tex((double) u3, (double) v3).lightmap(skyLight3, blockLight3).endVertex();
		if (shouldRenderUpUndersideFace) {
			buffer.pos(x + 0.0D, y + fluidHeight, z + 0.0D).color(red0, green0, blue0, 1.0F).tex((double) u0, (double) v0).lightmap(skyLight0, blockLight0).endVertex();
			buffer.pos(x + 1.0D, y + fluidHeightEast, z + 0.0D).color(red3, green3, blue3, 1.0F).tex((double) u3, (double) v3).lightmap(skyLight3, blockLight3).endVertex();
			buffer.pos(x + 1.0D, y + fluidHeightEastSouth, z + 1.0D).color(red2, green2, blue2, 1.0F).tex((double) u2, (double) v2).lightmap(skyLight2, blockLight2).endVertex();
			buffer.pos(x + 0.0D, y + fluidHeightSouth, z + 1.0D).color(red1, green1, blue1, 1.0F).tex((double) u1, (double) v1).lightmap(skyLight1, blockLight1).endVertex();
		}
		return true;
	}

	public boolean renderSide(
			final BufferBuilder buffer, TextureAtlasSprite textureatlassprite,
			final float red0, final float green0, final float blue0,
			final float red1, final float green1, final float blue1,
			final float red2, final float green2, final float blue2,
			final float red3, final float green3, final float blue3,
			final int facingIndex,
			final double y, final float y0, final float y1,
			final double x0, final double x1,
			final double z0, final double z1,
			final int combinedLightUpMax0, final int combinedLightUpMax1, final int combinedLightUpMax2, final int combinedLightUpMax3, final boolean shouldRenderOppositeFace
	) {
		final float u0 = UVHelper.getMinU(textureatlassprite);
		final float u1 = textureatlassprite.getInterpolatedU(8.0D);
		final float v0 = UVHelper.clampV(textureatlassprite.getInterpolatedV((double) ((1.0F - y0) * 16.0F * 0.5F)), textureatlassprite);
		final float v1 = UVHelper.clampV(textureatlassprite.getInterpolatedV((double) ((1.0F - y1) * 16.0F * 0.5F)), textureatlassprite);
		final float v2 = textureatlassprite.getInterpolatedV(8.0D);

		final int skyLight0 = combinedLightUpMax0 >> 16 & '\uffff';
		final int blockLight0 = combinedLightUpMax0 & '\uffff';
		final int skyLight1 = combinedLightUpMax1 >> 16 & '\uffff';
		final int blockLight1 = combinedLightUpMax1 & '\uffff';
		final int skyLight2 = combinedLightUpMax2 >> 16 & '\uffff';
		final int blockLight2 = combinedLightUpMax2 & '\uffff';
		final int skyLight3 = combinedLightUpMax3 >> 16 & '\uffff';
		final int blockLight3 = combinedLightUpMax3 & '\uffff';

		final float diffuse = facingIndex < 2 ? 0.8F : 0.6F;
		buffer.pos(x0, y + (double) y0, z0).color(diffuse * red0, diffuse * green0, diffuse * blue0, 1.0F).tex((double) u0, (double) v0).lightmap(skyLight0, blockLight0).endVertex();
		buffer.pos(x1, y + (double) y1, z1).color(diffuse * red1, diffuse * green1, diffuse * blue1, 1.0F).tex((double) u1, (double) v1).lightmap(skyLight1, blockLight1).endVertex();
		buffer.pos(x1, y + 0.0D, z1).color(diffuse * red2, diffuse * green2, diffuse * blue2, 1.0F).tex((double) u1, (double) v2).lightmap(skyLight2, blockLight2).endVertex();
		buffer.pos(x0, y + 0.0D, z0).color(diffuse * red3, diffuse * green3, diffuse * blue3, 1.0F).tex((double) u0, (double) v2).lightmap(skyLight3, blockLight3).endVertex();
		if (shouldRenderOppositeFace) {
			buffer.pos(x0, y + 0.0D, z0).color(diffuse * red3, diffuse * green3, diffuse * blue3, 1.0F).tex((double) u0, (double) v2).lightmap(skyLight3, blockLight3).endVertex();
			buffer.pos(x1, y + 0.0D, z1).color(diffuse * red2, diffuse * green2, diffuse * blue2, 1.0F).tex((double) u1, (double) v2).lightmap(skyLight2, blockLight2).endVertex();
			buffer.pos(x1, y + (double) y1, z1).color(diffuse * red1, diffuse * green1, diffuse * blue1, 1.0F).tex((double) u1, (double) v1).lightmap(skyLight1, blockLight1).endVertex();
			buffer.pos(x0, y + (double) y0, z0).color(diffuse * red0, diffuse * green0, diffuse * blue0, 1.0F).tex((double) u0, (double) v0).lightmap(skyLight0, blockLight0).endVertex();
		}
		return true;
	}

	public boolean renderDown(
			final int downCombinedLightUpMax0, final int downCombinedLightUpMax1, final int downCombinedLightUpMax2, final int downCombinedLightUpMax3,
			final BufferBuilder buffer, final TextureAtlasSprite textureAtlasSprite,
			final float red0, final float green0, final float blue0,
			final float red1, final float green1, final float blue1,
			final float red2, final float green2, final float blue2,
			final float red3, final float green3, final float blue3,
			final double x, final double y, final double z
	) {
		final float minU = UVHelper.getMinU(textureAtlasSprite);
		final float maxU = UVHelper.getMaxU(textureAtlasSprite);
		final float minV = UVHelper.getMinV(textureAtlasSprite);
		final float maxV = UVHelper.getMaxV(textureAtlasSprite);

		final int skyLight0 = downCombinedLightUpMax0 >> 16 & '\uffff';
		final int blockLight0 = downCombinedLightUpMax0 & '\uffff';
		final int skyLight1 = downCombinedLightUpMax1 >> 16 & '\uffff';
		final int blockLight1 = downCombinedLightUpMax1 & '\uffff';
		final int skyLight2 = downCombinedLightUpMax2 >> 16 & '\uffff';
		final int blockLight2 = downCombinedLightUpMax2 & '\uffff';
		final int skyLight3 = downCombinedLightUpMax3 >> 16 & '\uffff';
		final int blockLight3 = downCombinedLightUpMax3 & '\uffff';

		buffer.pos(x, y, z + 1.0D).color(0.5F * red0, 0.5F * green0, 0.5F * blue0, 1.0F).tex((double) minU, (double) maxV).lightmap(skyLight0, blockLight0).endVertex();
		buffer.pos(x, y, z).color(0.5F * red1, 0.5F * green1, 0.5F * blue1, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight1, blockLight1).endVertex();
		buffer.pos(x + 1.0D, y, z).color(0.5F * red2, 0.5F * green2, 0.5F * blue2, 1.0F).tex((double) maxU, (double) minV).lightmap(skyLight2, blockLight2).endVertex();
		buffer.pos(x + 1.0D, y, z + 1.0D).color(0.5F * red3, 0.5F * green3, 0.5F * blue3, 1.0F).tex((double) maxU, (double) maxV).lightmap(skyLight3, blockLight3).endVertex();
		return true;
	}

	@Nonnull
	public BlockFluidRenderer getOldFluidRenderer() {
		return fluidRenderer;
	}

	public boolean smoothLighting() {
		return Config.smoothFluidLighting;
	}

	public boolean colors() {
		return Config.smoothFluidColors;
	}

	public boolean textures() {
		return Config.naturalFluidTextures;
	}

}
