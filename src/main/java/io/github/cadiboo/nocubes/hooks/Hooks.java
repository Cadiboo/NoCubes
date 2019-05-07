package io.github.cadiboo.nocubes.hooks;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkCache;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapeInt;
import net.minecraft.util.math.shapes.VoxelShapePart;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IWorldReaderBase;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.HashSet;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Cadiboo
 */
public final class Hooks {

	public static void preIteration(final RenderChunk renderChunk, final float x, final float y, final float z, final ChunkRenderTask generator, final CompiledChunk compiledchunk, final BlockPos blockpos, final BlockPos blockpos1, final World world, final RenderChunkCache lvt_10_1_, final VisGraph lvt_11_1_, final HashSet lvt_12_1_, final boolean[] aboolean, final Random random, final BlockRendererDispatcher blockrendererdispatcher) {
		if (NoCubes.isEnabled()) {
			RenderDispatcher.renderChunk(renderChunk, blockpos, generator, compiledchunk, world, aboolean, random, blockrendererdispatcher);
		}
	}

	//return if normal rendering should happen
	public static boolean renderBlockDamage(final Tessellator tessellatorIn, final BufferBuilder bufferBuilderIn, final BlockPos blockpos, final IBlockState iblockstate, final WorldClient world, final TextureAtlasSprite textureatlassprite, final BlockRendererDispatcher blockrendererdispatcher) {
		if (!NoCubes.isEnabled() || !Config.renderSmoothTerrain || !iblockstate.nocubes_isTerrainSmoothable()) {
			return true;
		}
		RenderDispatcher.renderSmoothBlockDamage(tessellatorIn, bufferBuilderIn, blockpos, iblockstate, world, textureatlassprite);
		return false;
	}

	//return all the voxel shapes that entityShape intersects inside area
	public static Stream<VoxelShape> getCollisionBoxes(final IWorldReaderBase iWorldReaderBase, final VoxelShape p_212391_1_, final VoxelShape p_212391_2_, final boolean p_212391_3_, final int i, final int j, final int k, final int l, final int i1, final int j1, final WorldBorder worldborder, final boolean flag, final VoxelShapePart voxelshapepart, final Predicate<VoxelShape> predicate) {
		try {
			return CollisionHandler.getCollisionBoxes(iWorldReaderBase, p_212391_1_, p_212391_2_, p_212391_3_, i, j, k, l, i1, j1, worldborder, flag, voxelshapepart, predicate);
		} catch (Exception e) {
			Stream<VoxelShape> stream = StreamSupport.stream(BlockPos.MutableBlockPos.getAllInBoxMutable(i, k, i1, j - 1, l - 1, j1 - 1).spliterator(), false).map((p_212390_12_) -> {
				int k1 = p_212390_12_.getX();
				int l1 = p_212390_12_.getY();
				int i2 = p_212390_12_.getZ();
				boolean flag1 = k1 == i || k1 == j - 1;
				boolean flag2 = l1 == k || l1 == l - 1;
				boolean flag3 = i2 == i1 || i2 == j1 - 1;
				if ((!flag1 || !flag2) && (!flag2 || !flag3) && (!flag3 || !flag1) && iWorldReaderBase.isBlockLoaded(p_212390_12_)) {
					VoxelShape voxelshape;
					if (p_212391_3_ && !flag && !worldborder.contains(p_212390_12_)) {
						voxelshape = VoxelShapes.fullCube();
					} else {
						voxelshape = iWorldReaderBase.getBlockState(p_212390_12_).getCollisionShape(iWorldReaderBase, p_212390_12_);
					}

					VoxelShape voxelshape1 = p_212391_2_.withOffset((double) (-k1), (double) (-l1), (double) (-i2));
					if (VoxelShapes.compare(voxelshape1, voxelshape, IBooleanFunction.AND)) {
						return VoxelShapes.empty();
					} else if (voxelshape == VoxelShapes.fullCube()) {
						voxelshapepart.setFilled(k1 - i, l1 - k, i2 - i1, true, true);
						return VoxelShapes.empty();
					} else {
						return voxelshape.withOffset((double) k1, (double) l1, (double) i2);
					}
				} else {
					return VoxelShapes.empty();
				}
			}).filter(predicate);
			return Stream.concat(stream, Stream.generate(() -> new VoxelShapeInt(voxelshapepart, i, k, i1)).limit(1L).filter(predicate));
		}
	}

}
