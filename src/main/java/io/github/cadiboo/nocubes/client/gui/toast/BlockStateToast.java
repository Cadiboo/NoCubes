//package io.github.cadiboo.nocubes.client.gui.toast;
//
//import com.mojang.blaze3d.platform.GlStateManager;
//import io.github.cadiboo.nocubes.NoCubes;
//import io.github.cadiboo.nocubes.client.render.BufferBuilderCache;
//import net.minecraft.block.BlockRenderType;
//import net.minecraft.block.BlockState;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.toasts.IToast;
//import net.minecraft.client.gui.toasts.ToastGui;
//import net.minecraft.client.renderer.BlockModelRenderer;
//import net.minecraft.client.renderer.BlockRendererDispatcher;
//import net.minecraft.client.renderer.BufferBuilder;
//import net.minecraft.client.renderer.RenderHelper;
//import net.minecraft.client.renderer.Vector3f;
//import net.minecraft.client.renderer.model.ItemTransformVec3f;
//import net.minecraft.client.renderer.texture.AtlasTexture;
//import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
//import net.minecraft.client.renderer.vertex.VertexFormat;
//import net.minecraft.client.renderer.vertex.VertexFormatElement;
//import net.minecraft.client.resources.I18n;
//import net.minecraft.entity.Entity;
//import net.minecraft.util.BlockRenderLayer;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IWorldReader;
//import net.minecraftforge.client.ForgeHooksClient;
//import net.minecraftforge.common.model.TRSRTransformation;
//import org.lwjgl.opengl.GL11;
//
//import javax.annotation.Nonnull;
//import javax.vecmath.Matrix4f;
//import java.nio.ByteBuffer;
//import java.util.List;
//import java.util.Random;
//
//import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES;
//import static io.github.cadiboo.nocubes.client.ClientUtil.BLOCK_RENDER_LAYER_VALUES_LENGTH;
//
///**
// * @author Cadiboo
// */
//public abstract class BlockStateToast implements IToast {
//
//	private static final Matrix4f ITEM_CAMERA_TRANSFORM_MATRIX = TRSRTransformation.from(
//			new ItemTransformVec3f(
//					new Vector3f(-30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(0.625F, 0.625F, 0.625F)
//			)
//	).getMatrixVec();
//
//	@Nonnull
//	private final BufferBuilderCache bufferCache = new BufferBuilderCache(0x200, 0x200, 0x200, 0x200);
//	@Nonnull
//	private final boolean[] usedBlockRenderLayers = new boolean[BLOCK_RENDER_LAYER_VALUES_LENGTH];
//	@Nonnull
//	private final String name;
//
//	BlockStateToast(@Nonnull final BlockState state, @Nonnull final BlockPos pos) {
//		final Minecraft minecraft = Minecraft.getInstance();
//		this.name = state.getBlock().getNameTextComponent().getFormattedText();
//
//		this.build(state, pos, minecraft.world, minecraft.getBlockRendererDispatcher(), new Random());
//	}
//
//	/**
//	 * Copied from the Tessellator's vboUploader - Draw everything but don't reset the buffer
//	 */
//	private static void drawBuffer(@Nonnull final BufferBuilder bufferBuilderIn) {
//		if (bufferBuilderIn.getVertexCount() > 0) {
//			VertexFormat vertexformat = bufferBuilderIn.getVertexFormat();
//			int i = vertexformat.getSize();
//			ByteBuffer bytebuffer = bufferBuilderIn.getByteBuffer();
//			List<VertexFormatElement> list = vertexformat.getElements();
//
//			for (int j = 0; j < list.size(); ++j) {
//				VertexFormatElement vertexformatelement = list.get(j);
////				VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
////				int k = vertexformatelement.getType().getGlConstant();
////				int l = vertexformatelement.getIndex();
//				bytebuffer.position(vertexformat.getOffset(j));
//
//				// moved to VertexFormatElement.preDraw
//				vertexformatelement.getUsage().preDraw(vertexformat, j, i, bytebuffer);
//			}
//
//			GlStateManager.drawArrays(bufferBuilderIn.getDrawMode(), 0, bufferBuilderIn.getVertexCount());
//			int i1 = 0;
//
//			for (int j1 = list.size(); i1 < j1; ++i1) {
//				VertexFormatElement vertexformatelement1 = list.get(i1);
////				VertexFormatElement.EnumUsage vertexformatelement$enumusage1 = vertexformatelement1.getUsage();
////				int k1 = vertexformatelement1.getIndex();
//
//				// moved to VertexFormatElement.postDraw
//				vertexformatelement1.getUsage().postDraw(vertexformat, i1, i, bytebuffer);
//			}
//		}
//
//		// Do not reset buffer
////		bufferBuilderIn.reset();
//	}
//
//	private void build(
//			@Nonnull final BlockState state,
//			@Nonnull final BlockPos pos,
//			@Nonnull final IWorldReader reader,
//			@Nonnull final BlockRendererDispatcher blockRendererDispatcher,
//			@Nonnull final Random random
//	) {
//		if (state.getRenderType() != BlockRenderType.MODEL) {
//			return;
//		}
//
//		final boolean[] startedBufferBuilders = new boolean[BLOCK_RENDER_LAYER_VALUES_LENGTH];
//		final BlockModelRenderer blockModelRenderer = blockRendererDispatcher.getBlockModelRenderer();
//		{
//			for (int i = 0; i < BLOCK_RENDER_LAYER_VALUES_LENGTH; i++) {
//				final BlockRenderLayer blockRenderLayer = BLOCK_RENDER_LAYER_VALUES[i];
//				if (!state.getBlock().canRenderInLayer(state, blockRenderLayer)) {
//					continue;
//				}
//				ForgeHooksClient.setRenderLayer(blockRenderLayer);
//				final int blockRenderLayerId = blockRenderLayer.ordinal();
//				final BufferBuilder bufferBuilder = bufferCache.get(blockRenderLayerId);
//				if (!startedBufferBuilders[blockRenderLayerId]) {
//					startedBufferBuilders[blockRenderLayerId] = true;
//					// Copied from RenderChunk#preRenderBlocks
//					{
//						bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
//						bufferBuilder.setTranslation((-pos.getX()), (-pos.getY()), (-pos.getZ()));
//					}
//				}
//				// OptiFine Shaders compatibility
////				OptiFineCompatibility.pushShaderThing(state, pos, reader, bufferBuilder);
//				usedBlockRenderLayers[blockRenderLayerId] |= blockModelRenderer.renderModel(reader, blockRendererDispatcher.getModelForState(state), state, pos, bufferBuilder, false, random, state.getPositionRandom(pos));
////				OptiFineCompatibility.popShaderThing(bufferBuilder);
//			}
//			ForgeHooksClient.setRenderLayer(null);
//		}
//
//		// finishDrawing
//		for (int blockRenderLayerId = 0; blockRenderLayerId < usedBlockRenderLayers.length; blockRenderLayerId++) {
//			if (!startedBufferBuilders[blockRenderLayerId]) {
//				continue;
//			}
//			bufferCache.get(blockRenderLayerId).finishDrawing();
//		}
//	}
//
//	public abstract String getUpdateType();
//
//	@Nonnull
//	@Override
//	public Visibility draw(@Nonnull final ToastGui toastGui, final long delta) {
//		final Minecraft minecraft = toastGui.getMinecraft();
//		minecraft.getTextureManager().bindTexture(TEXTURE_TOASTS);
//		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//		toastGui.blit(0, 0, 0, 0, 160, 32);
//
//		minecraft.fontRenderer.drawString(I18n.format(getUpdateType()) + ":", 30, 7, 0xFFFFFFFF);
//		minecraft.fontRenderer.drawString(name, 30, 18, 0xFFFFFFFF);
//
//		// Code to draw the buffer
//		RENDER:
//		{
//			final Entity entity = minecraft.getRenderViewEntity();
//			if (entity == null) {
//				break RENDER;
//			}
//			{
//				minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//				minecraft.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
//
//				GlStateManager.pushMatrix();
//				GlStateManager.translatef(0, 0, 100);
//				GlStateManager.enableRescaleNormal();
//				GlStateManager.enableAlphaTest();
//				GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
//				GlStateManager.enableBlend();
//				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
//				GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//				{
//					GlStateManager.translatef(7, 21, 0);
//					GlStateManager.scalef(-1, -1, 1);
//					GlStateManager.scalef(20, 20, 20);
//				}
//				{
//					RenderHelper.enableGUIStandardItemLighting();
//				}
//				{
//					ForgeHooksClient.multiplyCurrentGlMatrix(ITEM_CAMERA_TRANSFORM_MATRIX);
//				}
//			}
//			for (int blockRenderLayerId = 0; blockRenderLayerId < usedBlockRenderLayers.length; blockRenderLayerId++) {
//				if (!usedBlockRenderLayers[blockRenderLayerId]) {
//					continue;
//				}
//				drawBuffer(bufferCache.get(blockRenderLayerId));
//			}
//			{
//				RenderHelper.disableStandardItemLighting();
//				GlStateManager.disableAlphaTest();
//				GlStateManager.disableRescaleNormal();
//				GlStateManager.popMatrix();
//
//				minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
//				minecraft.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
//			}
//		}
//
//		return delta >= 10000L ? Visibility.HIDE : Visibility.SHOW;
//	}
//
//	public static final class AddTerrain extends BlockStateToast {
//
//		public AddTerrain(@Nonnull final BlockState state, @Nonnull final BlockPos pos) {
//			super(state, pos);
//		}
//
//		@Override
//		public String getUpdateType() {
//			return NoCubes.MOD_ID + ".addedTerrainSmoothableBlockState";
//		}
//
//	}
//
//	public static final class RemoveTerrain extends BlockStateToast {
//
//		public RemoveTerrain(@Nonnull final BlockState state, @Nonnull final BlockPos pos) {
//			super(state, pos);
//		}
//
//		@Override
//		public String getUpdateType() {
//			return NoCubes.MOD_ID + ".removedTerrainSmoothableBlockState";
//		}
//
//	}
//
//	public static final class AddLeaves extends BlockStateToast {
//
//		public AddLeaves(@Nonnull final BlockState state, @Nonnull final BlockPos pos) {
//			super(state, pos);
//		}
//
//		@Override
//		public String getUpdateType() {
//			return NoCubes.MOD_ID + ".addedLeavesSmoothableBlockState";
//		}
//
//	}
//
//	public static final class RemoveLeaves extends BlockStateToast {
//
//		public RemoveLeaves(@Nonnull final BlockState state, @Nonnull final BlockPos pos) {
//			super(state, pos);
//		}
//
//		@Override
//		public String getUpdateType() {
//			return NoCubes.MOD_ID + ".removedLeavesSmoothableBlockState";
//		}
//
//	}
//
//}
