package cadiboo.nocubes.config;

import cadiboo.nocubes.util.ModEnums.RenderAlgorithm;
import cadiboo.nocubes.util.ModReference;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Blocks;
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

@Config(modid = ModReference.MOD_ID)
@LangKey(ModReference.MOD_ID + ".config.title")
public class ModConfig {

	@LangKey(ModReference.MOD_ID + ".config.enabled")
	public static boolean isEnabled = true;

	@LangKey(ModReference.MOD_ID + ".config.algorithm")
	public static RenderAlgorithm activeRenderingAlgorithm = RenderAlgorithm.MARCHING_CUBES;

	@LangKey(ModReference.MOD_ID + ".config.forcechunkreload")
	public static boolean shouldForceChunkReload = true;

	@LangKey(ModReference.MOD_ID + ".config.fixcullfacing")
	public static boolean shouldFixCullFacing = true;

	@LangKey(ModReference.MOD_ID + ".config.smoothliquids")
	public static boolean shouldSmoothLiquids = false;

	@LangKey(ModReference.MOD_ID + ".config.drawwireframe.comment")
	public static boolean shouldDrawWireframe = false;

	@LangKey(ModReference.MOD_ID + ".config.approximatelighting")
	public static boolean shouldAproximateLighting = true;

	@LangKey(ModReference.MOD_ID + ".config.smoothableblockstates")
	public static String[] smoothableBlockStates;

	@Config.Ignore
	private static final HashSet<IBlockState> FAST_SMOOTHABLE_BLOCK_STATES = new HashSet<>();

	static {

		final IBlockState[] defaultSmoothableBlockStates = new IBlockState[] {

			Blocks.GRASS.getDefaultState().withProperty(BlockGrass.SNOWY, false), Blocks.GRASS.getDefaultState().withProperty(BlockGrass.SNOWY, true),

			Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE_SMOOTH), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH),
			Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE), Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE_SMOOTH),

			Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, net.minecraft.block.BlockSand.EnumType.SAND), Blocks.SAND.getDefaultState().withProperty(BlockSand.VARIANT, net.minecraft.block.BlockSand.EnumType.RED_SAND),

			Blocks.SANDSTONE.getDefaultState().withProperty(BlockSandStone.TYPE, net.minecraft.block.BlockSandStone.EnumType.DEFAULT), Blocks.RED_SANDSTONE.getDefaultState().withProperty(BlockRedSandstone.TYPE, net.minecraft.block.BlockRedSandstone.EnumType.DEFAULT),

			Blocks.GRAVEL.getDefaultState(),

			Blocks.COAL_ORE.getDefaultState(), Blocks.IRON_ORE.getDefaultState(), Blocks.GOLD_ORE.getDefaultState(), Blocks.REDSTONE_ORE.getDefaultState(), Blocks.LIT_REDSTONE_ORE.getDefaultState(), Blocks.DIAMOND_ORE.getDefaultState(), Blocks.EMERALD_ORE.getDefaultState(), Blocks.QUARTZ_ORE.getDefaultState(),

			Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.STONE), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.COBBLESTONE), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.STONEBRICK),
			Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.MOSSY_STONEBRICK), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.CRACKED_STONEBRICK), Blocks.MONSTER_EGG.getDefaultState().withProperty(BlockSilverfish.VARIANT, net.minecraft.block.BlockSilverfish.EnumType.CHISELED_STONEBRICK),

			Blocks.GRASS_PATH.getDefaultState(), Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT), Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT), Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL),

			Blocks.CLAY.getDefaultState(), Blocks.HARDENED_CLAY.getDefaultState(),

			Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.WHITE), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.ORANGE), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.MAGENTA), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.LIGHT_BLUE),
			Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.YELLOW), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.LIME), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.PINK), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.GRAY),
			Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.SILVER), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.CYAN), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.PURPLE), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BLUE),
			Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BROWN), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.GREEN), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.RED), Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR, EnumDyeColor.BLACK),

			Blocks.SNOW.getDefaultState(),

			Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 1), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 2), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 3), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 4), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 5), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 6),
			Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 7), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, 8),

			Blocks.BEDROCK.getDefaultState(),

			Blocks.NETHERRACK.getDefaultState(), Blocks.GLOWSTONE.getDefaultState(),

			Blocks.END_STONE.getDefaultState(),

			Blocks.MYCELIUM.getDefaultState().withProperty(BlockMycelium.SNOWY, false), Blocks.MYCELIUM.getDefaultState().withProperty(BlockMycelium.SNOWY, true),

		};

		final ArrayList<String> tempSmoothableBlockStates = new ArrayList<>();

		for (IBlockState state : defaultSmoothableBlockStates) {
			FAST_SMOOTHABLE_BLOCK_STATES.add(state);
			tempSmoothableBlockStates.add(state.toString());
		}

		smoothableBlockStates = tempSmoothableBlockStates.toArray(new String[0]);

	}

	public static HashSet<IBlockState> getFastSmoothableBlockStates() {

		return FAST_SMOOTHABLE_BLOCK_STATES;
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
				FAST_SMOOTHABLE_BLOCK_STATES.clear();

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
						FAST_SMOOTHABLE_BLOCK_STATES.add(CommandBase.convertArgToBlockState(block, stateString));
					} catch (NumberInvalidException | InvalidBlockStateException e) {
						e.printStackTrace();
					}

				}

			}

		}

	}

}
