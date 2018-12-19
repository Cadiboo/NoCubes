package net.minecraft.block;

import clickme.nocubes.NoCubes;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.minecraft.block.BlockPressurePlate.Sensitivity;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.RegistryNamespaced;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldProviderEnd;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.util.RotationHelper;
import net.minecraftforge.event.ForgeEventFactory;

public class Block {
   public static final RegistryNamespaced field_149771_c = GameData.getBlockRegistry();
   private CreativeTabs field_149772_a;
   protected String field_149768_d;
   public static final Block.SoundType field_149769_e = new Block.SoundType("stone", 1.0F, 1.0F);
   public static final Block.SoundType field_149766_f = new Block.SoundType("wood", 1.0F, 1.0F);
   public static final Block.SoundType field_149767_g = new Block.SoundType("gravel", 1.0F, 1.0F);
   public static final Block.SoundType field_149779_h = new Block.SoundType("grass", 1.0F, 1.0F);
   public static final Block.SoundType field_149780_i = new Block.SoundType("stone", 1.0F, 1.0F);
   public static final Block.SoundType field_149777_j = new Block.SoundType("stone", 1.0F, 1.5F);
   public static final Block.SoundType field_149778_k = new Block.SoundType("stone", 1.0F, 1.0F) {
      private static final String __OBFID = "CL_00000200";

      public String func_150495_a() {
         return "dig.glass";
      }

      public String func_150496_b() {
         return "step.stone";
      }
   };
   public static final Block.SoundType field_149775_l = new Block.SoundType("cloth", 1.0F, 1.0F);
   public static final Block.SoundType field_149776_m = new Block.SoundType("sand", 1.0F, 1.0F);
   public static final Block.SoundType field_149773_n = new Block.SoundType("snow", 1.0F, 1.0F);
   public static final Block.SoundType field_149774_o = new Block.SoundType("ladder", 1.0F, 1.0F) {
      private static final String __OBFID = "CL_00000201";

      public String func_150495_a() {
         return "dig.wood";
      }
   };
   public static final Block.SoundType field_149788_p = new Block.SoundType("anvil", 0.3F, 1.0F) {
      private static final String __OBFID = "CL_00000202";

      public String func_150495_a() {
         return "dig.stone";
      }

      public String func_150496_b() {
         return "random.anvil_land";
      }
   };
   protected boolean field_149787_q;
   protected int field_149786_r;
   protected boolean field_149785_s;
   protected int field_149784_t;
   protected boolean field_149783_u;
   protected float field_149782_v;
   protected float field_149781_w;
   protected boolean field_149791_x = true;
   protected boolean field_149790_y = true;
   protected boolean field_149789_z;
   protected boolean field_149758_A;
   protected double field_149759_B;
   protected double field_149760_C;
   protected double field_149754_D;
   protected double field_149755_E;
   protected double field_149756_F;
   protected double field_149757_G;
   public Block.SoundType field_149762_H;
   public float field_149763_I;
   protected final Material field_149764_J;
   public float field_149765_K;
   private String field_149770_b;
   @SideOnly(Side.CLIENT)
   protected IIcon field_149761_L;
   private static final String __OBFID = "CL_00000199";
   protected ThreadLocal harvesters = new ThreadLocal();
   private ThreadLocal silk_check_meta = new ThreadLocal();
   private boolean isTileProvider = this instanceof ITileEntityProvider;
   private String[] harvestTool = new String[16];
   private int[] harvestLevel = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
   protected ThreadLocal captureDrops = new ThreadLocal() {
      protected Boolean initialValue() {
         return false;
      }
   };
   protected ThreadLocal capturedDrops = new ThreadLocal() {
      protected List initialValue() {
         return new ArrayList();
      }
   };

   public static int func_149682_b(Block p_149682_0_) {
      return field_149771_c.func_148757_b(p_149682_0_);
   }

   public static Block func_149729_e(int p_149729_0_) {
      Block ret = (Block)field_149771_c.func_148754_a(p_149729_0_);
      return ret == null ? Blocks.field_150350_a : ret;
   }

   public static Block func_149634_a(Item p_149634_0_) {
      return func_149729_e(Item.func_150891_b(p_149634_0_));
   }

   public static Block func_149684_b(String p_149684_0_) {
      if (field_149771_c.func_148741_d(p_149684_0_)) {
         return (Block)field_149771_c.func_82594_a(p_149684_0_);
      } else {
         try {
            return (Block)field_149771_c.func_148754_a(Integer.parseInt(p_149684_0_));
         } catch (NumberFormatException var2) {
            return null;
         }
      }
   }

   public boolean func_149730_j() {
      return this.field_149787_q;
   }

   public int func_149717_k() {
      return this.field_149786_r;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149751_l() {
      return this.field_149785_s;
   }

   public int func_149750_m() {
      return this.field_149784_t;
   }

   public boolean func_149710_n() {
      return this.field_149783_u;
   }

   public Material func_149688_o() {
      return this.field_149764_J;
   }

   public MapColor func_149728_f(int p_149728_1_) {
      return this.func_149688_o().func_151565_r();
   }

   public static void func_149671_p() {
      field_149771_c.func_148756_a(0, "air", (new BlockAir()).func_149663_c("air"));
      field_149771_c.func_148756_a(1, "stone", (new BlockStone()).func_149711_c(1.5F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("stone").func_149658_d("stone"));
      field_149771_c.func_148756_a(2, "grass", (new BlockGrass()).func_149711_c(0.6F).func_149672_a(field_149779_h).func_149663_c("grass").func_149658_d("grass"));
      field_149771_c.func_148756_a(3, "dirt", (new BlockDirt()).func_149711_c(0.5F).func_149672_a(field_149767_g).func_149663_c("dirt").func_149658_d("dirt"));
      Block block = (new Block(Material.field_151576_e)).func_149711_c(2.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("stonebrick").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("cobblestone");
      field_149771_c.func_148756_a(4, "cobblestone", block);
      Block block1 = (new BlockWood()).func_149711_c(2.0F).func_149752_b(5.0F).func_149672_a(field_149766_f).func_149663_c("wood").func_149658_d("planks");
      field_149771_c.func_148756_a(5, "planks", block1);
      field_149771_c.func_148756_a(6, "sapling", (new BlockSapling()).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("sapling").func_149658_d("sapling"));
      field_149771_c.func_148756_a(7, "bedrock", (new Block(Material.field_151576_e)).func_149722_s().func_149752_b(6000000.0F).func_149672_a(field_149780_i).func_149663_c("bedrock").func_149649_H().func_149647_a(CreativeTabs.field_78030_b).func_149658_d("bedrock"));
      field_149771_c.func_148756_a(8, "flowing_water", (new BlockDynamicLiquid(Material.field_151586_h)).func_149711_c(100.0F).func_149713_g(3).func_149663_c("water").func_149649_H().func_149658_d("water_flow"));
      field_149771_c.func_148756_a(9, "water", (new BlockStaticLiquid(Material.field_151586_h)).func_149711_c(100.0F).func_149713_g(3).func_149663_c("water").func_149649_H().func_149658_d("water_still"));
      field_149771_c.func_148756_a(10, "flowing_lava", (new BlockDynamicLiquid(Material.field_151587_i)).func_149711_c(100.0F).func_149715_a(1.0F).func_149663_c("lava").func_149649_H().func_149658_d("lava_flow"));
      field_149771_c.func_148756_a(11, "lava", (new BlockStaticLiquid(Material.field_151587_i)).func_149711_c(100.0F).func_149715_a(1.0F).func_149663_c("lava").func_149649_H().func_149658_d("lava_still"));
      field_149771_c.func_148756_a(12, "sand", (new BlockSand()).func_149711_c(0.5F).func_149672_a(field_149776_m).func_149663_c("sand").func_149658_d("sand"));
      field_149771_c.func_148756_a(13, "gravel", (new BlockGravel()).func_149711_c(0.6F).func_149672_a(field_149767_g).func_149663_c("gravel").func_149658_d("gravel"));
      field_149771_c.func_148756_a(14, "gold_ore", (new BlockOre()).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("oreGold").func_149658_d("gold_ore"));
      field_149771_c.func_148756_a(15, "iron_ore", (new BlockOre()).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("oreIron").func_149658_d("iron_ore"));
      field_149771_c.func_148756_a(16, "coal_ore", (new BlockOre()).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("oreCoal").func_149658_d("coal_ore"));
      field_149771_c.func_148756_a(17, "log", (new BlockOldLog()).func_149663_c("log").func_149658_d("log"));
      field_149771_c.func_148756_a(18, "leaves", (new BlockOldLeaf()).func_149663_c("leaves").func_149658_d("leaves"));
      field_149771_c.func_148756_a(19, "sponge", (new BlockSponge()).func_149711_c(0.6F).func_149672_a(field_149779_h).func_149663_c("sponge").func_149658_d("sponge"));
      field_149771_c.func_148756_a(20, "glass", (new BlockGlass(Material.field_151592_s, false)).func_149711_c(0.3F).func_149672_a(field_149778_k).func_149663_c("glass").func_149658_d("glass"));
      field_149771_c.func_148756_a(21, "lapis_ore", (new BlockOre()).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("oreLapis").func_149658_d("lapis_ore"));
      field_149771_c.func_148756_a(22, "lapis_block", (new BlockCompressed(MapColor.field_151652_H)).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("blockLapis").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("lapis_block"));
      field_149771_c.func_148756_a(23, "dispenser", (new BlockDispenser()).func_149711_c(3.5F).func_149672_a(field_149780_i).func_149663_c("dispenser").func_149658_d("dispenser"));
      Block block2 = (new BlockSandStone()).func_149672_a(field_149780_i).func_149711_c(0.8F).func_149663_c("sandStone").func_149658_d("sandstone");
      field_149771_c.func_148756_a(24, "sandstone", block2);
      field_149771_c.func_148756_a(25, "noteblock", (new BlockNote()).func_149711_c(0.8F).func_149663_c("musicBlock").func_149658_d("noteblock"));
      field_149771_c.func_148756_a(26, "bed", (new BlockBed()).func_149711_c(0.2F).func_149663_c("bed").func_149649_H().func_149658_d("bed"));
      field_149771_c.func_148756_a(27, "golden_rail", (new BlockRailPowered()).func_149711_c(0.7F).func_149672_a(field_149777_j).func_149663_c("goldenRail").func_149658_d("rail_golden"));
      field_149771_c.func_148756_a(28, "detector_rail", (new BlockRailDetector()).func_149711_c(0.7F).func_149672_a(field_149777_j).func_149663_c("detectorRail").func_149658_d("rail_detector"));
      field_149771_c.func_148756_a(29, "sticky_piston", (new BlockPistonBase(true)).func_149663_c("pistonStickyBase"));
      field_149771_c.func_148756_a(30, "web", (new BlockWeb()).func_149713_g(1).func_149711_c(4.0F).func_149663_c("web").func_149658_d("web"));
      field_149771_c.func_148756_a(31, "tallgrass", (new BlockTallGrass()).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("tallgrass"));
      field_149771_c.func_148756_a(32, "deadbush", (new BlockDeadBush()).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("deadbush").func_149658_d("deadbush"));
      field_149771_c.func_148756_a(33, "piston", (new BlockPistonBase(false)).func_149663_c("pistonBase"));
      field_149771_c.func_148756_a(34, "piston_head", new BlockPistonExtension());
      field_149771_c.func_148756_a(35, "wool", (new BlockColored(Material.field_151580_n)).func_149711_c(0.8F).func_149672_a(field_149775_l).func_149663_c("cloth").func_149658_d("wool_colored"));
      field_149771_c.func_148756_a(36, "piston_extension", new BlockPistonMoving());
      field_149771_c.func_148756_a(37, "yellow_flower", (new BlockFlower(0)).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("flower1").func_149658_d("flower_dandelion"));
      field_149771_c.func_148756_a(38, "red_flower", (new BlockFlower(1)).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("flower2").func_149658_d("flower_rose"));
      field_149771_c.func_148756_a(39, "brown_mushroom", (new BlockMushroom()).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149715_a(0.125F).func_149663_c("mushroom").func_149658_d("mushroom_brown"));
      field_149771_c.func_148756_a(40, "red_mushroom", (new BlockMushroom()).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("mushroom").func_149658_d("mushroom_red"));
      field_149771_c.func_148756_a(41, "gold_block", (new BlockCompressed(MapColor.field_151647_F)).func_149711_c(3.0F).func_149752_b(10.0F).func_149672_a(field_149777_j).func_149663_c("blockGold").func_149658_d("gold_block"));
      field_149771_c.func_148756_a(42, "iron_block", (new BlockCompressed(MapColor.field_151668_h)).func_149711_c(5.0F).func_149752_b(10.0F).func_149672_a(field_149777_j).func_149663_c("blockIron").func_149658_d("iron_block"));
      field_149771_c.func_148756_a(43, "double_stone_slab", (new BlockStoneSlab(true)).func_149711_c(2.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("stoneSlab"));
      field_149771_c.func_148756_a(44, "stone_slab", (new BlockStoneSlab(false)).func_149711_c(2.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("stoneSlab"));
      Block block3 = (new Block(Material.field_151576_e)).func_149711_c(2.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("brick").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("brick");
      field_149771_c.func_148756_a(45, "brick_block", block3);
      field_149771_c.func_148756_a(46, "tnt", (new BlockTNT()).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("tnt").func_149658_d("tnt"));
      field_149771_c.func_148756_a(47, "bookshelf", (new BlockBookshelf()).func_149711_c(1.5F).func_149672_a(field_149766_f).func_149663_c("bookshelf").func_149658_d("bookshelf"));
      field_149771_c.func_148756_a(48, "mossy_cobblestone", (new Block(Material.field_151576_e)).func_149711_c(2.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("stoneMoss").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("cobblestone_mossy"));
      field_149771_c.func_148756_a(49, "obsidian", (new BlockObsidian()).func_149711_c(50.0F).func_149752_b(2000.0F).func_149672_a(field_149780_i).func_149663_c("obsidian").func_149658_d("obsidian"));
      field_149771_c.func_148756_a(50, "torch", (new BlockTorch()).func_149711_c(0.0F).func_149715_a(0.9375F).func_149672_a(field_149766_f).func_149663_c("torch").func_149658_d("torch_on"));
      field_149771_c.func_148756_a(51, "fire", (new BlockFire()).func_149711_c(0.0F).func_149715_a(1.0F).func_149672_a(field_149766_f).func_149663_c("fire").func_149649_H().func_149658_d("fire"));
      field_149771_c.func_148756_a(52, "mob_spawner", (new BlockMobSpawner()).func_149711_c(5.0F).func_149672_a(field_149777_j).func_149663_c("mobSpawner").func_149649_H().func_149658_d("mob_spawner"));
      field_149771_c.func_148756_a(53, "oak_stairs", (new BlockStairs(block1, 0)).func_149663_c("stairsWood"));
      field_149771_c.func_148756_a(54, "chest", (new BlockChest(0)).func_149711_c(2.5F).func_149672_a(field_149766_f).func_149663_c("chest"));
      field_149771_c.func_148756_a(55, "redstone_wire", (new BlockRedstoneWire()).func_149711_c(0.0F).func_149672_a(field_149769_e).func_149663_c("redstoneDust").func_149649_H().func_149658_d("redstone_dust"));
      field_149771_c.func_148756_a(56, "diamond_ore", (new BlockOre()).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("oreDiamond").func_149658_d("diamond_ore"));
      field_149771_c.func_148756_a(57, "diamond_block", (new BlockCompressed(MapColor.field_151648_G)).func_149711_c(5.0F).func_149752_b(10.0F).func_149672_a(field_149777_j).func_149663_c("blockDiamond").func_149658_d("diamond_block"));
      field_149771_c.func_148756_a(58, "crafting_table", (new BlockWorkbench()).func_149711_c(2.5F).func_149672_a(field_149766_f).func_149663_c("workbench").func_149658_d("crafting_table"));
      field_149771_c.func_148756_a(59, "wheat", (new BlockCrops()).func_149663_c("crops").func_149658_d("wheat"));
      Block block4 = (new BlockFarmland()).func_149711_c(0.6F).func_149672_a(field_149767_g).func_149663_c("farmland").func_149658_d("farmland");
      field_149771_c.func_148756_a(60, "farmland", block4);
      field_149771_c.func_148756_a(61, "furnace", (new BlockFurnace(false)).func_149711_c(3.5F).func_149672_a(field_149780_i).func_149663_c("furnace").func_149647_a(CreativeTabs.field_78031_c));
      field_149771_c.func_148756_a(62, "lit_furnace", (new BlockFurnace(true)).func_149711_c(3.5F).func_149672_a(field_149780_i).func_149715_a(0.875F).func_149663_c("furnace"));
      field_149771_c.func_148756_a(63, "standing_sign", (new BlockSign(TileEntitySign.class, true)).func_149711_c(1.0F).func_149672_a(field_149766_f).func_149663_c("sign").func_149649_H());
      field_149771_c.func_148756_a(64, "wooden_door", (new BlockDoor(Material.field_151575_d)).func_149711_c(3.0F).func_149672_a(field_149766_f).func_149663_c("doorWood").func_149649_H().func_149658_d("door_wood"));
      field_149771_c.func_148756_a(65, "ladder", (new BlockLadder()).func_149711_c(0.4F).func_149672_a(field_149774_o).func_149663_c("ladder").func_149658_d("ladder"));
      field_149771_c.func_148756_a(66, "rail", (new BlockRail()).func_149711_c(0.7F).func_149672_a(field_149777_j).func_149663_c("rail").func_149658_d("rail_normal"));
      field_149771_c.func_148756_a(67, "stone_stairs", (new BlockStairs(block, 0)).func_149663_c("stairsStone"));
      field_149771_c.func_148756_a(68, "wall_sign", (new BlockSign(TileEntitySign.class, false)).func_149711_c(1.0F).func_149672_a(field_149766_f).func_149663_c("sign").func_149649_H());
      field_149771_c.func_148756_a(69, "lever", (new BlockLever()).func_149711_c(0.5F).func_149672_a(field_149766_f).func_149663_c("lever").func_149658_d("lever"));
      field_149771_c.func_148756_a(70, "stone_pressure_plate", (new BlockPressurePlate("stone", Material.field_151576_e, Sensitivity.mobs)).func_149711_c(0.5F).func_149672_a(field_149780_i).func_149663_c("pressurePlate"));
      field_149771_c.func_148756_a(71, "iron_door", (new BlockDoor(Material.field_151573_f)).func_149711_c(5.0F).func_149672_a(field_149777_j).func_149663_c("doorIron").func_149649_H().func_149658_d("door_iron"));
      field_149771_c.func_148756_a(72, "wooden_pressure_plate", (new BlockPressurePlate("planks_oak", Material.field_151575_d, Sensitivity.everything)).func_149711_c(0.5F).func_149672_a(field_149766_f).func_149663_c("pressurePlate"));
      field_149771_c.func_148756_a(73, "redstone_ore", (new BlockRedstoneOre(false)).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("oreRedstone").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("redstone_ore"));
      field_149771_c.func_148756_a(74, "lit_redstone_ore", (new BlockRedstoneOre(true)).func_149715_a(0.625F).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("oreRedstone").func_149658_d("redstone_ore"));
      field_149771_c.func_148756_a(75, "unlit_redstone_torch", (new BlockRedstoneTorch(false)).func_149711_c(0.0F).func_149672_a(field_149766_f).func_149663_c("notGate").func_149658_d("redstone_torch_off"));
      field_149771_c.func_148756_a(76, "redstone_torch", (new BlockRedstoneTorch(true)).func_149711_c(0.0F).func_149715_a(0.5F).func_149672_a(field_149766_f).func_149663_c("notGate").func_149647_a(CreativeTabs.field_78028_d).func_149658_d("redstone_torch_on"));
      field_149771_c.func_148756_a(77, "stone_button", (new BlockButtonStone()).func_149711_c(0.5F).func_149672_a(field_149780_i).func_149663_c("button"));
      field_149771_c.func_148756_a(78, "snow_layer", (new BlockSnow()).func_149711_c(0.1F).func_149672_a(field_149773_n).func_149663_c("snow").func_149713_g(0).func_149658_d("snow"));
      field_149771_c.func_148756_a(79, "ice", (new BlockIce()).func_149711_c(0.5F).func_149713_g(3).func_149672_a(field_149778_k).func_149663_c("ice").func_149658_d("ice"));
      field_149771_c.func_148756_a(80, "snow", (new BlockSnowBlock()).func_149711_c(0.2F).func_149672_a(field_149773_n).func_149663_c("snow").func_149658_d("snow"));
      field_149771_c.func_148756_a(81, "cactus", (new BlockCactus()).func_149711_c(0.4F).func_149672_a(field_149775_l).func_149663_c("cactus").func_149658_d("cactus"));
      field_149771_c.func_148756_a(82, "clay", (new BlockClay()).func_149711_c(0.6F).func_149672_a(field_149767_g).func_149663_c("clay").func_149658_d("clay"));
      field_149771_c.func_148756_a(83, "reeds", (new BlockReed()).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("reeds").func_149649_H().func_149658_d("reeds"));
      field_149771_c.func_148756_a(84, "jukebox", (new BlockJukebox()).func_149711_c(2.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("jukebox").func_149658_d("jukebox"));
      field_149771_c.func_148756_a(85, "fence", (new BlockFence("planks_oak", Material.field_151575_d)).func_149711_c(2.0F).func_149752_b(5.0F).func_149672_a(field_149766_f).func_149663_c("fence"));
      Block block5 = (new BlockPumpkin(false)).func_149711_c(1.0F).func_149672_a(field_149766_f).func_149663_c("pumpkin").func_149658_d("pumpkin");
      field_149771_c.func_148756_a(86, "pumpkin", block5);
      field_149771_c.func_148756_a(87, "netherrack", (new BlockNetherrack()).func_149711_c(0.4F).func_149672_a(field_149780_i).func_149663_c("hellrock").func_149658_d("netherrack"));
      field_149771_c.func_148756_a(88, "soul_sand", (new BlockSoulSand()).func_149711_c(0.5F).func_149672_a(field_149776_m).func_149663_c("hellsand").func_149658_d("soul_sand"));
      field_149771_c.func_148756_a(89, "glowstone", (new BlockGlowstone(Material.field_151592_s)).func_149711_c(0.3F).func_149672_a(field_149778_k).func_149715_a(1.0F).func_149663_c("lightgem").func_149658_d("glowstone"));
      field_149771_c.func_148756_a(90, "portal", (new BlockPortal()).func_149711_c(-1.0F).func_149672_a(field_149778_k).func_149715_a(0.75F).func_149663_c("portal").func_149658_d("portal"));
      field_149771_c.func_148756_a(91, "lit_pumpkin", (new BlockPumpkin(true)).func_149711_c(1.0F).func_149672_a(field_149766_f).func_149715_a(1.0F).func_149663_c("litpumpkin").func_149658_d("pumpkin"));
      field_149771_c.func_148756_a(92, "cake", (new BlockCake()).func_149711_c(0.5F).func_149672_a(field_149775_l).func_149663_c("cake").func_149649_H().func_149658_d("cake"));
      field_149771_c.func_148756_a(93, "unpowered_repeater", (new BlockRedstoneRepeater(false)).func_149711_c(0.0F).func_149672_a(field_149766_f).func_149663_c("diode").func_149649_H().func_149658_d("repeater_off"));
      field_149771_c.func_148756_a(94, "powered_repeater", (new BlockRedstoneRepeater(true)).func_149711_c(0.0F).func_149715_a(0.625F).func_149672_a(field_149766_f).func_149663_c("diode").func_149649_H().func_149658_d("repeater_on"));
      field_149771_c.func_148756_a(95, "stained_glass", (new BlockStainedGlass(Material.field_151592_s)).func_149711_c(0.3F).func_149672_a(field_149778_k).func_149663_c("stainedGlass").func_149658_d("glass"));
      field_149771_c.func_148756_a(96, "trapdoor", (new BlockTrapDoor(Material.field_151575_d)).func_149711_c(3.0F).func_149672_a(field_149766_f).func_149663_c("trapdoor").func_149649_H().func_149658_d("trapdoor"));
      field_149771_c.func_148756_a(97, "monster_egg", (new BlockSilverfish()).func_149711_c(0.75F).func_149663_c("monsterStoneEgg"));
      Block block6 = (new BlockStoneBrick()).func_149711_c(1.5F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("stonebricksmooth").func_149658_d("stonebrick");
      field_149771_c.func_148756_a(98, "stonebrick", block6);
      field_149771_c.func_148756_a(99, "brown_mushroom_block", (new BlockHugeMushroom(Material.field_151575_d, 0)).func_149711_c(0.2F).func_149672_a(field_149766_f).func_149663_c("mushroom").func_149658_d("mushroom_block"));
      field_149771_c.func_148756_a(100, "red_mushroom_block", (new BlockHugeMushroom(Material.field_151575_d, 1)).func_149711_c(0.2F).func_149672_a(field_149766_f).func_149663_c("mushroom").func_149658_d("mushroom_block"));
      field_149771_c.func_148756_a(101, "iron_bars", (new BlockPane("iron_bars", "iron_bars", Material.field_151573_f, true)).func_149711_c(5.0F).func_149752_b(10.0F).func_149672_a(field_149777_j).func_149663_c("fenceIron"));
      field_149771_c.func_148756_a(102, "glass_pane", (new BlockPane("glass", "glass_pane_top", Material.field_151592_s, false)).func_149711_c(0.3F).func_149672_a(field_149778_k).func_149663_c("thinGlass"));
      Block block7 = (new BlockMelon()).func_149711_c(1.0F).func_149672_a(field_149766_f).func_149663_c("melon").func_149658_d("melon");
      field_149771_c.func_148756_a(103, "melon_block", block7);
      field_149771_c.func_148756_a(104, "pumpkin_stem", (new BlockStem(block5)).func_149711_c(0.0F).func_149672_a(field_149766_f).func_149663_c("pumpkinStem").func_149658_d("pumpkin_stem"));
      field_149771_c.func_148756_a(105, "melon_stem", (new BlockStem(block7)).func_149711_c(0.0F).func_149672_a(field_149766_f).func_149663_c("pumpkinStem").func_149658_d("melon_stem"));
      field_149771_c.func_148756_a(106, "vine", (new BlockVine()).func_149711_c(0.2F).func_149672_a(field_149779_h).func_149663_c("vine").func_149658_d("vine"));
      field_149771_c.func_148756_a(107, "fence_gate", (new BlockFenceGate()).func_149711_c(2.0F).func_149752_b(5.0F).func_149672_a(field_149766_f).func_149663_c("fenceGate"));
      field_149771_c.func_148756_a(108, "brick_stairs", (new BlockStairs(block3, 0)).func_149663_c("stairsBrick"));
      field_149771_c.func_148756_a(109, "stone_brick_stairs", (new BlockStairs(block6, 0)).func_149663_c("stairsStoneBrickSmooth"));
      field_149771_c.func_148756_a(110, "mycelium", (new BlockMycelium()).func_149711_c(0.6F).func_149672_a(field_149779_h).func_149663_c("mycel").func_149658_d("mycelium"));
      field_149771_c.func_148756_a(111, "waterlily", (new BlockLilyPad()).func_149711_c(0.0F).func_149672_a(field_149779_h).func_149663_c("waterlily").func_149658_d("waterlily"));
      Block block8 = (new Block(Material.field_151576_e)).func_149711_c(2.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("netherBrick").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("nether_brick");
      field_149771_c.func_148756_a(112, "nether_brick", block8);
      field_149771_c.func_148756_a(113, "nether_brick_fence", (new BlockFence("nether_brick", Material.field_151576_e)).func_149711_c(2.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("netherFence"));
      field_149771_c.func_148756_a(114, "nether_brick_stairs", (new BlockStairs(block8, 0)).func_149663_c("stairsNetherBrick"));
      field_149771_c.func_148756_a(115, "nether_wart", (new BlockNetherWart()).func_149663_c("netherStalk").func_149658_d("nether_wart"));
      field_149771_c.func_148756_a(116, "enchanting_table", (new BlockEnchantmentTable()).func_149711_c(5.0F).func_149752_b(2000.0F).func_149663_c("enchantmentTable").func_149658_d("enchanting_table"));
      field_149771_c.func_148756_a(117, "brewing_stand", (new BlockBrewingStand()).func_149711_c(0.5F).func_149715_a(0.125F).func_149663_c("brewingStand").func_149658_d("brewing_stand"));
      field_149771_c.func_148756_a(118, "cauldron", (new BlockCauldron()).func_149711_c(2.0F).func_149663_c("cauldron").func_149658_d("cauldron"));
      field_149771_c.func_148756_a(119, "end_portal", (new BlockEndPortal(Material.field_151567_E)).func_149711_c(-1.0F).func_149752_b(6000000.0F));
      field_149771_c.func_148756_a(120, "end_portal_frame", (new BlockEndPortalFrame()).func_149672_a(field_149778_k).func_149715_a(0.125F).func_149711_c(-1.0F).func_149663_c("endPortalFrame").func_149752_b(6000000.0F).func_149647_a(CreativeTabs.field_78031_c).func_149658_d("endframe"));
      field_149771_c.func_148756_a(121, "end_stone", (new Block(Material.field_151576_e)).func_149711_c(3.0F).func_149752_b(15.0F).func_149672_a(field_149780_i).func_149663_c("whiteStone").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("end_stone"));
      field_149771_c.func_148756_a(122, "dragon_egg", (new BlockDragonEgg()).func_149711_c(3.0F).func_149752_b(15.0F).func_149672_a(field_149780_i).func_149715_a(0.125F).func_149663_c("dragonEgg").func_149658_d("dragon_egg"));
      field_149771_c.func_148756_a(123, "redstone_lamp", (new BlockRedstoneLight(false)).func_149711_c(0.3F).func_149672_a(field_149778_k).func_149663_c("redstoneLight").func_149647_a(CreativeTabs.field_78028_d).func_149658_d("redstone_lamp_off"));
      field_149771_c.func_148756_a(124, "lit_redstone_lamp", (new BlockRedstoneLight(true)).func_149711_c(0.3F).func_149672_a(field_149778_k).func_149663_c("redstoneLight").func_149658_d("redstone_lamp_on"));
      field_149771_c.func_148756_a(125, "double_wooden_slab", (new BlockWoodSlab(true)).func_149711_c(2.0F).func_149752_b(5.0F).func_149672_a(field_149766_f).func_149663_c("woodSlab"));
      field_149771_c.func_148756_a(126, "wooden_slab", (new BlockWoodSlab(false)).func_149711_c(2.0F).func_149752_b(5.0F).func_149672_a(field_149766_f).func_149663_c("woodSlab"));
      field_149771_c.func_148756_a(127, "cocoa", (new BlockCocoa()).func_149711_c(0.2F).func_149752_b(5.0F).func_149672_a(field_149766_f).func_149663_c("cocoa").func_149658_d("cocoa"));
      field_149771_c.func_148756_a(128, "sandstone_stairs", (new BlockStairs(block2, 0)).func_149663_c("stairsSandStone"));
      field_149771_c.func_148756_a(129, "emerald_ore", (new BlockOre()).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("oreEmerald").func_149658_d("emerald_ore"));
      field_149771_c.func_148756_a(130, "ender_chest", (new BlockEnderChest()).func_149711_c(22.5F).func_149752_b(1000.0F).func_149672_a(field_149780_i).func_149663_c("enderChest").func_149715_a(0.5F));
      field_149771_c.func_148756_a(131, "tripwire_hook", (new BlockTripWireHook()).func_149663_c("tripWireSource").func_149658_d("trip_wire_source"));
      field_149771_c.func_148756_a(132, "tripwire", (new BlockTripWire()).func_149663_c("tripWire").func_149658_d("trip_wire"));
      field_149771_c.func_148756_a(133, "emerald_block", (new BlockCompressed(MapColor.field_151653_I)).func_149711_c(5.0F).func_149752_b(10.0F).func_149672_a(field_149777_j).func_149663_c("blockEmerald").func_149658_d("emerald_block"));
      field_149771_c.func_148756_a(134, "spruce_stairs", (new BlockStairs(block1, 1)).func_149663_c("stairsWoodSpruce"));
      field_149771_c.func_148756_a(135, "birch_stairs", (new BlockStairs(block1, 2)).func_149663_c("stairsWoodBirch"));
      field_149771_c.func_148756_a(136, "jungle_stairs", (new BlockStairs(block1, 3)).func_149663_c("stairsWoodJungle"));
      field_149771_c.func_148756_a(137, "command_block", (new BlockCommandBlock()).func_149722_s().func_149752_b(6000000.0F).func_149663_c("commandBlock").func_149658_d("command_block"));
      field_149771_c.func_148756_a(138, "beacon", (new BlockBeacon()).func_149663_c("beacon").func_149715_a(1.0F).func_149658_d("beacon"));
      field_149771_c.func_148756_a(139, "cobblestone_wall", (new BlockWall(block)).func_149663_c("cobbleWall"));
      field_149771_c.func_148756_a(140, "flower_pot", (new BlockFlowerPot()).func_149711_c(0.0F).func_149672_a(field_149769_e).func_149663_c("flowerPot").func_149658_d("flower_pot"));
      field_149771_c.func_148756_a(141, "carrots", (new BlockCarrot()).func_149663_c("carrots").func_149658_d("carrots"));
      field_149771_c.func_148756_a(142, "potatoes", (new BlockPotato()).func_149663_c("potatoes").func_149658_d("potatoes"));
      field_149771_c.func_148756_a(143, "wooden_button", (new BlockButtonWood()).func_149711_c(0.5F).func_149672_a(field_149766_f).func_149663_c("button"));
      field_149771_c.func_148756_a(144, "skull", (new BlockSkull()).func_149711_c(1.0F).func_149672_a(field_149780_i).func_149663_c("skull").func_149658_d("skull"));
      field_149771_c.func_148756_a(145, "anvil", (new BlockAnvil()).func_149711_c(5.0F).func_149672_a(field_149788_p).func_149752_b(2000.0F).func_149663_c("anvil"));
      field_149771_c.func_148756_a(146, "trapped_chest", (new BlockChest(1)).func_149711_c(2.5F).func_149672_a(field_149766_f).func_149663_c("chestTrap"));
      field_149771_c.func_148756_a(147, "light_weighted_pressure_plate", (new BlockPressurePlateWeighted("gold_block", Material.field_151573_f, 15)).func_149711_c(0.5F).func_149672_a(field_149766_f).func_149663_c("weightedPlate_light"));
      field_149771_c.func_148756_a(148, "heavy_weighted_pressure_plate", (new BlockPressurePlateWeighted("iron_block", Material.field_151573_f, 150)).func_149711_c(0.5F).func_149672_a(field_149766_f).func_149663_c("weightedPlate_heavy"));
      field_149771_c.func_148756_a(149, "unpowered_comparator", (new BlockRedstoneComparator(false)).func_149711_c(0.0F).func_149672_a(field_149766_f).func_149663_c("comparator").func_149649_H().func_149658_d("comparator_off"));
      field_149771_c.func_148756_a(150, "powered_comparator", (new BlockRedstoneComparator(true)).func_149711_c(0.0F).func_149715_a(0.625F).func_149672_a(field_149766_f).func_149663_c("comparator").func_149649_H().func_149658_d("comparator_on"));
      field_149771_c.func_148756_a(151, "daylight_detector", (new BlockDaylightDetector()).func_149711_c(0.2F).func_149672_a(field_149766_f).func_149663_c("daylightDetector").func_149658_d("daylight_detector"));
      field_149771_c.func_148756_a(152, "redstone_block", (new BlockCompressedPowered(MapColor.field_151656_f)).func_149711_c(5.0F).func_149752_b(10.0F).func_149672_a(field_149777_j).func_149663_c("blockRedstone").func_149658_d("redstone_block"));
      field_149771_c.func_148756_a(153, "quartz_ore", (new BlockOre()).func_149711_c(3.0F).func_149752_b(5.0F).func_149672_a(field_149780_i).func_149663_c("netherquartz").func_149658_d("quartz_ore"));
      field_149771_c.func_148756_a(154, "hopper", (new BlockHopper()).func_149711_c(3.0F).func_149752_b(8.0F).func_149672_a(field_149766_f).func_149663_c("hopper").func_149658_d("hopper"));
      Block block9 = (new BlockQuartz()).func_149672_a(field_149780_i).func_149711_c(0.8F).func_149663_c("quartzBlock").func_149658_d("quartz_block");
      field_149771_c.func_148756_a(155, "quartz_block", block9);
      field_149771_c.func_148756_a(156, "quartz_stairs", (new BlockStairs(block9, 0)).func_149663_c("stairsQuartz"));
      field_149771_c.func_148756_a(157, "activator_rail", (new BlockRailPowered()).func_149711_c(0.7F).func_149672_a(field_149777_j).func_149663_c("activatorRail").func_149658_d("rail_activator"));
      field_149771_c.func_148756_a(158, "dropper", (new BlockDropper()).func_149711_c(3.5F).func_149672_a(field_149780_i).func_149663_c("dropper").func_149658_d("dropper"));
      field_149771_c.func_148756_a(159, "stained_hardened_clay", (new BlockColored(Material.field_151576_e)).func_149711_c(1.25F).func_149752_b(7.0F).func_149672_a(field_149780_i).func_149663_c("clayHardenedStained").func_149658_d("hardened_clay_stained"));
      field_149771_c.func_148756_a(160, "stained_glass_pane", (new BlockStainedGlassPane()).func_149711_c(0.3F).func_149672_a(field_149778_k).func_149663_c("thinStainedGlass").func_149658_d("glass"));
      field_149771_c.func_148756_a(161, "leaves2", (new BlockNewLeaf()).func_149663_c("leaves").func_149658_d("leaves"));
      field_149771_c.func_148756_a(162, "log2", (new BlockNewLog()).func_149663_c("log").func_149658_d("log"));
      field_149771_c.func_148756_a(163, "acacia_stairs", (new BlockStairs(block1, 4)).func_149663_c("stairsWoodAcacia"));
      field_149771_c.func_148756_a(164, "dark_oak_stairs", (new BlockStairs(block1, 5)).func_149663_c("stairsWoodDarkOak"));
      field_149771_c.func_148756_a(170, "hay_block", (new BlockHay()).func_149711_c(0.5F).func_149672_a(field_149779_h).func_149663_c("hayBlock").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("hay_block"));
      field_149771_c.func_148756_a(171, "carpet", (new BlockCarpet()).func_149711_c(0.1F).func_149672_a(field_149775_l).func_149663_c("woolCarpet").func_149713_g(0));
      field_149771_c.func_148756_a(172, "hardened_clay", (new BlockHardenedClay()).func_149711_c(1.25F).func_149752_b(7.0F).func_149672_a(field_149780_i).func_149663_c("clayHardened").func_149658_d("hardened_clay"));
      field_149771_c.func_148756_a(173, "coal_block", (new Block(Material.field_151576_e)).func_149711_c(5.0F).func_149752_b(10.0F).func_149672_a(field_149780_i).func_149663_c("blockCoal").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("coal_block"));
      field_149771_c.func_148756_a(174, "packed_ice", (new BlockPackedIce()).func_149711_c(0.5F).func_149672_a(field_149778_k).func_149663_c("icePacked").func_149658_d("ice_packed"));
      field_149771_c.func_148756_a(175, "double_plant", new BlockDoublePlant());
      Iterator iterator = field_149771_c.iterator();

      while(true) {
         while(iterator.hasNext()) {
            Block block10 = (Block)iterator.next();
            if (block10.field_149764_J == Material.field_151579_a) {
               block10.field_149783_u = false;
            } else {
               boolean flag = false;
               boolean flag1 = block10.func_149645_b() == 10;
               boolean flag2 = block10 instanceof BlockSlab;
               boolean flag3 = block10 == block4;
               boolean flag4 = block10.field_149785_s;
               boolean flag5 = block10.field_149786_r == 0;
               if (flag1 || flag2 || flag3 || flag4 || flag5) {
                  flag = true;
               }

               block10.field_149783_u = flag;
            }
         }

         return;
      }
   }

   protected Block(Material p_i45394_1_) {
      this.field_149762_H = field_149769_e;
      this.field_149763_I = 1.0F;
      this.field_149765_K = 0.6F;
      this.field_149764_J = p_i45394_1_;
      this.func_149676_a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      this.field_149787_q = this.func_149662_c();
      this.field_149786_r = this.func_149662_c() ? 255 : 0;
      this.field_149785_s = !p_i45394_1_.func_76228_b();
   }

   public Block func_149672_a(Block.SoundType p_149672_1_) {
      this.field_149762_H = p_149672_1_;
      return this;
   }

   public Block func_149713_g(int p_149713_1_) {
      this.field_149786_r = p_149713_1_;
      return this;
   }

   public Block func_149715_a(float p_149715_1_) {
      this.field_149784_t = (int)(15.0F * p_149715_1_);
      return this;
   }

   public Block func_149752_b(float p_149752_1_) {
      this.field_149781_w = p_149752_1_ * 3.0F;
      return this;
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149637_q() {
      return this.field_149764_J.func_76230_c() && this.func_149686_d();
   }

   public boolean func_149721_r() {
      return this.field_149764_J.func_76218_k() && this.func_149686_d() && !this.func_149744_f();
   }

   public boolean func_149686_d() {
      return true;
   }

   public boolean func_149655_b(IBlockAccess p_149655_1_, int p_149655_2_, int p_149655_3_, int p_149655_4_) {
      return !this.field_149764_J.func_76230_c();
   }

   public int func_149645_b() {
      return 0;
   }

   public Block func_149711_c(float p_149711_1_) {
      this.field_149782_v = p_149711_1_;
      if (this.field_149781_w < p_149711_1_ * 5.0F) {
         this.field_149781_w = p_149711_1_ * 5.0F;
      }

      return this;
   }

   public Block func_149722_s() {
      this.func_149711_c(-1.0F);
      return this;
   }

   public float func_149712_f(World p_149712_1_, int p_149712_2_, int p_149712_3_, int p_149712_4_) {
      return this.field_149782_v;
   }

   public Block func_149675_a(boolean p_149675_1_) {
      this.field_149789_z = p_149675_1_;
      return this;
   }

   public boolean func_149653_t() {
      return this.field_149789_z;
   }

   /** @deprecated */
   @Deprecated
   public boolean func_149716_u() {
      return this.hasTileEntity(0);
   }

   public final void func_149676_a(float p_149676_1_, float p_149676_2_, float p_149676_3_, float p_149676_4_, float p_149676_5_, float p_149676_6_) {
      this.field_149759_B = (double)p_149676_1_;
      this.field_149760_C = (double)p_149676_2_;
      this.field_149754_D = (double)p_149676_3_;
      this.field_149755_E = (double)p_149676_4_;
      this.field_149756_F = (double)p_149676_5_;
      this.field_149757_G = (double)p_149676_6_;
   }

   @SideOnly(Side.CLIENT)
   public int func_149677_c(IBlockAccess p_149677_1_, int p_149677_2_, int p_149677_3_, int p_149677_4_) {
      Block block = p_149677_1_.func_147439_a(p_149677_2_, p_149677_3_, p_149677_4_);
      int l = p_149677_1_.func_72802_i(p_149677_2_, p_149677_3_, p_149677_4_, block.getLightValue(p_149677_1_, p_149677_2_, p_149677_3_, p_149677_4_));
      if (l == 0 && block instanceof BlockSlab) {
         --p_149677_3_;
         block = p_149677_1_.func_147439_a(p_149677_2_, p_149677_3_, p_149677_4_);
         return p_149677_1_.func_72802_i(p_149677_2_, p_149677_3_, p_149677_4_, block.getLightValue(p_149677_1_, p_149677_2_, p_149677_3_, p_149677_4_));
      } else {
         return l;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149646_a(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_) {
      if (this.func_149662_c() && NoCubes.isBlockNatural(p_149646_1_.func_147439_a(p_149646_2_, p_149646_3_, p_149646_4_))) {
         return true;
      } else {
         return p_149646_5_ == 0 && this.field_149760_C > 0.0D ? true : (p_149646_5_ == 1 && this.field_149756_F < 1.0D ? true : (p_149646_5_ == 2 && this.field_149754_D > 0.0D ? true : (p_149646_5_ == 3 && this.field_149757_G < 1.0D ? true : (p_149646_5_ == 4 && this.field_149759_B > 0.0D ? true : (p_149646_5_ == 5 && this.field_149755_E < 1.0D ? true : !p_149646_1_.func_147439_a(p_149646_2_, p_149646_3_, p_149646_4_).func_149662_c())))));
      }
   }

   public boolean func_149747_d(IBlockAccess p_149747_1_, int p_149747_2_, int p_149747_3_, int p_149747_4_, int p_149747_5_) {
      return p_149747_1_.func_147439_a(p_149747_2_, p_149747_3_, p_149747_4_).func_149688_o().func_76220_a();
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149673_e(IBlockAccess p_149673_1_, int p_149673_2_, int p_149673_3_, int p_149673_4_, int p_149673_5_) {
      return this.func_149691_a(p_149673_5_, p_149673_1_.func_72805_g(p_149673_2_, p_149673_3_, p_149673_4_));
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149691_a(int p_149691_1_, int p_149691_2_) {
      return this.field_149761_L;
   }

   public void func_149743_a(World p_149743_1_, int p_149743_2_, int p_149743_3_, int p_149743_4_, AxisAlignedBB p_149743_5_, List p_149743_6_, Entity p_149743_7_) {
      AxisAlignedBB axisalignedbb1 = this.func_149668_a(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_);
      if (axisalignedbb1 != null && p_149743_5_.func_72326_a(axisalignedbb1)) {
         p_149743_6_.add(axisalignedbb1);
      }

   }

   public AxisAlignedBB func_149668_a(World p_149668_1_, int p_149668_2_, int p_149668_3_, int p_149668_4_) {
      return AxisAlignedBB.func_72330_a((double)p_149668_2_ + this.field_149759_B, (double)p_149668_3_ + this.field_149760_C, (double)p_149668_4_ + this.field_149754_D, (double)p_149668_2_ + this.field_149755_E, (double)p_149668_3_ + this.field_149756_F, (double)p_149668_4_ + this.field_149757_G);
   }

   @SideOnly(Side.CLIENT)
   public final IIcon func_149733_h(int p_149733_1_) {
      return this.func_149691_a(p_149733_1_, 0);
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB func_149633_g(World p_149633_1_, int p_149633_2_, int p_149633_3_, int p_149633_4_) {
      return AxisAlignedBB.func_72330_a((double)p_149633_2_ + this.field_149759_B, (double)p_149633_3_ + this.field_149760_C, (double)p_149633_4_ + this.field_149754_D, (double)p_149633_2_ + this.field_149755_E, (double)p_149633_3_ + this.field_149756_F, (double)p_149633_4_ + this.field_149757_G);
   }

   public boolean func_149662_c() {
      return true;
   }

   public boolean func_149678_a(int p_149678_1_, boolean p_149678_2_) {
      return this.func_149703_v();
   }

   public boolean func_149703_v() {
      return true;
   }

   public void func_149674_a(World p_149674_1_, int p_149674_2_, int p_149674_3_, int p_149674_4_, Random p_149674_5_) {
   }

   @SideOnly(Side.CLIENT)
   public void func_149734_b(World p_149734_1_, int p_149734_2_, int p_149734_3_, int p_149734_4_, Random p_149734_5_) {
   }

   public void func_149664_b(World p_149664_1_, int p_149664_2_, int p_149664_3_, int p_149664_4_, int p_149664_5_) {
   }

   public void func_149695_a(World p_149695_1_, int p_149695_2_, int p_149695_3_, int p_149695_4_, Block p_149695_5_) {
   }

   public int func_149738_a(World p_149738_1_) {
      return 10;
   }

   public void func_149726_b(World p_149726_1_, int p_149726_2_, int p_149726_3_, int p_149726_4_) {
   }

   public void func_149749_a(World p_149749_1_, int p_149749_2_, int p_149749_3_, int p_149749_4_, Block p_149749_5_, int p_149749_6_) {
      if (this.hasTileEntity(p_149749_6_) && !(this instanceof BlockContainer)) {
         p_149749_1_.func_147475_p(p_149749_2_, p_149749_3_, p_149749_4_);
      }

   }

   public int func_149745_a(Random p_149745_1_) {
      return 1;
   }

   public Item func_149650_a(int p_149650_1_, Random p_149650_2_, int p_149650_3_) {
      return Item.func_150898_a(this);
   }

   public float func_149737_a(EntityPlayer p_149737_1_, World p_149737_2_, int p_149737_3_, int p_149737_4_, int p_149737_5_) {
      return ForgeHooks.blockStrength(this, p_149737_1_, p_149737_2_, p_149737_3_, p_149737_4_, p_149737_5_);
   }

   public final void func_149697_b(World p_149697_1_, int p_149697_2_, int p_149697_3_, int p_149697_4_, int p_149697_5_, int p_149697_6_) {
      this.func_149690_a(p_149697_1_, p_149697_2_, p_149697_3_, p_149697_4_, p_149697_5_, 1.0F, p_149697_6_);
   }

   public void func_149690_a(World p_149690_1_, int p_149690_2_, int p_149690_3_, int p_149690_4_, int p_149690_5_, float p_149690_6_, int p_149690_7_) {
      if (!p_149690_1_.field_72995_K) {
         ArrayList items = this.getDrops(p_149690_1_, p_149690_2_, p_149690_3_, p_149690_4_, p_149690_5_, p_149690_7_);
         p_149690_6_ = ForgeEventFactory.fireBlockHarvesting(items, p_149690_1_, this, p_149690_2_, p_149690_3_, p_149690_4_, p_149690_5_, p_149690_7_, p_149690_6_, false, (EntityPlayer)this.harvesters.get());
         Iterator i$ = items.iterator();

         while(i$.hasNext()) {
            ItemStack item = (ItemStack)i$.next();
            if (p_149690_1_.field_73012_v.nextFloat() <= p_149690_6_) {
               this.func_149642_a(p_149690_1_, p_149690_2_, p_149690_3_, p_149690_4_, item);
            }
         }
      }

   }

   protected void func_149642_a(World p_149642_1_, int p_149642_2_, int p_149642_3_, int p_149642_4_, ItemStack p_149642_5_) {
      if (!p_149642_1_.field_72995_K && p_149642_1_.func_82736_K().func_82766_b("doTileDrops")) {
         if (((Boolean)this.captureDrops.get()).booleanValue()) {
            ((List)this.capturedDrops.get()).add(p_149642_5_);
            return;
         }

         float f = 0.7F;
         double d0 = (double)(p_149642_1_.field_73012_v.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
         double d1 = (double)(p_149642_1_.field_73012_v.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
         double d2 = (double)(p_149642_1_.field_73012_v.nextFloat() * f) + (double)(1.0F - f) * 0.5D;
         EntityItem entityitem = new EntityItem(p_149642_1_, (double)p_149642_2_ + d0, (double)p_149642_3_ + d1, (double)p_149642_4_ + d2, p_149642_5_);
         entityitem.field_145804_b = 10;
         p_149642_1_.func_72838_d(entityitem);
      }

   }

   public void func_149657_c(World p_149657_1_, int p_149657_2_, int p_149657_3_, int p_149657_4_, int p_149657_5_) {
      if (!p_149657_1_.field_72995_K) {
         while(p_149657_5_ > 0) {
            int i1 = EntityXPOrb.func_70527_a(p_149657_5_);
            p_149657_5_ -= i1;
            p_149657_1_.func_72838_d(new EntityXPOrb(p_149657_1_, (double)p_149657_2_ + 0.5D, (double)p_149657_3_ + 0.5D, (double)p_149657_4_ + 0.5D, i1));
         }
      }

   }

   public int func_149692_a(int p_149692_1_) {
      return 0;
   }

   public float func_149638_a(Entity p_149638_1_) {
      return this.field_149781_w / 5.0F;
   }

   public MovingObjectPosition func_149731_a(World p_149731_1_, int p_149731_2_, int p_149731_3_, int p_149731_4_, Vec3 p_149731_5_, Vec3 p_149731_6_) {
      this.func_149719_a(p_149731_1_, p_149731_2_, p_149731_3_, p_149731_4_);
      p_149731_5_ = p_149731_5_.func_72441_c((double)(-p_149731_2_), (double)(-p_149731_3_), (double)(-p_149731_4_));
      p_149731_6_ = p_149731_6_.func_72441_c((double)(-p_149731_2_), (double)(-p_149731_3_), (double)(-p_149731_4_));
      Vec3 vec32 = p_149731_5_.func_72429_b(p_149731_6_, this.field_149759_B);
      Vec3 vec33 = p_149731_5_.func_72429_b(p_149731_6_, this.field_149755_E);
      Vec3 vec34 = p_149731_5_.func_72435_c(p_149731_6_, this.field_149760_C);
      Vec3 vec35 = p_149731_5_.func_72435_c(p_149731_6_, this.field_149756_F);
      Vec3 vec36 = p_149731_5_.func_72434_d(p_149731_6_, this.field_149754_D);
      Vec3 vec37 = p_149731_5_.func_72434_d(p_149731_6_, this.field_149757_G);
      if (!this.func_149654_a(vec32)) {
         vec32 = null;
      }

      if (!this.func_149654_a(vec33)) {
         vec33 = null;
      }

      if (!this.func_149687_b(vec34)) {
         vec34 = null;
      }

      if (!this.func_149687_b(vec35)) {
         vec35 = null;
      }

      if (!this.func_149661_c(vec36)) {
         vec36 = null;
      }

      if (!this.func_149661_c(vec37)) {
         vec37 = null;
      }

      Vec3 vec38 = null;
      if (vec32 != null && (vec38 == null || p_149731_5_.func_72436_e(vec32) < p_149731_5_.func_72436_e(vec38))) {
         vec38 = vec32;
      }

      if (vec33 != null && (vec38 == null || p_149731_5_.func_72436_e(vec33) < p_149731_5_.func_72436_e(vec38))) {
         vec38 = vec33;
      }

      if (vec34 != null && (vec38 == null || p_149731_5_.func_72436_e(vec34) < p_149731_5_.func_72436_e(vec38))) {
         vec38 = vec34;
      }

      if (vec35 != null && (vec38 == null || p_149731_5_.func_72436_e(vec35) < p_149731_5_.func_72436_e(vec38))) {
         vec38 = vec35;
      }

      if (vec36 != null && (vec38 == null || p_149731_5_.func_72436_e(vec36) < p_149731_5_.func_72436_e(vec38))) {
         vec38 = vec36;
      }

      if (vec37 != null && (vec38 == null || p_149731_5_.func_72436_e(vec37) < p_149731_5_.func_72436_e(vec38))) {
         vec38 = vec37;
      }

      if (vec38 == null) {
         return null;
      } else {
         byte b0 = -1;
         if (vec38 == vec32) {
            b0 = 4;
         }

         if (vec38 == vec33) {
            b0 = 5;
         }

         if (vec38 == vec34) {
            b0 = 0;
         }

         if (vec38 == vec35) {
            b0 = 1;
         }

         if (vec38 == vec36) {
            b0 = 2;
         }

         if (vec38 == vec37) {
            b0 = 3;
         }

         return new MovingObjectPosition(p_149731_2_, p_149731_3_, p_149731_4_, b0, vec38.func_72441_c((double)p_149731_2_, (double)p_149731_3_, (double)p_149731_4_));
      }
   }

   private boolean func_149654_a(Vec3 p_149654_1_) {
      return p_149654_1_ == null ? false : p_149654_1_.field_72448_b >= this.field_149760_C && p_149654_1_.field_72448_b <= this.field_149756_F && p_149654_1_.field_72449_c >= this.field_149754_D && p_149654_1_.field_72449_c <= this.field_149757_G;
   }

   private boolean func_149687_b(Vec3 p_149687_1_) {
      return p_149687_1_ == null ? false : p_149687_1_.field_72450_a >= this.field_149759_B && p_149687_1_.field_72450_a <= this.field_149755_E && p_149687_1_.field_72449_c >= this.field_149754_D && p_149687_1_.field_72449_c <= this.field_149757_G;
   }

   private boolean func_149661_c(Vec3 p_149661_1_) {
      return p_149661_1_ == null ? false : p_149661_1_.field_72450_a >= this.field_149759_B && p_149661_1_.field_72450_a <= this.field_149755_E && p_149661_1_.field_72448_b >= this.field_149760_C && p_149661_1_.field_72448_b <= this.field_149756_F;
   }

   public void func_149723_a(World p_149723_1_, int p_149723_2_, int p_149723_3_, int p_149723_4_, Explosion p_149723_5_) {
   }

   public boolean func_149705_a(World p_149705_1_, int p_149705_2_, int p_149705_3_, int p_149705_4_, int p_149705_5_, ItemStack p_149705_6_) {
      return this.func_149707_d(p_149705_1_, p_149705_2_, p_149705_3_, p_149705_4_, p_149705_5_);
   }

   @SideOnly(Side.CLIENT)
   public int func_149701_w() {
      return 0;
   }

   public boolean func_149707_d(World p_149707_1_, int p_149707_2_, int p_149707_3_, int p_149707_4_, int p_149707_5_) {
      return this.func_149742_c(p_149707_1_, p_149707_2_, p_149707_3_, p_149707_4_);
   }

   public boolean func_149742_c(World p_149742_1_, int p_149742_2_, int p_149742_3_, int p_149742_4_) {
      return p_149742_1_.func_147439_a(p_149742_2_, p_149742_3_, p_149742_4_).isReplaceable(p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_);
   }

   public boolean func_149727_a(World p_149727_1_, int p_149727_2_, int p_149727_3_, int p_149727_4_, EntityPlayer p_149727_5_, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
      return false;
   }

   public void func_149724_b(World p_149724_1_, int p_149724_2_, int p_149724_3_, int p_149724_4_, Entity p_149724_5_) {
   }

   public int func_149660_a(World p_149660_1_, int p_149660_2_, int p_149660_3_, int p_149660_4_, int p_149660_5_, float p_149660_6_, float p_149660_7_, float p_149660_8_, int p_149660_9_) {
      return p_149660_9_;
   }

   public void func_149699_a(World p_149699_1_, int p_149699_2_, int p_149699_3_, int p_149699_4_, EntityPlayer p_149699_5_) {
   }

   public void func_149640_a(World p_149640_1_, int p_149640_2_, int p_149640_3_, int p_149640_4_, Entity p_149640_5_, Vec3 p_149640_6_) {
   }

   public void func_149719_a(IBlockAccess p_149719_1_, int p_149719_2_, int p_149719_3_, int p_149719_4_) {
   }

   public final double func_149704_x() {
      return this.field_149759_B;
   }

   public final double func_149753_y() {
      return this.field_149755_E;
   }

   public final double func_149665_z() {
      return this.field_149760_C;
   }

   public final double func_149669_A() {
      return this.field_149756_F;
   }

   public final double func_149706_B() {
      return this.field_149754_D;
   }

   public final double func_149693_C() {
      return this.field_149757_G;
   }

   @SideOnly(Side.CLIENT)
   public int func_149635_D() {
      return 16777215;
   }

   @SideOnly(Side.CLIENT)
   public int func_149741_i(int p_149741_1_) {
      return 16777215;
   }

   @SideOnly(Side.CLIENT)
   public int func_149720_d(IBlockAccess p_149720_1_, int p_149720_2_, int p_149720_3_, int p_149720_4_) {
      return 16777215;
   }

   public int func_149709_b(IBlockAccess p_149709_1_, int p_149709_2_, int p_149709_3_, int p_149709_4_, int p_149709_5_) {
      return 0;
   }

   public boolean func_149744_f() {
      return false;
   }

   public void func_149670_a(World p_149670_1_, int p_149670_2_, int p_149670_3_, int p_149670_4_, Entity p_149670_5_) {
   }

   public int func_149748_c(IBlockAccess p_149748_1_, int p_149748_2_, int p_149748_3_, int p_149748_4_, int p_149748_5_) {
      return 0;
   }

   public void func_149683_g() {
   }

   public void func_149636_a(World p_149636_1_, EntityPlayer p_149636_2_, int p_149636_3_, int p_149636_4_, int p_149636_5_, int p_149636_6_) {
      p_149636_2_.func_71064_a(StatList.field_75934_C[func_149682_b(this)], 1);
      p_149636_2_.func_71020_j(0.025F);
      if (this.canSilkHarvest(p_149636_1_, p_149636_2_, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_) && EnchantmentHelper.func_77502_d(p_149636_2_)) {
         ArrayList items = new ArrayList();
         ItemStack itemstack = this.func_149644_j(p_149636_6_);
         if (itemstack != null) {
            items.add(itemstack);
         }

         ForgeEventFactory.fireBlockHarvesting(items, p_149636_1_, this, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_, 0, 1.0F, true, p_149636_2_);
         Iterator i$ = items.iterator();

         while(i$.hasNext()) {
            ItemStack is = (ItemStack)i$.next();
            this.func_149642_a(p_149636_1_, p_149636_3_, p_149636_4_, p_149636_5_, is);
         }
      } else {
         this.harvesters.set(p_149636_2_);
         int i1 = EnchantmentHelper.func_77517_e(p_149636_2_);
         this.func_149697_b(p_149636_1_, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_, i1);
         this.harvesters.set((Object)null);
      }

   }

   protected boolean func_149700_E() {
      Integer meta = (Integer)this.silk_check_meta.get();
      return this.func_149686_d() && !this.hasTileEntity(meta == null ? 0 : meta.intValue());
   }

   protected ItemStack func_149644_j(int p_149644_1_) {
      int j = 0;
      Item item = Item.func_150898_a(this);
      if (item != null && item.func_77614_k()) {
         j = p_149644_1_;
      }

      return new ItemStack(item, 1, j);
   }

   public int func_149679_a(int p_149679_1_, Random p_149679_2_) {
      return this.func_149745_a(p_149679_2_);
   }

   public boolean func_149718_j(World p_149718_1_, int p_149718_2_, int p_149718_3_, int p_149718_4_) {
      return true;
   }

   public void func_149689_a(World p_149689_1_, int p_149689_2_, int p_149689_3_, int p_149689_4_, EntityLivingBase p_149689_5_, ItemStack p_149689_6_) {
   }

   public void func_149714_e(World p_149714_1_, int p_149714_2_, int p_149714_3_, int p_149714_4_, int p_149714_5_) {
   }

   public Block func_149663_c(String p_149663_1_) {
      this.field_149770_b = p_149663_1_;
      return this;
   }

   public String func_149732_F() {
      return StatCollector.func_74838_a(this.func_149739_a() + ".name");
   }

   public String func_149739_a() {
      return "tile." + this.field_149770_b;
   }

   public boolean func_149696_a(World p_149696_1_, int p_149696_2_, int p_149696_3_, int p_149696_4_, int p_149696_5_, int p_149696_6_) {
      return false;
   }

   public boolean func_149652_G() {
      return this.field_149790_y;
   }

   protected Block func_149649_H() {
      this.field_149790_y = false;
      return this;
   }

   public int func_149656_h() {
      return this.field_149764_J.func_76227_m();
   }

   @SideOnly(Side.CLIENT)
   public float func_149685_I() {
      return this.func_149637_q() ? 0.2F : 1.0F;
   }

   public void func_149746_a(World p_149746_1_, int p_149746_2_, int p_149746_3_, int p_149746_4_, Entity p_149746_5_, float p_149746_6_) {
   }

   @SideOnly(Side.CLIENT)
   public Item func_149694_d(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_) {
      return Item.func_150898_a(this);
   }

   public int func_149643_k(World p_149643_1_, int p_149643_2_, int p_149643_3_, int p_149643_4_) {
      return this.func_149692_a(p_149643_1_.func_72805_g(p_149643_2_, p_149643_3_, p_149643_4_));
   }

   @SideOnly(Side.CLIENT)
   public void func_149666_a(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
      p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
   }

   public Block func_149647_a(CreativeTabs p_149647_1_) {
      this.field_149772_a = p_149647_1_;
      return this;
   }

   public void func_149681_a(World p_149681_1_, int p_149681_2_, int p_149681_3_, int p_149681_4_, int p_149681_5_, EntityPlayer p_149681_6_) {
   }

   @SideOnly(Side.CLIENT)
   public CreativeTabs func_149708_J() {
      return this.field_149772_a;
   }

   public void func_149725_f(World p_149725_1_, int p_149725_2_, int p_149725_3_, int p_149725_4_, int p_149725_5_) {
   }

   public void func_149639_l(World p_149639_1_, int p_149639_2_, int p_149639_3_, int p_149639_4_) {
   }

   @SideOnly(Side.CLIENT)
   public boolean func_149648_K() {
      return false;
   }

   public boolean func_149698_L() {
      return true;
   }

   public boolean func_149659_a(Explosion p_149659_1_) {
      return true;
   }

   public boolean func_149667_c(Block p_149667_1_) {
      return this == p_149667_1_;
   }

   public static boolean func_149680_a(Block p_149680_0_, Block p_149680_1_) {
      return p_149680_0_ != null && p_149680_1_ != null ? (p_149680_0_ == p_149680_1_ ? true : p_149680_0_.func_149667_c(p_149680_1_)) : false;
   }

   public boolean func_149740_M() {
      return false;
   }

   public int func_149736_g(World p_149736_1_, int p_149736_2_, int p_149736_3_, int p_149736_4_, int p_149736_5_) {
      return 0;
   }

   public Block func_149658_d(String p_149658_1_) {
      this.field_149768_d = p_149658_1_;
      return this;
   }

   @SideOnly(Side.CLIENT)
   protected String func_149641_N() {
      return this.field_149768_d == null ? "MISSING_ICON_BLOCK_" + func_149682_b(this) + "_" + this.field_149770_b : this.field_149768_d;
   }

   @SideOnly(Side.CLIENT)
   public IIcon func_149735_b(int p_149735_1_, int p_149735_2_) {
      return this.func_149691_a(p_149735_1_, p_149735_2_);
   }

   @SideOnly(Side.CLIENT)
   public void func_149651_a(IIconRegister p_149651_1_) {
      this.field_149761_L = p_149651_1_.func_94245_a(this.func_149641_N());
   }

   @SideOnly(Side.CLIENT)
   public String func_149702_O() {
      return null;
   }

   public int getLightValue(IBlockAccess world, int x, int y, int z) {
      Block block = world.func_147439_a(x, y, z);
      return block != this ? block.getLightValue(world, x, y, z) : this.func_149750_m();
   }

   public boolean isLadder(IBlockAccess world, int x, int y, int z, EntityLivingBase entity) {
      return false;
   }

   public boolean isNormalCube(IBlockAccess world, int x, int y, int z) {
      return this.func_149688_o().func_76218_k() && this.func_149686_d() && !this.func_149744_f();
   }

   public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
      int meta = world.func_72805_g(x, y, z);
      if (this instanceof BlockSlab) {
         return (meta & 8) == 8 && side == ForgeDirection.UP || this.func_149730_j();
      } else if (this instanceof BlockFarmland) {
         return side != ForgeDirection.DOWN && side != ForgeDirection.UP;
      } else if (this instanceof BlockStairs) {
         boolean flipped = (meta & 4) != 0;
         return (meta & 3) + side.ordinal() == 5 || side == ForgeDirection.UP && flipped;
      } else if (this instanceof BlockSnow) {
         return (meta & 7) == 7;
      } else if (this instanceof BlockHopper && side == ForgeDirection.UP) {
         return true;
      } else {
         return this instanceof BlockCompressedPowered ? true : this.isNormalCube(world, x, y, z);
      }
   }

   public boolean isReplaceable(IBlockAccess world, int x, int y, int z) {
      return this.field_149764_J.func_76222_j();
   }

   public boolean isBurning(IBlockAccess world, int x, int y, int z) {
      return false;
   }

   public boolean isAir(IBlockAccess world, int x, int y, int z) {
      return this.func_149688_o() == Material.field_151579_a;
   }

   public boolean canHarvestBlock(EntityPlayer player, int meta) {
      return ForgeHooks.canHarvestBlock(this, player, meta);
   }

   public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
      return this.removedByPlayer(world, player, x, y, z);
   }

   /** @deprecated */
   @Deprecated
   public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
      return world.func_147468_f(x, y, z);
   }

   public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
      return Blocks.field_150480_ab.getFlammability(this);
   }

   public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
      return this.getFlammability(world, x, y, z, face) > 0;
   }

   public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
      return Blocks.field_150480_ab.getEncouragement(this);
   }

   public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
      if (this == Blocks.field_150424_aL && side == ForgeDirection.UP) {
         return true;
      } else {
         return world.field_73011_w instanceof WorldProviderEnd && this == Blocks.field_150357_h && side == ForgeDirection.UP;
      }
   }

   public boolean hasTileEntity(int metadata) {
      return this.isTileProvider;
   }

   public TileEntity createTileEntity(World world, int metadata) {
      return this.isTileProvider ? ((ITileEntityProvider)this).func_149915_a(world, metadata) : null;
   }

   public int quantityDropped(int meta, int fortune, Random random) {
      return this.func_149679_a(fortune, random);
   }

   public ArrayList getDrops(World world, int x, int y, int z, int metadata, int fortune) {
      ArrayList ret = new ArrayList();
      int count = this.quantityDropped(metadata, fortune, world.field_73012_v);

      for(int i = 0; i < count; ++i) {
         Item item = this.func_149650_a(metadata, world.field_73012_v, fortune);
         if (item != null) {
            ret.add(new ItemStack(item, 1, this.func_149692_a(metadata)));
         }
      }

      return ret;
   }

   public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
      this.silk_check_meta.set(metadata);
      boolean ret = this.func_149700_E();
      this.silk_check_meta.set((Object)null);
      return ret;
   }

   public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
      int meta = world.func_72805_g(x, y, z);
      if (!(this instanceof BlockSlab)) {
         if (this instanceof BlockStairs) {
            return (meta & 4) != 0;
         } else {
            return this.isSideSolid(world, x, y, z, ForgeDirection.UP);
         }
      } else {
         return (meta & 8) == 8 || this.func_149730_j();
      }
   }

   public boolean isBed(IBlockAccess world, int x, int y, int z, EntityLivingBase player) {
      return this == Blocks.field_150324_C;
   }

   public ChunkCoordinates getBedSpawnPosition(IBlockAccess world, int x, int y, int z, EntityPlayer player) {
      return world instanceof World ? BlockBed.func_149977_a((World)world, x, y, z, 0) : null;
   }

   public void setBedOccupied(IBlockAccess world, int x, int y, int z, EntityPlayer player, boolean occupied) {
      if (world instanceof World) {
         BlockBed.func_149979_a((World)world, x, y, z, occupied);
      }

   }

   public int getBedDirection(IBlockAccess world, int x, int y, int z) {
      return BlockBed.func_149895_l(world.func_72805_g(x, y, z));
   }

   public boolean isBedFoot(IBlockAccess world, int x, int y, int z) {
      return BlockBed.func_149975_b(world.func_72805_g(x, y, z));
   }

   public void beginLeavesDecay(World world, int x, int y, int z) {
   }

   public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
      return false;
   }

   public boolean isLeaves(IBlockAccess world, int x, int y, int z) {
      return this.func_149688_o() == Material.field_151584_j;
   }

   public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z) {
      return !this.func_149730_j();
   }

   public boolean isWood(IBlockAccess world, int x, int y, int z) {
      return false;
   }

   public boolean isReplaceableOreGen(World world, int x, int y, int z, Block target) {
      return this == target;
   }

   public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
      return this.func_149638_a(par1Entity);
   }

   public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
      world.func_147468_f(x, y, z);
      this.func_149723_a(world, x, y, z, explosion);
   }

   public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
      return this.func_149744_f() && side != -1;
   }

   public boolean canPlaceTorchOnTop(World world, int x, int y, int z) {
      if (this.isSideSolid(world, x, y, z, ForgeDirection.UP)) {
         return true;
      } else {
         return this == Blocks.field_150422_aJ || this == Blocks.field_150386_bk || this == Blocks.field_150359_w || this == Blocks.field_150463_bK;
      }
   }

   public boolean canRenderInPass(int pass) {
      return pass == this.func_149701_w();
   }

   public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
      Item item = this.func_149694_d(world, x, y, z);
      if (item == null) {
         return null;
      } else {
         Block block = item instanceof ItemBlock && !this.func_149648_K() ? func_149634_a(item) : this;
         return new ItemStack(item, 1, block.func_149643_k(world, x, y, z));
      }
   }

   public boolean isFoliage(IBlockAccess world, int x, int y, int z) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
      return false;
   }

   public boolean canSustainPlant(IBlockAccess world, int x, int y, int z, ForgeDirection direction, IPlantable plantable) {
      Block plant = plantable.getPlant(world, x, y + 1, z);
      EnumPlantType plantType = plantable.getPlantType(world, x, y + 1, z);
      if (plant == Blocks.field_150434_aF && this == Blocks.field_150434_aF) {
         return true;
      } else if (plant == Blocks.field_150436_aH && this == Blocks.field_150436_aH) {
         return true;
      } else if (plantable instanceof BlockBush && ((BlockBush)plantable).func_149854_a(this)) {
         return true;
      } else {
         switch(plantType) {
         case Desert:
            return this == Blocks.field_150354_m;
         case Nether:
            return this == Blocks.field_150425_aM;
         case Crop:
            return this == Blocks.field_150458_ak;
         case Cave:
            return this.isSideSolid(world, x, y, z, ForgeDirection.UP);
         case Plains:
            return this == Blocks.field_150349_c || this == Blocks.field_150346_d || this == Blocks.field_150458_ak;
         case Water:
            return world.func_147439_a(x, y, z).func_149688_o() == Material.field_151586_h && world.func_72805_g(x, y, z) == 0;
         case Beach:
            boolean isBeach = this == Blocks.field_150349_c || this == Blocks.field_150346_d || this == Blocks.field_150354_m;
            boolean hasWater = world.func_147439_a(x - 1, y, z).func_149688_o() == Material.field_151586_h || world.func_147439_a(x + 1, y, z).func_149688_o() == Material.field_151586_h || world.func_147439_a(x, y, z - 1).func_149688_o() == Material.field_151586_h || world.func_147439_a(x, y, z + 1).func_149688_o() == Material.field_151586_h;
            return isBeach && hasWater;
         default:
            return false;
         }
      }
   }

   public void onPlantGrow(World world, int x, int y, int z, int sourceX, int sourceY, int sourceZ) {
      if (this == Blocks.field_150349_c || this == Blocks.field_150458_ak) {
         world.func_147465_d(x, y, z, Blocks.field_150346_d, 0, 2);
      }

   }

   public boolean isFertile(World world, int x, int y, int z) {
      if (this == Blocks.field_150458_ak) {
         return world.func_72805_g(x, y, z) > 0;
      } else {
         return false;
      }
   }

   public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
      return this.func_149717_k();
   }

   public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
      if (entity instanceof EntityWither) {
         return this != Blocks.field_150357_h && this != Blocks.field_150384_bq && this != Blocks.field_150378_br && this != Blocks.field_150483_bI;
      } else if (!(entity instanceof EntityDragon)) {
         return true;
      } else {
         return this != Blocks.field_150343_Z && this != Blocks.field_150377_bs && this != Blocks.field_150357_h;
      }
   }

   public boolean isBeaconBase(IBlockAccess worldObj, int x, int y, int z, int beaconX, int beaconY, int beaconZ) {
      return this == Blocks.field_150475_bE || this == Blocks.field_150340_R || this == Blocks.field_150484_ah || this == Blocks.field_150339_S;
   }

   public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis) {
      return RotationHelper.rotateVanillaBlock(this, worldObj, x, y, z, axis);
   }

   public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z) {
      return RotationHelper.getValidVanillaBlockRotations(this);
   }

   public float getEnchantPowerBonus(World world, int x, int y, int z) {
      return this == Blocks.field_150342_X ? 1.0F : 0.0F;
   }

   public boolean recolourBlock(World world, int x, int y, int z, ForgeDirection side, int colour) {
      if (this == Blocks.field_150325_L) {
         int meta = world.func_72805_g(x, y, z);
         if (meta != colour) {
            world.func_72921_c(x, y, z, colour, 3);
            return true;
         }
      }

      return false;
   }

   public int getExpDrop(IBlockAccess world, int metadata, int fortune) {
      return 0;
   }

   public void onNeighborChange(IBlockAccess world, int x, int y, int z, int tileX, int tileY, int tileZ) {
   }

   public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
      return this.func_149721_r();
   }

   public boolean getWeakChanges(IBlockAccess world, int x, int y, int z) {
      return false;
   }

   public void setHarvestLevel(String toolClass, int level) {
      for(int m = 0; m < 16; ++m) {
         this.setHarvestLevel(toolClass, level, m);
      }

   }

   public void setHarvestLevel(String toolClass, int level, int metadata) {
      this.harvestTool[metadata] = toolClass;
      this.harvestLevel[metadata] = level;
   }

   public String getHarvestTool(int metadata) {
      return this.harvestTool[metadata];
   }

   public int getHarvestLevel(int metadata) {
      return this.harvestLevel[metadata];
   }

   public boolean isToolEffective(String type, int metadata) {
      if (!"pickaxe".equals(type) || this != Blocks.field_150450_ax && this != Blocks.field_150439_ay && this != Blocks.field_150343_Z) {
         return this.harvestTool[metadata] == null ? false : this.harvestTool[metadata].equals(type);
      } else {
         return false;
      }
   }

   protected List captureDrops(boolean start) {
      if (start) {
         this.captureDrops.set(true);
         ((List)this.capturedDrops.get()).clear();
         return null;
      } else {
         this.captureDrops.set(false);
         return (List)this.capturedDrops.get();
      }
   }

   // $FF: synthetic class
   static class SyntheticClass_1 {
      // $FF: synthetic field
      static final int[] $SwitchMap$net$minecraftforge$common$EnumPlantType = new int[EnumPlantType.values().length];

      static {
         try {
            $SwitchMap$net$minecraftforge$common$EnumPlantType[EnumPlantType.Desert.ordinal()] = 1;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            $SwitchMap$net$minecraftforge$common$EnumPlantType[EnumPlantType.Nether.ordinal()] = 2;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            $SwitchMap$net$minecraftforge$common$EnumPlantType[EnumPlantType.Crop.ordinal()] = 3;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            $SwitchMap$net$minecraftforge$common$EnumPlantType[EnumPlantType.Cave.ordinal()] = 4;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            $SwitchMap$net$minecraftforge$common$EnumPlantType[EnumPlantType.Plains.ordinal()] = 5;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            $SwitchMap$net$minecraftforge$common$EnumPlantType[EnumPlantType.Water.ordinal()] = 6;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            $SwitchMap$net$minecraftforge$common$EnumPlantType[EnumPlantType.Beach.ordinal()] = 7;
         } catch (NoSuchFieldError var1) {
            ;
         }

      }
   }

   public static class SoundType {
      public final String field_150501_a;
      public final float field_150499_b;
      public final float field_150500_c;
      private static final String __OBFID = "CL_00000203";

      public SoundType(String p_i45393_1_, float p_i45393_2_, float p_i45393_3_) {
         this.field_150501_a = p_i45393_1_;
         this.field_150499_b = p_i45393_2_;
         this.field_150500_c = p_i45393_3_;
      }

      public float func_150497_c() {
         return this.field_150499_b;
      }

      public float func_150494_d() {
         return this.field_150500_c;
      }

      public String func_150495_a() {
         return "dig." + this.field_150501_a;
      }

      public String func_150498_e() {
         return "step." + this.field_150501_a;
      }

      public String func_150496_b() {
         return this.func_150495_a();
      }
   }
}
