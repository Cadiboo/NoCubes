package io.github.cadiboo.nocubes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;

public class ClientUtil {

	public static void reloadAllChunks(Minecraft minecraft) {
		WorldRenderer worldRenderer = minecraft.levelRenderer;
		if (worldRenderer != null)
			worldRenderer.allChanged();
	}

}
