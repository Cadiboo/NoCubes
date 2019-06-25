package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.ConfigTracker;
import io.github.cadiboo.nocubes.tempnetwork.S2CSyncConfig;
import io.github.cadiboo.nocubes.world.ModWorldEventListener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * @author Cadiboo
 */
@EventBusSubscriber(modid = NoCubes.MOD_ID)
public final class ForgeEventSubscriber {

	private static final Logger LOGGER = LogManager.getLogger();

	@SubscribeEvent
	public static void onWorldLoadEvent(final WorldEvent.Load event) {
		final World world = event.getWorld();
		if (world instanceof World) {
			((World) world).addEventListener(new ModWorldEventListener());
		} else {
			LOGGER.error("Failed to attach event listener to world. world is not a World!");
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedInEvent(final PlayerEvent.PlayerLoggedInEvent event) {
		final EntityPlayer player = event.player;
		if (!(player instanceof EntityPlayerMP)) {
			NoCubes.LOGGER.error("WTF, player is not EntityPlayerMP", new IllegalStateException());
			return;
		}
		final EntityPlayerMP playerMP = (EntityPlayerMP) player;
		final SimpleNetworkWrapper network = NoCubes.NETWORK_MANAGER.NETWORK;
		for (final Pair<String, S2CSyncConfig> pair : ConfigTracker.INSTANCE.syncConfigs(false)) {
			network.sendTo(pair.getValue(), playerMP);
		}
	}

}
