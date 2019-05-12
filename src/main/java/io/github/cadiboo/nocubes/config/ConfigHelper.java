package io.github.cadiboo.nocubes.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirtSnowy;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.config.ModConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.minecraft.init.Blocks.ACACIA_LEAVES;
import static net.minecraft.init.Blocks.ANDESITE;
import static net.minecraft.init.Blocks.BEDROCK;
import static net.minecraft.init.Blocks.BIRCH_LEAVES;
import static net.minecraft.init.Blocks.BLACK_TERRACOTTA;
import static net.minecraft.init.Blocks.BROWN_TERRACOTTA;
import static net.minecraft.init.Blocks.CLAY;
import static net.minecraft.init.Blocks.COAL_ORE;
import static net.minecraft.init.Blocks.COARSE_DIRT;
import static net.minecraft.init.Blocks.DARK_OAK_LEAVES;
import static net.minecraft.init.Blocks.DIAMOND_ORE;
import static net.minecraft.init.Blocks.DIORITE;
import static net.minecraft.init.Blocks.DIRT;
import static net.minecraft.init.Blocks.EMERALD_ORE;
import static net.minecraft.init.Blocks.END_STONE;
import static net.minecraft.init.Blocks.GLOWSTONE;
import static net.minecraft.init.Blocks.GOLD_ORE;
import static net.minecraft.init.Blocks.GRANITE;
import static net.minecraft.init.Blocks.GRASS_BLOCK;
import static net.minecraft.init.Blocks.GRASS_PATH;
import static net.minecraft.init.Blocks.GRAVEL;
import static net.minecraft.init.Blocks.GRAY_TERRACOTTA;
import static net.minecraft.init.Blocks.INFESTED_STONE;
import static net.minecraft.init.Blocks.IRON_ORE;
import static net.minecraft.init.Blocks.JUNGLE_LEAVES;
import static net.minecraft.init.Blocks.LAPIS_ORE;
import static net.minecraft.init.Blocks.MAGMA_BLOCK;
import static net.minecraft.init.Blocks.MYCELIUM;
import static net.minecraft.init.Blocks.NETHERRACK;
import static net.minecraft.init.Blocks.NETHER_QUARTZ_ORE;
import static net.minecraft.init.Blocks.OAK_LEAVES;
import static net.minecraft.init.Blocks.ORANGE_TERRACOTTA;
import static net.minecraft.init.Blocks.PACKED_ICE;
import static net.minecraft.init.Blocks.PODZOL;
import static net.minecraft.init.Blocks.REDSTONE_ORE;
import static net.minecraft.init.Blocks.RED_SAND;
import static net.minecraft.init.Blocks.RED_SANDSTONE;
import static net.minecraft.init.Blocks.RED_TERRACOTTA;
import static net.minecraft.init.Blocks.SAND;
import static net.minecraft.init.Blocks.SANDSTONE;
import static net.minecraft.init.Blocks.SNOW;
import static net.minecraft.init.Blocks.SOUL_SAND;
import static net.minecraft.init.Blocks.SPRUCE_LEAVES;
import static net.minecraft.init.Blocks.STONE;
import static net.minecraft.init.Blocks.TERRACOTTA;
import static net.minecraft.init.Blocks.WHITE_TERRACOTTA;
import static net.minecraft.init.Blocks.YELLOW_TERRACOTTA;

/**
 * @author Cadiboo
 */
public final class ConfigHelper {

	private static ModConfig clientConfig;

	private static ModConfig serverConfig;

	public static void bakeClient(final ModConfig config) {
		clientConfig = config;

		Config.renderSmoothTerrain = ConfigHolder.CLIENT.renderSmoothTerrain.get();

		Config.renderSmoothLeaves = ConfigHolder.CLIENT.renderSmoothLeaves.get();
		Config.leavesMeshGenerator = ConfigHolder.CLIENT.leavesMeshGenerator.get();
		Config.leavesSmoothable = Sets.newHashSet(ConfigHolder.CLIENT.leavesSmoothable.get());
		initLeavesSmoothable();
		Config.smoothLeavesType = ConfigHolder.CLIENT.smoothLeavesType.get();

		Config.renderExtendedFluids = ConfigHolder.CLIENT.renderExtendedFluids.get();

		Config.applyDiffuseLighting = ConfigHolder.CLIENT.applyDiffuseLighting.get();

		Config.betterTextures = ConfigHolder.CLIENT.betterTextures.get();

		Config.smoothFluidLighting = ConfigHolder.CLIENT.smoothFluidLighting.get();
		Config.smoothFluidColors = ConfigHolder.CLIENT.smoothFluidColors.get();
		Config.naturalFluidTextures = ConfigHolder.CLIENT.naturalFluidTextures.get();

	}

	public static void bakeServer(final ModConfig config) {
		serverConfig = config;

		Config.terrainSmoothable = Sets.newHashSet(ConfigHolder.SERVER.terrainSmoothable.get());
		initTerrainSmoothable();

		Config.extendFluidsRange = ConfigHolder.SERVER.extendFluidsRange.get();

		Config.terrainMeshGenerator = ConfigHolder.SERVER.terrainMeshGenerator.get();
		Config.terrainCollisions = ConfigHolder.SERVER.terrainCollisions.get();
		Config.leavesCollisions = ConfigHolder.SERVER.leavesCollisions.get();
	}

	public static void addTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				Config.LOGGER.debug("Adding terrain smoothable: " + state);
				state.nocubes_setTerrainSmoothable(true);
				Config.terrainSmoothable.add(getStringFromState(state));
			}
			serverConfig.getConfigData().set("general.terrainSmoothable", new ArrayList<>(Config.terrainSmoothable));
			serverConfig.save();
		}
	}

	public static void removeTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				Config.LOGGER.debug("Removing terrain smoothable: " + state);
				state.nocubes_setTerrainSmoothable(false);
				Config.terrainSmoothable.remove(getStringFromState(state));
			}
			serverConfig.getConfigData().set("general.terrainSmoothable", new ArrayList<>(Config.terrainSmoothable));
			serverConfig.save();
		}
	}

	public static void addLeavesSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			synchronized (Config.leavesSmoothableBlocks) {
				for (final IBlockState originalState : states) {
					final Block block = originalState.getBlock();
					Config.LOGGER.debug("Adding leaves smoothable block: " + block);
					for (final IBlockState state : block.getStateContainer().getValidStates()) {
						Config.LOGGER.debug("Adding leaves smoothable state: " + state);
						state.nocubes_setLeavesSmoothable(true);
					}
					Config.leavesSmoothable.add(block.getRegistryName().toString());
					Config.leavesSmoothableBlocks.add(block);
				}
			}
			clientConfig.getConfigData().set("general.leavesSmoothable", new ArrayList<>(Config.leavesSmoothable));
			clientConfig.save();
		}
	}

	public static void removeLeavesSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			synchronized (Config.leavesSmoothableBlocks) {
				for (final IBlockState originalState : states) {
					final Block block = originalState.getBlock();
					Config.LOGGER.debug("Removing leaves smoothable block: " + block);
					for (final IBlockState state : block.getStateContainer().getValidStates()) {
						Config.LOGGER.debug("Removing leaves smoothable state: " + state);
						state.nocubes_setLeavesSmoothable(false);
					}
					Config.leavesSmoothable.remove(block.getRegistryName().toString());
					Config.leavesSmoothableBlocks.remove(block);
				}
			}
			clientConfig.getConfigData().set("general.leavesSmoothable", new ArrayList<>(Config.leavesSmoothable));
			clientConfig.save();
		}
	}

	private static void initTerrainSmoothable() {
		Config.LOGGER.debug("Initialising terrain smoothable");
		for (final String stateString : Config.terrainSmoothable) {
			Config.LOGGER.debug("Preparing to add \"" + stateString + "\" to terrain smoothable");
			final IBlockState state = getStateFromString(stateString);
			if (state != null) {
				Config.LOGGER.debug("Added \"" + state + "\" to terrain smoothable");
				state.nocubes_setTerrainSmoothable(true);
			} else {
				Config.LOGGER.debug("Cannot add invalid state \"" + stateString + "\" to terrain smoothable");
			}
		}
	}

	private static void initLeavesSmoothable() {
		Config.LOGGER.debug("Initialising leaves smoothable");
		for (final String blockString : Config.leavesSmoothable) {
			Config.LOGGER.debug("Preparing to add block \"" + blockString + "\" to leaves smoothable");
			final IBlockState defaultState = getStateFromString(blockString);
			if (defaultState != null) {
				final Block block = defaultState.getBlock();
				Config.LOGGER.debug("Added \"" + defaultState + "\" to leaves smoothable");
				for (final IBlockState state : block.getStateContainer().getValidStates()) {
					state.nocubes_setLeavesSmoothable(true);
				}
				Config.leavesSmoothableBlocks.add(block);
			} else {
				Config.LOGGER.debug("Cannot add invalid block \"" + blockString + "\" to leaves smoothable");
			}
		}
	}

	@Nullable
	private static IBlockState getStateFromString(@Nonnull final String stateString) {
		Preconditions.checkNotNull(stateString, "String to parse must not be null");
		try {
			return new BlockStateArgument().parse(new StringReader(stateString)).getState();
		} catch (final CommandSyntaxException e) {
			Config.LOGGER.error("Failed to parse blockstate \"" + stateString + "\"!", e);
			return null;
		}
	}

	@Nonnull
	private static String getStringFromState(@Nonnull final IBlockState state) {
		Preconditions.checkNotNull(state, "String to serialise must not be null");
		String stateString = state.getBlock().getRegistryName().toString();
		final ArrayList<String> properties = new ArrayList<>();
		for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getValues().entrySet()) {
			final IProperty<?> property = entry.getKey();
			final Comparable<?> value = entry.getValue();
			properties.add(property.getName() + "=" + Util.getValueName(property, value));
		}
		if (!properties.isEmpty()) {
			stateString += "[";
			stateString += Strings.join(properties, ",");
			stateString += "]";
		}
		return stateString;
	}

	@Nonnull
	public static List<String> getDefaultTerrainSmoothable() {
		final List<String> vanillaStates = Lists.newArrayList(

				GRASS_BLOCK.getDefaultState().with(BlockDirtSnowy.SNOWY, false),
				GRASS_BLOCK.getDefaultState().with(BlockDirtSnowy.SNOWY, true),

				STONE.getDefaultState(),
				GRANITE.getDefaultState(),
				DIORITE.getDefaultState(),
				ANDESITE.getDefaultState(),

				DIRT.getDefaultState(),
				COARSE_DIRT.getDefaultState(),

				PODZOL.getDefaultState().with(BlockDirtSnowy.SNOWY, false),
				PODZOL.getDefaultState().with(BlockDirtSnowy.SNOWY, true),

				SAND.getDefaultState(),
				RED_SAND.getDefaultState(),

				SANDSTONE.getDefaultState(),

				RED_SANDSTONE.getDefaultState(),

				GRAVEL.getDefaultState(),

				COAL_ORE.getDefaultState(),
				IRON_ORE.getDefaultState(),
				GOLD_ORE.getDefaultState(),
				REDSTONE_ORE.getDefaultState().with(BlockRedstoneOre.LIT, false),
				REDSTONE_ORE.getDefaultState().with(BlockRedstoneOre.LIT, true),
				DIAMOND_ORE.getDefaultState(),
				LAPIS_ORE.getDefaultState(),
				EMERALD_ORE.getDefaultState(),
				NETHER_QUARTZ_ORE.getDefaultState(),

				INFESTED_STONE.getDefaultState(),

				GRASS_PATH.getDefaultState(),

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

				BEDROCK.getDefaultState(),

				NETHERRACK.getDefaultState(),
				SOUL_SAND.getDefaultState(),
				MAGMA_BLOCK.getDefaultState(),
				GLOWSTONE.getDefaultState(),

				END_STONE.getDefaultState(),

				MYCELIUM.getDefaultState().with(BlockMycelium.SNOWY, true),
				MYCELIUM.getDefaultState().with(BlockMycelium.SNOWY, false)

		).stream().map(ConfigHelper::getStringFromState).collect(Collectors.toList());

		final List<String> moddedStates = Lists.newArrayList(

		);

		final ArrayList<String> finalStates = Lists.newArrayList();
		finalStates.addAll(vanillaStates);
		finalStates.addAll(moddedStates);
		return finalStates;
	}

	@Nonnull
	public static List<String> getDefaultLeavesSmoothable() {
		final List<String> vanillaStates = Lists.newArrayList(

				OAK_LEAVES,
				SPRUCE_LEAVES,
				BIRCH_LEAVES,
				JUNGLE_LEAVES,
				ACACIA_LEAVES,
				DARK_OAK_LEAVES

		).stream().map(Block::getRegistryName).map(ResourceLocation::toString).collect(Collectors.toList());

		final List<String> moddedStates = Lists.newArrayList(

		);

		final ArrayList<String> finalStates = Lists.newArrayList();
		finalStates.addAll(vanillaStates);
		finalStates.addAll(moddedStates);
		return finalStates;
	}

	public static void enableTerrainCollisions() {
		serverConfig.getConfigData().set("general.terrainCollisions", true);
	}

	public static void disableTerrainCollisions() {
		serverConfig.getConfigData().set("general.terrainCollisions", false);
	}

	public static void enableLeavesCollisions() {
		serverConfig.getConfigData().set("general.leavesCollisions", true);
	}

	public static void disableLeavesCollisions() {
		serverConfig.getConfigData().set("general.leavesCollisions", false);
	}

}
