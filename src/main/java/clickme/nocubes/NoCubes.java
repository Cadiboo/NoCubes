package clickme.nocubes;

import net.minecraft.client.settings.*;
import net.minecraftforge.common.config.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.relauncher.*;
import cpw.mods.fml.client.registry.*;
import clickme.nocubes.renderer.*;
import cpw.mods.fml.common.*;
import net.minecraft.client.*;
import clickme.nocubes.gui.*;
import net.minecraft.client.gui.*;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.event.*;
import java.net.*;
import com.google.common.io.*;
import com.google.gson.*;
import java.io.*;
import cpw.mods.fml.common.versioning.*;
import net.minecraft.block.*;
import java.util.*;
import net.minecraft.init.*;

@Mod(modid = "noCubes", version = "1.0")
public class NoCubes
{
    public static final String MOD_ID = "noCubes";
    public static final String VERSION = "1.0";
    private boolean isOutdated;
    public static boolean isNoCubesEnabled;
    public static boolean isAutoStepEnabled;
    public static KeyBinding keyOpenSettings;
    private static Configuration noCubesConfig;
    private static List naturalBlockList;
    private static List liquidBlockList;
    private static List leavesBlockList;
    
    public NoCubes() {
        this.isOutdated = false;
    }
    
    @Mod.EventHandler
    public void preInitialization(final FMLPreInitializationEvent event) {
        if (event.getSide() == Side.CLIENT) {
            ClientRegistry.registerKeyBinding(NoCubes.keyOpenSettings = new KeyBinding("key.noCubes", 24, "key.noCubes"));
            (NoCubes.noCubesConfig = new Configuration(event.getSuggestedConfigurationFile())).load();
            NoCubes.isNoCubesEnabled = NoCubes.noCubesConfig.get("general", "EnableNoCubes", true).getBoolean(true);
            NoCubes.isAutoStepEnabled = NoCubes.noCubesConfig.get("general", "EnableAutoStep", true).getBoolean(true);
            new SurfaceNets();
            NoCubes.isAutoStepEnabled = false;
            NoCubes.noCubesConfig.save();
            this.checkForPromotions();
            FMLCommonHandler.instance().bus().register((Object)new ForgeEventHandler(this));
        }
    }
    
    public static void saveConfig() {
        NoCubes.noCubesConfig.load();
        NoCubes.noCubesConfig.get("general", "EnableNoCubes", true).set(NoCubes.isNoCubesEnabled);
        NoCubes.noCubesConfig.get("general", "EnableAutoStep", true).set(NoCubes.isAutoStepEnabled);
        NoCubes.noCubesConfig.save();
    }
    
    protected void openNoCubesGui() {
        Minecraft.func_71410_x().func_147108_a((GuiScreen)new GuiNoCubes());
    }
    
    protected void notificatePlayerInChat(final EntityPlayer player) {
        if (this.isOutdated) {
            player.func_145747_a((IChatComponent)new ChatComponentTranslation("animals.outdated", new Object[0]));
            final String updateUrl = "http://goo.gl/z7zh90";
            final ChatComponentText url = new ChatComponentText(updateUrl);
            url.func_150256_b().func_150241_a(new ClickEvent(ClickEvent.Action.OPEN_URL, updateUrl));
            url.func_150256_b().func_150228_d(true);
            player.func_145747_a((IChatComponent)new ChatComponentTranslation("animals.download", new Object[] { url }));
        }
    }
    
    private void checkForPromotions() {
        new Thread("No Cubes Version Check") {
            @Override
            public void run() {
                try {
                    final URL url = new URL("https://dl.dropboxusercontent.com/u/71419016/nc/promotions.json");
                    final InputStream input = url.openStream();
                    final String data = new String(ByteStreams.toByteArray(input));
                    input.close();
                    final Map<String, Object> json = (Map<String, Object>)new Gson().fromJson(data, (Class)Map.class);
                    final Map<String, String> promos = json.get("promos");
                    final String lat = promos.get("1.7.10-latest");
                    final ArtifactVersion current = (ArtifactVersion)new DefaultArtifactVersion("1.0");
                    if (lat != null) {
                        final ArtifactVersion latest = (ArtifactVersion)new DefaultArtifactVersion(lat);
                        if (latest.compareTo((Object)current) > 0) {
                            NoCubes.this.isOutdated = true;
                        }
                    }
                }
                catch (IOException e) {}
                catch (JsonSyntaxException ex) {}
            }
        }.start();
    }
    
    public static void registerAsNatural(final Block block) {
        NoCubes.naturalBlockList.add(block);
    }
    
    public static void registerAsLiquid(final Block block) {
        NoCubes.liquidBlockList.add(block);
    }
    
    public static void registerAsLeaves(final Block block) {
        NoCubes.leavesBlockList.add(block);
    }
    
    public static boolean isBlockNatural(final Block block) {
        return NoCubes.naturalBlockList.contains(block) && NoCubes.isNoCubesEnabled;
    }
    
    public static boolean isBlockLiquid(final Block block) {
        return NoCubes.liquidBlockList.contains(block);
    }
    
    public static boolean isBlockLeaves(final Block block) {
        return NoCubes.leavesBlockList.contains(block);
    }
    
    static {
        NoCubes.naturalBlockList = new ArrayList();
        NoCubes.liquidBlockList = new ArrayList();
        NoCubes.leavesBlockList = new ArrayList();
        NoCubes.naturalBlockList.add(Blocks.field_150349_c);
        NoCubes.naturalBlockList.add(Blocks.field_150346_d);
        NoCubes.naturalBlockList.add(Blocks.field_150354_m);
        NoCubes.naturalBlockList.add(Blocks.field_150351_n);
        NoCubes.naturalBlockList.add(Blocks.field_150435_aG);
        NoCubes.naturalBlockList.add(Blocks.field_150458_ak);
        NoCubes.naturalBlockList.add(Blocks.field_150391_bh);
        NoCubes.naturalBlockList.add(Blocks.field_150431_aC);
        NoCubes.naturalBlockList.add(Blocks.field_150348_b);
        NoCubes.naturalBlockList.add(Blocks.field_150365_q);
        NoCubes.naturalBlockList.add(Blocks.field_150366_p);
        NoCubes.naturalBlockList.add(Blocks.field_150352_o);
        NoCubes.naturalBlockList.add(Blocks.field_150482_ag);
        NoCubes.naturalBlockList.add(Blocks.field_150450_ax);
        NoCubes.naturalBlockList.add(Blocks.field_150439_ay);
        NoCubes.naturalBlockList.add(Blocks.field_150412_bA);
        NoCubes.naturalBlockList.add(Blocks.field_150357_h);
        NoCubes.naturalBlockList.add(Blocks.field_150322_A);
        NoCubes.naturalBlockList.add(Blocks.field_150405_ch);
        NoCubes.naturalBlockList.add(Blocks.field_150424_aL);
        NoCubes.naturalBlockList.add(Blocks.field_150425_aM);
        NoCubes.naturalBlockList.add(Blocks.field_150449_bY);
        NoCubes.naturalBlockList.add(Blocks.field_150377_bs);
        NoCubes.liquidBlockList.add(Blocks.field_150355_j);
        NoCubes.liquidBlockList.add(Blocks.field_150358_i);
        NoCubes.liquidBlockList.add(Blocks.field_150353_l);
        NoCubes.liquidBlockList.add(Blocks.field_150356_k);
        NoCubes.leavesBlockList.add(Blocks.field_150362_t);
        NoCubes.leavesBlockList.add(Blocks.field_150361_u);
    }
}
