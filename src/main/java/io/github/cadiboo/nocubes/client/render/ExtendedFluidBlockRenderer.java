package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.client.renderer.BufferBuilder;
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

import static net.minecraft.client.renderer.FluidBlockRenderer.func_209556_a;
import static net.minecraft.client.renderer.FluidBlockRenderer.isAdjacentFluidSameAs;

/**
 * @author Cadiboo
 */
public final class ExtendedFluidBlockRenderer {

	public static boolean renderExtendedFluid(
			final double x, final double y, final double z,
			@Nonnull final BlockPos fluidPos,
			@Nonnull final IWorldReader worldIn,
			@Nonnull final IFluidState state,
			@Nonnull final BufferBuilder buffer,
			//TODO: eventually do better lighting for 0.3.0
			@Nonnull final LazyPackedLightCache packedLightCache
	) {

		final SmoothLightingBlockFluidRenderer fluidRenderer = ClientProxy.fluidRenderer;

		try (PooledMutableBlockPos renderPos = PooledMutableBlockPos.retain(x, y, z)) {
			OptiFineCompatibility.pushShaderThing(state, renderPos, worldIn, buffer);
			try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
				final boolean isLava = state.isTagged(FluidTags.LAVA);
				final TextureAtlasSprite[] atextureatlassprite = isLava ? fluidRenderer.atlasSpritesLava : fluidRenderer.atlasSpritesWater;

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

				final boolean shouldRenderUp = !isAdjacentFluidSameAs(worldIn, renderPos, Direction.UP, state);
				final boolean shouldRenderDown = !isAdjacentFluidSameAs(worldIn, renderPos, Direction.DOWN, state) && !func_209556_a(worldIn, renderPos, Direction.DOWN, 0.8888889F);
				final boolean shouldRenderNorth = !isAdjacentFluidSameAs(worldIn, renderPos, Direction.NORTH, state);
				final boolean shouldRenderSouth = !isAdjacentFluidSameAs(worldIn, renderPos, Direction.SOUTH, state);
				final boolean shouldRenderWest = !isAdjacentFluidSameAs(worldIn, renderPos, Direction.WEST, state);
				final boolean shouldRenderEast = !isAdjacentFluidSameAs(worldIn, renderPos, Direction.EAST, state);

				if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
					return false;
				}

				boolean wasAnythingRendered = false;
				final Fluid fluid = state.getFluid();
				float fluidHeight = fluidRenderer.getFluidHeight(worldIn, fluidPos, fluid);
				float fluidHeightSouth = fluidRenderer.getFluidHeight(worldIn, fluidPos.south(), fluid);
				float fluidHeightEastSouth = fluidRenderer.getFluidHeight(worldIn, fluidPos.east().south(), fluid);
				float fluidHeightEast = fluidRenderer.getFluidHeight(worldIn, fluidPos.east(), fluid);

//				final double x = (double) pos.getX();
//				final double y = (double) pos.getY();
//				final double z = (double) pos.getZ();

				if (shouldRenderUp && !func_209556_a(worldIn, renderPos, Direction.UP, Math.min(Math.min(fluidHeight, fluidHeightSouth), Math.min(fluidHeightEastSouth, fluidHeightEast)))) {

					// Commented out to fix transparent lines between bottom of sides.
					// The only reason that I can think of for this code to exist in the first place
					// is to try and solve z-fighting issues.
//					fluidHeight -= 0.001F;
//					fluidHeightSouth -= 0.001F;
//					fluidHeightEastSouth -= 0.001F;
//					fluidHeightEast -= 0.001F;

					if (!fluidRenderer.colors()) {
						if (!fluidRenderer.smoothLighting()) {
							final int combinedLightUpMax = fluidRenderer.getCombinedLightUpMax(worldIn, renderPos);
							wasAnythingRendered |= fluidRenderer.renderUp(
									buffer, atextureatlassprite,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
									x, y, z,
									combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
									state.shouldRenderSides(worldIn, renderPos.up()), state.getFlow(worldIn, fluidPos), MathHelper.getPositionRandom(renderPos)
							);
						} else {
							wasAnythingRendered |= fluidRenderer.renderUp(
									buffer, atextureatlassprite,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
									x, y, z,
									fluidRenderer.getCombinedLightUpMax(worldIn, renderPos), fluidRenderer.getCombinedLightUpMax(worldIn, renderPos.south()), fluidRenderer.getCombinedLightUpMax(worldIn, renderPos.east().south()), fluidRenderer.getCombinedLightUpMax(worldIn, renderPos.east()),
									state.shouldRenderSides(worldIn, renderPos.up()), state.getFlow(worldIn, fluidPos), MathHelper.getPositionRandom(renderPos)
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
//							final int waterColor0 = BiomeColors.getWaterColor(worldIn, renderPos);
//							red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
//							green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
//							blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
							red0 = red;
							green0 = green;
							blue0 = blue;
							final int waterColor1 = BiomeColors.getWaterColor(worldIn, renderPos.south());
							red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
							green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
							blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
							final int waterColor2 = BiomeColors.getWaterColor(worldIn, renderPos.east().south());
							red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
							green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
							blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
							final int waterColor3 = BiomeColors.getWaterColor(worldIn, renderPos.east());
							red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
							green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
							blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
						}

						if (!fluidRenderer.smoothLighting()) {
							final int combinedLightUpMax = fluidRenderer.getCombinedLightUpMax(worldIn, renderPos);
							wasAnythingRendered |= fluidRenderer.renderUp(
									buffer, atextureatlassprite,
									red0, green0, blue0,
									red1, green1, blue1,
									red2, green2, blue2,
									red3, green3, blue3,
									fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
									x, y, z,
									combinedLightUpMax, combinedLightUpMax, combinedLightUpMax, combinedLightUpMax,
									state.shouldRenderSides(worldIn, renderPos.up()), state.getFlow(worldIn, fluidPos), MathHelper.getPositionRandom(renderPos)
							);
						} else {
							wasAnythingRendered |= fluidRenderer.renderUp(
									buffer, atextureatlassprite,
									red0, green0, blue0,
									red1, green1, blue1,
									red2, green2, blue2,
									red3, green3, blue3,
									fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
									x, y, z,
									fluidRenderer.getCombinedLightUpMax(worldIn, renderPos), fluidRenderer.getCombinedLightUpMax(worldIn, renderPos.south()), fluidRenderer.getCombinedLightUpMax(worldIn, renderPos.east().south()), fluidRenderer.getCombinedLightUpMax(worldIn, renderPos.east()),
									state.shouldRenderSides(worldIn, renderPos.up()), state.getFlow(worldIn, fluidPos), MathHelper.getPositionRandom(renderPos)
							);
						}
					}
				}

				if (shouldRenderDown) {
					if (!fluidRenderer.colors()) {
						if (!fluidRenderer.smoothLighting()) {
							final int downCombinedLightUpMax = fluidRenderer.getCombinedLightUpMax(worldIn, renderPos.down());
							wasAnythingRendered |= fluidRenderer.renderDown(
									downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax,
									buffer, atextureatlassprite[0],
									red, green, blue,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									x, y, z
							);
						} else {
							final BlockPos down = renderPos.down();
							wasAnythingRendered |= fluidRenderer.renderDown(
									fluidRenderer.getCombinedLightUpMax(worldIn, down), fluidRenderer.getCombinedLightUpMax(worldIn, down.south()), fluidRenderer.getCombinedLightUpMax(worldIn, down.east().south()), fluidRenderer.getCombinedLightUpMax(worldIn, down.east()),
									buffer, atextureatlassprite[0],
									red, green, blue,
									red, green, blue,
									red, green, blue,
									red, green, blue,
									x, y, z
							);
						}
					} else {

						// I've fucked up somehow. I shouldn't need to offset this south
						final BlockPos down = renderPos.down().south();

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
							// I've fucked up somehow. I shouldn't need to offset this north, it should be south
							final int waterColor1 = BiomeColors.getWaterColor(worldIn, down.north());
							red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
							green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
							blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
							// I've fucked up somehow. I shouldn't need to offset this north, it should be south
							final int waterColor2 = BiomeColors.getWaterColor(worldIn, down.east().north());
							red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
							green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
							blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
							final int waterColor3 = BiomeColors.getWaterColor(worldIn, down.east());
							red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
							green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
							blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
						}

						if (!fluidRenderer.smoothLighting()) {
							final int downCombinedLightUpMax = fluidRenderer.getCombinedLightUpMax(worldIn, down);
							wasAnythingRendered |= fluidRenderer.renderDown(
									downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax, downCombinedLightUpMax,
									buffer, atextureatlassprite[0],
									red0, green0, blue0,
									red1, green1, blue1,
									red2, green2, blue2,
									red3, green3, blue3,
									x, y, z
							);
						} else {
							wasAnythingRendered |= fluidRenderer.renderDown(
									fluidRenderer.getCombinedLightUpMax(worldIn, down), fluidRenderer.getCombinedLightUpMax(worldIn, down.south()), fluidRenderer.getCombinedLightUpMax(worldIn, down.east().south()), fluidRenderer.getCombinedLightUpMax(worldIn, down.east()),
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

					if (shouldRenderSide && !func_209556_a(worldIn, renderPos, enumfacing, Math.max(y0, y1))) {
						final BlockPos offset = renderPos.offset(enumfacing);
						TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
						if (!isLava) {
							Block block = worldIn.getBlockState(offset).getBlock();
							if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
								textureatlassprite2 = fluidRenderer.atlasSpriteWaterOverlay;
							}
						}

						if (!fluidRenderer.colors()) {
							if (!fluidRenderer.smoothLighting()) {
								final int combinedLightUpMax = fluidRenderer.getCombinedLightUpMax(worldIn, offset);
								wasAnythingRendered = fluidRenderer.renderSide(
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
										textureatlassprite2 != fluidRenderer.atlasSpriteWaterOverlay
								);
							} else {
								wasAnythingRendered = fluidRenderer.renderSide(
										buffer, textureatlassprite2,
										red, green, blue,
										red, green, blue,
										red, green, blue,
										red, green, blue,
										facingIndex,
										y, y0, y1,
										x0, x1,
										z0, z1,
										fluidRenderer.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0)),
										fluidRenderer.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1)),
										fluidRenderer.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y, z1)),
										fluidRenderer.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y, z0)),
										textureatlassprite2 != fluidRenderer.atlasSpriteWaterOverlay
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

							if (!fluidRenderer.smoothLighting()) {
								final int combinedLightUpMax = fluidRenderer.getCombinedLightUpMax(worldIn, offset);
								wasAnythingRendered = fluidRenderer.renderSide(
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
										textureatlassprite2 != fluidRenderer.atlasSpriteWaterOverlay
								);
							} else {
								wasAnythingRendered = fluidRenderer.renderSide(
										buffer, textureatlassprite2,
										red0, green0, blue0,
										red1, green1, blue1,
										red2, green2, blue2,
										red3, green3, blue3,
										facingIndex,
										y, y0, y1,
										x0, x1,
										z0, z1,
										fluidRenderer.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0)),
										fluidRenderer.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1)),
										fluidRenderer.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x1, y, z1)),
										fluidRenderer.getCombinedLightUpMax(worldIn, pooledMutableBlockPos.setPos(x0, y, z0)),
										textureatlassprite2 != fluidRenderer.atlasSpriteWaterOverlay
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
	}

}
