package io.github.cadiboo.nocubes.tempcore;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import static net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

/**
 * @author Cadiboo
 */
@TransformerExclusions("io.github.cadiboo.nocubes.tempcore")
@MCVersion("1.12.2")
@SortingIndex(Integer.MAX_VALUE - 100)
public class LoadingPlugin implements IFMLLoadingPlugin {

	@Override
	public String[] getASMTransformerClass() {
		return new String[]{Transformer.class.getName()};
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

	}

	@Override
	public String getAccessTransformerClass() {
		return null;
	}

}
