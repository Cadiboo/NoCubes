package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.gui.toast.BlockStateToast;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.Config.ConfigHelper;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;

import java.text.DecimalFormat;
import java.util.List;

import static io.github.cadiboo.nocubes.util.IIsSmoothable.TERRAIN_SMOOTHABLE;
import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;
import static net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import static net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = NoCubes.MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	private static boolean hasSetSmoothLightingAndFancyGraphics = false;

	@SubscribeEvent
	public static void onClientTickEvent(final ClientTickEvent event) {

		if (event.phase != TickEvent.Phase.END) return;

//		if (false)
//		ObjectPoolingProfiler.onTick();

		final boolean toggleEnabledPressed = ClientProxy.toggleEnabled.isPressed();
		final boolean toggleTerrainSmoothableBlockStatePressed = ClientProxy.toggleTerrainSmoothableBlockState.isPressed();
		final boolean toggleLeavesSmoothableBlockStatePressed = ClientProxy.toggleLeavesSmoothableBlockState.isPressed();
		final boolean toggleProfilersPressed = ClientProxy.toggleProfilers.isPressed();

		if (ClientProxy.tempToggleCollisions.isPressed()) {
			if (!Config.terrainCollisions) {
				Minecraft.getInstance().player.sendMessage(new TextComponentTranslation(NoCubes.MOD_ID + ".collisionsEnabledWarning"));
				Minecraft.getInstance().player.sendMessage(new TextComponentTranslation(NoCubes.MOD_ID + ".collisionsDisablePress", new TextComponentTranslation(ClientProxy.tempToggleCollisions.getKey().getTranslationKey())));
			} else {
				Minecraft.getInstance().player.sendMessage(new TextComponentTranslation(NoCubes.MOD_ID + ".collisionsDisabled"));
			}
			Config.terrainCollisions = !Config.terrainCollisions;
		}

		if (toggleEnabledPressed || toggleTerrainSmoothableBlockStatePressed || toggleLeavesSmoothableBlockStatePressed || toggleProfilersPressed) {
			if (toggleEnabledPressed) {
				NoCubes.setEnabled(!NoCubes.isEnabled());
//				fireConfigChangedEvent();
				ClientUtil.tryReloadRenderers();
				return;
			}
			if (toggleTerrainSmoothableBlockStatePressed) {
				final Minecraft minecraft = Minecraft.getInstance();
				final RayTraceResult objectMouseOver = minecraft.objectMouseOver;
				if (objectMouseOver.type == BLOCK) {
					BlockPos blockPos = objectMouseOver.getBlockPos();
					final IBlockState state = minecraft.world.getBlockState(blockPos);

					final BlockStateToast toast;
					if (!state.nocubes_isTerrainSmoothable()) {
						ConfigHelper.addTerrainSmoothable(state);
						toast = new BlockStateToast.AddTerrain(state, blockPos);
					} else {
						ConfigHelper.removeTerrainSmoothable(state);
						toast = new BlockStateToast.RemoveTerrain(state, blockPos);
					}
					minecraft.getToastGui().add(toast);

					if (NoCubes.isEnabled()) {
						ClientUtil.tryReloadRenderers();
					}
					return;
				}
			}
			if (toggleLeavesSmoothableBlockStatePressed) {
				final Minecraft minecraft = Minecraft.getInstance();
				final RayTraceResult objectMouseOver = minecraft.objectMouseOver;
				if (objectMouseOver.type == BLOCK) {
					BlockPos blockPos = objectMouseOver.getBlockPos();
					final IBlockState state = minecraft.world.getBlockState(blockPos);

					final BlockStateToast toast;
					if (!state.nocubes_isLeavesSmoothable()) {
						ConfigHelper.addLeavesSmoothable(state);
						toast = new BlockStateToast.AddLeaves(state, blockPos);
					} else {
						ConfigHelper.removeLeavesSmoothable(state);
						toast = new BlockStateToast.RemoveLeaves(state, blockPos);
					}
					minecraft.getToastGui().add(toast);

					if (NoCubes.isEnabled()) {
						ClientUtil.tryReloadRenderers();
					}
					return;
				}
			}
			if (toggleProfilersPressed) {
				synchronized (ModProfiler.PROFILERS) {
					if (ModProfiler.profilersEnabled) {
						ModProfiler.disableProfiling();
					} else {
						ModProfiler.enableProfiling();
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onRenderTickEvent(final RenderTickEvent event) {

		//This is here because the RenderTickEvent is pretty much the first event to fire as soon as gameSettings saving is re-enabled
		if (!hasSetSmoothLightingAndFancyGraphics) {
			hasSetSmoothLightingAndFancyGraphics = true;
			try {
				final GameSettings gameSettings = Minecraft.getInstance().gameSettings;
				boolean needsResave = false;
				if (gameSettings.ambientOcclusion < 1) {
					NoCubes.LOGGER.info("Smooth lighting was off. EW! Just set it to MINIMAL");
					gameSettings.ambientOcclusion = 1;
					needsResave = true;
				}
				if (!gameSettings.fancyGraphics) {
					NoCubes.LOGGER.info("Fancy graphics were off. Ew, who plays with black leaves??? Just turned it on");
					gameSettings.fancyGraphics = true;
					needsResave = true;
				}
				if (needsResave) {
					gameSettings.saveOptions();
				}
			} catch (Exception e) {
				//go away idc about u
			}
		}

		if (!ModProfiler.profilersEnabled) {
			return;
		}

		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.world == null || minecraft.player == null || minecraft.getRenderViewEntity() == null) {
			return;
		}

		minecraft.profiler.startSection("debugNoCubes");
		GlStateManager.pushMatrix();
		try {
			renderProfilers();
		} catch (Exception e) {
			LogManager.getLogger("NoCubes Profile Renderer").error("Error Rendering Profilers.", e);
		}
		GlStateManager.popMatrix();
		minecraft.profiler.endSection();
	}

	protected static void renderProfilers() {
		final Minecraft mc = Minecraft.getInstance();

		synchronized (ModProfiler.PROFILERS) {
			for (int profilerIndex = 0; profilerIndex < ModProfiler.PROFILERS.size(); profilerIndex++) {
				final ModProfiler profiler = ModProfiler.PROFILERS.get(profilerIndex);
				List<Profiler.Result> list = profiler.getProfilingData("");
				Profiler.Result profiler$result = list.remove(0);
				GlStateManager.clear(256);
				GlStateManager.matrixMode(5889);
				GlStateManager.enableColorMaterial();
				GlStateManager.loadIdentity();
				GlStateManager.ortho(0.0D, (double) mc.mainWindow.getFramebufferWidth(), (double) mc.mainWindow.getFramebufferHeight(), 0.0D, 1000.0D, 3000.0D);
				GlStateManager.matrixMode(5888);
				GlStateManager.loadIdentity();
				GlStateManager.scalef(mc.mainWindow.getFramebufferWidth() / 1000F, mc.mainWindow.getFramebufferWidth() / 1000F, 1);
				GlStateManager.translatef(5F, 5F, 0F);
				GlStateManager.translatef(0.0F, 0.0F, -2000.0F);
				GlStateManager.lineWidth(1.0F);
				GlStateManager.disableTexture2D();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();
//				int i = 160;
//				int j = this.displayWidth - 160 - 10;
//				int k = this.displayHeight - 320;
//				int j = mc.displayWidth - (profilerIndex % 2) * 160;
//				int k = mc.displayHeight - (profilerIndex & 2) * 320;
//				final int cx = 176 + (profilerIndex) * 50;
//				final int cy = 80 + (profilerIndex & 2) * 320;
				final int cx = 160 + 320 * (profilerIndex % 3);
				final int cy = 80 + 320 * (profilerIndex / 3);
				GlStateManager.enableBlend();
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
				bufferbuilder.pos((double) ((float) cx - 176.0F), (double) ((float) cy - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
				bufferbuilder.pos((double) ((float) cx - 176.0F), (double) (cy + 320), 0.0D).color(200, 0, 0, 0).endVertex();
				bufferbuilder.pos((double) ((float) cx + 176.0F), (double) (cy + 320), 0.0D).color(200, 0, 0, 0).endVertex();
				bufferbuilder.pos((double) ((float) cx + 176.0F), (double) ((float) cy - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
				tessellator.draw();
				GlStateManager.disableBlend();
				double d0 = 0.0D;

				for (int l = 0; l < list.size(); ++l) {
					Profiler.Result profiler$result1 = list.get(l);
					int i11 = MathHelper.floor(profiler$result1.usePercentage / 4.0D) + 1;
					bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
					int j1 = profiler$result1.getColor();
					int k1 = j1 >> 16 & 255;
					int l1 = j1 >> 8 & 255;
					int i2 = j1 & 255;
					bufferbuilder.pos((double) cx, (double) cy, 0.0D).color(k1, l1, i2, 255).endVertex();

					for (int j2 = i11; j2 >= 0; --j2) {
						float f = (float) ((d0 + profiler$result1.usePercentage * (double) j2 / (double) i11) * (Math.PI * 2D) / 100.0D);
						float f1 = MathHelper.sin(f) * 160.0F;
						float f2 = MathHelper.cos(f) * 160.0F * 0.5F;
						bufferbuilder.pos((double) ((float) cx + f1), (double) ((float) cy - f2), 0.0D).color(k1, l1, i2, 255).endVertex();
					}

					tessellator.draw();
					bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

					for (int i3 = i11; i3 >= 0; --i3) {
						float f3 = (float) ((d0 + profiler$result1.usePercentage * (double) i3 / (double) i11) * (Math.PI * 2D) / 100.0D);
						float f4 = MathHelper.sin(f3) * 160.0F;
						float f5 = MathHelper.cos(f3) * 160.0F * 0.5F;
						bufferbuilder.pos((double) ((float) cx + f4), (double) ((float) cy - f5), 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
						bufferbuilder.pos((double) ((float) cx + f4), (double) ((float) cy - f5 + 10.0F), 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
					}

					tessellator.draw();
					d0 += profiler$result1.usePercentage;
				}

				DecimalFormat decimalformat = new DecimalFormat("##0.00");
				GlStateManager.enableTexture2D();
				String s11 = "";

				if (!"unspecified".equals(profiler$result.profilerName)) {
					s11 = s11 + "[0] ";
				}

				if (profiler$result.profilerName.isEmpty()) {
					s11 = s11 + "ROOT ";
				} else {
					s11 = s11 + profiler$result.profilerName + ' ';
				}

//			int l2 = 16777215;
				mc.fontRenderer.drawStringWithShadow(s11, (float) (cx - 160), (float) (cy - 80 - 16), 16777215);
				s11 = decimalformat.format(profiler$result.totalUsePercentage) + "%";
				mc.fontRenderer.drawStringWithShadow(s11, (float) (cx + 160 - mc.fontRenderer.getStringWidth(s11)), (float) (cy - 80 - 16), 16777215);

				for (int k2 = 0; k2 < list.size(); ++k2) {
					Profiler.Result profiler$result2 = list.get(k2);
					StringBuilder stringbuilder = new StringBuilder();

					if ("unspecified".equals(profiler$result2.profilerName)) {
						stringbuilder.append("[?] ");
					} else {
						stringbuilder.append("[").append(k2 + 1).append("] ");
					}

					String s1 = stringbuilder.append(profiler$result2.profilerName).toString();
					mc.fontRenderer.drawStringWithShadow(s1, (float) (cx - 160), (float) (cy + 80 + k2 * 8 + 20), profiler$result2.getColor());
					s1 = decimalformat.format(profiler$result2.usePercentage) + "%";
					mc.fontRenderer.drawStringWithShadow(s1, (float) (cx + 160 - 50 - mc.fontRenderer.getStringWidth(s1)), (float) (cy + 80 + k2 * 8 + 20), profiler$result2.getColor());
					s1 = decimalformat.format(profiler$result2.totalUsePercentage) + "%";
					mc.fontRenderer.drawStringWithShadow(s1, (float) (cx + 160 - mc.fontRenderer.getStringWidth(s1)), (float) (cy + 80 + k2 * 8 + 20), profiler$result2.getColor());
				}
			}
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {

		final EntityPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}

////		if (ModConfig.collisionsBlockHighlighting) {
//		if (!ModConfig.drawCollisionsCache) {
//			return;
//		}
//
//		final float partialTicks = event.getPartialTicks();
//
//		final double renderX = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
//		final double renderY = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
//		final double renderZ = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);
//
//		GlStateManager.enableBlend();
//		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//		GlStateManager.glLineWidth(2.0F);
//		GlStateManager.disableTexture2D();
//		GlStateManager.depthMask(false);
//		GlStateManager.disableDepth();
//
//		GlStateManager.color(0, 0, 0, 0);
//		GlStateManager.color(1, 1, 1, 1);
//
//		if (ModConfig.drawCollisionsCache) {
//			final Tessellator tessellator = Tessellator.getInstance();
//			final BufferBuilder bufferbuilder = tessellator.getBuffer();
//
//			bufferbuilder.setTranslation(-renderX, -renderY, -renderZ);
//
//			GlStateManager.enableBlend();
//			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//			GlStateManager.glLineWidth(3.0F);
//			GlStateManager.disableTexture2D();
//			GlStateManager.depthMask(false);
//
//			GlStateManager.color(0, 0, 0, 0);
//			GlStateManager.color(1, 1, 1, 1);
//
//			synchronized (CACHE) {
//				for (final CollisionsCache cache : CACHE.values()) {
//
//					synchronized (cache.faces) {
//						for (final Face face : cache.faces) {
//							try {
//
//								final Vec3 v0 = face.getVertex0();
//								final Vec3 v1 = face.getVertex1();
//								final Vec3 v2 = face.getVertex2();
//								final Vec3 v3 = face.getVertex3();
//								try {
//									bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
//
//									bufferbuilder.pos(v0.x, v0.y, v0.z).color(1, 1, 1, 0.4F).endVertex();
//									bufferbuilder.pos(v1.x, v1.y, v1.z).color(1, 1, 1, 0.4F).endVertex();
//									bufferbuilder.pos(v2.x, v2.y, v2.z).color(1, 1, 1, 0.4F).endVertex();
//									bufferbuilder.pos(v3.x, v3.y, v3.z).color(1, 1, 1, 0.4F).endVertex();
//									bufferbuilder.pos(v0.x, v0.y, v0.z).color(1, 1, 1, 0.4F).endVertex();
//
//									tessellator.draw();
//								} finally {
////							v0.close();
////							v1.close();
////							v2.close();
////							v3.close();
//								}
//							} finally {
////						face.close();
//							}
//
//						}
//					}
//
//				}
//			}
//			GlStateManager.enableDepth();
//
//			GlStateManager.depthMask(true);
//			GlStateManager.enableTexture2D();
//			GlStateManager.disableBlend();
//
//			bufferbuilder.setTranslation(0, 0, 0);
//		}
//
//		GlStateManager.color(0, 0, 0, 0);
//		GlStateManager.color(1, 1, 1, 1);

	}

	@SubscribeEvent
	public static void drawBlockHighlightEvent(final DrawBlockHighlightEvent event) {

		if (!NoCubes.isEnabled() || !Config.renderSmoothTerrain) {
			return;
		}

		final EntityPlayer player = event.getPlayer();
		if (player == null) {
			return;
		}

		final RayTraceResult rayTraceResult = event.getTarget();
		if ((rayTraceResult == null) || (rayTraceResult.type != RayTraceResult.Type.BLOCK)) {
			return;
		}

		final World world = player.world;
		if (world == null) {
			return;
		}

		final float partialTicks = event.getPartialTicks();
		final BlockPos pos = rayTraceResult.getBlockPos();
		final IBlockState blockState = world.getBlockState(pos);
		if ((blockState.getMaterial() == Material.AIR) || !world.getWorldBorder().contains(pos)) {
			return;
		}

		if (blockState.nocubes_isTerrainSmoothable()) {
			event.setCanceled(true);
		} else {
			return;
		}

		try (FaceList faces = MeshDispatcher.generateBlockMeshOffset(rayTraceResult.getBlockPos(), world, TERRAIN_SMOOTHABLE, Config.terrainMeshGenerator)) {
			final double renderX = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
			final double renderY = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
			final double renderZ = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);

			final Tessellator tessellator = Tessellator.getInstance();
			final BufferBuilder bufferbuilder = tessellator.getBuffer();

			bufferbuilder.setTranslation(-renderX, -renderY, -renderZ);

			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.lineWidth(3.0F);
			GlStateManager.disableTexture2D();
			GlStateManager.depthMask(false);

			GlStateManager.color4f(0, 0, 0, 1);
			GlStateManager.color4f(1, 1, 1, 1);

			for (final Face face : faces) {
				try {
					try (
							Vec3 v0 = face.getVertex0();
							Vec3 v1 = face.getVertex1();
							Vec3 v2 = face.getVertex2();
							Vec3 v3 = face.getVertex3()
					) {
						bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

						bufferbuilder.pos(v0.x, v0.y, v0.z).color(0, 0, 0, 0.4F).endVertex();
						bufferbuilder.pos(v1.x, v1.y, v1.z).color(0, 0, 0, 0.4F).endVertex();
						bufferbuilder.pos(v2.x, v2.y, v2.z).color(0, 0, 0, 0.4F).endVertex();
						bufferbuilder.pos(v3.x, v3.y, v3.z).color(0, 0, 0, 0.4F).endVertex();
						bufferbuilder.pos(v0.x, v0.y, v0.z).color(0, 0, 0, 0.4F).endVertex();

						tessellator.draw();
					}
				} finally {
					face.close();
				}
			}

			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();

			bufferbuilder.setTranslation(0, 0, 0);

		}

//		if (ModConfig.smoothBlockHighlighting || ModConfig.collisionsBlockHighlighting) {
//			final EntityPlayer player = event.getPlayer();
//			if (player == null) {
//				return;
//			}
//
//			final RayTraceResult rayTraceResult = event.getTarget();
//			if ((rayTraceResult == null) || (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK)) {
//				return;
//			}
//
//			final World world = player.world;
//			if (world == null) {
//				return;
//			}
//
//			final float partialTicks = event.getPartialTicks();
//			final BlockPos pos = rayTraceResult.getBlockPos();
//			final IBlockState blockState = world.getBlockState(pos);
//			if ((blockState.getMaterial() == Material.AIR) || !world.getWorldBorder().contains(pos)) {
//				return;
//			}
//
//			final double renderX = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
//			final double renderY = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
//			final double renderZ = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);
//
//			if (ModConfig.collisionsBlockHighlighting) {
//				GlStateManager.enableBlend();
//				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//				GlStateManager.glLineWidth(2.0F);
//				GlStateManager.disableTexture2D();
//				GlStateManager.depthMask(false);
//				GlStateManager.disableDepth();
//
//				GlStateManager.color(0, 0, 0, 0);
//				GlStateManager.color(1, 1, 1, 1);
//
//				{
////			    final AxisAlignedBB oldSelectedBox = blockState.getSelectedBoundingBox(world, pos);
//
//					final List<AxisAlignedBB> boxes = new ArrayList<>();
//
//					blockState.addCollisionBoxToList(world, pos, new AxisAlignedBB(pos), boxes, player, false);
//
////				if (boxes.size() <= 1) {
////					boxes.clear();
////					boxes.add(oldSelectedBox);
////				}
//
//					for (AxisAlignedBB box : boxes) {
//
//						final AxisAlignedBB renderBox = box.grow(0.0020000000949949026D).offset(-renderX, -renderY, -renderZ);
//
//						RenderGlobal.drawSelectionBoundingBox(renderBox, 0.0F, 1.0F, 1.0F, 0.4F);
//					}
//
//				}
//
//				{
//
//					for (AxisAlignedBB box : world.getCollisionBoxes(player, player.getEntityBoundingBox())) {
//
//						final AxisAlignedBB renderBox = box.grow(0.0020000000949949026D).offset(-renderX, -renderY, -renderZ);
//
//						RenderGlobal.drawSelectionBoundingBox(renderBox, 1.0F, 0.0F, 0.0F, 0.4F);
//					}
//
//				}
//
//				GlStateManager.enableDepth();
//
//				GlStateManager.depthMask(true);
//				GlStateManager.enableTexture2D();
//				GlStateManager.disableBlend();
//			}
//
//			if (ModConfig.smoothBlockHighlighting) {
//
//				final FaceList faces = NoCubes.MESH_DISPATCHER.generateBlockMeshOffset(rayTraceResult.getBlockPos(), world, ModUtil.TERRAIN_SMOOTHABLE, ModConfig.terrainMeshGenerator);
//
//				final Tessellator tessellator = Tessellator.getInstance();
//				final BufferBuilder bufferbuilder = tessellator.getBuffer();
//
//				bufferbuilder.setTranslation(-renderX, -renderY, -renderZ);
//
//				GlStateManager.enableBlend();
//				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
//				GlStateManager.glLineWidth(3.0F);
//				GlStateManager.disableTexture2D();
//				GlStateManager.depthMask(false);
//
//				GlStateManager.color(0, 0, 0, 0);
//				GlStateManager.color(1, 1, 1, 1);
//
//				for (final Face face : faces) {
//					try {
//
//						final Vec3 v0 = face.getVertex0();
//						final Vec3 v1 = face.getVertex1();
//						final Vec3 v2 = face.getVertex2();
//						final Vec3 v3 = face.getVertex3();
//						try {
//							bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
//
//							bufferbuilder.pos(v0.x, v0.y, v0.z).color(1, 1, 1, 0.4F).endVertex();
//							bufferbuilder.pos(v1.x, v1.y, v1.z).color(1, 1, 1, 0.4F).endVertex();
//							bufferbuilder.pos(v2.x, v2.y, v2.z).color(1, 1, 1, 0.4F).endVertex();
//							bufferbuilder.pos(v3.x, v3.y, v3.z).color(1, 1, 1, 0.4F).endVertex();
//							bufferbuilder.pos(v0.x, v0.y, v0.z).color(1, 1, 1, 0.4F).endVertex();
//
//							tessellator.draw();
//						} finally {
//							v0.close();
//							v1.close();
//							v2.close();
//							v3.close();
//						}
//					} finally {
//						face.close();
//					}
//
//				}
//
//				faces.close();
//
//				GlStateManager.depthMask(true);
//				GlStateManager.enableTexture2D();
//				GlStateManager.disableBlend();
//
//				bufferbuilder.setTranslation(0, 0, 0);
//
//			}
//
//		}
	}

	@SubscribeEvent
	public static void onPlayerSPPushOutOfBlocksEvent(final PlayerSPPushOutOfBlocksEvent event) {
		if (Config.terrainCollisions) {
			event.setCanceled(true);
		}
	}

}
