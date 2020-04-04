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

}
