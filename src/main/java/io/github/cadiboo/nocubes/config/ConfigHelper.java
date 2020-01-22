package io.github.cadiboo.nocubes.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.api.NoCubesAPI;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import joptsimple.internal.Strings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MyceliumBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.block.SnowyDirtBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.ConnectionType;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.cadiboo.nocubes.NoCubes.LOGGER;
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

	private static final Marker CONFIG_MARKER = MarkerManager.getMarker("config");

	public static void discoverDefaultTerrainSmoothable() {
		// TODO: Get from world generators & ore generators
	}

	public static void discoverDefaultLeavesSmoothable() {
		// TODO: Get from tree generators
	}

	// Only call from logical Server.
	public static void setTerrainSmoothable(final BlockState state, final boolean newSmoothability) {
		final String type = newSmoothability ? "Adding" : "Removing";
		LOGGER.debug(CONFIG_MARKER, type + " terrain smoothable: " + state);
		state.nocubes_isTerrainSmoothable = newSmoothability;
		final NoCubesConfig.Server.ConfigImpl cfg = NoCubesConfig.Server.INSTANCE;
		setBlockState(state, newSmoothability, NoCubesConfig.Server.terrainSmoothableWhitelist, NoCubesConfig.Server.terrainSmoothableBlacklist, cfg.terrainSmoothableWhitelist.get(), cfg.terrainSmoothableBlacklist.get());
		saveAndLoad(ModConfig.Type.SERVER);
	}

	// Only call from logical Client.
	public static void setTerrainSmoothablePreference(final BlockState state, final boolean newSmoothability) {
		final String type = newSmoothability ? "Adding" : "Removing";
		LOGGER.debug(CONFIG_MARKER, type + " terrain smoothable preference: " + state);
		state.nocubes_isTerrainSmoothable = newSmoothability;
		final NoCubesConfig.Client.ConfigImpl cfg = NoCubesConfig.Client.INSTANCE;
		setBlockState(state, newSmoothability, NoCubesConfig.Client.terrainSmoothableWhitelistPreference, NoCubesConfig.Client.terrainSmoothableBlacklistPreference, cfg.terrainSmoothableWhitelistPreference.get(), cfg.terrainSmoothableBlacklistPreference.get());
		saveAndLoad(ModConfig.Type.CLIENT);
	}

	private static void setBlockState(final BlockState state, final boolean newState, final Set<BlockState> whiteListBaked, final Set<BlockState> blackListBaked, final List<String> whiteListRaw, final List<String> blackListRaw) {
		final String stateString = getStringFromBlockState(state);
		if (newState) {
			blackListBaked.remove(state);
			whiteListBaked.add(state);
			blackListRaw.remove(stateString);
			whiteListRaw.add(stateString);
		} else {
			whiteListBaked.remove(state);
			blackListBaked.add(state);
			whiteListRaw.remove(stateString);
			blackListRaw.add(stateString);
		}
	}

	/**
	 * @return The state or null if it could not be parsed
	 */
	@Nullable
	public static BlockState getBlockStateFromStringOrNull(@Nonnull final String stateString) {
		Preconditions.checkNotNull(stateString, "String to parse must not be null");
		try {
			return new BlockStateArgument().parse(new StringReader(stateString)).getState();
		} catch (final CommandSyntaxException e) {
			LOGGER.warn(CONFIG_MARKER, "Failed to parse blockstate \"{}\": {}", stateString, e.getMessage());
			return null;
		}
	}

	public static Set<BlockState> stringsToBlockStates(final Collection<? extends String> strings) {
		return strings.parallelStream()
				.map(ConfigHelper::getBlockStateFromStringOrNull)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	@Nonnull
	public static String getStringFromBlockState(@Nonnull final BlockState state) {
		Preconditions.checkNotNull(state, "State to serialise must not be null");
		String stateString = Objects.requireNonNull(state.getBlock().getRegistryName(), "Block registry name cannot be null!").toString();
		final List<String> properties = new ArrayList<>();
		state.getValues().forEach((property, value) -> properties.add(property.getName() + "=" + Util.getValueName(property, value)));
		if (!properties.isEmpty()) {
			stateString += "[";
			stateString += Strings.join(properties, ",");
			stateString += "]";
		}
		return stateString;
	}

	public static Set<String> blockStatesToStrings(final Collection<? extends BlockState> blockStates) {
		return blockStates.parallelStream()
				.map(ConfigHelper::getStringFromBlockState)
				.collect(Collectors.toSet());
	}

	@Nonnull
	static List<String> getDefaultTerrainSmoothable() {
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
				.map(ConfigHelper::getStringFromBlockState)
				.collect(Collectors.toList());

		// Some BlockStates from other mods that should be smoothable by default.
		// Mods can also add their own blocks via the API.
		final List<String> moddedStates = Lists.newArrayList();
		final ModList modList = ModList.get();
		if (modList.isLoaded("biomesoplenty"))
			Collections.addAll(moddedStates,
					"biomesoplenty:grass[snowy=false,variant=sandy]",
					"biomesoplenty:dirt[coarse=false,variant=sandy]",
					"biomesoplenty:white_sand",
					"biomesoplenty:grass[snowy=false,variant=silty]",
					"biomesoplenty:dirt[coarse=false,variant=loamy]",
					"biomesoplenty:grass[snowy=false,variant=loamy]",
					"biomesoplenty:dried_sand",
					"biomesoplenty:hard_ice",
					"biomesoplenty:mud[variant=mud]",
					"biomesoplenty:dirt[coarse=false,variant=silty]"
			);
		if (modList.isLoaded("chisel"))
			Collections.addAll(moddedStates,
					"chisel:marble2[variation=7]",
					"chisel:limestone2[variation=7]"
			);
		if (modList.isLoaded("dynamictrees"))
			Collections.addAll(moddedStates,
					"dynamictrees:rootydirtspecies[life=0]",
					"dynamictrees:rootysand[life=0]"
			);
		if (modList.isLoaded("iceandfire"))
			Collections.addAll(moddedStates,
					"iceandfire:ash",
					"iceandfire:sapphire_ore",
					"iceandfire:chared_grass",
					"iceandfire:chared_stone",
					"iceandfire:frozen_grass_path"
			);
		if (modList.isLoaded("notenoughroofs"))
			Collections.addAll(moddedStates,
					"notenoughroofs:copper_ore"
			);
		if (modList.isLoaded("rustic"))
			Collections.addAll(moddedStates,
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

		// Some BlockStates from other mods that should be smoothable by default.
		// Mods can also add their own blocks via the API.
		final List<String> moddedStates = Lists.newArrayList();

		final ArrayList<String> finalStates = Lists.newArrayList();
		finalStates.addAll(vanillaStates);
		finalStates.addAll(moddedStates);
		return finalStates;
	}

	// Only call on the logical Server!
	public static void setExtendFluidsRange(final ExtendFluidsRange newValue) {
//		NoCubesConfig.Server.INSTANCE.extendFluidsRange.set(newValue);
		saveAndLoad(ModConfig.Type.SERVER);
	}

	// Only call on the logical Server!
	public static void setTerrainMeshGenerator(final MeshGeneratorType newValue) {
		NoCubesConfig.Server.INSTANCE.terrainMeshGenerator.set(newValue);
		saveAndLoad(ModConfig.Type.SERVER);
	}

	// Only call on the logical Server!
	public static void setTerrainCollisions(final boolean newValue) {
		NoCubesConfig.Server.INSTANCE.terrainCollisions.set(newValue);
		saveAndLoad(ModConfig.Type.SERVER);
	}

	// Only call on the logical Client!
	public static void setRenderSmoothTerrain(final boolean newValue) {
		NoCubesConfig.Client.INSTANCE.renderSmoothTerrain.set(newValue);
		saveAndLoad(ModConfig.Type.CLIENT);
	}

	// Only call on the logical Client!
//	public static void setRenderSmoothLeaves(final boolean newValue) {
//		NoCubesConfig.Client.INSTANCE.renderSmoothLeaves.set(newValue);
//		saveAndLoad(ModConfig.Type.CLIENT);
//	}

	// Only call with correct type.
	public static void saveAndLoad(final ModConfig.Type type) {
		ConfigTracker_getConfig(NoCubes.MOD_ID, type).ifPresent(modConfig -> {
			modConfig.save();
			((CommentedFileConfig) modConfig.getConfigData()).load();
//			modConfig.fireEvent(new ModConfig.Reloading(modConfig));
			fireReloadEvent(modConfig);
		});
	}

	private static Optional<ModConfig> ConfigTracker_getConfig(final String modId, final ModConfig.Type type) {
		Map<String, Map<ModConfig.Type, ModConfig>> configsByMod = ObfuscationReflectionHelper.getPrivateValue(ConfigTracker.class, ConfigTracker.INSTANCE, "configsByMod");
		return Optional.ofNullable(configsByMod.getOrDefault(modId, Collections.emptyMap()).getOrDefault(type, null));
	}

	private static void fireReloadEvent(final ModConfig modConfig) {
		final ModContainer modContainer = ModList.get().getModContainerById(modConfig.getModId()).get();
		final ModConfig.ConfigReloading event;
		try {
			event = ObfuscationReflectionHelper.findConstructor(ModConfig.ConfigReloading.class, ModConfig.class).newInstance(modConfig);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		modContainer.dispatchConfigEvent(event);
	}

	public static void refreshTerrainSmoothableBlockStateFields() {
		ForgeRegistries.BLOCKS.getValues().parallelStream()
				.map(Block::getStateContainer)
				.map(StateContainer::getValidStates)
				.map(List::parallelStream)
				.forEach(stateStream -> stateStream.forEach(state -> {
					state.nocubes_isTerrainSmoothable = NoCubesConfig.Server.terrainSmoothableWhitelist.contains(state) && !NoCubesConfig.Server.terrainSmoothableBlacklist.contains(state);
				}));
	}

	/**
	 * Disables collisions if we are connected to a vanilla server.
	 */
	public static void performServerConnectionStatusValidation() {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			final ClientPlayNetHandler connection = Minecraft.getInstance().getConnection();
			if (connection == null || NetworkHooks.getConnectionType(connection::getNetworkManager) != ConnectionType.MODDED)
				NoCubesConfig.Server.terrainCollisions = false;
		});
	}

	public static void addApiAddedBlockStates() {
		NoCubesConfig.Server.terrainSmoothableWhitelist.addAll(NoCubesAPI.instance().getAddedTerrainSmoothableBlockStates());
	}

}
