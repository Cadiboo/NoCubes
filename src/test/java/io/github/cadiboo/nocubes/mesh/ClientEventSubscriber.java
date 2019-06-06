package io.github.cadiboo.nocubes.mesh;

import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import io.github.cadiboo.nocubes.util.pooled.Vec3b;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;
import static net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	private static final BufferBuilder bufferBuilder = new BufferBuilder(0x200);

	private static int testDataIndex = 0;

	@SubscribeEvent
	public static void onClientTickEvent(final ClientTickEvent event) {

		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		final Minecraft minecraft = Minecraft.getInstance();
		final WorldClient world = minecraft.world;
		if (world == null) {
			return;
		}
		// only update once every 5 seconds, to show the caching
		if (world.getGameTime() % 100 != 0) {
			return;
		}

		final BufferBuilder bufferbuilder = bufferBuilder;
		bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

		final MeshTesting.TestData testData = MeshTesting.TEST_DATA[++testDataIndex % MeshTesting.TEST_DATA.length];
		final HashMap<Vec3b, FaceList> map = MeshGeneratorType.SurfaceNets.getMeshGenerator().generateChunk(testData.data, testData.dims);

		for (final Vec3b vec3b : map.keySet()) {
			vec3b.close();
		}

		for (final FaceList faces : map.values()) {
			try {
				for (int i = 0, facesSize = faces.size(); i < facesSize; i++) {
					try (Face face = faces.get(i)) {
						try (
								Vec3 v0 = face.getVertex0();
								Vec3 v1 = face.getVertex1();
								Vec3 v2 = face.getVertex2();
								Vec3 v3 = face.getVertex3()
						) {
							// Hologram colors:
							// "http://irfandiawhite.co/2019/03/11/blue-hologram-color-code/"
							// Onahou - #CFF7FB (0.81, 0.93, 0.98, 1.00)
							// Onahou - #CFEDFB (0.81, 0.93, 0.98, 1.00)
							// Hawkes Blue - #CFE4FB (0.81, 0.89, 0.98, 1.00)
							// Hawkes Blue - #CFDCFB (0.81, 0.86, 0.98, 1.00)
							// Lavender Blue - #CDD0F9 (0.80, 0.82, 0.98, 1.00)
							// "https://www.colorhexa.com/33b5e5"
							// Picton Blue - #33B5E5 (0.20, 0.71, 0.90, 1.00)
							// Picton Blue - #44B7E1 (0.27, 0.72, 0.88, 1.00)
							// Picton Blue - #4CC2E6 (0.30, 0.76, 0.90, 1.00)
							// From Picture on "https://www.minecraftforge.net/forum/topic/70900-creating-a-hologramwireframe/"
							// Faded Cyan - #81FEFD (0.49, 0.99, 0.99, 1.00)
							// Electric Blue - #7EFCFD (0.49, 0.99, 0.99, 1.00)
							// Faded Cyan - #93FBF9 (0.58, 0.98, 0.98, 1.00)
							// Danube - #5D86C4 (0.36, 0.53, 0.77, 1.00)
							// From WIPTech
							// ? - #3B9AC9FF (0.60, 0.79, 1.00, 1.00)
							// This was already in my palette and it looked good
							// Faded Blue - #8A8EF9 (0.54, 0.56, 0.98, 1.00)
							// The color I ended up using was none of these
							// Picton Blue - #3399FF (0.20, 0.60, 1.00)
							final double v0x = v0.x;
							final double v0y = v0.y;
							final double v0z = v0.z;
							// Start at v0. Transparent because we don't want to draw a line from wherever the previous vertex was
							bufferbuilder.pos(v0x, v0y, v0z).color(0.20F, 0.60F, 1.00F, 0.0F).endVertex();
							bufferbuilder.pos(v1.x, v1.y, v1.z).color(0.20F, 0.60F, 1.00F, 0.4F).endVertex();
							bufferbuilder.pos(v2.x, v2.y, v2.z).color(0.20F, 0.60F, 1.00F, 0.4F).endVertex();
							bufferbuilder.pos(v3.x, v3.y, v3.z).color(0.20F, 0.60F, 1.00F, 0.4F).endVertex();
							// End back at v0. Draw with alpha this time
							bufferbuilder.pos(v0x, v0y, v0z).color(0.20F, 0.60F, 1.00F, 0.4F).endVertex();
						}
					}
				}

			} finally {
				faces.close();
			}
		}

		bufferbuilder.finishDrawing();

	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {

		// Code to draw the buffer
		{
			final Entity entity = Minecraft.getInstance().getRenderViewEntity();
			if (entity == null) {
				return;
			}
			final float partialTicks = event.getPartialTicks();

			// Copied from EntityRenderer. This code can be found by looking at usages of Entity.prevPosX.
			// It also appears in many other places throughout Minecraft's rendering
			double renderPosX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * (double) partialTicks;
			double renderPosY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * (double) partialTicks;
			double renderPosZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * (double) partialTicks;

			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.lineWidth(1.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);

			GlStateManager.color4f(0, 0, 0, 1);
			GlStateManager.color4f(1, 1, 1, 1);

			GlStateManager.pushMatrix();
			GlStateManager.translated(-renderPosX, -renderPosY + 20, -renderPosZ);
			// Render at 1/4 scale
			GlStateManager.scalef(0.25F, 0.25F, 0.25F);
			drawBuffer(bufferBuilder);
			GlStateManager.popMatrix();

			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();

		}

	}

	// Coppied from the Tessellator's vboUploader - Draw everything but don't reset the buffer
	// Unused code from vanilla is commented out, along with the code that resets the buffer
	private static void drawBuffer(final BufferBuilder bufferBuilder) {
		if (bufferBuilder.getVertexCount() > 0) {
			VertexFormat vertexformat = bufferBuilder.getVertexFormat();
			int i = vertexformat.getSize();
			ByteBuffer bytebuffer = bufferBuilder.getByteBuffer();
			List<VertexFormatElement> list = vertexformat.getElements();

			for (int j = 0; j < list.size(); ++j) {
				VertexFormatElement vertexformatelement = list.get(j);
//				VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
//				int k = vertexformatelement.getType().getGlConstant();
//				int l = vertexformatelement.getIndex();
				bytebuffer.position(vertexformat.getOffset(j));

				// moved to VertexFormatElement.preDraw
				vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
			}

			GlStateManager.drawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
			int i1 = 0;

			for (int j1 = list.size(); i1 < j1; ++i1) {
				VertexFormatElement vertexformatelement1 = list.get(i1);
//				VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
//				int k1 = vertexformatelement1.getIndex();

				// moved to VertexFormatElement.postDraw
				vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
			}
		}

//		bufferBuilder.reset(); // Commented out from the tessellator's vboUploader. Don't reset the buffer
	}

}
