package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.UVHelper;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockFluidRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.biome.BiomeColorHelper;

import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN;
import static net.minecraft.util.EnumFacing.EAST;
import static net.minecraft.util.EnumFacing.NORTH;
import static net.minecraft.util.EnumFacing.SOUTH;
import static net.minecraft.util.EnumFacing.WEST;

/**
 * @author Cadiboo
 */
public final class SmoothLightingFluidBlockRenderer extends BlockFluidRenderer {

	public SmoothLightingFluidBlockRenderer() {
		super(Minecraft.getMinecraft().getBlockRendererDispatcher().fluidRenderer.blockColors);
	}

	@Override
	public boolean renderFluid(final IBlockAccess worldIn, final IBlockState state, final BlockPos pos, final BufferBuilder buffer) {
		OptiFineCompatibility.PROXY.pushShaderThing(state, pos, worldIn, buffer);
		PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain();
		try {

			final int x = pos.getX();
			final int y = pos.getY();
			final int z = pos.getZ();

			final BlockLiquid blockLiquid = (BlockLiquid) state.getBlock();
			final Material material = state.getMaterial();
			final boolean isLava = material == Material.LAVA;
			final TextureAtlasSprite[] atextureatlassprite = isLava ? this.atlasSpritesLava : this.atlasSpritesWater;

			final float red;
			final float green;
			final float blue;
			if (isLava) {
				red = 1.0F;
				green = 1.0F;
				blue = 1.0F;
			} else {
				final int waterColor = BiomeColorHelper.getWaterColorAtPos(worldIn, pos);
				red = (float) (waterColor >> 16 & 0xFF) / 255.0F;
				green = (float) (waterColor >> 8 & 0xFF) / 255.0F;
				blue = (float) (waterColor & 0xFF) / 255.0F;
			}

			boolean shouldRenderDown = state.shouldSideBeRendered(worldIn, pos, EnumFacing.DOWN);
			shouldRenderDown &= !(Config.renderSmoothTerrain && TERRAIN.test(worldIn.getBlockState(pooledMutableBlockPos.setPos(x, y - 1, z))));
			boolean shouldRenderUp = state.shouldSideBeRendered(worldIn, pos, EnumFacing.UP);
			shouldRenderUp &= !(Config.renderSmoothTerrain && TERRAIN.test(worldIn.getBlockState(pooledMutableBlockPos.setPos(x, y + 1, z))));
			boolean shouldRenderNorth = state.shouldSideBeRendered(worldIn, pos, EnumFacing.NORTH);
			shouldRenderNorth &= !(Config.renderSmoothTerrain && TERRAIN.test(worldIn.getBlockState(pooledMutableBlockPos.setPos(x, y, z - 1))));
			boolean shouldRenderSouth = state.shouldSideBeRendered(worldIn, pos, EnumFacing.SOUTH);
			shouldRenderSouth &= !(Config.renderSmoothTerrain && TERRAIN.test(worldIn.getBlockState(pooledMutableBlockPos.setPos(x, y, z + 1))));
			boolean shouldRenderWest = state.shouldSideBeRendered(worldIn, pos, EnumFacing.WEST);
			shouldRenderWest &= !(Config.renderSmoothTerrain && TERRAIN.test(worldIn.getBlockState(pooledMutableBlockPos.setPos(x - 1, y, z))));
			boolean shouldRenderEast = state.shouldSideBeRendered(worldIn, pos, EnumFacing.EAST);
			shouldRenderEast &= !(Config.renderSmoothTerrain && TERRAIN.test(worldIn.getBlockState(pooledMutableBlockPos.setPos(x + 1, y, z))));

			if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
				return false;
			}

			boolean wasAnythingRendered = false;

//			float fluidHeight = this.getFluidHeight(worldIn, pos, material);
//			float fluidHeightSouth = this.getFluidHeight(worldIn, pos.south(), material);
//			float fluidHeightEastSouth = this.getFluidHeight(worldIn, pos.east().south(), material);
//			float fluidHeightEast = this.getFluidHeight(worldIn, pos.east(), material);

			final float fluidHeight = this.getFluidHeight(worldIn, material, x, y, z, pooledMutableBlockPos);
			final float fluidHeightSouth = this.getFluidHeight(worldIn, material, x, y, z + 1, pooledMutableBlockPos);
			final float fluidHeightEastSouth = this.getFluidHeight(worldIn, material, x + 1, y, z + 1, pooledMutableBlockPos);
			final float fluidHeightEast = this.getFluidHeight(worldIn, material, x + 1, y, z, pooledMutableBlockPos);

//			final double x = (double) pos.getX();
//			final double y = (double) pos.getY();
//			final double z = (double) pos.getZ();

			final boolean smoothLighting = this.smoothLighting();
			final boolean colors = this.colors();

			if (shouldRenderUp) {

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
						final int combinedLightUpMax = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, y, z));
						light0 = combinedLightUpMax;
						light1 = combinedLightUpMax;
						light2 = combinedLightUpMax;
						light3 = combinedLightUpMax;
					} else {
						light0 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, y, z));
						// south
						light1 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, y, z + 1));
						// east south
						light2 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, y, z + 1));
						// east
						light3 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, y, z));
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
				wasAnythingRendered |= this.renderUp(
						buffer, atextureatlassprite,
						red0, green0, blue0,
						red1, green1, blue1,
						red2, green2, blue2,
						red3, green3, blue3,
						fluidHeight, fluidHeightSouth, fluidHeightEastSouth, fluidHeightEast,
						x, y, z,
						light0, light1, light2, light3,
						blockLiquid.shouldRenderSides(worldIn, pooledMutableBlockPos.setPos(x, y + 1, z)), blockLiquid.getFlow(worldIn, pos, state), MathHelper.getCoordinateRandom(x, y, z)
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
						final int downCombinedLightUpMax = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z));
						light0 = downCombinedLightUpMax;
						light1 = downCombinedLightUpMax;
						light2 = downCombinedLightUpMax;
						light3 = downCombinedLightUpMax;
					} else {
						// down south
						light0 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z + 1));
						// down
						light1 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x, ym1, z));
						// down east
						light2 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, ym1, z));
						// down east south
						light3 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x + 1, ym1, z + 1));
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

			final TextureAtlasSprite atlasSpriteWaterOverlay = this.atlasSpriteWaterOverlay;

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
							final int combinedLightUpMax = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos);
							light0 = combinedLightUpMax;
							light1 = combinedLightUpMax;
							light2 = combinedLightUpMax;
							light3 = combinedLightUpMax;
						} else {
							light0 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x0, y + y0, z0));
							light1 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x1, y + y1, z1));
							light2 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x1, y, z1));
							light3 = this.getCombinedLightUpMax_optimised(worldIn, pooledMutableBlockPos.setPos(x0, y, z0));
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
							light0, light1, light2, light3,
							textureatlassprite2 != atlasSpriteWaterOverlay
					);
				}
			}

			return wasAnythingRendered;
		} finally {
			pooledMutableBlockPos.release();
			OptiFineCompatibility.PROXY.popShaderThing(buffer);
		}
	}

	public int getCombinedLightUpMax_optimised(final IBlockAccess reader, PooledMutableBlockPos pooledMutableBlockPos) {
		int light = reader.getCombinedLight(pooledMutableBlockPos, 0);
		int lightUp = reader.getCombinedLight(pooledMutableBlockPos.move(EnumFacing.UP), 0);
		int blockLight = light & 0xFF;
		int blockLightUp = lightUp & 0xFF;
		int skyLight = light >> 0x10 & 0xFF;
		int skyLightUp = lightUp >> 0x10 & 0xFF;
		return (blockLight > blockLightUp ? blockLight : blockLightUp) | (skyLight > skyLightUp ? skyLight : skyLightUp) << 0x10;
	}

	public float getFluidHeight(
			IBlockAccess blockAccess, Material blockMaterial,
			final int posX, final int posY, final int posZ,
			final PooledMutableBlockPos pooledMutableBlockPos
	) {
		int divisor = 0;
		float height = 0.0F;

//		for (int j = 0; j < 4; ++j) {
//			{
		for (int x = 0; x > -2; --x) {
			for (int z = 0; z > -2; --z) {
//				pooledMutableBlockPos.setPos(posX - (j & 1), posY + 1, posZ - (j >> 1 & 1));
				pooledMutableBlockPos.setPos(posX + x, posY + 1, posZ + z);

				if (blockAccess.getBlockState(pooledMutableBlockPos).getMaterial() == blockMaterial) {
					return 1.0F;
				}

//				pooledMutableBlockPos.setPos(posX - (j & 1), posY, posZ - (j >> 1 & 1));
				pooledMutableBlockPos.setPos(posX + x, posY, posZ + z);

				IBlockState iblockstate = blockAccess.getBlockState(pooledMutableBlockPos);
				Material material = iblockstate.getMaterial();

				if (material != blockMaterial) {
					if (!material.isSolid()) {
						++height;
						++divisor;
					}
				} else {
					int k = iblockstate.getValue(BlockLiquid.LEVEL);

					if (k >= 8 || k == 0) {
						height += BlockLiquid.getLiquidHeightPercent(k) * 10.0F;
						divisor += 10;
					}

					height += BlockLiquid.getLiquidHeightPercent(k);
					++divisor;
				}
			}
		}

		return 1.0F - height / (float) divisor;
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

		int skyLight0 = combinedLightUpMax0 >> 16 & '\uffff';
		int blockLight0 = combinedLightUpMax0 & '\uffff';
		int skyLight1 = combinedLightUpMax1 >> 16 & '\uffff';
		int blockLight1 = combinedLightUpMax1 & '\uffff';
		int skyLight2 = combinedLightUpMax2 >> 16 & '\uffff';
		int blockLight2 = combinedLightUpMax2 & '\uffff';
		int skyLight3 = combinedLightUpMax3 >> 16 & '\uffff';
		int blockLight3 = combinedLightUpMax3 & '\uffff';

		// Correct full black lighting at edges, without breaking smooth lighting as it fades out normally
		// Get light from neighbours and from diagonally across
		final int skyLight0Check = skyLight0 - 0x70;
		final int skyLight1Check = skyLight1 - 0x70;
		final int skyLight2Check = skyLight2 - 0x70;
		final int skyLight3Check = skyLight3 - 0x70;
		if (skyLight0 < skyLight1Check) skyLight0 = skyLight1;
		else if (skyLight0 < skyLight2Check) skyLight0 = skyLight2;
		else if (skyLight0 < skyLight3Check) skyLight0 = skyLight3;
		if (skyLight1 < skyLight0Check) skyLight1 = skyLight0;
		else if (skyLight1 < skyLight2Check) skyLight1 = skyLight2;
		else if (skyLight1 < skyLight3Check) skyLight1 = skyLight3;
		if (skyLight2 < skyLight0Check) skyLight2 = skyLight0;
		else if (skyLight2 < skyLight1Check) skyLight2 = skyLight1;
		else if (skyLight2 < skyLight3Check) skyLight2 = skyLight3;
		if (skyLight3 < skyLight0Check) skyLight3 = skyLight0;
		else if (skyLight3 < skyLight1Check) skyLight3 = skyLight1;
		else if (skyLight3 < skyLight2Check) skyLight3 = skyLight2;

		// If light is max cancel check
		final int blockLight0Check = blockLight0 == 0xF0 ? 0 : blockLight0 - 0xD0;
		final int blockLight1Check = blockLight1 == 0xF0 ? 0 : blockLight1 - 0xD0;
		final int blockLight2Check = blockLight2 == 0xF0 ? 0 : blockLight2 - 0xD0;
		final int blockLight3Check = blockLight3 == 0xF0 ? 0 : blockLight3 - 0xD0;
		if (blockLight0 < blockLight1Check) blockLight0 = blockLight1;
		else if (blockLight0 < blockLight2Check) blockLight0 = blockLight2;
		else if (blockLight0 < blockLight3Check) blockLight0 = blockLight3;
		if (blockLight1 < blockLight0Check) blockLight1 = blockLight0;
		else if (blockLight1 < blockLight2Check) blockLight1 = blockLight2;
		else if (blockLight1 < blockLight3Check) blockLight1 = blockLight3;
		if (blockLight2 < blockLight0Check) blockLight2 = blockLight0;
		else if (blockLight2 < blockLight1Check) blockLight2 = blockLight1;
		else if (blockLight2 < blockLight3Check) blockLight2 = blockLight3;
		if (blockLight3 < blockLight0Check) blockLight3 = blockLight0;
		else if (blockLight3 < blockLight1Check) blockLight3 = blockLight1;
		else if (blockLight3 < blockLight2Check) blockLight3 = blockLight2;

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
			int combinedLightUpMax0, int combinedLightUpMax1, int combinedLightUpMax2, int combinedLightUpMax3,
			final boolean shouldRenderOppositeFace
	) {
		final float u0 = UVHelper.getMinU(textureatlassprite);
		final float u1 = textureatlassprite.getInterpolatedU(8.0D);
		final float v0 = UVHelper.clampV(textureatlassprite.getInterpolatedV((double) ((1.0F - y0) * 16.0F * 0.5F)), textureatlassprite);
		final float v1 = UVHelper.clampV(textureatlassprite.getInterpolatedV((double) ((1.0F - y1) * 16.0F * 0.5F)), textureatlassprite);
		final float v2 = textureatlassprite.getInterpolatedV(8.0D);

		int skyLight0 = combinedLightUpMax0 >> 16 & '\uffff';
		int blockLight0 = combinedLightUpMax0 & '\uffff';
		int skyLight1 = combinedLightUpMax1 >> 16 & '\uffff';
		int blockLight1 = combinedLightUpMax1 & '\uffff';
		int skyLight2 = combinedLightUpMax2 >> 16 & '\uffff';
		int blockLight2 = combinedLightUpMax2 & '\uffff';
		int skyLight3 = combinedLightUpMax3 >> 16 & '\uffff';
		int blockLight3 = combinedLightUpMax3 & '\uffff';

		// Correct full black lighting at edges, without breaking smooth lighting as it fades out normally
		// Get light from neighbours and from diagonally across
		final int skyLight0Check = skyLight0 - 0x70;
		final int skyLight1Check = skyLight1 - 0x70;
		final int skyLight2Check = skyLight2 - 0x70;
		final int skyLight3Check = skyLight3 - 0x70;
		if (skyLight0 < skyLight1Check) skyLight0 = skyLight1;
		else if (skyLight0 < skyLight2Check) skyLight0 = skyLight2;
		else if (skyLight0 < skyLight3Check) skyLight0 = skyLight3;
		if (skyLight1 < skyLight0Check) skyLight1 = skyLight0;
		else if (skyLight1 < skyLight2Check) skyLight1 = skyLight2;
		else if (skyLight1 < skyLight3Check) skyLight1 = skyLight3;
		if (skyLight2 < skyLight0Check) skyLight2 = skyLight0;
		else if (skyLight2 < skyLight1Check) skyLight2 = skyLight1;
		else if (skyLight2 < skyLight3Check) skyLight2 = skyLight3;
		if (skyLight3 < skyLight0Check) skyLight3 = skyLight0;
		else if (skyLight3 < skyLight1Check) skyLight3 = skyLight1;
		else if (skyLight3 < skyLight2Check) skyLight3 = skyLight2;

		// If light is max cancel check
		final int blockLight0Check = blockLight0 == 0xF0 ? 0 : blockLight0 - 0xD0;
		final int blockLight1Check = blockLight1 == 0xF0 ? 0 : blockLight1 - 0xD0;
		final int blockLight2Check = blockLight2 == 0xF0 ? 0 : blockLight2 - 0xD0;
		final int blockLight3Check = blockLight3 == 0xF0 ? 0 : blockLight3 - 0xD0;
		if (blockLight0 < blockLight1Check) blockLight0 = blockLight1;
		else if (blockLight0 < blockLight2Check) blockLight0 = blockLight2;
		else if (blockLight0 < blockLight3Check) blockLight0 = blockLight3;
		if (blockLight1 < blockLight0Check) blockLight1 = blockLight0;
		else if (blockLight1 < blockLight2Check) blockLight1 = blockLight2;
		else if (blockLight1 < blockLight3Check) blockLight1 = blockLight3;
		if (blockLight2 < blockLight0Check) blockLight2 = blockLight0;
		else if (blockLight2 < blockLight1Check) blockLight2 = blockLight1;
		else if (blockLight2 < blockLight3Check) blockLight2 = blockLight3;
		if (blockLight3 < blockLight0Check) blockLight3 = blockLight0;
		else if (blockLight3 < blockLight1Check) blockLight3 = blockLight1;
		else if (blockLight3 < blockLight2Check) blockLight3 = blockLight2;

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
			int downCombinedLightUpMax0, int downCombinedLightUpMax1, int downCombinedLightUpMax2, int downCombinedLightUpMax3,
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

		int skyLight0 = downCombinedLightUpMax0 >> 16 & '\uffff';
		int blockLight0 = downCombinedLightUpMax0 & '\uffff';
		int skyLight1 = downCombinedLightUpMax1 >> 16 & '\uffff';
		int blockLight1 = downCombinedLightUpMax1 & '\uffff';
		int skyLight2 = downCombinedLightUpMax2 >> 16 & '\uffff';
		int blockLight2 = downCombinedLightUpMax2 & '\uffff';
		int skyLight3 = downCombinedLightUpMax3 >> 16 & '\uffff';
		int blockLight3 = downCombinedLightUpMax3 & '\uffff';

		// Correct full black lighting at edges, without breaking smooth lighting as it fades out normally
		// Get light from neighbours and from diagonally across
		final int skyLight0Check = skyLight0 - 0x70;
		final int skyLight1Check = skyLight1 - 0x70;
		final int skyLight2Check = skyLight2 - 0x70;
		final int skyLight3Check = skyLight3 - 0x70;
		if (skyLight0 < skyLight1Check) skyLight0 = skyLight1;
		else if (skyLight0 < skyLight2Check) skyLight0 = skyLight2;
		else if (skyLight0 < skyLight3Check) skyLight0 = skyLight3;
		if (skyLight1 < skyLight0Check) skyLight1 = skyLight0;
		else if (skyLight1 < skyLight2Check) skyLight1 = skyLight2;
		else if (skyLight1 < skyLight3Check) skyLight1 = skyLight3;
		if (skyLight2 < skyLight0Check) skyLight2 = skyLight0;
		else if (skyLight2 < skyLight1Check) skyLight2 = skyLight1;
		else if (skyLight2 < skyLight3Check) skyLight2 = skyLight3;
		if (skyLight3 < skyLight0Check) skyLight3 = skyLight0;
		else if (skyLight3 < skyLight1Check) skyLight3 = skyLight1;
		else if (skyLight3 < skyLight2Check) skyLight3 = skyLight2;

		// If light is max cancel check
		final int blockLight0Check = blockLight0 == 0xF0 ? 0 : blockLight0 - 0xD0;
		final int blockLight1Check = blockLight1 == 0xF0 ? 0 : blockLight1 - 0xD0;
		final int blockLight2Check = blockLight2 == 0xF0 ? 0 : blockLight2 - 0xD0;
		final int blockLight3Check = blockLight3 == 0xF0 ? 0 : blockLight3 - 0xD0;
		if (blockLight0 < blockLight1Check) blockLight0 = blockLight1;
		else if (blockLight0 < blockLight2Check) blockLight0 = blockLight2;
		else if (blockLight0 < blockLight3Check) blockLight0 = blockLight3;
		if (blockLight1 < blockLight0Check) blockLight1 = blockLight0;
		else if (blockLight1 < blockLight2Check) blockLight1 = blockLight2;
		else if (blockLight1 < blockLight3Check) blockLight1 = blockLight3;
		if (blockLight2 < blockLight0Check) blockLight2 = blockLight0;
		else if (blockLight2 < blockLight1Check) blockLight2 = blockLight1;
		else if (blockLight2 < blockLight3Check) blockLight2 = blockLight3;
		if (blockLight3 < blockLight0Check) blockLight3 = blockLight0;
		else if (blockLight3 < blockLight1Check) blockLight3 = blockLight1;
		else if (blockLight3 < blockLight2Check) blockLight3 = blockLight2;

		buffer.pos(x, y, z + 1.0D).color(0.5F * red0, 0.5F * green0, 0.5F * blue0, 1.0F).tex((double) minU, (double) maxV).lightmap(skyLight0, blockLight0).endVertex();
		buffer.pos(x, y, z).color(0.5F * red1, 0.5F * green1, 0.5F * blue1, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight1, blockLight1).endVertex();
		buffer.pos(x + 1.0D, y, z).color(0.5F * red2, 0.5F * green2, 0.5F * blue2, 1.0F).tex((double) maxU, (double) minV).lightmap(skyLight2, blockLight2).endVertex();
		buffer.pos(x + 1.0D, y, z + 1.0D).color(0.5F * red3, 0.5F * green3, 0.5F * blue3, 1.0F).tex((double) maxU, (double) maxV).lightmap(skyLight3, blockLight3).endVertex();
		return true;
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
