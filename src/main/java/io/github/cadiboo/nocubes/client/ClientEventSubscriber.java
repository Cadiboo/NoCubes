package io.github.cadiboo.nocubes.client;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import io.github.cadiboo.nocubes.config.ConfigHelper;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import io.github.cadiboo.nocubes.network.C2SRequestSetTerrainCollisions;
import io.github.cadiboo.nocubes.network.C2SRequestSetTerrainSmoothable;
import io.github.cadiboo.nocubes.network.NoCubesNetwork;
import io.github.cadiboo.nocubes.util.IsSmoothable;
import io.github.cadiboo.nocubes.util.ModProfiler;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.GameSettings;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_N;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;

/**
 * Handles all the events that are fired on the FORGE event bus that should be handled
 * on the Client distribution.
 *
 * @author Cadiboo
 */
@Mod.EventBusSubscriber(modid = MOD_ID, value = CLIENT)
public final class ClientEventSubscriber {

	private static final String CATEGORY = "key.categories." + MOD_ID;

	private static final KeyBinding toggleRenderSmoothTerrain = new KeyBinding(MOD_ID + ".key.toggleRenderSmoothTerrain", GLFW_KEY_O, CATEGORY);
	//	private static final KeyBinding toggleRenderSmoothLeaves = new KeyBinding(MOD_ID + ".key.toggleRenderSmoothLeaves", GLFW_KEY_I, CATEGORY);
	private static final KeyBinding toggleProfilers = new KeyBinding(MOD_ID + ".key.toggleProfilers", GLFW_KEY_P, CATEGORY);

	private static final KeyBinding toggleTerrainSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleTerrainSmoothableBlockState", GLFW_KEY_N, CATEGORY);
	private static final KeyBinding toggleLeavesSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleLeavesSmoothableBlockState", GLFW_KEY_K, CATEGORY);
	private static final KeyBinding toggleTerrainCollisions = new KeyBinding(MOD_ID + ".key.toggleTerrainCollisions", GLFW_KEY_C, CATEGORY);

	//	public static SmoothLightingFluidBlockRenderer smoothLightingBlockFluidRenderer;
	private static VoxelShape cache = null;
	private static BlockPos lastPos = null;
	static {
		ClientRegistry.registerKeyBinding(toggleRenderSmoothTerrain);
//		ClientRegistry.registerKeyBinding(toggleRenderSmoothLeaves);
		ClientRegistry.registerKeyBinding(toggleProfilers);

		ClientRegistry.registerKeyBinding(toggleTerrainSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleLeavesSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleTerrainCollisions);
	}

	@SubscribeEvent
	public static void onClientTickEvent(final ClientTickEvent event) {

		if (event.phase != TickEvent.Phase.END)
			return;

		// Rendering
		{
			if (toggleRenderSmoothTerrain.isPressed()) {
				ConfigHelper.setRenderSmoothTerrain(!NoCubesConfig.Client.renderSmoothTerrain);
				ClientUtil.tryReloadRenderers();
			}
//			if (toggleRenderSmoothLeaves.isPressed()) {
//				ConfigHelper.setRenderSmoothLeaves(!NoCubesConfig.Client.renderSmoothLeaves);
//				ClientUtil.tryReloadRenderers();
//			}
		}

		if (toggleTerrainCollisions.isPressed())
			NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestSetTerrainCollisions(!NoCubesConfig.Server.terrainCollisions));

		// Smoothables
		SMOOTHABLES:
		{
			final boolean terrainPressed = toggleTerrainSmoothableBlockState.isPressed();
			final boolean leavesPressed = toggleLeavesSmoothableBlockState.isPressed();
			if (!terrainPressed && !leavesPressed)
				break SMOOTHABLES;

			final Minecraft minecraft = Minecraft.getInstance();
			final RayTraceResult objectMouseOver = minecraft.objectMouseOver;
			if (objectMouseOver == null || objectMouseOver.getType() != BLOCK)
				break SMOOTHABLES;

			final BlockPos blockPos = ((BlockRayTraceResult) objectMouseOver).getPos();
			final BlockState state = minecraft.world.getBlockState(blockPos);

			if (terrainPressed)
				setTerrainSmoothable(state, !state.nocubes_isTerrainSmoothable);
			if (leavesPressed)
				setLeavesSmoothable(state, !state.nocubes_isTerrainSmoothable);
		}

		if (toggleProfilers.isPressed())
			if (ModProfiler.isProfilingEnabled())
				ModProfiler.disableProfiling();
			else
				ModProfiler.enableProfiling();
	}

	private static void setTerrainSmoothable(final BlockState state, final boolean newSmoothability) {
		ConfigHelper.setTerrainSmoothablePreference(state, newSmoothability);
		// TODO: Handle what to do if we're on a vanilla server.
		NoCubesNetwork.CHANNEL.sendToServer(new C2SRequestSetTerrainSmoothable(state, newSmoothability));
	}

	private static void setLeavesSmoothable(final BlockState state, final boolean newSmoothability) {
//		ConfigHelper.setLeavesSmoothable(state, newSmoothability);
//		minecraft.getToastGui().add((BlockStateToast) new BlockStateToast.Leaves(state, newSmoothability));
//		if (NoCubesConfig.Client.renderSmoothLeaves)
//			ClientUtil.tryReloadRenderers();
	}

	@SubscribeEvent
	public static void onRenderTickEvent(final RenderTickEvent event) {
		if (!ModProfiler.isProfilingEnabled())
			return;

		final Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.world == null || minecraft.player == null || minecraft.getRenderViewEntity() == null)
			return;

		final IProfiler profiler = minecraft.getProfiler();
		profiler.startSection("debugNoCubes");
		RenderSystem.pushMatrix();
		try {
			renderProfilers();
		} catch (Exception e) {
			LogManager.getLogger("NoCubes Profile Renderer").error("Error Rendering Profilers.", e);
		}
		RenderSystem.popMatrix();
		profiler.endSection();
	}

	private static void renderProfilers() {
		final Minecraft mc = Minecraft.getInstance();

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

				ModProfiler.Result profileResult = list.remove(0);
				final int size = list.size();

				RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.IS_RUNNING_ON_MAC);
				RenderSystem.matrixMode(GL11.GL_PROJECTION);
				RenderSystem.enableColorMaterial();
				RenderSystem.loadIdentity();
				final MainWindow mainWindow = mc.func_228018_at_();
				final int framebufferWidth = mainWindow.getFramebufferWidth();
				final int framebufferHeight = mainWindow.getFramebufferHeight();
				RenderSystem.ortho(0.0D, framebufferWidth, framebufferHeight, 0.0D, 1000.0D, 3000.0D);
				RenderSystem.matrixMode(GL11.GL_MODELVIEW);
				RenderSystem.loadIdentity();
				RenderSystem.scalef(framebufferWidth / 1000F, framebufferWidth / 1000F, 1);
				RenderSystem.translatef(5F, 5F, 0F);
				RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
				RenderSystem.lineWidth(1.0F);

//				int i = 160;
//				int j = this.displayWidth - 160 - 10;
//				int k = this.displayHeight - 320;
//				int j = mc.displayWidth - (offset % 2) * 160;
//				int k = mc.displayHeight - (offset & 2) * 320;
//				final int cx = 176 + (offset) * 50;
//				final int cy = 80 + (offset & 2) * 320;
				final int cx = 160 + 320 * (offset % 3);
				final int cy = 20 + 80 + 320 * (offset / 3);

				RenderSystem.enableTexture();
				final FontRenderer fontRenderer = mc.fontRenderer;
				fontRenderer.drawStringWithShadow(thread.getName(), (float) (cx - 160), (float) (cy - 80 - 10 - 16), 0xFFFFFF);

				RenderSystem.disableTexture();
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder bufferbuilder = tessellator.getBuffer();

				RenderSystem.enableBlend();
				bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
//				bufferbuilder.pos((double) ((float) cx - 176.0F), (double) ((float) cy - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
//				bufferbuilder.pos((double) ((float) cx - 176.0F), (double) (cy + 320), 0.0D).color(200, 0, 0, 0).endVertex();
//				bufferbuilder.pos((double) ((float) cx + 176.0F), (double) (cy + 320), 0.0D).color(200, 0, 0, 0).endVertex();
//				bufferbuilder.pos((double) ((float) cx + 176.0F), (double) ((float) cy - 96.0F - 16.0F), 0.0D).color(200, 0, 0, 0).endVertex();
				tessellator.draw();
				RenderSystem.disableBlend();
				double d0 = 0.0D;

				for (int i = 0; i < size; ++i) {
					final ModProfiler.Result profileResult1 = list.get(i);
					final double usePercentage = profileResult1.usePercentage;
					int i11 = MathHelper.floor(usePercentage / 4.0D) + 1;
					bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
					int j1 = profileResult1.getColor();
					int k1 = j1 >> 16 & 255;
					int l1 = j1 >> 8 & 255;
					int i2 = j1 & 255;
//					bufferbuilder.pos((double) cx, (double) cy, 0.0D).color(k1, l1, i2, 255).endVertex();

					for (int j2 = i11; j2 >= 0; --j2) {
						float f = (float) ((d0 + usePercentage * (double) j2 / (double) i11) * (Math.PI * 2D) / 100.0D);
						float f1 = MathHelper.sin(f) * 160.0F;
						float f2 = MathHelper.cos(f) * 160.0F * 0.5F;
//						bufferbuilder.pos((double) ((float) cx + f1), (double) ((float) cy - f2), 0.0D).color(k1, l1, i2, 255).endVertex();
					}

					tessellator.draw();
					bufferbuilder.begin(5, DefaultVertexFormats.POSITION_COLOR);

					for (int i3 = i11; i3 >= 0; --i3) {
						float f3 = (float) ((d0 + usePercentage * (double) i3 / (double) i11) * (Math.PI * 2D) / 100.0D);
						float f4 = MathHelper.sin(f3) * 160.0F;
						float f5 = MathHelper.cos(f3) * 160.0F * 0.5F;
//						bufferbuilder.pos((double) ((float) cx + f4), (double) ((float) cy - f5), 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
//						bufferbuilder.pos((double) ((float) cx + f4), (double) ((float) cy - f5 + 10.0F), 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
					}

					tessellator.draw();
					d0 += usePercentage;
				}

				DecimalFormat decimalformat = new DecimalFormat("##0.00");
				RenderSystem.enableTexture();
				String str = "";

				final String profilerName = profileResult.profilerName;
				if (!"unspecified".equals(profilerName)) {
					str = str + "[0] ";
				}

				if (profilerName.isEmpty()) {
					str = str + "ROOT ";
				} else {
					str = str + profilerName + ' ';
				}

				fontRenderer.drawStringWithShadow(str, (float) (cx - 160), (float) (cy - 80 - 16), 0xFFFFFF);
				str = decimalformat.format(profileResult.totalUsePercentage) + "%";
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
		final Minecraft minecraft = Minecraft.getInstance();
		final GameSettings gameSettings = minecraft.gameSettings;
		if (!gameSettings.showDebugInfo || !gameSettings.showDebugProfilerChart || gameSettings.hideGUI)
			return;

		final ClientPlayerEntity player = minecraft.player;
		if (player == null)
			return;

		final World world = player.world;
		if (world == null)
			return;

		final ActiveRenderInfo activeRenderInfo = minecraft.gameRenderer.getActiveRenderInfo();

		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
		final MainWindow mainWindow = minecraft.func_228018_at_();
		RenderSystem.lineWidth(Math.max(2.5F, (float) mainWindow.getFramebufferWidth() / 1920.0F * 2.5F));
		RenderSystem.disableTexture();
		RenderSystem.depthMask(false);
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.scalef(1.0F, 1.0F, 0.999F);
		final Vec3d projectedView = activeRenderInfo.getProjectedView();
		double d0 = projectedView.getX();
		double d1 = projectedView.getY();
		double d2 = projectedView.getZ();

		// FIXME TEMP
		{
			Matrix4f matrix4f = event.getMatrixStack().func_227866_c_().func_227870_a_();
			final IVertexBuilder bufferBuilder = minecraft.func_228019_au_().func_228487_b_().getBuffer(RenderType.func_228659_m_());
			final BlockPos playerPos = new BlockPos(player);
			if (cache == null || !Objects.equals(lastPos, playerPos)) {
				lastPos = playerPos;
				VoxelShape shape = VoxelShapes.empty();
				for (final BlockPos blockPos : BlockPos.getAllInBoxMutable(playerPos.add(-8, -8, -8), playerPos.add(7, 7, 7))) {
					final BlockState blockState = world.getBlockState(blockPos);
					if (IsSmoothable.TERRAIN_SMOOTHABLE.test(blockState))
						shape = VoxelShapes.combine(shape, blockState.getShape(world, blockPos).withOffset(blockPos.getX(), blockPos.getY(), blockPos.getZ()), IBooleanFunction.OR);
				}
				cache = shape.simplify();
			}
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//	        WorldRenderer.func_228445_b_(event.getMatrixStack(), bufferBuilder, shape, -d0, -d1, -d2, 0.0F, 1.0F, 1.0F, 0.4F);
			drawShape(matrix4f, bufferBuilder, cache, 0.0F, 1.0F, 1.0F, 1.0F, -d0, -d1, -d2);
		}

		// Draw nearby collisions
//		for (final VoxelShape voxelShape : world.getCollisionShapes(player, new AxisAlignedBB(player.getPosition()).grow(3)).collect(Collectors.toList())) {
//			WorldRenderer.drawShape(voxelShape, -d0, -d1, -d2, 0, 1, 1, 0.4F);
//		}
		// Draw player intersecting collisions
//		for (final VoxelShape voxelShape : world.getCollisionShapes(player, new AxisAlignedBB(player.getPosition())).collect(Collectors.toList())) {
//			WorldRenderer.drawShape(voxelShape, -d0, -d1, -d2, 1, 0, 0, 0.4F);
//		}
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.depthMask(true);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	private static void drawShape(final Matrix4f matrix4f, final IVertexBuilder bufferBuilder, final VoxelShape shape, final float red, final float green, final float blue, final float alpha, final double x, final double y, final double z) {
		shape.forEachEdge((x0, y0, z0, x1, y1, z1) -> {
			bufferBuilder.func_227888_a_(matrix4f, (float) (x0 + x), (float) (y0 + y), (float) (z0 + z)).func_227885_a_(red, green, blue, alpha).endVertex();
			bufferBuilder.func_227888_a_(matrix4f, (float) (x1 + x), (float) (y1 + y), (float) (z1 + z)).func_227885_a_(red, green, blue, alpha).endVertex();
		});
	}

	@SubscribeEvent
	public static void drawBlockHighlightEvent(final DrawHighlightEvent event) {
		if (!NoCubesConfig.Client.renderSmoothTerrain /*&& !NoCubesConfig.Client.renderSmoothLeaves*/)
			return;

		final ClientPlayerEntity player = Minecraft.getInstance().player;
		if (player == null)
			return;

		final World world = player.world;
		if (world == null)
			return;

		final RayTraceResult rayTraceResult = event.getTarget();
		if (rayTraceResult == null || rayTraceResult.getType() != RayTraceResult.Type.BLOCK)
			return;

		BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;

		final BlockPos pos = blockRayTraceResult.getPos();
		final BlockState blockState = world.getBlockState(pos);
		if (blockState.getMaterial() == Material.AIR || !world.getWorldBorder().contains(pos))
			return;

//		final IsSmoothable isSmoothable;
//		final MeshGeneratorType meshGeneratorType;
//		if (Config.renderSmoothTerrain && blockState.nocubes_isTerrainSmoothable) {
//			isSmoothable = TERRAIN_SMOOTHABLE;
//			meshGeneratorType = Config.terrainMeshGenerator;
//			event.setCanceled(true);
//		} else if (Config.renderSmoothLeaves && blockState.nocubes_isLeavesSmoothable) {
//			isSmoothable = LEAVES_SMOOTHABLE;
//			meshGeneratorType = Config.leavesMeshGenerator;
//			event.setCanceled(true);
//		} else
//			return;
//
//		final Tessellator tessellator = Tessellator.getInstance();
//		final BufferBuilder bufferbuilder = tessellator.getBuffer();
//
//		bufferbuilder.setTranslation(
//				-TileEntityRendererDispatcher.staticPlayerX,
//				-TileEntityRendererDispatcher.staticPlayerY,
//				-TileEntityRendererDispatcher.staticPlayerZ
//		);
//
//		RenderSystem.enableBlend();
//		RenderSystem.blendFuncSeparate(RenderSystem.SourceFactor.SRC_ALPHA, RenderSystem.DestFactor.ONE_MINUS_SRC_ALPHA, RenderSystem.SourceFactor.ONE, RenderSystem.DestFactor.ZERO);
//		RenderSystem.lineWidth(3.0F);
//		RenderSystem.disableTexture();
//		RenderSystem.depthMask(true);
//
//		RenderSystem.color4f(0, 0, 0, 1);
//		RenderSystem.color4f(1, 1, 1, 1);
//
//		try (FaceList faces = MeshDispatcher.generateBlockMeshOffset(pos, world, isSmoothable, meshGeneratorType)) {
//			for (int i = 0, facesSize = faces.size(); i < facesSize; i++) {
//				try (Face face = faces.get(i)) {
//					try (
//							Vec3 v0 = face.getVertex0();
//							Vec3 v1 = face.getVertex1();
//							Vec3 v2 = face.getVertex2();
//							Vec3 v3 = face.getVertex3()
//					) {
//						bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
//						bufferbuilder.pos(v0.x, v0.y, v0.z).color(0, 0, 0, 0.4F).endVertex();
//						bufferbuilder.pos(v1.x, v1.y, v1.z).color(0, 0, 0, 0.4F).endVertex();
//						bufferbuilder.pos(v2.x, v2.y, v2.z).color(0, 0, 0, 0.4F).endVertex();
//						bufferbuilder.pos(v3.x, v3.y, v3.z).color(0, 0, 0, 0.4F).endVertex();
//						tessellator.draw();
//					}
//				}
//			}
//		}
//
//		RenderSystem.depthMask(true);
//		RenderSystem.enableTexture();
//		RenderSystem.disableBlend();
//
//		bufferbuilder.setTranslation(0, 0, 0);
	}

	@SubscribeEvent
	public static void onPlayerSPPushOutOfBlocksEvent(final PlayerSPPushOutOfBlocksEvent event) {
		// TODO: Do this better (Do the same thing as StolenReposeCode.getDensity)
//		if (NoCubesConfig.Server.terrainCollisions) {
//			event.setCanceled(true);
//		}
	}

}
