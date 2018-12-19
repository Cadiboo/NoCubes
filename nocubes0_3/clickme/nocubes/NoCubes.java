package clickme.nocubes;

import clickme.nocubes.blocks.DirtHook;
import clickme.nocubes.blocks.GrassHook;
import clickme.nocubes.blocks.GravelHook;
import clickme.nocubes.blocks.SandHook;
import clickme.nocubes.blocks.SnowHook;
import clickme.nocubes.blocks.StoneHook;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.FMLControlledNamespacedRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockSand;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemMultiTexture;
import org.apache.logging.log4j.LogManager;

@Mod(
   modid = "noCubes",
   version = "0.3"
)
public class NoCubes {
   public static final SmoothBlockRenderer renderer = new SmoothBlockRenderer();
   public static final int renderId = RenderingRegistry.getNextAvailableRenderId();

   public static boolean isBlockSmoothed(Block block) {
      return block.func_149645_b() == renderId;
   }

   @SideOnly(Side.CLIENT)
   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      RenderingRegistry.registerBlockHandler(renderer);
      this.overrideBlock(1, Blocks.field_150348_b, "minecraft:stone", "field_150348_b", "stone");
      this.overrideBlock(2, Blocks.field_150349_c, "minecraft:grass", "field_150349_c", "grass");
      this.overrideBlock(3, Blocks.field_150346_d, "minecraft:dirt", "field_150346_d", "dirt");
      this.overrideBlock(12, Blocks.field_150354_m, "minecraft:sand", "field_150354_m", "sand");
      this.overrideBlock(13, Blocks.field_150351_n, "minecraft:gravel", "field_150351_n", "gravel");
      this.overrideBlock(78, Blocks.field_150431_aC, "minecraft:snow_layer", "field_150431_aC", "snow_layer");
   }

   private boolean overrideBlock(int id, Block block, String name, String code, String code2) {
      Object hook;
      if (Blocks.field_150349_c == block) {
         hook = new GrassHook();
      } else if (Blocks.field_150346_d == block) {
         hook = new DirtHook();
      } else if (Blocks.field_150354_m == block) {
         hook = new SandHook();
      } else if (Blocks.field_150351_n == block) {
         hook = new GravelHook();
      } else if (Blocks.field_150431_aC == block) {
         hook = new SnowHook();
      } else {
         hook = new StoneHook();
      }

      ItemBlock item;
      if (Blocks.field_150346_d == block) {
         item = (new ItemMultiTexture((Block)hook, (Block)hook, BlockDirt.field_150009_a)).func_77655_b("dirt");
      } else if (Blocks.field_150354_m == block) {
         item = (new ItemMultiTexture((Block)hook, (Block)hook, BlockSand.field_149838_a)).func_77655_b("sand");
      } else {
         item = new ItemBlock((Block)hook);
      }

      try {
         Method method = FMLControlledNamespacedRegistry.class.getDeclaredMethod("addObjectRaw", Integer.TYPE, String.class, Object.class);
         method.setAccessible(true);
         method.invoke(Block.field_149771_c, id, name, hook);
         method.invoke(Item.field_150901_e, id, name, item);

         Field field;
         try {
            Field field = Blocks.class.getDeclaredField(code);
            field = Field.class.getDeclaredField("modifiers");
            field.setAccessible(true);
            field.setInt(field, field.getModifiers() & -17);
            field.setAccessible(true);
            field.set((Object)null, hook);
            return true;
         } catch (NoSuchFieldException var13) {
            try {
               field = Blocks.class.getDeclaredField(code2);
               Field field1 = Field.class.getDeclaredField("modifiers");
               field1.setAccessible(true);
               field1.setInt(field, field.getModifiers() & -17);
               field.setAccessible(true);
               field.set((Object)null, hook);
               return true;
            } catch (NoSuchFieldException var12) {
               LogManager.getLogger().error("No Cubes failed to override the block " + name + " ;(");
            }
         }
      } catch (NoSuchMethodException var14) {
         var14.printStackTrace();
      } catch (SecurityException var15) {
         var15.printStackTrace();
      } catch (IllegalAccessException var16) {
         var16.printStackTrace();
      } catch (IllegalArgumentException var17) {
         var17.printStackTrace();
      } catch (InvocationTargetException var18) {
         var18.printStackTrace();
      }

      return false;
   }
}
