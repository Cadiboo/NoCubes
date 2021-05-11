package io.github.cadiboo.nocubes.client.optifine;

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
