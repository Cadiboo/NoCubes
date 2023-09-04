package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.EnhancedRuntimeException;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class LazyPackedLightCache implements AutoCloseable {

	private static final ThreadLocal<LazyPackedLightCache> POOL = ThreadLocal.withInitial(() -> new LazyPackedLightCache(0));
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);

	public int[] cache;
	private Area area;
	private boolean inUse;

	private LazyPackedLightCache(int size) {
		this.cache = new int[size];
		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, this.cache, 0, size);
		this.inUse = false;
	}

	@Nonnull
	public static LazyPackedLightCache retain(Area area) {
		final LazyPackedLightCache pooled = POOL.get();

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

	private static class CustomArrayIndexOutOfBoundsException extends EnhancedRuntimeException {

		private final int x;
		private final int y;
		private final int z;
		private final int[] cache;
		private final int index;
		private final StateCache stateCache;
		private final IBlockAccess reader;
		private final MutableBlockPos mutableBlockPos;
		private final int chunkRenderPosX;
		private final int chunkRenderPosY;
		private final int chunkRenderPosZ;
		private final int startPaddingX;
		private final int startPaddingY;
		private final int startPaddingZ;
		private final int diffX;
		private final int diffY;
		private final int diffZ;

		CustomArrayIndexOutOfBoundsException(final int x, final int y, final int z, final int[] cache, final int index, final StateCache stateCache, final IBlockAccess reader, final MutableBlockPos mutableBlockPos, final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ, final int startPaddingX, final int startPaddingY, final int startPaddingZ, final int diffX, final int diffY, final int diffZ, final ArrayIndexOutOfBoundsException e) {
			super(e);
			this.x = x;
			this.y = y;
			this.z = z;
			this.cache = cache;
			this.index = index;
			this.stateCache = stateCache;
			this.reader = reader;
			this.mutableBlockPos = mutableBlockPos;
			this.chunkRenderPosX = chunkRenderPosX;
			this.chunkRenderPosY = chunkRenderPosY;
			this.chunkRenderPosZ = chunkRenderPosZ;
			this.startPaddingX = startPaddingX;
			this.startPaddingY = startPaddingY;
			this.startPaddingZ = startPaddingZ;
			this.diffX = diffX;
			this.diffY = diffY;
			this.diffZ = diffZ;
		}

		@Override
		protected void printStackTrace(final WrappedPrintStream stream) {
			stream.println("x: " + x);
			stream.println("y: " + y);
			stream.println("z: " + z);
			stream.println("cache: " + cache);
			stream.println("index: " + index);
			stream.println("stateCache: " + stateCache);
			stream.println("reader: " + reader);
			stream.println("mutableBlockPos: " + mutableBlockPos);
			stream.println("chunkRenderPosX: " + chunkRenderPosX);
			stream.println("chunkRenderPosY: " + chunkRenderPosY);
			stream.println("chunkRenderPosZ: " + chunkRenderPosZ);
			stream.println("startPaddingX: " + startPaddingX);
			stream.println("startPaddingY: " + startPaddingY);
			stream.println("startPaddingZ: " + startPaddingZ);
			stream.println("diffX: " + diffX);
			stream.println("diffY: " + diffY);
			stream.println("diffZ: " + diffZ);
		}

	}

}
