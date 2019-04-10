package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.collision.CollisionHandler.CollisionsCache;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.world.ModWorldEventListener;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.Iterator;

import static io.github.cadiboo.nocubes.collision.CollisionHandler.CACHE;

/**
 * Subscribe to events that should be handled on both PHYSICAL sides in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
public final class EventSubscriber {

	@SubscribeEvent
	public static void onWorldLoadEvent(final WorldEvent.Load event) {
		final World world = event.getWorld();
		world.addEventListener(new ModWorldEventListener());
	}

	//TODO: projectile impact event

	@SubscribeEvent
	public static void onServerTickEvent(final ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}

		final long currentTimeMillis = System.currentTimeMillis();

		try (final ModProfiler ignored = NoCubes.getProfiler().start("ServerTickEvent")) {
			synchronized (CACHE) {
				for (Iterator<CollisionsCache> iterator = CACHE.values().iterator(); iterator.hasNext(); ) {
					final CollisionsCache collisionsCache = iterator.next();
					if (currentTimeMillis - collisionsCache.timeLastUsed > 5000) {
						synchronized (collisionsCache.faces) {
							final FaceList faces = collisionsCache.faces;
							for (final Face face : faces) {
								{
									face.getVertex0().close();
									face.getVertex1().close();
									face.getVertex2().close();
									face.getVertex3().close();
								}
								face.close();
							}
							faces.close();
						}
						iterator.remove();
					}
				}
			}
		}
	}

}
