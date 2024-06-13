package io.github.cadiboo.nocubes.platform;

import java.util.ServiceLoader;

public class PlatformLoader {
	public static <T> T load(Class<T> type) {
		return ServiceLoader.load(type)
			.findFirst()
			.orElseThrow(() -> new NullPointerException("Failed to load service for " + type.getName()));
	}
}
