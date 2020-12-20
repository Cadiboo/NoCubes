package io.github.cadiboo.nocubes.util;

import net.minecraftforge.common.util.Lazy;

/**
 * @author Cadiboo
 */
public class ModUtil {

	public static final Lazy<Boolean> IS_DEVELOPER_WORKSPACE = Lazy.concurrentOf(() -> {
		final String target = System.getenv().get("target");
		if (target == null)
			return false;
		return target.contains("userdev");
	});

	public static int get3dIndexInto1dArray(int x, int y, int z, int xSize, int ySize) {
		return x + xSize * (y + ySize * (z));
	}

}
