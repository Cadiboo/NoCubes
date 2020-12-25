package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Cadiboo
 */
public interface OptiFineProxy {

	default ReportedException createInitialisationCrashReport(Exception e) {
		String version = getClass().getSimpleName();
		CrashReport crashReport = CrashReport.makeCrashReport(e, "Problem initialising OptiFine " + version + " compatibility");
		crashReport.makeCategory("NoCubes OptiFine compatibility");
		return new ReportedException(crashReport);
	}

	boolean isChunkCacheOF(@Nullable Object obj);

	ChunkCache getChunkRenderCache(IBlockAccess reader);

	void pushShaderThing(
			IBlockState blockState,
			BlockPos pos,
			IBlockAccess reader,
			BufferBuilder bufferBuilder
	);

	void popShaderThing(BufferBuilder bufferBuilder);

	Object getRenderEnv(BufferBuilder bufferBuilder, IBlockState blockState, BlockPos pos);

	IBakedModel getRenderModel(
			IBakedModel modelIn,
			IBlockState stateIn,
			Object renderEnv
	);

	List<BakedQuad> getRenderQuads(
			List<BakedQuad> quads,
			IBlockAccess worldIn,
			IBlockState stateIn,
			BlockPos posIn,
			EnumFacing enumfacing,
			BlockRenderLayer layer,
			long rand,
			Object renderEnv
	);

}
