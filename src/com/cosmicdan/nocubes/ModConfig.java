package com.cosmicdan.nocubes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

public class ModConfig {
	public static boolean MOD_ENABLED = true;
	public static boolean AUTOSTEPUP_ENABLED = false; // TODO
	public static HashSet<Integer> SMOOTHBLOCKS_IDS = new HashSet<Integer>();
	
	private static String[] SMOOTHBLOCKS;
	
	private static String CONFIG_PATH;
    private static Configuration CONFIG;
	
	public static void doConfig(File configPath) {
		
        // ensure the config directory exists
        try {
            CONFIG_PATH = configPath.getCanonicalPath() + "/" + Main.MODNAME;
            configPath = new File(CONFIG_PATH);
            if (!configPath.exists())
                configPath.mkdirs();
        } catch (IOException e) {
            Main.LOGGER.error("An error occured while getting/setting configuration: " + e.getMessage());
        }
        
        // load existing/default config
        CONFIG = new Configuration(new File(CONFIG_PATH, (Main.MODNAME + ".cfg")));
        CONFIG.load();

        // populate config vars
        Property MOD_ENABLED_PROP = getEnabled(MOD_ENABLED);
        Property AUTOSTEPUP_ENABLED_PROP = getStepup(AUTOSTEPUP_ENABLED);
        
        // build the default blocklist
        SMOOTHBLOCKS = getDefaultBlocks();
        Property SMOOTHBLOCKS_PROP = CONFIG.get("smoothing", "smoothblocks", SMOOTHBLOCKS);
        SMOOTHBLOCKS_PROP.comment = "The list of blocks to be smoothed, in modid:name format";
        
        // save config if it differs to the default values
        saveIfChanged();
        
        // Need to actually populate the values *after* saving config, otherwise first-run defaults are not persisted
        // there's probably a better way to do this...
        MOD_ENABLED = MOD_ENABLED_PROP.getBoolean(MOD_ENABLED);
        AUTOSTEPUP_ENABLED = AUTOSTEPUP_ENABLED_PROP.getBoolean(AUTOSTEPUP_ENABLED);
        SMOOTHBLOCKS = SMOOTHBLOCKS_PROP.getStringList();
        
        // all done
        Main.LOGGER.info("Config loaded");
    }
	
	public static void saveIfChanged() {
		if(CONFIG.hasChanged())
            CONFIG.save();
	}
	
	// B:smoothing
	public static Property getEnabled(boolean defaultVal) {
		Property PROP = CONFIG.get("general", "smoothing", defaultVal);
        PROP.comment = "Set if block smoothing is enabled by default. This can be toggled in-game.";
        return PROP;
	}
	public static void setEnabled(boolean newVal) {
		getEnabled(newVal).set(newVal);
		MOD_ENABLED = newVal;
	}
	
	// B:stepup
	public static Property getStepup(boolean defaultVal) {
		Property PROP = CONFIG.get("general", "stepup", defaultVal);
        //PROP.comment = "Set if step-up is enabled by default. This can be toggled in-game.";
		PROP.comment = "Not yet implemented. Use Iguana Tweaks for now if you want this.";
        return PROP;
	}
	public static void setStepup(boolean newVal) {
		getStepup(newVal).set(newVal);
		AUTOSTEPUP_ENABLED = newVal;
	}
	
	private static String[] getDefaultBlocks() {
		final ArrayList<String> SMOOTHBLOCKS_NAMES = new ArrayList<String>();
		
		SMOOTHBLOCKS_NAMES.add("minecraft:stone");
		SMOOTHBLOCKS_NAMES.add("minecraft:grass");
		SMOOTHBLOCKS_NAMES.add("minecraft:dirt");
		SMOOTHBLOCKS_NAMES.add("minecraft:bedrock");
		SMOOTHBLOCKS_NAMES.add("minecraft:sand");
		SMOOTHBLOCKS_NAMES.add("minecraft:gravel");
		SMOOTHBLOCKS_NAMES.add("minecraft:gold_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:iron_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:coal_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:lapis_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:sandstone");
		SMOOTHBLOCKS_NAMES.add("minecraft:diamond_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:farmland");
		SMOOTHBLOCKS_NAMES.add("minecraft:redstone_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:lit_redstone_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:snow_layer");
		SMOOTHBLOCKS_NAMES.add("minecraft:clay");
		SMOOTHBLOCKS_NAMES.add("minecraft:netherrack");
		SMOOTHBLOCKS_NAMES.add("minecraft:soul_sand");
		SMOOTHBLOCKS_NAMES.add("minecraft:mycelium");
		SMOOTHBLOCKS_NAMES.add("minecraft:end_stone");
		SMOOTHBLOCKS_NAMES.add("minecraft:emerald_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:quartz_ore");
		SMOOTHBLOCKS_NAMES.add("minecraft:hardened_clay");
		
		return SMOOTHBLOCKS_NAMES.toArray(new String[0]);
	}
	
	public static void buildSmoothBlockIds() {
		Main.LOGGER.info("Parsing " + SMOOTHBLOCKS.length + " block in list...");
		SMOOTHBLOCKS_IDS.clear();
		for (String smoothBlocksListEntry : SMOOTHBLOCKS) {
			String[] blockFqn = smoothBlocksListEntry.split(":");
	        if (blockFqn[0] == null || blockFqn[1] == null) {
	            Main.LOGGER.warn(" - Invalid block entry in config: " + smoothBlocksListEntry);
	            continue;
	        }
	        Block foundBlock = GameRegistry.findBlock(blockFqn[0], blockFqn[1]); 
	        if (foundBlock == null) {
	        	Main.LOGGER.warn(" - Block not found in game: " + smoothBlocksListEntry);
                continue;
            }
	        SMOOTHBLOCKS_IDS.add(Block.getIdFromBlock(foundBlock));
		}
	}
}
