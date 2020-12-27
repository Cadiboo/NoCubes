package io.github.cadiboo.nocubes.future;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ModContainer;

import java.util.EnumMap;
import java.util.Optional;
import java.util.function.Consumer;

public final class FutureModContainer {
	final ModContainer container;
	final EnumMap<ModConfig.Type, ModConfig> configs = new EnumMap<>(ModConfig.Type.class);
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	Optional<Consumer<ModConfig.ModConfigEvent>> configHandler = Optional.empty();

	public FutureModContainer(ModContainer container) {
		this.container = container;
		configHandler = Optional.of(MinecraftForge.EVENT_BUS::post);
	}

	public String getModId() {
		return container.getModId();
	}

	public void addConfig(final ModConfig modConfig) {
		configs.put(modConfig.getType(), modConfig);
	}

	public void dispatchConfigEvent(ModConfig.ModConfigEvent event) {
		configHandler.ifPresent(configHandler->configHandler.accept(event));
	}
}
