package io.github.cadiboo.nocubes.tempcore;

import io.github.cadiboo.nocubes.tempcore.classtransformer.OverwritingClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Map;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;
import static io.github.cadiboo.nocubes.util.ModReference.VERSION;

@Name(MOD_NAME)
@MCVersion("1.12.2")
@TransformerExclusions({"io.github.cadiboo.nocubes.tempcore."})
/* How early your core mod is called - Use > 1000 to work with srg names */
//??? needs higher than 1001??? 0xBADC0DE works
//@SortingIndex(value = 1001)
@SortingIndex(value = 0xBAD_C0DE + 100)
public final class LoadingPlugin implements IFMLLoadingPlugin {

	public static final String CORE_MARKER = MOD_ID;

	private static final Logger LOGGER = LogManager.getLogger(MOD_NAME + " Core Plugin");

	public LoadingPlugin() {
		LOGGER.debug("Initialising " + this.getClass().getSimpleName() + " version: " + VERSION);
		Launch.blackboard.put(CORE_MARKER, VERSION);
	}

	@Override
	public String[] getASMTransformerClass() {

		return new String[]{OverwritingClassTransformer.class.getName()};

	}

	@Override
	public String getModContainerClass() {
		return null;
	}

	@Override
	@Nullable
	public String getSetupClass() {
		return null;
	}

	@Override
	public void injectData(final Map<String, Object> data) {

	}

	@Override
	@Nullable
	public String getAccessTransformerClass() {
		return null;
	}

	private boolean getArgsBoolean(final String arg) {
		return false;
	}

}
