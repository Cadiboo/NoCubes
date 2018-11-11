package clickme.nocubes;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

public class ForgeEventHandler {
   private NoCubes noCubes;

   public ForgeEventHandler(NoCubes mod) {
      this.noCubes = mod;
   }

   @SubscribeEvent
   public void onKeyInput(KeyInputEvent event) {
      NoCubes noCubes = this.noCubes;
      if (NoCubes.keyOpenSettings.func_151468_f()) {
         this.noCubes.openCubeSettingsGui();
      }

   }
}
