package io.github.cadiboo.nocubes.hooks;

public final class SelfCheck {

	static boolean preIteration;
	static boolean getRenderFluidState;
	static boolean canBlockStateRender;
	static boolean renderBlockDamage;
	static boolean setBlocksDirty;
	static boolean canOccludeOverride;
	static boolean createFluidBlockRenderer;

	static boolean getCollisionShapeNoContextOverride;
	static boolean getCollisionShapeWithContextOverride;
	static boolean isCollisionShapeFullBlockOverride;
	static boolean hasLargeCollisionShapeOverride;
	static boolean isSuffocatingOverride;

	static boolean getFluidStateOverride;

	public static String[] info() {
		return new String[]{
			"preIteration hook called: " + preIteration,
			"getRenderFluidState hook called: " + getRenderFluidState,
			"canBlockStateRender hook called: " + canBlockStateRender,
			"renderBlockDamage hook called: " + renderBlockDamage,
			"setBlocksDirty hook called: " + setBlocksDirty,
			"canOccludeOverride hook called: " + canOccludeOverride,
			"createFluidBlockRenderer hook called: " + createFluidBlockRenderer,

			"getCollisionShapeOverride(NoContext) hook called: " + getCollisionShapeNoContextOverride,
			"getCollisionShapeOverride(WithContext) hook called: " + getCollisionShapeWithContextOverride,
			"isCollisionShapeFullBlockOverride hook called: " + isCollisionShapeFullBlockOverride,
			"hasLargeCollisionShapeOverride hook called: " + hasLargeCollisionShapeOverride,
			"isSuffocatingOverride hook called: " + isSuffocatingOverride,

			"getFluidStateOverride hook called: " + getFluidStateOverride,
		};
	}

}
