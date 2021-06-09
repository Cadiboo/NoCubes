package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.optifine.OptiFineProxy;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.render.RendererDispatcher.quad;

public final class MeshRenderer {

	static void renderBreakingTexture(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData, MeshGenerator generator, Area area) {
		FaceInfo renderInfo = new FaceInfo();
		Random random = dispatcher.random;
		MeshGenerator.translateToMeshStart(matrix, area.start, pos);
		generator.generate(area, NoCubes.smoothableHandler::isSmoothable, (relativePos, face) -> {
			renderInfo.setup(face, area.start);

			// Don't need textures or lighting because the crumbling texture overwrites them
			renderInfo.assignMissingQuads(state, random, modelData);
			LightCache light = null;
			renderFace(renderInfo, buffer, matrix, world, state, pos, null, null, light, false);

			return true;
		});
	}

	static boolean leavesBounds(BlockPos boundsStart, BlockPos boundsSize, BlockPos worldPos, Face face) {
		double x = worldPos.getX() - boundsStart.getX();
		double y = worldPos.getY() - boundsStart.getY();
		double z = worldPos.getZ() - boundsStart.getZ();

		Vec v0 = face.v0;
		Vec v1 = face.v1;
		Vec v2 = face.v2;
		Vec v3 = face.v3;

		double v0x = x + v0.x;
		double v0y = y + v0.y;
		double v0z = z + v0.z;
		double v1x = x + v1.x;
		double v1y = y + v1.y;
		double v1z = z + v1.z;
		double v2x = x + v2.x;
		double v2y = y + v2.y;
		double v2z = z + v2.z;
		double v3x = x + v3.x;
		double v3y = y + v3.y;
		double v3z = z + v3.z;

		final float epsilon = 3f;
		float minX = -epsilon;
		if (v0x < minX && v1x < minX && v2x < minX && v3x < minX)
			return true;
		float minY = -epsilon;
		if (v0y < minY && v1y < minY && v2y < minY && v3y < minY)
			return true;
		float minZ = -epsilon;
		if (v0z < minZ && v1z < minZ && v2z < minZ && v3z < minZ)
			return true;

		float maxX = boundsSize.getX() + epsilon;
		if (v0x >= maxX && v1x >= maxX && v2x >= maxX && v3x >= maxX)
			return true;
		float maxY = boundsSize.getY() + epsilon;
		if (v0y >= maxY && v1y >= maxY && v2y >= maxY && v3y >= maxY)
			return true;
		float maxZ = boundsSize.getZ() + epsilon;
		return v0z >= maxZ && v1z >= maxZ && v2z >= maxZ && v3z >= maxZ;
	}

	static void renderFaceForLayer(IBlockDisplayReader world, Random random, BlockRendererDispatcher dispatcher, FluentMatrixStack matrix, LightCache light, OptiFineProxy optiFine, FaceInfo renderInfo, BlockState state, BlockPos.Mutable worldPos, long rand, IModelData modelData, BufferBuilder buffer, Object renderEnv) {
		IBakedModel modelIn = dispatcher.getBlockModel(state);
		modelIn = optiFine.getModel(renderEnv, modelIn, state);
		renderInfo.findAndAssignQuads(modelIn, rand, state, random, modelData);
		renderFace(renderInfo, buffer, matrix.matrix, world, state, worldPos, light, optiFine, renderEnv, false);
	}

	static void renderFace(FaceInfo renderInfo, IVertexBuilder buffer, MatrixStack matrix, IBlockDisplayReader world, BlockState state, BlockPos pos, @Nullable LightCache light, @Nullable OptiFineProxy optiFine, @Nullable Object renderEnv, boolean doubleSided) {
		List<BakedQuad> quads = renderInfo.quads;
		for (int i = 0, l = quads.size(); i < l; ++i) {
			BakedQuad quad = quads.get(i);
			BakedQuad emissive = optiFine == null ? null : optiFine.getQuadEmissive(quad);
			if (emissive != null) {
				optiFine.preRenderQuad(renderEnv, emissive, state, pos);
				renderQuad(renderInfo, quad, buffer, matrix, world, state, pos, null, doubleSided);
			}
			if (optiFine != null)
				optiFine.preRenderQuad(renderEnv, quad, state, pos);
			renderQuad(renderInfo, quad, buffer, matrix, world, state, pos, light, doubleSided);
		}
	}

	private static void renderQuad(FaceInfo renderInfo, BakedQuad quad, IVertexBuilder buffer, MatrixStack matrix, IBlockDisplayReader world, BlockState state, BlockPos pos, @Nullable LightCache light, boolean doubleSided) {
		Color color = renderInfo.getColor(quad, state, world, pos);
		color.multiply(world.getShade(renderInfo.faceDirection, true));

		Texture uvs = renderInfo.texture;
		uvs.unpackFromQuad(quad);
		uvs.rearangeForDirection(renderInfo.faceDirection);

		Face face = renderInfo.face;
		int light0 = renderInfo.getLight(light, face.v0);
		int light1 = renderInfo.getLight(light, face.v1);
		int light2 = renderInfo.getLight(light, face.v2);
		int light3 = renderInfo.getLight(light, face.v3);

		quad(
			buffer, matrix, doubleSided,
			face, color, uvs, OverlayTexture.NO_OVERLAY, light0, light1, light2, light3, renderInfo.faceNormal
		);
	}

	static final /* inline? */ class FaceInfo {
		public Face face;
		public BlockPos faceRelativeToWorldPos;
		public final Face vertexNormals = new Face();
		public final Vec faceNormal = new Vec();
		public Direction faceDirection;
		public final Texture texture = new Texture();
		public final Color color = new Color();
		public final List<BakedQuad> quads = new ArrayList<>();

		public void setup(Face face, BlockPos faceRelativeToWorldPos) {
			this.face = face;
			this.faceRelativeToWorldPos = faceRelativeToWorldPos;
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
			return light == null ? LightCache.MAX_BRIGHTNESS : light.get(faceRelativeToWorldPos, vec, faceNormal);
		}

		public Color getColor(BakedQuad quad, BlockState state, IBlockDisplayReader world, BlockPos pos) {
			Color color = this.color;
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
		BlockState state = area.getAndCacheBlocks()[area.index(relativePos)];
		if (isSmoothable.test(state))
			return state;

		// Vertices can generate at positions different to the position of the block they are for
		// This occurs mostly for positions below, west of and north of the position they are for
		// Search the opposite of those directions for the actual block
		// We could also attempt to get the state from the vertex positions
		int x = relativePos.getX();
		int y = relativePos.getY();
		int z = relativePos.getZ();

		state = area.getBlockState(relativePos.move(direction.getOpposite()));
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
