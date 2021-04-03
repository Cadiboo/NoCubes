package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;

public class OOCollisionHandler {
	private final MeshGenerator meshGenerator;

	public OOCollisionHandler(MeshGenerator meshGenerator) {
		this.meshGenerator = meshGenerator;
	}

	public void generate(Area area, IShapeConsumer consumer) {
		final Face normal = new Face();
		final Vec averageOfNormal = new Vec();
		final Vec centre = new Vec();
		meshGenerator.generate(area, (pos, face) -> {
			face.assignNormalTo(normal);
			face.assignAverageTo(centre);

			normal.assignAverageTo(averageOfNormal);
			averageOfNormal.normalise().multiply(0.125F);

			generateShape(centre, averageOfNormal, consumer, face.v0);
			generateShape(centre, averageOfNormal, consumer, face.v1);
			generateShape(centre, averageOfNormal, consumer, face.v2);
			generateShape(centre, averageOfNormal, consumer, face.v3);
			return true;
		});
	}

	private static void generateShape(Vec centre, Vec averageOfNormal, IShapeConsumer consumer, Vec v) {
		float w = centre.x - v.x;
		if (-0.01 < w && w < 0.01)
			w = 0.0625F * averageOfNormal.x;
		float h = centre.y - v.y;
		if (-0.01 < h && h < 0.01)
			h = 0.0625F * averageOfNormal.y;
		float l = centre.z - v.z;
		if (-0.01 < l && l < 0.01)
			l = 0.0625F * averageOfNormal.z;
		consumer.accept(
			v.x, v.y, v.z,
			v.x + w, v.y + h, v.z + l
		);
	}

	interface IShapeConsumer {

		void accept(
			float x0, float y0, float z0,
			float x1, float y1, float z1
		);

	}

}
