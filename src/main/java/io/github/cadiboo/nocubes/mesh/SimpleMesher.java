package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.Area;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.Vec3i;

import java.util.function.Predicate;

abstract class SimpleMesher implements Mesher {

	void generate(Area area, Predicate<IBlockState> isSmoothable, Area.AreaTraverser action) {
		area.traverse(getNegativeAreaExtension(), getPositiveAreaExtension(), (x, y, z, index, state) -> {
			if (!isSmoothable.test(state))
				return true;
			return action.accept(x, y, z, index, state);
		});
	}

}
