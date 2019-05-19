package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import io.github.cadiboo.nocubes.util.SmoothLeavesType;
import net.minecraft.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class Config {

	static final Logger LOGGER = LogManager.getLogger();
	static final HashSet<Block> leavesSmoothableBlocks = new HashSet<>();

	// Client
	public static boolean renderSmoothTerrain = true;
	public static boolean renderSmoothLeaves = false;
	public static MeshGeneratorType leavesMeshGenerator = MeshGeneratorType.SurfaceNets;
	public static Set<String> leavesSmoothable = new HashSet<>();
	public static SmoothLeavesType smoothLeavesType = SmoothLeavesType.TOGETHER;

	public static boolean renderExtendedFluids = true;

	public static boolean applyDiffuseLighting = true;

	public static boolean betterTextures = true;

	public static boolean smoothFluidLighting = true;
	public static boolean smoothFluidColors = true;
	public static boolean naturalFluidTextures = false;

	// Server
	public static ExtendFluidsRange extendFluidsRange = ExtendFluidsRange.OneBlock;

	public static MeshGeneratorType terrainMeshGenerator = MeshGeneratorType.SurfaceNets;
	public static boolean terrainCollisions = false;
	public static Set<String> terrainSmoothable = new HashSet<>();

	public static boolean leavesCollisions = false;

	public static HashSet<Block> getLeavesSmoothableBlocks() {
		return leavesSmoothableBlocks;
	}

}
