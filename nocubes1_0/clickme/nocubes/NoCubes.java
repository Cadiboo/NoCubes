package clickme.nocubes;

import clickme.nocubes.gui.GuiNoCubes;
import clickme.nocubes.renderer.SurfaceNets;
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
   version = "1.0"
)
public class NoCubes {
   public static final String MOD_ID = "noCubes";
   public static final String VERSION = "1.0";
   private boolean isOutdated = false;
   public static boolean isNoCubesEnabled;
   public static boolean isAutoStepEnabled;
   public static KeyBinding keyOpenSettings;
   private static Configuration noCubesConfig;
   private static List naturalBlockList = new ArrayList();
   private static List liquidBlockList = new ArrayList();
   private static List leavesBlockList = new ArrayList();

   @EventHandler
   public void preInitialization(FMLPreInitializationEvent event) {
      if (event.getSide() == Side.CLIENT) {
         keyOpenSettings = new KeyBinding("key.noCubes", 24, "key.noCubes");
         ClientRegistry.registerKeyBinding(keyOpenSettings);
         noCubesConfig = new Configuration(event.getSuggestedConfigurationFile());
         noCubesConfig.load();
         isNoCubesEnabled = noCubesConfig.get("general", "EnableNoCubes", true).getBoolean(true);
         isAutoStepEnabled = noCubesConfig.get("general", "EnableAutoStep", true).getBoolean(true);
         new SurfaceNets();
         isAutoStepEnabled = false;
         noCubesConfig.save();
         this.checkForPromotions();
         FMLCommonHandler.instance().bus().register(new ForgeEventHandler(this));
      }

   }

   public static void saveConfig() {
      noCubesConfig.load();
      noCubesConfig.get("general", "EnableNoCubes", true).set(isNoCubesEnabled);
      noCubesConfig.get("general", "EnableAutoStep", true).set(isAutoStepEnabled);
      noCubesConfig.save();
   }

   protected void openNoCubesGui() {
      Minecraft.func_71410_x().func_147108_a(new GuiNoCubes());
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
               String lat = (String)promos.get("1.7.10-latest");
               ArtifactVersion current = new DefaultArtifactVersion("1.0");
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

   public static void registerAsNatural(Block block) {
      naturalBlockList.add(block);
   }

   public static void registerAsLiquid(Block block) {
      liquidBlockList.add(block);
   }

   public static void registerAsLeaves(Block block) {
      leavesBlockList.add(block);
   }

   public static boolean isBlockNatural(Block block) {
      return naturalBlockList.contains(block) && isNoCubesEnabled;
   }

   public static boolean isBlockLiquid(Block block) {
      return liquidBlockList.contains(block);
   }

   public static boolean isBlockLeaves(Block block) {
      return leavesBlockList.contains(block);
   }

   static {
      naturalBlockList.add(Blocks.field_150349_c);
      naturalBlockList.add(Blocks.field_150346_d);
      naturalBlockList.add(Blocks.field_150354_m);
      naturalBlockList.add(Blocks.field_150351_n);
      naturalBlockList.add(Blocks.field_150435_aG);
      naturalBlockList.add(Blocks.field_150458_ak);
      naturalBlockList.add(Blocks.field_150391_bh);
      naturalBlockList.add(Blocks.field_150431_aC);
      naturalBlockList.add(Blocks.field_150348_b);
      naturalBlockList.add(Blocks.field_150365_q);
      naturalBlockList.add(Blocks.field_150366_p);
      naturalBlockList.add(Blocks.field_150352_o);
      naturalBlockList.add(Blocks.field_150482_ag);
      naturalBlockList.add(Blocks.field_150450_ax);
      naturalBlockList.add(Blocks.field_150439_ay);
      naturalBlockList.add(Blocks.field_150412_bA);
      naturalBlockList.add(Blocks.field_150357_h);
      naturalBlockList.add(Blocks.field_150322_A);
      naturalBlockList.add(Blocks.field_150405_ch);
      naturalBlockList.add(Blocks.field_150424_aL);
      naturalBlockList.add(Blocks.field_150425_aM);
      naturalBlockList.add(Blocks.field_150449_bY);
      naturalBlockList.add(Blocks.field_150377_bs);
      liquidBlockList.add(Blocks.field_150355_j);
      liquidBlockList.add(Blocks.field_150358_i);
      liquidBlockList.add(Blocks.field_150353_l);
      liquidBlockList.add(Blocks.field_150356_k);
      leavesBlockList.add(Blocks.field_150362_t);
      leavesBlockList.add(Blocks.field_150361_u);
   }
}
