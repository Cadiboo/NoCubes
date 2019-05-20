package io.github.cadiboo.nocubes.client;

import io.github.cadiboo.nocubes.NoCubes;
import io.github.cadiboo.nocubes.client.render.SmoothLightingBlockFluidRenderer;
import io.github.cadiboo.nocubes.util.Proxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import static io.github.cadiboo.nocubes.NoCubes.LOGGER;
import static io.github.cadiboo.nocubes.NoCubes.MOD_ID;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_I;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_N;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;

/**
 * The version of IProxy that gets injected into {@link NoCubes#PROXY} on a PHYSICAL CLIENT
 *
 * @author Cadiboo
 */
public final class ClientProxy implements Proxy {

	public static final KeyBinding toggleRenderSmoothTerrain = new KeyBinding(MOD_ID + ".key.toggleRenderSmoothTerrain", GLFW_KEY_O, "key.categories." + MOD_ID);
	public static final KeyBinding toggleRenderSmoothLeaves = new KeyBinding(MOD_ID + ".key.toggleRenderSmoothLeaves", GLFW_KEY_I, "key.categories." + MOD_ID);
	public static final KeyBinding toggleProfilers = new KeyBinding(MOD_ID + ".key.toggleProfilers", GLFW_KEY_P, "key.categories." + MOD_ID);
//	public static final KeyBinding tempDiscoverSmoothables = new KeyBinding(MOD_ID + ".key.tempDiscoverSmoothables", GLFW_KEY_J, "key.categories." + MOD_ID);

	public static final KeyBinding toggleTerrainSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleTerrainSmoothableBlockState", GLFW_KEY_N, "key.categories." + MOD_ID);
	public static final KeyBinding toggleLeavesSmoothableBlockState = new KeyBinding(MOD_ID + ".key.toggleLeavesSmoothableBlockState", GLFW_KEY_K, "key.categories." + MOD_ID);
	public static final KeyBinding tempToggleTerrainCollisions = new KeyBinding(MOD_ID + ".key.tempToggleTerrainCollisions", GLFW_KEY_C, "key.categories." + MOD_ID);

	public static SmoothLightingBlockFluidRenderer fluidRenderer;

	static {
		ClientRegistry.registerKeyBinding(toggleRenderSmoothTerrain);
		ClientRegistry.registerKeyBinding(toggleRenderSmoothLeaves);
		ClientRegistry.registerKeyBinding(toggleProfilers);
//		ClientRegistry.registerKeyBinding(tempDiscoverSmoothables);

		ClientRegistry.registerKeyBinding(toggleTerrainSmoothableBlockState);
		ClientRegistry.registerKeyBinding(toggleLeavesSmoothableBlockState);
		ClientRegistry.registerKeyBinding(tempToggleTerrainCollisions);
	}

	@Override
	public void markBlocksForUpdate(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, boolean updateImmediately) {

		final WorldRenderer worldRenderer = Minecraft.getInstance().worldRenderer;

		if (worldRenderer != null && worldRenderer.world != null && worldRenderer.viewFrustum != null) {
			worldRenderer.markBlocksForUpdate(minX, minY, minZ, maxX, maxY, maxZ, updateImmediately);
		}
	}

	public void replaceFluidRendererCauseImBored() {
		LOGGER.debug("Replacing fluid renderer");
		final BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
		final SmoothLightingBlockFluidRenderer smoothLightingBlockFluidRenderer = new SmoothLightingBlockFluidRenderer(blockRendererDispatcher.fluidRenderer);
		blockRendererDispatcher.fluidRenderer = smoothLightingBlockFluidRenderer;
		ClientProxy.fluidRenderer = smoothLightingBlockFluidRenderer;
		LOGGER.debug("Replaced fluid renderer");
	}

	@Override
	public void preloadClasses() {
		Proxy.super.preloadClasses();
		preloadClass("net.minecraft.client.renderer.chunk.RenderChunk", "RenderChunk");
		preloadClass("net.minecraft.client.renderer.BlockFluidRenderer", "BlockFluidRenderer");
	}

}
