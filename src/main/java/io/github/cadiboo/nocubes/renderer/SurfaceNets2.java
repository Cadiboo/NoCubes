package io.github.cadiboo.nocubes.renderer;

import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;

import java.util.concurrent.ConcurrentHashMap;

import static io.github.cadiboo.nocubes.NoCubes.LOGGER;

public class SurfaceNets2 {

	public static volatile ConcurrentHashMap<BlockPos, ConcurrentHashMap<BlockPos, Object[]>> renderChunkPosConcurrentHashMap = new ConcurrentHashMap<>();

	public static void renderPre(final RebuildChunkPreEvent event) {

		final ChunkCache cache = event.getChunkCache();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();

		// The shadow values.
		final float shadowBottom = 0.6F;
		final float shadowTop = 1.0F;
		final float shadowLeft = 0.9F;
		final float shadowRight = 0.8F;

		final ConcurrentHashMap<BlockPos, Object[]> blockPosConcurrentHashMap = new ConcurrentHashMap<>();

		for (BlockPos pos : BlockPos.getAllInBoxMutable(event.getRenderChunkPosition(), event.getRenderChunkPosition().add(15, 15, 15))) {

			final IBlockState state = cache.getBlockState(pos);

			if (!ModUtil.shouldSmooth(state)) {
				continue;
			}

			final int x = pos.getX();
			final int y = pos.getY();
			final int z = pos.getZ();

			// The 8 points (corners) that make the block.
			Vec3[] points = new Vec3[8];
			points[0] = new Vec3(0.0D, 0.0D, 0.0D);
			points[1] = new Vec3(1.0D, 0.0D, 0.0D);
			points[2] = new Vec3(1.0D, 0.0D, 1.0D);
			points[3] = new Vec3(0.0D, 0.0D, 1.0D);
			points[4] = new Vec3(0.0D, 1.0D, 0.0D);
			points[5] = new Vec3(1.0D, 1.0D, 0.0D);
			points[6] = new Vec3(1.0D, 1.0D, 1.0D);
			points[7] = new Vec3(0.0D, 1.0D, 1.0D);

			// Loop through all the points:
			// Here everything will be 'smoothed'.
			for (int point = 0; point < 8; point++) {
				// Give the point the block's coordinates.
				points[point].xCoord += (double) x;
				points[point].yCoord += (double) y;
				points[point].zCoord += (double) z;

				// Check if the point is NOT intersecting with a manufactured block.
				if (!OldNoCubes.doesPointIntersectWithManufactured(cache, points[point])) {
					// Check if the block's bottom side intersects with air.
					if (point < 4 && OldNoCubes.doesPointBottomIntersectWithAir(cache, points[point]))
						points[point].yCoord = (double) y + 1.0D;
						// Check if the block's top side intersects with air.
					else if (point >= 4 && OldNoCubes.doesPointTopIntersectWithAir(cache, points[point]))
						points[point].yCoord = (double) y;

					// Give the point some random offset.
					points[point] = OldNoCubes.givePointRoughness(points[point]);
				}
			}

			boolean wasAnythingRendered = false;

			final Object[] enumFacingToData = new Object[EnumFacing.VALUES.length];

			// Loop through all the sides of the block:
			for (EnumFacing side : EnumFacing.VALUES) {

				final Vec3[] vertices = new Vec3[4];

				// Check if the side should be rendered:
				// This prevents a lot of lag!
				if (state.shouldSideBeRendered(cache, pos, side)) {
					wasAnythingRendered = true;

					// When you lower this value the block will become darker.
					float colorFactor = 1.0F;

					// These are the vertices used for the side.
					Vec3 vertex0 = null;
					Vec3 vertex1 = null;
					Vec3 vertex2 = null;
					Vec3 vertex3 = null;

					//to find the vertices for a side get the points in this order
					// 1➡️2
					//   ⬇️
					// 4⬅️3

					// to find the vertices for the opposite side
					// just get the points with the opposite axis for the side (for example east is +x and west is -x)
					// and then flip the order

					//example
					// west:
					// (0,0,0) (0,0,1)
					// (0,1,1) (0,1,0)

					// to get east you add 1 to the x coord for every point (east is +x and west is -x)
					// (1,0,0) (1,0,1)
					// (1,1,1) (1,1,0)

					// then flip the order (1234 -> 4321)
					// (1,1,0) (1,1,1)
					// (1,0,1) (1,0,0)

					switch (side) {
						case DOWN:
							colorFactor = shadowBottom;
							vertex0 = points[0];
							vertex1 = points[1];
							vertex2 = points[2];
							vertex3 = points[3];
							break;
						case UP:
							colorFactor = shadowTop;
							vertex0 = points[7];
							vertex1 = points[6];
							vertex2 = points[5];
							vertex3 = points[4];
							break;
						case NORTH:
							colorFactor = shadowLeft;
							vertex0 = points[1];
							vertex1 = points[0];
							vertex2 = points[4];
							vertex3 = points[5];
							break;
						case SOUTH:
							colorFactor = shadowRight;
							vertex0 = points[6];
							vertex1 = points[7];
							vertex2 = points[3];
							vertex3 = points[2];
							break;
						case WEST:
							colorFactor = shadowLeft;
							vertex0 = points[0];
							vertex1 = points[3];
							vertex2 = points[7];
							vertex3 = points[4];
							break;
						case EAST:
							colorFactor = shadowRight;
							vertex0 = points[5];
							vertex1 = points[6];
							vertex2 = points[2];
							vertex3 = points[1];
							break;
					}

					vertices[0] = new Vec3(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord);
					vertices[1] = new Vec3(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord);
					vertices[2] = new Vec3(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord);
					vertices[3] = new Vec3(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord);

					enumFacingToData[side.ordinal()] = new Object[]{colorFactor, vertices};

				}
			}

			blockPosConcurrentHashMap.put(pos.toImmutable(), new Object[]{wasAnythingRendered, enumFacingToData});

		}

		renderChunkPosConcurrentHashMap.put(renderChunkPos.toImmutable(), blockPosConcurrentHashMap);

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {
		SurfaceNets.renderLayer(event);
	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {
		SurfaceNets.renderType(event);
	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {
		final IBlockState state = event.getBlockState();
		if (!ModUtil.shouldSmooth(state)) {
			return;
		}
		final BlockPos pos = event.getBlockPos();
		final BlockPos renderChunkPos = event.getRenderChunkPosition();
		final BufferBuilder bufferBuilder = event.getBufferBuilder();
		final BlockRendererDispatcher blockRendererDispatcher = event.getBlockRendererDispatcher();
		final ChunkCache cache = event.getChunkCache();

		final float shadowTop = 1.0F;

		final TextureAtlasSprite sprite = ModUtil.getSprite(state, pos, blockRendererDispatcher);
		if (sprite == null) {
			return;
		}
		final double minU = sprite.getMinU();
		final double minV = sprite.getMinV();
		final double maxU = sprite.getMaxU();
		final double maxV = sprite.getMaxV();

		final LightmapInfo lightmapInfo = ModUtil.getLightmapInfo(pos, cache);
		final int lightmapSkyLight = lightmapInfo.getLightmapSkyLight();
		final int lightmapBlockLight = lightmapInfo.getLightmapBlockLight();

		try {
			final Object[] data = renderChunkPosConcurrentHashMap.get(renderChunkPos).get(pos);
			final boolean wasAnythingRendered = (boolean) data[0];
			if (!wasAnythingRendered) {
				return;
			} else {
				event.getUsedBlockRenderLayers()[state.getBlock().getRenderLayer().ordinal()] = true;
			}
			final Object[] enumFacingToData = (Object[]) data[1];
			for (EnumFacing side : EnumFacing.VALUES) {
				final Object[] sideData = (Object[]) enumFacingToData[side.ordinal()];
				if (sideData == null) {
					continue;
				}
				final float colorFactor = (float) sideData[0];
				final Vec3[] vertices = (Vec3[]) sideData[1];

				final Vec3 vertex0 = vertices[0];
				final Vec3 vertex1 = vertices[1];
				final Vec3 vertex2 = vertices[2];
				final Vec3 vertex3 = vertices[3];

				// And finally the side is going to be rendered!
//				bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue, 0xFF).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue, 0xFF).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue, 0xFF).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
//				bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(shadowTop * colorFactor * colorRed, shadowTop * colorFactor * colorGreen, shadowTop * colorFactor * colorBlue, 0xFF).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

				bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(shadowTop * colorFactor * 0, shadowTop * colorFactor * 1, shadowTop * colorFactor * 1, 1).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(shadowTop * colorFactor * 0, shadowTop * colorFactor * 1, shadowTop * colorFactor * 1, 1).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(shadowTop * colorFactor * 0, shadowTop * colorFactor * 1, shadowTop * colorFactor * 1, 1).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
				bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(shadowTop * colorFactor * 0, shadowTop * colorFactor * 1, shadowTop * colorFactor * 1, 1).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();

			}

		} catch (final NullPointerException npe) {
			LOGGER.warn(npe);
		}

		event.setCanceled(!(state.getBlock() instanceof BlockGrass));

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

		final BlockPos renderChunkPos = event.getRenderChunkPosition();

		try {
			renderChunkPosConcurrentHashMap.remove(renderChunkPos);
		} catch (Exception e) {
			LOGGER.error(e);
		}

	}

}
