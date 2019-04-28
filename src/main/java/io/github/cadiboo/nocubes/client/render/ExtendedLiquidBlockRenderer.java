package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientProxy;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.IFluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.BiomeColors;

import javax.annotation.Nonnull;

import static net.minecraft.client.renderer.BlockFluidRenderer.func_209556_a;
import static net.minecraft.client.renderer.BlockFluidRenderer.isAdjacentFluidSameAs;

/**
 * @author Cadiboo
 */
public final class ExtendedLiquidBlockRenderer {

	public static boolean renderExtendedLiquid(
			final double x, final double y, final double z,
			@Nonnull final BlockPos fluidPos,
			@Nonnull final IWorldReader worldIn,
			//TODO: eventually do better liquid rendering for 0.3.0
			@Nonnull final IBlockState smoothableState,
			@Nonnull final IFluidState state,
			@Nonnull final BufferBuilder buffer
	) {

		final SmoothLightingBlockFluidRenderer fluidRenderer = ClientProxy.fluidRenderer;

		try (PooledMutableBlockPos renderPos = PooledMutableBlockPos.retain(x, y, z)) {
			try {
				boolean isLava = state.isTagged(FluidTags.LAVA);

				TextureAtlasSprite[] atextureatlassprite = isLava ? fluidRenderer.atlasSpritesLava : fluidRenderer.atlasSpritesWater;

				int waterColor = isLava ? 0xFFFFFF : BiomeColors.getWaterColor(worldIn, fluidPos);
				float red = ((waterColor >> 16) & 255) / 255.0F;
				float green = ((waterColor >> 8) & 255) / 255.0F;
				float blue = (waterColor & 255) / 255.0F;

				boolean renderUp = !isAdjacentFluidSameAs(worldIn, fluidPos, EnumFacing.UP, state);
				boolean renderDown = !isAdjacentFluidSameAs(worldIn, fluidPos, EnumFacing.DOWN, state) && !func_209556_a(worldIn, fluidPos, EnumFacing.DOWN, 0.8888889F);
				boolean renderNorth = !isAdjacentFluidSameAs(worldIn, fluidPos, EnumFacing.NORTH, state);
				boolean renderSouth = !isAdjacentFluidSameAs(worldIn, fluidPos, EnumFacing.SOUTH, state);
				boolean renderWest = !isAdjacentFluidSameAs(worldIn, fluidPos, EnumFacing.WEST, state);
				boolean renderEast = !isAdjacentFluidSameAs(worldIn, fluidPos, EnumFacing.EAST, state);

				if (!renderUp && !renderDown && !renderEast && !renderWest && !renderNorth && !renderSouth) {
					return false;
				}

				boolean wasAnythingRendered = false;

				float fluidHeight = fluidRenderer.getFluidHeight(worldIn, fluidPos, state.getFluid());
				float fluidHeightS = fluidRenderer.getFluidHeight(worldIn, fluidPos.south(), state.getFluid());
				float fluidHeightES = fluidRenderer.getFluidHeight(worldIn, fluidPos.east().south(), state.getFluid());
				float fluidHeightE = fluidRenderer.getFluidHeight(worldIn, fluidPos.east(), state.getFluid());

//				double x = pos.getX();
//				double y = pos.getY();
//				double z = pos.getZ();

				if (renderUp && !func_209556_a(worldIn, fluidPos, EnumFacing.UP, Math.min(Math.min(fluidHeight, fluidHeightS), Math.min(fluidHeightES, fluidHeightE)))) {
					wasAnythingRendered = true;
					fluidHeight -= 0.001F;
					fluidHeightS -= 0.001F;
					fluidHeightES -= 0.001F;
					fluidHeightE -= 0.001F;
					fluidRenderer.renderUp(worldIn, renderPos, buffer, state, atextureatlassprite, red, green, blue, fluidHeight, fluidHeightS, fluidHeightES, fluidHeightE, x, y, z, state.getFlow(worldIn, fluidPos), isLava);
				}

				if (renderDown) {
					fluidRenderer.renderDown(buffer, atextureatlassprite[0], red, green, blue, x, y, z, worldIn, renderPos, isLava);
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

					if (renderSide && !func_209556_a(worldIn, fluidPos, enumfacing, Math.max(yAdd0, yadd1))) {
						wasAnythingRendered = true;
						BlockPos blockpos = fluidPos.offset(enumfacing);
						TextureAtlasSprite textureatlassprite2 = atextureatlassprite[1];
						if (!isLava) {
							IBlockState blockstate = worldIn.getBlockState(blockpos);
							if (blockstate.getBlockFaceShape(worldIn, blockpos, enumfacing) == net.minecraft.block.state.BlockFaceShape.SOLID) {
								textureatlassprite2 = fluidRenderer.atlasSpriteWaterOverlay;
							}
						}

						fluidRenderer.renderSide(worldIn, buffer, red, green, blue, y, facingIndex, yAdd0, yadd1, d3, z0, d5, z1, blockpos, textureatlassprite2, renderPos, isLava);
					}
				}

				return wasAnythingRendered;
			} finally {
//			    OptiFineCompatibility.popShaderThing(buffer);
			}
		}
	}

}
