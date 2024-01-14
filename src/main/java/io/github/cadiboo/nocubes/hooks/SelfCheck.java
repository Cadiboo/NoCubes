package io.github.cadiboo.nocubes.hooks;

public final class SelfCheck {

	static boolean preIteration;
	static boolean preIterationSodium;
	static boolean getRenderFluidState;

	public static String[] info() {
		return new String[]{
			"preIteration hook called: " + preIteration,
			"preIterationSodium hook called: " + preIterationSodium,
			"getRenderFluidState hook called: " + getRenderFluidState,
		};
	}

}
