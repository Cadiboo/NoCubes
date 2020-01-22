package io.github.cadiboo.nocubes.api;

import io.github.cadiboo.nocubes.api.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.NoCubesAPIImpl;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

/**
 * NoCubes' public API.
 *
 * @author Cadiboo
 */
public interface NoCubesAPI {

	static NoCubesAPI instance() {
		return InstanceHolder.INSTANCE;
	}

	/**
	 * @return If it is still possible to add a BlockState to NoCubes' smoothable list.
	 */
	boolean canAddBlockState();

	/**
	 * Validates and copies the list of addedTerrainSmoothableBlockStates.
	 *
	 * @return An immutable copy of the addedTerrainSmoothableBlockStates
	 */
	Set<BlockState> getAddedTerrainSmoothableBlockStates();

	/**
	 * Registers a {@link BlockState} as being terrain smoothable.
	 */
	void addTerrainSmoothableBlockState(final BlockState state);

	/**
	 * Registers multiple {@link BlockState}s as being terrain smoothable.
	 */
	void addTerrainSmoothableBlockStates(final BlockState... states);

	/**
	 * Registers all the states for a {@link Block} as being terrain smoothable.
	 */
	void addTerrainSmoothableBlock(final Block block);

	/**
	 * Registers all the states for multiple {@link Block}s as being terrain smoothable.
	 */
	void addTerrainSmoothableBlocks(final Block... blocks);

	/**
	 * Called right before adding MeshGenerators is disabled.
	 */
	default void preDisableAddingMeshGenerators() {
	}

	/**
	 * Called right before adding BlockStates is disabled.
	 */
	default void preDisableAddingBlockStates() {
	}

	/**
	 * @return If it is still possible to add a MeshGenerator to NoCubes.
	 */
	boolean canAddMeshGenerator();

	/**
	 * Adds a MeshGenerator to NoCubes. Must be called before the config is loaded.
	 *
	 * @param registryName  The registry name of the MeshGenerator to add. Should contain your ModId as the namespace
	 * @param name          The human readable name of the MeshGenerator to add
	 * @param meshGenerator the MeshGenerator to add
	 * @throws IllegalStateException If it is too late to add a MeshGenerator
	 */
	void addMeshGenerator(final ResourceLocation registryName, final String name, final MeshGenerator meshGenerator);

	class InstanceHolder {

		private static final NoCubesAPI INSTANCE = new NoCubesAPIImpl();

	}

}
