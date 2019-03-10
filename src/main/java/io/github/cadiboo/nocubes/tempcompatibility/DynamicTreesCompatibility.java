package io.github.cadiboo.nocubes.tempcompatibility;

import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.Loader;

/**
 * @author Cadiboo
 */
public class DynamicTreesCompatibility {

	public static final boolean IS_DYNAMIC_TREES_INSTALLED = Loader.isModLoaded("dynamictrees");

	public static boolean isRootyBlock(IBlockState state) {
		if (!IS_DYNAMIC_TREES_INSTALLED) {
			return false;
		}
		return state.getBlock() instanceof BlockRooty;
	}

}
