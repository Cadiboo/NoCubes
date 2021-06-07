package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.ThreadLocalArrayCache;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

/**
 * @author Cadiboo
 */
public final class LightCache implements AutoCloseable {

	public static final int MAX_BRIGHTNESS = LightTexture.pack(15, 15);
	private static final ThreadLocalArrayCache<int[]> CACHE = new ThreadLocalArrayCache<>(int[]::new, array -> array.length, LightCache::resetIntArray);

	public final BlockPos.Mutable mutablePos = new BlockPos.Mutable();
	public final BlockPos start;
	public final BlockPos size;
	private final ClientWorld world;
	private int[] array;

	public LightCache(ClientWorld world, BlockPos meshStart, BlockPos meshSize) {
		this.world = world;
		this.start = meshStart.offset(-1, -1, -1).immutable();
		this.size = meshSize.offset(2, 2, 2).immutable();
	}

	private static void resetIntArray(int[] array, int length) {
		Arrays.fill(array, 0, length, -1);
//		int[] resetArray = ClientUtil.NEGATIVE_1_8000;
//		int fillLength = resetArray.length;
//		for (int i = 0; i < length; i += fillLength)
//			System.arraycopy(resetArray, 0, array, i, length);
	}

	public MeshRenderer.PackedFaceLight get(BlockPos relativeTo, Face face, Vec normal, MeshRenderer.PackedFaceLight light) {
		light.v0 = get(relativeTo, face.v0, normal);
		light.v1 = get(relativeTo, face.v1, normal);
		light.v2 = get(relativeTo, face.v2, normal);
		light.v3 = get(relativeTo, face.v3, normal);
		return light;
	}

	public int get(BlockPos relativeTo, Vec vec, Vec normal) {
		BlockPos.Mutable pos = mutablePos;

		float posX = -0.5F + vec.x + MathHelper.clamp(normal.x * 4, -1, 1);
		float posY = -0.5F + vec.y + MathHelper.clamp(normal.y * 4, -1, 1);
		float posZ = -0.5F + vec.z + MathHelper.clamp(normal.z * 4, -1, 1);
		int relX = (int) (posX);
		int relY = (int) (posY);
		int relZ = (int) (posZ);
		int x = relativeTo.getX() + relX;
		int y = relativeTo.getY() + relY;
		int z = relativeTo.getZ() + relZ;

		int l000 = get(pos.set(x + 0, y + 0, z + 0));
		int l001 = get(pos.set(x + 0, y + 0, z + 1));
		int l010 = get(pos.set(x + 0, y + 1, z + 0));
		int l011 = get(pos.set(x + 0, y + 1, z + 1));
		int l100 = get(pos.set(x + 1, y + 0, z + 0));
		int l101 = get(pos.set(x + 1, y + 0, z + 1));
		int l110 = get(pos.set(x + 1, y + 1, z + 0));
		int l111 = get(pos.set(x + 1, y + 1, z + 1));

		// For debugging
		pos.set(x + 0, y + 0, z + 0);

		int s000 = LightTexture.sky(l000);
		int s001 = LightTexture.sky(l001);
		int s010 = LightTexture.sky(l010);
		int s011 = LightTexture.sky(l011);
		int s100 = LightTexture.sky(l100);
		int s101 = LightTexture.sky(l101);
		int s110 = LightTexture.sky(l110);
		int s111 = LightTexture.sky(l111);

		int b000 = LightTexture.block(l000);
		int b001 = LightTexture.block(l001);
		int b010 = LightTexture.block(l010);
		int b011 = LightTexture.block(l011);
		int b100 = LightTexture.block(l100);
		int b101 = LightTexture.block(l101);
		int b110 = LightTexture.block(l110);
		int b111 = LightTexture.block(l111);

		int maxBlock = max(b000, b001, b010, b011, b100, b101, b110, b111);
		int maxSky = max(s000, s001, s010, s011, s100, s101, s110, s111);

		return LightTexture.pack(maxBlock, maxSky);

//		// Try and remove 0 level lighting from calculations
//		int minUsableSky = maxSky - 1;
//		if (s000 < minUsableSky)
//			s000 = minUsableSky;
//		if (s001 < minUsableSky)
//			s001 = minUsableSky;
//		if (s010 < minUsableSky)
//			s010 = minUsableSky;
//		if (s011 < minUsableSky)
//			s011 = minUsableSky;
//		if (s100 < minUsableSky)
//			s100 = minUsableSky;
//		if (s101 < minUsableSky)
//			s101 = minUsableSky;
//		if (s110 < minUsableSky)
//			s110 = minUsableSky;
//		if (s111 < minUsableSky)
//			s111 = minUsableSky;
//
//		int minUsableBlock = maxBlock - 1;
//		if (b000 < minUsableBlock)
//			b000 = minUsableBlock;
//		if (b001 < minUsableBlock)
//			b001 = minUsableBlock;
//		if (b010 < minUsableBlock)
//			b010 = minUsableBlock;
//		if (b011 < minUsableBlock)
//			b011 = minUsableBlock;
//		if (b100 < minUsableBlock)
//			b100 = minUsableBlock;
//		if (b101 < minUsableBlock)
//			b101 = minUsableBlock;
//		if (b110 < minUsableBlock)
//			b110 = minUsableBlock;
//		if (b111 < minUsableBlock)
//			b111 = minUsableBlock;
//
//		float lerpX = posX - relX;
//		float lerpY = posY - relY;
//		float lerpZ = posZ - relZ;
//
//		int block = triLerp(b000, b001, b010, b011, b100, b101, b110, b111, lerpZ, lerpY, lerpX);
//		int sky = triLerp(s000, s001, s010, s011, s100, s101, s110, s111, lerpZ, lerpY, lerpX);
//		return LightTexture.pack(block, sky);
	}

	private static int max(int a, int b, int c, int d, int e, int f, int g, int h) {
		int max = a;
		if (b > max)
			max = b;
		if (c > max)
			max = c;
		if (d > max)
			max = d;
		if (e > max)
			max = e;
		if (f > max)
			max = f;
		if (g > max)
			max = g;
		if (h > max)
			max = h;
		return max;
	}

	private static int triLerp(
		int l000, int l001, int l010, int l011,
		int l100, int l101, int l110, int l111,
		float x, float y, float z
	) {
		return lerp(
			biLerp(l000, l001, l010, l011, y, z),
			biLerp(l100, l101, l110, l111, y, z),
			x
		);
	}

	private static int biLerp(int l00, int l10, int l01, int l11, float y, float z) {
		return lerp(
			lerp(l00, l10, y),
			lerp(l01, l11, y),
			z
		);
	}

	private static int lerp(int a, int b, float t) {
		return Math.round(a + t * (b - a));
	}

	private int get(BlockPos worldPos) {
		int index = indexIfInsideCache(worldPos);
		if (index == -1)
			return fetchCombinedLight(worldPos);

		int[] array = getArray();
		int light = array[index];
		if (light == -1)
			array[index] = light = fetchCombinedLight(worldPos);
		return light;
	}

	private int fetchCombinedLight(BlockPos worldPos) {
		ClientWorld world = this.world;
		BlockState state = world.getBlockState(worldPos);
		return WorldRenderer.getLightColor(world, state, worldPos);
	}

	private int[] getArray() {
		int[] array = this.array;
		if (array == null)
			this.array = array = CACHE.takeArray(numBlocks());
		return array;
	}

	private int numBlocks() {
		BlockPos size = this.size;
		return size.getX() * size.getY() * size.getZ();
	}

	private int indexIfInsideCache(BlockPos worldPos) {
		BlockPos start = this.start;
		BlockPos size = this.size;
		int x = worldPos.getX() - start.getX();
		int y = worldPos.getY() - start.getY();
		int z = worldPos.getZ() - start.getZ();
		if (x < 0 || x >= size.getX() || y < 0 || y >= size.getY() || z < 0 || z >= size.getZ())
			return -1; // Outside cache
		return ModUtil.get3dIndexInto1dArray(x, y, z, size.getX(), size.getY());
	}

	@Override
	public void close() {
	}

}
