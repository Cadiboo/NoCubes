package io.github.cadiboo.nocubes.test;

import com.google.common.base.Preconditions;
import net.minecraftforge.common.util.Lazy;

/**
 * @author Cadiboo
 */
public final class TestUtil {

	public static final Lazy<Boolean> IS_CI_ENVIRONMENT = Lazy.concurrentOf(() -> Boolean.parseBoolean(System.getenv("CI")) ||
		Boolean.parseBoolean(System.getenv("CONTINUOUS_INTEGRATION")) ||
		Integer.parseInt(System.getenv("BUILD_NUMBER")) > 0 ||
		Integer.parseInt(System.getenv("TRAVIS_BUILD_NUMBER")) > 0 ||
		Integer.parseInt(System.getenv("CIRCLE_BUILD_NUM")) > 0
	);

	public static void assertFalse(final boolean b) {
		assertTrue(!b);
	}

	public static void assertTrue(final boolean b) {
		Preconditions.checkArgument(b);
	}

}
