package io.github.cadiboo.nocubes.tempcompatibility;

import io.github.cadiboo.nocubes.util.ReflectionClassHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindClassException;

/**
 * @author Cadiboo
 */
public final class DynamicTreesCompatibility {

	public static final boolean IS_DYNAMIC_TREES_INSTALLED = Loader.isModLoaded("dynamictrees");
	private static final Class<? super Block> BLOCK_ROOTY;
	static {
		Class<? super Block> tempBlockRooty = null;
		try {
			tempBlockRooty = ReflectionClassHelper.findClass(DynamicTreesCompatibility.class.getClassLoader(), "com.ferreusveritas.dynamictrees.blocks.BlockRooty");
		} catch (UnableToFindClassException e) {
			// dynamictrees is not installed
		}
		BLOCK_ROOTY = tempBlockRooty;
	}

	public static boolean isRootyBlock(IBlockState state) {
		if (!IS_DYNAMIC_TREES_INSTALLED) {
			return false;
		}
		//return state.getBlock() instanceof BlockRooty;
		return BLOCK_ROOTY.isInstance(state.getBlock());
	}

}
