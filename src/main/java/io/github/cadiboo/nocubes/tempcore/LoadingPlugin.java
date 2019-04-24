//package io.github.cadiboo.nocubes.tempcore;
//
//import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
//
//import javax.annotation.Nullable;
//import java.io.File;
//import java.util.Map;
//
//import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
//import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
//import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
//
///**
// * @author Cadiboo
// */
//@TransformerExclusions("io.github.cadiboo.nocubes.tempcore")
//@MCVersion("1.12.2")
//@SortingIndex(Integer.MAX_VALUE - 100)
//public final class LoadingPlugin implements IFMLLoadingPlugin {
//
//	static String DUMP_BYTECODE_DIR = null;
//	static File MOD_LOCATION = null;
//
//	@Override
//	public String[] getASMTransformerClass() {
//		return new String[]{TransformerDispatcher.class.getName()};
//	}
//
//	@Override
//	public String getModContainerClass() {
//		return null;
//	}
//
//	@Nullable
//	@Override
//	public String getSetupClass() {
//		return null;
//	}
//
//	@Override
//	public void injectData(final Map<String, Object> data) {
//
//		MOD_LOCATION = (File) data.get("coremodLocation");
//		DUMP_BYTECODE_DIR = data.get("mcLocation") + "/" + "nocubes" + "/dumps/";
//
//	}
//
//	@Override
//	public String getAccessTransformerClass() {
//		return null;
//	}
//
//}
