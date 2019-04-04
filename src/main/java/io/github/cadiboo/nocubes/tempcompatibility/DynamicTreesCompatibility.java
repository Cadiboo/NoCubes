package io.github.cadiboo.nocubes.tempcompatibility;

import net.minecraft.block.state.IBlockState;

/**
 * @author Cadiboo
 */
public class DynamicTreesCompatibility {

//	private static final Class<? super Block> BLOCK_ROOTY;
//	static {
//		Class<? super Block> tempBlockRooty = null;
//		try {
//			tempBlockRooty = ObfuscationReflectionHelperCopy.getClass(Loader.instance().getModClassLoader(), "com.ferreusveritas.dynamictrees.blocks.BlockRooty");
//		} catch (UnableToFindClassException e) {
//			// dynamictrees is not installed
//		}
//		BLOCK_ROOTY = tempBlockRooty;
//	}
//
//	public static final boolean IS_DYNAMIC_TREES_INSTALLED = Loader.isModLoaded("dynamictrees");
//
//	public static boolean isRootyBlock(IBlockState state) {
//		if (!IS_DYNAMIC_TREES_INSTALLED) {
//			return false;
//		}
//		//return state.getBlock() instanceof BlockRooty;
//		return BLOCK_ROOTY.isInstance(state.getBlock());
//	}

	public static boolean isRootyBlock(final IBlockState state) {
		return false;
	}

}
