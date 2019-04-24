package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.collision.CollisionHandler.CollisionsCache;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.world.ModWorldEventListener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
		final IWorld world = event.getWorld();
		if (world instanceof World) {
			((World) world).addEventListener(new ModWorldEventListener());
		} else {
			NoCubes.NO_CUBES_LOG.warn("Could not add event listener! The world being loaded is not a World.");
		}
	}

	@SubscribeEvent
	public static void onProjectileImpactEvent(final ProjectileImpactEvent event) {
		if (ModConfig.enableCollisions) {
			final BlockPos pos = event.getRayTraceResult().getBlockPos();
			final Entity entity = event.getEntity();
			final World world = entity.world;
			final IBlockState state = world.getBlockState(pos);
			final List<AxisAlignedBB> collidingBoxes = new ArrayList<>();
			CollisionHandler.addCollisionBoxToList(state.getBlock(), state, world, pos, entity.getBoundingBox(), collidingBoxes, entity, false);
			if (!collidingBoxes.isEmpty()) {
				event.setCanceled(true);
			}
		}
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
