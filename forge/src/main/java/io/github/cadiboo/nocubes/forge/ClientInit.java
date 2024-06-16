package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.client.KeyMappings;
import io.github.cadiboo.nocubes.client.render.OverlayRenderers;
import io.github.cadiboo.nocubes.network.NoCubesNetworkClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.ConnectionType;
import net.minecraftforge.network.NetworkContext;

/**
 * @author Cadiboo
 */
public final class ClientInit {
	public static void register(IEventBus modBus, IEventBus events) {
		modBus.addListener((RegisterKeyMappingsEvent registerEvent) -> {
			KeyMappings.register(registerEvent::register, onTick -> events.addListener((TickEvent.ClientTickEvent tickEvent) -> {
				if (tickEvent.phase != TickEvent.Phase.END)
					return;
				onTick.run();
			}));
		});
		modBus.addListener((FMLClientSetupEvent clientSetupEvent) -> {
			OverlayRenderers.register(onFrame -> events.addListener((RenderLevelStageEvent event) -> {
				if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)
					return;
				onFrame.accept(event.getPoseStack());
			}));
			events.addListener((RenderHighlightEvent event) -> {
				var world = Minecraft.getInstance().level;
				if (world == null)
					return;
				var targetHitResult = event.getTarget();
				if (!(targetHitResult instanceof BlockHitResult target))
					return;
				var lookingAtPos = target.getBlockPos();
				var camera = event.getCamera().getPosition();
				if (OverlayRenderers.renderNoCubesBlockHighlight(
					event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()),
					camera.x, camera.y, camera.z,
					world, lookingAtPos, world.getBlockState(lookingAtPos)
				))
					event.setCanceled(true);
			});
		});
		events.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
			// In this case Forge has already loaded the default server configs for us (see NetworkHooks#handleClientLoginSuccess(Connection))
			var forgeAlreadyLoadedDefaultConfig = NetworkContext.get(event.getConnection()).getType() == ConnectionType.VANILLA;
			NoCubesNetworkClient.onJoinedServer(forgeAlreadyLoadedDefaultConfig);
		});
	}
}
