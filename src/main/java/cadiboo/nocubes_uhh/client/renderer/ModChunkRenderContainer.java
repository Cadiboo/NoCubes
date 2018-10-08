package cadiboo.nocubes_uhh.client.renderer;

import java.util.List;

import com.google.common.collect.Lists;

import cadiboo.nocubes_uhh.client.renderer.chunk.ModRenderChunk;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class ModChunkRenderContainer extends ChunkRenderContainer {
	public double				viewEntityX;
	public double				viewEntityY;
	public double				viewEntityZ;
	public List<ModRenderChunk>	renderChunks	= Lists.<ModRenderChunk>newArrayListWithCapacity(17424);
	public boolean				initialized;

	@Override
	public void initialize(final double viewEntityXIn, final double viewEntityYIn, final double viewEntityZIn) {
		this.initialized = true;
		this.renderChunks.clear();
		this.viewEntityX = viewEntityXIn;
		this.viewEntityY = viewEntityYIn;
		this.viewEntityZ = viewEntityZIn;
	}

	@Override
	public void preRenderChunk(final RenderChunk renderChunkIn) {
		this.preRenderChunk(new ModRenderChunk(renderChunkIn));
	}

	public void preRenderChunk(final ModRenderChunk renderChunkIn) {
		final BlockPos blockpos = renderChunkIn.getPosition();
		GlStateManager.translate((float) (blockpos.getX() - this.viewEntityX), (float) (blockpos.getY() - this.viewEntityY), (float) (blockpos.getZ() - this.viewEntityZ));
	}

	public void addRenderChunk(final ModRenderChunk renderChunkIn, final BlockRenderLayer layer) {
		this.renderChunks.add(renderChunkIn);
	}

	@Override
	public abstract void renderChunkLayer(BlockRenderLayer layer);
}