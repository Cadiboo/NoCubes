package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Random;

/**
 * @author Cadiboo
 */
public class MeshRenderer {

	public static void renderChunk(final ChunkRenderDispatcher.ChunkRender.RebuildTask rebuildTask, ChunkRenderDispatcher.ChunkRender chunkRender, final ChunkRenderDispatcher.CompiledChunk compiledChunkIn, final RegionRenderCacheBuilder builderIn, final BlockPos blockpos, final ChunkRenderCache chunkrendercache, final MatrixStack matrixstack, final Random random, final BlockRendererDispatcher blockrendererdispatcher) {
		if (NoCubesConfig.Client.render) {
			SurfaceNets.generate(
				blockpos.getX(), blockpos.getY(), blockpos.getZ(),
				16, 16, 16, chunkrendercache, NoCubes.smoothableHandler::isSmoothable,
				(pos, face) -> {
					final Vec v0 = face.v0;
					final Vec v1 = face.v1;
					final Vec v2 = face.v2;
					final Vec v3 = face.v3;
					final SmoothableHandler handler = NoCubes.smoothableHandler;
					// V0's normal
					final Vec n0 = Vec.normal(v3, v0, v1);
					float nx = 1F - (float) n0.x;
					float ny = 1F - (float) n0.y;
					float nz = 1F - (float) n0.z;
					n0.close();

					final Direction direction;
					// North: negative Z
					// East: positive X
					// South: positive Z
					// West: negative X
					float nm = Math.max(Math.max(Math.abs(nx), Math.abs(ny)), Math.abs(nz));
					if (nm == Math.abs(nx))
						direction = nm > 0 ? Direction.EAST : Direction.WEST;
					else if (nm == Math.abs(ny))
						direction = nm > 0 ? Direction.UP : Direction.DOWN;
					else if (nm == Math.abs(nz))
						direction = nm > 0 ? Direction.SOUTH : Direction.NORTH;
					else
						throw new IllegalStateException("Could not find a direction from the normal, wtf???");

					for (RenderType rendertype : RenderType.getBlockRenderTypes()) {
						if (pos.getX() > blockpos.getX() - 2 || pos.getX() > blockpos.getX() + 17)
							continue;
						if (pos.getY() > blockpos.getY() - 2 || pos.getY() > blockpos.getY() + 17)
							continue;
						if (pos.getY() > blockpos.getY() - 2 || pos.getY() > blockpos.getY() + 17)
							continue;
						net.minecraftforge.client.model.data.IModelData modelData = rebuildTask.getModelData(pos);
						BlockState bs = Blocks.SCAFFOLDING.getDefaultState();
						if (bs.getRenderType() != BlockRenderType.INVISIBLE && RenderTypeLookup.canRenderInLayer(bs, rendertype)) {
							RenderType rendertype1 = rendertype;
							BufferBuilder bufferbuilder2 = builderIn.getBuilder(rendertype1);
							if (compiledChunkIn.layersStarted.add(rendertype1)) {
								chunkRender.beginLayer(bufferbuilder2);
							}

							matrixstack.push();
							matrixstack.translate((double) (pos.getX() & 15), (double) (pos.getY() & 15), (double) (pos.getZ() & 15));
							if (blockrendererdispatcher.renderModel(bs, pos, chunkrendercache, matrixstack, bufferbuilder2, true, random, modelData)) {
								compiledChunkIn.empty = false;
								compiledChunkIn.layersUsed.add(rendertype1);
							}

							matrixstack.pop();
						}
					}
					net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);

					BlockState blockstate = chunkrendercache.getBlockState(pos);
					// Vertices can generate at positions different to the position of the block they are for
					// This occurs mostly for positions below, west of and north of the position they are for
					// Search the opposite of those directions for the actual block
					// We could also attempt to get the state from the vertex positions
					if (!handler.isSmoothable(blockstate) || blockstate.isAir(chunkrendercache, pos)) {
						int x = pos.getX();
						int y = pos.getY();
						int z = pos.getZ();
						blockstate = chunkrendercache.getBlockState(pos.setPos(x, y + 1, z)); // UP
						if (!handler.isSmoothable(blockstate) || blockstate.isAir(chunkrendercache, pos))
							blockstate = chunkrendercache.getBlockState(pos.setPos(x + 1, y, z)); // EAST
						if (!handler.isSmoothable(blockstate) || blockstate.isAir(chunkrendercache, pos))
							blockstate = chunkrendercache.getBlockState(pos.setPos(x, y, z + 1)); // SOUTH
						if (!handler.isSmoothable(blockstate) || blockstate.isAir(chunkrendercache, pos)) {
							// Give up
							blockstate = Blocks.SCAFFOLDING.getDefaultState();
							pos.setPos(x, y, z);
						}
					}

					long rand = blockstate.getPositionRandom(pos);

					net.minecraftforge.client.model.data.IModelData modelData = rebuildTask.getModelData(pos);
					for (RenderType rendertype : RenderType.getBlockRenderTypes()) {
						if (blockstate.getRenderType() == BlockRenderType.INVISIBLE || !RenderTypeLookup.canRenderInLayer(blockstate, rendertype))
							continue;
						net.minecraftforge.client.ForgeHooksClient.setRenderLayer(rendertype);
						BufferBuilder bufferbuilder = builderIn.getBuilder(rendertype);
						if (compiledChunkIn.layersStarted.add(rendertype)) {
							chunkRender.beginLayer(bufferbuilder);
						}
						matrixstack.push();
						matrixstack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

						IBakedModel modelIn = blockrendererdispatcher.getModelForState(blockstate);
						int light = WorldRenderer.getPackedLightmapCoords(chunkrendercache, blockstate, pos.offset(direction));
						random.setSeed(rand);
						List<BakedQuad> quads = modelIn.getQuads(blockstate, direction, random, modelData);
						if (quads.isEmpty()) // Barrier block
							quads = blockrendererdispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(blockstate, direction, random, modelData);
						for (int i1 = 0; i1 < quads.size(); i1++) {
							final BakedQuad quad = quads.get(i1);

							final int formatSize = DefaultVertexFormats.BLOCK.getIntegerSize();
							final int[] vertexData = quad.getVertexData();
							// Quads are packed xyz|argb|u|v|ts
							final float v0u = Float.intBitsToFloat(vertexData[4]);
							final float v0v = Float.intBitsToFloat(vertexData[5]);
							final float v1u = Float.intBitsToFloat(vertexData[formatSize + 4]);
							final float v1v = Float.intBitsToFloat(vertexData[formatSize + 5]);
							final float v2u = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
							final float v2v = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
							final float v3u = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
							final float v3v = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);
							bufferbuilder.pos(v0.x, v0.y, v0.z).color(1.0F, 1, 1, 1).tex(v0u, v0v).lightmap(light).normal(nx, ny, nz).endVertex();
							bufferbuilder.pos(v1.x, v1.y, v1.z).color(1.0F, 1, 1, 1).tex(v1u, v1v).lightmap(light).normal(nx, ny, nz).endVertex();
							bufferbuilder.pos(v2.x, v2.y, v2.z).color(1.0F, 1, 1, 1).tex(v2u, v2v).lightmap(light).normal(nx, ny, nz).endVertex();
							bufferbuilder.pos(v3.x, v3.y, v3.z).color(1.0F, 1, 1, 1).tex(v3u, v3v).lightmap(light).normal(nx, ny, nz).endVertex();
						}

						if (true) {
							compiledChunkIn.empty = false;
							compiledChunkIn.layersUsed.add(rendertype);
						}
						matrixstack.pop();
					}
					net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);
					return true;
				}
			);
		}
	}

}
