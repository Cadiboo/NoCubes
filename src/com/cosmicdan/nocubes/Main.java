package com.cosmicdan.nocubes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import com.cosmicdan.nocubes.gui.GuiNoCubes;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.MinecraftForge;

@Mod(
    modid = Main.MODID, 
    name = Main.MODNAME,
    version = "${version}",
    dependencies = "required-after:Forge@[10.13,);"
)

public class Main {
    public static final String MODID = "nocubes";
    public static final String MODNAME = "NoCubes";
    //private static final String PROXY_CLIENT = "com.cosmicdan.nocubes.client.ClientProxy";
    //private static final String PROXY_COMMON = "com.cosmicdan.nocubes.CommonProxy";
    
    public static KeyBinding KEYBIND_SETTINGS;
    
    @Instance(MODNAME)
    public static Main INSTANCE;
    
    //@SidedProxy(clientSide=PROXY_CLIENT, serverSide=PROXY_COMMON)
    //public static CommonProxy PROXY;
    
    public static Logger LOGGER = LogManager.getLogger(Main.MODNAME);

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	if (event.getSide() == Side.SERVER)
    		return;
        ModConfig.doConfig(event.getModConfigurationDirectory());
        KEYBIND_SETTINGS = new KeyBinding("key.settings", Keyboard.KEY_F4, MODNAME);
        ClientRegistry.registerKeyBinding(KEYBIND_SETTINGS);
        FMLCommonHandler.instance().bus().register(new EventsFML());
        MinecraftForge.EVENT_BUS.register(new EventsForge());
        //PROXY.preInit(event);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        //PROXY.init(event);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        //PROXY.postInit(event);
    }
    
    protected static void openSettingsGui() {
		Minecraft.getMinecraft().displayGuiScreen((GuiScreen) new GuiNoCubes());
	}
	
	public static boolean shouldSmooth(Block block) {
		if (!ModConfig.MOD_ENABLED)
			return false;
		if (ModConfig.SMOOTHBLOCKS_IDS.contains(Block.getIdFromBlock(block)))
			return true;
		return false;
	}
}
