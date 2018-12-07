package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.util.ModEnums.EffortLevel;
import io.github.cadiboo.nocubes.util.ModEnums.RenderAlgorithm;
import io.github.cadiboo.nocubes.util.ModReference;
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
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.LangKey;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;

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
import static net.minecraft.init.Blocks.LIT_REDSTONE_ORE;
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
import static net.minecraft.init.Blocks.STAINED_HARDENED_CLAY;
import static net.minecraft.init.Blocks.STONE;

@Config(modid = ModReference.MOD_ID)
@LangKey(ModReference.MOD_ID + ".config.title")
public class ModConfig {

	@Config.Ignore
	private static final HashSet<IBlockState> SMOOTHABLE_BLOCK_STATES_CACHE = new HashSet<>();
	@LangKey(ModReference.MOD_ID + ".config.isEnabled")
	public static boolean isEnabled = true;
	@LangKey(ModReference.MOD_ID + ".config.activeRenderingAlgorithm")
	public static RenderAlgorithm activeRenderingAlgorithm = RenderAlgorithm.OLD_NO_CUBES;
	@LangKey(ModReference.MOD_ID + ".config.reloadChunksOnConfigChange")
	public static boolean reloadChunksOnConfigChange = true;
	@LangKey(ModReference.MOD_ID + ".config.shouldFixFaceCulling")
	public static boolean shouldFixFaceCulling = true;
	@LangKey(ModReference.MOD_ID + ".config.shouldSmoothLiquids")
	public static boolean shouldSmoothLiquids = false;
	@LangKey(ModReference.MOD_ID + ".config.shouldDrawWireframe")
	public static boolean shouldDrawWireframe = false;
	@LangKey(ModReference.MOD_ID + ".config.approximateLightingLevel")
	public static EffortLevel approximateLightingLevel = EffortLevel.FAST;
	@LangKey(ModReference.MOD_ID + ".config.smoothableBlockStates")
	public static String[] smoothableBlockStates;
	@LangKey(ModReference.MOD_ID + ".config.cacheQuads")
	public static boolean cacheQuads;
	@LangKey(ModReference.MOD_ID + ".config.isosurfaceLevel")
	@Config.RangeDouble(min = -5, max = 5)
	public static double isosurfaceLevel = 0.5d;
	@LangKey(ModReference.MOD_ID + ".config.betterFoliageGrassCompatibility")
	public static boolean betterFoliageGrassCompatibility;
	static {

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

				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.WHITE),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.ORANGE),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.YELLOW),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.SILVER),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BROWN),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.RED),
				STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BLACK),

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

	public static HashSet<IBlockState> getSmoothableBlockStatesCache() {
		return SMOOTHABLE_BLOCK_STATES_CACHE;
	}

	public static float getIsosurfaceLevel() {
		return (float) isosurfaceLevel;
	}

	@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
	private static class EventSubscriber {

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {

			if (event.getModID().equals(ModReference.MOD_ID)) {
				ConfigManager.sync(ModReference.MOD_ID, Config.Type.INSTANCE);

				if (reloadChunksOnConfigChange) {
					if (Minecraft.getMinecraft().renderGlobal != null) {
						Minecraft.getMinecraft().renderGlobal.loadRenderers();
					}
				}

				SMOOTHABLE_BLOCK_STATES_CACHE.clear();

				for (String blockStateString : smoothableBlockStates) {
					final String[] splitBlockStateString = StringUtils.split(blockStateString, "[");
					final String blockString = splitBlockStateString[0];
					final String stateString;
					if (splitBlockStateString.length == 1) {
						stateString = "default";
					} else {
						stateString = StringUtils.reverse(StringUtils.reverse(StringUtils.split(blockStateString, "[")[1]).replaceFirst("]", ""));
					}
					final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockString));

					try {
						SMOOTHABLE_BLOCK_STATES_CACHE.add(CommandBase.convertArgToBlockState(block, stateString));
					} catch (NumberInvalidException | InvalidBlockStateException e) {
						e.printStackTrace();
					}

				}

			}

		}

	}

}
