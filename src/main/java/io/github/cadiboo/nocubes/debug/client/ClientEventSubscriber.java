package io.github.cadiboo.nocubes.debug.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.ClientUtil;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.debug.client.render.IDebugRenderAlgorithm.Face;
import io.github.cadiboo.nocubes.util.LightmapInfo;
import io.github.cadiboo.nocubes.util.ModReference;
import io.github.cadiboo.nocubes.util.ModUtil;
import io.github.cadiboo.nocubes.util.Vec3;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
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
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

		final MutableBlockPos pos = event.getBlockPos();
		final PooledMutableBlockPos pooledMutableBlockPos = PooledMutableBlockPos.retain(pos);

		try {

			final IBlockAccess cache = ClientUtil.getCache(event);

			final List<Face<Vec3>> faces = ModConfig.debug.activeRenderingAlgorithm.getFaces(pooledMutableBlockPos, cache);

			if (faces.isEmpty()) {
				return;
			}

			final IBlockState state = cache.getBlockState(pos);
			final int x = pos.getX();
			final int y = pos.getY();
			final int z = pos.getZ();

			final BufferBuilder bufferBuilder = event.getBufferBuilder();

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

			final BitSet bitset = new BitSet(3);

			for (Face<Vec3> face : faces) {
				EnumFacing facing = VALUES[facingIndex++];

				if (!event.getBlockState().shouldSideBeRendered(cache, pos, facing)
//				hmmmm
						&& ModUtil.shouldSmooth(cache.getBlockState(pos.offset(facing)))
				) {
					continue;
				}

				final float[] quadBounds = new float[EnumFacing.VALUES.length * 2];

				//updateVertexBrightness(IBlockAccess worldIn, IBlockState state, BlockPos centerPos, EnumFacing direction, float[] faceShape, BitSet shapeState)
				aoFace.updateVertexBrightness(cache, state, pos, facing, quadBounds, bitset);

				final int lightmapCombinedLight0 = aoFace.vertexBrightness[0];
				final int lightmapCombinedLight1 = aoFace.vertexBrightness[1];
				final int lightmapCombinedLight2 = aoFace.vertexBrightness[2];
				final int lightmapCombinedLight3 = aoFace.vertexBrightness[3];

				final int lightmapSkyLight0 = ClientUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(lightmapCombinedLight0);
				final int lightmapSkyLight1 = ClientUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(lightmapCombinedLight1);
				final int lightmapSkyLight2 = ClientUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(lightmapCombinedLight2);
				final int lightmapSkyLight3 = ClientUtil.getLightmapSkyLightCoordsFromPackedLightmapCoords(lightmapCombinedLight3);

				final int lightmapBlockLight0 = ClientUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(lightmapCombinedLight0);
				final int lightmapBlockLight1 = ClientUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(lightmapCombinedLight1);
				final int lightmapBlockLight2 = ClientUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(lightmapCombinedLight2);
				final int lightmapBlockLight3 = ClientUtil.getLightmapBlockLightCoordsFromPackedLightmapCoords(lightmapCombinedLight3);

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

				float diffuse = net.minecraftforge.client.model.pipeline.LightUtil.diffuseLight(facing);

				final int color = ClientUtil.getColor(quad, textureState, cache, texturePos);
				final float red = (((color >> 16) & 255) / 255F) * diffuse;
				final float green = (((color >> 8) & 255) / 255F) * diffuse;
				final float blue = ((color & 255) / 255F) * diffuse;
				final float alpha = 1.0F;

				final float minU = ClientUtil.getMinU(sprite);
				final float minV = ClientUtil.getMinV(sprite);
				final float maxU = ClientUtil.getMaxU(sprite);
				final float maxV = ClientUtil.getMaxV(sprite);

				final Vec3 vertex0 = face.getVertex0().offset(x, y, z);
				final Vec3 vertex1 = face.getVertex1().offset(x, y, z);
				final Vec3 vertex2 = face.getVertex2().offset(x, y, z);
				final Vec3 vertex3 = face.getVertex3().offset(x, y, z);

				if (ModConfig.offsetVertices) {
					ModUtil.offsetVertex(vertex0);
					ModUtil.offsetVertex(vertex1);
					ModUtil.offsetVertex(vertex2);
					ModUtil.offsetVertex(vertex3);
				}

				bufferBuilder.pos(vertex0.x, vertex0.y, vertex0.z).color(red, green, blue, alpha).tex(minU, minV).lightmap(lightmapSkyLight0, lightmapBlockLight0).endVertex();
				bufferBuilder.pos(vertex1.x, vertex1.y, vertex1.z).color(red, green, blue, alpha).tex(minU, maxV).lightmap(lightmapSkyLight1, lightmapBlockLight1).endVertex();
				bufferBuilder.pos(vertex2.x, vertex2.y, vertex2.z).color(red, green, blue, alpha).tex(maxU, maxV).lightmap(lightmapSkyLight2, lightmapBlockLight2).endVertex();
				bufferBuilder.pos(vertex3.x, vertex3.y, vertex3.z).color(red, green, blue, alpha).tex(maxU, minV).lightmap(lightmapSkyLight3, lightmapBlockLight3).endVertex();

			}
		} finally {
			// This gets called right before return, don't worry
			// (unless theres a BIG error in the try, in which case
			// releasing the pooled pos is the least of our worries)
			pooledMutableBlockPos.release();
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

		final PooledMutableBlockPos pos = PooledMutableBlockPos.retain(rayTraceResult.getBlockPos());

		final int x = pos.getX();
		final int y = pos.getY();
		final int z = pos.getZ();

		final List<Vec3> vertices = ModConfig.debug.activeRenderingAlgorithm.getVertices(pos, player.world);
		if (vertices.isEmpty() || vertices.size() < 8) {
			pos.release();
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
		final Vec3 v0 = vertices.get(0).offset(x, y, z);
		final Vec3 v1 = vertices.get(1).offset(x, y, z);
		final Vec3 v2 = vertices.get(2).offset(x, y, z);
		final Vec3 v3 = vertices.get(3).offset(x, y, z);
		final Vec3 v4 = vertices.get(4).offset(x, y, z);
		final Vec3 v5 = vertices.get(5).offset(x, y, z);
		final Vec3 v6 = vertices.get(6).offset(x, y, z);
		final Vec3 v7 = vertices.get(7).offset(x, y, z);

		for (Vec3 vertex : new Vec3[]{v0, v1, v2, v3, v4, v5, v6, v7}) {
			if (ModConfig.offsetVertices)
				ModUtil.offsetVertex(vertex);
		}

		v0.move(renderX, renderY, renderZ).move(nOffset, nOffset, nOffset);
		v1.move(renderX, renderY, renderZ).move(pOffset, nOffset, nOffset);
		v2.move(renderX, renderY, renderZ).move(nOffset, nOffset, pOffset);
		v3.move(renderX, renderY, renderZ).move(pOffset, nOffset, pOffset);
		v4.move(renderX, renderY, renderZ).move(nOffset, pOffset, nOffset);
		v5.move(renderX, renderY, renderZ).move(pOffset, pOffset, nOffset);
		v6.move(renderX, renderY, renderZ).move(nOffset, pOffset, pOffset);
		v7.move(renderX, renderY, renderZ).move(pOffset, pOffset, pOffset);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		// yay, magic numbers!
		bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

		GlStateManager.color(1F, 1F, 1F, 1F);

		// bottom
		// assuming facing north (towards neg z)
		// 0 1
		// 3 2
		bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).endVertex();
		// top
		// assuming facing north (towards neg z)
		// 4 5
		// 7 6
		bufferBuilder.pos(v4.x, v4.y, v4.z).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v4.x, v4.y, v4.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v5.x, v5.y, v5.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v6.x, v6.y, v6.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v7.x, v7.y, v7.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v4.x, v4.y, v4.z).color(red, green, blue, alpha).endVertex();
		// north
		// assuming facing north (towards neg z) & up (we only draw 0-4 & 1-5 cause the others are already drawn)
		// 0 1
		// 4 5
		bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v4.x, v4.y, v4.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v5.x, v5.y, v5.z).color(red, green, blue, alpha).endVertex();
		// south
		// assuming facing north (towards neg z) & up (we only draw 3-2 & 7-6 cause the others are already drawn)
		// 3 2
		// 7 6
		bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v7.x, v7.y, v7.z).color(red, green, blue, invis).endVertex();
		bufferBuilder.pos(v7.x, v7.y, v7.z).color(red, green, blue, alpha).endVertex();
		bufferBuilder.pos(v6.x, v6.y, v6.z).color(red, green, blue, alpha).endVertex();

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

		pos.release();

		profiler.endSection();

	}

}
