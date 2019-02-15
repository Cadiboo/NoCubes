package io.github.cadiboo.nocubes.client.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.StainedGlassBlock;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.block.BiomeColors;
import net.minecraft.client.render.block.BlockColorMap;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.ExtendedBlockView;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public class ExtendedLiquidBlockRenderer {

	public static boolean renderExtendedLiquid(
			@Nonnull final SpriteAtlasTexture textureMap,
			@Nonnull final BlockColorMap blockColors,
			final double renderPosX, final double renderPosY, final double renderPosZ,
			@Nonnull final BlockPos liquidPos,
			@Nonnull final ExtendedBlockView blockAccess,
			//TODO: eventually do better liquid rendering for 0.3.0
//			@Nonnull final IBlockState smoothableState,
			@Nonnull final FluidState liquidState,
			@Nonnull final BufferBuilder bufferBuilder
	) {

		final Sprite[] atlasSpritesLava = {
				textureMap.getSprite("minecraft:blocks/lava_still"),
				textureMap.getSprite("minecraft:blocks/lava_flow")
		};
		final Sprite[] atlasSpritesWater = {
				textureMap.getSprite("minecraft:blocks/water_still"),
				textureMap.getSprite("minecraft:blocks/water_flow")
		};
		final Sprite atlasSpriteWaterOverlay = textureMap.getSprite("minecraft:blocks/water_overlay");

		boolean isLava = liquidState.matches(FluidTags.LAVA);
		Sprite[] sprites = isLava ? atlasSpritesLava : atlasSpritesWater;

		int color = isLava ? 0xFFFFFF : BiomeColors.waterColorAt(blockAccess, liquidPos);
		float red = (float) (color >> 16 & 255) / 255.0F;
		float green = (float) (color >> 8 & 255) / 255.0F;
		float blue = (float) (color & 255) / 255.0F;
		boolean boolean_2 = !method_3348(blockAccess, liquidPos, Direction.UP, liquidState);
		boolean boolean_3 = !method_3348(blockAccess, liquidPos, Direction.DOWN, liquidState) && !method_3344(blockAccess, liquidPos, Direction.DOWN, 0.8888889F);
		boolean boolean_4 = !method_3348(blockAccess, liquidPos, Direction.NORTH, liquidState);
		boolean boolean_5 = !method_3348(blockAccess, liquidPos, Direction.SOUTH, liquidState);
		boolean boolean_6 = !method_3348(blockAccess, liquidPos, Direction.WEST, liquidState);
		boolean boolean_7 = !method_3348(blockAccess, liquidPos, Direction.EAST, liquidState);
		if (!boolean_2 && !boolean_3 && !boolean_7 && !boolean_6 && !boolean_4 && !boolean_5) {
			return false;
		} else {
			boolean wasAnythingRendered = false;
//			float float_4 = 0.5F;
//			float float_5 = 1.0F;
//			float float_6 = 0.8F;
//			float float_7 = 0.6F;
			float float_8 = getFluidHeight(blockAccess, liquidPos, liquidState.getFluid());
			float float_9 = getFluidHeight(blockAccess, liquidPos.south(), liquidState.getFluid());
			float float_10 = getFluidHeight(blockAccess, liquidPos.east().south(), liquidState.getFluid());
			float float_11 = getFluidHeight(blockAccess, liquidPos.east(), liquidState.getFluid());
//			double double_1 = (double) liquidPos.getX();
//			double double_2 = (double) liquidPos.getY();
//			double double_3 = (double) liquidPos.getZ();
			double double_1 = renderPosX;
			double double_2 = renderPosY;
			double double_3 = renderPosZ;
//			float float_12 = 0.001F;
			float float_25;
			float float_54;
			float float_55;
			float float_31;
			float float_32;
			float float_58;
			float float_59;
			float float_60;
			float float_33;
			if (boolean_2 && !method_3344(blockAccess, liquidPos, Direction.UP, Math.min(Math.min(float_8, float_9), Math.min(float_10, float_11)))) {
				wasAnythingRendered = true;
				float_8 -= 0.001F;
				float_9 -= 0.001F;
				float_10 -= 0.001F;
				float_11 -= 0.001F;
				Vec3d vec3d_1 = liquidState.method_15758(blockAccess, liquidPos);
				float float_26;
				float float_28;
				float float_30;
				Sprite sprite_2;
				float float_34;
				float float_35;
				float float_36;
				float float_37;
				if (vec3d_1.x == 0.0D && vec3d_1.z == 0.0D) {
					sprite_2 = sprites[0];
					float_25 = sprite_2.getU(0.0D);
					float_26 = sprite_2.getV(0.0D);
					float_54 = float_25;
					float_28 = sprite_2.getV(16.0D);
					float_55 = sprite_2.getU(16.0D);
					float_30 = float_28;
					float_31 = float_55;
					float_32 = float_26;
				} else {
					sprite_2 = sprites[1];
					float_34 = (float) MathHelper.atan2(vec3d_1.z, vec3d_1.x) - 1.5707964F;
					float_35 = MathHelper.sin(float_34) * 0.25F;
					float_36 = MathHelper.cos(float_34) * 0.25F;
//					float_37 = 8.0F;
					float_25 = sprite_2.getU((double) (8.0F + (-float_36 - float_35) * 16.0F));
					float_26 = sprite_2.getV((double) (8.0F + (-float_36 + float_35) * 16.0F));
					float_54 = sprite_2.getU((double) (8.0F + (-float_36 + float_35) * 16.0F));
					float_28 = sprite_2.getV((double) (8.0F + (float_36 + float_35) * 16.0F));
					float_55 = sprite_2.getU((double) (8.0F + (float_36 + float_35) * 16.0F));
					float_30 = sprite_2.getV((double) (8.0F + (float_36 - float_35) * 16.0F));
					float_31 = sprite_2.getU((double) (8.0F + (float_36 - float_35) * 16.0F));
					float_32 = sprite_2.getV((double) (8.0F + (-float_36 - float_35) * 16.0F));
				}

				float_33 = (float_25 + float_54 + float_55 + float_31) / 4.0F;
				float_34 = (float_26 + float_28 + float_30 + float_32) / 4.0F;
				float_35 = (float) sprites[0].getWidth() / (sprites[0].getMaxU() - sprites[0].getMinU());
				float_36 = (float) sprites[0].getHeight() / (sprites[0].getMaxV() - sprites[0].getMinV());
				float_37 = 4.0F / Math.max(float_36, float_35);
				float_25 = MathHelper.lerp(float_37, float_25, float_33);
				float_54 = MathHelper.lerp(float_37, float_54, float_33);
				float_55 = MathHelper.lerp(float_37, float_55, float_33);
				float_31 = MathHelper.lerp(float_37, float_31, float_33);
				float_26 = MathHelper.lerp(float_37, float_26, float_34);
				float_28 = MathHelper.lerp(float_37, float_28, float_34);
				float_30 = MathHelper.lerp(float_37, float_30, float_34);
				float_32 = MathHelper.lerp(float_37, float_32, float_34);
				int int_2 = method_3343(blockAccess, liquidPos);
				int int_3 = int_2 >> 16 & '\uffff';
				int int_4 = int_2 & '\uffff';
				float_58 = 1.0F * red;
				float_59 = 1.0F * green;
				float_60 = 1.0F * blue;
				bufferBuilder.vertex(double_1 + 0.0D, double_2 + (double) float_8, double_3 + 0.0D).color(float_58, float_59, float_60, 1.0F).texture((double) float_25, (double) float_26).texture(int_3, int_4).next();
				bufferBuilder.vertex(double_1 + 0.0D, double_2 + (double) float_9, double_3 + 1.0D).color(float_58, float_59, float_60, 1.0F).texture((double) float_54, (double) float_28).texture(int_3, int_4).next();
				bufferBuilder.vertex(double_1 + 1.0D, double_2 + (double) float_10, double_3 + 1.0D).color(float_58, float_59, float_60, 1.0F).texture((double) float_55, (double) float_30).texture(int_3, int_4).next();
				bufferBuilder.vertex(double_1 + 1.0D, double_2 + (double) float_11, double_3 + 0.0D).color(float_58, float_59, float_60, 1.0F).texture((double) float_31, (double) float_32).texture(int_3, int_4).next();
				if (liquidState.method_15756(blockAccess, liquidPos.up())) {
					bufferBuilder.vertex(double_1 + 0.0D, double_2 + (double) float_8, double_3 + 0.0D).color(float_58, float_59, float_60, 1.0F).texture((double) float_25, (double) float_26).texture(int_3, int_4).next();
					bufferBuilder.vertex(double_1 + 1.0D, double_2 + (double) float_11, double_3 + 0.0D).color(float_58, float_59, float_60, 1.0F).texture((double) float_31, (double) float_32).texture(int_3, int_4).next();
					bufferBuilder.vertex(double_1 + 1.0D, double_2 + (double) float_10, double_3 + 1.0D).color(float_58, float_59, float_60, 1.0F).texture((double) float_55, (double) float_30).texture(int_3, int_4).next();
					bufferBuilder.vertex(double_1 + 0.0D, double_2 + (double) float_9, double_3 + 1.0D).color(float_58, float_59, float_60, 1.0F).texture((double) float_54, (double) float_28).texture(int_3, int_4).next();
				}
			}

			if (boolean_3) {
				float_25 = sprites[0].getMinU();
				float_54 = sprites[0].getMaxU();
				float_55 = sprites[0].getMinV();
				float_31 = sprites[0].getMaxV();
				int int_5 = method_3343(blockAccess, liquidPos.down());
				int int_6 = int_5 >> 16 & '\uffff';
				int int_7 = int_5 & '\uffff';
				float_32 = 0.5F * red;
				float float_46 = 0.5F * green;
				float_33 = 0.5F * blue;
				bufferBuilder.vertex(double_1, double_2, double_3 + 1.0D).color(float_32, float_46, float_33, 1.0F).texture((double) float_25, (double) float_31).texture(int_6, int_7).next();
				bufferBuilder.vertex(double_1, double_2, double_3).color(float_32, float_46, float_33, 1.0F).texture((double) float_25, (double) float_55).texture(int_6, int_7).next();
				bufferBuilder.vertex(double_1 + 1.0D, double_2, double_3).color(float_32, float_46, float_33, 1.0F).texture((double) float_54, (double) float_55).texture(int_6, int_7).next();
				bufferBuilder.vertex(double_1 + 1.0D, double_2, double_3 + 1.0D).color(float_32, float_46, float_33, 1.0F).texture((double) float_54, (double) float_31).texture(int_6, int_7).next();
				wasAnythingRendered = true;
			}

			for (int int_8 = 0; int_8 < 4; ++int_8) {
				double double_16;
				double double_18;
				double double_17;
				double double_19;
				Direction direction_3;
				boolean boolean_12;
				if (int_8 == 0) {
					float_54 = float_8;
					float_55 = float_11;
					double_16 = double_1;
					double_17 = double_1 + 1.0D;
					double_18 = double_3 + 0.0010000000474974513D;
					double_19 = double_3 + 0.0010000000474974513D;
					direction_3 = Direction.NORTH;
					boolean_12 = boolean_4;
				} else if (int_8 == 1) {
					float_54 = float_10;
					float_55 = float_9;
					double_16 = double_1 + 1.0D;
					double_17 = double_1;
					double_18 = double_3 + 1.0D - 0.0010000000474974513D;
					double_19 = double_3 + 1.0D - 0.0010000000474974513D;
					direction_3 = Direction.SOUTH;
					boolean_12 = boolean_5;
				} else if (int_8 == 2) {
					float_54 = float_9;
					float_55 = float_8;
					double_16 = double_1 + 0.0010000000474974513D;
					double_17 = double_1 + 0.0010000000474974513D;
					double_18 = double_3 + 1.0D;
					double_19 = double_3;
					direction_3 = Direction.WEST;
					boolean_12 = boolean_6;
				} else {
					float_54 = float_11;
					float_55 = float_10;
					double_16 = double_1 + 1.0D - 0.0010000000474974513D;
					double_17 = double_1 + 1.0D - 0.0010000000474974513D;
					double_18 = double_3;
					double_19 = double_3 + 1.0D;
					direction_3 = Direction.EAST;
					boolean_12 = boolean_7;
				}

				if (boolean_12 && !method_3344(blockAccess, liquidPos, direction_3, Math.max(float_54, float_55))) {
					wasAnythingRendered = true;
					BlockPos blockPos_2 = liquidPos.offset(direction_3);
					Sprite sprite_3 = sprites[1];
					if (!isLava) {
						Block block_1 = blockAccess.getBlockState(blockPos_2).getBlock();
						if (block_1 == Blocks.GLASS || block_1 instanceof StainedGlassBlock) {
							sprite_3 = atlasSpriteWaterOverlay;
						}
					}

					float float_56 = sprite_3.getU(0.0D);
					float float_57 = sprite_3.getU(8.0D);
					float_58 = sprite_3.getV((double) ((1.0F - float_54) * 16.0F * 0.5F));
					float_59 = sprite_3.getV((double) ((1.0F - float_55) * 16.0F * 0.5F));
					float_60 = sprite_3.getV(8.0D);
					int int_9 = method_3343(blockAccess, blockPos_2);
					int int_10 = int_9 >> 16 & '\uffff';
					int int_11 = int_9 & '\uffff';
					float float_61 = int_8 < 2 ? 0.8F : 0.6F;
					float float_62 = 1.0F * float_61 * red;
					float float_63 = 1.0F * float_61 * green;
					float float_64 = 1.0F * float_61 * blue;
					bufferBuilder.vertex(double_16, double_2 + (double) float_54, double_18).color(float_62, float_63, float_64, 1.0F).texture((double) float_56, (double) float_58).texture(int_10, int_11).next();
					bufferBuilder.vertex(double_17, double_2 + (double) float_55, double_19).color(float_62, float_63, float_64, 1.0F).texture((double) float_57, (double) float_59).texture(int_10, int_11).next();
					bufferBuilder.vertex(double_17, double_2 + 0.0D, double_19).color(float_62, float_63, float_64, 1.0F).texture((double) float_57, (double) float_60).texture(int_10, int_11).next();
					bufferBuilder.vertex(double_16, double_2 + 0.0D, double_18).color(float_62, float_63, float_64, 1.0F).texture((double) float_56, (double) float_60).texture(int_10, int_11).next();
					if (sprite_3 != atlasSpriteWaterOverlay) {
						bufferBuilder.vertex(double_16, double_2 + 0.0D, double_18).color(float_62, float_63, float_64, 1.0F).texture((double) float_56, (double) float_60).texture(int_10, int_11).next();
						bufferBuilder.vertex(double_17, double_2 + 0.0D, double_19).color(float_62, float_63, float_64, 1.0F).texture((double) float_57, (double) float_60).texture(int_10, int_11).next();
						bufferBuilder.vertex(double_17, double_2 + (double) float_55, double_19).color(float_62, float_63, float_64, 1.0F).texture((double) float_57, (double) float_59).texture(int_10, int_11).next();
						bufferBuilder.vertex(double_16, double_2 + (double) float_54, double_18).color(float_62, float_63, float_64, 1.0F).texture((double) float_56, (double) float_58).texture(int_10, int_11).next();
					}
				}
			}

			return wasAnythingRendered;
		}
	}

	private static float getFluidHeight(ExtendedBlockView blockAccess, BlockPos blockPosIn, Fluid fluid) {
		int divisor = 0;
		float liquidHeightPercentage = 0.0F;

		for (int int_2 = 0; int_2 < 4; ++int_2) {
			BlockPos blockPos_2 = blockPosIn.add(-(int_2 & 1), 0, -(int_2 >> 1 & 1));
			if (blockAccess.getFluidState(blockPos_2.up()).getFluid().matchesType(fluid)) {
				return 1.0F;
			}

			FluidState fluidState_1 = blockAccess.getFluidState(blockPos_2);
			if (fluidState_1.getFluid().matchesType(fluid)) {
				float float_2 = fluidState_1.method_15763(blockAccess, blockPos_2);
				if (float_2 >= 0.8F) {
					liquidHeightPercentage += float_2 * 10.0F;
					divisor += 10;
				} else {
					liquidHeightPercentage += float_2;
					++divisor;
				}
			} else if (!blockAccess.getBlockState(blockPos_2).getMaterial().method_15799()) {
				++divisor;
			}
		}
		return liquidHeightPercentage / (float) divisor;
	}

	private static boolean method_3348(BlockView blockView_1, BlockPos blockPos_1, Direction direction_1, FluidState fluidState_1) {
		BlockPos blockPos_2 = blockPos_1.offset(direction_1);
		FluidState fluidState_2 = blockView_1.getFluidState(blockPos_2);
		return fluidState_2.getFluid().matchesType(fluidState_1.getFluid());
	}

	private static boolean method_3344(BlockView blockView_1, BlockPos blockPos_1, Direction direction_1, float float_1) {
		BlockPos blockPos_2 = blockPos_1.offset(direction_1);
		BlockState blockState_1 = blockView_1.getBlockState(blockPos_2);
		if (blockState_1.isFullBoundsCubeForCulling()) {
			VoxelShape voxelShape_1 = VoxelShapes.cube(0.0D, 0.0D, 0.0D, 1.0D, (double) float_1, 1.0D);
			VoxelShape voxelShape_2 = blockState_1.method_11615(blockView_1, blockPos_2);
			return VoxelShapes.method_1083(voxelShape_1, voxelShape_2, direction_1);
		} else {
			return false;
		}
	}

	private static int method_3343(ExtendedBlockView extendedBlockView_1, BlockPos blockPos_1) {
		int int_1 = extendedBlockView_1.getLightmapIndex(blockPos_1, 0);
		int int_2 = extendedBlockView_1.getLightmapIndex(blockPos_1.up(), 0);
		int int_3 = int_1 & 255;
		int int_4 = int_2 & 255;
		int int_5 = int_1 >> 16 & 255;
		int int_6 = int_2 >> 16 & 255;
		return (int_3 > int_4 ? int_3 : int_4) | (int_5 > int_6 ? int_5 : int_6) << 16;
	}

}
