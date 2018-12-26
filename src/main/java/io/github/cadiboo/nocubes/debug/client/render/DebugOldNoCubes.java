package io.github.cadiboo.nocubes.debug.client.render;

import io.github.cadiboo.nocubes.debug.client.render.IDebugRenderAlgorithm;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class DebugOldNoCubes implements IDebugRenderAlgorithm {

	@Nonnull
	@Override
	public List<Vec3> getVertices(final BlockPos pos, final IBlockAccess world) {
		return Collections.emptyList();
	}

}
