package clickme.nocubes;

import clickme.nocubes.gui.GuiCubeSettings;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;

@Mod(
   modid = "noCubes",
   name = "noCubes",
   version = "0.8"
)
public class NoCubes {
   public static final String MOD_ID = "noCubes";
   public static final String VERSION = "0.8";
   private boolean isOutdated = false;
   public static SoftBlockRenderer softBlockRenderer = new SoftBlockRenderer();
   private static List softBlockList = new ArrayList();
   private static List liquidBlockList = new ArrayList();
   private static List leavesBlockList = new ArrayList();
   public static KeyBinding keyOpenSettings;
   public static GuiScreen optionGui;
   private static Configuration cubesConfig;
   public static boolean isNoCubesEnabled;
   public static boolean isAutoStepEnabled;
   protected static List list = new ArrayList();
   protected static List ListBlack = new ArrayList();
   protected static boolean isblackListing = false;
   protected static boolean autoDetection = true;
   protected static boolean autoDirt;
   protected static boolean autoStone;
   protected static boolean autoGravel;
   protected static boolean autoLeaves;
   protected static boolean autoSnow;
   protected static boolean autoLiquid;
   protected static boolean autoIce = true;

   @EventHandler
   public void preInitialization(FMLPreInitializationEvent event) {
      if (event.getSide() == Side.CLIENT) {
         keyOpenSettings = new KeyBinding("key.noCubes", 24, "key.noCubes");
         optionGui = new GuiCubeSettings();
         ClientRegistry.registerKeyBinding(keyOpenSettings);
         (cubesConfig = new Configuration(event.getSuggestedConfigurationFile())).load();
         autoDetection = cubesConfig.get("general", "AutoDetection", true).getBoolean(true);
         isNoCubesEnabled = cubesConfig.get("general", "EnableNoCubes", true).getBoolean(true);
         isAutoStepEnabled = cubesConfig.get("general", "EnableAutoStep", true).getBoolean(true);
         cubesConfig.save();
         FMLCommonHandler.instance().bus().register(new ForgeEventHandler(this));
      }
   }

   @EventHandler
   public void postInitialization(FMLPostInitializationEvent event) {
      if (event.getSide() == Side.CLIENT) {
         cubesConfig.load();
         loadBlocks();
         loadBlocksBlackList();
         if (autoDetection) {
            loadAutoDetection();
         }

         cubesConfig.save();
      }
   }

   public static void loadAutoDetection() {
      autoDirt = cubesConfig.get("general", "autoDirt", true).getBoolean(true);
      autoGravel = cubesConfig.get("general", "autoGravel_and_Sand", true).getBoolean(true);
      autoLeaves = cubesConfig.get("general", "autoLeaves", true).getBoolean(true);
      autoStone = cubesConfig.get("general", "autoStone", false).getBoolean(false);
      autoSnow = cubesConfig.get("general", "autoSnow", true).getBoolean(true);
      autoIce = cubesConfig.get("general", "autoIce", true).getBoolean(true);
      autoLiquid = cubesConfig.get("general", "autoLiquid", true).getBoolean(true);
      String[] s = new String[]{"examplemod:stone"};
      String[] strautoblacklist = cubesConfig.getStringList("Auto Detection BlackList", "blocks", s, "Add Your Blocks Here");
      ArrayList autoblacklist = new ArrayList();
      String[] var3 = strautoblacklist;
      int var4 = strautoblacklist.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String str = var3[var5];
         autoblacklist.add(createBlock(str));
      }

      Iterator it = GameData.getBlockRegistry().iterator();

      while(true) {
         Block block;
         do {
            do {
               do {
                  if (!it.hasNext()) {
                     return;
                  }

                  block = (Block)it.next();
               } while(block.func_149688_o() == Material.field_151585_k);
            } while(autoblacklist.contains(block));

            System.out.println(block.func_149688_o().toString());
            if (autoDirt && block.func_149688_o() == Material.field_151578_c && block.func_149662_c() || autoDirt && block.func_149688_o() == Material.field_151577_b && block.func_149662_c()) {
               softBlockList.add(block);
            }

            if (autoGravel && block.func_149688_o() == Material.field_151595_p && block.func_149662_c()) {
               softBlockList.add(block);
            }

            if (autoStone && block.func_149688_o() == Material.field_151576_e && block.func_149662_c()) {
               softBlockList.add(block);
            }

            if (autoSnow && block.func_149688_o() == Material.field_151597_y || autoSnow && block.func_149688_o() == Material.field_151596_z) {
               softBlockList.add(block);
            }

            if (autoIce && block.func_149688_o() == Material.field_151588_w || autoIce && block.func_149688_o() == Material.field_151598_x) {
               softBlockList.add(block);
            }

            if (autoLeaves && block.func_149688_o() == Material.field_151584_j) {
               leavesBlockList.add(block);
            }
         } while((!autoLiquid || block.func_149688_o() != Material.field_151587_i) && (!autoLiquid || block.func_149688_o() != Material.field_151586_h) && (!autoLiquid || !(block instanceof BlockLiquid)));

         liquidBlockList.add(block);
      }
   }

   public static void saveCubeConfig() {
      cubesConfig.load();
      cubesConfig.get("general", "EnableNoCubes", true).set(isNoCubesEnabled);
      cubesConfig.get("general", "EnableAutoStep", true).set(isAutoStepEnabled);
      cubesConfig.save();
   }

   public static void loadBlocks() {
      String[] s = new String[]{"minecraft:grass", "minecraft:dirt", "minecraft:sand", "minecraft:gravel", "minecraft:clay", "minecraft:farmland", "minecraft:mycelium", "minecraft:snow_layer", "minecraft:stone", "minecraft:coal_ore", "minecraft:iron_ore", "minecraft:gold_ore", "minecraft:diamond_ore", "minecraft:redstone_ore", "minecraft:emerald_ore", "minecraft:bedrock", "minecraft:netherrack", "minecraft:soul_sand", "minecraft:soul_sand", "minecraft:end_stone"};
      String[] whitelist = cubesConfig.getStringList("No Cubes List", "blocks", s, "Add Your Blocks Here");
      isblackListing = cubesConfig.getBoolean("blacklist_mode", "blocks", false, "All blocks Except in Blacklist Mode will work");
      String[] var2 = whitelist;
      int var3 = whitelist.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String ss = var2[var4];
         softBlockList.add(createBlock(ss));
         if (ss.equals("minecraft:redstone_ore")) {
            softBlockList.add(Blocks.field_150439_ay);
         }
      }

   }

   public static void loadBlocksBlackList() {
      String[] ss = new String[]{"examplemod:wood"};
      String[] blacklist = cubesConfig.getStringList("No Cubes BlackList Mode", "blocks", ss, "BlackList Blocks Here");
      String[] var2 = blacklist;
      int var3 = blacklist.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String s = var2[var4];
         ListBlack.add(createBlock(s));
         if (s.equals("minecraft:redstone_ore")) {
            ListBlack.add(Blocks.field_150439_ay);
         }
      }

   }

   public static Block createBlock(String s) {
      s = s.replaceFirst(":", "©");
      String[] parts = s.split("©");
      return GameRegistry.findBlock(parts[0], parts[1]);
   }

   protected void openCubeSettingsGui() {
      Minecraft.func_71410_x().func_147108_a(new GuiCubeSettings());
   }

   public static boolean isBlockSoft(Block block) {
      if (!isblackListing) {
         return softBlockList.contains(block);
      } else {
         return !ListBlack.contains(block);
      }
   }

   public static boolean isBlockSoftForCollision(Block block) {
      return softBlockList.contains(block) && isAutoStepEnabled;
   }

   public static boolean isBlockLiquid(Block block) {
      return liquidBlockList.contains(block);
   }

   public static void renderBlockSoft(Block block) {
      softBlockList.add(block);
   }

   public static void registerAsLiquid(Block block) {
      liquidBlockList.add(block);
   }

   public static void registerAsLeaves(Block block) {
      leavesBlockList.add(block);
   }

   static {
      liquidBlockList.add(Blocks.field_150355_j);
      liquidBlockList.add(Blocks.field_150358_i);
      liquidBlockList.add(Blocks.field_150353_l);
      liquidBlockList.add(Blocks.field_150356_k);
      leavesBlockList.add(Blocks.field_150362_t);
      leavesBlockList.add(Blocks.field_150361_u);
   }
}
