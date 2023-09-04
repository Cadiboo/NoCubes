package io.github.cadiboo.nocubes.config;

import io.github.cadiboo.nocubes.mesh.CullingChamfer;
import io.github.cadiboo.nocubes.mesh.CullingCubic;
import io.github.cadiboo.nocubes.mesh.MarchingCubes;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.mesh.OldNoCubes;
import io.github.cadiboo.nocubes.mesh.StupidCubic;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.util.ExtendFluidsRange;
import io.github.cadiboo.nocubes.util.SmoothLeavesType;
import net.minecraft.block.Block;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cadiboo
 */
@SuppressWarnings("WeakerAccess")
public final class Config {

	@Nonnull
	public static final HashSet<Block> leavesSmoothableBlocks = new HashSet<>(); // Client

	@Nonnull
	static final Logger LOGGER = LogManager.getLogger("NoCubes Config");

	// Client
	public static boolean renderSmoothTerrain;
	public static boolean renderSmoothLeaves;
	public static boolean renderSmoothAndVanillaLeaves;
	public static Mesher leavesMeshGenerator;
	public static Set<String> leavesSmoothable;
	public static SmoothLeavesType smoothLeavesType;

	public static boolean applyDiffuseLighting;

	public static boolean betterTextures;

	public static boolean shortGrass;

	public static boolean smoothFluidLighting;
	public static boolean smoothFluidColors;
	public static boolean naturalFluidTextures;

	// Server
	public static ExtendFluidsRange extendFluidsRange;

	public static Mesher terrainMeshGenerator;
	public static boolean terrainCollisions;
	public static Set<String> terrainSmoothable;
	public static boolean forceVisuals;

	public static boolean oldNoCubesSlopes = false;
	public static float oldNoCubesRoughness = 0;

	public enum MesherType {
		SurfaceNets(new SurfaceNets(false)),
		OldNoCubes(new OldNoCubes()),
		Debug_SurfaceNets2xSmoothness(new SurfaceNets(true)),
		Debug_MarchingCubes(new MarchingCubes(false)),
		Debug_MarchingCubes2xSmoothness(new MarchingCubes(true)),
		Debug_CullingCubic(new CullingCubic()),
		Debug_StupidCubic(new StupidCubic()),
		Debug_CullingChamfer(new CullingChamfer()),
		;

		public final Mesher instance;

		MesherType(Mesher instance) {
			this.instance = instance;
		}
	}

}
