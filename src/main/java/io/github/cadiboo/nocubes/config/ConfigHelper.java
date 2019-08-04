package io.github.cadiboo.nocubes.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import io.github.cadiboo.nocubes.util.StateHolder;
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MyceliumBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.block.material.Material;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.state.IProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
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
import static io.github.cadiboo.nocubes.config.Config.renderSmoothAndVanillaLeaves;
import static io.github.cadiboo.nocubes.config.Config.renderSmoothLeaves;
import static io.github.cadiboo.nocubes.config.Config.renderSmoothTerrain;
import static io.github.cadiboo.nocubes.config.Config.shortGrass;
import static io.github.cadiboo.nocubes.config.Config.smoothFluidColors;
import static io.github.cadiboo.nocubes.config.Config.smoothFluidLighting;
import static io.github.cadiboo.nocubes.config.Config.smoothLeavesType;
import static io.github.cadiboo.nocubes.config.Config.terrainCollisions;
import static io.github.cadiboo.nocubes.config.Config.terrainMeshGenerator;
import static io.github.cadiboo.nocubes.config.Config.terrainSmoothable;
import static net.minecraft.block.Blocks.ACACIA_LEAVES;
import static net.minecraft.block.Blocks.ANDESITE;
import static net.minecraft.block.Blocks.BEDROCK;
import static net.minecraft.block.Blocks.BIRCH_LEAVES;
import static net.minecraft.block.Blocks.BLACK_TERRACOTTA;
import static net.minecraft.block.Blocks.BROWN_TERRACOTTA;
import static net.minecraft.block.Blocks.CLAY;
import static net.minecraft.block.Blocks.COAL_ORE;
import static net.minecraft.block.Blocks.COARSE_DIRT;
import static net.minecraft.block.Blocks.DARK_OAK_LEAVES;
import static net.minecraft.block.Blocks.DIAMOND_ORE;
import static net.minecraft.block.Blocks.DIORITE;
import static net.minecraft.block.Blocks.DIRT;
import static net.minecraft.block.Blocks.EMERALD_ORE;
import static net.minecraft.block.Blocks.END_STONE;
import static net.minecraft.block.Blocks.GLOWSTONE;
import static net.minecraft.block.Blocks.GOLD_ORE;
import static net.minecraft.block.Blocks.GRANITE;
import static net.minecraft.block.Blocks.GRASS_BLOCK;
import static net.minecraft.block.Blocks.GRASS_PATH;
import static net.minecraft.block.Blocks.GRAVEL;
import static net.minecraft.block.Blocks.GRAY_TERRACOTTA;
import static net.minecraft.block.Blocks.INFESTED_STONE;
import static net.minecraft.block.Blocks.IRON_ORE;
import static net.minecraft.block.Blocks.JUNGLE_LEAVES;
import static net.minecraft.block.Blocks.LAPIS_ORE;
import static net.minecraft.block.Blocks.MAGMA_BLOCK;
import static net.minecraft.block.Blocks.MYCELIUM;
import static net.minecraft.block.Blocks.NETHERRACK;
import static net.minecraft.block.Blocks.NETHER_QUARTZ_ORE;
import static net.minecraft.block.Blocks.OAK_LEAVES;
import static net.minecraft.block.Blocks.ORANGE_TERRACOTTA;
import static net.minecraft.block.Blocks.PACKED_ICE;
import static net.minecraft.block.Blocks.PODZOL;
import static net.minecraft.block.Blocks.REDSTONE_ORE;
import static net.minecraft.block.Blocks.RED_SAND;
import static net.minecraft.block.Blocks.RED_SANDSTONE;
import static net.minecraft.block.Blocks.RED_TERRACOTTA;
import static net.minecraft.block.Blocks.SAND;
import static net.minecraft.block.Blocks.SANDSTONE;
import static net.minecraft.block.Blocks.SNOW;
import static net.minecraft.block.Blocks.SOUL_SAND;
import static net.minecraft.block.Blocks.SPRUCE_LEAVES;
import static net.minecraft.block.Blocks.STONE;
import static net.minecraft.block.Blocks.TERRACOTTA;
import static net.minecraft.block.Blocks.WHITE_TERRACOTTA;
import static net.minecraft.block.Blocks.YELLOW_TERRACOTTA;

/**
 * @author Cadiboo
 */
public final class ConfigHelper {

	private static final Marker CONFIG_SMOOTHABLE = MarkerManager.getMarker("CONFIG_SMOOTHABLE");

	public static ModConfig clientConfig;

	public static ModConfig serverConfig;

	public static void bakeClient(final ModConfig config) {
		clientConfig = config;

		renderSmoothTerrain = ConfigHolder.CLIENT.renderSmoothTerrain.get();

		renderSmoothLeaves = ConfigHolder.CLIENT.renderSmoothLeaves.get();
		renderSmoothAndVanillaLeaves = ConfigHolder.CLIENT.renderSmoothAndVanillaLeaves.get();
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
//		final ArrayList<BlockState> discoveredStates = new ArrayList<>();
//		ForgeRegistries.BLOCKS.getValues().parallelStream()
//				.map(Block::getStateContainer)
//				.map(StateContainer::getValidStates)
//				.forEach(validStates -> validStates.parallelStream()
//						.forEach(state -> {
//							if (state.isNormalCube() && state.isBlockNormalCube() && state.isSolid() && state.causesSuffocation()) {
//								final Material material = state.getMaterial();
//								if (material == Material.GROUND || material == Material.ROCK) {
//									LOGGER.debug(CONFIG_SMOOTHABLE, "Discovered terrain smoothable \"" + state + "\"");
//									discoveredStates.add(state);
//								}
//							}
//						}));
//		addTerrainSmoothable(discoveredStates.toArray(new BlockState[0]));
	}

	public static void discoverDefaultLeavesSmoothable() {
		final ArrayList<BlockState> discoveredStates = new ArrayList<>();
		ForgeRegistries.BLOCKS.getValues().parallelStream()
				.forEach(block -> {
					final BlockState defaultState = block.getDefaultState();
					if (defaultState.getMaterial() == Material.LEAVES) {
						LOGGER.debug(CONFIG_SMOOTHABLE, "Discovered leaves smoothable \"" + block + "\"");
						discoveredStates.add(defaultState);
					}
				});
		addLeavesSmoothable(discoveredStates.toArray(new BlockState[0]));
	}

	public static void addTerrainSmoothable(final BlockState... states) {
		if (states.length > 0) {
			for (final BlockState state : states) {
				LOGGER.debug(CONFIG_SMOOTHABLE, "Adding terrain smoothable: " + state);
				state.nocubes_isTerrainSmoothable = true;
				terrainSmoothable.add(getStringFromState(state));
			}
			setValueAndSave(serverConfig, "general.terrainSmoothable", new ArrayList<>(terrainSmoothable));
		}
	}

	public static void removeTerrainSmoothable(final BlockState... states) {
		if (states.length > 0) {
			for (final BlockState state : states) {
				LOGGER.debug(CONFIG_SMOOTHABLE, "Removing terrain smoothable: " + state);
				state.nocubes_isTerrainSmoothable = false;
				terrainSmoothable.remove(getStringFromState(state));
			}
			setValueAndSave(serverConfig, "general.terrainSmoothable", new ArrayList<>(terrainSmoothable));
		}
	}

	public static void addLeavesSmoothable(final BlockState... states) {
		if (states.length > 0) {
			synchronized (leavesSmoothableBlocks) {
				for (final BlockState originalState : states) {
					final Block block = originalState.getBlock();
					LOGGER.debug(CONFIG_SMOOTHABLE, "Adding leaves smoothable block: " + block);
					for (final BlockState state : block.getStateContainer().getValidStates()) {
						LOGGER.debug(CONFIG_SMOOTHABLE, "Adding leaves smoothable state: " + state);
						state.nocubes_isLeavesSmoothable = true;
					}
					leavesSmoothable.add(block.getRegistryName().toString());
					leavesSmoothableBlocks.add(block);
				}
			}
			setValueAndSave(clientConfig, "general.leavesSmoothable", new ArrayList<>(leavesSmoothable));
		}
	}

	public static void removeLeavesSmoothable(final BlockState... states) {
		if (states.length > 0) {
			synchronized (leavesSmoothableBlocks) {
				for (final BlockState originalState : states) {
					final Block block = originalState.getBlock();
					LOGGER.debug(CONFIG_SMOOTHABLE, "Removing leaves smoothable block: " + block);
					for (final BlockState state : block.getStateContainer().getValidStates()) {
						LOGGER.debug(CONFIG_SMOOTHABLE, "Removing leaves smoothable state: " + state);
						state.nocubes_isLeavesSmoothable = false;
					}
					leavesSmoothable.remove(block.getRegistryName().toString());
					leavesSmoothableBlocks.remove(block);
				}
			}
			setValueAndSave(clientConfig, "general.leavesSmoothable", new ArrayList<>(leavesSmoothable));
		}
	}

	private static void initTerrainSmoothable() {
		LOGGER.debug(CONFIG_SMOOTHABLE, "Initialising terrain smoothable");

		final HashSet<BlockState> smoothableStates = new HashSet<>();
		for (final String stateString : terrainSmoothable) {
			final BlockState blockState = getStateFromString(stateString);
			if (blockState != null && blockState != StateHolder.AIR_DEFAULT) {
				smoothableStates.add(blockState);
			} else {
				LOGGER.error(CONFIG_SMOOTHABLE, "Cannot add invalid blockState \"" + stateString + "\" to terrain smoothable");
			}
		}

		for (final Block block : ForgeRegistries.BLOCKS.getValues()) {
			for (final BlockState state : block.getStateContainer().getValidStates()) {
				state.nocubes_isTerrainSmoothable = smoothableStates.contains(state);
			}
		}
	}

	private static void initLeavesSmoothable() {
		LOGGER.debug(CONFIG_SMOOTHABLE, "Initialising leaves smoothable");

		final HashSet<Block> smoothableBlocks = new HashSet<>();
		for (final String blockString : leavesSmoothable) {
			final BlockState defaultState = getStateFromString(blockString);
			if (defaultState != null && defaultState != StateHolder.AIR_DEFAULT) {
				final Block block = defaultState.getBlock();
				smoothableBlocks.add(block);
			} else {
				LOGGER.error(CONFIG_SMOOTHABLE, "Cannot add invalid block \"" + blockString + "\" to leaves smoothable");
			}
		}

		for (final Block block : ForgeRegistries.BLOCKS.getValues()) {
			final boolean isBlockSmoothable = smoothableBlocks.contains(block);
			for (final BlockState state : block.getStateContainer().getValidStates()) {
				state.nocubes_isLeavesSmoothable = isBlockSmoothable;
			}
		}
		leavesSmoothableBlocks.clear();
		leavesSmoothableBlocks.addAll(smoothableBlocks);
	}

	@Nullable
	private static BlockState getStateFromString(@Nonnull final String stateString) {
		Preconditions.checkNotNull(stateString, "String to parse must not be null");
		try {
			return new BlockStateArgument().parse(new StringReader(stateString)).getState();
		} catch (final CommandSyntaxException e) {
			LOGGER.error(CONFIG_SMOOTHABLE, "Failed to parse blockstate \"" + stateString + "\"!", e);
			return null;
		}
	}

	@Nonnull
	private static String getStringFromState(@Nonnull final BlockState state) {
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

				GRASS_BLOCK.getDefaultState().with(SnowyDirtBlock.SNOWY, false),
				GRASS_BLOCK.getDefaultState().with(SnowyDirtBlock.SNOWY, true),

				STONE.getDefaultState(),
				GRANITE.getDefaultState(),
				DIORITE.getDefaultState(),
				ANDESITE.getDefaultState(),

				DIRT.getDefaultState(),
				COARSE_DIRT.getDefaultState(),

				PODZOL.getDefaultState().with(SnowyDirtBlock.SNOWY, false),
				PODZOL.getDefaultState().with(SnowyDirtBlock.SNOWY, true),

				SAND.getDefaultState(),
				RED_SAND.getDefaultState(),

				SANDSTONE.getDefaultState(),

				RED_SANDSTONE.getDefaultState(),

				GRAVEL.getDefaultState(),

				COAL_ORE.getDefaultState(),
				IRON_ORE.getDefaultState(),
				GOLD_ORE.getDefaultState(),
				REDSTONE_ORE.getDefaultState().with(RedstoneOreBlock.LIT, false),
				REDSTONE_ORE.getDefaultState().with(RedstoneOreBlock.LIT, true),
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

				MYCELIUM.getDefaultState().with(MyceliumBlock.SNOWY, true),
				MYCELIUM.getDefaultState().with(MyceliumBlock.SNOWY, false)

		).stream()
				.map(ConfigHelper::getStringFromState)
				.collect(Collectors.toList());

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

		).stream()
				.map(Block::getRegistryName)
				.map(ResourceLocation::toString)
				.collect(Collectors.toList());

		final List<String> moddedStates = Lists.newArrayList(

		);

		final ArrayList<String> finalStates = Lists.newArrayList();
		finalStates.addAll(vanillaStates);
		finalStates.addAll(moddedStates);
		return finalStates;
	}

	public static void setExtendFluidsRange(final ExtendFluidsRange newRange) {
		setValueAndSave(serverConfig, "general.extendFluidsRange", newRange);
	}

	public static void setTerrainMeshGenerator(final MeshGeneratorType newGenerator) {
		setValueAndSave(serverConfig, "general.terrainMeshGenerator", newGenerator);
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

	public static void setValueAndSave(final ModConfig modConfig, final String path, final Object newValue) {
		modConfig.getConfigData().set(path, newValue);
		modConfig.save();
	}

}
