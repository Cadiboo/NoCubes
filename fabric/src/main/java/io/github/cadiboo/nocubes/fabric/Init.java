package io.github.cadiboo.nocubes.fabric;

import io.github.cadiboo.nocubes.config.ColorParser;
import io.github.cadiboo.nocubes.config.NoCubesConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.BuiltInRegistries;

public class Init implements ModInitializer {

	@Override
	public void onInitialize() {
		// TODO: REMOVE
		{
			NoCubesConfig.Common.debugEnabled = true;
			NoCubesConfig.Client.render = true;
			NoCubesConfig.Client.renderSelectionBox = true;
			NoCubesConfig.Client.selectionBoxColor = new ColorParser.Color(0, 255, 255, 255).toRenderableColor();
			NoCubesConfig.Server.mesher = NoCubesConfig.Server.MesherType.SurfaceNets.instance;
			NoCubesConfig.Server.collisionsEnabled = true;
			NoCubesConfig.Server.tempMobCollisionsDisabled = true;
			NoCubesConfig.Server.extendFluidsRange = 3;
			NoCubesConfig.Smoothables.recomputeInMemoryLookup(BuiltInRegistries.BLOCK.stream(), java.util.Collections.emptyList(), java.util.Collections.emptyList(), true);
		}
	}
}
