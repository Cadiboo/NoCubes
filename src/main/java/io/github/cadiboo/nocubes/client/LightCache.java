package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.Area;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class LightCache implements AutoCloseable {

	private static final ThreadLocal<LightCache> POOL = ThreadLocal.withInitial(() -> new LightCache(0));
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);

	public int[] cache;
	private Area area;
	private boolean inUse;

	private LightCache(int size) {
		this.cache = new int[size];
		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, this.cache, 0, size);
		this.inUse = false;
	}

	@Nonnull
	public static LightCache retain(Area area) {
		final LightCache pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("LazyPackedLightCache is already in use!");
		}
		pooled.inUse = true;
		pooled.area = area;

		int size = area.size.getX() * area.size.getY() * area.size.getZ();

		if (pooled.cache.length >= size) {
			System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, size);
			return pooled;
		}

		pooled.cache = new int[size];
		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, size);
		return pooled;
	}

	@Deprecated
	public int get(final int x, final int y, final int z) {
		return get(x, y, z, MUTABLE_BLOCK_POS.get());
	}

	public int get(final int x, final int y, final int z, MutableBlockPos pos) {
		return get(x, y, z, this.cache, this.area, pos);
	}

	public static int get(
			final int x, final int y, final int z,
			final int[] cache,
			Area area,
			MutableBlockPos mutableBlockPos
	) {
		int index = area.indexIfInsideCache(x, y, z);
		if (index != -1) {
			int packedLight = cache[index];
			if (packedLight != -1)
				return packedLight;
		}

		IBlockState state = area.getBlockState(index, mutableBlockPos);
		int packedLight = state.getPackedLightmapCoords(
			area.world,
			mutableBlockPos.setPos(area.start).add(x, y, z)
		);
		if (index != -1)
			cache[index] = packedLight;
		return packedLight;
	}

	@Override
	public void close() {
		this.inUse = false;
	}

}
