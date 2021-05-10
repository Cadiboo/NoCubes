package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineProxy;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public final class MeshRenderer {

	public static void renderChunk(final ChunkRenderDispatcher.ChunkRender.RebuildTask rebuildTask, ChunkRenderDispatcher.ChunkRender chunkRender, final ChunkRenderDispatcher.CompiledChunk compiledChunkIn, final RegionRenderCacheBuilder builderIn, final BlockPos blockpos, final IBlockDisplayReader chunkrendercache, final MatrixStack matrixstack, final Random random, final BlockRendererDispatcher blockrendererdispatcher) {
		if (!NoCubesConfig.Client.render)
			return;

		final Face vertexNormals = new Face();
		final Vec faceNormal = new Vec();
		final TextureInfo uvs = new TextureInfo();
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		OptiFineProxy optiFine = OptiFineCompatibility.proxy();

		try (
			Area area = new Area(Minecraft.getInstance().level, blockpos, ModUtil.CHUNK_SIZE, generator);
			LightCache light = new LightCache(Minecraft.getInstance().level, blockpos, ModUtil.CHUNK_SIZE)
		) {
			optiFine.preRenderChunk(blockpos);
			BlockPos diff = area.start.subtract(blockpos);
			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
			generator.generate(area, isSmoothable, ((pos, face) -> {
				if (face.v0.x < 0 || face.v1.x < 0 || face.v2.x < 0 || face.v3.x < 0)// || face.v0.x > 16 || face.v1.x > 16 || face.v2.x > 16 || face.v3.x > 16)
					return true;
				if (face.v0.y < 0 || face.v1.y < 0 || face.v2.y < 0 || face.v3.y < 0)// || face.v0.y > 16 || face.v1.y > 16 || face.v2.y > 16 || face.v3.y > 16)
					return true;
				if (face.v0.z < 0 || face.v1.z < 0 || face.v2.z < 0 || face.v3.z < 0)// || face.v0.z > 16 || face.v1.z > 16 || face.v2.z > 16 || face.v3.z > 16)
					return true;

				face.assignNormalTo(vertexNormals);
				vertexNormals.multiply(-1).assignAverageTo(faceNormal);
				Direction direction = faceNormal.getDirectionFromNormal();

				BlockState blockstate = getTexturePosAndState(pos.move(area.start), area, isSmoothable, direction);

				long rand = optiFine.getSeed(blockstate.getSeed(pos));
				BlockColors blockColors = Minecraft.getInstance().getBlockColors();
				int formatSize = DefaultVertexFormats.BLOCK.getIntegerSize();

				if (blockstate.getRenderShape() == BlockRenderType.INVISIBLE)
					return true;

				IModelData modelData = rebuildTask.getModelData(pos);

				for (RenderType rendertype : RenderType.chunkBufferLayers()) {
					if (!RenderTypeLookup.canRenderInLayer(blockstate, rendertype))
						continue;
					ForgeHooksClient.setRenderLayer(rendertype);
					BufferBuilder bufferbuilder = builderIn.builder(rendertype);

					if (compiledChunkIn.hasLayer.add(rendertype))
						chunkRender.beginLayer(bufferbuilder);

					matrixstack.pushPose();
					matrixstack.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
					optiFine.preRenderBlock(chunkRender, builderIn, chunkrendercache, rendertype, bufferbuilder, blockstate, pos);

					{
						IBakedModel modelIn = blockrendererdispatcher.getBlockModel(blockstate);
						modelIn = optiFine.getModel(modelIn, blockstate);
						random.setSeed(rand);
						List<BakedQuad> dirQuads;
						if (blockstate.hasProperty(BlockStateProperties.SNOWY))
							// Make grass/snow/mycilium side faces be rendered with their top texture
							// Equivalent to OptiFine's Better Grass feature
							if (!blockstate.getValue(BlockStateProperties.SNOWY))
								dirQuads = modelIn.getQuads(blockstate, direction, random, modelData);
							else {
								// The texture of grass underneath the snow (that normally never gets seen) is grey, we don't want that
								BlockState snow = Blocks.SNOW.defaultBlockState();
								dirQuads = blockrendererdispatcher.getBlockModel(snow).getQuads(snow, null, random, modelData);
							}
						else
							dirQuads = modelIn.getQuads(blockstate, direction, random, modelData);
						random.setSeed(rand);
						List<BakedQuad> nullQuads = modelIn.getQuads(blockstate, null, random, modelData);
						if (dirQuads.isEmpty() && nullQuads.isEmpty()) // dirQuads is empty for the Barrier block
							dirQuads = blockrendererdispatcher.getBlockModelShaper().getModelManager().getMissingModel().getQuads(blockstate, direction, random, modelData);
						renderQuads(chunkrendercache, uvs, area.start, diff, pos, face, vertexNormals, faceNormal, direction, blockstate, blockColors, formatSize, bufferbuilder, light, dirQuads, nullQuads, optiFine);
					}

					optiFine.postRenderBlock(bufferbuilder, chunkRender, builderIn, compiledChunkIn);
					if (true) {
						compiledChunkIn.isCompletelyEmpty = false;
						compiledChunkIn.hasBlocks.add(rendertype);
					}
					matrixstack.popPose();
				}
				return true;
			}));
			ForgeHooksClient.setRenderLayer(null);
		}
	}

	public static void renderBlockDamage(BlockRendererDispatcher blockRendererDispatcher, BlockState blockStateIn, BlockPos posIn, IBlockDisplayReader lightReaderIn, MatrixStack matrixStackIn, IVertexBuilder vertexBuilderIn, IModelData modelData) {
		if (!NoCubesConfig.Client.render)
			return;

//		final Face normal = new Face(new Vec(), new Vec(), new Vec(), new Vec());
//		final Vec averageOfNormal = new Vec();
//		final TextureInfo uvs = new TextureInfo();
//
//		long rand = blockStateIn.getPositionRandom(posIn);
//		Random random = blockRendererDispatcher.random;
//		IBakedModel model = blockRendererDispatcher.getBlockModelShapes().getModel(blockStateIn);
//		BlockColors blockColors = Minecraft.getInstance().getBlockColors();
//
//		// TODO: This seems suspicious, keep synced with {@link net.minecraft.client.renderer.BlockModelRenderer.renderModel(net.minecraft.world.IBlockDisplayReader, net.minecraft.client.renderer.model.IBakedModel, net.minecraft.block.BlockState, net.minecraft.util.math.BlockPos, com.mojang.blaze3d.matrix.MatrixStack, com.mojang.blaze3d.vertex.IVertexBuilder, boolean, java.util.Random, long, int, net.minecraftforge.client.model.data.IModelData)}
//		modelData = model.getModelData(lightReaderIn, posIn, blockStateIn, modelData);
//		final IModelData modelDataFinal = modelData;
//
//		Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
//		SurfaceNets.generate(
//			posIn.getX(), posIn.getY(), posIn.getZ(),
//			1, 1, 1, lightReaderIn, NoCubes.smoothableHandler::isSmoothable, CRACKING,
//			(pos, mask) -> true,
//			(pos, face) -> {
//				face.transform(matrix4f);
//
//				face.assignNormalTo(normal);
//				normal.multiply(-1);
//				normal.assignAverageTo(averageOfNormal);
//				Direction direction = averageOfNormal.getDirectionFromNormal();
//
//				int light = WorldRenderer.getPackedLightmapCoords(lightReaderIn, blockStateIn, pos.offset(direction));
//				random.setSeed(rand);
//				List<BakedQuad> dirQuads = model.getQuads(blockStateIn, direction, random, modelDataFinal);
//				random.setSeed(rand);
//				List<BakedQuad> nullQuads = model.getQuads(blockStateIn, null, random, modelDataFinal);
//				if (dirQuads.isEmpty() && nullQuads.isEmpty()) // dirQuads is empty for the Barrier block
//					dirQuads = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(blockStateIn, direction, random, modelDataFinal);
//
//				int formatSize = DefaultVertexFormats.BLOCK.getIntegerSize();
//				renderQuads(lightReaderIn, uvs, pos, face, normal, direction, blockStateIn, blockColors, formatSize, vertexBuilderIn, light, dirQuads, nullQuads);
//				return true;
//			}
//		);
	}

	private static void renderQuads(IBlockDisplayReader chunkrendercache, TextureInfo uvs, BlockPos areaStart, BlockPos renderOffset, BlockPos pos, Face face, Face vertexNormals, Vec normal, Direction direction, BlockState blockstate, BlockColors blockColors, int formatSize, IVertexBuilder bufferbuilder, LightCache light, List<BakedQuad> dirQuads, List<BakedQuad> nullQuads, OptiFineProxy optiFine) {
		final Vec v0 = face.v0;
		final Vec v1 = face.v1;
		final Vec v2 = face.v2;
		final Vec v3 = face.v3;

		final Vec n0 = vertexNormals.v0;
		final Vec n1 = vertexNormals.v1;
		final Vec n2 = vertexNormals.v2;
		final Vec n3 = vertexNormals.v3;

		final float shading = chunkrendercache.getShade(direction, true);

		int dirQuadsSize = dirQuads.size();
		for (int i1 = 0; i1 < dirQuadsSize + nullQuads.size(); i1++) {
			BakedQuad quad = i1 < dirQuadsSize ? dirQuads.get(i1) : nullQuads.get(i1 - dirQuadsSize);
			BakedQuad emissiveQuad = optiFine.getQuadEmissive(quad);
			if (emissiveQuad != null) {
				optiFine.preRenderQuad(emissiveQuad, blockstate, pos);
				renderQuad(chunkrendercache, uvs, areaStart, renderOffset, pos, direction, blockstate, blockColors, formatSize, bufferbuilder, LightCache.FULL_LIGHT, v0, v1, v2, v3, n0, n1, n2, n3, normal, shading, emissiveQuad);
			}
			optiFine.preRenderQuad(quad, blockstate, pos);
			renderQuad(chunkrendercache, uvs, areaStart, renderOffset, pos, direction, blockstate, blockColors, formatSize, bufferbuilder, light, v0, v1, v2, v3, n0, n1, n2, n3, normal, shading, quad);
		}
	}

	private static void renderQuad(IBlockDisplayReader chunkrendercache, TextureInfo uvs, BlockPos areaStart, BlockPos renderOffset, BlockPos pos, Direction direction, BlockState blockstate, BlockColors blockColors, int formatSize, IVertexBuilder bufferbuilder, LightCache light, Vec v0, Vec v1, Vec v2, Vec v3, Vec n0, Vec n1, Vec n2, Vec n3, Vec faceNormal, float shading, BakedQuad quad) {
		int l0 = light.get(areaStart, v0, n0);
		int l1 = light.get(areaStart, v1, n1);
		int l2 = light.get(areaStart, v2, n2);
		int l3 = light.get(areaStart, v3, n3);

//		if ((l0 & 0xFF) == 0 && (l1 & 0xFF) == 0 && (l2 & 0xFF) == 0 && (l3 & 0xFF) == 0)
//			return;

		uvs.unpackFromQuad(quad, formatSize);
		uvs.switchForDirection(direction);

		float red;
		float blue;
		float green;
		if (quad.isTinted()) {
			int packedColor = blockColors.getColor(blockstate, chunkrendercache, pos, quad.getTintIndex());
			red = (float) (packedColor >> 16 & 255) / 255.0F;
			green = (float) (packedColor >> 8 & 255) / 255.0F;
			blue = (float) (packedColor & 255) / 255.0F;
		} else {
			red = 1.0F;
			green = 1.0F;
			blue = 1.0F;
		}
		red *= shading;
		green *= shading;
		blue *= shading;
		final float alpha = 1.0F;
		float x = renderOffset.getX();
		float y = renderOffset.getY();
		float z = renderOffset.getZ();
		bufferbuilder.vertex(x + v0.x, y + v0.y, z + v0.z).color(red, green, blue, alpha).uv(uvs.u0, uvs.v0).uv2(l0).normal(n0.x, n0.y, n0.z).endVertex();
		bufferbuilder.vertex(x + v1.x, y + v1.y, z + v1.z).color(red, green, blue, alpha).uv(uvs.u1, uvs.v1).uv2(l1).normal(n1.x, n1.y, n1.z).endVertex();
		bufferbuilder.vertex(x + v2.x, y + v2.y, z + v2.z).color(red, green, blue, alpha).uv(uvs.u2, uvs.v2).uv2(l2).normal(n2.x, n2.y, n2.z).endVertex();
		bufferbuilder.vertex(x + v3.x, y + v3.y, z + v3.z).color(red, green, blue, alpha).uv(uvs.u3, uvs.v3).uv2(l3).normal(n3.x, n3.y, n3.z).endVertex();

//		bufferbuilder.vertex(x + v3.x, y + v3.y, z + v3.z).color(red, green, blue, alpha).uv(uvs.u3, uvs.v3).uv2(l3).normal(n3.x, n3.y, n3.z).endVertex();
//		bufferbuilder.vertex(x + v2.x, y + v2.y, z + v2.z).color(red, green, blue, alpha).uv(uvs.u2, uvs.v2).uv2(l2).normal(n2.x, n2.y, n2.z).endVertex();
//		bufferbuilder.vertex(x + v1.x, y + v1.y, z + v1.z).color(red, green, blue, alpha).uv(uvs.u1, uvs.v1).uv2(l1).normal(n1.x, n1.y, n1.z).endVertex();
//		bufferbuilder.vertex(x + v0.x, y + v0.y, z + v0.z).color(red, green, blue, alpha).uv(uvs.u0, uvs.v0).uv2(l0).normal(n0.x, n0.y, n0.z).endVertex();
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
			final int[] vertexData = quad.getVertices();
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

//	private static final int[][] OFFSETS_ORDERED = {
//		// check 6 immediate neighbours
//		{+0, -1, +0},
//		{+0, +1, +0},
//		{-1, +0, +0},
//		{+1, +0, +0},
//		{+0, +0, -1},
//		{+0, +0, +1},
//		// check 12 non-immediate, non-corner neighbours
//		{-1, -1, +0},
//		{-1, +0, -1},
//		{-1, +0, +1},
//		{-1, +1, +0},
//		{+0, -1, -1},
//		{+0, -1, +1},
//		// {+0, +0, +0}, // Don't check self
//		{+0, +1, -1},
//		{+0, +1, +1},
//		{+1, -1, +0},
//		{+1, +0, -1},
//		{+1, +0, +1},
//		{+1, +1, +0},
//		// check 8 corner neighbours
//		{+1, +1, +1},
//		{+1, +1, -1},
//		{-1, +1, +1},
//		{-1, +1, -1},
//		{+1, -1, +1},
//		{+1, -1, -1},
//		{-1, -1, +1},
//		{-1, -1, -1},
//	};

	/**
	 * Returns a state and sets the texturePooledMutablePos to the pos it found
	 *
	 * @return a state
	 */
	public static BlockState getTexturePosAndState(BlockPos.Mutable worldPos, Area area, Predicate<BlockState> isSmoothable, Direction direction) { //, boolean tryForBetterTexturesSnow, boolean tryForBetterTexturesGrass) {
		BlockState state = area.world.getBlockState(worldPos);
		if (isSmoothable.test(state))
			return state;

		// Vertices can generate at positions different to the position of the block they are for
		// This occurs mostly for positions below, west of and north of the position they are for
		// Search the opposite of those directions for the actual block
		// We could also attempt to get the state from the vertex positions
		int x = worldPos.getX();
		int y = worldPos.getY();
		int z = worldPos.getZ();

		state = area.world.getBlockState(worldPos.move(direction.getOpposite()));
		if (isSmoothable.test(state))
			return state;

//		for (int[] offset : OFFSETS_ORDERED) {
//			worldPos.set(
//				x + offset[0],
//				y + offset[1],
//				z + offset[2]
//			);
//			state = area.world.getBlockState(worldPos);
//			if (isSmoothable.test(state))
//				return state;
//		}

		// Give up
		worldPos.set(x, y, z);
		return Blocks.SCAFFOLDING.defaultBlockState();

//		if (NoCubesConfig.Client.betterTextures) {
//			if (tryForBetterTexturesSnow) {
//					IBlockState betterTextureState = blockCacheArray[stateCache.getIndex(
//						relativePosX + stateCacheStartPaddingX,
//						relativePosY + stateCacheStartPaddingY,
//						relativePosZ + stateCacheStartPaddingZ,
//						stateCacheSizeX, stateCacheSizeY
//					)];
//
//					if (isStateSnow(betterTextureState)) {
//						texturePooledMutablePos.setPos(posX, posY, posZ);
//						return betterTextureState;
//					}
//					for (int[] offset : OFFSETS_ORDERED) {
//						betterTextureState = blockCacheArray[stateCache.getIndex(
//							relativePosX + offset[0] + stateCacheStartPaddingX,
//							relativePosY + offset[1] + stateCacheStartPaddingY,
//							relativePosZ + offset[2] + stateCacheStartPaddingZ,
//							stateCacheSizeX, stateCacheSizeY
//						)];
//						if (isStateSnow(betterTextureState)) {
//							texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
//							return betterTextureState;
//						}
//					}
//			}
//			if (tryForBetterTexturesGrass) {
//					IBlockState betterTextureState = blockCacheArray[stateCache.getIndex(
//						relativePosX + stateCacheStartPaddingX,
//						relativePosY + stateCacheStartPaddingY,
//						relativePosZ + stateCacheStartPaddingZ,
//						stateCacheSizeX, stateCacheSizeY
//					)];
//
//					if (isStateGrass(betterTextureState)) {
//						texturePooledMutablePos.setPos(posX, posY, posZ);
//						return betterTextureState;
//					}
//					for (int[] offset : OFFSETS_ORDERED) {
//						betterTextureState = blockCacheArray[stateCache.getIndex(
//							relativePosX + offset[0] + stateCacheStartPaddingX,
//							relativePosY + offset[1] + stateCacheStartPaddingY,
//							relativePosZ + offset[2] + stateCacheStartPaddingZ,
//							stateCacheSizeX, stateCacheSizeY
//						)];
//						if (isStateGrass(betterTextureState)) {
//							texturePooledMutablePos.setPos(posX + offset[0], posY + offset[1], posZ + offset[2]);
//							return betterTextureState;
//						}
//					}
//				}
//			}
//		}
	}

}
