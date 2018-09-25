package cadiboo.nocubes.client.event;

import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class RenderBlockLayerEvent extends Event {
	private final RenderGlobal		context;
	private final BlockRenderLayer	blockRenderLayer;
	private final int				pass;
	private final double			partialTicks;
	private final Entity			entity;

	private int chunksRendered;

	public RenderBlockLayerEvent(final RenderGlobal renderGlobal, final BlockRenderLayer blockRenderLayer, final double partialTicks, final int pass, final Entity entity, final int chunksRendered) {
		this.context = renderGlobal;
		this.blockRenderLayer = blockRenderLayer;
		this.partialTicks = partialTicks;
		this.pass = pass;
		this.entity = entity;
		this.chunksRendered = chunksRendered;
	}

	public RenderGlobal getContext() {
		return this.context;
	}

	public BlockRenderLayer getBlockRenderLayer() {
		return this.blockRenderLayer;
	}

	public double getPartialTicks() {
		return this.partialTicks;
	}

	public int getPass() {
		return this.pass;
	}

	public Entity getEntity() {
		return this.entity;
	}

	public void incrementChunksRendered() {
		this.chunksRendered++;
	}

	public int getChunksRendered() {
		return this.chunksRendered;
	}

}