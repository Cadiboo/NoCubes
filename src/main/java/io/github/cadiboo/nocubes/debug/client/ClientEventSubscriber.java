package io.github.cadiboo.nocubes.debug.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.debug.client.render.IDebugRenderAlgorithm;
import io.github.cadiboo.nocubes.debug.client.render.IDebugRenderAlgorithm.Face;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.optifine.RebuildChunkBlockOptifineEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.mod.EnumEventType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer.AmbientOcclusionFace;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.BitSet;
import java.util.List;

import static io.github.cadiboo.nocubes.util.ModEnums.EffortLevel.OFF;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = ModReference.MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.debug.debugEnabled) {
			return;
		}

		final BlockPos pos = event.getBlockPos();
		final IBlockAccess cache;
		if (event.getType() == EnumEventType.FORGE_OPTIFINE) {
			cache = ((RebuildChunkBlockOptifineEvent) event).getChunkCacheOF();
		} else {
			cache = event.getChunkCache();
		}

		final List<Face<Vec3>> faces = ModConfig.debug.activeRenderingAlgorithm.getFaces(event.getBlockPos(), cache);

		if (faces.isEmpty()) {
			return;
		}

		final BufferBuilder bufferBuilder = event.getBufferBuilder();

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		final IBlockState state = cache.getBlockState(pos);
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();

		final Object[] texturePosAndState = ClientUtil.getTexturePosAndState(cache, pos, state);
		final BlockPos texturePos = (BlockPos) texturePosAndState[0];
		final IBlockState textureState = (IBlockState) texturePosAndState[1];

		//real pos not texture pos
		final LightmapInfo lightmapInfo = ClientUtil.getLightmapInfo(pos, cache);

		AmbientOcclusionFace aoFace = ClientUtil.makeAmbientOcclusionFace();

		event.setCanceled(true);
		event.setBlockRenderLayerUsed(event.getBlockRenderLayer(), true);

		EnumFacing[] VALUES = EnumFacing.VALUES;
		int facingIndex = 0;

		for (IDebugRenderAlgorithm.Face<Vec3> vec3Face : faces) {
			EnumFacing facing = VALUES[facingIndex++];

			if (!event.getBlockState().shouldSideBeRendered(cache, pos, facing)
				//hmmmm
//					&& ModUtil.shouldSmooth(cache.getBlockState(pos.offset(facing)))
			) {
				continue;
			}

			final float[] quadBounds = new float[EnumFacing.VALUES.length * 2];
			final BitSet bitset = new BitSet(3);

			//updateVertexBrightness(IBlockAccess worldIn, IBlockState state, BlockPos centerPos, EnumFacing direction, float[] faceShape, BitSet shapeState)
			aoFace.updateVertexBrightness(cache, state, pos, facing, quadBounds, bitset);

//			final int
			
			final int lightmapSkyLight0, lightmapSkyLight1, lightmapSkyLight2, lightmapSkyLight3;
			lightmapSkyLight0 = lightmapSkyLight1 = lightmapSkyLight2 = lightmapSkyLight3 = lightmapInfo.getLightmapSkyLight();
			final int lightmapBlockLight0, lightmapBlockLight1, lightmapBlockLight2, lightmapBlockLight3;
			lightmapBlockLight0 = lightmapBlockLight1 = lightmapBlockLight2 = lightmapBlockLight3 = lightmapInfo.getLightmapBlockLight();

			BakedQuad quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher, facing);
			if (quad == null) {
				quad = blockRendererDispatcher.getBlockModelShapes().getModelManager().getMissingModel().getQuads(null, null, 0L).get(0);
			}
			TextureAtlasSprite sprite = quad.getSprite();
			if (ModConfig.beautifyTexturesLevel != OFF) {
				if (sprite == blockRendererDispatcher.getModelForState(Blocks.GRASS.getDefaultState()).getQuads(Blocks.GRASS.getDefaultState(), EnumFacing.NORTH, 0L).get(0).getSprite()) {
					quad = ClientUtil.getQuad(textureState, texturePos, blockRendererDispatcher, EnumFacing.UP);
					sprite = quad.getSprite();
				}
			}
			final int color = ClientUtil.getColor(quad, textureState, cache, texturePos);
			final int red = (color >> 16) & 255;
			final int green = (color >> 8) & 255;
			final int blue = color & 255;
			final int alpha = 0xFF;

			final float minU = ClientUtil.getMinU(sprite);
			final float minV = ClientUtil.getMinV(sprite);
			final float maxU = ClientUtil.getMaxU(sprite);
			final float maxV = ClientUtil.getMaxV(sprite);

			final Vec3 vertex0 = vec3Face.getVertex0();
			final Vec3 vertex1 = vec3Face.getVertex1();
			final Vec3 vertex2 = vec3Face.getVertex2();
			final Vec3 vertex3 = vec3Face.getVertex3();

			bufferBuilder.pos(vertex0.xCoord, vertex0.yCoord, vertex0.zCoord).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight0, lightmapBlockLight0).endVertex();
			bufferBuilder.pos(vertex1.xCoord, vertex1.yCoord, vertex1.zCoord).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight1, lightmapBlockLight1).endVertex();
			bufferBuilder.pos(vertex2.xCoord, vertex2.yCoord, vertex2.zCoord).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight2, lightmapBlockLight2).endVertex();
			bufferBuilder.pos(vertex3.xCoord, vertex3.yCoord, vertex3.zCoord).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight3, lightmapBlockLight3).endVertex();

		}

	}

	@SubscribeEvent
	public static void onDrawBlockHighlightEvent(final DrawBlockHighlightEvent event) {

		if (!NoCubes.isEnabled()) {
			return;
		}

		if (!ModConfig.debug.debugEnabled) {
			return;
		}

		if (!ModConfig.debug.highlightVertices) {
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

		final List<Vec3> vertices = ModConfig.debug.activeRenderingAlgorithm.getVertices(rayTraceResult.getBlockPos(), player.world);
		if (vertices.isEmpty() || vertices.size() < 8) {
			return;
		}

//		event.setCanceled(true);

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
			GlStateManager.color(1F, 1F, 1F, 1F);
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
		final Vec3 v0 = vertices.get(0).offset(renderX, renderY, renderZ).offset(nOffset, nOffset, nOffset);
		final Vec3 v1 = vertices.get(1).offset(renderX, renderY, renderZ).offset(pOffset, nOffset, nOffset);
		final Vec3 v2 = vertices.get(2).offset(renderX, renderY, renderZ).offset(nOffset, nOffset, pOffset);
		final Vec3 v3 = vertices.get(3).offset(renderX, renderY, renderZ).offset(pOffset, nOffset, pOffset);
		final Vec3 v4 = vertices.get(4).offset(renderX, renderY, renderZ).offset(nOffset, pOffset, nOffset);
		final Vec3 v5 = vertices.get(5).offset(renderX, renderY, renderZ).offset(pOffset, pOffset, nOffset);
		final Vec3 v6 = vertices.get(6).offset(renderX, renderY, renderZ).offset(nOffset, pOffset, pOffset);
		final Vec3 v7 = vertices.get(7).offset(renderX, renderY, renderZ).offset(pOffset, pOffset, pOffset);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		// yay, magic numbers!
		bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

		GlStateManager.color(1F, 1F, 1F, 1F);

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
