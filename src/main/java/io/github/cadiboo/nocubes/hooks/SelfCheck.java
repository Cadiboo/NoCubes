package io.github.cadiboo.nocubes.hooks;

public final class SelfCheck {

	static boolean preIteration;
	static boolean canBlockStateRender;
	static boolean renderBlockDamage;
	static boolean getCollisionShapeNoContextOverride;
	static boolean getCollisionShapeWithContextOverride;
	static boolean isCollisionShapeFullBlockOverride;
	static boolean hasLargeCollisionShapeOverride;
	static boolean isSuffocatingOverride;
	static boolean canOccludeOverride;
	static boolean markForRerender;

	public static String[] info() {
		return new String[]{
			"preIteration hook called: " + preIteration,
			"canBlockStateRender hook called: " + canBlockStateRender,
			"renderBlockDamage hook called: " + renderBlockDamage,
			"getCollisionShapeOverride(NoContext) hook called: " + getCollisionShapeNoContextOverride,
			"getCollisionShapeOverride(WithContext) hook called: " + getCollisionShapeWithContextOverride,
			"isCollisionShapeFullBlockOverride hook called: " + isCollisionShapeFullBlockOverride,
			"hasLargeCollisionShapeOverride hook called: " + hasLargeCollisionShapeOverride,
			"isSuffocatingOverride hook called: " + isSuffocatingOverride,
			"canOccludeOverride hook called: " + canOccludeOverride,
			"markForRerender hook called: " + markForRerender,
		};
	}

}
