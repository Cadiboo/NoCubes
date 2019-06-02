package io.github.cadiboo.nocubes.tempcore;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Map;

import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

/**
 * @author Cadiboo
 */
@TransformerExclusions("io.github.cadiboo.nocubes.tempcore")
@MCVersion("1.12.2")
@SortingIndex(0x7FFFFFFF - 100)
public final class NoCubesLoadingPlugin implements IFMLLoadingPlugin {

	public static final boolean RCRCH_INSTALLED;
	public static boolean DEVELOPER_ENVIRONMENT = false;
	static String DUMP_BYTECODE_DIR = null;
	static File MOD_LOCATION = null;

	static {
		boolean found;
		try {
			Class.forName("io.github.cadiboo.renderchunkrebuildchunkhooks.core.RenderChunkRebuildChunkHooksLoadingPlugin", false, NoCubesLoadingPlugin.class.getClassLoader());
			found = true;
		} catch (ClassNotFoundException e) {
			// Yay, RenderChunk rebuildChunk Hooks isn't installed
			found = false;
		}
		RCRCH_INSTALLED = found;
	}

	public NoCubesLoadingPlugin() {
	}

	@Override
	public String[] getASMTransformerClass() {
		return RCRCH_INSTALLED ? new String[0] : new String[]{NoCubesClassTransformer.class.getName()};
	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Nullable
	@Override
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(final Map<String, Object> data) {

		DEVELOPER_ENVIRONMENT = !(boolean) data.get("runtimeDeobfuscationEnabled");
		MOD_LOCATION = (File) data.get("coremodLocation");
		DUMP_BYTECODE_DIR = data.get("mcLocation") + "/" + "nocubes" + "/dumps/";

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
