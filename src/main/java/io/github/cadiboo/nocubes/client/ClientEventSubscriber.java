package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.gui.toast.BlockStateToast;
import io.github.cadiboo.nocubes.client.render.RenderDispatcher;
import io.github.cadiboo.nocubes.config.ModConfig;
import io.github.cadiboo.nocubes.util.ModProfiler;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkBlockEvent;
import io.github.cadiboo.renderchunkrebuildchunkhooks.event.RebuildChunkPreEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;

import static io.github.cadiboo.nocubes.util.ModReference.MOD_ID;
import static io.github.cadiboo.nocubes.util.ModReference.MOD_NAME;
import static io.github.cadiboo.nocubes.util.ModReference.VERSION;
import static net.minecraft.util.math.RayTraceResult.Type.BLOCK;
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

	@SubscribeEvent
	public static void onRebuildChunkPreEvent(final RebuildChunkPreEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		RenderDispatcher.renderChunk(event);
	}

	@SubscribeEvent
	public static void onRebuildChunkBlockEvent(final RebuildChunkBlockEvent event) {
		if (!NoCubes.isEnabled()) {
			return;
		}
		RenderDispatcher.renderBlock(event);
	}

	@SubscribeEvent
	public static void onClientTickEvent(final ClientTickEvent event) {

		if (false) {
			ObjectPoolingProfiler.onTick();
		}

		final boolean toggleEnabledPressed = ClientProxy.toggleEnabled.isPressed();
		final boolean toggleSmoothableBlockstatePressed = ClientProxy.toggleSmoothableBlockstate.isPressed();
		final boolean toggleProfilersPressed = ClientProxy.toggleProfilers.isPressed();
		if (toggleEnabledPressed || toggleSmoothableBlockstatePressed || toggleProfilersPressed) {
			if (toggleEnabledPressed) {
				ModConfig.isEnabled = !ModConfig.isEnabled;
				fireConfigChangedEvent();
				ClientUtil.tryReloadRenderers();
				return;
			}
			if (toggleSmoothableBlockstatePressed) {
				if (addBlockstateToSmoothable()) {
					if (NoCubes.isEnabled()) {
						ClientUtil.tryReloadRenderers();
					}
					fireConfigChangedEvent();
					return;
				}
			}
			if (toggleProfilersPressed) {
				if (NoCubes.profilingEnabled) {
					NoCubes.disableProfiling();
				} else {
					NoCubes.enableProfiling();
				}
			}
		}
	}

	private static boolean addBlockstateToSmoothable() {
		final Minecraft minecraft = Minecraft.getMinecraft();
		final RayTraceResult objectMouseOver = minecraft.objectMouseOver;
		if (objectMouseOver.typeOfHit != BLOCK) {
			return false;
		}

		final IBlockState state = minecraft.world.getBlockState(objectMouseOver.getBlockPos());

		final BlockStateToast toast;
		final HashSet<IBlockState> smoothableBlockStatesCache = ModConfig.getSmoothableBlockStatesCache();
		if (!smoothableBlockStatesCache.remove(state)) {
			smoothableBlockStatesCache.add(state);
			toast = new BlockStateToast.Add(state);
		} else {
			toast = new BlockStateToast.Remove(state);
		}
		minecraft.getToastGui().add(toast);

		syncSmoothableBlockstatesWithCache();
		return true;
	}

	// Copied from GuiConfig
	private static void fireConfigChangedEvent() {
		if (Loader.isModLoaded(MOD_ID)) {
			ConfigChangedEvent configChangedEvent = new ConfigChangedEvent.OnConfigChangedEvent(MOD_ID, null, true, false);
			MinecraftForge.EVENT_BUS.post(configChangedEvent);
			if (!configChangedEvent.getResult().equals(Event.Result.DENY)) {
				MinecraftForge.EVENT_BUS.post(new ConfigChangedEvent.PostConfigChangedEvent(MOD_ID, null, true, false));
			}
		}
	}

	private static void syncSmoothableBlockstatesWithCache() {
		ModConfig.smoothableBlockStates = ModConfig.getSmoothableBlockStatesCache().stream()
				.map(IBlockState::toString)
				.toArray(String[]::new);
	}

	//	@SubscribeEvent
//	public static void onForgeRenderChunkChunkCacheThingFor1_13(final EventThatDoesntExistIn1_12_2 event) {
//		if (!NoCubes.isEnabled()) {
//			return;
//		}
//	}

	@SubscribeEvent
	public static void onEntityJoinWorldEvent(final EntityJoinWorldEvent event) {
		final Entity entity = event.getEntity();
		if (entity instanceof EntityPlayerSP) {
			final EntityPlayerSP player = (EntityPlayerSP) entity;
			player.sendMessage(net.minecraftforge.common.ForgeHooks.newChatWithLinks(MOD_NAME + " " + VERSION + ": The code for Collisions is 90% copied/ported/stolen from the Repose Mod's code. More accurate collisions with less exactly similar code are being worked on. Until then, enjoy and go give Repose some love at https://www.minecraftforum.net/forums/mapping-and-modding-java-edition/minecraft-mods/2076319-repose-walkable-soil-slopes-give-your-spacebar-a/"));
		}
	}

	@SubscribeEvent
	public static void onRenderTickEvent(final RenderTickEvent event) {
		if (!NoCubes.profilingEnabled) {
			return;
		}

		final Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.world == null || minecraft.player == null || minecraft.getRenderViewEntity() == null) {
			return;
		}

		minecraft.profiler.startSection("debugNoCubes");
		GlStateManager.pushMatrix();
		try {
			renderProfilers();
		} catch (Exception e) {
			LogManager.getLogger(MOD_NAME + " Profile Renderer").error("Error Rendering Profilers.", e);
		}
		GlStateManager.popMatrix();
		minecraft.profiler.endSection();
	}

	protected static void renderProfilers() {
		final Minecraft mc = Minecraft.getMinecraft();

		for (int profilerIndex = 0; profilerIndex < NoCubes.PROFILERS.size(); profilerIndex++) {
			final ModProfiler profiler = NoCubes.PROFILERS.get(profilerIndex);
			List<Profiler.Result> list = profiler.getProfilingData("");
			Profiler.Result profiler$result = list.remove(0);
			GlStateManager.clear(256);
			GlStateManager.matrixMode(5889);
			GlStateManager.enableColorMaterial();
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0D, (double) mc.displayWidth, (double) mc.displayHeight, 0.0D, 1000.0D, 3000.0D);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0.0F, 0.0F, -2000.0F);
			GlStateManager.glLineWidth(1.0F);
			GlStateManager.disableTexture2D();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferbuilder = tessellator.getBuffer();
//			int i = 160;
//			int j = this.displayWidth - 160 - 10;
//			int k = this.displayHeight - 320;
//			int j = mc.displayWidth - (profilerIndex % 2) * 160;
//			int k = mc.displayHeight - (profilerIndex & 2) * 320;
//			final int cx = 176 + (profilerIndex) * 50;
//			final int cy = 80 + (profilerIndex & 2) * 320;
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

			int l2 = 16777215;
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
