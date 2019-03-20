package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.world.ModWorldEventListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.HashSet;

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

		try (final ModProfiler ignored = NoCubes.getProfiler().start("ServerTickEvent")) {
			synchronized (CollisionHandler.CACHE) {
				final HashSet<BlockPos> toRemove = new HashSet<>();
				CollisionHandler.CACHE.forEach((blockPos, collisionsCache) -> {
					if (collisionsCache.timeSinceLastUsed > 100) {
						toRemove.add(blockPos);
						return;
					}
					++collisionsCache.timeSinceLastUsed;
				});
				for (final BlockPos blockPos : toRemove) {
					CollisionHandler.CACHE.remove(blockPos);
				}
			}
		}
	}

}
