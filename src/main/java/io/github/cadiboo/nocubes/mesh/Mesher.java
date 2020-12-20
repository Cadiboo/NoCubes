package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.client.render.util.Face;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public interface Mesher {
	Extension requiredExtension();

	class Extension {

		final int negativeX;
		final int negativeY;
		final int negativeZ;
		final int positiveX;
		final int positiveY;
		final int positiveZ;

		public Extension(int negativeX, int negativeY, int negativeZ, int positiveX, int positiveY, int positiveZ) {
			this.negativeX = negativeX;
			this.negativeY = negativeY;
			this.negativeZ = negativeZ;
			this.positiveX = positiveX;
			this.positiveY = positiveY;
			this.positiveZ = positiveZ;
		}
	}

	interface Action {

		boolean apply(BlockPos.Mutable pos, Face face);

	}
}
