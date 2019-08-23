package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.List;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public final class MeshCollisionUtil {

	/**
	 * Closes the vertices of "face"
	 */
	static void addIntersectingFaceShapesToList(
			final List<VoxelShape> outShapes,
			final Face face,
			final ModProfiler profiler,
			final double maxYLevel,
			final Predicate<VoxelShape> doesShapeIntersect,
			final boolean offsetShapes
	) {
		profiler.start("interpolate");

		//0___3
		//_____
		//_____
		//_____
		//1___2
		final Vec3 v0 = face.getVertex0();
		final Vec3 v1 = face.getVertex1();
		final Vec3 v2 = face.getVertex2();
		final Vec3 v3 = face.getVertex3();

		//0_*_3
		//_____
		//*___*
		//_____
		//1_*_2
		final Vec3 v0v1 = interp(v0, v1, 0.5F);
		final Vec3 v1v2 = interp(v1, v2, 0.5F);
		final Vec3 v2v3 = interp(v2, v3, 0.5F);
		final Vec3 v3v0 = interp(v3, v0, 0.5F);

//		//0x*x3
//		//x___x
//		//*___*
//		//x___x
//		//1x*x2
//		final Vec3 v0v1v0 = interp(v0v1, v0, 0.5F);
//		final Vec3 v0v1v1 = interp(v0v1, v1, 0.5F);
//		final Vec3 v1v2v1 = interp(v1v2, v1, 0.5F);
//		final Vec3 v1v2v2 = interp(v1v2, v2, 0.5F);
//		final Vec3 v2v3v2 = interp(v2v3, v2, 0.5F);
//		final Vec3 v2v3v3 = interp(v2v3, v3, 0.5F);
//		final Vec3 v3v0v3 = interp(v3v0, v3, 0.5F);
//		final Vec3 v3v0v0 = interp(v3v0, v0, 0.5F);

		//0x*x3
		//xa_ax
		//*___*
		//xa_ax
		//1x*x2
		final Vec3 v0v1v1v2 = interp(v0v1, v1v2, 0.5F);
		final Vec3 v1v2v2v3 = interp(v1v2, v2v3, 0.5F);
		final Vec3 v2v3v3v0 = interp(v2v3, v3v0, 0.5F);
		final Vec3 v3v0v0v1 = interp(v3v0, v0v1, 0.5F);

//		//0x*x3
//		//xabax
//		//*b_b*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3 = interp(v0v1v1v2, v1v2v2v3, 0.5F);
//		final Vec3 v1v2v2v3v2v3v3v0 = interp(v1v2v2v3, v2v3v3v0, 0.5F);
//		final Vec3 v2v3v3v0v3v0v0v1 = interp(v2v3v3v0, v3v0v0v1, 0.5F);
//		final Vec3 v3v0v0v1v0v1v1v2 = interp(v3v0v0v1, v0v1v1v2, 0.5F);

//		//0x*x3
//		//xabax
//		//*bcb*
//		//xabax
//		//1x*x2
//		final Vec3 v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1 = interp(v0v1v1v2v1v2v2v3, v2v3v3v0v3v0v0v1, 0.5F);
//		final Vec3 v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2 = interp(v1v2v2v3v2v3v3v0, v3v0v0v1v0v1v1v2, 0.5F);

		profiler.endStartSection("createShapes");

//		final VoxelShape v0shape = createShape(v0, maxYLevel, offsetShapes);
//		final VoxelShape v1shape = createShape(v1, maxYLevel, offsetShapes);
//		final VoxelShape v2shape = createShape(v2, maxYLevel, offsetShapes);
//		final VoxelShape v3shape = createShape(v3, maxYLevel, offsetShapes);
		final VoxelShape v0v1shape = createShape(v0v1, maxYLevel, offsetShapes);
		final VoxelShape v1v2shape = createShape(v1v2, maxYLevel, offsetShapes);
		final VoxelShape v2v3shape = createShape(v2v3, maxYLevel, offsetShapes);
		final VoxelShape v3v0shape = createShape(v3v0, maxYLevel, offsetShapes);
//		final VoxelShape v0v1v0shape = createShape(v0v1v0,maxYLevel, offsetShapes);
//		final VoxelShape v0v1v1shape = createShape(v0v1v1,maxYLevel, offsetShapes);
//		final VoxelShape v1v2v1shape = createShape(v1v2v1,maxYLevel, offsetShapes);
//		final VoxelShape v1v2v2shape = createShape(v1v2v2,maxYLevel, offsetShapes);
//		final VoxelShape v2v3v2shape = createShape(v2v3v2,maxYLevel, offsetShapes);
//		final VoxelShape v2v3v3shape = createShape(v2v3v3,maxYLevel, offsetShapes);
//		final VoxelShape v3v0v3shape = createShape(v3v0v3,maxYLevel, offsetShapes);
//		final VoxelShape v3v0v0shape = createShape(v3v0v0,maxYLevel, offsetShapes);
		final VoxelShape v0v1v1v2shape = createShape(v0v1v1v2, maxYLevel, offsetShapes);
		final VoxelShape v1v2v2v3shape = createShape(v1v2v2v3, maxYLevel, offsetShapes);
		final VoxelShape v2v3v3v0shape = createShape(v2v3v3v0, maxYLevel, offsetShapes);
		final VoxelShape v3v0v0v1shape = createShape(v3v0v0v1, maxYLevel, offsetShapes);
//		final VoxelShape v0v1v1v2v1v2v2v3shape = createShape(v0v1v1v2v1v2v2v3,maxYLevel, offsetShapes);
//		final VoxelShape v1v2v2v3v2v3v3v0shape = createShape(v1v2v2v3v2v3v3v0,maxYLevel, offsetShapes);
//		final VoxelShape v2v3v3v0v3v0v0v1shape = createShape(v2v3v3v0v3v0v0v1,maxYLevel, offsetShapes);
//		final VoxelShape v3v0v0v1v0v1v1v2shape = createShape(v3v0v0v1v0v1v1v2,maxYLevel, offsetShapes);
//		final VoxelShape v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1shape = createShape(v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1,maxYLevel, offsetShapes);
//		final VoxelShape v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2shape = createShape(v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2,maxYLevel, offsetShapes);

		profiler.endStartSection("addShapes");

//		addShape(outShapes, v0shape, doesShapeIntersect);
//		addShape(outShapes, v1shape, doesShapeIntersect);
//		addShape(outShapes, v2shape, doesShapeIntersect);
//		addShape(outShapes, v3shape, doesShapeIntersect);
		addShape(outShapes, v0v1shape, doesShapeIntersect);
		addShape(outShapes, v1v2shape, doesShapeIntersect);
		addShape(outShapes, v2v3shape, doesShapeIntersect);
		addShape(outShapes, v3v0shape, doesShapeIntersect);
//		addShape(outShapes, v0v1v0shape, doesShapeIntersect);
//		addShape(outShapes, v0v1v1shape, doesShapeIntersect);
//		addShape(outShapes, v1v2v1shape, doesShapeIntersect);
//		addShape(outShapes, v1v2v2shape, doesShapeIntersect);
//		addShape(outShapes, v2v3v2shape, doesShapeIntersect);
//		addShape(outShapes, v2v3v3shape, doesShapeIntersect);
//		addShape(outShapes, v3v0v3shape, doesShapeIntersect);
//		addShape(outShapes, v3v0v0shape, doesShapeIntersect);
		addShape(outShapes, v0v1v1v2shape, doesShapeIntersect);
		addShape(outShapes, v1v2v2v3shape, doesShapeIntersect);
		addShape(outShapes, v2v3v3v0shape, doesShapeIntersect);
		addShape(outShapes, v3v0v0v1shape, doesShapeIntersect);
//		addShape(outShapes, v0v1v1v2v1v2v2v3shape, doesShapeIntersect);
//		addShape(outShapes, v1v2v2v3v2v3v3v0shape, doesShapeIntersect);
//		addShape(outShapes, v2v3v3v0v3v0v0v1shape, doesShapeIntersect);
//		addShape(outShapes, v3v0v0v1v0v1v1v2shape, doesShapeIntersect);
//		addShape(outShapes, v0v1v1v2v1v2v2v3v2v3v3v0v3v0v0v1shape, doesShapeIntersect);
//		addShape(outShapes, v1v2v2v3v2v3v3v0v3v0v0v1v0v1v1v2shape, doesShapeIntersect);

		profiler.endSection();

		// Close the original face vectors even if we didn't generate shapes for them
		v0.close();
		v1.close();
		v2.close();
		v3.close();
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

	private static void addShape(
			final List<VoxelShape> collidingShapes,
			final VoxelShape shape,
			final Predicate<VoxelShape> doesShapeIntersect
	) {
		if (doesShapeIntersect.test(shape)) {
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

	private static VoxelShape createShape(final Vec3 vec3, final double maxY, final boolean offsetShape) {
		final float boxRadius = 0.15F;
		final double vy = vec3.y;
		final double vx = vec3.x;
		final double vz = vec3.z;

		final boolean isYOverMax = vy + boxRadius > maxY;
		if (offsetShape) {
			return VoxelShapes.create(
					//min
					vx - boxRadius,
					isYOverMax ? vy - boxRadius - boxRadius : vy - boxRadius,
					vz - boxRadius,
					//max
					vx + boxRadius,
					isYOverMax ? vy : vy + boxRadius,
					vz + boxRadius
			);
		} else {
			return VoxelShapes.create(
					//min
					-boxRadius,
					isYOverMax ? -boxRadius - boxRadius : -boxRadius,
					-boxRadius,
					//max
					+boxRadius,
					isYOverMax ? 0 : +boxRadius,
					+boxRadius
			);
		}
	}

}
