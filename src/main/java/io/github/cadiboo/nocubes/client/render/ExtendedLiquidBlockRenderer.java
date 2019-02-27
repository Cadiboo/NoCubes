package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.NoCubes;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.init.Blocks;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.biome.BiomeColors;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class ExtendedLiquidBlockRenderer {

	private static final TextureAtlasSprite[] atlasSpritesLava = new TextureAtlasSprite[2];
	private static final TextureAtlasSprite[] atlasSpritesWater = new TextureAtlasSprite[2];
	private static TextureAtlasSprite atlasSpriteWaterOverlay;

	protected static void initAtlasSprites() {
		if (atlasSpriteWaterOverlay != null) {
			return;
		}
		TextureMap texturemap = Minecraft.getInstance().getTextureMap();
		atlasSpritesLava[0] = Minecraft.getInstance().getModelManager().getBlockModelShapes().getModel(Blocks.LAVA.getDefaultState()).getParticleTexture();
		atlasSpritesLava[1] = texturemap.getSprite(ModelBakery.LOCATION_LAVA_FLOW);
		atlasSpritesWater[0] = Minecraft.getInstance().getModelManager().getBlockModelShapes().getModel(Blocks.WATER.getDefaultState()).getParticleTexture();
		atlasSpritesWater[1] = texturemap.getSprite(ModelBakery.LOCATION_WATER_FLOW);
		atlasSpriteWaterOverlay = texturemap.getSprite(ModelBakery.LOCATION_WATER_OVERLAY);
	}

	public static boolean renderExtendedLiquid(
			final double renderPosX, final double renderPosY, final double renderPosZ,
			@Nonnull final BlockPos fluidPos,
			@Nonnull final IWorldReader blockAccess,
			//TODO: eventually do better liquid rendering for 0.3.0
//			@Nonnull final IBlockState smoothableState,
			@Nonnull final IBlockState liquidState,
			final IFluidState fluidState, @Nonnull final BufferBuilder bufferBuilder
	) {
		NoCubes.getProfiler().start("renderExtendedLiquid");
		initAtlasSprites();

//		public boolean render(IWorldReader blockAccess, BlockPos fluidPos, BufferBuilder bufferBuilder, IFluidState fluidState) {
		boolean isLava = fluidState.isTagged(FluidTags.LAVA);
		TextureAtlasSprite[] sprites = isLava ? atlasSpritesLava : atlasSpritesWater;
		int color = isLava ? 0XFFFFFF : BiomeColors.getWaterColor(blockAccess, fluidPos);
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;
		boolean shouldRenderUp = !isAdjacentFluidSameAs(blockAccess, fluidPos, EnumFacing.UP, fluidState);
		boolean shouldRenderDown = !isAdjacentFluidSameAs(blockAccess, fluidPos, EnumFacing.DOWN, fluidState) && !func_209556_a(blockAccess, fluidPos, EnumFacing.DOWN, 0.8888889F);
		boolean shouldRenderNorth = !isAdjacentFluidSameAs(blockAccess, fluidPos, EnumFacing.NORTH, fluidState);
		boolean shouldRenderSouth = !isAdjacentFluidSameAs(blockAccess, fluidPos, EnumFacing.SOUTH, fluidState);
		boolean shouldRenderWest = !isAdjacentFluidSameAs(blockAccess, fluidPos, EnumFacing.WEST, fluidState);
		boolean shouldRenderEast = !isAdjacentFluidSameAs(blockAccess, fluidPos, EnumFacing.EAST, fluidState);
		if (!shouldRenderUp && !shouldRenderDown && !shouldRenderEast && !shouldRenderWest && !shouldRenderNorth && !shouldRenderSouth) {
			NoCubes.getProfiler().end();
			return false;
		} else {
			boolean wasAnythingRendered = false;
//			float f3 = 0.5F;
//			float f4 = 1.0F;
//			float f5 = 0.8F;
//			float f6 = 0.6F;
			float fluidHeight = getFluidHeight(blockAccess, fluidPos, fluidState.getFluid());
			float fluidHeightSouth = getFluidHeight(blockAccess, fluidPos.south(), fluidState.getFluid());
			float fluidHeightEastSouth = getFluidHeight(blockAccess, fluidPos.east().south(), fluidState.getFluid());
			float fluidHeightEast = getFluidHeight(blockAccess, fluidPos.east(), fluidState.getFluid());
//			double renderPosX = (double) fluidPos.getX();
//			double renderPosY = (double) fluidPos.getY();
//			double renderPosZ = (double) fluidPos.getZ();
//			float f11 = 0.001F;
			if (shouldRenderUp && !func_209556_a(blockAccess, fluidPos, EnumFacing.UP, Math.min(Math.min(fluidHeight, fluidHeightSouth), Math.min(fluidHeightEastSouth, fluidHeightEast)))) {
				wasAnythingRendered = true;
				fluidHeight -= 0.001F;
				fluidHeightSouth -= 0.001F;
				fluidHeightEastSouth -= 0.001F;
				fluidHeightEast -= 0.001F;
				Vec3d vec3d = fluidState.getFlow(blockAccess, fluidPos);
				float u_f12;
				float u_f13;
				float u_f14;
				float u_f15;
				float v_f16;
				float v_f17;
				float v_f18;
				float v_f19;
				if (vec3d.x == 0.0D && vec3d.z == 0.0D) {
					TextureAtlasSprite textureatlassprite1 = sprites[0];
					u_f12 = textureatlassprite1.getInterpolatedU(0.0D);
					v_f16 = textureatlassprite1.getInterpolatedV(0.0D);
					u_f13 = u_f12;
					v_f17 = textureatlassprite1.getInterpolatedV(16.0D);
					u_f14 = textureatlassprite1.getInterpolatedU(16.0D);
					v_f18 = v_f17;
					u_f15 = u_f14;
					v_f19 = v_f16;
				} else {
					TextureAtlasSprite textureatlassprite = sprites[1];
					float f20 = (float) MathHelper.atan2(vec3d.z, vec3d.x) - ((float) Math.PI / 2F);
					float f21 = MathHelper.sin(f20) * 0.25F;
					float f22 = MathHelper.cos(f20) * 0.25F;
//					float f23 = 8.0F;
					u_f12 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f22 - f21) * 16.0F));
					v_f16 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f22 + f21) * 16.0F));
					u_f13 = textureatlassprite.getInterpolatedU((double) (8.0F + (-f22 + f21) * 16.0F));
					v_f17 = textureatlassprite.getInterpolatedV((double) (8.0F + (f22 + f21) * 16.0F));
					u_f14 = textureatlassprite.getInterpolatedU((double) (8.0F + (f22 + f21) * 16.0F));
					v_f18 = textureatlassprite.getInterpolatedV((double) (8.0F + (f22 - f21) * 16.0F));
					u_f15 = textureatlassprite.getInterpolatedU((double) (8.0F + (f22 - f21) * 16.0F));
					v_f19 = textureatlassprite.getInterpolatedV((double) (8.0F + (-f22 - f21) * 16.0F));
				}

				int combinedLightUpMax = getCombinedLightUpMax(blockAccess, fluidPos);
				int skyLight = combinedLightUpMax >> 16 & '\uffff';
				int blockLight = combinedLightUpMax & '\uffff';
				float renderRed = 1.0F * red;
				float renderGreen = 1.0F * green;
				float renderBlue = 1.0F * blue;
				bufferBuilder.pos(renderPosX + 0.0D, renderPosY + (double) fluidHeight, renderPosZ + 0.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) u_f12, (double) v_f16).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(renderPosX + 0.0D, renderPosY + (double) fluidHeightSouth, renderPosZ + 1.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) u_f13, (double) v_f17).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(renderPosX + 1.0D, renderPosY + (double) fluidHeightEastSouth, renderPosZ + 1.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) u_f14, (double) v_f18).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(renderPosX + 1.0D, renderPosY + (double) fluidHeightEast, renderPosZ + 0.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) u_f15, (double) v_f19).lightmap(skyLight, blockLight).endVertex();
				if (fluidState.shouldRenderSides(blockAccess, fluidPos.up())) {
					bufferBuilder.pos(renderPosX + 0.0D, renderPosY + (double) fluidHeight, renderPosZ + 0.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) u_f12, (double) v_f16).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(renderPosX + 1.0D, renderPosY + (double) fluidHeightEast, renderPosZ + 0.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) u_f15, (double) v_f19).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(renderPosX + 1.0D, renderPosY + (double) fluidHeightEastSouth, renderPosZ + 1.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) u_f14, (double) v_f18).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(renderPosX + 0.0D, renderPosY + (double) fluidHeightSouth, renderPosZ + 1.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) u_f13, (double) v_f17).lightmap(skyLight, blockLight).endVertex();
				}
			}

			if (shouldRenderDown) {
				float minU = sprites[0].getMinU();
				float maxU = sprites[0].getMaxU();
				float minV = sprites[0].getMinV();
				float maxV = sprites[0].getMaxV();
				int combinedLightUpMax = getCombinedLightUpMax(blockAccess, fluidPos.down());
				int skyLight = combinedLightUpMax >> 16 & '\uffff';
				int blockLight = combinedLightUpMax & '\uffff';
				float renderRed = 0.5F * red;
				float renderGreen = 0.5F * green;
				float renderBlue = 0.5F * blue;
				bufferBuilder.pos(renderPosX, renderPosY, renderPosZ + 1.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) minU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(renderPosX, renderPosY, renderPosZ).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) minU, (double) minV).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(renderPosX + 1.0D, renderPosY, renderPosZ).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) maxU, (double) minV).lightmap(skyLight, blockLight).endVertex();
				bufferBuilder.pos(renderPosX + 1.0D, renderPosY, renderPosZ + 1.0D).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) maxU, (double) maxV).lightmap(skyLight, blockLight).endVertex();
				wasAnythingRendered = true;
			}

			for (int facingIndex = 0; facingIndex < 4; ++facingIndex) {
				float renderSidePosYAdd0;
				float renderSidePosYAdd1;
				double renderSidePosX0;
				double renderSidePosZ0;
				double renderSidePosX1;
				double renderSidePosZ1;
				EnumFacing enumfacing;
				boolean shouldRenderSide;
				if (facingIndex == 0) {
					renderSidePosYAdd0 = fluidHeight;
					renderSidePosYAdd1 = fluidHeightEast;
					renderSidePosX0 = renderPosX;
					renderSidePosX1 = renderPosX + 1.0D;
					renderSidePosZ0 = renderPosZ + (double) 0.001F;
					renderSidePosZ1 = renderPosZ + (double) 0.001F;
					enumfacing = EnumFacing.NORTH;
					shouldRenderSide = shouldRenderNorth;
				} else if (facingIndex == 1) {
					renderSidePosYAdd0 = fluidHeightEastSouth;
					renderSidePosYAdd1 = fluidHeightSouth;
					renderSidePosX0 = renderPosX + 1.0D;
					renderSidePosX1 = renderPosX;
					renderSidePosZ0 = renderPosZ + 1.0D - (double) 0.001F;
					renderSidePosZ1 = renderPosZ + 1.0D - (double) 0.001F;
					enumfacing = EnumFacing.SOUTH;
					shouldRenderSide = shouldRenderSouth;
				} else if (facingIndex == 2) {
					renderSidePosYAdd0 = fluidHeightSouth;
					renderSidePosYAdd1 = fluidHeight;
					renderSidePosX0 = renderPosX + (double) 0.001F;
					renderSidePosX1 = renderPosX + (double) 0.001F;
					renderSidePosZ0 = renderPosZ + 1.0D;
					renderSidePosZ1 = renderPosZ;
					enumfacing = EnumFacing.WEST;
					shouldRenderSide = shouldRenderWest;
				} else {
					renderSidePosYAdd0 = fluidHeightEast;
					renderSidePosYAdd1 = fluidHeightEastSouth;
					renderSidePosX0 = renderPosX + 1.0D - (double) 0.001F;
					renderSidePosX1 = renderPosX + 1.0D - (double) 0.001F;
					renderSidePosZ0 = renderPosZ;
					renderSidePosZ1 = renderPosZ + 1.0D;
					enumfacing = EnumFacing.EAST;
					shouldRenderSide = shouldRenderEast;
				}

				if (shouldRenderSide && !func_209556_a(blockAccess, fluidPos, enumfacing, Math.max(renderSidePosYAdd0, renderSidePosYAdd1))) {
					wasAnythingRendered = true;
					BlockPos blockpos = fluidPos.offset(enumfacing);
					TextureAtlasSprite textureatlassprite2 = sprites[1];
					if (!isLava) {
						IBlockState blockstate = blockAccess.getBlockState(blockpos);
						if (blockstate.getBlockFaceShape(blockAccess, blockpos, enumfacing) == net.minecraft.block.state.BlockFaceShape.SOLID) {
							textureatlassprite2 = atlasSpriteWaterOverlay;
						}
					}

					float minU = textureatlassprite2.getInterpolatedU(0.0D);
					float midU = textureatlassprite2.getInterpolatedU(8.0D);
					float intV0 = textureatlassprite2.getInterpolatedV((double) ((1.0F - renderSidePosYAdd0) * 16.0F * 0.5F));
					float intV1 = textureatlassprite2.getInterpolatedV((double) ((1.0F - renderSidePosYAdd1) * 16.0F * 0.5F));
					float midV = textureatlassprite2.getInterpolatedV(8.0D);
					int combinedLightUpMax = getCombinedLightUpMax(blockAccess, blockpos);
					int skyLight = combinedLightUpMax >> 16 & '\uffff';
					int blockLight = combinedLightUpMax & '\uffff';
					float faceColorMultiplier = facingIndex < 2 ? 0.8F : 0.6F;
					float renderRed = 1.0F * faceColorMultiplier * red;
					float renderGreen = 1.0F * faceColorMultiplier * green;
					float renderBlue = 1.0F * faceColorMultiplier * blue;
					bufferBuilder.pos(renderSidePosX0, renderPosY + (double) renderSidePosYAdd0, renderSidePosZ0).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) minU, (double) intV0).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(renderSidePosX1, renderPosY + (double) renderSidePosYAdd1, renderSidePosZ1).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) midU, (double) intV1).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(renderSidePosX1, renderPosY + 0.0D, renderSidePosZ1).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) midU, (double) midV).lightmap(skyLight, blockLight).endVertex();
					bufferBuilder.pos(renderSidePosX0, renderPosY + 0.0D, renderSidePosZ0).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) minU, (double) midV).lightmap(skyLight, blockLight).endVertex();
					if (textureatlassprite2 != atlasSpriteWaterOverlay) {
						bufferBuilder.pos(renderSidePosX0, renderPosY + 0.0D, renderSidePosZ0).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) minU, (double) midV).lightmap(skyLight, blockLight).endVertex();
						bufferBuilder.pos(renderSidePosX1, renderPosY + 0.0D, renderSidePosZ1).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) midU, (double) midV).lightmap(skyLight, blockLight).endVertex();
						bufferBuilder.pos(renderSidePosX1, renderPosY + (double) renderSidePosYAdd1, renderSidePosZ1).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) midU, (double) intV1).lightmap(skyLight, blockLight).endVertex();
						bufferBuilder.pos(renderSidePosX0, renderPosY + (double) renderSidePosYAdd0, renderSidePosZ0).color(renderRed, renderGreen, renderBlue, 1.0F).tex((double) minU, (double) intV0).lightmap(skyLight, blockLight).endVertex();
					}
				}
			}

			NoCubes.getProfiler().end();
			return wasAnythingRendered;
		}
//		}
	}

	private static boolean isAdjacentFluidSameAs(IBlockReader worldIn, BlockPos pos, EnumFacing side, IFluidState state) {
		BlockPos blockpos = pos.offset(side);
		IFluidState ifluidstate = worldIn.getFluidState(blockpos);
		return ifluidstate.getFluid().isEquivalentTo(state.getFluid());
	}

	private static boolean func_209556_a(IBlockReader reader, BlockPos pos, EnumFacing face, float heightIn) {
		BlockPos blockpos = pos.offset(face);
		IBlockState iblockstate = reader.getBlockState(blockpos);
		if (iblockstate.isSolid()) {
			VoxelShape voxelshape = VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, (double) heightIn, 1.0D);
			VoxelShape voxelshape1 = iblockstate.getRenderShape(reader, blockpos);
			return VoxelShapes.isCubeSideCovered(voxelshape, voxelshape1, face);
		} else {
			return false;
		}
	}

	private static int getCombinedLightUpMax(IWorldReader reader, BlockPos pos) {
		int i = reader.getCombinedLight(pos, 0);
		int j = reader.getCombinedLight(pos.up(), 0);
		int k = i & 255;
		int l = j & 255;
		int i1 = i >> 16 & 255;
		int j1 = j >> 16 & 255;
		return (k > l ? k : l) | (i1 > j1 ? i1 : j1) << 16;
	}

	private static float getFluidHeight(IWorldReaderBase reader, BlockPos pos, Fluid fluidIn) {
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

}
