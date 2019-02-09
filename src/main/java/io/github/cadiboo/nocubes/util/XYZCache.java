package io.github.cadiboo.nocubes.util;

/**
 * @author Cadiboo
 */
public class XYZCache {

	public final int sizeX;
	public final int sizeY;
	public final int sizeZ;

	public XYZCache(final int sizeX, final int sizeY, final int sizeZ) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}

	public int getIndex(final int x, final int y, final int z) {
		// Flat[x + WIDTH * (y + HEIGHT * z)] = Original[x, y, z]
		return x + sizeX * (y + sizeY * z);
	}

}
