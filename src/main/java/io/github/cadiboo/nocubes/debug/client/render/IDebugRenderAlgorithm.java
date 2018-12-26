package io.github.cadiboo.nocubes.debug.client.render;

import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nonnull;
import java.util.List;

public interface IDebugRenderAlgorithm {

	@Nonnull
	public List<Vec3> getVertices(BlockPos pos, IBlockAccess world);

}
