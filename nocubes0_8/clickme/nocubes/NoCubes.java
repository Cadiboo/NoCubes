package clickme.nocubes;

import clickme.nocubes.gui.GuiCubeSettings;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;
import cpw.mods.fml.relauncher.Side;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.init.Blocks;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.config.Configuration;

@Mod(
   modid = "noCubes",
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

   @EventHandler
   public void preInitialization(FMLPreInitializationEvent event) {
      if (event.getSide() == Side.CLIENT) {
         keyOpenSettings = new KeyBinding("key.noCubes", 24, "key.noCubes");
         optionGui = new GuiCubeSettings();
         ClientRegistry.registerKeyBinding(keyOpenSettings);
         cubesConfig = new Configuration(event.getSuggestedConfigurationFile());
         cubesConfig.load();
         isNoCubesEnabled = cubesConfig.get("general", "EnableNoCubes", true).getBoolean(true);
         isAutoStepEnabled = cubesConfig.get("general", "EnableAutoStep", true).getBoolean(true);
         cubesConfig.save();
         this.checkForPromotions();
         FMLCommonHandler.instance().bus().register(new ForgeEventHandler(this));
      }
   }

   public static void saveCubeConfig() {
      cubesConfig.load();
      cubesConfig.get("general", "EnableNoCubes", true).set(isNoCubesEnabled);
      cubesConfig.get("general", "EnableAutoStep", true).set(isAutoStepEnabled);
      cubesConfig.save();
   }

   protected void openCubeSettingsGui() {
      Minecraft.func_71410_x().func_147108_a(new GuiCubeSettings());
   }

   protected void notificatePlayerInChat(EntityPlayer player) {
      if (this.isOutdated) {
         player.func_145747_a(new ChatComponentTranslation("animals.outdated", new Object[0]));
         String updateUrl = "http://goo.gl/z7zh90";
         ChatComponentText url = new ChatComponentText(updateUrl);
         url.func_150256_b().func_150241_a(new ClickEvent(Action.OPEN_URL, updateUrl));
         url.func_150256_b().func_150228_d(true);
         player.func_145747_a(new ChatComponentTranslation("animals.download", new Object[]{url}));
      }

   }

   private void checkForPromotions() {
      (new Thread("No Cubes Version Check") {
         public void run() {
            try {
               URL url = new URL("https://dl.dropboxusercontent.com/u/71419016/nc/promotions.json");
               InputStream input = url.openStream();
               String data = new String(ByteStreams.toByteArray(input));
               input.close();
               Map json = (Map)(new Gson()).fromJson(data, Map.class);
               Map promos = (Map)json.get("promos");
               String lat = (String)promos.get("1.7.2-latest");
               ArtifactVersion current = new DefaultArtifactVersion("0.8");
               if (lat != null) {
                  ArtifactVersion latest = new DefaultArtifactVersion(lat);
                  if (latest.compareTo(current) > 0) {
                     NoCubes.this.isOutdated = true;
                  }
               }
            } catch (IOException var9) {
               ;
            } catch (JsonSyntaxException var10) {
               ;
            }

         }
      }).start();
   }

   public static boolean isBlockSoft(Block block) {
      return softBlockList.contains(block);
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
      softBlockList.add(Blocks.field_150349_c);
      softBlockList.add(Blocks.field_150346_d);
      softBlockList.add(Blocks.field_150354_m);
      softBlockList.add(Blocks.field_150351_n);
      softBlockList.add(Blocks.field_150435_aG);
      softBlockList.add(Blocks.field_150458_ak);
      softBlockList.add(Blocks.field_150391_bh);
      softBlockList.add(Blocks.field_150431_aC);
      softBlockList.add(Blocks.field_150348_b);
      softBlockList.add(Blocks.field_150365_q);
      softBlockList.add(Blocks.field_150366_p);
      softBlockList.add(Blocks.field_150352_o);
      softBlockList.add(Blocks.field_150482_ag);
      softBlockList.add(Blocks.field_150450_ax);
      softBlockList.add(Blocks.field_150439_ay);
      softBlockList.add(Blocks.field_150412_bA);
      softBlockList.add(Blocks.field_150357_h);
      softBlockList.add(Blocks.field_150424_aL);
      softBlockList.add(Blocks.field_150425_aM);
      softBlockList.add(Blocks.field_150449_bY);
      softBlockList.add(Blocks.field_150377_bs);
      liquidBlockList.add(Blocks.field_150355_j);
      liquidBlockList.add(Blocks.field_150358_i);
      liquidBlockList.add(Blocks.field_150353_l);
      liquidBlockList.add(Blocks.field_150356_k);
      leavesBlockList.add(Blocks.field_150362_t);
      leavesBlockList.add(Blocks.field_150361_u);
   }
}
