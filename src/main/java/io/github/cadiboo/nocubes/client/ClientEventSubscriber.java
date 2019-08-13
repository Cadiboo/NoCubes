package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.gui.toast.BlockStateToast;
import io.github.cadiboo.nocubes.client.render.SmoothLightingFluidBlockRenderer;
import io.github.cadiboo.nocubes.config.Config;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.ConfigTracker;
import io.github.cadiboo.nocubes.mesh.MeshDispatcher;
import io.github.cadiboo.nocubes.mesh.MeshGeneratorType;
import io.github.cadiboo.nocubes.network.C2SRequestAddTerrainSmoothable;
import io.github.cadiboo.nocubes.network.C2SRequestDisableTerrainCollisions;
import io.github.cadiboo.nocubes.network.C2SRequestEnableTerrainCollisions;
import io.github.cadiboo.nocubes.network.C2SRequestRemoveTerrainSmoothable;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.nocubes.util.pooled.Face;
import io.github.cadiboo.nocubes.util.pooled.FaceList;
import io.github.cadiboo.nocubes.util.pooled.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
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
import net.minecraftforge.fml.client.registry.ClientRegistry;
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
import static io.github.cadiboo.nocubes.util.IsSmoothable.LEAVES_SMOOTHABLE;
import static io.github.cadiboo.nocubes.util.IsSmoothable.TERRAIN_SMOOTHABLE;
import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;
import static net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import static net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import static net.minecraftforge.fml.relauncher.Side.CLIENT;
import static org.lwjgl.input.Keyboard.KEY_C;
import static org.lwjgl.input.Keyboard.KEY_I;
import static org.lwjgl.input.Keyboard.KEY_K;
import static org.lwjgl.input.Keyboard.KEY_N;
import static org.lwjgl.input.Keyboard.KEY_O;
import static org.lwjgl.input.Keyboard.KEY_P;

/**
 * Subscribe to events that should be handled on the PHYSICAL CLIENT in this class
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	private static final String CATEGORY = "key.categories." + MOD_ID;

	private static final KeyBinding toggleRenderSmoothTerrain = new KeyBinding(MOD_ID + ".key.toggleRenderSmoothTerrain", KEY_O, CATEGORY);
	private static final KeyBinding toggleRenderSmoothLeaves = new KeyBinding(MOD_ID + ".key.toggleRenderSmoothLeaves", KEY_I, CATEGORY);
	private static final KeyBinding toggleProfilers = new KeyBinding(MOD_ID + ".key.toggleProfilers", KEY_P, CATEGORY);
//	private static final KeyBinding tempDiscoverSmoothables = new KeyBinding(MOD_ID + ".key.tempDiscoverSmoothables", KEY_J, CATEGORY);

	private static final KeyBinding toggleTerrainSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleTerrainSmoothableBlockState", KEY_N, CATEGORY);
	private static final KeyBinding toggleLeavesSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleLeavesSmoothableBlockState", KEY_K, CATEGORY);
	private static final KeyBinding toggleTerrainCollisions = new KeyBinding(MOD_ID + ".key.toggleTerrainCollisions", KEY_C, CATEGORY);

	public static SmoothLightingFluidBlockRenderer smoothLightingBlockFluidRenderer;

	static {
		ClientRegistry.registerKeyBinding(toggleRenderSmoothTerrain);
		ClientRegistry.registerKeyBinding(toggleRenderSmoothLeaves);
		ClientRegistry.registerKeyBinding(toggleProfilers);
//		ClientRegistry.registerKeyBinding(tempDiscoverSmoothables);

		ClientRegistry.registerKeyBinding(toggleTerrainSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleLeavesSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleTerrainCollisions);
	}

	@SubscribeEvent
	public static void onClientTickEvent(final ClientTickEvent event) {

		if (event.phase != TickEvent.Phase.END) return;

		final Minecraft minecraft = Minecraft.getMinecraft();

		final NetHandlerPlayClient connection = minecraft.getConnection();
		if (connection != null) {
			final NetworkManager networkManager = connection.getNetworkManager();
			if (networkManager != null) {
				final NetworkDispatcher networkDispatcher = NetworkDispatcher.get(networkManager);
				if (networkDispatcher != null && networkDispatcher.getConnectionType() != NetworkDispatcher.ConnectionType.MODDED) {
					Config.terrainCollisions = false;
				}
			}
		}

		final WorldClient world = minecraft.world;
		// Every minute
		if (world != null && world.getWorldTime() % 1200 == 0) {
			BlockColorInfo.refresh();
		}

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

		// Rendering
		{
			if (toggleRenderSmoothTerrain.isPressed()) {
				final boolean newRenderSmoothTerrain = !Config.renderSmoothTerrain;
				ConfigHelper.setRenderSmoothTerrain(newRenderSmoothTerrain);
				// Config saving is async so set it now
				Config.renderSmoothTerrain = newRenderSmoothTerrain;
				ClientUtil.tryReloadRenderers();
				return;
			}
			if (toggleRenderSmoothLeaves.isPressed()) {
				final boolean newRenderSmoothLeaves = !Config.renderSmoothLeaves;
				ConfigHelper.setRenderSmoothLeaves(newRenderSmoothLeaves);
				// Config saving is async so set it now
				Config.renderSmoothLeaves = newRenderSmoothLeaves;
				ClientUtil.tryReloadRenderers();
				return;
			}
		}

		// Collisions
		{
			if (toggleTerrainCollisions.isPressed()) {
				if (Config.terrainCollisions) {
					NoCubes.CHANNEL.sendToServer(new C2SRequestDisableTerrainCollisions());
				} else {
					NoCubes.CHANNEL.sendToServer(new C2SRequestEnableTerrainCollisions());
				}
			}
		}

		// Smoothables
		SMOOTHABLES:
		{
			final boolean terrainPressed = toggleTerrainSmoothableBlockState.isPressed();
			final boolean leavesPressed = toggleLeavesSmoothableBlockState.isPressed();
			if (!terrainPressed && !leavesPressed) {
				break SMOOTHABLES;
			}

			final RayTraceResult objectMouseOver = minecraft.objectMouseOver;
			if (objectMouseOver.typeOfHit != BLOCK) {
				break SMOOTHABLES;
			}

//			final BlockPos blockPos = ((BlockRayTraceResult) objectMouseOver).getPos();
			final BlockPos blockPos = objectMouseOver.getBlockPos();
			final IBlockState state = minecraft.world.getBlockState(blockPos);

			if (terrainPressed) {
				if (state.nocubes_isTerrainSmoothable()) {
					NoCubes.CHANNEL.sendToServer(new C2SRequestRemoveTerrainSmoothable(Block.getStateId(state)));
				} else {
					NoCubes.CHANNEL.sendToServer(new C2SRequestAddTerrainSmoothable(Block.getStateId(state)));
				}
			}
			if (leavesPressed) {
				final BlockStateToast toast;
				if (!state.nocubes_isLeavesSmoothable()) {
					ConfigHelper.addLeavesSmoothable(state);
					toast = new BlockStateToast.AddLeaves(state, blockPos);
				} else {
					ConfigHelper.removeLeavesSmoothable(state);
					toast = new BlockStateToast.RemoveLeaves(state, blockPos);
				}
				minecraft.getToastGui().add(toast);

				if (Config.renderSmoothLeaves) {
					ClientUtil.tryReloadRenderers();
				}
			}
		}

		if (toggleProfilers.isPressed()) {
			if (ModProfiler.isProfilingEnabled()) {
				ModProfiler.disableProfiling();
			} else {
				ModProfiler.enableProfiling();
			}
		}
	}

	@SubscribeEvent
	public static void onRenderTickEvent(final RenderTickEvent event) {

		if (!ModProfiler.isProfilingEnabled()) {
			return;
		}

		final Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.world == null || minecraft.player == null || minecraft.getRenderViewEntity() == null) {
			return;
		}

		final Profiler profiler = minecraft.profiler;
		profiler.startSection("debugNoCubes");
		GlStateManager.pushMatrix();
		try {
			renderProfilers();
		} catch (Exception e) {
			LogManager.getLogger("NoCubes Profile Renderer").error("Error Rendering Profilers.", e);
		}
		GlStateManager.popMatrix();
		profiler.endSection();
	}

	private static void renderProfilers() {
		final Minecraft mc = Minecraft.getMinecraft();

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

		final Minecraft minecraft = Minecraft.getMinecraft();

		final GameSettings gameSettings = minecraft.gameSettings;
		if (!gameSettings.showDebugInfo || !gameSettings.showDebugProfilerChart || gameSettings.hideGUI) {
			return;
		}

		final EntityPlayerSP player = minecraft.player;
		if (player == null) {
			return;
		}

		final World world = player.world;
		if (world == null) {
			return;
		}

		final float partialTicks = event.getPartialTicks();

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

		if (!Config.renderSmoothTerrain && !Config.renderSmoothLeaves) {
			return;
		}

		final EntityPlayer player = event.getPlayer();
		if (player == null) {
			return;
		}

		final RayTraceResult rayTraceResult = event.getTarget();
		if ((rayTraceResult == null) || (rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK)) {
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

		final IsSmoothable isSmoothable;
		final MeshGeneratorType meshGeneratorType;
		if (Config.renderSmoothTerrain && blockState.nocubes_isTerrainSmoothable()) {
			isSmoothable = TERRAIN_SMOOTHABLE;
			meshGeneratorType = Config.terrainMeshGenerator;
			event.setCanceled(true);
		} else if (Config.renderSmoothLeaves && blockState.nocubes_isLeavesSmoothable()) {
			isSmoothable = LEAVES_SMOOTHABLE;
			meshGeneratorType = Config.leavesMeshGenerator;
			event.setCanceled(true);
		} else {
			return;
		}

		final double renderX = player.lastTickPosX + ((player.posX - player.lastTickPosX) * partialTicks);
		final double renderY = player.lastTickPosY + ((player.posY - player.lastTickPosY) * partialTicks);
		final double renderZ = player.lastTickPosZ + ((player.posZ - player.lastTickPosZ) * partialTicks);

		final Tessellator tessellator = Tessellator.getInstance();
		final BufferBuilder bufferbuilder = tessellator.getBuffer();

		bufferbuilder.setTranslation(-renderX, -renderY, -renderZ);

		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(3.0F);
		GlStateManager.disableTexture2D();
		GlStateManager.depthMask(false);

		GlStateManager.color(0, 0, 0, 1);
		GlStateManager.color(1, 1, 1, 1);

		bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);

		try (FaceList faces = MeshDispatcher.generateBlockMeshOffset(pos, world, isSmoothable, meshGeneratorType)) {
			for (int i = 0, facesSize = faces.size(); i < facesSize; i++) {
				try (Face face = faces.get(i)) {
					try (
							Vec3 v0 = face.getVertex0();
							Vec3 v1 = face.getVertex1();
							Vec3 v2 = face.getVertex2();
							Vec3 v3 = face.getVertex3()
					) {
						final double v0x = v0.x;
						final double v0y = v0.y;
						final double v0z = v0.z;
						// Start at v0. Transparent because we don't want to draw a line from wherever the previous vertex was
						bufferbuilder.pos(v0x, v0y, v0z).color(0, 0, 0, 0.0F).endVertex();
						bufferbuilder.pos(v1.x, v1.y, v1.z).color(0, 0, 0, 0.4F).endVertex();
						bufferbuilder.pos(v2.x, v2.y, v2.z).color(0, 0, 0, 0.4F).endVertex();
						bufferbuilder.pos(v3.x, v3.y, v3.z).color(0, 0, 0, 0.4F).endVertex();
						// End back at v0. Draw with alpha this time
						bufferbuilder.pos(v0x, v0y, v0z).color(0, 0, 0, 0.4F).endVertex();
					}
				}
			}
		}

		tessellator.draw();

		GlStateManager.depthMask(true);
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();

		bufferbuilder.setTranslation(0, 0, 0);

	}

	@SubscribeEvent
	public static void onPlayerSPPushOutOfBlocksEvent(final PlayerSPPushOutOfBlocksEvent event) {
		// TODO: Do this better (Do the same thing as StolenReposeCode.getDensity)
		if (Config.terrainCollisions) {
			event.setCanceled(true);
		}
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
