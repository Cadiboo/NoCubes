package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;

import java.util.function.Predicate;

public class OOCollisionHandler {
	private final MeshGenerator meshGenerator;

	public OOCollisionHandler(MeshGenerator meshGenerator) {
		this.meshGenerator = meshGenerator;
	}

	public void generate(Area area, IShapeConsumer consumer) {
		final Face normal = new Face();
		final Vec averageOfNormal = new Vec();
		final Vec centre = new Vec();
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable; // + || isLeavesSmoothable
		meshGenerator.generate(area, isSmoothable, (pos, face) -> {
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
		float width = centre.x + averageOfNormal.x - v.x;
		float height = centre.y + averageOfNormal.y - v.y;
		float length = centre.z + averageOfNormal.z - v.z;
//		if (-0.1 < width && width < 0.1)
//			width = averageOfNormal.x;
//		if (-0.1 < height && height < 0.1)
//			height = averageOfNormal.y;
//		if (-0.1 < length && length < 0.1)
//			length = averageOfNormal.z;
		consumer.accept(
			v.x, v.y, v.z,
			v.x + width, v.y + height, v.z + length
		);
	}

	public interface IShapeConsumer {

		void accept(
			float x0, float y0, float z0,
			float x1, float y1, float z1
		);

	}

}
