package cadiboo.nocubes.client;

import cadiboo.nocubes.client.event.RenderBlockLayerEvent;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.MinecraftForge;

public class NoCubesHooksClient {

	public static RenderBlockLayerEvent onRenderBlockLayerEvent(final RenderGlobal renderGlobal, final BlockRenderLayer blockRenderLayer, final double partialTicks, final int pass, final Entity entity, final int chunksRendered) {
		final RenderBlockLayerEvent event = new RenderBlockLayerEvent(renderGlobal, blockRenderLayer, partialTicks, pass, entity, chunksRendered);
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

}
