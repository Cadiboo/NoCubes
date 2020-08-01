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
					// Normals TODO: Optimise
					final Vec n0 = Vec.normal(v3, v0, v1).multiply(-1);
					final Vec n2 = Vec.normal(v1, v2, v3).multiply(-1);
					final Vec nAverage = Vec.of(
						(n0.x + n2.x) / 2,
						(n0.y + n2.y) / 2,
						(n0.z + n2.z) / 2
					);
					final Direction direction = getDirectionFromNormal(nAverage);
					float nx = (float) n0.x;
					float ny = (float) n0.y;
					float nz = (float) n0.z;
					n0.close();
					n2.close();
					nAverage.close();

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
					if (!handler.isSmoothable(blockstate)) {
						int x = pos.getX();
						int y = pos.getY();
						int z = pos.getZ();
						blockstate = chunkrendercache.getBlockState(pos.move(direction.getOpposite()));
//						blockstate = chunkrendercache.getBlockState(pos.setPos(x, y + 1, z)); // UP
//						if (!handler.isSmoothable(blockstate))
//							blockstate = chunkrendercache.getBlockState(pos.setPos(x + 1, y, z)); // EAST
//						if (!handler.isSmoothable(blockstate))
//							blockstate = chunkrendercache.getBlockState(pos.setPos(x, y, z + 1)); // SOUTH
						if (!handler.isSmoothable(blockstate)) {
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
						List<BakedQuad> dirQuads = modelIn.getQuads(blockstate, direction, random, modelData);
						random.setSeed(rand);
						List<BakedQuad> nullQuads = modelIn.getQuads(blockstate, null, random, modelData);
						if (dirQuads.isEmpty() && nullQuads.isEmpty()) // dirQuads is empty for the Barrier block
							dirQuads = blockrendererdispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(blockstate, direction, random, modelData);
						int dirQuadsSize = dirQuads.size();
						for (int i1 = 0; i1 < dirQuadsSize + nullQuads.size(); i1++) {
							final BakedQuad quad = i1 < dirQuadsSize ? dirQuads.get(i1) : nullQuads.get(i1 - dirQuadsSize);

							final int formatSize = DefaultVertexFormats.BLOCK.getIntegerSize();
							final int[] vertexData = quad.getVertexData();
							// Quads are packed xyz|argb|u|v|ts
							final float texu0 = Float.intBitsToFloat(vertexData[4]);
							final float texv0 = Float.intBitsToFloat(vertexData[5]);
							final float texu1 = Float.intBitsToFloat(vertexData[formatSize + 4]);
							final float texv1 = Float.intBitsToFloat(vertexData[formatSize + 5]);
							final float texu2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
							final float texv2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
							final float texu3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
							final float texv3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);
							final float v0u;
							final float v0v;
							final float v1u;
							final float v1v;
							final float v2u;
							final float v2v;
							final float v3u;
							final float v3v;
							switch (direction) {
								case DOWN:
								case SOUTH:
								case WEST:
									v0u = texu3;
									v0v = texv3;
									v1u = texu0;
									v1v = texv0;
									v2u = texu1;
									v2v = texv1;
									v3u = texu2;
									v3v = texv2;
									break;
								case UP:
									v0u = texu2;
									v0v = texv2;
									v1u = texu3;
									v1v = texv3;
									v2u = texu0;
									v2v = texv0;
									v3u = texu1;
									v3v = texv1;
									break;
								case NORTH:
								case EAST:
									v0u = texu0;
									v0v = texv0;
									v1u = texu1;
									v1v = texv1;
									v2u = texu2;
									v2v = texv2;
									v3u = texu3;
									v3v = texv3;
									break;
								default:
									throw new IllegalStateException("Unexpected value: " + direction);
							}
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

	public static Direction getDirectionFromNormal(Vec normal) {
		double x = normal.x;
		double y = normal.y;
		double z = normal.z;
		double max = Math.max(Math.max(Math.abs(x), Math.abs(y)), Math.abs(z));
		if (max == Math.abs(x))
			return x > 0 ? Direction.EAST : Direction.WEST;
		else if (max == Math.abs(y))
			return y > 0 ? Direction.UP : Direction.DOWN;
		else if (max == Math.abs(z))
			return z > 0 ? Direction.SOUTH : Direction.NORTH;
		else
			throw new IllegalStateException("Could not find a direction from the normal, wtf???");
	}

}
