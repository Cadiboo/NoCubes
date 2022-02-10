package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.RollingProfiler;
import io.github.cadiboo.nocubes.client.render.MeshRenderer.MutableObjects;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.ColorParser.Color;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

import java.util.function.Predicate;

import static io.github.cadiboo.nocubes.client.ClientUtil.line;
import static io.github.cadiboo.nocubes.client.render.MeshRenderer.FaceInfo;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class OverlayRenderer {

	private static final RollingProfiler meshProfiler = new RollingProfiler(600);

	@SubscribeEvent
	public static void onHighlightBlock(DrawSelectionEvent.HighlightBlock event) {
		if (!NoCubesConfig.Client.render)
			return;
		var world = Minecraft.getInstance().level;
		if (world == null)
			return;
		var lookingAtPos = event.getTarget().getBlockPos();
		var state = world.getBlockState(lookingAtPos);
		if (!NoCubes.smoothableHandler.isSmoothable(state))
			return;

		event.setCanceled(true);

		var camera = event.getCamera().getPosition();
		var matrix = event.getPoseStack();
		var buffer = event.getMultiBufferSource().getBuffer(RenderType.lines());
		var mesher = NoCubesConfig.Server.mesher;
		var stateSolidity = MeshRenderer.isSolidRender(state);
		try (var area = new Area(world, lookingAtPos, ModUtil.VEC_ONE, mesher)) {
			var color = NoCubesConfig.Client.selectionBoxColor;
			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
			mesher.generate(area, s -> isSmoothable.test(s) && MeshRenderer.isSolidRender(s) == stateSolidity, (pos, face) -> {
				drawFacePosColor(face, camera, area.start, color, buffer, matrix);
				return true;
			});
		}
	}

	@SubscribeEvent
	public static void onRenderLevelLastEvent(RenderLevelLastEvent event) {
		if (!NoCubesConfig.Client.debugEnabled)
			return;

		var minecraft = Minecraft.getInstance();
		var world = minecraft.level;
		if (world == null)
			return;

		var cameraInfo = minecraft.gameRenderer.getMainCamera();
		var viewer = cameraInfo.getEntity();
		if (viewer == null)
			return;

		var mesher = NoCubesConfig.Server.mesher;

		var camera = cameraInfo.getPosition();
		var matrixStack = event.getPoseStack();

		var bufferSource = minecraft.renderBuffers().bufferSource();
		var bufferBuilder = bufferSource.getBuffer(RenderType.lines());

		var targeted = viewer.pick(20.0D, 0.0F, false);
		// Where the player is looking at or their position of they're not looking at a block
		var targetedPos = targeted.getType() != HitResult.Type.BLOCK ? viewer.blockPosition() : ((BlockHitResult) targeted).getBlockPos();
		Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;

		// Destroy block progress
		if (false) {
			var start = targetedPos.offset(-2, -2, -2);
			var end = targetedPos.offset(2, 2, 2);
			var i = new int[] {0};
			BlockPos.betweenClosed(start, end).forEach(pos -> {
				minecraft.levelRenderer.destroyBlockProgress(100 + i[0]++, pos, 9);
			});
		}

		// Outline nearby smoothable blocks
		if (NoCubesConfig.Client.debugOutlineSmoothables) {
			var color = new Color(0, 1, 0, 0.4F);
			var start = viewer.blockPosition().offset(-5, -5, -5);
			var end = viewer.blockPosition().offset(5, 5, 5);
			BlockPos.betweenClosed(start, end).forEach(pos -> {
				if (isSmoothable.test(viewer.level.getBlockState(pos)))
					drawShape(matrixStack, bufferBuilder, Shapes.block(), pos, camera, color);
			});
		}

		// Draw nearby block densities and computed corner signed distance fields
		// This was just for understanding how SurfaceNets works
		// It made me understand why feeding it the 'proper' corner info results in much smoother terrain
		// at the cost of 1-block formations disappearing
		if (NoCubesConfig.Client.debugVisualiseDensitiesGrid) {
			var distanceIndicator = Shapes.box(0, 0, 0, 1 / 8F, 1 / 8F, 1 / 8F);
			var densityColor = new Color(0F, 0F, 1F, 0.5F);
			try (var area = new Area(world, targetedPos.offset(-2, -2, -2), new BlockPos(4, 4, 4), mesher)) {
				var states = area.getAndCacheBlocks();
				var densities = new float[area.numBlocks()];
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
				var pos = new BlockPos.MutableBlockPos();
				for (int z = minZ; z < maxZ; ++z) {
					for (int y = minY; y < maxY; ++y) {
						for (int x = minX; x < maxX; ++x, ++zyxIndex) {
							pos.set(x, y, z);
							var density = densities[zyxIndex];
							var densityScale = 0.5F + density / 2F; // from [-1, 1] -> [0, 1]
							if (densityScale > 0.01) {
								var box = Shapes.box(0.5 - densityScale / 2, 0.5 - densityScale / 2, 0.5 - densityScale / 2, 0.5 + densityScale / 2, 0.5 + densityScale / 2, 0.5 + densityScale / 2);
								drawShape(matrixStack, bufferBuilder, box, pos, camera, densityColor);
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
							drawShape(matrixStack, bufferBuilder, distanceIndicator, pos, camera, new Color(combinedDensityScale, 1 - combinedDensityScale, 0F, 0.4F));
						}
					}
				}
			}
		}

		final int collisionsRenderRadius = 10;

		// Draw nearby collisions in green and player intersecting collisions in red
		if (NoCubesConfig.Client.debugRenderCollisions) {
			Color intersectingColor = new Color(1, 0, 0, 0.4F);
			Color deviatingColor = new Color(0, 1, 0, 0.4F);
			VoxelShape viewerShape = Shapes.create(viewer.getBoundingBox());
			world.getBlockCollisions(viewer, viewer.getBoundingBox().inflate(collisionsRenderRadius)).forEach(voxelShape -> {
				boolean intersects = Shapes.joinIsNotEmpty(voxelShape, viewerShape, BooleanOp.AND);
				drawShape(matrixStack, bufferBuilder, voxelShape, BlockPos.ZERO, camera, intersects ? intersectingColor : deviatingColor);
			});
		}

		// Draw NoCubes' collisions in green (or yellow if debugRenderCollisions is enabled)
		if (NoCubesConfig.Client.debugRenderMeshCollisions) {
			var color = new Color(NoCubesConfig.Client.debugRenderCollisions ? 1 : 0, 1, 0, 0.4F);
			var start = viewer.blockPosition().offset(-collisionsRenderRadius, -collisionsRenderRadius, -collisionsRenderRadius);
			CollisionHandler.forEachCollisionShapeRelativeToStart(world, new BlockPos.MutableBlockPos(),
				start.getX(), start.getX() + collisionsRenderRadius * 2,
				start.getY(), start.getY() + collisionsRenderRadius * 2,
				start.getZ(), start.getZ() + collisionsRenderRadius * 2,
				shape -> {
					drawShape(matrixStack, bufferBuilder, shape, start, camera, color);
					return true;
				}
			);
		}

		// Measure the performance of meshing nearby blocks (and maybe render the result)
		if (NoCubesConfig.Client.debugRecordMeshPerformance || NoCubesConfig.Client.debugOutlineNearbyMesh) {
			var startNanos = System.nanoTime();
			drawNearbyMesh(viewer, matrixStack, camera, bufferBuilder);
			if (NoCubesConfig.Client.debugRecordMeshPerformance && meshProfiler.recordElapsedNanos(startNanos))
				LogManager.getLogger("Calc" + (NoCubesConfig.Client.debugOutlineNearbyMesh ? " & outline" : "") + " nearby mesh").debug("Average {}ms over the past {} frames", meshProfiler.average() / 1000_000F, meshProfiler.size());
		}

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.endBatch(RenderType.lines());
	}

	private static void drawNearbyMesh(Entity viewer, PoseStack matrix, Vec3 camera, VertexConsumer buffer) {
		var mesher = NoCubesConfig.Server.mesher;
		var meshSize = new BlockPos(16, 16, 16);
		var meshStart = viewer.blockPosition().offset(-meshSize.getX() / 2, -meshSize.getY() / 2 + 2, -meshSize.getZ() / 2);
		try (
			var area = new Area(viewer.level, meshStart, meshSize, mesher);
			var light = new LightCache((ClientLevel) viewer.level, meshStart, meshSize)
		) {
			var faceInfo = new FaceInfo();
			var objects = new MutableObjects();
			var mutable = new Vec();

			var faceColor = new Color(0F, 1F, 1F, 0.4F);
			var normalColor = new Color(0F, 0F, 1F, 0.2F);
			var averageNormalColor = new Color(1F, 0F, 0F, 0.4F);
			var normalDirectionColor = new Color(0F, 1F, 0F, 1F);
			var lightColor = new Color(1F, 1F, 0F, 1F);

			Predicate<BlockState> isSmoothable = NoCubes.smoothableHandler::isSmoothable;
			mesher.generate(area, isSmoothable, (pos, face) -> {
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
//				var faceRelativeToWorldPos = faceInfo.faceRelativeToWorldPos;
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

	private static void drawLinePosColorFromAdd(BlockPos offset, Vec start, Vec add, Color color, VertexConsumer buffer, PoseStack matrix, Vec3 camera) {
		var startX = (float) (offset.getX() - camera.x + start.x);
		var startY = (float) (offset.getY() - camera.y + start.y);
		var startZ = (float) (offset.getZ() - camera.z + start.z);
		line(
			buffer, matrix, color,
			startX, startY, startZ,
			startX + add.x, startY + add.y, startZ + add.z
		);
	}

	private static void drawLinePosColorFromTo(BlockPos startOffset, Vec start, BlockPos endOffset, Vec end, Color color, VertexConsumer buffer, PoseStack matrix, Vec3 camera) {
		line(
			buffer, matrix, color,
			(float) (startOffset.getX() + start.x - camera.x), (float) (startOffset.getY() + start.y - camera.y), (float) (startOffset.getZ() + start.z - camera.z),
			(float) (endOffset.getX() + end.x - camera.x), (float) (endOffset.getY() + end.y - camera.y), (float) (endOffset.getZ() + end.z - camera.z)
		);
	}

	private static void drawFacePosColor(Face face, Vec3 camera, BlockPos pos, Color color, VertexConsumer buffer, PoseStack matrix) {
		var v0 = face.v0;
		var v1 = face.v1;
		var v2 = face.v2;
		var v3 = face.v3;
		var x = pos.getX() - camera.x;
		var y = pos.getY() - camera.y;
		var z = pos.getZ() - camera.z;

		var v0x = (float) (x + v0.x);
		var v1x = (float) (x + v1.x);
		var v2x = (float) (x + v2.x);
		var v3x = (float) (x + v3.x);
		var v0y = (float) (y + v0.y);
		var v1y = (float) (y + v1.y);
		var v2y = (float) (y + v2.y);
		var v3y = (float) (y + v3.y);
		var v0z = (float) (z + v0.z);
		var v1z = (float) (z + v1.z);
		var v2z = (float) (z + v2.z);
		var v3z = (float) (z + v3.z);
		line(buffer, matrix, color, v0x, v0y, v0z, v1x, v1y, v1z);
		line(buffer, matrix, color, v1x, v1y, v1z, v2x, v2y, v2z);
		line(buffer, matrix, color, v2x, v2y, v2z, v3x, v3y, v3z);
		line(buffer, matrix, color, v3x, v3y, v3z, v0x, v0y, v0z);
	}

	private static void drawShape(PoseStack stack, VertexConsumer buffer, VoxelShape shape, BlockPos pos, Vec3 camera, Color color) {
		var x = pos.getX() - camera.x;
		var y = pos.getY() - camera.y;
		var z = pos.getZ() - camera.z;
		shape.forAllEdges((x0, y0, z0, x1, y1, z1) -> line(
			buffer, stack, color,
			(float) (x + x0), (float) (y + y0), (float) (z + z0),
			(float) (x + x1), (float) (y + y1), (float) (z + z1)
		));
	}

}
