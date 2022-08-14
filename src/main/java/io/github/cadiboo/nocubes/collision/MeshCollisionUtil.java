package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public final class MeshCollisionUtil {

	private static int roundAvg(double d0, double d1, double d2, double d3) {
		return (int) ((Math.round(d0) + Math.round(d1) + Math.round(d2) + Math.round(d3)) / 4D);
	}

	//hmmm
	static int floorAvg(double d0, double d1, double d2, double d3) {
		return MathHelper.floor((d0 + d1 + d2 + d3) / 4D);
	}

	//hmmm
	private static int average(final double d0, final double d1, final double d2, final double d3) {
		return (int) ((d0 + d1 + d2 + d3) / 4);
	}

	public static AxisAlignedBB makeShape(Vec3 centre, Vec3 averageOfNormal, Vec3 v) {
		double w = centre.x - v.x;
		if (-0.01 < w && w < 0.01)
			w = 0.0625 * averageOfNormal.x;
		double h = centre.y - v.y;
		if (-0.01 < h && h < 0.01)
			h = 0.0625 * averageOfNormal.y;
		double l = centre.z - v.z;
		if (-0.01 < l && l < 0.01)
			l = 0.0625 * averageOfNormal.z;
		return new AxisAlignedBB(
				v.x, v.y, v.z,
				v.x + w, v.y + h, v.z + l
		);
	}

	static void addShapeToListIfIntersects(List<AxisAlignedBB> outShapes, AxisAlignedBB shape, AxisAlignedBB checkIntersects){
		if (shape.intersects(checkIntersects))
			outShapes.add(shape);
	}

	static void addIntersectingFaceShapesToList(
			final List<AxisAlignedBB> outShapes,
			final Face face,
			final ModProfiler profiler,
			final double maxYLevel,
			final float shapeRadius,
			final Predicate<AxisAlignedBB> doesShapeIntersect,
			final boolean ignoreIntersects
	) {

		//0___3
		//_____
		//_____
		//_____
		//1___2
		final Vec3 v0;
		final Vec3 v1;
		final Vec3 v2;
		final Vec3 v3;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final Vec3 v0v1;
		final Vec3 v1v2;
		final Vec3 v2v3;
		final Vec3 v3v0;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final Vec3 v0v1v0;
//		final Vec3 v0v1v1;
//		final Vec3 v1v2v1;
//		final Vec3 v1v2v2;
//		final Vec3 v2v3v2;
//		final Vec3 v2v3v3;
//		final Vec3 v3v0v3;
//		final Vec3 v3v0v0;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final Vec3 v0v1v1v2;
		final Vec3 v1v2v2v3;
		final Vec3 v2v3v3v0;
		final Vec3 v3v0v0v1;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3;
//		final Vec3 v1v2v2v3v2v3v3v0;
//		final Vec3 v2v3v3v0v3v0v0v1;
//		final Vec3 v3v0v0v1v0v1v1v2;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1;
//		final Vec3 v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2;

		try (ModProfiler ignored = profiler.start("interpolate")) {
			v0 = face.getVertex0();
			v1 = face.getVertex1();
			v2 = face.getVertex2();
			v3 = face.getVertex3();
			v0v1 = interp(v0, v1, 0.5F);
			v1v2 = interp(v1, v2, 0.5F);
			v2v3 = interp(v2, v3, 0.5F);
			v3v0 = interp(v3, v0, 0.5F);
//			v0v1v0 = interp(v0v1, v0, 0.5F);
//			v0v1v1 = interp(v0v1, v1, 0.5F);
//			v1v2v1 = interp(v1v2, v1, 0.5F);
//			v1v2v2 = interp(v1v2, v2, 0.5F);
//			v2v3v2 = interp(v2v3, v2, 0.5F);
//			v2v3v3 = interp(v2v3, v3, 0.5F);
//			v3v0v3 = interp(v3v0, v3, 0.5F);
//			v3v0v0 = interp(v3v0, v0, 0.5F);
			v0v1v1v2 = interp(v0v1, v1v2, 0.5F);
			v1v2v2v3 = interp(v1v2, v2v3, 0.5F);
			v2v3v3v0 = interp(v2v3, v3v0, 0.5F);
			v3v0v0v1 = interp(v3v0, v0v1, 0.5F);
//			v0v1v1v2v1v2v2v3 = interp(v0v1v1v2, v1v2v2v3, 0.5F);
//			v1v2v2v3v2v3v3v0 = interp(v1v2v2v3, v2v3v3v0, 0.5F);
//			v2v3v3v0v3v0v0v1 = interp(v2v3v3v0, v3v0v0v1, 0.5F);
//			v3v0v0v1v0v1v1v2 = interp(v3v0v0v1, v0v1v1v2, 0.5F);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1 = interp(v0v1v1v2v1v2v2v3, v2v3v3v0v3v0v0v1, 0.5F);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2 = interp(v1v2v2v3v2v3v3v0, v3v0v0v1v0v1v1v2, 0.5F);
		}

		//0___3
		//_____
		//_____
		//_____
		//1___2
//		final AxisAlignedBB v0shape;
//		final AxisAlignedBB v1shape;
//		final AxisAlignedBB v2shape;
//		final AxisAlignedBB v3shape;
		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final AxisAlignedBB v0v1shape;
		final AxisAlignedBB v1v2shape;
		final AxisAlignedBB v2v3shape;
		final AxisAlignedBB v3v0shape;
//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final AxisAlignedBB v0v1v0shape;
//		final AxisAlignedBB v0v1v1shape;
//		final AxisAlignedBB v1v2v1shape;
//		final AxisAlignedBB v1v2v2shape;
//		final AxisAlignedBB v2v3v2shape;
//		final AxisAlignedBB v2v3v3shape;
//		final AxisAlignedBB v3v0v3shape;
//		final AxisAlignedBB v3v0v0shape;
		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final AxisAlignedBB v0v1v1v2shape;
		final AxisAlignedBB v1v2v2v3shape;
		final AxisAlignedBB v2v3v3v0shape;
		final AxisAlignedBB v3v0v0v1shape;
//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final AxisAlignedBB v0v1v1v2v1v2v2v3shape;
//		final AxisAlignedBB v1v2v2v3v2v3v3v0shape;
//		final AxisAlignedBB v2v3v3v0v3v0v0v1shape;
//		final AxisAlignedBB v3v0v0v1v0v1v1v2shape;
//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final AxisAlignedBB v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1shape;
//		final AxisAlignedBB v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2shape;

		try (ModProfiler ignored = profiler.start("createAxisAlignedBBs")) {
//			v0shape = createAxisAlignedBBForVertex(v0, shapeRadius, maxYLevel);
//			v1shape = createAxisAlignedBBForVertex(v1, shapeRadius, maxYLevel);
//			v2shape = createAxisAlignedBBForVertex(v2, shapeRadius, maxYLevel);
//			v3shape = createAxisAlignedBBForVertex(v3, shapeRadius, maxYLevel);
			v0v1shape = createAxisAlignedBBForVertex(v0v1, shapeRadius, maxYLevel);
			v1v2shape = createAxisAlignedBBForVertex(v1v2, shapeRadius, maxYLevel);
			v2v3shape = createAxisAlignedBBForVertex(v2v3, shapeRadius, maxYLevel);
			v3v0shape = createAxisAlignedBBForVertex(v3v0, shapeRadius, maxYLevel);
//			v0v1v0shape = createAxisAlignedBBForVertex(v0v1v0, shapeRadius, maxYLevel);
//			v0v1v1shape = createAxisAlignedBBForVertex(v0v1v1, shapeRadius, maxYLevel);
//			v1v2v1shape = createAxisAlignedBBForVertex(v1v2v1, shapeRadius, maxYLevel);
//			v1v2v2shape = createAxisAlignedBBForVertex(v1v2v2, shapeRadius, maxYLevel);
//			v2v3v2shape = createAxisAlignedBBForVertex(v2v3v2, shapeRadius, maxYLevel);
//			v2v3v3shape = createAxisAlignedBBForVertex(v2v3v3, shapeRadius, maxYLevel);
//			v3v0v3shape = createAxisAlignedBBForVertex(v3v0v3, shapeRadius, maxYLevel);
//			v3v0v0shape = createAxisAlignedBBForVertex(v3v0v0, shapeRadius, maxYLevel);
			v0v1v1v2shape = createAxisAlignedBBForVertex(v0v1v1v2, shapeRadius, maxYLevel);
			v1v2v2v3shape = createAxisAlignedBBForVertex(v1v2v2v3, shapeRadius, maxYLevel);
			v2v3v3v0shape = createAxisAlignedBBForVertex(v2v3v3v0, shapeRadius, maxYLevel);
			v3v0v0v1shape = createAxisAlignedBBForVertex(v3v0v0v1, shapeRadius, maxYLevel);
//			v0v1v1v2v1v2v2v3shape = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3, shapeRadius, maxYLevel);
//			v1v2v2v3v2v3v3v0shape = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0, shapeRadius, maxYLevel);
//			v2v3v3v0v3v0v0v1shape = createAxisAlignedBBForVertex(v2v3v3v0v3v0v0v1, shapeRadius, maxYLevel);
//			v3v0v0v1v0v1v1v2shape = createAxisAlignedBBForVertex(v3v0v0v1v0v1v1v2, shapeRadius, maxYLevel);
//			v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1shape = createAxisAlignedBBForVertex(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1, shapeRadius, maxYLevel);
//			v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2shape = createAxisAlignedBBForVertex(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2, shapeRadius, maxYLevel);
		}

		try (ModProfiler ignored = profiler.start("addAxisAlignedBBs")) {
//			addCollisionShapeToList(outShapes, v0shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v2shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v3shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v0v1shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v1v2shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v2v3shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v3v0shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v0v1v0shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v0v1v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1v2v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1v2v2shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v2v3v2shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v2v3v3shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v3v0v3shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v3v0v0shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v0v1v1v2shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v1v2v2v3shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v2v3v3v0shape, doesShapeIntersect, ignoreIntersects);
			addCollisionShapeToList(outShapes, v3v0v0v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v0v1v1v2v1v2v2v3shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1v2v2v3v2v3v3v0shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v2v3v3v0v3v0v0v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v3v0v0v1v0v1v1v2shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1shape, doesShapeIntersect, ignoreIntersects);
//			addCollisionShapeToList(outShapes, v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2shape, doesShapeIntersect, ignoreIntersects);
		}

		//DO NOT CLOSE original face vectors
		{
//			v0.close();
//			v1.close();
//			v2.close();
//			v3.close();
		}
		v0v1.close();
		v1v2.close();
		v2v3.close();
		v3v0.close();
//		v0v1v0.close();
//		v0v1v1.close();
//		v1v2v1.close();
//		v1v2v2.close();
//		v2v3v2.close();
//		v2v3v3.close();
//		v3v0v3.close();
//		v3v0v0.close();
		v0v1v1v2.close();
		v1v2v2v3.close();
		v2v3v3v0.close();
		v3v0v0v1.close();
//		v0v1v1v2v1v2v2v3.close();
//		v1v2v2v3v2v3v3v0.close();
//		v2v3v3v0v3v0v0v1.close();
//		v3v0v0v1v0v1v1v2.close();
//		v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1.close();
//		v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2.close();

	}

	private static void addCollisionShapeToList(
			final List<AxisAlignedBB> collidingShapes,
			final AxisAlignedBB shape,
			final Predicate<AxisAlignedBB> doesShapeIntersect,
			final boolean ignoreIntersects
	) {
		if (ignoreIntersects || doesShapeIntersect.test(shape)) {
			collidingShapes.add(shape);
		}
	}

	private static Vec3 interp(final Vec3 v0, final Vec3 v1, final float t) {
		return Vec3.retain(
				v0.x + t * (v1.x - v0.x),
				v0.y + t * (v1.y - v0.y),
				v0.z + t * (v1.z - v0.z)
		);
	}

	private static AxisAlignedBB createAxisAlignedBBForVertex(
			final Vec3 vec3,
			final float boxRadius,
			final double maxY
	) {
		final double vy = vec3.y;
		final double vx = vec3.x;
		final double vz = vec3.z;

		final boolean isOverMax = vy + boxRadius > maxY;
		return new AxisAlignedBB(
				//min
				vx - boxRadius,
				isOverMax ? vy - boxRadius - boxRadius : vy - boxRadius,
				vz - boxRadius,
				//max
				vx + boxRadius,
				isOverMax ? vy : vy + boxRadius,
				vz + boxRadius
		);
	}

}
