package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.ExtendLiquidRange;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static net.minecraft.init.Blocks.BEDROCK;
import static net.minecraft.init.Blocks.CLAY;
import static net.minecraft.init.Blocks.COAL_ORE;
import static net.minecraft.init.Blocks.DIAMOND_ORE;
import static net.minecraft.init.Blocks.DIRT;
import static net.minecraft.init.Blocks.EMERALD_ORE;
import static net.minecraft.init.Blocks.END_STONE;
import static net.minecraft.init.Blocks.GLOWSTONE;
import static net.minecraft.init.Blocks.GOLD_ORE;
import static net.minecraft.init.Blocks.GRASS;
import static net.minecraft.init.Blocks.GRASS_PATH;
import static net.minecraft.init.Blocks.GRAVEL;
import static net.minecraft.init.Blocks.HARDENED_CLAY;
import static net.minecraft.init.Blocks.IRON_ORE;
import static net.minecraft.init.Blocks.LAPIS_ORE;
import static net.minecraft.init.Blocks.LIT_REDSTONE_ORE;
import static net.minecraft.init.Blocks.MAGMA;
import static net.minecraft.init.Blocks.MONSTER_EGG;
import static net.minecraft.init.Blocks.MYCELIUM;
import static net.minecraft.init.Blocks.NETHERRACK;
import static net.minecraft.init.Blocks.QUARTZ_ORE;
import static net.minecraft.init.Blocks.REDSTONE_ORE;
import static net.minecraft.init.Blocks.RED_SANDSTONE;
import static net.minecraft.init.Blocks.SAND;
import static net.minecraft.init.Blocks.SANDSTONE;
import static net.minecraft.init.Blocks.SNOW;
import static net.minecraft.init.Blocks.SNOW_LAYER;
import static net.minecraft.init.Blocks.SOUL_SAND;
import static net.minecraft.init.Blocks.STAINED_HARDENED_CLAY;
import static net.minecraft.init.Blocks.STONE;
import static net.minecraft.item.EnumDyeColor.BLACK;
import static net.minecraft.item.EnumDyeColor.BROWN;
import static net.minecraft.item.EnumDyeColor.ORANGE;
import static net.minecraft.item.EnumDyeColor.RED;
import static net.minecraft.item.EnumDyeColor.SILVER;
import static net.minecraft.item.EnumDyeColor.WHITE;
import static net.minecraft.item.EnumDyeColor.YELLOW;

/**
 * Our Mod's configuration
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
@Config(modid = MOD_ID)
@LangKey(MOD_ID + ".config.title")
public final class ModConfig {

	@Config.Ignore
	private static final transient HashSet<IBlockState> SMOOTHABLE_BLOCK_STATES_CACHE = new HashSet<>();

	@LangKey(MOD_ID + ".config.isEnabled")
	public static boolean isEnabled = true;

	@LangKey(MOD_ID + ".config.meshGenerator")
	public static MeshGenerator meshGenerator = MeshGenerator.SurfaceNets;

	@LangKey(MOD_ID + ".config.reloadChunksOnConfigChange")
	public static boolean reloadChunksOnConfigChange = true;

	@LangKey(MOD_ID + ".config.smoothableBlockStates")
	public static String[] smoothableBlockStates;

	//	@LangKey(MOD_ID + ".config.isosurfaceLevel")
//	@Config.RangeDouble(min = -10, max = 10)
	@Config.Ignore
	public static double isosurfaceLevel = 1.0D;

	//	@LangKey(MOD_ID + ".config.offsetVertices")
	@Config.Ignore
	public static boolean offsetVertices = false;

	//	@LangKey(MOD_ID + ".config.offsetAmmount")
//	@Config.RangeDouble(min = -10, max = 10)
	@Config.Ignore
	public static double offsetAmmount = 0.5F;

	@LangKey(MOD_ID + ".config.approximateLighting")
	public static boolean approximateLighting = true;

	@LangKey(MOD_ID + ".config.smoothLeavesSeparate")
	public static boolean smoothLeavesSeparate = true;

	@LangKey(MOD_ID + ".config.extendLiquids")
	public static ExtendLiquidRange extendLiquids = ExtendLiquidRange.OneBlock;

	@Config.Ignore
	//yenah i dont like it //TODO: remove
	public static boolean renderEmptyBlocksOrWhatever = false;

	@Config.Ignore
//	@Config.RangeDouble(min = -10, max = 10)
	public static double smoothOtherBlocksAmount = 0.0F;

	@Config.Ignore
	public static boolean collisionsEnabled = true;

	@Config.Ignore
	public static boolean enablePools = true;

	@LangKey(MOD_ID + ".config.smoothBiomeColors")
	public static boolean smoothBiomeColors = true;

	@LangKey(MOD_ID + ".config.smoothFluidBiomeColors")
	public static boolean smoothFluidBiomeColors = true;

	@LangKey(MOD_ID + ".config.smoothFluidLighting")
	public static boolean smoothFluidLighting = true;

	@LangKey(MOD_ID + ".config.naturalFluidTextures")
	public static boolean naturalFluidTextures = false;

	static {
		setupSmoothableBlockStates();
	}

	public static HashSet<IBlockState> getSmoothableBlockStatesCache() {
		return SMOOTHABLE_BLOCK_STATES_CACHE;
	}

	public static float getIsosurfaceLevel() {
		return (float) isosurfaceLevel;
	}

	public static float getoffsetAmount() {
		if (!offsetVertices) {
			return 0;
		}
		return (float) offsetAmmount;
	}

	public static MeshGenerator getMeshGenerator() {
		return meshGenerator;
	}

	@Mod.EventBusSubscriber(modid = MOD_ID)
	private static class ConfigEventSubscriber {

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {

			if (event.getModID().equals(MOD_ID)) {
				final boolean wasEnabled = NoCubes.isEnabled();
				ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);

				if ((wasEnabled || NoCubes.isEnabled()) && reloadChunksOnConfigChange) {
					ClientUtil.tryReloadRenderers();
				}

				rebuildSmoothableBlockstatesCache();

			}

		}

		private static void rebuildSmoothableBlockstatesCache() {
			SMOOTHABLE_BLOCK_STATES_CACHE.clear();

			final IForgeRegistry<Block> BLOCKS = ForgeRegistries.BLOCKS;

			for (String blockStateString : smoothableBlockStates) {

				try {
					final String[] splitBlockStateString = StringUtils.split(blockStateString, "[");
					final String blockString = splitBlockStateString[0];
					final String stateString;
					if (splitBlockStateString.length == 1) {
						stateString = "default";
					} else if (splitBlockStateString.length == 2) {
						stateString = StringUtils.reverse(StringUtils.reverse(StringUtils.split(blockStateString, "[")[1]).replaceFirst("]", ""));
					} else {
						NoCubes.NO_CUBES_LOG.error("Block/Blockstate Parsing error for \"" + blockStateString + "\"");
						continue;
					}

					final Block block = BLOCKS.getValue(new ResourceLocation(blockString));
					if (block == null) {
						NoCubes.NO_CUBES_LOG.error("Block Parsing error for \"" + blockString + "\". Block does not exist!");
						continue;
					}
					try {
						SMOOTHABLE_BLOCK_STATES_CACHE.add(CommandBase.convertArgToBlockState(block, stateString));
					} catch (NumberInvalidException e) {
						NoCubes.NO_CUBES_LOG.error("Blockstate Parsing error " + e + " for \"" + stateString + "\". Invalid Number!");
					} catch (InvalidBlockStateException e) {
						NoCubes.NO_CUBES_LOG.error("Blockstate Parsing error " + e + " for \"" + stateString + "\". Invalid Blockstate!");
					}
				} catch (Exception e) {
					NoCubes.NO_CUBES_LOG.error("Smoothable Blockstate Parsing error " + e + " for \"" + blockStateString + "\"");
				}

			}
		}

	}

	private static void setupSmoothableBlockStates() {
		final IBlockState[] defaultSmoothableBlockStates = new IBlockState[]{

				GRASS.getDefaultState().withProperty(BlockGrass.SNOWY, true),
				GRASS.getDefaultState().withProperty(BlockGrass.SNOWY, false),

				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE_SMOOTH),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE_SMOOTH),

				SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.SAND),
				SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND),

				SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, BlockSandStone.EnumType.DEFAULT),

				RED_SANDSTONE.getDefaultState().withProperty(BlockRedSandstone.TYPE, BlockRedSandstone.EnumType.DEFAULT),

				GRAVEL.getDefaultState(),

				COAL_ORE.getDefaultState(),
				IRON_ORE.getDefaultState(),
				GOLD_ORE.getDefaultState(),
				REDSTONE_ORE.getDefaultState(),
				LIT_REDSTONE_ORE.getDefaultState(),
				DIAMOND_ORE.getDefaultState(),
				LAPIS_ORE.getDefaultState(),
				EMERALD_ORE.getDefaultState(),
				QUARTZ_ORE.getDefaultState(),

				MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONE),
				MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.COBBLESTONE),
				MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.STONEBRICK),
				MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.MOSSY_STONEBRICK),
				MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.CRACKED_STONEBRICK),
				MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, BlockSilverfish.EnumType.CHISELED_STONEBRICK),

				GRASS_PATH.getDefaultState(),

				DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT),
				DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT),
				DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL),

				CLAY.getDefaultState(),
				HARDENED_CLAY.getDefaultState(),

				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, WHITE),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, ORANGE),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, YELLOW),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, SILVER),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, BROWN),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, RED),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, BLACK),

				SNOW.getDefaultState(),

				SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 1),
				SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 2),
				SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 3),
				SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 4),
				SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 5),
				SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 6),
				SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 7),
				SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 8),

				BEDROCK.getDefaultState(),

				NETHERRACK.getDefaultState(),
				SOUL_SAND.getDefaultState(),
				MAGMA.getDefaultState(),
				GLOWSTONE.getDefaultState(),

				END_STONE.getDefaultState(),

				MYCELIUM.getDefaultState().withProperty(BlockMycelium.SNOWY, true),
				MYCELIUM.getDefaultState().withProperty(BlockMycelium.SNOWY, false),

		};

		final ArrayList<String> tempSmoothableBlockStates = new ArrayList<>();

		for (IBlockState state : defaultSmoothableBlockStates) {
			SMOOTHABLE_BLOCK_STATES_CACHE.add(state);
			tempSmoothableBlockStates.add(state.toString());
		}

		smoothableBlockStates = tempSmoothableBlockStates.toArray(new String[0]);
	}

}
