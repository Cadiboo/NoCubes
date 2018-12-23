package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.OldNoCubes;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInLayerEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockRenderInTypeEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPostEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	@SubscribeEvent
	public static void onRebuildChunkPreEvent(final RebuildChunkPreEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderPre(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockRenderInLayerEvent(final RebuildChunkBlockRenderInLayerEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderLayer(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockRenderInTypeEvent(final RebuildChunkBlockRenderInTypeEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderType(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderBlock(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkPostEvent(final RebuildChunkPostEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		ModConfig.activeRenderingAlgorithm.renderPost(event);
	}

	@SubscribeEvent
	public static void onDrawBlockHighlightEvent(final DrawBlockHighlightEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		final EntityPlayer player = Minecraft.getMinecraft().player;
		if (player == null) {
			return;
		}

		final RayTraceResult rayTraceResult = event.getTarget();
		if (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
			return;
		}

		final Vec3[] vertices = OldNoCubes.getPoints(rayTraceResult.getBlockPos(), player.world);
		if (vertices == null) {
			return;
		}

		event.setCanceled(true);

		final Profiler profiler = player.world.profiler;
		profiler.startSection("drawing dynamic block highlights");

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
		GlStateManager.glLineWidth(2.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		final float partialTicks = event.getPartialTicks();
		final double renderX = -(player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks));
		final double renderY = -(player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks));
		final double renderZ = -(player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks));

		for (AxisAlignedBB axisAlignedBB : player.world.getCollisionBoxes(player, new AxisAlignedBB(rayTraceResult.getBlockPos()))) {
			RenderGlobal.drawSelectionBoundingBox(axisAlignedBB.offset(renderX, renderY, renderZ), 1, 0, 0, 1);
		}

		//				drawSelectionBoundingBox(iblockstate.getSelectedBoundingBox(this.world, blockpos).grow(0.0020000000949949026D).offset(-d3, -d4, -d5), 0.0F, 0.0F, 0.0F, 0.4F);

		//todo: constants?
		final float
				red = 0.0F,
				green = 1.0F,
				blue = 1.0F,
				alpha = 0.4F,
				invis = 0.0F;

		final double pOffset = 0.0020000000949949026D;
		final double nOffset = -pOffset;

		// vertex for every corner
		final Vec3 v0 = vertices[0].offset(renderX, renderY, renderZ).offset(nOffset, nOffset, nOffset);
		final Vec3 v1 = vertices[1].offset(renderX, renderY, renderZ).offset(pOffset, nOffset, nOffset);
		final Vec3 v2 = vertices[2].offset(renderX, renderY, renderZ).offset(nOffset, nOffset, pOffset);
		final Vec3 v3 = vertices[3].offset(renderX, renderY, renderZ).offset(pOffset, nOffset, pOffset);
		final Vec3 v4 = vertices[4].offset(renderX, renderY, renderZ).offset(nOffset, pOffset, nOffset);
		final Vec3 v5 = vertices[5].offset(renderX, renderY, renderZ).offset(pOffset, pOffset, nOffset);
		final Vec3 v6 = vertices[6].offset(renderX, renderY, renderZ).offset(nOffset, pOffset, pOffset);
		final Vec3 v7 = vertices[7].offset(renderX, renderY, renderZ).offset(pOffset, pOffset, pOffset);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		// yay, magic numbers!
		bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

		// bottom
		// assuming facing north (towards neg z)
		// 0 1
		// 3 2
		bufferBuilder.pos(v0.xCoord, v0.yCoord, v0.zCoord).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v0.xCoord, v0.yCoord, v0.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v1.xCoord, v1.yCoord, v1.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v2.xCoord, v2.yCoord, v2.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v3.xCoord, v3.yCoord, v3.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v0.xCoord, v0.yCoord, v0.zCoord).color(red, green, blue, alpha).endVertex();
		// top
		// assuming facing north (towards neg z)
		// 4 5
		// 7 6
		bufferBuilder.pos(v4.xCoord, v4.yCoord, v4.zCoord).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v4.xCoord, v4.yCoord, v4.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v5.xCoord, v5.yCoord, v5.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v6.xCoord, v6.yCoord, v6.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v7.xCoord, v7.yCoord, v7.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v4.xCoord, v4.yCoord, v4.zCoord).color(red, green, blue, alpha).endVertex();
		// north
		// assuming facing north (towards neg z) & up (we only draw 0-4 & 1-5 cause the others are already drawn)
		// 0 1
		// 4 5
		bufferBuilder.pos(v0.xCoord, v0.yCoord, v0.zCoord).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v0.xCoord, v0.yCoord, v0.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v4.xCoord, v4.yCoord, v4.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v1.xCoord, v1.yCoord, v1.zCoord).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v1.xCoord, v1.yCoord, v1.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v5.xCoord, v5.yCoord, v5.zCoord).color(red, green, blue, alpha).endVertex();
		// south
		// assuming facing north (towards neg z) & up (we only draw 3-2 & 7-6 cause the others are already drawn)
		// 3 2
		// 7 6
		bufferBuilder.pos(v3.xCoord, v3.yCoord, v3.zCoord).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v3.xCoord, v3.yCoord, v3.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v2.xCoord, v2.yCoord, v2.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v7.xCoord, v7.yCoord, v7.zCoord).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v7.xCoord, v7.yCoord, v7.zCoord).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v6.xCoord, v6.yCoord, v6.zCoord).color(red, green, blue, alpha).endVertex();

		//		bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, 0.0F).endVertex();
//		bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, 0.0F).endVertex();
//		bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, 0.0F).endVertex();
//		bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, 0.0F).endVertex();
//		bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, 0.0F).endVertex();

//		bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, 0.0F).endVertex();
//		bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, minY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, maxY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(minX, maxY, maxZ).color(red, green, blue, 0.0F).endVertex();
//		bufferBuilder.pos(minX, minY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, maxY, maxZ).color(red, green, blue, 0.0F).endVertex();
//		bufferBuilder.pos(maxX, minY, maxZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, maxY, minZ).color(red, green, blue, 0.0F).endVertex();
//		bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, alpha).endVertex();
//		bufferBuilder.pos(maxX, minY, minZ).color(red, green, blue, 0.0F).endVertex();

		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		profiler.endSection();

	}

}
