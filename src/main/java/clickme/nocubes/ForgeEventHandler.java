package clickme.nocubes;

import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.*;

public class ForgeEventHandler
{
    private NoCubes noCubes;
    
    public ForgeEventHandler(final NoCubes mod) {
        this.noCubes = mod;
    }
    
    @SubscribeEvent
    public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        this.noCubes.notificatePlayerInChat(event.player);
    }
    
    @SubscribeEvent
    public void onKeyInput(final InputEvent.KeyInputEvent event) {
        final NoCubes noCubes = this.noCubes;
        if (NoCubes.keyOpenSettings.func_151468_f()) {
            this.noCubes.openNoCubesGui();
        }
    }
}
