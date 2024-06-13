package com.example.examplemod.platform;

import com.example.examplemod.platform.services.IPlatform;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import org.beryx.awt.color.ColorFactory;

import java.awt.*;

public class ForgePlatform implements IPlatform {

	@Override
	public String getPlatformName() {
		return "Forge";
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}

	@Override
	public Color parseColor(String color) {
		ChunkVertexEncoder.Vertex.uninitializedQuad();
		try {
			final java.awt.Color parsed = ColorFactory.valueOf(color);
			return new Color(parsed.getRed(), parsed.getGreen(), parsed.getBlue(), parsed.getAlpha());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Unable to parse color '" + color + "'", e);
		}
	}
}