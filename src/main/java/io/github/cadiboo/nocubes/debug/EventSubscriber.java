package io.github.cadiboo.nocubes.debug;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.OldNoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.block.state.IBlockState;
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

		if (!ModConfig.Debug.debugEnabled) {
			return;
		}

		final AxisAlignedBB bb = event.getEntityBoundingBox();
		final World world = event.getEntity().world;

		int minX = MathHelper.floor(bb.minX);
		int maxX = MathHelper.ceil(bb.maxX);
		int minY = MathHelper.floor(bb.minY);
		int maxY = MathHelper.ceil(bb.maxY);
		int minZ = MathHelper.floor(bb.minZ);
		int maxZ = MathHelper.ceil(bb.maxZ);
		BlockPos.PooledMutableBlockPos pooledMutableBlockPos = BlockPos.PooledMutableBlockPos.retain();

		for (int x = minX; x < maxX; ++x) {
			for (int y = minY; y < maxY; ++y) {
				for (int z = minZ; z < maxZ; ++z) {
					IBlockState state = world.getBlockState(pooledMutableBlockPos.setPos(x, y, z));

					if (ModUtil.shouldSmooth(state)) {
						pooledMutableBlockPos.release();
						event.setCanceled(true);
						return;
					}
				}
			}
		}
		pooledMutableBlockPos.release();

	}

	@SubscribeEvent
	public static void onGetCollisionBoxesEvent(@Nonnull final GetCollisionBoxesEvent event) {

		if (true) return;

		if (!NoCubes.isEnabled()) {
			return;
		}

		final AxisAlignedBB aabb = event.getAabb();

		final Vec3[] vertices = OldNoCubes.getPoints(new BlockPos(aabb.minX, aabb.minY, aabb.minZ), event.getWorld());

		if (vertices == null) {
			return;
		}

		final List<AxisAlignedBB> collisionBoxes = event.getCollisionBoxesList();

		final Vec3 v0 = vertices[0];
		final Vec3 v1 = vertices[1];
		final Vec3 v2 = vertices[2];
		final Vec3 v3 = vertices[3];
		final Vec3 v4 = vertices[4];
		final Vec3 v5 = vertices[5];
		final Vec3 v6 = vertices[6];
		final Vec3 v7 = vertices[7];

//		final AxisAlignedBB collisionBox = new AxisAlignedBB(v0.xCoord, v0.yCoord, v0.zCoord, v6.xCoord, v6.yCoord, v6.zCoord);
		final AxisAlignedBB collisionBox = new AxisAlignedBB(v0.xCoord, v0.yCoord, v0.zCoord, v6.xCoord, v6.yCoord, v6.zCoord);

		collisionBoxes.clear();
//		collisionBoxes.add(collisionBox);

		collisionBoxes.add(new AxisAlignedBB(new BlockPos(16, 16, 16)));

	}

}
