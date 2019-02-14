package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ExtendLiquidRange;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.GrassBlock;
import net.minecraft.block.MyceliumBlock;
import net.minecraft.block.SnowBlock;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static net.minecraft.block.Blocks.BEDROCK;
import static net.minecraft.block.Blocks.CLAY;
import static net.minecraft.block.Blocks.COAL_ORE;
import static net.minecraft.block.Blocks.COARSE_DIRT;
import static net.minecraft.block.Blocks.DIAMOND_ORE;
import static net.minecraft.block.Blocks.DIRT;
import static net.minecraft.block.Blocks.EMERALD_ORE;
import static net.minecraft.block.Blocks.END_STONE;
import static net.minecraft.block.Blocks.GLOWSTONE;
import static net.minecraft.block.Blocks.GOLD_ORE;
import static net.minecraft.block.Blocks.GRASS;
import static net.minecraft.block.Blocks.GRASS_PATH;
import static net.minecraft.block.Blocks.GRAVEL;
import static net.minecraft.block.Blocks.INFESTED_CHISELED_STONE_BRICKS;
import static net.minecraft.block.Blocks.INFESTED_COBBLESTONE;
import static net.minecraft.block.Blocks.INFESTED_CRACKED_STONE_BRICKS;
import static net.minecraft.block.Blocks.*;
import static net.minecraft.block.Blocks.INFESTED_STONE;
import static net.minecraft.block.Blocks.INFESTED_STONE_BRICKS;
import static net.minecraft.block.Blocks.IRON_ORE;
import static net.minecraft.block.Blocks.LAPIS_ORE;
import static net.minecraft.block.Blocks.MYCELIUM;
import static net.minecraft.block.Blocks.NETHERRACK;
import static net.minecraft.block.Blocks.NETHER_QUARTZ_ORE;
import static net.minecraft.block.Blocks.PODZOL;
import static net.minecraft.block.Blocks.REDSTONE_ORE;
import static net.minecraft.block.Blocks.RED_SANDSTONE;
import static net.minecraft.block.Blocks.SAND;
import static net.minecraft.block.Blocks.SANDSTONE;
import static net.minecraft.block.Blocks.SNOW;
import static net.minecraft.block.Blocks.SOUL_SAND;
import static net.minecraft.block.Blocks.STONE;
import static net.minecraft.block.Blocks.TERRACOTTA;
import static net.minecraft.util.DyeColor.ORANGE;
import static net.minecraft.util.DyeColor.*;

/**
 * Our Mod's configuration
 *
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
//@Config(modid = MOD_ID)
//@LangKey(MOD_ID + ".config.title")
public final class ModConfig {

	//@Config.Ignore
	private static final transient HashSet<BlockState> SMOOTHABLE_BLOCK_STATES_CACHE = new HashSet<>();

	//@LangKey(MOD_ID + ".config.isEnabled")
	public static boolean isEnabled = true;

	//@LangKey(MOD_ID + ".config.meshGenerator")
	public static MeshGenerator meshGenerator = MeshGenerator.SurfaceNets;

	//@LangKey(MOD_ID + ".config.reloadChunksOnConfigChange")
	public static boolean reloadChunksOnConfigChange = true;

	//@LangKey(MOD_ID + ".config.smoothableBlockStates")
	public static String[] smoothableBlockStates;

	//	//@LangKey(MOD_ID + ".config.isosurfaceLevel")
//	//@Config.RangeDouble(min = -10, max = 10)
	//@Config.Ignore
	public static double isosurfaceLevel = 1.0D;

	//	//@LangKey(MOD_ID + ".config.offsetVertices")
	//@Config.Ignore
	public static boolean offsetVertices = false;

	//	//@LangKey(MOD_ID + ".config.offsetAmmount")
//	//@Config.RangeDouble(min = -10, max = 10)
	//@Config.Ignore
	public static double offsetAmmount = 0.5F;

	//@LangKey(MOD_ID + ".config.approximateLighting")
	public static boolean approximateLighting = true;

	//@LangKey(MOD_ID + ".config.smoothLeavesSeparate")
	public static boolean smoothLeavesSeparate = true;

	//@LangKey(MOD_ID + ".config.extendLiquids")
	public static ExtendLiquidRange extendLiquids = ExtendLiquidRange.OneBlock;

	//@Config.Ignore
	//yenah i dont like it //TODO: remove
	public static boolean renderEmptyBlocksOrWhatever = false;

	//@Config.Ignore
//	//@Config.RangeDouble(min = -10, max = 10)
	public static double smoothOtherBlocksAmount = 0.0F;

	static {
		setupSmoothableBlockStates();
	}

	public static HashSet<BlockState> getSmoothableBlockStatesCache() {
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

//	@Mod.EventBusSubscriber(modid = MOD_ID)
//	private static class ConfigEventSubscriber {
//
//		/**
//		 * Inject the new values and save to the config file when the config has been changed from the GUI.
//		 *
//		 * @param event The event
//		 */
//		@SubscribeEvent
//		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
//
//			if (event.getModID().equals(MOD_ID)) {
//				ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
//
//				if (reloadChunksOnConfigChange) {
//					if (Minecraft.getMinecraft().renderGlobal != null) {
//						Minecraft.getMinecraft().renderGlobal.loadRenderers();
//					}
//				}
//
//				SMOOTHABLE_BLOCK_STATES_CACHE.clear();
//
//				final IForgeRegistry<Block> BLOCKS = ForgeRegistries.BLOCKS;
//
//				for (String blockStateString : smoothableBlockStates) {
//					final String[] splitBlockStateString = StringUtils.split(blockStateString, "[");
//					final String blockString = splitBlockStateString[0];
//					final String stateString;
//					if (splitBlockStateString.length == 1) {
//						stateString = "default";
//					} else if (splitBlockStateString.length == 2) {
//						stateString = StringUtils.reverse(StringUtils.reverse(StringUtils.split(blockStateString, "[")[1]).replaceFirst("]", ""));
//					} else {
//						NoCubes.NO_CUBES_LOG.error("Block/Blockstate Parsing error for \"" + blockStateString + "\"");
//						continue;
//					}
//
//					final Block block = BLOCKS.getValue(new ResourceLocation(blockString));
//					if (block == null) {
//						NoCubes.NO_CUBES_LOG.error("Block Parsing error NullPointerException for \"" + blockString + "\"");
//						continue;
//					}
//					try {
//						SMOOTHABLE_BLOCK_STATES_CACHE.add(CommandBase.convertArgToBlockState(block, stateString));
//					} catch (NumberInvalidException | InvalidBlockStateException e) {
//						NoCubes.NO_CUBES_LOG.error("Blockstate Parsing error " + e + " for \"" + stateString + "\"");
//						continue;
//					}
//
//				}
//
//			}
//
//		}
//
//	}

	private static void setupSmoothableBlockStates() {
		final BlockState[] defaultSmoothableBlockStates = new BlockState[]{

				GRASS.getDefaultState().with(GrassBlock.SNOWY, true),
				GRASS.getDefaultState().with(GrassBlock.SNOWY, false),

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
				REDSTONE_ORE.getDefaultState(),
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

				SNOW.getDefaultState(),

				SNOW.getDefaultState().with(SnowBlock.LAYERS, 1),
				SNOW.getDefaultState().with(SnowBlock.LAYERS, 2),
				SNOW.getDefaultState().with(SnowBlock.LAYERS, 3),
				SNOW.getDefaultState().with(SnowBlock.LAYERS, 4),
				SNOW.getDefaultState().with(SnowBlock.LAYERS, 5),
				SNOW.getDefaultState().with(SnowBlock.LAYERS, 6),
				SNOW.getDefaultState().with(SnowBlock.LAYERS, 7),
				SNOW.getDefaultState().with(SnowBlock.LAYERS, 8),

				BEDROCK.getDefaultState(),

				NETHERRACK.getDefaultState(),
				SOUL_SAND.getDefaultState(),
				MAGMA_BLOCK.getDefaultState(),
				GLOWSTONE.getDefaultState(),

				END_STONE.getDefaultState(),

				MYCELIUM.getDefaultState().with(MyceliumBlock.SNOWY, true),
				MYCELIUM.getDefaultState().with(MyceliumBlock.SNOWY, false),

		};

		final ArrayList<String> tempSmoothableBlockStates = new ArrayList<>();

		for (BlockState state : defaultSmoothableBlockStates) {
			SMOOTHABLE_BLOCK_STATES_CACHE.add(state);
			tempSmoothableBlockStates.add(state.toString());
		}

		smoothableBlockStates = tempSmoothableBlockStates.toArray(new String[0]);
	}

}
