package net.minecraft.block;

import net.minecraft.creativetab.*;
import cpw.mods.fml.relauncher.*;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.block.material.*;
import clickme.nocubes.*;
import java.util.*;
import net.minecraftforge.event.*;
import net.minecraft.entity.item.*;
import net.minecraft.stats.*;
import net.minecraft.enchantment.*;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.world.*;
import net.minecraft.tileentity.*;
import net.minecraft.entity.*;
import net.minecraft.util.*;
import net.minecraft.item.*;
import net.minecraft.client.particle.*;
import net.minecraftforge.common.*;
import net.minecraft.entity.boss.*;
import net.minecraftforge.common.util.*;
import cpw.mods.fml.common.registry.*;

public class Block
{
    public static final RegistryNamespaced field_149771_c;
    private CreativeTabs field_149772_a;
    protected String field_149768_d;
    public static final SoundType field_149769_e;
    public static final SoundType field_149766_f;
    public static final SoundType field_149767_g;
    public static final SoundType field_149779_h;
    public static final SoundType field_149780_i;
    public static final SoundType field_149777_j;
    public static final SoundType field_149778_k;
    public static final SoundType field_149775_l;
    public static final SoundType field_149776_m;
    public static final SoundType field_149773_n;
    public static final SoundType field_149774_o;
    public static final SoundType field_149788_p;
    protected boolean field_149787_q;
    protected int field_149786_r;
    protected boolean field_149785_s;
    protected int field_149784_t;
    protected boolean field_149783_u;
    protected float field_149782_v;
    protected float field_149781_w;
    protected boolean field_149791_x;
    protected boolean field_149790_y;
    protected boolean field_149789_z;
    protected boolean field_149758_A;
    protected double field_149759_B;
    protected double field_149760_C;
    protected double field_149754_D;
    protected double field_149755_E;
    protected double field_149756_F;
    protected double field_149757_G;
    public SoundType field_149762_H;
    public float field_149763_I;
    protected final Material field_149764_J;
    public float field_149765_K;
    private String field_149770_b;
    @SideOnly(Side.CLIENT)
    protected IIcon field_149761_L;
    private static final String __OBFID = "CL_00000199";
    protected ThreadLocal<EntityPlayer> harvesters;
    private ThreadLocal<Integer> silk_check_meta;
    private boolean isTileProvider;
    private String[] harvestTool;
    private int[] harvestLevel;
    protected ThreadLocal<Boolean> captureDrops;
    protected ThreadLocal<List<ItemStack>> capturedDrops;
    
    public static int func_149682_b(final Block p_149682_0_) {
        return Block.field_149771_c.func_148757_b((Object)p_149682_0_);
    }
    
    public static Block func_149729_e(final int p_149729_0_) {
        final Block ret = (Block)Block.field_149771_c.func_148754_a(p_149729_0_);
        return (ret == null) ? Blocks.field_150350_a : ret;
    }
    
    public static Block func_149634_a(final Item p_149634_0_) {
        return func_149729_e(Item.func_150891_b(p_149634_0_));
    }
    
    public static Block func_149684_b(final String p_149684_0_) {
        if (Block.field_149771_c.func_148741_d(p_149684_0_)) {
            return (Block)Block.field_149771_c.func_82594_a(p_149684_0_);
        }
        try {
            return (Block)Block.field_149771_c.func_148754_a(Integer.parseInt(p_149684_0_));
        }
        catch (NumberFormatException numberformatexception) {
            return null;
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
    
    public MapColor func_149728_f(final int p_149728_1_) {
        return this.func_149688_o().func_151565_r();
    }
    
    public static void func_149671_p() {
        Block.field_149771_c.func_148756_a(0, "air", (Object)new BlockAir().func_149663_c("air"));
        Block.field_149771_c.func_148756_a(1, "stone", (Object)new BlockStone().func_149711_c(1.5f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("stone").func_149658_d("stone"));
        Block.field_149771_c.func_148756_a(2, "grass", (Object)new BlockGrass().func_149711_c(0.6f).func_149672_a(Block.field_149779_h).func_149663_c("grass").func_149658_d("grass"));
        Block.field_149771_c.func_148756_a(3, "dirt", (Object)new BlockDirt().func_149711_c(0.5f).func_149672_a(Block.field_149767_g).func_149663_c("dirt").func_149658_d("dirt"));
        final Block block = new Block(Material.field_151576_e).func_149711_c(2.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("stonebrick").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("cobblestone");
        Block.field_149771_c.func_148756_a(4, "cobblestone", (Object)block);
        final Block block2 = new BlockWood().func_149711_c(2.0f).func_149752_b(5.0f).func_149672_a(Block.field_149766_f).func_149663_c("wood").func_149658_d("planks");
        Block.field_149771_c.func_148756_a(5, "planks", (Object)block2);
        Block.field_149771_c.func_148756_a(6, "sapling", (Object)new BlockSapling().func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("sapling").func_149658_d("sapling"));
        Block.field_149771_c.func_148756_a(7, "bedrock", (Object)new Block(Material.field_151576_e).func_149722_s().func_149752_b(6000000.0f).func_149672_a(Block.field_149780_i).func_149663_c("bedrock").func_149649_H().func_149647_a(CreativeTabs.field_78030_b).func_149658_d("bedrock"));
        Block.field_149771_c.func_148756_a(8, "flowing_water", (Object)new BlockDynamicLiquid(Material.field_151586_h).func_149711_c(100.0f).func_149713_g(3).func_149663_c("water").func_149649_H().func_149658_d("water_flow"));
        Block.field_149771_c.func_148756_a(9, "water", (Object)new BlockStaticLiquid(Material.field_151586_h).func_149711_c(100.0f).func_149713_g(3).func_149663_c("water").func_149649_H().func_149658_d("water_still"));
        Block.field_149771_c.func_148756_a(10, "flowing_lava", (Object)new BlockDynamicLiquid(Material.field_151587_i).func_149711_c(100.0f).func_149715_a(1.0f).func_149663_c("lava").func_149649_H().func_149658_d("lava_flow"));
        Block.field_149771_c.func_148756_a(11, "lava", (Object)new BlockStaticLiquid(Material.field_151587_i).func_149711_c(100.0f).func_149715_a(1.0f).func_149663_c("lava").func_149649_H().func_149658_d("lava_still"));
        Block.field_149771_c.func_148756_a(12, "sand", (Object)new BlockSand().func_149711_c(0.5f).func_149672_a(Block.field_149776_m).func_149663_c("sand").func_149658_d("sand"));
        Block.field_149771_c.func_148756_a(13, "gravel", (Object)new BlockGravel().func_149711_c(0.6f).func_149672_a(Block.field_149767_g).func_149663_c("gravel").func_149658_d("gravel"));
        Block.field_149771_c.func_148756_a(14, "gold_ore", (Object)new BlockOre().func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("oreGold").func_149658_d("gold_ore"));
        Block.field_149771_c.func_148756_a(15, "iron_ore", (Object)new BlockOre().func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("oreIron").func_149658_d("iron_ore"));
        Block.field_149771_c.func_148756_a(16, "coal_ore", (Object)new BlockOre().func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("oreCoal").func_149658_d("coal_ore"));
        Block.field_149771_c.func_148756_a(17, "log", (Object)new BlockOldLog().func_149663_c("log").func_149658_d("log"));
        Block.field_149771_c.func_148756_a(18, "leaves", (Object)new BlockOldLeaf().func_149663_c("leaves").func_149658_d("leaves"));
        Block.field_149771_c.func_148756_a(19, "sponge", (Object)new BlockSponge().func_149711_c(0.6f).func_149672_a(Block.field_149779_h).func_149663_c("sponge").func_149658_d("sponge"));
        Block.field_149771_c.func_148756_a(20, "glass", (Object)new BlockGlass(Material.field_151592_s, false).func_149711_c(0.3f).func_149672_a(Block.field_149778_k).func_149663_c("glass").func_149658_d("glass"));
        Block.field_149771_c.func_148756_a(21, "lapis_ore", (Object)new BlockOre().func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("oreLapis").func_149658_d("lapis_ore"));
        Block.field_149771_c.func_148756_a(22, "lapis_block", (Object)new BlockCompressed(MapColor.field_151652_H).func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("blockLapis").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("lapis_block"));
        Block.field_149771_c.func_148756_a(23, "dispenser", (Object)new BlockDispenser().func_149711_c(3.5f).func_149672_a(Block.field_149780_i).func_149663_c("dispenser").func_149658_d("dispenser"));
        final Block block3 = new BlockSandStone().func_149672_a(Block.field_149780_i).func_149711_c(0.8f).func_149663_c("sandStone").func_149658_d("sandstone");
        Block.field_149771_c.func_148756_a(24, "sandstone", (Object)block3);
        Block.field_149771_c.func_148756_a(25, "noteblock", (Object)new BlockNote().func_149711_c(0.8f).func_149663_c("musicBlock").func_149658_d("noteblock"));
        Block.field_149771_c.func_148756_a(26, "bed", (Object)new BlockBed().func_149711_c(0.2f).func_149663_c("bed").func_149649_H().func_149658_d("bed"));
        Block.field_149771_c.func_148756_a(27, "golden_rail", (Object)new BlockRailPowered().func_149711_c(0.7f).func_149672_a(Block.field_149777_j).func_149663_c("goldenRail").func_149658_d("rail_golden"));
        Block.field_149771_c.func_148756_a(28, "detector_rail", (Object)new BlockRailDetector().func_149711_c(0.7f).func_149672_a(Block.field_149777_j).func_149663_c("detectorRail").func_149658_d("rail_detector"));
        Block.field_149771_c.func_148756_a(29, "sticky_piston", (Object)new BlockPistonBase(true).func_149663_c("pistonStickyBase"));
        Block.field_149771_c.func_148756_a(30, "web", (Object)new BlockWeb().func_149713_g(1).func_149711_c(4.0f).func_149663_c("web").func_149658_d("web"));
        Block.field_149771_c.func_148756_a(31, "tallgrass", (Object)new BlockTallGrass().func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("tallgrass"));
        Block.field_149771_c.func_148756_a(32, "deadbush", (Object)new BlockDeadBush().func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("deadbush").func_149658_d("deadbush"));
        Block.field_149771_c.func_148756_a(33, "piston", (Object)new BlockPistonBase(false).func_149663_c("pistonBase"));
        Block.field_149771_c.func_148756_a(34, "piston_head", (Object)new BlockPistonExtension());
        Block.field_149771_c.func_148756_a(35, "wool", (Object)new BlockColored(Material.field_151580_n).func_149711_c(0.8f).func_149672_a(Block.field_149775_l).func_149663_c("cloth").func_149658_d("wool_colored"));
        Block.field_149771_c.func_148756_a(36, "piston_extension", (Object)new BlockPistonMoving());
        Block.field_149771_c.func_148756_a(37, "yellow_flower", (Object)new BlockFlower(0).func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("flower1").func_149658_d("flower_dandelion"));
        Block.field_149771_c.func_148756_a(38, "red_flower", (Object)new BlockFlower(1).func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("flower2").func_149658_d("flower_rose"));
        Block.field_149771_c.func_148756_a(39, "brown_mushroom", (Object)new BlockMushroom().func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149715_a(0.125f).func_149663_c("mushroom").func_149658_d("mushroom_brown"));
        Block.field_149771_c.func_148756_a(40, "red_mushroom", (Object)new BlockMushroom().func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("mushroom").func_149658_d("mushroom_red"));
        Block.field_149771_c.func_148756_a(41, "gold_block", (Object)new BlockCompressed(MapColor.field_151647_F).func_149711_c(3.0f).func_149752_b(10.0f).func_149672_a(Block.field_149777_j).func_149663_c("blockGold").func_149658_d("gold_block"));
        Block.field_149771_c.func_148756_a(42, "iron_block", (Object)new BlockCompressed(MapColor.field_151668_h).func_149711_c(5.0f).func_149752_b(10.0f).func_149672_a(Block.field_149777_j).func_149663_c("blockIron").func_149658_d("iron_block"));
        Block.field_149771_c.func_148756_a(43, "double_stone_slab", (Object)new BlockStoneSlab(true).func_149711_c(2.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("stoneSlab"));
        Block.field_149771_c.func_148756_a(44, "stone_slab", (Object)new BlockStoneSlab(false).func_149711_c(2.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("stoneSlab"));
        final Block block4 = new Block(Material.field_151576_e).func_149711_c(2.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("brick").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("brick");
        Block.field_149771_c.func_148756_a(45, "brick_block", (Object)block4);
        Block.field_149771_c.func_148756_a(46, "tnt", (Object)new BlockTNT().func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("tnt").func_149658_d("tnt"));
        Block.field_149771_c.func_148756_a(47, "bookshelf", (Object)new BlockBookshelf().func_149711_c(1.5f).func_149672_a(Block.field_149766_f).func_149663_c("bookshelf").func_149658_d("bookshelf"));
        Block.field_149771_c.func_148756_a(48, "mossy_cobblestone", (Object)new Block(Material.field_151576_e).func_149711_c(2.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("stoneMoss").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("cobblestone_mossy"));
        Block.field_149771_c.func_148756_a(49, "obsidian", (Object)new BlockObsidian().func_149711_c(50.0f).func_149752_b(2000.0f).func_149672_a(Block.field_149780_i).func_149663_c("obsidian").func_149658_d("obsidian"));
        Block.field_149771_c.func_148756_a(50, "torch", (Object)new BlockTorch().func_149711_c(0.0f).func_149715_a(0.9375f).func_149672_a(Block.field_149766_f).func_149663_c("torch").func_149658_d("torch_on"));
        Block.field_149771_c.func_148756_a(51, "fire", (Object)new BlockFire().func_149711_c(0.0f).func_149715_a(1.0f).func_149672_a(Block.field_149766_f).func_149663_c("fire").func_149649_H().func_149658_d("fire"));
        Block.field_149771_c.func_148756_a(52, "mob_spawner", (Object)new BlockMobSpawner().func_149711_c(5.0f).func_149672_a(Block.field_149777_j).func_149663_c("mobSpawner").func_149649_H().func_149658_d("mob_spawner"));
        Block.field_149771_c.func_148756_a(53, "oak_stairs", (Object)new BlockStairs(block2, 0).func_149663_c("stairsWood"));
        Block.field_149771_c.func_148756_a(54, "chest", (Object)new BlockChest(0).func_149711_c(2.5f).func_149672_a(Block.field_149766_f).func_149663_c("chest"));
        Block.field_149771_c.func_148756_a(55, "redstone_wire", (Object)new BlockRedstoneWire().func_149711_c(0.0f).func_149672_a(Block.field_149769_e).func_149663_c("redstoneDust").func_149649_H().func_149658_d("redstone_dust"));
        Block.field_149771_c.func_148756_a(56, "diamond_ore", (Object)new BlockOre().func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("oreDiamond").func_149658_d("diamond_ore"));
        Block.field_149771_c.func_148756_a(57, "diamond_block", (Object)new BlockCompressed(MapColor.field_151648_G).func_149711_c(5.0f).func_149752_b(10.0f).func_149672_a(Block.field_149777_j).func_149663_c("blockDiamond").func_149658_d("diamond_block"));
        Block.field_149771_c.func_148756_a(58, "crafting_table", (Object)new BlockWorkbench().func_149711_c(2.5f).func_149672_a(Block.field_149766_f).func_149663_c("workbench").func_149658_d("crafting_table"));
        Block.field_149771_c.func_148756_a(59, "wheat", (Object)new BlockCrops().func_149663_c("crops").func_149658_d("wheat"));
        final Block block5 = new BlockFarmland().func_149711_c(0.6f).func_149672_a(Block.field_149767_g).func_149663_c("farmland").func_149658_d("farmland");
        Block.field_149771_c.func_148756_a(60, "farmland", (Object)block5);
        Block.field_149771_c.func_148756_a(61, "furnace", (Object)new BlockFurnace(false).func_149711_c(3.5f).func_149672_a(Block.field_149780_i).func_149663_c("furnace").func_149647_a(CreativeTabs.field_78031_c));
        Block.field_149771_c.func_148756_a(62, "lit_furnace", (Object)new BlockFurnace(true).func_149711_c(3.5f).func_149672_a(Block.field_149780_i).func_149715_a(0.875f).func_149663_c("furnace"));
        Block.field_149771_c.func_148756_a(63, "standing_sign", (Object)new BlockSign((Class)TileEntitySign.class, true).func_149711_c(1.0f).func_149672_a(Block.field_149766_f).func_149663_c("sign").func_149649_H());
        Block.field_149771_c.func_148756_a(64, "wooden_door", (Object)new BlockDoor(Material.field_151575_d).func_149711_c(3.0f).func_149672_a(Block.field_149766_f).func_149663_c("doorWood").func_149649_H().func_149658_d("door_wood"));
        Block.field_149771_c.func_148756_a(65, "ladder", (Object)new BlockLadder().func_149711_c(0.4f).func_149672_a(Block.field_149774_o).func_149663_c("ladder").func_149658_d("ladder"));
        Block.field_149771_c.func_148756_a(66, "rail", (Object)new BlockRail().func_149711_c(0.7f).func_149672_a(Block.field_149777_j).func_149663_c("rail").func_149658_d("rail_normal"));
        Block.field_149771_c.func_148756_a(67, "stone_stairs", (Object)new BlockStairs(block, 0).func_149663_c("stairsStone"));
        Block.field_149771_c.func_148756_a(68, "wall_sign", (Object)new BlockSign((Class)TileEntitySign.class, false).func_149711_c(1.0f).func_149672_a(Block.field_149766_f).func_149663_c("sign").func_149649_H());
        Block.field_149771_c.func_148756_a(69, "lever", (Object)new BlockLever().func_149711_c(0.5f).func_149672_a(Block.field_149766_f).func_149663_c("lever").func_149658_d("lever"));
        Block.field_149771_c.func_148756_a(70, "stone_pressure_plate", (Object)new BlockPressurePlate("stone", Material.field_151576_e, BlockPressurePlate.Sensitivity.mobs).func_149711_c(0.5f).func_149672_a(Block.field_149780_i).func_149663_c("pressurePlate"));
        Block.field_149771_c.func_148756_a(71, "iron_door", (Object)new BlockDoor(Material.field_151573_f).func_149711_c(5.0f).func_149672_a(Block.field_149777_j).func_149663_c("doorIron").func_149649_H().func_149658_d("door_iron"));
        Block.field_149771_c.func_148756_a(72, "wooden_pressure_plate", (Object)new BlockPressurePlate("planks_oak", Material.field_151575_d, BlockPressurePlate.Sensitivity.everything).func_149711_c(0.5f).func_149672_a(Block.field_149766_f).func_149663_c("pressurePlate"));
        Block.field_149771_c.func_148756_a(73, "redstone_ore", (Object)new BlockRedstoneOre(false).func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("oreRedstone").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("redstone_ore"));
        Block.field_149771_c.func_148756_a(74, "lit_redstone_ore", (Object)new BlockRedstoneOre(true).func_149715_a(0.625f).func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("oreRedstone").func_149658_d("redstone_ore"));
        Block.field_149771_c.func_148756_a(75, "unlit_redstone_torch", (Object)new BlockRedstoneTorch(false).func_149711_c(0.0f).func_149672_a(Block.field_149766_f).func_149663_c("notGate").func_149658_d("redstone_torch_off"));
        Block.field_149771_c.func_148756_a(76, "redstone_torch", (Object)new BlockRedstoneTorch(true).func_149711_c(0.0f).func_149715_a(0.5f).func_149672_a(Block.field_149766_f).func_149663_c("notGate").func_149647_a(CreativeTabs.field_78028_d).func_149658_d("redstone_torch_on"));
        Block.field_149771_c.func_148756_a(77, "stone_button", (Object)new BlockButtonStone().func_149711_c(0.5f).func_149672_a(Block.field_149780_i).func_149663_c("button"));
        Block.field_149771_c.func_148756_a(78, "snow_layer", (Object)new BlockSnow().func_149711_c(0.1f).func_149672_a(Block.field_149773_n).func_149663_c("snow").func_149713_g(0).func_149658_d("snow"));
        Block.field_149771_c.func_148756_a(79, "ice", (Object)new BlockIce().func_149711_c(0.5f).func_149713_g(3).func_149672_a(Block.field_149778_k).func_149663_c("ice").func_149658_d("ice"));
        Block.field_149771_c.func_148756_a(80, "snow", (Object)new BlockSnowBlock().func_149711_c(0.2f).func_149672_a(Block.field_149773_n).func_149663_c("snow").func_149658_d("snow"));
        Block.field_149771_c.func_148756_a(81, "cactus", (Object)new BlockCactus().func_149711_c(0.4f).func_149672_a(Block.field_149775_l).func_149663_c("cactus").func_149658_d("cactus"));
        Block.field_149771_c.func_148756_a(82, "clay", (Object)new BlockClay().func_149711_c(0.6f).func_149672_a(Block.field_149767_g).func_149663_c("clay").func_149658_d("clay"));
        Block.field_149771_c.func_148756_a(83, "reeds", (Object)new BlockReed().func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("reeds").func_149649_H().func_149658_d("reeds"));
        Block.field_149771_c.func_148756_a(84, "jukebox", (Object)new BlockJukebox().func_149711_c(2.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("jukebox").func_149658_d("jukebox"));
        Block.field_149771_c.func_148756_a(85, "fence", (Object)new BlockFence("planks_oak", Material.field_151575_d).func_149711_c(2.0f).func_149752_b(5.0f).func_149672_a(Block.field_149766_f).func_149663_c("fence"));
        final Block block6 = new BlockPumpkin(false).func_149711_c(1.0f).func_149672_a(Block.field_149766_f).func_149663_c("pumpkin").func_149658_d("pumpkin");
        Block.field_149771_c.func_148756_a(86, "pumpkin", (Object)block6);
        Block.field_149771_c.func_148756_a(87, "netherrack", (Object)new BlockNetherrack().func_149711_c(0.4f).func_149672_a(Block.field_149780_i).func_149663_c("hellrock").func_149658_d("netherrack"));
        Block.field_149771_c.func_148756_a(88, "soul_sand", (Object)new BlockSoulSand().func_149711_c(0.5f).func_149672_a(Block.field_149776_m).func_149663_c("hellsand").func_149658_d("soul_sand"));
        Block.field_149771_c.func_148756_a(89, "glowstone", (Object)new BlockGlowstone(Material.field_151592_s).func_149711_c(0.3f).func_149672_a(Block.field_149778_k).func_149715_a(1.0f).func_149663_c("lightgem").func_149658_d("glowstone"));
        Block.field_149771_c.func_148756_a(90, "portal", (Object)new BlockPortal().func_149711_c(-1.0f).func_149672_a(Block.field_149778_k).func_149715_a(0.75f).func_149663_c("portal").func_149658_d("portal"));
        Block.field_149771_c.func_148756_a(91, "lit_pumpkin", (Object)new BlockPumpkin(true).func_149711_c(1.0f).func_149672_a(Block.field_149766_f).func_149715_a(1.0f).func_149663_c("litpumpkin").func_149658_d("pumpkin"));
        Block.field_149771_c.func_148756_a(92, "cake", (Object)new BlockCake().func_149711_c(0.5f).func_149672_a(Block.field_149775_l).func_149663_c("cake").func_149649_H().func_149658_d("cake"));
        Block.field_149771_c.func_148756_a(93, "unpowered_repeater", (Object)new BlockRedstoneRepeater(false).func_149711_c(0.0f).func_149672_a(Block.field_149766_f).func_149663_c("diode").func_149649_H().func_149658_d("repeater_off"));
        Block.field_149771_c.func_148756_a(94, "powered_repeater", (Object)new BlockRedstoneRepeater(true).func_149711_c(0.0f).func_149715_a(0.625f).func_149672_a(Block.field_149766_f).func_149663_c("diode").func_149649_H().func_149658_d("repeater_on"));
        Block.field_149771_c.func_148756_a(95, "stained_glass", (Object)new BlockStainedGlass(Material.field_151592_s).func_149711_c(0.3f).func_149672_a(Block.field_149778_k).func_149663_c("stainedGlass").func_149658_d("glass"));
        Block.field_149771_c.func_148756_a(96, "trapdoor", (Object)new BlockTrapDoor(Material.field_151575_d).func_149711_c(3.0f).func_149672_a(Block.field_149766_f).func_149663_c("trapdoor").func_149649_H().func_149658_d("trapdoor"));
        Block.field_149771_c.func_148756_a(97, "monster_egg", (Object)new BlockSilverfish().func_149711_c(0.75f).func_149663_c("monsterStoneEgg"));
        final Block block7 = new BlockStoneBrick().func_149711_c(1.5f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("stonebricksmooth").func_149658_d("stonebrick");
        Block.field_149771_c.func_148756_a(98, "stonebrick", (Object)block7);
        Block.field_149771_c.func_148756_a(99, "brown_mushroom_block", (Object)new BlockHugeMushroom(Material.field_151575_d, 0).func_149711_c(0.2f).func_149672_a(Block.field_149766_f).func_149663_c("mushroom").func_149658_d("mushroom_block"));
        Block.field_149771_c.func_148756_a(100, "red_mushroom_block", (Object)new BlockHugeMushroom(Material.field_151575_d, 1).func_149711_c(0.2f).func_149672_a(Block.field_149766_f).func_149663_c("mushroom").func_149658_d("mushroom_block"));
        Block.field_149771_c.func_148756_a(101, "iron_bars", (Object)new BlockPane("iron_bars", "iron_bars", Material.field_151573_f, true).func_149711_c(5.0f).func_149752_b(10.0f).func_149672_a(Block.field_149777_j).func_149663_c("fenceIron"));
        Block.field_149771_c.func_148756_a(102, "glass_pane", (Object)new BlockPane("glass", "glass_pane_top", Material.field_151592_s, false).func_149711_c(0.3f).func_149672_a(Block.field_149778_k).func_149663_c("thinGlass"));
        final Block block8 = new BlockMelon().func_149711_c(1.0f).func_149672_a(Block.field_149766_f).func_149663_c("melon").func_149658_d("melon");
        Block.field_149771_c.func_148756_a(103, "melon_block", (Object)block8);
        Block.field_149771_c.func_148756_a(104, "pumpkin_stem", (Object)new BlockStem(block6).func_149711_c(0.0f).func_149672_a(Block.field_149766_f).func_149663_c("pumpkinStem").func_149658_d("pumpkin_stem"));
        Block.field_149771_c.func_148756_a(105, "melon_stem", (Object)new BlockStem(block8).func_149711_c(0.0f).func_149672_a(Block.field_149766_f).func_149663_c("pumpkinStem").func_149658_d("melon_stem"));
        Block.field_149771_c.func_148756_a(106, "vine", (Object)new BlockVine().func_149711_c(0.2f).func_149672_a(Block.field_149779_h).func_149663_c("vine").func_149658_d("vine"));
        Block.field_149771_c.func_148756_a(107, "fence_gate", (Object)new BlockFenceGate().func_149711_c(2.0f).func_149752_b(5.0f).func_149672_a(Block.field_149766_f).func_149663_c("fenceGate"));
        Block.field_149771_c.func_148756_a(108, "brick_stairs", (Object)new BlockStairs(block4, 0).func_149663_c("stairsBrick"));
        Block.field_149771_c.func_148756_a(109, "stone_brick_stairs", (Object)new BlockStairs(block7, 0).func_149663_c("stairsStoneBrickSmooth"));
        Block.field_149771_c.func_148756_a(110, "mycelium", (Object)new BlockMycelium().func_149711_c(0.6f).func_149672_a(Block.field_149779_h).func_149663_c("mycel").func_149658_d("mycelium"));
        Block.field_149771_c.func_148756_a(111, "waterlily", (Object)new BlockLilyPad().func_149711_c(0.0f).func_149672_a(Block.field_149779_h).func_149663_c("waterlily").func_149658_d("waterlily"));
        final Block block9 = new Block(Material.field_151576_e).func_149711_c(2.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("netherBrick").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("nether_brick");
        Block.field_149771_c.func_148756_a(112, "nether_brick", (Object)block9);
        Block.field_149771_c.func_148756_a(113, "nether_brick_fence", (Object)new BlockFence("nether_brick", Material.field_151576_e).func_149711_c(2.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("netherFence"));
        Block.field_149771_c.func_148756_a(114, "nether_brick_stairs", (Object)new BlockStairs(block9, 0).func_149663_c("stairsNetherBrick"));
        Block.field_149771_c.func_148756_a(115, "nether_wart", (Object)new BlockNetherWart().func_149663_c("netherStalk").func_149658_d("nether_wart"));
        Block.field_149771_c.func_148756_a(116, "enchanting_table", (Object)new BlockEnchantmentTable().func_149711_c(5.0f).func_149752_b(2000.0f).func_149663_c("enchantmentTable").func_149658_d("enchanting_table"));
        Block.field_149771_c.func_148756_a(117, "brewing_stand", (Object)new BlockBrewingStand().func_149711_c(0.5f).func_149715_a(0.125f).func_149663_c("brewingStand").func_149658_d("brewing_stand"));
        Block.field_149771_c.func_148756_a(118, "cauldron", (Object)new BlockCauldron().func_149711_c(2.0f).func_149663_c("cauldron").func_149658_d("cauldron"));
        Block.field_149771_c.func_148756_a(119, "end_portal", (Object)new BlockEndPortal(Material.field_151567_E).func_149711_c(-1.0f).func_149752_b(6000000.0f));
        Block.field_149771_c.func_148756_a(120, "end_portal_frame", (Object)new BlockEndPortalFrame().func_149672_a(Block.field_149778_k).func_149715_a(0.125f).func_149711_c(-1.0f).func_149663_c("endPortalFrame").func_149752_b(6000000.0f).func_149647_a(CreativeTabs.field_78031_c).func_149658_d("endframe"));
        Block.field_149771_c.func_148756_a(121, "end_stone", (Object)new Block(Material.field_151576_e).func_149711_c(3.0f).func_149752_b(15.0f).func_149672_a(Block.field_149780_i).func_149663_c("whiteStone").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("end_stone"));
        Block.field_149771_c.func_148756_a(122, "dragon_egg", (Object)new BlockDragonEgg().func_149711_c(3.0f).func_149752_b(15.0f).func_149672_a(Block.field_149780_i).func_149715_a(0.125f).func_149663_c("dragonEgg").func_149658_d("dragon_egg"));
        Block.field_149771_c.func_148756_a(123, "redstone_lamp", (Object)new BlockRedstoneLight(false).func_149711_c(0.3f).func_149672_a(Block.field_149778_k).func_149663_c("redstoneLight").func_149647_a(CreativeTabs.field_78028_d).func_149658_d("redstone_lamp_off"));
        Block.field_149771_c.func_148756_a(124, "lit_redstone_lamp", (Object)new BlockRedstoneLight(true).func_149711_c(0.3f).func_149672_a(Block.field_149778_k).func_149663_c("redstoneLight").func_149658_d("redstone_lamp_on"));
        Block.field_149771_c.func_148756_a(125, "double_wooden_slab", (Object)new BlockWoodSlab(true).func_149711_c(2.0f).func_149752_b(5.0f).func_149672_a(Block.field_149766_f).func_149663_c("woodSlab"));
        Block.field_149771_c.func_148756_a(126, "wooden_slab", (Object)new BlockWoodSlab(false).func_149711_c(2.0f).func_149752_b(5.0f).func_149672_a(Block.field_149766_f).func_149663_c("woodSlab"));
        Block.field_149771_c.func_148756_a(127, "cocoa", (Object)new BlockCocoa().func_149711_c(0.2f).func_149752_b(5.0f).func_149672_a(Block.field_149766_f).func_149663_c("cocoa").func_149658_d("cocoa"));
        Block.field_149771_c.func_148756_a(128, "sandstone_stairs", (Object)new BlockStairs(block3, 0).func_149663_c("stairsSandStone"));
        Block.field_149771_c.func_148756_a(129, "emerald_ore", (Object)new BlockOre().func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("oreEmerald").func_149658_d("emerald_ore"));
        Block.field_149771_c.func_148756_a(130, "ender_chest", (Object)new BlockEnderChest().func_149711_c(22.5f).func_149752_b(1000.0f).func_149672_a(Block.field_149780_i).func_149663_c("enderChest").func_149715_a(0.5f));
        Block.field_149771_c.func_148756_a(131, "tripwire_hook", (Object)new BlockTripWireHook().func_149663_c("tripWireSource").func_149658_d("trip_wire_source"));
        Block.field_149771_c.func_148756_a(132, "tripwire", (Object)new BlockTripWire().func_149663_c("tripWire").func_149658_d("trip_wire"));
        Block.field_149771_c.func_148756_a(133, "emerald_block", (Object)new BlockCompressed(MapColor.field_151653_I).func_149711_c(5.0f).func_149752_b(10.0f).func_149672_a(Block.field_149777_j).func_149663_c("blockEmerald").func_149658_d("emerald_block"));
        Block.field_149771_c.func_148756_a(134, "spruce_stairs", (Object)new BlockStairs(block2, 1).func_149663_c("stairsWoodSpruce"));
        Block.field_149771_c.func_148756_a(135, "birch_stairs", (Object)new BlockStairs(block2, 2).func_149663_c("stairsWoodBirch"));
        Block.field_149771_c.func_148756_a(136, "jungle_stairs", (Object)new BlockStairs(block2, 3).func_149663_c("stairsWoodJungle"));
        Block.field_149771_c.func_148756_a(137, "command_block", (Object)new BlockCommandBlock().func_149722_s().func_149752_b(6000000.0f).func_149663_c("commandBlock").func_149658_d("command_block"));
        Block.field_149771_c.func_148756_a(138, "beacon", (Object)new BlockBeacon().func_149663_c("beacon").func_149715_a(1.0f).func_149658_d("beacon"));
        Block.field_149771_c.func_148756_a(139, "cobblestone_wall", (Object)new BlockWall(block).func_149663_c("cobbleWall"));
        Block.field_149771_c.func_148756_a(140, "flower_pot", (Object)new BlockFlowerPot().func_149711_c(0.0f).func_149672_a(Block.field_149769_e).func_149663_c("flowerPot").func_149658_d("flower_pot"));
        Block.field_149771_c.func_148756_a(141, "carrots", (Object)new BlockCarrot().func_149663_c("carrots").func_149658_d("carrots"));
        Block.field_149771_c.func_148756_a(142, "potatoes", (Object)new BlockPotato().func_149663_c("potatoes").func_149658_d("potatoes"));
        Block.field_149771_c.func_148756_a(143, "wooden_button", (Object)new BlockButtonWood().func_149711_c(0.5f).func_149672_a(Block.field_149766_f).func_149663_c("button"));
        Block.field_149771_c.func_148756_a(144, "skull", (Object)new BlockSkull().func_149711_c(1.0f).func_149672_a(Block.field_149780_i).func_149663_c("skull").func_149658_d("skull"));
        Block.field_149771_c.func_148756_a(145, "anvil", (Object)new BlockAnvil().func_149711_c(5.0f).func_149672_a(Block.field_149788_p).func_149752_b(2000.0f).func_149663_c("anvil"));
        Block.field_149771_c.func_148756_a(146, "trapped_chest", (Object)new BlockChest(1).func_149711_c(2.5f).func_149672_a(Block.field_149766_f).func_149663_c("chestTrap"));
        Block.field_149771_c.func_148756_a(147, "light_weighted_pressure_plate", (Object)new BlockPressurePlateWeighted("gold_block", Material.field_151573_f, 15).func_149711_c(0.5f).func_149672_a(Block.field_149766_f).func_149663_c("weightedPlate_light"));
        Block.field_149771_c.func_148756_a(148, "heavy_weighted_pressure_plate", (Object)new BlockPressurePlateWeighted("iron_block", Material.field_151573_f, 150).func_149711_c(0.5f).func_149672_a(Block.field_149766_f).func_149663_c("weightedPlate_heavy"));
        Block.field_149771_c.func_148756_a(149, "unpowered_comparator", (Object)new BlockRedstoneComparator(false).func_149711_c(0.0f).func_149672_a(Block.field_149766_f).func_149663_c("comparator").func_149649_H().func_149658_d("comparator_off"));
        Block.field_149771_c.func_148756_a(150, "powered_comparator", (Object)new BlockRedstoneComparator(true).func_149711_c(0.0f).func_149715_a(0.625f).func_149672_a(Block.field_149766_f).func_149663_c("comparator").func_149649_H().func_149658_d("comparator_on"));
        Block.field_149771_c.func_148756_a(151, "daylight_detector", (Object)new BlockDaylightDetector().func_149711_c(0.2f).func_149672_a(Block.field_149766_f).func_149663_c("daylightDetector").func_149658_d("daylight_detector"));
        Block.field_149771_c.func_148756_a(152, "redstone_block", (Object)new BlockCompressedPowered(MapColor.field_151656_f).func_149711_c(5.0f).func_149752_b(10.0f).func_149672_a(Block.field_149777_j).func_149663_c("blockRedstone").func_149658_d("redstone_block"));
        Block.field_149771_c.func_148756_a(153, "quartz_ore", (Object)new BlockOre().func_149711_c(3.0f).func_149752_b(5.0f).func_149672_a(Block.field_149780_i).func_149663_c("netherquartz").func_149658_d("quartz_ore"));
        Block.field_149771_c.func_148756_a(154, "hopper", (Object)new BlockHopper().func_149711_c(3.0f).func_149752_b(8.0f).func_149672_a(Block.field_149766_f).func_149663_c("hopper").func_149658_d("hopper"));
        final Block block10 = new BlockQuartz().func_149672_a(Block.field_149780_i).func_149711_c(0.8f).func_149663_c("quartzBlock").func_149658_d("quartz_block");
        Block.field_149771_c.func_148756_a(155, "quartz_block", (Object)block10);
        Block.field_149771_c.func_148756_a(156, "quartz_stairs", (Object)new BlockStairs(block10, 0).func_149663_c("stairsQuartz"));
        Block.field_149771_c.func_148756_a(157, "activator_rail", (Object)new BlockRailPowered().func_149711_c(0.7f).func_149672_a(Block.field_149777_j).func_149663_c("activatorRail").func_149658_d("rail_activator"));
        Block.field_149771_c.func_148756_a(158, "dropper", (Object)new BlockDropper().func_149711_c(3.5f).func_149672_a(Block.field_149780_i).func_149663_c("dropper").func_149658_d("dropper"));
        Block.field_149771_c.func_148756_a(159, "stained_hardened_clay", (Object)new BlockColored(Material.field_151576_e).func_149711_c(1.25f).func_149752_b(7.0f).func_149672_a(Block.field_149780_i).func_149663_c("clayHardenedStained").func_149658_d("hardened_clay_stained"));
        Block.field_149771_c.func_148756_a(160, "stained_glass_pane", (Object)new BlockStainedGlassPane().func_149711_c(0.3f).func_149672_a(Block.field_149778_k).func_149663_c("thinStainedGlass").func_149658_d("glass"));
        Block.field_149771_c.func_148756_a(161, "leaves2", (Object)new BlockNewLeaf().func_149663_c("leaves").func_149658_d("leaves"));
        Block.field_149771_c.func_148756_a(162, "log2", (Object)new BlockNewLog().func_149663_c("log").func_149658_d("log"));
        Block.field_149771_c.func_148756_a(163, "acacia_stairs", (Object)new BlockStairs(block2, 4).func_149663_c("stairsWoodAcacia"));
        Block.field_149771_c.func_148756_a(164, "dark_oak_stairs", (Object)new BlockStairs(block2, 5).func_149663_c("stairsWoodDarkOak"));
        Block.field_149771_c.func_148756_a(170, "hay_block", (Object)new BlockHay().func_149711_c(0.5f).func_149672_a(Block.field_149779_h).func_149663_c("hayBlock").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("hay_block"));
        Block.field_149771_c.func_148756_a(171, "carpet", (Object)new BlockCarpet().func_149711_c(0.1f).func_149672_a(Block.field_149775_l).func_149663_c("woolCarpet").func_149713_g(0));
        Block.field_149771_c.func_148756_a(172, "hardened_clay", (Object)new BlockHardenedClay().func_149711_c(1.25f).func_149752_b(7.0f).func_149672_a(Block.field_149780_i).func_149663_c("clayHardened").func_149658_d("hardened_clay"));
        Block.field_149771_c.func_148756_a(173, "coal_block", (Object)new Block(Material.field_151576_e).func_149711_c(5.0f).func_149752_b(10.0f).func_149672_a(Block.field_149780_i).func_149663_c("blockCoal").func_149647_a(CreativeTabs.field_78030_b).func_149658_d("coal_block"));
        Block.field_149771_c.func_148756_a(174, "packed_ice", (Object)new BlockPackedIce().func_149711_c(0.5f).func_149672_a(Block.field_149778_k).func_149663_c("icePacked").func_149658_d("ice_packed"));
        Block.field_149771_c.func_148756_a(175, "double_plant", (Object)new BlockDoublePlant());
        for (final Block block11 : Block.field_149771_c) {
            if (block11.field_149764_J == Material.field_151579_a) {
                block11.field_149783_u = false;
            }
            else {
                boolean flag = false;
                final boolean flag2 = block11.func_149645_b() == 10;
                final boolean flag3 = block11 instanceof BlockSlab;
                final boolean flag4 = block11 == block5;
                final boolean flag5 = block11.field_149785_s;
                final boolean flag6 = block11.field_149786_r == 0;
                if (flag2 || flag3 || flag4 || flag5 || flag6) {
                    flag = true;
                }
                block11.field_149783_u = flag;
            }
        }
    }
    
    protected Block(final Material p_i45394_1_) {
        this.field_149791_x = true;
        this.field_149790_y = true;
        this.harvesters = new ThreadLocal<EntityPlayer>();
        this.silk_check_meta = new ThreadLocal<Integer>();
        this.isTileProvider = (this instanceof ITileEntityProvider);
        this.harvestTool = new String[16];
        this.harvestLevel = new int[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1 };
        this.captureDrops = new ThreadLocal<Boolean>() {
            @Override
            protected Boolean initialValue() {
                return false;
            }
        };
        this.capturedDrops = new ThreadLocal<List<ItemStack>>() {
            @Override
            protected List<ItemStack> initialValue() {
                return new ArrayList<ItemStack>();
            }
        };
        this.field_149762_H = Block.field_149769_e;
        this.field_149763_I = 1.0f;
        this.field_149765_K = 0.6f;
        this.field_149764_J = p_i45394_1_;
        this.func_149676_a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        this.field_149787_q = this.func_149662_c();
        this.field_149786_r = (this.func_149662_c() ? 255 : 0);
        this.field_149785_s = !p_i45394_1_.func_76228_b();
    }
    
    public Block func_149672_a(final SoundType p_149672_1_) {
        this.field_149762_H = p_149672_1_;
        return this;
    }
    
    public Block func_149713_g(final int p_149713_1_) {
        this.field_149786_r = p_149713_1_;
        return this;
    }
    
    public Block func_149715_a(final float p_149715_1_) {
        this.field_149784_t = (int)(15.0f * p_149715_1_);
        return this;
    }
    
    public Block func_149752_b(final float p_149752_1_) {
        this.field_149781_w = p_149752_1_ * 3.0f;
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
    
    public boolean func_149655_b(final IBlockAccess p_149655_1_, final int p_149655_2_, final int p_149655_3_, final int p_149655_4_) {
        return !this.field_149764_J.func_76230_c();
    }
    
    public int func_149645_b() {
        return 0;
    }
    
    public Block func_149711_c(final float p_149711_1_) {
        this.field_149782_v = p_149711_1_;
        if (this.field_149781_w < p_149711_1_ * 5.0f) {
            this.field_149781_w = p_149711_1_ * 5.0f;
        }
        return this;
    }
    
    public Block func_149722_s() {
        this.func_149711_c(-1.0f);
        return this;
    }
    
    public float func_149712_f(final World p_149712_1_, final int p_149712_2_, final int p_149712_3_, final int p_149712_4_) {
        return this.field_149782_v;
    }
    
    public Block func_149675_a(final boolean p_149675_1_) {
        this.field_149789_z = p_149675_1_;
        return this;
    }
    
    public boolean func_149653_t() {
        return this.field_149789_z;
    }
    
    @Deprecated
    public boolean func_149716_u() {
        return this.hasTileEntity(0);
    }
    
    public final void func_149676_a(final float p_149676_1_, final float p_149676_2_, final float p_149676_3_, final float p_149676_4_, final float p_149676_5_, final float p_149676_6_) {
        this.field_149759_B = p_149676_1_;
        this.field_149760_C = p_149676_2_;
        this.field_149754_D = p_149676_3_;
        this.field_149755_E = p_149676_4_;
        this.field_149756_F = p_149676_5_;
        this.field_149757_G = p_149676_6_;
    }
    
    @SideOnly(Side.CLIENT)
    public int func_149677_c(final IBlockAccess p_149677_1_, final int p_149677_2_, int p_149677_3_, final int p_149677_4_) {
        Block block = p_149677_1_.func_147439_a(p_149677_2_, p_149677_3_, p_149677_4_);
        final int l = p_149677_1_.func_72802_i(p_149677_2_, p_149677_3_, p_149677_4_, block.getLightValue(p_149677_1_, p_149677_2_, p_149677_3_, p_149677_4_));
        if (l == 0 && block instanceof BlockSlab) {
            --p_149677_3_;
            block = p_149677_1_.func_147439_a(p_149677_2_, p_149677_3_, p_149677_4_);
            return p_149677_1_.func_72802_i(p_149677_2_, p_149677_3_, p_149677_4_, block.getLightValue(p_149677_1_, p_149677_2_, p_149677_3_, p_149677_4_));
        }
        return l;
    }
    
    @SideOnly(Side.CLIENT)
    public boolean func_149646_a(final IBlockAccess p_149646_1_, final int p_149646_2_, final int p_149646_3_, final int p_149646_4_, final int p_149646_5_) {
        return (this.func_149662_c() && NoCubes.isBlockNatural(p_149646_1_.func_147439_a(p_149646_2_, p_149646_3_, p_149646_4_))) || (p_149646_5_ == 0 && this.field_149760_C > 0.0) || (p_149646_5_ == 1 && this.field_149756_F < 1.0) || (p_149646_5_ == 2 && this.field_149754_D > 0.0) || (p_149646_5_ == 3 && this.field_149757_G < 1.0) || (p_149646_5_ == 4 && this.field_149759_B > 0.0) || (p_149646_5_ == 5 && this.field_149755_E < 1.0) || !p_149646_1_.func_147439_a(p_149646_2_, p_149646_3_, p_149646_4_).func_149662_c();
    }
    
    public boolean func_149747_d(final IBlockAccess p_149747_1_, final int p_149747_2_, final int p_149747_3_, final int p_149747_4_, final int p_149747_5_) {
        return p_149747_1_.func_147439_a(p_149747_2_, p_149747_3_, p_149747_4_).func_149688_o().func_76220_a();
    }
    
    @SideOnly(Side.CLIENT)
    public IIcon func_149673_e(final IBlockAccess p_149673_1_, final int p_149673_2_, final int p_149673_3_, final int p_149673_4_, final int p_149673_5_) {
        return this.func_149691_a(p_149673_5_, p_149673_1_.func_72805_g(p_149673_2_, p_149673_3_, p_149673_4_));
    }
    
    @SideOnly(Side.CLIENT)
    public IIcon func_149691_a(final int p_149691_1_, final int p_149691_2_) {
        return this.field_149761_L;
    }
    
    public void func_149743_a(final World p_149743_1_, final int p_149743_2_, final int p_149743_3_, final int p_149743_4_, final AxisAlignedBB p_149743_5_, final List p_149743_6_, final Entity p_149743_7_) {
        final AxisAlignedBB axisalignedbb1 = this.func_149668_a(p_149743_1_, p_149743_2_, p_149743_3_, p_149743_4_);
        if (axisalignedbb1 != null && p_149743_5_.func_72326_a(axisalignedbb1)) {
            p_149743_6_.add(axisalignedbb1);
        }
    }
    
    public AxisAlignedBB func_149668_a(final World p_149668_1_, final int p_149668_2_, final int p_149668_3_, final int p_149668_4_) {
        return AxisAlignedBB.func_72330_a(p_149668_2_ + this.field_149759_B, p_149668_3_ + this.field_149760_C, p_149668_4_ + this.field_149754_D, p_149668_2_ + this.field_149755_E, p_149668_3_ + this.field_149756_F, p_149668_4_ + this.field_149757_G);
    }
    
    @SideOnly(Side.CLIENT)
    public final IIcon func_149733_h(final int p_149733_1_) {
        return this.func_149691_a(p_149733_1_, 0);
    }
    
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB func_149633_g(final World p_149633_1_, final int p_149633_2_, final int p_149633_3_, final int p_149633_4_) {
        return AxisAlignedBB.func_72330_a(p_149633_2_ + this.field_149759_B, p_149633_3_ + this.field_149760_C, p_149633_4_ + this.field_149754_D, p_149633_2_ + this.field_149755_E, p_149633_3_ + this.field_149756_F, p_149633_4_ + this.field_149757_G);
    }
    
    public boolean func_149662_c() {
        return true;
    }
    
    public boolean func_149678_a(final int p_149678_1_, final boolean p_149678_2_) {
        return this.func_149703_v();
    }
    
    public boolean func_149703_v() {
        return true;
    }
    
    public void func_149674_a(final World p_149674_1_, final int p_149674_2_, final int p_149674_3_, final int p_149674_4_, final Random p_149674_5_) {
    }
    
    @SideOnly(Side.CLIENT)
    public void func_149734_b(final World p_149734_1_, final int p_149734_2_, final int p_149734_3_, final int p_149734_4_, final Random p_149734_5_) {
    }
    
    public void func_149664_b(final World p_149664_1_, final int p_149664_2_, final int p_149664_3_, final int p_149664_4_, final int p_149664_5_) {
    }
    
    public void func_149695_a(final World p_149695_1_, final int p_149695_2_, final int p_149695_3_, final int p_149695_4_, final Block p_149695_5_) {
    }
    
    public int func_149738_a(final World p_149738_1_) {
        return 10;
    }
    
    public void func_149726_b(final World p_149726_1_, final int p_149726_2_, final int p_149726_3_, final int p_149726_4_) {
    }
    
    public void func_149749_a(final World p_149749_1_, final int p_149749_2_, final int p_149749_3_, final int p_149749_4_, final Block p_149749_5_, final int p_149749_6_) {
        if (this.hasTileEntity(p_149749_6_) && !(this instanceof BlockContainer)) {
            p_149749_1_.func_147475_p(p_149749_2_, p_149749_3_, p_149749_4_);
        }
    }
    
    public int func_149745_a(final Random p_149745_1_) {
        return 1;
    }
    
    public Item func_149650_a(final int p_149650_1_, final Random p_149650_2_, final int p_149650_3_) {
        return Item.func_150898_a(this);
    }
    
    public float func_149737_a(final EntityPlayer p_149737_1_, final World p_149737_2_, final int p_149737_3_, final int p_149737_4_, final int p_149737_5_) {
        return ForgeHooks.blockStrength(this, p_149737_1_, p_149737_2_, p_149737_3_, p_149737_4_, p_149737_5_);
    }
    
    public final void func_149697_b(final World p_149697_1_, final int p_149697_2_, final int p_149697_3_, final int p_149697_4_, final int p_149697_5_, final int p_149697_6_) {
        this.func_149690_a(p_149697_1_, p_149697_2_, p_149697_3_, p_149697_4_, p_149697_5_, 1.0f, p_149697_6_);
    }
    
    public void func_149690_a(final World p_149690_1_, final int p_149690_2_, final int p_149690_3_, final int p_149690_4_, final int p_149690_5_, float p_149690_6_, final int p_149690_7_) {
        if (!p_149690_1_.field_72995_K) {
            final ArrayList<ItemStack> items = this.getDrops(p_149690_1_, p_149690_2_, p_149690_3_, p_149690_4_, p_149690_5_, p_149690_7_);
            p_149690_6_ = ForgeEventFactory.fireBlockHarvesting((ArrayList)items, p_149690_1_, this, p_149690_2_, p_149690_3_, p_149690_4_, p_149690_5_, p_149690_7_, p_149690_6_, false, (EntityPlayer)this.harvesters.get());
            for (final ItemStack item : items) {
                if (p_149690_1_.field_73012_v.nextFloat() <= p_149690_6_) {
                    this.func_149642_a(p_149690_1_, p_149690_2_, p_149690_3_, p_149690_4_, item);
                }
            }
        }
    }
    
    protected void func_149642_a(final World p_149642_1_, final int p_149642_2_, final int p_149642_3_, final int p_149642_4_, final ItemStack p_149642_5_) {
        if (!p_149642_1_.field_72995_K && p_149642_1_.func_82736_K().func_82766_b("doTileDrops")) {
            if (this.captureDrops.get()) {
                this.capturedDrops.get().add(p_149642_5_);
                return;
            }
            final float f = 0.7f;
            final double d0 = p_149642_1_.field_73012_v.nextFloat() * f + (1.0f - f) * 0.5;
            final double d2 = p_149642_1_.field_73012_v.nextFloat() * f + (1.0f - f) * 0.5;
            final double d3 = p_149642_1_.field_73012_v.nextFloat() * f + (1.0f - f) * 0.5;
            final EntityItem entityitem = new EntityItem(p_149642_1_, p_149642_2_ + d0, p_149642_3_ + d2, p_149642_4_ + d3, p_149642_5_);
            entityitem.field_145804_b = 10;
            p_149642_1_.func_72838_d((Entity)entityitem);
        }
    }
    
    public void func_149657_c(final World p_149657_1_, final int p_149657_2_, final int p_149657_3_, final int p_149657_4_, int p_149657_5_) {
        if (!p_149657_1_.field_72995_K) {
            while (p_149657_5_ > 0) {
                final int i1 = EntityXPOrb.func_70527_a(p_149657_5_);
                p_149657_5_ -= i1;
                p_149657_1_.func_72838_d((Entity)new EntityXPOrb(p_149657_1_, p_149657_2_ + 0.5, p_149657_3_ + 0.5, p_149657_4_ + 0.5, i1));
            }
        }
    }
    
    public int func_149692_a(final int p_149692_1_) {
        return 0;
    }
    
    public float func_149638_a(final Entity p_149638_1_) {
        return this.field_149781_w / 5.0f;
    }
    
    public MovingObjectPosition func_149731_a(final World p_149731_1_, final int p_149731_2_, final int p_149731_3_, final int p_149731_4_, Vec3 p_149731_5_, Vec3 p_149731_6_) {
        this.func_149719_a((IBlockAccess)p_149731_1_, p_149731_2_, p_149731_3_, p_149731_4_);
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
        }
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
        return new MovingObjectPosition(p_149731_2_, p_149731_3_, p_149731_4_, (int)b0, vec38.func_72441_c((double)p_149731_2_, (double)p_149731_3_, (double)p_149731_4_));
    }
    
    private boolean func_149654_a(final Vec3 p_149654_1_) {
        return p_149654_1_ != null && (p_149654_1_.field_72448_b >= this.field_149760_C && p_149654_1_.field_72448_b <= this.field_149756_F && p_149654_1_.field_72449_c >= this.field_149754_D && p_149654_1_.field_72449_c <= this.field_149757_G);
    }
    
    private boolean func_149687_b(final Vec3 p_149687_1_) {
        return p_149687_1_ != null && (p_149687_1_.field_72450_a >= this.field_149759_B && p_149687_1_.field_72450_a <= this.field_149755_E && p_149687_1_.field_72449_c >= this.field_149754_D && p_149687_1_.field_72449_c <= this.field_149757_G);
    }
    
    private boolean func_149661_c(final Vec3 p_149661_1_) {
        return p_149661_1_ != null && (p_149661_1_.field_72450_a >= this.field_149759_B && p_149661_1_.field_72450_a <= this.field_149755_E && p_149661_1_.field_72448_b >= this.field_149760_C && p_149661_1_.field_72448_b <= this.field_149756_F);
    }
    
    public void func_149723_a(final World p_149723_1_, final int p_149723_2_, final int p_149723_3_, final int p_149723_4_, final Explosion p_149723_5_) {
    }
    
    public boolean func_149705_a(final World p_149705_1_, final int p_149705_2_, final int p_149705_3_, final int p_149705_4_, final int p_149705_5_, final ItemStack p_149705_6_) {
        return this.func_149707_d(p_149705_1_, p_149705_2_, p_149705_3_, p_149705_4_, p_149705_5_);
    }
    
    @SideOnly(Side.CLIENT)
    public int func_149701_w() {
        return 0;
    }
    
    public boolean func_149707_d(final World p_149707_1_, final int p_149707_2_, final int p_149707_3_, final int p_149707_4_, final int p_149707_5_) {
        return this.func_149742_c(p_149707_1_, p_149707_2_, p_149707_3_, p_149707_4_);
    }
    
    public boolean func_149742_c(final World p_149742_1_, final int p_149742_2_, final int p_149742_3_, final int p_149742_4_) {
        return p_149742_1_.func_147439_a(p_149742_2_, p_149742_3_, p_149742_4_).isReplaceable((IBlockAccess)p_149742_1_, p_149742_2_, p_149742_3_, p_149742_4_);
    }
    
    public boolean func_149727_a(final World p_149727_1_, final int p_149727_2_, final int p_149727_3_, final int p_149727_4_, final EntityPlayer p_149727_5_, final int p_149727_6_, final float p_149727_7_, final float p_149727_8_, final float p_149727_9_) {
        return false;
    }
    
    public void func_149724_b(final World p_149724_1_, final int p_149724_2_, final int p_149724_3_, final int p_149724_4_, final Entity p_149724_5_) {
    }
    
    public int func_149660_a(final World p_149660_1_, final int p_149660_2_, final int p_149660_3_, final int p_149660_4_, final int p_149660_5_, final float p_149660_6_, final float p_149660_7_, final float p_149660_8_, final int p_149660_9_) {
        return p_149660_9_;
    }
    
    public void func_149699_a(final World p_149699_1_, final int p_149699_2_, final int p_149699_3_, final int p_149699_4_, final EntityPlayer p_149699_5_) {
    }
    
    public void func_149640_a(final World p_149640_1_, final int p_149640_2_, final int p_149640_3_, final int p_149640_4_, final Entity p_149640_5_, final Vec3 p_149640_6_) {
    }
    
    public void func_149719_a(final IBlockAccess p_149719_1_, final int p_149719_2_, final int p_149719_3_, final int p_149719_4_) {
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
    public int func_149741_i(final int p_149741_1_) {
        return 16777215;
    }
    
    @SideOnly(Side.CLIENT)
    public int func_149720_d(final IBlockAccess p_149720_1_, final int p_149720_2_, final int p_149720_3_, final int p_149720_4_) {
        return 16777215;
    }
    
    public int func_149709_b(final IBlockAccess p_149709_1_, final int p_149709_2_, final int p_149709_3_, final int p_149709_4_, final int p_149709_5_) {
        return 0;
    }
    
    public boolean func_149744_f() {
        return false;
    }
    
    public void func_149670_a(final World p_149670_1_, final int p_149670_2_, final int p_149670_3_, final int p_149670_4_, final Entity p_149670_5_) {
    }
    
    public int func_149748_c(final IBlockAccess p_149748_1_, final int p_149748_2_, final int p_149748_3_, final int p_149748_4_, final int p_149748_5_) {
        return 0;
    }
    
    public void func_149683_g() {
    }
    
    public void func_149636_a(final World p_149636_1_, final EntityPlayer p_149636_2_, final int p_149636_3_, final int p_149636_4_, final int p_149636_5_, final int p_149636_6_) {
        p_149636_2_.func_71064_a(StatList.field_75934_C[func_149682_b(this)], 1);
        p_149636_2_.func_71020_j(0.025f);
        if (this.canSilkHarvest(p_149636_1_, p_149636_2_, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_) && EnchantmentHelper.func_77502_d((EntityLivingBase)p_149636_2_)) {
            final ArrayList<ItemStack> items = new ArrayList<ItemStack>();
            final ItemStack itemstack = this.func_149644_j(p_149636_6_);
            if (itemstack != null) {
                items.add(itemstack);
            }
            ForgeEventFactory.fireBlockHarvesting((ArrayList)items, p_149636_1_, this, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_, 0, 1.0f, true, p_149636_2_);
            for (final ItemStack is : items) {
                this.func_149642_a(p_149636_1_, p_149636_3_, p_149636_4_, p_149636_5_, is);
            }
        }
        else {
            this.harvesters.set(p_149636_2_);
            final int i1 = EnchantmentHelper.func_77517_e((EntityLivingBase)p_149636_2_);
            this.func_149697_b(p_149636_1_, p_149636_3_, p_149636_4_, p_149636_5_, p_149636_6_, i1);
            this.harvesters.set(null);
        }
    }
    
    protected boolean func_149700_E() {
        final Integer meta = this.silk_check_meta.get();
        return this.func_149686_d() && !this.hasTileEntity((meta == null) ? 0 : ((int)meta));
    }
    
    protected ItemStack func_149644_j(final int p_149644_1_) {
        int j = 0;
        final Item item = Item.func_150898_a(this);
        if (item != null && item.func_77614_k()) {
            j = p_149644_1_;
        }
        return new ItemStack(item, 1, j);
    }
    
    public int func_149679_a(final int p_149679_1_, final Random p_149679_2_) {
        return this.func_149745_a(p_149679_2_);
    }
    
    public boolean func_149718_j(final World p_149718_1_, final int p_149718_2_, final int p_149718_3_, final int p_149718_4_) {
        return true;
    }
    
    public void func_149689_a(final World p_149689_1_, final int p_149689_2_, final int p_149689_3_, final int p_149689_4_, final EntityLivingBase p_149689_5_, final ItemStack p_149689_6_) {
    }
    
    public void func_149714_e(final World p_149714_1_, final int p_149714_2_, final int p_149714_3_, final int p_149714_4_, final int p_149714_5_) {
    }
    
    public Block func_149663_c(final String p_149663_1_) {
        this.field_149770_b = p_149663_1_;
        return this;
    }
    
    public String func_149732_F() {
        return StatCollector.func_74838_a(this.func_149739_a() + ".name");
    }
    
    public String func_149739_a() {
        return "tile." + this.field_149770_b;
    }
    
    public boolean func_149696_a(final World p_149696_1_, final int p_149696_2_, final int p_149696_3_, final int p_149696_4_, final int p_149696_5_, final int p_149696_6_) {
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
        return this.func_149637_q() ? 0.2f : 1.0f;
    }
    
    public void func_149746_a(final World p_149746_1_, final int p_149746_2_, final int p_149746_3_, final int p_149746_4_, final Entity p_149746_5_, final float p_149746_6_) {
    }
    
    @SideOnly(Side.CLIENT)
    public Item func_149694_d(final World p_149694_1_, final int p_149694_2_, final int p_149694_3_, final int p_149694_4_) {
        return Item.func_150898_a(this);
    }
    
    public int func_149643_k(final World p_149643_1_, final int p_149643_2_, final int p_149643_3_, final int p_149643_4_) {
        return this.func_149692_a(p_149643_1_.func_72805_g(p_149643_2_, p_149643_3_, p_149643_4_));
    }
    
    @SideOnly(Side.CLIENT)
    public void func_149666_a(final Item p_149666_1_, final CreativeTabs p_149666_2_, final List p_149666_3_) {
        p_149666_3_.add(new ItemStack(p_149666_1_, 1, 0));
    }
    
    public Block func_149647_a(final CreativeTabs p_149647_1_) {
        this.field_149772_a = p_149647_1_;
        return this;
    }
    
    public void func_149681_a(final World p_149681_1_, final int p_149681_2_, final int p_149681_3_, final int p_149681_4_, final int p_149681_5_, final EntityPlayer p_149681_6_) {
    }
    
    @SideOnly(Side.CLIENT)
    public CreativeTabs func_149708_J() {
        return this.field_149772_a;
    }
    
    public void func_149725_f(final World p_149725_1_, final int p_149725_2_, final int p_149725_3_, final int p_149725_4_, final int p_149725_5_) {
    }
    
    public void func_149639_l(final World p_149639_1_, final int p_149639_2_, final int p_149639_3_, final int p_149639_4_) {
    }
    
    @SideOnly(Side.CLIENT)
    public boolean func_149648_K() {
        return false;
    }
    
    public boolean func_149698_L() {
        return true;
    }
    
    public boolean func_149659_a(final Explosion p_149659_1_) {
        return true;
    }
    
    public boolean func_149667_c(final Block p_149667_1_) {
        return this == p_149667_1_;
    }
    
    public static boolean func_149680_a(final Block p_149680_0_, final Block p_149680_1_) {
        return p_149680_0_ != null && p_149680_1_ != null && (p_149680_0_ == p_149680_1_ || p_149680_0_.func_149667_c(p_149680_1_));
    }
    
    public boolean func_149740_M() {
        return false;
    }
    
    public int func_149736_g(final World p_149736_1_, final int p_149736_2_, final int p_149736_3_, final int p_149736_4_, final int p_149736_5_) {
        return 0;
    }
    
    public Block func_149658_d(final String p_149658_1_) {
        this.field_149768_d = p_149658_1_;
        return this;
    }
    
    @SideOnly(Side.CLIENT)
    protected String func_149641_N() {
        return (this.field_149768_d == null) ? ("MISSING_ICON_BLOCK_" + func_149682_b(this) + "_" + this.field_149770_b) : this.field_149768_d;
    }
    
    @SideOnly(Side.CLIENT)
    public IIcon func_149735_b(final int p_149735_1_, final int p_149735_2_) {
        return this.func_149691_a(p_149735_1_, p_149735_2_);
    }
    
    @SideOnly(Side.CLIENT)
    public void func_149651_a(final IIconRegister p_149651_1_) {
        this.field_149761_L = p_149651_1_.func_94245_a(this.func_149641_N());
    }
    
    @SideOnly(Side.CLIENT)
    public String func_149702_O() {
        return null;
    }
    
    public int getLightValue(final IBlockAccess world, final int x, final int y, final int z) {
        final Block block = world.func_147439_a(x, y, z);
        if (block != this) {
            return block.getLightValue(world, x, y, z);
        }
        return this.func_149750_m();
    }
    
    public boolean isLadder(final IBlockAccess world, final int x, final int y, final int z, final EntityLivingBase entity) {
        return false;
    }
    
    public boolean isNormalCube(final IBlockAccess world, final int x, final int y, final int z) {
        return this.func_149688_o().func_76218_k() && this.func_149686_d() && !this.func_149744_f();
    }
    
    public boolean isSideSolid(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection side) {
        final int meta = world.func_72805_g(x, y, z);
        if (this instanceof BlockSlab) {
            return ((meta & 0x8) == 0x8 && side == ForgeDirection.UP) || this.func_149730_j();
        }
        if (this instanceof BlockFarmland) {
            return side != ForgeDirection.DOWN && side != ForgeDirection.UP;
        }
        if (this instanceof BlockStairs) {
            final boolean flipped = (meta & 0x4) != 0x0;
            return (meta & 0x3) + side.ordinal() == 5 || (side == ForgeDirection.UP && flipped);
        }
        if (this instanceof BlockSnow) {
            return (meta & 0x7) == 0x7;
        }
        return (this instanceof BlockHopper && side == ForgeDirection.UP) || this instanceof BlockCompressedPowered || this.isNormalCube(world, x, y, z);
    }
    
    public boolean isReplaceable(final IBlockAccess world, final int x, final int y, final int z) {
        return this.field_149764_J.func_76222_j();
    }
    
    public boolean isBurning(final IBlockAccess world, final int x, final int y, final int z) {
        return false;
    }
    
    public boolean isAir(final IBlockAccess world, final int x, final int y, final int z) {
        return this.func_149688_o() == Material.field_151579_a;
    }
    
    public boolean canHarvestBlock(final EntityPlayer player, final int meta) {
        return ForgeHooks.canHarvestBlock(this, player, meta);
    }
    
    public boolean removedByPlayer(final World world, final EntityPlayer player, final int x, final int y, final int z, final boolean willHarvest) {
        return this.removedByPlayer(world, player, x, y, z);
    }
    
    @Deprecated
    public boolean removedByPlayer(final World world, final EntityPlayer player, final int x, final int y, final int z) {
        return world.func_147468_f(x, y, z);
    }
    
    public int getFlammability(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection face) {
        return Blocks.field_150480_ab.getFlammability(this);
    }
    
    public boolean isFlammable(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection face) {
        return this.getFlammability(world, x, y, z, face) > 0;
    }
    
    public int getFireSpreadSpeed(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection face) {
        return Blocks.field_150480_ab.getEncouragement(this);
    }
    
    public boolean isFireSource(final World world, final int x, final int y, final int z, final ForgeDirection side) {
        return (this == Blocks.field_150424_aL && side == ForgeDirection.UP) || (world.field_73011_w instanceof WorldProviderEnd && this == Blocks.field_150357_h && side == ForgeDirection.UP);
    }
    
    public boolean hasTileEntity(final int metadata) {
        return this.isTileProvider;
    }
    
    public TileEntity createTileEntity(final World world, final int metadata) {
        if (this.isTileProvider) {
            return ((ITileEntityProvider)this).func_149915_a(world, metadata);
        }
        return null;
    }
    
    public int quantityDropped(final int meta, final int fortune, final Random random) {
        return this.func_149679_a(fortune, random);
    }
    
    public ArrayList<ItemStack> getDrops(final World world, final int x, final int y, final int z, final int metadata, final int fortune) {
        final ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        for (int count = this.quantityDropped(metadata, fortune, world.field_73012_v), i = 0; i < count; ++i) {
            final Item item = this.func_149650_a(metadata, world.field_73012_v, fortune);
            if (item != null) {
                ret.add(new ItemStack(item, 1, this.func_149692_a(metadata)));
            }
        }
        return ret;
    }
    
    public boolean canSilkHarvest(final World world, final EntityPlayer player, final int x, final int y, final int z, final int metadata) {
        this.silk_check_meta.set(metadata);
        final boolean ret = this.func_149700_E();
        this.silk_check_meta.set(null);
        return ret;
    }
    
    public boolean canCreatureSpawn(final EnumCreatureType type, final IBlockAccess world, final int x, final int y, final int z) {
        final int meta = world.func_72805_g(x, y, z);
        if (this instanceof BlockSlab) {
            return (meta & 0x8) == 0x8 || this.func_149730_j();
        }
        if (this instanceof BlockStairs) {
            return (meta & 0x4) != 0x0;
        }
        return this.isSideSolid(world, x, y, z, ForgeDirection.UP);
    }
    
    public boolean isBed(final IBlockAccess world, final int x, final int y, final int z, final EntityLivingBase player) {
        return this == Blocks.field_150324_C;
    }
    
    public ChunkCoordinates getBedSpawnPosition(final IBlockAccess world, final int x, final int y, final int z, final EntityPlayer player) {
        if (world instanceof World) {
            return BlockBed.func_149977_a((World)world, x, y, z, 0);
        }
        return null;
    }
    
    public void setBedOccupied(final IBlockAccess world, final int x, final int y, final int z, final EntityPlayer player, final boolean occupied) {
        if (world instanceof World) {
            BlockBed.func_149979_a((World)world, x, y, z, occupied);
        }
    }
    
    public int getBedDirection(final IBlockAccess world, final int x, final int y, final int z) {
        return BlockBed.func_149895_l(world.func_72805_g(x, y, z));
    }
    
    public boolean isBedFoot(final IBlockAccess world, final int x, final int y, final int z) {
        return BlockBed.func_149975_b(world.func_72805_g(x, y, z));
    }
    
    public void beginLeavesDecay(final World world, final int x, final int y, final int z) {
    }
    
    public boolean canSustainLeaves(final IBlockAccess world, final int x, final int y, final int z) {
        return false;
    }
    
    public boolean isLeaves(final IBlockAccess world, final int x, final int y, final int z) {
        return this.func_149688_o() == Material.field_151584_j;
    }
    
    public boolean canBeReplacedByLeaves(final IBlockAccess world, final int x, final int y, final int z) {
        return !this.func_149730_j();
    }
    
    public boolean isWood(final IBlockAccess world, final int x, final int y, final int z) {
        return false;
    }
    
    public boolean isReplaceableOreGen(final World world, final int x, final int y, final int z, final Block target) {
        return this == target;
    }
    
    public float getExplosionResistance(final Entity par1Entity, final World world, final int x, final int y, final int z, final double explosionX, final double explosionY, final double explosionZ) {
        return this.func_149638_a(par1Entity);
    }
    
    public void onBlockExploded(final World world, final int x, final int y, final int z, final Explosion explosion) {
        world.func_147468_f(x, y, z);
        this.func_149723_a(world, x, y, z, explosion);
    }
    
    public boolean canConnectRedstone(final IBlockAccess world, final int x, final int y, final int z, final int side) {
        return this.func_149744_f() && side != -1;
    }
    
    public boolean canPlaceTorchOnTop(final World world, final int x, final int y, final int z) {
        return this.isSideSolid((IBlockAccess)world, x, y, z, ForgeDirection.UP) || this == Blocks.field_150422_aJ || this == Blocks.field_150386_bk || this == Blocks.field_150359_w || this == Blocks.field_150463_bK;
    }
    
    public boolean canRenderInPass(final int pass) {
        return pass == this.func_149701_w();
    }
    
    public ItemStack getPickBlock(final MovingObjectPosition target, final World world, final int x, final int y, final int z) {
        final Item item = this.func_149694_d(world, x, y, z);
        if (item == null) {
            return null;
        }
        final Block block = (item instanceof ItemBlock && !this.func_149648_K()) ? func_149634_a(item) : this;
        return new ItemStack(item, 1, block.func_149643_k(world, x, y, z));
    }
    
    public boolean isFoliage(final IBlockAccess world, final int x, final int y, final int z) {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(final World worldObj, final MovingObjectPosition target, final EffectRenderer effectRenderer) {
        return false;
    }
    
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(final World world, final int x, final int y, final int z, final int meta, final EffectRenderer effectRenderer) {
        return false;
    }
    
    public boolean canSustainPlant(final IBlockAccess world, final int x, final int y, final int z, final ForgeDirection direction, final IPlantable plantable) {
        final Block plant = plantable.getPlant(world, x, y + 1, z);
        final EnumPlantType plantType = plantable.getPlantType(world, x, y + 1, z);
        if (plant == Blocks.field_150434_aF && this == Blocks.field_150434_aF) {
            return true;
        }
        if (plant == Blocks.field_150436_aH && this == Blocks.field_150436_aH) {
            return true;
        }
        if (plantable instanceof BlockBush && ((BlockBush)plantable).func_149854_a(this)) {
            return true;
        }
        switch (plantType) {
            case Desert: {
                return this == Blocks.field_150354_m;
            }
            case Nether: {
                return this == Blocks.field_150425_aM;
            }
            case Crop: {
                return this == Blocks.field_150458_ak;
            }
            case Cave: {
                return this.isSideSolid(world, x, y, z, ForgeDirection.UP);
            }
            case Plains: {
                return this == Blocks.field_150349_c || this == Blocks.field_150346_d || this == Blocks.field_150458_ak;
            }
            case Water: {
                return world.func_147439_a(x, y, z).func_149688_o() == Material.field_151586_h && world.func_72805_g(x, y, z) == 0;
            }
            case Beach: {
                final boolean isBeach = this == Blocks.field_150349_c || this == Blocks.field_150346_d || this == Blocks.field_150354_m;
                final boolean hasWater = world.func_147439_a(x - 1, y, z).func_149688_o() == Material.field_151586_h || world.func_147439_a(x + 1, y, z).func_149688_o() == Material.field_151586_h || world.func_147439_a(x, y, z - 1).func_149688_o() == Material.field_151586_h || world.func_147439_a(x, y, z + 1).func_149688_o() == Material.field_151586_h;
                return isBeach && hasWater;
            }
            default: {
                return false;
            }
        }
    }
    
    public void onPlantGrow(final World world, final int x, final int y, final int z, final int sourceX, final int sourceY, final int sourceZ) {
        if (this == Blocks.field_150349_c || this == Blocks.field_150458_ak) {
            world.func_147465_d(x, y, z, Blocks.field_150346_d, 0, 2);
        }
    }
    
    public boolean isFertile(final World world, final int x, final int y, final int z) {
        return this == Blocks.field_150458_ak && world.func_72805_g(x, y, z) > 0;
    }
    
    public int getLightOpacity(final IBlockAccess world, final int x, final int y, final int z) {
        return this.func_149717_k();
    }
    
    public boolean canEntityDestroy(final IBlockAccess world, final int x, final int y, final int z, final Entity entity) {
        if (entity instanceof EntityWither) {
            return this != Blocks.field_150357_h && this != Blocks.field_150384_bq && this != Blocks.field_150378_br && this != Blocks.field_150483_bI;
        }
        return !(entity instanceof EntityDragon) || (this != Blocks.field_150343_Z && this != Blocks.field_150377_bs && this != Blocks.field_150357_h);
    }
    
    public boolean isBeaconBase(final IBlockAccess worldObj, final int x, final int y, final int z, final int beaconX, final int beaconY, final int beaconZ) {
        return this == Blocks.field_150475_bE || this == Blocks.field_150340_R || this == Blocks.field_150484_ah || this == Blocks.field_150339_S;
    }
    
    public boolean rotateBlock(final World worldObj, final int x, final int y, final int z, final ForgeDirection axis) {
        return RotationHelper.rotateVanillaBlock(this, worldObj, x, y, z, axis);
    }
    
    public ForgeDirection[] getValidRotations(final World worldObj, final int x, final int y, final int z) {
        return RotationHelper.getValidVanillaBlockRotations(this);
    }
    
    public float getEnchantPowerBonus(final World world, final int x, final int y, final int z) {
        return (this == Blocks.field_150342_X) ? 1.0f : 0.0f;
    }
    
    public boolean recolourBlock(final World world, final int x, final int y, final int z, final ForgeDirection side, final int colour) {
        if (this == Blocks.field_150325_L) {
            final int meta = world.func_72805_g(x, y, z);
            if (meta != colour) {
                world.func_72921_c(x, y, z, colour, 3);
                return true;
            }
        }
        return false;
    }
    
    public int getExpDrop(final IBlockAccess world, final int metadata, final int fortune) {
        return 0;
    }
    
    public void onNeighborChange(final IBlockAccess world, final int x, final int y, final int z, final int tileX, final int tileY, final int tileZ) {
    }
    
    public boolean shouldCheckWeakPower(final IBlockAccess world, final int x, final int y, final int z, final int side) {
        return this.func_149721_r();
    }
    
    public boolean getWeakChanges(final IBlockAccess world, final int x, final int y, final int z) {
        return false;
    }
    
    public void setHarvestLevel(final String toolClass, final int level) {
        for (int m = 0; m < 16; ++m) {
            this.setHarvestLevel(toolClass, level, m);
        }
    }
    
    public void setHarvestLevel(final String toolClass, final int level, final int metadata) {
        this.harvestTool[metadata] = toolClass;
        this.harvestLevel[metadata] = level;
    }
    
    public String getHarvestTool(final int metadata) {
        return this.harvestTool[metadata];
    }
    
    public int getHarvestLevel(final int metadata) {
        return this.harvestLevel[metadata];
    }
    
    public boolean isToolEffective(final String type, final int metadata) {
        return (!"pickaxe".equals(type) || (this != Blocks.field_150450_ax && this != Blocks.field_150439_ay && this != Blocks.field_150343_Z)) && this.harvestTool[metadata] != null && this.harvestTool[metadata].equals(type);
    }
    
    protected List<ItemStack> captureDrops(final boolean start) {
        if (start) {
            this.captureDrops.set(true);
            this.capturedDrops.get().clear();
            return null;
        }
        this.captureDrops.set(false);
        return this.capturedDrops.get();
    }
    
    static {
        field_149771_c = (RegistryNamespaced)GameData.getBlockRegistry();
        field_149769_e = new SoundType("stone", 1.0f, 1.0f);
        field_149766_f = new SoundType("wood", 1.0f, 1.0f);
        field_149767_g = new SoundType("gravel", 1.0f, 1.0f);
        field_149779_h = new SoundType("grass", 1.0f, 1.0f);
        field_149780_i = new SoundType("stone", 1.0f, 1.0f);
        field_149777_j = new SoundType("stone", 1.0f, 1.5f);
        field_149778_k = new SoundType("stone", 1.0f, 1.0f) {
            private static final String __OBFID = "CL_00000200";
            
            @Override
            public String func_150495_a() {
                return "dig.glass";
            }
            
            @Override
            public String func_150496_b() {
                return "step.stone";
            }
        };
        field_149775_l = new SoundType("cloth", 1.0f, 1.0f);
        field_149776_m = new SoundType("sand", 1.0f, 1.0f);
        field_149773_n = new SoundType("snow", 1.0f, 1.0f);
        field_149774_o = new SoundType("ladder", 1.0f, 1.0f) {
            private static final String __OBFID = "CL_00000201";
            
            @Override
            public String func_150495_a() {
                return "dig.wood";
            }
        };
        field_149788_p = new SoundType("anvil", 0.3f, 1.0f) {
            private static final String __OBFID = "CL_00000202";
            
            @Override
            public String func_150495_a() {
                return "dig.stone";
            }
            
            @Override
            public String func_150496_b() {
                return "random.anvil_land";
            }
        };
    }
    
    public static class SoundType
    {
        public final String field_150501_a;
        public final float field_150499_b;
        public final float field_150500_c;
        private static final String __OBFID = "CL_00000203";
        
        public SoundType(final String p_i45393_1_, final float p_i45393_2_, final float p_i45393_3_) {
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
