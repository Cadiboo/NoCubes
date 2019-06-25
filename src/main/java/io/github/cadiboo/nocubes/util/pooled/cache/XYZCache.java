package io.github.cadiboo.nocubes.util.pooled.cache;

/**
 * @author Cadiboo
 */
public class XYZCache {

	// The distance between the start of the cache and the ChunkRenderPosition
	public int startPaddingX;
	public int startPaddingY;
	public int startPaddingZ;

	public int sizeX;
	public int sizeY;
	public int sizeZ;

	public XYZCache(
			final int startPaddingX, final int startPaddingY, final int startPaddingZ,
			final int sizeX, final int sizeY, final int sizeZ
	) {
		this.startPaddingX = startPaddingX;
		this.startPaddingY = startPaddingY;
		this.startPaddingZ = startPaddingZ;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}

	@Deprecated
	public int getIndex(final int x, final int y, final int z) {
		return getIndex(x, y, z, this.sizeX, this.sizeY);
	}

	public int getIndex(final int x, final int y, final int z, final int sizeX, final int sizeY) {
		// (width * height * z) + (width * y) + x
		// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
		return x + sizeX * (y + sizeY * z);
	}

}
