package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.client.render.MarchingCubesRenderer;
import io.github.cadiboo.nocubes.client.render.SmoothLightingFluidBlockRenderer;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.future.ConfigTracker;
import io.github.cadiboo.nocubes.mesh.SurfaceNets;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.util.ColorParser;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.Vec;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.handshake.NetworkDispatcher;
import org.apache.logging.log4j.LogManager;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static io.github.cadiboo.nocubes.util.IsSmoothable.LEAVES;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN;
import static net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import static net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	public static SmoothLightingFluidBlockRenderer smoothLightingBlockFluidRenderer;

	@SubscribeEvent
	public static void onClientTickEvent(final ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;

		NoCubesNetwork.currentServerHasNoCubes = doesCurrentServerHaveNoCubes();
		if (!NoCubesNetwork.currentServerHasNoCubes)
			NoCubesConfig.Server.terrainCollisionsEnabled = false;

//		final WorldClient world = minecraft.world;
//		// Every minute
//		if (world != null && world.getWorldTime() % 1200 == 0) {
//			BlockColorInfo.refresh();
//		}

//		// TODO: Temp!
//		{
//			if (tempDiscoverSmoothables.isPressed()) {
////				LOGGER.info("Discovering smoothables...");
//				player.sendMessage(new TextComponentString("Discovering smoothables..."));
//				final long startTime = System.nanoTime();
//				ConfigHelper.discoverDefaultTerrainSmoothable();
//				ConfigHelper.discoverDefaultLeavesSmoothable();
//				player.sendMessage(new TextComponentString("Finished discovering smoothables in " + (System.nanoTime() - startTime) + " nano seconds"));
////				LOGGER.info("Finished discovering smoothables in " + (System.nanoTime() - startTime) + " nano seconds");
//			}
//		}
	}

	private static boolean doesCurrentServerHaveNoCubes() {
		NetHandlerPlayClient connection = ClientUtil.getMinecraft().getConnection();
		if (connection == null)
			return false;

		NetworkManager networkManager = connection.getNetworkManager();
		if (networkManager == null)
			return false;

		NetworkDispatcher networkDispatcher = NetworkDispatcher.get(networkManager);
		return networkDispatcher != null && networkDispatcher.getModList().containsKey(MOD_ID);
	}

	@SubscribeEvent
	public static void onRenderTickEvent(final RenderTickEvent event) {
		if (!ModProfiler.isProfilingEnabled())
			return;

		Minecraft minecraft = ClientUtil.getMinecraft();
		if (minecraft.world == null || minecraft.player == null || minecraft.getRenderViewEntity() == null)
			return;

		Profiler profiler = minecraft.profiler;
		profiler.startSection("debugNoCubes");
		GlStateManager.pushMatrix();
		try {
			renderProfilers();
		} catch (Exception e) {
			LogManager.getLogger().error("Error Rendering Profilers.", e);
		}
		GlStateManager.popMatrix();
		profiler.endSection();
	}

	private static void renderProfilers() {
		Minecraft mc = ClientUtil.getMinecraft();

		synchronized (ModProfiler.PROFILERS) {
			int visibleIndex = 0;
			for (Map.Entry<Thread, ModProfiler> entry : ModProfiler.PROFILERS.entrySet()) {
				Thread thread = entry.getKey();
				ModProfiler profiler = entry.getValue();
				List<ModProfiler.Result> list = profiler.getProfilingData("");
				if (list.size() < 2) { // Continue of thread is idle
					continue;
				}
				final int offset = visibleIndex++;

				ModProfiler.Result profiler$result = list.remove(0);
				final int size = list.size();

				GlStateManager.clear(256);
				GlStateManager.matrixMode(5889);
				GlStateManager.enableColorMaterial();
				GlStateManager.loadIdentity();
				final int framebufferWidth = mc.displayWidth;
				final int framebufferHeight = mc.displayHeight;
				GlStateManager.ortho(0.0D, (double) framebufferWidth, (double) framebufferHeight, 0.0D, 1000.0D, 3000.0D);
				GlStateManager.matrixMode(5888);
				GlStateManager.loadIdentity();
				GlStateManager.scale(framebufferWidth / 1000F, framebufferWidth / 1000F, 1);
				GlStateManager.translate(5F, 5F, 0F);
				GlStateManager.translate(0.0F, 0.0F, -2000.0F);
				GlStateManager.glLineWidth(1.0F);

//				int i = 160;
//				int j = this.displayWidth - 160 - 10;
//				int k = this.displayHeight - 320;
//				int j = mc.displayWidth - (offset % 2) * 160;
//				int k = mc.displayHeight - (offset & 2) * 320;
//				final int cx = 176 + (offset) * 50;
//				final int cy = 80 + (offset & 2) * 320;
				final int cx = 160 + 320 * (offset % 3);
				final int cy = 20 + 80 + 320 * (offset / 3);

				GlStateManager.enableTexture2D();
				final FontRenderer fontRenderer = mc.fontRenderer;
				fontRenderer.drawStringWithShadow(thread.getName(), (float) (cx - 160), (float) (cy - 80 - 10 - 16), 0xFFFFFF);

				GlStateManager.disableTexture2D();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();

				GlStateManager.enableBlend();
				bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
				bufferbuilder.pos((double) ((float) cx - 176.0F), (double) ((float) cy - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
				bufferbuilder.pos((double) ((float) cx - 176.0F), (double) (cy + 320), 0.0D).color(200, 0, 0, 0).endVertex();
				bufferbuilder.pos((double) ((float) cx + 176.0F), (double) (cy + 320), 0.0D).color(200, 0, 0, 0).endVertex();
				bufferbuilder.pos((double) ((float) cx + 176.0F), (double) ((float) cy - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
				tessellator.draw();
				GlStateManager.disableBlend();
				double d0 = 0.0D;

				for (int i = 0; i < size; ++i) {
					final ModProfiler.Result profiler$result1 = list.get(i);
					final double usePercentage = profiler$result1.usePercentage;
					int i11 = MathHelper.floor(usePercentage / 4.0D) + 1;
					bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
					int j1 = profiler$result1.getColor();
					int k1 = j1 >> 16 & 255;
					int l1 = j1 >> 8 & 255;
					int i2 = j1 & 255;
					bufferbuilder.pos((double) cx, (double) cy, 0.0D).color(k1, l1, i2, 255).endVertex();

					for (int j2 = i11; j2 >= 0; --j2) {
						float f = (float) ((d0 + usePercentage * (double) j2 / (double) i11) * (Math.PI * 2D) / 100.0D);
						float f1 = MathHelper.sin(f) * 160.0F;
						float f2 = MathHelper.cos(f) * 160.0F * 0.5F;
						bufferbuilder.pos((double) ((float) cx + f1), (double) ((float) cy - f2), 0.0D).color(k1, l1, i2, 255).endVertex();
					}

					tessellator.draw();
					bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

					for (int i3 = i11; i3 >= 0; --i3) {
						float f3 = (float) ((d0 + usePercentage * (double) i3 / (double) i11) * (Math.PI * 2D) / 100.0D);
						float f4 = MathHelper.sin(f3) * 160.0F;
						float f5 = MathHelper.cos(f3) * 160.0F * 0.5F;
						bufferbuilder.pos((double) ((float) cx + f4), (double) ((float) cy - f5), 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
						bufferbuilder.pos((double) ((float) cx + f4), (double) ((float) cy - f5 + 10.0F), 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
					}

					tessellator.draw();
					d0 += usePercentage;
				}

				DecimalFormat decimalformat = new DecimalFormat("##0.00");
				GlStateManager.enableTexture2D();
				String str = "";

				final String profilerName = profiler$result.profilerName;
				if (!"unspecified".equals(profilerName)) {
					str = str + "[0] ";
				}

				if (profilerName.isEmpty()) {
					str = str + "ROOT ";
				} else {
					str = str + profilerName + ' ';
				}

				fontRenderer.drawStringWithShadow(str, (float) (cx - 160), (float) (cy - 80 - 16), 0xFFFFFF);
				str = decimalformat.format(profiler$result.totalUsePercentage) + "%";
				fontRenderer.drawStringWithShadow(str, (float) (cx + 160 - fontRenderer.getStringWidth(str)), (float) (cy - 80 - 16), 0xFFFFFF);

				for (int k2 = 0; k2 < size; ++k2) {
					ModProfiler.Result profiler$result2 = list.get(k2);
					StringBuilder stringbuilder = new StringBuilder();

					final String profilerName1 = profiler$result2.profilerName;
					if ("unspecified".equals(profilerName1)) {
						stringbuilder.append("[?] ");
					} else {
						stringbuilder.append("[").append(k2 + 1).append("] ");
					}

					String s1 = stringbuilder.append(profilerName1).toString();
					final int color = profiler$result2.getColor();
					fontRenderer.drawStringWithShadow(s1, (float) (cx - 160), (float) (cy + 80 + k2 * 8 + 20), color);
					s1 = decimalformat.format(profiler$result2.usePercentage) + "%";
					fontRenderer.drawStringWithShadow(s1, (float) (cx + 160 - 50 - fontRenderer.getStringWidth(s1)), (float) (cy + 80 + k2 * 8 + 20), color);
					s1 = decimalformat.format(profiler$result2.totalUsePercentage) + "%";
					fontRenderer.drawStringWithShadow(s1, (float) (cx + 160 - fontRenderer.getStringWidth(s1)), (float) (cy + 80 + k2 * 8 + 20), color);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onRenderWorldLastEvent(final RenderWorldLastEvent event) {
		Minecraft minecraft = ClientUtil.getMinecraft();
		GameSettings gameSettings = minecraft.gameSettings;
		if (!gameSettings.showDebugInfo || !gameSettings.showDebugProfilerChart || gameSettings.hideGUI)
			return;

		EntityPlayerSP player = minecraft.player;
		if (player == null)
			return;

		World world = player.world;
		if (world == null)
			return;

		float partialTicks = event.getPartialTicks();

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(Math.max(2.5F, (float) minecraft.displayWidth / 1920.0F * 2.5F));
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);
		GlStateManager.matrixMode(5889);
		GlStateManager.pushMatrix();
		GlStateManager.scale(1.0F, 1.0F, 0.999F);
		double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
		double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
		double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;
		// Draw nearby collisions
		for (final AxisAlignedBB voxelShape : world.getCollisionBoxes(player, new AxisAlignedBB(player.getPosition()).grow(3))) {
			RenderGlobal.drawSelectionBoundingBox(voxelShape.offset(-d0, -d1, -d2), 0, 1, 1, 0.4F);
		}
		// Draw player intersecting collisions
		for (final AxisAlignedBB voxelShape : world.getCollisionBoxes(player, new AxisAlignedBB(player.getPosition()))) {
			RenderGlobal.drawSelectionBoundingBox(voxelShape.offset(-d0, -d1, -d2), 1, 0, 0, 0.4F);
		}
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5888);
		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

	}

	@SubscribeEvent
	public static void drawBlockHighlightEvent(final DrawBlockHighlightEvent event) {
		if (!NoCubesConfig.Client.renderSmoothTerrain && !NoCubesConfig.Client.renderSmoothLeaves)
			return;

		EntityPlayer player = event.getPlayer();
		if (player == null)
			return;

		RayTraceResult rayTraceResult = event.getTarget();
		if ((rayTraceResult == null) || (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK))
			return;

		World world = player.world;
		if (world == null)
			return;

		float partialTicks = event.getPartialTicks();
		BlockPos lookingAtPos = rayTraceResult.getBlockPos();
		IBlockState blockState = world.getBlockState(lookingAtPos);
		if ((blockState.getMaterial() == Material.AIR) || !world.getWorldBorder().contains(lookingAtPos))
			return;

		final IsSmoothable isSmoothable;
		if (NoCubesConfig.Client.renderSmoothTerrain && TERRAIN.test(blockState)) {
			isSmoothable = TERRAIN;
//			meshGeneratorType = Config.terrainMeshGenerator;
			event.setCanceled(true);
		} else if (NoCubesConfig.Client.renderSmoothLeaves && LEAVES.test(blockState)) {
			isSmoothable = LEAVES;
//			meshGeneratorType = Config.leavesMeshGenerator;
			event.setCanceled(!NoCubesConfig.Client.renderSmoothAndVanillaLeaves);
		} else
			return;

		double renderX = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
		double renderY = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
		double renderZ = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();

		bufferBuilder.setTranslation(-renderX, -renderY, -renderZ);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(3.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		GlStateManager.color(0, 0, 0, 1);
		GlStateManager.color(1, 1, 1, 1);

		bufferBuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

//		SurfaceNets.generate(
//				lookingAtPos, lookingAtPos.add(1, 1, 1),
//				world, isSmoothable, // HIGHLIGHT,
//				(mask, pos) -> true,
//				(face, pos) -> {
		BlockPos startPos = lookingAtPos.add(-8, -8, -8);
		MarchingCubesRenderer.marchChunk(
			startPos,
				world,
				(face, pos) -> {
					Vec v0 = face.v0;
					Vec v1 = face.v1;
					Vec v2 = face.v2;
					Vec v3 = face.v3;

					// TEMP: I am dumb & lazy - fix this in SurfaceNets
					face.add(startPos.getX(), lookingAtPos.getY(), lookingAtPos.getZ());

					ColorParser.Color color = NoCubesConfig.Client.selectionBoxColor;
					int red = color.red;
					int blue = color.blue;
					int green = color.green;
					int alpha = color.alpha;

					// Start at v0. Transparent because we don't want to draw a line from wherever the previous vertex was
					bufferBuilder.pos(v0.x, v0.y, v0.z).color(0, 0, 0, 0.0F).endVertex();
					bufferBuilder.pos(v1.x, v1.y, v1.z).color(red, green, blue, alpha).endVertex();
					bufferBuilder.pos(v2.x, v2.y, v2.z).color(red, green, blue, alpha).endVertex();
					bufferBuilder.pos(v3.x, v3.y, v3.z).color(red, green, blue, alpha).endVertex();
					// End back at v0. Draw with alpha this time
					bufferBuilder.pos(v0.x, v0.y, v0.z).color(red, green, blue, alpha).endVertex();
					return true;
				}
		);

//		try (FaceList faces = MeshDispatcher.generateBlockMeshOffset(lookingAtPos, world, isSmoothable, meshGeneratorType)) {
//			for (int i = 0, facesSize = faces.size(); i < facesSize; i++) {
//				try (Face face = faces.get(i)) {
//					try (
//							Vec3 v0 = face.getVertex0();
//							Vec3 v1 = face.getVertex1();
//							Vec3 v2 = face.getVertex2();
//							Vec3 v3 = face.getVertex3()
//					) {
//						final double v0x = v0.x;
//						final double v0y = v0.y;
//						final double v0z = v0.z;
//						// Start at v0. Transparent because we don't want to draw a line from wherever the previous vertex was
//						bufferBuilder.pos(v0x, v0y, v0z).color(0, 0, 0, 0.0F).endVertex();
//						bufferBuilder.pos(v1.x, v1.y, v1.z).color(0, 0, 0, 0.4F).endVertex();
//						bufferBuilder.pos(v2.x, v2.y, v2.z).color(0, 0, 0, 0.4F).endVertex();
//						bufferBuilder.pos(v3.x, v3.y, v3.z).color(0, 0, 0, 0.4F).endVertex();
//						// End back at v0. Draw with alpha this time
//						bufferBuilder.pos(v0x, v0y, v0z).color(0, 0, 0, 0.4F).endVertex();
//					}
//				}
//			}
//		}

		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		bufferBuilder.setTranslation(0, 0, 0);

	}

	@SubscribeEvent
	public static void onPlayerSPPushOutOfBlocksEvent(final PlayerSPPushOutOfBlocksEvent event) {
		// TODO: Do this better (Do the same thing as StolenReposeCode.getDensity)
		if (NoCubesConfig.Server.terrainCollisionsEnabled)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onClientConnectedToServerEvent(final FMLNetworkEvent.ClientConnectedToServerEvent event) {
		if (!event.isLocal()) {
//			final NetworkManager manager = event.getManager();
//			if (manager == null) {
//				throw new NullPointerException("ARGH! Network Manager is null (" + "MANAGER" + ")");
//			}
//			if (NetworkDispatcher.get(manager).getConnectionType() != NetworkDispatcher.ConnectionType.MODDED) {
//				NoCubes.LOGGER.info("Connected to a vanilla server. Catching up missing behaviour.");
//				ConfigTracker.INSTANCE.loadDefaultServerConfigs();
//			}
			ConfigTracker.INSTANCE.loadDefaultServerConfigs();
		}
	}

}
