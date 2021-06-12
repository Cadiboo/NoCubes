package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.RollingProfiler;
import io.github.cadiboo.nocubes.client.optifine.OptiFineCompatibility;
import io.github.cadiboo.nocubes.client.optifine.OptiFineProxy;
import io.github.cadiboo.nocubes.client.render.struct.Color;
import io.github.cadiboo.nocubes.client.render.struct.PackedLight;
import io.github.cadiboo.nocubes.client.render.struct.Texture;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender.RebuildTask;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.ClientUtil.vertex;
import static io.github.cadiboo.nocubes.client.render.MeshRenderer.*;

/**
 * @author Cadiboo
 */
public final class RendererDispatcher {

	private static final RollingProfiler fluidsProfiler = new RollingProfiler(256);
	private static final RollingProfiler meshProfiler = new RollingProfiler(256);

	public static void renderChunk(
		RebuildTask rebuildTask,
		ChunkRender chunkRender,
		CompiledChunk compiledChunk,
		RegionRenderCacheBuilder buffers,
		BlockPos chunkPos,
		IBlockDisplayReader world,
		MatrixStack matrixIn,
		Random random,
		BlockRendererDispatcher dispatcher
	) {
		long start = System.nanoTime();
		FluentMatrixStack matrix = new FluentMatrixStack(matrixIn);

		try (LightCache light = new LightCache(Minecraft.getInstance().level, chunkPos, ModUtil.CHUNK_SIZE)) {
			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
			OptiFineProxy optiFine = OptiFineCompatibility.proxy();
			// Matrix stack is translated to the start of the chunk
			optiFine.preRenderChunk(chunkRender, chunkPos, matrix.matrix);

			renderChunkFluids(chunkRender, compiledChunk, buffers, chunkPos, world, start, matrix, light, optiFine);

			if (NoCubesConfig.Client.render)
				renderChunkMesh(rebuildTask, chunkRender, compiledChunk, buffers, chunkPos, world, random, dispatcher, matrix, light, isSmoothable, optiFine);
		}
	}

	public static void renderBreakingTexture(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IBlockDisplayReader world, MatrixStack matrix, IVertexBuilder buffer, IModelData modelData) {
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		try (Area area = new Area(Minecraft.getInstance().level, pos, ModUtil.VEC_ONE, generator)) {
			MeshRenderer.renderBreakingTexture(dispatcher, state, pos, world, matrix, buffer, modelData, generator, area);
		}
	}

	private static void renderChunkFluids(ChunkRender chunkRender, CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, BlockPos chunkPos, IBlockDisplayReader world, long start, FluentMatrixStack matrix, LightCache light, OptiFineProxy optiFine) {
//		try (Area area = new Area(Minecraft.getInstance().level, chunkPos, ModUtil.CHUNK_SIZE)) {
//			FluidRenderer.render(area, light, (relativePos, fluid, block, fluidRenderer) -> {
//				renderInLayers(
//					chunkRender, compiledChunk, buffers, world, matrix, light, optiFine,
//					layer -> RenderTypeLookup.canRenderInLayer(fluid, layer),
//					(layer, buffer) -> optiFine.preRenderFluid(chunkRender, buffers, world, layer, buffer, fluid, block, worldPos),
//					(buffer, renderEnv) -> fluidRenderer.calcFaces((face, uvs) -> {
//						quad(
//							buffer, matrix, false,
//							face.v0.x, face.v0.y, face.v0.z
//						);
//					},
//					(buffer, renderEnv) -> optiFine.postRenderFluid(renderEnv, buffer, chunkRender, buffers, compiledChunk)
//				);
//			});
//		}
//		if (fluidsProfiler.recordElapsedNanos(start))
//			LogManager.getLogger("Render chunk fluids").debug("Average {}ms over the past {} chunks", fluidsProfiler.average() / 1000_000F, fluidsProfiler.size());
	}

	private static void renderChunkMesh(RebuildTask rebuildTask, ChunkRender chunkRender, CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, BlockPos chunkPos, IBlockDisplayReader world, Random random, BlockRendererDispatcher dispatcher, FluentMatrixStack matrix, LightCache light, Predicate<BlockState> isSmoothable, OptiFineProxy optiFine) {
		long start = System.nanoTime();
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		try (
			Area area = new Area(Minecraft.getInstance().level, chunkPos, ModUtil.CHUNK_SIZE, generator);
			FluentMatrixStack ignored = matrix.push()
		) {
			FaceInfo renderInfo = new FaceInfo();
			MeshGenerator.translateToMeshStart(matrix.matrix, area.start, chunkPos);
			generator.generate(area, isSmoothable, (relativePos, face) -> {
//				if (leavesBounds(chunkPos, ModUtil.CHUNK_SIZE, area.start, face))
//					return true;

				renderInfo.setup(face, area.start);
				BlockState state = MeshRenderer.TextureLocator.getTexturePosAndState(relativePos, area, isSmoothable, renderInfo.faceDirection);
				BlockPos.Mutable worldPos = relativePos.move(area.start);

				if (state.getRenderShape() == BlockRenderType.INVISIBLE)
					return true;
				long rand = optiFine.getSeed(state.getSeed(worldPos));
				IModelData modelData = rebuildTask.getModelData(worldPos);
				renderInLayers(
					chunkRender, compiledChunk, buffers, optiFine,
					layer -> RenderTypeLookup.canRenderInLayer(state, layer),
					(layer, buffer) -> optiFine.preRenderBlock(chunkRender, buffers, world, layer, buffer, state, worldPos),
					(buffer, renderEnv) -> {
						renderFaceForLayer(world, random, dispatcher, matrix, light, optiFine, renderInfo, state, worldPos, rand, modelData, buffer, renderEnv);
						return true;
					},
					(buffer, renderEnv) -> optiFine.postRenderBlock(renderEnv, buffer, chunkRender, buffers, compiledChunk)
				);
				return true;
			});
			ForgeHooksClient.setRenderLayer(null);
		}
		if (meshProfiler.recordElapsedNanos(start))
			LogManager.getLogger("Render chunk mesh").debug("Average {}ms over the past {} chunks", meshProfiler.average() / 1000_000F, meshProfiler.size());
	}

	private static void renderInLayers(
		ChunkRender chunkRender, CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, OptiFineProxy optiFine,
		Predicate<RenderType> predicate,
		BiFunction<RenderType, BufferBuilder, Object> preRender,
		BiFunction<BufferBuilder, Object, Boolean> render,
		BiConsumer<BufferBuilder, Object> postRender
	) {
		List<RenderType> chunkBufferLayers = RenderType.chunkBufferLayers();
		for (int i = 0, chunkBufferLayersSize = chunkBufferLayers.size(); i < chunkBufferLayersSize; i++) {
			RenderType layer = chunkBufferLayers.get(i);
			if (!predicate.test(layer))
				continue;

			BufferBuilder buffer = getAndStartBuffer(chunkRender, compiledChunk, buffers, layer);
			Object renderEnv = preRender.apply(layer, buffer);
			boolean used = render.apply(buffer, renderEnv);

			postRender.accept(buffer, renderEnv);
			if (used)
				markLayerUsed(compiledChunk, optiFine, layer);
		}
	}

	static BufferBuilder getAndStartBuffer(ChunkRender chunkRender, CompiledChunk compiledChunk, RegionRenderCacheBuilder buffers, RenderType layer) {
		ForgeHooksClient.setRenderLayer(layer);
		BufferBuilder buffer = buffers.builder(layer);

		if (compiledChunk.hasLayer.add(layer))
			chunkRender.beginLayer(buffer);
		return buffer;
	}

	static void markLayerUsed(CompiledChunk compiledChunk, OptiFineProxy optiFine, RenderType layer) {
		compiledChunk.isCompletelyEmpty = false;
		optiFine.markRenderLayerUsed(compiledChunk, layer);
	}

	static void quad(
		IVertexBuilder buffer, MatrixStack matrix, boolean doubleSided,
		Face face, Color color, Texture texture, int overlay, PackedLight light, Vec normal
	) {
		quad(
			buffer, matrix, doubleSided,
			face.v0, color, texture.u0, texture.v0, overlay, light.v0, normal,
			face.v1, color, texture.u1, texture.v1, overlay, light.v1, normal,
			face.v2, color, texture.u2, texture.v2, overlay, light.v2, normal,
			face.v3, color, texture.u3, texture.v3, overlay, light.v3, normal
		);
	}

	static void quad(
		IVertexBuilder buffer, MatrixStack matrix, boolean doubleSided,
		Vec vec0, Color color0, float u0, float v0, int overlay0, int light0, Vec normal0,
		Vec vec1, Color color1, float u1, float v1, int overlay1, int light1, Vec normal1,
		Vec vec2, Color color2, float u2, float v2, int overlay2, int light2, Vec normal2,
		Vec vec3, Color color3, float u3, float v3, int overlay3, int light3, Vec normal3
	) {
		quad(
			buffer, matrix, doubleSided,
			vec0.x, vec0.y, vec0.z, color0.red, color0.green, color0.blue, color0.alpha, u0, v0, overlay0, light0, normal0.x, normal0.y, normal0.z,
			vec1.x, vec1.y, vec1.z, color1.red, color1.green, color1.blue, color1.alpha, u1, v1, overlay1, light1, normal1.x, normal1.y, normal1.z,
			vec2.x, vec2.y, vec2.z, color2.red, color2.green, color2.blue, color2.alpha, u2, v2, overlay2, light2, normal2.x, normal2.y, normal2.z,
			vec3.x, vec3.y, vec3.z, color3.red, color3.green, color3.blue, color3.alpha, u3, v3, overlay3, light3, normal3.x, normal3.y, normal3.z
		);
	}

	static void quad(
		IVertexBuilder buffer, MatrixStack matrix, boolean doubleSided,
		float v0x, float v0y, float v0z, float red0, float green0, float blue0, float alpha0, float u0, float v0, int overlay0, int light0, float n0x, float n0y, float n0z,
		float v1x, float v1y, float v1z, float red1, float green1, float blue1, float alpha1, float u1, float v1, int overlay1, int light1, float n1x, float n1y, float n1z,
		float v2x, float v2y, float v2z, float red2, float green2, float blue2, float alpha2, float u2, float v2, int overlay2, int light2, float n2x, float n2y, float n2z,
		float v3x, float v3y, float v3z, float red3, float green3, float blue3, float alpha3, float u3, float v3, int overlay3, int light3, float n3x, float n3y, float n3z
	) {
		vertex(buffer, matrix, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, overlay0, light0, n0x, n0y, n0z);
		vertex(buffer, matrix, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, overlay1, light1, n1x, n1y, n1z);
		vertex(buffer, matrix, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, overlay2, light2, n2x, n2y, n2z);
		vertex(buffer, matrix, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, overlay3, light3, n3x, n3y, n3z);
		if (doubleSided) {
			vertex(buffer, matrix, v3x, v3y, v3z, red3, green3, blue3, alpha3, u3, v3, overlay3, light3, n3x, n3y, n3z);
			vertex(buffer, matrix, v2x, v2y, v2z, red2, green2, blue2, alpha2, u2, v2, overlay2, light2, n2x, n2y, n2z);
			vertex(buffer, matrix, v1x, v1y, v1z, red1, green1, blue1, alpha1, u1, v1, overlay1, light1, n1x, n1y, n1z);
			vertex(buffer, matrix, v0x, v0y, v0z, red0, green0, blue0, alpha0, u0, v0, overlay0, light0, n0x, n0y, n0z);
		}
	}

}
