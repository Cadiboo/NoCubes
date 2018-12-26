package io.github.cadiboo.nocubes.client.render;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.World;

import java.util.ArrayList;

/**
 * @author Cadiboo
 * @see "http://mikolalysenko.github.io/Isosurface/js/marchingtetrahedra.js"
 */
public final class MarchingTetrahedra {

	private static final int[][] CUBE_VERTICES = {
			{0, 0, 0},
			{1, 0, 0},
			{1, 1, 0},
			{0, 1, 0},
			{0, 0, 1},
			{1, 0, 1},
			{1, 1, 1},
			{0, 1, 1}
	},

	TETRA_LIST = {
			{0, 2, 3, 7},
			{0, 6, 2, 7},
			{0, 4, 6, 7},
			{0, 6, 1, 2},
			{0, 1, 6, 4},
			{5, 6, 1, 4}
	};

	private static Vec3 interp(float[] grid, int[] c, int[] x, ArrayList<Vec3> vertices, int i0, int i1) {
		float g0 = grid[i0], g1 = grid[i1];
		int[] p0 = CUBE_VERTICES[i0], p1 = CUBE_VERTICES[i1];
		float[] v = new float[]{c[0] + x[0], c[1] + x[1], c[2] + x[2]};
		float t = g0 - g1;
		if (Math.abs(t) > 1e-6) {
			t = g0 / t;
		}
		for (int i = 0; i < 3; ++i) {
			v[i] += p0[i] + t * (p1[i] - p0[i]);
		}

		Vec3 vec = new Vec3(v[0], v[1], v[2]);
		vertices.add(vec);
		return vec;
	}

	public static void renderPre(final RebuildChunkPreEvent event) {

		final BlockPos renderChunkPos = event.getRenderChunkPosition();
		final int[] c = {renderChunkPos.getX(), renderChunkPos.getY(), renderChunkPos.getZ()};
		final BlockPos.PooledMutableBlockPos pos = BlockPos.PooledMutableBlockPos.retain();
		final BlockPos.PooledMutableBlockPos pooledMutablePos = BlockPos.PooledMutableBlockPos.retain();
		final ChunkCache cache = event.getChunkCache();

		final ArrayList<Vec3> vertices = new ArrayList<>();
		final ArrayList<Vec3[]> faces = new ArrayList<>();

		int n = 0;
		float[] grid = new float[8];
		int[] x = {0, 0, 0};

		int[] dims = {16, 16, 16};

		//March over the volume
		for (x[2] = 0; x[2] < dims[2] - 1; ++x[2], n += dims[0]) {
			for (x[1] = 0; x[1] < dims[1] - 1; ++x[1], ++n) {
				for (x[0] = 0; x[0] < dims[0] - 1; ++x[0], ++n) {
					pos.setPos(c[0] + x[0], c[1] + x[1], c[2] + x[2]);
					//Read in cube
					for (int i = 0; i < 8; ++i) {
//						grid[i] = data[n + cube_vertices[i][0] + dims[0] * (cube_vertices[i][1] + dims[1] * cube_vertices[i][2])];
//						grid[i] = ModUtil.getBlockDensity(renderChunkPos.add(x[0], x[1], x[2]).add(CUBE_VERTICES[i][0], CUBE_VERTICES[i][0], CUBE_VERTICES[i][0]), cache);
						pooledMutablePos.setPos(c[0] + x[0] + CUBE_VERTICES[i][0], c[1] + x[1] + CUBE_VERTICES[i][1], c[2] + x[2] + CUBE_VERTICES[i][2]);
						grid[i] = ModUtil.getBlockDensity(pooledMutablePos, cache);
					}
					for (int[] tetra : TETRA_LIST) {
						int triIndex = 0;
						if (grid[tetra[0]] < 0) triIndex |= 1;
						if (grid[tetra[1]] < 0) triIndex |= 2;
						if (grid[tetra[2]] < 0) triIndex |= 4;
						if (grid[tetra[3]] < 0) triIndex |= 8;

						//Handle each case
						switch (triIndex) {
							case 0x00:
							case 0x0F:
								break;
							case 0x0E:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[0], tetra[1])
										, interp(grid, c, x, vertices, tetra[0], tetra[3])
										, interp(grid, c, x, vertices, tetra[0], tetra[2])});
								break;
							case 0x01:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[0], tetra[1])
										, interp(grid, c, x, vertices, tetra[0], tetra[2])
										, interp(grid, c, x, vertices, tetra[0], tetra[3])});
								break;
							case 0x0D:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[1], tetra[0])
										, interp(grid, c, x, vertices, tetra[1], tetra[2])
										, interp(grid, c, x, vertices, tetra[1], tetra[3])});
								break;
							case 0x02:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[1], tetra[0])
										, interp(grid, c, x, vertices, tetra[1], tetra[3])
										, interp(grid, c, x, vertices, tetra[1], tetra[2])});
								break;
							case 0x0C:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[1], tetra[2])
										, interp(grid, c, x, vertices, tetra[1], tetra[3])
										, interp(grid, c, x, vertices, tetra[0], tetra[3])
										, interp(grid, c, x, vertices, tetra[0], tetra[2])});
								break;
							case 0x03:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[1], tetra[2])
										, interp(grid, c, x, vertices, tetra[0], tetra[2])
										, interp(grid, c, x, vertices, tetra[0], tetra[3])
										, interp(grid, c, x, vertices, tetra[1], tetra[3])});
								break;
							case 0x04:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[2], tetra[0])
										, interp(grid, c, x, vertices, tetra[2], tetra[1])
										, interp(grid, c, x, vertices, tetra[2], tetra[3])});
								break;
							case 0x0B:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[2], tetra[0])
										, interp(grid, c, x, vertices, tetra[2], tetra[3])
										, interp(grid, c, x, vertices, tetra[2], tetra[1])});
								break;
							case 0x05:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[0], tetra[1])
										, interp(grid, c, x, vertices, tetra[1], tetra[2])
										, interp(grid, c, x, vertices, tetra[2], tetra[3])
										, interp(grid, c, x, vertices, tetra[0], tetra[3])});
								break;
							case 0x0A:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[0], tetra[1])
										, interp(grid, c, x, vertices, tetra[0], tetra[3])
										, interp(grid, c, x, vertices, tetra[2], tetra[3])
										, interp(grid, c, x, vertices, tetra[1], tetra[2])});
								break;
							case 0x06:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[2], tetra[3])
										, interp(grid, c, x, vertices, tetra[0], tetra[2])
										, interp(grid, c, x, vertices, tetra[0], tetra[1])
										, interp(grid, c, x, vertices, tetra[1], tetra[3])});
								break;
							case 0x09:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[2], tetra[3])
										, interp(grid, c, x, vertices, tetra[1], tetra[3])
										, interp(grid, c, x, vertices, tetra[0], tetra[1])
										, interp(grid, c, x, vertices, tetra[0], tetra[2])});
								break;
							case 0x07:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[3], tetra[0])
										, interp(grid, c, x, vertices, tetra[3], tetra[1])
										, interp(grid, c, x, vertices, tetra[3], tetra[2])});
								break;
							case 0x08:
								faces.add(new Vec3[]{
										interp(grid, c, x, vertices, tetra[3], tetra[0])
										, interp(grid, c, x, vertices, tetra[3], tetra[2])
										, interp(grid, c, x, vertices, tetra[3], tetra[1])});
								break;
						}

						faces.forEach(face -> {

							if (face.length != 3 && face.length != 4) {
								return; //TODO: wtf, how did we get here?
							}

							final IBlockState state = cache.getBlockState(pos);

							final BlockRenderData renderData = ClientUtil.getBlockRenderData(pos, cache);

							final BlockRenderLayer blockRenderLayer = renderData.getBlockRenderLayer();
							final int red = renderData.getRed();
							final int green = renderData.getGreen();
							final int blue = renderData.getBlue();
							final int alpha = renderData.getAlpha();
							final float minU = renderData.getMinU();
							final float maxU = renderData.getMaxU();
							final float minV = renderData.getMinV();
							final float maxV = renderData.getMaxV();
							final int lightmapSkyLight = renderData.getLightmapSkyLight();
							final int lightmapBlockLight = renderData.getLightmapBlockLight();

							final BufferBuilder bufferBuilder = event.getGenerator().getRegionRenderCacheBuilder().getWorldRendererByLayer(blockRenderLayer);
							final CompiledChunk compiledChunk = event.getCompiledChunk();

							if (!compiledChunk.isLayerStarted(blockRenderLayer)) {
								compiledChunk.setLayerStarted(blockRenderLayer);
								ClientUtil.compiledChunk_setLayerUsed(compiledChunk, blockRenderLayer);
								//pre render blocks
								bufferBuilder.begin(7, DefaultVertexFormats.BLOCK);
								bufferBuilder.setTranslation((double) (-renderChunkPos.getX()), (double) (-renderChunkPos.getY()), (double) (-renderChunkPos.getZ()));

							}

							if (face.length == 3) {
								final Vec3 vertex0 = face[0];
								final Vec3 vertex1 = face[1];
								final Vec3 vertex2 = face[2];
								//pretend its a quad
								bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							} else {
								final Vec3 vertex0 = face[0];
								final Vec3 vertex1 = face[1];
								final Vec3 vertex2 = face[2];
								final Vec3 vertex3 = face[3];
								bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
								bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight, lightmapBlockLight).endVertex();
							}

						});

						//we just rendered all the faces, now clear it in prep for next batch
						faces.clear();

					}
				}
			}
		}

	}

	public static void renderLayer(final RebuildChunkBlockRenderInLayerEvent event) {

	}

	public static void renderType(final RebuildChunkBlockRenderInTypeEvent event) {

	}

	public static void renderBlock(final RebuildChunkBlockEvent event) {

	}

	public static void renderPost(final RebuildChunkPostEvent event) {

	}

	public static Vec3[] getPoints(final BlockPos blockPos, final World world) {
		return null;
	}

}
