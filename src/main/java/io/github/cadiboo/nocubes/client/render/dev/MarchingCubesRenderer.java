package io.github.cadiboo.nocubes.client.render.dev;

import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.proxy.OptiFine;
import io.github.cadiboo.nocubes.hooks.Hooks;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderTask;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.IWorld;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Random;

import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES;
import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES_LENGTH;
import static io.github.cadiboo.nocubes.mesh.generator.MarchingCubes.CUBE_VERTS;
import static io.github.cadiboo.nocubes.mesh.generator.MarchingCubes.EDGE_INDEX;
import static io.github.cadiboo.nocubes.mesh.generator.MarchingCubes.EDGE_TABLE;
import static io.github.cadiboo.nocubes.mesh.generator.MarchingCubes.TRI_TABLE;

/**
 * @author Cadiboo
 */
public class MarchingCubesRenderer {

	public static void renderChunk(
			@Nonnull final ChunkRender chunkRender,
			@Nonnull final BlockPos chunkRenderPos,
			@Nonnull final ChunkRenderTask chunkRenderTask,
			@Nonnull final CompiledChunk compiledChunk,
			// Use World for eagerly generated caches
			@Nonnull final IWorld world,
			// Use RenderChunkCache for lazily generated caches
			@Nonnull final IEnviromentBlockReader chunkRenderCache,
			@Nonnull final boolean[] usedBlockRenderLayers,
			@Nonnull final Random random,
			@Nonnull final BlockRendererDispatcher blockRendererDispatcher
	) {
		final int posX = chunkRenderPos.getX();
		final int posY = chunkRenderPos.getY();
		final int posZ = chunkRenderPos.getZ();

		final RegionRenderCacheBuilder builders = chunkRenderTask.getRegionRenderCacheBuilder();
		final OptiFine optiFine = OptiFineCompatibility.get();

		try (PooledMutableBlockPos pos = PooledMutableBlockPos.retain()) {
			for (int z = 0; z < 16; ++z) {
				for (int y = 0; y < 16; ++y) {
					for (int x = 0; x < 16; ++x) {
						pos.setPos(posX + x, posY + y, posZ + z);

						final BlockState blockState = chunkRenderCache.getBlockState(pos);
						final IFluidState fluidState = chunkRenderCache.getFluidState(pos);

						boolean forgeRenderLayerChanged = false;
						IModelData modelData = null;

						for (int i = BLOCK_RENDER_LAYER_VALUES_LENGTH - 1; i >= 0; i--) {
							final BlockRenderLayer initial = BLOCK_RENDER_LAYER_VALUES[i];

							final boolean renderFluid = !fluidState.isEmpty() && fluidState.canRenderInLayer(initial);
							final boolean renderBlock = !Hooks.canBlockStateRender(blockState) && blockState.getRenderType() != BlockRenderType.INVISIBLE;

							int layerOrdinal = 0;
							BlockRenderLayer layer = null;
							BufferBuilder bufferBuilder = null;

							if (renderFluid || renderBlock) {
								layerOrdinal = ClientUtil.getCorrectRenderLayer(i);
								layer = BLOCK_RENDER_LAYER_VALUES[layerOrdinal];
								ForgeHooksClient.setRenderLayer(layer);
								forgeRenderLayerChanged = true;
								bufferBuilder = builders.getBuilder(layerOrdinal);
								if (modelData == null)
									modelData = chunkRenderTask.getModelData(pos);
							}
							if (renderFluid) {
//								Object renderEnv = optiFine.preRenderBlock(bufferBuilder, layer, fluidState, pos, builders, chunkRenderCache);
								Object renderEnv = optiFine.preRenderBlock(bufferBuilder, layer, blockState, pos, builders, chunkRenderCache);
								ClientUtil.startOrContinueBufferBuilder(compiledChunk, layer, chunkRender, chunkRenderPos, bufferBuilder);
								usedBlockRenderLayers[layerOrdinal] |= blockRendererDispatcher.renderFluid(pos, chunkRenderCache, bufferBuilder, fluidState);
								optiFine.postRenderBlock(renderEnv, chunkRender, builders, compiledChunk, usedBlockRenderLayers);
							}
							if (renderBlock) {
								Object renderEnv = optiFine.preRenderBlock(bufferBuilder, layer, blockState, pos, builders, chunkRenderCache);
								ClientUtil.startOrContinueBufferBuilder(compiledChunk, layer, chunkRender, chunkRenderPos, bufferBuilder);
//								usedBlockRenderLayers[layerOrdinal] |= blockRendererDispatcher.renderBlock(blockState, pos, chunkRenderCache, bufferBuilder, random, modelData);
								usedBlockRenderLayers[layerOrdinal] |= render(chunkRender, chunkRenderPos, chunkRenderTask, compiledChunk, world, chunkRenderCache, random, blockRendererDispatcher, builders, pos, layer, bufferBuilder, modelData, renderEnv);
								optiFine.postRenderBlock(renderEnv, chunkRender, builders, compiledChunk, usedBlockRenderLayers);
							}
						}
						if (forgeRenderLayerChanged)
							ForgeHooksClient.setRenderLayer(null);
					}
				}
			}
		}
	}

	private static boolean render(final ChunkRender chunkRender, final BlockPos chunkRenderPos, final ChunkRenderTask chunkRenderTask, final CompiledChunk compiledChunk, final IWorld world, final IEnviromentBlockReader chunkRenderCache, final Random random, final BlockRendererDispatcher blockRendererDispatcher, final RegionRenderCacheBuilder builders, final PooledMutableBlockPos pos, final BlockRenderLayer layer, final BufferBuilder bufferBuilder, final IModelData modelData, final Object renderEnv) {
		float isolevel = 0.5F;

		float[] grid = new float[8];
		for (int i = 0; i < CUBE_VERTS.length; i++) {
			final byte[] offset = CUBE_VERTS[i];
			final BlockState state = chunkRenderCache.getBlockState(pos.add(offset[0], offset[1], offset[2]));
			grid[i] = state.nocubes_isTerrainSmoothable ? 0 : 1;
		}

		// Calculate the index into the edge table which tells us which vertices
		// are inside of the surface as follows:
		// Loop over each of the 8 corners of the cube, and set the corresponding
		// bit to 1 if its value is below the surface level.
		// This will result in a value between 0 and 255.
		byte cubeIndex = 0;
		for (int i = 0; i < 8; i++) {
			if (grid[i] < isolevel)
				cubeIndex |= 1 << i;
		}

		final short edgeIndex = EDGE_TABLE[cubeIndex];
		if (edgeIndex == 0)
			return false; // Cube is entirely in/out of the surface

		int[] edges2vertices = new int[12]; // TODO: why 12?

		ArrayList<float[]> vertices = new ArrayList<>();

		// Look up the triangulation for the current cubeIndex.
		// Each entry is the index of an edge.
		final byte[] triangulation = TRI_TABLE[cubeIndex];
		for (final byte b : triangulation) {
			// Lookup the indices of the corner points making up the current edge
			byte[] pointsLookup = EDGE_INDEX[b];
			final byte point0Index = pointsLookup[0];
			final byte point1Index = pointsLookup[1];
			byte[] cornerPoint0 = CUBE_VERTS[point0Index];
			byte[] cornerPoint1 = CUBE_VERTS[point1Index];
//			final float point0Density = grid[point0Index];
//			final float point1Density = grid[point1Index];

			// Find midpoint of edge
			float[] newVertex = {
					(cornerPoint0[0] + cornerPoint1[0]) / 2F,
					(cornerPoint0[1] + cornerPoint1[1]) / 2F,
					(cornerPoint0[2] + cornerPoint1[2]) / 2F,
			};

			// Add position to vertex list
			vertices.add(newVertex);

		}

		// Instead of just iterating vertices we use the
		byte[] f = TRI_TABLE[cubeIndex];
		for (byte i = 0; i < f.length; i += 3) {
			try (Face face = Face.retain(
					Vec3.retain(vertices.get(edges2vertices[f[i]])),
					Vec3.retain(vertices.get(edges2vertices[f[i + 1]])),
					Vec3.retain(vertices.get(edges2vertices[f[i + 2]]))
			)) {
				// Render the face
			}
		}

		return true;
	}

	/**
	 * Linearly interpolate the position where an isosurface cuts
	 * an edge between two vertices, each with their own scalar value
	 */
	private static Vec3 vertexInterp(double isolevel, Vec3 p1, Vec3 p2, double valp1, double valp2) {
		if (Math.abs(isolevel - valp1) < 0.00001)
			return (p1);
		if (Math.abs(isolevel - valp2) < 0.00001)
			return (p2);
		if (Math.abs(valp1 - valp2) < 0.00001)
			return (p1);
		double mu = (isolevel - valp1) / (valp2 - valp1);
		Vec3 p = Vec3.retain(0, 0, 0);
		p.x = p1.x + mu * (p2.x - p1.x);
		p.y = p1.y + mu * (p2.y - p1.y);
		p.z = p1.z + mu * (p2.z - p1.z);

		return p;
	}

}
