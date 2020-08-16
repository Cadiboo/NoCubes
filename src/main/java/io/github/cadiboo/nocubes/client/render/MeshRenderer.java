package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.util.ReusableCache;
import io.github.cadiboo.nocubes.client.render.util.Vec;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.smoothable.SmoothableHandler;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

/**
 * @author Cadiboo
 */
public class MeshRenderer {

	private static final ReusableCache<boolean[][][]> CHUNKS = new ReusableCache.Local<>();
	private static final ReusableCache<boolean[][][]> CRACKING = new ReusableCache.Global<>();

	public static void renderChunk(final ChunkRenderDispatcher.ChunkRender.RebuildTask rebuildTask, ChunkRenderDispatcher.ChunkRender chunkRender, final ChunkRenderDispatcher.CompiledChunk compiledChunkIn, final RegionRenderCacheBuilder builderIn, final BlockPos blockpos, final ChunkRenderCache chunkrendercache, final MatrixStack matrixstack, final Random random, final BlockRendererDispatcher blockrendererdispatcher) {
		if (NoCubesConfig.Client.render) {
			SurfaceNets.generate(
				blockpos.getX(), blockpos.getY(), blockpos.getZ(),
				16, 16, 16, chunkrendercache, NoCubes.smoothableHandler::isSmoothable, CHUNKS,
				(pos, face, normal, averageNormal, direction) -> {
					final Vec v0 = face.v0;
					final Vec v1 = face.v1;
					final Vec v2 = face.v2;
					final Vec v3 = face.v3;
					final Vec n0 = normal.v0;
					final Vec n1 = normal.v1;
					final Vec n2 = normal.v2;
					final Vec n3 = normal.v3;
					final SmoothableHandler handler = NoCubes.smoothableHandler;

//					for (RenderType rendertype : RenderType.getBlockRenderTypes()) {
//						if (pos.getX() < blockpos.getX() - 2 || pos.getX() > blockpos.getX() + 17)
//							continue;
//						if (pos.getY() < blockpos.getY() - 2 || pos.getY() > blockpos.getY() + 17)
//							continue;
//						if (pos.getY() < blockpos.getZ() - 2 || pos.getY() > blockpos.getZ() + 17)
//							continue;
//						net.minecraftforge.client.model.data.IModelData modelData = rebuildTask.getModelData(pos);
//						BlockState bs = Blocks.SCAFFOLDING.getDefaultState();
//						if (bs.getRenderType() != BlockRenderType.INVISIBLE && RenderTypeLookup.canRenderInLayer(bs, rendertype)) {
//							RenderType rendertype1 = rendertype;
//							BufferBuilder bufferbuilder2 = builderIn.getBuilder(rendertype1);
//							if (compiledChunkIn.layersStarted.add(rendertype1)) {
//								chunkRender.beginLayer(bufferbuilder2);
//							}
//
//							matrixstack.push();
//							matrixstack.translate((double) (pos.getX() & 15), (double) (pos.getY() & 15), (double) (pos.getZ() & 15));
//							if (blockrendererdispatcher.renderModel(bs, pos, chunkrendercache, matrixstack, bufferbuilder2, true, random, modelData)) {
//								compiledChunkIn.empty = false;
//								compiledChunkIn.layersUsed.add(rendertype1);
//							}
//
//							matrixstack.pop();
//						}
//					}
//					net.minecraftforge.client.ForgeHooksClient.setRenderLayer(null);

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
						if (!handler.isSmoothable(blockstate)) {
							// Give up
							blockstate = Blocks.SCAFFOLDING.getDefaultState();
							pos.setPos(x, y, z);
						}
					}

					long rand = blockstate.getPositionRandom(pos);

					final BlockColors blockColors = Minecraft.getInstance().getBlockColors();

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
//							final float uTransform = -0.0625F * 1;
//							final float vTransform = -0.0625F * 2;
							final float uTransform = 0;
							final float vTransform = 0;
							final float texu0 = Float.intBitsToFloat(vertexData[4]) + uTransform;
							final float texv0 = Float.intBitsToFloat(vertexData[5]) + vTransform;
							final float texu1 = Float.intBitsToFloat(vertexData[formatSize + 4]) + uTransform;
							final float texv1 = Float.intBitsToFloat(vertexData[formatSize + 5]) + vTransform;
							final float texu2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]) + uTransform;
							final float texv2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]) + vTransform;
							final float texu3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]) + uTransform;
							final float texv3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]) + vTransform;
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

							float red;
							float green;
							float blue;
							float alpha = 1.0F;
							if (quad.hasTintIndex()) {
								int i = blockColors.getColor(blockstate, chunkrendercache, pos, quad.getTintIndex());
								red = (float) (i >> 16 & 255) / 255.0F;
								green = (float) (i >> 8 & 255) / 255.0F;
								blue = (float) (i & 255) / 255.0F;
							} else {
								red = 1.0F;
								green = 1.0F;
								blue = 1.0F;
							}

							final float shading = chunkrendercache.func_230487_a_(direction, false);
							red *= shading;
							green *= shading;
							blue *= shading;
							bufferbuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(v0u, v0v).lightmap(light).normal((float) n0.x, (float) n0.y, (float) n0.z).endVertex();
							bufferbuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(v1u, v1v).lightmap(light).normal((float) n1.x, (float) n1.y, (float) n1.z).endVertex();
							bufferbuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(v2u, v2v).lightmap(light).normal((float) n2.x, (float) n2.y, (float) n2.z).endVertex();
							bufferbuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(v3u, v3v).lightmap(light).normal((float) n3.x, (float) n3.y, (float) n3.z).endVertex();
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

	public static void renderBlockDamage(BlockRendererDispatcher blockRendererDispatcher, BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, IModelData modelData) {
		if (NoCubesConfig.Client.render) {
			long rand = blockStateIn.getPositionRandom(posIn);
			Random random = blockRendererDispatcher.random;
			IBakedModel model = blockRendererDispatcher.getBlockModelShapes().getModel(blockStateIn);
			BlockColors blockColors = Minecraft.getInstance().getBlockColors();
//			blockRendererDispatcher.getBlockModelRenderer().renderModel(lightReaderIn, model, blockStateIn, posIn, matrixStackIn, vertexBuilderIn, true, random, rand, OverlayTexture.NO_OVERLAY, modelData);
//			{
//				boolean flag = Minecraft.isAmbientOcclusionEnabled() && blockStateIn.getLightValue(lightReaderIn, posIn) == 0 && model.isAmbientOcclusion();
//				Vector3d vector3d = blockStateIn.getOffset(lightReaderIn, posIn);
//				matrixStackIn.translate(vector3d.x, vector3d.y, vector3d.z);
//				modelData = model.getModelData(lightReaderIn, posIn, blockStateIn, modelData);
//
//				boolean flag1 = false;
//				BitSet bitset = new BitSet(3);
//
//				for (Direction direction1 : Direction.values()) {
//					random.setSeed(rand);
//					List<BakedQuad> list = model.getQuads(blockStateIn, direction1, random, modelData);
//					if (!list.isEmpty() && Block.shouldSideBeRendered(blockStateIn, lightReaderIn, posIn, direction1)) {
//						int i = WorldRenderer.getPackedLightmapCoords(lightReaderIn, blockStateIn, posIn.offset(direction1));
//						for (BakedQuad bakedquad : list) {
//
//							float f = lightReaderIn.func_230487_a_(bakedquad.getFace(), bakedquad.func_239287_f_());
//							float f11;
//							float f1;
//							float f2;
//							if (bakedquad.hasTintIndex()) {
//								int i1 = blockColors.getColor(blockStateIn, lightReaderIn, posIn, bakedquad.getTintIndex());
//								f11 = (float) (i1 >> 16 & 255) / 255.0F;
//								f1 = (float) (i1 >> 8 & 255) / 255.0F;
//								f2 = (float) (i1 & 255) / 255.0F;
//							} else {
//								f11 = 1.0F;
//								f1 = 1.0F;
//								f2 = 1.0F;
//							}
//
//							MatrixStack.Entry matrixEntryIn = matrixStackIn.getLast();
//							float[] colorMuls = new float[]{f, f, f, f};
//							int[] aint = bakedquad.getVertexData();
//							Vector3i vector3i = bakedquad.getFace().getDirectionVec();
//							Vector3f vector3f = new Vector3f((float) vector3i.getX(), (float) vector3i.getY(), (float) vector3i.getZ());
//							Matrix4f matrix4f = matrixEntryIn.getMatrix();
//							vector3f.transform(matrixEntryIn.getNormal());
//							int i1 = 8;
//							int j = aint.length / 8;
//
//							try (MemoryStack memorystack = MemoryStack.stackPush()) {
//								ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormats.BLOCK.getSize());
//								IntBuffer intbuffer = bytebuffer.asIntBuffer();
//
//								for (int k = 0; k < j; ++k) {
//									((Buffer) intbuffer).clear();
//									intbuffer.put(aint, k * 8, 8);
//									float f31 = bytebuffer.getFloat(0);
//									float f12 = bytebuffer.getFloat(4);
//									float f21 = bytebuffer.getFloat(8);
//									float f3;
//									float f4;
//									float f5;
//									float f6 = (float) (bytebuffer.get(12) & 255) / 255.0F;
//									float f7 = (float) (bytebuffer.get(13) & 255) / 255.0F;
//									float f8 = (float) (bytebuffer.get(14) & 255) / 255.0F;
//									f3 = f6 * colorMuls[k] * f11;
//									f4 = f7 * colorMuls[k] * f1;
//									f5 = f8 * colorMuls[k] * f2;
//
//									int l = vertexBuilderIn.applyBakedLighting(new int[]{i, i, i, i}[k], bytebuffer);
//									float f9 = bytebuffer.getFloat(16);
//									float f10 = bytebuffer.getFloat(20);
//									Vector4f vector4f = new Vector4f(f31, f12, f21, 1.0F);
//									vector4f.transform(matrix4f);
////									vertexBuilderIn.applyBakedNormals(vector3f, bytebuffer, matrixEntryIn.getNormal());
////									vertexBuilderIn.addVertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), f3, f4, f5, 1.0F, f9, f10, OverlayTexture.NO_OVERLAY, l, vector3f.getX(), vector3f.getY(), vector3f.getZ());
//									vertexBuilderIn.addVertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), f3, f4, f5, 1.0F, f9, f10, OverlayTexture.NO_OVERLAY, l, vector3f.getX(), vector3f.getY(), vector3f.getZ());
//								}
//							}
//
//						}
//						flag1 = true;
//					}
//				}
//			}

//			// TODO: This seems suspicious, keep synced with {@link net.minecraft.client.renderer.BlockModelRenderer.renderModel(net.minecraft.world.IBlockDisplayReader, net.minecraft.client.renderer.model.IBakedModel, net.minecraft.block.BlockState, net.minecraft.util.math.BlockPos, com.mojang.blaze3d.matrix.MatrixStack, com.mojang.blaze3d.vertex.IVertexBuilder, boolean, java.util.Random, long, int, net.minecraftforge.client.model.data.IModelData)}
			modelData = model.getModelData(lightReaderIn, posIn, blockStateIn, modelData);
			final IModelData modelDataFinal = modelData;

			Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();

			SurfaceNets.generate(
				posIn.getX(), posIn.getY(), posIn.getZ(),
				1, 1, 1, lightReaderIn, NoCubes.smoothableHandler::isSmoothable, CRACKING,
				(pos, face, normal, averageNormal, direction) -> {
					final Vec v0 = face.v0;
					final Vec v1 = face.v1;
					final Vec v2 = face.v2;
					final Vec v3 = face.v3;
					final Vec n0 = normal.v0;
					final Vec n1 = normal.v1;
					final Vec n2 = normal.v2;
					final Vec n3 = normal.v3;

					v0.transform(matrix4f);
					v1.transform(matrix4f);
					v2.transform(matrix4f);
					v3.transform(matrix4f);

					int light = WorldRenderer.getPackedLightmapCoords(lightReaderIn, blockStateIn, pos.offset(direction));
					random.setSeed(rand);
					List<BakedQuad> dirQuads = model.getQuads(blockStateIn, direction, random, modelDataFinal);
					random.setSeed(rand);
					List<BakedQuad> nullQuads = model.getQuads(blockStateIn, null, random, modelDataFinal);
					if (dirQuads.isEmpty() && nullQuads.isEmpty()) // dirQuads is empty for the Barrier block
						dirQuads = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(blockStateIn, direction, random, modelDataFinal);
					int dirQuadsSize = dirQuads.size();

//					Vector3i vector3i = quadIn.getFace().getDirectionVec();
//					Vector3f vector3f = new Vector3f((float)vector3i.getX(), (float)vector3i.getY(), (float)vector3i.getZ());
//					Matrix4f matrix4f = matrixEntryIn.getMatrix();
//					vector3f.transform(matrixEntryIn.getNormal());

					for (int i1 = 0; i1 < dirQuadsSize + nullQuads.size(); i1++) {
						final BakedQuad quad = i1 < dirQuadsSize ? dirQuads.get(i1) : nullQuads.get(i1 - dirQuadsSize);

//						Vector4f vector4f = new Vector4f(f, f1, f2, 1.0F);
//						vector4f.transform(matrix4f);

						final int formatSize = DefaultVertexFormats.BLOCK.getIntegerSize();
						final int[] vertexData = quad.getVertexData();
						// Quads are packed xyz|argb|u|v|ts
						final float uTransform = 0.001F;
						final float vTransform = 0.001F;
						final float texu0 = Float.intBitsToFloat(vertexData[4]) + uTransform;
						final float texv0 = Float.intBitsToFloat(vertexData[5]) + vTransform;
						final float texu1 = Float.intBitsToFloat(vertexData[formatSize + 4]) + uTransform;
						final float texv1 = Float.intBitsToFloat(vertexData[formatSize + 5]) + vTransform;
						final float texu2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]) + uTransform;
						final float texv2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]) + vTransform;
						final float texu3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]) + uTransform;
						final float texv3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]) + vTransform;
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

						final float shading = lightReaderIn.func_230487_a_(direction, false);
						final float red = 1.0F * shading;
						final float green = 1.0F * shading;
						final float blue = 1.0F * shading;
						final float alpha = 1.0F;
						vertexBuilderIn.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(v0u, v0v).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal((float) n0.x, (float) n0.y, (float) n0.z).endVertex();
						vertexBuilderIn.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(v1u, v1v).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal((float) n1.x, (float) n1.y, (float) n1.z).endVertex();
						vertexBuilderIn.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(v2u, v2v).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal((float) n2.x, (float) n2.y, (float) n2.z).endVertex();
						vertexBuilderIn.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(v3u, v3v).overlay(OverlayTexture.NO_OVERLAY).lightmap(light).normal((float) n3.x, (float) n3.y, (float) n3.z).endVertex();
					}
					return true;
				}
			);
		}
	}
}
