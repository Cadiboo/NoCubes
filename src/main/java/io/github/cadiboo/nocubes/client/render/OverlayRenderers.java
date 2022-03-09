package io.github.cadiboo.nocubes.client.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.RollingProfiler;
import io.github.cadiboo.nocubes.client.render.MeshRenderer.MutableObjects;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.ColorParser.Color;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.Mesher;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.DrawHighlightEvent.HighlightBlock;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.github.cadiboo.nocubes.client.RenderHelper.*;
import static io.github.cadiboo.nocubes.client.render.MeshRenderer.FaceInfo;

/**
 * @author Cadiboo
 */
public final class OverlayRenderers {

	public static void register(IEventBus events) {
		events.addListener(OverlayRenderers::renderBlockHighlight);
		RollingProfiler meshProfiler = new RollingProfiler(600);
		ArrayList<Pair<String, DebugOverlay>> debugOverlays = Lists.<Pair<String, DebugOverlay>>newArrayList(
			Pair.of("drawOutlineAroundNearbySmoothableBlocks", OverlayRenderers::drawOutlineAroundNearbySmoothableBlocks),
			Pair.of("drawOutlineAroundNearbySmoothableBlocks", OverlayRenderers::drawOutlineAroundNearbySmoothableBlocks),
			Pair.of("maybeRenderMeshAndRecordPerformance", (camera, matrix, lines) -> maybeRenderMeshAndRecordPerformance(camera, matrix, lines, meshProfiler)),
			Pair.of("drawNearbyMeshCollisions", OverlayRenderers::drawNearbyMeshCollisions),
			Pair.of("drawNearbyCollisions", OverlayRenderers::drawNearbyCollisions),
			Pair.of("drawNearbyDensities", OverlayRenderers::drawNearbyDensities)
		);
		events.addListener((RenderWorldLastEvent event) -> renderDebugOverlays(event, debugOverlays));
	}


	public static void renderBlockHighlight(HighlightBlock event) {
		if (!NoCubesConfig.Client.render)
			return;
		ClientWorld world = Minecraft.getInstance().level;
		if (world == null)
			return;
		BlockPos lookingAtPos = event.getTarget().getBlockPos();
		BlockState state = world.getBlockState(lookingAtPos);
		if (!NoCubes.smoothableHandler.isSmoothable(state))
			return;

		event.setCanceled(true);

		Vector3d camera = event.getInfo().getPosition();
		MatrixStack matrix = event.getMatrix();
		IVertexBuilder buffer = event.getBuffers().getBuffer(RenderType.lines());
		Mesher mesher = NoCubesConfig.Server.mesher;
		boolean stateSolidity = MeshRenderer.isSolidRender(state);
		try (Area area = new Area(world, lookingAtPos, ModUtil.VEC_ONE, mesher)) {
			Color color = NoCubesConfig.Client.selectionBoxColor;
			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
			mesher.generateGeometry(area, s -> isSmoothable.test(s) && MeshRenderer.isSolidRender(s) == stateSolidity, (pos, face) -> {
				drawFacePosColor(face, camera, area.start, color, buffer, matrix);
				return true;
			});
		}
	}

	public static void renderDebugOverlays(RenderWorldLastEvent event, List<Pair<String, DebugOverlay>> overlays) {
		if (!NoCubesConfig.Client.debugEnabled)
			return;

		Minecraft minecraft = Minecraft.getInstance();
		ActiveRenderInfo camera = minecraft.gameRenderer.getMainCamera();

//		drawBlockDestructionProgressForDebug(minecraft, camera);
		IRenderTypeBuffer.Impl bufferSource = minecraft.renderBuffers().bufferSource();
		Supplier<IVertexBuilder> linesSupplier = () -> bufferSource.getBuffer(RenderType.lines());
		overlays.forEach(overlay -> {
			IProfiler profiler = minecraft.getProfiler();
			profiler.push(overlay.getLeft());
			try {
				overlay.getRight().render(camera, event.getMatrixStack(), linesSupplier);
			} finally {
				profiler.pop();
			}
		});

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.endBatch(RenderType.lines());
	}

	interface DebugOverlay {
		void render(ActiveRenderInfo camera, MatrixStack matrix, Supplier<IVertexBuilder> linesSupplier);
	}

	private static BlockPos getTargetedPosForDebugRendering(Entity viewer) {
		RayTraceResult targeted = viewer.pick(20.0D, 0.0F, false);
		// Where the player is looking at or their position of they're not looking at a block
		return targeted.getType() != RayTraceResult.Type.BLOCK ? viewer.blockPosition() : ((BlockRayTraceResult) targeted).getBlockPos();
	}

	/**
	 * Draws block destruction progress (cracking texture) near the viewer.
	 * Was used to debug issues with matrix transformations while implementing our custom destruction progress rendering.
	 */
	private static void drawBlockDestructionProgressForDebug(Minecraft minecraft, ActiveRenderInfo camera) {
		Entity viewer = camera.getEntity();
		BlockPos targetedPos = getTargetedPosForDebugRendering(viewer);
		BlockPos start = targetedPos.offset(-2, -2, -2);
		BlockPos end = targetedPos.offset(2, 2, 2);
		int[] i = new int[]{0};
		BlockPos.betweenClosed(start, end)
			.forEach(pos -> minecraft.levelRenderer.destroyBlockProgress(100 + i[0]++, pos, 9));
	}

	private static void drawOutlineAroundNearbySmoothableBlocks(ActiveRenderInfo camera, MatrixStack matrix, Supplier<IVertexBuilder> buffer) {
		if (!NoCubesConfig.Client.debugOutlineSmoothables)
			return;
		Entity viewer = camera.getEntity();
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		Color color = new Color(0, 1, 0, 0.4F);
		BlockPos start = viewer.blockPosition().offset(-5, -5, -5);
		BlockPos end = viewer.blockPosition().offset(5, 5, 5);
		BlockPos.betweenClosed(start, end).forEach(pos -> {
			if (isSmoothable.test(viewer.level.getBlockState(pos)))
				drawShape(matrix, buffer.get(), VoxelShapes.block(), pos, camera.getPosition(), color);
		});
	}

	private static void drawNearbyDensities(ActiveRenderInfo camera, MatrixStack matrix, Supplier<IVertexBuilder> buffer) {
		// Draw nearby block densities and computed corner signed distance fields
		// This was just for understanding how SurfaceNets works
		// It made me understand why feeding it the 'proper' corner info results in much smoother terrain
		// at the cost of 1-block formations disappearing
		if (!NoCubesConfig.Client.debugVisualiseDensitiesGrid)
			return;
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
		VoxelShape distanceIndicator = VoxelShapes.box(0, 0, 0, 1 / 8F, 1 / 8F, 1 / 8F);
		Color densityColor = new Color(0F, 0F, 1F, 0.5F);
		Entity viewer = camera.getEntity();
		try (Area area = new Area(viewer.level, getTargetedPosForDebugRendering(viewer).offset(-2, -2, -2), new BlockPos(4, 4, 4), NoCubesConfig.Server.mesher)) {
			BlockState[] states = area.getAndCacheBlocks();
			float[] densities = new float[area.numBlocks()];
			for (int i = 0; i < densities.length; ++i)
				densities[i] = ModUtil.getBlockDensity(isSmoothable, states[i]);

			int minZ = area.start.getZ();
			int minY = area.start.getY();
			int minX = area.start.getX();
			int width = area.size.getX();
			int height = area.size.getY();
			int maxZ = minZ + area.size.getZ();
			int maxY = minY + height;
			int maxX = minX + width;
			int zyxIndex = 0;
			BlockPos.Mutable pos = new BlockPos.Mutable();
			for (int z = minZ; z < maxZ; ++z) {
				for (int y = minY; y < maxY; ++y) {
					for (int x = minX; x < maxX; ++x, ++zyxIndex) {
						pos.set(x, y, z);
						float density = densities[zyxIndex];
						float densityScale = 0.5F + density / 2F; // from [-1, 1] -> [0, 1]
						if (densityScale > 0.01) {
							VoxelShape box = VoxelShapes.box(0.5 - densityScale / 2, 0.5 - densityScale / 2, 0.5 - densityScale / 2, 0.5 + densityScale / 2, 0.5 + densityScale / 2, 0.5 + densityScale / 2);
							drawShape(matrix, buffer.get(), box, pos, camera.getPosition(), densityColor);
						}
						if (x <= minX || y <= minY || z <= minZ)
							continue;

						float combinedDensity = 0; // AKA signed distance field
						int idx = zyxIndex;
						for (int cornerZ = 0; cornerZ < 2; ++cornerZ, idx -= width * (height - 2))
							for (int cornerY = 0; cornerY < 2; ++cornerY, idx -= width - 2)
								for (byte cornerX = 0; cornerX < 2; ++cornerX, --idx) {
									combinedDensity += densities[idx];
								}
						float combinedDensityScale = 0.5F + combinedDensity / 16F; // from [-8, 8] -> [0, 1]
						drawShape(matrix, buffer.get(), distanceIndicator, pos, camera.getPosition(), new Color(combinedDensityScale, 1 - combinedDensityScale, 0F, 0.4F));
					}
				}
			}
		}
	}

	private static void drawNearbyCollisions(ActiveRenderInfo camera, MatrixStack matrix, Supplier<IVertexBuilder> buffer) {
		// Draw nearby collisions in green and player intersecting collisions in red
		if (!NoCubesConfig.Client.debugRenderCollisions)
			return;
		float collisionsRenderRadius = 10;
		Color intersectingColor = new Color(1, 0, 0, 0.4F);
		Color deviatingColor = new Color(0, 1, 0, 0.4F);
		Entity viewer = camera.getEntity();
		VoxelShape viewerShape = VoxelShapes.create(viewer.getBoundingBox());
		viewer.level.getBlockCollisions(viewer, viewer.getBoundingBox().inflate(collisionsRenderRadius)).forEach(voxelShape -> {
			boolean intersects = VoxelShapes.joinIsNotEmpty(voxelShape, viewerShape, IBooleanFunction.AND);
			drawShape(matrix, buffer.get(), voxelShape, BlockPos.ZERO, camera.getPosition(), intersects ? intersectingColor : deviatingColor);
		});
	}

	private static void drawNearbyMeshCollisions(ActiveRenderInfo camera, MatrixStack matrix, Supplier<IVertexBuilder> buffer) {
		// Draw NoCubes' collisions in green (or yellow if debugRenderCollisions is enabled)
		if (!NoCubesConfig.Client.debugRenderMeshCollisions)
			return;
		int collisionsRenderRadius = 10;
		Color color = new Color(NoCubesConfig.Client.debugRenderCollisions ? 1 : 0, 1, 0, 0.4F);
		Entity viewer = camera.getEntity();
		BlockPos start = viewer.blockPosition().offset(-collisionsRenderRadius, -collisionsRenderRadius, -collisionsRenderRadius);
		CollisionHandler.forEachCollisionShapeRelativeToStart(viewer.level, new BlockPos.Mutable(),
			start.getX(), start.getX() + collisionsRenderRadius * 2,
			start.getY(), start.getY() + collisionsRenderRadius * 2,
			start.getZ(), start.getZ() + collisionsRenderRadius * 2,
			shape -> {
				drawShape(matrix, buffer.get(), shape, start, camera.getPosition(), color);
				return true;
			}
		);
	}

	private static void maybeRenderMeshAndRecordPerformance(ActiveRenderInfo camera, MatrixStack matrix, Supplier<IVertexBuilder> linesSupplier, RollingProfiler profiler) {
		// Measure the performance of meshing nearby blocks (and maybe render the result)
		if (!NoCubesConfig.Client.debugRecordMeshPerformance && !NoCubesConfig.Client.debugOutlineNearbyMesh)
			return;
		IVertexBuilder buffer = linesSupplier.get();
		long startNanos = System.nanoTime();
		drawNearbyMesh(camera.getEntity(), camera.getPosition(), matrix, buffer);
		if (NoCubesConfig.Client.debugRecordMeshPerformance && profiler.recordElapsedNanos(startNanos))
			LogManager.getLogger("Calc" + (NoCubesConfig.Client.debugOutlineNearbyMesh ? " & outline" : "") + " nearby mesh").debug("Average {}ms over the past {} frames", profiler.average() / 1000_000F, profiler.size());
	}

	private static void drawNearbyMesh(Entity viewer, Vector3d camera, MatrixStack matrix, IVertexBuilder buffer) {
		Mesher mesher = NoCubesConfig.Server.mesher;
		BlockPos meshSize = new BlockPos(16, 16, 16);
		BlockPos meshStart = viewer.blockPosition().offset(-meshSize.getX() / 2, -meshSize.getY() / 2 + 2, -meshSize.getZ() / 2);
		try (
			Area area = new Area(viewer.level, meshStart, meshSize, mesher);
			LightCache light = new LightCache((ClientWorld) viewer.level, meshStart, meshSize)
		) {
			FaceInfo faceInfo = new FaceInfo();
			MutableObjects objects = new MutableObjects();
			Vec mutable = new Vec();

			Color faceColor = new Color(0F, 1F, 1F, 0.4F);
			Color normalColor = new Color(0F, 0F, 1F, 0.2F);
			Color averageNormalColor = new Color(1F, 0F, 0F, 0.4F);
			Color normalDirectionColor = new Color(0F, 1F, 0F, 1F);
			Color lightColor = new Color(1F, 1F, 0F, 1F);

			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
			mesher.generateGeometry(area, isSmoothable, (pos, face) -> {
				if (!NoCubesConfig.Client.debugOutlineNearbyMesh)
					return true;
				drawFacePosColor(face, camera, area.start, faceColor, buffer, matrix);

				faceInfo.setup(face);

				// Draw face normal vec + resulting direction
				final float dirMul = 0.2F;
				drawLinePosColorFromAdd(area.start, faceInfo.centre, mutable.set(faceInfo.normal).multiply(dirMul), averageNormalColor, buffer, matrix, camera);
				drawLinePosColorFromAdd(area.start, faceInfo.centre, mutable.set(faceInfo.approximateDirection.getStepX(), faceInfo.approximateDirection.getStepY(), faceInfo.approximateDirection.getStepZ()).multiply(dirMul), normalDirectionColor, buffer, matrix, camera);

				// Draw each vertex normal
				drawLinePosColorFromAdd(area.start, face.v0, mutable.set(faceInfo.vertexNormals.v0).multiply(dirMul), normalColor, buffer, matrix, camera);
				drawLinePosColorFromAdd(area.start, face.v1, mutable.set(faceInfo.vertexNormals.v1).multiply(dirMul), normalColor, buffer, matrix, camera);
				drawLinePosColorFromAdd(area.start, face.v2, mutable.set(faceInfo.vertexNormals.v2).multiply(dirMul), normalColor, buffer, matrix, camera);
				drawLinePosColorFromAdd(area.start, face.v3, mutable.set(faceInfo.vertexNormals.v3).multiply(dirMul), normalColor, buffer, matrix, camera);

				// Draw texture pos
				mutable.set(0.5F, 0.5F, 0.5F);
				MeshRenderer.RenderableState.findAt(objects, area, faceInfo.normal, faceInfo.centre, isSmoothable);
				pos.move(area.start);
				drawLinePosColorFromTo(area.start, faceInfo.centre, pos, mutable, lightColor, buffer, matrix, camera);

//				// Draw light pos
//				mutable.set(0, 0, 0);
//				float faceRelativeToWorldPos = faceInfo.faceRelativeToWorldPos;
//				if (light.get(faceRelativeToWorldPos, face.v0, faceNormal) == 0)
//					drawLinePosColorFromTo(area.start, face.v0, light.lightWorldPos(area.start, face.v0, faceNormal), mutable, lightColor, buffer, matrix, camera);
//				if (light.get(faceRelativeToWorldPos, face.v1, faceNormal) == 0)
//					drawLinePosColorFromTo(area.start, face.v1, light.lightWorldPos(area.start, face.v1, faceNormal), mutable, lightColor, buffer, matrix, camera);
//				if (light.get(faceRelativeToWorldPos, face.v2, faceNormal) == 0)
//					drawLinePosColorFromTo(area.start, face.v2, light.lightWorldPos(area.start, face.v2, faceNormal), mutable, lightColor, buffer, matrix, camera);
//				if (light.get(faceRelativeToWorldPos, face.v3, faceNormal) == 0)
//					drawLinePosColorFromTo(area.start, face.v3, light.lightWorldPos(area.start, face.v3, faceNormal), mutable, lightColor, buffer, matrix, camera);

				return true;
			});
		}
	}

}
