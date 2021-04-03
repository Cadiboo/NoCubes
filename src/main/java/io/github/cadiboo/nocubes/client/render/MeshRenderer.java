package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ReusableCache;
import io.github.cadiboo.nocubes.util.Vec;
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
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;

/**
 * @author Cadiboo
 */
public class MeshRenderer {

	private static final ReusableCache<float[]> CHUNKS = new ReusableCache.Local<>();
	private static final ReusableCache<float[]> CRACKING = new ReusableCache.Global<>();

	public static void renderChunk(final ChunkRenderDispatcher.ChunkRender.RebuildTask rebuildTask, ChunkRenderDispatcher.ChunkRender chunkRender, final ChunkRenderDispatcher.CompiledChunk compiledChunkIn, final RegionRenderCacheBuilder builderIn, final BlockPos blockpos, final IBlockDisplayReader chunkrendercache, final MatrixStack matrixstack, final Random random, final BlockRendererDispatcher blockrendererdispatcher) {
		if (!NoCubesConfig.Client.render)
			return;

		final Face normal = new Face(new Vec(), new Vec(), new Vec(), new Vec());
		final Vec averageOfNormal = new Vec();
		final TextureInfo uvs = new TextureInfo();
		SurfaceNets.generate(
			blockpos.getX(), blockpos.getY(), blockpos.getZ(),
			16, 16, 16, chunkrendercache, NoCubes.smoothableHandler::isSmoothable, CHUNKS,
			(pos, mask) -> true,
			(pos, face) -> {
				SmoothableHandler handler = NoCubes.smoothableHandler;

				face.assignNormalTo(normal);
				normal.multiply(-1);
				normal.assignAverageTo(averageOfNormal);
				Direction direction = averageOfNormal.getDirectionFromNormal();

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
				BlockColors blockColors = Minecraft.getInstance().getBlockColors();
				int formatSize = DefaultVertexFormats.BLOCK.getIntegerSize();

				IModelData modelData = rebuildTask.getModelData(pos);
				for (RenderType rendertype : RenderType.getBlockRenderTypes()) {
					if (blockstate.getRenderType() == BlockRenderType.INVISIBLE || !RenderTypeLookup.canRenderInLayer(blockstate, rendertype))
						continue;
					ForgeHooksClient.setRenderLayer(rendertype);
					BufferBuilder bufferbuilder = builderIn.getBuilder(rendertype);
					if (compiledChunkIn.layersStarted.add(rendertype)) {
						chunkRender.beginLayer(bufferbuilder);
					}
					matrixstack.push();
					matrixstack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);

					IBakedModel modelIn = blockrendererdispatcher.getModelForState(blockstate);
					int light = WorldRenderer.getPackedLightmapCoords(chunkrendercache, blockstate, pos.offset(direction));
					random.setSeed(rand);
					List<BakedQuad> dirQuads;
					if (blockstate.hasProperty(BlockStateProperties.SNOWY))
						// Make grass/snow/mycilium side faces be rendered with their top texture
						// Equivalent to OptiFine's Better Grass feature
						if (!blockstate.get(BlockStateProperties.SNOWY))
							dirQuads = modelIn.getQuads(blockstate, Direction.UP, random, modelData);
						else {
							// The texture of grass underneath the snow (that normally never gets seen) is grey, we don't want that
							BlockState snow = Blocks.SNOW.getDefaultState();
							dirQuads = blockrendererdispatcher.getModelForState(snow).getQuads(snow, null, random, modelData);
						}
					else
						dirQuads = modelIn.getQuads(blockstate, direction, random, modelData);
					random.setSeed(rand);
					List<BakedQuad> nullQuads = modelIn.getQuads(blockstate, null, random, modelData);
					if (dirQuads.isEmpty() && nullQuads.isEmpty()) // dirQuads is empty for the Barrier block
						dirQuads = blockrendererdispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(blockstate, direction, random, modelData);
					renderQuads(chunkrendercache, uvs, pos, face, normal, direction, blockstate, blockColors, formatSize, bufferbuilder, light, dirQuads, nullQuads);

					if (true) {
						compiledChunkIn.empty = false;
						compiledChunkIn.layersUsed.add(rendertype);
					}
					matrixstack.pop();
				}
				ForgeHooksClient.setRenderLayer(null);
				return true;
			}
		);
	}

	public static void renderBlockDamage(BlockRendererDispatcher blockRendererDispatcher, BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, IModelData modelData) {
		if (!NoCubesConfig.Client.render)
			return;

		final Face normal = new Face(new Vec(), new Vec(), new Vec(), new Vec());
		final Vec averageOfNormal = new Vec();
		final TextureInfo uvs = new TextureInfo();

		long rand = blockStateIn.getPositionRandom(posIn);
		Random random = blockRendererDispatcher.random;
		IBakedModel model = blockRendererDispatcher.getBlockModelShapes().getModel(blockStateIn);
		BlockColors blockColors = Minecraft.getInstance().getBlockColors();

		// TODO: This seems suspicious, keep synced with {@link net.minecraft.client.renderer.BlockModelRenderer.renderModel(net.minecraft.world.IBlockDisplayReader, net.minecraft.client.renderer.model.IBakedModel, net.minecraft.block.BlockState, net.minecraft.util.math.BlockPos, com.mojang.blaze3d.matrix.MatrixStack, com.mojang.blaze3d.vertex.IVertexBuilder, boolean, java.util.Random, long, int, net.minecraftforge.client.model.data.IModelData)}
		modelData = model.getModelData(lightReaderIn, posIn, blockStateIn, modelData);
		final IModelData modelDataFinal = modelData;

		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
		SurfaceNets.generate(
			posIn.getX(), posIn.getY(), posIn.getZ(),
			1, 1, 1, lightReaderIn, NoCubes.smoothableHandler::isSmoothable, CRACKING,
			(pos, mask) -> true,
			(pos, face) -> {
				face.transform(matrix4f);

				face.assignNormalTo(normal);
				normal.multiply(-1);
				normal.assignAverageTo(averageOfNormal);
				Direction direction = averageOfNormal.getDirectionFromNormal();

				int light = WorldRenderer.getPackedLightmapCoords(lightReaderIn, blockStateIn, pos.offset(direction));
				random.setSeed(rand);
				List<BakedQuad> dirQuads = model.getQuads(blockStateIn, direction, random, modelDataFinal);
				random.setSeed(rand);
				List<BakedQuad> nullQuads = model.getQuads(blockStateIn, null, random, modelDataFinal);
				if (dirQuads.isEmpty() && nullQuads.isEmpty()) // dirQuads is empty for the Barrier block
					dirQuads = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(blockStateIn, direction, random, modelDataFinal);

				int formatSize = DefaultVertexFormats.BLOCK.getIntegerSize();
				renderQuads(lightReaderIn, uvs, pos, face, normal, direction, blockStateIn, blockColors, formatSize, vertexBuilderIn, light, dirQuads, nullQuads);
				return true;
			}
		);
	}

	private static void renderQuads(IBlockDisplayReader chunkrendercache, TextureInfo uvs, BlockPos.Mutable pos, Face face, Face reversedNormal, Direction direction, BlockState blockstate, BlockColors blockColors, int formatSize, IVertexBuilder bufferbuilder, int light, List<BakedQuad> dirQuads, List<BakedQuad> nullQuads) {
		final Vec v0 = face.v0;
		final Vec v1 = face.v1;
		final Vec v2 = face.v2;
		final Vec v3 = face.v3;

		final Vec n0 = reversedNormal.v0;
		final Vec n1 = reversedNormal.v1;
		final Vec n2 = reversedNormal.v2;
		final Vec n3 = reversedNormal.v3;

		int dirQuadsSize = dirQuads.size();
		for (int i1 = 0; i1 < dirQuadsSize + nullQuads.size(); i1++) {
			final BakedQuad quad = i1 < dirQuadsSize ? dirQuads.get(i1) : nullQuads.get(i1 - dirQuadsSize);

			uvs.unpackFromQuad(quad, formatSize);
			uvs.switchForDirection(direction);

			final float shading = chunkrendercache.func_230487_a_(direction, false);
			float red;
			float blue;
			float green;
			if (quad.hasTintIndex()) {
				int packedColor = blockColors.getColor(blockstate, chunkrendercache, pos, quad.getTintIndex());
				red = (float)(packedColor >> 16 & 255) / 255.0F;
				green = (float)(packedColor >> 8 & 255) / 255.0F;
				blue = (float)(packedColor & 255) / 255.0F;
			} else {
				red = 1.0F;
				green = 1.0F;
				blue = 1.0F;
			}
			red *= shading;
			green *= shading;
			blue *= shading;
			final float alpha = 1.0F;
			bufferbuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).tex(uvs.u0, uvs.v0).lightmap(light).normal((float) n0.x, (float) n0.y, (float) n0.z).endVertex();
			bufferbuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).tex(uvs.u1, uvs.v1).lightmap(light).normal((float) n1.x, (float) n1.y, (float) n1.z).endVertex();
			bufferbuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).tex(uvs.u2, uvs.v2).lightmap(light).normal((float) n2.x, (float) n2.y, (float) n2.z).endVertex();
			bufferbuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).tex(uvs.u3, uvs.v3).lightmap(light).normal((float) n3.x, (float) n3.y, (float) n3.z).endVertex();
		}
	}

	static final class TextureInfo {
		public float u0;
		public float v0;
		public float u1;
		public float v1;
		public float u2;
		public float v2;
		public float u3;
		public float v3;

		public void unpackFromQuad(BakedQuad quad, int formatSize) {
			final int[] vertexData = quad.getVertexData();
			// Quads are packed xyz|argb|u|v|ts
			u0 = Float.intBitsToFloat(vertexData[4]);
			v0 = Float.intBitsToFloat(vertexData[5]);
			u1 = Float.intBitsToFloat(vertexData[formatSize + 4]);
			v1 = Float.intBitsToFloat(vertexData[formatSize + 5]);
			u2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 4]);
			v2 = Float.intBitsToFloat(vertexData[formatSize * 2 + 5]);
			u3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 4]);
			v3 = Float.intBitsToFloat(vertexData[formatSize * 3 + 5]);
		}

		public void switchForDirection(Direction direction) {
			switch (direction) {
				case NORTH:
				case EAST:
					break;
				case DOWN:
				case SOUTH:
				case WEST: {
					float u0 = this.u0;
					float v0 = this.v0;
					float u1 = this.u1;
					float v1 = this.v1;
					float u2 = this.u2;
					float v2 = this.v2;
					float u3 = this.u3;
					float v3 = this.v3;

					this.u0 = u3;
					this.v0 = v3;
					this.u1 = u0;
					this.v1 = v0;
					this.u2 = u1;
					this.v2 = v1;
					this.u3 = u2;
					this.v3 = v2;
					break;
				}
				case UP: {
					float u0 = this.u0;
					float v0 = this.v0;
					float u1 = this.u1;
					float v1 = this.v1;
					float u2 = this.u2;
					float v2 = this.v2;
					float u3 = this.u3;
					float v3 = this.v3;

					this.u0 = u2;
					this.v0 = v2;
					this.u1 = u3;
					this.v1 = v3;
					this.u2 = u0;
					this.v2 = v0;
					this.u3 = u1;
					this.v3 = v1;
					break;
				}
				default:
					throw new IllegalStateException("Unexpected value: " + direction);
			}
		}

	}

}
