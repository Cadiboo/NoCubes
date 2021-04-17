package io.github.cadiboo.nocubes.collision;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
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
		meshGenerator.generate(area, isSmoothable, (pos, amount) -> {
			// Generate collisions for blocks that are fully inside the isosurface
			// The face handler will generate collisions for the surface
			if (amount == 1) {
				float x0 = pos.getX();
				float y0 = pos.getY();
				float z0 = pos.getZ();
				if (meshGenerator instanceof SurfaceNets) {
					// Pretty disgusting, see the comments in SurfaceNets about densities and corners for why this offset exists
					x0 += 0.5F;
					y0 += 0.5F;
					z0 += 0.5F;
				}
				consumer.accept(
					x0, y0, z0,
					x0 + 1, y0 + 1, z0 + 1
				);
			}
			return true;
		}, (pos, face) -> {
			face.assignNormalTo(normal);
			face.assignAverageTo(centre);

			normal.assignAverageTo(averageOfNormal);

			generateShape(centre, averageOfNormal, consumer, face.v0);
			generateShape(centre, averageOfNormal, consumer, face.v1);
			generateShape(centre, averageOfNormal, consumer, face.v2);
			generateShape(centre, averageOfNormal, consumer, face.v3);
			return true;
		});
	}

	private static void generateShape(Vec centre, Vec averageOfNormal, IShapeConsumer consumer, Vec v) {
		consumer.accept(
			v.x, v.y, v.z,
			centre.x + averageOfNormal.x, centre.y + averageOfNormal.y, centre.z + averageOfNormal.z
		);
	}

	public interface IShapeConsumer {

		void accept(
			float x0, float y0, float z0,
			float x1, float y1, float z1
		);

	}

}
