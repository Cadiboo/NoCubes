package io.github.cadiboo.nocubes.hooks;

public final class SelfCheck {

	static boolean preIteration;
	static boolean canBlockStateRender;
	static boolean renderBlockDamage;
	static boolean getCollisionShapeNoContext;
	static boolean getCollisionShapeWithContext;
	static boolean isCollisionShapeFullBlock;
	static boolean hasLargeCollisionShape;
	static boolean canOcclude;
	static boolean markForRerender;

	public static String[] info() {
		return new String[]{
			"preIteration hook called: " + preIteration,
			"canBlockStateRender hook called: " + canBlockStateRender,
			"renderBlockDamage hook called: " + renderBlockDamage,
			"getCollisionShape(NoContext) hook called: " + getCollisionShapeNoContext,
			"getCollisionShape(WithContext) hook called: " + getCollisionShapeWithContext,
			"isCollisionShapeFullBlock hook called: " + isCollisionShapeFullBlock,
			"hasLargeCollisionShape hook called: " + hasLargeCollisionShape,
			"canOcclude hook called: " + canOcclude,
			"markForRerender hook called: " + markForRerender,
		};
	}

}
