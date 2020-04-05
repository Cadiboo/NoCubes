package io.github.cadiboo.nocubes.test;

import com.google.common.base.Preconditions;
import net.minecraftforge.common.util.Lazy;

/**
 * @author Cadiboo
 */
public final class TestUtil {

	public static void assertFalse(final boolean b) {
		assertTrue(!b);
	}

	public static void assertTrue(final boolean b) {
		Preconditions.checkArgument(b);
	}

	public static final Lazy<Boolean> IS_CI_ENVIRONMENT = Lazy.concurrentOf(() -> Boolean.parseBoolean(System.getenv("BUILD_NUMBER")) || Boolean.parseBoolean(System.getenv("TRAVIS_BUILD_NUMBER")) || Boolean.parseBoolean(System.getenv("CIRCLE_BUILD_NUM")));

}
