package io.github.cadiboo.nocubes.client.gui.toast;

import io.github.cadiboo.nocubes.client.ClientProxy;
import io.github.cadiboo.nocubes.client.ModelHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.LightUtil;

import javax.annotation.Nonnull;
import java.util.List;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static org.lwjgl.opengl.GL11.GL_QUADS;

/**
 * @author Cadiboo
 */
public abstract class BlockStateToast implements IToast {

	@Nonnull
	private final IBlockState state;
	@Nonnull
	private final BlockPos pos;

	BlockStateToast(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
		this.state = state;
		this.pos = pos;
	}

	public abstract String getUpdateType();

	@Nonnull
	@Override
	public Visibility draw(@Nonnull final GuiToast toastGui, final long delta) {
		final Minecraft minecraft = toastGui.getMinecraft();
		minecraft.getTextureManager().bindTexture(TEXTURE_TOASTS);
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		toastGui.drawTexturedModalRect(0, 0, 0, 0, 160, 32);

		toastGui.getMinecraft().fontRenderer.drawString(I18n.format(getUpdateType()) + ":", 30, 7, 0xFFFFFFFF);
		toastGui.getMinecraft().fontRenderer.drawString(state.getBlock().getLocalizedName(), 30, 18, 0xFFFFFFFF);

		RenderHelper.enableGUIStandardItemLighting();

		GlStateManager.pushMatrix();
		{
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			minecraft.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			{

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferBuilder = tessellator.getBuffer();
				bufferBuilder.begin(GL_QUADS, DefaultVertexFormats.BLOCK);

				switch (state.getRenderType()) {
					case MODEL:
						final IBakedModel model = minecraft.getBlockRendererDispatcher().getModelForState(state);

						for (EnumFacing enumfacing : ModelHelper.ENUMFACING_QUADS_ORDERED) {
							List<BakedQuad> list = model.getQuads(state, enumfacing, MathHelper.getPositionRandom(pos));

							if (!list.isEmpty()) {

								Vec3d vec3d = state.getOffset(minecraft.world, pos);
								double d0 = (double) pos.getX() + vec3d.x;
								double d1 = (double) pos.getY() + vec3d.y;
								double d2 = (double) pos.getZ() + vec3d.z;
								int i = 0;

								for (int j = list.size(); i < j; ++i) {
									BakedQuad bakedquad = list.get(i);

									bufferBuilder.addVertexData(bakedquad.getVertexData());
									bufferBuilder.putBrightness4(0xF000F0, 0xF000F0, 0xF000F0, 0xF000F0);

									if (bakedquad.hasTintIndex()) {
										int k = minecraft.getBlockColors().colorMultiplier(state, minecraft.world, pos, bakedquad.getTintIndex());

										if (EntityRenderer.anaglyphEnable) {
											k = TextureUtil.anaglyphColor(k);
										}

										float f = (float) (k >> 16 & 255) / 255.0F;
										float f1 = (float) (k >> 8 & 255) / 255.0F;
										float f2 = (float) (k & 255) / 255.0F;
										if (bakedquad.shouldApplyDiffuseLighting()) {
											float diffuse = LightUtil.diffuseLight(bakedquad.getFace());
											f *= diffuse;
											f1 *= diffuse;
											f2 *= diffuse;
										}
										bufferBuilder.putColorMultiplier(f, f1, f2, 4);
										bufferBuilder.putColorMultiplier(f, f1, f2, 3);
										bufferBuilder.putColorMultiplier(f, f1, f2, 2);
										bufferBuilder.putColorMultiplier(f, f1, f2, 1);
									} else if (bakedquad.shouldApplyDiffuseLighting()) {
										float diffuse = LightUtil.diffuseLight(bakedquad.getFace());
										bufferBuilder.putColorMultiplier(diffuse, diffuse, diffuse, 4);
										bufferBuilder.putColorMultiplier(diffuse, diffuse, diffuse, 3);
										bufferBuilder.putColorMultiplier(diffuse, diffuse, diffuse, 2);
										bufferBuilder.putColorMultiplier(diffuse, diffuse, diffuse, 1);
									}

									bufferBuilder.putPosition(d0, d1, d2);
								}

							}
						}

						break;
					default:
					case ENTITYBLOCK_ANIMATED:
						break;
					case LIQUID:
						ClientProxy.fluidRenderer.renderFluid(minecraft.world, state, pos, bufferBuilder);
						break;
				}

				tessellator.draw();
			}

			GlStateManager.disableAlpha();
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableLighting();
			minecraft.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			minecraft.getTextureManager().getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
		}
		GlStateManager.popMatrix();

		RenderHelper.disableStandardItemLighting();

		return delta >= 100000L ? Visibility.HIDE : Visibility.SHOW;
	}

	public static class Add extends BlockStateToast {

		public Add(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
			super(state, pos);
		}

		@Override
		public String getUpdateType() {
			return MOD_ID + ".addedSmoothableBlockState";
		}

	}

	public static class Remove extends BlockStateToast {

		public Remove(@Nonnull final IBlockState state, @Nonnull final BlockPos pos) {
			super(state, pos);
		}

		@Override
		public String getUpdateType() {
			return MOD_ID + ".removedSmoothableBlockState";
		}

	}

}
