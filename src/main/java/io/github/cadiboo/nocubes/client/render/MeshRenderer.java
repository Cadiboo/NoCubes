package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.RollingProfiler;
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
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender.RebuildTask;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * @author Cadiboo
 */
public final class MeshRenderer {

	private static final RollingProfiler profiler = new RollingProfiler(256);

	public static void renderChunk(
		RebuildTask rebuildTask,
		ChunkRender chunkRender,
		CompiledChunk compiledChunk,
		RegionRenderCacheBuilder buffers,
		BlockPos chunkPos,
		IBlockDisplayReader world,
		MatrixStack matrix,
		Random random,
		BlockRendererDispatcher dispatcher
	) {
		if (!NoCubesConfig.Client.render)
			return;

		long start = System.nanoTime();
		OptiFineProxy optiFine = OptiFineCompatibility.proxy();
		optiFine.preRenderChunk(chunkPos);
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		FaceInfo renderInfo = new FaceInfo();

		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		try (
			Area area = new Area(Minecraft.getInstance().level, chunkPos, ModUtil.CHUNK_SIZE, generator);
			LightCache light = new LightCache(Minecraft.getInstance().level, chunkPos, ModUtil.CHUNK_SIZE)
		) {
			// See the javadoc on Face#addMeshOffset for an explanation of this
			BlockPos areaMeshOffset = area.start.subtract(chunkPos);
			generator.generate(area, isSmoothable, (relativePos, face) -> {
				face.addMeshOffset(areaMeshOffset);
//				if (face.v0.x < 0 || face.v1.x < 0 || face.v2.x < 0 || face.v3.x < 0)// || face.v0.x > 16 || face.v1.x > 16 || face.v2.x > 16 || face.v3.x > 16)
//					return true;
//				if (face.v0.y < 0 || face.v1.y < 0 || face.v2.y < 0 || face.v3.y < 0)// || face.v0.y > 16 || face.v1.y > 16 || face.v2.y > 16 || face.v3.y > 16)
//					return true;
//				if (face.v0.z < 0 || face.v1.z < 0 || face.v2.z < 0 || face.v3.z < 0)// || face.v0.z > 16 || face.v1.z > 16 || face.v2.z > 16 || face.v3.z > 16)
//					return true;

				renderInfo.setup(face, chunkPos);
				BlockState state = getTexturePosAndState(relativePos, area, isSmoothable, renderInfo.faceDirection);
				BlockPos.Mutable worldPos = relativePos.move(area.start);

				if (state.getRenderShape() == BlockRenderType.INVISIBLE)
					return true;

				long rand = optiFine.getSeed(state.getSeed(worldPos));
				IModelData modelData = rebuildTask.getModelData(worldPos);

				for (RenderType rendertype : RenderType.chunkBufferLayers()) {
					if (!RenderTypeLookup.canRenderInLayer(state, rendertype))
						continue;
					ForgeHooksClient.setRenderLayer(rendertype);
					BufferBuilder buffer = buffers.builder(rendertype);

					if (compiledChunk.hasLayer.add(rendertype))
						chunkRender.beginLayer(buffer);

					matrix.pushPose();
					matrix.translate(worldPos.getX() & 15, worldPos.getY() & 15, worldPos.getZ() & 15);
					Object renderEnv = optiFine.preRenderBlock(chunkRender, buffers, world, rendertype, buffer, state, worldPos);

					IBakedModel modelIn = dispatcher.getBlockModel(state);
					modelIn = optiFine.getModel(renderEnv, modelIn, state);

					renderInfo.findAndAssignQuads(modelIn, rand, state, random, modelData);
					renderFace(renderInfo, buffer, world, state, worldPos, light, optiFine, renderEnv, false);

					optiFine.postRenderBlock(renderEnv, buffer, chunkRender, buffers, compiledChunk);
					if (true) {
						compiledChunk.isCompletelyEmpty = false;
						optiFine.markRenderLayerUsed(compiledChunk, rendertype);
					}
					matrix.popPose();
				}
				return true;
			});
			ForgeHooksClient.setRenderLayer(null);
		}
		if (profiler.recordElapsedNanos(start))
			LogManager.getLogger("Render chunk mesh").debug("Average {}ms over the past {} chunks", profiler.average() / 1000_000F, profiler.size());
	}

	public static void renderSmoothBlockDamage(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData) {
		FaceInfo renderInfo = new FaceInfo();
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		Random random = dispatcher.random;
		try (
			Area area = new Area(Minecraft.getInstance().level, pos, ModUtil.VEC_ONE, generator);
		) {
			// See the javadoc on Face#addMeshOffset for an explanation of this
			BlockPos areaMeshOffset = area.start.subtract(pos);
			Matrix3f normal = matrix.last().normal();
			Matrix4f pose = matrix.last().pose();
			generator.generate(area, NoCubes.smoothableHandler::isSmoothable, (ignored, face) -> {
				face.addMeshOffset(areaMeshOffset);
				renderInfo.setup(face, pos);

				renderInfo.vertexNormals.transform(normal);
				renderInfo.faceNormal.transform(normal);
				face.transform(pose);

				// Don't need textures or lighting because the crumbling texture overwrites them
				renderInfo.assignMissingQuads(state, random, modelData);
				LightCache light = null;
				renderFace(renderInfo, buffer, world, state, pos, null, null, light, false);

				return true;
			});
		}
	}

	private static void renderFace(FaceInfo renderInfo, IVertexBuilder buffer, IBlockDisplayReader world, BlockState state, BlockPos pos, @Nullable LightCache light, @Nullable OptiFineProxy optiFine, @Nullable Object renderEnv, boolean doubleSided) {
		List<BakedQuad> quads = renderInfo.quads;
		for (int i = 0, l = quads.size(); i < l; ++i) {
			BakedQuad quad = quads.get(i);
			BakedQuad emissive = optiFine == null ? null : optiFine.getQuadEmissive(quad);
			if (emissive != null) {
				optiFine.preRenderQuad(renderEnv, emissive, state, pos);
				renderQuad(renderInfo, quad, buffer, world, state, pos, null, doubleSided);
			}
			if (optiFine != null)
				optiFine.preRenderQuad(renderEnv, quad, state, pos);
			renderQuad(renderInfo, quad, buffer, world, state, pos, light, doubleSided);
		}
	}

	private static void renderQuad(FaceInfo renderInfo, BakedQuad quad, IVertexBuilder buffer, IBlockDisplayReader world, BlockState state, BlockPos pos, @Nullable LightCache light, boolean doubleSided) {
		// Pos
		Face face = renderInfo.face;
		Vec v0 = face.v0;
		Vec v1 = face.v1;
		Vec v2 = face.v2;
		Vec v3 = face.v3;

		// Color
		ColorInfo color = renderInfo.getColor(quad, state, world, pos);
		color.multiply(world.getShade(renderInfo.faceDirection, true));
		float red = color.red;
		float blue = color.blue;
		float green = color.green;
		float alpha = color.alpha;

		// UV (tex)
		TextureInfo uvs = renderInfo.texture;
		uvs.unpackFromQuad(quad);
		uvs.rearangeForDirection(renderInfo.faceDirection);

		// UV2 (light)
		int l0 = renderInfo.getLight(light, v0);
		int l1 = renderInfo.getLight(light, v1);
		int l2 = renderInfo.getLight(light, v2);
		int l3 = renderInfo.getLight(light, v3);

		// Normal
		Vec n0 = renderInfo.faceNormal;
		Vec n1 = renderInfo.faceNormal;
		Vec n2 = renderInfo.faceNormal;
		Vec n3 = renderInfo.faceNormal;

		// Draw
		buffer.vertex(v0.x, v0.y, v0.z, red, green, blue, alpha, uvs.u0, uvs.v0, OverlayTexture.NO_OVERLAY, l0, n0.x, n0.y, n0.z);
		buffer.vertex(v1.x, v1.y, v1.z, red, green, blue, alpha, uvs.u1, uvs.v1, OverlayTexture.NO_OVERLAY, l1, n1.x, n1.y, n1.z);
		buffer.vertex(v2.x, v2.y, v2.z, red, green, blue, alpha, uvs.u2, uvs.v2, OverlayTexture.NO_OVERLAY, l2, n2.x, n2.y, n2.z);
		buffer.vertex(v3.x, v3.y, v3.z, red, green, blue, alpha, uvs.u3, uvs.v3, OverlayTexture.NO_OVERLAY, l3, n3.x, n3.y, n3.z);
		if (doubleSided) {
			buffer.vertex(v3.x, v3.y, v3.z, red, green, blue, alpha, uvs.u3, uvs.v3, OverlayTexture.NO_OVERLAY, l3, n3.x, n3.y, n3.z);
			buffer.vertex(v2.x, v2.y, v2.z, red, green, blue, alpha, uvs.u2, uvs.v2, OverlayTexture.NO_OVERLAY, l2, n2.x, n2.y, n2.z);
			buffer.vertex(v1.x, v1.y, v1.z, red, green, blue, alpha, uvs.u1, uvs.v1, OverlayTexture.NO_OVERLAY, l1, n1.x, n1.y, n1.z);
			buffer.vertex(v0.x, v0.y, v0.z, red, green, blue, alpha, uvs.u0, uvs.v0, OverlayTexture.NO_OVERLAY, l0, n0.x, n0.y, n0.z);
		}
	}

	static final /* inline? */ class FaceInfo {
		public Face face;
		/** faceOffsetPos + face = worldPosOfFace */
		public BlockPos faceOffsetPos;
		public final Face vertexNormals = new Face();
		public final Vec faceNormal = new Vec();
		public Direction faceDirection;
		public final TextureInfo texture = new TextureInfo();
		public final ColorInfo color = new ColorInfo();
		public final List<BakedQuad> quads = new ArrayList<>();

		public void setup(Face face, BlockPos faceOffsetPos) {
			this.face = face;
			this.faceOffsetPos = faceOffsetPos;
			face.assignNormalTo(vertexNormals);
			vertexNormals.multiply(-1).assignAverageTo(faceNormal);
			faceDirection = faceNormal.getDirectionFromNormal();
		}

		public void findAndAssignQuads(IBakedModel model, long rand, BlockState state, Random random, IModelData modelData) {
			quads.clear();

			random.setSeed(rand);
			List<BakedQuad> nullQuads = model.getQuads(state, null, random, modelData);

			random.setSeed(rand);
			List<BakedQuad> dirQuads;
			if (!state.hasProperty(BlockStateProperties.SNOWY))
				dirQuads = model.getQuads(state, faceDirection, random, modelData);
			else {
				// Make grass/snow/mycilium side faces be rendered with their top texture
				// Equivalent to OptiFine's Better Grass feature
				if (!state.getValue(BlockStateProperties.SNOWY))
					dirQuads = model.getQuads(state, faceDirection, random, modelData);
				else {
					// The texture of grass underneath the snow (that normally never gets seen) is grey, we don't want that
					BlockState snow = Blocks.SNOW.defaultBlockState();
					dirQuads = Minecraft.getInstance().getBlockRenderer().getBlockModel(snow).getQuads(snow, null, random, modelData);
				}
			}

			if (dirQuads.isEmpty() && nullQuads.isEmpty()) {
				// dirQuads is empty for the Barrier block
				assignMissingQuads(state, random, modelData);
			} else {
				quads.addAll(dirQuads);
				quads.addAll(nullQuads);
			}
		}

		public void assignMissingQuads(BlockState state, Random random, IModelData modelData) {
			quads.addAll(Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager().getMissingModel().getQuads(state, faceDirection, random, modelData));
		}

		public int getLight(LightCache light, Vec vec) {
			return light == null ? LightCache.MAX_BRIGHTNESS : light.get(faceOffsetPos, vec, faceNormal);
		}

		public ColorInfo getColor(BakedQuad quad, BlockState state, IBlockDisplayReader world, BlockPos pos) {
			ColorInfo color = this.color;
			if (!quad.isTinted()) {
				color.red = 1.0F;
				color.green = 1.0F;
				color.blue = 1.0F;
//				color.alpha = 1.0F;
			} else {
				// TODO: Colors cache
				int packedColor = Minecraft.getInstance().getBlockColors().getColor(state, world, pos, quad.getTintIndex());
				color.red = (float) (packedColor >> 16 & 255) / 255.0F;
				color.green = (float) (packedColor >> 8 & 255) / 255.0F;
				color.blue = (float) (packedColor & 255) / 255.0F;
//				color.alpha = 1.0F;
			}
			return color;
		}
	}


	static final /* inline */ class TextureInfo {
		public float u0;
		public float v0;
		public float u1;
		public float v1;
		public float u2;
		public float v2;
		public float u3;
		public float v3;

		public void unpackFromQuad(BakedQuad quad) {
			int formatSize = getFormatSize(quad);
			int[] vertexData = quad.getVertices();
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

		private static int getFormatSize(BakedQuad quad) {
			return DefaultVertexFormats.BLOCK.getIntegerSize();
		}

		public void rearangeForDirection(Direction direction) {
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

	static final /* inline */ class ColorInfo {
		public float red;
		public float green;
		public float blue;
		public final float alpha = 1.0F;

		public void multiply(float shading) {
			red *= shading;
			green *= shading;
			blue *= shading;
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
	public static BlockState getTexturePosAndState(BlockPos.Mutable relativePos, Area area, Predicate<BlockState> isSmoothable, Direction direction) { //, boolean tryForBetterTexturesSnow, boolean tryForBetterTexturesGrass) {
		BlockState[] states = area.getAndCacheBlocks();
		BlockState state = states[area.index(relativePos)];
		if (isSmoothable.test(state))
			return state;

		// Vertices can generate at positions different to the position of the block they are for
		// This occurs mostly for positions below, west of and north of the position they are for
		// Search the opposite of those directions for the actual block
		// We could also attempt to get the state from the vertex positions
		int x = relativePos.getX();
		int y = relativePos.getY();
		int z = relativePos.getZ();

		state = states[area.index(relativePos.move(direction.getOpposite()))];
		if (isSmoothable.test(state))
			return state;

//		for (int[] offset : OFFSETS_ORDERED) {
//			relativePos.set(
//				x + offset[0],
//				y + offset[1],
//				z + offset[2]
//			);
//			state = states[area.index(relativePos)];
//			if (isSmoothable.test(state))
//				return state;
//		}

		// Give up
		relativePos.set(x, y, z);
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
