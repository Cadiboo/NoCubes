package io.github.cadiboo.nocubes.config;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.mojang.brigadier.StringReader;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ExtendLiquidRange;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.SmoothLeavesLevel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockSnowLayer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static net.minecraft.block.BlockRedstoneOre.LIT;
import static net.minecraft.init.Blocks.ANDESITE;
import static net.minecraft.init.Blocks.BEDROCK;
import static net.minecraft.init.Blocks.BLACK_TERRACOTTA;
import static net.minecraft.init.Blocks.BROWN_TERRACOTTA;
import static net.minecraft.init.Blocks.CLAY;
import static net.minecraft.init.Blocks.COAL_ORE;
import static net.minecraft.init.Blocks.COARSE_DIRT;
import static net.minecraft.init.Blocks.DIAMOND_ORE;
import static net.minecraft.init.Blocks.DIORITE;
import static net.minecraft.init.Blocks.DIRT;
import static net.minecraft.init.Blocks.EMERALD_ORE;
import static net.minecraft.init.Blocks.END_STONE;
import static net.minecraft.init.Blocks.GLOWSTONE;
import static net.minecraft.init.Blocks.GOLD_ORE;
import static net.minecraft.init.Blocks.GRANITE;
import static net.minecraft.init.Blocks.GRASS;
import static net.minecraft.init.Blocks.GRASS_PATH;
import static net.minecraft.init.Blocks.GRAVEL;
import static net.minecraft.init.Blocks.GRAY_TERRACOTTA;
import static net.minecraft.init.Blocks.INFESTED_CHISELED_STONE_BRICKS;
import static net.minecraft.init.Blocks.INFESTED_COBBLESTONE;
import static net.minecraft.init.Blocks.INFESTED_CRACKED_STONE_BRICKS;
import static net.minecraft.init.Blocks.INFESTED_MOSSY_STONE_BRICKS;
import static net.minecraft.init.Blocks.INFESTED_STONE;
import static net.minecraft.init.Blocks.INFESTED_STONE_BRICKS;
import static net.minecraft.init.Blocks.IRON_ORE;
import static net.minecraft.init.Blocks.LAPIS_ORE;
import static net.minecraft.init.Blocks.MAGMA_BLOCK;
import static net.minecraft.init.Blocks.MYCELIUM;
import static net.minecraft.init.Blocks.NETHERRACK;
import static net.minecraft.init.Blocks.NETHER_QUARTZ_ORE;
import static net.minecraft.init.Blocks.ORANGE_TERRACOTTA;
import static net.minecraft.init.Blocks.PACKED_ICE;
import static net.minecraft.init.Blocks.PODZOL;
import static net.minecraft.init.Blocks.POLISHED_ANDESITE;
import static net.minecraft.init.Blocks.POLISHED_DIORITE;
import static net.minecraft.init.Blocks.POLISHED_GRANITE;
import static net.minecraft.init.Blocks.REDSTONE_ORE;
import static net.minecraft.init.Blocks.RED_SAND;
import static net.minecraft.init.Blocks.RED_SANDSTONE;
import static net.minecraft.init.Blocks.RED_TERRACOTTA;
import static net.minecraft.init.Blocks.SAND;
import static net.minecraft.init.Blocks.SANDSTONE;
import static net.minecraft.init.Blocks.SNOW;
import static net.minecraft.init.Blocks.SOUL_SAND;
import static net.minecraft.init.Blocks.STONE;
import static net.minecraft.init.Blocks.TERRACOTTA;
import static net.minecraft.init.Blocks.WHITE_TERRACOTTA;
import static net.minecraft.init.Blocks.YELLOW_TERRACOTTA;

/**
 * Our Mod's configuration
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
//@Config(modid = MOD_ID)
//@LangKey(MOD_ID + ".config.title")
public final class ModConfig {

	//	@Config.Ignore
	private static final transient HashSet<IBlockState> TERRAIN_SMOOTHABLE_BLOCK_STATES_CACHE = new HashSet<>();

	//	@Config.Ignore
	private static final transient HashSet<IBlockState> LEAVES_SMOOTHABLE_BLOCK_STATES_CACHE = new HashSet<>();

	//	@LangKey(MOD_ID + ".config.isEnabled")
	public static boolean isEnabled = true;

	//	@LangKey(MOD_ID + ".config.terrainMeshGenerator")
	public static MeshGenerator terrainMeshGenerator = MeshGenerator.SurfaceNets;

	//	@LangKey(MOD_ID + ".config.leavesMeshGenerator")
	public static MeshGenerator leavesMeshGenerator = MeshGenerator.SurfaceNets;

	//	@LangKey(MOD_ID + ".config.reloadChunksOnConfigChange")
	public static boolean reloadChunksOnConfigChange = true;

	//	@LangKey(MOD_ID + ".config.terrainSmoothableBlockStates")
	public static String[] terrainSmoothableBlockStates;

	//	@LangKey(MOD_ID + ".config.leavesSmoothableBlockStates")
	public static String[] leavesSmoothableBlockStates;

	@Beta
//	@LangKey(MOD_ID + ".config.offsetVertices")
//	@Config.Ignore
	public static boolean offsetVertices = false;

	//	@LangKey(MOD_ID + ".config.approximateLighting")
	public static boolean approximateLighting = true;

	//	@LangKey(MOD_ID + ".config.smoothLeavesLevel")
	public static SmoothLeavesLevel smoothLeavesLevel = SmoothLeavesLevel.TOGETHER;

	//	@LangKey(MOD_ID + ".config.extendLiquids")
	public static ExtendLiquidRange extendLiquids = ExtendLiquidRange.OneBlock;

	@Beta
//	@LangKey(MOD_ID + ".config.enableCollisions")
	public static boolean enableCollisions = true;

	@VisibleForTesting
//	@LangKey(MOD_ID + ".config.enablePools")
	public static boolean enablePools = true;

	//	@LangKey(MOD_ID + ".config.smoothBiomeColorTransitions")
	public static boolean smoothBiomeColorTransitions = true;

	//	@LangKey(MOD_ID + ".config.fluid") // Doesn't work for some reason
	public static FluidConfig fluidConfig = new FluidConfig();

	//	@LangKey(MOD_ID + ".config.overrideIsOpaqueCube")
	public static boolean overrideIsOpaqueCube = true;

	//TODO: remove
	@VisibleForTesting
//	@LangKey(MOD_ID + ".config.smoothOtherBlocksAmount")
	public static double smoothOtherBlocksAmount = 0;

	@Beta
//	@LangKey(MOD_ID + ".config.smoothBlockHighlighting")
	public static boolean smoothBlockHighlighting = false;

	@Beta
//	@LangKey(MOD_ID + ".config.collisionsBlockHighlighting")
	public static boolean collisionsBlockHighlighting = false;

	@Beta
//	@LangKey(MOD_ID + ".config.applyDiffuseLighting")
	public static boolean applyDiffuseLighting = false;

	@Beta
//	@LangKey(MOD_ID + ".config.collisionsForNullEntities")
	public static boolean collisionsForNullEntities = false;

	@Beta
//	@LangKey(MOD_ID + ".config.drawCollisionsCache")
	public static boolean drawCollisionsCache = false;

	@Beta
//	@LangKey(MOD_ID + ".config.shortGrassEnabled")
	public static boolean shortGrassEnabled = false;

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

	private static void setupTerrainSmoothableBlockStates() {
		final IBlockState[] defaultSmoothableBlockStates = new IBlockState[]{

				GRASS.getDefaultState().with(BlockGrass.SNOWY, true),
				GRASS.getDefaultState().with(BlockGrass.SNOWY, false),

				STONE.getDefaultState(),
				GRANITE.getDefaultState(),
				POLISHED_GRANITE.getDefaultState(),
				DIORITE.getDefaultState(),
				POLISHED_DIORITE.getDefaultState(),
				ANDESITE.getDefaultState(),
				POLISHED_ANDESITE.getDefaultState(),

				SAND.getDefaultState(),
				RED_SAND.getDefaultState(),

				SANDSTONE.getDefaultState(),

				RED_SANDSTONE.getDefaultState(),

				GRAVEL.getDefaultState(),

				COAL_ORE.getDefaultState(),
				IRON_ORE.getDefaultState(),
				GOLD_ORE.getDefaultState(),
				REDSTONE_ORE.getDefaultState().with(LIT, false),
				REDSTONE_ORE.getDefaultState().with(LIT, true),
				DIAMOND_ORE.getDefaultState(),
				LAPIS_ORE.getDefaultState(),
				EMERALD_ORE.getDefaultState(),
				NETHER_QUARTZ_ORE.getDefaultState(),

				INFESTED_STONE.getDefaultState(),
				INFESTED_COBBLESTONE.getDefaultState(),
				INFESTED_STONE_BRICKS.getDefaultState(),
				INFESTED_MOSSY_STONE_BRICKS.getDefaultState(),
				INFESTED_CRACKED_STONE_BRICKS.getDefaultState(),
				INFESTED_CHISELED_STONE_BRICKS.getDefaultState(),

				GRASS_PATH.getDefaultState(),

				DIRT.getDefaultState(),
				COARSE_DIRT.getDefaultState(),
				PODZOL.getDefaultState(),

				CLAY.getDefaultState(),
				TERRACOTTA.getDefaultState(),

				WHITE_TERRACOTTA.getDefaultState(),
				ORANGE_TERRACOTTA.getDefaultState(),
				YELLOW_TERRACOTTA.getDefaultState(),
				GRAY_TERRACOTTA.getDefaultState(),
				BROWN_TERRACOTTA.getDefaultState(),
				RED_TERRACOTTA.getDefaultState(),
				BLACK_TERRACOTTA.getDefaultState(),

				PACKED_ICE.getDefaultState(),

				SNOW.getDefaultState(),

				SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, 1),
				SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, 2),
				SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, 3),
				SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, 4),
				SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, 5),
				SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, 6),
				SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, 7),
				SNOW.getDefaultState().with(BlockSnowLayer.LAYERS, 8),

				BEDROCK.getDefaultState(),

				NETHERRACK.getDefaultState(),
				SOUL_SAND.getDefaultState(),
				MAGMA_BLOCK.getDefaultState(),
				GLOWSTONE.getDefaultState(),

				END_STONE.getDefaultState(),

				MYCELIUM.getDefaultState().with(BlockMycelium.SNOWY, true),
				MYCELIUM.getDefaultState().with(BlockMycelium.SNOWY, false),

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
				for (final IBlockState state : block.getStateContainer().getValidStates()) {
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

	@Mod.EventBusSubscriber(modid = MOD_ID)
	private static class ConfigEventSubscriber {

		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {

//			if (event.getModID().equals(MOD_ID)) {
//				final boolean wasEnabled = NoCubes.isEnabled();
//				ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
//
//				if ((wasEnabled || NoCubes.isEnabled()) && reloadChunksOnConfigChange) {
//					ClientUtil.tryReloadRenderers();
//				}
//
//				rebuildTerrainSmoothableBlockStatesCache();
//				rebuildLeavesSmoothableBlockStatesCache();
//
//			}

		}

		private static void rebuildTerrainSmoothableBlockStatesCache() {
			parseBlockStatesToCache(TERRAIN_SMOOTHABLE_BLOCK_STATES_CACHE, terrainSmoothableBlockStates);
		}

		private static void rebuildLeavesSmoothableBlockStatesCache() {
			parseBlockStatesToCache(LEAVES_SMOOTHABLE_BLOCK_STATES_CACHE, leavesSmoothableBlockStates);
		}

		private static void parseBlockStatesToCache(@Nonnull final HashSet<IBlockState> cache, @Nonnull final String[] blockStateStrings) {

			cache.clear();

			for (String blockStateString : blockStateStrings) {

				try {
					cache.add(
							Objects.requireNonNull(
									new BlockStateParser(new StringReader(blockStateString), false).parse(false).getState(),
									"Parsed state was null!"
							)
					);
				} catch (Exception e) {
					NoCubes.NO_CUBES_LOG.error("Smoothable BlockState Parsing error " + e + " for \"" + blockStateString + "\"");
				}

			}
		}

	}

}
