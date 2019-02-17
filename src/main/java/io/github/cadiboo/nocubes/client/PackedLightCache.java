package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.util.XYZCache;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * @author Cadiboo
 */
@OnlyIn(Dist.CLIENT)
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
		// STOPSHIP: 2019-02-13 FIXME TODO POOLED CACHES
		return new PackedLightCache(sizeX, sizeY, sizeZ);
	}

}
