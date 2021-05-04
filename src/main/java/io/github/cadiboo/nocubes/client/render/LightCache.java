package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.ThreadLocalArrayCache;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.Arrays;

/**
 * @author Cadiboo
 */
public final class LightCache implements AutoCloseable {

	private static final ThreadLocalArrayCache<int[]> CACHE = new ThreadLocalArrayCache<>(int[]::new, array -> array.length, LightCache::resetIntArray);

	private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();
	private final ClientWorld world;
	private final BlockPos start;
	private final BlockPos size;
	private int[] array;

	public LightCache(ClientWorld world, BlockPos meshStart, BlockPos meshSize) {
		this.world = world;
		this.start = meshStart.offset(-1, -1, -1).immutable();
		this.size = meshSize.offset(2, 2, 2).immutable();
	}

	private static void resetIntArray(int length, int[] array) {
		Arrays.fill(array, 0, length, -1);
//		int[] resetArray = ClientUtil.NEGATIVE_1_8000;
//		int fillLength = resetArray.length;
//		for (int i = 0; i < length; i += fillLength)
//			System.arraycopy(resetArray, 0, array, i, length);
	}

	/**
	 * @param relativeTo Where this vertex/normal is relative to in world space (i.e. relativeTo + vec = worldPosOfVec)
	 * @return The position in world space to use to get light values for this vertex
	 */
	public BlockPos lightPos(BlockPos relativeTo, Vec vec, Vec normal) {
		return locateWorldLightPosFor(relativeTo, vec, normal, this.mutablePos);
	}

	/**
	 * @param relativeTo Where this vertex/normal is relative to in world space (i.e. relativeTo + vec = worldPosOfVec)
	 */
	public static BlockPos locateWorldLightPosFor(BlockPos relativeTo, Vec vec, Vec normal, BlockPos.Mutable toMove) {
		float vx = vec.x + MathHelper.clamp(normal.x * 10, -1, 1);
		float vy = vec.y + MathHelper.clamp(normal.y * 10, -1, 1);
		float vz = vec.z + MathHelper.clamp(normal.z * 10, -1, 1);

		int x = (int) Math.round(vx);
		int y = (int) Math.round(vy);
		int z = (int) Math.round(vz);
		toMove.set(relativeTo).move(x, y, z);
		return toMove;
	}

	/**
	 * @param relativeTo Where this vertex/normal is relative to in world space (i.e. relativeTo + vec = worldPosOfVec)
	 */
	public int get(BlockPos relativeTo, Vec vec, Vec normal) {
		return get(lightPos(relativeTo, vec, normal));
//		return get((int)(vx), (int)(vy), (int)(vz));
//		int x = (int) Math.ceil(vx);
//		int y = (int) Math.ceil(vy);
//		int z = (int) Math.ceil(vz);
//		int x = (int) (vx);
//		int y = (int) (vy);
//		int z = (int) (vz);
//
//		int l000 = get(x + 0, y + 0, z + 0);
//		int l001 = get(x + 0, y + 0, z + 1);
//		int l010 = get(x + 0, y + 1, z + 0);
//		int l011 = get(x + 0, y + 1, z + 1);
//		int l100 = get(x + 1, y + 0, z + 0);
//		int l101 = get(x + 1, y + 0, z + 1);
//		int l110 = get(x + 1, y + 1, z + 0);
//		int l111 = get(x + 1, y + 1, z + 1);
//
//		float lerpX = vx - x;
//		float lerpY = vy - y;
//		float lerpZ = vz - z;
//
//		int skyLight = triLerp(l000 >> 16, l001 >> 16, l010 >> 16, l011 >> 16, l100 >> 16, l101 >> 16, l110 >> 16, l111 >> 16, lerpZ, lerpY, lerpX);
//		int blockLight = triLerp(l000 & 0xFF, l001 & 0xFF, l010 & 0xFF, l011 & 0xFF, l100 & 0xFF, l101 & 0xFF, l110 & 0xFF, l111 & 0xFF, lerpZ, lerpY, lerpX);
//		return (skyLight << 16) | blockLight;
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
		return Math.round(a + t * (a - b));
	}

	public int get(BlockPos worldPos) {
		int index = index(worldPos);
		int[] array = getArray();

		if (index < 0 || index >= numBlocks()) // TODO: Shouldn't need this
			return fetchCombinedLight(worldPos);

		int color = array[index];
		if (color == -1)
			array[index] = color = fetchCombinedLight(worldPos);
		return color;
	}

	public int fetchCombinedLight(BlockPos worldPos) {
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

	public int numBlocks() {
		BlockPos size = this.size;
		return size.getX() * size.getY() * size.getZ();
	}

	private int index(BlockPos worldPos) {
		BlockPos start = this.start;
		int x = worldPos.getX() - start.getX();
		int y = worldPos.getY() - start.getY();
		int z = worldPos.getZ() - start.getZ();
		BlockPos size = this.size;
		return ModUtil.get3dIndexInto1dArray(x, y, z, size.getX(), size.getY());
	}

	@Override
	public void close() {
	}

}
