package io.github.cadiboo.nocubes.hooks;

public final class SelfCheck {

	static boolean preIteration;
	static boolean getRenderFluidState;

	static boolean getCollisionShapeNoContextOverride;
	static boolean getCollisionShapeWithContextOverride;
	static boolean isCollisionShapeFullBlockOverride;
	static boolean hasLargeCollisionShapeOverride;
	static boolean isSuffocatingOverride;

	public static String[] info() {
		return new String[]{
			"preIteration hook called: " + preIteration,
			"getRenderFluidState hook called: " + getRenderFluidState,

			"getCollisionShapeOverride(NoContext) hook called: " + getCollisionShapeNoContextOverride,
			"getCollisionShapeOverride(WithContext) hook called: " + getCollisionShapeWithContextOverride,
			"isCollisionShapeFullBlockOverride hook called: " + isCollisionShapeFullBlockOverride,
			"hasLargeCollisionShapeOverride hook called: " + hasLargeCollisionShapeOverride,
			"isSuffocatingOverride hook called: " + isSuffocatingOverride,
		};
	}

}
