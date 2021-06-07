package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.RollingProfiler;
import io.github.cadiboo.nocubes.collision.CollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.Area;
import io.github.cadiboo.nocubes.util.Face;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

import static io.github.cadiboo.nocubes.client.ClientUtil.vertex;
import static io.github.cadiboo.nocubes.client.render.MeshRenderer.FaceInfo;
import static io.github.cadiboo.nocubes.config.ColorParser.Color;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class OverlayRenderer {

	private static final RollingProfiler meshProfiler = new RollingProfiler(600);

	@SubscribeEvent
	public static void onHighlightBlock(DrawHighlightEvent.HighlightBlock event) {
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
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		try (Area area = new Area(world, lookingAtPos, ModUtil.VEC_ONE, generator)) {
			generator.generate(area, NoCubes.smoothableHandler::isSmoothable, (pos, face) -> {
				drawFacePosColor(face, camera, area.start, NoCubesConfig.Client.selectionBoxColor, buffer, matrix);
				return true;
			});
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(RenderWorldLastEvent event) {
		if (!NoCubesConfig.Client.debugEnabled)
			return;

		Minecraft minecraft = Minecraft.getInstance();
		World world = minecraft.level;
		if (world == null)
			return;

		Entity viewer = minecraft.gameRenderer.getMainCamera().getEntity();
		if (viewer == null)
			return;

		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;

		Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();
		MatrixStack matrixStack = event.getMatrixStack();

		IRenderTypeBuffer.Impl bufferSource = minecraft.renderBuffers().bufferSource();
		IVertexBuilder bufferBuilder = bufferSource.getBuffer(RenderType.lines());

		RayTraceResult targeted = viewer.pick(20.0D, 0.0F, false);
		// Where the player is looking at or their position of they're not looking at a block
		BlockPos targetedPos = targeted.getType() != RayTraceResult.Type.BLOCK ? viewer.blockPosition() : ((BlockRayTraceResult) targeted).getBlockPos();

		// Destroy block progress
		if (false) {
			BlockPos start = targetedPos.offset(-2, -2, -2);
			BlockPos end = targetedPos.offset(2, 2, 2);
			int[] i = {0};
			BlockPos.betweenClosed(start, end).forEach(pos -> {
				minecraft.levelRenderer.destroyBlockProgress(100 + i[0]++, pos, 9);
			});
		}

		// Outline nearby smoothable blocks
		if (NoCubesConfig.Client.debugOutlineSmoothables) {
			Color color = new Color(0, 1, 0, 0.4F);
			BlockPos start = viewer.blockPosition().offset(-5, -5, -5);
			BlockPos end = viewer.blockPosition().offset(5, 5, 5);
			BlockPos.betweenClosed(start, end).forEach(pos -> {
				if (NoCubes.smoothableHandler.isSmoothable(viewer.level.getBlockState(pos)))
					drawShape(matrixStack, bufferBuilder, VoxelShapes.block(), pos, camera, color);
			});
		}

		// Draw nearby block densities and computed corner signed distance fields
		// This was just for understanding how SurfaceNets works
		// It made me understand why feeding it the 'proper' corner info results in much smoother terrain
		// at the cost of 1-block formations disappearing
		if (NoCubesConfig.Client.debugVisualiseDensitiesGrid) {
			VoxelShape distanceIndicator = VoxelShapes.box(0, 0, 0, 1 / 8F, 1 / 8F, 1 / 8F);
			Color densityColor = new Color(0F, 0F, 1F, 0.5F);
			try (Area area = new Area(world, targetedPos.offset(-2, -2, -2), new BlockPos(4, 4, 4), generator)) {
				BlockState[] states = area.getAndCacheBlocks();
				float[] densities = new float[area.numBlocks()];
				for (int i = 0; i < densities.length; ++i) {
					BlockState state = states[i];
					boolean smoothable = NoCubes.smoothableHandler.isSmoothable(state);
					densities[i] = ModUtil.getBlockDensity(smoothable, state);
				}

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

		// Draw nearby collisions in green and player intersecting collisions in red
		if (NoCubesConfig.Client.debugRenderCollisions) {
			Color intersectingColor = new Color(1, 0, 0, 0.4F);
			Color deviatingColor = new Color(0, 1, 0, 0.4F);
			VoxelShape viewerShape = VoxelShapes.create(viewer.getBoundingBox());
			world.getBlockCollisions(viewer, viewer.getBoundingBox().inflate(1)).forEach(voxelShape -> {
				boolean intersects = VoxelShapes.joinIsNotEmpty(voxelShape, viewerShape, IBooleanFunction.AND);
				drawShape(matrixStack, bufferBuilder, voxelShape, BlockPos.ZERO, camera, intersects ? intersectingColor : deviatingColor);
			});
		}

		// Draw NoCubes' collisions in green (or yellow if debugRenderCollisions is enabled)
		if (NoCubesConfig.Client.debugRenderMeshCollisions) {
			Color color = new Color(NoCubesConfig.Client.debugRenderCollisions ? 1 : 0, 1, 0, 0.4F);
			BlockPos size = new BlockPos(10, 10, 10);
			BlockPos start = viewer.blockPosition().offset(-size.getX() / 2, -size.getY() / 2, -size.getZ() / 2);
			try (Area area = new Area(world, start, size, generator)) {
				CollisionHandler.generate(area, generator, (x0, y0, z0, x1, y1, z1) -> {
					VoxelShape voxelShape = VoxelShapes.box(x0, y0, z0, x1, y1, z1);
					drawShape(matrixStack, bufferBuilder, voxelShape, area.start, camera, color);
				});
			}
		}

		// Measure the performance of meshing nearby blocks (and maybe render the result)
		if (NoCubesConfig.Client.debugRecordMeshPerformance || NoCubesConfig.Client.debugOutlineNearbyMesh) {
			long startNanos = System.nanoTime();
			drawNearbyMesh(viewer, matrixStack, camera, bufferBuilder);
			if (NoCubesConfig.Client.debugRecordMeshPerformance) {
				if (meshProfiler.recordElapsedNanos(startNanos))
					LogManager.getLogger("Calc" + (NoCubesConfig.Client.debugOutlineNearbyMesh ? " & outline" : "") + " nearby mesh").debug("Average {}ms over the past {} frames", meshProfiler.average() / 1000_000F, meshProfiler.size());
			}
		}

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.endBatch(RenderType.lines());
	}

	private static void drawNearbyMesh(Entity viewer, MatrixStack matrix, Vector3d camera, IVertexBuilder buffer) {
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		BlockPos meshSize = new BlockPos(16, 16, 16);
		BlockPos meshStart = viewer.blockPosition().offset(-meshSize.getX() / 2, -meshSize.getY() / 2 + 2, -meshSize.getZ() / 2);
		try (
			Area area = new Area(viewer.level, meshStart, meshSize, generator);
			LightCache light = new LightCache((ClientWorld) viewer.level, meshStart, meshSize)
		) {
			FaceInfo faceInfo = new FaceInfo();
			Vec centre = new Vec();
			Vec mutable = new Vec();

			Color faceColor = new Color(0F, 1F, 1F, 1F);
			Color normalColor = new Color(0F, 0F, 1F, 0.5F);
			Color averageNormalColor = new Color(1F, 0F, 0F, 1F);
			Color normalDirectionColor = new Color(0F, 1F, 0F, 1F);
			Color lightColor = new Color(1F, 1F, 0F, 1F);

			generator.generate(area, NoCubes.smoothableHandler::isSmoothable, (pos, face) -> {
				if (!NoCubesConfig.Client.debugOutlineNearbyMesh)
					return true;
				drawFacePosColor(face, camera, area.start, faceColor, buffer, matrix);

				faceInfo.setup(face, area.start);
				Face vertexNormals = faceInfo.vertexNormals;
				Vec faceNormal = faceInfo.faceNormal;
				Direction faceDirection = faceInfo.faceDirection;
				face.assignAverageTo(centre);

				// Draw face normal vec + resulting direction
				final float dirMul = 0.2F;
				drawLinePosColorFromAdd(area.start, centre, mutable.set(faceNormal).multiply(dirMul), averageNormalColor, buffer, matrix, camera);
				drawLinePosColorFromAdd(area.start, centre, mutable.set(faceDirection.getStepX(), faceDirection.getStepY(), faceDirection.getStepZ()).multiply(dirMul), normalDirectionColor, buffer, matrix, camera);

				// Draw each vertex normal
				drawLinePosColorFromAdd(area.start, face.v0, mutable.set(vertexNormals.v0).multiply(dirMul), normalColor, buffer, matrix, camera);
				drawLinePosColorFromAdd(area.start, face.v1, mutable.set(vertexNormals.v1).multiply(dirMul), normalColor, buffer, matrix, camera);
				drawLinePosColorFromAdd(area.start, face.v2, mutable.set(vertexNormals.v2).multiply(dirMul), normalColor, buffer, matrix, camera);
				drawLinePosColorFromAdd(area.start, face.v3, mutable.set(vertexNormals.v3).multiply(dirMul), normalColor, buffer, matrix, camera);

				// Draw light pos
				mutable.set(0, 0, 0);
				if (faceInfo.getLight(light, face.v0) == 0)
					drawLinePosColorFromTo(area.start, face.v0, light.lightWorldPos(area.start, face.v0, faceNormal), mutable, lightColor, buffer, matrix, camera);
				if (faceInfo.getLight(light, face.v1) == 0)
					drawLinePosColorFromTo(area.start, face.v1, light.lightWorldPos(area.start, face.v1, faceNormal), mutable, lightColor, buffer, matrix, camera);
				if (faceInfo.getLight(light, face.v2) == 0)
					drawLinePosColorFromTo(area.start, face.v2, light.lightWorldPos(area.start, face.v2, faceNormal), mutable, lightColor, buffer, matrix, camera);
				if (faceInfo.getLight(light, face.v3) == 0)
					drawLinePosColorFromTo(area.start, face.v3, light.lightWorldPos(area.start, face.v3, faceNormal), mutable, lightColor, buffer, matrix, camera);

				return true;
			});
		}
	}

	private static void drawLinePosColorFromAdd(BlockPos offset, Vec start, Vec add, Color color, IVertexBuilder buffer, MatrixStack matrix, Vector3d camera) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;
		float startX = (float) (offset.getX() - camera.x + start.x);
		float startY = (float) (offset.getY() - camera.y + start.y);
		float startZ = (float) (offset.getZ() - camera.z + start.z);
		vertex(buffer, matrix, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, startX + add.x, startY + add.y, startZ + add.z).color(red, green, blue, alpha).endVertex();
	}

	private static void drawLinePosColorFromTo(BlockPos startOffset, Vec start, BlockPos endOffset, Vec end, Color color, IVertexBuilder buffer, MatrixStack matrix, Vector3d camera) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;
		vertex(buffer, matrix, (float) (startOffset.getX() + start.x - camera.x), (float) (startOffset.getY() + start.y - camera.y), (float) (startOffset.getZ() + start.z - camera.z)).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, (float) (endOffset.getX() + end.x - camera.x), (float) (endOffset.getY() + end.y - camera.y), (float) (endOffset.getZ() + end.z - camera.z)).color(red, green, blue, alpha).endVertex();
	}

	private static void drawFacePosColor(Face face, Vector3d camera, BlockPos pos, Color color, IVertexBuilder buffer, MatrixStack matrix) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;

		Vec v0 = face.v0;
		Vec v1 = face.v1;
		Vec v2 = face.v2;
		Vec v3 = face.v3;
		double x = pos.getX() - camera.x;
		double y = pos.getY() - camera.y;
		double z = pos.getZ() - camera.z;

		float v0x = (float) (x + v0.x);
		float v1x = (float) (x + v1.x);
		float v2x = (float) (x + v2.x);
		float v3x = (float) (x + v3.x);
		float v0y = (float) (y + v0.y);
		float v1y = (float) (y + v1.y);
		float v2y = (float) (y + v2.y);
		float v3y = (float) (y + v3.y);
		float v0z = (float) (z + v0.z);
		float v1z = (float) (z + v1.z);
		float v2z = (float) (z + v2.z);
		float v3z = (float) (z + v3.z);
		vertex(buffer, matrix, v0x, v0y, v0z).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, v1x, v1y, v1z).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, v1x, v1y, v1z).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, v2x, v2y, v2z).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, v2x, v2y, v2z).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, v3x, v3y, v3z).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, v3x, v3y, v3z).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix, v0x, v0y, v0z).color(red, green, blue, alpha).endVertex();
	}

	private static void drawShape(MatrixStack stack, IVertexBuilder buffer, VoxelShape shape, BlockPos pos, Vector3d camera, Color color) {
		Matrix4f pose = stack.last().pose();
		double x = pos.getX() - camera.x;
		double y = pos.getY() - camera.y;
		double z = pos.getZ() - camera.z;
		shape.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
			vertex(buffer, pose, (float) (x + x0), (float) (y + y0), (float) (z + z0)).color(color.red, color.green, color.blue, color.alpha).endVertex();
			vertex(buffer, pose, (float) (x + x1), (float) (y + y1), (float) (z + z1)).color(color.red, color.green, color.blue, color.alpha).endVertex();
		});
	}

}
