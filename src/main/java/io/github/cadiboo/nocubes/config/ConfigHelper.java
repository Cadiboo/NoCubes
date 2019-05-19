package io.github.cadiboo.nocubes.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.InvalidBlockStateException;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.github.cadiboo.nocubes.config.Config.LOGGER;
import static io.github.cadiboo.nocubes.config.Config.leavesSmoothable;
import static io.github.cadiboo.nocubes.config.Config.leavesSmoothableBlocks;
import static io.github.cadiboo.nocubes.config.Config.terrainSmoothable;
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
import static net.minecraft.init.Blocks.LEAVES;
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
 * @author Cadiboo
 */
public final class ConfigHelper {

//	private static ModConfig clientConfig;
//
//	private static ModConfig serverConfig;
//
//	public static void bakeClient(final ModConfig config) {
//		clientConfig = config;
//
//		renderSmoothTerrain = ConfigHolder.CLIENT.renderSmoothTerrain.get();
//
//		renderSmoothLeaves = ConfigHolder.CLIENT.renderSmoothLeaves.get();
//		leavesMeshGenerator = ConfigHolder.CLIENT.leavesMeshGenerator.get();
//		leavesSmoothable = Sets.newHashSet(ConfigHolder.CLIENT.leavesSmoothable.get());
//		initLeavesSmoothable();
//		smoothLeavesType = ConfigHolder.CLIENT.smoothLeavesType.get();
//
//		renderExtendedFluids = ConfigHolder.CLIENT.renderExtendedFluids.get();
//
//		applyDiffuseLighting = ConfigHolder.CLIENT.applyDiffuseLighting.get();
//
//		betterTextures = ConfigHolder.CLIENT.betterTextures.get();
//
//		smoothFluidLighting = ConfigHolder.CLIENT.smoothFluidLighting.get();
//		smoothFluidColors = ConfigHolder.CLIENT.smoothFluidColors.get();
//		naturalFluidTextures = ConfigHolder.CLIENT.naturalFluidTextures.get();
//
//	}
//
//	public static void bakeServer(final ModConfig config) {
//		serverConfig = config;
//
//		terrainSmoothable = Sets.newHashSet(ConfigHolder.SERVER.terrainSmoothable.get());
//		initTerrainSmoothable();
//
//		extendFluidsRange = ConfigHolder.SERVER.extendFluidsRange.get();
//
//		terrainMeshGenerator = ConfigHolder.SERVER.terrainMeshGenerator.get();
//		terrainCollisions = ConfigHolder.SERVER.terrainCollisions.get();
//		leavesCollisions = ConfigHolder.SERVER.leavesCollisions.get();
//	}

	public static void discoverDefaultTerrainSmoothable() {
		final ArrayList<IBlockState> discoveredStates = new ArrayList<>();
		ForgeRegistries.BLOCKS.getValues().parallelStream()
				.map(Block::getBlockState)
				.map(BlockStateContainer::getValidStates)
				.forEach(validStates -> validStates.parallelStream()
						.forEach(state -> {
							if (state.isNormalCube() && state.isBlockNormalCube() && state.isOpaqueCube() && state.causesSuffocation()) {
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
					if (block.getMaterial(defaultState) == Material.LEAVES) {
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
//			serverConfig.getConfigData().set("general.terrainSmoothable", new ArrayList<>(terrainSmoothable));
//			serverConfig.save();
		}
	}

	public static void removeTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				LOGGER.debug("Removing terrain smoothable: " + state);
				state.nocubes_setTerrainSmoothable(false);
				terrainSmoothable.remove(getStringFromState(state));
			}
//			serverConfig.getConfigData().set("general.terrainSmoothable", new ArrayList<>(terrainSmoothable));
//			serverConfig.save();
		}
	}

	public static void addLeavesSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			synchronized (leavesSmoothableBlocks) {
				for (final IBlockState originalState : states) {
					final Block block = originalState.getBlock();
					LOGGER.debug("Adding leaves smoothable block: " + block);
					for (final IBlockState state : block.getBlockState().getValidStates()) {
						LOGGER.debug("Adding leaves smoothable state: " + state);
						state.nocubes_setLeavesSmoothable(true);
					}
					leavesSmoothable.add(block.getRegistryName().toString());
					leavesSmoothableBlocks.add(block);
				}
			}
//			clientConfig.getConfigData().set("general.leavesSmoothable", new ArrayList<>(leavesSmoothable));
//			clientConfig.save();
		}
	}

	public static void removeLeavesSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			synchronized (leavesSmoothableBlocks) {
				for (final IBlockState originalState : states) {
					final Block block = originalState.getBlock();
					LOGGER.debug("Removing leaves smoothable block: " + block);
					for (final IBlockState state : block.getBlockState().getValidStates()) {
						LOGGER.debug("Removing leaves smoothable state: " + state);
						state.nocubes_setLeavesSmoothable(false);
					}
					leavesSmoothable.remove(block.getRegistryName().toString());
					leavesSmoothableBlocks.remove(block);
				}
			}
//			clientConfig.getConfigData().set("general.leavesSmoothable", new ArrayList<>(leavesSmoothable));
//			clientConfig.save();
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
				for (final IBlockState state : block.getBlockState().getValidStates()) {
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
			if (block == null) {
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
//		Preconditions.checkNotNull(state, "String to serialise must not be null");
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
	public static List<String> getDefaultTerrainSmoothable() {
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

//				LEAVES.getDefaultState().withProperty(BlockNewLeaf.VARIANT, OAK),
//				LEAVES.getDefaultState().withProperty(BlockNewLeaf.VARIANT, SPRUCE),
//				LEAVES.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BIRCH),
//				LEAVES.getDefaultState().withProperty(BlockNewLeaf.VARIANT, JUNGLE),
//				LEAVES.getDefaultState().withProperty(BlockNewLeaf.VARIANT, ACACIA),
//				LEAVES.getDefaultState().withProperty(BlockNewLeaf.VARIANT, DARK_OAK)

				LEAVES

		).stream().map(Block::getRegistryName).map(ResourceLocation::toString).collect(Collectors.toList());

		final List<String> moddedStates = Lists.newArrayList(

		);

		final ArrayList<String> finalStates = Lists.newArrayList();
		finalStates.addAll(vanillaStates);
		finalStates.addAll(moddedStates);
		return finalStates;
	}

//	public static void setTerrainCollisions(final boolean enabled) {
//		serverConfig.getConfigData().set("general.terrainCollisions", enabled);
//		serverConfig.save();
//	}
//
//	public static void setRenderSmoothTerrain(final boolean enabled) {
//		clientConfig.getConfigData().set("general.renderSmoothTerrain", enabled);
//		clientConfig.save();
//	}
//
//	public static void setRenderSmoothLeaves(final boolean enabled) {
//		clientConfig.getConfigData().set("general.renderSmoothLeaves", enabled);
//		clientConfig.save();
//	}
//
//	public static void setLeavesCollisions(final boolean enabled) {
//		serverConfig.getConfigData().set("general.leavesCollisions", enabled);
//		serverConfig.save();
//	}

}
