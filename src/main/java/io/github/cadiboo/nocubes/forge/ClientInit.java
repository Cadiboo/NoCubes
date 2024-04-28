package io.github.cadiboo.nocubes.forge;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.KeyMappings;
import io.github.cadiboo.nocubes.client.render.OverlayRenderers;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
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
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Cadiboo
 */
public final class ClientInit {

	private static final Logger LOG = LogManager.getLogger();

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
			LOG.debug("Client joined server");
			loadDefaultServerConfigIfWeAreOnAModdedServerWithoutNoCubes(event);
			ClientUtil.sendPlayerInfoMessage();
			ClientUtil.warnPlayerIfVisualsDisabled();
			if (!NoCubesNetwork.currentServerHasNoCubes) {
				// This lets players not phase through the ground on servers that don't have NoCubes installed
				NoCubesConfig.Server.collisionsEnabled = false;
				ClientUtil.warnPlayer(NoCubes.MOD_ID + ".notification.notInstalledOnServer", KeyMappings.translate(KeyMappings.TOGGLE_SMOOTHABLE_BLOCK_TYPE));
			}
		});
	}

	/**
	 * This lets NoCubes load properly on modded servers that don't have it installed
	 */
	private static void loadDefaultServerConfigIfWeAreOnAModdedServerWithoutNoCubes(ClientPlayerNetworkEvent.LoggingIn event) {
		if (NoCubesNetwork.currentServerHasNoCubes) {
			// Forge has synced the server config to us, no need to load the default (see ConfigSync.syncConfigs)
			LOG.debug("Not loading default server config - current server has NoCubes installed");
			return;
		}

		var connection = event.getConnection();
		if (connection != null && NetworkHooks.isVanillaConnection(connection)) {
			// Forge has already loaded the default server configs for us (see NetworkHooks#handleClientLoginSuccess(Connection))
			LOG.debug("Not loading default server config - Forge has already loaded it for us");
			return;
		}

		if (connection == null)
			LOG.debug("Connection was null, assuming we're connected to a modded server without NoCubes!");
		LOG.debug("Connected to a modded server that doesn't have NoCubes installed, loading default server config");
		NoCubesConfig.Hacks.loadDefaultServerConfig();
	}

}
