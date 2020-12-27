package io.github.cadiboo.nocubes.client.optifine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;

import javax.annotation.Nullable;
import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * @author Cadiboo
 */
public final class OptiFineCompatibility {

	public static final boolean ENABLED;
	public static final OptiFineProxy PROXY;
	static {
		OptiFineProxy proxy = makeProxy();
		if (proxy == null) {
			ENABLED = false;
			PROXY = dummyProxy();
		} else {
			ENABLED = true;
			PROXY = proxy;
		}
		getLogger().info("OptiFineCompatibility: Compatibility enabled = " + ENABLED);
	}

	private static OptiFineProxy makeProxy() {
		try {
			return new HD_U_F5();
		} catch (OutOfMemoryError oom) {
			throw oom;
		} catch (Throwable t) {
			return null;
		}
	}

	private static OptiFineProxy dummyProxy() {
		return new OptiFineProxy() {
			@Override
			public boolean isChunkCacheOF(@Nullable Object obj) {
				return false;
			}

			@Override
			public ChunkCache getChunkRenderCache(IBlockAccess reader) {
				throw new RuntimeException();
			}

			@Override
			public void pushShaderThing(final IBlockState blockState, final BlockPos pos, final IBlockAccess reader, final BufferBuilder bufferBuilder) {
			}

			@Override
			public void popShaderThing(final BufferBuilder bufferBuilder) {
			}

			@Override
			public Object getRenderEnv(final BufferBuilder bufferBuilder, final IBlockState blockState, final BlockPos pos) {
				return null;
			}

			@Override
			public IBakedModel getRenderModel(final IBakedModel modelIn, final IBlockState stateIn, final Object renderEnv) {
				return modelIn;
			}

			@Override
			public List<BakedQuad> getRenderQuads(final List<BakedQuad> quads, final IBlockAccess worldIn, final IBlockState stateIn, final BlockPos posIn, final EnumFacing enumfacing, final BlockRenderLayer layer, final long rand, final Object renderEnv) {
				return quads;
			}
		};
	}

}
