package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;
import java.util.List;

import static io.github.cadiboo.nocubes.NoCubes.VERTICES;

/**
 * Subscribe to events that should be handled on both PHYSICAL sides in this class
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID)
public final class EventSubscriber {

	@SubscribeEvent
	public static void onGetCollisionBoxesEvent(@Nonnull final GetCollisionBoxesEvent event) {

		final AxisAlignedBB aabb = event.getAabb();
		final BlockPos pos = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);

		final Vec3[] vertices = VERTICES.get(pos);
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

		final AxisAlignedBB collisionBox = new AxisAlignedBB(v0.xCoord, v0.yCoord, v0.zCoord, v7.xCoord, v7.yCoord, v7.zCoord);

	}

}
