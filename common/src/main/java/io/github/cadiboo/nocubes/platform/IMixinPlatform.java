package io.github.cadiboo.nocubes.platform;

import java.util.Set;

/**
 * Called from our Mixin plugin.
 */
public interface IMixinPlatform {
	Set<String> getLoadedModIds();
}
