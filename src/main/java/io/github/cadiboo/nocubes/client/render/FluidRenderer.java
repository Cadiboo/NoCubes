package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.util.Area;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.FluidBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fluids.FluidAttributes;

public class FluidRenderer extends FluidBlockRenderer {

	public static void render(Area area, LightCache light) {
		BlockPos size = area.size;
		int depth = size.getZ();
		int height = size.getY();
		int width = size.getX();

		BlockState[] blocks = area.getAndCacheBlocks();
		BlockPos.Mutable pos = new BlockPos.Mutable();

		int index = 0;
		for (int z = 0; z < depth; ++z) {
			for (int y = 0; y < height; ++y) {
				for (int x = 0; x < width; ++x, ++index) {
					if (isOutsideArea(x, y, z, size))
						continue;
					BlockState block = blocks[index];
					FluidState fluid = block.getFluidState();

				}
			}
		}
	}

	private static boolean isOutsideArea(int x, int y, int z, BlockPos size) {
		return x < 1 || y < 1 || z < 1 || x >= size.getX() - 1 || y >= size.getY() - 1 || z >= size.getZ() - 1;
	}


	private static TextureAtlasSprite[] getStaticAndFlowingSprites(FluidBlockRenderer fluidRenderer, IWorld world, BlockState block, FluidState fluid, BlockPos worldPos) {
		return ForgeHooksClient.getFluidSprites(world, worldPos, fluid);
	}

	private static void setColor(Color color, FluidState fluid, IBlockDisplayReader world, BlockPos worldPos) {
		FluidAttributes attributes = fluid.getType().getAttributes();
		color.unpackFromARGB(attributes.getColor(world, worldPos));
	}

}
