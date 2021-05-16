package io.github.cadiboo.nocubes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.RollingProfiler;
import io.github.cadiboo.nocubes.collision.OOCollisionHandler;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.hooks.SelfCheck;
import io.github.cadiboo.nocubes.mesh.MeshGenerator;
import io.github.cadiboo.nocubes.util.*;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
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

import static io.github.cadiboo.nocubes.client.render.MeshRenderer.FaceInfo;
import static io.github.cadiboo.nocubes.config.ColorParser.Color;

/**
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class OverlayRenderer {

	private static final RollingProfiler meshProfiler = new RollingProfiler(600);
	private static long selfCheckInfoPrintedAt = Long.MIN_VALUE;

	@SubscribeEvent
	public static void onHighlightBlock(final DrawHighlightEvent.HighlightBlock event) {
		if (!NoCubesConfig.Client.render)
			return;
		final ClientWorld world = Minecraft.getInstance().level;
		if (world == null)
			return;
		final BlockPos lookingAtPos = event.getTarget().getBlockPos();
		final BlockState state = world.getBlockState(lookingAtPos);
		if (!NoCubes.smoothableHandler.isSmoothable(state))
			return;

		event.setCanceled(true);

		final Vector3d camera = event.getInfo().getPosition();
		final Matrix4f matrix4f = event.getMatrix().last().pose();
		final IVertexBuilder bufferBuilder = event.getBuffers().getBuffer(RenderType.lines());
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		try (Area area = new Area(world, lookingAtPos, ModUtil.VEC_ONE, generator)) {
			generator.generate(area, NoCubes.smoothableHandler::isSmoothable, (pos, face) -> {
				drawFacePosColor(face, camera, area.start, NoCubesConfig.Client.selectionBoxColor, bufferBuilder, matrix4f);
				return true;
			});
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
		if (!NoCubesConfig.Client.debugEnabled)
			return;

		final Minecraft minecraft = Minecraft.getInstance();
		final World world = minecraft.level;
		if (world == null)
			return;

		if (Screen.hasAltDown()) {
			long time = world.getGameTime();
			// Only print once every 10 seconds, don't spam the log
			if (time - 10 * 20 > selfCheckInfoPrintedAt) {
				selfCheckInfoPrintedAt = time;
				LogManager.getLogger("SelfCheck").debug(String.join("\n", SelfCheck.info()));
			}
		}

		Entity viewer = minecraft.gameRenderer.getMainCamera().getEntity();
		if (viewer == null)
			return;

		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;

		final Vector3d camera = minecraft.gameRenderer.getMainCamera().getPosition();
		double cameraX = camera.x;
		double cameraY = camera.y;
		double cameraZ = camera.z;
		final MatrixStack matrixStack = event.getMatrixStack();

		final IRenderTypeBuffer.Impl bufferSource = minecraft.renderBuffers().bufferSource();
		final IVertexBuilder bufferBuilder = bufferSource.getBuffer(RenderType.lines());

		RayTraceResult targeted = viewer.pick(20.0D, 0.0F, false);
		// Where the player is looking at or their position of they're not looking at a block
		BlockPos targetedPos = targeted.getType() != RayTraceResult.Type.BLOCK ? viewer.blockPosition() : ((BlockRayTraceResult) targeted).getBlockPos();

		// Destroy block progress
		if (false) {
			BlockPos start = targetedPos.offset(-2, -2, -2);
			BlockPos end = targetedPos.offset(2, 2, 2);
			final int[] i = {0};
			BlockPos.betweenClosed(start, end).forEach(pos -> {
				minecraft.levelRenderer.destroyBlockProgress(100 + i[0]++, pos, 9);
			});
		}

		// Outline nearby smoothable blocks
		if (NoCubesConfig.Client.debugOutlineSmoothables) {
			BlockPos start = viewer.blockPosition().offset(-5, -5, -5);
			BlockPos end = viewer.blockPosition().offset(5, 5, 5);
			BlockPos.betweenClosed(start, end).forEach(blockPos -> {
				if (NoCubes.smoothableHandler.isSmoothable(viewer.level.getBlockState(blockPos)))
					drawShape(matrixStack, bufferBuilder, VoxelShapes.block(), -cameraX + blockPos.getX(), -cameraY + blockPos.getY(), -cameraZ + blockPos.getZ(), 0.0F, 1.0F, 1.0F, 0.4F);
			});
		}

		// Draw nearby block densities and computed corner signed distance fields
		// This was just for understanding how SurfaceNets works
		// It made me understand why feeding it the 'proper' corner info results in much smoother terrain
		// at the cost of 1-block formations disappearing
		if (NoCubesConfig.Client.debugVisualiseDensitiesGrid) {
			VoxelShape distanceIndicator = VoxelShapes.box(0, 0, 0, 1 / 8F, 1 / 8F, 1 / 8F);
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
				for (int z = minZ; z < maxZ; ++z) {
					for (int y = minY; y < maxY; ++y) {
						for (int x = minX; x < maxX; ++x, ++zyxIndex) {
							float density = densities[zyxIndex];
							float densityScale = 0.5F + density / 2F; // from [-1, 1] -> [0, 1]
							if (densityScale > 0.01) {
								VoxelShape box = VoxelShapes.box(0.5 - densityScale / 2, 0.5 - densityScale / 2, 0.5 - densityScale / 2, 0.5 + densityScale / 2, 0.5 + densityScale / 2, 0.5 + densityScale / 2);
								drawShape(matrixStack, bufferBuilder, box, x - cameraX, y - cameraY, z - cameraZ, 0F, 0F, 1F, 0.5F);
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
							drawShape(matrixStack, bufferBuilder, distanceIndicator, x - cameraX, y - cameraY, z - cameraZ, combinedDensityScale, 1 - combinedDensityScale, 0F, 0.4F);
						}
					}
				}
			}
		}

		// Draw nearby collisions in green and player intersecting collisions in red
		if (NoCubesConfig.Client.debugRenderCollisions) {
			VoxelShape viewerShape = VoxelShapes.create(viewer.getBoundingBox());
			world.getBlockCollisions(viewer, viewer.getBoundingBox().inflate(5.0D)).forEach(voxelShape -> {
				boolean intersects = VoxelShapes.joinIsNotEmpty(voxelShape, viewerShape, IBooleanFunction.OR);
				float red = intersects ? 1 : 0;
				float green = intersects ? 0 : 1;
				drawShape(matrixStack, bufferBuilder, voxelShape, -cameraX, -cameraY, -cameraZ, red, green, 0.0F, 0.4F);
			});
		}

		// Draw NoCubes' collisions in green (or yellow if debugRenderCollisions is enabled)
		if (NoCubesConfig.Client.debugRenderMeshCollisions) {
			BlockPos size = new BlockPos(10, 10, 10);
			BlockPos start = viewer.blockPosition().offset(-size.getX() / 2, -size.getY() / 2, -size.getZ() / 2);
			try (Area area = new Area(world, start, size)) {
				new OOCollisionHandler(generator).generate(area, (x0, y0, z0, x1, y1, z1) -> {
					double x = area.start.getX();
					double y = area.start.getY();
					double z = area.start.getZ();
					VoxelShape voxelShape = VoxelShapes.box(
						x + x0, y + y0, z + z0,
						x + x1, y + y1, z + z1
					);
					float red = NoCubesConfig.Client.debugRenderCollisions ? 1.0F : 0.0F;
					drawShape(matrixStack, bufferBuilder, voxelShape, -cameraX, -cameraY, -cameraZ, red, 1.0F, 0.0F, 0.4F);
				});
			}
		}

		// Measure the performance of meshing nearby blocks (and maybe render the result)
		if (NoCubesConfig.Client.debugRecordMeshPerformance || NoCubesConfig.Client.debugOutlineNearbyMesh) {
			long startNanos = System.nanoTime();
			drawNearbyMesh(viewer, matrixStack.last().pose(), camera, bufferBuilder);
			if (NoCubesConfig.Client.debugRecordMeshPerformance) {
				if (meshProfiler.recordElapsedNanos(startNanos))
					LogManager.getLogger("Calc" + (NoCubesConfig.Client.debugOutlineNearbyMesh ? " & outline" : "") + " nearby mesh").debug("Average {}ms over the past {} frames", meshProfiler.average() / 1000_000F, meshProfiler.size());
			}
		}

		// Hack to finish buffer because RenderWorldLastEvent seems to fire after vanilla normally finishes them
		bufferSource.endBatch(RenderType.lines());
	}

	private static void drawNearbyMesh(Entity viewer, Matrix4f matrix4f, Vector3d camera, IVertexBuilder bufferBuilder) {
		MeshGenerator generator = NoCubesConfig.Server.meshGenerator;
		BlockPos meshSize = new BlockPos(16, 16, 16);
		BlockPos meshStart = viewer.blockPosition().offset(-meshSize.getX() / 2, -meshSize.getY() / 2 + 2, -meshSize.getZ() / 2);
		try (
			Area area = new Area(viewer.level, meshStart, meshSize, generator);
			LightCache light = new LightCache((ClientWorld) viewer.level, meshStart, meshSize);
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
				BlockPos start = area.start;
				drawFacePosColor(face, camera, start, faceColor, bufferBuilder, matrix4f);

				faceInfo.setup(face, start);
				Face vertexNormals = faceInfo.vertexNormals;
				Vec faceNormal = faceInfo.faceNormal;
				Direction faceDirection = faceInfo.faceDirection;
				face.assignAverageTo(centre);

				// Draw face normal vec + resulting direction
				final float dirMul = 0.2F;
				drawLinePosColorFromAdd(start, centre, mutable.set(faceNormal).multiply(dirMul), averageNormalColor, bufferBuilder, matrix4f, camera);
				drawLinePosColorFromAdd(start, centre, mutable.set(faceDirection.getStepX(), faceDirection.getStepY(), faceDirection.getStepZ()).multiply(dirMul), normalDirectionColor, bufferBuilder, matrix4f, camera);

				// Draw each vertex normal
				drawLinePosColorFromAdd(start, face.v0, mutable.set(vertexNormals.v0).multiply(dirMul), normalColor, bufferBuilder, matrix4f, camera);
				drawLinePosColorFromAdd(start, face.v1, mutable.set(vertexNormals.v1).multiply(dirMul), normalColor, bufferBuilder, matrix4f, camera);
				drawLinePosColorFromAdd(start, face.v2, mutable.set(vertexNormals.v2).multiply(dirMul), normalColor, bufferBuilder, matrix4f, camera);
				drawLinePosColorFromAdd(start, face.v3, mutable.set(vertexNormals.v3).multiply(dirMul), normalColor, bufferBuilder, matrix4f, camera);

				// Draw light pos
				mutable.set(0, 0, 0);
				if (light.get(start, face.v0, faceNormal) == 0)
					drawLinePosColorFromTo(start, face.v0, light.mutablePos, mutable, lightColor, bufferBuilder, matrix4f, camera);
				if (light.get(start, face.v1, faceNormal) == 0)
					drawLinePosColorFromTo(start, face.v1, light.mutablePos, mutable, lightColor, bufferBuilder, matrix4f, camera);
				if (light.get(start, face.v2, faceNormal) == 0)
					drawLinePosColorFromTo(start, face.v2, light.mutablePos, mutable, lightColor, bufferBuilder, matrix4f, camera);
				if (light.get(start, face.v3, faceNormal) == 0)
					drawLinePosColorFromTo(start, face.v3, light.mutablePos, mutable, lightColor, bufferBuilder, matrix4f, camera);

				return true;
			});
		}
	}

	private static void drawLinePosColorFromAdd(BlockPos offset, Vec start, Vec add, Color color, IVertexBuilder buffer, Matrix4f matrix4f, Vector3d camera) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;
		float startX = (float) (offset.getX() - camera.x + start.x);
		float startY = (float) (offset.getY() - camera.y + start.y);
		float startZ = (float) (offset.getZ() - camera.z + start.z);
		vertex(buffer, matrix4f, startX, startY, startZ).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix4f, startX + add.x, startY + add.y, startZ + add.z).color(red, green, blue, alpha).endVertex();
	}

	private static void drawLinePosColorFromTo(BlockPos startOffset, Vec start, BlockPos endOffset, Vec end, Color color, IVertexBuilder buffer, Matrix4f matrix4f, Vector3d camera) {
		int red = color.red;
		int blue = color.blue;
		int green = color.green;
		int alpha = color.alpha;
		vertex(buffer, matrix4f, (float) (startOffset.getX() + start.x - camera.x), (float) (startOffset.getY() + start.y - camera.y), (float) (startOffset.getZ() + start.z - camera.z)).color(red, green, blue, alpha).endVertex();
		vertex(buffer, matrix4f, (float) (endOffset.getX() + end.x - camera.x), (float) (endOffset.getY() + end.y - camera.y), (float) (endOffset.getZ() + end.z - camera.z)).color(red, green, blue, alpha).endVertex();
	}

	private static void drawFacePosColor(Face face, Vector3d camera, BlockPos pos, Color color, IVertexBuilder bufferBuilder, Matrix4f matrix4f) {
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
		vertex(bufferBuilder, matrix4f, v0x, v0y, v0z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v1x, v1y, v1z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v1x, v1y, v1z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v2x, v2y, v2z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v2x, v2y, v2z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v3x, v3y, v3z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v3x, v3y, v3z).color(red, green, blue, alpha).endVertex();
		vertex(bufferBuilder, matrix4f, v0x, v0y, v0z).color(red, green, blue, alpha).endVertex();
	}

	private static IVertexBuilder vertex(IVertexBuilder bufferBuilder, Matrix4f matrix4f, float x, float y, float z) {
		// Calling 'bufferBuilder.vertex(matrix4f, x, y, z)' allocates a Vector4f
		// To avoid allocating so many short lived vectors we do the transform ourselves instead
		float w = 1.0F;
		float tx = matrix4f.m00 * x + matrix4f.m01 * y + matrix4f.m02 * z + matrix4f.m03 * w;
		float ty = matrix4f.m10 * x + matrix4f.m11 * y + matrix4f.m12 * z + matrix4f.m13 * w;
		float tz = matrix4f.m20 * x + matrix4f.m21 * y + matrix4f.m22 * z + matrix4f.m23 * w;
//		float tw = matrix4f.m30 * x + matrix4f.m31 * y + matrix4f.m32 * z + matrix4f.m33 * w;
		return bufferBuilder.vertex(tx, ty, tz);
	}

	private static void drawShape(MatrixStack matrixStackIn, IVertexBuilder bufferIn, VoxelShape shapeIn, double xIn, double yIn, double zIn, float red, float green, float blue, float alpha) {
		Matrix4f matrix4f = matrixStackIn.last().pose();
		shapeIn.forAllEdges((x0, y0, z0, x1, y1, z1) -> {
			vertex(bufferIn, matrix4f, (float) (x0 + xIn), (float) (y0 + yIn), (float) (z0 + zIn)).color(red, green, blue, alpha).endVertex();
			vertex(bufferIn, matrix4f, (float) (x1 + xIn), (float) (y1 + yIn), (float) (z1 + zIn)).color(red, green, blue, alpha).endVertex();
		});
	}

}
