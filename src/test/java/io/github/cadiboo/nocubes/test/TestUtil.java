package io.github.cadiboo.nocubes.test;

import com.google.common.base.Preconditions;

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

}
