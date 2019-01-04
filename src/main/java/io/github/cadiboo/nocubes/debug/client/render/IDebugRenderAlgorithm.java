package io.github.cadiboo.nocubes.debug.client.render;

import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.List;

public interface IDebugRenderAlgorithm {

	@Nonnull
	public List<Vec3> getVertices(PooledMutableBlockPos pos, IBlockAccess world);

	@Nonnull
	public List<Face<Vec3>> getFaces(PooledMutableBlockPos pos, IBlockAccess world);

	class Face<T> {

		private final T vertex0;

		private final T vertex1;

		private final T vertex2;

		private final T vertex3;

		public Face(final T vertex0, final T vertex1, final T vertex2, final T vertex3) {
			this.vertex0 = vertex0;
			this.vertex1 = vertex1;
			this.vertex2 = vertex2;
			this.vertex3 = vertex3;
		}

		public Face(final T vertex1, final T vertex2, final T vertex3) {
			this(vertex1, vertex1, vertex2, vertex3);
		}

		public T getVertex0() {
			return vertex0;
		}

		public T getVertex1() {
			return vertex1;
		}

		public T getVertex2() {
			return vertex2;
		}

		public T getVertex3() {
			return vertex3;
		}

	}

}
