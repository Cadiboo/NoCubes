package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ThreadLocalArrayCache;
import io.github.cadiboo.nocubes.util.Vec;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.EnhancedRuntimeException;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class LightCache implements AutoCloseable {

	private static final ThreadLocalArrayCache<int[]> CACHE = new ThreadLocalArrayCache<>(int[]::new, array -> array.length, ClientUtil::resetIntArray);

	private final MutableBlockPos mutablePos = new MutableBlockPos();
	private final Area area;
	private int[] array;

	public LightCache(Area area) {
		this.area = area;
	}

	public int get(Vec vec, Vec normal) {
		float vx = vec.x - normal.x;
		float vy = vec.y - normal.y;
		float vz = vec.z - normal.z;
		return get(Math.round(vx), Math.round(vy), Math.round(vz));
//		int x = (int) vx;
//		int y = (int) vy;
//		int z = (int) vz;
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


	/**
	 * x, y & z are relative to the start of the area.
	 */
	public int get(int x, int y, int z) {
		int index = index(x, y, z);
		int[] array = getArray();

		if (index >= array.length)
			return 0;

		int color = array[index];
		if (color == -1) {
			MutableBlockPos unsetWorldPos = this.mutablePos;
			BlockPos start = area.start;
			unsetWorldPos.setPos(start.getX() + x, start.getY() + y, start.getZ() + z);
			array[index] = color = compute(index, unsetWorldPos);
		}
		return color;
	}

	private int compute(int index, MutableBlockPos worldPos) {
		Area area = this.area;
		return area.getAndCacheBlocks()[index].getPackedLightmapCoords(area.world, worldPos);
	}

	private int[] getArray() {
		int[] array = this.array;
		if (array == null)
			this.array = array = CACHE.takeArray(area.getLength());
		return array;
	}

	private int index(int x, int y, int z) {
		BlockPos size = area.size;
		return size.getX() * size.getY() * z + size.getX() * y + x;
	}

	@Override
	public void close() {
	}

}
