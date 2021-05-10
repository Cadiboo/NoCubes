package io.github.cadiboo.nocubes.client.optifine;

public class OptiFineCompatibility {
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
		return new Dummy();
	}
}
