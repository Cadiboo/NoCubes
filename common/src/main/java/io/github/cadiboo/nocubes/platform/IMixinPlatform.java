package io.github.cadiboo.nocubes.platform;

import java.util.Set;

/**
 * Called from our Mixin plugin.
 * See also {@link IPlatform}
 */
public interface IMixinPlatform {
	Set<String> getLoadedModIds();
	void onLoad();
}
