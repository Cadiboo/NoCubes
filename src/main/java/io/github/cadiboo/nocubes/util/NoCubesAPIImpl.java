package io.github.cadiboo.nocubes.util;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.api.NoCubesAPI;
import io.github.cadiboo.nocubes.api.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Default Implementation of {@link NoCubesAPI}.
 * Used internally - Mods interacting with NoCubes should not have any reason to use this.
 *
 * @author Cadiboo
 */
@Beta
public class NoCubesAPIImpl implements NoCubesAPI {

	private static final Set<BlockState> ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES = new HashSet<>();
	private static boolean canAddMeshGenerators = true;
	private static boolean canAddBlocks = true;

	public static boolean isInvalidBlockState(final BlockState state) {
		return state == null || state == Blocks.AIR.getDefaultState();
	}

	public static void ensureCanAddBlockState() {
		if (!NoCubesAPI.instance().canAddBlockState())
			throw new IllegalStateException("Too late to register a BlockState as smoothable!");
	}

	/**
	 * Called by NoCubes when it is too late to add MeshGenerators anymore.
	 * Do not call this method.
	 */
	public static void disableAddingMeshGenerators() {
		if (!NoCubesAPI.instance().canAddMeshGenerator())
			return;
		NoCubesAPI.instance().preDisableAddingMeshGenerators();
		canAddMeshGenerators = false;
		NoCubes.LOGGER.debug("Finalised API added MeshGenerators:");
		for (final MeshGeneratorType type : MeshGeneratorType.getValues())
			NoCubes.LOGGER.debug(type.name() + ", " + type.toString() + ", " + type.getMeshGenerator());
	}

	/**
	 * Called by NoCubes when it is too late to add MeshGenerators anymore.
	 * Do not call this method.
	 */
	public static void disableAddingBlockStates() {
		if (!NoCubesAPI.instance().canAddBlockState())
			return;
		NoCubesAPI.instance().preDisableAddingBlockStates();
		canAddBlocks = false;
		NoCubes.LOGGER.info("Finalised API added smoothables:");
		for (final BlockState blockState : NoCubesAPI.instance().getAddedTerrainSmoothableBlockStates())
			NoCubes.LOGGER.info(ConfigHelper.getStringFromBlockState(blockState));
	}

	private void ensureCanAddMeshGenerator() {
		if (!canAddMeshGenerator())
			throw new IllegalStateException("Too late to add MeshGenerator!");
	}

	/**
	 * @return If it is still possible to add a BlockState to NoCubes' smoothable list.
	 */
	@Override
	public boolean canAddBlockState() {
		return canAddBlocks;
	}

	/**
	 * Validates and copies the list of addedTerrainSmoothableBlockStates.
	 *
	 * @return An immutable copy of the addedTerrainSmoothableBlockStates
	 */
	@Override
	public Set<BlockState> getAddedTerrainSmoothableBlockStates() {
		ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.removeIf(NoCubesAPIImpl::isInvalidBlockState);
		return ImmutableSet.<BlockState>builder().addAll(ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES).build();
	}

	/**
	 * Registers a {@link BlockState} as being terrain smoothable.
	 */
	@Override
	public void addTerrainSmoothableBlockState(final BlockState state) {
		ensureCanAddBlockState();
		ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.add(state);
	}

	/**
	 * Registers multiple {@link BlockState}s as being terrain smoothable.
	 */
	@Override
	public void addTerrainSmoothableBlockStates(final BlockState... states) {
		ensureCanAddBlockState();
		ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.addAll(Arrays.asList(states));
	}

	/**
	 * Registers all the states for a {@link Block} as being terrain smoothable.
	 */
	@Override
	public void addTerrainSmoothableBlock(final Block block) {
		ensureCanAddBlockState();
		ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.addAll(block.getStateContainer().getValidStates());
	}

	/**
	 * Registers all the states for multiple {@link Block}s as being terrain smoothable.
	 */
	@Override
	public void addTerrainSmoothableBlocks(final Block... blocks) {
		ensureCanAddBlockState();
		for (final Block block : blocks)
			ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.addAll(block.getStateContainer().getValidStates());
	}

	/**
	 * @return If it is still possible to add a MeshGenerator to NoCubes.
	 */
	@Override
	public boolean canAddMeshGenerator() {
		return canAddMeshGenerators;
	}

	/**
	 * Adds a MeshGenerator to NoCubes. Must be called before the config is loaded.
	 *
	 * @param registryName  The registry name of the MeshGenerator to add. Should contain your ModId as the namespace
	 * @param name          The human readable name of the MeshGenerator to add
	 * @param meshGenerator the MeshGenerator to add
	 */
	@Override
	public void addMeshGenerator(final ResourceLocation registryName, final String name, final MeshGenerator meshGenerator) {
		ensureCanAddMeshGenerator();
		MeshGeneratorType.create(registryName.toString().replace(':', '_').toUpperCase(), name, meshGenerator);
	}

}
