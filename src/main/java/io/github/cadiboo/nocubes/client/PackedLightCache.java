package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.StateCache;
import io.github.cadiboo.nocubes.util.XYZCache;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
@SideOnly(Side.CLIENT)
public class PackedLightCache extends XYZCache {

	@Nonnull
	private final int[] packedLightCache;

	private PackedLightCache(final int sizeX, final int sizeY, final int sizeZ) {
		super(sizeX, sizeY, sizeZ);
		packedLightCache = new int[sizeX * sizeY * sizeZ];
	}

	@Nonnull
	public int[] getPackedLightCache() {
		return packedLightCache;
	}

	@Nonnull
	public static PackedLightCache retain(final int sizeX, final int sizeY, final int sizeZ) {
		return new PackedLightCache(sizeX, sizeY, sizeZ);
	}

}
