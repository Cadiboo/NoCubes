package clickme.nocubes.asm;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import java.util.Map;

@TransformerExclusions({"clickme.nocubes.asm"})
public class CoreModLoader implements IFMLLoadingPlugin {
   public String[] getASMTransformerClass() {
      return new String[]{"clickme.nocubes.asm.BlockTweakInjector", "clickme.nocubes.asm.RenderTweakInjector"};
   }

   public String getModContainerClass() {
      return null;
   }

   public String getSetupClass() {
      return null;
   }

   public void injectData(Map data) {
   }

   public String getAccessTransformerClass() {
      return null;
   }
}
