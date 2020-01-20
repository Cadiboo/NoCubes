package io.github.cadiboo.nocubes.api;

import com.google.common.collect.ImmutableSet;
import io.github.cadiboo.nocubes.api.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * NoCubes' public API.
 *
 * @author Cadiboo
 */
public final class NoCubesAPI {

	private static final Set<BlockState> ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES = new HashSet<>();
	private static boolean canAddMeshGenerators = true;
	private static boolean canAddBlocks = true;

	private NoCubesAPI() {
	}

	/**
	 * @return If it is still possible to add a MeshGenerator to NoCubes.
	 */
	public static boolean canAddMeshGenerator() {
		return canAddMeshGenerators;
	}

	/**
	 * Adds a MeshGenerator to NoCubes. Must be called before the config is loaded.
	 *
	 * @param registryName  The registry name of the MeshGenerator to add. Should contain your ModId as the namespace
	 * @param name          The human readable name of the MeshGenerator to add
	 * @param meshGenerator the MeshGenerator to add
	 */
	public static void addMeshGenerator(final ResourceLocation registryName, final String name, final MeshGenerator meshGenerator) {
		ensureCanAddMeshGenerator();
		MeshGeneratorType.create(registryName.toString().replace(':', '_').toUpperCase(), name, meshGenerator);
	}

	private static void ensureCanAddMeshGenerator() {
		if (!canAddMeshGenerator())
			throw new IllegalStateException("Too late to add MeshGenerator!");
	}

	/**
	 * Called by NoCubes when it is too late to add MeshGenerators anymore.
	 * Do not call this method.
	 */
	public static void disableAddingMeshGenerators() {
		NoCubesAPI.canAddMeshGenerators = false;
	}

	/**
	 * Validates and copies the list of addedTerrainSmoothableBlockStates.
	 *
	 * @return An immutable copy of the addedTerrainSmoothableBlockStates
	 */
	public static Set<BlockState> getAddedTerrainSmoothableBlockStates() {
		ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.removeIf(NoCubesAPI::isInvalidBlockState);
		return ImmutableSet.<BlockState>builder().addAll(ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES).build();
	}

	private static boolean isInvalidBlockState(final BlockState state) {
		return state == null || state == Blocks.AIR.getDefaultState();
	}

	private static void ensureCanAddBlockState() {
		if (!canAddBlocks())
			throw new IllegalStateException("Too late to register a BlockState as smoothable!");
	}

	/**
	 * Registers a {@link BlockState} as being terrain smoothable.
	 */
	public static void addTerrainSmoothableBlockState(final BlockState state) {
		ensureCanAddBlockState();
		ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.add(state);
	}

	/**
	 * Registers multiple {@link BlockState}s as being terrain smoothable.
	 */
	public static void addTerrainSmoothableBlockStates(final BlockState... states) {
		ensureCanAddBlockState();
		ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.addAll(Arrays.asList(states));
	}

	/**
	 * Registers all the states for a {@link Block} as being terrain smoothable.
	 */
	public static void addTerrainSmoothableBlock(final Block block) {
		ensureCanAddBlockState();
		ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.addAll(block.getStateContainer().getValidStates());
	}

	/**
	 * Registers all the states for multiple {@link Block}s as being terrain smoothable.
	 */
	public static void addTerrainSmoothableBlocks(final Block... blocks) {
		ensureCanAddBlockState();
		for (final Block block : blocks)
			ADDED_TERRAIN_SMOOTHABLE_BLOCK_STATES.addAll(block.getStateContainer().getValidStates());
	}

	/**
	 * @return If it is still possible to add a BlockState to NoCubes' smoothable list.
	 */
	public static boolean canAddBlocks() {
		return canAddBlocks;
	}

	/**
	 * Called by NoCubes when it is too late to add MeshGenerators anymore.
	 * Do not call this method.
	 */
	public static void disableAddingBlocks() {
		NoCubesAPI.canAddBlocks = false;
	}

	public static void preDisableAddingMeshGenerators() {
	}

	public static void preDisableAddingBlocks() {
	}

}
