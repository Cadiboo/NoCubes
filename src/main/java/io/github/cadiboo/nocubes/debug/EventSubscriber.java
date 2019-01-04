package io.github.cadiboo.nocubes.debug;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Subscribe to events that should be handled on both PHYSICAL sides in this class
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
public final class EventSubscriber {

	/**
	 * copied from {@link World#checkBlockCollision(AxisAlignedBB)}
	 */
	@SubscribeEvent
	public static void onPlayerSPPushOutOfBlocksEvent(@Nonnull final PlayerSPPushOutOfBlocksEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.debug.debugEnabled) {
			return;
		}

		if (!ModConfig.debug.realisticCollisions) {
			return;
		}

		final AxisAlignedBB entityBox = event.getEntityBoundingBox();
		final World world = event.getEntity().world;

		int minX = MathHelper.floor(entityBox.minX);
		int maxX = MathHelper.ceil(entityBox.maxX);
		int minY = MathHelper.floor(entityBox.minY);
		int maxY = MathHelper.ceil(entityBox.maxY);
		int minZ = MathHelper.floor(entityBox.minZ);
		int maxZ = MathHelper.ceil(entityBox.maxZ);
		BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain();

		try {

			event.setCanceled(true);
//		for (int x = minX; x < maxX; ++x) {
//			for (int y = minY; y < maxY; ++y) {
//				for (int z = minZ; z < maxZ; ++z) {
//					IBlockState state = world.getBlockState(pooledMutableBlockPos.setPos(x, y, z));
//
//					if (ModUtil.shouldSmooth(state)) {
//						pooledMutableBlockPos.release();
//						event.setCanceled(true);
//						return;
//					}
//				}
//			}
//		}
		} finally {
			// This gets called right before return, don't worry
			// (unless theres a BIG error in the try, in which case
			// releasing the pooled pos is the least of our worries)
			pooledMutableBlockPos.release();
		}
	}

	@SubscribeEvent
	public static void onGetCollisionBoxesEvent(@Nonnull final GetCollisionBoxesEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.debug.debugEnabled) {
			return;
		}

		if (!ModConfig.debug.realisticCollisions) {
			return;
		}

		BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain();

		try {
			final AxisAlignedBB aabb = event.getAabb();

			final BlockPos pos = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
			final int x = pos.getX();
			final int y = pos.getY();
			final int z = pos.getZ();

			final List<Vec3> vertices = ModConfig.debug.activeRenderingAlgorithm.getVertices(pooledMutableBlockPos, event.getWorld());

			if (vertices.isEmpty() || vertices.size() < 8) {
				return;
			}

			final List<AxisAlignedBB> collisionBoxes = event.getCollisionBoxesList();

			final Vec3 v0 = vertices.get(0).offset(x, y, z);
			final Vec3 v1 = vertices.get(1).offset(x, y, z);
			final Vec3 v2 = vertices.get(2).offset(x, y, z);
			final Vec3 v3 = vertices.get(3).offset(x, y, z);
			final Vec3 v4 = vertices.get(4).offset(x, y, z);
			final Vec3 v5 = vertices.get(5).offset(x, y, z);
			final Vec3 v6 = vertices.get(6).offset(x, y, z);
			final Vec3 v7 = vertices.get(7).offset(x, y, z);

//		final AxisAlignedBB collisionBox = new AxisAlignedBB(v0.x, v0.y, v0.z, v6.x, v6.y, v6.z);
//		final AxisAlignedBB collisionBox = new AxisAlignedBB(v0.x, v0.y, v0.z, v6.x, v6.y, v6.z);
			final AxisAlignedBB box0 = new AxisAlignedBB(x, y, z, v0.x, v0.y, v0.z);

			collisionBoxes.clear();
			collisionBoxes.add(box0);

//		collisionBoxes.add(new AxisAlignedBB(new BlockPos(16, 16, 16)));
		} finally {
			// This gets called right before return, don't worry
			// (unless theres a BIG error in the try, in which case
			// releasing the pooled pos is the least of our worries)
			pooledMutableBlockPos.release();
		}
	}

}
