package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.api.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.generator.OldNoCubes;
import net.minecraftforge.common.IExtensibleEnum;

/**
 * Would be nice not to use the IExtensibleEnum hack but I need it to be an enum for the config.
 *
 * @author Cadiboo
 */
public enum MeshGeneratorType implements IExtensibleEnum {

//	SURFACE_NETS("Surface Nets", new SurfaceNets()),
//	MARCHING_CUBES("Marching Cubes", new MarchingCubes()),
//	MARCHING_TETRAHEDRA("Marching Tetrahedra", new MarchingTetrahedra()),

	OLD_NO_CUBES("Old NoCubes", new OldNoCubes());

	public static final MeshGeneratorType[] VALUES = values();
	public static final int VALUES_LENGTH = VALUES.length;

	private final String name;
	private final MeshGenerator meshGenerator;

	MeshGeneratorType(final String name, final MeshGenerator meshGenerator) {
		this.name = name;
		this.meshGenerator = meshGenerator;
	}

	public static MeshGeneratorType create(String enumName, String name, MeshGenerator meshGenerator) {
		throw new IllegalStateException("Enum not extended");
	}

	@Override
	public String toString() {
		return name;
	}

	public MeshGenerator getMeshGenerator() {
		return meshGenerator;
	}

}
