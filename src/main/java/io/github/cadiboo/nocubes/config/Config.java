package io.github.cadiboo.nocubes.config;

import com.google.common.collect.Sets;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import io.github.cadiboo.nocubes.util.SmoothLeavesType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class Config {

	static final Logger LOGGER = LogManager.getLogger();
	private static final HashSet<Block> leavesSmoothableBlocks = new HashSet<>();
	// Client
	public static boolean renderSmoothTerrain;
	public static boolean renderSmoothLeaves;
	public static MeshGenerator leavesMeshGenerator;
	public static Set<String> leavesSmoothable;
	public static SmoothLeavesType smoothLeavesType;
	public static boolean renderExtendedFluids;
	public static boolean smoothFluidLighting;
	public static boolean smoothFluidColors;
	public static boolean naturalFluidTextures;
	// Server
	public static ExtendFluidsRange extendFluidsRange;
	public static MeshGenerator terrainMeshGenerator;
	public static boolean terrainCollisions;
	public static Set<String> terrainSmoothable;
	// Internal Client
	private static ModConfig clientConfig;
	// Internal Server
	private static ModConfig serverConfig;

	public static HashSet<Block> getLeavesSmoothableBlocks() {
		return leavesSmoothableBlocks;
	}

	public static void bakeClient(final ModConfig config) {
		clientConfig = config;

		renderSmoothTerrain = ConfigHolder.CLIENT.renderSmoothTerrain.get();

		renderSmoothLeaves = ConfigHolder.CLIENT.renderSmoothLeaves.get();
		leavesMeshGenerator = ConfigHolder.CLIENT.leavesMeshGenerator.get();
		leavesSmoothable = Sets.newHashSet(ConfigHolder.CLIENT.leavesSmoothable.get());
		initLeavesSmoothable();
		smoothLeavesType = ConfigHolder.CLIENT.smoothLeavesType.get();
		renderExtendedFluids = ConfigHolder.CLIENT.renderExtendedFluids.get();

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

	public static void addTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				state.nocubes_setTerrainSmoothable(true);
				terrainSmoothable.add(state.toString());
			}
			serverConfig.getConfigData().set("terrainSmoothable", new ArrayList<>(terrainSmoothable));
			serverConfig.save();
		}
	}

	public static void removeTerrainSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			for (final IBlockState state : states) {
				state.nocubes_setTerrainSmoothable(false);
				terrainSmoothable.remove(state.toString());
			}
			serverConfig.getConfigData().set("terrainSmoothable", new ArrayList<>(terrainSmoothable));
			serverConfig.save();
		}
	}

	public static void addLeavesSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			synchronized (leavesSmoothableBlocks) {
				for (final IBlockState originalState : states) {
					final Block block = originalState.getBlock();
					for (final IBlockState state : block.getStateContainer().getValidStates()) {
						state.nocubes_setLeavesSmoothable(true);
					}
					leavesSmoothable.add(block.getRegistryName().toString());
					leavesSmoothableBlocks.add(block);
				}
			}
			clientConfig.getConfigData().set("leavesSmoothable", new ArrayList<>(leavesSmoothable));
			clientConfig.save();
		}
	}

	public static void removeLeavesSmoothable(final IBlockState... states) {
		if (states.length > 0) {
			synchronized (leavesSmoothableBlocks) {
				for (final IBlockState originalState : states) {
					final Block block = originalState.getBlock();
					for (final IBlockState state : block.getStateContainer().getValidStates()) {
						state.nocubes_setLeavesSmoothable(false);
					}
					leavesSmoothable.remove(block.getRegistryName().toString());
					leavesSmoothableBlocks.remove(block);
				}
			}
			clientConfig.getConfigData().set("leavesSmoothable", new ArrayList<>(leavesSmoothable));
			clientConfig.save();
		}
	}

	private static void initTerrainSmoothable() {
		for (final String stateString : terrainSmoothable) {
			final IBlockState state = getStateFromString(stateString);
			if (state != null) {
				state.nocubes_setTerrainSmoothable(true);
			}
		}
	}

	private static void initLeavesSmoothable() {
		for (final String stateString : leavesSmoothable) {
			final IBlockState state = getStateFromString(stateString);
			if (state != null) {
				state.nocubes_setTerrainSmoothable(true);
				leavesSmoothableBlocks.add(state.getBlock());
			}
		}
	}

	private static IBlockState getStateFromString(final String stateString) {
		try {
			return new BlockStateArgument().parse(new StringReader(stateString)).getState();
		} catch (final CommandSyntaxException e) {
			LOGGER.error("Failed to parse blockstate \"" + stateString + "\"!", e);
			return null;
		}
	}

}
