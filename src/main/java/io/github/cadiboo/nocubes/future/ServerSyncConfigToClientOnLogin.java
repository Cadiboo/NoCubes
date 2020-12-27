package io.github.cadiboo.nocubes.future;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.apache.commons.lang3.tuple.Pair;

import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * @author Cadiboo
 */
@EventBusSubscriber(modid = NoCubes.MOD_ID)
public final class ServerSyncConfigToClientOnLogin {

	@SubscribeEvent
	public static void onPlayerLoggedInEvent(final PlayerEvent.PlayerLoggedInEvent event) {
		final EntityPlayer player = event.player;
		if (!(player instanceof EntityPlayerMP)) {
			getLogger().error("WTF, player is not EntityPlayerMP", new IllegalStateException());
			return;
		}
		final EntityPlayerMP playerMP = (EntityPlayerMP) player;
		final SimpleNetworkWrapper network = NoCubesNetwork.CHANNEL;
		for (final Pair<String, S2CConfigData> pair : ConfigTracker.INSTANCE.syncConfigs(false))
			network.sendTo(pair.getValue(), playerMP);
	}

}
