package io.github.cadiboo.nocubes.client.optifine;

import org.apache.logging.log4j.LogManager;

/**
 * This compatibility system isn't perfect and leads to a lot of code duplication
 * However, it's a lot better than the previous system.
 */
public class OptiFineCompatibility {

	private static final OptiFineProxy[] PROXIES = {
		new HD_U_H5(),
		new HD_U_G8(),
	};
	private static volatile OptiFineProxy instance;

	public static OptiFineProxy proxy() {
		if (instance == null) {
			synchronized (OptiFineCompatibility.class) {
				if (instance == null) {
					instance = createProxy();
					LogManager.getLogger("NoCubes OptiFine Compatibility").info("Using {} proxy", instance.getClass().getSimpleName());
				}
			}
		}
		return instance;
	}

	private static OptiFineProxy createProxy() {
		for (OptiFineProxy proxy : PROXIES) {
			if (proxy.initialisedAndUsable())
				return proxy;
		}
		return new Dummy();
	}
}
