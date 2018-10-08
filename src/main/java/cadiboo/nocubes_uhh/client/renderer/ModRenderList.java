package cadiboo.nocubes_uhh.client.renderer;

import cadiboo.nocubes_uhh.client.renderer.chunk.ModListedRenderChunk;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModRenderList extends ModChunkRenderContainer {
	@Override
	public void renderChunkLayer(final BlockRenderLayer layer) {
		if (this.initialized) {
			for (final RenderChunk renderchunk : this.renderChunks) {
				final ModListedRenderChunk listedrenderchunk = (ModListedRenderChunk) renderchunk;
				GlStateManager.pushMatrix();
				this.preRenderChunk(renderchunk);
				GlStateManager.callList(listedrenderchunk.getDisplayList(layer, listedrenderchunk.getCompiledChunk()));
				GlStateManager.popMatrix();
			}

			GlStateManager.resetColor();
			this.renderChunks.clear();
		}
	}
}