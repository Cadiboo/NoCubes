package cadiboo.nocubes_uhh.client.renderer;

import javax.annotation.Nullable;

import cadiboo.nocubes_uhh.client.renderer.chunk.ModRenderChunk;
import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.IRenderChunkFactory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModViewFrustum extends ViewFrustum {
	public final ModRenderGlobal	renderGlobal;
	public final World				world;
	public int						countChunksY;
	public int						countChunksX;
	public int						countChunksZ;
	public ModRenderChunk[]			renderChunks;

	public ModViewFrustum(final World worldIn, final int renderDistanceChunks, final ModRenderGlobal renderGlobalIn, final IRenderChunkFactory renderChunkFactory) {
		super(worldIn, renderDistanceChunks, renderGlobalIn, renderChunkFactory);
		this.renderGlobal = renderGlobalIn;
		this.world = worldIn;
		this.setCountChunksXYZ(renderDistanceChunks);
		this.createRenderChunks(renderChunkFactory);
	}

	@Override
	public void createRenderChunks(final IRenderChunkFactory renderChunkFactory) {
		final int i = this.countChunksX * this.countChunksY * this.countChunksZ;
		this.renderChunks = new ModRenderChunk[i];
		int j = 0;

		for (int k = 0; k < this.countChunksX; ++k) {
			for (int l = 0; l < this.countChunksY; ++l) {
				for (int i1 = 0; i1 < this.countChunksZ; ++i1) {
					final int j1 = (((i1 * this.countChunksY) + l) * this.countChunksX) + k;
					this.renderChunks[j1] = new ModRenderChunk(renderChunkFactory.create(this.world, this.renderGlobal, j++));
					this.renderChunks[j1].setPosition(k * 16, l * 16, i1 * 16);
				}
			}
		}
	}

	@Override
	public void deleteGlResources() {
		for (final ModRenderChunk renderchunk : this.renderChunks) {
			renderchunk.deleteGlResources();
		}
	}

	@Override
	public void setCountChunksXYZ(final int renderDistanceChunks) {
		final int i = (renderDistanceChunks * 2) + 1;
		this.countChunksX = i;
		this.countChunksY = 16;
		this.countChunksZ = i;
	}

	@Override
	public void updateChunkPositions(final double viewEntityX, final double viewEntityZ) {
		final int i = MathHelper.floor(viewEntityX) - 8;
		final int j = MathHelper.floor(viewEntityZ) - 8;
		final int k = this.countChunksX * 16;

		for (int l = 0; l < this.countChunksX; ++l) {
			final int i1 = this.getBaseCoordinate(i, k, l);

			for (int j1 = 0; j1 < this.countChunksZ; ++j1) {
				final int k1 = this.getBaseCoordinate(j, k, j1);

				for (int l1 = 0; l1 < this.countChunksY; ++l1) {
					final int i2 = l1 * 16;
					final ModRenderChunk renderchunk = this.renderChunks[(((j1 * this.countChunksY) + l1) * this.countChunksX) + l];
					renderchunk.setPosition(i1, i2, k1);
				}
			}
		}
	}

	public int getBaseCoordinate(final int p_178157_1_, final int p_178157_2_, final int p_178157_3_) {
		final int i = p_178157_3_ * 16;
		int j = (i - p_178157_1_) + (p_178157_2_ / 2);

		if (j < 0) {
			j -= p_178157_2_ - 1;
		}

		return i - ((j / p_178157_2_) * p_178157_2_);
	}

	@Override
	public void markBlocksForUpdate(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final boolean updateImmediately) {
		final int i = MathHelper.intFloorDiv(minX, 16);
		final int j = MathHelper.intFloorDiv(minY, 16);
		final int k = MathHelper.intFloorDiv(minZ, 16);
		final int l = MathHelper.intFloorDiv(maxX, 16);
		final int i1 = MathHelper.intFloorDiv(maxY, 16);
		final int j1 = MathHelper.intFloorDiv(maxZ, 16);

		for (int k1 = i; k1 <= l; ++k1) {
			int l1 = k1 % this.countChunksX;

			if (l1 < 0) {
				l1 += this.countChunksX;
			}

			for (int i2 = j; i2 <= i1; ++i2) {
				int j2 = i2 % this.countChunksY;

				if (j2 < 0) {
					j2 += this.countChunksY;
				}

				for (int k2 = k; k2 <= j1; ++k2) {
					int l2 = k2 % this.countChunksZ;

					if (l2 < 0) {
						l2 += this.countChunksZ;
					}

					final int i3 = (((l2 * this.countChunksY) + j2) * this.countChunksX) + l1;
					final ModRenderChunk renderchunk = this.renderChunks[i3];
					renderchunk.setNeedsUpdate(updateImmediately);
				}
			}
		}
	}

	@Override
	@Nullable
	public ModRenderChunk getRenderChunk(final BlockPos pos) {
		int i = MathHelper.intFloorDiv(pos.getX(), 16);
		final int j = MathHelper.intFloorDiv(pos.getY(), 16);
		int k = MathHelper.intFloorDiv(pos.getZ(), 16);

		if ((j >= 0) && (j < this.countChunksY)) {
			i = i % this.countChunksX;

			if (i < 0) {
				i += this.countChunksX;
			}

			k = k % this.countChunksZ;

			if (k < 0) {
				k += this.countChunksZ;
			}

			final int l = (((k * this.countChunksY) + j) * this.countChunksX) + i;
			return this.renderChunks[l];
		} else {
			return null;
		}
	}
}