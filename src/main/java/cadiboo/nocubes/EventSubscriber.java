package cadiboo.nocubes;

import com.cosmicdan.nocubes.renderer.SurfaceNets;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public final class EventSubscriber {

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {

		SurfaceNets.renderChunk(0, 1, 1, 1, Minecraft.getMinecraft().world, event.getContext());
	}

}
