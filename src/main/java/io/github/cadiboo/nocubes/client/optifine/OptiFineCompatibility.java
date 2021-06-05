package io.github.cadiboo.nocubes.client.optifine;

import org.apache.logging.log4j.LogManager;

public class OptiFineCompatibility {

	private static final OptiFineProxy[] PROXIES = {
		new HD_U_G8(),
	};
	private static OptiFineProxy instance;

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
