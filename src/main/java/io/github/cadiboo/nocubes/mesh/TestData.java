package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;

import java.util.Random;

public interface TestData {

	final class TestMesh {
		private final Vec dimX;
		private final Vec dimY;
		private final Vec dimZ;
		private final DistanceFunction distanceFunction;
		public final BlockPos dimensions;

		private TestMesh(Vec dimX, Vec dimY, Vec dimZ, DistanceFunction distanceFunction) {
			this.dimX = dimX;
			this.dimY = dimY;
			this.dimZ = dimZ;
			this.distanceFunction = distanceFunction;
			this.dimensions = new BlockPos(getAxisSize(dimX), getAxisSize(dimY), getAxisSize(dimZ));
		}

		public float[] generateDistanceField(int posX, int posY, int posZ) {
			PerlinNoise.setOffset(posX, posY, posZ);
			float[] distanceField = new float[ModUtil.length(dimensions)];
			int index = 0;
			for (float k = 0, z = dimZ.x - dimZ.z; k < dimensions.getZ(); ++k, z += dimZ.z)
				for (float j = 0, y = dimY.x - dimY.z; j < dimensions.getY(); ++j, y += dimY.z)
					for (float i = 0, x = dimX.x - dimX.z; i < dimensions.getX(); ++i, x += dimX.z, ++index)
						distanceField[index] = distanceFunction.apply(x, y, z);
			return distanceField;
		}

		private static int getAxisSize(Vec point) {
			return 2 + (int) Math.ceil((point.y - point.x) / point.z);
		}
	}

	final class PerlinNoise {

		static final class NoiseConfig {
			int offsetX, offsetY, offsetZ;
			SimplexNoise generator = new SimplexNoise(new Random(0));
		}

		private static final ThreadLocal<NoiseConfig> CONFIG = ThreadLocal.withInitial(NoiseConfig::new);

		public static void setOffset(int posX, int posY, int posZ) {
			NoiseConfig config = CONFIG.get();
			config.offsetX = posX;
			config.offsetY = posY;
			config.offsetZ = posZ;
		}

		public static float noise(float x, float y, float z) {
			NoiseConfig config = CONFIG.get();
			x += config.offsetX;
			y += config.offsetY;
			z += config.offsetZ;
			return (float) config.generator.getValue(x, y, z) / 32F;
		}
	}

	interface DistanceFunction {
		float apply(float x, float y, float z);
	}

	static TestMesh makeVolume(Vec dimX, Vec dimY, Vec dimZ, DistanceFunction func) {
		return new TestMesh(dimX, dimY, dimZ, func);
	}

	TestMesh SPHERE = makeVolume(
		new Vec(-1.0F, 1.0F, 0.25F),
		new Vec(-1.0F, 1.0F, 0.25F),
		new Vec(-1.0F, 1.0F, 0.25F),
		(x, y, z) -> x * x + y * y + z * z - 1.0F
	);

	TestMesh TORUS = makeVolume(
		new Vec(-2.0F, 2.0F, 0.2F),
		new Vec(-2.0F, 2.0F, 0.2F),
		new Vec(-1.0F, 1.0F, 0.2F),
		(x, y, z) -> (float) Math.pow(1.0F - Math.sqrt(x * x + y * y), 2) + z * z - 0.25F
	);


	TestMesh DONUT = makeVolume(
		new Vec(-2.0F, 2.0F, 0.2F),
		new Vec(-1.0F, 1.0F, 0.2F),
		new Vec(-2.0F, 2.0F, 0.2F),
		(x, y, z) -> (float) Math.pow(1.0F - Math.sqrt(x * x + z * z), 2) + y * y - 0.25F
	);

	TestMesh BIG_SPHERE = makeVolume(
		new Vec(-1.0F, 1.0F, 0.05F),
		new Vec(-1.0F, 1.0F, 0.05F),
		new Vec(-1.0F, 1.0F, 0.05F),
		(x, y, z) -> x * x + y * y + z * z - 1.0F
	);

	TestMesh HYPER_ELLIPTIC = makeVolume(
		new Vec(-1.0F, 1.0F, 0.05F),
		new Vec(-1.0F, 1.0F, 0.05F),
		new Vec(-1.0F, 1.0F, 0.05F),
		(x, y, z) -> (float) (Math.pow(Math.pow(x, 6) + Math.pow(y, 6) + Math.pow(z, 6), 1.0 / 6.0) - 1.0F)
	);

	TestMesh NODAL_CUBIC = makeVolume(
		new Vec(-2.0F, 2.0F, 0.05F),
		new Vec(-2.0F, 2.0F, 0.05F),
		new Vec(-2.0F, 2.0F, 0.05F),
		(x, y, z) -> x * y + y * z + z * x + x * y * z
	);

	TestMesh GOURSATS_SURFACE = makeVolume(
		new Vec(-2.0F, 2.0F, 0.05F),
		new Vec(-2.0F, 2.0F, 0.05F),
		new Vec(-2.0F, 2.0F, 0.05F),
		(x, y, z) -> (float) (Math.pow(x, 4) + Math.pow(y, 4) + Math.pow(z, 4) - 1.5 * (x * x + y * y + z * z) + 1)
	);

	TestMesh HEART = makeVolume(
		new Vec(-2.0F, 2.0F, 0.05F),
		new Vec(-2.0F, 2.0F, 0.05F),
		new Vec(-2.0F, 2.0F, 0.05F),
		(x, y, z) -> {
			y *= 1.5;
			z *= 1.5;
			return (float) (Math.pow(2 * x * x + y * y + 2 * z * z - 1, 3) - 0.1 * z * z * y * y * y - y * y * y * x * x);
		}
	);

	TestMesh NORDSTRANDS_WEIRD_SURFACE = makeVolume(
		new Vec(-0.8F, 0.8F, 0.01F),
		new Vec(-0.8F, 0.8F, 0.01F),
		new Vec(-0.8F, 0.8F, 0.01F),
		(x, y, z) -> (float) (25 * (Math.pow(x, 3) * (y + z) + Math.pow(y, 3) * (x + z) + Math.pow(z, 3) * (x + y)) +
			50 * (x * x * y * y + x * x * z * z + y * y * z * z) -
			125 * (x * x * y * z + y * y * x * z + z * z * x * y) +
			60 * x * y * z -
			4 * (x * y + x * z + y * z))
	);

	float PI = (float) Math.PI;
	TestMesh SINE_WAVES = makeVolume(
		new Vec(-PI * 2F, PI * 2F, PI / 8F),
		new Vec(-PI * 2F, PI * 2F, PI / 8F),
		new Vec(-PI * 2F, PI * 2F, PI / 8F),
		(x, y, z) -> (float) (Math.sin(x) + Math.sin(y) + Math.sin(z))
	);

	TestMesh PERLIN_NOISE = makeVolume(
		new Vec(-5F, 5F, 0.25F),
		new Vec(-5F, 5F, 0.25F),
		new Vec(-5F, 5F, 0.25F),
		(x, y, z) -> PerlinNoise.noise(x, y, z) - 0.5F
	);

	TestMesh ASTEROID = makeVolume(
		new Vec(-1F, 1F, 0.08F),
		new Vec(-1F, 1F, 0.08F),
		new Vec(-1F, 1F, 0.08F),
		(x, y, z) -> (x * x + y * y + z * z) - PerlinNoise.noise(x * 2, y * 2, z * 2)
	);

	TestMesh TERRAIN = makeVolume(
		new Vec(-1F, 1F, 0.05F),
		new Vec(-1F, 1F, 0.05F),
		new Vec(-1F, 1F, 0.05F),
		(x, y, z) -> y + PerlinNoise.noise(x * 2 + 5, y * 2 + 3, z * 2 + 0.6F)
	);

	static float distanceFromConvexPlanes(float[][] planes, float[][] planeOffsets, float x, float y, float z) {
		float maxDistance = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < planes.length; i++) {
			float x_ = x - planeOffsets[i][0];
			float y_ = y - planeOffsets[i][1];
			float z_ = z - planeOffsets[i][2];

			float dotProduct = planes[i][0] * x_ + planes[i][1] * y_ + planes[i][2] * z_;
			maxDistance = Math.max(maxDistance, dotProduct);
		}
		return maxDistance;
	}

	TestMesh PYRAMID = makeVolume(
		new Vec(-1F, 1F, 0.125F),
		new Vec(-1F, 1F, 0.125F),
		new Vec(-1F, 1F, 0.125F),
		(x, y, z) -> {
			float ROOT_3 = (float) Math.sqrt(3);
			float[][] planes = {
				{-ROOT_3, ROOT_3, -ROOT_3},
				{-ROOT_3, ROOT_3, ROOT_3},
				{ROOT_3, ROOT_3, -ROOT_3},
				{ROOT_3, ROOT_3, ROOT_3},
			};
			float[][] planeOffsets = {
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
				{0, 0, 0},
			};
			return distanceFromConvexPlanes(planes, planeOffsets, x, y, z);
		}
	);

	TestMesh HALF_OFFSET_PYRAMID = makeVolume(
		new Vec(-1, 1, 0.125F),
		new Vec(-1, 1, 0.125F),
		new Vec(-1, 1, 0.125F),
		(x, y, z) -> {
			float ROOT_3 = (float) Math.sqrt(3);
			float[][] planes = {
				{-ROOT_3, ROOT_3, -ROOT_3},
				{-ROOT_3, ROOT_3, ROOT_3},
				{ROOT_3, ROOT_3, -ROOT_3},
				{ROOT_3, ROOT_3, ROOT_3},
			};
			float[][] planeOffsets = {
				{0.0625F, 0.0625F, 0.0625F},
				{0.0625F, 0.0625F, 0.0625F},
				{0.0625F, 0.0625F, 0.0625F},
				{0.0625F, 0.0625F, 0.0625F},
			};
			return distanceFromConvexPlanes(planes, planeOffsets, x, y, z);
		}
	);

	TestMesh TETRAHEDRON = makeVolume(
		new Vec(-1F, 1F, 0.125F),
		new Vec(-1F, 1F, 0.125F),
		new Vec(-1F, 1F, 0.125F),
		(x, y, z) -> {
			float INV_ROOT_3 = (float) (Math.sqrt(3) / 3);

			float[][] planes = {
				{INV_ROOT_3, INV_ROOT_3, INV_ROOT_3},
				{-INV_ROOT_3, -INV_ROOT_3, INV_ROOT_3},
				{INV_ROOT_3, -INV_ROOT_3, -INV_ROOT_3},
				{-INV_ROOT_3, INV_ROOT_3, -INV_ROOT_3},
			};
			float[][] planeOffsets = {
				{0.25F, 0.25F, 0.25F},
				{-0.25F, -0.25F, 0.25F},
				{0.25F, -0.25F, -0.25F},
				{-0.25F, 0.25F, -0.25F},
			};
			return distanceFromConvexPlanes(planes, planeOffsets, x, y, z);
		}
	);

	TestMesh HALF_OFFSET_TETRAHEDRON = makeVolume(
		new Vec(-1, 1, 0.125F),
		new Vec(-1, 1, 0.125F),
		new Vec(-1, 1, 0.125F),
		(x, y, z) -> {
			float INV_ROOT_3 = (float) (Math.sqrt(3) / 3);

			float[][] planes = {
				{INV_ROOT_3, INV_ROOT_3, INV_ROOT_3},
				{-INV_ROOT_3, -INV_ROOT_3, INV_ROOT_3},
				{INV_ROOT_3, -INV_ROOT_3, -INV_ROOT_3},
				{-INV_ROOT_3, INV_ROOT_3, -INV_ROOT_3},
			};
			float[][] planeOffsets = {
				{0.3125F, 0.3125F, 0.3125F},
				{-0.3125F, -0.3125F, 0.3125F},
				{0.3125F, -0.3125F, -0.3125F},
				{-0.3125F, 0.3125F, -0.3125F},
			};
			return distanceFromConvexPlanes(planes, planeOffsets, x, y, z);
		}
	);

}
