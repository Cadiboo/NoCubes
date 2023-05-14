package io.github.cadiboo.nocubes.hooks;

public final class SelfCheck {

	static boolean preIteration;
	static boolean getRenderFluidState;

	public static String[] info() {
		return new String[]{
			"preIteration hook called: " + preIteration,
			"getRenderFluidState hook called: " + getRenderFluidState,
		};
	}

}
