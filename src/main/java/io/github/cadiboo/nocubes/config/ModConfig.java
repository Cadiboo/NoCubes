package io.github.cadiboo.nocubes.config;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.ExtendLiquidRange;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.SmoothLeavesLevel;
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
import net.minecraft.block.material.Material;
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

import javax.annotation.Nonnull;
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
import static net.minecraft.init.Blocks.PACKED_ICE;
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
	private static final transient HashSet<IBlockState> TERRAIN_SMOOTHABLE_BLOCK_STATES_CACHE = new HashSet<>();

	@Config.Ignore
	private static final transient HashSet<IBlockState> LEAVES_SMOOTHABLE_BLOCK_STATES_CACHE = new HashSet<>();

	@LangKey(MOD_ID + ".config.isEnabled")
	public static boolean isEnabled = true;

	@LangKey(MOD_ID + ".config.terrainMeshGenerator")
	public static MeshGenerator terrainMeshGenerator = MeshGenerator.SurfaceNets;

	@LangKey(MOD_ID + ".config.leavesMeshGenerator")
	public static MeshGenerator leavesMeshGenerator = MeshGenerator.SurfaceNets;

	@LangKey(MOD_ID + ".config.reloadChunksOnConfigChange")
	public static boolean reloadChunksOnConfigChange = true;

	@LangKey(MOD_ID + ".config.terrainSmoothableBlockStates")
	public static String[] terrainSmoothableBlockStates;

	@LangKey(MOD_ID + ".config.leavesSmoothableBlockStates")
	public static String[] leavesSmoothableBlockStates;

	@Beta
	@LangKey(MOD_ID + ".config.offsetVertices")
//	@Config.Ignore
	public static boolean offsetVertices = false;

	@LangKey(MOD_ID + ".config.approximateLighting")
	public static boolean approximateLighting = true;

	@LangKey(MOD_ID + ".config.smoothLeavesLevel")
	public static SmoothLeavesLevel smoothLeavesLevel = SmoothLeavesLevel.TOGETHER;

	@LangKey(MOD_ID + ".config.extendLiquids")
	public static ExtendLiquidRange extendLiquids = ExtendLiquidRange.OneBlock;

	@Beta
	@LangKey(MOD_ID + ".config.enableCollisions")
	public static boolean enableCollisions = true;

	@VisibleForTesting
	@LangKey(MOD_ID + ".config.enablePools")
	public static boolean enablePools = true;

	@LangKey(MOD_ID + ".config.smoothBiomeColorTransitions")
	public static boolean smoothBiomeColorTransitions = true;

	@LangKey(MOD_ID + ".config.fluid") // Doesn't work for some reason
	public static FluidConfig fluidConfig = new FluidConfig();

	@LangKey(MOD_ID + ".config.overrideIsOpaqueCube")
	public static boolean overrideIsOpaqueCube = true;

	@VisibleForTesting
	@LangKey(MOD_ID + ".config.smoothOtherBlocksAmount")
	public static double smoothOtherBlocksAmount = 0;

	@Beta
	@LangKey(MOD_ID + ".config.smoothBlockHighlighting")
	public static boolean smoothBlockHighlighting = false;

	@Beta
	@LangKey(MOD_ID + ".config.collisionsBlockHighlighting")
	public static boolean collisionsBlockHighlighting = false;

	@Beta
	@LangKey(MOD_ID + ".config.applyDiffuseLighting")
	public static boolean applyDiffuseLighting = false;

	static {
		setupTerrainSmoothableBlockStates();
		setupLeavesSmoothableBlockStates();
	}

	public static HashSet<IBlockState> getTerrainSmoothableBlockStatesCache() {
		return TERRAIN_SMOOTHABLE_BLOCK_STATES_CACHE;
	}

	//FIXME TODO predicates that ignore check_decay and decayable
	public static HashSet<IBlockState> getLeavesSmoothableBlockStatesCache() {
		return LEAVES_SMOOTHABLE_BLOCK_STATES_CACHE;
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

				rebuildTerrainSmoothableBlockStatesCache();
				rebuildLeavesSmoothableBlockStatesCache();

			}

		}

		private static void rebuildTerrainSmoothableBlockStatesCache() {
			parseBlockStatesToCache(TERRAIN_SMOOTHABLE_BLOCK_STATES_CACHE, terrainSmoothableBlockStates);
		}

		private static void rebuildLeavesSmoothableBlockStatesCache() {
			parseBlockStatesToCache(LEAVES_SMOOTHABLE_BLOCK_STATES_CACHE, leavesSmoothableBlockStates);
		}

		private static void parseBlockStatesToCache(@Nonnull final HashSet<IBlockState> cache, @Nonnull final String[] blockStateStrings) {

			final IForgeRegistry<Block> BLOCKS = ForgeRegistries.BLOCKS;

			cache.clear();

			for (String blockStateString : blockStateStrings) {

				try {
					final String[] splitBlockStateString = StringUtils.split(blockStateString, "[");
					final String blockString = splitBlockStateString[0];
					final String stateString;
					if (splitBlockStateString.length == 1) {
						stateString = "default";
					} else if (splitBlockStateString.length == 2) {
						stateString = StringUtils.reverse(StringUtils.reverse(StringUtils.split(blockStateString, "[")[1]).replaceFirst("]", ""));
					} else {
						NoCubes.NO_CUBES_LOG.error("Block/BlockState Parsing error for \"" + blockStateString + "\"");
						continue;
					}

					final Block block = BLOCKS.getValue(new ResourceLocation(blockString));
					if (block == null) {
						NoCubes.NO_CUBES_LOG.error("Block Parsing error for \"" + blockString + "\". Block does not exist!");
						continue;
					}
					try {
						cache.add(CommandBase.convertArgToBlockState(block, stateString));
					} catch (NumberInvalidException e) {
						NoCubes.NO_CUBES_LOG.error("BlockState Parsing error " + e + " for \"" + stateString + "\". Invalid Number!");
					} catch (InvalidBlockStateException e) {
						NoCubes.NO_CUBES_LOG.error("BlockState Parsing error " + e + " for \"" + stateString + "\". Invalid BlockState!");
					}
				} catch (Exception e) {
					NoCubes.NO_CUBES_LOG.error("Smoothable BlockState Parsing error " + e + " for \"" + blockStateString + "\"");
				}

			}
		}

	}

	private static void setupTerrainSmoothableBlockStates() {
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

				PACKED_ICE.getDefaultState(),

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
			TERRAIN_SMOOTHABLE_BLOCK_STATES_CACHE.add(state);
			tempSmoothableBlockStates.add(state.toString());
		}

		terrainSmoothableBlockStates = tempSmoothableBlockStates.toArray(new String[0]);
	}

	private static void setupLeavesSmoothableBlockStates() {

		final ArrayList<IBlockState> defaultSmoothableBlockStates = new ArrayList<>();

		try (final ModProfiler ignored = NoCubes.getProfiler().start("setupLeavesSmoothableBlockStates")) {
			for (final Block block : ForgeRegistries.BLOCKS.getValues()) {
				for (final IBlockState state : block.getBlockState().getValidStates()) {
					if (state.getMaterial() == Material.LEAVES) {
						defaultSmoothableBlockStates.add(state);
					}
				}
			}
		}

		final ArrayList<String> tempSmoothableBlockStates = new ArrayList<>();

		for (IBlockState state : defaultSmoothableBlockStates) {
			LEAVES_SMOOTHABLE_BLOCK_STATES_CACHE.add(state);
			tempSmoothableBlockStates.add(state.toString());
		}

		leavesSmoothableBlockStates = tempSmoothableBlockStates.toArray(new String[0]);
	}

	//	@LangKey(MOD_ID + ".config.fluid")  // Doesn't work for some reason
	public static class FluidConfig {

		//		@LangKey(MOD_ID + ".config.fluid.smoothFluidBiomeColorTransitions") // Doesn't work for some reason
		public boolean smoothFluidBiomeColorTransitions = true;

		//		@LangKey(MOD_ID + ".config.fluid.smoothFluidLighting") // Doesn't work for some reason
		public boolean smoothFluidLighting = true;

		//		@LangKey(MOD_ID + ".config.fluid.naturalFluidTextures") // Doesn't work for some reason
		public boolean naturalFluidTextures = false;

		public static boolean areSmoothFluidBiomeColorTransitionsEnabled() {
			return fluidConfig.smoothFluidBiomeColorTransitions;
		}

		public static boolean isSmoothFluidLightingEnabled() {
			return fluidConfig.smoothFluidLighting;
		}

		public static boolean areNaturalFluidTexturesEnabled() {
			return fluidConfig.naturalFluidTextures;
		}

	}

}
