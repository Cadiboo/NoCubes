package io.github.cadiboo.nocubes.client.gui.toast;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.common.model.TransformationHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Toast for rendering a BlockState (All other toasts render an ItemStack).
 * Many hacks and lots of copy pasta code reside here.
 * Dragons abound.
 *
 * @author Cadiboo
 */
public class BlockStateToast implements IToast {

	// Copied from ForgeHooksClient#handlePerspective
	@SuppressWarnings("deprecation")
	private static final TransformationMatrix ITEM_CAMERA_TRANSFORMATION_MATRIX = TransformationHelper.toTransformation(
			new ItemTransformVec3f(
					// From the item/generated.json
					new Vector3f(-30, 225, 0),
					new Vector3f(0, 0, 0),
					new Vector3f(0.625F, 0.625F, 0.625F)
			)
	);

	private final SingleBlockBufferCache cache = new SingleBlockBufferCache();
	private final String message;
	private final String blockName;

	public BlockStateToast(final BlockState state, final BlockPos pos, final boolean smoothability, final String translationKeySuffix) {
		this(state, pos, (smoothability ? "added" : "removed") + translationKeySuffix);
	}

	public BlockStateToast(final BlockState state, final BlockPos pos, final String translationKey) {
		this.message = I18n.format(translationKey);
		this.blockName = state.getBlock().getNameTextComponent().getFormattedText();
		this.build(state, pos);
	}

	/**
	 * Draws a BufferBuilder without resetting its internal data.
	 *
	 * @param bufferBuilder The BufferBuilder to draw (but not reset)
	 */
	private static void drawBufferWithoutResetting(final BufferBuilder bufferBuilder) {
		// Get the internal data from the BufferBuilder (This resets the BufferBuilder's own copy of this data)
		final Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.getAndResetData();
		final ByteBuffer byteBuffer = pair.getSecond();

		// Set the BufferBuilder's internal data to the original data (it was reset by getAndResetData)
		bufferBuilder.putBulkData(byteBuffer);
		// getAndResetData clears the list of DrawStates. We need to repopulate this list.
		// finishDrawing repopulates the list. It throws an exception if the buffer hasn't been started so we start it.
		bufferBuilder.begin(pair.getFirst().getDrawMode(), bufferBuilder.getVertexFormat());
		bufferBuilder.finishDrawing();

		// Draw the BufferBuilder
		WorldVertexBufferUploader.draw(bufferBuilder);

		// Set the BufferBuilder's internal data back to the original data (it was reset by WorldVertexBufferUploader.draw)
		bufferBuilder.putBulkData(byteBuffer);
		// WorldVertexBufferUploader.draw clears the list of DrawStates. We need to repopulate this list.
		// finishDrawing repopulates the list. It throws an exception if the buffer hasn't been started so we start it.
		bufferBuilder.begin(pair.getFirst().getDrawMode(), bufferBuilder.getVertexFormat());
		bufferBuilder.finishDrawing();
	}

	private void build(final BlockState blockState, final BlockPos pos) {
		if (blockState.getRenderType() == BlockRenderType.INVISIBLE)
			return;

		final Minecraft minecraft = Minecraft.getInstance();
		final ClientWorld world = minecraft.world;
		// Coppied from net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.ChunkRender.RebuildTask.func_228940_a_
		// (Previously ChunkRender#rebuildChunk)
		final MatrixStack matrixStack = new MatrixStack();
		final Random random = new Random();
		final BlockRendererDispatcher blockRendererDispatcher = minecraft.getBlockRendererDispatcher();

		for (final RenderType renderType : RenderType.getBlockRenderTypes()) {
			ForgeHooksClient.setRenderLayer(renderType);
			if (RenderTypeLookup.canRenderInLayer(blockState, renderType)) {
				BufferBuilder bufferBuilder = cache.getBuffer(renderType);
				cache.startBuffer(renderType);
				bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

				matrixStack.push();
				if (blockRendererDispatcher.renderModel(blockState, pos, world, matrixStack, bufferBuilder, false, random)) {
					cache.empty = false;
					cache.useBuffer(renderType);
				}
				matrixStack.pop();
			}
		}
		ForgeHooksClient.setRenderLayer(null);
		for (RenderType renderType : cache.getStartedTypes())
			cache.getBuffer(renderType).finishDrawing();
	}

	@Nonnull
	@Override
	public Visibility draw(@Nonnull final ToastGui toastGui, final long delta) {
		final Minecraft minecraft = toastGui.getMinecraft();
		minecraft.getTextureManager().bindTexture(TEXTURE_TOASTS);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		toastGui.blit(0, 0, 0, 0, 160, 32);

		minecraft.fontRenderer.drawString(message, 30, 7, 0xFFFFFFFF);
		minecraft.fontRenderer.drawString(blockName, 30, 18, 0xFFFFFFFF);

		// Code to draw the buffer
		{
			// Setup - Mostly copied from ItemRenderer.renderItemAndEffectIntoGUI
			{
				minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
				minecraft.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);

				RenderSystem.pushMatrix();
				RenderSystem.translatef(0, 0, 100);
				RenderSystem.enableRescaleNormal();
				RenderSystem.enableAlphaTest();
				RenderSystem.alphaFunc(GL11.GL_GREATER, 0.1F);
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				{
//					RenderSystem.translatef(7, 21, 0);
//					RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
//					RenderSystem.scalef(20, 20, 20);
//					RenderSystem.rotatef(180, 0, 1, 0);
				}
				{
					RenderHelper.enableStandardItemLighting();
				}
				{
//					ForgeHooksClient.multiplyCurrentGlMatrix(ITEM_CAMERA_TRANSFORMATION_MATRIX);
					RenderSystem.multMatrix(ITEM_CAMERA_TRANSFORMATION_MATRIX.getMatrix());
				}
			}

			for (RenderType renderType : cache.getUsedTypes())
				drawBufferWithoutResetting(cache.getBuffer(renderType));

			// Cleanup - Mostly copied from ItemRenderer.renderItemAndEffectIntoGUI
			{
				RenderHelper.disableStandardItemLighting();
				RenderSystem.disableAlphaTest();
				RenderSystem.disableRescaleNormal();
				RenderSystem.popMatrix();

				minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
				minecraft.getTextureManager().getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
			}
		}

		return delta >= 100_000L ? Visibility.HIDE : Visibility.SHOW;
	}

	/**
	 * Copy and combination of {@link RegionRenderCacheBuilder} and {@link ChunkRenderDispatcher.CompiledChunk}
	 */
	public static class SingleBlockBufferCache {

		private final Set<RenderType> usedTypes = new ObjectArraySet<>();
		private final Set<RenderType> startedTypes = new ObjectArraySet<>();
		// Smallest default size because we're rendering one block and it won't render in most layers
		private final Map<RenderType, BufferBuilder> builders = RenderType.getBlockRenderTypes().stream()
				.collect(Collectors.toMap(Function.identity(), $ -> new BufferBuilder(DefaultVertexFormats.BLOCK.getSize())));
		public boolean empty = true;

		public Set<RenderType> getUsedTypes() {
			return usedTypes;
		}

		public Set<RenderType> getStartedTypes() {
			return startedTypes;
		}

		public BufferBuilder getBuffer(RenderType renderType) {
			return this.builders.get(renderType);
		}

		public void useBuffer(RenderType renderType) {
			this.usedTypes.add(renderType);
		}

		public void startBuffer(final RenderType renderType) {
			this.startedTypes.add(renderType);
		}

	}

	public static class Terrain extends BlockStateToast {

		public Terrain(final BlockState state, final boolean smoothability) {
			this(state, BlockPos.ZERO, smoothability);
		}

		public Terrain(final BlockState state, final BlockPos pos, final boolean smoothability) {
			super(state, pos, smoothability, "TerrainSmoothableBlockState");
		}

	}

	public static class Leaves extends BlockStateToast {

		public Leaves(final BlockState state, final boolean smoothability) {
			this(state, BlockPos.ZERO, smoothability);
		}

		public Leaves(final BlockState state, final BlockPos pos, final boolean smoothability) {
			super(state, pos, smoothability, "LeavesSmoothableBlockState");
		}

	}

}
