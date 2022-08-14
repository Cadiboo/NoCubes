package io.github.cadiboo.nocubes.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.repackage.net.minecraftforge.fml.config.ModConfig;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import io.github.cadiboo.nocubes.util.INoCubesBlockState;
import io.github.cadiboo.nocubes.util.StateHolder;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.cadiboo.nocubes.config.Config.*;
import static net.minecraft.init.Blocks.*;
import static net.minecraft.item.EnumDyeColor.*;

/**
 * @author Cadiboo
 */
public final class ConfigHelper {

	private static final Marker CONFIG_SMOOTHABLE = MarkerManager.getMarker("CONFIG_SMOOTHABLE");

	public static ModConfig clientConfig;

	public static ModConfig serverConfig;

	public static void bakeClient(final ModConfig config) {
		clientConfig = config;

		// Directly querying the baked field - won't cause a NPE on the client when there is no server
	 	renderSmoothTerrain = Config.forceVisuals | ConfigHolder.CLIENT.renderSmoothTerrain.get();

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

		forceVisuals = ConfigHolder.SERVER.forceVisuals.get();
		if (forceVisuals)
			// Directly setting the baked field - won't cause a NPE on the dedicated server
			renderSmoothTerrain = true;
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
		final ArrayList<IBlockState> discoveredStates = new ArrayList<>();
		ForgeRegistries.BLOCKS.getValues().parallelStream()
				.forEach(block -> {
					final IBlockState defaultState = block.getDefaultState();
					if (defaultState.getMaterial() == Material.LEAVES) {
						LOGGER.debug(CONFIG_SMOOTHABLE, "Discovered leaves smoothable \"" + block + "\"");
						discoveredStates.add(defaultState);
					}
				});
		addLeavesSmoothable(discoveredStates.toArray(new IBlockState[0]));
	}

	public static void addTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				LOGGER.debug(CONFIG_SMOOTHABLE, "Adding terrain smoothable: " + state);
				((INoCubesBlockState) state).nocubes_setTerrainSmoothable(true);
				terrainSmoothable.add(getStringFromState(state));
			}
			setValueAndSave(serverConfig, "general.terrainSmoothable", new ArrayList<>(terrainSmoothable));
		}
	}

	public static void removeTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				LOGGER.debug(CONFIG_SMOOTHABLE, "Removing terrain smoothable: " + state);
				((INoCubesBlockState) state).nocubes_setTerrainSmoothable(false);
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
					LOGGER.debug(CONFIG_SMOOTHABLE, "Adding leaves smoothable block: " + block);
					for (final IBlockState state : block.getBlockState().getValidStates()) {
						LOGGER.debug(CONFIG_SMOOTHABLE, "Adding leaves smoothable state: " + state);
						((INoCubesBlockState) state).nocubes_setLeavesSmoothable(true);
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
					LOGGER.debug(CONFIG_SMOOTHABLE, "Removing leaves smoothable block: " + block);
					for (final IBlockState state : block.getBlockState().getValidStates()) {
						LOGGER.debug(CONFIG_SMOOTHABLE, "Removing leaves smoothable state: " + state);
						((INoCubesBlockState) state).nocubes_setLeavesSmoothable(false);
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

		final HashSet<IBlockState> smoothableStates = new HashSet<>();
		for (final String stateString : terrainSmoothable) {
			final IBlockState blockState = getStateFromString(stateString);
			if (blockState != null && blockState != StateHolder.AIR_DEFAULT) {
				smoothableStates.add(blockState);
			} else {
				LOGGER.error(CONFIG_SMOOTHABLE, "Cannot add invalid blockState \"" + stateString + "\" to terrain smoothable");
			}
		}

		for (final Block block : ForgeRegistries.BLOCKS.getValues()) {
			for (final IBlockState state : block.getBlockState().getValidStates()) {
				((INoCubesBlockState) state).nocubes_setTerrainSmoothable(smoothableStates.contains(state));
			}
		}
	}

	private static void initLeavesSmoothable() {
		LOGGER.debug(CONFIG_SMOOTHABLE, "Initialising leaves smoothable");

		final HashSet<Block> smoothableBlocks = new HashSet<>();
		for (final String blockString : leavesSmoothable) {
			final IBlockState defaultState = getStateFromString(blockString);
			if (defaultState != null && defaultState != StateHolder.AIR_DEFAULT) {
				final Block block = defaultState.getBlock();
				smoothableBlocks.add(block);
			} else {
				LOGGER.error(CONFIG_SMOOTHABLE, "Cannot add invalid block \"" + blockString + "\" to leaves smoothable");
			}
		}

		for (final Block block : ForgeRegistries.BLOCKS.getValues()) {
			final boolean isBlockSmoothable = smoothableBlocks.contains(block);
			for (final IBlockState state : block.getBlockState().getValidStates()) {
				((INoCubesBlockState) state).nocubes_setLeavesSmoothable(isBlockSmoothable);
			}
		}
		leavesSmoothableBlocks.clear();
		leavesSmoothableBlocks.addAll(smoothableBlocks);
	}

	@Nullable
	private static IBlockState getStateFromString(@Nonnull final String stateString) {
		Preconditions.checkNotNull(stateString, "String to parse must not be null");
		try {
			final String[] splitBlockStateString = StringUtils.split(stateString, "[");
			final String blockString = splitBlockStateString[0];
			final String variantsString;
			if (splitBlockStateString.length == 1) {
				variantsString = "default";
			} else if (splitBlockStateString.length == 2) {
				variantsString = StringUtils.reverse(StringUtils.reverse(StringUtils.split(stateString, "[")[1]).replaceFirst("]", ""));
			} else {
				LOGGER.error("Block/BlockState Parsing error for \"" + stateString + "\"");
				return null;
			}

			final Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockString));
			if (block == null || block == Blocks.AIR) {
				LOGGER.error("Block Parsing error for \"" + blockString + "\". Block does not exist!");
				return null;
			}
			try {
				return CommandBase.convertArgToBlockState(block, variantsString);
			} catch (NumberInvalidException e) {
				LOGGER.error("BlockState Parsing error " + e + " for \"" + variantsString + "\". Invalid Number!");
				return null;
			} catch (InvalidBlockStateException e) {
				LOGGER.error("BlockState Parsing error " + e + " for \"" + variantsString + "\". Invalid BlockState!");
				return null;
			}
		} catch (Exception e) {
			LOGGER.error("Failed to parse blockstate \"" + stateString + "\"!", e);
			return null;
		}
	}

	@Nonnull
	private static String getStringFromState(@Nonnull final IBlockState state) {
		Preconditions.checkNotNull(state, "State to serialise must not be null");
//		String stateString = Objects.requireNonNull(state.getBlock().getRegistryName(), "Block registry name cannot be null!").toString();
//		final ArrayList<String> properties = new ArrayList<>();
//		for (Map.Entry<IProperty<?>, Comparable<?>> entry : state.getProperties().entrySet()) {
//			final IProperty<?> property = entry.getKey();
//			final Comparable<?> value = entry.getValue();
//			properties.add(property.getName() + "=" + Util.getValueName(property, value));
//		}
//		if (!properties.isEmpty()) {
//			stateString += "[";
//			stateString += Strings.join(properties, ",");
//			stateString += "]";
//		}
//		return stateString;
		return state.toString();
	}

	@Nonnull
	static List<String> getDefaultTerrainSmoothable() {
		final List<String> vanillaStates = Lists.newArrayList(

				GRASS.getDefaultState().withProperty(BlockDirt.SNOWY, false),
				GRASS.getDefaultState().withProperty(BlockDirt.SNOWY, true),

				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.GRANITE),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE),
				STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.ANDESITE),

				DIRT.getDefaultState(),
				DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT),

				DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL).withProperty(BlockDirt.SNOWY, false),
				DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.PODZOL).withProperty(BlockDirt.SNOWY, true),

				SAND.getDefaultState(),
				SAND.getDefaultState().withProperty(BlockSand.VARIANT, BlockSand.EnumType.RED_SAND),

				SANDSTONE.getDefaultState(),

				RED_SANDSTONE.getDefaultState(),

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

				GRASS_PATH.getDefaultState(),

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

				SNOW_LAYER.getDefaultState(),

				BEDROCK.getDefaultState(),

				NETHERRACK.getDefaultState(),
				SOUL_SAND.getDefaultState(),
				MAGMA.getDefaultState(),
				GLOWSTONE.getDefaultState(),

				END_STONE.getDefaultState(),

				MYCELIUM.getDefaultState().withProperty(BlockMycelium.SNOWY, true),
				MYCELIUM.getDefaultState().withProperty(BlockMycelium.SNOWY, false)

		).stream()
				.map(ConfigHelper::getStringFromState)
				.collect(Collectors.toList());

		final List<String> moddedStates = Lists.newArrayList(
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
				"rustic:slate"
		);

		final ArrayList<String> finalStates = Lists.newArrayList();
		finalStates.addAll(vanillaStates);
		finalStates.addAll(moddedStates);
		return finalStates;
	}

	@Nonnull
	static List<String> getDefaultLeavesSmoothable() {
		final List<String> vanillaStates = Lists.newArrayList(

				LEAVES,
				LEAVES2

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
		modConfig.fireEvent(new ModConfig.Reloading(modConfig));
	}

}
