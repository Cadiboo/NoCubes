package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.client.renderer.BlockRendererDispatcher;
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
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.BiomeColors;

import javax.annotation.Nonnull;

import static net.minecraft.util.Direction.DOWN;
import static net.minecraft.util.Direction.EAST;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Direction.UP;
import static net.minecraft.util.Direction.WEST;

/**
 * @author Cadiboo
 */
public final class ExtendedFluidBlockRenderer {

	public static boolean renderExtendedFluid(
			final int renderX, final int renderY, final int renderZ,
			@Nonnull final BlockPos fluidPos,
			@Nonnull final IWorldReader worldIn,
			@Nonnull final IFluidState state,
			@Nonnull final BufferBuilder buffer,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
			//TODO: eventually do better lighting for 0.3.0
			@Nonnull final LazyPackedLightCache packedLightCache
	) {

		try (
				PooledMutableBlockPos renderPos = PooledMutableBlockPos.retain(renderX, renderY, renderZ);
				PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()
		) {
			OptiFineCompatibility.pushShaderThing(state, renderPos, worldIn, buffer);

			final SmoothLightingFluidBlockRenderer smoothLightingFluidBlockRenderer = ClientProxy.smoothLightingBlockFluidRenderer;

			final Fluid fluid = state.getFluid();

			final IFluidState upFluidState = worldIn.getFluidState(pooledMutableBlockPos.setPos(renderX, renderY + 1, renderZ));
			final IFluidState downFluidState = worldIn.getFluidState(pooledMutableBlockPos.setPos(renderX, renderY - 1, renderZ));
			final boolean func_209556_a_hardcoded_result = smoothLightingFluidBlockRenderer.func_209556_a_optimised_hardcoded(worldIn, DOWN, pooledMutableBlockPos);
			final IFluidState northFluidState = worldIn.getFluidState(pooledMutableBlockPos.setPos(renderX, renderY, renderZ - 1));
			final IFluidState southFluidState = worldIn.getFluidState(pooledMutableBlockPos.setPos(renderX, renderY, renderZ + 1));
			final IFluidState westFluidState = worldIn.getFluidState(pooledMutableBlockPos.setPos(renderX - 1, renderY, renderZ));
			final IFluidState eastFluidState = worldIn.getFluidState(pooledMutableBlockPos.setPos(renderX + 1, renderY, renderZ));

			final boolean shouldRenderUp = !upFluidState.getFluid().isEquivalentTo(fluid);
			final boolean shouldRenderDown = !downFluidState.getFluid().isEquivalentTo(fluid) && !func_209556_a_hardcoded_result;
			final boolean shouldRenderNorth = !northFluidState.getFluid().isEquivalentTo(fluid);
			final boolean shouldRenderSouth = !southFluidState.getFluid().isEquivalentTo(fluid);
			final boolean shouldRenderWest = !westFluidState.getFluid().isEquivalentTo(fluid);
			final boolean shouldRenderEast = !eastFluidState.getFluid().isEquivalentTo(fluid);

			if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
				return false;
			}

			final FluidBlockRenderer fluidRenderer = blockRendererDispatcher.fluidRenderer;
			// Use fluidRenderer sprites instead of smoothLightingFluidBlockRenderer sprites for compatibility
			final TextureAtlasSprite atlasSpriteWaterOverlay = fluidRenderer.atlasSpriteWaterOverlay;
			final TextureAtlasSprite[] atlasSpritesLava = fluidRenderer.atlasSpritesLava;
			final TextureAtlasSprite[] atlasSpritesWater = fluidRenderer.atlasSpritesWater;

			final boolean isLava = state.isTagged(FluidTags.LAVA);
			final TextureAtlasSprite[] atextureatlassprite = isLava ? atlasSpritesLava : atlasSpritesWater;

			final float red;
			final float green;
			final float blue;
			if (isLava) {
				red = 1.0F;
				green = 1.0F;
				blue = 1.0F;
			} else {
				final int waterColor = BiomeColors.getWaterColor(worldIn, renderPos);
				red = (float) (waterColor >> 16 & 0xFF) / 255.0F;
				green = (float) (waterColor >> 8 & 0xFF) / 255.0F;
				blue = (float) (waterColor & 0xFF) / 255.0F;
			}

			boolean wasAnythingRendered = false;

			final int fluidX = fluidPos.getX();
			final int fluidY = fluidPos.getY();
			final int fluidZ = fluidPos.getZ();

			final float fluidHeight = smoothLightingFluidBlockRenderer.getFluidHeight(worldIn, fluid, fluidX, fluidY, fluidZ, pooledMutableBlockPos);
			final float fluidHeightSouth = smoothLightingFluidBlockRenderer.getFluidHeight(worldIn, fluid, fluidX, fluidY, fluidZ + 1, pooledMutableBlockPos);
			final float fluidHeightEastSouth = smoothLightingFluidBlockRenderer.getFluidHeight(worldIn, fluid, fluidX + 1, fluidY, fluidZ + 1, pooledMutableBlockPos);
			final float fluidHeightEast = smoothLightingFluidBlockRenderer.getFluidHeight(worldIn, fluid, fluidX + 1, fluidY, fluidZ, pooledMutableBlockPos);

			final boolean smoothLighting = smoothLightingFluidBlockRenderer.smoothLighting();
			final boolean colors = smoothLightingFluidBlockRenderer.colors();

			if (shouldRenderUp && !smoothLightingFluidBlockRenderer.func_209556_a_optimised(worldIn, UP, Math.min(Math.min(fluidHeight, fluidHeightSouth), Math.min(fluidHeightEastSouth, fluidHeightEast)), pooledMutableBlockPos.setPos(renderX, renderY + 1, renderZ))) {

				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for this code to exist in the first place
				// is to try and solve renderZ-fighting issues.
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
						final int combinedLightUpMax = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX, renderY, renderZ));
						light0 = combinedLightUpMax;
						light1 = combinedLightUpMax;
						light2 = combinedLightUpMax;
						light3 = combinedLightUpMax;
					} else {
						light0 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX, renderY, renderZ));
						// south
						light1 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX, renderY, renderZ + 1));
						// east south
						light2 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX + 1, renderY, renderZ + 1));
						// east
						light3 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX + 1, renderY, renderZ));
					}
					if (!colors) {
						red0 = red1 = red2 = red3 = red;
						green0 = green1 = green2 = green3 = green;
						blue0 = blue1 = blue2 = blue3 = blue;
					} else {
//						final int waterColor0 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(renderX, renderY, renderZ));
//						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
//						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
//						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						red0 = red;
						green0 = green;
						blue0 = blue;
						// south
						final int waterColor1 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(renderX, renderY, renderZ + 1));
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						// east south
						final int waterColor2 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(renderX + 1, renderY, renderZ + 1));
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						// east
						final int waterColor3 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(renderX + 1, renderY, renderZ));
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}
				}
				wasAnythingRendered |= smoothLightingFluidBlockRenderer.renderUp(
						buffer, atextureatlassprite,
						red0, green0, blue0,
						red1, green1, blue1,
						red2, green2, blue2,
						red3, green3, blue3,
						fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
						renderX, renderY, renderZ,
						light0, light1, light2, light3,
						state.shouldRenderSides(worldIn, pooledMutableBlockPos.setPos(renderX, renderY + 1, renderZ)), state.getFlow(worldIn, renderPos), MathHelper.getCoordinateRandom(renderX, renderY, renderZ)
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
					final int ym1 = renderY - 1;
					if (!smoothLighting) {
						final int downCombinedLightUpMax = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX, ym1, renderZ));
						light0 = downCombinedLightUpMax;
						light1 = downCombinedLightUpMax;
						light2 = downCombinedLightUpMax;
						light3 = downCombinedLightUpMax;
					} else {
						// down south
						light0 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX, ym1, renderZ + 1));
						// down
						light1 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX, ym1, renderZ));
						// down east
						light2 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX + 1, ym1, renderZ));
						// down east south
						light3 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(renderX + 1, ym1, renderZ + 1));
					}
					if (!colors) {
						red0 = red1 = red2 = red3 = red;
						green0 = green1 = green2 = green3 = green;
						blue0 = blue1 = blue2 = blue3 = blue;
					} else {
						// down south
						final int waterColor0 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(renderX, ym1, renderZ + 1));
						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						// down
						final int waterColor1 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(renderX, ym1, renderZ));
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						// down east
						final int waterColor2 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(renderX + 1, ym1, renderZ));
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						// down east south
						final int waterColor3 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(renderX + 1, ym1, renderZ + 1));
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}
				}
				wasAnythingRendered |= smoothLightingFluidBlockRenderer.renderDown(
						light0, light1, light2, light3,
						buffer, atextureatlassprite[0],
						red0, green0, blue0,
						red1, green1, blue1,
						red2, green2, blue2,
						red3, green3, blue3,
						renderX, renderY, renderZ
				);
			}

			for (int facingIndex = 0; facingIndex < 4; ++facingIndex) {
				final float y0;
				final float y1;
				final double x0;
				final double z0;
				final double x1;
				final double z1;
				final Direction direction;
				final boolean shouldRenderSide;
				if (facingIndex == 0) {
					y0 = fluidHeight;
					y1 = fluidHeightEast;
					x0 = renderX;
					x1 = renderX + 1.0D;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve renderZ-fighting issues.
					z0 = renderZ;// + (double) 0.001F;
					z1 = renderZ;// + (double) 0.001F;
					direction = NORTH;
					shouldRenderSide = shouldRenderNorth;
				} else if (facingIndex == 1) {
					y0 = fluidHeightEastSouth;
					y1 = fluidHeightSouth;
					x0 = renderX + 1.0D;
					x1 = renderX;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve renderZ-fighting issues.
					z0 = renderZ + 1.0D;// - (double) 0.001F;
					z1 = renderZ + 1.0D;// - (double) 0.001F;
					direction = SOUTH;
					shouldRenderSide = shouldRenderSouth;
				} else if (facingIndex == 2) {
					y0 = fluidHeightSouth;
					y1 = fluidHeight;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve renderZ-fighting issues.
					x0 = renderX;// + (double) 0.001F;
					x1 = renderX;// + (double) 0.001F;
					z0 = renderZ + 1.0D;
					z1 = renderZ;
					direction = WEST;
					shouldRenderSide = shouldRenderWest;
				} else {
					y0 = fluidHeightEast;
					y1 = fluidHeightEastSouth;
					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve renderZ-fighting issues.
					x0 = renderX + 1.0D;// - (double) 0.001F;
					x1 = renderX + 1.0D;// - (double) 0.001F;
					z0 = renderZ;
					z1 = renderZ + 1.0D;
					direction = EAST;
					shouldRenderSide = shouldRenderEast;
				}

				pooledMutableBlockPos.setPos(renderX, renderY, renderZ).move(direction);
				if (shouldRenderSide && !smoothLightingFluidBlockRenderer.func_209556_a_optimised(worldIn, direction, Math.max(y0, y1), pooledMutableBlockPos)) {
					TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
					if (!isLava) {
						Block block = worldIn.getBlockState(pooledMutableBlockPos).getBlock();
						if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
							textureatlassprite2 = atlasSpriteWaterOverlay;
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
							final int combinedLightUpMax = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos);
							light0 = combinedLightUpMax;
							light1 = combinedLightUpMax;
							light2 = combinedLightUpMax;
							light3 = combinedLightUpMax;
						} else {
							light0 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x0, renderY + y0, z0));
							light1 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x1, renderY + y1, z1));
							light2 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x1, renderY, z1));
							light3 = smoothLightingFluidBlockRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x0, renderY, z0));
						}
						if (!colors) {
							red0 = red1 = red2 = red3 = red;
							green0 = green1 = green2 = green3 = green;
							blue0 = blue1 = blue2 = blue3 = blue;
						} else {
							final int waterColor0 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x0, renderY + y0, z0));
							red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
							green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
							blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
							final int waterColor1 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x1, renderY + y1, z1));
							red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
							green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
							blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
							final int waterColor2 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x1, renderY, z1));
							red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
							green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
							blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
							final int waterColor3 = BiomeColors.getWaterColor(worldIn, pooledMutableBlockPos.setPos(x0, renderY, z0));
							red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
							green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
							blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
						}
					}
					wasAnythingRendered = smoothLightingFluidBlockRenderer.renderSide(
							buffer, textureatlassprite2,
							red0, green0, blue0,
							red1, green1, blue1,
							red2, green2, blue2,
							red3, green3, blue3,
							facingIndex,
							renderY, y0, y1,
							x0, x1,
							z0, z1,
							light0, light1, light2, light3,
							textureatlassprite2 != atlasSpriteWaterOverlay
					);
				}
			}

			return wasAnythingRendered;
		} finally {
			OptiFineCompatibility.popShaderThing(buffer);
		}
	}

}
