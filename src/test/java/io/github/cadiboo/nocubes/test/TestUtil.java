package io.github.cadiboo.nocubes.test;

import com.google.common.base.Preconditions;
import net.minecraftforge.common.util.Lazy;

/**
 * @author Cadiboo
 */
public final class TestUtil {

	public static final Lazy<Boolean> IS_CI_ENVIRONMENT = Lazy.concurrentOf(TestUtil::isCIEnvironment);

	private static boolean isCIEnvironment() {
		if (System.getenv("CI") != null)
			return true;
		if (System.getenv("CONTINUOUS_INTEGRATION") != null)
			return true;
		if (System.getenv("GITHUB_ACTIONS") != null)
			return true;
		if (System.getenv("BUILD_NUMBER") != null)
			return true;
		if (System.getenv("TRAVIS_BUILD_NUMBER") != null)
			return true;
		if (System.getenv("CIRCLE_BUILD_NUM") != null)
			return true;
		return false;
	}

	public static void assertFalse(final boolean b) {
		assertTrue(!b);
	}

	public static void assertTrue(final boolean b) {
		Preconditions.checkArgument(b);
	}

}
