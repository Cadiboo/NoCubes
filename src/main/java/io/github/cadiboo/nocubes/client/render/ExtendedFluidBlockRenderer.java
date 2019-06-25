package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.client.LazyPackedLightCache;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;

import javax.annotation.Nonnull;

import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.WEST;

/**
 * @author Cadiboo
 */
public final class ExtendedFluidBlockRenderer {

	public static boolean renderExtendedFluid(
			final int x, final int y, final int z,
			@Nonnull final BlockPos fluidPos,
			@Nonnull final IBlockAccess worldIn,
			@Nonnull final IBlockState state,
			@Nonnull final BufferBuilder buffer,
			//TODO: eventually do better lighting for 0.3.0
			@Nonnull final LazyPackedLightCache packedLightCache
	) {

		final SmoothLightingBlockFluidRenderer fluidRenderer = ClientProxy.fluidRenderer;

		final int fluidX = fluidPos.getX();
		final int fluidY = fluidPos.getY();
		final int fluidZ = fluidPos.getZ();

		PooledMutableBlockPos renderPos = PooledMutableBlockPos.retain(x, y, z);
		OptiFineCompatibility.pushShaderThing(state, renderPos, worldIn, buffer);
		PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {
			BlockLiquid blockLiquid = (BlockLiquid) state.getBlock();
			final Material material = state.getMaterial();
			final boolean isLava = material == Material.LAVA;
			final TextureAtlasSprite[] atextureatlassprite = isLava ? fluidRenderer.atlasSpritesLava : fluidRenderer.atlasSpritesWater;

			final float red;
			final float green;
			final float blue;
			if (isLava) {
				red = 1.0F;
				green = 1.0F;
				blue = 1.0F;
			} else {
				final int waterColor = BiomeColorHelper.getWaterColorAtPos(worldIn, renderPos);
				red = (float) (waterColor >> 16 & 0xFF) / 255.0F;
				green = (float) (waterColor >> 8 & 0xFF) / 255.0F;
				blue = (float) (waterColor & 0xFF) / 255.0F;
			}

			boolean shouldRenderDown = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.DOWN);
			shouldRenderDown &= !(Config.renderSmoothTerrain && worldIn.getBlockState(pooledMutableBlockPos.setPos(x, y - 1, z)).nocubes_isTerrainSmoothable());
			boolean shouldRenderUp = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.UP);
			shouldRenderUp &= !(Config.renderSmoothTerrain && worldIn.getBlockState(pooledMutableBlockPos.setPos(x, y + 1, z)).nocubes_isTerrainSmoothable());
			boolean shouldRenderNorth = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.NORTH);
			shouldRenderNorth &= !(Config.renderSmoothTerrain && worldIn.getBlockState(pooledMutableBlockPos.setPos(x, y, z - 1)).nocubes_isTerrainSmoothable());
			boolean shouldRenderSouth = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.SOUTH);
			shouldRenderSouth &= !(Config.renderSmoothTerrain && worldIn.getBlockState(pooledMutableBlockPos.setPos(x, y, z + 1)).nocubes_isTerrainSmoothable());
			boolean shouldRenderWest = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.WEST);
			shouldRenderWest &= !(Config.renderSmoothTerrain && worldIn.getBlockState(pooledMutableBlockPos.setPos(x - 1, y, z)).nocubes_isTerrainSmoothable());
			boolean shouldRenderEast = state.shouldSideBeRendered(worldIn, renderPos, EnumFacing.EAST);
			shouldRenderEast &= !(Config.renderSmoothTerrain && worldIn.getBlockState(pooledMutableBlockPos.setPos(x + 1, y, z)).nocubes_isTerrainSmoothable());

			if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
				return false;
			}

			boolean wasAnythingRendered = false;

			final float fluidHeight = fluidRenderer.getFluidHeight(worldIn, material, fluidX, fluidY, fluidZ, pooledMutableBlockPos);
			final float fluidHeightSouth = fluidRenderer.getFluidHeight(worldIn, material, fluidX, fluidY, fluidZ + 1, pooledMutableBlockPos);
			final float fluidHeightEastSouth = fluidRenderer.getFluidHeight(worldIn, material, fluidX + 1, fluidY, fluidZ + 1, pooledMutableBlockPos);
			final float fluidHeightEast = fluidRenderer.getFluidHeight(worldIn, material, fluidX + 1, fluidY, fluidZ, pooledMutableBlockPos);

//			final double x = (double) pos.getX();
//			final double y = (double) pos.getY();
//			final double z = (double) pos.getZ();

			final boolean smoothLighting = fluidRenderer.smoothLighting();
			final boolean colors = fluidRenderer.colors();

			if (shouldRenderUp) {

				// Commented out to fix transparent lines between bottom of sides.
				// The only reason that I can think of for fluidRenderer.code to exist in the first place
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
						final int combinedLightUpMax = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, y, z));
						light0 = combinedLightUpMax;
						light1 = combinedLightUpMax;
						light2 = combinedLightUpMax;
						light3 = combinedLightUpMax;
					} else {
						light0 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, y, z));
						// south
						light1 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, y, z + 1));
						// east south
						light2 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, y, z + 1));
						// east
						light3 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, y, z));
					}
					if (!colors) {
						red0 = red1 = red2 = red3 = red;
						green0 = green1 = green2 = green3 = green;
						blue0 = blue1 = blue2 = blue3 = blue;
					} else {
//						final int waterColor0 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x, y, z));
//						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
//						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
//						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						red0 = red;
						green0 = green;
						blue0 = blue;
						// south
						final int waterColor1 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x, y, z + 1));
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						// east south
						final int waterColor2 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x + 1, y, z + 1));
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						// east
						final int waterColor3 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x + 1, y, z));
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}
				}
				wasAnythingRendered |= fluidRenderer.renderUp(
						buffer, atextureatlassprite,
						red0, green0, blue0,
						red1, green1, blue1,
						red2, green2, blue2,
						red3, green3, blue3,
						fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
						x, y, z,
						light0, light1, light2, light3,
						blockLiquid.shouldRenderSides(worldIn, pooledMutableBlockPos.setPos(x, y + 1, z)), blockLiquid.getFlow(worldIn, fluidPos, state), MathHelper.getCoordinateRandom(x, y, z)
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
					final int ym1 = y - 1;
					if (!smoothLighting) {
						final int downCombinedLightUpMax = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z));
						light0 = downCombinedLightUpMax;
						light1 = downCombinedLightUpMax;
						light2 = downCombinedLightUpMax;
						light3 = downCombinedLightUpMax;
					} else {
						// down south
						light0 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z + 1));
						// down
						light1 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z));
						// down east
						light2 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, ym1, z));
						// down east south
						light3 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, ym1, z + 1));
					}
					if (!colors) {
						red0 = red1 = red2 = red3 = red;
						green0 = green1 = green2 = green3 = green;
						blue0 = blue1 = blue2 = blue3 = blue;
					} else {
						// down south
						final int waterColor0 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x, ym1, z + 1));
						red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
						green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
						blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
						// down
						final int waterColor1 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x, ym1, z));
						red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
						green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
						blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
						// down east
						final int waterColor2 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x + 1, ym1, z));
						red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
						green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
						blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
						// down east south
						final int waterColor3 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x + 1, ym1, z + 1));
						red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
						green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
						blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
					}
				}
				wasAnythingRendered |= fluidRenderer.renderDown(
						light0, light1, light2, light3,
						buffer, atextureatlassprite[0],
						red0, green0, blue0,
						red1, green1, blue1,
						red2, green2, blue2,
						red3, green3, blue3,
						x, y, z
				);
			}

			final TextureAtlasSprite atlasSpriteWaterOverlay = fluidRenderer.atlasSpriteWaterOverlay;

			for (int facingIndex = 0; facingIndex < 4; ++facingIndex) {
				final float y0;
				final float y1;
				final double x0;
				final double z0;
				final double x1;
				final double z1;
				final EnumFacing direction;
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
					direction = NORTH;
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
					direction = SOUTH;
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
					direction = WEST;
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
					direction = EAST;
					shouldRenderSide = shouldRenderEast;
				}

				pooledMutableBlockPos.setPos(x, y, z).move(direction);
				if (shouldRenderSide) {
					TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
					if (!isLava) {
						IBlockState blockstate = worldIn.getBlockState(pooledMutableBlockPos);
						if (blockstate.getBlockFaceShape(worldIn, pooledMutableBlockPos, direction) == BlockFaceShape.SOLID) {
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
							final int combinedLightUpMax = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos);
							light0 = combinedLightUpMax;
							light1 = combinedLightUpMax;
							light2 = combinedLightUpMax;
							light3 = combinedLightUpMax;
						} else {
							light0 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0));
							light1 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1));
							light2 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x1, y, z1));
							light3 = fluidRenderer.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x0, y, z0));
						}
						if (!colors) {
							red0 = red1 = red2 = red3 = red;
							green0 = green1 = green2 = green3 = green;
							blue0 = blue1 = blue2 = blue3 = blue;
						} else {
							final int waterColor0 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0));
							red0 = (float) (waterColor0 >> 16 & 0xFF) / 255.0F;
							green0 = (float) (waterColor0 >> 8 & 0xFF) / 255.0F;
							blue0 = (float) (waterColor0 & 0xFF) / 255.0F;
							final int waterColor1 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1));
							red1 = (float) (waterColor1 >> 16 & 0xFF) / 255.0F;
							green1 = (float) (waterColor1 >> 8 & 0xFF) / 255.0F;
							blue1 = (float) (waterColor1 & 0xFF) / 255.0F;
							final int waterColor2 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x1, y, z1));
							red2 = (float) (waterColor2 >> 16 & 0xFF) / 255.0F;
							green2 = (float) (waterColor2 >> 8 & 0xFF) / 255.0F;
							blue2 = (float) (waterColor2 & 0xFF) / 255.0F;
							final int waterColor3 = BiomeColorHelper.getWaterColorAtPos(worldIn, pooledMutableBlockPos.setPos(x0, y, z0));
							red3 = (float) (waterColor3 >> 16 & 0xFF) / 255.0F;
							green3 = (float) (waterColor3 >> 8 & 0xFF) / 255.0F;
							blue3 = (float) (waterColor3 & 0xFF) / 255.0F;
						}
					}
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
							light0, light1, light2, light3,
							textureatlassprite2 != atlasSpriteWaterOverlay
					);
				}
			}

			return wasAnythingRendered;
		} finally {
			pooledMutableBlockPos.release();
			OptiFineCompatibility.popShaderThing(buffer);
			renderPos.release();
		}
	}

}
