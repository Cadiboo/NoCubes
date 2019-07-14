package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.pooled.cache.StateCache;
import io.github.cadiboo.nocubes.util.pooled.cache.XYZCache;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.fml.common.EnhancedRuntimeException;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
public final class LazyPackedLightCache extends XYZCache implements AutoCloseable {

	private static final ThreadLocal<LazyPackedLightCache> POOL = ThreadLocal.withInitial(() -> new LazyPackedLightCache(0, 0, 0, 0, 0, 0));
	private static final ThreadLocal<MutableBlockPos> MUTABLE_BLOCK_POS = ThreadLocal.withInitial(MutableBlockPos::new);
	@Nonnull
	public IEnviromentBlockReader reader;
	@Nonnull
	public StateCache stateCache;
	@Nonnull
	public int[] cache;
	private int chunkRenderPosX;
	private int chunkRenderPosY;
	private int chunkRenderPosZ;
	private boolean inUse;

	private LazyPackedLightCache(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {
		super(startPaddingX, startPaddingY, startPaddingZ, sizeX, sizeY, sizeZ);
		final int size = sizeX * sizeY * sizeZ;
		this.cache = new int[size];
		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, this.cache, 0, size);
		this.inUse = false;
	}

	@Nonnull
	public static LazyPackedLightCache retain(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ,
			@Nonnull final IEnviromentBlockReader reader,
			@Nonnull final StateCache stateCache,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ
	) {

		final LazyPackedLightCache pooled = POOL.get();

		if (pooled.inUse) {
			throw new IllegalStateException("LazyPackedLightCache is already in use!");
		}
		pooled.inUse = true;

		pooled.reader = reader;
		pooled.stateCache = stateCache;

		pooled.chunkRenderPosX = chunkRenderPosX;
		pooled.chunkRenderPosY = chunkRenderPosY;
		pooled.chunkRenderPosZ = chunkRenderPosZ;

		pooled.startPaddingX = startPaddingX;
		pooled.startPaddingY = startPaddingY;
		pooled.startPaddingZ = startPaddingZ;

		final int size = sizeX * sizeY * sizeZ;

		if (pooled.sizeX == sizeX && pooled.sizeY == sizeY && pooled.sizeZ == sizeZ) {
			System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, size);
			return pooled;
		}

		pooled.sizeX = sizeX;
		pooled.sizeY = sizeY;
		pooled.sizeZ = sizeZ;

		if (pooled.cache.length < size || pooled.cache.length > size * 1.25F) {
			pooled.cache = new int[size];
		}

		System.arraycopy(ClientUtil.NEGATIVE_1_8000, 0, pooled.cache, 0, size);

		return pooled;
	}

	public static int get(
			final int x, final int y, final int z,
			final int[] cache,
			final int index,
			final StateCache stateCache, final IEnviromentBlockReader reader,
			final MutableBlockPos mutableBlockPos,
			final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int diffX, final int diffY, final int diffZ,
			final int stateCacheSizeX, final int stateCacheSizeY
	) {
		try {
			int packedLight = cache[index];
			if (packedLight == -1) {
				try {
					packedLight = stateCache.getBlockStates()[stateCache.getIndex(
							x + diffX,
							y + diffY,
							z + diffZ,
							stateCacheSizeX, stateCacheSizeY
					)].getPackedLightmapCoords(
							reader,
							mutableBlockPos.setPos(
									chunkRenderPosX + x - startPaddingX,
									chunkRenderPosY + y - startPaddingY,
									chunkRenderPosZ + z - startPaddingZ
							)
					);
				} catch (final ArrayIndexOutOfBoundsException e) {
					throw new CustomArrayIndexOutOfBoundsException.StateCacheException(
							x, y, z,
							cache,
							index,
							stateCache, reader,
							mutableBlockPos,
							chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
							startPaddingX, startPaddingY, startPaddingZ,
							diffX, diffY, diffZ,
							e,
							x + diffX,
							y + diffY,
							z + diffZ,
							stateCacheSizeX,
							stateCacheSizeY,
							stateCache.getIndex(
									x + diffX,
									y + diffY,
									z + diffZ,
									stateCacheSizeX, stateCacheSizeY
							)
					);
				}
				cache[index] = packedLight;
				if (packedLight == -1) LogManager.getLogger().error("BARRRF");
			}
			return packedLight;
		} catch (final ArrayIndexOutOfBoundsException e) {
			throw new CustomArrayIndexOutOfBoundsException(
					x, y, z,
					cache,
					index,
					stateCache, reader,
					mutableBlockPos,
					chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
					startPaddingX, startPaddingY, startPaddingZ,
					diffX, diffY, diffZ,
					e
			);
		}
	}

	@Deprecated
	public int get(final int x, final int y, final int z) {
		return get(x, y, z, this.cache, this.stateCache, this.reader, MUTABLE_BLOCK_POS.get(), this.chunkRenderPosX, this.chunkRenderPosY, this.chunkRenderPosZ, this.startPaddingX, this.startPaddingY, this.startPaddingZ, this.stateCache.startPaddingX - this.startPaddingX, this.stateCache.startPaddingY - this.startPaddingY, this.stateCache.startPaddingZ - this.startPaddingZ);
	}

	@Deprecated
	public int get(final int x, final int y, final int z, final int[] cache, final StateCache stateCache, final IEnviromentBlockReader reader, final MutableBlockPos mutableBlockPos, final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ, final int startPaddingX, final int startPaddingY, final int startPaddingZ, final int diffX, final int diffY, final int diffZ) {
		return get(x, y, z, cache, getIndex(x, y, z, this.sizeX, this.sizeY), stateCache, reader, mutableBlockPos, chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ, startPaddingX, startPaddingY, startPaddingZ, diffX, diffY, diffZ, stateCache.sizeX, stateCache.sizeY);
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
		private final IEnviromentBlockReader reader;
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

		CustomArrayIndexOutOfBoundsException(
				final int x, final int y, final int z,
				final int[] cache,
				final int index,
				final StateCache stateCache,
				final IEnviromentBlockReader reader,
				final MutableBlockPos mutableBlockPos,
				final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
				final int startPaddingX, final int startPaddingY, final int startPaddingZ,
				final int diffX, final int diffY, final int diffZ,
				final ArrayIndexOutOfBoundsException e
		) {
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

		public static class StateCacheException extends CustomArrayIndexOutOfBoundsException {

			private final int xPdiffX;
			private final int yPdiffY;
			private final int zPdiffZ;
			private final int stateCacheSizeX;
			private final int stateCacheSizeY;
			private final int stateCacheIndex;

			StateCacheException(
					final int x, final int y, final int z,
					final int[] cache,
					final int index,
					final StateCache stateCache,
					final IEnviromentBlockReader reader,
					final MutableBlockPos mutableBlockPos,
					final int chunkRenderPosX, final int chunkRenderPosY, final int chunkRenderPosZ,
					final int startPaddingX, final int startPaddingY, final int startPaddingZ,
					final int diffX, final int diffY, final int diffZ,
					final ArrayIndexOutOfBoundsException e,
					final int xPdiffX,
					final int yPdiffY,
					final int zPdiffZ,
					final int stateCacheSizeX,
					final int stateCacheSizeY,
					final int stateCacheIndex
			) {
				super(
						x, y, z,
						cache,
						index,
						stateCache,
						reader,
						mutableBlockPos,
						chunkRenderPosX, chunkRenderPosY, chunkRenderPosZ,
						startPaddingX, startPaddingY, startPaddingZ,
						diffX, diffY, diffZ,
						e
				);
				this.xPdiffX = xPdiffX;
				this.yPdiffY = yPdiffY;
				this.zPdiffZ = zPdiffZ;
				this.stateCacheSizeX = stateCacheSizeX;
				this.stateCacheSizeY = stateCacheSizeY;
				this.stateCacheIndex = stateCacheIndex;
			}

			@Override
			protected void printStackTrace(final WrappedPrintStream stream) {
				super.printStackTrace();
				stream.println("x + diffX: " + xPdiffX);
				stream.println("y + diffY: " + yPdiffY);
				stream.println("z + diffZ: " + zPdiffZ);
				stream.println("stateCacheSizeX: " + stateCacheSizeX);
				stream.println("stateCacheSizeY: " + stateCacheSizeY);
				stream.println("stateCacheIndex: " + stateCacheIndex);
			}

		}

	}

}
