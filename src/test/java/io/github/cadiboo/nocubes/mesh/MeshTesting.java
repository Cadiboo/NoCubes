package io.github.cadiboo.nocubes.mesh;

/**
 * @author Cadiboo
 */
public final class MeshTesting {

	static final TestData TORUS = makeVolume(
			new float[][]{
					{-2.0F, 2.0F, 0.2F},
					{-2.0F, 2.0F, 0.2F},
					{-1.0F, 1.0F, 0.2F}
			},
			(x, y, z) -> (float) (Math.pow(1.0 - Math.sqrt(x * x + y * y), 2) + z * z - 0.25F)
	);
	static final TestData BIG_SPHERE = makeVolume(
			new float[][]{
					{-1.0F, 1.0F, 0.05F},
					{-1.0F, 1.0F, 0.05F},
					{-1.0F, 1.0F, 0.05F}
			},
			(x, y, z) -> x * x + y * y + z * z - 1.0F
	);
	static final TestData EMPTY = new MeshTesting.TestData(new float[32 * 32 * 32], new byte[]{32, 32, 32});
	static final TestData HYPERELLIPTIC = makeVolume(
			new float[][]{
					{-1.0F, 1.0F, 0.05F},
					{-1.0F, 1.0F, 0.05F},
					{-1.0F, 1.0F, 0.05F}
			},
			(x, y, z) -> (float) (Math.pow(Math.pow(x, 6) + Math.pow(y, 6) + Math.pow(z, 6), 1.0 / 6.0) - 1.0F)
	);
	static final TestData NODAL_CUBIC = makeVolume(
			new float[][]{
					{-2.0F, 2.0F, 0.05F},
					{-2.0F, 2.0F, 0.05F},
					{-2.0F, 2.0F, 0.05F}
			},
			(x, y, z) -> x * y + y * z + z * x + x * y * z
	);
	static final TestData GOURSATS_SURFACE = makeVolume(
			new float[][]{
					{-2.0F, 2.0F, 0.05F},
					{-2.0F, 2.0F, 0.05F},
					{-2.0F, 2.0F, 0.05F}
			},
			(x, y, z) -> (float) (Math.pow(x, 4) + Math.pow(y, 4) + Math.pow(z, 4) - 1.5F * (x * x + y * y + z * z) + 1F)
	);
	static final TestData HEART = makeVolume(
			new float[][]{
					{-2.0F, 2.0F, 0.05F},
					{-2.0F, 2.0F, 0.05F},
					{-2.0F, 2.0F, 0.05F}
			},
			(x, y, z) -> {
				y *= 1.5;
				z *= 1.5;
				return (float) (Math.pow(2 * x * x + y * y + 2 * z * z - 1, 3) - 0.1F * z * z * y * y * y - y * y * y * x * x);
			}
	);
	//	private static final TestData NORDSTRANDS_WEIRD_SURFACE = makeVolume(
//			new float[][]{
//					{-0.8F, 0.8F, 0.01F},
//					{-0.8F, 0.8F, 0.01F},
//					{-0.8F, 0.8F, 0.01F}
//			},
//			(x, y, z) -> (float) (25 * (Math.pow(x, 3) * (y + z) + Math.pow(y, 3) * (x + z) + Math.pow(z, 3) * (x + y)) +
//					50 * (x * x * y * y + x * x * z * z + y * y * z * z) -
//					125 * (x * x * y * z + y * y * x * z + z * z * x * y) +
//					60 * x * y * z -
//					4 * (x * y + x * z + y * z))
//	);
	static final TestData SINE_WAVES = makeVolume(
			new float[][]{
					{(float) (-Math.PI * 2F), (float) (Math.PI * 2F), (float) (Math.PI / 8F)},
					{(float) (-Math.PI * 2F), (float) (Math.PI * 2F), (float) (Math.PI / 8F)},
					{(float) (-Math.PI * 2F), (float) (Math.PI * 2F), (float) (Math.PI / 8F)}
			},
			(x, y, z) -> (float) (Math.sin(x) + Math.sin(y) + Math.sin(z))
	);
	static final TestData PERLIN_NOISE = makeVolume(
			new float[][]{
					{-5F, 5F, 0.25F},
					{-5F, 5F, 0.25F},
					{-5F, 5F, 0.25F}
			},
			(x, y, z) -> (float) (PerlinNoise.noise(x, y, z) - 0.5)
	);
	static final TestData ASTEROID = makeVolume(
			new float[][]{
					{-1F, 1F, 0.08F},
					{-1F, 1F, 0.08F},
					{-1F, 1F, 0.08F}
			},
			(x, y, z) -> (float) ((x * x + y * y + z * z) - PerlinNoise.noise(x * 2, y * 2, z * 2))
	);
	static final TestData TERRAIN = makeVolume(
			new float[][]{
					{-1F, 1F, 0.05F},
					{-1F, 1F, 0.05F},
					{-1F, 1F, 0.05F},
			},
			(x, y, z) -> (float) (y + PerlinNoise.noise(x * 2 + 5, y * 2 + 3, z * 2 + 0.6))
	);
	static final TestData SPHERE = makeVolume(
			new float[][]{
					{-1.0F, 1.0F, 0.25F},
					{-1.0F, 1.0F, 0.25F},
					{-1.0F, 1.0F, 0.25F}
			},
			(x, y, z) -> x * x + y * y + z * z - 1.0F
	);

	private static TestData makeVolume(float[][] dims, final ScalarFunction f) {
		byte[] res = new byte[3];
		for (int i = 0; i < 3; ++i) {
			res[i] = (byte) (2 + Math.ceil((dims[i][1] - dims[i][0]) / dims[i][2]));
		}
		float[] volume = new float[res[0] * res[1] * res[2]];
		int n = 0;
		for (int k = 0, z = (int) (dims[2][0] - dims[2][2]); k < res[2]; ++k, z += dims[2][2])
			for (int j = 0, y = (int) (dims[1][0] - dims[1][2]); j < res[1]; ++j, y += dims[1][2])
				for (int i = 0, x = (int) (dims[0][0] - dims[0][2]); i < res[0]; ++i, x += dims[0][2], ++n) {
					volume[n] = f.apply(x, y, z);
				}
		return new TestData(volume, res);
	}

	private interface ScalarFunction {

		float apply(int x, int y, int z);

	}

	static class TestData {

		final float[] data;
		final byte[] dims;

		public TestData(final float[] data, final byte[] dims) {
			this.data = data;
			this.dims = dims;
		}

	}

	// JAVA REFERENCE IMPLEMENTATION OF IMPROVED NOISE - COPYRIGHT 2002 KEN PERLIN.
	public static final class PerlinNoise {

		static final int[] p = new int[512];
		static final int[] permutation = {151, 160, 137, 91, 90, 15,
				131, 13, 201, 95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99, 37, 240, 21, 10, 23,
				190, 6, 148, 247, 120, 234, 75, 0, 26, 197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33,
				88, 237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74, 165, 71, 134, 139, 48, 27, 166,
				77, 146, 158, 231, 83, 111, 229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40, 244,
				102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76, 132, 187, 208, 89, 18, 169, 200, 196,
				135, 130, 116, 188, 159, 86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250, 124, 123,
				5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207, 206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42,
				223, 183, 170, 213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155, 167, 43, 172, 9,
				129, 22, 39, 253, 19, 98, 108, 110, 79, 113, 224, 232, 178, 185, 112, 104, 218, 246, 97, 228,
				251, 34, 242, 193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235, 249, 14, 239, 107,
				49, 192, 214, 31, 181, 199, 106, 157, 184, 84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254,
				138, 236, 205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66, 215, 61, 156, 180
		};
		static {
			for (int i = 0; i < 256; i++) p[256 + i] = p[i] = permutation[i];
		}

		static double noise(double x, double y, double z) {
			int X = (int) Math.floor(x) & 255,                  // FIND UNIT CUBE THAT
					Y = (int) Math.floor(y) & 255,                  // CONTAINS POINT.
					Z = (int) Math.floor(z) & 255;
			x -= Math.floor(x);                                // FIND RELATIVE X,Y,Z
			y -= Math.floor(y);                                // OF POINT IN CUBE.
			z -= Math.floor(z);
			double u = fade(x),                                // COMPUTE FADE CURVES
					v = fade(y),                                // FOR EACH OF X,Y,Z.
					w = fade(z);
			int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z,      // HASH COORDINATES OF
					B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;      // THE 8 CUBE CORNERS,

			return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z),  // AND ADD
					grad(p[BA], x - 1, y, z)), // BLENDED
					lerp(u, grad(p[AB], x, y - 1, z),  // RESULTS
							grad(p[BB], x - 1, y - 1, z))),// FROM  8
					lerp(v, lerp(u, grad(p[AA + 1], x, y, z - 1),  // CORNERS
							grad(p[BA + 1], x - 1, y, z - 1)), // OF CUBE
							lerp(u, grad(p[AB + 1], x, y - 1, z - 1),
									grad(p[BB + 1], x - 1, y - 1, z - 1))));
		}

		static double fade(double t) {
			return t * t * t * (t * (t * 6 - 15) + 10);
		}

		static double lerp(double t, double a, double b) {
			return a + t * (b - a);
		}

		static double grad(int hash, double x, double y, double z) {
			int h = hash & 15;                      // CONVERT LO 4 BITS OF HASH CODE
			double u = h < 8 ? x : y,                 // INTO 12 GRADIENT DIRECTIONS.
					v = h < 4 ? y : h == 12 || h == 14 ? x : z;
			return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
		}

	}

}

