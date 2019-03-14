package io.github.cadiboo.nocubes.tempcompatibility;

import io.github.cadiboo.nocubes.util.reflect.ObfuscationReflectionHelperCopy;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.ReflectionHelper.UnableToFindClassException;

/**
 * @author Cadiboo
 */
public class DynamicTreesCompatibility {

	private static final Class<? super Block> BLOCK_ROOTY;
	static {
		Class<? super Block> tempBlockRooty = null;
		try {
			tempBlockRooty = ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), "com.ferreusveritas.dynamictrees.blocks.BlockRooty");
		} catch (UnableToFindClassException e) {
			// dynamictrees is not installed
		}
		BLOCK_ROOTY = tempBlockRooty;
	}

	public static final boolean IS_DYNAMIC_TREES_INSTALLED = Loader.isModLoaded("dynamictrees");

	public static boolean isRootyBlock(IBlockState state) {
		if (!IS_DYNAMIC_TREES_INSTALLED) {
			return false;
		}
		//return state.getBlock() instanceof BlockRooty;
		return BLOCK_ROOTY.isInstance(state.getBlock());
	}

}
