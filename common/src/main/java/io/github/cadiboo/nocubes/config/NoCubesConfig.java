package io.github.cadiboo.nocubes.config;

import com.google.common.collect.Sets;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.mesh.*;
import io.github.cadiboo.nocubes.util.BlockStateSerializer;
import io.github.cadiboo.nocubes.util.ModUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.Blocks.*;

/**
 * The Config for NoCubes.
 * Contains the Common, Client and Sever configs as inner classes.
 *
 * @author Cadiboo
 */
public final class NoCubesConfig {

	/**
	 * Settings that effect the app state regardless of if it is a client or server.
	 */
	public static class Common {
		public static boolean debugEnabled;
	}

	/**
	 * Settings that effect the client state i.e. rendering options.
	 */
	public static class Client {
		public static final String INFO_MESSAGE = "infoMessage";
		public static String RENDER = "render";

		public static boolean infoMessage;
		public static boolean render;
		public static boolean renderSelectionBox;
		public static Color selectionBoxColor;
		public static boolean betterGrassSides;
		public static boolean moreSnow;
		public static boolean fixPlantHeight;
		public static boolean grassTufts;

		public static boolean debugOutlineSmoothables;
		public static boolean debugVisualiseDensitiesGrid;
		public static boolean debugRenderCollisions;
		public static boolean debugRenderMeshCollisions;
		public static boolean debugRecordMeshPerformance;
		public static boolean debugOutlineNearbyMesh;
		public static boolean debugSkipNoCubesRendering;
	}

	/**
	 * Settings that effect the server state i.e. gameplay options.
	 */
	public static class Server {

		public static Mesher mesher;
		public static boolean collisionsEnabled;
		public static boolean tempMobCollisionsDisabled;
		public static int oldStyleCollisionsEnhancementLevel;
		public static boolean onlyOldStyleCollisions;
		public static boolean forceVisuals;
		public static int extendFluidsRange;
		public static boolean oldNoCubesSlopes;
		public static boolean oldNoCubesInFluids;
		public static float oldNoCubesRoughness;

		public enum MesherType {
			SurfaceNets(new SurfaceNets(false)),
			OldNoCubes(new OldNoCubes()),
			Debug_SurfaceNets2xSmoothness(new SurfaceNets(true)),
			Debug_MarchingCubes(new MarchingCubes(false)),
			Debug_MarchingCubes2xSmoothness(new MarchingCubes(true)),
			Debug_CullingCubic(new CullingCubic()),
			Debug_StupidCubic(new StupidCubic()),
			Debug_CullingChamfer(new CullingChamfer()),
			Debug_WulferisMesher(new WulferisMesher()),
			;

			public final Mesher instance;

			MesherType(Mesher instance) {
				this.instance = instance;
			}
		}
	}

	public static class Smoothables {

		/**
		 * Stores the list of blocks that 'just are' smoothable by default.
		 * This includes stuff like Stone and any blocks that other mods register as smoothable.
		 */
		private static final Set<BlockState> DEFAULT_SMOOTHABLES = Sets.newIdentityHashSet();

		private static final Logger LOG = LogManager.getLogger();

		static {
			// The minecraft wiki is a useful resource https://minecraft.fandom.com/wiki/Category:Natural_blocks
			// TODO: This should include carpet-like blocks (moss, skulk, vine) once rendering issues have been fixed
			// Add all possible BlockStates for these blocks
			DEFAULT_SMOOTHABLES.addAll(Arrays.stream(new Block[]{
				STONE, GRANITE, DIORITE, ANDESITE,
				GRASS_BLOCK, DIRT, COARSE_DIRT, PODZOL, MYCELIUM,
				DEEPSLATE, ROOTED_DIRT, TUFF, CALCITE, SMOOTH_BASALT, AMETHYST_BLOCK, BUDDING_AMETHYST,
				BEDROCK,
				SAND, RED_SAND,
				SANDSTONE, RED_SANDSTONE,
				GRAVEL,
				COAL_ORE, IRON_ORE, COPPER_ORE, GOLD_ORE, REDSTONE_ORE, DIAMOND_ORE, LAPIS_ORE, EMERALD_ORE, NETHER_QUARTZ_ORE, NETHER_GOLD_ORE,
				RAW_IRON_BLOCK, RAW_COPPER_BLOCK, RAW_GOLD_BLOCK,
				DEEPSLATE_COAL_ORE, DEEPSLATE_IRON_ORE, DEEPSLATE_COPPER_ORE, DEEPSLATE_GOLD_ORE, DEEPSLATE_REDSTONE_ORE, DEEPSLATE_DIAMOND_ORE, DEEPSLATE_LAPIS_ORE, DEEPSLATE_EMERALD_ORE,
				INFESTED_STONE, INFESTED_DEEPSLATE,
				BONE_BLOCK,
				DIRT_PATH,
				CLAY, TERRACOTTA, WHITE_TERRACOTTA, ORANGE_TERRACOTTA, MAGENTA_TERRACOTTA, LIGHT_BLUE_TERRACOTTA, YELLOW_TERRACOTTA, LIME_TERRACOTTA, PINK_TERRACOTTA, GRAY_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, CYAN_TERRACOTTA, PURPLE_TERRACOTTA, BLUE_TERRACOTTA, BROWN_TERRACOTTA, GREEN_TERRACOTTA, RED_TERRACOTTA, BLACK_TERRACOTTA,
				SNOW, SNOW_BLOCK, ICE, PACKED_ICE, FROSTED_ICE,
				NETHERRACK, SOUL_SAND, SOUL_SOIL, BASALT, MAGMA_BLOCK, GLOWSTONE, NETHER_WART_BLOCK, CRIMSON_STEM, WARPED_NYLIUM, WARPED_WART_BLOCK, WARPED_STEM, BLACKSTONE,
				END_STONE,
				MOSS_BLOCK, SCULK,
				OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
				OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES, DARK_OAK_LEAVES,
			}).flatMap(block -> ModUtil.getStates(block).stream()).collect(Collectors.toList()));

			// Add each of these individual BlockStates
			DEFAULT_SMOOTHABLES.addAll(Arrays.stream(new BlockState[]{
			}).collect(Collectors.toList()));

			// Add these modded BlockStates
			//noinspection RedundantArrayCreation
			DEFAULT_SMOOTHABLES.addAll(parseBlockStates(Arrays.asList(new String[]{
				"biomesoplenty:grass[snowy=false,variant=sandy]",
				"biomesoplenty:dirt[coarse=false,variant=sandy]",
				"biomesoplenty:white_sand",
				"biomesoplenty:grass[snowy=false,variant=silty]",
				"biomesoplenty:dirt[coarse=false,variant=loamy]",
				"biomesoplenty:grass[snowy=false,variant=loamy]",
				"biomesoplenty:dried_sand",
				"biomesoplenty:hard_ice",
				"biomesoplenty:mud[variant=mud]",
				"biomesoplenty:dirt[coarse=false,variant=silty]",
				"chisel:marble2[variation=7]",
				"chisel:limestone2[variation=7]",
				"dynamictrees:rootydirtspecies[life=0]",
				"dynamictrees:rootysand[life=0]",
				"iceandfire:ash",
				"iceandfire:sapphire_ore",
				"iceandfire:chared_grass",
				"iceandfire:chared_stone",
				"iceandfire:frozen_grass_path",
				"notenoughroofs:copper_ore",
				"rustic:slate",
			})));
		}

		public static void updateUserDefinedSmoothableStringLists(boolean newValue, BlockState[] states, List<String> whitelist, List<String> blacklist) {
			LOG.debug("Updating user-defined smoothable string lists");
			var toAddTo = newValue ? whitelist : blacklist;
			var toRemoveFrom = newValue ? blacklist : whitelist;
			for (var state : states) {
				var string = BlockStateSerializer.toString(state);
				NoCubes.smoothableHandler.setSmoothable(newValue, state);
				if (!toAddTo.contains(string))
					toAddTo.add(string);
				//noinspection StatementWithEmptyBody
				while (toRemoveFrom.remove(string))
					// The loop runs until there are no more occurrences of 'string' in the list
					;
			}
		}

		public static void recomputeInMemoryLookup(Stream<Block> blocks, List<? extends String> whitelist, List<? extends String> blacklist, boolean useDefaultSmoothables) {
			LOG.debug("Recomputing in-memory smoothable lookups from user-defined smoothable string lists");
			var whitelisted = parseBlockStates(whitelist);
			var blacklisted = parseBlockStates(blacklist);
			blocks.parallel()
				.flatMap(block -> ModUtil.getStates(block).parallelStream())
				.forEach(state -> {
					var smoothable = (whitelisted.contains(state) || (useDefaultSmoothables && Smoothables.DEFAULT_SMOOTHABLES.contains(state))) && !blacklisted.contains(state);
					if (Common.debugEnabled && NoCubes.smoothableHandler.isSmoothable(state) != smoothable)
						LOG.debug(() -> "Updating smoothness of %s to %b".formatted(state, smoothable));
					NoCubes.smoothableHandler.setSmoothable(smoothable, state);
				});
		}

		static Set<BlockState> parseBlockStates(List<? extends String> list) {
			var set = Sets.<BlockState>newIdentityHashSet();
			list.stream()
				.map(BlockStateSerializer::fromStringOrNull)
				.filter(Objects::nonNull)
				.forEach(set::add);
			return set;
		}

		public static void addDefault(BlockState... states) {
			DEFAULT_SMOOTHABLES.addAll(Arrays.asList(states));
		}
	}

}
