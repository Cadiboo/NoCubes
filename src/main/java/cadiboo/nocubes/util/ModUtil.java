package cadiboo.nocubes.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.function.BiFunction;

import cadiboo.nocubes.config.ModConfig;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlocksEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockClay;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGlowstone;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockGrassPath;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockNetherrack;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;

public class ModUtil {

	public static boolean renderBlockEnumFacing(IBlockState state, final BlockPos pos, final IBlockAccess blockAccess, final BufferBuilder bufferBuilderIn, final BlockRendererDispatcher blockRendererDispatcher, final boolean checkSides, final EnumFacing side, final boolean force) {

		try {
			final EnumBlockRenderType enumblockrendertype = state.getRenderType();

			if (enumblockrendertype == EnumBlockRenderType.INVISIBLE) {
				return false;
			} else {
				if (blockAccess.getWorldType() != WorldType.DEBUG_ALL_BLOCK_STATES) {
					try {
						state = state.getActualState(blockAccess, pos);
					} catch (final Exception var8) {
						;
					}
				}

				switch (enumblockrendertype) {
					case MODEL:
						final IBakedModel model = blockRendererDispatcher.getModelForState(state);
						return renderModel(blockAccess, model, state, pos, bufferBuilderIn, checkSides, side, force);
					case ENTITYBLOCK_ANIMATED:
						return false;
					case LIQUID:
//						return blockRendererDispatcher.renderBlock(state, pos, blockAccess, bufferBuilderIn);
						return renderFluid(blockAccess, state, pos, bufferBuilderIn, checkSides, side, force);
					default:
						return false;
				}
			}
		} catch (final Throwable throwable) {
			final CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
			final CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
			CrashReportCategory.addBlockInfo(crashreportcategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
			throw new ReportedException(crashreport);
		}
	}

	public static boolean renderFluid(final IBlockAccess blockAccess, final IBlockState stateIn, final BlockPos posIn, final BufferBuilder bufferBuilder, final boolean checkSides, final EnumFacing side, final boolean forceFull) {
		// init stuff from BlockFluidRenderer
		final BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
		final TextureAtlasSprite[] lavaSprites = new TextureAtlasSprite[2];
		final TextureAtlasSprite[] waterSprites = new TextureAtlasSprite[2];
		TextureAtlasSprite waterOverlaySprite;

		final TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
		lavaSprites[0] = texturemap.getAtlasSprite("minecraft:blocks/lava_still");
		lavaSprites[1] = texturemap.getAtlasSprite("minecraft:blocks/lava_flow");
		waterSprites[0] = texturemap.getAtlasSprite("minecraft:blocks/water_still");
		waterSprites[1] = texturemap.getAtlasSprite("minecraft:blocks/water_flow");
		waterOverlaySprite = texturemap.getAtlasSprite("minecraft:blocks/water_overlay");

		final BlockLiquid blockliquid = (BlockLiquid) stateIn.getBlock();
		final boolean isLava = stateIn.getMaterial() == Material.LAVA;
		final TextureAtlasSprite[] sprites = isLava ? lavaSprites : waterSprites;
		final int colorMultiplier = blockColors.colorMultiplier(stateIn, blockAccess, posIn, 0);
		final float redFloat = ((colorMultiplier >> 16) & 255) / 255.0F;
		final float greenFloat = ((colorMultiplier >> 8) & 255) / 255.0F;
		final float blueFloat = (colorMultiplier & 255) / 255.0F;
		final boolean shouldRenderTop;
		final boolean shouldRenderBottom;
		final boolean[] shouldRenderSide;
		if (checkSides) {
			if (forceFull) {
				shouldRenderTop = stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.UP) || (blockAccess.getBlockState(posIn.offset(EnumFacing.UP)).getMaterial() != stateIn.getMaterial());
				shouldRenderBottom = stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.DOWN) || (blockAccess.getBlockState(posIn.offset(EnumFacing.DOWN)).getMaterial() != stateIn.getMaterial());
				shouldRenderSide = new boolean[] { stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.NORTH) || (blockAccess.getBlockState(posIn.offset(EnumFacing.NORTH)).getMaterial() != stateIn.getMaterial()), stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.SOUTH) || (blockAccess.getBlockState(posIn.offset(EnumFacing.SOUTH)).getMaterial() != stateIn.getMaterial()),
						stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.WEST) || (blockAccess.getBlockState(posIn.offset(EnumFacing.WEST)).getMaterial() != stateIn.getMaterial()), stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.EAST) || (blockAccess.getBlockState(posIn.offset(EnumFacing.EAST)).getMaterial() != stateIn.getMaterial()) };

			} else {
				shouldRenderTop = stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.UP);
				shouldRenderBottom = stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.DOWN);
				shouldRenderSide = new boolean[] { stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.NORTH), stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.SOUTH), stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.WEST), stateIn.shouldSideBeRendered(blockAccess, posIn, EnumFacing.EAST) };
			}
		} else {
			shouldRenderTop = true;
			shouldRenderBottom = true;
			shouldRenderSide = new boolean[] { true, true, true, true };
		}

		if (!shouldRenderTop && !shouldRenderBottom && !shouldRenderSide[0] && !shouldRenderSide[1] && !shouldRenderSide[2] && !shouldRenderSide[3]) {
			return false;
		} else {
			boolean didRenderFluid = false;
			final Material material = stateIn.getMaterial();
			float fluidHeight = getFluidHeight(blockAccess, posIn, material);
			float fluidHeightSouth = getFluidHeight(blockAccess, posIn.south(), material);
			float fluidHeightEastSouth = getFluidHeight(blockAccess, posIn.east().south(), material);
			float fluidHeightEast = getFluidHeight(blockAccess, posIn.east(), material);
			if (forceFull) {
				fluidHeight = 0.8888889F;
				fluidHeightSouth = 0.8888889F;
				fluidHeightEastSouth = 0.8888889F;
				fluidHeightEast = 0.8888889F;
			}
			final double posX = posIn.getX();
			final double posY = posIn.getY();
			final double posZ = posIn.getZ();

			if (shouldRenderTop) {
				didRenderFluid = true;
				float liquidSlopeAngle = BlockLiquid.getSlopeAngle(blockAccess, posIn, material, stateIn);
				if (forceFull) {
					liquidSlopeAngle = -1000.0F;
				}
				final TextureAtlasSprite textureatlassprite = liquidSlopeAngle > -999.0F ? sprites[1] : sprites[0];
				fluidHeight -= 0.001F;
				fluidHeightSouth -= 0.001F;
				fluidHeightEastSouth -= 0.001F;
				fluidHeightEast -= 0.001F;
				float texU1;
				float texU2;
				float texU3;
				float texU4;
				float texV1;
				float texV2;
				float texV3;
				float texV4;

				if (liquidSlopeAngle < -999.0F) {
					texU1 = textureatlassprite.getInterpolatedU(0.0D);
					texV1 = textureatlassprite.getInterpolatedV(0.0D);
					texU2 = texU1;
					texV2 = textureatlassprite.getInterpolatedV(16.0D);
					texU3 = textureatlassprite.getInterpolatedU(16.0D);
					texV3 = texV2;
					texU4 = texU3;
					texV4 = texV1;
				} else {
					final float quarterOfSinLiquidSlopeAngle = MathHelper.sin(liquidSlopeAngle) * 0.25F;
					final float quartefOfCosLiquidSlopeAngle = MathHelper.cos(liquidSlopeAngle) * 0.25F;
					texU1 = textureatlassprite.getInterpolatedU(8.0F + ((-quartefOfCosLiquidSlopeAngle - quarterOfSinLiquidSlopeAngle) * 16.0F));
					texV1 = textureatlassprite.getInterpolatedV(8.0F + ((-quartefOfCosLiquidSlopeAngle + quarterOfSinLiquidSlopeAngle) * 16.0F));
					texU2 = textureatlassprite.getInterpolatedU(8.0F + ((-quartefOfCosLiquidSlopeAngle + quarterOfSinLiquidSlopeAngle) * 16.0F));
					texV2 = textureatlassprite.getInterpolatedV(8.0F + ((quartefOfCosLiquidSlopeAngle + quarterOfSinLiquidSlopeAngle) * 16.0F));
					texU3 = textureatlassprite.getInterpolatedU(8.0F + ((quartefOfCosLiquidSlopeAngle + quarterOfSinLiquidSlopeAngle) * 16.0F));
					texV3 = textureatlassprite.getInterpolatedV(8.0F + ((quartefOfCosLiquidSlopeAngle - quarterOfSinLiquidSlopeAngle) * 16.0F));
					texU4 = textureatlassprite.getInterpolatedU(8.0F + ((quartefOfCosLiquidSlopeAngle - quarterOfSinLiquidSlopeAngle) * 16.0F));
					texV4 = textureatlassprite.getInterpolatedV(8.0F + ((-quartefOfCosLiquidSlopeAngle - quarterOfSinLiquidSlopeAngle) * 16.0F));
				}

				final int lightmapCoords = stateIn.getPackedLightmapCoords(blockAccess, posIn);
				final int lightmapSkyLight = (lightmapCoords >> 16) & 65535;
				final int lightmapBlockLight = lightmapCoords & 65535;
				final float red = 1.0F * redFloat;
				final float green = 1.0F * greenFloat;
				final float blue = 1.0F * blueFloat;
				bufferBuilder.pos(posX + 0.0D, posY + fluidHeight, posZ + 0.0D).color(red, green, blue, 1.0F).tex(texU1, texV1).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(posX + 0.0D, posY + fluidHeightSouth, posZ + 1.0D).color(red, green, blue, 1.0F).tex(texU2, texV2).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(posX + 1.0D, posY + fluidHeightEastSouth, posZ + 1.0D).color(red, green, blue, 1.0F).tex(texU3, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(posX + 1.0D, posY + fluidHeightEast, posZ + 0.0D).color(red, green, blue, 1.0F).tex(texU4, texV4).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

				if (blockliquid.shouldRenderSides(blockAccess, posIn.up())) {
					bufferBuilder.pos(posX + 0.0D, posY + fluidHeight, posZ + 0.0D).color(red, green, blue, 1.0F).tex(texU1, texV1).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(posX + 1.0D, posY + fluidHeightEast, posZ + 0.0D).color(red, green, blue, 1.0F).tex(texU4, texV4).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(posX + 1.0D, posY + fluidHeightEastSouth, posZ + 1.0D).color(red, green, blue, 1.0F).tex(texU3, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(posX + 0.0D, posY + fluidHeightSouth, posZ + 1.0D).color(red, green, blue, 1.0F).tex(texU2, texV2).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				}
			}

			if (shouldRenderBottom) {
				final float minU = sprites[0].getMinU();
				final float maxU = sprites[0].getMaxU();
				final float minV = sprites[0].getMinV();
				final float maxV = sprites[0].getMaxV();
				final int lightmapCoords = stateIn.getPackedLightmapCoords(blockAccess, posIn.down());
				final int lightmapSkyLight = (lightmapCoords >> 16) & 65535;
				final int lightmapBlockLight = lightmapCoords & 65535;
				bufferBuilder.pos(posX, posY, posZ + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(posX, posY, posZ).color(0.5F, 0.5F, 0.5F, 1.0F).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(posX + 1.0D, posY, posZ).color(0.5F, 0.5F, 0.5F, 1.0F).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(posX + 1.0D, posY, posZ + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				didRenderFluid = true;
			}

			for (int horizontalFacingIndex = 0; horizontalFacingIndex < 4; ++horizontalFacingIndex) {
				int posAddX = 0;
				int posAddY = 0;

				if (horizontalFacingIndex == 0) {
					--posAddY;
				}

				if (horizontalFacingIndex == 1) {
					++posAddY;
				}

				if (horizontalFacingIndex == 2) {
					--posAddX;
				}

				if (horizontalFacingIndex == 3) {
					++posAddX;
				}

				final BlockPos blockpos = posIn.add(posAddX, 0, posAddY);
				TextureAtlasSprite sprite = sprites[1];

				if (!isLava) {
					final IBlockState state = blockAccess.getBlockState(blockpos);

					if (state.getBlockFaceShape(blockAccess, blockpos, EnumFacing.VALUES[horizontalFacingIndex + 2].getOpposite()) == net.minecraft.block.state.BlockFaceShape.SOLID) {
						sprite = waterOverlaySprite;
					}
				}

				if (shouldRenderSide[horizontalFacingIndex]) {
					float magicFluidHeightUsedForTextureInterpolationAndYPositioning1;
					float magicFluidHeightUsedForTextureInterpolationAndYPositioning2;
					double finalPosX1;
					double finalPosZ1;
					double finalPosX2;
					double finalPosZ2;

					if (horizontalFacingIndex == 0) {
						magicFluidHeightUsedForTextureInterpolationAndYPositioning1 = fluidHeight;
						magicFluidHeightUsedForTextureInterpolationAndYPositioning2 = fluidHeightEast;
						finalPosX1 = posX;
						finalPosX2 = posX + 1.0D;
						finalPosZ1 = posZ + 0.0010000000474974513D;
						finalPosZ2 = posZ + 0.0010000000474974513D;
					} else if (horizontalFacingIndex == 1) {
						magicFluidHeightUsedForTextureInterpolationAndYPositioning1 = fluidHeightEastSouth;
						magicFluidHeightUsedForTextureInterpolationAndYPositioning2 = fluidHeightSouth;
						finalPosX1 = posX + 1.0D;
						finalPosX2 = posX;
						finalPosZ1 = (posZ + 1.0D) - 0.0010000000474974513D;
						finalPosZ2 = (posZ + 1.0D) - 0.0010000000474974513D;
					} else if (horizontalFacingIndex == 2) {
						magicFluidHeightUsedForTextureInterpolationAndYPositioning1 = fluidHeightSouth;
						magicFluidHeightUsedForTextureInterpolationAndYPositioning2 = fluidHeight;
						finalPosX1 = posX + 0.0010000000474974513D;
						finalPosX2 = posX + 0.0010000000474974513D;
						finalPosZ1 = posZ + 1.0D;
						finalPosZ2 = posZ;
					} else {
						magicFluidHeightUsedForTextureInterpolationAndYPositioning1 = fluidHeightEast;
						magicFluidHeightUsedForTextureInterpolationAndYPositioning2 = fluidHeightEastSouth;
						finalPosX1 = (posX + 1.0D) - 0.0010000000474974513D;
						finalPosX2 = (posX + 1.0D) - 0.0010000000474974513D;
						finalPosZ1 = posZ;
						finalPosZ2 = posZ + 1.0D;
					}

					didRenderFluid = true;
					final float texU1 = sprite.getInterpolatedU(0.0D);
					final float texU2 = sprite.getInterpolatedU(8.0D);
					final float texV1 = sprite.getInterpolatedV((1.0F - magicFluidHeightUsedForTextureInterpolationAndYPositioning1) * 16.0F * 0.5F);
					final float texV2 = sprite.getInterpolatedV((1.0F - magicFluidHeightUsedForTextureInterpolationAndYPositioning2) * 16.0F * 0.5F);
					final float texV3 = sprite.getInterpolatedV(8.0D);
					final int lightmapCoords = stateIn.getPackedLightmapCoords(blockAccess, blockpos);
					final int lightmapSkyLight = (lightmapCoords >> 16) & 65535;
					final int lightmapBlockLight = lightmapCoords & 65535;
					final float f31 = horizontalFacingIndex < 2 ? 0.8F : 0.6F;
					final float red = 1.0F * f31 * redFloat;
					final float green = 1.0F * f31 * greenFloat;
					final float blue = 1.0F * f31 * blueFloat;
					bufferBuilder.pos(finalPosX1, posY + magicFluidHeightUsedForTextureInterpolationAndYPositioning1, finalPosZ1).color(red, green, blue, 1.0F).tex(texU1, texV1).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(finalPosX2, posY + magicFluidHeightUsedForTextureInterpolationAndYPositioning2, finalPosZ2).color(red, green, blue, 1.0F).tex(texU2, texV2).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(finalPosX2, posY + 0.0D, finalPosZ2).color(red, green, blue, 1.0F).tex(texU2, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					bufferBuilder.pos(finalPosX1, posY + 0.0D, finalPosZ1).color(red, green, blue, 1.0F).tex(texU1, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

					if (sprite != waterOverlaySprite) {
						bufferBuilder.pos(finalPosX1, posY + 0.0D, finalPosZ1).color(red, green, blue, 1.0F).tex(texU1, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilder.pos(finalPosX2, posY + 0.0D, finalPosZ2).color(red, green, blue, 1.0F).tex(texU2, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilder.pos(finalPosX2, posY + magicFluidHeightUsedForTextureInterpolationAndYPositioning2, finalPosZ2).color(red, green, blue, 1.0F).tex(texU2, texV2).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
						bufferBuilder.pos(finalPosX1, posY + magicFluidHeightUsedForTextureInterpolationAndYPositioning1, finalPosZ1).color(red, green, blue, 1.0F).tex(texU1, texV1).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
					}
				}
			}

			return didRenderFluid;
		}
	}

	public static float getFluidHeight(final IBlockAccess blockAccess, final BlockPos pos, final Material blockMaterial) {
		int divisor = 0;
		float numerator = 0.0F;

		for (int horizontalFacingIndex = 0; horizontalFacingIndex < 4; ++horizontalFacingIndex) {
			final BlockPos blockpos = pos.add(-(horizontalFacingIndex & 1), 0, -((horizontalFacingIndex >> 1) & 1));

			if (blockAccess.getBlockState(blockpos.up()).getMaterial() == blockMaterial) {
				return 1.0F;
			}

			final IBlockState iblockstate = blockAccess.getBlockState(blockpos);
			final Material material = iblockstate.getMaterial();

			if (material != blockMaterial) {
				if (!material.isSolid()) {
					++numerator;
					++divisor;
				}
			} else {
				final int liquidLevel = iblockstate.getValue(BlockLiquid.LEVEL).intValue();

				if ((liquidLevel >= 8) || (liquidLevel == 0)) {
					numerator += BlockLiquid.getLiquidHeightPercent(liquidLevel) * 10.0F;
					divisor += 10;
				}

				numerator += BlockLiquid.getLiquidHeightPercent(liquidLevel);
				++divisor;
			}
		}

		return 1.0F - (numerator / divisor);
	}

	public static boolean renderModel(final IBlockAccess blockAccessIn, final IBakedModel modelIn, final IBlockState blockStateIn, final BlockPos blockPosIn, final BufferBuilder buffer, final boolean checkSides, final EnumFacing side, final boolean force) {
		return renderModel(blockAccessIn, modelIn, blockStateIn, blockPosIn, buffer, checkSides, side, MathHelper.getPositionRandom(blockPosIn), force);
	}

	public static boolean renderModel(final IBlockAccess worldIn, final IBakedModel modelIn, final IBlockState stateIn, final BlockPos posIn, final BufferBuilder buffer, final boolean checkSides, final EnumFacing side, final long rand, final boolean force) {
		if (checkSides && !stateIn.shouldSideBeRendered(worldIn, posIn, side) && !force) {
			return false;
		}

		final List<BakedQuad> quads = modelIn.getQuads(stateIn, side, MathHelper.getPositionRandom(posIn));
		if (quads.size() <= 0) {
			return false;
		}
		final BakedQuad quad = quads.get(0);
		if (quad == null) {
			return false;
		}
		final TextureAtlasSprite sprite = quad.getSprite();
		if (sprite == null) {
			return false;
		}

		final float redFloat;
		final float greenFloat;
		final float blueFloat;

		if (quad.hasTintIndex()) {
			final int colorMultiplier = Minecraft.getMinecraft().getBlockColors().colorMultiplier(stateIn, worldIn, posIn, 0);
			redFloat = ((colorMultiplier >> 16) & 255) / 255.0F;
			greenFloat = ((colorMultiplier >> 8) & 255) / 255.0F;
			blueFloat = (colorMultiplier & 255) / 255.0F;
		} else {
			redFloat = 1;
			greenFloat = 1;
			blueFloat = 1;
		}

		final double x_size = 1 / 2d;
		final double y_size = 1 / 2d;
		final double z_size = 1 / 2d;
		final double x = posIn.getX() + x_size;
		final double y = posIn.getY() + y_size;
		final double z = posIn.getZ() + z_size;
		// final int red = 0xFF;
		// final int green = 0xFF;
		// final int blue = 0xFF;
		// final int red = new Random().nextInt(0xFF);
		// final int green = new Random().nextInt(0xFF);
		// final int blue = new Random().nextInt(0xFF);
		final int red = (int) (0xFF * redFloat);
		final int green = (int) (0xFF * greenFloat);
		final int blue = (int) (0xFF * blueFloat);
		final int alpha = 0xFF;
		final double minU = sprite.getMinU();
		final double maxU = sprite.getMaxU();
		final double minV = sprite.getMinV();
		final double maxV = sprite.getMaxV();
		final int lightmapSkyLight = 0xF << 4;
		final int lightmapBlockLight = 0;

		switch (side) {
			case DOWN:
				buffer.pos(-x_size + x, -y_size + y, z_size + z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(-x_size + x, -y_size + y, -z_size + z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, -y_size + y, -z_size + z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, -y_size + y, z_size + z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				break;
			case UP:
				buffer.pos(-x_size + x, y_size + y, -z_size + z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(-x_size + x, y_size + y, z_size + z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, y_size + y, z_size + z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, y_size + y, -z_size + z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				break;
			case NORTH:
				buffer.pos(-x_size + x, -y_size + y, -z_size + z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(-x_size + x, y_size + y, -z_size + z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, y_size + y, -z_size + z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, -y_size + y, -z_size + z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				break;
			case SOUTH:
				buffer.pos(x_size + x, -y_size + y, z_size + z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, y_size + y, z_size + z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(-x_size + x, y_size + y, z_size + z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(-x_size + x, -y_size + y, z_size + z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				break;
			case WEST:
				buffer.pos(-x_size + x, -y_size + y, -z_size + z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(-x_size + x, -y_size + y, z_size + z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(-x_size + x, y_size + y, z_size + z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(-x_size + x, y_size + y, -z_size + z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				break;
			case EAST:
				buffer.pos(x_size + x, -y_size + y, z_size + z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, -y_size + y, -z_size + z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, y_size + y, -z_size + z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				buffer.pos(x_size + x, y_size + y, z_size + z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				break;
			default:
				return false;
		}

		return true;
	}

	public static boolean shouldSmooth(final IBlockState state) {
		boolean smooth = false;

		smooth |= state.getBlock() instanceof BlockGrass;
		smooth |= state.getBlock() instanceof BlockStone;
		smooth |= state.getBlock() instanceof BlockSand;
		smooth |= state.getBlock() instanceof BlockSandStone;
		smooth |= state.getBlock() instanceof BlockGravel;
		smooth |= state.getBlock() instanceof BlockOre;
		smooth |= state.getBlock() instanceof BlockRedstoneOre;
		smooth |= state.getBlock() instanceof BlockSilverfish;
		smooth |= state.getBlock() instanceof BlockGrassPath;
		smooth |= state.getBlock() instanceof BlockDirt;
		smooth |= state.getBlock() instanceof BlockClay;
		smooth |= state.getBlock() instanceof BlockSnow;
		smooth |= state.getBlock() == Blocks.BEDROCK;

		smooth |= state.getBlock() instanceof BlockNetherrack;
		smooth |= state.getBlock() instanceof BlockGlowstone;

		smooth |= state.getBlock() == Blocks.END_STONE;

		smooth |= state.getBlock() instanceof BlockMycelium;

		return smooth;
	}

	public static final int	SURFACE_NETS_CUBE_EDGES_SIZE	= 24;
	public static final int	SURFACE_NETS_EDGE_TABLE_SIZE	= 256;

	// Precompute edge table, like Paul Bourke does.
	// This saves a bit of time when computing the centroid of each boundary cell
	public static final int[]	SURFACE_NETS_CUBE_EDGES	= new int[SURFACE_NETS_CUBE_EDGES_SIZE];
	public static final int[]	SURFACE_NETS_EDGE_TABLE	= new int[SURFACE_NETS_EDGE_TABLE_SIZE];

	static {
		// Initialize the cube_edges table
		// This is just the vertex number of each cube
		int cubeEdgeIndex = 0;
		for (int i = 0; i < 8; ++i) {
			for (int j = 1; j <= 4; j <<= 1) {
				final int p = i ^ j;
				if (i <= p) {
					SURFACE_NETS_CUBE_EDGES[cubeEdgeIndex++] = i;
					SURFACE_NETS_CUBE_EDGES[cubeEdgeIndex++] = p;
				}
			}
		}

		// Initialize the intersection table.
		// This is a 2^(cube configuration) -> 2^(edge configuration) map
		// There is one entry for each possible cube configuration, and the output is a 12-bit vector enumerating all edges crossing the 0-level.
		for (int edgeTableIndex = 0; edgeTableIndex < SURFACE_NETS_EDGE_TABLE_SIZE; ++edgeTableIndex) {
			int em = 0;

			for (int j = 0; j < SURFACE_NETS_CUBE_EDGES_SIZE; j += 2) {
				final boolean a = (edgeTableIndex & (1 << SURFACE_NETS_CUBE_EDGES[j])) != 0;
				final boolean b = (edgeTableIndex & (1 << SURFACE_NETS_CUBE_EDGES[j + 1])) != 0;
				em |= a != b ? 1 << (j >> 1) : 0;
			}

			SURFACE_NETS_EDGE_TABLE[edgeTableIndex] = em;
		}
	}

	/**
	 * Javascript Marching Cubes Based on Paul Bourke's classic implementation: http://local.wasp.uwa.edu.au/~pbourke/geometry/polygonise/ JS port by Mikola Lysenko
	 */

	public static final int[] MARCHING_CUBES_EDGE_TABLE = new int[] { 0x0, 0x109, 0x203, 0x30a, 0x406, 0x50f, 0x605, 0x70c, 0x80c, 0x905, 0xa0f, 0xb06, 0xc0a, 0xd03, 0xe09, 0xf00, 0x190, 0x99, 0x393, 0x29a, 0x596, 0x49f, 0x795, 0x69c, 0x99c, 0x895, 0xb9f, 0xa96, 0xd9a, 0xc93, 0xf99, 0xe90, 0x230, 0x339, 0x33, 0x13a, 0x636, 0x73f, 0x435, 0x53c, 0xa3c, 0xb35, 0x83f, 0x936, 0xe3a, 0xf33, 0xc39, 0xd30, 0x3a0, 0x2a9, 0x1a3, 0xaa, 0x7a6, 0x6af, 0x5a5, 0x4ac, 0xbac, 0xaa5, 0x9af, 0x8a6, 0xfaa, 0xea3,
			0xda9, 0xca0, 0x460, 0x569, 0x663, 0x76a, 0x66, 0x16f, 0x265, 0x36c, 0xc6c, 0xd65, 0xe6f, 0xf66, 0x86a, 0x963, 0xa69, 0xb60, 0x5f0, 0x4f9, 0x7f3, 0x6fa, 0x1f6, 0xff, 0x3f5, 0x2fc, 0xdfc, 0xcf5, 0xfff, 0xef6, 0x9fa, 0x8f3, 0xbf9, 0xaf0, 0x650, 0x759, 0x453, 0x55a, 0x256, 0x35f, 0x55, 0x15c, 0xe5c, 0xf55, 0xc5f, 0xd56, 0xa5a, 0xb53, 0x859, 0x950, 0x7c0, 0x6c9, 0x5c3, 0x4ca, 0x3c6, 0x2cf, 0x1c5, 0xcc, 0xfcc, 0xec5, 0xdcf, 0xcc6, 0xbca, 0xac3, 0x9c9, 0x8c0, 0x8c0, 0x9c9, 0xac3, 0xbca,
			0xcc6, 0xdcf, 0xec5, 0xfcc, 0xcc, 0x1c5, 0x2cf, 0x3c6, 0x4ca, 0x5c3, 0x6c9, 0x7c0, 0x950, 0x859, 0xb53, 0xa5a, 0xd56, 0xc5f, 0xf55, 0xe5c, 0x15c, 0x55, 0x35f, 0x256, 0x55a, 0x453, 0x759, 0x650, 0xaf0, 0xbf9, 0x8f3, 0x9fa, 0xef6, 0xfff, 0xcf5, 0xdfc, 0x2fc, 0x3f5, 0xff, 0x1f6, 0x6fa, 0x7f3, 0x4f9, 0x5f0, 0xb60, 0xa69, 0x963, 0x86a, 0xf66, 0xe6f, 0xd65, 0xc6c, 0x36c, 0x265, 0x16f, 0x66, 0x76a, 0x663, 0x569, 0x460, 0xca0, 0xda9, 0xea3, 0xfaa, 0x8a6, 0x9af, 0xaa5, 0xbac, 0x4ac, 0x5a5,
			0x6af, 0x7a6, 0xaa, 0x1a3, 0x2a9, 0x3a0, 0xd30, 0xc39, 0xf33, 0xe3a, 0x936, 0x83f, 0xb35, 0xa3c, 0x53c, 0x435, 0x73f, 0x636, 0x13a, 0x33, 0x339, 0x230, 0xe90, 0xf99, 0xc93, 0xd9a, 0xa96, 0xb9f, 0x895, 0x99c, 0x69c, 0x795, 0x49f, 0x596, 0x29a, 0x393, 0x99, 0x190, 0xf00, 0xe09, 0xd03, 0xc0a, 0xb06, 0xa0f, 0x905, 0x80c, 0x70c, 0x605, 0x50f, 0x406, 0x30a, 0x203, 0x109, 0x0 };

	public static final int[][] MARCHING_CUBES_TRI_TABLE = new int[][] { new int[] {}, new int[] { 0, 8, 3 }, new int[] { 0, 1, 9 }, new int[] { 1, 8, 3, 9, 8, 1 }, new int[] { 1, 2, 10 }, new int[] { 0, 8, 3, 1, 2, 10 }, new int[] { 9, 2, 10, 0, 2, 9 }, new int[] { 2, 8, 3, 2, 10, 8, 10, 9, 8 }, new int[] { 3, 11, 2 }, new int[] { 0, 11, 2, 8, 11, 0 }, new int[] { 1, 9, 0, 2, 3, 11 }, new int[] { 1, 11, 2, 1, 9, 11, 9, 8, 11 }, new int[] { 3, 10, 1, 11, 10, 3 },
			new int[] { 0, 10, 1, 0, 8, 10, 8, 11, 10 }, new int[] { 3, 9, 0, 3, 11, 9, 11, 10, 9 }, new int[] { 9, 8, 10, 10, 8, 11 }, new int[] { 4, 7, 8 }, new int[] { 4, 3, 0, 7, 3, 4 }, new int[] { 0, 1, 9, 8, 4, 7 }, new int[] { 4, 1, 9, 4, 7, 1, 7, 3, 1 }, new int[] { 1, 2, 10, 8, 4, 7 }, new int[] { 3, 4, 7, 3, 0, 4, 1, 2, 10 }, new int[] { 9, 2, 10, 9, 0, 2, 8, 4, 7 }, new int[] { 2, 10, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4 }, new int[] { 8, 4, 7, 3, 11, 2 },
			new int[] { 11, 4, 7, 11, 2, 4, 2, 0, 4 }, new int[] { 9, 0, 1, 8, 4, 7, 2, 3, 11 }, new int[] { 4, 7, 11, 9, 4, 11, 9, 11, 2, 9, 2, 1 }, new int[] { 3, 10, 1, 3, 11, 10, 7, 8, 4 }, new int[] { 1, 11, 10, 1, 4, 11, 1, 0, 4, 7, 11, 4 }, new int[] { 4, 7, 8, 9, 0, 11, 9, 11, 10, 11, 0, 3 }, new int[] { 4, 7, 11, 4, 11, 9, 9, 11, 10 }, new int[] { 9, 5, 4 }, new int[] { 9, 5, 4, 0, 8, 3 }, new int[] { 0, 5, 4, 1, 5, 0 }, new int[] { 8, 5, 4, 8, 3, 5, 3, 1, 5 },
			new int[] { 1, 2, 10, 9, 5, 4 }, new int[] { 3, 0, 8, 1, 2, 10, 4, 9, 5 }, new int[] { 5, 2, 10, 5, 4, 2, 4, 0, 2 }, new int[] { 2, 10, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8 }, new int[] { 9, 5, 4, 2, 3, 11 }, new int[] { 0, 11, 2, 0, 8, 11, 4, 9, 5 }, new int[] { 0, 5, 4, 0, 1, 5, 2, 3, 11 }, new int[] { 2, 1, 5, 2, 5, 8, 2, 8, 11, 4, 8, 5 }, new int[] { 10, 3, 11, 10, 1, 3, 9, 5, 4 }, new int[] { 4, 9, 5, 0, 8, 1, 8, 10, 1, 8, 11, 10 }, new int[] { 5, 4, 0, 5, 0, 11, 5, 11, 10, 11, 0, 3 },
			new int[] { 5, 4, 8, 5, 8, 10, 10, 8, 11 }, new int[] { 9, 7, 8, 5, 7, 9 }, new int[] { 9, 3, 0, 9, 5, 3, 5, 7, 3 }, new int[] { 0, 7, 8, 0, 1, 7, 1, 5, 7 }, new int[] { 1, 5, 3, 3, 5, 7 }, new int[] { 9, 7, 8, 9, 5, 7, 10, 1, 2 }, new int[] { 10, 1, 2, 9, 5, 0, 5, 3, 0, 5, 7, 3 }, new int[] { 8, 0, 2, 8, 2, 5, 8, 5, 7, 10, 5, 2 }, new int[] { 2, 10, 5, 2, 5, 3, 3, 5, 7 }, new int[] { 7, 9, 5, 7, 8, 9, 3, 11, 2 }, new int[] { 9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 11 },
			new int[] { 2, 3, 11, 0, 1, 8, 1, 7, 8, 1, 5, 7 }, new int[] { 11, 2, 1, 11, 1, 7, 7, 1, 5 }, new int[] { 9, 5, 8, 8, 5, 7, 10, 1, 3, 10, 3, 11 }, new int[] { 5, 7, 0, 5, 0, 9, 7, 11, 0, 1, 0, 10, 11, 10, 0 }, new int[] { 11, 10, 0, 11, 0, 3, 10, 5, 0, 8, 0, 7, 5, 7, 0 }, new int[] { 11, 10, 5, 7, 11, 5 }, new int[] { 10, 6, 5 }, new int[] { 0, 8, 3, 5, 10, 6 }, new int[] { 9, 0, 1, 5, 10, 6 }, new int[] { 1, 8, 3, 1, 9, 8, 5, 10, 6 }, new int[] { 1, 6, 5, 2, 6, 1 },
			new int[] { 1, 6, 5, 1, 2, 6, 3, 0, 8 }, new int[] { 9, 6, 5, 9, 0, 6, 0, 2, 6 }, new int[] { 5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8 }, new int[] { 2, 3, 11, 10, 6, 5 }, new int[] { 11, 0, 8, 11, 2, 0, 10, 6, 5 }, new int[] { 0, 1, 9, 2, 3, 11, 5, 10, 6 }, new int[] { 5, 10, 6, 1, 9, 2, 9, 11, 2, 9, 8, 11 }, new int[] { 6, 3, 11, 6, 5, 3, 5, 1, 3 }, new int[] { 0, 8, 11, 0, 11, 5, 0, 5, 1, 5, 11, 6 }, new int[] { 3, 11, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9 },
			new int[] { 6, 5, 9, 6, 9, 11, 11, 9, 8 }, new int[] { 5, 10, 6, 4, 7, 8 }, new int[] { 4, 3, 0, 4, 7, 3, 6, 5, 10 }, new int[] { 1, 9, 0, 5, 10, 6, 8, 4, 7 }, new int[] { 10, 6, 5, 1, 9, 7, 1, 7, 3, 7, 9, 4 }, new int[] { 6, 1, 2, 6, 5, 1, 4, 7, 8 }, new int[] { 1, 2, 5, 5, 2, 6, 3, 0, 4, 3, 4, 7 }, new int[] { 8, 4, 7, 9, 0, 5, 0, 6, 5, 0, 2, 6 }, new int[] { 7, 3, 9, 7, 9, 4, 3, 2, 9, 5, 9, 6, 2, 6, 9 }, new int[] { 3, 11, 2, 7, 8, 4, 10, 6, 5 },
			new int[] { 5, 10, 6, 4, 7, 2, 4, 2, 0, 2, 7, 11 }, new int[] { 0, 1, 9, 4, 7, 8, 2, 3, 11, 5, 10, 6 }, new int[] { 9, 2, 1, 9, 11, 2, 9, 4, 11, 7, 11, 4, 5, 10, 6 }, new int[] { 8, 4, 7, 3, 11, 5, 3, 5, 1, 5, 11, 6 }, new int[] { 5, 1, 11, 5, 11, 6, 1, 0, 11, 7, 11, 4, 0, 4, 11 }, new int[] { 0, 5, 9, 0, 6, 5, 0, 3, 6, 11, 6, 3, 8, 4, 7 }, new int[] { 6, 5, 9, 6, 9, 11, 4, 7, 9, 7, 11, 9 }, new int[] { 10, 4, 9, 6, 4, 10 }, new int[] { 4, 10, 6, 4, 9, 10, 0, 8, 3 },
			new int[] { 10, 0, 1, 10, 6, 0, 6, 4, 0 }, new int[] { 8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 10 }, new int[] { 1, 4, 9, 1, 2, 4, 2, 6, 4 }, new int[] { 3, 0, 8, 1, 2, 9, 2, 4, 9, 2, 6, 4 }, new int[] { 0, 2, 4, 4, 2, 6 }, new int[] { 8, 3, 2, 8, 2, 4, 4, 2, 6 }, new int[] { 10, 4, 9, 10, 6, 4, 11, 2, 3 }, new int[] { 0, 8, 2, 2, 8, 11, 4, 9, 10, 4, 10, 6 }, new int[] { 3, 11, 2, 0, 1, 6, 0, 6, 4, 6, 1, 10 }, new int[] { 6, 4, 1, 6, 1, 10, 4, 8, 1, 2, 1, 11, 8, 11, 1 },
			new int[] { 9, 6, 4, 9, 3, 6, 9, 1, 3, 11, 6, 3 }, new int[] { 8, 11, 1, 8, 1, 0, 11, 6, 1, 9, 1, 4, 6, 4, 1 }, new int[] { 3, 11, 6, 3, 6, 0, 0, 6, 4 }, new int[] { 6, 4, 8, 11, 6, 8 }, new int[] { 7, 10, 6, 7, 8, 10, 8, 9, 10 }, new int[] { 0, 7, 3, 0, 10, 7, 0, 9, 10, 6, 7, 10 }, new int[] { 10, 6, 7, 1, 10, 7, 1, 7, 8, 1, 8, 0 }, new int[] { 10, 6, 7, 10, 7, 1, 1, 7, 3 }, new int[] { 1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7 }, new int[] { 2, 6, 9, 2, 9, 1, 6, 7, 9, 0, 9, 3, 7, 3, 9 },
			new int[] { 7, 8, 0, 7, 0, 6, 6, 0, 2 }, new int[] { 7, 3, 2, 6, 7, 2 }, new int[] { 2, 3, 11, 10, 6, 8, 10, 8, 9, 8, 6, 7 }, new int[] { 2, 0, 7, 2, 7, 11, 0, 9, 7, 6, 7, 10, 9, 10, 7 }, new int[] { 1, 8, 0, 1, 7, 8, 1, 10, 7, 6, 7, 10, 2, 3, 11 }, new int[] { 11, 2, 1, 11, 1, 7, 10, 6, 1, 6, 7, 1 }, new int[] { 8, 9, 6, 8, 6, 7, 9, 1, 6, 11, 6, 3, 1, 3, 6 }, new int[] { 0, 9, 1, 11, 6, 7 }, new int[] { 7, 8, 0, 7, 0, 6, 3, 11, 0, 11, 6, 0 }, new int[] { 7, 11, 6 },
			new int[] { 7, 6, 11 }, new int[] { 3, 0, 8, 11, 7, 6 }, new int[] { 0, 1, 9, 11, 7, 6 }, new int[] { 8, 1, 9, 8, 3, 1, 11, 7, 6 }, new int[] { 10, 1, 2, 6, 11, 7 }, new int[] { 1, 2, 10, 3, 0, 8, 6, 11, 7 }, new int[] { 2, 9, 0, 2, 10, 9, 6, 11, 7 }, new int[] { 6, 11, 7, 2, 10, 3, 10, 8, 3, 10, 9, 8 }, new int[] { 7, 2, 3, 6, 2, 7 }, new int[] { 7, 0, 8, 7, 6, 0, 6, 2, 0 }, new int[] { 2, 7, 6, 2, 3, 7, 0, 1, 9 }, new int[] { 1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6 },
			new int[] { 10, 7, 6, 10, 1, 7, 1, 3, 7 }, new int[] { 10, 7, 6, 1, 7, 10, 1, 8, 7, 1, 0, 8 }, new int[] { 0, 3, 7, 0, 7, 10, 0, 10, 9, 6, 10, 7 }, new int[] { 7, 6, 10, 7, 10, 8, 8, 10, 9 }, new int[] { 6, 8, 4, 11, 8, 6 }, new int[] { 3, 6, 11, 3, 0, 6, 0, 4, 6 }, new int[] { 8, 6, 11, 8, 4, 6, 9, 0, 1 }, new int[] { 9, 4, 6, 9, 6, 3, 9, 3, 1, 11, 3, 6 }, new int[] { 6, 8, 4, 6, 11, 8, 2, 10, 1 }, new int[] { 1, 2, 10, 3, 0, 11, 0, 6, 11, 0, 4, 6 },
			new int[] { 4, 11, 8, 4, 6, 11, 0, 2, 9, 2, 10, 9 }, new int[] { 10, 9, 3, 10, 3, 2, 9, 4, 3, 11, 3, 6, 4, 6, 3 }, new int[] { 8, 2, 3, 8, 4, 2, 4, 6, 2 }, new int[] { 0, 4, 2, 4, 6, 2 }, new int[] { 1, 9, 0, 2, 3, 4, 2, 4, 6, 4, 3, 8 }, new int[] { 1, 9, 4, 1, 4, 2, 2, 4, 6 }, new int[] { 8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 10, 1 }, new int[] { 10, 1, 0, 10, 0, 6, 6, 0, 4 }, new int[] { 4, 6, 3, 4, 3, 8, 6, 10, 3, 0, 3, 9, 10, 9, 3 }, new int[] { 10, 9, 4, 6, 10, 4 },
			new int[] { 4, 9, 5, 7, 6, 11 }, new int[] { 0, 8, 3, 4, 9, 5, 11, 7, 6 }, new int[] { 5, 0, 1, 5, 4, 0, 7, 6, 11 }, new int[] { 11, 7, 6, 8, 3, 4, 3, 5, 4, 3, 1, 5 }, new int[] { 9, 5, 4, 10, 1, 2, 7, 6, 11 }, new int[] { 6, 11, 7, 1, 2, 10, 0, 8, 3, 4, 9, 5 }, new int[] { 7, 6, 11, 5, 4, 10, 4, 2, 10, 4, 0, 2 }, new int[] { 3, 4, 8, 3, 5, 4, 3, 2, 5, 10, 5, 2, 11, 7, 6 }, new int[] { 7, 2, 3, 7, 6, 2, 5, 4, 9 }, new int[] { 9, 5, 4, 0, 8, 6, 0, 6, 2, 6, 8, 7 },
			new int[] { 3, 6, 2, 3, 7, 6, 1, 5, 0, 5, 4, 0 }, new int[] { 6, 2, 8, 6, 8, 7, 2, 1, 8, 4, 8, 5, 1, 5, 8 }, new int[] { 9, 5, 4, 10, 1, 6, 1, 7, 6, 1, 3, 7 }, new int[] { 1, 6, 10, 1, 7, 6, 1, 0, 7, 8, 7, 0, 9, 5, 4 }, new int[] { 4, 0, 10, 4, 10, 5, 0, 3, 10, 6, 10, 7, 3, 7, 10 }, new int[] { 7, 6, 10, 7, 10, 8, 5, 4, 10, 4, 8, 10 }, new int[] { 6, 9, 5, 6, 11, 9, 11, 8, 9 }, new int[] { 3, 6, 11, 0, 6, 3, 0, 5, 6, 0, 9, 5 }, new int[] { 0, 11, 8, 0, 5, 11, 0, 1, 5, 5, 6, 11 },
			new int[] { 6, 11, 3, 6, 3, 5, 5, 3, 1 }, new int[] { 1, 2, 10, 9, 5, 11, 9, 11, 8, 11, 5, 6 }, new int[] { 0, 11, 3, 0, 6, 11, 0, 9, 6, 5, 6, 9, 1, 2, 10 }, new int[] { 11, 8, 5, 11, 5, 6, 8, 0, 5, 10, 5, 2, 0, 2, 5 }, new int[] { 6, 11, 3, 6, 3, 5, 2, 10, 3, 10, 5, 3 }, new int[] { 5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2 }, new int[] { 9, 5, 6, 9, 6, 0, 0, 6, 2 }, new int[] { 1, 5, 8, 1, 8, 0, 5, 6, 8, 3, 8, 2, 6, 2, 8 }, new int[] { 1, 5, 6, 2, 1, 6 },
			new int[] { 1, 3, 6, 1, 6, 10, 3, 8, 6, 5, 6, 9, 8, 9, 6 }, new int[] { 10, 1, 0, 10, 0, 6, 9, 5, 0, 5, 6, 0 }, new int[] { 0, 3, 8, 5, 6, 10 }, new int[] { 10, 5, 6 }, new int[] { 11, 5, 10, 7, 5, 11 }, new int[] { 11, 5, 10, 11, 7, 5, 8, 3, 0 }, new int[] { 5, 11, 7, 5, 10, 11, 1, 9, 0 }, new int[] { 10, 7, 5, 10, 11, 7, 9, 8, 1, 8, 3, 1 }, new int[] { 11, 1, 2, 11, 7, 1, 7, 5, 1 }, new int[] { 0, 8, 3, 1, 2, 7, 1, 7, 5, 7, 2, 11 },
			new int[] { 9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 11, 7 }, new int[] { 7, 5, 2, 7, 2, 11, 5, 9, 2, 3, 2, 8, 9, 8, 2 }, new int[] { 2, 5, 10, 2, 3, 5, 3, 7, 5 }, new int[] { 8, 2, 0, 8, 5, 2, 8, 7, 5, 10, 2, 5 }, new int[] { 9, 0, 1, 5, 10, 3, 5, 3, 7, 3, 10, 2 }, new int[] { 9, 8, 2, 9, 2, 1, 8, 7, 2, 10, 2, 5, 7, 5, 2 }, new int[] { 1, 3, 5, 3, 7, 5 }, new int[] { 0, 8, 7, 0, 7, 1, 1, 7, 5 }, new int[] { 9, 0, 3, 9, 3, 5, 5, 3, 7 }, new int[] { 9, 8, 7, 5, 9, 7 },
			new int[] { 5, 8, 4, 5, 10, 8, 10, 11, 8 }, new int[] { 5, 0, 4, 5, 11, 0, 5, 10, 11, 11, 3, 0 }, new int[] { 0, 1, 9, 8, 4, 10, 8, 10, 11, 10, 4, 5 }, new int[] { 10, 11, 4, 10, 4, 5, 11, 3, 4, 9, 4, 1, 3, 1, 4 }, new int[] { 2, 5, 1, 2, 8, 5, 2, 11, 8, 4, 5, 8 }, new int[] { 0, 4, 11, 0, 11, 3, 4, 5, 11, 2, 11, 1, 5, 1, 11 }, new int[] { 0, 2, 5, 0, 5, 9, 2, 11, 5, 4, 5, 8, 11, 8, 5 }, new int[] { 9, 4, 5, 2, 11, 3 }, new int[] { 2, 5, 10, 3, 5, 2, 3, 4, 5, 3, 8, 4 },
			new int[] { 5, 10, 2, 5, 2, 4, 4, 2, 0 }, new int[] { 3, 10, 2, 3, 5, 10, 3, 8, 5, 4, 5, 8, 0, 1, 9 }, new int[] { 5, 10, 2, 5, 2, 4, 1, 9, 2, 9, 4, 2 }, new int[] { 8, 4, 5, 8, 5, 3, 3, 5, 1 }, new int[] { 0, 4, 5, 1, 0, 5 }, new int[] { 8, 4, 5, 8, 5, 3, 9, 0, 5, 0, 3, 5 }, new int[] { 9, 4, 5 }, new int[] { 4, 11, 7, 4, 9, 11, 9, 10, 11 }, new int[] { 0, 8, 3, 4, 9, 7, 9, 11, 7, 9, 10, 11 }, new int[] { 1, 10, 11, 1, 11, 4, 1, 4, 0, 7, 4, 11 },
			new int[] { 3, 1, 4, 3, 4, 8, 1, 10, 4, 7, 4, 11, 10, 11, 4 }, new int[] { 4, 11, 7, 9, 11, 4, 9, 2, 11, 9, 1, 2 }, new int[] { 9, 7, 4, 9, 11, 7, 9, 1, 11, 2, 11, 1, 0, 8, 3 }, new int[] { 11, 7, 4, 11, 4, 2, 2, 4, 0 }, new int[] { 11, 7, 4, 11, 4, 2, 8, 3, 4, 3, 2, 4 }, new int[] { 2, 9, 10, 2, 7, 9, 2, 3, 7, 7, 4, 9 }, new int[] { 9, 10, 7, 9, 7, 4, 10, 2, 7, 8, 7, 0, 2, 0, 7 }, new int[] { 3, 7, 10, 3, 10, 2, 7, 4, 10, 1, 10, 0, 4, 0, 10 }, new int[] { 1, 10, 2, 8, 7, 4 },
			new int[] { 4, 9, 1, 4, 1, 7, 7, 1, 3 }, new int[] { 4, 9, 1, 4, 1, 7, 0, 8, 1, 8, 7, 1 }, new int[] { 4, 0, 3, 7, 4, 3 }, new int[] { 4, 8, 7 }, new int[] { 9, 10, 8, 10, 11, 8 }, new int[] { 3, 0, 9, 3, 9, 11, 11, 9, 10 }, new int[] { 0, 1, 10, 0, 10, 8, 8, 10, 11 }, new int[] { 3, 1, 10, 11, 3, 10 }, new int[] { 1, 2, 11, 1, 11, 9, 9, 11, 8 }, new int[] { 3, 0, 9, 3, 9, 11, 1, 2, 9, 2, 11, 9 }, new int[] { 0, 2, 11, 8, 0, 11 }, new int[] { 3, 2, 11 },
			new int[] { 2, 3, 8, 2, 8, 10, 10, 8, 9 }, new int[] { 9, 10, 2, 0, 9, 2 }, new int[] { 2, 3, 8, 2, 8, 10, 0, 1, 8, 1, 10, 8 }, new int[] { 1, 10, 2 }, new int[] { 1, 3, 8, 9, 1, 8 }, new int[] { 0, 9, 1 }, new int[] { 0, 3, 8 }, {} };

	public static final int[][] MARCHING_CUBES_CUBE_VERTS = new int[][] { new int[] { 0, 0, 0 }, new int[] { 1, 0, 0 }, new int[] { 1, 1, 0 }, new int[] { 0, 1, 0 }, new int[] { 0, 0, 1 }, new int[] { 1, 0, 1 }, new int[] { 1, 1, 1 }, new int[] { 0, 1, 1 } };

	public static final int[][] MARCHING_CUBES_EDGE_INDEX = new int[][] { new int[] { 0, 1 }, new int[] { 1, 2 }, new int[] { 2, 3 }, new int[] { 3, 0 }, new int[] { 4, 5 }, new int[] { 5, 6 }, new int[] { 6, 7 }, new int[] { 7, 4 }, new int[] { 0, 4 }, new int[] { 1, 5 }, new int[] { 2, 6 }, new int[] { 3, 7 } };

	public static float getBlockDensity(final BlockPos pos, final IBlockAccess cache) {
		float density = 0.0F;

		final MutableBlockPos mutablePos = new MutableBlockPos(pos);

		for (int x = 0; x < 2; ++x) {
			for (int y = 0; y < 2; ++y) {
				for (int z = 0; z < 2; ++z) {
					mutablePos.setPos(pos.getX() - x, pos.getY() - y, pos.getZ() - z);

					final IBlockState state = cache.getBlockState(mutablePos);

					if (ModUtil.shouldSmooth(state)) {
						density += 1;
					} else if (state.isNormalCube()) {

					} else if (state.getBlock().getMaterial(state) == Material.VINE) {
						density -= 0.75;
					} else {
						density -= 1;
					}

					if (state.getBlock() == Blocks.BEDROCK) {
						density += 0.000000000000000000000000000000000000000000001f;
					}

				}
			}
		}

		return density;
	}

	public static class SurfaceNet {
		public final List<BlockPosSurfaceNetInfo> posInfos;

		public SurfaceNet(final List<BlockPosSurfaceNetInfo> posInfos) {
			this.posInfos = posInfos;
		}
	}

	public static class BlockPosSurfaceNetInfo {
		public final BlockPos				pos;
		public final IBlockState			state;
		public final BlockPos				brightnessPos;
		public final List<QuadVertexList>	vertexList;

		public BlockPosSurfaceNetInfo(final BlockPos pos, final IBlockState state, final BlockPos brightnessPos, final List<QuadVertexList> quadVertexList) {
			this.pos = pos;
			this.state = state;
			this.brightnessPos = brightnessPos;
			this.vertexList = quadVertexList;
		}
	}

	public static class QuadVertexList {
		public final Vec3d	vertex1;
		public final Vec3d	vertex2;
		public final Vec3d	vertex3;
		public final Vec3d	vertex4;

		public QuadVertexList(final Vec3d vertex1, final Vec3d vertex2, final Vec3d vertex3, final Vec3d vertex4) {
			this.vertex1 = vertex1;
			this.vertex2 = vertex2;
			this.vertex3 = vertex3;
			this.vertex4 = vertex4;
		}

		public Vec3d[] getVertexes() {
			return new Vec3d[] { this.vertex1, this.vertex2, this.vertex3, this.vertex4 };
		}
	}

	public static SurfaceNet generateSurfaceNet(final BlockPos startingPositionIn, final IBlockAccess cache, final BiFunction<BlockPos, IBlockAccess, Float> potential) {
		// dims: "A 3D vector of integers representing the resolution of the isosurface". Resolution in our context means size
		final int[] dims = new int[] { 16, 16, 16 };
		final int[] startPos = new int[] { startingPositionIn.getX(), startingPositionIn.getY(), startingPositionIn.getZ() };
		final int[] currentPos = new int[3];
		final int[] edgesIThink = new int[] { 1, dims[0] + 3, (dims[0] + 3) * (dims[1] + 3) };
		final float[] grid = new float[8];
		final float[][] buffer = new float[edgesIThink[2] * 2][3];
		int bufno = 1;

		// "Resize buffer if necessary" is what mikolalysenko said, but Click_Me seems to have removed this code. This is probably because the buffer should never (and actually
		// can't be in java) be resized
		final ArrayList<BlockPosSurfaceNetInfo> posInfos = new ArrayList<>();
		// March over the voxel grid
		for (currentPos[2] = 0; currentPos[2] < (dims[2] + 1); edgesIThink[2] = -edgesIThink[2], ++currentPos[2], bufno ^= 1) {

			// m is the pointer into the buffer we are going to use.
			// "This is slightly obtuse because javascript does not have good support for packed data structures, so we must use typed arrays :(" is what mikolalysenko said, it
			// obviously doesn't apply here
			// The contents of the buffer will be the indices of the vertices on the previous x/y slice of the volume
			int m = 1 + ((dims[0] + 3) * (1 + (bufno * (dims[1] + 3))));

			for (currentPos[1] = 0; currentPos[1] < (dims[1] + 1); ++currentPos[1], m += 2) {
				for (currentPos[0] = 0; currentPos[0] < (dims[0] + 1); ++currentPos[0], ++m) {

					// Read in 8 field values around this vertex and store them in an array
					// Also calculate 8-bit mask, like in marching cubes, so we can speed up sign checks later
					int mask = 0;
					int g = 0;

					for (int z = 0; z < 2; ++z) {
						for (int y = 0; y < 2; ++y) {
							for (int x = 0; x < 2; ++g) {
								// TODO: mutableblockpos?
								final float p = potential.apply(new BlockPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z), cache);

								// final float p = getBlockDensity(new BlockPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z),
								// cache);
								grid[g] = p;
								mask |= p > 0.0F ? 1 << g : 0;
								++x;

							}
						}
					}

					// Check for early termination if cell does not intersect boundary
					if ((mask == 0) || (mask == 0xFF)) {
						continue;
					}

					IBlockState stateActual = Blocks.AIR.getDefaultState();

					final MutableBlockPos pos = new MutableBlockPos();
					getStateAndPos: for (int y = -1; y < 2; ++y) {
						for (int z = -1; z < 2; ++z) {
							for (int x = -1; x < 2; ++x) {
								pos.setPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z);
								final IBlockState tempStateActual = cache.getBlockState(pos).getActualState(cache, pos);

								// if (shouldSmooth(tempState) && (state.getBlock() != Blocks.GRASS))
								// {
								// state = tempState;
								// if ((tempState.getBlock() == Blocks.GRASS))
								// {
								// break getStateAndPos;
								// }
								// }

								if (shouldSmooth(tempStateActual) && (stateActual.getBlock() != Blocks.SNOW_LAYER) && (stateActual.getBlock() != Blocks.GRASS)) {
									stateActual = tempStateActual;
									if ((tempStateActual.getBlock() == Blocks.SNOW_LAYER) || (tempStateActual.getBlock() == Blocks.GRASS)) {
										break getStateAndPos;
									}
								}
							}
						}
					}

					final int[] brightnessPos = new int[] { startPos[0] + currentPos[0], startPos[1] + currentPos[1] + 1, startPos[2] + currentPos[2] };

					getBrightnessPos: for (int y = -1; y < 2; ++y) {
						for (int z = -2; z < 3; ++z) {
							for (int x = -1; x < 2; ++x) {
								// TODO: mutableblockpos?
								final IBlockState tempState = cache.getBlockState(new BlockPos(startPos[0] + currentPos[0] + x, startPos[1] + currentPos[1] + y, startPos[2] + currentPos[2] + z));
								if (!tempState.isOpaqueCube()) {
									brightnessPos[0] = startPos[0] + currentPos[0] + x;
									brightnessPos[1] = startPos[1] + currentPos[1] + y;
									brightnessPos[2] = startPos[2] + currentPos[2] + z;
									break getBrightnessPos;
								}
							}
						}
					}

					// Sum up edge intersections
					final int edge_mask = SURFACE_NETS_EDGE_TABLE[mask];
					int e_count = 0;
					final float[] v = new float[] { 0.0F, 0.0F, 0.0F };

					// For every edge of the cube...
					for (int i = 0; i < 12; ++i) {

						// Use edge mask to check if it is crossed
						if ((edge_mask & (1 << i)) == 0) {
							continue;
						}

						// If it did, increment number of edge crossings
						++e_count;

						// Now find the point of intersection
						final int e0 = SURFACE_NETS_CUBE_EDGES[i << 1]; // Unpack vertices
						final int e1 = SURFACE_NETS_CUBE_EDGES[(i << 1) + 1];
						final float g0 = grid[e0]; // Unpack grid values
						final float g1 = grid[e1];
						float t = g0 - g1; // Compute point of intersection
						if (Math.abs(t) > 0.0F) {
							t = g0 / t;
							int j = 0;

							// Interpolate vertices and add up intersections (this can be done without multiplying)
							for (int k = 1; j < 3; k <<= 1) {
								final int a = e0 & k;
								final int b = e1 & k;
								if (a != b) {
									v[j] += a != 0 ? 1.0F - t : t;
								} else {
									v[j] += a != 0 ? 1.0F : 0.0F;
								}

								++j;
							}

						}
					}

					// Now we just average the edge intersections and add them to coordinate
					final float s = 1.0F / e_count;
					for (int i = 0; i < 3; ++i) {
						v[i] = startPos[i] + currentPos[i] + (s * v[i]);
					}

					final int tx = currentPos[0] == 16 ? 0 : currentPos[0];
					final int ty = currentPos[1] == 16 ? 0 : currentPos[1];
					final int tz = currentPos[2] == 16 ? 0 : currentPos[2];
					long i1 = (tx * 3129871) ^ (tz * 116129781L) ^ ty;
					i1 = (i1 * i1 * 42317861L) + (i1 * 11L);
					v[0] = (float) (v[0] - (((((i1 >> 16) & 15L) / 15.0F) - 0.5D) * 0.2D));
					v[1] = (float) (v[1] - (((((i1 >> 20) & 15L) / 15.0F) - 1.0D) * 0.2D));
					v[2] = (float) (v[2] - (((((i1 >> 24) & 15L) / 15.0F) - 0.5D) * 0.2D));

					// "Add vertex to buffer, store pointer to vertex index in buffer" is what mikolalysenko said, but Click_Me seems to have changed something

					buffer[m] = v;

					final BlockPos brightnessBlockPos = new BlockPos(brightnessPos[0], brightnessPos[1], brightnessPos[2]);

					final ArrayList<QuadVertexList> vertexes = new ArrayList<>();

					// Now we need to add faces together, to do this we just loop over 3 basis components
					for (int axis = 0; axis < 3; ++axis) {
						// The first three entries of the edge_mask count the crossings along the edge
						if ((edge_mask & (1 << axis)) == 0) {
							continue;
						}

						// i = axes we are point along. iu, iv = orthogonal axes
						final int iu = (axis + 1) % 3;
						final int iv = (axis + 2) % 3;

						// If we are on a boundary, skip it
						if ((currentPos[iu] == 0) || (currentPos[iv] == 0)) {
							continue;
						}

						// Otherwise, look up adjacent edges in buffer
						final int du = edgesIThink[iu];
						final int dv = edgesIThink[iv];

						final float[] v0 = buffer[m];
						final float[] v1 = buffer[m - du];
						final float[] v2 = buffer[m - du - dv];
						final float[] v3 = buffer[m - dv];

						final QuadVertexList vertexList;

						// Remember to flip orientation depending on the sign of the corner.
						if ((mask & 1) != 0) {
							vertexList = new QuadVertexList(new Vec3d(v0[0], v0[1], v0[2]), new Vec3d(v1[0], v1[1], v1[2]), new Vec3d(v2[0], v2[1], v2[2]), new Vec3d(v3[0], v3[1], v3[2]));

						} else {
							vertexList = new QuadVertexList(new Vec3d(v0[0], v0[1], v0[2]), new Vec3d(v3[0], v3[1], v3[2]), new Vec3d(v2[0], v2[1], v2[2]), new Vec3d(v1[0], v1[1], v1[2]));

						}

						vertexes.add(vertexList);
					}

					final BlockPosSurfaceNetInfo posInfo = new BlockPosSurfaceNetInfo(pos, stateActual, brightnessBlockPos, vertexes);

					posInfos.add(posInfo);

				}

			}
		}
		return new SurfaceNet(posInfos);
	}

	public static void smoothWater(final RebuildChunkBlocksEvent event) {

		if (!ModConfig.shouldSmoothWater) {
			return;
		}

		final ChunkCache cache = event.getWorldView();

		final Hashtable<BlockPos, Tuple<IBlockState, BlockPos>> redoPositions = new Hashtable<>();

		for (final BlockPos pos : event.getChunkBlockPositions()) {
			final IBlockState state = cache.getBlockState(pos);

			final Block block = state.getBlock();

			if (ModUtil.shouldSmooth(state)) {
				continue;
			}

			if (block instanceof BlockLiquid) {
				if (state.getValue(BlockLiquid.LEVEL) == 0) {
					boolean shouldExtend = false;
					for (final EnumFacing facing : EnumFacing.VALUES) {
						if (facing == EnumFacing.UP) {
							continue;
						}
						final BlockPos offset = pos.offset(facing);

						shouldExtend |= cache.getBlockState(offset).getBlock() == state.getBlock();
					}

					if (shouldExtend) {
						for (final EnumFacing facing : EnumFacing.VALUES) {
							if (facing == EnumFacing.UP) {
								continue;
							}
							final BlockPos offset = pos.offset(facing);

							if (!ModUtil.shouldSmooth(cache.getBlockState(offset))) {
								continue;
							}

							redoPositions.put(offset, new Tuple<>(state.getActualState(cache, pos), pos.toImmutable()));
						}
					}
				}
			}

			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!block.canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
					final BufferBuilder bufferBuilder = event.startOrContinueLayer(blockRenderLayer);

					final boolean wasLayerUsed = event.getBlockRendererDispatcher().renderBlock(state, pos, cache, bufferBuilder);

					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, wasLayerUsed);

				}
			}
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		}

		for (final BlockPos pos : redoPositions.keySet()) {

			// make sure that the position is in the same chunk as our chunk
			if (!new ChunkPos(event.getRenderChunkPosition().toImmutable()).equals(new ChunkPos(pos.toImmutable()))) {
				continue;
			}

			final Tuple<IBlockState, BlockPos> stateAndOldPos = redoPositions.get(pos);
			final IBlockState state = stateAndOldPos.getFirst();
			final BlockPos oldPos = stateAndOldPos.getSecond();
			final Block block = state.getBlock();
			for (final BlockRenderLayer blockRenderLayer : BlockRenderLayer.values()) {
				if (!block.canRenderInLayer(state, blockRenderLayer)) {
					continue;
				}
				net.minecraftforge.client.ForgeHooksClient.setRenderLayer(blockRenderLayer);

				if (state.getRenderType() != EnumBlockRenderType.INVISIBLE) {
					final BufferBuilder bufferBuilder = event.startOrContinueLayer(blockRenderLayer);

					boolean wasLayerUsed = false;
					if (!(block instanceof BlockLiquid)) {
						wasLayerUsed = event.getBlockRendererDispatcher().renderBlock(state, pos, cache, bufferBuilder);
					} else {
						// init stuff from BlockFluidRenderer
						final BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
						final TextureAtlasSprite[] lavaSprites = new TextureAtlasSprite[2];
						final TextureAtlasSprite[] waterSprites = new TextureAtlasSprite[2];
						TextureAtlasSprite waterOverlaySprite;

						final TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
						lavaSprites[0] = texturemap.getAtlasSprite("minecraft:blocks/lava_still");
						lavaSprites[1] = texturemap.getAtlasSprite("minecraft:blocks/lava_flow");
						waterSprites[0] = texturemap.getAtlasSprite("minecraft:blocks/water_still");
						waterSprites[1] = texturemap.getAtlasSprite("minecraft:blocks/water_flow");
						waterOverlaySprite = texturemap.getAtlasSprite("minecraft:blocks/water_overlay");

						final BlockLiquid blockliquid = (BlockLiquid) state.getBlock();
						final boolean isLava = state.getMaterial() == Material.LAVA;
						final TextureAtlasSprite[] sprites = isLava ? lavaSprites : waterSprites;
						final int colorMultiplier = blockColors.colorMultiplier(state, cache, oldPos, 0);
						final float redFloat = ((colorMultiplier >> 16) & 255) / 255.0F;
						final float greenFloat = ((colorMultiplier >> 8) & 255) / 255.0F;
						final float blueFloat = (colorMultiplier & 255) / 255.0F;
						final boolean shouldRenderTop;
						final boolean shouldRenderBottom;
						final boolean[] shouldRenderSide;

						shouldRenderTop = state.shouldSideBeRendered(cache, pos, EnumFacing.UP) || ((cache.getBlockState(pos.offset(EnumFacing.UP)).getMaterial() != state.getMaterial()) && !ModUtil.shouldSmooth(cache.getBlockState(pos.offset(EnumFacing.UP))));
						shouldRenderBottom = state.shouldSideBeRendered(cache, pos, EnumFacing.DOWN) || ((cache.getBlockState(pos.offset(EnumFacing.DOWN)).getMaterial() != state.getMaterial()) && !ModUtil.shouldSmooth(cache.getBlockState(pos.offset(EnumFacing.DOWN))));
						shouldRenderSide = new boolean[] { state.shouldSideBeRendered(cache, pos, EnumFacing.NORTH) || ((cache.getBlockState(pos.offset(EnumFacing.NORTH)).getMaterial() != state.getMaterial()) && !ModUtil.shouldSmooth(cache.getBlockState(pos.offset(EnumFacing.NORTH)))),
								state.shouldSideBeRendered(cache, pos, EnumFacing.SOUTH) || ((cache.getBlockState(pos.offset(EnumFacing.SOUTH)).getMaterial() != state.getMaterial()) && !ModUtil.shouldSmooth(cache.getBlockState(pos.offset(EnumFacing.SOUTH)))), state.shouldSideBeRendered(cache, pos, EnumFacing.WEST) || ((cache.getBlockState(pos.offset(EnumFacing.WEST)).getMaterial() != state.getMaterial()) && !ModUtil.shouldSmooth(cache.getBlockState(pos.offset(EnumFacing.WEST)))),
								state.shouldSideBeRendered(cache, pos, EnumFacing.EAST) || ((cache.getBlockState(pos.offset(EnumFacing.EAST)).getMaterial() != state.getMaterial()) && !ModUtil.shouldSmooth(cache.getBlockState(pos.offset(EnumFacing.EAST)))) };

						boolean didRenderFluid = false;
						final Material material = state.getMaterial();
						float fluidHeight = ModUtil.getFluidHeight(cache, pos, material);
						float fluidHeightSouth = ModUtil.getFluidHeight(cache, pos.south(), material);
						float fluidHeightEastSouth = ModUtil.getFluidHeight(cache, pos.east().south(), material);
						float fluidHeightEast = ModUtil.getFluidHeight(cache, pos.east(), material);
						if (true) {
							fluidHeight = 0.8888889F;
							fluidHeightSouth = 0.8888889F;
							fluidHeightEastSouth = 0.8888889F;
							fluidHeightEast = 0.8888889F;
						}
						final double posX = pos.getX();
						final double posY = pos.getY();
						final double posZ = pos.getZ();

						if (shouldRenderTop) {
							didRenderFluid = true;
							float liquidSlopeAngle = BlockLiquid.getSlopeAngle(cache, pos, material, state);
							if (true) {
								liquidSlopeAngle = -1000.0F;
							}
							final TextureAtlasSprite textureatlassprite = liquidSlopeAngle > -999.0F ? sprites[1] : sprites[0];
							fluidHeight -= 0.001F;
							fluidHeightSouth -= 0.001F;
							fluidHeightEastSouth -= 0.001F;
							fluidHeightEast -= 0.001F;
							float texU1;
							float texU2;
							float texU3;
							float texU4;
							float texV1;
							float texV2;
							float texV3;
							float texV4;

							if (liquidSlopeAngle < -999.0F) {
								texU1 = textureatlassprite.getInterpolatedU(0.0D);
								texV1 = textureatlassprite.getInterpolatedV(0.0D);
								texU2 = texU1;
								texV2 = textureatlassprite.getInterpolatedV(16.0D);
								texU3 = textureatlassprite.getInterpolatedU(16.0D);
								texV3 = texV2;
								texU4 = texU3;
								texV4 = texV1;
							} else {
								final float quarterOfSinLiquidSlopeAngle = MathHelper.sin(liquidSlopeAngle) * 0.25F;
								final float quartefOfCosLiquidSlopeAngle = MathHelper.cos(liquidSlopeAngle) * 0.25F;
								texU1 = textureatlassprite.getInterpolatedU(8.0F + ((-quartefOfCosLiquidSlopeAngle - quarterOfSinLiquidSlopeAngle) * 16.0F));
								texV1 = textureatlassprite.getInterpolatedV(8.0F + ((-quartefOfCosLiquidSlopeAngle + quarterOfSinLiquidSlopeAngle) * 16.0F));
								texU2 = textureatlassprite.getInterpolatedU(8.0F + ((-quartefOfCosLiquidSlopeAngle + quarterOfSinLiquidSlopeAngle) * 16.0F));
								texV2 = textureatlassprite.getInterpolatedV(8.0F + ((quartefOfCosLiquidSlopeAngle + quarterOfSinLiquidSlopeAngle) * 16.0F));
								texU3 = textureatlassprite.getInterpolatedU(8.0F + ((quartefOfCosLiquidSlopeAngle + quarterOfSinLiquidSlopeAngle) * 16.0F));
								texV3 = textureatlassprite.getInterpolatedV(8.0F + ((quartefOfCosLiquidSlopeAngle - quarterOfSinLiquidSlopeAngle) * 16.0F));
								texU4 = textureatlassprite.getInterpolatedU(8.0F + ((quartefOfCosLiquidSlopeAngle - quarterOfSinLiquidSlopeAngle) * 16.0F));
								texV4 = textureatlassprite.getInterpolatedV(8.0F + ((-quartefOfCosLiquidSlopeAngle - quarterOfSinLiquidSlopeAngle) * 16.0F));
							}

							final int lightmapCoords = state.getPackedLightmapCoords(cache, oldPos);
							final int lightmapSkyLight = (lightmapCoords >> 16) & 65535;
							final int lightmapBlockLight = lightmapCoords & 65535;
							final float red = 1.0F * redFloat;
							final float green = 1.0F * greenFloat;
							final float blue = 1.0F * blueFloat;
							bufferBuilder.pos(posX + 0.0D, posY + fluidHeight, posZ + 0.0D).color(red, green, blue, 1.0F).tex(texU1, texV1).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(posX + 0.0D, posY + fluidHeightSouth, posZ + 1.0D).color(red, green, blue, 1.0F).tex(texU2, texV2).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(posX + 1.0D, posY + fluidHeightEastSouth, posZ + 1.0D).color(red, green, blue, 1.0F).tex(texU3, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(posX + 1.0D, posY + fluidHeightEast, posZ + 0.0D).color(red, green, blue, 1.0F).tex(texU4, texV4).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

							if (blockliquid.shouldRenderSides(cache, pos.up())) {
								bufferBuilder.pos(posX + 0.0D, posY + fluidHeight, posZ + 0.0D).color(red, green, blue, 1.0F).tex(texU1, texV1).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(posX + 1.0D, posY + fluidHeightEast, posZ + 0.0D).color(red, green, blue, 1.0F).tex(texU4, texV4).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(posX + 1.0D, posY + fluidHeightEastSouth, posZ + 1.0D).color(red, green, blue, 1.0F).tex(texU3, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(posX + 0.0D, posY + fluidHeightSouth, posZ + 1.0D).color(red, green, blue, 1.0F).tex(texU2, texV2).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							}
						}

						if (shouldRenderBottom) {
							final float minU = sprites[0].getMinU();
							final float maxU = sprites[0].getMaxU();
							final float minV = sprites[0].getMinV();
							final float maxV = sprites[0].getMaxV();
							final int lightmapCoords = state.getPackedLightmapCoords(cache, oldPos.down());
							final int lightmapSkyLight = (lightmapCoords >> 16) & 65535;
							final int lightmapBlockLight = lightmapCoords & 65535;
							bufferBuilder.pos(posX, posY, posZ + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(posX, posY, posZ).color(0.5F, 0.5F, 0.5F, 1.0F).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(posX + 1.0D, posY, posZ).color(0.5F, 0.5F, 0.5F, 1.0F).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							bufferBuilder.pos(posX + 1.0D, posY, posZ + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							didRenderFluid = true;
						}

						for (int horizontalFacingIndex = 0; horizontalFacingIndex < 4; ++horizontalFacingIndex) {
							int posAddX = 0;
							int posAddY = 0;

							if (horizontalFacingIndex == 0) {
								--posAddY;
							}

							if (horizontalFacingIndex == 1) {
								++posAddY;
							}

							if (horizontalFacingIndex == 2) {
								--posAddX;
							}

							if (horizontalFacingIndex == 3) {
								++posAddX;
							}

							final BlockPos blockpos = pos.add(posAddX, 0, posAddY);
							TextureAtlasSprite sprite = sprites[1];

							if (!isLava) {
								final IBlockState state2 = cache.getBlockState(blockpos);

								if (state2.getBlockFaceShape(cache, blockpos, EnumFacing.VALUES[horizontalFacingIndex + 2].getOpposite()) == net.minecraft.block.state.BlockFaceShape.SOLID) {
									sprite = waterOverlaySprite;
								}
							}

							if (shouldRenderSide[horizontalFacingIndex]) {
								float magicFluidHeightUsedForTextureInterpolationAndYPositioning1;
								float magicFluidHeightUsedForTextureInterpolationAndYPositioning2;
								double finalPosX1;
								double finalPosZ1;
								double finalPosX2;
								double finalPosZ2;

								if (horizontalFacingIndex == 0) {
									magicFluidHeightUsedForTextureInterpolationAndYPositioning1 = fluidHeight;
									magicFluidHeightUsedForTextureInterpolationAndYPositioning2 = fluidHeightEast;
									finalPosX1 = posX;
									finalPosX2 = posX + 1.0D;
									finalPosZ1 = posZ + 0.0010000000474974513D;
									finalPosZ2 = posZ + 0.0010000000474974513D;
								} else if (horizontalFacingIndex == 1) {
									magicFluidHeightUsedForTextureInterpolationAndYPositioning1 = fluidHeightEastSouth;
									magicFluidHeightUsedForTextureInterpolationAndYPositioning2 = fluidHeightSouth;
									finalPosX1 = posX + 1.0D;
									finalPosX2 = posX;
									finalPosZ1 = (posZ + 1.0D) - 0.0010000000474974513D;
									finalPosZ2 = (posZ + 1.0D) - 0.0010000000474974513D;
								} else if (horizontalFacingIndex == 2) {
									magicFluidHeightUsedForTextureInterpolationAndYPositioning1 = fluidHeightSouth;
									magicFluidHeightUsedForTextureInterpolationAndYPositioning2 = fluidHeight;
									finalPosX1 = posX + 0.0010000000474974513D;
									finalPosX2 = posX + 0.0010000000474974513D;
									finalPosZ1 = posZ + 1.0D;
									finalPosZ2 = posZ;
								} else {
									magicFluidHeightUsedForTextureInterpolationAndYPositioning1 = fluidHeightEast;
									magicFluidHeightUsedForTextureInterpolationAndYPositioning2 = fluidHeightEastSouth;
									finalPosX1 = (posX + 1.0D) - 0.0010000000474974513D;
									finalPosX2 = (posX + 1.0D) - 0.0010000000474974513D;
									finalPosZ1 = posZ;
									finalPosZ2 = posZ + 1.0D;
								}

								didRenderFluid = true;
								final float texU1 = sprite.getInterpolatedU(0.0D);
								final float texU2 = sprite.getInterpolatedU(8.0D);
								final float texV1 = sprite.getInterpolatedV((1.0F - magicFluidHeightUsedForTextureInterpolationAndYPositioning1) * 16.0F * 0.5F);
								final float texV2 = sprite.getInterpolatedV((1.0F - magicFluidHeightUsedForTextureInterpolationAndYPositioning2) * 16.0F * 0.5F);
								final float texV3 = sprite.getInterpolatedV(8.0D);
								final int lightmapCoords = state.getPackedLightmapCoords(cache, oldPos.add(posAddX, 0, posAddY));
								final int lightmapSkyLight = (lightmapCoords >> 16) & 65535;
								final int lightmapBlockLight = lightmapCoords & 65535;
								final float f31 = horizontalFacingIndex < 2 ? 0.8F : 0.6F;
								final float red = 1.0F * f31 * redFloat;
								final float green = 1.0F * f31 * greenFloat;
								final float blue = 1.0F * f31 * blueFloat;
								bufferBuilder.pos(finalPosX1, posY + magicFluidHeightUsedForTextureInterpolationAndYPositioning1, finalPosZ1).color(red, green, blue, 1.0F).tex(texU1, texV1).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(finalPosX2, posY + magicFluidHeightUsedForTextureInterpolationAndYPositioning2, finalPosZ2).color(red, green, blue, 1.0F).tex(texU2, texV2).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(finalPosX2, posY + 0.0D, finalPosZ2).color(red, green, blue, 1.0F).tex(texU2, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(finalPosX1, posY + 0.0D, finalPosZ1).color(red, green, blue, 1.0F).tex(texU1, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

								if (sprite != waterOverlaySprite) {
									bufferBuilder.pos(finalPosX1, posY + 0.0D, finalPosZ1).color(red, green, blue, 1.0F).tex(texU1, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
									bufferBuilder.pos(finalPosX2, posY + 0.0D, finalPosZ2).color(red, green, blue, 1.0F).tex(texU2, texV3).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
									bufferBuilder.pos(finalPosX2, posY + magicFluidHeightUsedForTextureInterpolationAndYPositioning2, finalPosZ2).color(red, green, blue, 1.0F).tex(texU2, texV2).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
									bufferBuilder.pos(finalPosX1, posY + magicFluidHeightUsedForTextureInterpolationAndYPositioning1, finalPosZ1).color(red, green, blue, 1.0F).tex(texU1, texV1).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								}

							}

							wasLayerUsed |= didRenderFluid;
						}

					}

					event.setBlockRenderLayerUsedWithOrOpperation(blockRenderLayer, wasLayerUsed);

				}
			}
			net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
		}
	}

}
