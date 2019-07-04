package io.github.cadiboo.nocubes;

import io.github.cadiboo.nocubes.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;
import static net.minecraftforge.fml.common.Mod.EventBusSubscriber;

/**
 * @author Cadiboo
 */
@EventBusSubscriber(modid = NoCubes.MOD_ID)
public final class ForgeEventSubscriber {

	private static final Logger LOGGER = LogManager.getLogger();

	@SubscribeEvent
	public static void onTickEvent(final TickEvent event) {
		Config.terrainCollisions = false;
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(final TickEvent.PlayerTickEvent event) {
		final Minecraft minecraft = Minecraft.getInstance();

		final RayTraceResult objectMouseOver = minecraft.objectMouseOver;
		if (objectMouseOver == null || objectMouseOver.getType() != BLOCK) {
			return;
		}

		BlockPos blockPos = ((BlockRayTraceResult) objectMouseOver).getPos();

		if (!blockPos.equals(new BlockPos(245, 95, -277))) {
			return;
		}
		final World world = event.player.world;

		blockPos = new BlockPos(((245 >> 4) - 2 << 4) + 8, 95, ((-277 >> 4) - 2 << 4) + 8);

		try (PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain()) {
			for (int r = 0; r <= 0xF; ++r)
				for (int g = 0; g <= 0xF; ++g)
					for (int b = 0; b <= 0xF; ++b) {
						pooledMutableBlockPos.setPos(blockPos).move(r, g, b).move(0, 20, 0);
						world.setBlockState(pooledMutableBlockPos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("simplecoloredblocks", r + "r_" + g + "g_" + b + "b_glass")).getDefaultState());
					}

			int r = 8;

			for (int tx = -r; tx < r + 1; tx++) {
				for (int ty = -r; ty < r + 1; ty++) {
					for (int tz = -r; tz < r + 1; tz++) {
						if (Math.sqrt(Math.pow(tx, 2) + Math.pow(ty, 2) + Math.pow(tz, 2)) <= r - 2) {
							pooledMutableBlockPos.setPos(blockPos).move(tx, ty, tz).move(8, 50, 8);
							world.setBlockState(pooledMutableBlockPos, ForgeRegistries.BLOCKS.getValue(new ResourceLocation("simplecoloredblocks", (8 + tx) + "r_" + (8 + ty) + "g_" + (8 + tz) + "b_glass")).getDefaultState());
						}
					}
				}
			}
		}
	}

}
