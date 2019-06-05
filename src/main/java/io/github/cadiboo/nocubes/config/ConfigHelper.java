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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.cadiboo.nocubes.config.Config.LOGGER;
import static io.github.cadiboo.nocubes.config.Config.applyDiffuseLighting;
import static io.github.cadiboo.nocubes.config.Config.betterTextures;
import static io.github.cadiboo.nocubes.config.Config.extendFluidsRange;
import static io.github.cadiboo.nocubes.config.Config.leavesMeshGenerator;
import static io.github.cadiboo.nocubes.config.Config.leavesSmoothable;
import static io.github.cadiboo.nocubes.config.Config.leavesSmoothableBlocks;
import static io.github.cadiboo.nocubes.config.Config.naturalFluidTextures;
import static io.github.cadiboo.nocubes.config.Config.renderSmoothLeaves;
import static io.github.cadiboo.nocubes.config.Config.renderSmoothTerrain;
import static io.github.cadiboo.nocubes.config.Config.shortGrass;
import static io.github.cadiboo.nocubes.config.Config.smoothFluidColors;
import static io.github.cadiboo.nocubes.config.Config.smoothFluidLighting;
import static io.github.cadiboo.nocubes.config.Config.smoothLeavesType;
import static io.github.cadiboo.nocubes.config.Config.terrainCollisions;
import static io.github.cadiboo.nocubes.config.Config.terrainMeshGenerator;
import static io.github.cadiboo.nocubes.config.Config.terrainSmoothable;
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

		renderSmoothTerrain = ConfigHolder.CLIENT.renderSmoothTerrain.get();

		renderSmoothLeaves = ConfigHolder.CLIENT.renderSmoothLeaves.get();
		leavesMeshGenerator = ConfigHolder.CLIENT.leavesMeshGenerator.get();
		leavesSmoothable = Sets.newHashSet(ConfigHolder.CLIENT.leavesSmoothable.get());
		initLeavesSmoothable();
		smoothLeavesType = ConfigHolder.CLIENT.smoothLeavesType.get();

		applyDiffuseLighting = ConfigHolder.CLIENT.applyDiffuseLighting.get();

		betterTextures = ConfigHolder.CLIENT.betterTextures.get();

		shortGrass = ConfigHolder.CLIENT.shortGrass.get();

		smoothFluidLighting = ConfigHolder.CLIENT.smoothFluidLighting.get();
		smoothFluidColors = ConfigHolder.CLIENT.smoothFluidColors.get();
		naturalFluidTextures = ConfigHolder.CLIENT.naturalFluidTextures.get();

	}

	public static void bakeServer(final ModConfig config) {
		serverConfig = config;

		terrainSmoothable = Sets.newHashSet(ConfigHolder.SERVER.terrainSmoothable.get());
		initTerrainSmoothable();

		extendFluidsRange = ConfigHolder.SERVER.extendFluidsRange.get();

		terrainMeshGenerator = ConfigHolder.SERVER.terrainMeshGenerator.get();
		terrainCollisions = ConfigHolder.SERVER.terrainCollisions.get();
	}

	public static void discoverDefaultTerrainSmoothable() {
		final ArrayList<IBlockState> discoveredStates = new ArrayList<>();
		ForgeRegistries.BLOCKS.getValues().parallelStream()
				.map(Block::getStateContainer)
				.map(StateContainer::getValidStates)
				.forEach(validStates -> validStates.parallelStream()
						.forEach(state -> {
							if (state.isNormalCube() && state.isBlockNormalCube() && state.isSolid() && state.causesSuffocation()) {
								final Material material = state.getMaterial();
								if (material == Material.GROUND || material == Material.ROCK) {
									LOGGER.debug("Discovered terrain smoothable \"" + state + "\"");
									discoveredStates.add(state);
								}
							}
						}));
		addTerrainSmoothable(discoveredStates.toArray(new IBlockState[0]));
	}

	public static void discoverDefaultLeavesSmoothable() {
		final ArrayList<IBlockState> discoveredStates = new ArrayList<>();
		ForgeRegistries.BLOCKS.getValues().parallelStream()
				.forEach(block -> {
					final IBlockState defaultState = block.getDefaultState();
					if (defaultState.getMaterial() == Material.LEAVES) {
						LOGGER.debug("Discovered leaves smoothable \"" + block + "\"");
						discoveredStates.add(defaultState);
					}
				});
		addLeavesSmoothable(discoveredStates.toArray(new IBlockState[0]));
	}

	public static void addTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				LOGGER.debug("Adding terrain smoothable: " + state);
				state.nocubes_setTerrainSmoothable(true);
				terrainSmoothable.add(getStringFromState(state));
			}
			setValueAndSave(serverConfig, "general.terrainSmoothable", new ArrayList<>(terrainSmoothable));
		}
	}

	public static void removeTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				LOGGER.debug("Removing terrain smoothable: " + state);
				state.nocubes_setTerrainSmoothable(false);
				terrainSmoothable.remove(getStringFromState(state));
			}
			setValueAndSave(serverConfig, "general.terrainSmoothable", new ArrayList<>(terrainSmoothable));
		}
	}

	public static void addLeavesSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			synchronized (leavesSmoothableBlocks) {
				for (final IBlockState originalState : states) {
					final Block block = originalState.getBlock();
					LOGGER.debug("Adding leaves smoothable block: " + block);
					for (final IBlockState state : block.getStateContainer().getValidStates()) {
						LOGGER.debug("Adding leaves smoothable state: " + state);
						state.nocubes_setLeavesSmoothable(true);
					}
					leavesSmoothable.add(block.getRegistryName().toString());
					leavesSmoothableBlocks.add(block);
				}
			}
			setValueAndSave(clientConfig, "general.leavesSmoothable", new ArrayList<>(leavesSmoothable));
		}
	}

	public static void removeLeavesSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			synchronized (leavesSmoothableBlocks) {
				for (final IBlockState originalState : states) {
					final Block block = originalState.getBlock();
					LOGGER.debug("Removing leaves smoothable block: " + block);
					for (final IBlockState state : block.getStateContainer().getValidStates()) {
						LOGGER.debug("Removing leaves smoothable state: " + state);
						state.nocubes_setLeavesSmoothable(false);
					}
					leavesSmoothable.remove(block.getRegistryName().toString());
					leavesSmoothableBlocks.remove(block);
				}
			}
			setValueAndSave(clientConfig, "general.leavesSmoothable", new ArrayList<>(leavesSmoothable));
		}
	}

	private static void initTerrainSmoothable() {
		LOGGER.debug("Initialising terrain smoothable");
		for (final String stateString : terrainSmoothable) {
			LOGGER.debug("Preparing to add \"" + stateString + "\" to terrain smoothable");
			final IBlockState state = getStateFromString(stateString);
			if (state != null) {
				LOGGER.debug("Added \"" + state + "\" to terrain smoothable");
				state.nocubes_setTerrainSmoothable(true);
			} else {
				LOGGER.debug("Cannot add invalid state \"" + stateString + "\" to terrain smoothable");
			}
		}
	}

	private static void initLeavesSmoothable() {
		LOGGER.debug("Initialising leaves smoothable");
		for (final String blockString : leavesSmoothable) {
			LOGGER.debug("Preparing to add block \"" + blockString + "\" to leaves smoothable");
			final IBlockState defaultState = getStateFromString(blockString);
			if (defaultState != null) {
				final Block block = defaultState.getBlock();
				LOGGER.debug("Added \"" + defaultState + "\" to leaves smoothable");
				for (final IBlockState state : block.getStateContainer().getValidStates()) {
					state.nocubes_setLeavesSmoothable(true);
				}
				leavesSmoothableBlocks.add(block);
			} else {
				LOGGER.debug("Cannot add invalid block \"" + blockString + "\" to leaves smoothable");
			}
		}
	}

	@Nullable
	private static IBlockState getStateFromString(@Nonnull final String stateString) {
		Preconditions.checkNotNull(stateString, "String to parse must not be null");
		try {
			return new BlockStateArgument().parse(new StringReader(stateString)).getState();
		} catch (final CommandSyntaxException e) {
			LOGGER.error("Failed to parse blockstate \"" + stateString + "\"!", e);
			return null;
		}
	}

	@Nonnull
	private static String getStringFromState(@Nonnull final IBlockState state) {
		Preconditions.checkNotNull(state, "State to serialise must not be null");
		String stateString = Objects.requireNonNull(state.getBlock().getRegistryName(), "Block registry name cannot be null!").toString();
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

	public static void setTerrainCollisions(final boolean enabled) {
		setValueAndSave(serverConfig, "general.terrainCollisions", enabled);
	}

	public static void setRenderSmoothTerrain(final boolean enabled) {
		setValueAndSave(clientConfig, "general.renderSmoothTerrain", enabled);
	}

	public static void setRenderSmoothLeaves(final boolean enabled) {
		setValueAndSave(clientConfig, "general.renderSmoothLeaves", enabled);
	}

	private static void setValueAndSave(final ModConfig modConfig, final String path, final Object newValue) {
		modConfig.getConfigData().set(path, newValue);
		modConfig.save();
	}

}
