package clickme.nocubes;

import cpw.mods.fml.common.eventhandler.*;
import cpw.mods.fml.common.gameevent.*;

public class ForgeEventHandler
{
    private NoCubes noCubes;

    public ForgeEventHandler(final NoCubes mod)
    {
        this.noCubes = mod;
    }

    @SubscribeEvent
    public void onKeyInput(final InputEvent.KeyInputEvent event)
    {
        final NoCubes noCubes = this.noCubes;
        if(NoCubes.keyOpenSettings.isPressed())
        {
            this.noCubes.openCubeSettingsGui();
        }
    }
}
