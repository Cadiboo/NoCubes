package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.UVHelper;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.FluidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.BiomeColors;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class SmoothLightingBlockFluidRenderer extends FluidBlockRenderer {

	@Nonnull
	private final FluidBlockRenderer fluidRenderer;

	public SmoothLightingBlockFluidRenderer(@Nonnull final FluidBlockRenderer fluidRenderer) {
		super();
		this.fluidRenderer = fluidRenderer;
	}

	@Override
	public boolean render(final IEnviromentBlockReader worldIn, final BlockPos pos, final BufferBuilder buffer, final IFluidState state) {

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

			final boolean shouldRenderUp = !isAdjacentFluidSameAs(worldIn, pos, Direction.UP, state);
			final boolean shouldRenderDown = !isAdjacentFluidSameAs(worldIn, pos, Direction.DOWN, state) && !func_209556_a(worldIn, pos, Direction.DOWN, 0.8888889F);
			final boolean shouldRenderNorth = !isAdjacentFluidSameAs(worldIn, pos, Direction.NORTH, state);
			final boolean shouldRenderSouth = !isAdjacentFluidSameAs(worldIn, pos, Direction.SOUTH, state);
			final boolean shouldRenderWest = !isAdjacentFluidSameAs(worldIn, pos, Direction.WEST, state);
			final boolean shouldRenderEast = !isAdjacentFluidSameAs(worldIn, pos, Direction.EAST, state);

			if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
				return false;
			}

			boolean wasAnythingRendered = false;

			final Fluid fluid = state.getFluid();
			float fluidHeight = this.getFluidHeight(worldIn, pos, fluid);
			float fluidHeightSouth = this.getFluidHeight(worldIn, pos.south(), fluid);
			float fluidHeightEastSouth = this.getFluidHeight(worldIn, pos.east().south(), fluid);
			float fluidHeightEast = this.getFluidHeight(worldIn, pos.east(), fluid);

			final double x = (double) pos.getX();
			final double y = (double) pos.getY();
			final double z = (double) pos.getZ();

			final boolean smoothLighting = this.smoothLighting();
			final boolean colors = this.colors();

			if (shouldRenderUp && !func_209556_a(worldIn, pos, Direction.UP, Math.min(Math.min(fluidHeight, fluidHeightSouth), Math.min(fluidHeightEastSouth, fluidHeightEast)))) {

				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for this code to exist in the first place
				// is to try and solve z-fighting issues.
//				fluidHeight -= 0.001F;
//				fluidHeightSouth -= 0.001F;
//				fluidHeightEastSouth -= 0.001F;
//				fluidHeightEast -= 0.001F;

				final int light0;
				final int light1;
				final int light2;
				final int light3;

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
					light0 = light1 = light2 = light3 = 0x00F000F0; // 240, 240
					red0 = red1 = red2 = red3 = 1.0F;
					green0 = green1 = green2 = green3 = 1.0F;
					blue0 = blue1 = blue2 = blue3 = 1.0F;
				} else {
					if (!smoothLighting) {
						final int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, pos);
						light0 = combinedLightUpMax;
						light1 = combinedLightUpMax;
						light2 = combinedLightUpMax;
						light3 = combinedLightUpMax;
					} else {
						light0 = this.getCombinedLightUpMax(worldIn, pos);
						//TODO: use less new objects
						light1 = this.getCombinedLightUpMax(worldIn, pos.south());
						light2 = this.getCombinedLightUpMax(worldIn, pos.east().south());
						light3 = this.getCombinedLightUpMax(worldIn, pos.east());
					}
					if (!colors) {
						red0 = red1 = red2 = red3 = red;
						green0 = green1 = green2 = green3 = green;
						blue0 = blue1 = blue2 = blue3 = blue;
					} else {
//						final int waterColor0 = BiomeColors.getWaterColor(worldIn, pos);
//						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
//						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
//						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						red0 = red;
						green0 = green;
						blue0 = blue;
						//TODO: use less new objects
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
				}
				wasAnythingRendered |= this.renderUp(
						buffer, atextureatlassprite,
						red0, green0, blue0,
						red1, green1, blue1,
						red2, green2, blue2,
						red3, green3, blue3,
						fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
						x, y, z,
						light0, light1, light2, light3,
						state.shouldRenderSides(worldIn, pos.up()), state.getFlow(worldIn, pos), MathHelper.getPositionRandom(pos)
				);
			}

			if (shouldRenderDown) {
				final int light0;
				final int light1;
				final int light2;
				final int light3;

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
					light0 = light1 = light2 = light3 = 0x00F000F0; // 240, 240
					red0 = red1 = red2 = red3 = 1.0F;
					green0 = green1 = green2 = green3 = 1.0F;
					blue0 = blue1 = blue2 = blue3 = 1.0F;
				} else {
					if (!smoothLighting) {
						final int downCombinedLightUpMax = this.getCombinedLightUpMax(worldIn, pos.down());
						light0 = downCombinedLightUpMax;
						light1 = downCombinedLightUpMax;
						light2 = downCombinedLightUpMax;
						light3 = downCombinedLightUpMax;
					} else {
						final BlockPos down = pos.down();
						light0 = this.getCombinedLightUpMax(worldIn, down);
						light1 = this.getCombinedLightUpMax(worldIn, down.south());
						light2 = this.getCombinedLightUpMax(worldIn, down.east().south());
						light3 = this.getCombinedLightUpMax(worldIn, down.east());
					}
					if (!colors) {
						red0 = red1 = red2 = red3 = red;
						green0 = green1 = green2 = green3 = green;
						blue0 = blue1 = blue2 = blue3 = blue;
					} else {
						final BlockPos down = pos.down();
						final BlockPos downSouth = down.south();
						final int waterColor0 = BiomeColors.getWaterColor(worldIn, downSouth);
						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						final int waterColor1 = BiomeColors.getWaterColor(worldIn, down);
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						final int waterColor2 = BiomeColors.getWaterColor(worldIn, down.east());
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						final int waterColor3 = BiomeColors.getWaterColor(worldIn, downSouth.east());
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}
				}
				wasAnythingRendered |= this.renderDown(
						light0, light1, light2, light3,
						buffer, atextureatlassprite[0],
						red0, green0, blue0,
						red1, green1, blue1,
						red2, green2, blue2,
						red3, green3, blue3,
						x, y, z
				);
			}

			for (int facingIndex = 0; facingIndex < 4; ++facingIndex) {
				final float y0;
				final float y1;
				final double x0;
				final double z0;
				final double x1;
				final double z1;
				final Direction enumfacing;
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
					enumfacing = Direction.NORTH;
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
					enumfacing = Direction.SOUTH;
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
					enumfacing = Direction.WEST;
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
					enumfacing = Direction.EAST;
					shouldRenderSide = shouldRenderEast;
				}

				if (shouldRenderSide && !func_209556_a(worldIn, pos, enumfacing, Math.max(y0, y1))) {
					final BlockPos offset = pos.offset(enumfacing);
					TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
					if (!isLava) {
						Block block = worldIn.getBlockState(offset).getBlock();
						if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
							textureatlassprite2 = fluidRenderer.atlasSpriteWaterOverlay;
						}
					}

					final int light0;
					final int light1;
					final int light2;
					final int light3;

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
						light0 = light1 = light2 = light3 = 0x00F000F0; // 240, 240
						red0 = red1 = red2 = red3 = 1.0F;
						green0 = green1 = green2 = green3 = 1.0F;
						blue0 = blue1 = blue2 = blue3 = 1.0F;
					} else {
						if (!smoothLighting) {
							final int combinedLightUpMax = this.getCombinedLightUpMax(worldIn, offset);
							light0 = combinedLightUpMax;
							light1 = combinedLightUpMax;
							light2 = combinedLightUpMax;
							light3 = combinedLightUpMax;
						} else {
							light0 = this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0));
							light1 = this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1));
							light2 = this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y, z1));
							light3 = this.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y, z0));
						}
						if (!colors) {
							red0 = red1 = red2 = red3 = red;
							green0 = green1 = green2 = green3 = green;
							blue0 = blue1 = blue2 = blue3 = blue;
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
					}
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
							light0, light1, light2, light3,
							textureatlassprite2 != this.atlasSpriteWaterOverlay
					);
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
	public FluidBlockRenderer getOldFluidRenderer() {
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
